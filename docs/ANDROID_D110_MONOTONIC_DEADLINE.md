# Atelier Time — D-110 monotonic Class deadline correction

## Latest real CoGo phone evidence — D-110 build PASS

- **D-110 phone build verified from user screenshot (2026-10-25, ~16:10 local):** CoGo Gradle executed `:app:assembleDebug` and `:app:testDebugUnitTest`; terminal displayed **BUILD SUCCESSFUL in 47s**, **42 actionable tasks: 14 executed, 28 up-to-date**. The subsequent `ls -lh` confirmed a roughly **12MB** APK at `/storage/emulated/0/Download/Atelier-Time-D110.apk`. This is **REAL PHONE BUILD + JVM UNIT TEST TASK PASS**, not Android connected instrumentation. **Install/update, About version readback, preserved data and focused D-110 UI/clock QA NOT YET CONFIRMED.** No evidence of Play/signed release. D-108 Stable 4 remains frozen.
- Installation gate: open the downloaded APK through Android Downloads / My Files and **update** the installed application, never uninstall or clear data. Verify About shows `D-110`; verify existing Classes and selected sounds persist; then do focused monotonic Class clock behavior check only. Avoid changing phone system time during an actual user Class. Other previously approved cues/UI/background/notification behavior does not require repeating.
- Earlier below section labeled `Build/acceptance gate — NOT YET PERFORMED` was written **before** this actual build and is retained as historical instructions; the Gradle build portion is now complete, while update-install/acceptance remains open.

**Date:** 2026-10-25. **Latest status:** D-110 **PHONE GRADLE BUILD AND JVM UNIT TEST TASK PASS** (user screenshot; 47s, 42 tasks). **APK INSTALL / ON-PHONE ACCEPTANCE NOT YET VERIFIED**.

## Why

QA-R03 showed a ±120-second discrepancy in Class elapsed/remaining after a synthetic adjustment to the device wall clock. A source audit also found the root Compose loop checked **`System.currentTimeMillis() >= classEndEpochMs`** to end the Class. A forward correction could therefore finish early, and a backward correction could delay forced completion. This was a source-supported theoretical edge, not observed in user-approved normal phone behavior.

The accepted **physical Class deadline** must be monotonic, yet the displayed Clock, Class start/end and phase start/end labels are wall-clock values. The virtual phase cursor can pause or jump with user actions; therefore using `TimerView.totalRemainingSeconds` for physical Class time would silently violate Pause/Next semantics.

## Changes (branch `android-native-kotlin-compose` ONLY)

1. `android-native/app/src/main/java/com/ateliertime/domain/ClassClockTracker.kt`
   - Accept optional injected monotonic clock provider, leaving pre-existing injected-epoch JVM test calls compatible.
   - When Android provides the monotonic clock, capture a physical Class start and immutable physical deadline at `newClass`; retain deadline across `restartClass`.
   - Use that monotonic reference for Class physical elapsed and remaining values, including while paused and following Next.
   - Provide `hasReachedClassDeadline(nowEpochMs)`; Android uses the monotonic deadline, with legacy wall-clock fallback for callers without a provider.
   - **Preserve existing wall-clock timestamp labels, palette, Timer controls, cues, and approved UI design.** An actual system clock correction can still make *clock-of-day labels* appear historical; determining desired relabeling is separate, not claimed fixed here.
2. `android-native/app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt`
   - Instantiate `ClassClockTracker { SystemClock.elapsedRealtime() }` (same monotonic source already used by the timer).
   - Decide physical deadline completion using `classClockTracker.hasReachedClassDeadline(nowEpochMs)`, no longer comparing system wall time to the original epoch deadline.
   - Leave user action and sound dispatch logic unchanged.
3. `android-native/app/src/test/java/com/ateliertime/domain/ClassClockTrackerTest.kt`
   - Add **3 new Kotlin JVM regression test sources** covering ±120s clock adjustments, exact physical deadline, Pause/Next physical time, and Class restart retaining the original deadline.
4. `android-native/app/build.gradle.kts`
   - Prepare future next APK: `versionName = "D-110"`, `versionCode = 5` (up from D-108/code 4). About continues to obtain real PackageManager versionName.

## Actual test evidence (assistant local Kotlin/JVM)

Executed local **source-equivalent extracted D-108 ClassClockTracker timing paths with D-110 patch**, `kotlinc` + `java`. These are *not* executed Gradle `:app:testDebugUnitTest` and *not* Android instrumentation. Local harness compiles isolated timer/clock subset with stubs for unrelated features; no assertion about untouched full Gradle compile.

- Seven targeted assertions groups PASS: baseline clock, wall forward +120s not ending early, wall backward −120s not ending late, Pause physical progress, Next not advancing physical Class time, exact 180-second boundary, finished Class 0 remaining.
- Separate 180-minute simulation with wall time stepping +120s after minute 60, −120s after minute 120 and normal thereafter: **10,801 one-second checkpoints** PASS for independently calculated physical elapsed, remaining and deadline; epoch class-end label remains the stored original value.
- Real wall-clock, Android application scheduling/audio, Doze, process-death, installed D-110 behavior: **NOT RUN / OPEN**.

## Build/acceptance gate on user's phone — NOT YET PERFORMED

Existing accepted D-108 installation remains. Keep all saved Classes and Sound settings. At next user-controlled Code on the Go session, first update existing `android-native-kotlin-compose` checkout safely (no uninstall/clear data), and from `android-native/` run:

```sh
git pull --ff-only &&
set -o pipefail &&
bash "$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle" :app:testDebugUnitTest :app:assembleDebug --no-daemon --console=plain "-Pandroid.aapt2FromMavenOverride=$ANDROID_HOME/build-tools/36.0.0/aapt2" 2>&1 | tee "$HOME/atelier-d110-build.log" &&
cp app/build/outputs/apk/debug/app-debug.apk /storage/emulated/0/Download/Atelier-Time-D110.apk &&
ls -lh /storage/emulated/0/Download/Atelier-Time-D110.apk
```

**Do not claim BUILD PASS before real phone Gradle evidence.** A successful build only proves JVM task/assembly; focused device verification is a separate gate. Phone install is a normal update, not uninstall. If clock-correction is tested manually, avoid changing system time during a live class the user depends on; test only a disposable Class with saved personal settings protected.

## Regressions and scope

- Frozen rollback `stable/android-d108` is **unchanged**.
- Accepted UI/Day-Night, Sound A/B/C, normal background, Back/Recents notification cleanup, preset storage, timer Pause/Next/Restart behavior not intentionally modified.
- QA-R01 connected instrumentation remains BLOCKED by unexecutable/empty CoGo ADB path.
- QA-R04 reboot retention of Classes/Sound PASS (prior user's own phone observation).
- Real three-hour Android Class with physical audio/Doze remains OPEN.
- Mandatory user-approved logo and app launcher/store icon BEFORE Play publication remains OPEN.

**Exit condition:** Actual D-110 Gradle/Kotlin PASS from CoGo, update-install preserving user data, then narrowly scoped clock-correction/physical remaining/finish test on phone if feasible. Record evidence; no Stable 5 declaration before explicit user approval.
