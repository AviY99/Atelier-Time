# Atelier Time — D-160 / Code 54 — Finish approval dialog follow-up

**Date:** 2026-11-13 (Asia/Jerusalem). **Status:** DEV candidate source, Samsung QA not yet run.  
**Baseline:** D-159 / Code53 was built locally (326/326 JVM pass, artwork included, AndroidTest compiled only) and user reported operation worked, but requested three visual corrections.

## Requested and implemented
1. Restore the entire `Calculated finish: dd/MM HH:mm:ss` line to the **original uniform D-158 typography**, `MaterialTheme.typography.titleMedium`. Remove the D159 small-caption/oversized-number split. No change to live seconds/date logic.
2. Remove the redundant Class name line **inside the Run approval dialog only**; the saved Class name, storage and library display are not changed.
3. Remove the extra `Use calculated finish` action under the hour/minute wheels. The existing lower `Change finish time` button becomes `Back` while wheels are open, remains enabled, and restores the live calculated time and closes the picker. After closing it becomes `Change finish time` again. This reuses the same restore behavior previously attached to `Use calculated finish`.

## Unchanged / exclusions
- The three bottom actions and existing shared button style from D159, with Back replacing Change when wheels are shown.
- Original `EndClockWheel` mechanics, validity checks/red boundary protection, `Selected finish`, `Final two Sessions`, warning of zero volume, and Confirm/Cancel behavior.
- Timing engine, audio, saved Classes, notification/FGS, artwork and previous frozen releases.
- No source edits to Stable18 `stable/android-d158-qa18-background-notifications-user-accepted`, SHA `7994dcfc2d077e68d72800400fa2086dacdc4132`. Google Play upload remains a separate approval gate; no changes there.

## Candidate and QA
- Development branch: `android-native-kotlin-compose`.
- Debug app: `com.ateliertime.android.dev`, `versionName D-160`, `versionCode 54` (separate from the Play app and D158 Code52 AAB).
- `RunEndApprovalUiTest.kt` updated to verify old calculated line, bottom action layout, Back label and behavior after a wheel change, no redundant reset action, and maintained Selected/Final/Run semantics. AndroidTest is **source only until compiled; not executed**.
- Guarded Samsung-only build helper: `android-native/tools/build_d160_finish_dialog_back_on_phone.sh`, locked to exact D160 app source tree, requires 326+ successful JVM tests including 8 D158 background tests, compiles AndroidTest, builds a DEV APK, verifies approved artwork; no install or cloud CI.

**Phone command after review:**
```bash
cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native && git pull --ff-only && bash -n tools/build_d160_finish_dialog_back_on_phone.sh && bash tools/build_d160_finish_dialog_back_on_phone.sh
```
Expected output only on PASS: `/storage/emulated/0/Download/Atelier-Time-D160-Code54-FinishDialog-DEV-QA.apk`.

**Acceptance gate:** Build on Samsung, verify UI with picker closed/open, Back resetting a changed time, original font, no class name and no additional `Use calculated finish` control; never mark Stable19 without explicit user acceptance.
