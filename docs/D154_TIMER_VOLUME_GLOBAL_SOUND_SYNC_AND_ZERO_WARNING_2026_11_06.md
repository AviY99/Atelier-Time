# Atelier Time — D-154 / Code48 — Timer volume ↔ global Sound ON/OFF synchronization

> **2026-11-06 — D154 FOLLOW-UP SOURCE/PREFLIGHT COMPLETED (AFTER INTERRUPTED CHAT).** Reopened current DEV and confirmed D154/code48 functionality already committed: five explicit successful selected-stream Volume actions synchronize saved global Sound preference from actual hardware volume (Media 0=OFF, Media positive=ON; Alarm stays ON/locked). Free volume-zero warning is unconditional, as in Class, and disappears after actual selected hardware level rises. Four new SoundSettings JVM source tests (24 target) and two Compose AndroidTest source methods are in GitHub; **these have not yet been executed on Samsung**. Found and fixed a leftover D153 manifest copy error in D154 local build script: output now writes `version_code=48` (not 46), matching D-154 Gradle versionCode48. Verified source contract, 5 callback sites, test imports, D154 script guards and approved-art-in-APK check statically; **not a claim of Gradle or device PASS**. **NEXT ONE command on Samsung:** `cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native && git pull --ff-only && bash tools/build_d154_sound_volume_sync_on_phone.sh`. If successful, install over existing app **without uninstall or clearing data**, test Media Mute/slider/Max+confirmation and 0% warning in Free and Class, then Sound ON/OFF and Alarm lock in Sounds; retain Stable16 frozen until explicit new acceptance. No paid cloud build, Play AAB or upload.


**Date:** 2026-11-06  
**Development:** `android-native-kotlin-compose`. **SOURCE COMMITTED; Samsung local build and user acceptance PENDING**. Do not claim APK/JVM test success until actual phone screenshot.  
**Last user-accepted stable:** **Stable16 / D153 / Code47**, immutable `stable/android-d153-qa16-ui-sound-user-accepted`, exact SHA `04270365f330af78861d54f3710048d65dadc462`, **untouched**.

## User-reported regression and intended behavior

After Stable16, user screenshot showed **Programmed/Class Timer** warning at `Media Volume 0%` but **Free Timer** showed `Media Volume 0%` without its warning. The user clarified that the underlying issue is **Sound OFF and hardware volume were treated as unrelated despite timer controls being another place to control the same Sound preference**:

> "הבעיה שכפתור sound off לא עובד נכון , אם מעלים וליום בטיימרים הוא צריך להשתנות , האזהרה תמיד קיימת אם אנחנו על וליום אפס , אם מעלים וליום במקס או בהעלאת בר אז הוא משתנה בהתאם , זה רק מיקום נוסף לשלוט באופציה."

**Root cause in frozen D153:** Free warning was gated on `classAlertVolumeMuted && soundEnabled`, while Class warning required only `classAlertVolumeMuted`. Additionally, `Mute`, the timer slider, and confirmed `Max` changed the selected phone Media/Alarm **hardware** stream but never updated the persisted `SoundSettings.timerSoundsEnabled`. Consequently raising volume from the Timer could leave the globally disabled Sound OFF, and Free warning could disappear at hardware level zero.

### Corrected user contract

1. **All five on-screen timer volume operations** (Free slider, Free Mute, Class Run slider, Class Mute, shared confirmed Max) still affect only the Media **or** Alarm channel selected in Sounds, and use `ClassVolumeControl` to read back the *actual* resulting level. Only on a successful adjustment do they synchronize the app-wide Sound ON/OFF preference via persisted `SoundSettings`. No change to the other hardware stream, selected route, stored cue sounds or saved Class.
2. **Media route:** selected hardware level **0 → global Sound OFF**. Actual selected hardware level **above 0 → global Sound ON**, including when previously switched OFF in Sounds. Both Class and Free follow this same rule. If Android quantizes a requested small percentage to zero, use the actual level and keep OFF / warning visible.
3. **Alarm route:** existing D151 policy remains — `Use sound: Alarm` forces Sound ON and disables global OFF even if its hardware volume is zero. Still show zero-volume warning when Alarm is at zero. Choosing Media restores ON/OFF control; subsequent timer volume action synchronizes it as above.
4. **Warning:** Free warning always appears when selected stream **actual volume is zero**, regardless of global Sound ON/OFF; disappears when selected volume is actually nonzero. Class warning already worked this way and remains unchanged. No warning at nonzero volume purely because Sound has been manually set OFF; users can still explicitly switch Sound OFF on Sounds tab when Media selected.
5. **Important distinction:** Sound tab still controls the app-wide cue enablement independently when the user explicitly toggles it. The synchronization occurs on successful explicit Timer slider/Max/Mute operations; changing device volume outside the app does not silently overwrite a manually selected global Sound state, although visible hardware volume and zero-warning refresh. This avoids excessive writes and unexpected undo of a deliberate Sound OFF.

## Source updates

- `SoundSettings.kt`: `afterSelectedStreamVolumeChange(actualLevel: Int)` reuses the existing Alarm-safe `setTimerSoundsEnabled(actualLevel > 0)`; validates negative input, retains cue selections and route.
- `SessionEditorScreen.kt`: shared `syncSoundAfterTimerVolume(levels: ClassVolumeLevels)` reads back the actual chosen channel and persists the updated Sound setting via existing `MelodyPreferences` state pipeline. Used in all **five** success callbacks; failures never modify global preference. Free warning no longer depends on `soundEnabled`, tagged `free_selected_volume_zero_warning`. Class warning and Max consent preserved.
- `MelodySettingsSection.kt`: help now accurately states that Timer volume controls also synchronize global Sound ON/OFF and explains Alarm lock / warning.
- `ClassVolumeControl.kt` and `RunningClassQuickControls.kt`: comment-only architectural clarification. Existing hardware-only controller and visual UI unchanged.
- `SoundSettingsTest.kt`: four new pure JVM tests (prior 20 → **24 targeted test methods**, subject to actual build verification) for Media zero OFF, subsequent positive ON, Alarm zero forcing ON, preservation / invalid-level guard.
- New `D154TimerVolumeSoundSyncUiTest.kt`: two Compose UI methods covering Sound-tab toggle reflecting programmatic zero/positive selected volume and Alarm zero lock. **Compile only** under phone builder; does not touch device audio and is **not executed** by script.
- `app/build.gradle.kts`: versionName **D-154**, versionCode **48**. Prior accepted Stable16 source untouched.
- `tools/build_d154_sound_volume_sync_on_phone.sh`: Samsung Code on the Go local builder checks 5 synchronization points, unconditional Free warning and updated Sounds help, requires 24 SoundSettings JVM tests, runs all JVM tests, compiles AndroidTest APK (without running instrumentation), builds debug APK, verifies exact approved splash SHA256 inside produced APK. **No paid CI/cloud build, install/uninstall, app-data clearing, Play signing/upload**.

## One Samsung local build command

```bash
cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native && git pull --ff-only && bash tools/build_d154_sound_volume_sync_on_phone.sh
```

**ONLY if device build reports success:** expected `/storage/emulated/0/Download/Atelier-Time-D154-API36-Candidate.apk` plus a D154 manifest/build log. User must install as an **in-place update, never uninstall**, and check:
- Media 0: warning in **both Class and Free**, Sounds global OFF after pressing Timer Mute.
- Increase Media using Class/Free slider: warning disappears once actual channel >0, Sounds global ON.
- From Media global OFF press Max and approve: hardware selected stream rises, warning disappears, Sounds global ON.
- Switch to Alarm, adjust down to zero: warning shown but global Sound ON remains locked and selected output unchanged; return Media to allow OFF.
- No unintended changes to other channel volume, Max confirmation, stored preferences or timer behavior.

**No promotion to Stable17 without explicit user acceptance.** Google Play remains FREE and initial invite-only Internal Testing; no signed AAB or submission.
