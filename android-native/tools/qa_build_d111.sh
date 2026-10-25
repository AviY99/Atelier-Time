#!/usr/bin/env bash
# Runs every JVM unit test, compiles debug app + Android instrumentation APK,
# and publishes both to Downloads. Execute INSIDE Code on the Go terminal.
set -euo pipefail
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."
: "${ANDROID_HOME:?Android SDK missing in Code on the Go terminal}"
GRADLE="$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle"
AAPT2="$ANDROID_HOME/build-tools/36.0.0/aapt2"
test -f "$GRADLE" || { echo "ERROR: existing CoGo Gradle not found"; exit 2; }
test -f "$AAPT2" || { echo "ERROR: native ARM64 AAPT2 not found"; exit 2; }
echo "QA D-111: JVM suite, app APK and AndroidTest APK"
bash "$GRADLE" \
    :app:testDebugUnitTest \
    :app:assembleDebug \
    :app:assembleDebugAndroidTest \
    --no-daemon --console=plain \
    "-Pandroid.aapt2FromMavenOverride=$AAPT2"
APP_APK="app/build/outputs/apk/debug/app-debug.apk"
TEST_APK="$(find app/build/outputs/apk/androidTest -type f -name '*.apk' -print -quit)"
test -s "$APP_APK" || { echo "ERROR: app APK not found"; exit 3; }
test -n "$TEST_APK" && test -s "$TEST_APK" || {
    echo "ERROR: AndroidTest APK not found"; exit 3;
}
DEST="/storage/emulated/0/Download"
cp "$APP_APK" "$DEST/Atelier-Time-D111.apk"
cp "$TEST_APK" "$DEST/Atelier-Time-D111-Tests.apk"
echo "PASS: JVM test Gradle task and both APK builds succeeded."
ls -lh "$DEST/Atelier-Time-D111.apk" "$DEST/Atelier-Time-D111-Tests.apk"
echo "No installation has occurred. Run tools/qa_device_d111.sh in Termux next."
