# Atelier Time — Stage 8 Android / Google Play permission and policy audit

**Reviewed 2026-10-29. Scope:** read-only review of Android development source `android-native-kotlin-compose` D-127/versionCode21. **Result: current permission implementation structurally consistent; Play release NOT READY until the explicitly identified store gates are resolved.** No Android source, Gradle config, installed APK, saved data, frozen Stable 9 ref or test behavior changed. No compilation, installation, cloud builds, QA reruns or Play submission occurred.

## Source inventory and accepted behavior — do not re-test without a relevant change

- `android-native/app/src/main/AndroidManifest.xml`: exactly three direct permission declarations: `android.permission.POST_NOTIFICATIONS`, `android.permission.FOREGROUND_SERVICE`, `android.permission.FOREGROUND_SERVICE_SPECIAL_USE`. No direct declared `INTERNET`, camera, microphone, location, storage-wide, `WAKE_LOCK`, `SCHEDULE_EXACT_ALARM`, or `USE_FULL_SCREEN_INTENT` permissions. **This is a source manifest observation; a merged Release/AAB manifest still requires inspection at release packaging.**
- `SessionEditorScreen.kt`: on Class-start initiation (before the live finish-approval workflow), `requestClassStatusNotificationPermission()` uses `ActivityResultContracts.RequestPermission()` and `Manifest.permission.POST_NOTIFICATIONS` on Android 13+ when not already granted. This is the notification prompt remembered by the user from an earlier upgrade; it is an **existing approved flow**, not a new feature or a request to implement another prompt. If permission is already granted, the code does not request it again. If declined, a later attempted Class start may re-invoke the request function subject to Android dialog behavior; do not claim a guaranteed once-only prompt.
- `ActiveClassStatusNotification.kt`: quiet `IMPORTANCE_LOW` channel, no channel sound/vibration, updates only when a Class is active, declines to post when notification permission/channel disabled, never owns time or cue playback, and is cleared on completion/exit.
- `ClassNotificationTaskService.kt`: `START_NOT_STICKY`; service started after user began Class (when permission allows), `startForeground(... FOREGROUND_SERVICE_TYPE_SPECIAL_USE)` on Android 14+, non-exported in manifest; `onTaskRemoved` and MainActivity Back/close handling remove stale status. **Service is notification/task-lifecycle helper, not the clock/audio engine.** Never represent it as the engine keeping the Class running.
- `SessionEditorScreen.kt` `FLAG_KEEP_SCREEN_ON` under a user toggle during active Class or Free countdown: NO `WAKE_LOCK` permission needed. `MelodyAudio.kt` uses `AudioTrack`, `MediaPlayer`, `AudioManager.requestAudioFocus` and reads mute status without special runtime audio permissions or forcing device volume. `RingtoneManager` system picker and `ActivityResultContracts.OpenDocument` with selected URI grant for audio files avoid broad media/storage access.
- `RunDiagnosticsLibrary.kt` and private archive: reports kept in private app files; user-initiated `CreateDocument` SAF export and confirmed deletion. `android:allowBackup="false"`; no automatic cloud sync, account, Internet permission, automatic telemetry or data export found in these reviewed Android app source/manifest files. New install starts with local app data; updates preserve existing application data (already user-confirmed for D-127); no product requirement to retain data after uninstall. A final Release manifest/library inventory is still required before public Data safety declarations.
- Existing user-accepted screen lock, Home/background, Back, Recents-swiped-close, timer audio and notifications were completed in earlier releases (notably D-108 Stable 4); current D-127 6/6 + 8/8 Stage 7 instrumentation and older D-126 Stable 9 285/285 PASS are **historical separate test results**. **Do not re-run accepted functional QA as routine Stage 8 work.**

## Release-specific policy gates (not phone-feature defects)

| Item | Verified finding | Gate |
| --- | --- | --- |
| Runtime permission prompt | Existing contextual `POST_NOTIFICATIONS` request on Class-start flow; Android 13+ only; denial does not block Class timer/sounds | **NO NEW CODE NEEDED** |
| Android FGS manifest structure | `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_SPECIAL_USE`, type `specialUse`, explanatory service subtype and `android:exported="false"`; code uses matching service type | **STRUCTURALLY MATCHING**, approval is NOT guaranteed |
| FGS policy eligibility | Play policy requires user-perceptible/user-beneficial, user stoppable, *non-deferrable/non-interruptible* FGS work. Current FGS only holds status + task-removal handling, while the timer and sounds are handled elsewhere and status itself is optional. This makes a claim that FGS is indispensable **not proven**. | **OPEN — elevated Play review/rejection risk; seek policy-compliant design/justification without inventing a need** |
| Play Console FGS declaration | Explanation of exactly what the helper does, what deferral/interruption affects, accurate user-triggered demonstration video and `specialUse` subtype justification; Google reviews the submission | **PENDING until Play setup; cannot be approved from code audit** |
| Target API | `compileSdk=36`, `targetSdk=35`, `minSdk=26`. Google Play requires API **36+ for new phone app / app-update submissions from 2026-08-31**. Target 35 existing installations are not retroactively broken | **OPEN — migrate release candidate to target 36 and run focused regression gate** |
| Android 16 behavior | Target 36 changes predictive Back integration and edge-to-edge; existing `MainActivity` uses `OnBackPressedDispatcher` to call `finish()` and has an accepted task-close/notification lifecycle. Check behavior in Android 16 target-36 environment; do not change D-127/current user install to discover compliance. | **OPEN — focused compatibility validation in future separate release candidate** |
| Local data privacy | Local-only Class/Sound/Run report data in private app area and explicit SAF export; `allowBackup=false`, no network permission; update data preservation already user-confirmed. | **SOURCE REVIEW COMPLETE; packaging/dependencies recheck** |
| Privacy policy / Data safety | Play requires a public privacy-policy webpage linked in Play Console and policy text/link accessible within the app even for local-only apps; fill Data safety accurately. User data processed solely on device ordinarily is **not** reported as collected off device. | **OPEN — prepare final public policy/URL and in-app access before Play submission** |

## Suggested Play Console FGS declaration — DRAFT, NOT FOR SUBMISSION BEFORE ELIGIBILITY REVIEW

- **User-triggered flow:** When the instructor begins an Atelier Time Class and grants notifications, the app shows a low-interruption ongoing status notification of the current Class/session and time remaining; when the user ends/closes the Class or removes its task from Recents, the app removes the status.
- **What the service actually does:** A foreground notification helper mirrors the app-owned countdown and detects task removal so the notification is not left stale. It does **not** own the timekeeping or sounds, restart automatically after device reboot, upload data, or operate when no Class is active.
- **User benefit:** glanceable live status when switching applications/locking screen, and correct cleanup when the task ends.
- **Deferral/interruption:** may interrupt the *notification/cleanup* feature, but based on current implementation is **not proven** to interrupt the actual countdown or sound cue engine. **This weakens a claim under the FGS non-deferrability policy criterion**; evaluate permissible alternatives and Google Play feedback before submission, while preserving user-approved notification semantics.
- **Demonstration required by Play:** a truthful screen recording showing starting a Class, permission request when applicable, notification in background/lock, exiting via Back and Recents, disappearance of notification. No recording is claimed to exist yet.
- **Do not claim** `mediaPlayback` type merely because the app plays short cues. The foreground *service* does not play the audio.

## Focused next work; no broad re-testing

1. **No code changes now** to D-127/code21 or the approved service. Treat specialUse Play eligibility as unresolved. If a different lifecycle/API design is later necessary, propose the exact minimal code patch and targeted tests **before** touching app behavior; approval and data-preserving in-place upgrade required for phone operations.
2. For the separate release candidate (not the existing D-127 installation): `targetSdk=36` with version bump under normal versioning rules; inspect merged manifest, compile locally on Samsung only, test only Android 16 Back/Recents/notification cleanup and relevant edge-to-edge layout after this material change. Other already-passed tests do not need a blanket rerun. If no Android 16 testing host is available, record this as a release verification gap rather than asking the user to obtain an unknown device.
3. Before store publication: verify precise FGS Play eligibility / declarations, record compliant video, complete publicly hosted privacy policy and in-app notice, accurate Data safety, release signing and store assets in later stages. Nothing submitted or approved yet.

## Official sources checked 2026-10-29

- Android notification permission: https://developer.android.com/develop/ui/compose/notifications/notification-permission
- Android 14+ FGS service types / specialUse: https://developer.android.com/about/versions/14/changes/fgs-types-required
- Google Play FGS policy criteria: https://support.google.com/googleplay/android-developer/answer/16559646
- Google Play FGS declaration/video: https://support.google.com/googleplay/android-developer/answer/13392821
- Google Play target API timetable: https://support.google.com/googleplay/android-developer/answer/11926878
- Android 16 behavior changes: https://developer.android.com/about/versions/16/behavior-changes-16
- Google Play privacy policy: https://support.google.com/googleplay/android-developer/answer/10144311
- Google Play Data safety: https://support.google.com/googleplay/android-developer/answer/10787469

**Status:** STAGE 8 SOURCE/POLICY AUDIT DONE; **PLAY READINESS BLOCKED** by the explicitly listed release gates. Stable 9 frozen, D-127 remains installed, current Android code unchanged, no new user permissions.
