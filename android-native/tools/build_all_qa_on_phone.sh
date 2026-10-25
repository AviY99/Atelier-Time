#!/usr/bin/env bash
# Atelier Time D-111 — build and package every JVM + Android instrumented test
# from Code on the Go. No app installation, user data changes or ADB needed.
set -euo pipefail

cd "$(dirname "$0")/.."
if ! grep -q 'versionName = "D-111"' app/build.gradle.kts; then
    echo "ERROR: checkout is not D-111. Check branch and git pull --ff-only." >&2
    exit 2
fi
: "${ANDROID_HOME:?CoGo ANDROID_HOME must point to its Android SDK}"
gradle="$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle"
aapt2="$ANDROID_HOME/build-tools/36.0.0/aapt2"

if [ ! -f "$gradle" ] || [ ! -f "$aapt2" ]; then
    echo "ERROR: Required CoGo Gradle or native AAPT2 was not found." >&2
    exit 2
fi

echo "D-111 gate 1/3: all JVM unit tests + both APK assemblies"
bash "$gradle" "-Pandroid.aapt2FromMavenOverride=$aapt2" \
    :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest

app_apk="$(find app/build/outputs/apk/debug -maxdepth 2 -type f -name '*.apk' -print -quit)"
test_apk="$(find app/build/outputs/apk/androidTest -maxdepth 3 -type f -name '*.apk' -print -quit)"

if [ -z "$app_apk" ] || [ -z "$test_apk" ]; then
    echo "ERROR: A build finished but one of the two APKs was not produced." >&2
    exit 3
fi

destination="/storage/emulated/0/Download"
echo "D-111 gate 2/3: copying assembled APKs to Downloads"
cp "$app_apk" "$destination/Atelier-Time-D111.apk"
cp "$test_apk" "$destination/Atelier-Time-D111-Tests.apk"
cp "tools/termux_run_all_device_qa.sh" "$destination/Atelier-Time-D111-QA.sh"

echo "D-111 gate 3/3: ready for Termux ADB device tests"
ls -lh "$destination/Atelier-Time-D111.apk" \
    "$destination/Atelier-Time-D111-Tests.apk" \
    "$destination/Atelier-Time-D111-QA.sh"
echo "BUILD + JVM TESTS PASS; Android device instrumentation NOT YET RUN."
echo "Next, in Termux: bash ~/storage/downloads/Atelier-Time-D111-QA.sh"
