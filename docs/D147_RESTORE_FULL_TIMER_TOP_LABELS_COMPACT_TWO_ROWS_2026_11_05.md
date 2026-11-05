# D-147 / versionCode 41 — readable running-Timer top controls

**Status:** Android DEV **source committed**, local Samsung compile / tests / debug APK / device QA **PENDING**.  
**Date:** 2026-11-05.  
**Frozen accepted:** Stable14 / D145 / code39, `stable/android-d145-qa14-field-accepted`, HEAD `73bf60996234f739243b3974ac1d02c38ad065ba` (untouched).  
**D146/code40 before this:** user-supplied Samsung CoGo screenshot **283/283 full executed JVM tests PASS** (0 failures/errors/skips); `APK ARTWORK INCLUDED PASS` verifies approved PNG inside debug APK; `D-146 JVM TESTS AND BUILD SUCCESSFUL`. AndroidTest **compiled only, not run**. User reports the app runs correctly **except the top controls' abbreviated labels are unclear**. No stable acceptance for D146/147 declared.

## User's D147 feedback
"בהרצה הכל תקין אך הכיתוב לשורות העליונות לא ברור, הכיתוב צריך להיות הכיתוב הקודם, אפשר להכניס ב-2 שורות אבל צפופות עד כמה שאפשר ולצמצם את המרחק גם למסך ההרצה שמתחתם".

**Implemented exclusively in `android-native-kotlin-compose`:**
1. Replace D146's opaque one-line `Screen on`, `M … · A …` shorthand with **TWO compact, tightly adjacent rows** restoring the ORIGINAL readable English strings: row 1 = `Keep screen awake during timers` / `Uses more battery` plus the existing screen awake switch; row 2 = `Class sound volume` / `Media N% · Alarm N% · using Media/Alarm` plus the unchanged `Max both` action. No omitted percentages or battery explanation. Text may grow vertically for larger accessibility fonts; don't reduce hit area below 48dp. Rows have 0dp extra spacing. Same preferences, volume behavior and Max both confirmation; no runtime timer/audio scheduling changes.
2. Pull running-class top controls closer to the Timer view: parent Timer column gap **8dp → 2dp while running/paused**; `SessionTimerScreen` upper inset **16dp → 2dp**, retaining side/bottom padding and the timer's internal header/graph layout.
3. New presentation-only composable `android-native/app/src/main/java/com/ateliertime/android/RunningClassQuickControls.kt`, with stable UI tags. Root `SessionEditorScreen.kt` delegates only the UI while supplying current values and callbacks. `SessionTimerScreen.kt` narrows its top padding only. D147 code41 in `app/build.gradle.kts`.
4. Two new **compile-only** Android Compose UI tests `D147RunningClassQuickControlsUiTest.kt` verify original full labels, two adjacent >=48dp rows and intact Screen awake / Max both callbacks; AndroidTest APK is compiled but tests **not automatically executed**. Full JVM suite + D146 group labels + D145 timer regressions remain mandatory.
5. New script `android-native/tools/build_d147_readable_timer_top_controls_on_phone.sh` preserves the correct offline Git-based automatic restoration of the approved artwork if absent (never overwrite altered image), SHA256 audit **inside** the actual APK, full test gate, private logs and a local debug APK. No GitHub Actions/cloud charges, app install/uninstall, data clearing, signing or Play upload.

## One local test/build command
```bash
cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native && git pull --ff-only && bash tools/build_d147_readable_timer_top_controls_on_phone.sh
```

**ONLY after successful local test/build** expect `Downloads/Atelier-Time-D147-API36-Candidate.apk`, `Atelier-Time-D147-API36-Manifest.txt`, `Atelier-Time-D147-API36-Build.log`. Do not claim an execution PASS until a screenshot shows it. After device installation, the user should inspect actual readable labels, 2-row tight spacing and gap to Timer, in both Day/Night; preserve all app data.

**Google Play:** app remains FREE, first distribution invite-only Internal Testing. No signed Play AAB or Console upload; source update does not authorize publishing. **Latest stable still Stable14** pending explicit acceptance of later user-feedback checkpoint.
