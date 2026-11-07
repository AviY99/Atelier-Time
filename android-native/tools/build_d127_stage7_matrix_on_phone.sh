#!/usr/bin/env bash
# D-127 Stage 7 NEW accessibility matrix ONLY; no main-app install, data reset,
# old six-test rerun, GitHub Actions, remote upload, or system-setting changes.
set -euo pipefail
cd "$(dirname "$0")/.."

if [[ "$(git branch --show-current)" != "android-native-kotlin-compose" ]]; then
  echo "STOP: wrong branch; use android-native-kotlin-compose." >&2
  exit 2
fi
if [[ -n "$(git status --porcelain --untracked-files=no)" ]] ||
   [[ -n "$(git ls-files --others --exclude-standard -- app/src tools)" ]]; then
  echo "STOP: local modifications; do not overwrite or build unknown source."
  git status --short
  exit 2
fi
if ! grep -Eq 'versionName[[:space:]]*=[[:space:]]*"D-127"' app/build.gradle.kts ||
   ! grep -Eq 'versionCode[[:space:]]*=[[:space:]]*21([[:space:]]|$)' app/build.gradle.kts; then
  echo "STOP: expected D-127/code21 (no version bump)." >&2
  exit 2
fi

matrix="app/src/androidTest/java/com/ateliertime/android/AccessibilityStage7MatrixTest.kt"
old="app/src/androidTest/java/com/ateliertime/android/AccessibilityStage7UiTest.kt"
if [[ ! -s "$matrix" || ! -s "$old" ]]; then
  echo "STOP: missing the new matrix or previous six-test source." >&2
  exit 2
fi
new_count="$(grep -Ec '^[[:space:]]*@Test fun ' "$matrix")"
old_count="$(grep -Ec '^[[:space:]]*@Test fun ' "$old")"
if [[ "$new_count" != "8" || "$old_count" != "6" ]]; then
  echo "STOP: expected eight NEW matrix methods and six PREVIOUS methods." >&2
  exit 2
fi

if [[ -z "$(printenv ANDROID_HOME || true)" ]]; then
  echo "STOP: existing Samsung CoGo SDK required." >&2
  exit 2
fi
gradle="$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle"
aapt2="$ANDROID_HOME/build-tools/36.0.0/aapt2"
if [[ ! -f "$gradle" || ! -s "$aapt2" ]]; then
  echo "STOP: expected existing Samsung Gradle 9.6.1 and ARM64 AAPT2." >&2
  exit 2
fi

downloads="/storage/emulated/0/Download"
mkdir -p "$downloads"
log="$downloads/Atelier-Time-D127-Stage7-Matrix-Build.log"
echo "Building ONLY new D-127 AndroidTest APK in Samsung CoGo."
echo "No app installation, no tests executed, no user data, no GitHub Actions."
if bash "$gradle" "-Pandroid.aapt2FromMavenOverride=$aapt2" \
    :app:assembleDebugAndroidTest --console=plain --no-daemon > "$log" 2>&1; then
  echo "BUILD SUCCESSFUL (local log saved in Downloads)."
else
  echo "BUILD FAILED. Kotlin/Gradle errors from saved local log:"
  awk '/(^|[[:space:]])e: |(^|[[:space:]])error: |\.kt:[0-9]+|^FAILURE:|^BUILD FAILED|Execution failed for task/ { if (n++ < 30) print }' "$log"
  echo "No APK installed. Full log: $log"
  exit 1
fi

apk_dir="app/build/outputs/apk/androidTest"
count="$(find "$apk_dir" -type f -name '*.apk' | wc -l | tr -d ' ')"
if [[ "$count" != "1" ]]; then
  echo "STOP: expected exactly one AndroidTest APK; found $count." >&2
  exit 3
fi
source="$(find "$apk_dir" -type f -name '*.apk' -print -quit)"
out="$downloads/Atelier-Time-D127-Stage7-Matrix-Tests.apk"
cp "$source" "$out"
manifest="$downloads/Atelier-Time-D127-Stage7-Matrix-Manifest.txt"
{
  printf 'version=D-127\ncode=21\nbranch=android-native-kotlin-compose\n'
  printf 'source_commit=%s\n' "$(git rev-parse HEAD)"
  printf 'new_matrix_test_count=%s\n' "$new_count"
  printf 'old_stage7_tests_already_passed=6\n'
  printf 'test_sha256=%s\n' "$(sha256sum "$out" | awk '{print $1}')"
  printf 'qa_state=compiled_only; eight_new_tests_not_run; no_main_app_install\n'
} > "$manifest"

runner="tools/termux_run_d127_stage7_matrix.sh"
[[ -s "$runner" ]] || { echo "STOP: missing guarded Termux runner." >&2; exit 3; }
cp "$runner" "$downloads/Atelier-Time-D127-Stage7-MatrixQA.sh"
echo "MATRIX APK READY: $out"
echo "Manifest: $manifest"
echo "Runner: $downloads/Atelier-Time-D127-Stage7-MatrixQA.sh"
echo "No tests run yet; main D-127 APK and user data untouched."
