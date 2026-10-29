#!/usr/bin/env bash
# Stage 7 D-127 compile gate in the existing Code on the Go environment.
# This script builds two APKs but NEVER installs, clears data, runs device
# tests, updates a stable branch or modifies Saved Classes/Sounds.
set -euo pipefail
cd "$(dirname "$0")/.."
if [[ "$(git branch --show-current)" != "android-native-kotlin-compose" ]]; then
  echo "ERROR: Android development branch required." >&2; exit 2
fi
if [[ -n "$(git status --porcelain --untracked-files=no)" ]] ||
   [[ -n "$(git ls-files --others --exclude-standard -- app/src tools)" ]]; then
  echo "ERROR: inspect local modifications before building." >&2
  git status --short; exit 2
fi
if ! grep -Eq 'versionName[[:space:]]*=[[:space:]]*"D-127"' app/build.gradle.kts ||
   ! grep -Eq 'versionCode[[:space:]]*=[[:space:]]*21([[:space:]]|$)' app/build.gradle.kts; then
  echo "ERROR: expected D-127/versionCode 21 source." >&2; exit 2
fi
: "${ANDROID_HOME:?Code on the Go Android SDK required}"
gradle="$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle"
aapt2="$ANDROID_HOME/build-tools/36.0.0/aapt2"
if [[ ! -f "$gradle" || ! -s "$aapt2" ]]; then
  echo "ERROR: existing Gradle 9.6.1/ARM64 AAPT2 toolchain required." >&2; exit 2
fi
downloads="/storage/emulated/0/Download"
mkdir -p "$downloads"
log="$downloads/Atelier-Time-D127-Stage7-build.log"
echo "D-127 targeted UI compilation + two APKs. NO INSTALL; NO JVM or device test run."
bash "$gradle" "-Pandroid.aapt2FromMavenOverride=$aapt2" \
  :app:assembleDebug :app:assembleDebugAndroidTest \
  --console=plain --no-daemon 2>&1 | tee "$log"
mapfile -t regular_apks < <(find app/build/outputs/apk/debug -type f -name '*.apk')
mapfile -t test_apks < <(find app/build/outputs/apk/androidTest -type f -name '*.apk')
if [[ "${#regular_apks[@]}" -ne 1 || "${#test_apks[@]}" -ne 1 ]]; then
  echo "ERROR: expected one regular and one AndroidTest APK." >&2; exit 3
fi
regular="$downloads/Atelier-Time-D127-Candidate.apk"
tests="$downloads/Atelier-Time-D127-Tests.apk"
cp "${regular_apks[0]}" "$regular"
cp "${test_apks[0]}" "$tests"
manifest="$downloads/Atelier-Time-D127-Stage7-build-manifest.txt"
{
  printf 'version=D-127\ncode=21\nbranch=android-native-kotlin-compose\n'
  printf 'source_commit=%s\n' "$(git rev-parse HEAD)"
  printf 'app_sha256=%s\n' "$(sha256sum "$regular" | awk '{print $1}')"
  printf 'test_sha256=%s\n' "$(sha256sum "$tests" | awk '{print $1}')"
  printf 'qa_state=build-only; android_instrumentation_not_run; installation_not_performed\n'
} > "$manifest"
echo "COMPILE/BUILD PASS: $regular ; $tests ; $manifest"
echo "No APK installed, app data untouched. Separate device/visual QA remains mandatory."
