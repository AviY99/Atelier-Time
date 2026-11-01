> **USER VISUAL CONFIRMATION (2026-11-01):** The user supplied an actual mobile screenshot of the fully rendered Atelier Time Privacy Policy page and explicitly confirmed the page opens from the **already implemented installed-app About link**. Record: [Phone QA evidence / Data Safety / FGS remaining gates](PLAY_PRIVACY_POLICY_USER_PHONE_QA_AND_DATA_SAFETY_GATE_2026_11_01.md). No need to implement the link or upload the screenshot to GitHub. The previous statement below that independent public network retrieval was unavailable still applies to *assistant/global accessibility*, not the user's successful mobile test. Google's policy says Data Safety form is not required for an app *exclusively on Internal testing*; Closed/Open/Production must be assessed. The candidate no-collection/no-sharing answer remains conditional until final AAB SDK/manifest review, and FGS evidence/video remains pending. Stable12 unchanged.

# Atelier Time — Stable 12 / D-141: Privacy and Google Play Data Safety source audit

**Date:** 2026-11-01. **Status:** documentation-only, draft answers; **NOT a Play Console submission, reviewed release AAB, or independent confirmation of the public website's accessibility**. This is the current targeted Data Safety audit of the user's accepted **Stable 12/D-141** (versionCode 35), native Android source branch `stable/android-d141-qa12-final-user-accepted`. Never change this frozen branch. Work follows [the current handoff](HANDOFF_CURRENT_STABLE12_TO_GOOGLE_PLAY_2026_10_31.md).

## Already completed — do not repeat

- The user explicitly confirmed the **Privacy Policy link already exists in the installed app**. Static source verification: `android-native/app/src/main/java/com/ateliertime/android/AtelierDesign.kt` lines 598–609 and 624–625 contain an active About footer link and the exact `ATELIER_PRIVACY_POLICY_URL` below; lines 646–672 enable the HTTPS button using `uriHandler.openUri(url)`. It is **not pending implementation**.
- Canonical privacy URL to enter in Google Play Console **when app creation becomes available**: **https://aviy99.github.io/Atelier-Time/privacy-policy.html**. The source HTML is `main/docs/privacy-policy.html`; the matching full markdown policy is `main/PRIVACY_POLICY.md`. The HTML includes app name and publisher Avi Yosfan, privacy contact, on-device classes/sounds/diagnostics and device metadata, user-directed export, notification/FGS behavior, retention/deletion, and external Instagram link.
- Current website verification limit: a direct independent browser request to the exact GitHub Pages URL could **not be completed** from the assistant's available network environment. This is **not evidence the page is down**. The user already confirms in-app linking; **no new UI build, URL or privacy host is requested**. At Play release/Console review, confirm the exact page opens in a public, login-free browser; don't claim Google approved the URL.
- The store icon, locked original feature graphic, final 8 screenshots in approved original order and all 10 original screenshot backups are already durably stored **inside the verified [GitHub ZIP](https://github.com/AviY99/Atelier-Time/blob/main/artwork/play-store/Atelier_Time_Google_Play_Approved_Assets_Complete_Pack.zip)**, with [verification receipt](../artwork/play-store/GOOGLE_PLAY_APPROVED_ASSETS_ARCHIVE_VERIFIED_2026_11_01.md). Do not recreate/reupload.

## Verified Stable12 source evidence

| Topic | Verified source / behavior | Consequence |
|---|---|---|
| Manifest | `android-native/app/src/main/AndroidManifest.xml` declares `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`, `android:allowBackup=false`; **no direct `INTERNET` permission**. Service type `specialUse` / subtype declared | No direct network permission in the **source** manifest; must check final **merged release manifest**; foreground service requires separate Play declaration |
| Direct app dependencies | `android-native/app/build.gradle.kts`: Compose BOM, Activity Compose, AndroidX UI/Foundation/Material3; test-only JUnit/AndroidX test. **No ad SDK, analytics SDK, Firebase/cloud SDK or HTTP client declared as a direct app dependency** | Verify resolved/transitive release deps and release AAB before categorical Play declaration |
| Saved classes | `AndroidPresetStore.kt` uses Android private SharedPreferences | Local saved classes, schedules and preferences |
| User sounds/design | `MelodyAudio.kt` and `AtelierDesign.kt` store private sound and design settings; user-selectable audio through Android picker | No automatic upload of user's audio; chosen URI references remain device-local |
| Run reports | `RunDiagnostics.kt` writes private `filesDir/atelier_run_reports/*.jsonl` containing class names/rules, phase/timing events, choices and `device.androidApi/manufacturer/model/appVersion/timezone`. Explicitly omits chosen audio-file URI and sound contents | Local processing; policy correctly discloses device metadata, class names and reports |
| Export | `RunDiagnosticsLibrary.kt`: Android `ActivityResultContracts.CreateDocument`, then `RunDiagnosticsArchive.export` writes to **user-chosen** URI via ContentResolver | User-initiated transfer to destination that can be another app/cloud provider. Privacy policy explains this; assess Google sharing exception as below |
| Deletion | `RunDiagnosticsLibrary.kt` supports deleting one report or **Delete all** private Run reports with confirmation; class deletion has its own controls. Exported file copies are not deleted by in-app deletion | App does not retain copies on publisher server; no in-app cloud-account deletion needed |
| External links | `AtelierDesign.kt` opens privacy URL and Instagram in external browser/handler. WhatsApp sharing sends a *draft text promoting Atelier Time and Instagram*, only after the user taps Share; user chooses recipient/confirm | External app's privacy practices apply. No evidence of app-controlled embedded WebView or uploading class data to social service |
| Background status | `ClassNotificationTaskService.kt` is user-started notification-status FGS, `START_NOT_STICKY`, cleans notification on task dismissal; Compose owns timer and sound | Continue using the existing approved FGS `specialUse` rationale; a separate declaration and reviewer-accessible video are required |

### Important interpretation
Google Data Safety defines **collection** as user data sent *off the device* (including by SDKs); **purely on-device access/processing is not in scope**. Transfers to another app can qualify as **sharing**, **but a specific user-initiated action where the user reasonably expects the transfer is an explicit sharing exception**. Here the Export button first opens Android's save-document destination picker; no background export has been identified. A Share button opens an external app with a templated promotional text, not private reports. Source-based **candidate: neither developer collection nor declarable sharing of personal/user data**. This is **conditional on the release artifact, exact Play form wording and no behavior/SDK changes**; do not claim a signed AAB has already been inspected.

Google official policy references:
- Data Safety definitions, local-only exclusion and user-initiated sharing exception: https://support.google.com/googleplay/android-developer/answer/10787469
- Privacy Policy requirements (public non-editable, worldwide, app + store listing): https://support.google.com/googleplay/android-developer/answer/10144311
- App content Privacy Policy field: https://support.google.com/googleplay/android-developer/answer/9859455
- Android 14+ foreground-service declaration with user-impact rationale and demonstration video: https://support.google.com/googleplay/android-developer/answer/13392821

## Proposed Google Play Console entries (DRAFT / NOT SUBMITTED)

| Area / likely console prompt | Proposed response | Evidence / gate |
|---|---|---|
| Privacy Policy URL | `https://aviy99.github.io/Atelier-Time/privacy-policy.html` | Already implemented in About; final public browser/Play accessibility confirmation remains |
| Data Safety: Does the app collect or share any required user data? | **No — conditional draft** | Local-only data and user-chosen export exception on Stable12 source; verify release merged manifest, resolved SDKs and AAB before selecting |
| User data types | **None to mark as transmitted/collected/shared**, conditional | Local class names/timing/report device context are nevertheless disclosed in full public Privacy Policy |
| Third-party advertising | **No**, based on app source | No ads UI/SDK in audited app module; final dependency/release check |
| App accounts | **No account creation/sign-in** | None in source / privacy policy; user does not have a remote account to delete |
| Data deletion | Local saved classes can be removed; Run reports individually or Delete all; external exports deleted at selected location | Do not assert server-side deletion request system exists or is needed; follow actual field wording |
| Encryption in transit | **Not applicable as a general positive claim** if there is no app-transmitted data | Never select an unverified `all data encrypted in transit` answer merely because browser links are HTTPS |
| Notification permission | Optional `POST_NOTIFICATIONS`; decline does not block timer and audio | No user-data transfer implied |
| FGS declaration | Existing `specialUse` core functionality and interruption/deferral rationale | [Approved draft](D128_SPECIALUSE_PLAY_CONSOLE_DECLARATION_AND_RATIONALE_2026_10_30.md); Google Play **still requires a reviewer-accessible video and form submission** |

## Exact remaining gates (do not reopen completed work)

1. **Already done:** publish and link privacy content inside app; keep using the existing URL. Already done: artwork archival. These are **not** new development tasks.
2. **Only before Play submission:** inspect local **release AAB merged AndroidManifest** and **resolved release dependencies**; compare to frozen D-141 source. This cannot be claimed complete from a source-only audit and does not authorize a release build.
3. Confirm public GitHub Pages policy page opens without login and is shown correctly in Play Console. The user's reported in-app link existence stands; don't ask to install a new build to add the link.
4. Prepare a real **FGS specialUse demonstration video** from existing app behavior, plus submit truthful FGS rationale in Play Console when authorized; [approved pre-draft](D128_SPECIALUSE_PLAY_CONSOLE_DECLARATION_AND_RATIONALE_2026_10_30.md).
5. After Google's developer identity/contact verification enables app creation, **enter** Data Safety and Privacy URL into a draft Play listing only with user authorization. The previously documented identity-verification state remains the latest known; do not assume it has changed.
6. No code, Android release signing, AAB, APK, user data, stable branches, Play app draft, Data Safety form or publication was modified/submitted in this audit.

## Handoff note
**Next decision point:** complete Play developer account identity verification and/or the FGS evidence clip, then source-to-release-artifact verification when a local signed release AAB is specifically authorized. No need to recreate the privacy link, design, images or to rerun already accepted Stable12 debug QA.
