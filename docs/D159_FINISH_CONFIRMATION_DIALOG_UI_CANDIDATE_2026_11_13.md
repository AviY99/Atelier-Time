# Atelier Time — D-159 / Code 53 — Class finish confirmation dialog UI candidate

**Date:** 2026-11-13 (Asia/Jerusalem). **Status: DEV SOURCE ONLY, NOT BUILT OR DEVICE-ACCEPTED.**

## Baseline and safety
- Previously approved latest frozen release remains **Stable 18 / D-158 / Code 52** on `stable/android-d158-qa18-background-notifications-user-accepted`, commit `7994dcfc2d077e68d72800400fa2086dacdc4132`.
- Google Play Internal Testing was last user-reported at Stable 17 / D-157 / Code 51. The locally signed D158 Code52 AAB remains separate, **not uploaded**.
- This proposed UI-only improvement is implemented on **`android-native-kotlin-compose`** with `versionName="D-159"`, `versionCode=53` and debug-only package suffix `.dev`. Never edit frozen branches or use paid GitHub Actions. No build/install/upload has been performed in this change.

## User request / implementation
In the pre-Run "Confirm Class finish time" dialog:
1. Removed the in-dialog **Test Class alert sound** button. Did not modify audio behavior or Sound settings.
2. Displays `Calculated finish:` and the live date/time with seconds **on one horizontal line**, giving the time an enlarged, auto-sized type range (12–32sp) to prioritize readability and avoid clipping on normal phone widths. The static caption is compact.
3. Places **three clear shared-style buttons at the bottom**: full-width **Change finish time** above **Cancel** and **Confirm & Run** side by side, avoiding cramped three-across labels.
4. `Change finish time` remains visible but disabled while hour/minute wheels are shown. Opening wheels alone still does not select/freeze time. Existing **Use calculated finish** restores live choice and re-enables Change.
5. Original `EndClockWheel`, red-boundary enforcement, `Selected finish` row, `Final two Sessions` formatting, warnings, final-time planning, persisted Classes and confirmation flow are unchanged.

## Changed files
- `android-native/app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt`: dialog presentation and action layout only.
- `android-native/app/src/androidTest/java/com/ateliertime/android/RunEndApprovalUiTest.kt`: two additional Compose UI tests for button hierarchy, sound-action removal, disabled/reenabled Change and preserved Selected/Final display.
- `android-native/app/build.gradle.kts`: D159 / Code53 debug-candidate version.
- `android-native/tools/build_d159_finish_approval_ui_on_phone.sh`: local Samsung QA helper pinned to D159 app source, retains 326+ JVM and 8+ D158 background tests as mandatory gates; builds debug `.dev` APK and compiles AndroidTest **without executing instrumentation**.

## Pending acceptance
On Samsung Code on the Go, **only when ready**:
```bash
cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native
git pull --ff-only
bash -n tools/build_d159_finish_approval_ui_on_phone.sh
bash tools/build_d159_finish_approval_ui_on_phone.sh
```
Expected APK after a successful script: `/storage/emulated/0/Download/Atelier-Time-D159-Code53-FinishApproval-DEV-QA.apk`. No installation by the script. User must separately agree to installing, ideally after no Class/Free timer is active, and visually verify small screens / enlarged accessibility fonts, one-line calculated time, wheel behavior, Cancel/Run, original Saved Classes and Sound. Compile-only instrumentation **must not be called executed**.

**No Stable19 designation** until device QA and explicit user acceptance. No signed release AAB or Google Play submission for D159 is authorized.
