#!/usr/bin/env bash
# Atelier Time D-111 — run the full Android instrumentation QA on ONE connected
# authorized device, after the CoGo build gate succeeded.
#
# IMPORTANT: this updates the installed app to D-111 using adb install -r.
# It does NOT run when a real Class is active: finish/close your Class first.
# There is no uninstall, pm clear, permission change or reset of saved data.
set -euo pipefail

downloads="$HOME/storage/downloads"
app="$downloads/Atelier-Time-D111.apk"
tests="$downloads/Atelier-Time-D111-Tests.apk"
log="$downloads/Atelier-Time-D111-QA-result.txt"
expected_tests=32
runner="com.ateliertime.android.test/androidx.test.runner.AndroidJUnitRunner"

for path in "$app" "$tests"; do
    if [ ! -s "$path" ]; then
        echo "ERROR: missing APK $path. Run the CoGo build QA gate first." >&2
        exit 2
    fi
done
if ! command -v adb >/dev/null 2>&1; then
    echo "ERROR: adb is not installed in Termux." >&2
    exit 2
fi

serials="$(adb devices | awk 'NR>1 && $2=="device" {print $1}')"
count="$(printf '%s\n' "$serials" | awk 'NF{n++} END{print n+0}')"
if [ "$count" != 1 ]; then
    echo "ERROR: exactly one authorized ADB device is required (found $count)." >&2
    echo "Check Developer options > Wireless debugging and adb devices -l." >&2
    exit 3
fi
serial="$serials"
echo "Connected device: $serial"
echo "Updating regular Atelier Time to D-111 IN PLACE (app data retained)."
adb -s "$serial" install -r "$app"

package_info="$(adb -s "$serial" shell dumpsys package com.ateliertime.android)"
if [[ "$package_info" != *"versionName=D-111"* ]]; then
    echo "ERROR: installed Atelier Time is not D-111; aborting instrumentation." >&2
    exit 4
fi
echo "Installed app: D-111 confirmed."

echo "Installing the TEST-ONLY APK."
adb -s "$serial" install -t -r "$tests"

instruments="$(adb -s "$serial" shell pm list instrumentation)"
if [[ "$instruments" != *"$runner"* ]]; then
    echo "ERROR: expected AndroidJUnitRunner missing after installation." >&2
    printf '%s\n' "$instruments"
    exit 5
fi

echo "Running all Android device tests (including isolated Compose UI tests)."
if output="$(adb -s "$serial" shell am instrument -w "$runner" 2>&1)"; then
    printf '%s\n' "$output" | tee "$log"
else
    printf '%s\n' "$output" | tee "$log"
    echo "FAIL: Android instrumentation command could not complete." >&2
    exit 6
fi

if [[ "$output" == *"OK ($expected_tests tests)"* ]]; then
    echo "PASS: $expected_tests / $expected_tests Android tests on D-111."
    echo "Saved local QA result: $log"
else
    echo "FAIL: expected OK ($expected_tests tests), inspect $log." >&2
    exit 7
fi

echo "QA completed. Wireless debugging may now be disabled in Developer options."
