# Work log — every iteration and approval checkpoint

## 2026-10-22 — Iteration 1.0: Stage 1 Step 1.3 APPROVED and completed (documentation only)

**Explicit user authorization:** “I approve the remaining Atelier Time Stage 1 Step 1.3 proposals as documented in STEP_1_3_REVIEW.md, with D-036 overriding any zero-duration break/session allowance. Record approval in GitHub and complete Step 1.3 only. Do not start Step 1.4 programming yet.”

**Completed:**
- Marked six scheduling/validation policies P-01–P-06 **APPROVED** and refined 25/5 warning-endpoint behavior and per-complete-session warning counter as approved. Automatic one-break midpoint and tie-to-earlier, multiple even placement, manual ordinals, blocking invalid schedule versus acknowledgment-required advisory warnings, final-pose feasibility/custom adjustments and immutable preview lifecycle are all recorded.
- Preserved **D-036:** all full session durations and configured actual normal/special break durations >0. A 0-minute summary is allowed only with a positive total and valid work time; optional special replacement *count* of 0 is distinct from 0-duration break and is approved.
- Preserved **D-031:** warn after more than 25m cumulative posing without a qualifying >=5m break; valid positive 1–4m breaks do not reset; user must explicitly acknowledge before approving a valid exception.
- Approved accumulation is scoped to one complete activated session by default; no automatic carryover into a different preset. Exactly 25m accumulated posing at end needs no phantom trailing break; an uninterrupted 30m pose warns.
- Registered **D-037** Step 1.3 approval, **D-038** narrowly unresolved (whether positive special break duration may be <= normal positive break), and **D-039** strict stop before Step 1.4.
- Updated STEP_1_3_REVIEW, SCHEDULING_EDGE_POLICY, DECISIONS, ROADMAP, SCHEDULING_RULES, DATA_MODEL, TEST_PLAN, README, WORK_LOG, CHANGELOG and HANDOFF v1.0.
- Approved A/B sample schedules and three final-pose options unchanged.

**Verification:** GitHub documentation commits and read-back only. **No Dart/Flutter code, scaffold, scheduling engine, unit-test execution, build or installable release exists.** Test cases remain **planned**, not passed.

**Next gate:** Step 1.3 is complete. Stop. User must separately approve Step 1.4, including any chosen implementation scope. Before enforcing a length comparison involving a positive special break <= normal break, get a decision on D-038 rather than assuming either behavior.

---


## 2026-10-22 — Iteration 0.9: latest break/session positivity correction
**User instruction:** “You are correct, Break of 0 or session of 0 is not valid.” This is the latest substantive clarification and supersedes the earlier D-031 *allowance* for 0-minute breaks while retaining D-031's **25 minutes accumulated posing / >=5m qualifying rest** rule as a *warning requiring affirmative user acknowledgment* for otherwise mathematically legal schedules.

**Approved D-036:** Full session duration >0, normal break and any configured active special break duration >0. The initial whole-minute editor thus has a 1m minimum per actual break; positive shorter-than-5m breaks remain editable. A 0m full session/break is a **blocking error**, never safety-warning override. The 5m/15m default values remain editable positive durations. A count of 0 optional special breaks is separate from a break duration of 0 and is pending final Step 1.3 policy approval. A possible 0m *final summary* is also distinct from 0m full session.

**Documentation changed:** D-036 in DECISIONS, revised SCHEDULING_EDGE_POLICY and STEP_1_3_REVIEW (previous logical 0m-slot concept withdrawn), PRODUCT_SPEC, SCHEDULING_RULES, DATA_MODEL, TEST_PLAN (replaced obsolete 0-break-allowed cases), ROADMAP, README, CHANGELOG and HANDOFF v0.9. Existing earlier work-log entries remain for traceability, not current authority.

**No implementation:** No Flutter/Dart source, scaffold, automated tests or builds. **Stage 1 Step 1.3 other rules still pending review; Step 1.4 not authorized.**

**Verification:** GitHub read-back of updated documents only; planned tests not executed.

---


## 2026-10-22 — Iteration 0.8: consolidated Step 1.3 scheduling-policy review
**User message:** “Continue” after the revised explanation of 25 accumulated posing minutes, recommended 5-minute adequate rest, and allowed 0-minute break with warning and explicit approval.

**Completed:** Continued **documentation and review within Step 1.3 only**. Added [STEP_1_3_REVIEW.md](STEP_1_3_REVIEW.md) with precise proposals for zero-duration **logical break slot** ordinals (without zero-length timeline phases), retaining slots for later special-break replacement, a possible last-two-pose allocation across a logical 0m break, 25/5 warning boundaries and one-full-session scope by default. Consolidated pending P-01–P-06 break placement/validation and added proposed test cases P-T09 to P-T15. Linked review in README/ROADMAP, updated DECISIONS with D-034/D-035, synchronized SCHEDULING_EDGE_POLICY and updated changelog/handoff.

**Status:** D-031 is approved and unchanged. New proposed details and prior P-01–P-06 **await user approval**; “Continue” did not constitute acceptance of every outstanding rule. Cross-preset rest accumulation remains an open separate feature question. Approved A/B schedules are unchanged. No code or executable tests created.

**Next action:** User reviews the consolidated Step 1.3 proposal and approves/revises it. Even after approval, obtain separate authorization for Step 1.4 programming and regression tests.

---


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
