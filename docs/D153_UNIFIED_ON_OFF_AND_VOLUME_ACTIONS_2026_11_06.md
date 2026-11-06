# Atelier Time — D-153 / Code47 — unified ON/OFF and corrected timer top controls

**Date:** 2026-11-06.  
**Status:** source committed on Android DEV `android-native-kotlin-compose`; **Samsung local build/JVM execution/AndroidTest compilation/device inspection NOT YET VERIFIED for D153**.  
**Latest accepted stable:** **Stable15 / D147 / Code41** at immutable `stable/android-d147-qa15-ui-install-accepted`, SHA `c10f424c0ec000eca2dd561911934841decb4522` — unchanged.  
**Predecessor:** D152/Code46 Samsung build passed **300/300 actual JVM tests**, approved splash SHA256 verified INSIDE APK, debug build successful. User inspected D152 Sound/Free Timer screenshots and stated **"הכל תקין"** (functional), but requested two additional visual refinements; D152 was **not** separately promoted to Stable16.

## User's corrected UI requirements (authoritative)

1. **ON/OFF:** existing native sliding switches in Editor, Sound, and the Timer setup visually disagree with the preferred small rectangular **ON/OFF** button already shown by the running Timers. Standardize all app ON/OFF controls using the running timer look, not the native switch. Retain ON/OFF state, callbacks and Alarm-related disabled behavior.
2. **Timer top row for BOTH Class Run and Free Timer:** place **Keep screen awake** immediately alongside its **ON/OFF** control, with less empty space. **Volume is plain label text**, exactly like Keep screen awake — **NOT framed and NOT a third button**. To its right, **Max** and **Mute** are the **only two independent buttons**, with same shared 12dp rounded corners, 40dp visible height, 1dp outlines and 48dp touch targets. **NO shared frame enclosing Volume/Max/Mute and no internal separators.** The visible former `Sounds off` text becomes **Mute**; action remains selected-channel hardware volume to 0 (not persistent global Sound OFF).
3. Keep a single slider for the Media OR Alarm output stream selected only in Sounds. Max still asks for consent and affects only the selected stream, with existing restoration rules. No changes to scheduling, replay, special breaks, persisted preferences or artwork.

## Code changes on DEV

- `AtelierActionStyle.kt`: new `AtelierOnOffButton`, a 48dp touch / 40dp visual / 12dp round / 1dp border true accessible toggle (Role.Switch), with explicit `enabled` support. Colors follow Day/Night and preserve selected/disabled contrast.
- `SessionEditorScreen.kt`: Editor automatic Special Breaks ON/OFF and non-running Class Timer awake preference now use the same toggle. Original callbacks and warnings remain.
- `MelodySettingsSection.kt`: Sound master ON/OFF uses the common toggle, remains disabled while Alarm is selected; the Help text now calls the individual timer action `Mute`.
- `RunningClassQuickControls.kt`: reduces the previously fixed 83dp label width to a responsive 72dp maximum to bring Awake ON/OFF closer. Removes the entire old enclosing `running_volume_actions_border` frame and its dividers. Displays plain `Volume` text with tag `running_volume_label`, plus separate `SlimTimerAction` Max and Mute controls on the same scroll-safe row. Original callbacks `onVolumeMax` and `onSoundsOff` are untouched. Tags `class_volume_max` and `class_volume_sounds_off` intentionally retained to preserve previous UI interactions/instrumentation compatibility while displaying `Mute`.
- `app/build.gradle.kts`: D-153, versionCode 47.
- D151 and D152 AndroidTest source expectations updated for the two independent buttons; new `D153ToggleAndVolumeRowUiTest.kt` has three Compose UI cases. These are **compiled only** by the Samsung helper, not run automatically on device.
- `tools/build_d153_toggles_volume_actions_on_phone.sh` verifies the new presentation contract and legacy audio/timer safety, executes the full JVM suite, compiles AndroidTest APK, packages the debug APK and verifies exact approved splash SHA256 **inside the final APK**.

## ONE local build command, only on Samsung Code on the Go

```bash
cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native && git pull --ff-only && bash tools/build_d153_toggles_volume_actions_on_phone.sh
```

**If and only if actual Samsung output reports success:** `/storage/emulated/0/Download/Atelier-Time-D153-API36-Candidate.apk` plus D153 manifest/log. Build script does not install/uninstall, clear app data, run paid CI, sign AAB or submit to Play. Do an **in-place update**, do not uninstall; inspect Editor / Sound / idle & running Class Timer / Free Timer in Day and Night, on-screen ON/OFF proximity, plain Volume text, independent Max/Mute outlines, click areas and persistent data. Also check Sound Alarm ON lock / Media allowing OFF and selected-stream volume actions.

**Do not claim D153 passed tests, is installed, or has user acceptance until evidence exists. Do not freeze Stable16 without explicit user approval.** Google Play remains FREE and first invite-only Internal Testing.
