# Atelier Time

> **DOCUMENTATION MAP:** [Branch roles, documentation structure and current handoff](docs/DOCUMENTATION_INDEX_AND_BRANCH_POLICY.md). `main` is the documentation/historical entrypoint. Android production code is in `android-native-kotlin-compose`; Stable 9 is frozen. Read the compact [current status](docs/CURRENT_STATUS_AND_HANDOFF_STABLE9_2026_10_28.md) before older dated records.


> **CURRENT STATUS — 2026-10-28: Stable 9 / D-126 (verified).** User-accepted native Android Kotlin/Compose Timer with unified scheduling and per-Run JSONL diagnostics. Samsung: **243/243 JVM + 42/42 Android instrumentation = 285/285 automated PASS**, 72 Gradle tasks PASS, both APK SHA-256 checks OK; user confirmed all Saved Classes and Sounds survived testing. The installed app is still D-126/versionCode20 (no rebuild for QA9). Current stable checkpoint: [stable/android-d126-qa9-tests-complete](https://github.com/AviY99/Atelier-Time/tree/stable/android-d126-qa9-tests-complete); production source remains on [android-native-kotlin-compose](https://github.com/AviY99/Atelier-Time/tree/android-native-kotlin-compose), **not merged into main**. See [authoritative status and handoff](docs/CURRENT_STATUS_AND_HANDOFF_STABLE9_2026_10_28.md), [main handoff](docs/HANDOFF.md), [release QA certificate](docs/STABLE9_D126_285_AUTOMATED_QA_ACCEPTANCE_2026_10_28.md). All older “current” paragraphs below are historical.


> **D-089 / 2026-10-24 ACTIVE STATUS:** User confirms installed **D-088 fully working** ("הכל עובד"), superseding earlier D-088 APK/UI pending statements; real prior phone Gradle `BUILD SUCCESSFUL in 38s` remains recorded. New native Kotlin UI code: fixed **non-scrollable, single-row 5-way LTR TabRow** in exact visual order **Editor | Timer | Sound | Design | About** (internal SOUNDS enum, display label Sound, compact 11sp, `LocalLayoutDirection.Ltr` even for Hebrew devices), replacing horizontally swiped `ScrollableTabRow`. **About now English-only**, displays exactly `Atlier Time`, **numeric only APK version derived from installed PackageManager metadata** (for `0.1.0-dev` display `0.1.0` without changing app build version), `© 2026`, `Developed by Avi Yosfan with assistance from Open AI`, and brief English class scheduling/timer summary. No timer, saved class, sound, or Day palette changes. User finds currently implemented Night too dark; **lighter Night Soft proposal is NOT APPROVED and NOT APPLIED**: background #1B2636, surface #29384B, raised #35465B, text #F2F5FA, secondary #D0D9E5, blue #8AB8F0, lavender #B9A8EA, border #60748C; tahini #D5BD94 as before. **D-089 source not yet phone Gradle compiled/JVM tested/assembled/installed/device QA-accepted.** When user approves Night Soft, implement in a separate subsequent increment; not part of current D-089 diff. Frozen stable/android-d086 stays exactly at d0a03d2; accepted D-088 remains current installed working phone baseline; PR #3 still OPEN/DRAFT/unmerged, no paid Actions.

> **CURRENT 2026-10-24 D-088 — DESIGN SOURCE IMPLEMENTED; NEW PHONE BUILD/QA PENDING.** User approved Day (A Studio Linen), Night (B Night Focus), Flow-inspired rounded modern type; no font/digit motion or blink. Active Session's timer-card **solid tahini fill grows** and bar grows, Break's same-colored **fill drains** and bar drains, no gradient. New Design tab with Day/Night/Auto; Auto Night threshold chosen by hour slider 12–23, return to Day 06:00; offline SharedPreferences separate from saved Class catalog and live switching using device-local time. New About tab uses exact requested “Atlier Time”, installed version metadata (currently `0.1.0-dev`), © 2026, “פותח ע״י Avi Yosfan בסיוע Open AI” and short Hebrew functional description. Five scrollable tabs. Pure Kotlin new DesignClock/DesignMotion with **7 new written JVM test methods** (NOT executed). Feature code in `android-native-kotlin-compose`, user-approved stable branch `stable/android-d086` frozen unchanged at `d0a03d2`. **D-088 NOT yet Gradle built, tested, update-installed, UI verified**, no exact JUnit execution evidence. See [D-088 implementation & phone QA](D088_DESIGN_IMPLEMENTATION_AND_QA.md), [D-088 decision](DECISIONS.md), [stable baseline](ANDROID_STABLE_BASELINE_D086_2026_10_24.md). No paid Actions; PR #3 Draft/unmerged.

> **CURRENT AUTHORITATIVE CHECKPOINT — 2026-10-24 / D-087:** User reported D-086 fully working on Android phone ("הכל עובד"), following screenshot-confirmed `:app:testDebugUnitTest :app:assembleDebug` BUILD SUCCESSFUL in 36s (42 tasks: 7 executed, 35 up-to-date). Declared **Atelier Time Android Stable 1 — D-086** (user-accepted installed functional baseline; not signed production or store release). Frozen source checkpoint branch `stable/android-d086` points to **`d0a03d2792cc324aeeba39b6327136ba6d37fd0c`**; current Android dev branch / open Draft PR #3 remain unmerged and should not be modified for accepted stable source. No CI/actions, release tag, binary publication or app `versionName` change. **Previously pending D-086 install/phone visual-acceptance statements below are historical and superseded by this newer direct user report.** Stable baseline docs: [ANDROID_STABLE_BASELINE_D086_2026_10_24.md](ANDROID_STABLE_BASELINE_D086_2026_10_24.md). Outstanding: 9 written Android instrumentation methods not executed on device; background/lock/doze audio reliability; lifecycle/time edge cases; accessibility, motion/contrast, performance/battery; signed release/distribution; future iOS/desktop multiplatform efforts; older D-015/D-016 wording only if change proposed. **Design exploration is authorized, but no visual concept has been approved or implemented:** three styles and animation plans in [VISUAL_DESIGN_CONCEPTS_2026_10_24.md](VISUAL_DESIGN_CONCEPTS_2026_10_24.md), with safe nonflashing/reduced-motion fallback. Keep accepted functions/domain/stored classes unchanged.

> **CURRENT 2026-10-23 D-070 K03 IMPLEMENTATION PROGRESS:** Native Kotlin [android-native/](https://github.com/AviY99/Atelier-Time/tree/android-native-kotlin-compose/android-native) source now includes **offline approved preset saving and management** (load/save/update/rename/duplicate/delete/activate selection), versioned private phone storage and verified-on-load rule/timeline/warning integrity, alongside Compose editor and deterministic scheduler. **32 Kotlin JVM and 4 Android instrumentation test cases WRITTEN, NOT EXECUTED.** No compiled Android APK/device acceptance yet, so do not claim K03 passed. Running timer/3 audio cues K04 not implemented. [Latest cross-chat handoff v3.3](docs/HANDOFF.md) · [native QA gates](docs/ANDROID_NATIVE_EXECUTION_AND_TEST.md) · [draft PR #3](https://github.com/AviY99/Atelier-Time/pull/3). Product rules unchanged; no paid CI/merges.


> **CURRENT 2026-10-23 D-068/D-069 — REQUIREMENTS UNCHANGED, REAL ANDROID KOTLIN IMPLEMENTATION STARTED:** User authorized continuing with Kotlin+Jetpack Compose+Gradle on Android phone and confirmed all previously approved Atelier Time scheduling/product behavior should remain unchanged. [New native Android project](https://github.com/AviY99/Atelier-Time/tree/android-native-kotlin-compose/android-native) in isolated branch/draft [PR #3](https://github.com/AviY99/Atelier-Time/pull/3) has actual deterministic pure Kotlin scheduling core, Compose editor and **21 Kotlin JUnit test methods WRITTEN, NOT EXECUTED**. No APK compiled/installed yet; no preset storage, timer or sound code. New [developer workflow, acceptance tests K01–K06 and frozen product requirements](docs/ANDROID_NATIVE_EXECUTION_AND_TEST.md) supersede historical Flutter/Dart-Web plans below. Existing 35/35 scheduling and 11/11 Dart adapter tests remain separately verified. No paid GitHub Actions or merges.


> **LATEST STACK DECISION D-067 (2026-10-23):** Android FIRST **Kotlin + Jetpack Compose + Gradle** as the industry-standard native Android code stack; develop/build the actual Android application on the phone using maintained **Code on the Go** (on-phone IDE, not the standard desktop Android Studio). Future portability planned with Kotlin Multiplatform / Compose Multiplatform for iOS/Windows/macOS. **User rejects disposable feasibility spikes**; the Dart-to-JS/Capacitor experimental branch and prior toy Kotlin APK gate are **historical, not current requirements**. Preserve prior 35+11 Dart regression test successes and Dart/Flutter branches for port parity. No Kotlin APK compiled yet; no paid GitHub Actions.


> **2026-10-23 — D-064 SHORTLIST CORRECTED:** **Two unproven Android-phone candidates** now tracked: (A) keep 35+11 test-verified Dart business logic and prove a small Dart-JS browser app / later Termux Capacitor native APK; (B) build Kotlin Android directly using maintained Code on the Go, then explore Kotlin Multiplatform, accepting a substantial Dart-to-Kotlin port. **No APK has been built on this phone and no replacement stack has been selected.** Test first with the smallest isolated `spike-android-dart-web` proof, then verify an actual installed native APK and locked-screen alerts before full UI migration. See [D-064](docs/DECISIONS.md) and [spike README](https://github.com/AviY99/Atelier-Time/blob/spike-android-dart-web/spikes/android_dart_web/README.md).


> **2026-10-23 — LATEST REQUIREMENT D-062:** **Build Android app on user's Android phone first**, with a fast on-phone development/test/preview/build loop and efficient later iOS/Windows/macOS ports. D-061's universal day-one local cross-OS build requirement is superseded; original Flutter SDK on Termux is still unverified and its source is preserved/frozen. Preferred **unproven** feasibility candidate is **existing Dart scheduling engine → Dart-to-JavaScript browser UI → Capacitor-generated native Android APK in Termux**; later OS wrappers reuse frontend/core, with OS-specific signing/alert adapters. A tiny spike is on [spike-android-dart-web](https://github.com/AviY99/Atelier-Time/tree/spike-android-dart-web), and [acceptance gates](docs/ANDROID_FIRST_PLATFORM_SPIKE.md) forbid claiming APK or background reliability until real testing. No GitHub Actions/paid workflows. Stage 1 35/35 and Stage 2 adapter 11/11 verified.


> **CURRENT APPROVED REQUIREMENT (D-062, 2026-10-23):** Android FIRST, **develop/debug/test/build/install an APK directly on the user's Android ARM64 phone efficiently**. Preserve efficient LATER portability to iOS/iPadOS, Windows, macOS; those are no longer first-release blockers. [Android-first development plan](docs/ANDROID_FIRST_DEVELOPMENT_PLAN.md) identifies **Kotlin + actively maintained Code on the Go** as a preferred **candidate pending one real on-device APK proof** (D-063), then Kotlin/Compose Multiplatform as a later port option. NO migration/installed APK/verified CoGo build yet. Keep existing **35/35 Dart scheduling and 11/11 Dart editor adapter** test evidence; Flutter draft source remains uncompiled, PRs unmerged, no paid GitHub Actions. Older platform status sections below are historical only.


> **2026-10-23 — D-061 PLATFORM DECISION REOPENED:** The user requires cross-platform compatibility **from development and automated testing through native builds/signing, installation, and live operation**, not just a shared Flutter runtime on four targets. The original Flutter recommendation did NOT verify Android Termux as a supported developer host or establish viable Apple/Windows/macOS builders. **Pause further Stage 2 Flutter-specific coding and PR merges.** See [full documented feasibility assessment](docs/PLATFORM_REASSESSMENT_2026_10_23.md) and [updated handoff](docs/HANDOFF.md). No PWA/native-wrapper replacement has been approved. Previously verified 35/35 core Dart and 11/11 Stage 2 form-adapter test evidence is preserved; eight Flutter widget tests have never run. No GitHub Actions/paid builds.


A planned offline-capable, installable posing/break timer for live-model **drawing and sculpting** ateliers.

**Project status (2026-10-23, D-052–D-054):** **Stage 1 Step 1.4 COMPLETE on tested branch** — Android Termux Dart 3.13.7 screenshot verified **35/35 Dart unit tests PASS**; prior smoke test and analyzer passed. [Stage 1 integration PR #1](https://github.com/AviY99/Atelier-Time/pull/1) is OPEN, **not merged**. At the user's instruction to **“finish documentation and continue”**, **Stage 2 session editor/preview started**: [implementation plan](docs/STAGE_2_PLAN.md), [uncompiled Flutter prototype branch](https://github.com/AviY99/Atelier-Time/tree/stage-2-session-editor), [draft PR #2](https://github.com/AviY99/Atelier-Time/pull/2). **Flutter source is NOT tested/built**, no APK, saved presets, live timer/audio, GitHub Actions or paid builds. No later stages authorized.

Atelier Time lets an organizer configure pose and break rules, generate and approve a chronological schedule, save reusable presets, and run a timer with three distinct sound cues. If the last pose would be incomplete, schedule review offers: (1) keep the remaining short final pose; (2) split the time of the last two poses as evenly as possible; or (3) adjust their split with a ruler/slider.

## Documentation

- [Product requirements](docs/PRODUCT_SPEC.md)
- [Schedule model and algorithms](docs/SCHEDULING_RULES.md)
- [Complete 3-hour example schedules](docs/EXAMPLES.md)
- [Delivery stages and approval gates](docs/ROADMAP.md)
- [Stage 2 editor/preview development plan](docs/STAGE_2_PLAN.md)
- [Decision register](docs/DECISIONS.md)
- [Design and technical approach](docs/ARCHITECTURE.md)
- [Step 1.2 — Dart data-model design](docs/DATA_MODEL.md)
- [Step 1.3 — approved scheduling placement and edge-case policies](docs/SCHEDULING_EDGE_POLICY.md)
- [Step 1.3 — approved decision record](docs/STEP_1_3_REVIEW.md)
- [Approved framework and platform decision](docs/TECH_DECISION_001_PLATFORM.md)
- [Acceptance and test plan](docs/TEST_PLAN.md)
- [Iteration record](docs/WORK_LOG.md)
- [Current handoff to a new chat](docs/HANDOFF.md)
- [Change history](CHANGELOG.md)

## Important project boundaries

- Existing GitHub repository used throughout. Tested pure Dart scheduling engine and a separate **source-only Flutter editor prototype** now exist on isolated branches; neither is merged to `main`.
- **Approved:** One Flutter + Dart codebase for Android, Windows, iOS/iPadOS and macOS in the first release; platform-specific compiled packages. Native OS menus/graphics are optional.
- **Latest approved D-036:** A full session or a configured normal/active special break of **0 minutes is invalid** (blocking error). Positive 1–4-minute breaks remain allowed; **D-031 warning and explicit user acknowledgment** apply to a mathematically legal schedule exceeding the 25-minute posing / 5-minute rest recommendation. Regular/special defaults remain editable positive 5m/15m. Logical 0-minute break-slot proposals were withdrawn.
- **Approved Step 1.3:** Automatic midpoint/earlier-tie replacement, evenly spaced multiple long breaks, manual positioning, whole-timeline validations/approvals, and 25/5 warning counter **within one full activated session** (no automatic carryover between distinct saved presets).
- **D-038 resolved by user-approved D-044:** Any **positive** special break may replace a normal break, even when shorter or equal (for example normal 5m, special 3m). The engine permits it and applies the approved 25/5 rest warning/acknowledgment. **Zero-minute configured breaks and total sessions remain invalid (D-036).** Dart core code and all **35 unit tests were executed successfully on Android Termux** (D-052).
- **Step 1.4 VERIFIED/COMPLETE** on the isolated core branch, 35/35 Android Dart tests passing. **Stage 2 STARTED** with a neutral Flutter editor source; it remains **uncompiled/unbuilt**, and visual design is provisional. Stages 3–6, distribution and production branding remain unapproved. No paid GitHub Actions.
- Every number in both timing examples is configurable through a future per-session input screen; pose groups can be added/removed.
- “Summary” is a named final reserved time block; its specific activity has not been further defined.
- Do not silently convert open technical assumptions into approved product rules.
- Every iteration must update the decision log, work log, change log and handoff with evidence and next actions.

## Step 1.4 — budget-free Dart verification

The development branch [stage-1-4-scheduling-core](https://github.com/AviY99/Atelier-Time/tree/stage-1-4-scheduling-core) now contains a safer immutable schedule approval API and local verification helpers:
- Windows: [tool/verify.ps1](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/tool/verify.ps1).
- macOS/Linux: [tool/verify.sh](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/tool/verify.sh).
- Each helper calls the pure Dart smoke test, `dart pub get`, `dart analyze` and `dart test`, writing `atelier-time-verification.log`.

**Not yet executed:** The assistant's local runtime does not contain Dart/Flutter, and DNS fails for SDK download hosts; no Github Actions or paid runners were used. To run the tests on your computer, install only the [official Dart SDK](https://dart.dev/get-dart), download/clone the development branch, run the appropriate helper and share the generated log. The SDK installed on a user's PC is not automatically available in ChatGPT's separate environment. No Flutter interface or Stage 2 work has been authorized.

### Android Dart verification (2026-10-23)

The user installed **Dart 3.13.7 on Android ARM64 via Termux**, successfully ran `dart tool/smoke.dart` after the warning-offset compile fix, and later reported **“All tests passed!”** after the requested `dart pub get && dart analyze && dart test` command chain. This is **user-reported test success**, not an attached test log or independently observed analyzer trace. The test source currently defines **34 Dart unit tests**. No Android APK, Flutter app, cross-platform build or later-stage screen exists. Stage 1 Step 1.4 **remains in final acceptance audit**, on the isolated branch only.


**Latest verification:** Actual Android Termux screenshots confirm `dart analyze` **No issues found!** and `dart test` **34 passing**. No separate APK, Flutter application or non-Android build has been produced. Final Step 1.4 QA coverage review still needs the valid 1m+34m custom-split endpoint before formal completion.


**Acceptance coverage checkpoint D-051:** A 35th unit test for the missing valid 1m+34m final-pose split was added on the development branch after the screenshots. **34 tests have screenshot-confirmed PASS; the new 35th test has NOT run.** To retest in existing Android Termux clone: `git pull --ff-only && dart test`.

**Final Step 1.4 verification (D-052):** Screenshot-confirmed `00:00 +35: All tests passed!` on Android Termux after branch update `8cf7a24..907f871`. Existing 34-test analyzer screenshot reported no issues; updated test-only version was not re-analyzed in supplied evidence. **Step 1.4 core complete on branch, but not merged.** Future changes require separate user approval.


## Stage 2 pure Dart editor-input tests (D-056; 2026-10-23)

The [Stage 2 branch](https://github.com/AviY99/Atelier-Time/tree/stage-2-session-editor) now includes a Flutter-independent [text-to-schedule rules adapter](https://github.com/AviY99/Atelier-Time/blob/stage-2-session-editor/lib/src/editor_draft.dart) used by the Flutter editor. This enables [11 new form-input regression tests](https://github.com/AviY99/Atelier-Time/blob/stage-2-session-editor/test/editor_draft_test.dart) to be run with the existing Android Termux Dart SDK. **These 11 tests have not run yet.** The scheduling engine remains unchanged; its Stage 1 screenshot-verified 35 passing tests are a separate result. The eight Flutter widget tests also remain unexecuted because there is no Flutter SDK.

From a Stage 2 branch checkout, a free targeted command is `dart test test/editor_draft_test.dart`. No GitHub Actions, paid runner or app build is needed for that test.

**D-059 verification update (2026-10-23):** The user's clean Android Termux `~/Atelier-Time-Stage2` worktree at exact GitHub branch HEAD `3bb2bad` completed **11/11 pure-Dart editor-adapter tests PASS**, screenshot-confirmed. This supersedes the earlier “11 tests have not run” wording above. A combined `dart analyze && dart test` for Stage 2 is still outstanding, and all eight Flutter widget tests remain unexecuted pending Flutter SDK. No APK/build or paid workflow.


## Dart SDK vs Flutter SDK — Stage 2 analyzer fix (D-060)

The root package is **pure Dart**; `apps/atelier_time_flutter/` is a separate package that requires a **Flutter SDK**. Termux currently has bare Dart only. Running root `dart analyze` before the package boundaries were configured caused large numbers of Flutter SDK diagnostic errors; the chained `dart test` therefore did NOT execute.

The Stage 2 development branch now has root `analysis_options.yaml` excluding the nested Flutter package from bare-Dart analysis and a separate `apps/atelier_time_flutter/analysis_options.yaml` for proper later Flutter toolchain verification. **No actual new post-fix analyzer or 46-test pass has been observed yet**. Android Termux rerun after updating the Stage 2 worktree: `dart analyze && dart test`. For Flutter code, eventually run `flutter analyze && flutter test` inside the nested app on a Flutter-capable machine. This does not change approved scheduling logic or imply an installable APK.
