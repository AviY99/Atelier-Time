# CURRENT AUTHORITATIVE HANDOFF — Atelier Time Stable 19 / D-160 / Code 54 — Internal Testing published and device-updated

**Updated:** 2026-11-13 (Asia/Jerusalem), AFTER successful Google Play Internal Testing rollout and user-confirmed in-place update. **This is the latest authoritative handoff and supersedes earlier Stable18 and earlier Stable19 preparation status.** Read this before any code/Play task. Earlier "signed AAB not built", "not uploaded" and "Stable17 is on Play" descriptions are historical.

**CURRENT RESULT — COMPLETE:** Stable19 **D-160 / versionCode 54** was signed on Samsung using the EXISTING upload key, uploaded to the existing invite-only **Google Play Internal Testing** track, and shown in Play Console as **Active / Available to internal testers**, release **2026-11-13 19:29 (Asia/Jerusalem)**, with review label **Not reviewed** and temporary app name `com.ateliertime.android (unreviewed)`. The user then updated the installed **Play** app in place on Samsung and explicitly confirmed **all tested functionality works and prior saved data was retained**. This is a user/device-confirmed Play update, **NOT** a public Production release, Google policy approval, or instrumentation test execution.

## 1. Latest accepted stable release

**Stable 19 / D-160 / Code 54** was explicitly approved by the user on Samsung: **"הכל עובד מצויין , זאת גרסה יציבה חדשה , שיפורי ממשק"** (everything works excellently; new stable version for UI improvements). The final D160 device QA follows D159, which was functional but needed design corrections.

**Frozen recovery branch:** [`stable/android-d160-qa19-finish-dialog-ui-user-accepted`](https://github.com/AviY99/Atelier-Time/tree/stable/android-d160-qa19-finish-dialog-ui-user-accepted), **commit `55e82d51788062368970cde7e747edc5b7e72bb1`**, app source tree **`717ea00fe4304cdcea4e53e04a1eaab0a378e559`**; branch verified against source and guarded build-script pin. **Never change this or any earlier stable branch.**

**Approved behavior changes — Run → Confirm Class finish time:** no in-dialog audio-test action; no repeated Class-name line; restore original one-line consistent typography for live `Calculated finish`; three bottom actions (Change/Back, Cancel, Confirm & Run); when wheels appear Change becomes **Back**, and Back closes wheels and restores the live calculated finish; no separate **Use calculated finish** line. Existing hour/minute wheels, constraints, `Selected finish`, `Final two Sessions`, timing/audio/FGS and persistent data unchanged.

**Samsung proof, 2026-11-13:** `D160 ALL JVM ACTUAL: tests=326 failures=0 errors=0 skipped=0`; background regression class **8/8 pass**; approved splash SHA verified **inside DEV APK**; debug APK successfully built; AndroidTest **COMPILED, NOT EXECUTED**. User subsequently installed/tested and confirmed all works excellently. Local output: `/storage/emulated/0/Download/Atelier-Time-D160-Code54-FinishDialog-DEV-QA.apk`. This is a **debug** build with package `com.ateliertime.android.dev`, installed beside the Play app. It is **not a Play release**. [Stable19 acceptance certificate](STABLE19_D160_FINISH_DIALOG_UI_USER_ACCEPTANCE_2026_11_13.md).

**Prior recovery points unchanged:** Stable18 / D158 / Code52 at `stable/android-d158-qa18-background-notifications-user-accepted`, SHA `7994dcfc2d077e68d72800400fa2086dacdc4132` (Class group labels + independent parallel Free notification); Stable17 / D157 / Code51 at `stable/android-d157-qa17-user-accepted`, SHA `f6866dca809f6214c6d1f88c1adf8f6592163ada`.

## 2. Google Play Internal Testing — confirmed completed 2026-11-13

| Artifact / evidence | Confirmed state |
|---|---|
| **Play Internal Testing CURRENT** | **Stable19 / D-160 / Code54**, **Active / Available to internal testers** per Console screenshot, released **Nov 13, 19:29 local**; displayed **Not reviewed**. |
| **Samsung Play update** | User explicitly confirmed **Update** completed successfully from Google Play, all tested operations run correctly, and **previously saved user data survived the in-place update**. Do not uninstall or clear data. |
| **Stable19 signed release AAB** | Successfully built and signed locally using Samsung Code on the Go: `/storage/emulated/0/Download/Atelier-Time-D160-Code54-InternalTest-SIGNED.aab`; signed-bundle content integrity PASS; release package `com.ateliertime.android`, D-160 / Code54, targetSdk 36, FGS/permissions and approved splash/launcher checks PASS. **Not the DEV debug APK**. |
| **Release test evidence** | Samsung log showed **326 JVM tests passed**, **8/8 background regressions passed**; AndroidTest **compiled but not executed**. Bundle signature verified with `jarsigner`. |
| **Upload key matching** | Local signed AAB upload-certificate SHA-256 **matched exactly** the Console **Upload key certificate** fingerprint: `89:72:FA:E8:4D:47:4C:32:C5:50:5E:AA:85:6C:F9:E9:09:37:CC:31:45:F8:18:EB:57:2F:07:F5:A7:6B:07:39`. This is a *public certificate fingerprint*, not a secret. Never upload or expose keystore/password. |
| **Prior Stable17 / D-157 / Code51** | Historical previous Internal Testing release, replaced in the track by Code54. Frozen source remains unchanged. |
| **Stable18 / D-158 / Code52** | Historical locally signed AAB, **never uploaded**. Frozen source remains unchanged. |
| **Production** | **Not released.** No public distribution authorized. |

**Local signed-AAB preparation helper:** [`android-native/tools/build_d160_signed_aab_for_internal_test_on_phone.sh`](https://github.com/AviY99/Atelier-Time/blob/android-native-kotlin-compose/android-native/tools/build_d160_signed_aab_for_internal_test_on_phone.sh), development-branch signing-input fix at `07c5172e575eccef4262596c8a3a2e59926e7e50`. It prompts once at startup and holds the key password in shell memory for signing, never stores it in source files or logs. The previous failed passphrase-stdin attempt preceded this successful run. **This helper is not authorization for an additional build or Play upload.**

**Console release review details:** version 54 accepted by Console with two nonblocking diagnostics warnings: missing deobfuscation file and missing native debug symbols (impact on crash/ANR analysis, not evidence of runtime failure). Device support remained unchanged. Console's **Save and publish** action was carried out by the user; current status shown as **Available to internal testers**.

## 3. Remaining post-Internal-Testing readiness (NOT blockers to the already successful update)

1. **Google Play FGS policy:** `FOREGROUND_SERVICE_SPECIAL_USE` and `specialUse` subtype are present for user-started Class/Free status notifications. Console **App content → Foreground service permissions** showed **Start declaration**; selecting **Other** requested a demo-video link. A 67-second demonstration was recorded; the user uploaded `AtlierTimeRecordingV1.mp4` to Google Drive with `Anyone with the link — Viewer`, URL `https://drive.google.com/file/d/1miOqtIp8fIa0s6Y1PAr8C-eZkdZtDFWc/view?usp=sharing`. The user later reported an external playback error. **Do not claim the video is independently playable, the declaration saved, or FGS approved by Google.** Resolve video accessibility and declaration before wider tracks/if Play prompts; the service coordinates notifications/cleanup, not the clocks or audio, and cannot guarantee uninterrupted execution if process is killed.
2. **Store listing / account:** Play Console still showed temporary app name `com.ateliertime.android (unreviewed)` and banner to **Declare your trader status**. These are pending Console/content items, not evidence that the successful internal update failed. Review before broader release.
3. **Privacy / Data Safety:** public policy `https://aviy99.github.io/Atelier-Time/privacy-policy.html` remains linked and phone-verified. Its wording still focuses on Class notification and may be updated to mention Free Timer via a separate, explicitly approved documentation/website change. Source-based no-automatic-collection statements remain conditional on actual SDK behavior and relevant track requirements. Internal Testing-only Data Safety exception is not a Production exemption.
4. **Debugging symbols:** two warnings (deobfuscation mapping and native symbols) did not block Code54; include symbols in a later release if needed for useful Play crashes/ANR reports.
5. **Future releases:** public **Production** rollout requires separate user approval and full Console/policy review. About → WhatsApp currently shares Instagram instead of a verified Play listing: pre-public-production task only. No cloud CI, data deletion or stable-branch modifications.

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

**Documentation update only:** the already completed signed release build, Play Internal Testing rollout, Samsung in-place update and data-preservation result above are historical user actions; this handoff edit does **not** perform any further build, signing, upload, policy submission, source modification, stable-branch edit or data deletion.
