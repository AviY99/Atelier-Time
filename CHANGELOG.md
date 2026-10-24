# Changelog — Atelier Time

## 2026-10-24 — Stable Android D-086 accepted; D-087 design proposals pending selection

**CURRENT AUTHORITATIVE CHECKPOINT — 2026-10-24 / D-087:** User reported D-086 fully working on Android phone ("הכל עובד"), following screenshot-confirmed `:app:testDebugUnitTest :app:assembleDebug` BUILD SUCCESSFUL in 36s (42 tasks: 7 executed, 35 up-to-date). Declared **Atelier Time Android Stable 1 — D-086** (user-accepted installed functional baseline; not signed production or store release). Frozen source checkpoint branch `stable/android-d086` points to **`d0a03d2792cc324aeeba39b6327136ba6d37fd0c`**; current Android dev branch / open Draft PR #3 remain unmerged and should not be modified for accepted stable source. No CI/actions, release tag, binary publication or app `versionName` change. **Previously pending D-086 install/phone visual-acceptance statements below are historical and superseded by this newer direct user report.** Stable baseline docs: [ANDROID_STABLE_BASELINE_D086_2026_10_24.md](ANDROID_STABLE_BASELINE_D086_2026_10_24.md). Outstanding: 9 written Android instrumentation methods not executed on device; background/lock/doze audio reliability; lifecycle/time edge cases; accessibility, motion/contrast, performance/battery; signed release/distribution; future iOS/desktop multiplatform efforts; older D-015/D-016 wording only if change proposed. **Design exploration is authorized, but no visual concept has been approved or implemented:** three styles and animation plans in [VISUAL_DESIGN_CONCEPTS_2026_10_24.md](VISUAL_DESIGN_CONCEPTS_2026_10_24.md), with safe nonflashing/reduced-motion fallback. Keep accepted functions/domain/stored classes unchanged.

---

## 2026-10-24 — D-086 native Gradle gate passed, device UI gate open

- User phone terminal screenshot confirms **BUILD SUCCESSFUL in 36s**, 42 tasks (7 executed, 35 up-to-date), with `:app:testDebugUnitTest` and `:app:assembleDebug` completed. Exact test count not shown.
- Debug APK ready for update-install and visual check of two-line remainder choice plus Even and Custom, all inside the width of the phone. Existing D-085 installed app is user-accepted; no D-086 install/appearance acceptance yet. No CI or merge.

## 2026-10-24 — D-086 Editor ending-option chip fits phone width (device QA pending)

- User confirmed prior D-085 Android release works. In Editor → Complete class timeline, fixed the long "Last gets remaining time" choice by breaking its label into two centered lines and giving the three FilterChip options proportionate widths (1.5:0.8:1.1) within a fixed full-width row instead of horizontal scrolling.
- No change to selected ending option, scheduling, Save, test definitions, audio, signed storage or Timer operation. Code commit `d0a03d2` on draft Android branch; **new D-086 compilation/build/device layout acceptance still pending**.

## 2026-10-24 — D-085 live Class clock and Timer/list polish (new-source QA pending)

- User reports D-084 working on phone. Changed Class elapsed to real physical elapsed (no appended Class total); elapsed continues while paused/after Next, Class target end remains unchanged; Class remaining and progress use actual wall time. At deadline a Class still running/paused ends with one C cue.
- Increased only Class start/end/length label fonts 1.3×, numeric values unchanged. Stabilized Segment End with precise monotonic milliseconds and fixed-width monospaced seconds to prevent End card text bounce.
- Organized each saved Class card into two visible rows: Run/Edit; Duplicate/Rename/Delete, with distinct button styles, no horizontal scrolling.
- Added 4 JVM test source methods / updated earlier deadline expectations: **106 written JVM tests, 9 instrumented methods**. **D-085 new source not yet compiled, tested, APK-installed or device QA-accepted.** Retain saved data; PR #3 stays Draft; no paid CI.

## 2026-10-24 — D-084 segmented clock precision and tighter Class header (device QA pending)

- User feedback: restore `HH:mm:ss` for active Session/Break Start/End while preserving `HH:mm` for Clock and Class start/end, and put **Class length** below Class end as the third equally styled compact line under Clock.
- Android Compose source only: two timestamp formatters, separate immutable approved Class duration in HH:mm, three `WallClockRow` labels with 0dp row spacing. Existing Class remaining/elapsed/progress and phase countdown stay intact.
- New source commit `5f0df02c169ba4134cad2a6603d2df288ad96dcc`; **unbuilt, untested, not yet accepted on phone** for D-084. D-083 earlier phone Gradle PASS does not validate this delta. No app-data or timer-engine changes; Draft PR #3 retained, no paid Actions.

## 2026-10-24 — D-083 compact Timer controls and non-blocking Next (new-source QA pending)

- User confirmed D-082 updated Android app working on phone (“הכל עובד”); prior CoGo Gradle `BUILD SUCCESSFUL in 12s` with 42/42 UP-TO-DATE tasks.
- New Android branch source: replace long Exit label with distinct-color **Exit** in same four-button row as Pause/Restart/Next, with separate Exit help and confirmation. Display real wall-clock values in HH:mm without seconds; enlarge phase/Class start and end time values approximately 1.5×.
- Next can advance through the final pair without rewriting already-observed work, falls back to advancing and adjusting projected end when compensation cannot fit, and finishes/returns to saved Classes with melody C on the final regular work Session or summary. Other Next A/B cues, saved plans and user settings preserved.
- Added 5 net JVM test definitions (total **102 written**, 9 Android instrumentation methods). D-083 **not yet compiled, unit-tested, installed or manually QA-verified on phone**. PR #3 remains open Draft; no paid GitHub Actions.

## 2026-10-23 — K03 offline preset management in Android native source / HANDOFF v3.3

- User authorized the next stage; continued real Android Kotlin/Compose/Gradle code on `android-native-kotlin-compose` draft PR #3.
- Implemented pure Kotlin `PresetCatalog` / storage contract with approved-only save/update/load/duplicate/rename/delete/activate selection and timeline regeneration/full-revision consistency verification.
- Implemented versioned offline private Android `SharedPreferences` JSON collection persistence, no silent data loss on corrupt/unknown schema, isolated test storage; integrated full Compose saved-sessions UI with delete confirmation. Fixed whole-minute overflow input.
- Added 10 Kotlin preset unit tests + 1 Kotlin input-overflow test + 4 Android isolated instrumentation cases; now **32 JVM + 4 Android instrumentation test methods WRITTEN, NONE EXECUTED**. No APK, phone QA, Gradle unit tests, GitHub Actions or paid runners.
- K03 **CODED BUT UNVERIFIED**; K04 monotonic timer and three distinct audible cues remain next. Updated D-070, docs/roadmap/test plan, native README and self-contained HANDOFF v3.3; product requirements unchanged.


## 2026-10-23 — First native Android Kotlin app increment / HANDOFF v3.2 (D-068/D-069)

- User approved implementing Android-first Kotlin/Jetpack Compose/Gradle and reconfirmed all existing Atelier Time **functional and scheduling rules remain unchanged**; old D-062 release/toolchain pivot only.
- New isolated [android-native-kotlin-compose](https://github.com/AviY99/Atelier-Time/tree/android-native-kotlin-compose) branch and draft [PR #3](https://github.com/AviY99/Atelier-Time/pull/3). Real `android-native/` Gradle Android source: Compose session editor, fully editable opening/break/rhythm rules, timeline preview, 3 ending options, 25/5 advisory warnings and explicit in-memory approval. Pure Kotlin deterministic engine includes D-043 DP replacement.
- **16 Kotlin scheduler + 5 editor input JUnit methods written, none run. No Android APK/Flutter run or device validation claimed.** Previous screenshot-confirmed Dart 35+11 tests preserved as parity fixture oracles. Later offline presets and timer/3 sounds remain unimplemented.
- Added [requirements freeze + Android CoGo development/QA contract](docs/ANDROID_NATIVE_EXECUTION_AND_TEST.md) with K01–K06 gates; updated decisions D-068/D-069, roadmap, architecture, product, Stage 2, test plan, work log and handoff v3.2.
- No GitHub Actions/paid builds, PR merge or destruction of old branches/user files.


## 2026-10-23 — Android industry-standard decision, no toy spikes / Handoff v3.1

- At user's demand for a firm industry-standard choice rather than new experiments, selected **Kotlin + Jetpack Compose + Gradle** for Android-first app built on phone with **Code on the Go** (specialized mobile IDE; Android Studio is standard desktop IDE). Future Kotlin Multiplatform/Compose Multiplatform sharing for iOS/Windows/macOS (D-067).
- Sourced official Google May 2026 Compose First, JetBrains Multiplatform stability and migration docs, maintained Code on the Go 26.43 and Reddit developer/Termux community discussions; archived AndroidIDE not recommended.
- Existing duplicate/conflicting D-064 GitHub rows reconciled as D-064/D-065/D-066 history plus **D-067 authoritative**; older unexecuted Dart-to-JS/Capacitor spike remains historical only. **No disposable prototype builds required**; next work is real Atelier Time native Android implementation and ordinary QA.
- Preserved Dart scheduling 35 tests PASS, form adapter 11 tests PASS, unmerged historical PRs. No Kotlin APK yet, no GitHub Actions or paid build. Updated docs and HANDOFF v3.1.


## 2026-10-23 — Phone-first Dart reuse proof and Kotlin alternative reconciled / Handoff v3.0 (D-064)

- User-approved requirement D-062 now prioritizes fast, efficient Android development/build/install ON PHONE, with iOS/Windows/macOS ports efficiently later; D-061 strict universal build host condition superseded.
- Evaluated supported on-phone Kotlin / Code on the Go candidate (D-063) versus preserving Dart core via Dart-to-JS and community Termux Capacitor APK pipeline (D-064). **Neither Android APK route has passed on user's phone**. Kotlin would require porting verified Dart scheduler; Dart web may reuse it but native Android alerts remain a major unknown.
- Committed small UNEXECUTED browser feasibility project on isolated [spike-android-dart-web](https://github.com/AviY99/Atelier-Time/tree/spike-android-dart-web), checking canonical Example B using unchanged Dart core, plus pure Dart local server. No installable APK or migrated production UI.
- Deduplicated same-number D-062 entries in the decision register, recorded D-064, updated Android-first plans, docs and self-contained handoff v3.0. No GitHub Actions costs, migrations, or PR merges.


## 2026-10-23 — Android PHONE-first requirement and unexecuted Dart-web feasibility spike / Handoff v2.9

- User relaxed D-061 universal all-host native-build requirement: **fast efficient Android development directly on the phone FIRST**, with later efficient iOS/Windows/macOS reuse (D-062). Offline scheduling/presets/background-alert product features remain required.
- Recommended **bounded Dart web → Capacitor Android APK** candidate, not finalized: officially supported Dart JS compilation can potentially reuse already-verified 35/35 scheduler and 11/11 adapter; community Termux Capacitor build guide exists but is unverified on user's device.
- Created isolated [spike-android-dart-web](https://github.com/AviY99/Atelier-Time/tree/spike-android-dart-web) with nested Dart web package, canonical Example B browser schedule, plain Dart localhost server and non-destructive Termux instructions. **NOT COMPILED/RUN; NO APK BUILT**.
- New [Android-first feasibility plan](docs/ANDROID_FIRST_PLATFORM_SPIKE.md), updated platform assessment, historic TD-001, product specification, architecture, roadmap, test plan, Stage 2 plan, decision register and self-contained handoff v2.9.
- Preserved old branches/PRs and all confirmed test evidence; no GitHub Actions, paid services, UI migration or native audio/timer implementation.


## 2026-10-23 — Android-first, develop/build on the phone; portability later / Handoff v2.9

- User changed primary delivery requirement: **develop, debug, test, build and install Android directly on Android phone efficiently**, design for efficient future iOS/Windows/macOS port, no simultaneous four-platform first release obligation (D-062).
- Research verified maintained Android phone IDE [Code on the Go](https://appdevforall.org/code-on-the-go/) v26.43 (Oct 20, 2026) supports Kotlin/Java Android application builds on device; JetBrains Kotlin/Compose Multiplatform is a prospective future port path, NOT verified in this toolchain.
- Preferred candidate Kotlin Android + CoGo (D-063) awaits one successfully installed minimal on-device APK. No Kotlin project, UI or APK build is claimed. No broad migration until proven; Kotlin requires porting original verified Dart logic.
- New [Android-first plan](docs/ANDROID_FIRST_DEVELOPMENT_PLAN.md) and updated decision register, tech decision, product spec, architecture, roadmap, Stage 2 plan, platform reassessment and self-contained HANDOFF v2.9.
- Preserved prior **35/35 Dart scheduler**, **11/11 Dart adapter** passes and both unmerged PRs; no paid CI/Actions, auto-merge, data deletion or Stage 3+ work.


## 2026-10-23 — Full-lifecycle platform reassessment / Handoff v2.8 (D-061)

- At the user's demand, re-evaluated Flutter against **cross-platform development, tests, builds, signing, installation, and live operation**, not only app runtime.
- Official Flutter supports the four deployment targets but **not Android Termux as an official Flutter development host**; Apple iOS builds/signing require macOS/Xcode and desktop targets need their toolchains.
- Documented [PLATFORM_REASSESSMENT_2026_10_23.md](docs/PLATFORM_REASSESSMENT_2026_10_23.md), including F01–F08 hard criteria, Flutter/PWA/web+native wrappers/Expo/cloud-host comparison, PWA timer/background/installer risks and a required pre-code feasibility proof.
- **Original Flutter platform suitability reopened (D-061); further Stage 2 Flutter-specific implementation PAUSED**, draft PRs remain unmerged. No unilateral replacement, migration, new APK, GitHub Actions or paid builders.
- Prior 35/35 pure Dart scheduling and 11/11 pure Dart adapter successes preserved. Updated architecture, product, TD-001, roadmap, Stage 2 plan, decisions, README and self-contained HANDOFF v2.8.


## 2026-10-23 — Stage 2 Dart/Flutter analyzer isolation / Handoff v2.7

- Investigated user screenshot showing massive `dart analyze` failures on Android Termux Stage 2. Distinct nested Flutter package imports require Flutter SDK; bare Dart cannot resolve them. Chained `dart test` did not start after analyzer failure.
- Added root analyzer exclusion `apps/atelier_time_flutter/**` and separate nested Flutter analysis options on Stage 2 branch. **Does not suppress later Flutter SDK testing** and makes no changes to schedule/adapter behavior.
- **Post-fix analyzer and combined 46 Dart tests NOT YET RUN**; prior separately verified 35 Stage 1 and 11 Stage 2 passes unaffected. Eight Flutter widget tests still UNRUN, no APK.
- Recorded D-060 and updated test plan, roadmap, Stage 2 plan, README, branch README, work log and handoff v2.7. No GitHub Actions/paid runner, budget change, merge or Stage 3+.


## 2026-10-23 — Clean Stage 2 worktree verified 11/11 / Handoff v2.6

- New screenshots show `~/Atelier-Time-Stage2` checkout HEAD `3bb2bad` and `dart test test/editor_draft_test.dart` concluding **`+11: All tests passed!`**.
- GitHub commit comparison establishes `3bb2bad` is exactly Stage 2 branch tip `3bb2bade992fddab7dc3d7ece31d9aa58231380f`; D-057/D-058 checkout provenance problem resolved using separate worktree, original staged files untouched.
- Stage 1 35 verified core tests remain historical separate evidence. Full Stage 2 analyzer/root suite and eight Flutter widget tests still outstanding. No GitHub Actions/paid CI, APK, branch merges or later-stage programming.
- Recorded decision D-059, test plan, roadmap, Stage 2 plan and complete handoff v2.6.


## 2026-10-23 — Termux branch/index diagnosis clarified / Handoff v2.5

- User's read-only Git screenshot proves checkout remains `stage-1-4-scheduling-core` at `907f871`, despite successful fetch and failed Stage 2 switch. Stage 2 files including the 11-test adapter are **staged additions** in the old Stage 1 working tree; `pubspec.lock` remains untracked.
- Earlier screenshot of `+11 All tests passed!` is real execution from a **mixed Stage 1 HEAD plus staged Stage 2 files**, not a verified clean Stage 2 commit.
- Recommended non-destructive repair: separate `~/Atelier-Time-Stage2` Git worktree based on fetched Stage 2 commit, leaving all original staged/untracked user files untouched; confirm HEAD and rerun 11 tests there. No code changes or forced cleanup.
- Logged D-058, updated QA plan, work log and self-contained handoff v2.5. No Flutter widget tests, paid Actions, merges, APK or later-stage programming.


## 2026-10-23 — Termux editor-draft tests pass; branch switch discrepancy / Handoff v2.4

- Screenshot from user's Android Termux shows **`00:00 +11: All tests passed!`** after `dart pub get && dart test test/editor_draft_test.dart` (D-057).
- The immediately preceding fetch of Stage 2 branch succeeded, but **`git switch --track origin/stage-2-session-editor` failed** (`starting point ... is not a branch`). Exact local branch/HEAD and tracked status of test file therefore remain unconfirmed; do not falsely state the checkout switched or attribute the test run to a particular commit.
- GitHub confirms `test/editor_draft_test.dart` exists on Stage 2 branch but not the Stage 1 branch. Next safe step is to inspect local Git status/branch/HEAD and repair the checkout if needed, then rerun targeted tests once on identified Stage 2 commit.
- No code changes, paid Actions, PR merges, APK, Stage 3 or Stage 4. Previous Stage 1 35/35 remains verified; eight Flutter widget tests are still unexecuted.


## 2026-10-23 — Stage 2 Flutter-independent editor-input tests / Handoff v2.3

- Continued Stage 2 on isolated `stage-2-session-editor` branch after user asked to continue.
- Extracted Flutter UI text parsing into pure Dart `lib/src/editor_draft.dart`, exported from `lib/atelier_time.dart`. Preserves user-entered minutes, ordered opening groups, cycle and special placement; the scheduling engine still validates positivity and warnings. Ignores inactive manual ordinal text when special-break count is zero.
- Added **11 unexecuted plain Dart test definitions** for editor input conversion (`test/editor_draft_test.dart`) runnable with Android Termux Dart, without Flutter.
- **New adapter/tests NOT compiled or run** in the assistant runtime. Previously verified Stage 1 core 35 tests PASS; eight Flutter widget test cases still NOT run. No paid GitHub Actions, builds, merge, persisted preset or timer development.
- Updated project decision D-056, Stage 2 plan, roadmap, test plan, work log and handoff v2.3.


## 2026-10-23 — Stage 2 UI safety and widget-test source / Handoff v2.2

- Continued authorized Stage 2 `stage-2-session-editor` work; added stable widget identifiers for inputs, approval, endings, warning controls.
- Blocked malformed inputs and suppressed misleading partial timelines for invalid candidates. Updated mobile scroll layout while preserving two-pane wide display.
- Added **eight Flutter widget-test definitions** and `flutter_test` dev dependency, covering stage T19–T23 requirements and canonical Example B preview. Corrected short-rest warning fixture to a valid zero-summary session.
- **FLUTTER TESTS NOT EXECUTED**: no accessible Flutter SDK. Previously verified Stage 1 Dart core remains 35/35 PASS on Android Termux; it was not changed.
- Recorded D-055 and updated roadmap/QA/Stage 2 plan and handoff v2.2. PR #1 / draft PR #2 remain unmerged; no GitHub Actions, paid builds, APK, persisted presets, live timer or later-stage work.



## 2026-10-23 — Stage 1 integration proposed; Stage 2 editor prototype begun / Handoff v2.1

- User explicitly asked to **finish documentation and continue** after verified Stage 1 Step 1.4 completion (D-052). Recorded continuation decision D-053 and source/checkpoint D-054.
- Opened [verified Dart core PR #1](https://github.com/AviY99/Atelier-Time/pull/1), **not merged**. Created isolated [Stage 2 editor branch](https://github.com/AviY99/Atelier-Time/tree/stage-2-session-editor), nested Flutter prototype source under `apps/atelier_time_flutter/` consuming existing core via path dependency, and [draft PR #2](https://github.com/AviY99/Atelier-Time/pull/2).
- Added [Stage 2 input/preview and QA plan](docs/STAGE_2_PLAN.md) with all user-approved fields, full phase preview, three ending choices, and 25/5 warning acknowledgment; provisional neutral styling only. Stage 3 presets and Stage 4 timer/audio remain unstarted.
- Stage 1 Dart evidence remains **35/35 user-screenshot-confirmed passing**. Stage 2 Flutter source only underwent noncompiler structural check; **NO Flutter SDK, `flutter analyze`, Flutter unit/widget tests, APK or app build**. No GitHub Actions/paid workflow, budget change, branch merge or Stage 3+ coding.
- Updated DECISIONS, ROADMAP, TEST_PLAN, PRODUCT_SPEC, README, WORK_LOG, CHANGELOG and self-contained HANDOFF v2.1.



## 2026-10-23 — Stage 1 Step 1.4 pure Dart core verified / Handoff v2.0 (D-052)

- User screenshot confirms branch update `8cf7a24..907f871` and **`00:00 +35: All tests passed!`** under Android Termux Dart 3.13.7. The 35th T09 1m+34m regression passed along with all prior tests.
- Earlier screenshot proved `dart analyze` **No issues found!** for the prior 34-test checkout, plus prior successful smoke check. The new 35th test-only revision was **not separately re-analyzed**.
- **Stage 1 Step 1.4 scheduling core/required test gate VERIFIED and COMPLETE ON ISOLATED BRANCH.** Wider T13/T14 edge combinations remain a later QA backlog; no installed app or cross-platform build evidence.
- **No merge to main; no Stage 2 authorization. No GitHub Actions, paid runners, user budget modification, Flutter screens, Android APK or deployment.** Handoff v2.0 and decision D-052 preserve the exact stop point.



## 2026-10-23 — Stage 1 Step 1.4 T09 edge regression added / Handoff v1.9

- Screenshot-confirmed Android `dart analyze`: **No issues found!** and `dart test`: **34 tests all passed** (D-050). Separate smoke check passed.
- Required Stage 1 T09 custom final-pose endpoint audit identified untested 1m+34m (the complementary 34m+1m and zero-duration rejection were covered). Added a **35th** regression test including exact phase times and 25/5 warning acknowledgement on the same isolated code branch, commit `4b2f5d9fdd8dabc6dea8dcc0204c5062c7b6916a`.
- **35th test NOT EXECUTED YET**. User's free Android Termux command: `git pull --ff-only && dart test`. Retain 34-test screenshot PASS, no new success claim.
- Decision D-051, test plan, roadmap, README, branch progress, WORK_LOG and HANDOFF v1.9 updated. Stage 1 Step 1.4 still in final QA; no main merge, Stage 2 authorization, GitHub Actions, paid runners, builds or budget modification.


## 2026-10-23 — Screenshot-verified 34 Dart tests / Handoff v1.8

- Received Termux screenshots proving `dart pub get` succeeded, `dart analyze` reports **No issues found!**, and `dart test` ends **00:01 +34: All tests passed!** on Android Dart 3.13.7. The earlier smoke check also passed.
- Elevated verification to **screenshot-confirmed results D-050**, superseding user-report-only status D-049. This is not a multi-platform Flutter application build.
- Acceptance audit of the 34 tests identifies one explicit Stage 1 T09 custom split endpoint not yet tested (1m+34m); 34m+1m and 0m rejection already covered. No new tests or code committed in this log iteration, so original 34-pass evidence remains valid.
- **Step 1.4 remains in final QA review**, branch unmerged, Stage 2 not authorized. No GitHub Actions, paid runners, budget changes, UI, or APK.



## 2026-10-23 — Full Android Dart unit suite user-reported passing / Handoff v1.7

- Following user-confirmed Termux smoke test PASS (D-048), user reports **“All tests passed!”** after requested `dart pub get && dart analyze && dart test` on Android Dart 3.13.7 (D-049). Written suite currently defines **34 tests**. No full analyzer/test transcript or local commit ID supplied; preserve as **USER-REPORTED PASS**, not independently reproduced.
- Step 1.4 scheduling implementation has real positive test feedback. Remains in acceptance/coverage review; do not merge as final or start Stage 2 without appropriate checkpoints.
- No GitHub Actions, paid runners, builds, Flutter UI, APK or budget modifications. Updated test plan, roadmap, work log and self-contained HANDOFF v1.7.


## 2026-10-23 — Android Dart smoke pass / Handoff v1.6

- User confirms **“Scheduling smoke checks succeeded.”** on Android Termux Dart 3.13.7 following Step 1.4 warning-offset integer fix. This is a **user-confirmed successful Dart smoke run**, including compilation and the smoke assertions.
- Previous D-047 `num`/`int?` compiler error is no longer observed in that smoke run.
- Next checkpoint: `dart pub get && dart analyze && dart test`. **Full analyzer and unit-test results are still pending; do not call Stage 1 Step 1.4 complete.**
- D-048 and v1.6 handoff recorded; no Actions, paid runners, builds, branch merge, or Stage 2 authorization.


## 2026-10-23 — Stage 1 Step 1.4 first real Android compiler feedback / Handoff v1.5

- User successfully ran Dart SDK **3.13.7** in Android Termux and downloaded the development branch. The first smoke invocation **FAILED compilation** with `num` supplied to `int?` at `offsetSeconds: crossingOffset` in `lib/src/scheduling.dart`.
- Fixed the integer typing issue with conditional integer arithmetic; committed to `stage-1-4-scheduling-core` as `0dedd95756b6d3984b2e16548a58599179fbf7ff`.
- **Updated code NOT YET retested**. Next step for Termux: `git pull --ff-only && dart tool/smoke.dart`, followed (if successful) by Dart analyzer and test suite.
- D-047 and handoff v1.5 updated. No GitHub Actions, paid runner, merge, Stage 2 code or unverified success claim.



## 2026-10-22 — Stage 1 Step 1.4 verification preparation / Handoff v1.4

- Diagnosed missing Dart SDK and DNS resolution failures in assistant's container. This blocked **actual Dart test execution**, not source-code drafting.
- On isolated development branch, restricted `ScheduleCandidate` construction to engine/library code and retained full timestamped rest-warning acknowledgment records in immutable approved sessions; added unexecuted regression assertions.
- Added local no-GitHub-Actions helpers `tool/verify.ps1` and `tool/verify.sh` and `.gitignore` for test logs/caches. Minimum install for this pure-Dart step is **Dart SDK only** on a computer capable of execution.
- Recorded D-046, updated roadmap, README, test plan, work log and handoff v1.4. **No Dart compiler/analyzer/test run, build, paid Actions usage, merge or Stage 2 work. Step 1.4 remains IN PROGRESS**.

---


## 2026-10-22 — Stage 1 Step 1.4 D-044 support / Handoff v1.3

- User **approved D-044**, resolving former D-038: any **positive** special break can replace a normal break even if shorter/equal (examples regular 5m, special 1m/3m/5m), with the D-031 25/5 warning and acknowledgement when necessary. D-036 still blocks all 0-minute sessions/actual breaks.
- Updated Dart scheduling core on isolated `stage-1-4-scheduling-core` branch to accept negative, zero and positive `L−B` without `pendingDecision`.
- Added written regression and dependency-free smoke test source for shorter/equal special breaks and valid warning-override flows.
- Independent Python-based placement comparisons matched **25,000 virtual-schedule random cases** plus **4,000 additional exhaustive real-timeline cases**. This is **not Dart test execution**.
- **No Dart SDK/network access for download; no Dart compiler, `dart analyze`, `dart test`, smoke script execution, GitHub Actions, paid runners, build or merge**. Reported GitHub budget remains unchanged; Stage 1 Step 1.4 still **IN PROGRESS** and no Stage 2 authorization.
- Updated decision/policy/test docs, roadmap, README, work log, and self-contained HANDOFF v1.3.


## 2026-10-22 — Stage 1 Step 1.4 DP fix / Handoff v1.2

- Continued authorized pure Dart coding on `stage-1-4-scheduling-core` after user requested “Continue” and explanation of open D-038.
- Explained **D-038**: whether positive special break may be shorter or equal to positive regular break (e.g. special 3m versus regular 5m). Recommendation to allow with 25/5 warnings is **not a decision**. Code preserves `pendingDecision`.
- Fixed exponential automatic-break selection and a false-invalid-placement case using exact dynamic programming with original midpoint/even placement objective and deterministic earlier-ordinal ties.
- Added regression sources for a formerly false-invalid 97m case and an 8-hour five-special-break case. An **independent Python model** matched exhaustive search over 25,000 sampled configurations and canonical A/B positions, but **Dart tests have NOT run**.
- Source remains on isolated branch; no Dart/Flutter SDK, no GitHub Actions, budget change, compile, merge, UI or app build. **Step 1.4 remains IN PROGRESS; Stage 2 is not authorized.**


## 2026-10-22 — Stage 1 Step 1.4 first source increment / Handoff v1.1

- User's **“Continue”** after the blocked GitHub budget discussion authorized **Step 1.4 only** (D-040). Created isolated development branch [stage-1-4-scheduling-core](https://github.com/AviY99/Atelier-Time/tree/stage-1-4-scheduling-core).
- Committed pure Dart scheduling engine, standalone package manifest/export, canonical 3h A/B and 25/5 regression test code, dependency-free smoke script, and branch progress documentation.
- D-036 positive session/break rule retained; D-031 25/5 warning retained; D-038 positive-special-versus-regular-break comparison explicitly returns **pendingDecision**, not falsely accepted/rejected.
- **GitHub Actions not used or configured** due reported $12/month budget block (D-041).
- **Dart and Flutter SDKs unavailable** in current environment; **NO compilation, Dart analyzer, tests or app builds run**. Simple bracket/string/comment structural check only. Engine's multi-break search still needs performance verification.
- Source is on **development branch only**, not merged. Stage 1 Step 1.4 **IN PROGRESS / NOT COMPLETE**; no Stage 2 authorization. Updated root docs and handoff to v1.1.


## 2026-10-22 — Stage 1 Step 1.3 APPROVED / Handoff v1.0

- **Explicit approval:** User approved all remaining documented scheduling proposals **P-01–P-06** subject to **D-036**, and specifically prohibited beginning Stage 1 Step 1.4.
- Registered **D-037** Step 1.3 completion, **D-038** the one explicitly unresolved positive special-break-versus-normal-break comparison, and **D-039** Step 1.4 stop gate.
- Confirmed automatic closest-to-work-midpoint break placement (tie earlier), evenly spaced multiple special replacements, manual ordinals, validation, custom ending options, full preview and warnings with user acknowledgement.
- Approved zero *count* of special replacements and zero final summary for otherwise valid positive sessions. **Zero total session or actual break remains a hard error; no zero-length break slots.**
- Set 25/5 warning counter to **one full activated session** (no automatic cross-preset carryover); an overall 25m ending needs no invented break, while over-25m posing warns.
- Updated current docs, TEST_PLAN accepted future tests, README, roadmap, WORK_LOG and self-contained HANDOFF v1.0.
- **Documentation-only commits/read-back. No source code, automated test execution, build, UI, or installation. Step 1.4 NOT AUTHORIZED.**


## 2026-10-22 — Stage 1 Step 1.3 clarification / Handoff v0.9
- **Latest approved D-036:** A 0-minute full session or configured normal/active special break is invalid; previous D-031 permission for zero-minute breaks superseded. Positive whole-minute break minimum is 1 minute.
- **Retained D-031:** Accumulated 25m of posing calls for >=5m rest before continuing; valid shorter breaks and long poses trigger a warning requiring explicit user acknowledgement rather than automatic rejection.
- Withdrawn: the proposed zero-minute logical break-slot model from the earlier Step 1.3 review. Updated current policy, review, data model, product specification, tests, roadmap, README and handoff. Historical changelog entries remain for traceability, not current rules.
- Other Step 1.3 technical policies still require user approval. **No executable code or tests; Step 1.4 remains on hold.**


## 2026-10-22 — Stage 1 Step 1.3 consolidated review / Handoff v0.8
- After the user's “Continue,” consolidated remaining scheduling decisions into [STEP_1_3_REVIEW.md](docs/STEP_1_3_REVIEW.md).
- Proposed logical 0-minute regular/special break slots without elapsed zero-length phases, plus manual replacement ordinals, warning approval after final-pose changes, and boundary/cross-preset interpretation.
- Linked and refined Step 1.3 policy and pending tests; added decision-log entries D-034/D-035, revised roadmap, README, work log and handoff.
- **Approved 25/5 warning with 0-minute break override D-031 remains unchanged; all new details remain pending user sign-off. No code, automated tests or build.**


## 2026-10-22 — Stage 1 Step 1.3 safety clarification / Handoff v0.7
- **Approved D-031:** A 5-minute break is recommended after 25 minutes of accumulated posing without an adequate break. Accumulate consecutive pose phases; a rest under 5 minutes does not reset the counter.
- **Permitted:** A user can choose a 0-minute break, and can approve a schedule violating the 25/5 recommendation **after an explicit warning acknowledgement**; the app must not silently adjust the entered durations.
- **Superseded:** Prior hard minimum of 1 minute attributed to D-030; break defaults stay 5m regular / 15m special, both editable.
- Updated policy, product specification, data model, schedule rules, decision register and QA tests; documented pending cross-session and zero-length special replacement slot semantics.
- Updated progress roadmap, work log and handoff to v0.7. Other Step 1.3 proposals are not yet approved.
- **No executable code/tests, Flutter setup or app builds; Step 1.4 remains on hold.**


## 2026-10-22 — Stage 1 Step 1.3 / Handoff v0.6
- User authorized discussion/documentation of scheduling placement and edge policies by saying “Continue” after Step 1.2.
- Added [SCHEDULING_EDGE_POLICY.md](docs/SCHEDULING_EDGE_POLICY.md): six **proposed** rules covering midpoint long-break replacement, multiple evenly-spaced long breaks, manual ordinals, invalid configuration handling and final-pose edge cases.
- Preserved both approved example schedules exactly; these rule proposals are **not yet user-approved**.
- Updated D-026 through D-029, roadmap, scheduling contract, work log, README and current cross-chat handoff.
- **No Dart/Flutter source code, test executions or installable builds; Step 1.4 remains blocked.**


## 2026-10-22 — Stage 1 Step 1.2 / Handoff v0.5
- User's “Continue” authorized only the **data-model design** following approved Step 1.1.
- Added [DATA_MODEL.md](docs/DATA_MODEL.md) with proposed Dart session rules, variable-length editable opening pose groups, phase intervals, long-break replacement representation, validation issues, final-pose option model, and immutable approved preset shape.
- Included future test fixture input payloads for approved Example A and B; canonical expected schedules unchanged.
- Recorded decisions/gates D-023 to D-025; updated roadmap, architecture, README, work log and handoff.
- **No code or tests; Step 1.3 and engine implementation remain on hold.**


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
- v0.4: User approved Flutter/Dart four-platform choice and non-native UI clarification for Stage 1 Step 1.1 only.
- v0.5: Step 1.2 documented the proposed data model.
- v0.6: Step 1.3 placement and edge-policy proposals prepared for review.
- v0.7: D-031 25/5 warning with historical zero-minute allowance, **later superseded by D-036**.
- v0.8: Initial review including now-withdrawn zero-minute logical break slots.
- v0.9: D-036 forbids 0-minute full session and configured breaks.
- v1.0: Stage 1 Step 1.3 approved as D-037; strict stop before Step 1.4. Current source of truth: docs/HANDOFF.md.
