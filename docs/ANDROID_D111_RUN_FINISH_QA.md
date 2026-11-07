# Atelier Time — D-111: user-approved Run finish time + automated QA

**Status:** implementation and automated test sources committed to `android-native-kotlin-compose`; **phone build/install/QA of D-111 NOT YET EXECUTED**. Existing D-110 Android instrumentation **14/14 PASS** on Samsung SM-S921B (2026-10-25); that does not establish D-111 quality. Stable 4 **D-108** remains frozen.

## Approved product rule

When the user taps **Run** for a saved Class, do not start the timer immediately. First calculate and show the predicted absolute finish date/time (`dd/MM HH:mm`) from the current wall clock and the approved Class duration. The user must either **Confirm & Run** at the proposed end or turn on **Choose a different end time** and enter a valid `HH:mm` finish. Preview its resolved date and both final Session lengths. **Cancel** returns to the saved-Class library with no Class started and no saved-Class selection changed.

The confirmed time is a real finish target, whether earlier, equal to, or later than the initially predicted finish. The signed difference is deducted from or added to **only the last two regular Sessions**, preserving earlier poses, all breaks, summary, and saved approved plans; follow the existing REMAINDER/EVEN/CUSTOM final-pair distribution. Reject invalid clock text, a finish not in the future, missing adjustable final pair when a changed finish is requested, any shortened final Session below one minute, arithmetic overflow, or any nonrepresentable timeline. Keep the dialog open with a correctable error; do **not** start or partially mutate a timer or saved Class.

`HH:mm` resolves to the **next occurrence** of that local clock time: later today or tomorrow if the time of day has already passed. The dialog displays the resolved calendar date so midnight behavior is visible. For an unchanged expected finish the displayed, precise initial target is preserved. Stored Class duration remains unchanged; runtime View length and progress use the adjusted duration. Exact device display uses minute precision; actual computation/deadline uses seconds and `SystemClock.elapsedRealtime`, not mutable wall clock. Long pauses and Restart add to the preapproved signed correction; if recovery no longer fits the two final Sessions it fails safely according to the existing timer policy. Previously entered final Sessions may not be rewritten. User's existing Exit/Back/Recents/nextPhase semantics remain unchanged.

**Backward compatibility:** if a legacy saved Class has no adjustable final pair, approving its unchanged predicted finish is allowed; attempting to change its finish is rejected without touching the saved Class. Notification permission is requested *before* the finish approval dialog to avoid postponing the approved start.

## Sources changed (D-111 development, versionCode 6)

- `android-native/app/src/main/java/com/ateliertime/domain/RunEndTimePlanner.kt`: pure deterministic validation, HH:mm resolver and preview.
- `android-native/app/src/main/java/com/ateliertime/domain/MonotonicSessionTimer.kt`: atomic signed initial shift + cumulative pause/Next/Restart compensation.
- `android-native/app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt`: mandatory pre-Run approval, dates, validation, user-friendly preview and isolated UI-test storage injection.
- `android-native/app/src/main/java/com/ateliertime/android/PresetLibrarySection.kt`: Run is read-only until approved; help updated.
- `android-native/app/src/main/java/com/ateliertime/android/SessionTimerScreen.kt`: display actual adjusted Class length/progress.
- `android-native/app/build.gradle.kts`: D-111 / versionCode 6, not a Stable promotion.
- **Tests:** `RunEndTimePlannerTest.kt`, `ApprovedFinishTimerTest.kt`, `RunEndTimePropertyTest.kt`, `RunEndTimeDeviceTest.kt`, `RunEndApprovalUiTest.kt`.

## All currently prepared automatic QA

**JVM:** all existing `:app:testDebugUnitTest` including scheduler, expiry/progress, monotonic timing, ClassClockTracker, pause/restart/Next, presets, sound, display modes, existing final-pair compensation, plus new Run clock parsing, today/tomorrow/midnight, earlier/later/equal, infeasible selections, one-pose compatibility, atomic start, post-pause/restart initial target preservation, selected deadline under simulated ± clock changes and minute-by-minute property sweep. The property test exercises 3 split modes × 149 valid signed minute offsets = **447** in one `@Test` method plus rejected offsets.

**Real Android:** prior 14 device tests (7 isolated AndroidPresetStore, 6 isolated MelodyPreferences, 1 silent AudioTrack) + 7 new pure/device-time/manifest tests + 3 Compose UI tests (approval + cancel, invalid entry + rejection, confirm -> actual Class -> Exit) = **24 Android `@Test` methods expected**. Compose UI tests seed temporary test-only SharedPreferences names and remove them after running. Neither the test harness nor device script clears the real stored Classes or Sounds. Android UI tests can momentarily open their own test Class, so run them only while no personal Class is active. Manual real 3-hour audio/Doze and actual unexpected OS process-kill notification cleanup remain separate reliability gates and cannot be claimed as passed from these short tests.

**CoGo build script:** `android-native/tools/qa_build_d111.sh` invokes all JVM tests and builds both real debug APKs with the proven native ARM64 AAPT2 override. Only after a successful build copies `Atelier-Time-D111.apk` and `Atelier-Time-D111-Tests.apk` into `/storage/emulated/0/Download`; no device installation in this step.

**Termux device script:** `android-native/tools/qa_device_d111.sh --run`, after wireless ADB connection, requires exactly one authorized connected phone and both APKs, updates app + test package with `adb install -r` (no uninstall/data clear), confirms `versionName=D-111` and installed AndroidJUnitRunner, runs `adb shell am instrument -w`, and requires exact **`OK (24 tests)`**. Stores full instrumentation output at `~/atelier-time-d111-device-qa.log`. Script never force-stops the app, deletes preferences, changes system clock, edits permissions, or disables any notification by command. It does update the installed D-110 debug APK to D-111, **after** the user explicitly runs this script.

### Exact handoff for the user's phone

1. In **Code on the Go**, once `git pull --ff-only` on the active `android-native-kotlin-compose` checkout succeeds and there are no local tracked modifications, execute:
   ```sh
   cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native
   bash tools/qa_build_d111.sh
   ```
   Do not remove the untracked `../../.cg/` CoGo directory. This builds and validates JVM source and both APKs; if Gradle fails, send the failure rather than installing.
2. In **Termux** (Wireless debugging must be ON, ADB paired and `adb devices -l` showing `device`, and **no Class active**) run:
   ```sh
   bash ~/storage/shared/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native/tools/qa_device_d111.sh --run
   ```
   Do not send personal pairing codes. If the link under `~/storage/shared` is unavailable, call script using the shared Android path `/storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native/tools/qa_device_d111.sh`.
3. Only after actual `BUILD SUCCESSFUL` and `OK (24 tests)` are observed may respective D-111 QA gates be marked PASS. Manual phone review of the finish-clock UI is recommended before considering Stable 5.
4. After QA, disable Wireless debugging in Android Developer options; normal D-111 UI operation does not require Termux or ADB.

**Current gate:** implementation committed; **D-111 compile/unit/device tests NOT RUN**. Historical D-110 14/14 PASS remains recorded separately.
