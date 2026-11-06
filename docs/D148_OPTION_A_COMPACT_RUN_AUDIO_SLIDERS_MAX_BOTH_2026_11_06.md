# Atelier Time — D-148 / versionCode 42: Approved Option A Run controls

**Date:** 2026-11-06  
**Status:** **DEV SOURCE COMMITTED; SAMSUNG LOCAL BUILD AND USER QA NOT YET RUN.**  
**Latest accepted:** Stable15 / D147 / code41 (frozen branch `stable/android-d147-qa15-ui-install-accepted`, SHA `c10f424c0ec000eca2dd561911934841decb4522`), **untouched**.  
**Working branch:** `android-native-kotlin-compose`; app `com.ateliertime.android`, versionName D-148 / versionCode 42.

## What the user explicitly approved

User reviewed four image concepts for the two top controls of the **Run** screen and selected **Option A (upper-left): compact framed card**. After viewing a slightly more compact single-option mockup they approved implementation and expressly added: **`Max both` at the far-right edge of the volume controls**.

The actual **Android Kotlin Compose** layout on Run is now:
- A small rounded `Keep Screen Awake` card with an intuitive lock icon, **ON/OFF text and colored actual Switch**. ON uses a tinted background/strong active control; OFF neutral. This continues the existing `FLAG_KEEP_SCREEN_ON` behavior for the active class/free timer, not an Android Keyguard lock.
- A compact rounded `Volumes` card immediately below, with **`Max both` at the far right** of the volume header, maintaining the **existing confirmation dialog** explaining device-wide volume, loudness, Android DND, and restoration; no silent automatic Max both.
- Two **independent real device** sliders, `Media Volume` and `Alarm Volume`, showing editable bars, numerical percentages and a **`Selected`** marker immediately alongside the selected row's percent. **Selected** Class alert stream is a darker filled row; unselected row has transparent/neutral background. Pressing the Media/Alarm row heading changes **which Android stream is used for Class cues** via the existing persisted `SoundSettings.classAlertsUseAlarmVolume`; this is *different* from changing level.
- Dragging either bar presents the intermediate percent visually. **Releasing** the bar writes the nearest supported integer hardware volume step to **that stream only** through `AudioManager.setStreamVolume(..., flags=0)` and reflects the actual hardware percentage on screen; no cross-stream changes. Hardware volume button changes are reflected by the existing running wall-clock refresh (plus immediate refresh after an in-app change). Errors from Android volume restrictions/DND surface under controls.
- Dedicated `ClassVolumeControl.setMediaPercent` / `setAlarmPercent` handle manual overrides. Pre-existing **Max both** auto-restore remains at Class exit/finish **only for streams whose app-initiated Max both setting is unchanged**. A subsequent explicit slider change of one stream relinquishes restoration ownership **for that stream only**, preserving user choice. Failed set operations report error and best-effort roll back.
- Use existing Atelier Time DAY (Studio Linen) / NIGHT (blue) theme colors, not fixed screenshot promotional purple, accessible labels/testTags, 48dp target for the toggle and Max both, narrower side margins; retain D147's small 2dp gap to running Timer. No change to the timer algorithm, class completion sounds, schedules, user saved Classes, approved artwork, Sounds configuration, duration, etc.

## Source/test changes on development only

1. `android-native/app/src/main/java/com/ateliertime/android/RunningClassQuickControls.kt`: compact card layout and both sliders, separate row selection callbacks, Max both at right.
2. `android-native/app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt`: save selected class-alert stream through existing sound preferences; write individual sliders to Android device streams; update UI immediately and record explicit volume changes; keep original Max-both confirmation.
3. `android-native/app/src/main/java/com/ateliertime/android/ClassVolumeControl.kt`: new independent manual per-stream setters with failure feedback and safe conditional restoration of earlier Max-both overrides.
4. `android-native/app/src/main/java/com/ateliertime/domain/VolumeSliderMath.kt`: pure percentage-to-hardware-step conversion.
5. `android-native/app/src/test/java/com/ateliertime/domain/D148VolumeSliderMathTest.kt`: **8 new JVM test methods** for 0/100%, nearest device step, clamp, unavailable stream and exact roundtrip across several max values.
6. `android-native/app/src/androidTest/java/com/ateliertime/android/D147RunningClassQuickControlsUiTest.kt`: adapt 2 compile-only Compose tests to Option A, preserving callback/geometry checks; `D148OptionAControlsUiTest.kt`: 2 new compile-only tests for Max both alignment, slider visibility and source selection without unintended changes.
7. `android-native/app/build.gradle.kts`: D-148/code42.
8. `android-native/tools/build_d148_selected_audio_sliders_on_phone.sh`: **Samsung Code on the Go LOCAL** full JVM test run including new 8 test methods, compile AndroidTest APK **without executing instrumentation**, build debug APK, confirm the exact already-tracked approved splash SHA256 from inside resulting APK. **No paid GitHub Actions/cloud build, no auto install/uninstall, no user data clear, no signing or Play upload.**

## NEXT — one Samsung local QA command

```bash
cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native && git pull --ff-only && bash tools/build_d148_selected_audio_sliders_on_phone.sh
```

The only permissible D148 success claim is based on the actual phone terminal output. If all pass, expect `/storage/emulated/0/Download/Atelier-Time-D148-API36-Candidate.apk`, local SHA256 manifest and private log. **Do NOT state D148 JVM PASS, Build Successful, AndroidTest executed, installed or Stable16 before screenshot and explicit user confirmation.** Historical D147 full suite was **283/283 PASS** and user-accepted as Stable15; D148 adds eight pure JVM tests (291 anticipated, not verified).

After a successful local build and separately authorized in-place installation, inspect the two stacked cards on real narrow phone in DAY and NIGHT; colored ON/OFF, selected/deselected row dark/transparent, both percentage bars independently draggable, Selected switches/persists, alarm vs media sound route, manual volume on Class exit, existing Max-both confirmation and conditional restore, 25/5/Repeat Last and other timer invariants, and preservation of Saved Classes/Sounds/diagnostic history. Do not touch frozen Stable15.

**Play commercial/distribution decisions unchanged:** FREE, initial invite-only INTERNAL TESTING. No signed AAB, Play Console upload or public release established.
