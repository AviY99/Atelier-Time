# Atelier Time — STABLE 16 / D-153 / versionCode 47 — user-accepted UI / Sound controls

**Accepted:** 2026-11-06.  
**Official user acceptance (verbatim):** "הכל עובד , זוהי גרסה יציבה חדשה הכוללת  עיצוב שפה עיצובית של כפתורים ומתגים ושיפורי ממשק תפעול של קולות".  
**Classification:** app-wide unified button and toggle design language, plus operational Sound controls / UI refinements.  
**Status:** **Stable 16, user-accepted and frozen**. App package `com.ateliertime.android`, Android Kotlin / Compose, versionName `D-153`, versionCode `47`, compile/target API36.

## Immutable recovery checkpoint

- Freeze branch: [`stable/android-d153-qa16-ui-sound-user-accepted`](https://github.com/AviY99/Atelier-Time/tree/stable/android-d153-qa16-ui-sound-user-accepted).
- **Verified immutable Stable16 HEAD:** `04270365f330af78861d54f3710048d65dadc462`. GitHub confirmed the new frozen branch at this exact commit; do not modify it after freeze.
- Prior approved **Stable 15 / D147 / code41** is preserved unchanged on `stable/android-d147-qa15-ui-install-accepted` at `c10f424c0ec000eca2dd561911934841decb4522`.
- Last substantive DEV D153 app / build helper change: `0df7f6f757ca62fd13a70951baa8418964b8b754`; GitHub compare between that commit and the D153 verified source shows **only documentation changes** (README, CHANGELOG, D153 spec, index and handoff), no Android Kotlin, tests, assets, Gradle or scripts changed. Freeze captures that verified source + acceptance documentation.
- No new feature development, force-push, merges or edits on the stable branch. Continue future work on DEV; central documentation and Play assets on main.

## Accepted feature scope

- **D148–D151: Sound usability and timer controls:** globally persisted Class+Free Sound ON/OFF; Sounds tab is the single choice for Media versus Alarm output; Alarm selection forces Sound ON and disables app-wide OFF until Media chosen. Shared/separate cue settings, 5s/10s/Custom 5–60s durations, one selected hardware volume slider in Run and Free, per-selected-stream Max with confirmation and selected-stream Mute (not the global Sound OFF), compact Class/Free actions and bottom Sounds help.
- **D152 design unification:** shared `AtelierActionStyle.kt` wrappers for buttons/chips throughout Editor, Saved Classes, Sounds, Design and dialogs; 12dp radius, 40dp visible frames, 1dp stroke, 56dp minimum width for ordinary Material button/chips with responsive widths; 48dp touch areas on compact actions and Sounds preview.
- **D153 final visual refinement:** one reusable rectangular `AtelierOnOffButton` across Editor, Sounds and Class/Free Timer; awake label closer to its ON/OFF; `Volume` is **plain, frameless label**, with **Max** and **Mute** as two independent matching framed buttons, never a 3-segment enclosing frame. Preserve the single Sound-selected Media/Alarm slider, confirmation, original volume behavior and persisted data.
- No changes to timer core, replay/Next, 25/5 rule, special-break logic, saved-classes storage, notifications, approved artwork or approved Google Play listing assets.

## Test, build and user-acceptance evidence

User provided Samsung Code on the Go terminal screenshot after `android-native/tools/build_d153_toggles_volume_actions_on_phone.sh`:
- `ANDROIDTEST IMPORT PREFLIGHT PASS`; `ARTWORK AUDIT PASS`.
- `SoundSettingsTest ACTUAL: tests=20 errors=0 failures=0 skipped=0`; **ALL JVM ACTUAL: tests=300 errors=0 failures=0 skipped=0**.
- `APK ARTWORK INCLUDED PASS: assets/atelier_splash_approved_d139.png, SHA256 matched.` This checks the exact approved image inside the built APK.
- `D-153 JVM TESTS AND BUILD SUCCESSFUL`; local debug APK `/storage/emulated/0/Download/Atelier-Time-D153-API36-Candidate.apk` plus local Manifest and private Build.log. The debug APK is on the phone; it was **not uploaded to GitHub or to Play**.
- AndroidTest sources compiled into test APK, **instrumentation tests NOT executed**. Do not describe them as passing.
- Phone install and UI acceptance: the user explicitly confirmed **"הכל עובד"** and declared this the **new stable** after the build and installation instructions. Treat this as user functional acceptance, not an independently executed automated UI test run.
- All compilation performed on the user's Samsung phone; no paid GitHub Actions/cloud CI, uninstall, data clear, signed AAB or Google Play submission.

## Play readiness / next steps

Previously established commercial choice: **FREE**. First test distribution: **private invite-only Internal Testing** (not public rollout). Current stable source is D153/code47; do not silently release older D147/code41. A **separate user-approved, locally signed release AAB** is required for Play; a debug APK is not a release artifact. Validate release manifest/policy/Data Safety/foreground-service declarations and privacy link in the actual bundle; check developer identity/Console access and store listing readiness at the time of action. Never create or store signing secrets in GitHub, never clear saved classes/reports. No signed AAB, Play upload or publication has been performed as part of this acceptance.

**Current handoff:** [Stable16 → Google Play](HANDOFF_CURRENT_STABLE16_TO_GOOGLE_PLAY_2026_11_06.md).  
**D153 implementation:** [unified ON/OFF and separate Max/Mute](D153_UNIFIED_ON_OFF_AND_VOLUME_ACTIONS_2026_11_06.md).  
**Previous stable:** [Stable15 acceptance certificate](STABLE15_D147_UI_INSTALL_USER_ACCEPTANCE_2026_11_05.md).
