#!/usr/bin/env bash
# Run ONLY the eight NEW D-127 Stage 7 matrix methods on an already
# installed, user-approved D-127. No main APK installation or data deletion.
# Never rerun the older six or Stable 9's 285 tests.
set -euo pipefail
if [[ "$(printenv HOME || true)" == "" ]]; then echo "STOP: HOME unavailable"; exit 2; fi
downloads="$HOME/storage/downloads"
apk="$downloads/Atelier-Time-D127-Stage7-Matrix-Tests.apk"
manifest="$downloads/Atelier-Time-D127-Stage7-Matrix-Manifest.txt"
log="$downloads/Atelier-Time-D127-Stage7-Matrix-Results.log"
package="com.ateliertime.android"
runner="com.ateliertime.android.test/androidx.test.runner.AndroidJUnitRunner"
test_class="com.ateliertime.android.AccessibilityStage7MatrixTest"

if [[ "$#" -ne 1 || "$1" != "--run" ]]; then
  echo "PREVIEW. Run from Termux after Samsung-only test APK build:"
  echo "bash ~/storage/downloads/Atelier-Time-D127-Stage7-MatrixQA.sh --run"
  exit 2
fi
for file in "$apk" "$manifest"; do
  [[ -s "$file" ]] || { echo "STOP: missing $file"; exit 2; }
done
for cmd in adb sha256sum awk sed grep; do
  command -v "$cmd" >/dev/null 2>&1 ||
    { echo "STOP: required Termux tool missing: $cmd"; exit 2; }
done
field() { sed -n "s/^$1=//p" "$manifest"; }
if [[ "$(field version)" != "D-127" ||
      "$(field code)" != "21" ||
      "$(field branch)" != "android-native-kotlin-compose" ||
      "$(field new_matrix_test_count)" != "8" ]]; then
  echo "STOP: wrong build identity or new-test count."; exit 3
fi
source_commit="$(field source_commit)"
expected="$(field test_sha256)"
if [[ ! "$source_commit" =~ ^[a-f0-9]{40}$ ||
      ! "$expected" =~ ^[a-f0-9]{64}$ ||
      "$(sha256sum "$apk" | awk '{print $1}')" != "$expected" ]]; then
  echo "STOP: test APK integrity/manifest mismatch. Nothing installed."; exit 3
fi
echo "PASS: NEW Stage 7 matrix APK matches Samsung build manifest."

devices="$(adb devices | awk 'NR>1 && $2 == "device" {print $1}')"
count="$(printf '%s\n' "$devices" | awk 'NF {n++} END {print n+0}')"
if [[ "$count" -ne 1 ]]; then
  echo "STOP: expected exactly one authorized ADB device; found $count."
  adb devices -l
  exit 4
fi
serial="$devices"
phone() { adb -s "$serial" "$@"; }

installed="$(phone shell dumpsys package "$package" | tr -d '\r')"
if ! printf '%s\n' "$installed" | grep -Eq '^[[:space:]]*versionName=D-127[[:space:]]*$' ||
   ! printf '%s\n' "$installed" | grep -Eq '^[[:space:]]*versionCode=21([[:space:]]|$)'; then
  echo "STOP: installed main application is not exactly D-127/code21."
  echo "Do not uninstall, downgrade, or clear data."
  exit 4
fi

echo "The matrix tests temporarily own the app process, not user data."
echo "Do NOT interrupt an active Class or Free Timer."
read -r -p "Type IDLE only if NO real timer is running: " answer
if [[ "$answer" != "IDLE" ]]; then
  echo "Cancelled safely. No installation or tests executed."
  exit 2
fi
echo "Installing ONLY test APK; original user app stays installed."
if ! phone install -t -r "$apk"; then
  echo "STOP: test APK update failed. Never uninstall the main app."
  exit 5
fi
if [[ "$(phone shell pm list instrumentation)" != *"$runner"* ]]; then
  echo "STOP: expected AndroidJUnitRunner missing."
  exit 5
fi

{
  echo "Atelier Time D-127 Stage 7 - eight NEW accessibility matrix tests"
  echo "source_commit=$source_commit"
  echo "test_class=$test_class"
  echo "test_count=8"
} > "$log"
echo "Executing ONLY the eight new matrix tests (not six old or 285 Stable 9)."
if phone shell am instrument -w -e class "$test_class" "$runner" >> "$log" 2>&1; then
  :
else
  echo "MATRIX TESTS FAILED; no PASS claimed."
  tail -n 85 "$log"
  exit 6
fi
tail -n 85 "$log"
if ! grep -Eq '^OK \(8 tests\)' "$log"; then
  echo "MATRIX TESTS NOT ALL PASS. Inspect saved report, do not rerun blindly."
  exit 7
fi
echo "PASS: 8/8 NEW D-127 Stage 7 matrix accessibility UI tests."
echo "Original six tests remain historical PASS; not rerun."
echo "Full test output stored locally: $log"
echo "Remaining human QA: actual visual contrast/legibility and TalkBack speech quality."
echo "Verify Saved Classes/Sounds remain intact. No data clear or main APK change."
