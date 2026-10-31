#!/usr/bin/env bash
# D-114 Stage 2: Termux-only test APK update + instrumentation runner.
# DOES NOT install the regular app. Running instrumentation may stop/restart
# the target process, use a temporary test Class and touch its notification.
# Do not run while an actual Class is active; explicit approval required.
set -euo pipefail

if [[ "${1:-}" != "--run" ]]; then
    echo "PREVIEW ONLY. Device tests change the test APK/process; separate user approval required."
    echo "Once approved AND no live Class exists, use: bash ~/storage/downloads/Atelier-Time-D114-QA.sh --run"
    exit 2
fi

downloads="$HOME/storage/downloads"
tests="$downloads/Atelier-Time-D114-Tests.apk"
manifest="$downloads/Atelier-Time-D114-QA-manifest.txt"
runner="com.ateliertime.android.test/androidx.test.runner.AndroidJUnitRunner"
log="$downloads/Atelier-Time-D114-Instrumentation.log"
for file in "$tests" "$manifest"; do
    if [[ ! -s "$file" ]]; then
        echo "ERROR: missing $file. Run the D-114 CoGo build stage first." >&2
        exit 2
    fi
done
if ! command -v adb >/dev/null 2>&1; then
    echo "ERROR: Termux android-tools/adb missing." >&2
    exit 2
fi
serials="$(adb devices | awk 'NR>1 && $2=="device" {print $1}')"
count="$(printf '%s\n' "$serials" | awk 'NF{n++} END{print n+0}')"
if [[ "$count" != 1 ]]; then
    echo "ERROR: exactly one authorized device required, found $count." >&2
    adb devices -l
    exit 3
fi
serial="$serials"
version="$(sed -n 's/^version=//p' "$manifest")"
branch="$(sed -n 's/^branch=//p' "$manifest")"
commit="$(sed -n 's/^commit=//p' "$manifest")"
expected="$(sed -n 's/^test_count=//p' "$manifest")"
hash="$(sed -n 's/^test_sha256=//p' "$manifest")"
if [[ "$version" != "D-114" || "$branch" != "android-native-kotlin-compose" ||
      ! "$commit" =~ ^[0-9a-f]{40}$ ||
      ! "$expected" =~ ^[0-9]+$ || "$expected" -lt 1 ||
      ! "$hash" =~ ^[0-9a-f]{64}$ ]]; then
    echo "ERROR: invalid D-114 QA manifest." >&2
    exit 3
fi
actual_hash="$(sha256sum "$tests" | awk '{print $1}')"
if [[ "$hash" != "$actual_hash" ]]; then
    echo "ERROR: TEST APK hash differs from CoGo build manifest." >&2
    exit 3
fi

echo "ADB device=$serial; expected app=D-114; suite commit=$commit; source methods=$expected."
installed="$(adb -s "$serial" shell dumpsys package com.ateliertime.android)"
if [[ "$installed" != *"versionName=D-114"* ]] ||
   [[ "$installed" != *"versionCode=9"* ]]; then
    echo "ERROR: installed app is not D-114/versionCode 9. NO app update allowed." >&2
    exit 4
fi
echo "D-114 application installed. NO regular APK will be installed."
echo "WARNING: instrumentation restarts/uses target app and a test Class."
echo "DO NOT continue if a real Class is active or saved activity is in progress."
read -r -p "Type NO-ACTIVE-CLASS to confirm approval and idle app: " confirm
if [[ "$confirm" != "NO-ACTIVE-CLASS" ]]; then
    echo "Cancelled: no installation or instrumentation performed."
    exit 2
fi

echo "Installing TEST package only (adb install -t -r)."
adb -s "$serial" install -t -r "$tests"
instruments="$(adb -s "$serial" shell pm list instrumentation)"
if [[ "$instruments" != *"$runner"* ]]; then
    echo "ERROR: test runner absent after test APK installation." >&2
    printf '%s\n' "$instruments"
    exit 5
fi
echo "D-114 full Android instrumentation; log=$log"
{
    printf 'D-114 device instrumentation; source commit=%s\n' "$commit"
    printf 'installed=D-114; expected_methods=%s\n' "$expected"
} > "$log"
if adb -s "$serial" shell am instrument -w "$runner" >> "$log" 2>&1; then
    cat "$log"
else
    cat "$log"
    echo "FAIL: instrumentation command returned nonzero status; inspect log." >&2
    exit 6
fi
if ! grep -Eq "^OK \\($expected tests\\)" "$log"; then
    echo "FAIL: missing expected OK ($expected tests); inspect log for failures/skips." >&2
    exit 7
fi
echo "PASS: $expected Android instrumentation methods on D-114; actual log=$log"
echo "No app update/uninstall/pm clear/force-stop/system clock change was invoked."
