# Stable 8 / D-126 — Actual Samsung Android instrumentation QA: 42/42 PASS

**Evidence date:** 2026-10-28. **Result: PASS**.

## Real, physical-phone evidence

The user supplied a screenshot of the **actual Termux AndroidJUnitRunner retest** on their Samsung phone after applying test-source-only corrections. The displayed terminal output explicitly confirms:

- `PASS: corrected AndroidTest APK SHA-256.`
- `Confirmed installed main app D-126/code20; it will NOT be updated.`
- The user typed `IDLE` to confirm **no live real Class**.
- `Updating ONLY the AndroidTest APK` → `Performing Streamed Install` → `Success`.
- `expected_methods=42`.
- **`Time: 21.697`**.
- **`OK (42 tests)`**.
- **`PASS: 42/42 D-126 Android instrumentation tests.`**
- Runner confirms **no main app update, uninstall or data clear**.

**Test-APK source commit recorded in actual runner log:**
`07816e22014e2ff6593757cbd33879344ada1e80`.
This is not the release APK's source build SHA; it is the dev-branch AndroidTest correction snapshot. The installed regular-app D-126/source build remains `b2d47ca32c2f470d38e556ea7a571afaa89400e3`, Android versionCode 20.

**Actual Samsung instrumentation test suites completed successfully:**
`AndroidPresetStoreTest`, `MelodyPreferencesTest`, `RunDiagnosticsInstrumentedTest`, `RunEndApprovalUiTest`, `RunEndTimeDeviceTest`, `StaticAudioTrackLifecycleTest`.

The full on-device log remains at
`~/storage/downloads/Atelier-Time-D126-Instrumentation-Retest.log`
(visible from Termux under the phone's Download folder). The screenshot is the evidence obtained in chat; the new raw retest log was not separately uploaded.

## Earlier failures and their resolution

The first real D-126 instrumentation run executed **42 tests with 4 failures**. The user supplied the raw first-run log in chat. Exact triage and corrected AndroidTest source is documented in
[D-126 42-method instrumented-test triage](D126_INSTRUMENTATION_42_TEST_FAILURE_TRIAGE_2026_10_28.md).

- Three `RunDiagnosticsInstrumentedTest` methods failed at setup before assertions because the instrumentation APK private-context file directory was not writable from the target-app UID. Tests now use a randomized isolated target-app **cache** directory and clean up only that random QA folder; no real reports or saved data are altered.
- One `RunEndApprovalUiTest.confirmingLiveCalculatedFinishStartsOnlyAfterApproval` failed with `ComposeNotIdleException`. The test now freezes **only the Compose test virtual clock auto-advancement** after entering the constantly ticking Class UI; production timer logic was not changed.
- Retest with those AndroidTest-only fixes: **42/42 PASS, zero failures**.

## Consolidated acceptance status

| Gate | Actual result |
| --- | --- |
| D-126 Samsung Gradle build | PASS, **72/72 tasks** |
| D-126 JVM suite | PASS, **243/243**; failures/errors/skips all zero |
| Main app and AndroidTest APK SHA-256 vs build manifest | PASS, both `OK` |
| Physical Samsung Android instrumentation retest | PASS, **42/42**, `OK (42 tests)`, 21.697s |
| Combined executed automated tests | **285 passed across the JVM and device instrumentation gates** |
| User's manual functional acceptance | PASS: user explicitly declared installed D-126 **Stable 8, unified Timer logic + diagnostic logging** |
| Main app update or data deletion during retest | **None performed by the runner**; main D-126/code20 retained |
| Manual post-instrumentation Saved Classes/Sounds verification | **PASS — user confirmed all saved Classes and Sounds remain intact** (manual observation, not byte-level storage comparison) |
| Long-term physical Doze, process reclaim, all audible paths | Outside this instrumentation result; no newly claimed proof |

**Immutable checkpoints:** `stable/android-d126-qa8-logic-diagnostics` (current consolidated Stable 8), plus historical `stable/android-d124-qa8` (original Stable 8 logic-only baseline), must **remain frozen**. Corrections to AndroidTest sources and these QA documentation updates are on **`android-native-kotlin-compose` only**. Do not change application production source, rebuild/reinstall the main app, or change version numbering as part of recording this test PASS.

**Final device QA closure (2026-10-28):** The user explicitly confirmed **"הכל נשמר"** after the successful 42/42 Android instrumentation rerun: all existing Saved Classes and Sound settings were preserved. Mark manual post-test persistence **PASS based on the user's direct observation**, without claiming a byte-for-byte audit. Combined D-126 quality gates: Samsung Gradle 72/72 PASS, JVM 243/243 PASS, SHA-256 for both APKs OK, Android instrumentation 42/42 PASS, manual post-test preservation PASS. **Stable 8 stays frozen; no application rebuild/update is required.**
