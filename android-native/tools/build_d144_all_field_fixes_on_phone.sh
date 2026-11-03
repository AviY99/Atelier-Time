#!/usr/bin/env bash
# D-144 local Samsung Code on the Go QA gate: no install, cloud, signing or app data changes.
set -euo pipefail
cd "$(dirname "$0")/.."
[[ "$(git branch --show-current)" == "android-native-kotlin-compose" ]] || { echo "STOP: wrong branch"; exit 2; }
[[ -z "$(git status --porcelain --untracked-files=no)" ]] || { echo "STOP: dirty tracked checkout"; exit 2; }
grep -Fq "versionCode = 38" app/build.gradle.kts
grep -Fq 'versionName = "D-144"' app/build.gradle.kts
bash tools/audit_artwork_on_phone.sh
gradle_bin="$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle"
aapt2="${ANDROID_HOME:?}/build-tools/36.0.0/aapt2"
log="/storage/emulated/0/Download/Atelier-Time-D144-Build.log"
echo "D-144: full JVM suite and AndroidTest compilation (no install)."
if ! bash "$gradle_bin" "-Pandroid.aapt2FromMavenOverride=$aapt2" :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest --rerun-tasks --console=plain --no-daemon > "$log" 2>&1; then
  grep -E "^e: |\.kt:[0-9]+|FAILED|FAILURE:|^BUILD FAILED" "$log" | tail -n 50 || true
  echo "FAILED; local log: $log"
  exit 1
fi
junit="app/build/test-results/testDebugUnitTest"
for spec in "domain.D144RuntimeRepeatSafetyTest" "domain.D144PhaseDisplayNamesTest" "android.EditorResetD143Test" "domain.TailBreakRecoveryD142Test"; do
  [[ -s "$junit/TEST-com.ateliertime.$spec.xml" ]] || { echo "STOP: missing executed test $spec"; exit 3; }
done
apk="app/build/outputs/apk/debug/app-debug.apk"
[[ -s "$apk" ]] || { echo "STOP: APK missing"; exit 3; }
out="/storage/emulated/0/Download/Atelier-Time-D144-API36-Candidate.apk"
cp "$apk" "$out"
echo "D-144 JVM SUITE AND BUILD SUCCESSFUL: $out"
echo "Private log: $log"
echo "AndroidTest compiled, not executed. APK not installed."
