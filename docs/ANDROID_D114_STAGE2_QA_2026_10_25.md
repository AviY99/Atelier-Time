# Atelier Time — D-114 Stage 2 automated QA (working runbook)

**2026-10-25 · Stage 2 explicitly approved by user in new conversation.**
**Document classification: SOURCE AUDIT + PREPARED TESTS, NOT EXECUTED RESULTS.**

## Non-negotiable baseline

- User approved Stable 5 D-114 on real Samsung SM-S921B (manual acceptance). This is not automated AndroidTest PASS.
- Immutable Stable 5 ref: `stable/android-d114` = `34a692a98282944a76e314efcc228dac3bd072ff`; versionCode 9, versionName D-114.
- All Stage 2 modifications are on `android-native-kotlin-compose`. **Do not write, update or merge into Stable 5.**
- The previous `OK (14 tests)` instrumentation evidence was generated with D-110 only.
- No tests/builds on D-114 have been executed in this conversation as of this checkpoint. Do not infer PASS from source method count, past device screenshots or a future APK build.

## 1. Source audit, exact inventory at Stage 2 preparation

| Suite | Source files | `@Test` methods | Evidence |
|---|---:|---:|---|
| JVM unit tests: `app/src/test` | 18 | **185** | Source counted; **NOT RUN** |
| Android instrumentation: `app/src/androidTest` | 5 | **37** | Source counted; **NOT RUN** |
| Android: persistence of saved Classes | 1 | 7 | Isolated random prefs; **NOT RUN on D-114** |
| Android: sound preferences | 1 | 6 | Isolated random prefs; **NOT RUN on D-114** |
| Android: silent AudioTrack lifecycle | 1 | 1 | Silent test; **NOT RUN on D-114** |
| Android: finish-time/device API checks | 1 | 11 | Side-effect-free domain/device checks; **NOT RUN** |
| Android: Compose run-finish approval UI | 1 | 12 | D-114 wheel tests; **NOT RUN** |

The source scan found **0 `@Ignore` annotations** in these suites. Actual Gradle/JUnit test totals may differ: record the task output and JUnit XML; do not substitute this table for run results.

Changes prepared:
- `RunEndApprovalUiTest.kt`: 7 obsolete typed HH:mm tests replaced with 12 D-114 UI checks: live calculated clock, hidden-until-requested wheels, still-live on opening, red previous-hour tap and rapid downward swipe hard stop, explanation on block, clearing after permitted green selection, reset, Cancel, default Confirm, feasible earlier/later minute and isolation of saved Class signature.
- `RunEndStage2BoundaryTest.kt`: 6 JVM boundary tests for live suggested finish, 2× late cap, minimum final-pair minutes, actual-start revalidation, neighboring red/green and crossing midnight.
- `build_all_qa_on_phone.sh`: D-114/current-development-branch gates; runs `testDebugUnitTest` with `--rerun-tasks`, assembles **test APK only**, checks actual XML reports, copies test package and runner to Downloads with SHA-256/commit/source-count manifest and logs.
- `termux_run_all_device_qa.sh`: D-114 installed-app version preflight; authorized one-device gate; test-only APK hash verification and install `adb install -t -r`; explicit interactive confirmation that no Class is active; full `AndroidJUnitRunner`, full local log and actual outcome check.
- Legacy `qa_build_d111.sh`, `qa_device_d111.sh` now fail closed with a pointer to modern scripts. They cannot accidentally install a D-111 APK. No production Kotlin, application version or saved Classes/Sounds were changed.

## 2. CoGo gate (approved Stage 2 source build; does not affect installed app)

In the **Code on the Go terminal** on the user's phone:

```bash
cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native
git pull --ff-only
bash tools/build_all_qa_on_phone.sh
```

The build runner refuses unexpected branch/version or dirty working tree. It verifies JVM result XML counts and Gradle errors; this gate compiles/packages D-114 AndroidTest but **does not run instrumentation or touch the installed app**. A `BUILD SUCCESSFUL` line is evidence of build/JVM task completion, not Android device test PASS.

Expected generated files in Downloads after successful CoGo gate:
- `Atelier-Time-D114-JVM-build.log`
- `Atelier-Time-D114-Tests.apk` (**test APK only**)
- `Atelier-Time-D114-QA.sh`
- `Atelier-Time-D114-QA-manifest.txt` (build commit, source method count and SHA-256)

**Collect actual** Gradle result and `D-114 JVM ACTUAL: tests=... failures=... errors=... skipped=...`. If the build fails, retain full log and do not proceed to Termux instrumentation. Test source is not proven compile-clean until this gate passes.

## 3. Termux / ADB gate (requires separate explicit user approval)

**Do NOT run at this stage without user approval specifically for device-affecting operations.** Android instrumentation runs in the installed app's package and may restart its process, create a temporary test Class, or affect notification/audio during testing. Ensure **no actual Class is active**. Data preservation is mandatory.

After the approval, ensure Termux Wireless debugging ADB has one authorized `device`, and invoke in Termux:

```bash
adb devices -l
bash ~/storage/downloads/Atelier-Time-D114-QA.sh --run
```

The runner validates the *already-installed* D-114 versionCode 9, signed APK manifest hash, and requires interactive `NO-ACTIVE-CLASS` confirmation before installing **only the test package**. It never installs/replaces the regular app, calls `pm clear`/uninstall, changes clocks, or force-stops an active class deliberately. Android instrumentation itself may restart the app process; only run when idle.

The device log is `~/storage/downloads/Atelier-Time-D114-Instrumentation.log`. Record actual JUnit `OK (... tests)` or failures/stack traces, device metadata and any skipped/inconclusive cases. The build manifest's expected test count **37** is the source target, not a claim of a completed run.

Do not reuse the old D-111 runner or D-110 result. Do not presume Wireless debugging's old Wi-Fi address/port are current. CoGo's 0-byte `$ANDROID_HOME/platform-tools/adb` must remain untouched; use Termux's working ADB.

## 4. Follow-up safety and coverage boundaries

After instrumented tests pass or failures are reproduced, use **safe, read-only** ADB checks for package metadata, manifest/foreground service registration, instrumentation discovery and relevant crash logs only when separately approved. Android HOME/Back/Recents/lock/notification transitions need grounded explicit on-phone observations; do not mark them automated PASS from policy unit tests.

Deferred until another *specific* approval: actual clock shift ±120 seconds, abnormal kill/process reclaim, long ~3-hour run, Doze/overnight battery behavior, audible output and real lock-screen timing. Fake injected clocks are domain-test evidence only.

Keep separate statuses:
1. `SOURCE READY` = authored/committed test code.
2. `BUILD PASS` = real CoGo Gradle execution and test APK assembled.
3. `JVM PASS` = actual Gradle test execution with JUnit XML counts.
4. `ANDROID PASS` = actual Termux/ADB AndroidJUnitRunner `OK(N tests)`, local logs.
5. `MANUAL ACCEPTED` = user-verified D-114 Stable 5; not a substitute for automated tests.

## 5. History and links

- Mandatory Stage 2 handoff: [Stable 5 to automated QA](HANDOFF_STABLE5_D114_TO_AUTOMATED_QA_2026_10_25.md)
- Main cross-chat handoff: [HANDOFF](HANDOFF.md)
- Test plan: [TEST_PLAN](TEST_PLAN.md)
- Device recovery and old QA evidence: [ANDROID_QA_RECOVERY_AND_DEVICE_TESTS_2026_10_25.md](ANDROID_QA_RECOVERY_AND_DEVICE_TESTS_2026_10_25.md)
