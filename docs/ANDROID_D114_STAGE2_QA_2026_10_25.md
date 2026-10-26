## 2026-10-26 — REAL D-114 Stage 2 CoGo JVM PASS / AndroidTest APK build PASS (device not yet run)

**Evidence:** user-provided screenshot from the Samsung Code on the Go terminal after pulling the fixed D-114 UI test imports. Actual output: `:app:assembleDebugAndroidTest` and `:app:testDebugUnitTest` completed, `BUILD SUCCESSFUL in 1m`, `56 actionable tasks: 56 executed`. The script parsed actual JVM JUnit XML and printed **`D-114 JVM ACTUAL: tests=185 failures=0 errors=0 skipped=0`**. It also reported **`SOURCE AndroidTest methods=37, compiled APK NOT EXECUTED on device.`** AndroidTest APK path: `/storage/emulated/0/Download/Atelier-Time-D114-Tests.apk` (about 1.1 MB); associated manifest `/storage/emulated/0/Download/Atelier-Time-D114-QA-manifest.txt` (190 bytes), build/JVM log `/storage/emulated/0/Download/Atelier-Time-D114-JVM-build.log` (~6 KB). This is evidence of **185/185 JVM PASS and instrumentation APK BUILD PASS**, NOT evidence of 37/37 device execution or PASS.

**Branch control:** source tested at development branch `android-native-kotlin-compose` commit `3e78db0d014ec79f55ad41dd07f25c982a90173b` (prior to documentation-only updates); frozen Stable 5 ref `stable/android-d114` confirmed unchanged at `34a692a98282944a76e314efcc228dac3bd072ff`. No production app APK copied/installed, no saved Classes/Sounds data reset or deleted.

**STOP GATE:** Next task is AndroidTest instrumentation via Termux/ADB, but the user previously demanded **separate explicit approval before any operation that affects the installed application/phone**. Request approval before using `adb install -t -r` (test APK only) or running `am instrument`, which may restart the target process and exercise a temporary test Class/notification. Require no real Class active; never install ordinary APK, never `pm clear` or `uninstall`. After user approval, connect/verify ADB and run the approved Termux QA script, preserving its actual log and reporting honest results. This checkpoint marks Android instrumentation **PENDING**, not PASS.

## 2026-10-26 — D-114 AndroidTest compilation: exact two-import fix

Real CoGo compiler output from `Atelier-Time-D114-JVM-build.log` was provided by user. The only `e:` compiler diagnostics visible in the supplied screenshot are `RunEndApprovalUiTest.kt:5:33 Unresolved reference 'assertDoesNotExist'` and `RunEndApprovalUiTest.kt:6:33 Unresolved reference 'assertExists'`. Older lines marked `w:` are warnings, not the cause of this compilation failure. **Compilation FAIL remains an observed result; no new compile/AndroidTest PASS claim.**

On development branch `android-native-kotlin-compose`, commit `5c86956d7feec21aa0f698720131d5b8e0df90ba` removed only the two erroneous top-level imports. The existing Compose `SemanticsNodeInteraction.assertDoesNotExist()` / `assertExists()` method calls and all test assertions remain intact. No production Kotlin, saved presets, Sounds, app version, installed APK or Stable 5 files changed.

**Next safe gate:** in CoGo `cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native`, then `git pull --ff-only` and `bash tools/build_all_qa_on_phone.sh` (JVM test + AndroidTest APK build only). Review full actual `D-114 JVM ACTUAL` and Gradle output before claiming PASS; isolate additional compiler errors if any. Device install/instrumentation still requires separate explicit permission.

## 2026-10-26 — Actual D-114 CoGo compilation attempt failed (user screenshot)

The user reran `bash tools/build_all_qa_on_phone.sh` after the unrelated `.cg/` guard correction. Screenshot of real Code on the Go terminal shows `Execution failed for task ':app:compileDebugAndroidTestKotlin'`, Kotlin compiler reported `Compilation error. See log for more details`, and **`BUILD FAILED in 47s`**, `48 actionable tasks: 48 executed`. This is **AndroidTest Kotlin compilation FAIL**, not an Android instrumentation execution result. **Do not classify the overall build, JVM suite, or AndroidTest as PASS without separately inspecting actual JUnit XML.** Detailed compiler errors are above the photographed screen area and are not yet available.

The build script piped output to `/storage/emulated/0/Download/Atelier-Time-D114-JVM-build.log`. Next action is to retrieve only compiler `e:` / `error:` lines from the existing log (no rebuild, no phone install, no app data mutation), correct exact offending tests on development branch, then rerun the safe CoGo gate. Stable 5 stays frozen.

## 2026-10-26 — Stage 2 CoGo guard corrected for an unrelated helper directory

User ran `git pull --ff-only` in CoGo and provided a screenshot confirming the **fast-forward completed** to `bc5114b`. The next `bash tools/build_all_qa_on_phone.sh` invocation stopped **before Gradle** at `ERROR: working tree has local changes`. The requested `git status --short` showed only `?? ../../.cg/` — an unrelated untracked helper directory; the screenshot shows **no tracked file modifications**. This is a **QA script preflight false positive**, not a JVM or instrumentation failure.

Fix committed to the development branch only: `android-native/tools/build_all_qa_on_phone.sh` now checks tracked file modifications across the checkout and untracked app source/test/tool files under `app/src` and `tools`, without blocking on unrelated untracked IDE/helper folders such as `../../.cg/`. **Do not delete or reset the `.cg/` directory**. Stable 5 is not changed. GitHub commit `7fedf4b90e89a2b2b31de0d1aff565ca9d015921`.

Next in **Code on the Go** (safe source/test build only):
```bash
cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native
git pull --ff-only
bash tools/build_all_qa_on_phone.sh
```
Await the actual Gradle/JVM and AndroidTest APK build results. **No D-114 JVM, AndroidTest compilation, or device instrumentation PASS has been demonstrated in this stage.** Device installation/instrumentation remains behind a separate user approval checkpoint.

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
