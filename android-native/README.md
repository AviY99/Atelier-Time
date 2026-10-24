# Atelier Time — Android native Kotlin / Jetpack Compose

> **2026-10-24 LATEST AUDIO/TIMER SOURCE — PHONE BUILD PENDING:** User confirmed prior APK/editor and preserved saved preset after upgrade but reports the short DTMF preview sounds are too similar and **automatic timer alarms did not sound**. New source replaces them with six 5-second multi-note AudioTrack melodies; three independent persistent choices and preview buttons in **main editor**, automatic per-pose (including opening), break-end and session-end cue dispatch. Pause or Stop followed by continuing the same session now keeps original finish deadline by shortening last two regular poses under the selected Remainder/Even/Custom split, while preserving summary and intervening rest; impossible/retrospective adjustment is blocked. A distinct "Start new full session" resets the deadline. Separate sound settings do not edit saved presets; actual lock/background cues require foreground service. **55 JVM source tests / 8 Android instrumentation source tests written; new commits NOT YET BUILT ON PHONE.** Previous `BUILD SUCCESSFUL in 37s` belongs to older code. See [decisions D-073/074](https://github.com/AviY99/Atelier-Time/blob/main/docs/DECISIONS.md) and [handoff](https://github.com/AviY99/Atelier-Time/blob/main/docs/HANDOFF.md).

> **LATEST 2026-10-24 D-071 — APPROVED GROUP BREAKS, SOURCE NOT BUILT:** User explicitly confirmed the rule: configurable break after every opening group; earlier group boundaries permit `0m`, last group requires `>=1m`; the existing first BREAK_FIRST break counts once if identical (Example A remains 00:20–00:25). Implemented editor fields/help, pure Kotlin scheduling phases/validation, optional version-1 JSON group-break serialization and signature protection preserving legacy approved sessions. Regression test source now **46 JVM JUnit + 7 Android instrumentation**, with the 9 new group-break JVM methods and one new device test **NOT EXECUTED**, in addition to 5 unrun K04 timer JVM methods. Earlier phone `:app:testDebugUnitTest`/APK passes apply only to previous source; updated D-071/K04 code has **not** been built/installed yet. PR #3 remains draft, no paid Actions. See [approved decision](https://github.com/AviY99/Atelier-Time/blob/main/docs/DECISIONS.md), [test plan](https://github.com/AviY99/Atelier-Time/blob/main/docs/TEST_PLAN.md), [handoff](https://github.com/AviY99/Atelier-Time/blob/main/docs/HANDOFF.md).

> **LATEST 2026-10-24 REAL PHONE + K04 SOURCE STATUS:** The user installed/launched Atelier Time on Android and supplied a screenshot of the working Compose editor, canonical Example A full schedule and existing selected saved preset. Previous Gradle `:app:testDebugUnitTest`, `:app:assembleDebug` and APK SHA-256 were screenshot-verified at earlier code. **NEW K04 code (NOT YET PHONE-BUILT)**: `domain/MonotonicSessionTimer.kt` monotonic clock, `android/SessionTimerScreen.kt` running/paused/restart view and three distinct ToneGenerator cues, navigated via "Open session timer" from an approved schedule. **Five new JVM JUnit tests WRITTEN, NOT RUN** (37 total source methods; 6 Android instrumentation). Editor labels and per-group/section Help improved, under [copy review](https://github.com/AviY99/Atelier-Time/blob/main/docs/EDITOR_LABEL_REVIEW_2026_10_24.md). New user requirement D-071: breaks after opening groups (pre-final boundaries can be zero; last break positive), **NOT YET IMPLEMENTED** pending scheduling-slot compatibility review with Example A and saved presets. K04 background/locked-screen sound guarantee not implemented; D-016 cue granularity open. No paid CI or PR merge. See [up-to-date handoff](https://github.com/AviY99/Atelier-Time/blob/main/docs/HANDOFF.md).

> **CURRENT STATUS — 2026-10-23, K03 hardening after D-070:** Offline approved-preset catalog, JSON storage and Compose UI are written but **uncompiled and unverified on Android**. A subsequent corruption-safety fix wraps non-string SharedPreferences values as storage failures and refuses direct writes that would erase malformed/future-version data. Added two more instrumentation regression methods: **32 JVM JUnit + 6 Android instrumentation methods written, NONE RUN**. The Kotlin Gradle test, APK build, installation and device persistence checks remain outstanding. K04 timer/three cues NOT IMPLEMENTED. See the [live handoff](https://github.com/AviY99/Atelier-Time/blob/main/docs/HANDOFF.md) and [test plan](https://github.com/AviY99/Atelier-Time/blob/main/docs/TEST_PLAN.md).


**Status (2026-10-23): REAL project source committed, NOT compiled/analyzed/device-tested, no APK yet.** This is **not** a sample/throwaway proof project. Its UI already edits session rules and previews/approves actual generated schedules. Offline preset persistence and running timer/audio are the next functional deliverables, **not yet implemented**.

## Architecture / portability
- `app/src/main/java/com/ateliertime/domain/` — pure Kotlin scheduling models + deterministic engine, **no Android imports**. Deliberately follows approved Dart semantics and can later be extracted to Kotlin Multiplatform (KMP).
- `app/src/main/java/com/ateliertime/android/` — native Android Activity, Jetpack Compose editor and text-to-rules converter. The editor supports session, opening groups, all break parameters, automatic/manual placement, last-two-pose choices, warning acknowledgement and in-memory timeline approval.
- `app/src/test/` — **21 written Kotlin JUnit test methods** (16 scheduler, 5 editor input). Exact Example A/B phase spans, safety warnings, D-036/D-037/D-044, D-043 DP edge/8-hour optimization, and custom-split extremes. **NONE has run yet**; original Dart 35/35 and 11/11 PASS are separate historical results.

## On-phone development (not an experiment)
Use maintained **Code on the Go** (CoGo) for Android ARM64: [official site](https://appdevforall.org/code-on-the-go/) / [releases](https://github.com/appdevforall/CodeOnTheGo/releases). Use its Git/open-project workflow to obtain this repository on branch **`android-native-kotlin-compose`**, and **open the nested `android-native` directory as Gradle project root**. Do not select a Flutter or Dart package as the project.

Build tasks in CoGo/Gradle:
- `:app:testDebugUnitTest` — compiled JVM tests for Kotlin scheduler, editor text conversion and exact A/B regression parity
- `:app:assembleDebug` — local debug APK (should appear under `app/build/outputs/apk/debug/`)
- `:app:installDebug` (if Gradle and device access permit), otherwise install debug APK through CoGo's install action
- When device instrumentation setup is available: `:app:connectedDebugAndroidTest`

**No Gradle wrapper JAR is checked in.** CoGo manages its own Gradle 9.6.1 / AGP 9.3.1 / Kotlin 2.3.21 toolchain per upstream on-device documentation. The source pins corresponding AGP, Kotlin and Compose Compiler versions rather than mixing incompatible plugins. AGP 9 builds Kotlin natively; do NOT add obsolete `org.jetbrains.kotlin.android` plugin to module. If CoGo reports different toolchain versions or a missing SDK, inspect its actual environment rather than repeatedly reinstalling unrelated tools.

The user can still inspect the code with Termux Git, but **bare Termux Dart does not compile this Kotlin/Compose app**. Avoid rerunning 35/11 old Dart tests as a substitute for Kotlin tests.

## Stages and status (no capability claims before evidence)

1. Native Gradle + Android Compose editor source: **WRITTEN**, pending compile/phone UI verification.
2. Port core domain behavior with approved parity: **WRITTEN as Kotlin**, 21 regression checks unexecuted; D-043 DP selector included.
3. Save, load, rename, duplicate and activate named presets offline: **NOT DONE**.
4. Clock with pause/resume/stop, three distinct sounds, screen-lock/background handling: **NOT DONE**.
5. Android real-device acceptance and APK release: **NOT DONE**. Future KMP/Compose multiplatform migration comes *after* Android meets product acceptance.

No runtime tests, APK builds, paid GitHub Actions/CI, branch merges or desktop requirement changes happened during the authoring of this source. Historic Flutter and Dart branches remain intact and are not included as executable dependencies in this Android Gradle project.

## External toolchain source

[Code on the Go releases (26.43)](https://github.com/appdevforall/CodeOnTheGo/releases), [CoGo on-device AGP 9.3.1 / Kotlin 2.3.21 / Gradle 9.6.1 documentation](https://github.com/appdevforall/CodeOnTheGo/blob/stage/docs/PLUGIN_AUTHORING.md), [Google AGP 9 built-in Kotlin](https://developer.android.com/build/migrate-to-built-in-kotlin), [Google official Compose compiler setup](https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler), [Compose BOM](https://developer.android.com/develop/ui/compose/bom).


## 2026-10-23 — K03 offline saved session increment (source only, NOT device-verified)

**Added to this real Android project:**
- `domain/Presets.kt`: platform-neutral `PresetStore` interface and `PresetCatalog` with save approved / update / load / duplicate / rename / delete / activate-as-selected; rejects invalid, unresolved and unacknowledged warning schedules. Saved approval is revalidated against the scheduling engine whenever loaded; a full-revision length-prefixed consistency signature covers **all** rules (including inactive settings), final choice, phase boundaries and warning offsets. This is consistency detection, **not a cryptographic security signature**.
- `android/AndroidPresetStore.kt`: a versioned offline JSON persistence adapter using one committed private `SharedPreferences` value for the library and selected ID. Corrupt/unrecognized storage is **not replaced** with empty data. A configurable test-only preferences namespace avoids touching real user presets.
- `android/PresetLibrarySection.kt` and updated `SessionEditorScreen.kt`: Compose UI to save an explicitly approved plan, update, load, duplicate, rename, confirm deletion, and select the plan for **future** timer activation. Editing still clears the prior approval. New names are nonblank. Saved sessions can be loaded after restart.
- Input parser now uses checked `Long` multiplication to reject minute-to-second overflow instead of silently wrapping.

**Tests added:** 10 pure Kotlin JUnit preset lifecycle/integrity cases, 1 overflow JUnit case, **six** Android instrumentation cases for persistence/restart, CRUD, unreadable data and future storage schema. All written, **NONE RUN**. Total now **32 JVM `@Test` methods** (16 scheduler + 6 input + 10 preset) and **6 Android instrumentation tests**. Original Dart 35+11 passing evidence does not verify these Kotlin tests.

**Scope:** K03 code authored but no actual CoGo compiler, Gradle tasks, APK install, SQLite/DataStore migration, phone storage restart test, or Android emulator run. The K03 milestone is **NOT COMPLETE until real device checks pass**. Loading/activating selects an approved session; it does NOT start a countdown or play sound. K04 timer/3 audio cues remain unimplemented.

**Ordinary local on-phone QA tasks in Code on the Go:**
```text
:app:testDebugUnitTest
:app:assembleDebug
:app:connectedDebugAndroidTest  (when an instrumentation device is available)
```

Open the `android-native/` folder on branch `android-native-kotlin-compose`; no throwaway project, no GitHub Actions, no paid remote builds.
