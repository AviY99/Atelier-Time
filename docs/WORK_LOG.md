# Work log — every iteration and approval checkpoint

## 2026-10-22 — Iteration 0.7: Step 1.3 break warning and zero-minute override
**User clarification:** After 25 minutes of posing in one phase or accumulated across consecutive poses without qualifying rest, recommend a break of at least 5 minutes; violating configurations **must warn and require affirmative user approval**. The user may **choose 0** for break duration. This explicitly supersedes previous documentation that described an absolute minimum of 1 minute.

**Documented approved requirement (D-031):** cumulative pose-time counter resets only at breaks >=5 minutes; 0–4m breaks do not reset it. No silent rejection for the 25/5 rule; must display affected times and obtain explicit acknowledgement for each approved revision. Default new-session breaks remain 5m normal and 15m special but are editable, including 0.

**Updated:** Step 1.3 edge-policy doc and its P-04 zero-break rows, PRODUCT_SPEC.md, SCHEDULING_RULES.md, DATA_MODEL.md, TEST_PLAN.md (superseded hard-minimum tests), DECISIONS.md (noted D-030 historical reference, D-031 approved, D-032 proposed details), ROADMAP.md, README.md, CHANGELOG.md and this handoff.

**Open for review:** Whether warning accumulation should span separately activated saved full sessions (as opposed to consecutive poses within one full session); treatment of a session ending at exactly 25m without more posing; exact zero-minute *special* break slot presentation. Other P-01–P-06 policies still pending approval.

**No implementation:** No Flutter/Dart scaffolding, schedule engine, executable tests, UI or installable build. **Stage 1 Step 1.4 not authorized.**

**Verification:** GitHub file creation/updates and read-back checks, not runtime tests.

**Next:** Confirm the remaining interpretation, approve Step 1.3 rules, then request separate Step 1.4 authorization.

---


## 2026-10-22 — Iteration 0.6: Stage 1 Step 1.3 scheduling-rule proposals
**Instruction:** User said **“Continue”** after the Step 1.2 summary with a request to proceed to Step 1.3. This authorized **discussion/documentation of Step 1.3 only**.

**Work done (documentation):**
- Re-checked approved A/B reference schedules and the conceptual Dart data model.
- Added [SCHEDULING_EDGE_POLICY.md](SCHEDULING_EDGE_POLICY.md) with proposals P-01 to P-06: automatic long break nearest midpoint of working time, tie to earlier eligible slot, multiple breaks approximately evenly distributed, manual break ordinal selection, input validation, and final-pose handling when the three options are unavailable.
- Noted strict replacement of existing short-break slot, preserved final summary, required positive poses, deterministic error reporting, and future presentation of actual times.
- Proposed rules were reasoned against approved A/B example placements (third break in each); no programmatic tests or simulator were run.
- Updated README, roadmap, decision register, scheduling contract, changelog and HANDOFF.md.

**Approval status:** Step 1.3 design **prepared for review**. User has **not yet approved** P-01–P-06. Decisions D-026–D-029 record authorization, proposals and next gate.

**Excluded:** No Flutter/Dart project scaffold, app source, scheduling implementation, automated tests, audio, graphical UI or build. **Step 1.4 not authorized.**

**Next:** Show user the proposed placement/edge rules; request explicit approval/changes. Only after approval may the user authorize Step 1.4.

---


## 2026-10-22 — Iteration 0.5: Stage 1 Step 1.2, conceptual scheduling data model
**Instruction:** “Continue” after the user-approved completion of Step 1.1. Interpreted as permission for **Step 1.2 only** in accordance with the documented one-step-at-a-time process.

**Completed (documentation only):**
- Re-read the current approved GitHub handoff, architecture, roadmap, scheduling contract, examples and decisions.
- Added [DATA_MODEL.md](DATA_MODEL.md): conceptual Dart types, editable per-session inputs, ordered pose groups, replacement-break policies, immutable timeline intervals, final-pose adjustment choice and approval/persistence boundaries.
- Added representative Example A and B input payloads and related expected outputs as future regression fixtures. Marked automatic midpoint/tie behavior and edge-case policies explicitly unresolved.
- Updated the roadmap, architecture, decision register, README, changelog and handoff to reflect Step 1.2 preparation and current stop gate.

**Decisions:** D-023 authorizes documentation; D-024 is a technical **proposal** (not yet approved); D-025 retains the Step 1.3 approval gate. No approved user behavior changed.

**Not done:** No executable Dart or Flutter code, Flutter scaffold, rule-engine implementation, test runner, automated tests, installable packages, UX work or audio integration.

**Verification:** Documentation file creation/read-back and Markdown consistency checks only; no code tests possible or claimed.

**Next action:** Present Step 1.2 model summary for review and ask for explicit approval before Step 1.3; do not start Step 1.3 without it.

---


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
