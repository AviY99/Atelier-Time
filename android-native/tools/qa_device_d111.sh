#!/usr/bin/env bash
# Safe on-device automated QA from Termux. Never clears user data, force-stops,
# changes system clocks, dismisses notifications or launches a real Class.
# Prerequisites: D-111 debug APK and matching androidTest APK in Downloads.
set -euo pipefail

if [[ "${1:-}" != "--run" ]]; then
    echo "Usage: bash qa_device_d111.sh --run"
    echo "Confirm no Class is currently running before executing."
    exit 2
fi
command -v adb >/dev/null || { echo "FAIL: install android-tools in Termux"; exit 2; }
DEVICE_COUNT="$(adb devices | awk 'NR>1 && $2=="device" { n++ } END { print n+0 }')"
if [[ "$DEVICE_COUNT" != "1" ]]; then
    echo "FAIL: expected exactly one connected, authorized ADB device; found $DEVICE_COUNT"
    adb devices -l
    exit 2
fi

DOWNLOADS="$HOME/storage/downloads"
APP="$DOWNLOADS/Atelier-Time-D111.apk"
TEST="$DOWNLOADS/Atelier-Time-D111-Tests.apk"
test -s "$APP" || { echo "FAIL: missing $APP"; exit 2; }
test -s "$TEST" || { echo "FAIL: missing $TEST"; exit 2; }

echo "Atelier Time D-111: device + installed package verification"
adb devices -l
echo "Installing debug APP update, keeping data and settings..."
adb install -r "$APP"
echo "Installing instrumentation TEST package..."
adb install -t -r "$TEST"

echo "Checking that the D-111 application and AndroidJUnitRunner are registered..."
adb shell dumpsys package com.ateliertime.android | grep -F 'versionName=D-111' >/dev/null || {
    echo "FAIL: D-111 versionName not reported by Android package manager"
    exit 3
}
adb shell pm list instrumentation | grep -F 'com.ateliertime.android.test/androidx.test.runner.AndroidJUnitRunner' >/dev/null || {
    echo "FAIL: test runner not registered by Android"
    adb shell pm list instrumentation
    exit 3
}

echo "Running the full Android device instrumentation suite..."
LOG="$HOME/atelier-time-d111-device-qa.log"
if adb shell am instrument -w \
    com.ateliertime.android.test/androidx.test.runner.AndroidJUnitRunner > "$LOG" 2>&1; then
    cat "$LOG"
else
    cat "$LOG"
    echo "FAIL: Android instrumentation command returned nonzero status"
    exit 4
fi

if grep -Fq 'OK (24 tests)' "$LOG"; then
    echo "PASS: 24/24 Android instrumentation tests on physical phone."
else
    echo "FAIL: no exact OK (24 tests) result; inspect $LOG"
    exit 5
fi

echo "PASS: D-111 QA device gate, no real saved Classes or Sound settings cleared."
echo "JVM Gradle unit tests and APK builds are checked by qa_build_d111.sh in CoGo."
