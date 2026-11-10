# CURRENT AUTHORITATIVE HANDOFF — Atelier Time Stable 18 / D-158 / Code 52 → Google Play Internal Testing update

**Updated:** 2026-11-10 (Asia/Jerusalem). **This file supersedes the Stable17 handoff** as the current project continuation point. Older handoffs, historical logs and dated "QA pending" statements remain historical and do not override this one.

## 1. Current stable release and evidence

**Latest stable (user-accepted on-device): Stable18 / D-158 / Code52.** New frozen branch [`stable/android-d158-qa18-background-notifications-user-accepted`](https://github.com/AviY99/Atelier-Time/tree/stable/android-d158-qa18-background-notifications-user-accepted) at exact **`7994dcfc2d077e68d72800400fa2086dacdc4132`**; app source tree `d835f1e6301e19b0fcd478151ce7602f1300a6d5` matches the build helper pin. Do not change that branch.

**Accepted fix A:** Class background notification now uses the same group-aware numbering and presentation as the running Class UI. No change to stored phase IDs or scheduling.

**Accepted fix B:** Free Timer now shows its own Running/Paused background notification, including coexistence with Class notification. ID107 Class and ID109 Free are independently dismissed; one existing FGS coordinates statuses, not timer clocks or sounds.

**Device and build proof:** On 2026-11-10 Samsung Code on the Go reported **326/326 JVM tests PASS** (including 8/8 D158 background tests), 0 failures/errors/skips, debug APK built, original approved splash present inside the APK, AndroidTest sources **COMPILED NOT EXECUTED**. Private candidate: `/storage/emulated/0/Download/Atelier-Time-D158-Code52-DEV-QA.apk`; private build log `Atelier-Time-D158-Code52-QA-20261110-204211.log`. User then explicitly wrote **"הכל עובד כנדרש , זוהי גרסה יציבה חדשה , טיפול ותיקון באגים בעבודה ברקע"**, accepting this as new stable. This is real-device user acceptance, not assistant-run instrumentation. [Full acceptance certificate](STABLE18_D158_BACKGROUND_NOTIFICATIONS_USER_ACCEPTANCE_2026_11_10.md).

**Earlier frozen baseline:** Stable17 / D157 / Code51 at `stable/android-d157-qa17-user-accepted`, SHA `f6866dca809f6214c6d1f88c1adf8f6592163ada`, unchanged. Stable16 and previous branches remain preserved.

## 2. Critical distinction — phone DEV app versus Google Play

- The tested D158 **debug** app displays **Atelier Time DEV**, package `com.ateliertime.android.dev` (suffix only in debug). This permits installation **beside** the existing Play app without overwriting its data.
- The **release** build must keep unchanged `applicationId=com.ateliertime.android` with **versionCode52** and **versionName D-158**. DO NOT publish the `.dev` APK or use the debug artifact as a Play release.
- **Google Play Internal Testing currently has Stable17 / D157 / Code51** available to invited testers; it is not silently upgraded by freezing Stable18. No Stable18 release AAB built/signed/uploaded and no new Play rollout verified.
- Signed Play installs and debug installs use different signing identities; avoid uninstall/clear data unless the user separately decides. The privately created `atelier-upload.p12` Upload Key was backed up off-device according to user's confirmation; **never request or publish the key or password**.
- Google Play currently uses an unreviewed temporary package-based app name until store setup/review is complete. Public release has not occurred.

## 3. Next authorized action gate — verify readiness for Code52 Play update

**Proposed next step, not automatically approved:** Prepare a local signed **release AAB** of frozen D158/Code52 with the already backed-up Upload Key, after assessing Play policy changes for both background timers. Build from Android DEV with exact frozen **app tree** and verify output `com.ateliertime.android` (not `.dev`), Code52, signature, artwork and required SDK. Prefer adapting the existing guarded phone-local release helper for D158 rather than using paid cloud CI.

Before any upload: review `AndroidManifest.xml` foreground service `specialUse` subtype now mentioning user-started Class **and Free** statuses; Play Console required foreground-service declarations/demo video and Data Safety/privacy/permissions, especially new Free notification. Verify release symbols/config as applicable. Confirm next Play versionCode52 is unused.

**Only after separate user authorization:** upload a signed D158/Code52 AAB to the existing invite-only **Internal Testing** track, inspect Console errors/warnings, verify tester installation/update. No public Production publishing without separate authorization.

## 4. Remaining pre-publication items and optional work

- Finish store listing assets, verified app name/icon, Privacy Policy, Data Safety, relevant content/trader declarations, FGS specialUse documentation; check actual Play Console state. Approved graphic pack on `main/artwork/play-store/Atelier_Time_Google_Play_Approved_Assets_Complete_Pack.zip`. Privacy Policy: `https://aviy99.github.io/Atelier-Time/privacy-policy.html`.
- **Before public release** update the About → WhatsApp share from interim Instagram link to an active verified public Play listing/landing page. The public Play URL format alone is not proof it is live.
- **Optional future upgrades** Studio Display/Screen Mirroring and paired Remote Control remain research/planning only, not Stable18 or first public-launch blockers. [Optional future scope](OPTIONAL_POST_RELEASE_STUDIO_DISPLAY_AND_REMOTE_CONTROL_2026_11_08.md).

## 5. Working conventions and evidence index

- Coding branch `android-native-kotlin-compose`; repository-wide documentation on `main`; frozen stable branches **immutable**. New development changes require new candidate and local Samsung QA, not editing frozen Stable18.
- Preserve existing phone data, local APK/AAB build preference, no paid GitHub Actions builds.
- [Stable18 accepted certificate](STABLE18_D158_BACKGROUND_NOTIFICATIONS_USER_ACCEPTANCE_2026_11_10.md)
- [D158 implementation and Samsung QA](D158_PROPOSED_BACKGROUND_NOTIFICATIONS_GROUP_LABELS_AND_PARALLEL_FREE_TIMER_2026_11_09.md)
- [Previous Stable17 handoff — historical](HANDOFF_CURRENT_STABLE17_TO_GOOGLE_PLAY_2026_11_07.md)
- [Documentation index](DOCUMENTATION_INDEX_AND_BRANCH_POLICY.md)

**Not performed during Stable18 promotion:** application source changes, new phone build, Play Console mutation, release AAB signing, update upload, user data wipe, or paid CI.
