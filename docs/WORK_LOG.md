# Work log — every iteration and approval checkpoint

## 2026-10-22 — Iteration 0.4: Stage 1, Step 1.1 approved and documented
**User authorization:** Approve Flutter + Dart as the single-codebase technical framework; target Android phones/tablets, Windows, iOS/iPadOS and macOS for the first release. Non-native UI is acceptable. Update GitHub/handoff and **stop before Step 1.2 or coding**.

**Completed (documentation only):**
- Recorded approved technology, four release OS targets, one source codebase with separate OS packages, and native-controls-as-optional clarification in [TD-001](TECH_DECISION_001_PLATFORM.md).
- Updated DECISIONS (D-019 through D-022), product/architecture docs, README and roadmap as well as handoff/change history.
- Preserved the rule that every example numeric input is editable per full session; example schedules are tests, not hardcoded settings.

**Not started:** Stage 1 Step 1.2, Flutter project scaffold, source code, schedule calculation engine, tests, any CI/build, UI and installable packages. No platform-specific audio behavior is yet tested.

**Verification:** GitHub documentation commits and read-back checks; no runtime or automated code tests were performed because the user has explicitly prohibited implementation at this step.

**Current gate:** Stage 1 Step 1.1 **COMPLETE**; Step 1.2 **ON HOLD until next explicit user authorization**. Next action is to show decision/documentation links and request only Step 1.2 permission when the user is ready.

---

## 2026-10-22 — Iteration 0.3: document approved requirements
**Request:** All prior product behavior approved; inspect available GitHub repository, populate all project documentation, present stages, seek approval to start Stage 1.

**Repository:** Existing `AviY99/Atelier-Time`, public, default `main`; no need to create new repository.

**Done:**
- Confirmed repository and write permissions through connected GitHub tools.
- Initialized documentation in README and docs/.
- Recorded two 3h schedules, all three final-incomplete-pose options and exact elapsed timings.
- Specified requirements, scheduling invariants, decision register, stages/approval gates, proposed architecture, test plan and self-contained handoff.

**Not done:** No application source code, executable schedule generator, actual timer, app UI, audio assets, automated tests, installable package or user logo. These require stage approval.

**Verification:** All 11 files were re-fetched successfully from GitHub after creation, with nonempty contents and blob SHAs. Documentation stage is complete. No app/unit tests executed.

**Approvals:** Product scope approved by user. Authorization to start Stage 1 not yet received.

**Open items:** Platform(s), exact long-break auto-position tie rule, semantic meaning of final summary, sound nuance, visual design and user-supplied logo (later).

**Next action:** Present roadmap and explicitly request Stage 1 approval.

## How to extend this log
Every future iteration: date, stage, request/decision IDs, changed files + commits, work completed, test IDs/results (not just promises), blockers/limitations, status of user approval, and a precise next action. Update HANDOFF.md in the same iteration.
