# CURRENT AUTHORITATIVE HANDOFF — Atelier Time Stable 19 / D-160 / Code 54 → Next Google Play / development gate

**Updated:** 2026-11-13 (Asia/Jerusalem). **This is the latest authoritative handoff and supersedes the Stable18 handoff as current status.** Read this before any code/Play task. Earlier documents and pending-QA remarks are historical snapshots, not current blockers where newer evidence exists.

## 1. Latest accepted stable release

**Stable 19 / D-160 / Code 54** was explicitly approved by the user on Samsung: **"הכל עובד מצויין , זאת גרסה יציבה חדשה , שיפורי ממשק"** (everything works excellently; new stable version for UI improvements). The final D160 device QA follows D159, which was functional but needed design corrections.

**Frozen recovery branch:** [`stable/android-d160-qa19-finish-dialog-ui-user-accepted`](https://github.com/AviY99/Atelier-Time/tree/stable/android-d160-qa19-finish-dialog-ui-user-accepted), **commit `55e82d51788062368970cde7e747edc5b7e72bb1`**, app source tree **`717ea00fe4304cdcea4e53e04a1eaab0a378e559`**; branch verified against source and guarded build-script pin. **Never change this or any earlier stable branch.**

**Approved behavior changes — Run → Confirm Class finish time:** no in-dialog audio-test action; no repeated Class-name line; restore original one-line consistent typography for live `Calculated finish`; three bottom actions (Change/Back, Cancel, Confirm & Run); when wheels appear Change becomes **Back**, and Back closes wheels and restores the live calculated finish; no separate **Use calculated finish** line. Existing hour/minute wheels, constraints, `Selected finish`, `Final two Sessions`, timing/audio/FGS and persistent data unchanged.

**Samsung proof, 2026-11-13:** `D160 ALL JVM ACTUAL: tests=326 failures=0 errors=0 skipped=0`; background regression class **8/8 pass**; approved splash SHA verified **inside DEV APK**; debug APK successfully built; AndroidTest **COMPILED, NOT EXECUTED**. User subsequently installed/tested and confirmed all works excellently. Local output: `/storage/emulated/0/Download/Atelier-Time-D160-Code54-FinishDialog-DEV-QA.apk`. This is a **debug** build with package `com.ateliertime.android.dev`, installed beside the Play app. It is **not a Play release**. [Stable19 acceptance certificate](STABLE19_D160_FINISH_DIALOG_UI_USER_ACCEPTANCE_2026_11_13.md).

**Prior recovery points unchanged:** Stable18 / D158 / Code52 at `stable/android-d158-qa18-background-notifications-user-accepted`, SHA `7994dcfc2d077e68d72800400fa2086dacdc4132` (Class group labels + independent parallel Free notification); Stable17 / D157 / Code51 at `stable/android-d157-qa17-user-accepted`, SHA `f6866dca809f6214c6d1f88c1adf8f6592163ada`.

## 2. Actual Google Play and signed-bundle status — DO NOT CONFUSE

| Artifact | Latest verified or user-reported status |
|---|---|
| **Play Internal Testing** | Last reported **Stable17 / D157 / Code51**, invite-only. No later Play upload confirmed. |
| **Stable18 / D158 / Code52** | Signed **release AAB BUILT locally** on Samsung on 2026-11-12; script reported manifest/artwork checks and successful `jarsigner` verification. File: `/storage/emulated/0/Download/Atelier-Time-D158-Code52-InternalTest-SIGNED.aab`. **NOT uploaded** to Play; uploading Code52 is not presumed authorized. |
| **Stable19 / D160 / Code54** | Frozen, user-accepted, **DEV debug APK** only. No signed **Code54 release AAB** built, uploaded, reviewed or distributed through Play. |
| **Production** | No public release confirmed. |

Stable18/Code52 signed AAB used the **existing privately backed-up** upload keystore `$HOME/.atelier-time-signing/atelier-upload.p12`; never request its bytes or passphrase. Signed AAB presence does not establish that its upload certificate matches the Play Console record until checked there. **Version code 53** was D159 intermediate candidate, **54** is accepted D160; next Play update must choose intended target version explicitly and verify Console rules. Never upload the older AAB automatically simply because it is ready.

## 3. Next Play preparation gate (ONLY upon explicit approval)

1. Decide whether the next Internal Testing release should be the **current user-accepted Stable19 / Code54** rather than the already-signed older Stable18 / Code52. Do not assume or publish.
2. For Stable19, create a new **guarded, locally executed Samsung release AAB** helper based on the reviewed D158 signing process but locked to the **Stable19 app tree**, checking `applicationId=com.ateliertime.android` (no debug `.dev`), `versionCode=54`, `versionName=D-160`, SDK, merged permissions, FGS, splash, no untracked app sources, signature and upload certificate. Existing D158 release helper is pinned to Code52 and must NOT be used unchanged for Code54.
3. Before a Play upload check the local AAB against the Play Console **Upload key certificate SHA-256**, existing track/version-code status and Play's exact current warnings. No upload or publish until separately authorized.
4. Android foreground service: `foregroundServiceType=specialUse` and subtype must truthfully cover user-initiated **Class + Free** ongoing status. FGS supports foreground-process importance/status/cleanup; service itself does NOT compute clocks or play audio and cannot guarantee indefinite operation if the app process is killed. **Play policy approval is unverified**, and an evidence video/use-case declaration remains a release gate.
5. Privacy: public policy at `https://aviy99.github.io/Atelier-Time/privacy-policy.html` is already linked from About and was visually verified on the phone. Existing hosted wording may need a narrowly scoped update to explicitly cover Free Timer background notification (separate documentation approval); no new in-app link required. The source-based Data Safety position remains tentative until actual release manifest/dependency review and the Console form state are checked. The Internal Testing-only exception to the Data Safety form should not be generalized to other tracks.
6. Approved Play artwork and ZIP are in `main/artwork/play-store/`; do not recreate. Exact publication/store listing state requires a Console check. About → WhatsApp currently shares Instagram; changing it to a verified Play listing is a **pre-public-production** task only, not a blocker for Internal Testing.

## 4. Development conventions and safety

- Android source working branch: `android-native-kotlin-compose`; project-wide authoritative documentation: `main`; **all stable branches frozen/immutable**. Branch `stable/android-d160-qa19-finish-dialog-ui-user-accepted` is recovery only.
- No paid GitHub Actions, no cloud builds, no uninstall/data clear, no secrets shared in chat/GitHub, no unexpected changes to timer audio/scheduling.
- Developer computer/Android SDK/Gradle cannot be assumed available to ChatGPT; Samsung Code on the Go is the established local builder. User-supplied terminal screenshots evidence actual execution; assistant checks are repository/source only unless stated.
- User acceptance of Stable19 does **not** authorize a new Play upload, signing action, reinstallation, policy submission, production release or further app change.
- Additional optional projects (Studio Display/mirroring and remote control) remain research scope, not required for this release.

## 5. Source of truth / handoff documents

- [Stable19 user-acceptance record](STABLE19_D160_FINISH_DIALOG_UI_USER_ACCEPTANCE_2026_11_13.md)
- [Stable19 candidate D160 UI rationale, implementation and local QA helper](https://github.com/AviY99/Atelier-Time/blob/stable/android-d160-qa19-finish-dialog-ui-user-accepted/docs/D160_FINISH_CONFIRMATION_DIALOG_BACK_AND_TEXT_2026_11_13.md)
- [Stable18 background-notification acceptance](STABLE18_D158_BACKGROUND_NOTIFICATIONS_USER_ACCEPTANCE_2026_11_10.md)
- [Historical Stable18 handoff](HANDOFF_CURRENT_STABLE18_TO_GOOGLE_PLAY_2026_11_10.md)
- [Documentation index and branch policy](DOCUMENTATION_INDEX_AND_BRANCH_POLICY.md)

**No additional Android build, source modification, signed AAB upload, Play action, key access, stable-branch edit or data deletion is part of this Stable19 promotion.** 
