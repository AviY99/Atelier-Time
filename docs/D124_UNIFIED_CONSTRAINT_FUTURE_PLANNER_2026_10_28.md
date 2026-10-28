# D-124 — Unified constraint-based future planner

**FINAL STATUS — USER-APPROVED STABLE 8 (2026-10-28).** Android identity **D-124 / versionCode 19**; **243/243 Samsung JVM tests passed**, 72 Gradle tasks succeeded, APK SHA-256 matched manifest, and user approved and exercised installed D-124 before explicitly declaring it **Stable 8**. Freeze checkpoint `stable/android-d124-qa8` (documented in [Stable 8 certificate](STABLE8_D124_QA_ACCEPTANCE_2026_10_28.md)). Earlier provisional, build-pending or failed-gate paragraphs below are historical, superseded by QA-D124-02/03/05 and user acceptance. No connected Android instrumentation run is claimed. D-125 subminute Summary rule is rejected, with unchanged fixed Summary.

## Why D-120 and D-121 failed

Those versions centralized the monotonic signed correction but still selected among source-pair, future-tail and last-only compensators. D-120 was rejected after Next QA, while D-121 misplaced a genuine special break during Restart after Next. D-123 introduced a terminal-tail special-case and was stopped at architecture review. This new attempt uses one constraint planner for all runtime reflows.

## D-124 design

- `ConstraintFuturePlanner.plan` is the one allocation function used by `FinalPairCompensation.reflow` for initial approval, Next, Restart and Resume.
- Inputs: immutable approved schedule, current runtime phases, one signed correction, protected-phase index, Next entry target, and real physical time remaining.
- The allocation depends on how many future ending work Sessions remain editable. Two available Sessions can be redistributed by the approved final choice; one available Session can absorb changes or divide into regular work/rest cycles. Neither case depends on any named Class.
- Completed/entered phases and Next target kind/start are protected. An already merged pair must not conscript earlier regular Sessions. Restart/Resume freeze previously inserted or relocated structure.
- New full regular Session+Break cycles require earned Next credit and the required one-minute residual. Preserve the approved strict threshold for insertion ahead of the final pair. The existing intervening Break is not charged twice.
- Genuine special `L>B` cannot disappear. `L<=B` has ordinary runtime treatment but retains its configured length when present. Manual special placement is preserved. Automatic genuine special relocation occurs only after Next and only in unstarted eligible slots.
- Check unique phase IDs, positive contiguous intervals, work >=60s, fixed Summary length, protected prefix, physical remaining-time invariant under 2000ms quantization, and total exact signed correction BEFORE committing.
- Older public `adjust` helpers remain for backwards compatibility and direct historical tests; no runtime action dispatches to them. The action-specific terminal splitter was removed from runtime.

## Regression plan

Added seven `D124ConstraintPlannerGeneralContractTest` tests covering an independent finite pair-feasibility oracle, thousands of budget combinations, cross-product of work/rest/special/summary/cycle lengths and consecutive Next, mixed Restart/Pause/Resume with deterministic random seed and atomic rejection, equal-length special, Next-entered legal merge, continuation after a merged final pair, and UI final-pair reporting after a merge.

All earlier 228 D-122 tests and eight D-123 tests remain. **Target: at least 243/243 JVM tests; actual result not yet observed.** Testing still required to validate code compilation and logic. No tests have been deleted, weakened, or skipped.

## Build-only gate

Development branch `android-native-kotlin-compose`, Android versionName `D-124`, versionCode `19`. Run `android-native/tools/build_d124_candidate_on_phone.sh` after fast-forward update on Samsung Code on the Go. Script validates source/version, runs JVM suite, assembles app and test APKs, writes candidate APK SHA-256 manifest; does not install or alter device data. A failure must be diagnosed before any installation approval.

After a passing build, review SHA, request separate device installation approval, and manually test six-minute `test`, 1-minute Session/3-minute Break, 25-minute and three-hour Classes, repeated Next/Restart/Resume, genuine Special placement, end deadline, Summary and saved data. Passing a finite test suite cannot guarantee every possible future action combination.

Frozen Stable 7 branch `stable/android-d119-qa7` is unchanged.


## Source review clarification

The planner evaluates the legally editable whole ending-pair allocation first and, when an invariant prevents it, the editable single-final-Session allocation under **the same validator**. This is constrained candidate enumeration, not the old split between source, future-tail and last-only correction mechanisms. The old functions remain audit/test helpers only. The actual terminal Session is also used for duration reporting after an allowed pair merge, rather than treating an unrelated earlier Session as part of the final ending.

**Evidence limitation:** inspected source and static GitHub file identities only. The 243 expected JVM regressions and Android APK have not yet been executed. Unexpected test failures must be investigated rather than changing acceptance criteria. No installed app changes.

## QA-D124-01 — Samsung JVM gate FAILED (2026-10-28)

**User-provided on-phone screenshot:** Gradle `:app:testDebugUnitTest FAILED`; `Execution failed for task ':app:testDebugUnitTest'`; `There were failing tests`. The run terminated `BUILD FAILED in 1m 22s` with `66 actionable tasks: 66 executed`. Some APK packaging tasks ran but the entire build-only QA gate is **FAIL**. **Do not use or install any D-124 APK**, do not mark D-124 accepted, and do not modify Stable 7.

The screenshot shows only the Gradle summary, not which JUnit cases failed, test counts, assertion messages or stack traces. Next step is a read-only diagnostic of `android-native/app/build/test-results/testDebugUnitTest/TEST-*.xml` on the phone, printing failing test names and exceptions. Make no speculative code changes and do not weaken or skip tests. Repair the unified constraint model based on evidence, rerun the full gate, and require a clean result before considering installation.

## QA-D124-01 — Samsung 243/4 failures traced to test-fixture construction (2026-10-28)

**Device evidence:** 243 JVM tests ran; exactly four failures, all within the newly created `D124ConstraintPlannerGeneralContractTest`. The APK build gate failed and **no D-124 installation is authorized**.

1. `aNewlyEnteredUnobservedFinalSessionMayUseLegalPairMerge`: incorrect fixture failed `ScheduleEngine.approve` with `Cannot approve invalid or unresolved schedule`. It specifies a 65-minute Class with 25-minute regular Sessions, creating a shortened 5-minute ending without a required `FinalPoseChoice.remainder()`.
2. `equalLengthSpecialRetainsItsConfiguredDurationEvenWithNextCredit`: NPE at the helper `plan(... )!!`, because the helper incorrectly computed total time as `6*pose+4*max(rest,special)+summary` and generated an unapprovable Class.
3. `crossProductNextReflowsProtectThePastAndPhysicalDeadline`: assertion at test line 158 that `cases>80`, not a failing physical-end/observed-prefix check; too many invalid fixture combinations were silently skipped.
4. `randomizedMixedNextRestartResumeRejectionsAreAtomic`: assertion at line 199 that `operations>=200`, likewise insufficient generated cases, not a reported atomicity violation.

**Correction:** Test helper now constructs EXACTLY six work Sessions and five Breaks for POSE_FIRST or six Breaks for BREAK_FIRST, replaces one standard Break's duration by the configured special duration, and uses the valid approved final-choice flow. Rather than silently skip illegal fixtures, it fails with a descriptive explanation. The direct 65-minute fixture now expressly selects Remainder. The engine, production logic, existing test assertions and count requirement **were not changed for this diagnostic fix**.

**QA status:** a new full Samsung JVM run is REQUIRED. This patch fixes preconditions of four tests but may expose previously untested genuine engine problems; it is **not** a PASS claim. Preserve D-122, Stable 7 and user data; do not install until green results and approval.

**Build-gate ergonomics:** D-124 build-only helper now writes D-124-labelled log, app APK, tests APK and hash manifest (formerly misnamed D-123). If Gradle/JVM tests fail, it prints failing JUnit test class/name/messages automatically from XML before exiting nonzero. This changes no app logic or data. The failing 243/4 result remains historical until a full fresh run is observed.

## QA-D124-02 — Samsung build-only PASS, 243/243 JVM (2026-10-28)

**Evidence supplied by user:** Samsung Code on the Go terminal screenshot after the test fixture repairs and the latest D-124 build helper:
- `BUILD SUCCESSFUL in 1m 36s`; `72 actionable tasks: 72 executed`.
- `D-124 JVM ACTUAL: tests=243 failures=0 errors=0 skipped=0`.
- `BUILD ONLY PASS` with `Atelier-Time-D124-Candidate.apk`, `Atelier-Time-D124-Tests.apk`, `Atelier-Time-D124-QA-manifest.txt` written to `/storage/emulated/0/Download`.
- Output explicitly states `NO regular/test APK installed`.
- Deprecation warning for `createComposeRule` occurred during AndroidTest compile and did not fail the build.

**Scope:** Full local JVM suite PASSED; Android app and instrumentation APK assembly PASSED. **Android connected instrumentation was not run**, installed-app lifecycle and background behavior not checked, and user acceptance of new general planning not given. This is NOT a Stable release. The prior D-124 243/4 test run remains in the history as a failed gate before fixture repair.

**Static source audit:** `FinalPairCompensation.reflow` invokes `ConstraintFuturePlanner.plan`; legacy `adjust`, `adjustFutureTail`, `adjustLastOnly` remain callable for historical isolated tests, but no timer runtime transition dispatches directly to them. Constraint planner checks locked phase prefix, target, special identity/count, Summary, min-duration continuity, physical remaining-time coherence and unique IDs. These checks establish tested conditions, not a mathematical proof for all possible schedules. Preserve Stable 7 and installed D-122 until device acceptance.

**Next non-installing gate:** compare candidate APK SHA-256 to `app_sha256` in the generated manifest and check `version=D-124`, `code=19`, `branch=android-native-kotlin-compose`, and source `commit`. No installation or clear-data action without explicit separate approval.

## QA-D124-03 — APK hash verification PASS on Samsung (2026-10-28)

User provided a phone-terminal screenshot of the exact SHA-256 comparison between `/storage/emulated/0/Download/Atelier-Time-D124-Candidate.apk` and the generated `Atelier-Time-D124-QA-manifest.txt`. The check explicitly printed **`D-124 APK VERIFIED - PASS`**.

Manifest fields visible:
- `version=D-124`
- `code=19`
- `branch=android-native-kotlin-compose`
- `commit=09c63d42d5b7ae3ddc13b8755a2262b7b0e5c9a7`

This is the commit represented by the built APK/manifest, distinct from subsequent documentation-only commits. The screenshot does not print the digest itself, but its shell equality check confirms the manifest's `app_sha256` equals the candidate APK's `sha256sum`. No APK was installed as part of verification. Earlier build-only QA: **243 tests, 0 failures, 0 errors, 0 skips**; application and androidTest APKs built successfully. Connected instrumentation and runtime acceptance are still pending.

**Gate:** WAIT for the user's **explicit permission to update-install** the already verified application APK. On approval, install `Atelier-Time-D124-Candidate.apk` as an update only (no uninstall, no clear storage/data, no install of test APK). Immediately verify saved Classes including `test` and `Draw1425a`, Sound settings, repeated Next + Restart behavior with minimum 1-minute Sessions, long-break and 25/5 controls, fixed physical Class finish, and Summary. Frozen Stable 7 branch remains unchanged. Never claim stable D-124 solely from JVM/hash passes.

## QA-D124-04 — explicit user approval for update installation (2026-10-28)

After the phone screenshot confirmed SHA-256 `D-124 APK VERIFIED - PASS`, the user explicitly answered **`כן`** to: "האם אתה מאשר להתקין את D-124 כעדכון על הגרסה הקיימת, תוך שמירה על כל ה־Saved Classes והגדרות הצלילים?" This grants permission for an **in-place update only** of `/storage/emulated/0/Download/Atelier-Time-D124-Candidate.apk` (D-124/code19, manifest build commit `09c63d42d5b7ae3ddc13b8755a2262b7b0e5c9a7`). It does **not** authorize uninstall, clear-data, forced downgrade or `Atelier-Time-D124-Tests.apk` installation or Android instrumentation.

**Previously successful Samsung method:** My Files → Internal storage → Download → `Atelier-Time-D124-Candidate.apk` → Android installer `Update`. If Android requests uninstall or refuses package/signature compatibility, stop without reset. Before update stop any active Class by normal user controls. Post-update check About D-124, Saved Classes `test` and `Draw1425a`, Sound A/B/C choices, then run manual Next/Restart QA. **The user has approved the installation, but the actual on-phone install is not yet reported or observed.** Stable 7 frozen.

## QA-D124-05 — installed phone: 2m45s final Session is the legal minimum-residual outcome (2026-10-28)

User supplied two installed-device screenshots of running saved Class `test` after several Next actions, reporting **"בעקרון הכל עבד"** and substantial improvement over the earlier oversized-single-Session issue. The timer shows `Session 5` lasting `02:45`, `Adjusted final two sessions: 01:00 + 02:45`, the unchanged 6-minute Class with 1-minute Summary and end time `10:18`. The action history shows successive `Next` over `phase_3`, `phase_4`, `phase_5` and `runtime-tail-*` added phases, with natural transition into `runtime-tail-pose-4`. This is device evidence of multiple generated runtime-tail cycles, not proof of every timing case.

The user asked why the last `02:45` was not split as `01:00 Session + 01:00 ordinary Break + 00:45 Session`. The approved runtime constraint is **all work Sessions >= 60 seconds**, including the final Session. For `P=60s, B=60s`, an additional cycle requires `P+B+minFinal=180s` of future work time. `02:45 = 165s` is **15s short**; the proposed `00:45` final Session violates the one-minute minimum. This is explicitly enforced in `ConstraintFuturePlanner.allocateLast` via `remaining >= p + b + min`. Under the current approved product rules, preserving the 165-second terminal Session is **correct** and is NOT the earlier bug.

Do not change the code or lower the final Session minimum based on this screenshot. If the user proposes changing product rules to permit <60-second terminal Sessions, treat that as a separate approval/review with implications for editor safety, final-pair compensation, Next/Restart, sound cues, and UI; not as a bug fix. D-124 is installed as inferred from screenshots plus prior explicit installation approval and run logs, but **the user has not yet explicitly confirmed post-install saved-class/sound persistence or declared a new Stable release**. Frozen Stable 7 remains unchanged.


## QA-D124-06 — user accepts current behavior; D-125 rejected (2026-10-28)

User confirms the current installed D-124 version is **good for now** and explicitly rejects the proposed D-125 idea of transferring a <60-second terminal residue to Summary. This extreme configuration of 60-second repeated Sessions/Breaks is a situational decision for the user rather than a software automation requirement. Preserve fixed Summary length and 60-second minimum Session. **No production code or APK changes**. D-125 closed/not adopted; refer to [D-125 decision](D125_TERMINAL_SUBMINUTE_REMAINDER_TO_SUMMARY_2026_10_28.md). Do not label D-124 as a new stable-numbered release without explicit user instruction. Stable 7 frozen.
