# Atelier Time — D155 / Code49 — true two-way Sound/volume synchronization and Android-determined Alarm minimum

**Date:** 2026-11-06  
**Status:** implementation committed on `android-native-kotlin-compose`, static source checks performed; **Samsung Gradle/JVM run, AndroidTest compile, APK, on-device functional/user acceptance NOT YET VERIFIED for D155**. Do not claim this candidate is working until device evidence.  
**Latest user-accepted stable:** **Stable16 / D153 / Code47** frozen at `stable/android-d153-qa16-ui-sound-user-accepted`, SHA `04270365f330af78861d54f3710048d65dadc462`, unchanged. D154 / code48 was reportedly installed by user but DOES NOT meet the agreed behavior; do not promote it.

## Corrected product requirement (user-verified)

1. **Class Timer and Free Timer have exactly the same Sound/volume rules.** Sounds ON/OFF, the single selected-stream slider, Mute and Max are distinct UI locations for the **same selected Android stream**; state updates in BOTH directions, never only from the Timer slider toward Sound.
2. **Sound OFF:** request selected stream hardware volume zero and display Sound OFF only when actual stream really reaches zero. Timer warning is visible at actual zero, even when app Sound is OFF.
3. **Sound ON:** if current actual selected stream already positive, use it. Otherwise restore **last positive actual Android volume step** previously used for that stream, persisted across application restarts; when no positive history exists, restore **25%**, clamped to at least one real positive Android step, respecting device minimum.
4. Timer Mute and slider zero (when allowed) set actual stream zero and global Sound OFF. Moving selected slider to a real positive level or confirming Max sets Sound ON, warning disappears, and sound cues can play. Actual hardware volume, not the request percent, is always used for UI reconciliation.
5. **Alarm is not universally locked by our app.** For BOTH streams, the device determines whether zero is allowed. On API28+ inspect `AudioManager.getStreamMinVolume(STREAM_ALARM/STREAM_MUSIC)`; API26–27 treat min as 0 and rely on attempted set/readback checks. If the selected Android stream min is >0, an OFF/Mute attempt cannot reach zero: the Sound OFF control is disabled based **on the actual selected stream minimum**, not simply the word Alarm; Mute shows an Android device restriction rather than claiming success. If the selected Alarm stream can reach 0, it behaves **identically to Media**, including Sound ON/OFF and zero warnings.
6. Switching sound route inside Sounds never changes the actual hardware volume of either stream: the global Sound control reflects the newly selected stream's actual volume. Audio control does not change the nonselected stream, cue assignments, Class schedules, saved data, timer algorithms or artwork.
7. On screen app startup, route switching and periodic clock refresh, reconcile the stored Sound state to the ACTUAL selected hardware volume, including fixes for D154 installs where sound preference and device volume were contradictory. No automatic hardware volume change from opening the app: only direct tap on Sound/slider/Max/Mute can change stream volume.
8. If Android rejects changing volume, leave the saved Sound state unchanged and display an error. Max remains gated behind explicit loudness confirmation; original max-restore rules remain and audio UI state is re-synced after Max restoration on Timer exit.

## Root causes of incomplete D154 correction

- `MelodySettingsSection` Sound master called only `saveSoundSettings`, so it did NOT change actual device volume; D154 sync ran only on 5 Timer-initiated actions. Sound OFF with Media positive suppressed playback and looked contradictory.
- `SoundSettings.selectOutputAlarm`, `setTimerSoundsEnabled`, `normalizedAlarmSoundOn`, `MelodyAudio.MelodyPreferences.saveSettings`, and the Sound toggle UI forced Alarm ON/disabled OFF regardless of device minimum. User explicitly corrected this mistaken assumption.
- A condition in Free Timer originally gated the warning on `soundEnabled`; D154 already removed that condition, retained in D155.

## D155 changes in DEV

- `ClassVolumeControl.kt`: one `setSelectedSoundEnabled(useAlarm, enabled)` for both streams, uses Android `getStreamMinVolume` (API28+) and real post-set readback; persistent last-positive Android volume indices per stream and 25%-fallback policy; protected min produces clear restriction; selected slider clamps to device minimum; Mute checks real zero capability; readback on Max restoration.
- `MediaSoundRestorePolicy.kt` originally committed during unfinished D155 work and now used for BOTH selected streams; 6 pure JVM tests `MediaSoundRestorePolicyTest.kt`, including last positive and fallback at small device maximum.
- `SoundSettings.kt`: remove hardcoded Alarm-ON restriction; selected stream actual zero => OFF and actual positive => ON for both routes; route change itself never changes audio hardware.
- `MelodyAudio.kt`: allow persistence of a legitimate `Alarm + Sound OFF` state when the device actually permits Alarm 0, without deleting any cue or custom settings.
- `SessionEditorScreen.kt`: `onSoundTabChange` now routes Sound toggles through the same `ClassVolumeControl`, saves state ONLY after Android readback; route change selects device stream readback; `LaunchedEffect` reconciles older persisted values/hardware buttons; both Timer slider/Mute callbacks and consented Max continue to share one sync function; resync after timer Max restoration.
- `MelodySettingsSection.kt`: adds `canMuteSelectedAudio` parameter from actual device minimum, removes hardcoded Alarm lock, clarifies behavior in Sounds Help.
- Legacy domain, preferences and Compose UI tests updated from false assumptions about Alarm; new D155 UI source covers protected vs zero-capable Alarm (compiled only in standard phone builder).
- `app/build.gradle.kts`: D155 / versionCode 49.
- `tools/build_d155_unified_sound_android_minimum_on_phone.sh`: **LOCAL ONLY** helper: source/preflight checks for both-way controls and Android minimum, full executed JVM test suite, compile AndroidTest sources only, debug APK build and approved splash SHA256 audit INSIDE APK. No Google paid Actions, no install/uninstall/data clear, no Play submission or signing. Samsung local result PENDING.

## ONE Samsung Code on the Go local build command

```bash
cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native && git pull --ff-only && bash tools/build_d155_unified_sound_android_minimum_on_phone.sh
```

**If and ONLY if actual build reports success**, candidate: `/storage/emulated/0/Download/Atelier-Time-D155-API36-Candidate.apk` plus D155 local build log and manifest. In-place install over D154; **never uninstall/clear data**. Check both Timer types and Sounds:

- Media positive → Sound ON; tap Sound OFF → Media 0 + warning in both Timers; tap Sound ON → last positive (or 25%) + no warning.
- Free Timer Mute/slider 0 → Sound OFF, warning; Class Timer slider/Max positive → Sound ON and both warning gone; repeat from other Timer and back to Sound tab.
- Alarm 0-permitted device → same full two-way operation as Media, Sound OFF allowed; Alarm minimum-protected device → minimum retained, OFF unavailable, no incorrect zero warning; device-level attempts fail safely.
- Media/Alarm route changes affect only which stream is observed/controlled, not the other stream's volume. Confirm Max still warns and saved classes/sounds survive.
- Test app restart after a mute and a positive restore; check persisted last positive value.
- Android instrumentation UI tests are **compiled but not executed automatically**. Do not declare D155 stable without explicit user approval and runtime testing.

**Google Play unchanged:** FREE app, first invited Internal Testing. No signed release AAB / Play upload / publication authorized.
