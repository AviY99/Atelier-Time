# Changelog — Atelier Time

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
- v0.4: User approved Flutter/Dart four-platform choice and non-native UI clarification for Stage 1 Step 1.1 only; current source of truth remains docs/HANDOFF.md.
