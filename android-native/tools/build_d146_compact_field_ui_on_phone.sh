#!/usr/bin/env bash
# D-146 / Code40: Samsung Code on the Go LOCAL build only. No cloud, install, signing, deletion.
set -euo pipefail
cd "$(dirname "$0")/.."
[[ "$(git branch --show-current)" == "android-native-kotlin-compose" ]] || { echo "STOP: wrong branch"; exit 2; }
[[ -z "$(git status --porcelain --untracked-files=no)" ]] || { echo "STOP: local source changed"; git status --short; exit 2; }
[[ -z "$(git ls-files --others --exclude-standard -- app/src tools)" ]] || { echo "STOP: untracked source"; exit 2; }
for setting in 'compileSdk = 36' 'targetSdk = 36' 'versionCode = 40' 'versionName = "D-146"'; do
  grep -Fq "$setting" app/build.gradle.kts || { echo "STOP: missing $setting"; exit 2; }
done
for file in \
  app/src/main/java/com/ateliertime/domain/PhaseDisplayNames.kt \
  app/src/main/java/com/ateliertime/android/SessionTimerScreen.kt \
  app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt \
  app/src/main/java/com/ateliertime/android/MelodySettingsSection.kt \
  app/src/test/java/com/ateliertime/domain/D146GroupTimerLabelsTest.kt \
  app/src/androidTest/java/com/ateliertime/android/D146CompactUserFeedbackUiTest.kt; do
  [[ -s "$file" ]] || { echo "STOP: missing $file"; exit 2; }
done
grep -Fq 'class_controls_row' app/src/main/java/com/ateliertime/android/SessionTimerScreen.kt
grep -Fq 'class_controls_help' app/src/main/java/com/ateliertime/android/SessionTimerScreen.kt
grep -Fq 'OutlinedIconButton(' app/src/main/java/com/ateliertime/android/MelodySettingsSection.kt
if grep -Fq 'Text("Class controls"' app/src/main/java/com/ateliertime/android/SessionTimerScreen.kt; then
  echo "STOP: redundant Class controls heading"; exit 2
fi
bash tools/audit_artwork_on_phone.sh
: "$ANDROID_HOME"
[[ -n "$ANDROID_HOME" ]] || { echo "STOP: Android SDK missing"; exit 2; }
gradle="$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle"
aapt2="$ANDROID_HOME/build-tools/36.0.0/aapt2"
[[ -f "$gradle" && -s "$aapt2" ]] || { echo "STOP: phone Gradle/AAPT2 unavailable"; exit 2; }
dest="/storage/emulated/0/Download"
mkdir -p "$dest"
log="$dest/Atelier-Time-D146-API36-Build.log"
echo "D-146: full JVM suite + debug APK + COMPILE AndroidTest only. NO INSTALL."
if ! bash "$gradle" "-Pandroid.aapt2FromMavenOverride=$aapt2" \
  :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest \
  --rerun-tasks --console=plain --no-daemon > "$log" 2>&1; then
  echo "D-146 JVM TEST / COMPILE FAILED:"
  grep -E '(^| )e: |(^| )error: |\.kt:[0-9]+|^FAILURE:|^BUILD FAILED|Execution failed for task|FAILED' "$log" | head -60 || true
  echo "Private full log: $log"
  exit 1
fi
python3 - app/build/test-results/testDebugUnitTest <<'PY'
import pathlib, sys, xml.etree.ElementTree as ET
found = {}
totals = dict.fromkeys(('tests','errors','failures','skipped'),0)
for p in pathlib.Path(sys.argv[1]).glob('TEST-*.xml'):
    r = ET.parse(p).getroot()
    for x in ([r] if r.tag == 'testsuite' else r.findall('.//testsuite')):
        data = {k:int(x.attrib.get(k,'0')) for k in totals}
        found[x.attrib.get('name','')] = data
        for k in totals: totals[k] += data[k]
required = {
 'com.ateliertime.domain.D146GroupTimerLabelsTest':6,
 'com.ateliertime.domain.D145NetWorkSpecialBreakTest':8,
 'com.ateliertime.domain.D145RepeatOnceSpecialSafetyTest':4,
 'com.ateliertime.domain.D144RuntimeRepeatSafetyTest':5,
 'com.ateliertime.domain.D144PhaseDisplayNamesTest':4,
 'com.ateliertime.android.EditorResetD143Test':2,
 'com.ateliertime.domain.TailBreakRecoveryD142Test':9,
}
for name, minimum in required.items():
    v = found.get(name)
    print(name.split('.')[-1] + ' ACTUAL:', v)
    if v is None or v['tests'] < minimum or any(v[k] for k in ('failures','errors','skipped')):
        raise SystemExit('STOP: target JVM suite failed, skipped or missing.')
print('ALL JVM ACTUAL: ' + ' '.join(k+'='+str(v) for k,v in totals.items()))
if totals['tests'] <= 0 or any(totals[k] for k in ('failures','errors','skipped')):
    raise SystemExit('STOP: full JVM suite not fully passing.')
PY
apk_dir=app/build/outputs/apk/debug
test_dir=app/build/outputs/apk/androidTest
[[ "$(find "$apk_dir" -type f -name '*.apk' | wc -l)" -eq 1 ]] || { echo "STOP: expected 1 debug APK"; exit 3; }
[[ "$(find "$test_dir" -type f -name '*.apk' | wc -l)" -eq 1 ]] || { echo "STOP: AndroidTest APK not compiled"; exit 3; }
apk=$(find "$apk_dir" -type f -name '*.apk' -print -quit)
[[ -s "$apk" ]] || { echo "STOP: empty debug APK"; exit 3; }
out="$dest/Atelier-Time-D146-API36-Candidate.apk"
cp "$apk" "$out"
manifest="$dest/Atelier-Time-D146-API36-Manifest.txt"
{
 echo "version=D-146"
 echo "version_code=40"
 echo "branch=android-native-kotlin-compose"
 echo "source_commit=$(git rev-parse HEAD)"
 echo "apk_sha256=$(sha256sum "$out" | awk '{print $1}')"
 echo "jvm_test_state=ALL_PASSED"
 echo "androidTest=COMPILED_NOT_EXECUTED"
 echo "device_install=NOT_PERFORMED"
} > "$manifest"
echo "D-146 JVM TESTS AND BUILD SUCCESSFUL: $out"
echo "Manifest: $manifest"
echo "Private log: $log"
echo "No install, app data clear, paid cloud build, signing or Play submission."
