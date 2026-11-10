# Atelier Time — Stable 18 / D-158 / Code 52 — Accepted background-notification bug fixes

**Acceptance date:** 2026-11-10 (Asia/Jerusalem). **Official status: Stable 18, USER ACCEPTED, FROZEN on GitHub.**  
**Scope:** Post-Stable17 bug fixes to background notifications only.  
**App production package:** `com.ateliertime.android`; Android `versionName = "D-158"`, `versionCode = 52`; Android compile/target SDK 36. The **debug QA variant** is intentionally `com.ateliertime.android.dev` with display name "Atelier Time DEV", installed side-by-side with the existing Play version.

## Verbatim user acceptance

User explicitly wrote on 2026-11-10 after installing and testing the separate D158 DEV APK: **"הכל עובד כנדרש , זוהי גרסה יציבה חדשה , טיפול ותיקון באגים בעבודה ברקע"** — everything works as required; this is a new stable version addressing background-operation bugs.

The two approved, user-accepted fixes are:
1. **Class Timer background display numbering:** Use the same group/session/break presentation names as the foreground Class Timer, rather than the old absolute sequential pose counter. `PhaseDisplayNames.title(..., session.rules)` is shared for notifications; timer phase IDs, schedule calculations and stored sessions are unchanged.
2. **Free Timer background notifications:** A distinct notification for Free (Running and Paused, remaining time), including **when Free and Class run concurrently**. Class ID **107** and Free ID **109** stay independently visible and can stop/complete independently without clearing one another. A single foreground service coordinates the notifications, with no second clock or sound player created for a notification.

User's "everything works" is **real-device functional acceptance**, not evidence of an assistant-run instrumentation suite, not a public Play approval, and not a claim that the user individually confirmed every regression-matrix row.

## Verified frozen source and previous stable

- Frozen stable recovery branch: [`stable/android-d158-qa18-background-notifications-user-accepted`](https://github.com/AviY99/Atelier-Time/tree/stable/android-d158-qa18-background-notifications-user-accepted).
- Exact frozen commit: **`7994dcfc2d077e68d72800400fa2086dacdc4132`** (the Android DEV commit immediately after successful build evidence documentation and before Stable18 acceptance documentation changes). GitHub app tree exactly **`d835f1e6301e19b0fcd478151ce7602f1300a6d5`**, which matches the D158 guarded Samsung QA build-script pin.
- The earlier official Stable17 frozen branch `stable/android-d157-qa17-user-accepted` remains at `f6866dca809f6214c6d1f88c1adf8f6592163ada`. Earlier Stable16 remains unaffected. **Never edit frozen stable branches.**
- Active code-development branch: `android-native-kotlin-compose`; project-wide documentation and approved Play/store assets: `main`.

## Samsung build/test evidence (2026-11-10, user screenshot)

```text
D158 ALL JVM ACTUAL: tests=326 failures=0 errors=0 skipped=0
D158 BACKGROUND REGRESSIONS: {'tests': 8, 'failures': 0, 'errors': 0, 'skipped': 0}
D158 FULL JVM PASS
D158 APK ARTWORK INCLUDED PASS
D158 JVM TESTS AND BUILD SUCCESSFUL:
  /storage/emulated/0/Download/Atelier-Time-D158-Code52-DEV-QA.apk
AndroidTest COMPILED, NOT EXECUTED
Debug package com.ateliertime.android.dev preserves installed Play data.
```

Private local QA log: `/storage/emulated/0/Download/Atelier-Time-D158-Code52-QA-20261110-204211.log`. The approved `atelier_splash_approved_d139.png` was checked inside the built APK. The APK and log are **local to the user's device**, not stored as public GitHub assets; the assistant did not directly inspect their bytes. New 8 JVM regressions included in the **326 total**. User later confirmed installed app works and declared Stable18.

## Status boundary for Google Play / future work

- **Currently published to invite-only Google Play Internal Testing:** **Stable17 / D157 / Code51** (previous AAB signed and accepted, installed from Play by the user).
- **Stable18 / D158 / Code52 is a locally installed, user-accepted DEV debug QA candidate and frozen source checkpoint.** It has **NOT** yet been packaged as a release-signed AAB, uploaded to Google Play Internal Testing, approved through new Play policy declarations, or made available as a Play update. Do not conflate a stable Git checkpoint with a store release.
- Any Code52 Play release must preserve `com.ateliertime.android` without the debug-only `.dev` suffix, use the user's existing privately backed-up Upload Key with Play App Signing, and verify Android 14+ `foregroundServiceType="specialUse"` subtype/declaration for the two timer statuses, Android privacy/Data Safety, app icon / store listing and package/version before uploading.
- Local builds only (Samsung Code on the Go), **no paid GitHub Actions**, no app uninstall/data clearing, and no sharing keystore/password in GitHub or chat.
- Public production release is a separate user approval gate. About → WhatsApp currently shares Instagram instead of a verified Google Play public product listing; replacing that link is a mandatory **before-public-production** task, not a blocker for internal testing.
- Optional longer-term Studio Display and Remote Control remain post-release proposals, not part of Stable18.

**Canonical next handoff:** [Stable18 → Google Play](HANDOFF_CURRENT_STABLE18_TO_GOOGLE_PLAY_2026_11_10.md).  
**Technical implementation and QA evidence:** [D158 background notifications](D158_PROPOSED_BACKGROUND_NOTIFICATIONS_GROUP_LABELS_AND_PARALLEL_FREE_TIMER_2026_11_09.md).  
**Earlier accepted baseline:** [Stable17](STABLE17_D157_VOLUME_AND_SOUND_USER_ACCEPTANCE_2026_11_07.md).
