# Atelier Time — D-107 / ongoing Android Class status notification

**State (2026-10-25): Native Android source committed on `android-native-kotlin-compose`; NO D-107 phone Gradle compile, JVM test execution, debug APK install or notification runtime approval has been observed yet.**

## User request, evidence and agreed scope

The user already confirmed that during a Class the **timer and audio work** after switching to other apps, after Android Back, and while the device is locked. They observed that Android **Recents swipe-out closes the app**. They also observed there was **no ongoing status in the notification shade**, clarified this exact meaning, and then explicitly asked **"הוסף אותה" — add the ongoing notification**.

Implement a **quiet ongoing Class notification**, visible in Android's notification shade during a running or paused/stopped Class. Show:

- Title: **Atelier Time · Running / Paused / Stopped**.
- Current **Session / Break / Class summary** and remaining time.
- Expanded detail with the selected Class name and full physical Class remaining time.
- **Native countdown chronometer** for the running phase only; frozen value shown during Pause/Stop.
- Tapping the notification opens/reuses the main Android Activity.
- Dismiss it on **Exit**, normal Class completion, manual Next-to-finish or root UI disposal; no permanent notification with no active Class.
- **Silent LOW-priority Android channel**; status refresh at most once a second (existing Timer ticks every 200ms), no audio/vibration additions, no new UI tabs or changes to A/B/C melodies.

**Android 13+**: `POST_NOTIFICATIONS` declared in manifest and requested during the explicit Run/Start action. If declined or disabled in Settings, app timing/audio **must keep functioning**; only this new status notification is unavailable. Channel uses private lock-screen visibility. A small functional monochrome clock status icon was added; it is **not** the final brand logo/launcher icon (that remains the mandatory pre-store user-approval gate).

**Important scope boundary:** This lightweight `NotificationManager` observer **does not implement an Android Foreground Service** and **does not make a Recents swipe/force-stop/service process removal safe**. Android may stop/kilI the running process and notifications may be disabled by user/system. The notification includes a system timeout aligned to the physical Class deadline to reduce stale notifications after unexpected loss of the UI process. Developing a separately justified foreground service and OS-type/Play-policy review is another change and **has not been authorized or implemented in D-107**. User's previously observed locked/background success is retained, but not interpreted as guaranteed Doze/process-kill robustness.

## Source changes

- `domain/TimerNotificationStatus.kt`: pure Kotlin formatting of running/paused/stopped phase, Class remaining, and precise live phase countdown.
- `app/src/test/java/com/ateliertime/domain/TimerNotificationStatusTest.kt`: 5 new JVM test source methods for running status/precision, paused/stopped, phase naming, longer durations, absence on READY/FINISHED.
- `android/ActiveClassStatusNotification.kt`: Android 26+ low importance channel, optional notification permission check, native Builder BigTextStyle and countdown, fixed notification ID, return-to-app PendingIntent, max once-per-second refresh, cancellation/timeout.
- `res/drawable/ic_atelier_class_status.xml`: functional monochrome clock, NOT branding artwork.
- `android/SessionEditorScreen.kt`: ask optional notification permission on actual Run/Start; update from existing live Timer tick; clear at Exit/finish/disposal; no class/timer/audio state ownership moved.
- `AndroidManifest.xml`: optional POST_NOTIFICATIONS permission, no FGS claim/permission/type added.
- `app/build.gradle.kts`: next user-requested actual D-version label `versionName = "D-107"` and Android monotonic `versionCode = 3`. The previously built and copied D-104 APK is unchanged.

## Phone build and acceptance gate — NOT EXECUTED

Use existing Code on the Go local directory `android-native`:

```bash
git pull --ff-only &&
set -o pipefail &&
bash "$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle" :app:testDebugUnitTest :app:assembleDebug --no-daemon --console=plain "-Pandroid.aapt2FromMavenOverride=$ANDROID_HOME/build-tools/36.0.0/aapt2" 2>&1 | tee "$HOME/atelier-d107-build.log" &&
cp app/build/outputs/apk/debug/app-debug.apk /storage/emulated/0/Download/Atelier-Time-D107.apk &&
ls -lh /storage/emulated/0/Download/Atelier-Time-D107.apk
```

Do not repeat known stable functionality tests unnecessarily. Only after `BUILD SUCCESSFUL`, update-install the debug APK from Downloads **without uninstall/data clearing**. Then verify:

| ID | Device acceptance |
|---|---|
| N01 | About says **Version D-107** after installing the new APK; saved Classes and Sound choices survive |
| N02 | Android 13+ optionally requests notification access when first Run/Start is initiated, not on launch; grant it for visible testing |
| N03 | Pull down notification shade while a Class runs: one quiet ongoing item, correct Class name, current Session/Break, remaining Class time and live phase countdown |
| N04 | Switch to another app, press Back, lock/unlock phone: notification remains current while the existing background timer remains functional |
| N05 | Pause: show Paused and stop the live phase countdown. Resume: show Running and count down again |
| N06 | Next/Restart: update the phase name/remaining time and avoid duplicate notifications |
| N07 | Exit or natural/Next-finish: remove the notification, not leave a stale ongoing Class; tapping active item returns to app |
| N08 | Disable/deny notification permission: **NO Timer or A/B/C audio regression**; just no visible notification. Reenable notifications and check next active class |
| N09 | No notification sound/vibration, repeated popups, excessive flicker, or changed Studio Linen/Night Blue/Editor/Sound visuals |
| N10 | **Distinct later reliability work:** Recents-swipe process kill, Doze, reboot, OS memory reclaim and proper service policy are NOT considered passed or fixed by N01–N09 |

All tests remain **PENDING** in the current conversation. `stable/android-d102` (user-accepted Stable 3) remains immutable; PR #3 remains Draft/unmerged; no paid CI.
