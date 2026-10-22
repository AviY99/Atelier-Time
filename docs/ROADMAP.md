# Delivery roadmap and approval gates

Updated: 2026-10-22. Stage 0 and Stage 1 Steps 1.1–1.3 approved/documented. **Step 1.4 is IN PROGRESS (D-040)** on branch `stage-1-4-scheduling-core` with pure Dart scheduling source and written regression tests. **No SDK, no Dart test/analyzer results, no GitHub Actions runs or builds**. D-036 prohibits 0m sessions/breaks, D-031 retains the rest warnings, and D-038 remains open.

This is a gated, iterative project. Show a working result and documentation after each implementation stage; obtain approval before moving to the next stage. No implied permission to implement later stages.

| Stage | Name | Planned result | Exit/approval evidence | State |
|---|---|---|---|---|
| 0 | Project documentation and baseline | Existing GitHub repository populated with approved specification, schedule examples, decision register, roadmap, test plan, and handoff | All 11 documentation files committed and retrieved successfully | **COMPLETE** |
| 1 | Technical baseline + scheduling core | Flutter + Dart approved for Android, Windows, iOS/iPadOS, macOS; remaining work: data model, deterministic schedule engine, validation, final-pose allocator and regression tests | Demonstrate exact A/B schedules and three split modes with real automated test results | **IN PROGRESS: Step 1.4 pure Dart core implemented on isolated branch, test execution BLOCKED (no Dart SDK / GitHub budget), not yet complete** |
| 2 | Cross-platform UX and session editor | Discuss screens and graphic style (Flutter-rendered controls accepted); implement a per-session input screen where every numerical rule is editable, plus complete schedule preview/approval and ruler/slider | Show working session editor and ruler that recalculates all times | Not started |
| 3 | Local preset library | Save, load, edit, copy, rename, delete and activate approved sequences; offline persistence | Demonstrate persistence after app restart and preserve selected final-pose resolution | Not started |
| 4 | Runtime timer and sounds | Reliable playback of scheduled poses/breaks/summary; phase progress, pause/resume/stop, three distinct sounds | Functional timing, sound, pause/interruption checks on target device | Not started |
| 5 | Quality, installation and accessibility | Device-based tests, clock/background edge cases, installation package, usability, reliability | Recorded acceptance tests and user-tested installable build | Not started |
| 6 | Final release and maintenance handoff | Versioned release artifact, installation/use instructions, complete continuation pack | User acceptance and tagged project release | Not started |

## Stage 1 breakdown — approve **each step** separately
1. **Step 1.1 COMPLETE (2026-10-22)**: Flutter + Dart approved with one shared codebase across Android (phone/tablet), Windows, iOS/iPadOS, macOS. Native-looking widgets optional. Details: [TD-001](TECH_DECISION_001_PLATFORM.md). **No code created.**
2. **Step 1.2 DATA MODEL DOCUMENTED**: [DATA_MODEL.md](DATA_MODEL.md) outlines configurable rules, schedule phases, approval states and example input fixtures. User's following “Continue” allowed advancing into Step 1.3, but does not independently finalize every proposed field name.
3. **Step 1.3 COMPLETE / APPROVED (2026-10-22)**: [SCHEDULING_EDGE_POLICY.md](SCHEDULING_EDGE_POLICY.md) and [STEP_1_3_REVIEW.md](STEP_1_3_REVIEW.md) approved by user **D-037**: automatic midpoint/ties, multiple and manual positions, input validation, final pose choices, 25/5 warnings and explicit approval. **D-036 forbids 0m sessions/active breaks**; D-031 preserves advisory warning for valid shorter rests. Approved one-complete-session posing accumulation; no automatic cross-preset carryover. **Only D-038** (positive special break <= normal) remains open.
4. **Step 1.4 STARTED / IN PROGRESS (2026-10-22)**: The user's later “Continue” authorized source-code work (D-040). Written engine/tests and no-dependency smoke script are on [stage-1-4-scheduling-core](https://github.com/AviY99/Atelier-Time/tree/stage-1-4-scheduling-core). **No Dart SDK available so compilation/tests have NOT run**. No GitHub Actions workflows were created or invoked because of the user's metered-budget block. D-038's unresolved comparison is represented as `pendingDecision`, not unapproved validation. **Do not mark complete until actual tests pass; no Stage 2 approval.**
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
Following D-036, the [Step 1.3 approved review](STEP_1_3_REVIEW.md) withdraws zero-duration break slots, preserves 25/5 advisory warnings for valid positive rest durations, and **records user approval D-037** for automatic/manual positions and remaining documented policies. Exact policy for positive special break <= normal remains OPEN as D-038. **Step 1.4 has subsequently been authorized (D-040) but is still IN PROGRESS and unverified; no later stage authorized.**
