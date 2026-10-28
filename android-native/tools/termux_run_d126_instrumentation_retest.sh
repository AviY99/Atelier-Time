#!/usr/bin/env bash
# D-126 instrumentation-only rerun after isolated AndroidTest fixes.
# NEVER installs the regular app, uninstalls anything, clears preferences/data,
# kills the main app on purpose, or changes the system clock.
set -euo pipefail
if [[ "${1:-}" != "--run" ]]; then
  echo "PREVIEW: bash ~/storage/downloads/Atelier-Time-D126-Retest.sh --run"
  exit 2
fi
downloads="$HOME/storage/downloads"
testapk="$downloads/Atelier-Time-D126-Tests-Retest.apk"
manifest="$downloads/Atelier-Time-D126-Tests-Retest-manifest.txt"
log="$downloads/Atelier-Time-D126-Instrumentation-Retest.log"
runner="com.ateliertime.android.test/androidx.test.runner.AndroidJUnitRunner"
for file in "$testapk" "$manifest"; do
  [[ -s "$file" ]] || { echo "STOP: Missing $file"; exit 2; }
done
command -v adb >/dev/null 2>&1 || { echo "STOP: Termux ADB missing."; exit 2; }

value() { sed -n "s/^${1}=//p" "$manifest"; }
[[ "$(value version)" == "D-126" && "$(value code)" == "20" &&
   "$(value branch)" == "android-native-kotlin-compose" ]] ||
  { echo "STOP: wrong D-126 manifest."; exit 3; }
[[ "$(value test_count)" == "42" ]] || { echo "STOP: wrong test count."; exit 3; }
commit="$(value commit)"
[[ "$commit" =~ ^[a-f0-9]{40}$ ]] || { echo "STOP: invalid build commit."; exit 3; }
expected_hash="$(value test_sha256)"
[[ "$expected_hash" =~ ^[a-f0-9]{64}$ ]] || { echo "STOP: invalid test hash."; exit 3; }
[[ "$(sha256sum "$testapk" | awk '{print $1}')" == "$expected_hash" ]] ||
  { echo "STOP: test APK SHA-256 mismatch; NO installation."; exit 3; }
echo "PASS: corrected AndroidTest APK SHA-256."

devices="$(adb devices | awk 'NR>1 && $2=="device" {print $1}')"
count="$(printf '%s\n' "$devices" | awk 'NF{n++} END{print n+0}')"
[[ "$count" -eq 1 ]] || {
  echo "STOP: expected one authorized connected ADB phone, found $count."
  adb devices -l
  exit 4
}
serial="$devices"
adb_phone() { adb -s "$serial" "$@"; }
installed="$(adb_phone shell dumpsys package com.ateliertime.android)"
[[ "$installed" == *"versionName=D-126"* &&
   "$installed" == *"versionCode=20"* ]] || {
  echo "STOP: installed regular app must remain D-126/code20."
  echo "No installation was attempted."
  exit 4
}
echo "Confirmed installed main app D-126/code20; it will NOT be updated."
echo "Instrumentation briefly owns the app process; finish any real Class first."
read -r -p "Type IDLE if NO real Class is running: " confirmation
[[ "$confirmation" == "IDLE" ]] ||
  { echo "Cancelled without installation or instrumentation."; exit 2; }

echo "Updating ONLY the AndroidTest APK."
adb_phone install -t -r "$testapk"
instruments="$(adb_phone shell pm list instrumentation)"
[[ "$instruments" == *"$runner"* ]] ||
  { echo "STOP: Android test runner missing."; exit 5; }
{
  printf 'D-126 Samsung instrumentation RETEST, test APK source commit=%s\n' "$commit"
  printf 'expected_methods=42\n'
  printf 'device=%s\n' "$serial"
} > "$log"
echo "Executing all 42 instrumentation tests, preserving full result log."
if adb_phone shell am instrument -w "$runner" >> "$log" 2>&1; then :; else
  echo "FAIL: test execution error; inspect log."
  tail -n 75 "$log"
  exit 6
fi
tail -n 75 "$log"
if ! grep -Eq '^OK \(42 tests\)' "$log"; then
  echo "FAIL: complete suite did not pass. Full report: $log"
  exit 7
fi
echo "PASS: 42/42 D-126 Android instrumentation tests."
echo "Full report: $log"
echo "Verify previously saved Classes/Sounds still exist after tests."
echo "NO main app update, uninstall, or data clear."
