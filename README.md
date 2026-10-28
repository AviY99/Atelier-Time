# Atelier Time

> **LATEST CURRENT — Stable 9 / D-126 (2026-10-28):** Native Android Kotlin/Compose Timer and complete private JSONL diagnostics are user-accepted. Real Samsung QA: **243/243 JVM PASS**, **42/42 Android instrumentation PASS**, **285/285 combined**, Gradle 72 tasks successful, main/test APK SHA-256 OK; Saved Classes and Sounds preserved after tests (user confirmation). This is the same installed D-126/code20 build, not a new APK version. Immutable `stable/android-d126-qa9-tests-complete`; earlier Stable 8 branches unchanged. [Current status and Handoff](docs/CURRENT_STATUS_AND_HANDOFF_STABLE9_2026_10_28.md), [Stable 9 certificate](docs/STABLE9_D126_285_AUTOMATED_QA_ACCEPTANCE_2026_10_28.md). Old stage-status text below is historical.


A planned offline-capable, installable posing/break timer for live-model **drawing and sculpting** ateliers.

**Project status (2026-10-22):** Stage 0 and Stage 1 Step 1.1 complete; Step 1.2 data model documented; **Stage 1 Step 1.3 scheduling rules APPROVED and complete (D-037)**. D-036 positive session/break rule and D-031 25/5 rest-warning rule remain in force. **Step 1.4 implementation NOT AUTHORIZED.** No application source code, Flutter scaffold, builds or executable tests exist.

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
- **Narrow question still open (D-038):** Whether an actual positive special break may be shorter than or equal to a regular positive break. Platform distribution/signing, UI and logo remain later decisions. **No code/scaffold or Step 1.4 until separate authorization.**
- Every number in both timing examples is configurable through a future per-session input screen; pose groups can be added/removed.
- “Summary” is a named final reserved time block; its specific activity has not been further defined.
- Do not silently convert open technical assumptions into approved product rules.
- Every iteration must update the decision log, work log, change log and handoff with evidence and next actions.
