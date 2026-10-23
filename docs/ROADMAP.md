# Delivery roadmap and approval gates

**Current authoritative status — 2026-10-23 (D-052):** Stage 1 Step 1.4 pure Dart scheduling core and required regression-test gate **VERIFIED / COMPLETE ON ISOLATED BRANCH** `stage-1-4-scheduling-core`, **not yet merged**. Android Termux Dart 3.13.7 screenshots show `dart test` **+35 All tests passed!** after the T09 split endpoint addition; previous screenshot showed `dart analyze` **No issues found!** on the prior 34-test checkout. Earlier smoke also passed. Static analysis was not rerun after the 35th test-only edit. Extended T13/T14 edge-matrix cases are documented as a later QA backlog, not falsely claimed executed. **Stage 2 NOT AUTHORIZED; no GitHub Actions, paid runners, Flutter UI, APK or cross-platform app build.**

This is a gated, iterative project. Show a working result and documentation after each implementation stage; obtain approval before moving to the next stage. No implied permission to implement later stages.

| Stage | Name | Planned result | Exit/approval evidence | State |
|---|---|---|---|---|
| 0 | Project documentation and baseline | Existing GitHub repository populated with approved specification, schedule examples, decision register, roadmap, test plan, and handoff | All 11 documentation files committed and retrieved successfully | **COMPLETE** |
| 1 | Technical baseline + scheduling core | Flutter + Dart selected; conceptual data model and deterministic scheduling engine with validation, final-pose options and regressions | Android Termux screenshot-confirmed 35/35 Dart unit tests PASS, earlier smoke PASS, earlier analyzer 0 issues; Step 1.4 code remains on branch | **STEPS 1.1–1.4 COMPLETED/VERIFIED FOR AUTHORIZED DESIGN+CORE SCOPE; branch unmerged; platform builds belong to later stages** |
| 2 | Cross-platform UX and session editor | Discuss screens and graphic style (Flutter-rendered controls accepted); implement a per-session input screen where every numerical rule is editable, plus complete schedule preview/approval and ruler/slider | Show working session editor and ruler that recalculates all times | Not started |
| 3 | Local preset library | Save, load, edit, copy, rename, delete and activate approved sequences; offline persistence | Demonstrate persistence after app restart and preserve selected final-pose resolution | Not started |
| 4 | Runtime timer and sounds | Reliable playback of scheduled poses/breaks/summary; phase progress, pause/resume/stop, three distinct sounds | Functional timing, sound, pause/interruption checks on target device | Not started |
| 5 | Quality, installation and accessibility | Device-based tests, clock/background edge cases, installation package, usability, reliability | Recorded acceptance tests and user-tested installable build | Not started |
| 6 | Final release and maintenance handoff | Versioned release artifact, installation/use instructions, complete continuation pack | User acceptance and tagged project release | Not started |

## Stage 1 breakdown — approve **each step** separately
1. **Step 1.1 COMPLETE (2026-10-22)**: Flutter + Dart approved with one shared codebase across Android (phone/tablet), Windows, iOS/iPadOS, macOS. Native-looking widgets optional. Details: [TD-001](TECH_DECISION_001_PLATFORM.md). **No code created.**
2. **Step 1.2 DATA MODEL DOCUMENTED**: [DATA_MODEL.md](DATA_MODEL.md) outlines configurable rules, schedule phases, approval states and example input fixtures. User's following “Continue” allowed advancing into Step 1.3, but does not independently finalize every proposed field name.
3. **Step 1.3 COMPLETE / APPROVED (2026-10-22)**: [SCHEDULING_EDGE_POLICY.md](SCHEDULING_EDGE_POLICY.md) and [STEP_1_3_REVIEW.md](STEP_1_3_REVIEW.md) approved by user **D-037**: automatic midpoint/ties, multiple and manual positions, input validation, final pose choices, 25/5 warnings and explicit approval. **D-036 forbids 0m sessions/active breaks**; D-031 preserves advisory warning for valid shorter rests. Approved one-complete-session posing accumulation; no automatic cross-preset carryover. **Only D-038** (positive special break <= normal) remains open.
4. **Step 1.4 VERIFIED / COMPLETE on development branch (2026-10-23, D-052)**: Dart scheduling core and 35 tests on [`stage-1-4-scheduling-core`](https://github.com/AviY99/Atelier-Time/tree/stage-1-4-scheduling-core); user screenshot `00:00 +35: All tests passed!` on Android ARM64 Dart 3.13.7. Earlier `dart analyze`: `No issues found!` for prior 34 tests; not rerun after test-only addition. T01–T12 and T15–T18 acceptance core mapped; T13/T14 extended cases remain later QA. **No merge to `main`, Flutter screens, APK, billable Actions, or Stage 2 authorized.**
5. Show generated tables for Examples A/B, every option, full invariants, and automated-test evidence.
6. Update DECISIONS, TEST_PLAN, WORK_LOG, CHANGELOG, HANDOFF and any affected product docs; request approval for Stage 2.

## Definition of a stage report
A stage report includes: agreed scope, work completed, files/commit IDs, tests run and actual results, deviations or proposed decisions, screenshots/builds when useful, blockers, next-stage options, and a fresh self-contained cross-chat handoff.

## Operating rules
- Do not alter earlier decisions silently; log changes with supersedes links.
- User approves substantive product behavior **and progression to each next sub-step**. The next “Continue” after Step 1.2 authorized documentation/discussion of Step 1.3 only, not automatic acceptance of the proposed edge behavior or any implementation.
- Use GitHub for authoritative text documents/source once created. Keep the handoff at docs/HANDOFF.md updated after each iteration.
- Preserve signed-off examples as regression fixtures.
- Ask for app icon image and discuss graphic design **when branding/design is reached**; do not invent the icon now.
- Never claim a working timer or installable build before implementing and verifying it.

## Step 1.3 consolidated review (2026-10-22)
Following D-036, the [Step 1.3 approved review](STEP_1_3_REVIEW.md) withdraws zero-duration break slots, preserves 25/5 advisory warnings for valid positive rest durations, and **records user approval D-037** for automatic/manual positions and remaining documented policies. Policy for positive special break <= normal was subsequently APPROVED via D-044 (including valid shorter or equal lengths); 0-minute configured breaks remain invalid. **Step 1.4 has subsequently been authorized (D-040) but is still IN PROGRESS and unverified; no later stage authorized.**

## Step 1.4 verification blocker and source hardening (D-046)

Step 1.4 remains **IN PROGRESS** on branch `stage-1-4-scheduling-core`, not yet merged. Further work added private candidate construction to prevent fabricated approved schedules, retained immutable timestamped safety-warning records in approved snapshots, and wrote a new unexecuted regression test. Local `tool/verify.ps1` (Windows) and `tool/verify.sh` (macOS/Linux) provide budget-free Dart smoke/analyzer/test checks and a shareable log.

**Blocking environment:** No Dart or Flutter installed here, and DNS resolution of SDK download hosts fails. Installing the official **Dart SDK** on a test machine is sufficient for Step 1.4; full Flutter/platform toolchains are later-stage needs. No GitHub Actions/workflow or metered build triggered. Do not call Step 1.4 complete without Dart test execution and fixes. No Stage 2 authorization.

## 2026-10-23 — Stage 1 Step 1.4 acceptance checkpoint

Android Termux Dart 3.13.7 user reports smoke success (D-048) and subsequently **“All tests passed!”** after running the requested `dart pub get && dart analyze && dart test` chain (D-049). The branch test file currently defines **34 Dart unit tests**, including both canonical three-hour references, ending splits, replacement-break placement and safety-warning behavior. User supplied no full test log or analyzer transcript; recorded as **user-reported results**, not independently reproduced.

**Stage 1 Step 1.4 remains IN PROGRESS pending coverage audit against the broader T01–T12/T15–T18 acceptance plan, any needed tests, and project completion review.** No GitHub Actions, paid runners, APKs, Flutter UI or later-stage implementation. Work stays on `stage-1-4-scheduling-core`; Stage 2 requires distinct explicit approval.


## Android screenshot-confirmed code check — 2026-10-23 (D-050)

Termux on Android ARM64: `dart pub get` succeeded, `dart analyze` returned **No issues found!**, `dart test` ended **00:01 +34: All tests passed!**. This is screenshot evidence, not merely user narration. The branch scheduling engine is functional under this SDK, but **Step 1.4 remains at the acceptance gate**: the minimum T09 final-split edge test requires both 1-minute endpoints; the current 34 tests explicitly cover only the 34m+1m direction. A small additional test and Android rerun should close that item before declaring Step 1.4 complete. No action on Stage 2, paid GitHub workflows or build distribution.


## Final Stage 1.4 T09 edge-case check committed (D-051)

An acceptance-mapping gap in T09 was closed at the **test-source** level by adding a 35th Dart test for the approved 1m+34m custom last-two-poses endpoint (commit `4b2f5d9`). The first 34 tests and analyzer have screenshot-confirmed PASS (D-050). **The 35th test is not yet executed**, so Step 1.4 remains in final acceptance QA. User can update their existing Termux project with `git pull --ff-only && dart test`. Do not merge or start Stage 2 or billable workflows.

## 2026-10-23 — Stage 1 Step 1.4 required test gate CLOSED (D-052)

- Screenshot shows branch fast-forward `8cf7a24..907f871`, then **`00:00 +35: All tests passed!`** for updated 35-test suite on Android Termux Dart 3.13.7.
- T09 opposite 1m+34m split now tested. Prior `dart analyze` on 34-test checkout returned **No issues found!**; prior smoke also passed. No analyzer re-run reported after 35th test addition.
- Pure Dart scheduling-core Step 1.4 **verified/complete on branch**. Additional optional/extreme T13/T14 cases remain in QA backlog. No claim of full Flutter application or multi-target build.
- **Stop at this gate**: wait for explicit authorization before merging branch or beginning Stage 2. GitHub Actions budget untouched.
