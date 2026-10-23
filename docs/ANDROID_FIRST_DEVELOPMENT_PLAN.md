# Atelier Time — Android-first, phone-developed delivery plan

**Date:** 2026-10-23  
**Status:** User-approved **change in priorities** (D-062); **candidate implementation path** awaiting a real build feasibility spike (D-063). This is the new active platform-planning document. See historical [full-lifecycle reassessment](PLATFORM_REASSESSMENT_2026_10_23.md).

## Revised product delivery requirement — user instruction

> "פיתוח לאנדרואיד על הטלפון מהיר ויעיל שניתן אחכ להסב ביעילות לסביבות אחרות"

**Priority 1 (NOW):** Develop, compile, test, debug and install a working **Android APK directly on the user's Android ARM64 phone**, fast and efficiently, with no required desktop, paid GitHub Actions, or unsupported SDK workarounds.

**Priority 2 (ARCHITECTURE NOW, RELEASES LATER):** Minimize effort to adapt the app to **iOS/iPadOS, Windows and macOS** in subsequent phases. They remain desired later targets, **not a current prerequisite** for shipping/testing the Android edition. Future target packaging may require platform-specific host/toolchain; that is now acceptable in a future phase and must be documented when reached.

**Product behavior unchanged:** offline live-model posing timer, approved Examples A/B and D-031/036/037/044, fully editable rules, preview/explicit approval, named offline presets, live accurate clock and three different sound cues; lock/background requirements still need on-device tests.

## Preferred candidate — Kotlin Android-first + Code on the Go, then Kotlin Multiplatform

**Editor/build host:** [Code on the Go (CoGo)](https://appdevforall.org/code-on-the-go/) by nonprofit App Dev for All, actively maintained successor to archived AndroidIDE. Official description explicitly supports **writing, building, debugging Kotlin/Java Android APK apps directly on an Android phone**, including offline-capable work. Current GitHub [release 26.43](https://github.com/appdevforall/CodeOnTheGo/releases/tag/26.43) dated October 20, 2026. User is on Android ARM64 (known working Termux Dart 3.13.7); device-specific CoGo install/build success has **NOT** yet been demonstrated.

**UI/domain approach:** Kotlin with Android project first; prefer **Jetpack Compose** where the selected CoGo release actually builds it (the CoGo 26.40 release notes mention Compose Preview plugin), with separate domain model, rules scheduler, persistence and clock/audio adapters. Compose Multiplatform is a future UI-sharing path; [JetBrains Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform/kmp-overview.html) can share business logic and [Compose Multiplatform](https://kotlinlang.org/docs/multiplatform/faq.html) can share UI on Android/iOS/Windows/macOS. **CoGo support for a full KMP multi-target Gradle build on-phone is NOT proven and is not required now**; avoid immediately creating an iOS/macOS/Windows multiplatform project on Android.

**Device prerequisites (CoGo's published guidance):** Android 9+, ARM64 supported, roughly 3.5 GB free storage and 1.5 GB free RAM minimum (4 GB RAM recommended); actual requirements vary with Gradle caching and app complexity. Only obtain installer from CoGo project's official website or verified GitHub releases; do not install abandoned AndroidIDE. Releases 26.38+ changed toolchain; use a new project with the chosen current release instead of copying old Gradle settings.

**Important tradeoff:** Current verified scheduling engine and editor input adapter are **Dart**. Kotlin cannot call those Dart classes directly in an ordinary Kotlin Android app. Migration would require **porting scheduling logic** to Kotlin and using the saved 180-minute Example A/B exact outputs and **35 + 11 existing Dart tests as regression specifications**; port is work, not free. Preserve original branches/PRs until a new runnable APK and parity tests justify replacement. We must minimize that cost and prove the new toolchain first.

## Three-stage technical validation BEFORE committing the migration

1. **CoGo installation / compatibility (NO Atelier Time migration yet):** Install official CoGo for ARM64, create a small Kotlin Android project with a simple screen, compile **debug APK locally on the phone**, install and run it. Capture only build result/version and any first relevant error. No cloud paid runs. If Compose fails but XML succeeds, report the tradeoff before deciding UI strategy.
2. **Proof of core + project structure:** Build small Kotlin domain proof of canonical example B with deterministic phases, not the entire engine yet. Run on-device JUnit tests for schedule outputs; demonstrate UI uses the logic and can change duration. Preserve files under a **new isolated prototype branch**, not overwrite Dart branch. Measure build speed/device usability and local persistence feasibility.
3. **Proof of timer/background/audio and future code portability:** On-device test a small monotonic-clock phase timer with a real audible event while screen is on and while locked/background; confirm Android OS restrictions. Confirm domain layer has no Android-specific imports and UI Composables are not tightly coupled to Activity; only then approve larger port of approved Dart code.

**Gate to migrate:** If step (1) succeeds and Kotlin/Compose can build APK on the user's device, propose staged migration of only the scheduling core/input interface with parity checks. Do **not** assert the APK already exists or that Compose/KMP is fully compatible before first actual build. If CoGo cannot build the project, reconsider alternatives (including maintained Termux Flutter ports) based on reproducible evidence, not assumptions. Do not force a second framework rewrite without this test.

## Exit criteria / future target plan

- **Now / Android:** edit/build/debug/install APK directly from phone; all schedules/tests and preview work; persistent local presets; accurate timer + three cues, offline; no unexpected fees.
- **Later iOS/Windows/macOS:** preserve domain logic and composable UI by design; adopt KMP/Compose Multiplatform when compatible on a desktop/macOS build host, reuse as much Kotlin business/UI code as actually supported, add native integrations for timer/notifications/sound, run target-specific acceptance tests. **No promise of zero-effort or phone-only builds for Apple/desktop**.
- **Current status:** Preferred direction **Kotlin + CoGo**, pending local APK proof; the user authorized revised **requirements**, not an untested wholesale code migration.
