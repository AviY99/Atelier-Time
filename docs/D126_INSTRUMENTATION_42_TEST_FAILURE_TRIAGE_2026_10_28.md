# D-126 — actual Samsung 42-method instrumentation run: four failures, test-only corrections

**RESOLVED 2026-10-28:** Real Samsung retest: `OK (42 tests)`, 42/42 PASS in 21.697 seconds. Original four failures are historical; fixes affected AndroidTest only. No main-app update or data clear. Saved Classes/Sounds post-test confirmation remains pending. [QA pass certificate](STABLE8_D126_ANDROID_INSTRUMENTATION_QA_PASS_2026_10_28.md).


**Date:** 2026-10-28.
**Source:** User-uploaded, unabridged `Atelier-Time-D126-Instrumentation.log`, produced by the D-126 AndroidJUnitRunner via Termux ADB. **Do not treat this as a passing instrumentation run.**
**Tested installed binary / accepted Stable 8 app:** D-126/versionCode 20, from `b2d47ca32c2f470d38e556ea7a571afaa89400e3`. Freeze `stable/android-d126-qa8-logic-diagnostics` intact. D-124 historical checkpoint `stable/android-d124-qa8` intact.

## Observed device outcome

- The phone Android runner executed **42 methods**, with **4 failures** (38/42 passed), `Time: 49.069`, `FAILURES!!!`.
- **Three failures** in `RunDiagnosticsInstrumentedTest`:
  - `unfinishedRunIsStillRetainedForCrashInvestigation`
  - `longRunRetainsMoreThanThirtyEventsAndInitialReplannedSchedule`
  - `archivedRunHasCompleteEditorSnapshotOrderedEventsAndExplicitEnd`
  All throw `IllegalStateException: Could not create the private diagnostics directory.` at `RunDiagnosticsArchive.begin(RunDiagnostics.kt:30)`, **before any report assertion**. The test code passed `InstrumentationRegistry.getInstrumentation().context` (the TEST package context), but Android instrumentation executes with target application storage permissions, not arbitrary write access to test APK private `filesDir`. This does not demonstrate a defect in the regular D-126 app's production storage.
- **One failure** in `RunEndApprovalUiTest.confirmingLiveCalculatedFinishStartsOnlyAfterApproval`, at the first Compose query immediately after clicking Run confirmation, `RunEndApprovalUiTest.kt:257`. Android Compose reported `ComposeNotIdleException` and `IdlingResourceTimeoutException`, with continuously pending recompositions. The running app uses a 200ms live-timer ticker and the Compose TestRule auto-advances virtual time during waiting. This is a test synchronization issue consistent with the stack trace; **actual production behavior and any latent instability still require test rerun to validate**.

## Changes on development branch ONLY

1. `android-native/app/src/androidTest/java/com/ateliertime/android/RunDiagnosticsInstrumentedTest.kt`: use the `targetContext` under the target Android UID, but wrap it with a randomized isolated `filesDir` nested under `targetContext.cacheDir`. `RunDiagnosticsArchive` therefore writes only to a unique QA test directory, not to production `filesDir/atelier_run_reports`. `@After` removes only this randomized QA test directory. All 3 report tests and their assertions remain in place; no real Saved Classes/Sounds/reports are touched.
2. `android-native/app/src/androidTest/java/com/ateliertime/android/RunEndApprovalUiTest.kt`: in the 3 tests that assert RUNNING after confirming the Class, pause the Compose **test** main clock's automatic advancement and explicitly advance 64ms of frame time before assertions. This prevents the test rule from continually chasing a live timer's 200ms ticker and leaves the running Timer unchanged. The original status/legend/saved-Class assertions remain.
3. `android-native/tools/build_d126_instrumentation_retest_on_phone.sh`: Code on the Go compiles **only `:app:assembleDebugAndroidTest`** (42 test methods), writes isolated `Atelier-Time-D126-Tests-Retest.apk`, manifest SHA and build log into Download; **does NOT publish or install a regular app APK**.
4. `android-native/tools/termux_run_d126_instrumentation_retest.sh`: verifies the installed D-126/code20, the new test APK SHA, connected device and IDLE preflight; `adb install -t -r` updates **only the test APK**, executes all 42 tests, and saves a separate `Atelier-Time-D126-Instrumentation-Retest.log`, requiring exact `OK (42 tests)` to report PASS. **No app uninstall, app update or data wipe**.

## User execution if approved / connection available

In Code on the Go terminal at existing `Atelier-Time/android-native` (build only):

```sh
git pull --ff-only && bash tools/build_d126_instrumentation_retest_on_phone.sh && cp tools/termux_run_d126_instrumentation_retest.sh /storage/emulated/0/Download/Atelier-Time-D126-Retest.sh
```

In Termux with one connected `adb devices` entry and no real Class active:

```sh
bash ~/storage/downloads/Atelier-Time-D126-Retest.sh --run
```

The user previously approved installing/running the Android test APK. Before rerun, the script still requires confirmation no live Class is active. The existing user-accepted installed Stable 8 remains untouched.

**Status after source changes:** Root causes identified and test sources corrected; **the corrected AndroidTest APK has NOT YET been compiled on Samsung or executed on the real device**. The latest real instrumentation result is still **38 passed / 4 failed**, until a new log proves otherwise. No stable source or installation is changed.
