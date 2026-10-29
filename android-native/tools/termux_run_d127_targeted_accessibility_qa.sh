#!/usr/bin/env bash
# D-127 Stage 7: run ONLY six newly authored accessibility UI methods.
# NEVER installs/replaces the MAIN application, clears data, uninstalls
# anything, changes the clock or runs legacy D-126 regression suites.
set -euo pipefail

if [[ "${1:-}" != "--run" ]]; then
  echo "PREVIEW ONLY. Requires an already installed, separately approved D-127/code21."
  echo "Run in Termux: bash ~/storage/downloads/Atelier-Time-D127-TargetedQA.sh --run"
  exit 2
fi

downloads="$HOME/storage/downloads"
tests="$downloads/Atelier-Time-D127-Tests.apk"
manifest="$downloads/Atelier-Time-D127-Stage7-build-manifest.txt"
log="$downloads/Atelier-Time-D127-Stage7-Instrumentation.log"
package="com.ateliertime.android"
runner="com.ateliertime.android.test/androidx.test.runner.AndroidJUnitRunner"
test_class="com.ateliertime.android.AccessibilityStage7UiTest"
methods=6

for f in "$tests" "$manifest"; do
  [[ -s "$f" ]] || { echo "STOP: missing $f. Build D-127 first, without installing."; exit 2; }
done
for cmd in adb sha256sum awk sed grep; do
  command -v "$cmd" >/dev/null 2>&1 ||
    { echo "STOP: Termux command missing: $cmd"; exit 2; }
done

get_field() { sed -n "s/^${1}=//p" "$manifest"; }
[[ "$(get_field version)" == "D-127" &&
   "$(get_field code)" == "21" &&
   "$(get_field branch)" == "android-native-kotlin-compose" ]] ||
  { echo "STOP: wrong version/branch in D-127 build manifest."; exit 3; }
[[ "$(get_field stage7_test_count)" == "$methods" ]] ||
  { echo "STOP: manifest must name exactly six new Stage 7 tests."; exit 3; }
commit="$(get_field source_commit)"
[[ "$commit" =~ ^[a-f0-9]{40}$ ]] ||
  { echo "STOP: invalid source commit in manifest."; exit 3; }
expected="$(get_field test_sha256)"
[[ "$expected" =~ ^[a-f0-9]{64}$ ]] ||
  { echo "STOP: invalid test APK SHA-256 in manifest."; exit 3; }
[[ "$(sha256sum "$tests" | awk '{print $1}')" == "$expected" ]] ||
  { echo "STOP: test APK integrity mismatch. Nothing installed."; exit 3; }
echo "PASS: D-127 AndroidTest APK SHA-256 verified."

devices="$(adb devices | awk 'NR>1 && $2=="device" {print $1}')"
count="$(printf '%s\n' "$devices" | awk 'NF{n++} END{print n+0}')"
[[ "$count" -eq 1 ]] || {
  echo "STOP: expected exactly one connected, authorized ADB device; found $count."
  adb devices -l
  exit 4
}
serial="$devices"
adb_phone() { adb -s "$serial" "$@"; }

installed="$(adb_phone shell dumpsys package "$package")"
# Read exact Android package fields, not substring matches (D-127x/code210
# must never be mistaken for the separately approved D-127/code21).
if ! printf '%s\n' "$installed" | grep -Eq '^[[:space:]]*versionName=D-127[[:space:]]*
  echo "STOP: installed main app is not D-127/code21."
  echo "Stable 9 D-126 is left unchanged. This runner NEVER installs the main APK."
  echo "A separate explicit in-place upgrade approval is required before device QA."
  exit 4
fi
echo "PASS: D-127/code21 already installed. No main-app installation will occur."
echo "The tests temporarily own the app process: do not interrupt an active Class."
read -r -p "Type IDLE if NO real Class or Free timer is active: " answer
[[ "$answer" == "IDLE" ]] ||
  { echo "Cancelled. No installation or instrumentation executed."; exit 2; }

echo "Updating ONLY the AndroidTest APK; existing app data is preserved."
adb_phone install -t -r "$tests"
instruments="$(adb_phone shell pm list instrumentation)"
[[ "$instruments" == *"$runner"* ]] ||
  { echo "STOP: AndroidJUnitRunner not registered. No tests run."; exit 5; }
{
  printf 'Atelier Time D-127 Stage 7 FOCUSED instrumentation\n'
  printf 'source_commit=%s\n' "$commit"
  printf 'expected_new_methods=%s\n' "$methods"
  printf 'test_class=%s\n' "$test_class"
  printf 'device=%s\n' "$serial"
} > "$log"
echo "Running ONLY $test_class; not the older 285 D-126 tests."
if adb_phone shell am instrument -w -e class "$test_class" "$runner" >> "$log" 2>&1; then
  :
else
  echo "FAIL: instrument command failed; do not claim QA PASS."
  tail -n 80 "$log"
  exit 6
fi
tail -n 85 "$log"
if ! grep -Eq '^OK \(6 tests\)' "$log"; then
  echo "FAIL: expected 'OK (6 tests)' was not found. Inspect $log."
  exit 7
fi
echo "PASS: six of six D-127 targeted accessibility UI tests."
echo "Full evidence: $log"
echo "Next: real TalkBack/font-scale/Day/Night/manual screen acceptance."
echo "Verify saved Classes and Sounds remain intact; no uninstall or data clear."
 ||
   ! printf '%s\n' "$installed" | grep -Eq '^[[:space:]]*versionCode=21([[:space:]]|$)'; then
  echo "STOP: installed main app is not D-127/code21."
  echo "Stable 9 D-126 is left unchanged. This runner NEVER installs the main APK."
  echo "A separate explicit in-place upgrade approval is required before device QA."
  exit 4
fi
echo "PASS: D-127/code21 already installed. No main-app installation will occur."
echo "The tests temporarily own the app process: do not interrupt an active Class."
read -r -p "Type IDLE if NO real Class or Free timer is active: " answer
[[ "$answer" == "IDLE" ]] ||
  { echo "Cancelled. No installation or instrumentation executed."; exit 2; }

echo "Updating ONLY the AndroidTest APK; existing app data is preserved."
adb_phone install -t -r "$tests"
instruments="$(adb_phone shell pm list instrumentation)"
[[ "$instruments" == *"$runner"* ]] ||
  { echo "STOP: AndroidJUnitRunner not registered. No tests run."; exit 5; }
{
  printf 'Atelier Time D-127 Stage 7 FOCUSED instrumentation\n'
  printf 'source_commit=%s\n' "$commit"
  printf 'expected_new_methods=%s\n' "$methods"
  printf 'test_class=%s\n' "$test_class"
  printf 'device=%s\n' "$serial"
} > "$log"
echo "Running ONLY $test_class; not the older 285 D-126 tests."
if adb_phone shell am instrument -w -e class "$test_class" "$runner" >> "$log" 2>&1; then
  :
else
  echo "FAIL: instrument command failed; do not claim QA PASS."
  tail -n 80 "$log"
  exit 6
fi
tail -n 85 "$log"
if ! grep -Eq '^OK \(6 tests\)' "$log"; then
  echo "FAIL: expected 'OK (6 tests)' was not found. Inspect $log."
  exit 7
fi
echo "PASS: six of six D-127 targeted accessibility UI tests."
echo "Full evidence: $log"
echo "Next: real TalkBack/font-scale/Day/Night/manual screen acceptance."
echo "Verify saved Classes and Sounds remain intact; no uninstall or data clear."
