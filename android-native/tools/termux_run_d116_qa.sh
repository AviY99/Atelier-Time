#!/usr/bin/env bash
# D-116 instrumentation runner. Must be separately approved after installing
# the matching D-116 regular APK. NEVER updates the regular app or clears data.
set -euo pipefail

if [[ "${1:-}" != "--run" ]]; then
    echo "PREVIEW ONLY: requires separate explicit approval and NO active Class."
    echo "After approval: bash ~/storage/downloads/Atelier-Time-D116-QA.sh --run"
    exit 2
fi
downloads="$HOME/storage/downloads"
tests="$downloads/Atelier-Time-D116-Tests.apk"
manifest="$downloads/Atelier-Time-D116-QA-manifest.txt"
log="$downloads/Atelier-Time-D116-Instrumentation.log"
runner="com.ateliertime.android.test/androidx.test.runner.AndroidJUnitRunner"
for file in "$tests" "$manifest"; do
    if [[ ! -s "$file" ]]; then echo "ERROR: missing $file" >&2; exit 2; fi
done
if ! command -v adb >/dev/null 2>&1; then
    echo "ERROR: Termux adb missing." >&2; exit 2
fi
serials="$(adb devices | awk 'NR>1 && $2=="device" {print $1}')"
count="$(printf '%s\n' "$serials" | awk 'NF{n++} END{print n+0}')"
if [[ "$count" != 1 ]]; then
    echo "ERROR: expected exactly one authorized device." >&2
    adb devices -l
    exit 3
fi
version="$(sed -n 's/^version=//p' "$manifest")"
code="$(sed -n 's/^code=//p' "$manifest")"
branch="$(sed -n 's/^branch=//p' "$manifest")"
commit="$(sed -n 's/^commit=//p' "$manifest")"
expected="$(sed -n 's/^test_count=//p' "$manifest")"
hash="$(sed -n 's/^test_sha256=//p' "$manifest")"
if [[ "$version" != "D-116" || "$code" != "11" ||
      "$branch" != "android-native-kotlin-compose" ||
      ! "$commit" =~ ^[0-9a-f]{40}$ ||
      ! "$expected" =~ ^[0-9]+$ || "$expected" -lt 1 ||
      ! "$hash" =~ ^[0-9a-f]{64}$ ]]; then
    echo "ERROR: invalid D-116 QA manifest." >&2; exit 3
fi
if [[ "$(sha256sum "$tests" | awk '{print $1}')" != "$hash" ]]; then
    echo "ERROR: test APK hash mismatch." >&2; exit 3
fi
installed="$(adb -s "$serials" shell dumpsys package com.ateliertime.android)"
if [[ "$installed" != *"versionName=D-116"* ||
      "$installed" != *"versionCode=11"* ]]; then
    echo "ERROR: D-116/versionCode 11 must already be installed separately." >&2
    echo "No installation attempted." >&2
    exit 4
fi
echo "D-116 confirmed. Instrumentation affects the target process."
echo "Stop if a real Class is active. Saved Classes/Sounds must be verified after."
read -r -p "Type NO-ACTIVE-CLASS to confirm separate approval: " confirm
if [[ "$confirm" != "NO-ACTIVE-CLASS" ]]; then
    echo "Cancelled without installation."; exit 2
fi
adb -s "$serials" install -t -r "$tests"
if [[ "$(adb -s "$serials" shell pm list instrumentation)" != *"$runner"* ]]; then
    echo "ERROR: AndroidJUnitRunner not registered." >&2; exit 5
fi
{
    printf 'D-116 instrumentation; source commit=%s; expected=%s\n' "$commit" "$expected"
    adb -s "$serials" shell am instrument -w "$runner"
} 2>&1 | tee "$log"
if ! grep -Eq "^OK \($expected tests\)" "$log"; then
    echo "FAIL: actual result does not match expected count; inspect $log" >&2
    exit 6
fi
echo "PASS: $expected D-116 Android tests (actual device log: $log)"
echo "No regular app installation, uninstall, data clear or clock change."
