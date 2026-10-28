# D-124 — Unified constraint-based future planner

**Development only. Source committed; automated build and phone QA not yet executed. Not Stable.**

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
