# Changelog — Atelier Time

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
- v0.7: D-031 break-safety 25/5 warning and 0-minute override documented; unresolved nuances remain. Source of truth: docs/HANDOFF.md.
