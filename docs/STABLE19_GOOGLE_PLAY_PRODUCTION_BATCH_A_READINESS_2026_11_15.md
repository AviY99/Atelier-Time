# Atelier Time — Stable 19 / D-160 / Code 54 — Google Play Production preparation, Batch A

**Date:** 2026-11-15 (Asia/Jerusalem). **Status:** Batch A source/document/video analysis performed; **NOT** a Play Console submission, Google review, publication, privacy-site update, video replacement, app build or change to a frozen stable branch.

**Approval scope:** User explicitly approved Batch A items 1–9: independent investigation, evidence-based drafts, official-policy research, GitHub documentation and canonical handoff update. Batch B (actual privacy-site update, possible video adaptation, packaging final instructions) needs **new approval**. Play Console actions and public Production require independent user actions and separate approval.

**Canonical baseline:** Stable 19 / D-160 / versionCode 54, frozen branch `stable/android-d160-qa19-finish-dialog-ui-user-accepted`, SHA `55e82d51788062368970cde7e747edc5b7e72bb1`. Android package `com.ateliertime.android`, targetSdk 36. Internal Testing active since 2026-11-13 19:29 local; successful in-place user Play update with saved data preserved. 326/326 JVM tests and 8/8 background regressions passed on Samsung; AndroidTest compiled, not executed. **Production is not published, and Google has not approved the FGS use case.**

## A1 — Original FGS demonstration video: technical inspection, NOT a public-link playback approval

Source of inspection: user-uploaded video `AtlierTimeRecordingV1-1.mp4` available in current conversation. Original previously shared on Google Drive as `AtlierTimeRecordingV1.mp4`, link:
https://drive.google.com/file/d/1miOqtIp8fIa0s6Y1PAr8C-eZkdZtDFWc/view?usp=sharing

Local inspection using `ffprobe`, `ffmpeg`:
- Container MP4 (`mp42`), **66.938022 seconds**, **69,800,471 bytes**, SHA-256 `01df32b3a7d066dcbb4ae60e1291a03cac2b936765fe5a8d8228618ec26f05ce` for the conversation-uploaded copy.
- H.264 Main, level 5.1, 1080×2340, yuv420p, BT.709; 3,971 video packets/frames, approximately 59.3 fps **average** with variable presentation intervals. Audio AAC LC, 48 kHz, stereo.
- Decoder ran across the complete clip. Source video packet DTS values were strictly increasing. `ffmpeg` generated nonmonotonic-DTS **output-muxer warnings when flattening the VFR clip to a null output stream**; those are not proof the source is corrupt. The source MP4 `moov` atom is near the end rather than preceding `mdat`.
- User confirms that downloading from Drive and playing locally on Samsung **works**. Two prior screenshot captures show a Drive preview playback error.
- **Visual sample audit performed on the actual ~67s file** (frames at 2, 10, 16, 20, 22, 25, 28, 31, 34, 36, 39, 43, 45, 48, 51, 54, 57, 60, 63, 65 seconds): the clip shows Class Timer running, Free Timer started independently, **both active status notifications simultaneously** (clearest around 39s), and subsequent return/notification dismissal. These still-frame samples corroborate the intended demonstration; they do not constitute independent end-to-end interaction test.
- **Reviewer identity qualification:** the Android notification shade visibly labels the recording app **`Atelier Time DEV`**, while the Play release app is labeled `Atelier Time`. This appears to be a video of the `.dev` QA build, not a verified capture from the Google Play release. It demonstrates the tested feature but could prompt a reviewer question about differing app identity. **Do not assert that the recording proves the Play-installed Code54 specifically**; do not demand a new recording without a concrete Play policy or reviewer requirement. No screenshot/frame from the recording was uploaded to public GitHub.
- Google Drive officially lists MP4 H.264/AAC as supported, but describes **1920×1080 maximum playback resolution** and warns of browser, cookies, processing and device playback limitations. The tall 1080×2340 original, level 5.1, VFR and end-placed `moov` are possible interoperability contributors, **not a proven single root cause**.
- This environment cannot independently open/play the specific shared Drive link (web retrieval reports disabled). Therefore **reviewer-accessible streaming is still UNVERIFIED**.
- **Batch A did not transcode, remux, trim, rename, upload or modify the video.** Batch B, if approved and needed: first consider a minimally altered fast-start MP4 test; if Drive preview still fails, create a **new reviewer-compatible H.264/AAC derivative with portrait dimensions within a 1920×1080 playback envelope** and test in a public/private Chrome session. Preserve the 66.94s original as the master; do not guarantee that re-encoding fixes Drive playback. The actual upload and playback verification remain user/Drive-dependent.

Official: https://support.google.com/drive/answer/2423694 and https://support.google.com/googleplay/android-developer/answer/13392821

## A2 — FGS `specialUse` draft text, matching the FROZEN D-160 source

**Source checked:** `android-native/app/src/main/AndroidManifest.xml`; `ClassNotificationTaskService.kt`; `ActiveClassStatusNotification.kt`; `SessionEditorScreen.kt` on the frozen Stable19 branch.

Actual app manifest: `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`, `POST_NOTIFICATIONS`, `MODIFY_AUDIO_SETTINGS`; `ClassNotificationTaskService` is non-exported, `foregroundServiceType="specialUse"`, declared special-use subtype is **"User-started Class and Free Timer countdown statuses; dismiss on task close"**. The service runs in the default app process, coordinates ongoing Class and Free notifications (IDs 107 and 109), foreground ownership, end/task-removal cleanup. **Compose/timer domain handles countdown; audio handling is separate.** Background process priority can indirectly help in-process activity. It cannot guarantee uninterrupted timing after process termination or restore a killed timer. Timer operation does not require that notification permission be granted.

**Use-case label / Console category:** Choose `Other` only where the actual Play Console declaration prompts for `FOREGROUND_SERVICE_SPECIAL_USE` and requests the video link. **Never claim it has been submitted/approved.**

**Feature explanation (English, ready to copy if the field exists):**

> Atelier Time provides user-initiated Class and Free Timers for live drawing and sculpting sessions. During an active timer, a specialUse foreground service manages ongoing status notifications, including separate Class and Free notifications if both are running. It handles foreground-notification ownership and notification removal when a timer ends or the application task is dismissed. The service runs in the same Android application process as the timer and audio components, indirectly supporting the process while the user teaches with the phone in the background. The service does not calculate time, play audio, or restore a timer after process termination. Users explicitly start and stop the timers.

**Impact if delayed (English):**

> If foreground status service startup is delayed, the user may not promptly receive ongoing timer status notifications or expected task-dismissal notification handling. The application's separate timer and audio components run in a less protected process when it is backgrounded.

**Impact if interrupted (English):**

> If Android stops the foreground service, the status notifications and associated foreground-process importance may be lost. Under system memory or background restrictions the application process may stop, interrupting the in-process timer and audio; the service itself cannot guarantee recovery or uninterrupted countdown execution.

**Duration/user control (English):**

> The user explicitly starts a Class or Free Timer. The status service is associated with active timer status, including supported paused states, and its notifications are removed as timers end or the app task is dismissed. If notification permission is unavailable, timing and audible cues can still operate without foreground status notifications.

**Review video:** Show user action starting Class and Free, dual ongoing notifications when backgrounded, independent stopping, and notification cleanup. The user-recorded ~67-second clip is the existing demonstration; link still **not verified publicly streamable**. No claim of exact-alarm scheduling, standalone FGS countdown/audio, or Android process-kill recovery.

Google official FGS statement: https://support.google.com/googleplay/android-developer/answer/13392821

## A3 — Data Safety / SDK source audit (conditional, NOT completed declaration)

**Frozen source checks:** `app/build.gradle.kts`: `compileSdk=targetSdk=36`, `versionCode=54`, release ID `com.ateliertime.android`, debug-only `.dev` package suffix. Direct app runtime dependencies: AndroidX Activity Compose, Compose UI/Foundation/Material3 via BOM. No direct Firebase, ad SDK, analytics, network client or cloud SDK. **Source app manifest has no INTERNET permission**, and disables Android backup (`allowBackup=false`). Source runtime audio, Class/Free, private presets, sounds and report code operates locally. Diagnostic Run reports are JSONL in `filesDir/atelier_run_reports` and may contain class names, schedules, actions, timing and basic device information; user-initiated Export writes to a chosen Android document-provider destination. No automatic report upload identified in audited source.

**Candidate (not definitive):** select **No collection and no declarable sharing** *only if* the exact Code54 release/merged manifest and resolved release dependencies have no off-device data flows beyond explicitly user-initiated document export, and the Google questionnaire's current definitions confirm this exception. Cloud-backed export destination is chosen by the user. Do not falsely claim "encrypted in transit" if the app transmits no data. Verify all third-party components, including transitively resolved dependencies, before submitting. No actual signed AAB bytes, merged release manifest/dependency inventory or live Play SDK inventory were available in this environment during Batch A. Historical Samsung local signed-AAB artwork/manifest/signature checks are valid evidence of those checks but **not a complete independent review of SDK data transfer**.

**Internal-only exemption is NOT a Production exemption:** Data Safety required for closed/open/production even for apps that collect no data. Public and in-app privacy links required.

Google official: https://support.google.com/googleplay/android-developer/answer/10787469 and https://support.google.com/googleplay/android-developer/answer/10144311

## A4 — Privacy Policy: exact proposed wording, NOT yet published

**Existing public URL:** https://aviy99.github.io/Atelier-Time/privacy-policy.html (linked from the installed app and previously phone-verified).
**Source:** `main/docs/privacy-policy.html`, section 4 currently only names the Class status notification. Preserve site style, contact, report export, storage/deletion and other sections.

**Replace only the opening paragraph of section 4 with this DRAFT after a separate Batch B approval:**

> On supported Android versions, Atelier Time may request notification permission to display ongoing Class Timer and Free Timer status notifications, including separate notifications when both timers are active. If you decline notification permission, ordinary timer operation and audible cues can still continue, but live notifications may not be displayed. During a user-started Class or Free Timer, a foreground service can maintain ongoing status notifications, support the application's process while in the background, and remove notifications when timers end or the app task is dismissed. The service itself does not calculate countdowns or play audio, and it does not guarantee uninterrupted operation after Android terminates the application process. These notifications do not upload timer information to the developer or a server.

Do **not** edit this public HTML, `PRIVACY_POLICY.md` or GitHub Pages in Batch A. On approval, synchronize any parallel published Markdown policy wording (if maintained) and update the page's "Last updated" date.

## A5 — Store listing draft / search terms (not entered in Console)

Official Google Play limits: App name **30**, short description **80**, full description **4,000** characters. Keywords belong in truthful, natural descriptions; Google Play does **not** provide a stand-alone keyword metadata field. Avoid spammy repetitions or claims that exceed the app's current capabilities.

**App name:** `Atelier Time` (12 chars).

**Short description (English):**
> Flexible timers for drawing, sculpting and live-model studio sessions.

**Full description (English):**

> Atelier Time helps artists and studio instructors organize timed drawing and sculpting sessions.
>
> Plan and save classes with posing sessions, breaks and a summary period. Review the class timeline, choose the finish time and run the session with clear on-screen countdowns.
>
> Use a separate Free Timer for flexible timing. Class and Free Timer can run at the same time, with independent status notifications when notifications are enabled on Android.
>
> Customize cue sounds and their playback settings. Choose sounds supplied with the app, system sounds, or an individual sound file through Android's picker.
>
> Adjust the appearance for your studio and optionally keep the screen awake while timers are active. Diagnostic Run reports are stored locally and can be exported to a destination you choose.
>
> Atelier Time works offline and does not require an account.
>
> Notification and background behavior depends on Android permissions and device conditions.

**Natural discoverability vocabulary** (do not add as an isolated keyword list in the Store): studio timer, art class timer, atelier, live-model drawing, figure drawing, posing sessions, sculpting timer, drawing timer, breaks. English is the proposed listing locale; Hebrew localized listing is optional and not presumed approved. Google review/metadata compliance still pending.

Official: https://support.google.com/googleplay/android-developer/answer/9859152 ; https://support.google.com/googleplay/android-developer/answer/9898842

## A6 — Approved store artwork: source-of-truth audit

**Authoritative verified GitHub receipt:** `main/artwork/play-store/GOOGLE_PLAY_APPROVED_ASSETS_ARCHIVE_VERIFIED_2026_11_01.md`.
**Original approved archive:** `main/artwork/play-store/Atelier_Time_Google_Play_Approved_Assets_Complete_Pack.zip`, recorded GitHub object SHA-1 `506a9b9e6dd27b7d4531e71d1053096de1d3e6b0`, ZIP SHA-256 `289de4efa7f5f7f9e51995265d964931c9ffa6aa059a30c3d6c105aa5a48b80f`. Prior November 1 receipt verified the binary and CRC; this Batch A **re-read the receipt but did not newly download/re-hash ZIP bytes**.

Approved contents: 512×512 Store icon; **locked original** 1024×500 feature graphic; **exactly 8** approved 1080×1920 screenshots, in order: Splash, Class Editor, Complete Class Timeline, Saved Classes and Reports, Class Timer Progress, Free Timer, Design Settings, Sound Choices. All 10 screenshot originals are additionally preserved inside the ZIP. No recreating or altering art. Console visual/upload verification is still a user-assisted step and **not done**.

## A7 — Reuse Code 54 for Production without a new build

Google official release workflow includes **"Add from library"** for a previously uploaded version. Accordingly, Code 54 is **technically eligible to be selected from Play's artifact library** if Console offers it for the target track and there is no new policy/code defect; this is **not** proof that Production access has been granted or that Google approves FGS. **No new AAB/build/upload/signing is required merely because the rollout track changes.** Actual eligibility and selection remain to be verified in Play Console.

New personal developer accounts created after Nov 13 2023 need **Closed testing: minimum 12 testers continuously opted in for at least 14 days**, then request/receive Production access; Internal Testing does not fulfill this. The user's account has been described as personal and created in 2026; account-specific Production access status still requires the Console screenshot. Google review may ask for more testing.

Official: https://support.google.com/googleplay/android-developer/answer/9859348 and https://support.google.com/googleplay/android-developer/answer/14151465

## A8 — App content and account worksheet (draft; actual Console forms remain unsubmitted)

| Console subject | Source-supported proposed answer | Condition / action |
|---|---|---|
| Privacy Policy | `https://aviy99.github.io/Atelier-Time/privacy-policy.html` | Link already in app; verify globally accessible and update Free Timer paragraph before wider track |
| Foreground service permissions | `FOREGROUND_SERVICE_SPECIAL_USE`; select `Other` in user's observed screen | Use A2 text and publicly playable proof video; policy approval remains uncertain |
| Data Safety | **Tentative No collection / No declarable sharing** | Full Code54 merged-manifest/resolved-SDK check and review of final current question wording first |
| App access | All main functionality accessible without account or login | Confirm exact Console options and save |
| Ads | **No ads** based on direct dependencies / audited app behavior | Check release SDKs and no hidden ad services |
| Target audience | Intended for studio instructors/artists; do **not** infer age groups from code | Publisher selects actual target age groups based on audience; avoid accidental child-directed representation |
| Content rating | Timing/utility app; no user-generated social feed, violence or interactive gambling in source | Complete actual IARC questionnaire accurately; don't invent an age rating |
| News, government, health, financial, gambling, social, special policies (if Console prompts) | No corresponding product features identified in reviewed source | Respond only to actual Play form prompts and any further evidence |
| Trader status | **No unilateral Trader/Non-trader selection** | User must determine commercial/professional context and countries; Google policy for new accounts requires declaration immediately, existing by 2027-02-01 |
| Store listing | App title, draft texts A5 and approved art A6 | Await actual Console listing check and user approval |
| App support | Existing privacy contact: `Avi_yosfan@walla.com` | Verify Console's desired public contact fields; do not invent phone/address |
| Additional permissions | `POST_NOTIFICATIONS`, `MODIFY_AUDIO_SETTINGS`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE` in source manifest | FGS specialUse review; no direct INTERNET in source; verify merged release |
| About → WhatsApp | Current share points to Instagram | Future **app code** change would require user approval and new version. Do not force rebuild Code54 for this item without a real release blocker |

Prerequisite details: Google target-audience form may depend on Ads/App access and privacy policy; user's actual Console screenshot dictates exact sequencing. Google trader declaration: https://support.google.com/googleplay/android-developer/answer/17594752

General app content: https://support.google.com/googleplay/android-developer/answer/9859455 ; target audience https://support.google.com/googleplay/android-developer/answer/9867159

## A9 — Group closure and approval gates

| Batch A item | State | What was **not** done |
|---|---|---|
| 1. Video technical inspection | **Done: local inspect/decode** | Drive public reviewer playback not verified |
| 2. Draft FGS text | **Done: prepared** | No form filled, no Google approval |
| 3. Data Safety source audit | **Done: conditional source audit** | No independently inspected signed release AAB/SDK dependency graph; no form submitted |
| 4. Privacy wording | **Done: exact draft** | No public page changed |
| 5. Store listing name/descriptions/search terms | **Done: drafts** | Not entered or approved on Console |
| 6. Store art | **Done: GitHub receipt/catalog verification** | No new ZIP byte validation or Console upload check |
| 7. Code54 promotion rule | **Done: official Add from library confirmation** | No actual Console selection or eligibility grant |
| 8. App content worksheet | **Done: conditional draft** | No declarations made |
| 9. Documentation/handoff | **This document + authoritative handoff and index update** | No code/stable branch change |

**Batch B — separate user approval required:** public privacy site/Markdown update based on A4; determine whether new video derivative is actually needed and if so create (do not upload without user/Drive authorization); package final copy/paste instructions and final Production gate checklist; then update docs/handoff and seek approval again.

**Group C — user/Google dependent:** Drive upload and demonstrably public playable video; FGS Console filing; Data Safety, App content, store listing/graphics, trader declaration; personal-account Closed Test and Production-access request as applicable; Google review; user-approved Production release. **No release or upload is authorized merely by these drafts.**

**Excluded:** Crash/accidental Class Timer recovery idea was explicitly deferred by user; no implementation or release planning under this gate.

**No mutations other than documentation described here:** no app source/edit/manifest/change, build, paid actions, signing key, Play Console action, Google Drive upload, device action, installer change, data loss, artwork modification, or edit to frozen Stable17/18/19 refs.
