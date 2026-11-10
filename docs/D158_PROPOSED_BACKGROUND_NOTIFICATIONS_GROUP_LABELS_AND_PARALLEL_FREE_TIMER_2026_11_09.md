> **FINAL STATUS 2026-11-10: STABLE18 / D-158 / CODE52 ACCEPTED.** After the documented Samsung **326/326 JVM PASS** and APK artwork checks, the user installed the D158 `.dev` candidate and explicitly confirmed everything works, designating a new stable version for background-notification bug fixes. Frozen branch [`stable/android-d158-qa18-background-notifications-user-accepted`](https://github.com/AviY99/Atelier-Time/tree/stable/android-d158-qa18-background-notifications-user-accepted) at `7994dcfc2d077e68d72800400fa2086dacdc4132`. [Stable18 acceptance](STABLE18_D158_BACKGROUND_NOTIFICATIONS_USER_ACCEPTANCE_2026_11_10.md) · [current handoff](HANDOFF_CURRENT_STABLE18_TO_GOOGLE_PLAY_2026_11_10.md). Play Internal Testing remains D157/Code51; no Code52 Play AAB or rollout. This newer statement supersedes QA-pending sections below, retained for historical evidence.

# Atelier Time — proposed background notification fixes after first Google Play Internal Test

**Reported:** 2026-11-09, Asia/Jerusalem.  
**Classification (updated 2026-11-10): USER-APPROVED D-158 / Code52 SOURCE IMPLEMENTED ON ANDROID DEV; LOCAL SAMSUNG BUILD AND DEVICE QA PENDING.** No Play update, Stable18 promotion, paid CI, installed APK or signed AAB has occurred. The original pre-approval statements below are historical.
**Baseline:** Stable 17 / D-157 / Android versionCode 51 — frozen source branch `stable/android-d157-qa17-user-accepted`, SHA `f6866dca809f6214c6d1f88c1adf8f6592163ada`.

## Confirmed user acceptance of the first Play Internal Testing build

The user installed Atelier Time **through Google Play** (Internal Testing / invite-only) on their own Samsung, replacing the prior sideloaded Debug installation at their discretion. They explicitly confirmed: **"הכל עובד כנדרש !! הגרסה עובדת בדיוק כמו גרסת הפיתוח."** — all required operation works; Play-installed version behaves like the development version. This is **user functional acceptance of the Play-delivered D157/Code51 build**, not device instrumentation PASS or public Production publication. The earlier Play `Item not found` temporary condition resolved; the listing became available to the enrolled tester. Other invited tester installation is not yet confirmed.

The user then reported **two pre-existing bugs**, present in the development version as well. This acceptance does not imply those bugs were fixed.

## A — Background Class notification retains obsolete sequential numbering

**Observed:** The visible Class Timer now shows Group labels and within-group session counters, but the Android background/notification view still shows the older sequential Session number.

**Desired:** Background/notification labels follow the *same presentation convention* as the actual Class Timer. For opening grouped poses show e.g. `Group 1 · Session 2/4`, for group breaks `Group 1 · Break`, and for later regular sessions use the currently accepted numbering (not the underlying old cumulative poseNumber); special/regular breaks and summary retain the correct on-screen naming. Preserve remaining-phase time, total Class time and running/paused distinction.

**Source-confirmed gap / likely direct cause:**
- `android-native/app/src/main/java/com/ateliertime/android/SessionTimerScreen.kt` already renders `PhaseDisplayNames.title(phase, session.rules)`.
- `android-native/app/src/main/java/com/ateliertime/domain/PhaseDisplayNames.kt` contains group-aware display names without changing stored timer identifiers.
- But `domain/TimerNotificationStatus.kt`, `TimerNotificationFormatter.forActiveClass`, currently hardcodes `"Session " + poseNumber`, and uses `"Opening break"` without group numbering. Its interface lacks the session's display rules.
- `android/ActiveClassStatusNotification.kt` calls that formatter; `SessionEditorScreen.kt` supplies the running snapshot and class name.

**Implemented in D158 DEV source, awaiting Samsung build and device acceptance:** Reuse the established group-aware display-name formatter using the active saved session rules, passed safely into the notification formatting adapter; avoid changing the scheduler's underlying phase identifiers, persisted format, replay behavior or duration calculations. Check screen and background text agree across all pose/break cases.

## B — Free Timer lacks its own background notification

**Observed:** A running Free Timer does not appear in Android's background notification area. User explicitly warns that Free and Class timers **may run at the same time**, and in such cases **both require display**.

**Desired:** A separately identifiable Free Timer notification while Free is Running or Paused with remaining time and clear `Free Timer`/state labeling. If Class Timer is also active, **two independently visible notifications** must coexist. Each must update, pause/resume and clear independently: stopping/finishing Free must not cancel Class, and stopping/finishing Class must not cancel Free. Preserve actual Free and Class timing and sound behavior (including parallel sounds), session persistence and existing lifecycle/exit rules.

**Source-confirmed gap:**
- `SessionEditorScreen.kt` already owns and ticks `FreeCountdownTimer` and `freeTimerView` in the shared 200ms tick loop (separate from the Class timer), but only calls `classStatusNotification.update(...)`.
- `ActiveClassStatusNotification.kt` and `ClassNotificationTaskService.kt` handle **Class only**, using channel `atelier_active_class_v1` and notification ID `107`.
- `FreeCountdownSection.kt` renders Free status only in the app UI.
- No Free notification adapter or explicitly distinct notification ID was found among the current native source filenames. This describes the current source, not a claim about Android OS behavior on every device.

**Approved implementation, requiring Samsung QA and Play policy verification:** Add Free notification state formatting and lifecycle, assign unique Free notification/channel IDs so notification updates/cancels never replace Class ID 107; respect Android 13+ `POST_NOTIFICATIONS`, system notification settings, Android notification UI, task removal and app exit. Determine whether a second foreground service is necessary or whether an existing foreground process can legitimately host another notification: this must be evaluated against Android 14+ `specialUse` Foreground Service policy / Play Console declarations, rather than adding a service or claiming unrestricted background execution. Keep the primary timer clocks in existing owners, not recreated inside notifications; a notification must mirror actual monotonic time, not independently drift.

## Regression and acceptance matrix for a subsequent development candidate

1. **Class only:** grouped 1/N and group break names agree on-screen and in shade/lock-screen; after group closing, regular Session numbering matches; regular/special breaks and summary labels correct. Include multiple opening groups and display fallbacks.
2. **Free only:** Running countdown shown in notification, Paused state does not continue counting, Resume works, Finish/Exit removes the correct Free item; opening the app keeps the timer state.
3. **Concurrent Class + Free:** Both active -> two distinct entries; pause/exit one -> other entry remains with correct time; class completes first and free continues, and vice versa; no cue interference or duplicate status IDs.
4. **Background and lifecycle:** Home, locked screen, return to app, notification permission denied/granted, multitasking/split-screen, task swipe/exit and resumed state. Respect existing user-approved behavior and Android platform limitations. Notifications should not silently turn sound on/off or change Media/Alarm stream.
5. **No regression:** Saved Classes, edited schedule signatures, Repeat Last / Restart / Next, special-break planning, free completion audio, approved splash, local user data, diagnostic run history and stable API/permissions.
6. **Quality gates:** Tests for display-formatter parity, Free Running/Paused/Finished snapshots and concurrent cancellation; local Samsung JVM suite + AndroidTest **execution when feasible** (don't call compilation execution); actual on-device UI/notification verification by user; original artwork SHA-in-APK/AAB; separately user-accepted new stable checkpoint; then a higher versionCode and separately signed Google Play Internal Testing update only if approved. Don't mutate frozen Stable 17, nor reuse versionCode 51 for a new Play upload.

## Explicit user approval received — D158 source implemented, verification pending

**User approval on 2026-11-10:** "כן" to implementing **both fixes together** as **D-158 / versionCode 52**. Implementation committed to `android-native-kotlin-compose`, not to the frozen Stable17 branch or `main` app source. Source changes:

- `domain/TimerNotificationStatus.kt`: Class notification accepts actual saved SessionRules and shares the existing `PhaseDisplayNames.title` formatter; Free gets its own Running/Paused status snapshots; foreground selection and unique IDs live in `ActiveTimerNotificationSlots`, **Class 107** and **Free 109**.
- `android/SessionEditorScreen.kt`: passes saved Class rules to notification, ticks the Free snapshot alongside Class on the existing clock loop, explicitly starts/pauses/exits Free notifications, and requests optional Android notification permission when Free starts.
- `android/ActiveFreeStatusNotification.kt` (new): separate low-importance Free channel, notification ID109, no duplicate sound, countdown/paused text and status, own cancellation.
- `android/ClassNotificationTaskService.kt`: reuses **one** existing FGS for user-started Class and/or Free, with lifecycle actions for each timer. During overlap, Class remains foreground ID107 with Free independently posted ID109; on Class completion Free can become foreground without resetting the Free clock. Exit/Recents task removal clears both. No second service is introduced. Android specialUse declarations need review before eventual Play upload of Code52.
- `AndroidManifest.xml`: FGS specialUse subtype description acknowledges both user-started timers; same permissions and service as before.
- `app/build.gradle.kts`: app D-158 / Code52; **debug-only** `applicationIdSuffix=".dev"` and label "Atelier Time DEV", allowing separate testing without uninstalling or replacing the installed Play-signed Stable17 package `com.ateliertime.android`. Release applicationId remains unchanged.
- `D158BackgroundNotificationsTest.kt` (new): 8 JVM cases for grouped/regular/break/special/summary parity, paused and Running Free, ID coexistence and transitions. Existing 318 JVM tests are not claimed re-executed; minimum planned QA total 326.
- Phone-only guarded script: `android-native/tools/build_d158_group_and_parallel_notifications_on_phone.sh`. It checks exact D158 app tree, original artwork and Code52, runs the complete JVM suite and compiles AndroidTest source, builds a **distinct .dev debug APK**, verifies approved artwork inside the APK and saves it to Downloads. **The assistant cannot run the user's Samsung local build; no build pass or AndroidTest execution is claimed.**

**Safe local command, only when ready to test on Samsung Code on the Go (no paid cloud build):**

```bash
cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native && git pull --ff-only && bash -n tools/build_d158_group_and_parallel_notifications_on_phone.sh && bash tools/build_d158_group_and_parallel_notifications_on_phone.sh
```

**Next gates:** Get actual Samsung build/JVM screenshot, then separately approve installing the new debug **DEV** app, verify Class-only, Free-only, simultaneous timers and independent cancel/lock screen behavior. Only after user-confirmed runtime tests consider a new Stable18 checkpoint and separately signed Code52 release AAB for a Play Internal Testing update. No production/public release, data wipe, signed upload, or unrelated features authorized by this approval. Frozen Stable17 remains available unchanged.

**Working documents:** [Current Stable17 → Play handoff](HANDOFF_CURRENT_STABLE17_TO_GOOGLE_PLAY_2026_11_07.md) · [Frozen Stable17 acceptance](STABLE17_D157_VOLUME_AND_SOUND_USER_ACCEPTANCE_2026_11_07.md).

## Samsung local QA evidence — user screenshot, 2026-11-10 20:44 Israel time

**D-158 / Code52 Samsung compile and JVM PASS (not physical notification acceptance yet).** User ran `tools/build_d158_group_and_parallel_notifications_on_phone.sh` after a Git pull on the `android-native-kotlin-compose` branch. Terminal explicitly reported:

```text
D158 ALL JVM ACTUAL: tests=326 failures=0 errors=0 skipped=0
D158 BACKGROUND REGRESSIONS: {'tests': 8, 'failures': 0, 'errors': 0, 'skipped': 0}
D158 FULL JVM PASS
D158 APK ARTWORK INCLUDED PASS
D158 JVM TESTS AND BUILD SUCCESSFUL: /storage/emulated/0/Download/Atelier-Time-D158-Code52-DEV-QA.apk
AndroidTest COMPILED, NOT EXECUTED; no APK install, Play upload or cloud CI.
Debug package com.ateliertime.android.dev preserves installed Play data.
```

Private local log: `/storage/emulated/0/Download/Atelier-Time-D158-Code52-QA-20261110-204211.log`. Terminal printed a SHA256 for the APK, but assistant has not independently read that artifact or its bytes. Successful local build is supported by the screenshot; notification and lifecycle behavior **remain to be confirmed by on-device user QA**. **Do not mark Stable18 or Play Code52 released.**

**Next device QA:** Install `Atelier-Time-D158-Code52-DEV-QA.apk` from Downloads without uninstalling the Play app. Confirm separate **Atelier Time DEV** icon, correct Group/session labels in the Class notification, Free Running and Paused notification, **two simultaneous Class and Free notifications**, independent stopping/completion of either, Home/lock screen/return, sound cues. Android notification permission must be granted if user wants visible status; denied permission must not stop either timer. Any real-device errors require source correction and rerunning the suite before a new candidate.
