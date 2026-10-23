# ATELIER TIME — CURRENT CROSS-CHAT PROJECT HANDOFF (v3.4)

## D-071 APPROVED AND IMPLEMENTED IN SOURCE — 2026-10-24; MUST REBUILD ON PHONE

The user replied **“כן”** to the proposed opening-group break semantics: configurable `0m` allowed between earlier opening groups, strictly positive rest after last group, and matching first regular `BREAK_FIRST` break **reused not duplicated**. Logged **APPROVED** in [DECISIONS.md D-071](DECISIONS.md), with detailed rules in [EDITOR_LABEL_REVIEW_2026_10_24.md](EDITOR_LABEL_REVIEW_2026_10_24.md). Real `android-native-kotlin-compose` source now has optional group after-break field, explicit editor input/validation, group-break phases that do not consume ordinary break replacement ordinals, version-1 optional persistence field and signature extension for explicit values while leaving legacy signed records unchanged. Example A UI newly sets 0m + 5m. **46 Kotlin JVM test methods + 7 Android instrumentation methods written:** 32 earlier JVM methods completed a successful Gradle task on phone, plus 5 K04 timer and 9 newly added group-break JVM methods still unrun. Seven Android instrumentation methods remain unrun (six prior plus one new). **New sources are NOT compiled/tested on the phone yet**, previous 32-test / APK PASS belongs to earlier source. Next device step update checkout and run native-AAPT2 Gradle tests/build; then reinstall, validate timer and group-break behavior and saved preset compatibility. PR #3 remains unmerged, no paid CI.

---

## LATEST — 2026-10-24: INSTALLED ANDROID UI OBSERVED, K04 TIMER SOURCE ADDED / UNVERIFIED

User uploaded screenshot of the **installed Atelier Time Compose app running on their Android phone**, showing live session editor, Example A preview, explicit approval and an existing saved preset. This is first confirmed **K02 on-device launch/visual output**, not proof of exhaustive editor operations or K03 persistence through relaunch. Earlier phone screenshots verified `:app:testDebugUnitTest`, `:app:assembleDebug` and APK file hash at prior source commit.

After explicit user request to **build timer/clock with three sounds**, new source on `android-native-kotlin-compose` adds platform-independent `MonotonicSessionTimer.kt`, `SessionTimerScreen.kt`, approval-to-timer navigation, and **5 NEW JUnit clock methods** (total now 37 JVM source tests; 6 Android instrumentation sources). Editor labels and section/group Help improved, with a proposed copy review. New K04 code is **NOT COMPILED/TESTED ON PHONE YET**, locked-screen/background sound reliability unimplemented; D-016 per-opening-pose vs group remains open, and code deliberately emits no sound between consecutive opening poses. D-071 records user's new requirement for configurable rests following each opening group (earlier group boundary may be 0; last cannot be 0) with final placement/compatibility review needed before changing approved scheduling fixtures or saved JSON. Draft PR #3 remains unmerged; no Actions/CI expense.

Next: phone `git pull --ff-only`, `gradle :app:testDebugUnitTest :app:assembleDebug` via Bash with local native AAPT2 override, fix any **actual** errors, reinstall, verify timer/sounds and K03 cold restart. No claim of source-only features already working on device.

---

## ACTUAL PHONE APK FILE VERIFIED — 2026-10-23

After screenshot-verified `:app:assembleDebug BUILD SUCCESSFUL`, user ran on-phone `ls -lh app/build/outputs/apk/debug/app-debug.apk && sha256sum app/build/outputs/apk/debug/app-debug.apk`. Both succeeded: real `app-debug.apk` exists at the expected project path, reported **12M**, with SHA-256 **`d793f6f322000307fb4d636ff3c6d842dad5c3724517776ba3de81dedd78ae77`** (screenshot). **Build + APK filesystem verification PASS, installation and execution NOT yet verified.** Next install APK and verify launch/editor, then offline K03 save/restart/load and available instrumentation. No background timer K04 yet. Historical old "no APK" statements below predate this evidence. See [WORK_LOG](WORK_LOG.md) and [TEST_PLAN](TEST_PLAN.md).

---

## PHONE DEBUG APK ASSEMBLY SUCCESS — 2026-10-23

New screenshot-verified phone milestone: native `android-native/` `:app:assembleDebug` with CoGo's Android-native AAPT2 property finished **`BUILD SUCCESSFUL in 47s`**, 36 actionable tasks (18 executed, 18 up-to-date), including packaging/signing task stages. `:app:testDebugUnitTest` earlier passed Gradle task in 34 seconds on same phone. **Debug APK build Gradle task now PASSED**, but actual APK path/hash, installation, app launch, canonical editor UI and persisted-session acceptance have **not yet been demonstrated**. Verify `app/build/outputs/apk/debug/app-debug.apk`, install/open, and check K03 offline preset operations/relaunch next. Six Android instrumentation tests and numerical JUnit XML evidence remain outstanding; K04 unimplemented; PR #3 draft/unmerged. Source/build changes not required solely to record successful build.

---

## REAL KOTLIN UNIT-TEST GRADLE SUCCESS — 2026-10-23

**New phone evidence, superseding older pending/failed K01 statements below:** On Code on the Go Android ARM64, actual `:app:testDebugUnitTest` ran with native AAPT2 Gradle property; screenshot shows Android resource/Kotlin/JVM test tasks and **`BUILD SUCCESSFUL in 34s`** (24 tasks; 10 executed/14 up-to-date). Latest screenshot-confirmed device Git checkout from preceding pull was `a4b9469`, before subsequent runner/document-only branch commits. **K01 Gradle unit test gate now PASSED ON THE PHONE.** JUnit XML count not yet seen; 32 JUnit methods are written in source. **No `:app:assembleDebug` result, APK, installation, editor acceptance or K03 persistent-storage acceptance yet.** The six Android instrumentation tests not run. K04 not implemented. Next perform real debug APK build with the same CoGo AAPT2 override, then device installation/UI/K03 persistence tests; no Actions, auto merge or repeated historical Dart suites. See [WORK_LOG.md](WORK_LOG.md) and [TEST_PLAN.md](TEST_PLAN.md).

---

## PHONE EXECUTION BRIDGE PENDING DEVICE SETUP — 2026-10-23

At the user's request to make ChatGPT-triggered Android execution possible, added a **restricted** GitHub-Issues-to-CoGo-terminal task bridge to the live `android-native-kotlin-compose` implementation branch: [runner source](https://github.com/AviY99/Atelier-Time/blob/android-native-kotlin-compose/android-native/tools/phone_task_runner.py), [setup instructions](https://github.com/AviY99/Atelier-Time/blob/android-native-kotlin-compose/android-native/PHONE_RUNNER.md). [Issue #4](https://github.com/AviY99/Atelier-Time/issues/4) queues a **doctor** environment-inspection task for when the phone worker is started. Script is authored and committed, not installed or exercised on user's phone. It will require CoGo toolchain, terminal Python/git, a narrow GitHub Issues token held only on-device, and an actively running process. Its CLI Gradle compatibility is **not yet verified**. GitHub Actions remain unused. K01–K03 unverified, APK/device storage untested, K04 not implemented. See [WORK_LOG](WORK_LOG.md) for evidence.

---

## LATEST SOURCE-ONLY FOLLOW-UP — K03 corruption guards (2026-10-23)

Current implementation branch `android-native-kotlin-compose`; draft [PR #3](https://github.com/AviY99/Atelier-Time/pull/3) remains open, unmerged. After previous D-070 handoff, committed actual code fix [`01e56f0`](https://github.com/AviY99/Atelier-Time/commit/01e56f01be73f0d90e473a2263ecbda2be187404) ensuring wrong-type SharedPreferences data is treated as a readable error without erasure, and direct adapter writes reject invalid/future-version data. Added two Android instrumentation cases ([`95facb8`](https://github.com/AviY99/Atelier-Time/commit/95facb8005578b605307771e0876c377ac747ae8); corrected JSON literal [`0db22e8`](https://github.com/AviY99/Atelier-Time/commit/0db22e8f5833459dc2e7c80300b4e6a5cfe22748)). GitHub wrote and read back source successfully, **not compiled**.

**Current count: 32 Kotlin JVM + 6 Android instrumentation methods WRITTEN, NONE EXECUTED. K01–K03 remain UNVERIFIED; APK NOT BUILT/INSTALLED; K04 runtime timer and three cues NOT IMPLEMENTED.** Current assistant environment has Kotlin 1.9 and Java 21 but **no Gradle, Android SDK, ADB or access to user's phone**. Phone-local CoGo must run `:app:testDebugUnitTest`, `:app:assembleDebug`, then install/reopen and check offline persistence; `:app:connectedDebugAndroidTest` if available. Detailed evidence in [WORK_LOG](WORK_LOG.md), [TEST_PLAN](TEST_PLAN.md) and [native branch README](https://github.com/AviY99/Atelier-Time/blob/android-native-kotlin-compose/android-native/README.md). No new product decisions, GitHub Actions, Dart reruns or merge; original handoff v3.4 paragraphs below remain a historical snapshot.

---
**Updated:** 2026-10-23  
**Repository:** https://github.com/AviY99/Atelier-Time (existing public repo; default `main`)  
**CURRENT PRODUCT/IMPLEMENTATION REQUIREMENT:** Develop Android on user's phone quickly/efficiently, build/install APK there, while isolating pure Kotlin domain logic and Compose UI for later iOS/Windows/macOS via KMP/Compose MP. **Industry-standard code stack selected D-067, not proof-spike gating.**
**CURRENT AUTHORITATIVE STATUS — 2026-10-23, HANDOFF v3.4 / D-070:** **NEXT CHAT: START WITH [SELF-CONTAINED HANDOFF](NEXT_CHAT_HANDOFF_2026_10_23.md)**. Approved Android-first phone development Kotlin/Compose/Gradle; no changed product requirements. Latest native branch `android-native-kotlin-compose`, draft PR #3 unmerged, K03 approved-only offline preset catalog/Android JSON/Compose UI authored. **32 Kotlin JVM + 4 Android instrumentation test methods NOT RUN; Kotlin NOT COMPILED; APK NOT BUILT/INSTALLED.** Old Dart 35 + 11 tests screenshot-verified separately; K04 live timer/three cues NOT IMPLEMENTED. D-015 / D-016 unresolved; no paid cloud builds, CI, reset or merges.

## AUTHORITATIVE NEXT-CHAT HANDOFF v3.4 — READ THIS FIRST (2026-10-23)

**A complete, compact, independently usable handoff was prepared from the live GitHub product docs AND current implementation branch:** [NEXT_CHAT_HANDOFF_2026_10_23.md](NEXT_CHAT_HANDOFF_2026_10_23.md). It is the **single recommended starting point for the next conversation**, rather than rereading all older exploratory decisions below.

**Latest verified state:** User-authorized Kotlin + Jetpack Compose + Gradle native Android app on phone, future KMP/Compose Multiplatform adaptation, **approved product requirements unchanged**. Active branch [`android-native-kotlin-compose`](https://github.com/AviY99/Atelier-Time/tree/android-native-kotlin-compose), [draft PR #3](https://github.com/AviY99/Atelier-Time/pull/3) not merged. K03 offline-approved preset CRUD / versioned private storage / Compose library authored and integrated. **32 Kotlin JVM JUnit tests + 4 Android instrumentation tests are WRITTEN BUT HAVE NEVER RUN**; **no Kotlin compile, on-phone Gradle result, APK or installed application**. Historical separately proven **35 Dart scheduler + 11 Dart input tests PASS**, not proof of Kotlin. K04 countdown/audio/background is NOT IMPLEMENTED. D-015 (summary meaning) / D-016 (cue granularity) remain open.

**Next real activity after opening next chat:** normal CoGo work on `android-native/` Gradle root; `:app:testDebugUnitTest`, `:app:assembleDebug`, install/reopen, verify exact A/B/editor and saved presets, run `:app:connectedDebugAndroidTest` if available. Correct any actual errors before marking K01/K02/K03 complete, then implement K04. **No throwaway experiment, paid Actions, PR auto merge, unwarranted platform re-selection or deletion of historical work.** This step is NOT executed by authoring the handoff.

---
## LATEST STAGE — K03 offline approved presets authored, unverified (D-070; 2026-10-23)

**User instruction:** “להתקדם לשלב הבא”. This authorizes next real Android development stage, not another technology evaluation or throwaway test project.

**Source in [android-native-kotlin-compose](https://github.com/AviY99/Atelier-Time/tree/android-native-kotlin-compose)** / open [draft PR #3](https://github.com/AviY99/Atelier-Time/pull/3):
- `android-native/app/src/main/java/com/ateliertime/domain/Presets.kt`: platform-neutral `PresetStore`, `PresetCatalog`, `StoredPreset`, approved-only save/update/open/duplicate/rename/delete/select. Every loaded plan is **regenerated by ScheduleEngine and approved again** only when all hard errors/final-pose requirements are resolved and existing rest warnings had been explicitly acknowledged. Consistency fingerprint uses all input rules including inactive settings, selected final choice, phase times, break ordinals and acknowledged warning offsets. Consistency signature is **not cryptographic protection**.
- `android-native/app/src/main/java/com/ateliertime/android/AndroidPresetStore.kt`: private on-phone `SharedPreferences` version 1 JSON containing the full collection and active selection, written with one synchronous committed transaction, denies unsupported versions/corrupt JSON without overwriting; test adapter supports isolated names so instrumented tests do not touch user presets.
- `android-native/app/src/main/java/com/ateliertime/android/PresetLibrarySection.kt`, `SessionEditorScreen.kt`: Compose offline save/overwrite/save-as-new/load/duplicate/rename/delete-confirm/select. Loaded approved timeline returns to editor, editing invalidates approval and acknowledgements. Selection **does NOT yet start a timer**.
- `EditorInput.kt`: checked `Long` intermediate minute-to-second conversion prevents silent signed-`Int` overflow.
- `app/src/test/`: **32 Kotlin JVM JUnit methods** (16 schedule + 6 input + 10 preset catalog). `app/src/androidTest/`: **4 Android instrumentation methods** for storage persistence across new repository instance, CRUD, corrupted and unknown future version, using a separate test-only private preferences file. **NO NEW TESTS ACTUALLY EXECUTED; no APK/compiler/device verification.**

**Next normal QA:** Open `android-native/` in Code on the Go on user's Android phone and run `:app:testDebugUnitTest`, `:app:assembleDebug`, then install/run and check editor and save/relaunch/load actions; run `:app:connectedDebugAndroidTest` when local instrumentation works. Any actual compiler error must be resolved based on the build log before marking K01/K02/K03 complete. No GitHub Actions/paid builds. No need to run the already verified Dart suites or delete the earlier dirty Termux worktree.

**Next coding after K03 acceptance:** K04 accurate monotonic phase timer (start/pause/resume/stop), background/lock behavior and 3 different audible cues. Do NOT silently decide D-016 sound per pose vs group or D-015 summary label without user decision.

**Status:** Rules unchanged, source advanced, **Android compilation/device QA outstanding**, persistence **not proven to work on phone**, K04 not started. Keep this handoff current each iteration.

---

## LATEST CURRENT WORK — D-068/D-069 user authorized true Android app (2026-10-23)

**Exact user approval:** “מאושר , תתקדם , יש פער גדול לסגור , אשר שהגדרות והדרישות.מובנות תועדו ולא דורשות.שינוי , עדכן את תהליך הפיתוח והבדיקה למע החדשות”. This means **continue coding** actual Kotlin Android app and update pipeline, **not** ask for further stack selection or toy proof, and **not** change approved session/scheduling functionality.

**Requirements checked — NONE NEED CHANGING:** [PRODUCT_SPEC](PRODUCT_SPEC.md), [EXAMPLES](EXAMPLES.md), [SCHEDULING_RULES](SCHEDULING_RULES.md), [SCHEDULING_EDGE_POLICY](SCHEDULING_EDGE_POLICY.md), [STEP_1_3_REVIEW](STEP_1_3_REVIEW.md). D-031 accumulated 25m posing / >=5m qualifying rest warning and explicit separate acknowledgement, D-036 zero session/break invalid, D-037 zero summary/count and deterministic multiple/manual replacements, D-043 efficient exact dynamic programming, D-044 special replacement any positive length; exact canonical A/B, editable rules/opening groups, three end choices, preview+approval, offline named presets, accurate phase timer and three distinct sounds. Logo/style, D-015 final summary meaning and D-016 cue granularity remain user-controlled/open. **ONLY prior D-062 developer host/release priority and D-067 technology changed:** Android-first on-phone now, future iOS/Windows/macOS KMP path.

**Committed source, new isolated branch:** [android-native-kotlin-compose](https://github.com/AviY99/Atelier-Time/tree/android-native-kotlin-compose), [draft PR #3](https://github.com/AviY99/Atelier-Time/pull/3) open/unmerged. Based on Stage2 tested Dart branch (to preserve old fixtures). `android-native/` is the actual native Android Gradle project: CoGo toolchain-aligned Gradle 9.6.1 / AGP 9.3.1 / Kotlin+Compose compiler 2.3.21; `Models.kt`, `ScheduleEngine.kt` with platform-free Kotlin version of full deterministic schedule and special-break DP; `MainActivity.kt`, `EditorInput.kt`, `SessionEditorScreen.kt` with actual Compose editable editor, chronological preview, final remainder/even/custom options, D-031 safety acknowledgement, in-memory approval invalidated by rule edits; `app/src/test` 16 schedule and 5 input tests **WRITTEN BUT NOT EXECUTED**. No fake APK or tested UI claim.

**New authoritative engineering workflow:** [ANDROID_NATIVE_EXECUTION_AND_TEST.md](ANDROID_NATIVE_EXECUTION_AND_TEST.md) describes invariant requirements and **K01 Kotlin JUnit + scheduler parity**, **K02 actual phone APK/editor**, **K03 local offline preset persistence**, **K04 monotonic runtime+3 distinct audio cues+lock-screen**, **K05 install/accessibility/offline workshop QA**, **K06 future KMP architecture**. The application code must be built/tested directly on phone with [Code on the Go](https://appdevforall.org/code-on-the-go/) using Gradle tasks `:app:testDebugUnitTest`, `:app:assembleDebug`; use **`android-native` Gradle root** on the new branch, not Dart root. Phone CoGo has NOT yet run these tasks. Old Dart Termux `dart test` does not compile Kotlin or Jetpack Compose. No Gradle wrapper JAR in repo; mobile IDE provides toolchain. No GitHub Actions/paid builds or automatic PR merge.

**Actual present state:** Pure Dart Stage1 35/35 PASS and Stage2 editor adapter 11/11 PASS historically, **Kotlin 21 tests NOT RUN**, no APK, no saved presets or timer/audio or end-to-end user testing. User explicitly authorized progression to build missing real features, no repeat approvals required for current Android phase. Always document actual build output; do not mark Stage 2 editor complete on source creation alone.

**Next iteration:** Get native Android project into actual Code on the Go on phone via its Git/open-project flow, compile unit tests and debug APK as normal implementation QA; fix first actual errors. Then progress offline saving/activation and monotonic timer/sounds with Android OS restrictions; keep product contract exact and preserve existing user staged worktrees.

---

## AUTHORITATIVE UPDATED STACK SELECTION — D-067 (2026-10-23)

User explicitly says **no more experimental builds or PoCs**, and asked for the **industry standard** after checking official vendor guidance and developer forums. The current, definitive software-engineering selection for Android phone-first is:

**Kotlin + Jetpack Compose + Android Gradle Plugin/Gradle**, a normal Android native application. Use actively maintained **Code on the Go** on the Android device as the on-phone editor/build host. **Code on the Go is not the standard IDE of the Android industry — Android Studio on desktop is — but its device-local Kotlin/Gradle build workflow matches user's phone-only developer constraint.** Google declared Compose First in May 2026. JetBrains offers stable Kotlin Multiplatform/Compose Multiplatform for later shared business/UI code on iOS, Windows and macOS; the additional target toolchains remain future-stage issues, and later migration is not guaranteed zero-work.

**This decision SUPERSEDES earlier temporary proposal to test Dart→JavaScript/Capacitor proof on `spike-android-dart-web`, and the Kotlin toy APK first gate.** The spike branch may remain for historical preservation only; **DO NOT ask the user to run its test commands or write any separate toy app**. Next deliverable is REAL Atelier Time native Android Kotlin/Compose source with normal build/test as part of implementation, not a feasibility experiment. Keep current verified Dart scheduling engine (35 passing tests), Dart input adapter (11 passing tests), approved Example A/B fixtures, D-031/036/037/044 and current unmerged branches intact. Kotlin port of Dart algorithms will require parity tests. **No Kotlin app/APK has been built yet; don't claim one exists.** No paid CI/Actions or automatic PR merge.

[Decision D-067](DECISIONS.md) is authoritative. Official references: [Google Compose First](https://android-developers.googleblog.com/2026/05/android-ui-development-is-compose-first.html), [Kotlin Android](https://kotlinlang.org/docs/android-overview.html), [JetBrains KMP FAQ](https://kotlinlang.org/docs/multiplatform/faq.html), [CoGo release history](https://github.com/appdevforall/CodeOnTheGo/releases), [independent r/termux APK builds discussion](https://www.reddit.com/r/termux/comments/1v1eew4/building_apks_via_termux/).

---

## LATEST FEASIBILITY SPIKE — Preserve Dart core, verify browser on phone first (D-064)

**Requirement:** User now prioritizes the easiest reliable Android-phone development, test, build, install cycle. Efficient later iOS/Windows/macOS port is architectural, not an immediate multi-platform build gate (D-062). Two honest options: (A) **Dart web + native Capacitor wrapper** preserves the tested scheduler and input adapter and may build an Android APK on Termux using a community recipe; (B) **Kotlin/Code on the Go** offers a maintained Android-native phone IDE and future KMP/Compose but requires a full Dart logic port verified against 35+11 tests. Neither phone APK workflow has passed on this device. **D-063 Kotlin candidate remains documented, but D-064 ranks the small Dart reuse proof first to avoid premature rewriting**.

**New isolated [spike-android-dart-web](https://github.com/AviY99/Atelier-Time/tree/spike-android-dart-web):** `spikes/android_dart_web/` has `pubspec.yaml` depending on local `atelier_time_core` and `package:web`, `web/main.dart` with real Example B schedule/offset assertions, `www/index.html`, and `tool/serve.dart` local HTTP server, plus README. **These source files were committed but neither Dart-to-JS compilation nor browser execution has been verified.** No APK or Android background audio. Do not claim success.

**Safe next device proof (NO new SDK install):**
```bash
git -C ~/Atelier-Time-Stage2 fetch origin spike-android-dart-web
git -C ~/Atelier-Time-Stage2 worktree add -b spike-android-dart-web ~/Atelier-Time-Spike FETCH_HEAD
cd ~/Atelier-Time-Spike/spikes/android_dart_web
dart pub get
dart compile js -O1 -o www/main.dart.js web/main.dart
dart run tool/serve.dart
```
In Android browser open `http://127.0.0.1:8765`. Expected *if proof works*: PASS and Example B long break at 01:25–01:40 and final summary at 02:45–03:00. **If command fails, record and fix errors; do not pretend pass.** Leave `~/Atelier-Time` and `~/Atelier-Time-Stage2` untouched. If branch/worktree already exists, stop rather than force.

**Later APK feasibility:** A [community Termux Capacitor build recipe](https://github.com/rayzery/termux-capacitor-apk) uses JDK21/Android SDK/aapt2 Gradle; not official support, tested on only a contributor's device. **Do not install this heavyweight toolchain** until basic Dart-JS browser reuse is verified. Even an APK alone cannot certify native accurate alerts behind lock; test that separately with native APIs.

**Alternative:** [Code on the Go](https://appdevforall.org/code-on-the-go/), active ARM64 Android IDE for Kotlin/Java, then Jetpack Compose and later Kotlin Multiplatform, with full Dart domain port and parity tests required. Assess if web wrapper/native timing cannot be made reliable. Preserve user requirements and old branches, no silent migration.

**Docs:** [Android-first spike plan](ANDROID_FIRST_PLATFORM_SPIKE.md), [Kotlin alternative](ANDROID_FIRST_DEVELOPMENT_PLAN.md), chronological [D-062 through D-064](DECISIONS.md).

---

## HISTORICAL KOTLIN CANDIDATE — Android developed ON THE PHONE FIRST (D-062/D-063)

**Exact user revision:** “בקיצור , הדרישה תשתנה , פיתוח לאנדרואיד על הטלפון מהיר ויעיל שניתן אחכ להסב ביעילות לסביבות אחרות”. The user changes the requirement (not merely asks for options): **quick, efficient Android development and APK build on the Android phone now**, with efficient later expansion to iOS/iPadOS, Windows and macOS. All four must no longer be built in the first release. Approved offline/scheduling/timer/audio product behavior is unchanged.

**Research / preferred candidate:** Actively maintained [Code on the Go](https://appdevforall.org/code-on-the-go/) can create, build, debug and install Android Java/Kotlin applications entirely on an Android ARM64 phone. The latest Oct 20, 2026 [GitHub release is 26.43](https://github.com/appdevforall/CodeOnTheGo/releases/tag/26.43). Its older predecessor AndroidIDE was archived in 2024; **do not recommend AndroidIDE**. CoGo's compatibility guide says Android 9+, ARM32/ARM64, approx 3.5 GB free storage / 1.5 GB free RAM minimum (4 GB free recommended), subject to actual device. CoGo 26.40 release notes explicitly mention Compose Preview plugin; actual project/app Compose build compatibility on this phone still **UNTESTED**.

**Future portability:** [JetBrains Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform/faq.html) and Compose Multiplatform support shared logic and potentially shared UI across Android, iOS, Windows, macOS. This is a later migration path, not a claim CoGo can locally produce all four app artifacts. Avoid Android-only APIs in domain; keep persistence, timer/audio as adapters.

**Crucial legacy assets:** Original Dart engine is verified 35/35 and input adapter 11/11; Kotlin **does not directly reuse Dart implementation**. Porting core will take work and must match all existing fixtures/safety properties. Do not discard code or merge old PRs. User approved **new delivery requirements**, not a complete untested code migration.

**One short engineering proof required before migration:** Install official 64-bit Code on the Go, create a minimal Kotlin Android app, build/debug **APK entirely on the user's device**, install/run it. If Compose supported verify minimal screen; otherwise note UI tradeoff. Record actual version/build outcome, time and device constraints. **Only if it works**, create separate proof branch for minimal scheduling Example B and parity test; no rewriting entire engine before proof. No paid Actions/cloud build.

**Next assistant action:** Explain the recommended CoGo installation from official site [appdevforall.org/code-on-the-go](https://appdevforall.org/code-on-the-go/) and request only the build outcome of one minimal default sample. Do not request Dart/Flutter reinstall, flood screenshots or rebuild all 46 Dart tests. Maintain original Termux files. Read [ANDROID_FIRST_DEVELOPMENT_PLAN.md](ANDROID_FIRST_DEVELOPMENT_PLAN.md) for exact criteria. Project-specific implementation coding is on hold until the Android-on-phone compiler proof; after success, continue user-approved Android-first development in separately recorded stage, preserving test oracle assets. Do not start KMP full multi-target Gradle project on phone.

---

## Historical D-061 full-lifecycle decision (superseded in priorities by D-062) — full-lifecycle platform suitability D-061 (2026-10-23)

**User escalation:** “תריץ את הבחינה הראשונית שוב ברור שהיא נדרשת להיות קרוס פלטפורם בכל השלבים מהפיתוח ועד הישום” — re-run the initial assessment because cross-platform was required at every stage from development to implementation. This is **not** a request to continue cranking out Flutter UI code or to install an unofficial Flutter binary in Termux.

**Engineering audit completed and committed:** [PLATFORM_REASSESSMENT_2026_10_23.md](PLATFORM_REASSESSMENT_2026_10_23.md), updated TD-001, decisions D-061, architecture, product scope, roadmap and Stage 2 gate.

**Independent source verification:**
- Flutter official FAQ: developers use Windows, macOS, Linux, ChromeOS; **Android is not an officially supported Flutter development host**. Flutter deployment supports Android, iOS, Windows, macOS; these must not be confused.
- Flutter official build docs: Apple iOS builds need macOS/Xcode; Windows/native macOS packages require relevant platform toolchains.
- Tauri and Capacitor similarly rely on OS-target toolchains for native packages; moving languages does not eliminate Apple restrictions.
- Offline **PWA** could be installed through a browser on modern target systems and developed/tested using Node + a supported browser on Termux, but **PWA is not a separate native IPA/EXE/DMG package**, requires initial browser install pathway, and its background timers/audio under lock are not reliably equivalent to a native runtime. MDN documents throttled timers when the page is hidden. Do NOT present PWA as a confirmed drop-in.
- A remote/macOS/Windows build infrastructure could allow cross-platform *operation of the workflow* from Android but it adds external host/build costs and still must be proven, while GitHub Actions account budget is reported blocked.

**Required acceptance before next platform/code stage:** F01 single app feature parity; F02 supportable development and preview workflow from Android Termux/other user environments; F03 runnable tests; F04 genuinely documented installation path/verified signed native artifacts for all 4 original targets OR explicit user approval to change definition; F05 offline presets; F06 reliable elapsed clock & 3 alerts under device-lock/background; F07 predictable no-surprise costs; F08 all canonical scheduling invariants unchanged.

**Current finding:** For strict *fully local, Android-only, native signed builds for all four OSs* there is **no verified option**: Apple's iOS toolchain requires macOS/Xcode. If user permits remote per-OS builders, Flutter/web-wrapper could become candidates but that needs proof, cost approval and developer environment. If browser-installed PWA is acceptable instead of each native installer, web-first could become candidate, but offline timer/audio and lock-screen behavior need experimental proof.

**STOP LINE:** Original TD-001/D-019 not erased; its sufficiency for the stronger lifecycle criterion is **WITHDRAWN FOR RE-REVIEW**. **Freeze further Flutter UI feature coding**, avoid PR merges / paid CI / extra installation instructions, and **do not choose a new stack without user approval**. Keep existing 35/35 Dart scheduling and 11/11 Dart adapter tests as validated assets. No Stage 3+, no production build.

**Next work:** Clarify only the genuine contradiction in requirements (signed native build from every host vs workflow remotely operable from any host; native packages vs installed PWA; lock-screen alert guarantee), run a *minimal feasibility spike* only once the scope is approved, and log working commands, actual artifacts and costs. Document failures before recommending another platform. The user should not be asked for more screenshots of the old analyzer flood or to reinstall Flutter.

---

## Latest Stage 2 analyzer failure / isolated SDK scopes (D-060, 2026-10-23)

**User message (Hebrew):** “זה רק עשירית מהשגיאות” (this is only a tenth of the errors). Attached extremely narrow tall terminal screenshot of large Dart analyzer diagnostic output after trying `dart analyze && dart test` on the verified `~/Atelier-Time-Stage2` checkout. Do not ask for hundreds more screenshots or say the Dart tests failed. With `&&`, **`dart test` DID NOT run after the nonzero analyzer exit**.

**Root cause indicated by verified repository structure:** Root `pubspec.yaml` defines a **pure Dart** `atelier_time_core` package with `test` dependency. Nested `apps/atelier_time_flutter/pubspec.yaml` is a separate Flutter package needing `flutter: sdk: flutter` and `flutter_test: sdk: flutter`. User's Android Termux has **Dart SDK ONLY**, so analyzer cannot resolve these Flutter SDK dependencies if it traverses nested UI files. The screenshot resolution is too small to verify each particular diagnostic; do not guarantee all errors are Flutter-generated until post-fix analysis.

**Stage 2 code-only fix committed on remote branch:**
- [root analysis_options.yaml](https://github.com/AviY99/Atelier-Time/blob/stage-2-session-editor/analysis_options.yaml) includes `analyzer: exclude: - apps/atelier_time_flutter/**`, so bare-Dart root analysis should cover core/adapter/tests/tools without traversing the nested Flutter application.
- [nested Flutter analysis_options.yaml](https://github.com/AviY99/Atelier-Time/blob/stage-2-session-editor/apps/atelier_time_flutter/analysis_options.yaml) has independent scope (exclude only build/generated cache), keeping UI/widget tests analyzable later with a **real Flutter SDK** from the app directory. This is not an attempt to hide Flutter errors from Flutter analysis.
- **No scheduling engine, adapter or Flutter behavior changed**. Official documentation: https://dart.dev/tools/analysis.

**NO post-fix Dart analyzer/test results yet.** The earlier Stage 1 35/35 and clean Stage 2 adapter 11/11 tests remain separately verified. Eight Flutter widget tests still not executed. The root suite should discover 35+11=**46 tests**, but NEVER claim 46 PASS before user reports exact output. No GitHub Actions/paid services, APK or Stage 3+.

**Next Android Termux command (existing clean worktree):**
```bash
cd ~/Atelier-Time-Stage2
git pull --ff-only
dart analyze && dart test
```
If error persists, ask only for the first **10–20 lines** (or an image showing first error), not the entire flood. Alternative targeted diagnostics are `dart analyze lib` and `dart analyze test` (avoids UI subtree) if the root exclusion behaves unexpectedly; resolve any genuine Dart package errors separately. The nested Flutter UI will still require proper Flutter tooling. Update this handoff after the result.

---

## Latest Stage 2 clean worktree verified (D-059, 2026-10-23)

**User supplied new Termux screenshots:** A separate `~/Atelier-Time-Stage2` folder exists and `git rev-parse --short HEAD` output is **`3bb2bad`**. Running `dart pub get && dart test test/editor_draft_test.dart` in this folder downloaded dependencies successfully and ended **`00:01 +11: All tests passed!`**.

**GitHub cross-check:** `compare_commits` with base `3bb2bad` and head `stage-2-session-editor` returned `status: identical`, `ahead_by: 0`, `behind_by: 0`, full SHA **`3bb2bade992fddab7dc3d7ece31d9aa58231380f`**. This **resolves D-057/D-058**: the 11 independent Stage 2 Dart adapter tests have now truly passed on the correct latest checked-out Stage 2 code, not merely old Stage 1 with staged additions. Original `~/Atelier-Time` mixed/staged worktree remains preserved; **do not reset/clean it**.

**Verification scope:** Stage 1 pure Dart core 35/35 tests passed separately earlier (D-052). Stage 2 parser tests **11/11 screenshot-verified PASS** at exact Stage 2 HEAD. **Full Stage 2 root `dart analyze && dart test` and eight Flutter widget tests have NOT been verified on this revision.** Flutter UI still requires a proper Flutter SDK, and no Flutter platform project/APK/installer exists.

**Budget-free next step:** Using the **already working** clean Stage 2 folder, run:
```bash
cd ~/Atelier-Time-Stage2
dart analyze && dart test
```
The full root Dart test discovery should cover both core scheduling and the editor adapter, but **do not claim combined pass until actual output received**. This does NOT run the nested Flutter UI tests. No GitHub Actions, paid builds, PR merges, or Stage 3/4 work.

---
## Latest Termux diagnosis — D-058: staged Stage 2 files on Stage 1 HEAD (2026-10-23)

**Full second screenshot evidence (read-only user commands):**

```text
## stage-1-4-scheduling-core...origin/stage-1-4-scheduling-core
A  apps/atelier_time_flutter/README.md
A  apps/atelier_time_flutter/lib/main.dart
A  apps/atelier_time_flutter/lib/session_editor_page.dart
A  apps/atelier_time_flutter/pubspec.yaml
A  apps/atelier_time_flutter/test/session_editor_widget_test.dart
M  docs/STAGE_1_4_PROGRESS.md
M  lib/atelier_time.dart
A  lib/src/editor_draft.dart
A  test/editor_draft_test.dart
?? pubspec.lock
git branch --show-current -> stage-1-4-scheduling-core
git rev-parse --short HEAD -> 907f871
git ls-files test/editor_draft_test.dart -> test/editor_draft_test.dart
```

**Meaning:** `git switch --track` failed previously and left Stage 2 contents staged on the Stage 1 checkout. `git ls-files` includes staged additions, so it does NOT mean the test file is committed in Stage 1 HEAD. The prior `+11 All tests passed!` is a genuine local test run using Stage 2 files, but **not validated on a clean Stage 2 Git branch**. Do not tell the user the test failed or that switch succeeded.

**Safest next instruction: create an entirely separate Stage 2 worktree while preserving existing dirty/staged and untracked files**. From Termux:
```bash
git -C ~/Atelier-Time fetch origin stage-2-session-editor
git -C ~/Atelier-Time worktree add -b stage-2-session-editor ~/Atelier-Time-Stage2 FETCH_HEAD
cd ~/Atelier-Time-Stage2
git status --short --branch
git rev-parse --short HEAD
dart pub get && dart test test/editor_draft_test.dart
```
If worktree path/local branch already exists, **STOP and inspect**; never force/delete. The first original `~/Atelier-Time` and its staged files are untouched by this approach. Only after clean checkout succeeds may report a verified Stage 2 commit/test pass. The 8 Flutter widget tests still require Flutter, not available in Termux. No paid GitHub Actions, merge or Stage 3+.

---
## New screenshot evidence and Git checkout issue — D-057 (2026-10-23)

**User terminal screenshot shows:**
- `git pull --ff-only && dart test` on `stage-1-4-scheduling-core`, `+35: All tests passed!` (historical previously verified Stage 1);
- `git fetch origin stage-2-session-editor:refs/remotes/origin/stage-2-session-editor` succeeded, showing remote ref `origin/stage-2-session-editor`;
- `git switch --track origin/stage-2-session-editor` **FAILED**, fatal `cannot set up tracking information; starting point 'origin/stage-2-session-editor' is not a branch`;
- Despite switch failure, `dart pub get && dart test test/editor_draft_test.dart` says `Got dependencies!` then **`00:00 +11: All tests passed!`**.

**Correct reading:** real 11-test suite PASS in the user's local filesystem, but no proof that the Git HEAD/branch changed to Stage 2. Tool lookup confirmed `test/editor_draft_test.dart` is **present on GitHub Stage 2**, **absent from GitHub Stage 1**, so the user's local file state is uncertain; do not assert impossible details or silently assume success/failure of branch migration.

**Safe next step:** Ask user to run only non-mutating Git commands from `~/Atelier-Time`:
```bash
git status --short --branch
git branch --show-current
git rev-parse --short HEAD
git ls-files test/editor_draft_test.dart
```
Based on output, safely establish a local Stage 2 branch using previously fetched commit (e.g., if working tree clean and no local branch exists, `git fetch origin stage-2-session-editor` followed by `git switch -c stage-2-session-editor --no-track FETCH_HEAD`; confirm with `git branch --show-current`, `git rev-parse --short HEAD`). **Do not switch/overwrite untracked files blindly.** Only then attribute a new `dart test test/editor_draft_test.dart` PASS to the verified Stage 2 commit. No Flutter widget tests have run; user does NOT need SDK reinstall or GitHub Actions.

## Latest Stage 2 progress — D-056 plain-Dart editor adapter (2026-10-23)

**User instruction:** “Can you continue?” Continued the existing authorized Stage 2 branch. No change to approved scheduling algorithm or later-stage scope.

**Branch source committed:**
- [lib/src/editor_draft.dart](https://github.com/AviY99/Atelier-Time/blob/stage-2-session-editor/lib/src/editor_draft.dart): new Flutter-independent `SessionEditorDraft` and `OpeningGroupDraft` models, with immutable text input snapshots. They parse whole-minute fields, ordered opening groups, trimmed session name, cycle order, and active manual special-break ordinal text into existing `SessionRules`.
- [lib/atelier_time.dart](https://github.com/AviY99/Atelier-Time/blob/stage-2-session-editor/lib/atelier_time.dart): public export of the adapter alongside the untouched scheduling engine.
- [Flutter editor](https://github.com/AviY99/Atelier-Time/blob/stage-2-session-editor/apps/atelier_time_flutter/lib/session_editor_page.dart): delegates `_readRules()` to `SessionEditorDraft.toRules()`, avoiding duplicate form-parsing logic. `N=0` requested special breaks correctly ignores inactive manual-ordinal text even if stale/invalid; active ordinal parsing remains strict. Zero and negative configured durations reach the scheduler to be reported as hard validation errors, NOT silently corrected.
- [test/editor_draft_test.dart](https://github.com/AviY99/Atelier-Time/blob/stage-2-session-editor/test/editor_draft_test.dart): **11 new pure Dart test SOURCES** for canonical B mapping, custom field values, D-036 zeros, invalid text, opening groups order, manual ordinals, inactive ordinal case, D-044 shorter break and D-031 warning.

**Verification:** GitHub file read-back and a primitive bracket/quote check succeeded, but these do **not constitute Dart compilation**. **The 11 new plain-Dart tests have NOT run.** User has Termux Dart 3.13.7 and can now verify the Stage 2 adapter without installing Flutter. The original 35 tests passed earlier on the Stage 1 branch and remain valid there. EIGHT Flutter widget tests still await a real Flutter SDK and **have NOT run**. No Android APK.

**Budget-free next verification, optional user action:** From the existing Termux `~/Atelier-Time` folder currently on Stage 1 branch, switch once to Stage 2:

```bash
git fetch origin stage-2-session-editor
git switch --track origin/stage-2-session-editor
dart pub get
dart test test/editor_draft_test.dart
```

If the branch is already local, `git switch stage-2-session-editor` instead of repeating `--track`. Ask for terminal result to fix genuine Dart errors. **Do not run `flutter test` or claim Flutter UI validation from this check.** The nested Flutter source still requires a proper Flutter toolchain. No Actions, merges, app deployments or Stage 3–6.

---

## Latest Stage 2 source / tests update — D-055 (2026-10-23)

**User instruction:** “continue” followed earlier authorized Stage 2 work. Continued only existing Stage 2 editor/preview branch; no new policy or release authorization assumed.

**Changes on [stage-2-session-editor](https://github.com/AviY99/Atelier-Time/tree/stage-2-session-editor):**
- [Flutter editor source](https://github.com/AviY99/Atelier-Time/blob/stage-2-session-editor/apps/atelier_time_flutter/lib/session_editor_page.dart) now gives controls stable `ValueKey` identifiers, shows malformed numeric inputs as explicit **blocking input errors**, hides partial/unapprovable timeline phases on engine validation errors, and keeps the full phone editor/preview mounted in one scrollable page. Wide screen remains two-pane.
- `apps/atelier_time_flutter/pubspec.yaml` now has a `flutter_test` SDK dev dependency.
- [Eight Flutter widget tests](https://github.com/AviY99/Atelier-Time/blob/stage-2-session-editor/apps/atelier_time_flutter/test/session_editor_widget_test.dart) **WRITTEN, NOT RUN**: T19 0/non-numeric and 0 normal break validation; T20 exact Example B long break 01:25–01:40 and summary 02:45–03:00; T19 editable/removable opening groups; T21 ending remainder/even/custom slider and revision-invalidated approval; T22 qualifying-rest warning acknowledgement and reset; T23 wide two-column layout.
- Widget 25/5 warning fixture uses a **positive 51-minute complete session, 0 reserved summary**, 25m pose + 1m normal break + 25m pose; the 0 summary is legal under D-037, and **0 configured breaks or complete sessions remain invalid**.
- Pure Dart scheduling core untouched; its **35/35 screenshot-verified passing tests** (D-052) are unaffected. No test rerun needed until source/core changes require it.

**Verification limit:** Assistant runtime has no Flutter SDK (`flutter` and `dart` absent); user's Android Termux has **bare Dart 3.13.7**, which is NOT enough to compile Flutter widgets. No `flutter pub get`, `flutter analyze`, widget tests, APK, emulator execution or target OS builds have run. Treat structural source review as **not compilation**. Do not claim eight widgets passed. **Stage 2 IN PROGRESS, NOT COMPLETE.** No GitHub Actions/paid resources, no merges, no Stage 3+.

**Safe next work:** Use [STAGE_2_PLAN.md](STAGE_2_PLAN.md) and [Stage 2 branch README](https://github.com/AviY99/Atelier-Time/blob/stage-2-session-editor/apps/atelier_time_flutter/README.md); obtain a Flutter-capable free test environment, run `cd apps/atelier_time_flutter && flutter pub get && flutter analyze && flutter test`, fix real errors, then discuss provisional visual style with the user before finalizing. Never re-request already authorized Stage 2 initiation or ask them to repeat all 35 passing Dart core tests.

---

## Stage 2 continuation — current active work (D-053/D-054, 2026-10-23)

**The user explicitly directed:** “What's going on u dont respond to actions ??? I want you to finish documentation and continue”. This follows completed D-052 Stage 1 and authorizes beginning Stage 2 source/plan now (D-053). Do not ask again to start Stage 2 design/source. Final visual styling and branding have NOT been chosen; use provisional neutral appearance and request a review when available.

**GitHub integration and isolation:**
- **Stage 1 verified Dart core**: [PR #1](https://github.com/AviY99/Atelier-Time/pull/1) proposes adding the tested package to `main`; **open, not merged**, no Actions invoked. The main branch contains authoritative documentation and separate commits. Avoid automatic merges or bypassing the review gate.
- **Stage 2 draft Flutter editor**: [branch `stage-2-session-editor`](https://github.com/AviY99/Atelier-Time/tree/stage-2-session-editor) based on `stage-1-4-scheduling-core`, [draft PR #2](https://github.com/AviY99/Atelier-Time/pull/2) targets the core branch. This stacking preserves the 35 tested Dart core regressions without needing an immediate merge.
- **Requirements/design contract**: [STAGE_2_PLAN.md](STAGE_2_PLAN.md) on `main`. Initial nested Flutter app code: `apps/atelier_time_flutter/pubspec.yaml`, `lib/main.dart`, `lib/session_editor_page.dart`, and README on Stage 2 branch. Flutter's path dependency points `../..` to the tested Dart core package.

**Stage 2 source implemented (NOT run):** responsive one-column phone / two-pane wide input and preview screen, per-session name and total/summary, positive pose/regular/special break lengths and special break count, editable ordered opening groups with add/remove/reorder, pose-first/break-first, automatic/manual special break ordinals, exact chronological preview phases and totals, errors distinct from 25/5 advisory warnings, remainder/even/custom final-pose choice with 1m slider resolution, separate safety acknowledgement, explicit in-memory approved snapshot invalidated on any edit. This prototype **cannot save, activate or time** sessions; those belong to Stage 3/4. Material 3 colors/layout are provisional, not user-approved branding.

**Verification:** Connector read-back verified Flutter source files were committed; basic lexical delimiters were balanced in Dart UI files. **NO Flutter SDK in assistant environment, NO `flutter pub get`, `flutter analyze`, widget tests, emulator run, platform scaffolding, APK or installed app**. Android Termux has **Dart only**, which does NOT run the Flutter UI; do not ask user to rerun 35 Dart tests or install unrelated tools without a specific next testing task. Stage 2 T19–T23 tests are **PLANNED, NOT RUN**.

**Next safe work within authorized Stage 2:** review UI source/acceptance checklist; add appropriate widget-test source if useful; obtain Flutter-capable testing environment without paid GitHub Actions; run Flutter analyzer/widget tests and correct actual issues; discuss appearance/logo when ready. **Do NOT begin Stages 3–6 or auto-merge PRs** without distinct permission. Update [docs/HANDOFF.md](HANDOFF.md) after each authorized iteration.

---

## Stage 1 Step 1.4 — COMPLETED/VERIFIED on development branch (D-052)

**Authoritative latest milestone:** User's Android Termux screenshot (2026-10-23) includes:

```text
~/Atelier-Time $ git pull --ff-only && dart test
...
8cf7a24..907f871  stage-1-4-scheduling-core -> origin/stage-1-4-scheduling-core
Fast-forward
  docs/STAGE_1_4_PROGRESS.md
  test/scheduling_test.dart
...
00:00 +35: All tests passed!
```

- This is **screenshot-confirmed 35/35 passing Dart unit tests on Android ARM64 Dart 3.13.7** on the branch after adding required T09 valid custom first=1m / last=34m edge case. Earlier screenshot-confirmed `dart analyze` **No issues found!** refers to the immediately preceding 34-test version, before test-only changes; earlier smoke test also passed.
- **Minimum Stage 1 test gate fulfilled:** T01–T12, T15–T18 map to the 35 passing automated cases, including canonical A/B exact 3h sessions, break replacement, three final-pose choices and both 1m split endpoints, positivity checks, 25/5 advisory warning+acknowledgment, deterministic contiguous schedules. Wider T13/T14 edge-matrix combinations **remain only partially tested** and belong to future QA; do not claim exhaustive inputs, other operating systems or Flutter application verification.
- **Stage 1 Step 1.4 COMPLETE ON ISOLATED BRANCH** [`stage-1-4-scheduling-core`](https://github.com/AviY99/Atelier-Time/tree/stage-1-4-scheduling-core); [engine](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/lib/src/scheduling.dart), [35 tests](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/test/scheduling_test.dart), [branch verification log](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/docs/STAGE_1_4_PROGRESS.md). **No merge to `main`** yet. Stage 2 editor/preview work was subsequently authorized by D-053 and started as source-only prototype (D-054); Stage 3 presets, Stage 4 timer/audio and builds remain separate.
- **HISTORICAL GATE (D-052, since superseded by D-053):** Stage 1 stopped after verification awaiting explicit next instruction. The user has since told us to “finish documentation and continue,” authorizing Stage 2 early editor/preview work, not a merge or paid Actions. Do not ask user to reinstall Dart or rerun the already proven 35 core tests.

**Historical notes below are preserved for change history. Any “NOT RUN,” “IN PROGRESS,” “T09 pending,” or “SDK unavailable” in earlier dated sections refers to earlier iterations only and is superseded by this current result.**



## Historical iteration D-046 — SDK blocker diagnosis and local test helpers

**User question:** “What to do to solve ur failure? Can you continue?” YES: continued **only** authorized Step 1.4 source hardening and test preparation, not Stage 2 or billing-dependent Actions.

**Environment/root cause:** In assistant Linux container, `dart` and `flutter` are absent and none found in local installations. Command-line DNS for `storage.googleapis.com`, `github.com`, `dart.dev` fails; `curl` cannot fetch SDK binaries. This is a **missing test toolchain / network access problem**, not a confirmed Dart engine compile/test failure.

**Completed on isolated branch** [stage-1-4-scheduling-core](https://github.com/AviY99/Atelier-Time/tree/stage-1-4-scheduling-core):
- Engine [lib/src/scheduling.dart](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/lib/src/scheduling.dart): made `ScheduleCandidate._` constructor private to its Dart library; callers cannot construct an approvable fake schedule. `ApprovedSession` now contains immutable full `acknowledgedWarnings` (including actual elapsed time) and keeps `acknowledgedWarningCodes` for UI access.
- Expanded [test/scheduling_test.dart](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/test/scheduling_test.dart) with **unexecuted** test checking preserved warning timestamp and immutable lists.
- Added [tool/verify.ps1](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/tool/verify.ps1) (Windows PowerShell) and [tool/verify.sh](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/tool/verify.sh) (macOS/Linux Bash) plus `.gitignore`. These scripts run `dart --version`, `dart tool/smoke.dart`, `dart pub get`, `dart analyze`, `dart test`, and write `atelier-time-verification.log`; they do NOT use GitHub Actions.
- **NO source was merged into `main`; NO Dart test/analyzer/compiler/smoke execution took place** because SDK is missing. No paid runners, CI, builds, or budget changes. **Step 1.4 remains IN PROGRESS**.

**Minimal user-assisted unblock when necessary:** Install **ONLY the Dart SDK** from https://dart.dev/get-dart on a computer (Flutter is NOT needed to test this pure Dart scheduling core); download/clone the `stage-1-4-scheduling-core` branch; run `powershell -ExecutionPolicy Bypass -File tool\verify.ps1` on Windows from repo root, or `bash tool/verify.sh` on macOS/Linux; share `atelier-time-verification.log` for fixes. Installing Dart on the user's machine **does not** install it inside the assistant's container. No passwords or secrets should be shared.

**Next code gate:** Fix actual errors revealed by Dart execution before declaring Step 1.4 complete. Continue this step without another authorization; **Stage 2 and other activities remain blocked**.

## Historical iteration D-051 — T09 35th regression added (subsequently PASSED D-052)

- The user supplied screenshots showing *prior 34* unit tests PASS and Dart analyzer 0 issues on Android Termux; those results remain verified.
- The Stage 1 Step 1.4 acceptance check required both positive custom-pose ruler extremes. Test 34m+1m was already passing, but the inverse **1m+34m** endpoint was missing.
- **New 35th test on development branch** [commit `4b2f5d9`](https://github.com/AviY99/Atelier-Time/commit/4b2f5d9fdd8dabc6dea8dcc0204c5062c7b6916a) asserts Example A 125–126m first pose, 126–131m rest, 131–165m final pose, 165–180m reserved summary; invalid 0m case remains blocked and 25/5 warning still requires acknowledgment.
- The **35th test has NOT run**. One budget-free Termux rerun is needed: `git pull --ff-only && dart test`. Do not claim 35 PASS until evidence received. No GitHub Actions, main merge, UI/Android APK or Stage 2 work.

## Historical verified checkpoint — Android Dart 34/34 tests PASS (D-050, superseded by 35/35 D-052)

**User submitted three actual Termux screenshots** after running `dart pub get && dart analyze && dart test` on **Dart SDK 3.13.7 (Android ARM64)** in the `~/Atelier-Time` working tree on `stage-1-4-scheduling-core`. Visible final output:

```text
Changed 49 dependencies!
Analyzing Atelier-Time...
No issues found!
Built test:test.
00:01 +34: All tests passed!
```

**Test evidence is SCREENSHOT-VERIFIED:** Dart analyzer reports zero issues and all 34 written unit tests have passed. Earlier smoke script also reported `Scheduling smoke checks succeeded.` following the D-047 integer-type fix. This supersedes earlier “not run,” “retest pending,” or “user-reported only” statuses appearing as historical notes below or in older docs. The screenshots do not display the exact local checked-out commit SHA, so do not invent it; no non-Android Dart or Flutter UI/build verification has been performed.

**Current Stage 1 acceptance audit:** The original 34 passing tests cover canonical Example A/B, three final-pose choices, deterministic/contiguous timing, replacement breaks, hard zero rules and 25/5 warnings. Required T09 missing 1m+34m counterpart **was added as the 35th test** in branch commit `4b2f5d9fdd8dabc6dea8dcc0204c5062c7b6916a` (D-051); existing 34m+1m and 0m rejection were already covered. **This 35th test is UNEXECUTED**, so screenshot proof is for prior 34 only. User can run `git pull --ff-only && dart test` on Android Termux. T13/T14 wider edge matrix remains partial; defer broader QA decisions without inventing new requirements. No Stage 2 authority has been granted. No GitHub Actions/paid work, no merge to `main`. **Stage 1 Step 1.4 = final QA review, not completed.**

### Immediate next task after this handoff

The **35th T09 endpoint test is already committed** on `stage-1-4-scheduling-core`. Ask the user to run `git pull --ff-only && dart test` in their existing Android Termux `~/Atelier-Time` directory, then inspect/pass/fix new evidence. Do not ask for Dart reinstallation. Keep main and all later Flutter/UI stages untouched until separately approved.

## Historical iteration D-047 — initial compile failed and was fixed

**User has succeeded installing Dart on Android via Termux**: Dart SDK 3.13.7 stable, `android_arm64`. User cloned branch `stage-1-4-scheduling-core` into Termux's `~/Atelier-Time` and ran **`dart tool/smoke.dart`**.

**Real observed test outcome on old branch:** **Dart COMPILE FAILED** at `lib/src/scheduling.dart:621:28` with `The argument type 'num' can't be assigned to the parameter type 'int?'`. The warning's `offsetSeconds:` argument was `crossingOffset`, which had type `num` because `math.max(0, _maxUnbrokenPoseSeconds - previousAccumulated)` was not inferred as int in user's Dart 3.13.7 compiler.

**Fix already committed to branch**: [commit `0dedd95756b6d3984b2e16548a58599179fbf7ff`](https://github.com/AviY99/Atelier-Time/commit/0dedd95756b6d3984b2e16548a58599179fbf7ff) replaces `math.max` with integer-only condition `secondsUntilThreshold > 0 ? secondsUntilThreshold : 0`, preserving exact warning threshold. D-047 logged.

**CRITICAL:** The code **has NOT been retested since this fix**. No smoke pass, no `dart analyze` or `dart test` pass yet. Do not claim compile/test success. The older failure is now a real engineering finding, not just missing SDK.

**Next user instruction:** They are **already in `~/Atelier-Time` on Termux**, so no reinstall or re-clone required. Run:
```bash
git pull --ff-only && dart tool/smoke.dart
```
If smoke succeeds, run `dart pub get && dart analyze && dart test`, share output/error; if any command fails, continue source corrections within already-authorized Step 1.4. No Actions/paid runners, no main merge, no Stage 2.

## Latest mandatory precedence — 0-minute sessions/breaks

**D-036 (2026-10-22) is the LATEST approved clarification and overrides any contrary older note:**

> User: “You are correct, Break of 0 or session of 0 is not valid.”

Therefore:
- A **complete session total MUST be >0**. A 0-minute session is a **hard validation ERROR** and cannot be preview-approved, saved or activated.
- Every **configured actual regular/short break or active long/special replacement break MUST have duration >0**. A break lasting 0 minutes is a **hard validation ERROR**; it cannot be waived by the 25/5 advisory warning mechanism. In the planned whole-minute editor, the smallest valid configured break is **1 minute**.
- New-session input defaults: **normal/short break 5 minutes**; **long/special replacement break 15 minutes**. Both remain independently editable to **other POSITIVE values**.
- The previous D-031 approval of 0-minute breaks and the follow-up proposal for “logical 0-minute break slots” are **SUPERSEDED/WITHDRAWN**. Do not implement them. Old work log, changelog or superseded decision text is history, **not current authority**.
- **APPROVED D-037:** Zero **number** of optional special replacements means none are scheduled (NOT a 0-duration break). A zero-duration **reserved final summary** `F=0` is permitted when the complete session and work block are positive; it is different from an invalid complete-session duration `D=0`.

**D-031 remains APPROVED insofar as it defines the 25/5 advisory warning:**
- After **25 minutes of accumulated posing without a qualifying break**, recommend a **>=5-minute rest** before posing continues. Add together durations of consecutive poses, including consecutive 1-minute or 4-minute opening poses; simply reaching a pose boundary does **not** reset the counter.
- Only a **break >=5 minutes** resets the posing counter. A **valid positive 1–4-minute break does not** reset it. A single pose running past 25 minutes also triggers a warning.
- A legal schedule violating the 25/5 recommendation shows a clear **WARNING** and requires **explicit separate user acknowledgment plus ordinary schedule approval** before saving/activating. Never silently alter entered numbers.
- An edit, placement change or final-pose split change invalidates the prior warning acknowledgment and ordinary approval; recompute from the final full timeline.
- A **0-minute break/session is an ERROR, not a warning**: a user cannot override it.
- **APPROVED D-037:** Accumulated posing/rest-warning counter is **within one complete activated session**; do **not automatically carry it across separately activated saved presets**. A full session ending at **exactly 25 minutes** of uninterrupted accumulated posing needs no invented trailing break; an uninterrupted pose exceeding 25m still warns. Separate cross-preset carryover would be a new feature requiring separate approval.

## User purpose and full approved features

**Atelier Time** is an offline-capable installable timer for **live-model drawing and sculpting ateliers**. Organizer configures, previews and approves an exact complete sequence, saves reusable named sessions, activates the approved schedule and runs a timer with sounds. The user supplies a logo/image later during visual design; **do not invent it** now.

**Every numeric parameter in BOTH examples is editable for EACH full session**, not hardcoded: total duration, final reserved summary, any number of ordered opening pose groups with user-editable pose counts/durations (can add, remove, reorder), recurring regular pose length, normal break duration, special replacement length and count/placement, cycle pose-first or break-first, and final-pose allocation. Editing regenerates an entirely new preview, needing approval. Future Stage 2 editor must permit changing all these numbers.

Opening poses flow consecutively in the canonical Example A (no default breaks between them). An active long/special break **REPLACES** a scheduled short-break slot; never append another break at that boundary. The final reserved summary is included **inside** the total; its exact activity remains undefined. No trailing normal break without a following pose. All actual phase time ranges are contiguous, non-overlapping and strictly positive, covering the total exactly.

If the final repeating pose is incomplete, offer during review:
1. **Remainder**: keep previous nominal pose full length; last pose takes leftover.
2. **Near-equal**: distribute the combined available posing time of the last two poses as floor/ceil at whole-minute precision.
3. **Custom ruler/slider**: divide that combined time in whole-minute steps, >=1m for each pose where feasible.

Two-pose formula: `S` = start of earlier final pose, `E` = work-window end, `Q` = duration of the existing intervening **positive** break, `T=E−S−Q`. For selected first pose length `x`, phases are first pose `[S,S+x)`, intervening break `[S+x,S+x+Q)`, final pose `[S+x+Q,E)`. All preceding phases, Q, session total, and summary remain unchanged. If structurally impossible, do not create fictitious poses/breaks.

Saved-session operations later: create/name, review/approve, save, list, activate, edit/reapprove, duplicate, rename and delete; usable offline. Runtime later: current phase and countdown, next phase, elapsed/remaining time, pause/resume/stop, three distinct audible cues (pose/set ending; break ending; entire sequence ending). Exact per-opening-pose vs per-set sound nuance remains OPEN. Timing should use monotonic elapsed clock, with later platform-specific notification/audio tests.

## Two canonical THREE-HOUR reference fixtures — never change without approval

Each example has **D=180m**, final summary **F=15m**, work `W=165m`, normal pose `P=25m`, normal break `B=5m`, one long replacement `L=15m`. Both approved final baselines total **130m posing + 35m breaks + 15m summary**. Every number is a configurable *example input*, NOT a product restriction.

**Example A — opening 4×1m, then 4×4m; recurring break-first:**

```text
00:00–00:01 Pose 1 1m
00:01–00:02 Pose 2 1m
00:02–00:03 Pose 3 1m
00:03–00:04 Pose 4 1m
00:04–00:08 Pose 5 4m
00:08–00:12 Pose 6 4m
00:12–00:16 Pose 7 4m
00:16–00:20 Pose 8 4m
00:20–00:25 Short break 5m
00:25–00:50 Pose 9 25m
00:50–00:55 Short break 5m
00:55–01:20 Pose 10 25m
01:20–01:35 LONG replacement break 15m
01:35–02:00 Pose 11 25m
02:00–02:05 Short break 5m
02:05–02:30 Pose 12 25m
02:30–02:35 Short break 5m
02:35–02:45 Pose 13 10m (partial)
02:45–03:00 Summary 15m
```

A end-pair (S=02:05, E=02:45, Q=5m, T=35m):
- Remainder 25+10: Pose12 02:05–02:30, break 02:30–02:35, Pose13 02:35–02:45.
- Near-even 17+18: Pose12 02:05–02:22, break 02:22–02:27, Pose13 02:27–02:45.
- Custom sample 20+15: Pose12 02:05–02:25, break 02:25–02:30, Pose13 02:30–02:45.

**Example B — no opening poses, recurring pose-first:**

```text
00:00–00:25 Pose 1 25m
00:25–00:30 Short break 5m
00:30–00:55 Pose 2 25m
00:55–01:00 Short break 5m
01:00–01:25 Pose 3 25m
01:25–01:40 LONG replacement break 15m
01:40–02:05 Pose 4 25m
02:05–02:10 Short break 5m
02:10–02:35 Pose 5 25m
02:35–02:40 Short break 5m
02:40–02:45 Pose 6 5m (partial)
02:45–03:00 Summary 15m
```

B end-pair (S=02:10, E=02:45, Q=5m, T=30m):
- Remainder 25+5: Pose5 02:10–02:35, break 02:35–02:40, Pose6 02:40–02:45.
- Near-even 15+15: Pose5 02:10–02:25, break 02:25–02:30, Pose6 02:30–02:45.
- Custom sample 18+12: Pose5 02:10–02:28, break 02:28–02:33, Pose6 02:33–02:45.

Full reference inputs and tables: [EXAMPLES.md](EXAMPLES.md). Neither example has a 25/5 warning.

## Stage 1 Step 1.3 — policies P-01–P-06 APPROVED / COMPLETE (D-037)

**Read latest [STEP_1_3_REVIEW.md](STEP_1_3_REVIEW.md) and [SCHEDULING_EDGE_POLICY.md](SCHEDULING_EDGE_POLICY.md).**

- **P-01 APPROVED:** one long break chosen at the eligible ordinary break whose **replacement interval center** is closest to midpoint of working time `W/2`; tie earlier ordinal; must reproduce A/B (each third short break).
- **P-02 APPROVED:** multiple long replacements distributed approximately evenly (targets `j/(N+1)` work time), in feasible distinct break slots; deterministic earlier-ordinal ties. Zero optional special replacement *count* means none, not a 0m break.
- **P-03 APPROVED:** manual selection by normal break ordinals; errors if editing invalidates a chosen ordinal; never silent relocation.
- **P-04 APPROVED:** mathematically invalid edge inputs (opening groups overlong, summary consuming total, no positive final phase, trailing phantom breaks or excessive replacement count) are hard errors; `F=0` summary, opening-only positive sessions that fit exactly and zero optional replacement *count* are valid. **D-036:** actual session and active normal/special break durations must be positive.
- **P-05 APPROVED:** if final-two-pose structure is unavailable, disable infeasible split options; permit explicitly chosen custom first pose >nominal if both poses stay positive; do not insert extra break or pose.
- **P-06 APPROVED:** preview displays every stage and validation; distinguish blocking errors from advisory 25/5 warnings; require explicit user acknowledgment stored per approved revision, and invalidate approval on edits.
- **D-037 RESOLVED:** an exactly 25m ending needs no trailing invented break, and pose-time warning counter **does not auto-carry** between separate saved preset activations. **D-038 now RESOLVED via D-044:** positive special breaks may be shorter/equal/longer than positive normal breaks; D-031 warning and D-036 zero-duration ban remain in force.
- **Zero-length logical break slots and any final-two split across such a slot were WITHDRAWN by D-036. Do not implement.**

## Approved D-044 — positive shorter/equal special replacements (2026-10-22)

The user explicitly approved: **“allow any positive special break duration, even shorter than or equal to the normal break, with the already approved 25/5 rest warnings. Zero-minute breaks remain invalid.”**

- **D-044 resolves formerly OPEN D-038.** A selected special break of **L>0** can be shorter, equal, or longer than positive ordinary B. It replaces B (it does NOT add to B). D-036 prohibits 0-minute sessions/configured breaks; D-031 requires the 25/5 rest warning and affirmative acknowledgement when applicable.
- **Branch code:** [scheduling engine](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/lib/src/scheduling.dart) accepts all positive special lengths and generalizes exact DP placement for negative, zero, and positive `L-B`. The old `pendingDecision` rejection is gone.
- **Tests written NOT RUN:** [regression suite](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/test/scheduling_test.dart) and [smoke script](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/tool/smoke.dart) cover normal=5m with special=1m/3m/5m, multiple shorter replacements, warnings and explicit approval. A/B canonical cases unchanged.
- **Independent Python design checks** matched brute force across **25,000 virtual-schedule random cases** and **4,000 additional real-schedule exhaustive cases**, including `L<B`, `L=B` and `L>B`. These do **NOT verify Dart code**.
- **No Dart/Flutter SDK, no executable Dart tests, analyzer, smoke execution or build.** SDK download DNS unavailable. GitHub Actions/paid runners intentionally not run owing to user-reported account budget block. Branch unmerged. **Step 1.4 remains IN PROGRESS; Stage 2 NOT authorized.**

## Historical Step 1.4 start — coding branch, GitHub budget protection

- **Authorization D-040:** The user said “Continue” after asking how to work with minimum intervention while GitHub's $12 metered budget was exhausted on another project. This explicitly started Step 1.4 only.
- **Branch:** [stage-1-4-scheduling-core](https://github.com/AviY99/Atelier-Time/tree/stage-1-4-scheduling-core), forked from the existing `main`. All new code/test files are on this branch **only**.
- **Dart package files:** `pubspec.yaml`, `lib/atelier_time.dart`, `lib/src/scheduling.dart`. Pure Dart, no Flutter or paid runner requirements.
- **Test SOURCE:** `test/scheduling_test.dart` (canonical Example A/B complete precise timelines, all three final-pose alternatives, schedule invariants, manual/automatic special break selection, D-036 positive durations, D-031 warnings and explicit acknowledgment, D-044 shorter/equal positive replacement approval). `tool/smoke.dart` runs a subset with only Dart SDK, no external packages. Source files are committed and read back, **but have not executed**.
- **Branch-only status:** [docs/STAGE_1_4_PROGRESS.md](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/docs/STAGE_1_4_PROGRESS.md) details code, assumptions and limits, including the newly fixed optimizer (D-043).
- **Environment:** Git available, but **no Dart or Flutter SDK** in current execution environment. No successful code compilation, `dart analyze`, `dart test`, smoke execution, Actions run or installer. A structural bracket/comment/string check passed, **not a compiler test**.
- **Budget safety D-041:** No `.github/workflows` additions, Actions invocations, billable build/deployment or user budget changes. User reports GitHub billing limit blocks other project. Continue with branch source/offline checks until a safe Dart execution environment is available.
- **D-038 RESOLVED by D-044:** normal rest 5m, special rest 3m is approved; any positive special duration is legal. The code no longer reports `pendingDecision`. D-031 rest warnings/acknowledgment and D-036 invalid 0m sessions/breaks remain mandatory. **D-043 improvement:** replaced exponential combination enumeration with exact dynamic programming over adjusted virtual break-slot timing, avoiding false-rejection of valid schedules. Additional test sources cover a 97m formerly false-invalid case and 480m with five long replacements. Independent **Python-only** comparison with exhaustive search matched 25,000 random small cases and canonical A/B placements; this does **NOT** execute/verify Dart. Dart SDK is still absent; Dart compilation, static analysis, actual tests and broader performance tests remain pending.
- **Stage 1.4 remains IN PROGRESS**; no Flutter app screens, live timer, saved preset database, audio, cross-platform builds or later-stage work.

## Design model, tests and GitHub artifacts

- **[TECH_DECISION_001_PLATFORM.md](TECH_DECISION_001_PLATFORM.md)** records approved Flutter + Dart, four release targets and allowance for non-native UI.
- **[DATA_MODEL.md](DATA_MODEL.md)** defines only **conceptual** Dart types: `SessionRules`, `OpeningPoseGroup`, `RegularCycle`, `LongBreakRule/Placement`, `SchedulePhase`, `ScheduleCandidate`, `EndPoseResolution`, `ApprovedSessionPreset`, `ScheduleIssue`. Internally integer seconds; whole-minute UI initially; records are not yet executable Dart.
- **[TEST_PLAN.md](TEST_PLAN.md)** lists canonical T01–T18 and P-T/R-B edge cases; **current D-052 evidence: 35/35 Step 1.4 Dart unit tests PASS on Android ARM64** after the added T09 endpoint, prior analyzer no issues, prior smoke PASS. T13/T14 broader edge cases remain a future QA backlog.
- **[PRODUCT_SPEC.md](PRODUCT_SPEC.md)** approved product scope, **[SCHEDULING_RULES.md](SCHEDULING_RULES.md)** core contract, **[ROADMAP.md](ROADMAP.md)** gates, **[DECISIONS.md](DECISIONS.md)** chronological register through **D-064**, **[WORK_LOG.md](WORK_LOG.md)** iterations, **[CHANGELOG.md](../CHANGELOG.md)** history. See branch-only [Stage 1.4 progress](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/docs/STAGE_1_4_PROGRESS.md).
- **Decision precedence:** D-036 supersedes D-031's previous 0-minute break allowance, but D-031 25/5 warning remains. D-037 approves P-01–P-06 and within-one-session warning scope, with zero summary / zero special-break **count** permitted. **D-038 resolved by D-044**; engine now accepts positive shorter/equal replacements, with D-031 warnings. D-045 records unexecuted source/tests. **D-039** was the prior Step 1.4 gate; **D-040** subsequently authorizes Step 1.4 only; **D-041** prohibits GitHub Actions/paid workflow use under budget block; **D-042** documents unexecuted branch code. D-030/D-032/D-034/D-035 are earlier historical/proposal records, NOT permission for zero-minute break slots.
- GitHub is the authoritative store. After every iteration, update decisions/roadmap/work log/changelog and **THIS HANDOFF**; verify GitHub read-back and link concrete commits. Do not create a duplicate repo.

## Stage gate and immediate next action — CURRENT 2026-10-23

**Stage 0 COMPLETE. Stage 1 Step 1.1 platform choice COMPLETE; Step 1.2 conceptual data model DOCUMENTED; Step 1.3 policies APPROVED; Step 1.4 pure-Dart core and 35 required tests VERIFIED/COMPLETE (D-052).**

**Stage 2 STARTED (D-053/D-054), NOT COMPLETE.** The user directly instructed finishing documentation and continuing. Stage 1 [PR #1](https://github.com/AviY99/Atelier-Time/pull/1) and Stage 2 [draft PR #2](https://github.com/AviY99/Atelier-Time/pull/2) are OPEN, not merged. Provisional Flutter editor source is on `stage-2-session-editor` and has never been compiled or run. No release/APK, no Flutter platform scaffolding, no paid Actions, no Stage 3–6 work. D-031, D-036, D-037, D-044 remain product invariants.

**Immediate next action in a future chat — HANDOFF v3.4 / D-070:** READ [NEXT_CHAT_HANDOFF_2026_10_23.md](NEXT_CHAT_HANDOFF_2026_10_23.md) FIRST and current Android branch source. All business requirements unchanged; Android Kotlin code exists with K03 persistence, but 32 JUnit and 4 Android instrumentation tests are WRITTEN/UNRUN, no APK/Gradle compilation evidence. First verify real app on phone with Code on the Go via `:app:testDebugUnitTest`, `:app:assembleDebug`, then on-device install/reopen/preset tests and instrumentation. Fix compile/runtime issues as real development. K04 timer/3 cues remains not implemented. Do not reselect technology, ask approval already granted, rerun proven Dart tests as substitute, use paid GitHub Actions or merge PRs. Keep handoff updated.
