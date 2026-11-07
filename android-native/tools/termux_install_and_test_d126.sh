#!/usr/bin/env bash
# D-126 controlled Samsung device gate, invoked from Termux.
# Authorized by user on 2026-10-28: in-place app update + test APK + instrumentation.
# Does NOT uninstall either package, clear app data, reset sound preferences,
# use force-stop, change system time, or modify the frozen Stable 8 Git branch.
set -euo pipefail

if [[ "${1:-}" != "--run" ]]; then
  echo "PREVIEW ONLY. Requires approval, idle Class, and a D-126 build manifest."
  echo "Run from Termux: bash ~/storage/downloads/Atelier-Time-D126-DeviceQA.sh --run"
  exit 2
fi

downloads="${HOME}/storage/downloads"
app="${downloads}/Atelier-Time-D126-Candidate.apk"
tests="${downloads}/Atelier-Time-D126-Tests.apk"
manifest="${downloads}/Atelier-Time-D126-QA-manifest.txt"
log="${downloads}/Atelier-Time-D126-Instrumentation.log"
package="com.ateliertime.android"
runner="com.ateliertime.android.test/androidx.test.runner.AndroidJUnitRunner"
build_commit="b2d47ca32c2f470d38e556ea7a571afaa89400e3"

for f in "$app" "$tests" "$manifest"; do
  if [[ ! -s "$f" ]]; then
    echo "STOP: missing build artifact: $f" >&2
    echo "The CoGo build-only stage must complete before device installation." >&2
    exit 2
  fi
done
for cmd in adb sha256sum sed awk grep; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "STOP: Termux command missing: $cmd" >&2
    exit 2
  fi
done

get_field() {
  sed -n "s/^${1}=//p" "$manifest"
}
[[ "$(get_field version)" == "D-126" ]] || { echo "STOP: wrong version"; exit 3; }
[[ "$(get_field code)" == "20" ]] || { echo "STOP: wrong versionCode"; exit 3; }
[[ "$(get_field branch)" == "android-native-kotlin-compose" ]] ||
  { echo "STOP: wrong branch"; exit 3; }
[[ "$(get_field commit)" == "$build_commit" ]] ||
  { echo "STOP: expected vetted D-126 APK build commit $build_commit"; exit 3; }
expected="$(get_field test_count)"
[[ "$expected" =~ ^[0-9]+$ && "$expected" -ge 40 ]] ||
  { echo "STOP: unexpected test-method count: $expected"; exit 3; }
for pair in "app_sha256:$app" "test_sha256:$tests"; do
  field="${pair%%:*}"
  file="${pair#*:}"
  expected_hash="$(get_field "$field")"
  [[ "$expected_hash" =~ ^[a-f0-9]{64}$ ]] ||
    { echo "STOP: missing/invalid hash: $field"; exit 3; }
  actual_hash="$(sha256sum "$file" | awk '{print $1}')"
  if [[ "$actual_hash" != "$expected_hash" ]]; then
    echo "STOP: SHA-256 mismatch for $file; nothing was installed." >&2
    exit 3
  fi
  echo "SHA-256 PASS: ${file##*/}"
done

serials="$(adb devices | awk 'NR>1 && $2=="device" {print $1}')"
count="$(printf '%s\n' "$serials" | awk 'NF { n++ } END { print n+0 }')"
if [[ "$count" != 1 ]]; then
  echo "STOP: exactly one authorized ADB device is required, found $count." >&2
  adb devices -l
  exit 4
fi
serial="$serials"
adb_for_phone() { adb -s "$serial" "$@"; }
installed="$(adb_for_phone shell dumpsys package "$package")"
if [[ "$installed" == *"versionName=D-124"* && "$installed" == *"versionCode=19"* ]]; then
  needs_upgrade=1
  echo "Installed original Stable 8 confirmed: D-124/versionCode 19."
elif [[ "$installed" == *"versionName=D-126"* && "$installed" == *"versionCode=20"* ]]; then
  needs_upgrade=0
  echo "D-126/versionCode 20 already installed; no repeated app update."
else
  echo "STOP: installed app identity is NOT expected D-124/19 or D-126/20." >&2
  echo "No installation or instrumentation performed. Inspect:"
  printf '%s\n' "$installed" | grep -E 'versionCode=|versionName=' | head -n 8 || true
  exit 4
fi

echo
echo "BEFORE INSTALL: A running Class must NOT be interrupted."
echo "Check that no real Class is active, and note your Saved Classes and Sounds."
read -r -p "Type IDLE to confirm that NO Class is running: " idle
[[ "$idle" == "IDLE" ]] || { echo "Cancelled without installation."; exit 2; }

if [[ "$needs_upgrade" == "1" ]]; then
  echo "Installing MAIN app as a non-destructive UPDATE: adb install -r."
  if ! adb_for_phone install -r "$app"; then
    echo "STOP: update failed. Do NOT uninstall or clear data to make it work." >&2
    exit 5
  fi
fi
installed="$(adb_for_phone shell dumpsys package "$package")"
if [[ "$installed" != *"versionName=D-126"* ||
      "$installed" != *"versionCode=20"* ]]; then
  echo "STOP: after update the installed app is not D-126/20." >&2
  exit 5
fi
echo "D-126 installed and verified, without app uninstall/data clear."
echo
echo "MANUAL PRESERVATION GATE:"
echo "Open Atelier Time on the phone; verify every Saved Class, including"
echo "the original test and Draw1425a classes (if still present), and A/B/C"
echo "sound selections. Check that the app opens normally."
echo "Return to Termux before proceeding; do not run a real Class during QA."
read -r -p "Type PRESERVED ONLY if all prior Classes/Sounds are intact: " preserved
if [[ "$preserved" != "PRESERVED" ]]; then
  echo "STOP: No test APK installed or instrumentation run. Check the app first."
  exit 5
fi

echo "Installing TEST APK only, keeping existing main-app data."
if ! adb_for_phone install -t -r "$tests"; then
  echo "STOP: AndroidTest APK install failed; main-app data not cleared." >&2
  exit 6
fi
instruments="$(adb_for_phone shell pm list instrumentation)"
if [[ "$instruments" != *"$runner"* ]]; then
  echo "STOP: expected AndroidJUnitRunner is not registered." >&2
  printf '%s\n' "$instruments"
  exit 6
fi

{
  printf 'D-126 actual Samsung instrumentation\n'
  printf 'source_build_commit=%s\n' "$build_commit"
  printf 'expected_methods=%s\n' "$expected"
  printf 'device=%s\n' "$serial"
} > "$log"
echo "Running $expected Android instrumentation methods. Logging to $log"
if adb_for_phone shell am instrument -w "$runner" >> "$log" 2>&1; then
  :
else
  echo "FAIL: Android instrumentation command returned an error." >&2
  tail -n 90 "$log"
  exit 7
fi
tail -n 100 "$log"
if ! grep -Eq "^OK \\($expected tests\\)" "$log"; then
  echo "FAIL: Did not observe exact expected OK ($expected tests)." >&2
  echo "Inspect $log. Do NOT call the instrumentation tests PASS." >&2
  exit 8
fi
echo
echo "PASS: $expected/$expected real Android instrumentation tests on D-126."
echo "Log: $log"
echo "NEXT MANUAL CHECK: Saved Classes/Sounds still intact."
echo "NEXT FEATURE CHECK: run a short class, >30 Next/automatic events,"
echo "scroll the bottom history to Start; Export a Run diagnostic in Timer HOME."
echo "No uninstall, pm clear, force-stop, or system-clock change was invoked."
