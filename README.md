# Atelier Time

A planned offline-capable, installable posing/break timer for live-model **drawing and sculpting** ateliers.

**Project status (2026-10-22):** Stage 0 and Step 1.1 complete; **Step 1.2 data-model proposal documented; Step 1.3 scheduling-edge policies prepared for review**. Step 1.4 implementation is **NOT authorized**. No application source code, Flutter scaffold, build or executable tests have started.

Atelier Time lets an organizer configure pose and break rules, generate and approve a chronological schedule, save reusable presets, and run a timer with three distinct sound cues. If the last pose would be incomplete, schedule review offers: (1) keep the remaining short final pose; (2) split the time of the last two poses as evenly as possible; or (3) adjust their split with a ruler/slider.

## Documentation

- [Product requirements](docs/PRODUCT_SPEC.md)
- [Schedule model and algorithms](docs/SCHEDULING_RULES.md)
- [Complete 3-hour example schedules](docs/EXAMPLES.md)
- [Delivery stages and approval gates](docs/ROADMAP.md)
- [Decision register](docs/DECISIONS.md)
- [Design and technical approach](docs/ARCHITECTURE.md)
- [Step 1.2 — Dart data-model design](docs/DATA_MODEL.md)
- [Step 1.3 — scheduling placement and edge-case proposals](docs/SCHEDULING_EDGE_POLICY.md)
- [Approved framework and platform decision](docs/TECH_DECISION_001_PLATFORM.md)
- [Acceptance and test plan](docs/TEST_PLAN.md)
- [Iteration record](docs/WORK_LOG.md)
- [Current handoff to a new chat](docs/HANDOFF.md)
- [Change history](CHANGELOG.md)

## Important project boundaries

- An existing GitHub repository is being used. No application implementation has started.
- **Approved:** One Flutter + Dart codebase for Android, Windows, iOS/iPadOS and macOS in the first release; platform-specific compiled packages. Native OS menus/graphics are optional.
- **Still open for user approval:** proposed long-break midpoint/multiple-break rules and invalid-input behavior in Step 1.3; package distribution/signing, UI design and logo remain later decisions. No code/scaffold or Step 1.4 until separate authorization.
- Every number in both timing examples is configurable through a future per-session input screen; pose groups can be added/removed.
- “Summary” is a named final reserved time block; its specific activity has not been further defined.
- Do not silently convert open technical assumptions into approved product rules.
- Every iteration must update the decision log, work log, change log and handoff with evidence and next actions.
