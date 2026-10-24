# Delivery roadmap and approval gates

> **CURRENT AUTHORITATIVE CHECKPOINT — 2026-10-24 / D-087:** User reported D-086 fully working on Android phone ("הכל עובד"), following screenshot-confirmed `:app:testDebugUnitTest :app:assembleDebug` BUILD SUCCESSFUL in 36s (42 tasks: 7 executed, 35 up-to-date). Declared **Atelier Time Android Stable 1 — D-086** (user-accepted installed functional baseline; not signed production or store release). Frozen source checkpoint branch `stable/android-d086` points to **`d0a03d2792cc324aeeba39b6327136ba6d37fd0c`**; current Android dev branch / open Draft PR #3 remain unmerged and should not be modified for accepted stable source. No CI/actions, release tag, binary publication or app `versionName` change. **Previously pending D-086 install/phone visual-acceptance statements below are historical and superseded by this newer direct user report.** Stable baseline docs: [ANDROID_STABLE_BASELINE_D086_2026_10_24.md](ANDROID_STABLE_BASELINE_D086_2026_10_24.md). Outstanding: 9 written Android instrumentation methods not executed on device; background/lock/doze audio reliability; lifecycle/time edge cases; accessibility, motion/contrast, performance/battery; signed release/distribution; future iOS/desktop multiplatform efforts; older D-015/D-016 wording only if change proposed. **Design exploration is authorized, but no visual concept has been approved or implemented:** three styles and animation plans in [VISUAL_DESIGN_CONCEPTS_2026_10_24.md](VISUAL_DESIGN_CONCEPTS_2026_10_24.md), with safe nonflashing/reduced-motion fallback. Keep accepted functions/domain/stored classes unchanged.

> **NEXT STAGE SOURCE PROGRESS D-070 — 2026-10-23:** K03 offline approved-session catalog, phone-private versioned JSON storage and Compose saved-session actions implemented in [native Android branch](https://github.com/AviY99/Atelier-Time/tree/android-native-kotlin-compose/android-native); includes 32 Kotlin JVM tests and 4 Android instrumentation tests **written but unexecuted**. Regenerated approved timelines checked for unchanged rules/phase/warning signature, errors preserved. **K03 NOT YET VERIFIED ON DEVICE; NO APK**. K01/K02 also await Gradle/device evidence. K04 accurate countdown, pause/resume/stop and 3 audio cues remain next source stage. Rules/examples unchanged; no Actions, paid CI, PR merges.


> **CURRENT STATUS — 2026-10-23 D-068/D-069:** The user APPROVED continued work on actual Android Kotlin+Jetpack Compose+Gradle application, explicitly confirmed product/scheduling settings unchanged. Isolated [android-native-kotlin-compose](https://github.com/AviY99/Atelier-Time/tree/android-native-kotlin-compose) with [draft PR #3](https://github.com/AviY99/Atelier-Time/pull/3) now contains native Gradle source, platform-independent Kotlin domain/DP scheduler, Android editor and **21 Kotlin tests written, NOT executed**. [New process/QA gates K01–K06](ANDROID_NATIVE_EXECUTION_AND_TEST.md) replaces old Flutter SDK test dependencies. Next: real phone Gradle unit tests/debug APK, then offline persistence/runtime timer/audio. **Do not change original accepted product rules.** Old Flutter/Stage2 items below are historical; current Android milestones are pending device evidence. No paid Actions or automatic merges.


> **LATEST D-064 (2026-10-23) — STANDARD STACK SELECTED:** Kotlin + Jetpack Compose + Gradle Android architecture, with platform-independent Kotlin core to ease later Kotlin Multiplatform/Compose Multiplatform port. Code on the Go is the Android-phone IDE needed for the user's workflow, **not industry-standard IDE** (Android Studio is official standard). User rejected separate toy APK/feasibility experiments. **No toy APK proof gate**: proceed with real Android implementation and its usual build/test/debug process. Prior D-063 gate text below is historical/superseded. No Kotlin app has been built yet; preserve verified Dart 35+11 tests, no paid Actions.


> **2026-10-23 — LATEST SELECTED ANDROID STACK D-064:** User rejected standalone capability tests and required an industry-standard choice. **Kotlin + Jetpack Compose + Gradle Android** is selected for Android-first development; **Code on the Go** is the dedicated phone-local IDE (not standard desktop Android Studio), and Kotlin/Compose Multiplatform is the prospective later shared-code/UI path. **No throwaway feasibility spike; real APK build/testing belongs to normal development.** Preserve verified Dart schedule/adapter baselines, migrate Kotlin behavior with regression parity, keep domain independent of Android framework APIs. No Kotlin APK exists yet. Do not resume Flutter draft UI or auto-merge old PRs; no paid Actions.


> **2026-10-23 — LATEST USER-APPROVED DELIVERY PRIORITY D-062:** Replace D-061's universal day-one native-build requirement with **fast, efficient Android PHONE-ON-DEVICE development → test → preview → installable Android build FIRST**, preserving architecture for later efficient iOS/Windows/macOS port. **Feasibility spike now authorized**: original Dart engine compiled to browser JavaScript, then separate small Capacitor APK build on the phone, then native background/audio proof. All production UI migration waits for proof; no paid Actions or PR merges. See [ANDROID_FIRST_PLATFORM_SPIKE.md](ANDROID_FIRST_PLATFORM_SPIKE.md) and [isolated spike branch](https://github.com/AviY99/Atelier-Time/tree/spike-android-dart-web).


> **2026-10-23 — LATEST USER APPROVED PRIORITY D-062:** Replace immediate four-target release gate with **Android-first development on the phone**, test/debug/build/install Android APK locally and efficiently, while architecting for a later move to iOS, Windows, macOS. **D-063 recommended proof toolchain:** Kotlin with active Code on the Go mobile IDE, plus domain/UI separation for later Kotlin/Compose Multiplatform reuse, ONLY AFTER demonstrated on-device APK build. The former D-061 freeze while requiring all OS toolchains immediately is superseded; **do not resume Flutter feature coding or migrate to Kotlin before feasibility proof**. See [new plan](ANDROID_FIRST_DEVELOPMENT_PLAN.md). No stage beyond the authorized Android-first proof, no paid Actions or release claims.


> **2026-10-23 — D-061 CRITICAL GATE BEFORE ANY FURTHER STAGE 2 CODE:** User requires **full-lifecycle cross-platform development, tests, release builds, signing, installability and runtime**, not just source/runtime portability. The original Flutter platform evaluation did not satisfy the development/build-host requirement and is **UNDER REASSESSMENT**. **Stage 2 platform-specific implementation PAUSED**; no PR merges, GitHub Actions, cloud build spending or additional UI scaffolding until [platform audit](PLATFORM_REASSESSMENT_2026_10_23.md) demonstrates a viable path and user approves it. Stage 1 Dart scheduling 35/35 and Stage 2 Dart adapter 11/11 previous passes remain valid. No platform migration decided.


**Current authoritative status — 2026-10-23 (D-052):** Stage 1 Step 1.4 pure Dart scheduling core and required regression-test gate **VERIFIED / COMPLETE ON ISOLATED BRANCH** `stage-1-4-scheduling-core`, **not yet merged**. Android Termux Dart 3.13.7 screenshots show `dart test` **+35 All tests passed!** after the T09 split endpoint addition; previous screenshot showed `dart analyze` **No issues found!** on the prior 34-test checkout. Earlier smoke also passed. Static analysis was not rerun after the 35th test-only edit. Extended T13/T14 edge-matrix cases are documented as a later QA backlog, not falsely claimed executed. **Stage 2 initial editor/preview work subsequently authorized by user instruction “finish documentation and continue” (D-053). Flutter prototype SOURCE exists on an isolated branch, uncompiled/unbuilt. No GitHub Actions, paid runners, APK or cross-platform app build.**

This is a gated, iterative project. Show a working result and documentation after each implementation stage; obtain approval before moving to the next stage. No implied permission to implement later stages.

| Stage | Name | Planned result | Exit/approval evidence | State |
|---|---|---|---|---|
| 0 | Project documentation and baseline | Existing GitHub repository populated with approved specification, schedule examples, decision register, roadmap, test plan, and handoff | All 11 documentation files committed and retrieved successfully | **COMPLETE** |
| 1 | Technical baseline + scheduling core | Flutter + Dart selected; conceptual data model and deterministic scheduling engine with validation, final-pose options and regressions | Android Termux screenshot-confirmed 35/35 Dart unit tests PASS, earlier smoke PASS, earlier analyzer 0 issues; Step 1.4 code remains on branch | **STEPS 1.1–1.4 COMPLETED/VERIFIED FOR AUTHORIZED DESIGN+CORE SCOPE; branch unmerged; platform builds belong to later stages** |
| 2 | Cross-platform UX and session editor | Responsive per-session input for every numerical rule; complete preview, safety warnings, last-two-pose ruler and approval | Run Flutter analyzer/widget tests and obtain design review once SDK available | **STARTED (D-053/D-054): isolated Flutter editor source, NOT compiled/tested; temporary theme only** |
| 3 | Local preset library | Save, load, edit, copy, rename, delete and activate approved sequences; offline persistence | Demonstrate persistence after app restart and preserve selected final-pose resolution | Not started |
| 4 | Runtime timer and sounds | Reliable playback of scheduled poses/breaks/summary; phase progress, pause/resume/stop, three distinct sounds | Functional timing, sound, pause/interruption checks on target device | Not started |
| 5 | Quality, installation and accessibility | Device-based tests, clock/background edge cases, installation package, usability, reliability | Recorded acceptance tests and user-tested installable build | Not started |
| 6 | Final release and maintenance handoff | Versioned release artifact, installation/use instructions, complete continuation pack | User acceptance and tagged project release | Not started |

## Stage 1 breakdown — approve **each step** separately
1. **Step 1.1 COMPLETE (2026-10-22)**: Flutter + Dart approved with one shared codebase across Android (phone/tablet), Windows, iOS/iPadOS, macOS. Native-looking widgets optional. Details: [TD-001](TECH_DECISION_001_PLATFORM.md). **No code created.**
2. **Step 1.2 DATA MODEL DOCUMENTED**: [DATA_MODEL.md](DATA_MODEL.md) outlines configurable rules, schedule phases, approval states and example input fixtures. User's following “Continue” allowed advancing into Step 1.3, but does not independently finalize every proposed field name.
3. **Step 1.3 COMPLETE / APPROVED (2026-10-22)**: [SCHEDULING_EDGE_POLICY.md](SCHEDULING_EDGE_POLICY.md) and [STEP_1_3_REVIEW.md](STEP_1_3_REVIEW.md) approved by user **D-037**: automatic midpoint/ties, multiple and manual positions, input validation, final pose choices, 25/5 warnings and explicit approval. **D-036 forbids 0m sessions/active breaks**; D-031 preserves advisory warning for valid shorter rests. Approved one-complete-session posing accumulation; no automatic cross-preset carryover. **Only D-038** (positive special break <= normal) remains open.
4. **Step 1.4 VERIFIED / COMPLETE on development branch (2026-10-23, D-052)**: Dart scheduling core and 35 tests on [`stage-1-4-scheduling-core`](https://github.com/AviY99/Atelier-Time/tree/stage-1-4-scheduling-core); user screenshot `00:00 +35: All tests passed!` on Android ARM64 Dart 3.13.7. Earlier `dart analyze`: `No issues found!` for prior 34 tests; not rerun after test-only addition. T01–T12 and T15–T18 acceptance core mapped; T13/T14 extended cases remain later QA. **No merge to `main`, Flutter screens, APK, billable Actions, or Stage 2 authorized.**
5. Show generated tables for Examples A/B, every option, full invariants, and automated-test evidence.
6. Update DECISIONS, TEST_PLAN, WORK_LOG, CHANGELOG, HANDOFF and any affected product docs; request approval for Stage 2.

## Definition of a stage report
A stage report includes: agreed scope, work completed, files/commit IDs, tests run and actual results, deviations or proposed decisions, screenshots/builds when useful, blockers, next-stage options, and a fresh self-contained cross-chat handoff.

## Operating rules
- Do not alter earlier decisions silently; log changes with supersedes links.
- User approves substantive product behavior **and progression to each next sub-step**. The next “Continue” after Step 1.2 authorized documentation/discussion of Step 1.3 only, not automatic acceptance of the proposed edge behavior or any implementation.
- Use GitHub for authoritative text documents/source once created. Keep the handoff at docs/HANDOFF.md updated after each iteration.
- Preserve signed-off examples as regression fixtures.
- Ask for app icon image and discuss graphic design **when branding/design is reached**; do not invent the icon now.
- Never claim a working timer or installable build before implementing and verifying it.

## Step 1.3 consolidated review (2026-10-22)
Following D-036, the [Step 1.3 approved review](STEP_1_3_REVIEW.md) withdraws zero-duration break slots, preserves 25/5 advisory warnings for valid positive rest durations, and **records user approval D-037** for automatic/manual positions and remaining documented policies. Policy for positive special break <= normal was subsequently APPROVED via D-044 (including valid shorter or equal lengths); 0-minute configured breaks remain invalid. **Step 1.4 has subsequently been authorized (D-040) but is still IN PROGRESS and unverified; no later stage authorized.**

## Step 1.4 verification blocker and source hardening (D-046)

Step 1.4 remains **IN PROGRESS** on branch `stage-1-4-scheduling-core`, not yet merged. Further work added private candidate construction to prevent fabricated approved schedules, retained immutable timestamped safety-warning records in approved snapshots, and wrote a new unexecuted regression test. Local `tool/verify.ps1` (Windows) and `tool/verify.sh` (macOS/Linux) provide budget-free Dart smoke/analyzer/test checks and a shareable log.

**Blocking environment:** No Dart or Flutter installed here, and DNS resolution of SDK download hosts fails. Installing the official **Dart SDK** on a test machine is sufficient for Step 1.4; full Flutter/platform toolchains are later-stage needs. No GitHub Actions/workflow or metered build triggered. Do not call Step 1.4 complete without Dart test execution and fixes. No Stage 2 authorization.

## 2026-10-23 — Stage 1 Step 1.4 acceptance checkpoint

Android Termux Dart 3.13.7 user reports smoke success (D-048) and subsequently **“All tests passed!”** after running the requested `dart pub get && dart analyze && dart test` chain (D-049). The branch test file currently defines **34 Dart unit tests**, including both canonical three-hour references, ending splits, replacement-break placement and safety-warning behavior. User supplied no full test log or analyzer transcript; recorded as **user-reported results**, not independently reproduced.

**Stage 1 Step 1.4 remains IN PROGRESS pending coverage audit against the broader T01–T12/T15–T18 acceptance plan, any needed tests, and project completion review.** No GitHub Actions, paid runners, APKs, Flutter UI or later-stage implementation. Work stays on `stage-1-4-scheduling-core`; Stage 2 requires distinct explicit approval.


## Android screenshot-confirmed code check — 2026-10-23 (D-050)

Termux on Android ARM64: `dart pub get` succeeded, `dart analyze` returned **No issues found!**, `dart test` ended **00:01 +34: All tests passed!**. This is screenshot evidence, not merely user narration. The branch scheduling engine is functional under this SDK, but **Step 1.4 remains at the acceptance gate**: the minimum T09 final-split edge test requires both 1-minute endpoints; the current 34 tests explicitly cover only the 34m+1m direction. A small additional test and Android rerun should close that item before declaring Step 1.4 complete. No action on Stage 2, paid GitHub workflows or build distribution.


## Final Stage 1.4 T09 edge-case check committed (D-051)

An acceptance-mapping gap in T09 was closed at the **test-source** level by adding a 35th Dart test for the approved 1m+34m custom last-two-poses endpoint (commit `4b2f5d9`). The first 34 tests and analyzer have screenshot-confirmed PASS (D-050). **The 35th test is not yet executed**, so Step 1.4 remains in final acceptance QA. User can update their existing Termux project with `git pull --ff-only && dart test`. Do not merge or start Stage 2 or billable workflows.

## 2026-10-23 — Stage 1 Step 1.4 required test gate CLOSED (D-052)

- Screenshot shows branch fast-forward `8cf7a24..907f871`, then **`00:00 +35: All tests passed!`** for updated 35-test suite on Android Termux Dart 3.13.7.
- T09 opposite 1m+34m split now tested. Prior `dart analyze` on 34-test checkout returned **No issues found!**; prior smoke also passed. No analyzer re-run reported after 35th test addition.
- Pure Dart scheduling-core Step 1.4 **verified/complete on branch**. Additional optional/extreme T13/T14 cases remain in QA backlog. No claim of full Flutter application or multi-target build.
- **Stop at this gate**: wait for explicit authorization before merging branch or beginning Stage 2. GitHub Actions budget untouched.

## 2026-10-23 — Stage 2 continuation after user instruction (D-053/D-054)

- **Step 1.4 was already marked COMPLETE on tested branch** under D-052 (Android Dart 3.13.7 screenshot 35/35 unit tests PASS; earlier analyzer and smoke PASS). No more user re-testing needed to reproduce that checkpoint.
- Opened [PR #1](https://github.com/AviY99/Atelier-Time/pull/1) for the verified Dart core targeting `main`, **NOT MERGED**. The PR is for review/integration; no paid Actions triggered intentionally.
- User's explicit instruction to **“finish documentation and continue”** authorizes **Stage 2 early editor/preview work** (D-053), without preapproving final styling, storage, timer/audio or deployment.
- Prepared [Stage 2 design/implementation plan](STAGE_2_PLAN.md); created [isolated Stage 2 branch](https://github.com/AviY99/Atelier-Time/tree/stage-2-session-editor) from tested core with a nested Flutter editor prototype and [draft PR #2](https://github.com/AviY99/Atelier-Time/pull/2) targeting the core branch.
- **Flutter editor source is NOT compiled, analyzed or widget-tested** (no accessible Flutter SDK); no Android APK, platform folder builds or installed app. It includes only in-memory approval, not saved presets or a runtime timer. Those remain future stages.
- **Next Stage 2 gate:** review editor interaction against T19–T23, run Flutter tests using a suitable Flutter SDK without paid Actions, revise visual direction with user's input before freezing design. Do not auto-merge PRs or begin Stage 3.


## Stage 2 source hardening and test preparation (D-055, 2026-10-23)

The second Stage 2 increment improved invalid-input feedback, guarded partial previews, made mobile preview scrolling stable, and added **eight Flutter widget test sources** on `stage-2-session-editor` (draft [PR #2](https://github.com/AviY99/Atelier-Time/pull/2)). These test cases have **not run**, because Flutter SDK is unavailable. Existing Android Termux 35/35 *Dart core* success is not a Flutter test. Next prerequisite is a Flutter-capable machine for analyzer/widget runs and then design review. Stage 2 IN PROGRESS; Stages 3–6, releases, automatic merges and paid Actions remain not authorized.


## 2026-10-23 — Stage 2 pure-Dart form parsing (D-056)

The Flutter editor delegates its input parsing to new `lib/src/editor_draft.dart` on `stage-2-session-editor`, preserving all configured minute fields and opening group order and deferring schedule validation to the tested core. The new adapter is testable with Android Termux Dart without Flutter. **11 additional Dart test cases are written but not yet run**; previously verified Stage 1 core remains 35/35 passing. Eight separate Flutter widget tests still await a Flutter SDK. Stage 2 is in progress; no paid builds, PR merges, persisted presets or timer work.

## 2026-10-23 — Stage 2 form-adapter verified at clean HEAD (D-059)

Android Termux screenshot confirms `~/Atelier-Time-Stage2` at commit `3bb2bad` and **11/11 pure-Dart editor adapter tests PASS**. GitHub comparison confirms `3bb2bad` equals current `stage-2-session-editor` tip. D-057/D-058 mixed-worktree issue resolved using a separate Git worktree, preserving the original files. Stage 1 core still has separately verified 35/35 tests. Next low-cost check is a full `dart analyze && dart test` from the clean Stage 2 worktree; eight Flutter widget tests still require Flutter SDK. Stage 2 NOT complete; no PR merges, APK, GitHub Actions, paid runners, or later-stage work.


## 2026-10-23 — Root Dart analyzer / Flutter subpackage isolation (D-060)

The user tried the combined Stage 2 command `dart analyze && dart test` in Android Termux and encountered a flood of analyzer errors, associated with nested Flutter source despite Termux only having the **Dart SDK**. The `&&` prevented full `dart test` from running after analysis failed. The existing 35 Stage 1 and 11 clean Stage 2 test passes remain distinct historical evidence, not a new 46-test pass.

Committed root and nested package `analysis_options.yaml` files on Stage 2 branch: root excludes the separate Flutter app from bare-Dart analysis, while the Flutter package remains independently analyzable on a Flutter-capable machine. Root analyzer/test rerun is **PENDING** on user device; Flutter UI test suite (8) still unrun. Stage 2 IN PROGRESS; no budget spend, PR merge, APK or Stage 3+ work.
