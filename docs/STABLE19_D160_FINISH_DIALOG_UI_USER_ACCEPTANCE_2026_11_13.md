# Atelier Time — Stable 19 / D-160 / Code 54 — User-accepted finish-dialog UI improvements

**Acceptance date:** 2026-11-13 (Asia/Jerusalem). **Status:** USER-ACCEPTED, FROZEN STABLE 19.  
**Scope:** Display and interaction refinements in **Confirm Class finish time** only, following Stable18.  
**Production applicationId:** `com.ateliertime.android`; **versionName:** `D-160`; **versionCode:** `54`; **compileSdk/targetSdk:** `36`.  
**Device QA package:** `com.ateliertime.android.dev`, label `Atelier Time DEV`, installed independently of the Google Play app.

## 1. Explicit user acceptance

After installing and checking the D-160 DEV candidate on Samsung, the user explicitly declared:

> **"הכל עובד מצויין , זאת גרסה יציבה חדשה , שיפורי ממשק"**

English meaning: everything works excellently; this is a new stable version for UI improvements.

This is **user-performed real-device functional acceptance**; it is not a claim that Android instrumentation tests ran or that Google Play reviewed the application.

## 2. Accepted changes

In the saved-Class Run confirmation dialog:

1. Removed the unnecessary **Test Class alert sound** control from that dialog; audio functionality elsewhere is retained.
2. Removed the redundant **Class name** line only inside the confirmation dialog, without modifying saved Class names/data.
3. Restored **Calculated finish: dd/MM HH:mm:ss** as a *single uniform original-style* line in `MaterialTheme.typography.titleMedium`. The earlier D159 mixed small caption/oversized numbers were rejected and corrected in D160. Live seconds continue to update.
4. Retained the bottom action area: full-width **Change finish time** and, beneath it, **Cancel** and **Confirm & Run**.
5. When the time-picker wheels are open, **Change finish time** becomes an active **Back** button. Tapping **Back** hides the wheels and restores the live calculated finish, even after time edits. The previous separate **Use calculated finish** line is removed.
6. Existing **Selected finish**, **Final two Sessions**, wheel validity/red-boundary behavior, underlying end-time calculations, audio cues, notifications, Class/Free timers, saved Classes and cancellation/confirmation logic remain unchanged.

Intermediate D159/Code53 was built and device-tested but received further UI correction requests; D160/Code54 is the **accepted** final version.

## 3. Immutable recovery checkpoint

- Frozen branch: [`stable/android-d160-qa19-finish-dialog-ui-user-accepted`](https://github.com/AviY99/Atelier-Time/tree/stable/android-d160-qa19-finish-dialog-ui-user-accepted).
- Frozen commit SHA: **`55e82d51788062368970cde7e747edc5b7e72bb1`**.
- Exact Android application Git tree: **`717ea00fe4304cdcea4e53e04a1eaab0a378e559`**. Matches the pinned D160 phone build helper.
- Development branch: `android-native-kotlin-compose`; documentation authoritative on `main`. Do **not** modify frozen stable branches.
- Preceding accepted Stable18 remains at `7994dcfc2d077e68d72800400fa2086dacdc4132`; Stable17 remains at `f6866dca809f6214c6d1f88c1adf8f6592163ada`.

## 4. Local Samsung build and test evidence

The user supplied a Samsung Code on the Go terminal screenshot on 2026-11-13 (~14:55 local). It showed:

```text
D160 ALL JVM ACTUAL: tests=326 failures=0 errors=0 skipped=0
D160 INHERITED BACKGROUND REGRESSIONS: {'tests': 8, 'failures': 0, 'errors': 0, 'skipped': 0}
D160 FULL JVM PASS
D160 APK ARTWORK INCLUDED PASS
D160 JVM TESTS AND BUILD SUCCESSFUL:
/storage/emulated/0/Download/Atelier-Time-D160-Code54-FinishDialog-DEV-QA.apk
AndroidTest COMPILED, NOT EXECUTED; no APK install, Play upload or cloud CI.
Debug package com.ateliertime.android.dev preserves installed Play data.
```

Local log path reported by Samsung: `/storage/emulated/0/Download/Atelier-Time-D160-Code54-QA-20261113-145234.log`. Source tree and approved artwork verification were guarded in `android-native/tools/build_d160_finish_dialog_back_on_phone.sh`. 326 JVM unit tests were **executed**; Compose UI/AndroidTest tests were **compiled but not executed**. The APK and log are local device artifacts, not uploaded as GitHub releases. The user's subsequent statement confirms the installed D160 application works as desired.

## 5. Google Play distinction and next approval gate

- **Last user-confirmed Play Internal Testing:** Stable17 / D157 / Code51. No further Play update was reported in this conversation.
- **Stable18 / D158 / Code52:** A signed release **AAB was successfully built locally on Samsung on 2026-11-12** using the existing backed-up Upload Key. File: `/storage/emulated/0/Download/Atelier-Time-D158-Code52-InternalTest-SIGNED.aab`. The local script reported `SIGNED AAB SIGNATURE STRUCTURE PASS` and `SIGNED RELEASE AAB PREPARED FOR HUMAN REVIEW — NOT UPLOADED`. It was **not uploaded** to Play.
- **Stable19 / D160 / Code54:** user-accepted separate **debug APK**, frozen source. **No Stable19 signed AAB built, signed, uploaded or published**; promoting the frozen Git branch is not a Play deployment.
- Before any Google Play upload, explicitly choose the target version to publish, validate Upload Key certificate and Play Console version code availability, check release-bundle merged manifest, FGS `specialUse` justification/video for Class + Free, Data Safety/privacy, and request specific user authorization.
- Do not request private signing keys/password, invoke paid cloud CI/GitHub Actions, alter a frozen branch, uninstall apps or clear user data.

**Current canonical handoff:** [Stable19 → Google Play / next work](HANDOFF_CURRENT_STABLE19_TO_GOOGLE_PLAY_2026_11_13.md).  
**D160 implementation/QA specification:** [D160 finish dialog Back and typography](https://github.com/AviY99/Atelier-Time/blob/stable/android-d160-qa19-finish-dialog-ui-user-accepted/docs/D160_FINISH_CONFIRMATION_DIALOG_BACK_AND_TEXT_2026_11_13.md).  
**Previous stable record:** [Stable18 acceptance](STABLE18_D158_BACKGROUND_NOTIFICATIONS_USER_ACCEPTANCE_2026_11_10.md).
