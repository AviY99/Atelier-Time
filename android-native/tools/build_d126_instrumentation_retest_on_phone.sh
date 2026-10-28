#!/usr/bin/env bash
# D-126 QA: compile the corrected AndroidTest APK ONLY (same installed D-126 app).
# NEVER installs/rebuilds/releases a new regular-app candidate or wipes any data.
set -euo pipefail

cd "$(dirname "$0")/.."
[[ "$(git branch --show-current)" == "android-native-kotlin-compose" ]] ||
  { echo "STOP: development branch required."; exit 2; }
if [[ -n "$(git status --porcelain --untracked-files=no)" ]] ||
   [[ -n "$(git ls-files --others --exclude-standard -- app/src tools)" ]]; then
  echo "STOP: unreviewed local edits detected."; git status --short; exit 2
fi
grep -Eq 'versionName[[:space:]]*=[[:space:]]*"D-126"' app/build.gradle.kts ||
  { echo "STOP: app version is not D-126."; exit 2; }
grep -Eq 'versionCode[[:space:]]*=[[:space:]]*20([[:space:]]|$)' app/build.gradle.kts ||
  { echo "STOP: app versionCode is not 20."; exit 2; }
: "${ANDROID_HOME:?CoGo Android SDK missing}"
gradle="$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle"
aapt2="$ANDROID_HOME/build-tools/36.0.0/aapt2"
[[ -f "$gradle" && -s "$aapt2" ]] || { echo "STOP: CoGo toolchain missing."; exit 2; }

downloads="/storage/emulated/0/Download"
mkdir -p "$downloads"
log="$downloads/Atelier-Time-D126-AndroidTest-Retest-Build.log"
echo "Compiling ONLY corrected AndroidTest package; NO app install or wipe."
if bash "$gradle" "-Pandroid.aapt2FromMavenOverride=$aapt2" \
  :app:assembleDebugAndroidTest --rerun-tasks --console=plain --no-daemon \
  2>&1 | tee "$log"; then
  :
else
  echo "BUILD FAILED. No test APK has been published."; exit 3
fi

mapfile -t apks < <(find app/build/outputs/apk/androidTest -type f -name '*.apk')
[[ "${#apks[@]}" -eq 1 ]] ||
  { echo "STOP: expected exactly one compiled AndroidTest APK."; exit 4; }
count="$(grep -R -E '^[[:space:]]*@Test' app/src/androidTest/java | wc -l | tr -d ' ')"
[[ "$count" == "42" ]] ||
  { echo "STOP: 42 AndroidTest methods expected, found $count"; exit 4; }
testapk="$downloads/Atelier-Time-D126-Tests-Retest.apk"
manifest="$downloads/Atelier-Time-D126-Tests-Retest-manifest.txt"
cp "${apks[0]}" "$testapk"
{
  printf 'version=D-126\ncode=20\nbranch=android-native-kotlin-compose\n'
  printf 'commit=%s\n' "$(git rev-parse HEAD)"
  printf 'test_count=%s\n' "$count"
  printf 'test_sha256=%s\n' "$(sha256sum "$testapk" | awk '{print $1}')"
} > "$manifest"
echo "ANDROIDTEST RETEST APK BUILT: test_count=$count"
echo "test=$testapk"
echo "manifest=$manifest"
echo "build_log=$log"
echo "MAIN APP NOT INSTALLED, NO USER DATA MODIFIED."
