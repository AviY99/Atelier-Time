# ATELIER TIME — Hand-off for the next ChatGPT conversation

**Date:** 2026-10-23 (Asia/Jerusalem). **Handoff status:** AUTHORITATIVE SNAPSHOT / SOURCE-ONLY ANDROID DEVELOPMENT IN PROGRESS.
**Repository:** https://github.com/AviY99/Atelier-Time  
**Default `main`:** authoritative product decisions, specification, test plan and cross-chat history.  
**Current implementation branch:** [`android-native-kotlin-compose`](https://github.com/AviY99/Atelier-Time/tree/android-native-kotlin-compose).  
**Current code review:** [draft PR #3](https://github.com/AviY99/Atelier-Time/pull/3) (head Android native branch, based on Stage 2 Dart branch; **NOT MERGED**).

## START HERE — literal continuation instructions

The user requested that the conversation end and an independent handoff be prepared **from real GitHub project documents**. In the next conversation, read **THIS FILE FIRST** plus [docs/HANDOFF.md](HANDOFF.md), [docs/DECISIONS.md](DECISIONS.md), [docs/ANDROID_NATIVE_EXECUTION_AND_TEST.md](ANDROID_NATIVE_EXECUTION_AND_TEST.md), [docs/PRODUCT_SPEC.md](PRODUCT_SPEC.md), [docs/EXAMPLES.md](EXAMPLES.md), [docs/SCHEDULING_RULES.md](SCHEDULING_RULES.md), [docs/SCHEDULING_EDGE_POLICY.md](SCHEDULING_EDGE_POLICY.md), [docs/TEST_PLAN.md](TEST_PLAN.md) and the implementation-branch [Android README](https://github.com/AviY99/Atelier-Time/blob/android-native-kotlin-compose/android-native/README.md). These files together are the authoritative contract; older contradictory Flutter/experiment paragraphs in historical documents are superseded by D-062/D-067/D-068/D-070.

**DO NOT reopen the platform decision, request approval again, invent toy proof-of-concept apps, run paid GitHub Actions, merge PRs automatically, reset/delete prior user files or claim compilation/tests passed without actual logs.** User values real executable progress, tight reporting, no repeat work/questions and no time wasted.

## 1. Approved project/technology

- Atelier Time: **offline-installed Android-first workshop pose/break scheduler** for live drawing and sculpting. Product requirements and all approved timing/safety rules were **explicitly confirmed unchanged** (D-068).
- Platform priority **D-062**: edit, test, build, install and debug **on the user's Android ARM64 phone** quickly and efficiently; later adapt to **iOS/iPadOS, Windows and macOS** without forcing simultaneous releases.
- Selected development stack **D-067**: native **Kotlin + Jetpack Compose + Gradle/Android Gradle Plugin**. Use maintained **Code on the Go** as on-phone IDE (a specialized solution; desktop Android Studio is the official industry-standard IDE). Keep scheduling/domain Kotlin **free of Android APIs** for later Kotlin Multiplatform; Compose Multiplatform may reduce future UI porting.
- Do not reinstate Flutter as active app development. Old Flutter UI/draft PR #2 is historical, uncompiled. Do not choose PWA or Dart/JS spike.
- No paid remote/cloud builders or GitHub Actions. User's GitHub Actions budget has been blocked before.

## 2. Product invariants — NEVER silently change

- Editable named total session, reserved optional summary, regular pose length, normal and special break duration/count, ordered opening pose groups, pose-first/break-first cycle and manual/automatic special-break ordinal placement.
- **D-031:** after >25 minutes cumulative posing without >=5-minute qualifying rest, show visible **advisory** and require **separate explicit acknowledgment** before approving; 1–4-minute valid breaks **do not reset** posing accumulation.
- **D-036:** full session duration and configured pose/break lengths must be >0; optional reserved summary and special-break count may be 0. No silently repaired invalid inputs.
- **D-037 / D-043:** deterministic, exact contiguous schedule including summary inside total, no phantom terminal breaks, replacement of existing normal breaks (never extra appended break), manual or automatic deterministic placement; efficient exact DP handles multiple breaks and nonstandard durations.
- **D-044:** special break may be **any strictly positive length**, including shorter than, equal to or longer than normal; warning if short rest fails D-031.
- **Three final-pose choices:** keep remainder; even split across two eligible regular poses with existing separating break; custom whole-minute split (each >=1m), no invented rest. Rule edits invalidate preview acknowledgment/approval.
- Canonical **Example A and B** both 180m with 15m reserved final summary and exactly approved phase boundaries; see [EXAMPLES](EXAMPLES.md) rather than inventing examples.
- Must eventually support persistent reusable presets (save, list, load, edit, duplicate, rename, delete, activate), accurate monotonic pause/resume/stop timer, **three distinct audio cues**, offline operation, background/locked-screen behavior and real Android installation.
- **D-015** exact interpretation/label for reserved summary and **D-016** audio cue granularity (pose vs opening group) are **still open**; do not pretend they have been approved. Final branding/logo also not frozen.

## 3. Source and actual QA evidence as of this handoff

### Verified historical assets (NOT new Kotlin evidence)

- Stage 1 pure Dart scheduling engine on `stage-1-4-scheduling-core`: **35/35 TESTS PASSED on Android Termux Dart 3.13.7**, screenshot-confirmed; earlier analyzer/smoke success.
- Stage 2 pure Dart editor-form adapter tests on `stage-2-session-editor`: **11/11 PASSED** in clean `~/Atelier-Time-Stage2` Git worktree at commit `3bb2bad` (verified against remote branch head at that time). These 46 past Dart tests are regression **oracles** and do **NOT prove the Kotlin port compiles**.
- Existing older `~/Atelier-Time` Termux checkout had staged/untracked files from a failed switch; separate `~/Atelier-Time-Stage2` worktree was created. **Do not reset/clean either location without deliberate inventory/approval**.

### Current active Kotlin application source (D-069 + D-070)

Gradle project root **`android-native/`** within [`android-native-kotlin-compose`](https://github.com/AviY99/Atelier-Time/tree/android-native-kotlin-compose/android-native); do NOT open repository Dart root as Gradle project.

- `app/src/main/java/com/ateliertime/domain/Models.kt`, `ScheduleEngine.kt`: platform-independent Kotlin port of core scheduling with D-043 automatic DP, final choices, errors and warning/approval.
- `app/src/main/java/com/ateliertime/android/MainActivity.kt`, `EditorInput.kt`, `SessionEditorScreen.kt`: Jetpack Compose editable editor, approved A/B examples, input conversion, full phase preview, final choice and explicit D-031 acknowledgment/approval. Input includes checked minute*60 overflow.
- **K03 source (new latest stage, D-070):** `domain/Presets.kt` (`PresetStore`, `PresetCatalog`, approved-only save/update/load/rename/duplicate/delete/activate-as-selection; re-generate exact timeline and consistency signature); `android/AndroidPresetStore.kt` (private versioned JSON via SharedPreferences one atomic synchronous commit; preserve malformed/unsupported records, separate test namespace); `android/PresetLibrarySection.kt` (Compose saved-list actions, delete confirmation, connected to SessionEditorScreen). **Activation means selecting a saved approved plan, NOT running a timer.**
- **Written Kotlin tests: 32 JVM JUnit methods** (16 scheduler + 6 editor input + 10 preset catalog) and **4 Android instrumentation tests** for persistence/corruption isolation. **ALL NOT EXECUTED**. They are not passed tests.
- **No confirmed Kotlin/Compose compilation or Gradle build; NO APK; NO on-phone install/run; K01/K02/K03 remain UNVERIFIED.** There is **no** runtime countdown, pause/resume/stop, background/locked-screen implementation or three audio cues yet — **K04 NOT IMPLEMENTED**.
- No installed/working Code on the Go on user's exact device has been established in conversation; don't infer it from Termux Dart success.
- `android-native` pins AGP 9.3.1, Kotlin/Compose compiler plugin 2.3.21, Java 17; project expects compatible Gradle from CoGo. Check actual IDE/build errors rather than assuming complete compatibility. There is no checked-in Gradle wrapper JAR.
- Earlier Flutter Stage 2 had eight UNRUN widget tests and old draft PR #2. Current Android draft PR #3 is **NOT MERGED**. No cloud builds or expenses.

## 4. Next action — standard Android development QA, then continue product

**Do not write more documents as a substitute for build verification.** Once user resumes work and has CoGo access, use its normal Git/open-project workflow for branch `android-native-kotlin-compose`, open **`android-native/`** Gradle root, and execute:

1. `:app:testDebugUnitTest` — compile and run the 32 real Kotlin JVM JUnit methods, fix any errors.
2. `:app:assembleDebug` — build **real Android debug APK**, install/open on Android phone; inspect editor, A/B exact timeline, warnings/final choices, saved session CRUD.
3. `:app:connectedDebugAndroidTest` when on-device instrumentation is available — run 4 isolated storage tests; manually verify save/reopen/restore, offline mode, corruption/no unexpected overwrite and selected preset behavior.
4. Record exact Git commit, task/log output and passed/failed tests, APK path and actual phone behavior in [TEST_PLAN](TEST_PLAN.md), [WORK_LOG](WORK_LOG.md) and [HANDOFF](HANDOFF.md). **Do NOT call it a working APK or passed tests until evidence.**
5. After K01–K03 are actually verified, continue **K04** real monotonic timer with start/pause/resume/stop and 3 distinct cues, validate Android background/lock restrictions and resolve D-016 only through explicit user decision if the choice becomes material; do not alter the approved schedule contract.

If a new chat asks simply “continue”, **ACT on the current Android project**, not old Flutter or platform debates. No extra approval needed for existing approved scope. User explicitly rejected fake experiments, throwaway toy builds, re-running historical Dart suites and long speculative plans.

## 5. Critical source links and decision priority

1. [Current decision register](DECISIONS.md): **D-070** K03 written/unverified; **D-068** all requirements unchanged; **D-067** Kotlin/Compose/Gradle; **D-062** Android-on-phone first; **D-031/036/037/043/044** scheduling invariants.
2. [Canonical cross-chat project history](HANDOFF.md), [product specification](PRODUCT_SPEC.md), [reference schedules](EXAMPLES.md), [edge policies](SCHEDULING_EDGE_POLICY.md).
3. [Android implementation and test gates K01–K06](ANDROID_NATIVE_EXECUTION_AND_TEST.md); [roadmap](ROADMAP.md), [test plan](TEST_PLAN.md), [work log](WORK_LOG.md).
4. [Current native branch code](https://github.com/AviY99/Atelier-Time/tree/android-native-kotlin-compose/android-native), [draft PR #3](https://github.com/AviY99/Atelier-Time/pull/3). Prior PR #1 (verified Dart core) and PR #2 (uncompiled Flutter editor) remain historic/unmerged unless source state changes; check live PR state before taking action.

**Handoff ownership:** This document reports existing repository evidence only. This handoff does NOT itself approve a new functionality change, compiler result, APK, PR merge or paid service. **No code was changed solely to prepare this handoff.**
