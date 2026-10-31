# Atelier Time — D-128 Google Play FGS `specialUse` declaration / product rationale (2026-10-30)

**Decision:** User explicitly chose **RETAIN EXISTING D-128 ARCHITECTURE AND FGS `specialUse`; NO APP CODE CHANGE**. Proceed with a truthful evidence-based Play Console declaration and preserve already-accepted background, screen-lock, sound and notification behavior. This supersedes earlier *recommendations to remove FGS or transfer the timer into the service*, which were not approved. The prior read-only risk audit remains useful historical evidence: [Stage 8 specialUse review](STAGE8_SPECIALUSE_FGS_POLICY_REVIEW_2026_10_30.md).

**Status:** DECLARATION DRAFT / INTERNAL RELEASE EVIDENCE PREPARATION COMPLETE; **NOT submitted or approved by Google Play**. Google Play FGS review remains an OPEN publication gate. Do not call it Play-approved, a completed Stage 9 release, or Stable 10.

## 1. Non-negotiable product rationale — WHY this is core functionality

Atelier Time's core purpose is **unattended, dependable execution of a pre-approved live-model drawing/sculpting class**. An instructor cannot continuously look at or operate a phone while teaching. A class must preserve its agreed physical end time, advance between poses/breaks, deliver A/B/C audio cues when due, and expose a reliable, up-to-date class status without the instructor reopening the screen. Locking the phone or switching to another app is an expected, user-approved mode of operation — not an accessory feature.

Consequently, a stale class-status notification, a missed transition tone, or an unexpected stop under background process pressure directly compromises the promised core experience. Reliable notification **and** protection against easily interruptible background execution are **product-critical**. The already-accepted Samsung lock/background/exit/notification/audio tests support the product's real behavior on the tested phone; do not misrepresent them as proof for every Android device/OS.

The risk inventory of *removing* FGS constitutes the counterfactual justification, not a recommendation to remove it:
- More exposure of the UI-owned countdown and audio playback to process termination while the app is backgrounded or locked.
- Loss of the existing `onTaskRemoved` service callback used to cleanly remove the notification when the user swipes the task away; alternative cleanup would be less reliable without separate redesign.
- Greater likelihood of stale/unreliably updated status versus the user's approved user-facing behavior.

Android documents that a foreground-service hosting process is treated as *visible/important*, protecting it against ordinary background-priority process eviction, **not** against every OS/user intervention: https://developer.android.com/guide/components/activities/process-lifecycle .

## 2. Exact D-128 architecture — defend ONLY what exists

- Android development branch: `android-native-kotlin-compose`; `android-native/app/src/main/AndroidManifest.xml` declares `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`, `POST_NOTIFICATIONS`, non-exported `ClassNotificationTaskService`, `foregroundServiceType="specialUse"`, and a `PROPERTY_SPECIAL_USE_FGS_SUBTYPE` describing a user-initiated class countdown/status and task dismissal.
- `ClassNotificationTaskService.kt` is in the **same default Android app process** as `MainActivity` (no `android:process` override). It is started by the running-Class status observer, calls `startForeground()`, presents an ongoing notification and handles `onTaskRemoved`/destruction/explicit dismissal; it does **not** perform countdown calculations, A/B/C playback, timer controls or recovery.
- `SessionEditorScreen.kt` owns the live `MonotonicSessionTimer`, `ClassClockTracker`, 200-ms ticker, phase/deadline changes, sound triggering and diagnostic callbacks. `MelodyAudio.kt` plays the cue audio. `ActiveClassStatusNotification.kt` receives live timer status, updates the persistent notification and requests the FGS when permitted.
- Foreground-process priority can **indirectly support the timer and sounds in the same process** even though these routines are not executed by the Service. We do **not** claim the Service itself owns the timer, plays audio, delivers exact alarms, guarantees survival, automatically recovers killed tasks, or prevents Android FGS Task Manager stop / force-stop.
- The FGS is **optional/conditional in the current code**: with `POST_NOTIFICATIONS` denied/notifications disabled, the timer and sounds still work without launching this status service; exceptions during FGS startup are caught and the timer is not blocked. Thus do **not** claim every class runs protected by FGS, or that denying notifications disables the core countdown. The declaration is for the foreground operation actually performed **when enabled**.
- User initiation: the Class user explicitly approves the end time and starts the Class; service starts for the active-class status (RUNNING/PAUSED/STOPPED updates). User termination: Exit / Back / Recents task dismissal / natural finish cleanup in supported flows. Never imply FGS service runs permanently without active Class.
- D-128 `versionCode=22`, `targetSdk=36`, locally built and user-installed in place on Samsung. User confirmed existing Saved Classes/Sounds and normal UI. Stable 9 D-126 is still the last **explicitly frozen stable** reference. No new D-128 automated test run or proven Android 16 device OS version.

## 3. Official policy criteria / evidence matrix

Google Play FGS policy: https://support.google.com/googleplay/android-developer/answer/18258653 (Permissions for Foreground Services section).
Android FGS types: https://developer.android.com/develop/background-work/services/fgs/service-types .
Play Console declaration guidance: https://support.google.com/googleplay/android-developer/answer/13392821 .

| Criterion | Evidence / truthful claim | Remaining limitation |
|---|---|---|
| Benefit to core function | Live unattended teaching requires timely phase cues and a persistent, accurate class-status notification | Benefits of process importance are indirect to UI-owned clock/audio |
| User initiated or perceptible | User starts an approved Class; live status notification is user-visible and time-sensitive | Permission denied: no active FGS |
| User stoppable | Exit, Back and Recents/task removal cleanup; class finish dismisses | User/system may also stop the app through OS controls |
| Cannot safely defer/interupt | Without FGS the process is less protected while backgrounded, risking missed cue dispatch and stale/missing live status; task-removal cleanup loses its service callback | **Play reviewer acceptance of necessity remains unproven; no guarantee of 100% uninterrupted execution** |
| Only necessary duration | Status/FGS corresponds to a user-started active Class; shuts down upon finish/exit/task close | Confirm behavior only through supported, already tested flows; not universal OS certification |
| Correct type | `specialUse` covers live, user-initiated class status/process protection not accurately characterized as continuous media playback, location, data sync or shortService | Play requires reviewing the use case and subtype declaration |

### Honest response to a potential Play reviewer objection

**Question:** "Why does `specialUse` run if the countdown and audio are in an Activity/Compose component rather than the Service?"

**Answer:** The foreground service is intentionally co-located with the timer/audio in the default app process. It maintains the active-Class status notification and receives task-dismissal callbacks; foreground-service process importance materially supports the continued execution of the in-process timer/audio while the instructor locks the device or switches away. The user-visible notification must reflect a live, instructor-initiated class and must not become stale when the app is dismissed. This does not claim that the service itself computes timing, nor that FGS makes the system immune to interruption.

## 4. Play Console — proposed copy/paste ENGLISH answers (DRAFT, not submitted)

### A. Feature and why foreground execution is necessary

> Atelier Time manages instructor-initiated drawing and sculpting classes with timed posing sessions, breaks, audible transition cues and a fixed class completion time. Its core use case is hands-off class operation while the instructor teaches, with the phone locked or another app in use. During an active class, a specialUse foreground service displays an ongoing, time-sensitive class-status notification and handles status removal when the app task is dismissed. The foreground service runs in the same Android process as the app's timer and audio components; foreground process importance supports reliable class continuity and timely status while backgrounded. The service itself does not calculate the timer or play audio. The user starts the class and can end it through the app.

### B. Consequence if deferred (does not start promptly)

> If the active-class foreground service is delayed, its ongoing status and task-lifecycle handling are not available promptly. The timer and cue playback then run in a less protected background app process, increasing the risk of an interrupted class or missed audible transitions, and the instructor may lack an accurate persistent status while teaching without watching the screen.

### C. Consequence if interrupted or stopped by the system

> Interruption removes the process-importance and active-notification protections provided by the foreground service. Under background resource pressure the app process may then be terminated, interrupting in-process class timing/audio, or the user may receive missing or stale class-status information. We do not claim the foreground service prevents all system- or user-initiated stops, nor that the countdown or audio runs inside the service.

### D. Intended duration and control

> The service is associated with a class explicitly started by the user, including its active and paused status, and ends when the class completes or is exited or dismissed using supported controls. A notification-permission refusal does not prevent timer and sound cues; the foreground class-status feature is conditional on notifications being available.

### E. Suggested use-case label

> User-initiated live class countdown status, reliable background continuity and accurate task-dismissal notification cleanup for an unattended instructional timer.

**Manifest subtype consistency:** existing `android:value` in the D-128 manifest mentions the user-initiated countdown/status and task removal. The declaration above clarifies that the service **supports** in-process countdown continuity; it does not claim to compute the countdown. No edit to manifest is authorized. If Play requires different subtype wording, report the proposed minimal manifest text and obtain separate approval **before any code change**.

## 5. Video evidence package — NOT YET RECORDED

Play Console requires a **link to a demonstration video** for each declared type. Record on a phone only when preparing store submission, without needing a new build:
1. Begin on Saved Classes, show a pre-approved class selected.
2. Display/confirm the live end-time choice and explicitly press Run.
3. Show Class running, class-status notification, and current session/break status.
4. Press Home / switch to another app; show Android notification and advancing countdown without keeping the Atelier screen open.
5. Lock/unlock the phone (if screen recording cannot capture lock, use separate camera footage), and show live status/cues during the running Class.
6. Return to Atelier, display the current class time/phase, demonstrate the existing user-approved behavior; do not alter session data.
7. Demonstrate Exit/Back or Recents swipe-away and verify that a stale class notification is not left behind.
8. Optionally show notification-permission consent **only if it naturally occurs**; do not reset permissions, uninstall or clear data to recreate a prompt.

Keep test Class short for filming; mask any saved personal names, notification previews, other apps or device information. Do not claim a video exists yet. The published link must be accessible to Google's reviewer; no video URL has been provided or fabricated.

## 6. Required release gates and scope boundaries

- **Completed here:** product rationale, exact technical explanation, policy citation, copy/paste declaration draft, refusal/interruption impact, candidate video storyboard and policy uncertainty documented.
- **NOT completed:** reviewer-accessible demonstration video/link; actual Play Console FGS submission or decision; signed release AAB/release merged-manifest validation; Android-16-device OS-specific QA; hosted public privacy policy and in-app access; final Data safety and SDK inventory; store graphic assets. None should be claimed done.
- **Decision retained:** no code/manifest/service/audio/permission/UI change; no APK build/install/test rerun, no uninstall/clear data, no cloud GitHub Actions, no Stable 10 designation. Any subsequent source change or Play submission requires an explicit next gate.

**Canonical handoff:** [Current project status](CURRENT_STATUS_AND_HANDOFF_STABLE9_2026_10_28.md) · [Stage 8 release audit](D128_STAGE8_ANDROID_PLAY_POLICY_AUDIT_2026_10_29.md) · [Documentation branch policy](DOCUMENTATION_INDEX_AND_BRANCH_POLICY.md).
