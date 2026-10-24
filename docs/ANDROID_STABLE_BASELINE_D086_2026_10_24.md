# Atelier Time — Android Stable 1 (D-086), 2026-10-24

## Status and evidence

**DECLARED STABLE FOR THE CURRENT ANDROID PHONE / USER-ACCEPTED FUNCTIONAL BASELINE.** User explicitly reported "הכל עובד" after the D-086 update-install step and previously accepted D-085 ("הכל תקין"). This validates the app's current functionality **by user report**, including the latest Complete class timeline's two-line "Last gets remaining time" chip alongside Even and Custom. This is not a declaration of a signed production release, App Store / Play Store publication, exhaustive instrumentation, locked/background cue guarantees, or non-Android platform readiness.

- Stable label: **Atelier Time Android Stable 1 — D-086** (project checkpoint; Android manifest `versionName`/`versionCode` remain whatever source defines, not silently edited).
- **Immutable source snapshot:** branch [`stable/android-d086`](https://github.com/AviY99/Atelier-Time/tree/stable/android-d086), based exactly on native branch commit **`d0a03d2792cc324aeeba39b6327136ba6d37fd0c`**. Snapshot branch is not merged into the default branch; it records the accepted application source. No release tag or binary distribution is claimed.
- Active development continues on [`android-native-kotlin-compose`](https://github.com/AviY99/Atelier-Time/tree/android-native-kotlin-compose), [PR #3](https://github.com/AviY99/Atelier-Time/pull/3) remains open / Draft / unmerged (base `stage-2-session-editor`). Default branch `main` contains project documentation, not a promoted stable Android executable.
- Phone source sync and Gradle gate for D-086: user-provided screenshot displays **`BUILD SUCCESSFUL in 36s`** after `:app:testDebugUnitTest :app:assembleDebug`, **42 actionable tasks: 7 executed, 35 up-to-date**. No exact JUnit XML test count was submitted; Gradle task successful is supported. Previous D-085 Gradle was successful in 39s after a one-line compile fix.
- Application updated and working: **user-confirmed** ("הכל עובד"), not independently read from device logs. Existing saved Classes, signature verification, timer and cues are user-accepted in ordinary use. Do not re-request prior build/install for D-086 without a new reason.

## Functionality in the stable phone baseline

- Editor: fully configurable Class schedule and examples, opening groups, regular/special breaks, warnings and approval; Complete class timeline ending choice now fits width (Remainder two lines / Even / Custom); manual final pair split.
- Saved Classes in Timer HOME: approve/save/load/run/edit/rename/duplicate/delete; offline private versioned storage and persistence across ordinary upgrades.
- Timer: physical Class elapsed and end target, active Session/Break actual timestamps with seconds, phase countdown, Pause/Resume/Next/Restart segment/Exit with contextual Help; three chosen five-second audio cues A/B/C. Class start and finish clocks remain fixed in the accepted implementation. Next steps through phases and completes from last work Session.
- Layout is **functional Material 3 baseline, NOT visually approved brand design**. Future animations must be presentation-only and derive from stable timer state, never drive scheduling, sound, storage or actual time.

## Remaining gates and future development (not closed by stable baseline)

1. **Android instrumented tests**: nine Android instrumentation test methods exist as source but have no screenshot-confirmed connected-device execution. Validate saved-class storage restart/upgrade, corruption/future data protection, lifecycle and UI automation.
2. **Background, locked screen and audio continuity**: formally test notifications/cues while app backgrounded, screen locked, battery optimization/doze interruptions and foreground-service needs. Three cues heard during ordinary usage do not prove these states.
3. **Reliability / edge tests**: long sessions, sleep/time-zone/wall-clock changes, process termination/restoration, pause/restart near Class end and late Next. Check one-and-only-one completion cue, monotonically updated Session boundary End and fixed Class deadline. Capture logs and real device scenarios.
4. **Design / accessibility stage (next proposed stage, not implemented)**: pick one of three [visual themes](VISUAL_DESIGN_CONCEPTS_2026_10_24.md), freeze accessible palettes/fonts/components and interaction behavior. Motion user preference, reduced-motion mode, contrast, dynamic type, TalkBack, narrow screens and battery must be validated.
5. **Distribution/release hardening**: version code/name strategy, signing key protection, release APK/AAB, upgrade smoke tests, user guide, privacy/security review. Only debug APK via CoGo has been shown; no official store or GitHub release.
6. **Future portability**: Android-first decision D-062/D-067 remains. After Android release-quality gates, plan domain/UI reuse via Kotlin Multiplatform / Compose Multiplatform for iOS/iPadOS, Windows/macOS; no other platform builds are currently validated.
7. **Older open product decisions**: reconcile legacy D-015 summary label and D-016 cue granularity (in old specs) against already accepted phone behavior before any future functional modification. Do not silently alter accepted behavior.

## Continuation rules

Preserve the accepted stable branch exactly; use a separate feature branch or current Android draft branch for design changes and regression checks. Do not overwrite Android Stable 1 with untested animation prototypes. Animation must pause/disable with appropriate system and user settings, and must never flash rapidly. No paid GitHub Actions or branch merge without user approval.
