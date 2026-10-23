# Technical Decision TD-001 — Flutter + Dart, shared cross-platform application

> **2026-10-23 CRITICAL STATUS UPDATE — FEASIBILITY REOPENED (D-061):** This is the **historical** user-approved Flutter+Dart decision, **NOT current evidence of all-lifecycle toolchain suitability**. User explicitly requires cross-platform capability **from development and tests through builds, native signing/installers and release**, including accessible development from Android Termux. Flutter officially supports deployment on all four target OS families, but **does not officially support Android as a Flutter development host**; Apple iOS builds require macOS/Xcode and desktop Windows/macOS builds require OS-specific tools. The previously acknowledged caveats were incorrectly left to future stages rather than made a pre-code gate. **Do not use this document to justify further Flutter-specific coding.** Read [full re-assessment](PLATFORM_REASSESSMENT_2026_10_23.md); **platform choice on hold pending demonstrated workflow and renewed user approval**. Existing approved requirements/tests are preserved. No replacement stack chosen.


- **Date:** 2026-10-22
- **Status:** APPROVED by user
- **Scope:** Stage 1, Step 1.1 ONLY (technical platform choice)
- **Related decisions:** D-003, D-014, D-019, D-020 in DECISIONS.md
- **Implementing code:** NOT STARTED. Step 1.2 NOT AUTHORIZED.

## Decision

Use **Flutter + Dart** for Atelier Time, developed and maintained as **one shared application codebase** in the existing `AviY99/Atelier-Time` repository.

The **first release** must target:
1. **Android** phones and tablets
2. **Windows** computers
3. **iOS / iPadOS** (iPhone and iPad)
4. **macOS** computers

Linux and a browser/web release are **not required** for the first release.

User preference is the same single app in product behavior and codebase across targets; each operating system will nevertheless require its own compiled/distributed installation artifact. Where necessary, thin platform-specific integrations (for alarms, notifications, audio, packaging) are acceptable; they do not turn the project into four separate applications.

## Clarification of the original 'native controls' request

Native operating-system menus, graphics, and controls **are optional**. A consistent Flutter-rendered, non-native-looking interface is acceptable. The higher priorities are:
- ONE maintained cross-platform codebase
- feature and scheduling parity on all four selected platforms
- offline usability and installability
- an input screen in each session/preset where **every example number is editable**, including adding/removing optional pose groups
- verified, correct schedules, preset behavior, timer and sounds

Do not redesign the app around different OS-specific interfaces just to obtain native visuals. Responsive layout and accessibility remain important.

## Rationale

Flutter + Dart supports a common UI and domain implementation for the four required platforms without making native-widget fidelity a central architectural constraint. Use a shared schedule/calculation core, isolated from UI and audio. Platform-specific build and notification/audio integrations are anticipated where required by operating-system rules.

## Boundaries and caveats

- Four OS targets in the **first release** do not imply a single universal binary. A separate build/package is required for each target.
- Apple builds/signing require a suitable macOS/Xcode environment; Windows packages require Windows build tooling. Exact CI/CD, signing, developer accounts and distribution methods are **not yet decided**.
- Background/locked-device sound accuracy cannot be guaranteed from framework selection alone. Test on actual devices in the appropriate later stages.
- UI design, application logo, specific storage packages and third-party dependencies remain open.
- Scheduling details and example timings already approved in EXAMPLES.md do not change.

## Scope control / stop line

This decision closes **Stage 1, Step 1.1**. No source code, Flutter project scaffolding, dependency installation, tests, build workflow or Step 1.2 data model work should begin without the user's subsequent explicit authorization.

## Next gate

Present and seek approval to begin **Stage 1, Step 1.2 — define session/schedule data structures**, using Flutter/Dart context. Do not implement Step 1.2 before authorization.
