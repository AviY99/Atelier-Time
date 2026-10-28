# Architecture and design constraints — implemented core, Stage 2 editor underway

> **CURRENT ARCHITECTURE — Stable 9 / D-126 (2026-10-28):** Android Kotlin + Jetpack Compose, pure Kotlin scheduling domain with a unified `ConstraintFuturePlanner` for runtime Session/Break timeline reflow, Android-specific notification/audio and isolated Saved Class/Sound data stores, private per-Run JSONL diagnostics and user-directed SAF export. Existing Flutter/Dart design passages below are historical. Implementation is on [Android dev branch](https://github.com/AviY99/Atelier-Time/tree/android-native-kotlin-compose/android-native). [Current status](CURRENT_STATUS_AND_HANDOFF_STABLE9_2026_10_28.md).


> **D-068/D-069 — 2026-10-23: CURRENT IMPLEMENTATION** User approved actual Android Kotlin/Compose source, explicitly **no changes to accepted session/schedule/product rules**. `android-native/` Gradle project on new [android-native-kotlin-compose](https://github.com/AviY99/Atelier-Time/tree/android-native-kotlin-compose) branch separates pure Kotlin deterministic `domain` from Android Compose editor/input for later KMP extraction. D-043 exact DP special break placement in Kotlin port; 21 Kotlin JUnit sources unrun. Offline local preset repository, monotonic timer and 3 cue adapters remain next tasks. Historic Flutter architecture below is obsolete for current delivery. See [native development/QA process](ANDROID_NATIVE_EXECUTION_AND_TEST.md).


> **LATEST D-064:** Select **native Kotlin + Jetpack Compose + Gradle** for Android, modular `core/domain` pure Kotlin separated from platform-specific storage/timer/audio. Future Kotlin Multiplatform/Compose Multiplatform reuse is a target, not a promise of zero port effort. **Android Studio = official industry-standard IDE; Code on the Go = current mobile IDE**, not the standard. Drop D-063's toy APK proof prerequisite and instead implement real Atelier Time. Earlier Flutter-specific architecture below is historical/paused, and 35+11 Dart regression test evidence must be preserved for Kotlin port parity.


> **2026-10-23 — ARCHITECTURE SELECTED D-064:** Android-first **Kotlin, Jetpack Compose, standard Android Gradle plugins/builds**; author/debug/build directly on Android ARM64 phone using maintained **Code on the Go**. Android Studio remains conventional *desktop* IDE and CoGo is phone-specific, not the official industry standard. Establish portable **pure Kotlin** scheduling domain + rules validation + regression cases, separated from Jetpack Compose screens, local persistence, and Android foreground/background timer/audio adapters. Future Kotlin Multiplatform for shared domain and Compose Multiplatform UI on iOS/Windows/macOS. **Do not copy Dart code as if it were Kotlin**, and do not port unverified Flutter source blindly. No proof-spike prereq per user's instruction; build the real app with normal tests. Earlier Flutter architecture sections retained historically.


> **2026-10-23 — D-062 LATEST ANDROID-PHONE-FIRST PRIORITY:** The user now requires **fast on-phone Android development and installability first**, while making iOS/Windows/macOS port efficient later. D-061's universal build-on-every-host restriction is superseded. Maintain platform-independent tested Dart scheduling and editor parsing; prototype [Dart-to-JavaScript browser proof](ANDROID_FIRST_PLATFORM_SPIKE.md) on an isolated branch, then verify phone-built Capacitor APK and background notifications before choosing a migration. **Do not redesign the core or delete the existing Flutter prototype** without results/approval.


> **2026-10-23 — CURRENT D-062/D-063 DESIGN PRIORITY:** Build/run Android APP and APK **on user's own phone** first; plan efficient future port to iOS/iPadOS, Windows, macOS. The earlier Flutter-only UI architecture is **historical draft, NOT selected for the new Android-first gate**. Candidate is Kotlin Android + Code on the Go; isolate domain from UI/Android APIs, evaluate Jetpack Compose with future Compose Multiplatform after a REAL first APK build. Preserve all deterministic timing requirements and existing Dart fixtures for parity testing. See [ANDROID_FIRST_DEVELOPMENT_PLAN.md](ANDROID_FIRST_DEVELOPMENT_PLAN.md). No Kotlin/KMP compilation claimed yet.


> **2026-10-23 — D-061 CROSS-PLATFORM LIFE-CYCLE AUDIT SUPERSEDES THE PLATFORM ASSUMPTION BELOW:** Flutter is a supported **deployment** framework for the targets but not an officially supported **Android/Termux development host**. The user now explicitly requires viable cross-platform work from development/tests through packaging, installation and operation. This document's Flutter layer/packaging choice is **PROVISIONAL / UNDER REVIEW**, not final implementation permission. See [PLATFORM_REASSESSMENT_2026_10_23.md](PLATFORM_REASSESSMENT_2026_10_23.md). Do not modify tested scheduling formulas; stop adding Flutter UI until the platform feasibility gate is resolved.


**Current status (2026-10-23, D-052–D-054):** Flutter + Dart and four target OS families approved. The Stage 1 pure-Dart scheduling core is **implemented and screenshot-verified 35/35 tests PASS** on isolated branch `stage-1-4-scheduling-core`; see [core integration PR #1](https://github.com/AviY99/Atelier-Time/pull/1). Stage 2 responsive editor/preview was subsequently authorized for preliminary source work and drafted on `stage-2-session-editor`; see [plan](STAGE_2_PLAN.md), [draft PR #2](https://github.com/AviY99/Atelier-Time/pull/2). **Neither PR is merged. Flutter UI has not compiled or run.** No saved presets, live timer, audio, APK, GitHub Actions or paid build.

## Required layers
1. **Rules/input validation**: validate editable counts, durations and cycle order.
2. **Pure deterministic schedule engine**: emit immutable ordered phases with ID/type, planned duration and elapsed start/end; no UI or sound responsibilities; uses integer time.
3. **Approval/allocator**: when last pose incomplete, provide remainder, near-equal, custom slider allocation, recompute all affected elapsed times, and commit only an approved schedule.
4. **Preset persistence**: named versioned data containing rules, allocated schedule and choice; reliable offline storage with migration path.
5. **Playback clock**: derive live state from actual elapsed clock and immutable scheduled phase boundaries (avoid accumulating timer callback delays); support pause/resume and interruption.
6. **Sound adapter**: route pose-end, break-end and final completion to three different sound cues; avoid double alerts.
7. **Shared cross-platform UI**: Flutter-rendered controls are acceptable for the per-session editor (all numbers editable), chronological review, split slider, preset library, live timer and settings; native OS widgets are optional, not required.
8. **Packaging**: platform-specific installable/offline builds for approved Android, Windows, iOS/iPadOS and macOS targets from the shared codebase; investigate platform limitations for background/lock-screen audio rather than promising behavior prematurely.

## Proposed data shape (illustrative, not schema lock)

The more detailed authoritative **proposal** (including user-editable opening groups, data types, draft/approval lifecycle and JSON examples) is [DATA_MODEL.md](DATA_MODEL.md). The abbreviated shape below remains introductory only.
```text
SessionRules
  title, totalDuration, summaryDuration,
  openingGroups[{count,poseDuration}],
  regularPoseDuration, shortBreakDuration,
  longBreakDuration, longBreakCount, longBreakPlacement,
  cycleStartsWith

SchedulePhase
  id, kind = pose|short_break|long_break|summary,
  duration, startsAt, endsAt, poseNumber?

ApprovedPreset
  schemaVersion, id, name, sourceRules,
  finalPoseChoice = remainder|equal|custom|none,
  customSplit?, approvedSchedule[]
```

## Required algorithm boundaries
The long break REPLACES a normal break. The end-pose resolution is user-selected during preview and must preserve the intervening break, final summary and total duration. The 180-minute example schedules are exact regression oracles.

## Technical issues for Stage 1
- **Step 1.1 COMPLETE:** Flutter + Dart, one application codebase for Android/Windows/iOS-iPadOS/macOS; platform-specific compiled packages; native graphics/menus optional.
- **Step 1.2 DESIGN PREPARED, NOT IMPLEMENTED:** SessionRules, opening groups, cycle, break-placement policy, candidate phases and approved preset are defined conceptually in [DATA_MODEL.md](DATA_MODEL.md). User review is pending.
- **Step 1.3 NOT AUTHORIZED:** Finalize automatic-middle positioning and edge-case behavior only after user's next approval.
- Determine sound reliability and clock behavior in background, sleep or phone lock under its restrictions.
- Confirm the precision and edge-case validation.
- Clarify whether long break count/position is explicitly selected vs automatically centered; show actual placement.
- Determine if presets are local-only initially (assumed yes).
- No server or user login is necessary for the approved behavior.

## Design sequence
Prioritize an accessible, responsive, cohesive Flutter interface rather than insisting on native widgets. Request a logo image from the user during visual design. Provide an actual working preview before asking for approval of the interface stage.
