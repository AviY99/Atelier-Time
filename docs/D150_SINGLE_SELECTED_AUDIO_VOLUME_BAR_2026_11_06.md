# Atelier Time — D-150 / Code44: One selected Sound-volume bar in Class Run and Free Timer

**Date:** 2026-11-06  
**Status:** **DEV SOURCE COMMITTED; local Samsung build/JVM/device QA PENDING.**  
**Code branch:** `android-native-kotlin-compose`; app `D-150`, versionCode **44**, compile/target API36, appId `com.ateliertime.android`.  
**Last frozen accepted:** Stable15 / D147 / code41 at `stable/android-d147-qa15-ui-install-accepted`, SHA `c10f424c0ec000eca2dd561911934841decb4522`, unchanged.  
**D149 / code43 evidence:** user screenshot shows **297/297 executed JVM tests PASS**, 0 failures/errors/skips, `APK ARTWORK INCLUDED PASS` (approved SHA256 bytes inside compiled APK), and `D-149 JVM TESTS AND BUILD SUCCESSFUL`. User reports everything functions properly, then requested additional compact UI refinements; **D149 was not expressly frozen as a new stable**.

## Authoritative D150 user request, correcting the previous dual-stream interpretation

The user clarified:
> "לא , מאחר ולא ברור אז יהיה כפתור אחד volume max ושני sounds off שלא משנה מצב אלא רק משפיע על מצב הבאר וכדי לפשט יותר הבאר שיופיע יהיה אחד והוא זה שנבחר ב sounds. הבחירה בין מדיה ל alarm תקרה רק ב sounds"

**Mandatory behavior in BOTH Run and Free Timer:**

1. **Only one slider**: use exactly the Android volume channel chosen in **Sounds → Use sound → Media/Alarm**. Display `Media Volume` or `Alarm Volume` plus numerical percentage immediately above the same slim `AtelierSlimSlider` style used in Sound. No second slider, no `Volumes` header, no direct output-channel selector in Run/Free. The global selection remains persisted in Sounds, as in D149.
2. **One compact action row** above the bar: `Keep screen awake` (colored ON/OFF control), **`Volume max`** and **`Sounds off`** — two separate one-shot volume buttons, not a cycling switch. Keep >=48dp touch targets and restrained small-screen layout.
3. **Volume max**: after the **existing loudness confirmation** (now correctly describing the *selected channel only*), set ONLY that selected Android Media **or** Alarm stream to the device maximum. Do NOT touch the other stream. If no manual change occurs afterward, preserve the original Max-volume conditional restoration of that one stream at Class end/exit or Free Timer exit.
4. **Sounds off**: set ONLY that same displayed/selected Android hardware volume bar to **0%**, and immediately refresh its percentage. It **does not change** the persisted Sounds tab `Sound ON/OFF` toggle, which remains independently controlled by the user; it also **does not change** the global Media/Alarm channel selection. The nonselected channel's volume is untouched. A deliberate mute supersedes prior Max-volume restoration ownership on the selected channel, so it is not silently unmuted at class end.
5. Dragging the single bar still changes ONLY the selected Android audio stream on finger release, using existing `ClassVolumeControl.setMediaPercent` / `setAlarmPercent`, with Android DND/volume errors shown. The selected stream bar reflects hardware-button changes on the running UI tick. Screen awake behavior and source state remain unchanged.
6. **No additional Sound tab controls** were added or removed. `Sound ON/OFF`, `Use sound Media/Alarm`, Shared/Separate sound and Custom duration only remain exactly as in D149. No timer algorithm/sound pattern changes, saved Classes/Sounds/diagnostic history edits or artwork changes.

## Actual DEV implementation (user acceptance still pending)

- `RunningClassQuickControls.kt`: single `AtelierSlimSlider` composable invocation, dynamically tagged `running_media_volume_slider` **or** `running_alarm_volume_slider`, `Keep screen awake` colored ON/OFF, `Volume max`, `Sounds off`; source selection not exposed; shorter row-to-bar spacing and narrower horizontal inset.
- `SessionEditorScreen.kt`: Free Timer + Class Run each pass just `selectedVolumePercent` and one `onSelectedVolumeChange`; `Volume max` invokes a revised confirmation tied to the currently Sound-selected stream; `Sounds off` invokes `muteSelected`; neither modifies `SoundSettings.timerSoundsEnabled` nor `SoundSettings.classAlertsUseAlarmVolume`. Source-only selection remains in `MelodySettingsSection.kt`.
- `ClassVolumeControl.kt`: new `maxSelected(useAlarm)` / `muteSelected(useAlarm)` methods. The obsolete public `maxBoth` / `muteBoth` actions are removed; preserve per-channel conditional restoration. Failing Android restrictions produce visible errors.
- `app/build.gradle.kts`: D-150/code44; no package/SDK/signing changes.
- `D147RunningClassQuickControlsUiTest.kt`, `D148OptionAControlsUiTest.kt`, `D149GlobalSoundControlsUiTest.kt`: historical Compose instrumentation sources adapted to one bar and the new actions, **COMPILED ONLY**.
- **New** `D150SelectedVolumeBarUiTest.kt` (3 Compose test methods): only the selected bar exists; max/mute independent callbacks; externally changing Sounds selection switches bar without clicking a selector in Run; identical controls in Free. AndroidTest compiled but **not executed** by phone runner.
- **New local-only script:** `android-native/tools/build_d150_one_selected_volume_bar_on_phone.sh`. Branch/version/asset checks, static one-slider and selected-stream-only gates, entire JVM suite including 17 SoundSettings and 8 VolumeSliderMath cases, `assembleDebug` plus **compile-only** `assembleDebugAndroidTest`, SHA256 comparison to approved GitHub PNG embedded inside result APK, manifest and private log. No GitHub paid CI, no install/uninstall, no data wipe, no signed AAB or Play submission.

## Next action: one Samsung Code on the Go command

```bash
cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native && git pull --ff-only && bash tools/build_d150_one_selected_volume_bar_on_phone.sh
```

If and **only if** that finishes successfully, expect `/storage/emulated/0/Download/Atelier-Time-D150-API36-Candidate.apk`, `Atelier-Time-D150-API36-Manifest.txt`, and a private Build.log. User should send the final terminal screenshot before any claim of D150 tests/build success. Do not ask user to reinstall earlier D149, recover art manually, run paid GitHub Actions or clear app data.

**Play decision unchanged:** app is FREE; initial distribution by personally invited Internal Testing only. Stable15 remains latest frozen accepted; no signed release AAB or store publication.
