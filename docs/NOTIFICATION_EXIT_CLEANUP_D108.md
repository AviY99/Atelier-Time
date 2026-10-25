# D-108 — Close Class notification when Android closes Atelier Time

> **D-108 KOTLIN COMPILER ERROR ROOT CAUSE FOUND AND FIXED IN SOURCE (2026-10-25):** User's `grep` against `~/atelier-d108-build.log` reports `ClassNotificationTaskService.kt:35:17 'val' cannot be reassigned`. Readback of exact source confirms `onStartCommand(intent: Intent?, flags: Int, startId: Int)` defines immutable `flags` parameter, shadowing `Intent.flags` inside `Intent(...).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP }`. In development branch, changed ONLY that line to `addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)` in commit [`02beb0af46719301cccb72ad4698a337ab416d73`](https://github.com/AviY99/Atelier-Time/commit/02beb0af46719301cccb72ad4698a337ab416d73). **Source fix committed/verified; the D-108 compile has NOT yet been rerun, so build+APK install+Back/Recents notification cleanup QA remain OPEN.** Reuse existing phone `git pull --ff-only && gradle :app:testDebugUnitTest :app:assembleDebug` CoGo AAPT2 command, then update-install without clearing Classes. D-108 version label still D-108, code4; Stable 3 frozen and D-107 previously installed.

> **D-108 REAL ON-PHONE BUILD FAILURE (2026-10-25, USER SCREENSHOT):** Code on the Go Android Gradle stopped at `:app:compileDebugKotlin` with `Compilation error. See log for more details`; **BUILD FAILED in 25s**, 31 actionable tasks (6 executed, 25 up-to-date). The screenshot shows only Gradle's generic failure summary, **not the actual Kotlin compiler diagnostic** (`e: ... .kt:<line>`). The failed command included `tee "$HOME/atelier-d108-build.log"`; next diagnostic is to extract exact lines via `grep -n -E '(^e:|error:|Unresolved reference|Type mismatch|Cannot access)' "$HOME/atelier-d108-build.log" | head -50` **without rebuilding**. Do not guess a Kotlin fix or claim compilation PASS. D-108 APK NOT generated/copied, update-install and new notification lifecycle QA NOT attempted; D-107 user-tested runtime remains installed. Back and Recents swipe stale-notification defects still OPEN, Home/other-app background operation previously PASS. Stable 3 checkpoint unchanged. When exact compiler error is provided, fix only offending lines, then rerun on-device test/assemble.

**Date:** 2026-10-25. **Status:** DEVELOPMENT SOURCE COMMITTED; no D-108 Gradle/JVM/device build, APK update-install or real-device behavior acceptance yet. Do not report a fix as verified until the Samsung phone test. User-approved Stable 3 is immutable.

## Reproduction from user's real-phone D-107 test

| Android navigation | Observed D-107 effect | Required D-108 result |
|---|---|---|
| Home / square button | Class keeps running in background; status notification updates correctly | **Preserve** |
| Back arrow | App closes; notification remains frozen | **Remove notification on real Activity finish** |
| Three-bar Recents task overview | Switching to overview/another app keeps Class and notification active | **Preserve** |
| Swipe Atelier Time up out of Recents | App/task closes; notification remains frozen | **Remove notification on task removal** |
| Existing Timer Exit or natural Class completion | Already has notification cleanup | **Preserve**, no duplicate |
| Rotation/configuration change | Not a user exit | **Do not treat as exit** |
| Notification permission denied | Timer/audio unaffected | **Preserve** |

The user explicitly distinguishes exiting the app from merely sending it to background. Do NOT hide the notification on generic `onPause` or `onStop`, and do NOT reopen the approved design, music library or phase/time engine.

## Root cause and implementation

D-107 used `NotificationManager.notify()` from Compose and cancelled mainly from `DisposableEffect.onDispose` or explicit Timer Exit. Android may terminate or remove an Activity/task without delivering those in-process callbacks. Because notifications belong to the OS, the last posted notification survives with frozen status until timeout.

D-108 adds complementary OS task-lifecycle cleanup:

1. **Back/Activity finish** — `MainActivity.onStop` and `onDestroy` cancel the notification and service only when `isFinishing && !isChangingConfigurations`. Home, another app, Recents overview and rotation do not match.
2. **Swipe task away** — non-exported `ClassNotificationTaskService : Service`, declared `stopWithTask=false`, implements Android `Service.onTaskRemoved(rootIntent)`. It marks the task as closed, removes foreground notification ID 107 and stops itself, even if Compose cleanup isn't called.
3. The existing notification controller starts this **notification-lifecycle-only foreground service** when a permitted active Class first publishes status, then continues publishing the *same notification ID* with current Class/phase/remaining time. The service does **not** create/restore timers or play cues. The regular Compose timer still owns schedules, live time, audio and UI. User still expects removing the task to close the app, **not** background-resume from a removed task.
4. Android 14+ requires a valid `foregroundServiceType` and corresponding permissions; this explicitly uses `specialUse` for the ongoing user-initiated Class status, with subtype property in the manifest and permissions `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`. This subtype **requires separate Play Console policy review before any store publication**; treat that approval as OPEN. The optional Android 13+ `POST_NOTIFICATIONS` grant is still requested only on actual Run/Start.
5. The controller's `clear()` cancels posted status and stops the service; a fresh new Activity launch resets task-closed guard and removes a stale notification from any previous task. Service is `START_NOT_STICKY` and cannot autonomously resurrect a Class. Unexpected service restrictions are caught so status infrastructure does not abort an ongoing Class.
6. Version for NEXT APK is actual **D-108**: `versionName = "D-108"`, Android monotonic `versionCode = 4`; user-approved D-107 APK and Stable 3 D-102 remain untouched.

Android docs: [Service.onTaskRemoved](https://developer.android.com/reference/android/app/Service#onTaskRemoved(android.content.Intent)), [Activity finishing](https://developer.android.com/reference/android/app/Activity#isFinishing()), [foreground service requirements and specialUse](https://developer.android.com/develop/background-work/services/fgs/service-types).

## Phone test gate (OPEN; no false PASS)

After on-phone Gradle succeeds, update-install the same-package D-108 APK **without uninstalling or clearing stored Classes**. Verify:

- **E01** About shows exactly `Version D-108`; previously saved Classes and Sound choices remain.
- **E02** Start a Class, grant notification access if prompted. Home/square: status remains live and timer sounds still work.
- **E03** Back arrow with Class active: app closes; notification disappears (not a frozen item).
- **E04** New Class → Recents overview: status still updates, app still resumes normally from overview.
- **E05** Swipe Atelier Time away from Recents: notification disappears immediately or promptly when task-removal callback runs; reopening app does not show a stale notification.
- **E06** Pause/Resume/Next keep a single updating notification; Timer Exit and natural end remove it.
- **E07** Lock screen while Class active: existing background audio/clock behavior and status remain.
- **E08** System rotation/locale configuration change is not interpreted as Back (no permanent notification loss where Class state can survive); permission denied does not prevent timer/audio.
- **E09** Force Stop from Android Settings, OS killing the process without task removal, reboot/Doze and production Play review **are not proven** by this test and require separate policy/reliability work. `specialUse` Play eligibility must be reviewed before the pre-store branding gate and publishing.

**Existing foreground audio/phase transitions were already user-approved and should not be needlessly retested from scratch.**

## On-phone build (from existing `android-native` directory)

```bash
git pull --ff-only &&
set -o pipefail &&
bash "$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle" :app:testDebugUnitTest :app:assembleDebug --no-daemon --console=plain "-Pandroid.aapt2FromMavenOverride=$ANDROID_HOME/build-tools/36.0.0/aapt2" 2>&1 | tee "$HOME/atelier-d108-build.log" &&
cp app/build/outputs/apk/debug/app-debug.apk /storage/emulated/0/Download/Atelier-Time-D108.apk &&
ls -lh /storage/emulated/0/Download/Atelier-Time-D108.apk
```

No paid CI, no PR merge, no stable checkpoint rewrite. Source review/static readback alone does not prove this build or Samsung task-removal callbacks work.
