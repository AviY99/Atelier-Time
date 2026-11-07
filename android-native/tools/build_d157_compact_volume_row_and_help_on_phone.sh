#!/usr/bin/env bash
# D-157 / Code51: Samsung Code on the Go LOCAL build only. No cloud, install, signing, deletion.
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
for setting in 'compileSdk = 36' 'targetSdk = 36' 'versionCode = 51' 'versionName = "D-157"'; do
  grep -Fq "$setting" app/build.gradle.kts || { echo "STOP: missing $setting"; exit 2; }
done
for file in \
  app/src/main/java/com/ateliertime/domain/PhaseDisplayNames.kt \
  app/src/main/java/com/ateliertime/android/SessionTimerScreen.kt \
  app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt \
  app/src/main/java/com/ateliertime/android/RunningClassQuickControls.kt \
  app/src/main/java/com/ateliertime/android/AtelierActionStyle.kt \
  app/src/main/java/com/ateliertime/android/AtelierDesign.kt \
  app/src/main/java/com/ateliertime/android/MelodySettingsSection.kt \
  app/src/main/java/com/ateliertime/android/PresetLibrarySection.kt \
  app/src/main/java/com/ateliertime/android/SlimTimerAction.kt \
  app/src/main/java/com/ateliertime/android/FreeCountdownSection.kt \
  app/src/main/java/com/ateliertime/android/ClassVolumeControl.kt \
  app/src/main/java/com/ateliertime/android/AtelierSlimSlider.kt \
  app/src/main/java/com/ateliertime/domain/VolumeSliderMath.kt \
  app/src/main/java/com/ateliertime/domain/MediaSoundRestorePolicy.kt \
  app/src/main/java/com/ateliertime/domain/SoundSettings.kt \
  app/src/main/java/com/ateliertime/android/MelodyAudio.kt \
  app/src/test/java/com/ateliertime/domain/D146GroupTimerLabelsTest.kt \
  app/src/test/java/com/ateliertime/domain/D148VolumeSliderMathTest.kt \
  app/src/test/java/com/ateliertime/domain/SoundSettingsTest.kt \
  app/src/test/java/com/ateliertime/domain/MediaSoundRestorePolicyTest.kt \
  app/src/test/java/com/ateliertime/domain/D156DeviceMinimumVolumeTest.kt \
  app/src/androidTest/java/com/ateliertime/android/MelodyPreferencesTest.kt \
  app/src/androidTest/java/com/ateliertime/android/D146CompactUserFeedbackUiTest.kt \
  app/src/androidTest/java/com/ateliertime/android/D147RunningClassQuickControlsUiTest.kt \
  app/src/androidTest/java/com/ateliertime/android/D148OptionAControlsUiTest.kt \
  app/src/androidTest/java/com/ateliertime/android/D148SharedSoundCustomUiTest.kt \
  app/src/androidTest/java/com/ateliertime/android/D149GlobalSoundControlsUiTest.kt \
  app/src/androidTest/java/com/ateliertime/android/D150SelectedVolumeBarUiTest.kt \
  app/src/androidTest/java/com/ateliertime/android/D151SlimControlsAlarmHelpUiTest.kt \
  app/src/androidTest/java/com/ateliertime/android/D152UnifiedButtonsUiTest.kt \
  app/src/androidTest/java/com/ateliertime/android/D153ToggleAndVolumeRowUiTest.kt \
  app/src/androidTest/java/com/ateliertime/android/D154TimerVolumeSoundSyncUiTest.kt \
  app/src/androidTest/java/com/ateliertime/android/D155SelectedStreamSoundUiTest.kt \
  app/src/androidTest/java/com/ateliertime/android/D156VolumeHitTargetUiTest.kt \
  app/src/androidTest/java/com/ateliertime/android/D157UnifiedVolumeRowAndHelpUiTest.kt; do
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
    "assertIsOn": r"\.assertIsOn\s*\(",
    "assertIsOff": r"\.assertIsOff\s*\(",
    "assertIsEnabled": r"\.assertIsEnabled\s*\(",
    "getUnclippedBoundsInRoot": r"\.getUnclippedBoundsInRoot\s*\(",
    "performScrollTo": r"\.performScrollTo\s*\(",
    "performClick": r"\.performClick\s*\(",
    "performTouchInput": r"\.performTouchInput\s*\(",
    "swipe": r"\.swipe\s*\(",
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

# D155 uses ONE shared toggle face plus SlimTimerAction faces;
# RunningClassQuickControls owns the scroll row and delegates face geometry.
for file in app/src/main/java/com/ateliertime/android/AtelierActionStyle.kt \
            app/src/main/java/com/ateliertime/android/SlimTimerAction.kt; do
  grep -Fq 'AtelierActionStyle.visibleHeight' "$file" ||
    { echo "STOP: unified 40dp visible button height missing in $file"; exit 2; }
  grep -Fq 'AtelierActionStyle.touchHeight' "$file" ||
    { echo "STOP: 48dp touch target missing in $file"; exit 2; }
done
grep -Fq 'AtelierActionStyle.touchHeight' app/src/main/java/com/ateliertime/android/RunningClassQuickControls.kt ||
  { echo "STOP: timer row lost full 48dp touch height"; exit 2; }
for token in 'RoundedCornerShape(12.dp)' 'visibleHeight = 40.dp' 'touchHeight = 48.dp' 'minimumWidth = 56.dp' 'stroke = 1.dp'; do
  grep -Fq "$token" app/src/main/java/com/ateliertime/android/AtelierActionStyle.kt ||
    { echo "STOP: action token missing: $token"; exit 2; }
done
for button in AtelierButton AtelierOutlinedButton AtelierTonalButton AtelierTextButton AtelierFilterChip; do
  grep -Fq "fun $button(" app/src/main/java/com/ateliertime/android/AtelierActionStyle.kt ||
    { echo "STOP: unified action wrapper missing: $button"; exit 2; }
done
for layout in SessionEditorScreen.kt MelodySettingsSection.kt PresetLibrarySection.kt AtelierDesign.kt; do
  grep -Eq 'Atelier(Button|OutlinedButton|TonalButton|TextButton|FilterChip)\(' "app/src/main/java/com/ateliertime/android/$layout" ||
    { echo "STOP: $layout not using shared action wrappers"; exit 2; }
done
if grep -Eq '(^|[^A-Za-z])(Button|OutlinedButton|FilledTonalButton|TextButton|FilterChip)\('   app/src/main/java/com/ateliertime/android/{SessionEditorScreen,MelodySettingsSection,PresetLibrarySection,AtelierDesign}.kt; then
  echo 'STOP: material action used directly in a principal screen; use AtelierActionStyle.' >&2; exit 2
fi
for token in 'running_volume_label' 'class_volume_max' 'class_volume_sounds_off' 'label = "Mute"'; do
  grep -Fq "$token" app/src/main/java/com/ateliertime/android/RunningClassQuickControls.kt ||
    { echo "STOP: missing separated Volume / Max / Mute UI: $token"; exit 2; }
done
if grep -Fq 'running_volume_actions_border' app/src/main/java/com/ateliertime/android/RunningClassQuickControls.kt; then
  echo 'STOP: old Volume group frame must be removed'; exit 2
fi
for file in RunningClassQuickControls.kt SessionEditorScreen.kt MelodySettingsSection.kt; do
  grep -Fq 'AtelierOnOffButton(' "app/src/main/java/com/ateliertime/android/$file" ||
    { echo "STOP: $file missing shared ON/OFF toggle"; exit 2; }
  if grep -Fq 'Switch(' "app/src/main/java/com/ateliertime/android/$file"; then
    echo "STOP: native Switch still used in $file"; exit 2
  fi
done
grep -Fq 'keep_screen_awake_visual' app/src/main/java/com/ateliertime/android/RunningClassQuickControls.kt ||
  { echo 'STOP: thin Awake ON/OFF visual tag missing'; exit 2; }
[[ "$(grep -Fc 'SlimTimerAction(' app/src/main/java/com/ateliertime/android/SessionTimerScreen.kt)" -eq 5 ]] ||
  { echo 'STOP: five slim Class buttons missing'; exit 2; }
[[ "$(grep -Fc 'SlimTimerAction(' app/src/main/java/com/ateliertime/android/FreeCountdownSection.kt)" -eq 4 ]] ||
  { echo 'STOP: Free preset and Start/Pause/Exit slim buttons missing'; exit 2; }
grep -Fq 'sound_output_help' app/src/main/java/com/ateliertime/android/MelodySettingsSection.kt ||
  { echo 'STOP: bottom Sounds Help missing'; exit 2; }
grep -Fq 'enabled = canMuteSelectedAudio' app/src/main/java/com/ateliertime/android/MelodySettingsSection.kt ||
  { echo 'STOP: Sound OFF availability must reflect Android hardware minimum'; exit 2; }
grep -Fq 'settings.selectOutputAlarm(useAlarm)' app/src/main/java/com/ateliertime/android/MelodySettingsSection.kt ||
  { echo 'STOP: output route control missing'; exit 2; }
for piece in 'selectOutputAlarm' 'setTimerSoundsEnabled' 'afterSelectedStreamVolumeChange'; do
  grep -Fq "$piece" app/src/main/java/com/ateliertime/domain/SoundSettings.kt ||
    { echo "STOP: shared Sound route/volume logic missing: $piece"; exit 2; }
done
grep -Fq 'getStreamMinVolume(stream(useAlarm))' app/src/main/java/com/ateliertime/android/ClassVolumeControl.kt ||
  { echo 'STOP: Android stream minimum not queried'; exit 2; }
grep -Fq 'fun setSelectedSoundEnabled(useAlarm: Boolean, enabled: Boolean)' app/src/main/java/com/ateliertime/android/ClassVolumeControl.kt ||
  { echo 'STOP: no bidirectional Sound-to-volume function'; exit 2; }
grep -Fq 'fun onSoundTabChange(updated: SoundSettings)' app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt ||
  { echo 'STOP: Sound tab bypasses shared hardware controller'; exit 2; }
grep -Fq 'setSelectedSoundEnabled(' app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt ||
  { echo 'STOP: Sounds ON/OFF is not applied to hardware'; exit 2; }
grep -Fq 'canMuteSelectedAudio = classVolumeControl.canMuteSelected(' app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt ||
  { echo 'STOP: Sound toggle does not use selected hardware minimum'; exit 2; }

grep -Fq '.size(AtelierActionStyle.visibleHeight)' app/src/main/java/com/ateliertime/android/MelodySettingsSection.kt ||
  { echo 'STOP: Sound Preview icon no longer uses shared 40dp frame.'; exit 2; }
grep -Fq 'Modifier.size(AtelierActionStyle.touchHeight)' app/src/main/java/com/ateliertime/android/MelodySettingsSection.kt ||
  { echo 'STOP: Sound Preview has lost 48dp touch area.'; exit 2; }
# D-155: ONE selected audio stream bar, no duplicate Volumes header,
# a plain Volume label and two independent Max / Mute buttons. Keep 2dp gap.
for label in 'Keep screen awake' 'Media Volume' 'Alarm Volume' 'Volume' 'Mute' 'ON' 'OFF'; do
  grep -Fq "$label" app/src/main/java/com/ateliertime/android/RunningClassQuickControls.kt ||
    { echo "STOP: D150 selected-bar UI text missing: $label"; exit 2; }
done
for control in 'running_media_volume_slider' 'running_alarm_volume_slider' 'running_volume_card' 'class_volume_max' 'class_volume_sounds_off' 'onSelectedVolumeChange: (Int) -> Unit' 'onVolumeMax: () -> Unit' 'onSoundsOff: () -> Unit'; do
  grep -Fq "$control" app/src/main/java/com/ateliertime/android/RunningClassQuickControls.kt ||
    { echo "STOP: D150 selected-bar action contract missing: $control"; exit 2; }
done
for method in 'setMediaPercent' 'setAlarmPercent' 'maxSelected' 'muteSelected' 'setSelectedSoundEnabled'; do
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
# D-155: no decorative filled cards or thick native sliders; the exact
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
# D156 updated user requirement: Max acts immediately and persists, NO warning.
[[ "$(grep -Fc 'onVolumeMax = { applySelectedVolumeMax() }' app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt)" -eq 2 ]] ||
  { echo 'STOP: both Timers must invoke the SAME immediate Max action'; exit 2; }
grep -Fq 'fun applySelectedVolumeMax()' app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt ||
  { echo 'STOP: shared immediate Max handler missing'; exit 2; }
for banned in 'restoreIfUnchanged' 'showConfirmMaxVolume' 'class_volume_max_dialog' 'class_volume_max_confirm' 'maxVolumeFromFreeTimer'; do
  if grep -Fq "$banned" app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt ||
     grep -Fq "$banned" app/src/main/java/com/ateliertime/android/ClassVolumeControl.kt; then
    echo "STOP: obsolete Max confirmation or restore rule remains: $banned"; exit 2
  fi
done
if grep -Fq 'mediaBaseline' app/src/main/java/com/ateliertime/android/ClassVolumeControl.kt ||
   grep -Fq 'alarmBaseline' app/src/main/java/com/ateliertime/android/ClassVolumeControl.kt; then
  echo 'STOP: automatic Max restoration baselines must not survive'; exit 2
fi
grep -Fq 'containerTag = "free_timer_quick_controls"' app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt ||
  { echo "STOP: Free Timer common volume controls missing"; exit 2; }
grep -Fq 'settings.setTimerSoundsEnabled(it)' app/src/main/java/com/ateliertime/android/MelodySettingsSection.kt ||
  { echo "STOP: global Sound ON/OFF guarded selector missing"; exit 2; }
grep -Fq 'useAlarmVolume = soundSettings.classAlertsUseAlarmVolume' app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt ||
  { echo "STOP: global Media/Alarm audio output missing"; exit 2; }
grep -Fq '.putBoolean("timerSoundsEnabled"' app/src/main/java/com/ateliertime/android/MelodyAudio.kt ||
  { echo "STOP: global sound enable not persisted"; exit 2; }
# D-155: choosing Media/Alarm is allowed ONLY in Sounds, not Run/Free.
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
# D156 fail-closed regression checks: Mute must ALWAYS request the real
# device minimum instead of merely showing an Alarm-minimum error. Both timer
# UIs must have reachable zero-end thumbs and 56dp hit areas.
volcontrol=app/src/main/java/com/ateliertime/android/ClassVolumeControl.kt
mathsrc=app/src/main/java/com/ateliertime/domain/VolumeSliderMath.kt
runner=app/src/main/java/com/ateliertime/android/RunningClassQuickControls.kt
slim=app/src/main/java/com/ateliertime/android/AtelierSlimSlider.kt
# Both Mute and Sound-tab OFF must request the Android minimum without
# throwing when the phone protects Alarm at a positive minimum.
grep -Fq 'val requested = if (!enabled) minimum' "$volcontrol" ||
  { echo 'STOP: Sound-tab OFF must target Android minimum'; exit 2; }
grep -Fq 'val after = setPercent(stream(useAlarm), 0)' "$volcontrol" ||
  { echo "STOP: Mute does not request selected Android stream minimum"; exit 2; }
if grep -Fq 'check(canMuteSelected(useAlarm))' "$volcontrol"; then
  echo "STOP: D155 blocked Mute on protected Alarm instead of lowering to minimum"; exit 2
fi
grep -Fq 'VolumeSliderMath.selectedTarget(percent, minIndex(!media), max)' "$volcontrol" ||
  { echo "STOP: slider bypasses Android minimum and first positive step"; exit 2; }
grep -Fq 'fun selectedTarget(percent: Int, min: Int, max: Int): Int' "$mathsrc" ||
  { echo "STOP: selected device-volume target policy missing"; exit 2; }
for part in 'steps = 0' 'compactVisual = false' 'volumeVisual = true' '.padding(horizontal = 7.dp)' '.heightIn(min = 56.dp)'; do
  grep -Fq "$part" "$runner" ||
    { echo "STOP: timer volume slider still hard to drag at zero: $part"; exit 2; }
done
grep -Fq 'compactVisual: Boolean = true' "$slim" ||
  { echo "STOP: other slim sliders no longer preserve their design"; exit 2; }
grep -Fq 'class D156VolumeHitTargetUiTest' app/src/androidTest/java/com/ateliertime/android/D156VolumeHitTargetUiTest.kt ||
  { echo "STOP: zero-thumb hit-target Compose regression source missing"; exit 2; }

# D157: source-level guard for one compact horizontal volume row in BOTH
# Timers, 12dp track, ~2x percent, true percent-aligned thumb, and no
# default Material end-stop indicator. Independent upper/lower Helps.
editor=app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt
for token in 'running_volume_stream_label' 'running_volume_percent' 'typography.headlineSmall' 'Text("Volume"' 'Text(if (useAlarmVolume) "Alarm" else "Media"' 'showZeroVolumeWarning' 'TimerVolumeZeroWarning('; do
  grep -Fq "$token" "$runner" ||
    { echo "STOP: D157 common compact volume row missing: $token"; exit 2; }
done
grep -Fq 'Canvas(Modifier.fillMaxWidth().height(12.dp))' "$slim" ||
  { echo "STOP: D157 volume track must be 12dp (three quarters of previous 16dp)"; exit 2; }
grep -Fq 'thumb = {' "$slim" ||
  { echo "STOP: D157 single true thumb must replace confusing end-stop dot"; exit 2; }
grep -Fq 'volumeVisual: Boolean = false' "$slim" ||
  { echo "STOP: D157 custom volume track must not affect other Sounds sliders"; exit 2; }
[[ "$(grep -Fc 'showZeroVolumeWarning = classAlertVolumeMuted' "$editor")" -eq 2 ]] ||
  { echo "STOP: Class and Free must use the same zero-volume warning"; exit 2; }
if grep -Fq 'Selected sound volume is zero. Free Timer completion may be silent.' "$editor" ||
   grep -Fq 'Class alert volume is at zero.' "$editor"; then
  echo "STOP: old inconsistent zero-volume warning remains"; exit 2
fi
helpsrc=app/src/main/java/com/ateliertime/android/MelodySettingsSection.kt
for token in 'showChoicesHelp' 'showOutputHelp' 'sound_choices_help' 'sound_output_help' 'title = { Text("Media and Alarm") }'; do
  grep -Fq "$token" "$helpsrc" ||
    { echo "STOP: two independent Help dialogs required: $token"; exit 2; }
done
grep -Fq 'class D157UnifiedVolumeRowAndHelpUiTest' app/src/androidTest/java/com/ateliertime/android/D157UnifiedVolumeRowAndHelpUiTest.kt ||
  { echo "STOP: D157 UI regression source missing"; exit 2; }
# D157 verify both timer warnings and dimension-safe Compose UI assertions.
ui=app/src/androidTest/java/com/ateliertime/android/D157UnifiedVolumeRowAndHelpUiTest.kt
grep -Fq 'track.width > 0.dp' "$ui" ||
  { echo "STOP: D157 Compose Dp comparison must use 0.dp (not 0f)"; exit 2; }
grep -Fq 'fun programmedTimerUsesTheSameSingleLineZeroWarningAsFree()' "$ui" ||
  { echo "STOP: Class and Free need separate shared-warning regression tests"; exit 2; }


# D155/D157 regression guard: every user-initiated Timer volume change syncs
# the persisted global Sound ON/OFF using the REAL selected hardware level.
editor=app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt
settings=app/src/main/java/com/ateliertime/domain/SoundSettings.kt
[[ "$(grep -Fc 'syncSoundAfterTimerVolume(changed)' "$editor")" -eq 5 ]] ||
  { echo "STOP: all five Free/Class Mute, slider and Max sync paths required"; exit 2; }
grep -Fq 'levels.selected(soundSettings.classAlertsUseAlarmVolume)' "$editor" ||
  { echo "STOP: selected REAL stream not reconciled to Sound ON/OFF"; exit 2; }
grep -Fq 'fun afterSelectedStreamVolumeChange(actualLevel: Int): SoundSettings' "$settings" ||
  { echo "STOP: domain volume-to-Sound state policy missing"; exit 2; }
if grep -Fq 'if (classAlertVolumeMuted && soundEnabled)' "$editor"; then
  echo 'STOP: Free Timer low-volume warning incorrectly depends on Sound toggle'; exit 2
fi
grep -Fq 'free_selected_volume_zero_warning' "$runner" ||
  { echo "STOP: Free Timer zero-volume warning missing"; exit 2; }
grep -Fq 'Some devices enforce a positive Alarm minimum.' \
  app/src/main/java/com/ateliertime/android/MelodySettingsSection.kt ||
  { echo "STOP: Sounds help does not explain device minimum"; exit 2; }
for marker in 'class D154TimerVolumeSoundSyncUiTest' 'class D155SelectedStreamSoundUiTest'; do
  grep -R -Fq "$marker" app/src/androidTest/java/com/ateliertime/android/ ||
    { echo "STOP: UI regression source missing: $marker"; exit 2; }
done
grep -Fq 'soundSettings.afterSelectedStreamVolumeChange(' app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt ||
  { echo "STOP: hardware volume does not drive Sound"; exit 2; }
if grep -Fq 'if (classAlertsUseAlarmVolume) true else enabled' app/src/main/java/com/ateliertime/domain/SoundSettings.kt; then
  echo "STOP: obsolete app-forced Alarm ON lock remains"; exit 2
fi

bash tools/audit_artwork_on_phone.sh
: "$ANDROID_HOME"
[[ -n "$ANDROID_HOME" ]] || { echo "STOP: Android SDK missing"; exit 2; }
gradle="$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle"
aapt2="$ANDROID_HOME/build-tools/36.0.0/aapt2"
[[ -f "$gradle" && -s "$aapt2" ]] || { echo "STOP: phone Gradle/AAPT2 unavailable"; exit 2; }
dest="/storage/emulated/0/Download"
mkdir -p "$dest"
log="$dest/Atelier-Time-D157-API36-Build.log"
echo "D-157: full JVM suite + debug APK + COMPILE AndroidTest only. NO INSTALL."
if ! bash "$gradle" "-Pandroid.aapt2FromMavenOverride=$aapt2" \
  :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest \
  --rerun-tasks --console=plain --no-daemon > "$log" 2>&1; then
  echo "D-157 JVM TEST / COMPILE FAILED:"
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
 'com.ateliertime.domain.SoundSettingsTest':24,
 'com.ateliertime.domain.MediaSoundRestorePolicyTest':6,
 'com.ateliertime.domain.D156DeviceMinimumVolumeTest':8,
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
out="$dest/Atelier-Time-D157-API36-Candidate.apk"
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
manifest="$dest/Atelier-Time-D157-API36-Manifest.txt"
{
 echo "version=D-157"
 echo "version_code=51"
 echo "branch=android-native-kotlin-compose"
 echo "source_commit=$(git rev-parse HEAD)"
 echo "apk_sha256=$(sha256sum "$out" | awk '{print $1}')"
 echo "jvm_test_state=ALL_PASSED"
 echo "androidTest=COMPILED_NOT_EXECUTED"
 echo "device_install=NOT_PERFORMED"
} > "$manifest"
echo "D-157 JVM TESTS AND BUILD SUCCESSFUL: $out"
echo "Manifest: $manifest"
echo "Private log: $log"
echo "No install, app data clear, paid cloud build, signing or Play submission."
