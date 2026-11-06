#!/usr/bin/env bash
# D-151 / Code45: Samsung Code on the Go LOCAL build only. No cloud, install, signing, deletion.
set -euo pipefail
cd "$(dirname "$0")/.."
[[ "$(git branch --show-current)" == "android-native-kotlin-compose" ]] || { echo "STOP: wrong branch"; exit 2; }
# The approved splash is versioned in GitHub and already stored in the local
# repository objects after git pull. Restore ONLY if the worktree copy is
# missing (the user's phone showed this exact tracked deletion). Never copy
# unrelated phone files, overwrite a modified image, or clear app data.
art="app/src/main/assets/atelier_splash_approved_d139.png"
expected_blob="8950a9c509bbc2c2bac02d18c0b9bfdfca06eabc"
# Git object path is relative to REPOSITORY ROOT, not android-native/ CWD.
# The working-tree art path below is still relative to android-native/.
repo_blob="$(git rev-parse "HEAD:android-native/$art" 2>/dev/null || true)"
if [[ "$repo_blob" != "$expected_blob" ]]; then
  echo "STOP: expected approved splash Git blob is not in this checkout." >&2
  exit 2
fi
if [[ ! -e "$art" && ! -L "$art" ]]; then
  echo "Restoring GitHub-tracked approved splash from this commit for APK packaging..."
  git restore --source=HEAD --worktree -- "$art"
fi
if [[ ! -f "$art" || -L "$art" ]] ||
   [[ "$(git hash-object -- "$art")" != "$expected_blob" ]]; then
  echo "STOP: splash is missing, altered or not the approved Git image; refusing to replace local edits." >&2
  exit 2
fi
echo "APPROVED SPLASH: tracked original is available to Gradle assets packaging."

[[ -z "$(git status --porcelain --untracked-files=no)" ]] || { echo "STOP: local source changed"; git status --short; exit 2; }
[[ -z "$(git ls-files --others --exclude-standard -- app/src tools)" ]] || { echo "STOP: untracked source"; exit 2; }
for setting in 'compileSdk = 36' 'targetSdk = 36' 'versionCode = 45' 'versionName = "D-151"'; do
  grep -Fq "$setting" app/build.gradle.kts || { echo "STOP: missing $setting"; exit 2; }
done
for file in \
  app/src/main/java/com/ateliertime/domain/PhaseDisplayNames.kt \
  app/src/main/java/com/ateliertime/android/SessionTimerScreen.kt \
  app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt \
  app/src/main/java/com/ateliertime/android/RunningClassQuickControls.kt \
  app/src/main/java/com/ateliertime/android/SlimTimerAction.kt \
  app/src/main/java/com/ateliertime/android/FreeCountdownSection.kt \
  app/src/main/java/com/ateliertime/android/ClassVolumeControl.kt \
  app/src/main/java/com/ateliertime/domain/VolumeSliderMath.kt \
  app/src/main/java/com/ateliertime/domain/SoundSettings.kt \
  app/src/main/java/com/ateliertime/android/MelodyAudio.kt \
  app/src/main/java/com/ateliertime/android/MelodySettingsSection.kt \
  app/src/test/java/com/ateliertime/domain/D146GroupTimerLabelsTest.kt \
  app/src/test/java/com/ateliertime/domain/D148VolumeSliderMathTest.kt \
  app/src/test/java/com/ateliertime/domain/SoundSettingsTest.kt \
  app/src/androidTest/java/com/ateliertime/android/MelodyPreferencesTest.kt \
  app/src/androidTest/java/com/ateliertime/android/D146CompactUserFeedbackUiTest.kt \
  app/src/androidTest/java/com/ateliertime/android/D147RunningClassQuickControlsUiTest.kt \
  app/src/androidTest/java/com/ateliertime/android/D148OptionAControlsUiTest.kt \
  app/src/androidTest/java/com/ateliertime/android/D148SharedSoundCustomUiTest.kt \
  app/src/androidTest/java/com/ateliertime/android/D149GlobalSoundControlsUiTest.kt \
  app/src/androidTest/java/com/ateliertime/android/D150SelectedVolumeBarUiTest.kt \
  app/src/androidTest/java/com/ateliertime/android/D151SlimControlsAlarmHelpUiTest.kt; do
  [[ -s "$file" ]] || { echo "STOP: missing $file"; exit 2; }
done
# Guard against missing Compose UI-test Kotlin extension imports before
# running costly Gradle builds (D151 encountered this in three test sources).
python3 - <<'PY'
from pathlib import Path
import re, sys
root = Path("app/src/androidTest")
required = {
    "assertIsNotEnabled": r"\.assertIsNotEnabled\s*\(",
    "assertIsEnabled": r"\.assertIsEnabled\s*\(",
    "getUnclippedBoundsInRoot": r"\.getUnclippedBoundsInRoot\s*\(",
    "performScrollTo": r"\.performScrollTo\s*\(",
    "performClick": r"\.performClick\s*\(",
    "onNodeWithTag": r"\.onNodeWithTag\s*\(",
    "onNodeWithText": r"\.onNodeWithText\s*\(",
    "dp": r"\b\d+\.dp\b",
}
issues = []
for p in root.rglob("*.kt"):
    source = p.read_text(encoding="utf-8")
    for method, pattern in required.items():
        if not re.search(pattern, source):
            continue
        imp = ("androidx.compose.ui.unit.dp" if method == "dp"
               else "androidx.compose.ui.test." + method)
        if not re.search(r"^import\s+" + re.escape(imp) + r"\s*$",
                         source, re.MULTILINE):
            issues.append(f"{p}: missing import {imp}")
if issues:
    print("STOP: AndroidTest Compose Kotlin imports missing:", file=sys.stderr)
    for issue in issues:
        print(issue, file=sys.stderr)
    sys.exit(2)
print("ANDROIDTEST IMPORT PREFLIGHT PASS")
PY

grep -Fq 'class_controls_row' app/src/main/java/com/ateliertime/android/SessionTimerScreen.kt
grep -Fq 'class_controls_help' app/src/main/java/com/ateliertime/android/SessionTimerScreen.kt

# D-151 visual acceptance guardrails: thin 34dp visual buttons, all 48dp
# interactive, one shared Awake/Volume row in Run and Free, and bottom Help.
for file in app/src/main/java/com/ateliertime/android/RunningClassQuickControls.kt \
            app/src/main/java/com/ateliertime/android/SlimTimerAction.kt; do
  grep -Fq 'height(34.dp)' "$file" || { echo "STOP: thin 34dp visual control missing in $file"; exit 2; }
  grep -Fq 'height(48.dp)' "$file" || { echo "STOP: 48dp touch-area wrapper missing in $file"; exit 2; }
done
grep -Fq 'running_volume_actions_border' app/src/main/java/com/ateliertime/android/RunningClassQuickControls.kt ||
  { echo 'STOP: thin Volume group border missing'; exit 2; }
grep -Fq 'keep_screen_awake_visual' app/src/main/java/com/ateliertime/android/RunningClassQuickControls.kt ||
  { echo 'STOP: thin Awake ON/OFF visual tag missing'; exit 2; }
[[ "$(grep -Fc 'SlimTimerAction(' app/src/main/java/com/ateliertime/android/SessionTimerScreen.kt)" -eq 5 ]] ||
  { echo 'STOP: five slim Class buttons missing'; exit 2; }
[[ "$(grep -Fc 'SlimTimerAction(' app/src/main/java/com/ateliertime/android/FreeCountdownSection.kt)" -eq 4 ]] ||
  { echo 'STOP: Free preset and Start/Pause/Exit slim buttons missing'; exit 2; }
grep -Fq 'sound_output_help' app/src/main/java/com/ateliertime/android/MelodySettingsSection.kt ||
  { echo 'STOP: bottom Sounds Help missing'; exit 2; }
grep -Fq 'enabled = !settings.classAlertsUseAlarmVolume' app/src/main/java/com/ateliertime/android/MelodySettingsSection.kt ||
  { echo 'STOP: Alarm mode no longer locks Sound ON'; exit 2; }
grep -Fq 'settings.selectOutputAlarm(useAlarm)' app/src/main/java/com/ateliertime/android/MelodySettingsSection.kt ||
  { echo 'STOP: selecting Alarm no longer forces Sound ON'; exit 2; }
for piece in 'selectOutputAlarm' 'setTimerSoundsEnabled' 'normalizedAlarmSoundOn'; do
  grep -Fq "$piece" app/src/main/java/com/ateliertime/domain/SoundSettings.kt ||
    { echo "STOP: Alarm Sound ON safety missing: $piece"; exit 2; }
done
grep -Fq ').normalizedAlarmSoundOn()' app/src/main/java/com/ateliertime/android/MelodyAudio.kt ||
  { echo 'STOP: legacy Alarm OFF preferences migration missing'; exit 2; }

grep -Fq 'OutlinedIconButton(' app/src/main/java/com/ateliertime/android/MelodySettingsSection.kt
# D-151: ONE selected audio stream bar, no duplicate Volumes header,
# separate Volume max and Sounds off buttons. Keep 2dp gap to Timer.
for label in 'Keep screen awake' 'Media Volume' 'Alarm Volume' 'Volume' 'Sounds' 'ON' 'OFF'; do
  grep -Fq "$label" app/src/main/java/com/ateliertime/android/RunningClassQuickControls.kt ||
    { echo "STOP: D150 selected-bar UI text missing: $label"; exit 2; }
done
for control in 'running_media_volume_slider' 'running_alarm_volume_slider' 'running_volume_card' 'class_volume_max' 'class_volume_sounds_off' 'onSelectedVolumeChange: (Int) -> Unit' 'onVolumeMax: () -> Unit' 'onSoundsOff: () -> Unit'; do
  grep -Fq "$control" app/src/main/java/com/ateliertime/android/RunningClassQuickControls.kt ||
    { echo "STOP: D150 selected-bar action contract missing: $control"; exit 2; }
done
for method in 'setMediaPercent' 'setAlarmPercent' 'maxSelected' 'muteSelected' 'restoreIfUnchanged'; do
  grep -Fq "$method" app/src/main/java/com/ateliertime/android/ClassVolumeControl.kt ||
    { echo "STOP: missing device volume control: $method"; exit 2; }
done
# Sounds tab: shorter button; shared Class A/B/C + Free 5s/10s/Custom.
grep -Fq 'Separate sounds' app/src/main/java/com/ateliertime/android/MelodySettingsSection.kt ||
  { echo 'STOP: compact Separate sounds label missing'; exit 2; }
for control in 'Same sound for all' 'sound_shared_mode_' 'sound_shared_custom_length_slider' 'sound_master_enabled' 'sound_output_media' 'sound_output_alarm' 'Use sound'; do
  grep -Fq "$control" app/src/main/java/com/ateliertime/android/MelodySettingsSection.kt ||
    { echo "STOP: shared 5/10/Custom UI missing: $control"; exit 2; }
done
for method in 'selectSharedCustomDuration' 'setSharedCustomDuration' 'setSharedCustomVolumePercent' 'playbackVolumePercentFor'; do
  grep -Fq "$method" app/src/main/java/com/ateliertime/domain/SoundSettings.kt ||
    { echo "STOP: shared Custom model missing: $method"; exit 2; }
done
grep -Fq 'playbackVolumePercent = soundSettings.playbackVolumePercentFor(' app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt ||
  { echo 'STOP: sound playback gain call sites missing'; exit 2; }
# D-151: no decorative filled cards or thick native sliders; the exact
# same thin sound-style control is available in Free Timer and in Class Run.
for fragment in 'AtelierSlimSlider(' 'Keep screen awake' 'running_media_volume_slider' 'running_alarm_volume_slider' 'onVolumeMax' 'onSoundsOff'; do
  grep -Fq "$fragment" app/src/main/java/com/ateliertime/android/RunningClassQuickControls.kt ||
    { echo "STOP: slim common Timer control missing: $fragment"; exit 2; }
done
if grep -Fq 'Surface(' app/src/main/java/com/ateliertime/android/RunningClassQuickControls.kt ||
   grep -Fq '"🔊"' app/src/main/java/com/ateliertime/android/RunningClassQuickControls.kt; then
  echo "STOP: decorative Run cards/emoji reintroduced"; exit 2
fi
if grep -Fq 'sound_shared_custom_volume_slider' app/src/main/java/com/ateliertime/android/MelodySettingsSection.kt; then
  echo "STOP: removed Custom gain slider reintroduced"; exit 2
fi
# Both Timer layouts must use the same single slider and selected-channel actions.
if grep -Fq 'onChooseVolume' app/src/main/java/com/ateliertime/android/RunningClassQuickControls.kt ||
   grep -Fq 'fun muteBoth(' app/src/main/java/com/ateliertime/android/ClassVolumeControl.kt ||
   grep -Fq 'fun maxBoth(' app/src/main/java/com/ateliertime/android/ClassVolumeControl.kt; then
  echo 'STOP: old dual-stream behavior is still accessible.' >&2; exit 2
fi
[[ "$(grep -Fc 'AtelierSlimSlider(' app/src/main/java/com/ateliertime/android/RunningClassQuickControls.kt)" -eq 1 ]] ||
  { echo 'STOP: expected exactly ONE volume slider.' >&2; exit 2; }
[[ "$(grep -Fc 'onSelectedVolumeChange =' app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt)" -eq 2 ]] ||
  { echo 'STOP: selected bar must be wired in Run AND Free.' >&2; exit 2; }
[[ "$(grep -Fc 'classVolumeControl.muteSelected(' app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt)" -eq 2 ]] ||
  { echo 'STOP: selected sound mute missing in Run/Free.' >&2; exit 2; }
grep -Fq 'classVolumeControl.maxSelected(' app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt ||
  { echo 'STOP: Volume max must only raise selected stream.' >&2; exit 2; }
grep -Fq 'Set $selectedName volume to maximum?' app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt ||
  { echo 'STOP: selected-channel loudness consent missing.' >&2; exit 2; }
grep -Fq 'containerTag = "free_timer_quick_controls"' app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt ||
  { echo "STOP: Free Timer common volume controls missing"; exit 2; }
grep -Fq 'settings.setTimerSoundsEnabled(it)' app/src/main/java/com/ateliertime/android/MelodySettingsSection.kt ||
  { echo "STOP: global Sound ON/OFF guarded selector missing"; exit 2; }
grep -Fq 'useAlarmVolume = soundSettings.classAlertsUseAlarmVolume' app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt ||
  { echo "STOP: global Media/Alarm audio output missing"; exit 2; }
grep -Fq '.putBoolean("timerSoundsEnabled"' app/src/main/java/com/ateliertime/android/MelodyAudio.kt ||
  { echo "STOP: global sound enable not persisted"; exit 2; }
# D-151: choosing Media/Alarm is allowed ONLY in Sounds, not Run/Free.
grep -Fq 'settings.selectOutputAlarm(useAlarm)' app/src/main/java/com/ateliertime/android/MelodySettingsSection.kt ||
  { echo 'STOP: Sounds tab guarded Media/Alarm selection missing'; exit 2; }
if grep -Fq 'onChooseVolume' app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt; then
  echo 'STOP: Media/Alarm is selectable outside Sounds.' >&2; exit 2
fi
grep -Fq 'if (runningClass) 2.dp else 8.dp' app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt ||
  { echo 'STOP: running-parent spacing not compact'; exit 2; }
grep -Fq 'top = 2.dp, bottom = 16.dp' app/src/main/java/com/ateliertime/android/SessionTimerScreen.kt ||
  { echo 'STOP: timer header top gap not compact'; exit 2; }
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
log="$dest/Atelier-Time-D151-API36-Build.log"
echo "D-151: full JVM suite + debug APK + COMPILE AndroidTest only. NO INSTALL."
if ! bash "$gradle" "-Pandroid.aapt2FromMavenOverride=$aapt2" \
  :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest \
  --rerun-tasks --console=plain --no-daemon > "$log" 2>&1; then
  echo "D-151 JVM TEST / COMPILE FAILED:"
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
 'com.ateliertime.domain.SoundSettingsTest':20,
 'com.ateliertime.domain.D148VolumeSliderMathTest':8,
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
out="$dest/Atelier-Time-D151-API36-Candidate.apk"
cp "$apk" "$out"
# A source file alone is insufficient proof: verify the final distributed APK
# contains the EXACT approved bytes at the Android assets path.
python3 - "$out" "$art" <<'PY'
import hashlib, pathlib, sys, zipfile
apk, asset = sys.argv[1:]
entry = "assets/atelier_splash_approved_d139.png"
def digest(stream):
    h = hashlib.sha256()
    while True:
        part = stream.read(1024 * 1024)
        if not part:
            break
        h.update(part)
    return h.hexdigest()
with open(asset, "rb") as f:
    source_sha = digest(f)
with zipfile.ZipFile(apk) as package:
    if entry not in package.namelist():
        raise SystemExit("STOP: approved splash missing from final APK.")
    with package.open(entry) as file:
        packaged_sha = digest(file)
if source_sha != "c7d69a52c53a86738cc416b4c29eb621f8792cf367539732ce0ffa3639d8460d":
    raise SystemExit("STOP: approved original has unexpected SHA256.")
if packaged_sha != source_sha:
    raise SystemExit("STOP: packaged artwork differs from approved original.")
print("APK ARTWORK INCLUDED PASS: assets/atelier_splash_approved_d139.png, SHA256 matched.")
PY
manifest="$dest/Atelier-Time-D151-API36-Manifest.txt"
{
 echo "version=D-151"
 echo "version_code=45"
 echo "branch=android-native-kotlin-compose"
 echo "source_commit=$(git rev-parse HEAD)"
 echo "apk_sha256=$(sha256sum "$out" | awk '{print $1}')"
 echo "jvm_test_state=ALL_PASSED"
 echo "androidTest=COMPILED_NOT_EXECUTED"
 echo "device_install=NOT_PERFORMED"
} > "$manifest"
echo "D-151 JVM TESTS AND BUILD SUCCESSFUL: $out"
echo "Manifest: $manifest"
echo "Private log: $log"
echo "No install, app data clear, paid cloud build, signing or Play submission."
