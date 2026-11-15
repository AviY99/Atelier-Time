# Atelier Time — Stable 19 / D-160 / Code 54 — Batch B completion and Google Play Console copy/paste packet

**Date:** 2026-11-15 (Asia/Jerusalem). **Status:** Batch B **prepared and documentation/website-source changes complete**; Play Console submissions and public Production **NOT approved or performed**.

**User authorization:** Batch B items 10–14, including public GitHub Pages privacy-policy source change, creating a compatible *copy* of the existing demo video, copy/paste drafts, final readiness checklist, documentation/handoff updates. **Do not interpret this approval as authorization to upload to Google Drive, submit declarations, build/install a new app, change stable branches, or publish Production.**

**Baseline:** Frozen Stable19 / D-160 / `versionCode 54`, release package `com.ateliertime.android`, branch `stable/android-d160-qa19-finish-dialog-ui-user-accepted`, SHA `55e82d51788062368970cde7e747edc5b7e72bb1`. Internal Testing successful; user confirmed in-place Play update preserves saved data. This is NOT a public release.

## B10 — Public privacy policy source edited (GitHub `main`)

- Updated `main/docs/privacy-policy.html`, section **4. Permissions, notifications and audio**: explicitly explains ongoing **Class and Free Timer status notifications**, simultaneous independent notifications, optional notification permission, a user-initiated FGS that supports notification/process status rather than calculating countdown or playing audio, notification cleanup, non-guaranteed process survival, and no upload to developer/server.
- Synchronized `main/PRIVACY_POLICY.md` section 4 with the same product statements.
- Both policy dates changed from 2026-10-30 to **2026-11-15**, with the rest of the disclosures and the current privacy contact retained.
- GitHub commits: `8009338bb6db198822d9e8a98688ed2117ed00ce` (HTML) and `d30966ce1cae67e2e609fab88494a3c4e1c6f352` (Markdown).
- Intended public Pages URL remains **https://aviy99.github.io/Atelier-Time/privacy-policy.html**; already linked by installed About screen. **Source commit is confirmed, but public Pages propagation / unauthenticated global reachability could not be independently verified from this environment**. Require a user-side live Page test in Group C. Do not claim worldwide publication is confirmed.
- No Android source, version code, app link, or signed AAB changed.

## B11 — Review video adapted and verified locally; NOT uploaded to Drive

**Source retained, completely unchanged:** `AtlierTimeRecordingV1-1.mp4`, SHA-256 `01df32b3a7d066dcbb4ae60e1291a03cac2b936765fe5a8d8228618ec26f05ce`; 69,800,471 bytes / 66.938 s; 1080×2340 H.264 Main Level 5.1 variable-frame-rate, AAC LC.

**New derivative created in the conversation runtime:**
`AtelierTime_FGS_Review_Compatible_886x1920_H264_AAC.mp4`
- 6,551,185 bytes / **66.933 s**; **886×1920 portrait**, H.264 High **Level 4.1**, **constant 30 fps**, yuv420p, AAC LC stereo 48 kHz 128 kbps; `moov` metadata precedes `mdat` (`+faststart`).
- SHA-256 `fd61f510bd8f566e86f484f7de7071ec437059805985316b05af6ded636a2ef5`.
- **FULL VIDEO + AUDIO DECODE PASS** via `ffmpeg -xerror`. `ffprobe` output fields verified; 3 representative frames at ~12s, ~39s, ~58s visually checked against intended content (simultaneous active notifications visible at ~39s). No overlays, edits to user data, added visuals, audio substitution, or truncation were intended.
- Re-encoding was a **compatibility precaution**, NOT proof of why the Google Drive original failed to stream. The original already has supported MP4/H.264/AAC codecs; source resolution is taller than Drive's stated 1920×1080 max playback resolution. Portrait orientation 886×1920 keeps the long side within 1920.
- Link for user in conversation: **the produced file is a conversation download artifact**, not a GitHub file, Drive asset or public URL. Local container path `/mnt/data/atelier_time_batch_b/AtelierTime_FGS_Review_Compatible_886x1920_H264_AAC.mp4` was verified when generated. No private signed files involved.
- **Still required:** user uploads *this derivative as a new separate file* to Drive, sets **Anyone with the link — Viewer**, tests **inline Play** from an unauthenticated/incognito Chrome browser from start to finish, and then supplies the **new Drive URL** for Play FGS declaration. The old URL does not magically point to the new file. Check reviewer accessibility and re-review processing status. Video visibly labels `Atelier Time DEV`, which may raise a reviewer identity question; no re-recording is being requested without actual need.

Google support: https://support.google.com/drive/answer/2423694

## B12 — Console copy/paste packet (all DRAFT, adapt to actual on-screen field labels)

### 1. Foreground service — use-case

**Permission identified:** `android.permission.FOREGROUND_SERVICE_SPECIAL_USE`. **Observed choice:** `Other`. Source Manifest subtype: `User-started Class and Free Timer countdown statuses; dismiss on task close`.

**Functionality description:**

> Atelier Time provides user-initiated Class and Free Timers for live drawing and sculpting sessions. During an active timer, a specialUse foreground service manages ongoing status notifications, including separate Class and Free notifications when both timers are active. It handles foreground-notification ownership and notification removal when a timer ends or the application task is dismissed. The service runs in the same Android application process as the timer and audio components, indirectly supporting the process while the user teaches with the phone in the background. The service itself does not calculate time, play audio, or restore a timer after process termination. Users explicitly start and stop the timers.

**If task initiation is deferred:**

> If foreground status service startup is delayed, the user may not promptly receive ongoing timer status notifications or expected task-dismissal notification handling. The application's separate timer and audio components run in a less protected process when it is backgrounded.

**If task is interrupted/stopped:**

> If Android stops the foreground service, the status notifications and associated foreground-process importance may be lost. Under system memory or background restrictions the application process may stop, interrupting the in-process timer and audio; the service itself cannot guarantee recovery or uninterrupted countdown execution.

**Expected duration/control:**

> The user explicitly starts a Class or Free Timer. The status service is associated with active timer status, including supported paused states, and its notifications are removed as timers end or the app task is dismissed. If notification permission is unavailable, timing and audible cues can still operate without foreground status notifications.

**Video URL:** `PENDING — paste only the new, independently tested, publicly streamable Drive link`. Existing link's Drive inline playback produced an error; do not submit it as verified reviewer evidence. Never claim this declaration was saved/approved.

### 2. Store listing — proposed English text (not submitted)

**App name:** `Atelier Time`

**Short description:**

> Flexible timers for drawing, sculpting and live-model studio sessions.

**Full description:**

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

No standalone Google Play keyword metadata field; use natural terms in descriptions. This English draft is a **proposal for user review**, not a user-approved listing. App name limit 30, short description 80, full description 4,000 characters.

**Optional release notes for a *future* Code54 Production release (DRAFT):**

> Stable 19 brings a clearer Class finish-time confirmation screen, improved Class and Free Timer background notifications, and interface refinements.

### 3. Data Safety and App content (conditional decision worksheet, NOT a submitted declaration)

| Subject | Prepared answer | Mandatory check before clicking Save |
|---|---|---|
| Data Safety — collects/shares user data? | **Candidate: No collection / No declarable sharing** | Inspect Code54 **release merged manifest and resolved runtime/transitive SDK dependencies**, ensure no unreported off-device transfers; explicit user-directed exports may qualify for an exception under current Google definitions; read actual field wording |
| Privacy Policy URL | `https://aviy99.github.io/Atelier-Time/privacy-policy.html` | Confirm updated page with Free Timer from an unauthenticated device |
| App access | No sign-in/account is required; app functions available without special access | Match actual Console questionnaire |
| Ads | No ads identified in audited source/runtime direct dependencies | Validate exact release SDKs |
| Target audience | Art/studio instructors and drawing/sculpting users | **Publisher must select actual intended age groups**; don't infer or falsely classify child-directed |
| Content rating | Timer/creative studio utility function | Complete Google IARC answers based on real prompts; do not invent rating |
| Trader status | **Decision pending publisher** | Commercial/professional activity and regional rules determine Trader/Non-trader; no unilateral selection |
| App name | Atelier Time | Verify Play listing saves and that `(unreviewed)` temporary name has gone through required review |
| App support | Privacy contact already documented in policy | Confirm suitable public support fields; do not invent phone/address |
| Additional declarations | `POST_NOTIFICATIONS`, `MODIFY_AUDIO_SETTINGS`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE` in source | Some Console questionnaires may be omitted/added per actual app; do not guess |
| Artwork | 512×512 approved icon; locked 1024×500 feature graphic; approved eight 1080×1920 screenshots in documented order | Verify actual uploaded assets in Console; preserve approved artwork unchanged |

**Important:** Google Play *Internal Testing* does **not** prove that FGS, Data Safety, Trader or Production requirements have been satisfied. Source-only Data Safety audit is not conclusive. Do not answer 'all data encrypted in transit' without a verified data-flow context.

Official: https://support.google.com/googleplay/android-developer/answer/13392821 ; https://support.google.com/googleplay/android-developer/answer/10787469 ; https://support.google.com/googleplay/android-developer/answer/9859152 ; https://support.google.com/googleplay/android-developer/answer/9859455

## B13 — Final Production readiness checklist / decision gates

| Gate | Criterion to accept before next gate | Known state after Batch B |
|---|---|---|
| A. Source baseline | Stable19 Code54, existing signed Play AAB and user-updated install confirmed; no stable modifications | **PASS historical evidence** |
| B. Privacy source | `main/docs/privacy-policy.html` and Markdown reflect Class + Free Timer | **PASS committed**; public Pages visibility **not externally proven** |
| C. FGS video | Compatible derivative uploaded, shared to anyone/viewer, **plays in browser without login** across clip | **PENDING user Drive action**; converted file locally decoded |
| D. FGS policy | Declaration `specialUse` truthful, feature rationale, interruption/deferral and reviewer video entered; Google accepts | **PENDING Console/Google** |
| E. Data Safety | Exact Code54 release AAB merged manifest + resolved SDK inventory reviewed, form truthfully submitted | **PENDING artifact/source verification and Console** |
| F. App content | App access, Ads, Target audience, Content rating and applicable remaining questionnaires complete | **PENDING Console/user input** |
| G. Trader/account identity | Publisher determines Trader status and submits correct account information | **PENDING publisher/Console** |
| H. Store listing | Public name, English descriptions, privacy contact and approved icon/feature/8 screenshots match user-approved assets | **PENDING Console review** |
| I. Production access | Confirm entitlement; for new personal 2026 account typically Closed Testing at least 12 continuous opt-ins for 14 days plus approved Production access | **PENDING**; Internal Testing does NOT replace Closed Testing |
| J. About → WhatsApp placeholder | Decide whether existing Instagram-targeted share link can remain for first public release; a code fix would require new version and user approval | **OPEN decision**; do not silently change source |
| K. Reuse Code54 | Console Production release may offer **Add from library** for Code54 AAB; no new build solely for different track | **Official workflow supported; actual Console availability not checked** |
| L. Release warnings | Deobfuscation mapping and native debug symbols warnings reviewed; known nonblocking for Internal Testing | **Historical warnings, recheck only if Production prompts** |
| M. Final review | No blockers, data-preserving update path, correct release identity, no unexpected permissions; review summary against device/Console snapshots | **PENDING** |
| N. Production publication | Explicit separate user approval after reviewing exact release plan; Google's review/approval | **NOT AUTHORIZED** |

**Risk-first sequence for Group C:** confirm Production eligibility/Closed Test first (may govern calendar), then verify video public streaming and Play FGS policy, privacy website, Data Safety/release SDKs, account/App content/Trader, Store Listing/artwork, reuse Code54, then an explicit separate Production-approval gate. Handle actual Console screenshots not inferred UI.

**Closed Test requirement:** Google's official guide states that personal developer accounts created after Nov 13, 2023 generally need 12 testers continuously opted in for >=14 days, followed by an application for Production access. Existing Internal Testing alone doesn't satisfy this. Check actual account eligibility before making claims.

**No change to Class recovery feature:** user deferred its implementation; not a prerequisite for Production.

## B14 — Completion and GitHub handoff

- Batch B results recorded in this file on `main`; canonical handoff `docs/HANDOFF_CURRENT_STABLE19_TO_GOOGLE_PLAY_2026_11_13.md` and documentation index to point here and indicate Group C requires separate user approval.
- Public policy source changed in two documentation files only; video derivative created in conversation runtime only.
- **No app code**, frozen stable branches, source manifest, `versionCode`, user data, approved artworks, keystore/signing, AAB, Play Console forms, Drive uploads or Production publication were changed.
