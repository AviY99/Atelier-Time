# Changelog — Atelier Time

## 2026-10-23 — Stage 2 Flutter-independent editor-input tests / Handoff v2.3

- Continued Stage 2 on isolated `stage-2-session-editor` branch after user asked to continue.
- Extracted Flutter UI text parsing into pure Dart `lib/src/editor_draft.dart`, exported from `lib/atelier_time.dart`. Preserves user-entered minutes, ordered opening groups, cycle and special placement; the scheduling engine still validates positivity and warnings. Ignores inactive manual ordinal text when special-break count is zero.
- Added **11 unexecuted plain Dart test definitions** for editor input conversion (`test/editor_draft_test.dart`) runnable with Android Termux Dart, without Flutter.
- **New adapter/tests NOT compiled or run** in the assistant runtime. Previously verified Stage 1 core 35 tests PASS; eight Flutter widget test cases still NOT run. No paid GitHub Actions, builds, merge, persisted preset or timer development.
- Updated project decision D-056, Stage 2 plan, roadmap, test plan, work log and handoff v2.3.


## 2026-10-23 — Stage 2 UI safety and widget-test source / Handoff v2.2

- Continued authorized Stage 2 `stage-2-session-editor` work; added stable widget identifiers for inputs, approval, endings, warning controls.
- Blocked malformed inputs and suppressed misleading partial timelines for invalid candidates. Updated mobile scroll layout while preserving two-pane wide display.
- Added **eight Flutter widget-test definitions** and `flutter_test` dev dependency, covering stage T19–T23 requirements and canonical Example B preview. Corrected short-rest warning fixture to a valid zero-summary session.
- **FLUTTER TESTS NOT EXECUTED**: no accessible Flutter SDK. Previously verified Stage 1 Dart core remains 35/35 PASS on Android Termux; it was not changed.
- Recorded D-055 and updated roadmap/QA/Stage 2 plan and handoff v2.2. PR #1 / draft PR #2 remain unmerged; no GitHub Actions, paid builds, APK, persisted presets, live timer or later-stage work.



## 2026-10-23 — Stage 1 integration proposed; Stage 2 editor prototype begun / Handoff v2.1

- User explicitly asked to **finish documentation and continue** after verified Stage 1 Step 1.4 completion (D-052). Recorded continuation decision D-053 and source/checkpoint D-054.
- Opened [verified Dart core PR #1](https://github.com/AviY99/Atelier-Time/pull/1), **not merged**. Created isolated [Stage 2 editor branch](https://github.com/AviY99/Atelier-Time/tree/stage-2-session-editor), nested Flutter prototype source under `apps/atelier_time_flutter/` consuming existing core via path dependency, and [draft PR #2](https://github.com/AviY99/Atelier-Time/pull/2).
- Added [Stage 2 input/preview and QA plan](docs/STAGE_2_PLAN.md) with all user-approved fields, full phase preview, three ending choices, and 25/5 warning acknowledgment; provisional neutral styling only. Stage 3 presets and Stage 4 timer/audio remain unstarted.
- Stage 1 Dart evidence remains **35/35 user-screenshot-confirmed passing**. Stage 2 Flutter source only underwent noncompiler structural check; **NO Flutter SDK, `flutter analyze`, Flutter unit/widget tests, APK or app build**. No GitHub Actions/paid workflow, budget change, branch merge or Stage 3+ coding.
- Updated DECISIONS, ROADMAP, TEST_PLAN, PRODUCT_SPEC, README, WORK_LOG, CHANGELOG and self-contained HANDOFF v2.1.



## 2026-10-23 — Stage 1 Step 1.4 pure Dart core verified / Handoff v2.0 (D-052)

- User screenshot confirms branch update `8cf7a24..907f871` and **`00:00 +35: All tests passed!`** under Android Termux Dart 3.13.7. The 35th T09 1m+34m regression passed along with all prior tests.
- Earlier screenshot proved `dart analyze` **No issues found!** for the prior 34-test checkout, plus prior successful smoke check. The new 35th test-only revision was **not separately re-analyzed**.
- **Stage 1 Step 1.4 scheduling core/required test gate VERIFIED and COMPLETE ON ISOLATED BRANCH.** Wider T13/T14 edge combinations remain a later QA backlog; no installed app or cross-platform build evidence.
- **No merge to main; no Stage 2 authorization. No GitHub Actions, paid runners, user budget modification, Flutter screens, Android APK or deployment.** Handoff v2.0 and decision D-052 preserve the exact stop point.



## 2026-10-23 — Stage 1 Step 1.4 T09 edge regression added / Handoff v1.9

- Screenshot-confirmed Android `dart analyze`: **No issues found!** and `dart test`: **34 tests all passed** (D-050). Separate smoke check passed.
- Required Stage 1 T09 custom final-pose endpoint audit identified untested 1m+34m (the complementary 34m+1m and zero-duration rejection were covered). Added a **35th** regression test including exact phase times and 25/5 warning acknowledgement on the same isolated code branch, commit `4b2f5d9fdd8dabc6dea8dcc0204c5062c7b6916a`.
- **35th test NOT EXECUTED YET**. User's free Android Termux command: `git pull --ff-only && dart test`. Retain 34-test screenshot PASS, no new success claim.
- Decision D-051, test plan, roadmap, README, branch progress, WORK_LOG and HANDOFF v1.9 updated. Stage 1 Step 1.4 still in final QA; no main merge, Stage 2 authorization, GitHub Actions, paid runners, builds or budget modification.


## 2026-10-23 — Screenshot-verified 34 Dart tests / Handoff v1.8

- Received Termux screenshots proving `dart pub get` succeeded, `dart analyze` reports **No issues found!**, and `dart test` ends **00:01 +34: All tests passed!** on Android Dart 3.13.7. The earlier smoke check also passed.
- Elevated verification to **screenshot-confirmed results D-050**, superseding user-report-only status D-049. This is not a multi-platform Flutter application build.
- Acceptance audit of the 34 tests identifies one explicit Stage 1 T09 custom split endpoint not yet tested (1m+34m); 34m+1m and 0m rejection already covered. No new tests or code committed in this log iteration, so original 34-pass evidence remains valid.
- **Step 1.4 remains in final QA review**, branch unmerged, Stage 2 not authorized. No GitHub Actions, paid runners, budget changes, UI, or APK.



## 2026-10-23 — Full Android Dart unit suite user-reported passing / Handoff v1.7

- Following user-confirmed Termux smoke test PASS (D-048), user reports **“All tests passed!”** after requested `dart pub get && dart analyze && dart test` on Android Dart 3.13.7 (D-049). Written suite currently defines **34 tests**. No full analyzer/test transcript or local commit ID supplied; preserve as **USER-REPORTED PASS**, not independently reproduced.
- Step 1.4 scheduling implementation has real positive test feedback. Remains in acceptance/coverage review; do not merge as final or start Stage 2 without appropriate checkpoints.
- No GitHub Actions, paid runners, builds, Flutter UI, APK or budget modifications. Updated test plan, roadmap, work log and self-contained HANDOFF v1.7.


## 2026-10-23 — Android Dart smoke pass / Handoff v1.6

- User confirms **“Scheduling smoke checks succeeded.”** on Android Termux Dart 3.13.7 following Step 1.4 warning-offset integer fix. This is a **user-confirmed successful Dart smoke run**, including compilation and the smoke assertions.
- Previous D-047 `num`/`int?` compiler error is no longer observed in that smoke run.
- Next checkpoint: `dart pub get && dart analyze && dart test`. **Full analyzer and unit-test results are still pending; do not call Stage 1 Step 1.4 complete.**
- D-048 and v1.6 handoff recorded; no Actions, paid runners, builds, branch merge, or Stage 2 authorization.


## 2026-10-23 — Stage 1 Step 1.4 first real Android compiler feedback / Handoff v1.5

- User successfully ran Dart SDK **3.13.7** in Android Termux and downloaded the development branch. The first smoke invocation **FAILED compilation** with `num` supplied to `int?` at `offsetSeconds: crossingOffset` in `lib/src/scheduling.dart`.
- Fixed the integer typing issue with conditional integer arithmetic; committed to `stage-1-4-scheduling-core` as `0dedd95756b6d3984b2e16548a58599179fbf7ff`.
- **Updated code NOT YET retested**. Next step for Termux: `git pull --ff-only && dart tool/smoke.dart`, followed (if successful) by Dart analyzer and test suite.
- D-047 and handoff v1.5 updated. No GitHub Actions, paid runner, merge, Stage 2 code or unverified success claim.



## 2026-10-22 — Stage 1 Step 1.4 verification preparation / Handoff v1.4

- Diagnosed missing Dart SDK and DNS resolution failures in assistant's container. This blocked **actual Dart test execution**, not source-code drafting.
- On isolated development branch, restricted `ScheduleCandidate` construction to engine/library code and retained full timestamped rest-warning acknowledgment records in immutable approved sessions; added unexecuted regression assertions.
- Added local no-GitHub-Actions helpers `tool/verify.ps1` and `tool/verify.sh` and `.gitignore` for test logs/caches. Minimum install for this pure-Dart step is **Dart SDK only** on a computer capable of execution.
- Recorded D-046, updated roadmap, README, test plan, work log and handoff v1.4. **No Dart compiler/analyzer/test run, build, paid Actions usage, merge or Stage 2 work. Step 1.4 remains IN PROGRESS**.

---


## 2026-10-22 — Stage 1 Step 1.4 D-044 support / Handoff v1.3

- User **approved D-044**, resolving former D-038: any **positive** special break can replace a normal break even if shorter/equal (examples regular 5m, special 1m/3m/5m), with the D-031 25/5 warning and acknowledgement when necessary. D-036 still blocks all 0-minute sessions/actual breaks.
- Updated Dart scheduling core on isolated `stage-1-4-scheduling-core` branch to accept negative, zero and positive `L−B` without `pendingDecision`.
- Added written regression and dependency-free smoke test source for shorter/equal special breaks and valid warning-override flows.
- Independent Python-based placement comparisons matched **25,000 virtual-schedule random cases** plus **4,000 additional exhaustive real-timeline cases**. This is **not Dart test execution**.
- **No Dart SDK/network access for download; no Dart compiler, `dart analyze`, `dart test`, smoke script execution, GitHub Actions, paid runners, build or merge**. Reported GitHub budget remains unchanged; Stage 1 Step 1.4 still **IN PROGRESS** and no Stage 2 authorization.
- Updated decision/policy/test docs, roadmap, README, work log, and self-contained HANDOFF v1.3.


## 2026-10-22 — Stage 1 Step 1.4 DP fix / Handoff v1.2

- Continued authorized pure Dart coding on `stage-1-4-scheduling-core` after user requested “Continue” and explanation of open D-038.
- Explained **D-038**: whether positive special break may be shorter or equal to positive regular break (e.g. special 3m versus regular 5m). Recommendation to allow with 25/5 warnings is **not a decision**. Code preserves `pendingDecision`.
- Fixed exponential automatic-break selection and a false-invalid-placement case using exact dynamic programming with original midpoint/even placement objective and deterministic earlier-ordinal ties.
- Added regression sources for a formerly false-invalid 97m case and an 8-hour five-special-break case. An **independent Python model** matched exhaustive search over 25,000 sampled configurations and canonical A/B positions, but **Dart tests have NOT run**.
- Source remains on isolated branch; no Dart/Flutter SDK, no GitHub Actions, budget change, compile, merge, UI or app build. **Step 1.4 remains IN PROGRESS; Stage 2 is not authorized.**


## 2026-10-22 — Stage 1 Step 1.4 first source increment / Handoff v1.1

- User's **“Continue”** after the blocked GitHub budget discussion authorized **Step 1.4 only** (D-040). Created isolated development branch [stage-1-4-scheduling-core](https://github.com/AviY99/Atelier-Time/tree/stage-1-4-scheduling-core).
- Committed pure Dart scheduling engine, standalone package manifest/export, canonical 3h A/B and 25/5 regression test code, dependency-free smoke script, and branch progress documentation.
- D-036 positive session/break rule retained; D-031 25/5 warning retained; D-038 positive-special-versus-regular-break comparison explicitly returns **pendingDecision**, not falsely accepted/rejected.
- **GitHub Actions not used or configured** due reported $12/month budget block (D-041).
- **Dart and Flutter SDKs unavailable** in current environment; **NO compilation, Dart analyzer, tests or app builds run**. Simple bracket/string/comment structural check only. Engine's multi-break search still needs performance verification.
- Source is on **development branch only**, not merged. Stage 1 Step 1.4 **IN PROGRESS / NOT COMPLETE**; no Stage 2 authorization. Updated root docs and handoff to v1.1.


## 2026-10-22 — Stage 1 Step 1.3 APPROVED / Handoff v1.0

- **Explicit approval:** User approved all remaining documented scheduling proposals **P-01–P-06** subject to **D-036**, and specifically prohibited beginning Stage 1 Step 1.4.
- Registered **D-037** Step 1.3 completion, **D-038** the one explicitly unresolved positive special-break-versus-normal-break comparison, and **D-039** Step 1.4 stop gate.
- Confirmed automatic closest-to-work-midpoint break placement (tie earlier), evenly spaced multiple special replacements, manual ordinals, validation, custom ending options, full preview and warnings with user acknowledgement.
- Approved zero *count* of special replacements and zero final summary for otherwise valid positive sessions. **Zero total session or actual break remains a hard error; no zero-length break slots.**
- Set 25/5 warning counter to **one full activated session** (no automatic cross-preset carryover); an overall 25m ending needs no invented break, while over-25m posing warns.
- Updated current docs, TEST_PLAN accepted future tests, README, roadmap, WORK_LOG and self-contained HANDOFF v1.0.
- **Documentation-only commits/read-back. No source code, automated test execution, build, UI, or installation. Step 1.4 NOT AUTHORIZED.**


## 2026-10-22 — Stage 1 Step 1.3 clarification / Handoff v0.9
- **Latest approved D-036:** A 0-minute full session or configured normal/active special break is invalid; previous D-031 permission for zero-minute breaks superseded. Positive whole-minute break minimum is 1 minute.
- **Retained D-031:** Accumulated 25m of posing calls for >=5m rest before continuing; valid shorter breaks and long poses trigger a warning requiring explicit user acknowledgement rather than automatic rejection.
- Withdrawn: the proposed zero-minute logical break-slot model from the earlier Step 1.3 review. Updated current policy, review, data model, product specification, tests, roadmap, README and handoff. Historical changelog entries remain for traceability, not current rules.
- Other Step 1.3 technical policies still require user approval. **No executable code or tests; Step 1.4 remains on hold.**


## 2026-10-22 — Stage 1 Step 1.3 consolidated review / Handoff v0.8
- After the user's “Continue,” consolidated remaining scheduling decisions into [STEP_1_3_REVIEW.md](docs/STEP_1_3_REVIEW.md).
- Proposed logical 0-minute regular/special break slots without elapsed zero-length phases, plus manual replacement ordinals, warning approval after final-pose changes, and boundary/cross-preset interpretation.
- Linked and refined Step 1.3 policy and pending tests; added decision-log entries D-034/D-035, revised roadmap, README, work log and handoff.
- **Approved 25/5 warning with 0-minute break override D-031 remains unchanged; all new details remain pending user sign-off. No code, automated tests or build.**


## 2026-10-22 — Stage 1 Step 1.3 safety clarification / Handoff v0.7
- **Approved D-031:** A 5-minute break is recommended after 25 minutes of accumulated posing without an adequate break. Accumulate consecutive pose phases; a rest under 5 minutes does not reset the counter.
- **Permitted:** A user can choose a 0-minute break, and can approve a schedule violating the 25/5 recommendation **after an explicit warning acknowledgement**; the app must not silently adjust the entered durations.
- **Superseded:** Prior hard minimum of 1 minute attributed to D-030; break defaults stay 5m regular / 15m special, both editable.
- Updated policy, product specification, data model, schedule rules, decision register and QA tests; documented pending cross-session and zero-length special replacement slot semantics.
- Updated progress roadmap, work log and handoff to v0.7. Other Step 1.3 proposals are not yet approved.
- **No executable code/tests, Flutter setup or app builds; Step 1.4 remains on hold.**


## 2026-10-22 — Stage 1 Step 1.3 / Handoff v0.6
- User authorized discussion/documentation of scheduling placement and edge policies by saying “Continue” after Step 1.2.
- Added [SCHEDULING_EDGE_POLICY.md](docs/SCHEDULING_EDGE_POLICY.md): six **proposed** rules covering midpoint long-break replacement, multiple evenly-spaced long breaks, manual ordinals, invalid configuration handling and final-pose edge cases.
- Preserved both approved example schedules exactly; these rule proposals are **not yet user-approved**.
- Updated D-026 through D-029, roadmap, scheduling contract, work log, README and current cross-chat handoff.
- **No Dart/Flutter source code, test executions or installable builds; Step 1.4 remains blocked.**


## 2026-10-22 — Stage 1 Step 1.2 / Handoff v0.5
- User's “Continue” authorized only the **data-model design** following approved Step 1.1.
- Added [DATA_MODEL.md](docs/DATA_MODEL.md) with proposed Dart session rules, variable-length editable opening pose groups, phase intervals, long-break replacement representation, validation issues, final-pose option model, and immutable approved preset shape.
- Included future test fixture input payloads for approved Example A and B; canonical expected schedules unchanged.
- Recorded decisions/gates D-023 to D-025; updated roadmap, architecture, README, work log and handoff.
- **No code or tests; Step 1.3 and engine implementation remain on hold.**


## 2026-10-22 — Stage 1 Step 1.1 / Handoff v0.4
- **Approved:** Flutter + Dart as one shared application codebase for Android phones/tablets, Windows, iOS/iPadOS and macOS in first release.
- **Clarified:** Native menus/graphics/widgets are optional; non-native Flutter UI is acceptable. Distinct OS-specific installation packages remain necessary.
- **Reaffirmed:** Every numeric rule from the examples is configurable through a per-session editor, including optional pose groups.
- Added approved [TD-001](docs/TECH_DECISION_001_PLATFORM.md) and updated product, architecture, decisions, roadmap, work log, README and handoff.
- **STOP:** Step 1.2 not approved. No code, Flutter scaffold, automated tests or builds created.


## 2026-10-22 — Documentation baseline 0.3
- Located the existing `AviY99/Atelier-Time` GitHub repository and initialized project documentation.
- Consolidated drawing/sculpting product specification, configurable numeric rules, review/approval, offline presets, live timer and three cues.
- Canonicalized both complete 180-minute examples and the three incomplete-final-pose alternatives with exact times.
- Added scheduling invariants, planned QA tests, six planned implementation stages following documentation Stage 0, decision register and reusable handoff.
- Verified all 11 new documentation files can be read back from GitHub.
- **No implemented app functionality or automated test results yet.** Stage 1 requires separate approval.

## Handoff history
- v0.1: Initial concept and example sequences drafted in chat.
- v0.2: Three-choice end-pose handling specified, time-ruler prototype shown in chat.
- v0.3: User approved requirements and authorized GitHub documentation setup.
- v0.4: User approved Flutter/Dart four-platform choice and non-native UI clarification for Stage 1 Step 1.1 only.
- v0.5: Step 1.2 documented the proposed data model.
- v0.6: Step 1.3 placement and edge-policy proposals prepared for review.
- v0.7: D-031 25/5 warning with historical zero-minute allowance, **later superseded by D-036**.
- v0.8: Initial review including now-withdrawn zero-minute logical break slots.
- v0.9: D-036 forbids 0-minute full session and configured breaks.
- v1.0: Stage 1 Step 1.3 approved as D-037; strict stop before Step 1.4. Current source of truth: docs/HANDOFF.md.
