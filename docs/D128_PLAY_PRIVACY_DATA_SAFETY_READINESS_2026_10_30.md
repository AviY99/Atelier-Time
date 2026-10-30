# Atelier Time — D-128 pre-release privacy / Data safety evidence worksheet (2026-10-30)

**Decision/scope:** Documentation-only next release-preparation step after the user approved **keeping existing `specialUse` implementation unchanged**. This worksheet is **not** a published privacy policy, Play form, completed release audit, Play approval, or authorization for app code/UI/build changes. Canonical FGS declaration: [approved `specialUse` rationale and Play Console draft](D128_SPECIALUSE_PLAY_CONSOLE_DECLARATION_AND_RATIONALE_2026_10_30.md).

## Verified source facts for the D-128 Android development branch

1. `android-native/app/src/main/AndroidManifest.xml` currently declares `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`; **no INTERNET** permission is declared in the app's source manifest; `android:allowBackup="false"`. Verify the **release merged manifest** rather than assuming final packaging matches the source.
2. `android-native/app/build.gradle.kts` declares AndroidX Compose BOM 2026.06.01, Activity Compose 1.12.4, UI/Foundation/Material3, and debug/test-only libraries. There is **no declared advertising, analytics, cloud-sync SDK or third-party service client** in this app module; inspect resolved runtime dependency tree and packaged AAB to verify final transitively included code before answering Play forms.
3. `AndroidPresetStore.kt` persists Saved Classes, timeline rule preferences and selection in Android application-private SharedPreferences. Existing ordinary in-place updates preserve this data; no account required.
4. `RunDiagnostics.kt` creates **private app-files JSONL run logs**. They record approved Class name and rules, timing events, sound selection metadata, and a `device` block containing **Android API version, manufacturer, device model, app version and timezone**. It deliberately does **not** record selected audio content URIs in the JSONL. The app does not automatically upload these files. These attributes should be described accurately in the privacy policy, especially when users **choose** to export.
5. `RunDiagnosticsLibrary.kt` provides **user-initiated** Export using Android `ActivityResultContracts.CreateDocument`, and explicit Delete with a confirmation dialog. The user selects the destination (which could be local or a provider-controlled destination); the app does not initiate a background upload to the publisher.
6. `ActiveClassStatusNotification.kt` respects notification permission and lets the user continue the Class without posting notifications if disabled. Notification text shows class status; avoid filming/exposing personally named classes inadvertently.
7. Audio settings allow user-selected system or document-provider sound files; storage access occurs through OS system picker/persisted URI. No broad media scan is declared in the source.

## Proposed Data safety response — NOT submitted / conditional

**Candidate working position:** The reviewed D-128 source is **offline-first and does not appear to automatically collect/transmit user data off-device to the developer**; local-only calculation and storage are generally **not Play-defined collection**. This is *not* a signed-release or resolved-transitive-dependency verification; do not submit a categorical "no data collected or shared" answer until merged manifest, runtime SDKs and user-initiated export treatment have been checked. Official: https://support.google.com/googleplay/android-developer/answer/10787469 .

**User-selected export nuance:** When a user chooses to export a Run JSONL file, the document provider/destination is selected by that user; Google's Data safety definition of "sharing" can include transfers to another on-device app but identifies an exclusion for **specific user-initiated transfers where sharing is reasonably expected**. Check this against the precise export UI and Play form wording; do not claim the app automatically shares reports, but do disclose the user's export action in the privacy policy. Official: https://support.google.com/googleplay/android-developer/answer/10787469 .

## Existing draft needing pre-publication finishing

[Privacy-policy EN draft, created 2026-10-29](STAGE8_PLAY_PRIVACY_POLICY_DRAFT_2026_10_29.md) already covers offline local Saved Classes, sounds, reports, export, permission and deletion. Before publishing, ensure it also **explicitly describes device Android/API/manufacturer/model/app version/timezone recorded locally in diagnostic reports** and that exported files can leave app-private control via the user's chosen destination. Keep app-developer contact/publication date as honest **unfilled placeholders** until actual publisher information is confirmed.

Google Play requires an accessible, **non-editable, public, non-geofenced webpage** for the privacy policy, plus an in-app privacy-policy link or text and Play Console link, even for apps with no collected personal data: https://support.google.com/googleplay/android-developer/answer/10144311 . A GitHub Markdown code-view/edit page or a local draft is **not yet a verified compliant hosted privacy policy**.

## Remaining gating work

- Publisher supplies final privacy contact (and exact publisher identity, if needed), then approve final privacy text and hosting; neither publisher identity nor URL is invented here.
- Host public privacy page and verify it loads globally in a normal browser without login; **not done**.
- Make privacy text or link accessible **inside the app**; this is a **future code/UI change requiring explicit authorization**, not approved by this documentation-only request.
- Inspect eventual release merged manifest, runtime dependencies and artifact; **not done** (no cloud builds; future signing/build on user-authorized local workflow).
- Final Data safety answers, age/target audience, app access/store listings, Play permission declaration and actual submission require later release steps; **not done**.
- Obtain Play `specialUse` video/link and reviewer assessment; still OPEN, no policy approval claim.

**No changes to code, manifests, runtime permissions, storage, installed app, user data, builds, tests, stable branches or Play Console have been performed to prepare this worksheet.**
