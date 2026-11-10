> **2026-11-10 SAMSUNG BUILD VERIFIED BY USER SCREENSHOT — D-158 / CODE52 LOCAL QA PASS, DEVICE ACCEPTANCE PENDING.** User ran the guarded phone-local D158 build on Samsung Code on the Go. Terminal reports **D158 ALL JVM ACTUAL: tests=326 failures=0 errors=0 skipped=0**, **D158 BACKGROUND REGRESSIONS: 8 tests, 0 failures/errors/skips**, **D158 FULL JVM PASS**, **D158 APK ARTWORK INCLUDED PASS**, and **D158 JVM TESTS AND BUILD SUCCESSFUL**. Produced `/storage/emulated/0/Download/Atelier-Time-D158-Code52-DEV-QA.apk` plus a private build log `Atelier-Time-D158-Code52-QA-20261110-204211.log`. **AndroidTest APK compiled, instrumentation NOT executed.** Debug install package `com.ateliertime.android.dev` / **Atelier Time DEV** is designed for side-by-side install next to Play Internal Testing Stable17 D157/Code51 `com.ateliertime.android`; **installation and on-device UI/notification verification of D158 have NOT yet been reported**. Next: install DEV candidate only, then verify Class grouped numbering in shade/lock-screen; Free Running/Paused; both simultaneously independent through stop/finish; app exit/background, sounds, and no effect on Play version. **Stable17 frozen unchanged; no new Stable18, AAB, Play upload, paid cloud CI or public release.** [D158 plan and evidence](D158_PROPOSED_BACKGROUND_NOTIFICATIONS_GROUP_LABELS_AND_PARALLEL_FREE_TIMER_2026_11_09.md). Earlier "build pending" notes below are superseded.

> **2026-11-10 — D158 / Code52 USER-APPROVED SOURCE COMMITTED; LOCAL BUILD/DEVICE QA PENDING.** Class notification labels now match grouped/regular Class screen; separate Free status ID109 and Class ID107 coexist through one foreground service. Class/Free clocks, alarms and stored data are not intentionally changed. Added **8 JVM regression cases** (expect at least **326 JVM tests overall**) and guarded local `android-native/tools/build_d158_group_and_parallel_notifications_on_phone.sh`. Debug QA uses `com.ateliertime.android.dev` (Atelier Time DEV), installed separately from Google Play Stable17 D157/Code51. **No Samsung D158 build PASS, AndroidTest execution, user device acceptance, Play upload, Stable18 freeze, or production release has occurred.** Current stable remains unchanged. [D158 implementation/QA instructions](D158_PROPOSED_BACKGROUND_NOTIFICATIONS_GROUP_LABELS_AND_PARALLEL_FREE_TIMER_2026_11_09.md). Previous approval-pending entries are historical.

# CURRENT AUTHORITATIVE HANDOFF — Atelier Time Stable 17 / D-157 / Code 51 → Google Play

**Updated:** 2026-11-07, Asia/Jerusalem. This file supersedes the Stable16 handoff as the **current** entry point. Historic earlier handoffs and work records remain available but their "latest/pending" wording reflects the earlier time.

## 1. Stable recovery and accepted build

**Latest accepted/frozen:** **Stable17 / D-157 / versionCode 51**, package `com.ateliertime.android`, compile/target SDK36. The user explicitly reports installing the Samsung-local D157 APK and finding everything working, and explicitly declares **stable number 17**. Prior Stable16 is **D153/code47**, *not* D157; D151 was Code45, *not* Code51.

**Frozen branch:** [`stable/android-d157-qa17-user-accepted`](https://github.com/AviY99/Atelier-Time/tree/stable/android-d157-qa17-user-accepted) at exact **`f6866dca809f6214c6d1f88c1adf8f6592163ada`**. This is the D157/code51 DEV source snapshot plus the corrected *post-success shell duplication* in its build helper, frozen **before any new acceptance-document changes**. Do not modify this or any earlier stable branch. Previous Stable16 is `stable/android-d153-qa16-ui-sound-user-accepted` at `04270365f330af78861d54f3710048d65dadc462`, unchanged.

**Evidence:** Existing 2026-11-07 Samsung screenshot reports **318/318 full executed JVM tests PASS**, no errors/failures/skips; Gradle debug APK built; AndroidTest compiled (not run); approved splash SHA256 matched *inside* APK; private manifest saved locally. Candidate debug APK `/storage/emulated/0/Download/Atelier-Time-D157-API36-Candidate.apk`. Following success, a duplicate/truncated shell block emitted `line 480` error; subsequently removed from DEV without app source changes. User now confirms APK installation + general device functionality; do not claim device instrumentation or exact APK binary hash/source_commit without the private manifest. This is a recovery checkpoint, **not a Play signing or submission**.

**Accepted functional scope:** D154–D157 Sound and volume improvements; Android real Media/Alarm stream minimum, synchronized Sound ON/OFF and timer volume, Mute honoring allowed minimum, immediate Max without auto restore, compact unified Free/Class volume row (two-line left label, 12dp track, 24sp percent, accessible touch area), identical centered selected-stream-zero warning, separate Sounds upper and lower Help. Existing class timer logic and saved data preserved by user acceptance. [Full certificate](STABLE17_D157_VOLUME_AND_SOUND_USER_ACCEPTANCE_2026_11_07.md); [D157 details](D157_COMPACT_VOLUME_ROW_UNIFIED_WARNING_AND_HELP_2026_11_07.md).

## 2. Working model for next conversation

- **Application development:** `android-native-kotlin-compose` branch, currently D157/code51. Any future changes need a subsequent development revision and separate local QA; do not alter `stable/android-d157-qa17-user-accepted`.
- **Documentation, main project navigation and approved Play artwork:** `main`. The current handoff, certificate, README, CHANGELOG, index, ROADMAP, WORK_LOG and D157 entry are updated to point here. DEV docs may mirror this accepted state; historical snapshots retain their dates.
- **On-device build preference:** user compiles with Samsung Code on the Go; avoid paid cloud/GitHub Actions builds. Build APKs locally, preserve installed app data. Never represent compiled AndroidTest APK as run instrumentation.
- **Source/release distinction:** Code 51 is accepted *debug APK* code. No signed Play AAB yet. Do not change versionCode to 52 merely for promotion or relabel `versionName` as "Stable 17" in the app without a new, separately approved app revision.

## 3. Google Play continuation (not yet completed)

Commercial/distribution decision remains **FREE** and **private invitation-only Internal Testing first**, no public launch now. Previous Google Play personal developer account and Play-store artwork work are documented. Approved store artwork archive remains `main/artwork/play-store/Atelier_Time_Google_Play_Approved_Assets_Complete_Pack.zip` (keep unchanged); in-app About Privacy Policy URL is `https://aviy99.github.io/Atelier-Time/privacy-policy.html`.

Proceed in order and verify current external conditions separately:
1. Confirm Play Console developer/account eligibility, app listing/identity verification state and store metadata.
2. Review actual accepted **D157/Code51** release dependencies/manifest against Privacy Policy, Data Safety and required foreground-service `specialUse` declaration/evidence/video. Prior Stable12 source audits are a starting point, not automatic approval for Code51.
3. Only with separate user authorization, prepare a **locally signed release AAB**, securely keeping keystore/secrets off GitHub and the chat, check its versionCode51, package, signatures, Android requirements and included approved artwork.
4. Only with explicit user approval, upload to **Internal Testing** and invite testers. Production/public release remains a separate decision.

**No Play submission, new AAB build, account mutation, notification, data wipe, cloud paid workflow, source-code change, or automatic deployment was performed during Stable17 freeze/documentation.**

## 3A. Optional upgrades AFTER initial release (not a blocker)

**Decision recorded 2026-11-08:** Two future optional upgrade tracks were accepted **for further research only**: (1) external screen mirroring or a dedicated **Studio Display** for desktop/TV/projector/second phone; (2) a paired, explicitly authorized **Remote Control** of the *primary-phone timer* from a desktop/second phone. First examine no-code screen mirroring with the currently approved APK; later consider view-only large-screen browser displays with QR/session pairing, and separately control rights. Network reliability, single timer authority, security, audio routing and app policy impact require design/QA before any coding. **Not implemented, no development version assigned, not required for the first Play Internal Testing release.** [Detailed optional upgrades plan](OPTIONAL_POST_RELEASE_STUDIO_DISPLAY_AND_REMOTE_CONTROL_2026_11_08.md).

### About → WhatsApp sharing: pre-production store-link gate (2026-11-08)

**Verified from frozen Stable17 source** `android-native/app/src/main/java/com/ateliertime/android/AtelierDesign.kt`: the `Share with a friend` About card uses `shareAtelierTimeOnWhatsApp`, which currently shares a draft message with `https://www.instagram.com/aviyosfan/` via WhatsApp, **not** the Google Play listing. This was deliberate before any listing existed (D133 implementation); user must manually choose recipient and send. **For the initial invite-only Internal Testing release this can stay unchanged**; test access is limited to authorized opted-in testers, not any WhatsApp recipient, and a generic store link may not work for non-testers. When Internal Testing is available, send official Play opt-in/test access link *separately* to invited testers.

**Required checkpoint BEFORE first public Production distribution:** Replace About WhatsApp share text/destination with a verified, accessible public Google Play product listing link, using package `com.ateliertime.android`; expected URL pattern `https://play.google.com/store/apps/details?id=com.ateliertime.android` but **do not treat it as currently public/live or blindly deploy an unverified link**. Consider a permanent project-controlled landing URL that can be redirected to the verified Play listing later, if user approves. Update accompanying About text (currently says `For now, share the developer's Instagram link.`), regression-test WhatsApp+Business+fallback, rerun on-device QA and prepare a new versionCode signed AAB **only with user authorization**. Do not edit immutable Stable17. This is a **public-release checklist gate, not a blocker to current Internal Testing**, and no source change or Play action has occurred.

### 2026-11-08 — Phone-local signed AAB preparation (NOT YET BUILT OR UPLOADED)

User completed local generation and verification of the private PKCS12 upload key `atelier-upload` (RSA 3072) on Samsung Code on the Go, and user reports that an off-device backup has been made. **Never request passwords or the private keystore via chat; never add secrets or keystores to GitHub.** The current app on the phone is a debug-signed D157 APK; Play-signed builds will have a different certificate and **may not update in-place**, so avoid uninstalling / wiping installed Saved Classes and diagnostic data without an accepted preservation plan.

Prepared **script only**, on Android DEV `android-native/tools/build_d157_signed_aab_for_internal_test_on_phone.sh`. It verifies that Android app source tree equals frozen Stable17, exact D157/code51 Gradle settings and original splash, runs local `:app:bundleRelease` using CoGo Gradle 9.6.1 + Android SDK36 aapt2 override, checks original art inside AAB, signs with the **private, already backed-up** upload key using interactive `jarsigner`, then verifies the signed JAR and app bundle structure. It never changes app code, overwrites an existing signed AAB, runs paid CI, installs the app, or uploads to Play. **No release AAB build, signing or phone execution of this helper is confirmed yet.** Source-level validation completed; command includes `bash -n` phone syntax check before execution:

```bash
cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native && git pull --ff-only && bash -n tools/build_d157_signed_aab_for_internal_test_on_phone.sh && bash tools/build_d157_signed_aab_for_internal_test_on_phone.sh
```

If successful, output in Downloads: `Atelier-Time-D157-Code51-InternalTest-SIGNED.aab`, with release build and signature-verification logs. Actual user screenshot/log required before any claim of PASS or before instructions to upload. Google Play Internal Testing has a selected list of 2 invitees (confirmed user screenshot); Play App Signing active; Automatic Protection / auto-translation optional and not enabled. Public About WhatsApp share still points to Instagram, tracked separately as a **pre-PRODUCTION** task, not a blocker for Internal Testing.

### 2026-11-08 — ACTUAL Samsung D157/Code51 signed Play AAB build SUCCESS (user screenshot)

**The previous “not yet built” note below is historical and superseded by this update.** After the user confirmed an off-device backup of their private PKCS12 upload key, they ran the locally guarded \`android-native/tools/build_d157_signed_aab_for_internal_test_on_phone.sh\` on Samsung Code on the Go. Their 22:57 screenshot directly reports:

- \`D157 Code51 exact frozen app source verified. Building release AAB locally...\`
- \`AAB ARTWORK INCLUDED PASS\` with the original approved splash inside the AAB.
- First interactive jarsigner attempt ended \`you must enter key password\`; **the user retried and successfully signed**. This earlier attempt is **not the final build status**.
- Second attempt: \`jar signed.\`, then \`AAB STRUCTURE AND SIGNATURE ENTRIES PASS\`, then \`SIGNED AAB READY FOR REVIEW — NOT UPLOADED:\` in phone Downloads.
- Signed local artifact: \`/storage/emulated/0/Download/Atelier-Time-D157-Code51-InternalTest-SIGNED.aab\`; screenshot reports about **11 MB** and printed SHA256 (private local artifact; not uploaded to GitHub). Local build and signature verification logs are in Downloads; \`jarsigner -verify\` passed according to the guarded script's successful termination.
- The JAR signer emits the standard self-signed certificate warning, expected for a new private upload key; Google Play App Signing remains enabled for delivery. No signing key or password has been exposed in documentation.
- **Boundary:** Build, local signing and structural validation **PASS based on user screenshot**. No independent inspection of the actual AAB bytes, Google Play bundle acceptance, app signing certificate, release rollout, or tester install has yet been reported. Do **not** claim Play approved or published the AAB. No paid CI or app uninstall/data wipe.

**Next:** return to the already-open Google Play Console **Internal testing > Create release > App bundles > Upload** and select that one signed AAB, then inspect Google validation (package \`com.ateliertime.android\`, versionCode \`51\`, SDK, signatures, policy errors). Upload to the release draft is not rollout. Release name may be \`D-157 / Stable 17 — Internal Test\`; add English release notes when requested. **Do not press Review/Confirm rollout** until Play validation and any compliance/setup actions are reviewed. Existing installed debug APK may have a different signing certificate from Play-delivered APK; do not uninstall it or try to replace it in place without preserving user data first. Test on a second consenting device if necessary. Public About WhatsApp listing-link replacement remains a separate pre-Production requirement.


## 2026-11-09 — Google Play installed and two background notification fixes awaiting approval

The user **installed** the D157/Code51 Google Play Internal Testing build on their own Samsung and explicitly confirmed full working parity with the former locally installed stable debug APK. This is **successful user field QA for Play delivery**, not public Production launch. The first tester's install is confirmed; independent second tester QA remains open.

Two **pre-existing** source issues were then reported: (A) Class background notifications use obsolete sequential Session numbers while the app shows Group labels and group-local numbering; (B) Free Timer lacks background notification and must be visible separately **even when Class and Free run concurrently**. Initial source read confirms the Class notification formatter hardcodes poseNumber while the screen uses group-aware `PhaseDisplayNames`; only Class notification ID 107/service is wired. **No code has been edited for these issues**. Full scope, test matrix, Android permissions/FGS compliance considerations and explicit approval gate are in [D158 proposed fixes](D158_PROPOSED_BACKGROUND_NOTIFICATIONS_GROUP_LABELS_AND_PARALLEL_FREE_TIMER_2026_11_09.md).

**Next:** request and receive explicit user approval **before** implementing both fixes together on Android DEV; likely subsequent D158/Code52 candidate. Preserve frozen Stable17, the currently published Internal Testing build and app user data. Do not auto-upload another AAB or start production rollout.

## 4. Evidence pointers and integrity

- [Stable17 acceptance](STABLE17_D157_VOLUME_AND_SOUND_USER_ACCEPTANCE_2026_11_07.md)
- [D157 implementation and Samsung build evidence](D157_COMPACT_VOLUME_ROW_UNIFIED_WARNING_AND_HELP_2026_11_07.md)
- [Stable16 handoff (historical)](HANDOFF_CURRENT_STABLE16_TO_GOOGLE_PLAY_2026_11_06.md)
- [Documentation/branch policy](DOCUMENTATION_INDEX_AND_BRANCH_POLICY.md)
- [Approved Play assets evidence](../artwork/play-store/GOOGLE_PLAY_APPROVED_ASSETS_ARCHIVE_VERIFIED_2026_11_01.md)

**Next development or Play steps require their own permission. All source code and prior stable branches remain unchanged by this promotion.**
