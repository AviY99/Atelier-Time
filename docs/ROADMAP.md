# Delivery roadmap and approval gates

Updated: 2026-10-22. **Stage 0 complete; Stage 1 Step 1.1 complete; Step 1.2 data-model design prepared for user review; Step 1.3 NOT AUTHORIZED. No application implementation has started.**

This is a gated, iterative project. Show a working result and documentation after each implementation stage; obtain approval before moving to the next stage. No implied permission to implement later stages.

| Stage | Name | Planned result | Exit/approval evidence | State |
|---|---|---|---|---|
| 0 | Project documentation and baseline | Existing GitHub repository populated with approved specification, schedule examples, decision register, roadmap, test plan, and handoff | All 11 documentation files committed and retrieved successfully | **COMPLETE** |
| 1 | Technical baseline + scheduling core | Flutter + Dart approved for Android, Windows, iOS/iPadOS, macOS; remaining work: data model, deterministic schedule engine, validation, final-pose allocator and regression tests | Demonstrate exact A/B schedules and three split modes with real automated test results | **IN PROGRESS: Steps 1.1 complete; Step 1.2 design prepared; awaiting user review before Step 1.3** |
| 2 | Cross-platform UX and session editor | Discuss screens and graphic style (Flutter-rendered controls accepted); implement a per-session input screen where every numerical rule is editable, plus complete schedule preview/approval and ruler/slider | Show working session editor and ruler that recalculates all times | Not started |
| 3 | Local preset library | Save, load, edit, copy, rename, delete and activate approved sequences; offline persistence | Demonstrate persistence after app restart and preserve selected final-pose resolution | Not started |
| 4 | Runtime timer and sounds | Reliable playback of scheduled poses/breaks/summary; phase progress, pause/resume/stop, three distinct sounds | Functional timing, sound, pause/interruption checks on target device | Not started |
| 5 | Quality, installation and accessibility | Device-based tests, clock/background edge cases, installation package, usability, reliability | Recorded acceptance tests and user-tested installable build | Not started |
| 6 | Final release and maintenance handoff | Versioned release artifact, installation/use instructions, complete continuation pack | User acceptance and tagged project release | Not started |

## Stage 1 breakdown — approve **each step** separately
1. **Step 1.1 COMPLETE (2026-10-22)**: Flutter + Dart approved with one shared codebase across Android (phone/tablet), Windows, iOS/iPadOS, macOS. Native-looking widgets optional. Details: [TD-001](TECH_DECISION_001_PLATFORM.md). **No code created.**
2. **Step 1.2 DESIGN PREPARED (2026-10-22)**: User instructed “Continue” following the completion of Step 1.1. Prepared [DATA_MODEL.md](DATA_MODEL.md), describing configurable inputs, generated phase records, final-pose choices, validation issues, approval state and canonical example fixture payloads. Documentation only; user review/approval pending.
3. **Step 1.3 NOT AUTHORIZED**: Specify deterministic midpoint long-break selection, ties, multiple replacements and short/invalid schedule handling. Request separate approval before beginning.
4. Implement and test schedule generator plus three end-pose options, in an appropriate core language for the target platform.
5. Show generated tables for Examples A/B, every option, full invariants, and automated-test evidence.
6. Update DECISIONS, TEST_PLAN, WORK_LOG, CHANGELOG, HANDOFF and any affected product docs; request approval for Stage 2.

## Definition of a stage report
A stage report includes: agreed scope, work completed, files/commit IDs, tests run and actual results, deviations or proposed decisions, screenshots/builds when useful, blockers, next-stage options, and a fresh self-contained cross-chat handoff.

## Operating rules
- Do not alter earlier decisions silently; log changes with supersedes links.
- User approves substantive product behavior **and progression to each next sub-step**. User's subsequent “Continue” authorized documentation of Step 1.2 only; it does not authorize Step 1.3 or coding.
- Use GitHub for authoritative text documents/source once created. Keep the handoff at docs/HANDOFF.md updated after each iteration.
- Preserve signed-off examples as regression fixtures.
- Ask for app icon image and discuss graphic design **when branding/design is reached**; do not invent the icon now.
- Never claim a working timer or installable build before implementing and verifying it.
