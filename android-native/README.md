# Atelier Time — Android native Kotlin / Jetpack Compose

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
