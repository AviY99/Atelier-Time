#!/usr/bin/env bash
# D-141 user-approved field feedback changes — local Samsung Code on the Go only.
# PREPARED ONLY. Run only after separate user authorization. No install or cloud CI.
set -euo pipefail
cd "$(dirname "$0")/.."
if [[ "$(git branch --show-current)" != "android-native-kotlin-compose" ]]; then
  echo 'STOP: Android development branch is required.' >&2; exit 2
fi
if [[ -n "$(git status --porcelain --untracked-files=no)" ]] ||
   [[ -n "$(git ls-files --others --exclude-standard -- app/src tools)" ]]; then
  echo 'STOP: local app source is not clean.' >&2
  git status --short; exit 2
fi
for config in 'compileSdk = 36' 'targetSdk = 36' 'versionCode = 35' 'versionName = "D-141"'; do
  if ! grep -Fq "$config" app/build.gradle.kts; then
    echo "STOP: expected D-141 config is missing: $config" >&2; exit 2
  fi
done
bash tools/audit_artwork_on_phone.sh
for source in \
  app/src/main/java/com/ateliertime/domain/ConstraintFuturePlanner.kt \
  app/src/main/java/com/ateliertime/domain/RunEndTimePlanner.kt \
  app/src/main/java/com/ateliertime/domain/FinalPairCompensation.kt \
  app/src/main/java/com/ateliertime/android/EndClockWheel.kt \
  app/src/test/java/com/ateliertime/domain/RunEndTimePlannerTest.kt; do
  [[ -s "$source" ]] || { echo "STOP: required D-141 source is missing: $source" >&2; exit 2; }
done
: "${ANDROID_HOME:?Samsung Code on the Go Android SDK is required}"
gradle="$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle"
aapt2="$ANDROID_HOME/build-tools/36.0.0/aapt2"
if [[ ! -f "$gradle" || ! -s "$aapt2" ]]; then
  echo 'STOP: existing Samsung Gradle / ARM64 AAPT2 tools are required.' >&2; exit 2
fi
downloads='/storage/emulated/0/Download'
mkdir -p "$downloads"
log="$downloads/Atelier-Time-D141-API36-Build.log"
echo 'D-141: local JVM regression tests + debug APK assembly; NO INSTALL.'
if ! bash "$gradle" "-Pandroid.aapt2FromMavenOverride=$aapt2" \
    :app:testDebugUnitTest :app:assembleDebug --console=plain --no-daemon > "$log" 2>&1; then
  echo 'D-141 TEST/BUILD FAILED. Compiler or test failures:'
  awk '/(^|[[:space:]])e: |(^|[[:space:]])error: |\.kt:[0-9]+|^FAILURE:|^BUILD FAILED|Execution failed for task|FAILED/ {if (n++ < 60) print}' "$log"
  echo "Full local log: $log"
  exit 1
fi
apk_dir='app/build/outputs/apk/debug'
mapfile -t apks < <(find "$apk_dir" -type f -name '*.apk')
if [[ "${#apks[@]}" -ne 1 ]]; then
  echo "STOP: expected one APK, found ${#apks[@]}." >&2; exit 3
fi
out="$downloads/Atelier-Time-D141-API36-Candidate.apk"
cp "${apks[0]}" "$out"
{
  printf 'version=D-141\ncode=35\ntargetSdk=36\n'
  printf 'branch=android-native-kotlin-compose\n'
  printf 'source_commit=%s\n' "$(git rev-parse HEAD)"
  printf 'apk_sha256=%s\n' "$(sha256sum "$out" | awk '{print $1}')"
  printf 'state=JVM_TESTED_AND_COMPILED_ONLY; NOT_INSTALLED; NOT_DEVICE_TESTED\n'
} > "$downloads/Atelier-Time-D141-API36-Manifest.txt"
echo "D-141 JVM tests and BUILD SUCCESSFUL: $out"
echo "Manifest: $downloads/Atelier-Time-D141-API36-Manifest.txt"
echo 'No install, GitHub Actions, push, permission change, or app data wipe.'
