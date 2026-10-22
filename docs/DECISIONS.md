# Decision register — Atelier Time

Updated: 2026-10-22. Decision statuses: **APPROVED** (user accepted), **WORKING PROPOSAL** (engineering default, changeable), **OPEN**, **SUPERSEDED**. Approval of product scope is not approval of a particular OS, framework or visual identity.

| ID | Status | Decision and context |
|---|---|---|
| D-001 | APPROVED | Name is **Atelier Time**. |
| D-002 | APPROVED | Intended for **drawing AND sculpting ateliers** using a live model. |
| D-003 | APPROVED | Standalone, offline-capable, installable app with native menus/controls wherever feasible. OS/stack remain open. |
| D-004 | APPROVED | Editable numerical rules; automatically calculate complete sequence; show for approval; save named reusable presets and activate later. |
| D-005 | APPROVED | Distinct sounds for pose/set end, break end and complete sequence end. Specific per-pose vs per-set transition handling will be validated in Stage 1. |
| D-006 | SUPERSEDED by D-010 | Former idea: simply shorten final pose without asking. Replaced by explicit three-option choice. |
| D-007 | WORKING PROPOSAL | The illustrative long break is around the middle and replaces a short break. Exact deterministic slot selection/ties and user placement configuration to be specified/tested in Stage 1. |
| D-008 | APPROVED | Logo comes from user picture **when relevant**; graphic direction will be discussed, not selected in advance. |
| D-009 | APPROVED | Maintain GitHub process, decisions, tests and cross-chat handoff after every iteration. |
| D-010 | APPROVED | On partial final pose, ask at preview: remainder (show times), near 50/50 split of last two poses (show times), or user-adjusted ruler/slider split (show times). Preserve total, interposing break and final summary. |
| D-011 | APPROVED as initial UI rule | One-minute slider increments and at least 1 minute per pose when feasible; revisit only by documented change. |
| D-012 | APPROVED | User said all requirements approved (2026-10-22) and instructed GitHub documentation setup **before** seeking authorization to begin Stage 1. |
| D-013 | EXECUTED | Use the existing public GitHub repository `AviY99/Atelier-Time`, rather than create a duplicate. GitHub documentation may be populated. |
| D-014 | OPEN | Target operating system(s), distribution / app packaging, and final technology stack. |
| D-015 | OPEN | Meaning/labels of the last reserved “summary” period beyond holding the reserved time slot. |
| D-016 | OPEN | Exact short opening pose alert behavior: each pose boundary vs each complete pose set/group. |
| D-017 | OPEN | Detailed timeline behavior for unusual input combinations and custom long-break placements. |
| D-018 | OPEN | Color palette, visual layout and user-supplied icon/image. |

## Scheduling examples: approved reference outputs
- A: 180m; four 1m poses + four 4m poses; repeated (5m break, 25m pose); 15m long break replaces a short break; final 15m summary. Baseline last pose 10m. Split options for last two poses: 25+10; 17+18; user sample 20+15, all with 5m intervening break.
- B: 180m; repeated (25m pose, 5m break); same replacement and summary. Baseline last pose 5m. Options: 25+5; 15+15; user sample 18+12, all with 5m intervening break.
- Canonical full schedules and elapsed-time start/end values in EXAMPLES.md.

## Change control
Every decision alteration gets a new entry or status update with date, reasoning, impacted files/tests, and user approval if the product behavior changes. Do not erase superseded decisions. The stage handoff must restate current approved and open decisions.
