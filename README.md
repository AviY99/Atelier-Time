# Atelier Time

A planned offline-capable, installable posing/break timer for live-model **drawing and sculpting** ateliers.

**Project status (2026-10-22):** Stage 0 and Stage 1 Steps 1.1–1.3 complete/approved. **Step 1.4 NOW IN PROGRESS (D-040)**: pure Dart scheduling core and tests committed on the [development branch](https://github.com/AviY99/Atelier-Time/tree/stage-1-4-scheduling-core). **No Dart SDK available, so no tests/analyzer/builds have run.** D-044 shorter/equal positive special-break cases are now implemented and covered by written regression tests, not yet executed. Automatic break placement has now been improved from exponential enumeration to exact dynamic programming on the development branch, with additional unexecuted regression tests (D-043). GitHub Actions remain unused because the user's account is budget-blocked. No Flutter UI/scaffold, timer, build or later stage work exists.

Atelier Time lets an organizer configure pose and break rules, generate and approve a chronological schedule, save reusable presets, and run a timer with three distinct sound cues. If the last pose would be incomplete, schedule review offers: (1) keep the remaining short final pose; (2) split the time of the last two poses as evenly as possible; or (3) adjust their split with a ruler/slider.

## Documentation

- [Product requirements](docs/PRODUCT_SPEC.md)
- [Schedule model and algorithms](docs/SCHEDULING_RULES.md)
- [Complete 3-hour example schedules](docs/EXAMPLES.md)
- [Delivery stages and approval gates](docs/ROADMAP.md)
- [Decision register](docs/DECISIONS.md)
- [Design and technical approach](docs/ARCHITECTURE.md)
- [Step 1.2 — Dart data-model design](docs/DATA_MODEL.md)
- [Step 1.3 — approved scheduling placement and edge-case policies](docs/SCHEDULING_EDGE_POLICY.md)
- [Step 1.3 — approved decision record](docs/STEP_1_3_REVIEW.md)
- [Approved framework and platform decision](docs/TECH_DECISION_001_PLATFORM.md)
- [Acceptance and test plan](docs/TEST_PLAN.md)
- [Iteration record](docs/WORK_LOG.md)
- [Current handoff to a new chat](docs/HANDOFF.md)
- [Change history](CHANGELOG.md)

## Important project boundaries

- An existing GitHub repository is being used. No application implementation has started.
- **Approved:** One Flutter + Dart codebase for Android, Windows, iOS/iPadOS and macOS in the first release; platform-specific compiled packages. Native OS menus/graphics are optional.
- **Latest approved D-036:** A full session or a configured normal/active special break of **0 minutes is invalid** (blocking error). Positive 1–4-minute breaks remain allowed; **D-031 warning and explicit user acknowledgment** apply to a mathematically legal schedule exceeding the 25-minute posing / 5-minute rest recommendation. Regular/special defaults remain editable positive 5m/15m. Logical 0-minute break-slot proposals were withdrawn.
- **Approved Step 1.3:** Automatic midpoint/earlier-tie replacement, evenly spaced multiple long breaks, manual positioning, whole-timeline validations/approvals, and 25/5 warning counter **within one full activated session** (no automatic carryover between distinct saved presets).
- **D-038 resolved by user-approved D-044:** Any **positive** special break may replace a normal break, even when shorter or equal (for example normal 5m, special 3m). The engine permits it and applies the approved 25/5 rest warning/acknowledgment. **Zero-minute configured breaks and total sessions remain invalid (D-036).** Dart code and tests are written but **not yet executed**.
- **Step 1.4** is restricted to the code/test core on a development branch, **without GitHub Actions, paid runners or deploys**. Dart compilation and test results are still pending. Flutter UI/scaffold, timer, distribution/signing and later stages remain unauthorized.
- Every number in both timing examples is configurable through a future per-session input screen; pose groups can be added/removed.
- “Summary” is a named final reserved time block; its specific activity has not been further defined.
- Do not silently convert open technical assumptions into approved product rules.
- Every iteration must update the decision log, work log, change log and handoff with evidence and next actions.
