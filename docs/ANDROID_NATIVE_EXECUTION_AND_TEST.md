> **LATEST — 2026-10-29 — ADDITIONAL D-127 AUTOMATED ACCESSIBILITY MATRIX WRITTEN, NOT EXECUTED:** Following Samsung **6/6 PASS** of the original D-127 accessibility tests, eight **new, non-duplicative** Compose UI matrix test methods have been authored on Android dev, with isolated preferences and simulated font scales of 100%, 130%, 200%. Covers navigation tabs, narrow Free wheels/presets, Design, Sound and Timeline accessible descriptions. The new test-only APK must first be built in Samsung Code on the Go with `git pull --ff-only && bash tools/build_d127_stage7_matrix_on_phone.sh`; then run only the eight new methods from Termux using `bash ~/storage/downloads/Atelier-Time-D127-Stage7-MatrixQA.sh --run` when no timer is active. **No new 8/8 PASS claimed yet**. User data and system font settings are untouched by these isolated tests. The accepted Stable 9 branch is unchanged, six existing D-127 tests remain PASS; Stage 7 awaits new-matrix evidence and limited real TalkBack/contrast/visual acceptance. No GitHub Actions, cloud builds, rerun of old 285 tests or app reinstall. [Matrix test gate](D127_STAGE7_AUTOMATED_MATRIX_2026_10_29.md).

# Atelier Time — Approved requirements baseline and Android phone development/QA process

> **CURRENT PHONE BUILD/QA — Stable 9 / D-126 (2026-10-28):** Real Samsung Code on the Go Gradle **72/72** tasks PASS, **243/243 JVM** PASS, both APK SHA-256 OK; separately Termux ADB Android Instrumentation **42/42** PASS (`OK (42 tests)`), with **post-test Saved Classes/Sounds preserved** per user. Do not invoke a new main APK build or installation to rename Stable 9. User's ordinary update workflow is My Files → Download → update-in-place, while Android instrumentation may use already paired Termux ADB with express approval and idle Class. Historical earlier “D-086 current” statuses below are superseded. [Full QA](STABLE8_D126_ANDROID_INSTRUMENTATION_QA_PASS_2026_10_28.md), [current status](CURRENT_STATUS_AND_HANDOFF_STABLE9_2026_10_28.md).


> **CURRENT AUTHORITATIVE CHECKPOINT — 2026-10-24 / D-087:** User reported D-086 fully working on Android phone ("הכל עובד"), following screenshot-confirmed `:app:testDebugUnitTest :app:assembleDebug` BUILD SUCCESSFUL in 36s (42 tasks: 7 executed, 35 up-to-date). Declared **Atelier Time Android Stable 1 — D-086** (user-accepted installed functional baseline; not signed production or store release). Frozen source checkpoint branch `stable/android-d086` points to **`d0a03d2792cc324aeeba39b6327136ba6d37fd0c`**; current Android dev branch / open Draft PR #3 remain unmerged and should not be modified for accepted stable source. No CI/actions, release tag, binary publication or app `versionName` change. **Previously pending D-086 install/phone visual-acceptance statements below are historical and superseded by this newer direct user report.** Stable baseline docs: [ANDROID_STABLE_BASELINE_D086_2026_10_24.md](ANDROID_STABLE_BASELINE_D086_2026_10_24.md). Outstanding: 9 written Android instrumentation methods not executed on device; background/lock/doze audio reliability; lifecycle/time edge cases; accessibility, motion/contrast, performance/battery; signed release/distribution; future iOS/desktop multiplatform efforts; older D-015/D-016 wording only if change proposed. **Design exploration is authorized, but no visual concept has been approved or implemented:** three styles and animation plans in [VISUAL_DESIGN_CONCEPTS_2026_10_24.md](VISUAL_DESIGN_CONCEPTS_2026_10_24.md), with safe nonflashing/reduced-motion fallback. Keep accepted functions/domain/stored classes unchanged.

> **FOLLOW-UP SOURCE CHECKPOINT — 2026-10-23 (post D-070):** K03 wrong-type preferences and direct corrupt/future-version overwrite safeguards were committed to the real Android branch; two more Android instrumentation tests were authored. **32 Kotlin JVM + 6 Android instrumentation tests written; NONE executed; no Gradle/Android SDK/connected phone available in this assistant runtime.** K01–K03 acceptance still OPEN; K04 not implemented. The older 4-test and K03-not-implemented historical statements below are superseded. Continue real on-phone CoGo `:app:testDebugUnitTest`, `:app:assembleDebug`, install/relaunch, and `:app:connectedDebugAndroidTest` if supported, without paid Actions.


> **CURRENT K03 UPDATE D-070 — 2026-10-23:** User authorized moving to the next stage. The Kotlin Android branch now contains actual offline approved-session storage, versioned Android-private persistence, Compose UI for save/load/update/duplicate/rename/delete/select and **32 JVM + 4 instrumented Kotlin test METHODS WRITTEN, NOT RUN**. The preserved product rules remain unchanged. The K03 acceptance gate remains **UNVERIFIED** until a real CoGo build, Android APK run, restart/persistence and corruption testing. K04 timer and 3 audio cues are still not implemented. See [native branch README](https://github.com/AviY99/Atelier-Time/blob/android-native-kotlin-compose/android-native/README.md).


**Date:** 2026-10-23. **Status:** Android-first Kotlin/Compose implementation in progress, NOT build-verified. **Authorized by user:** “מאושר , תתקדם , יש פער גדול לסגור , אשר שהגדרות והדרישות.מובנות תועדו ולא דורשות.שינוי , עדכן את תהליך הפיתוח והבדיקה למע החדשות”.  
**Authoritative decisions:** D-062 Android on phone first, D-067 Kotlin/Compose/Gradle and CoGo, D-068 baseline confirmation and process, D-069 first native source increment.

## 1. Requirements are understood and remain unchanged

**Explicit confirmation:** No alteration to approved Atelier Time **product behavior** or scheduling parameters is required to change implementation language from Dart/Flutter to Kotlin/Compose. The only earlier agreed requirements change is **delivery priority / developer host** (D-062): Android APK built and debugged **on the Android phone first**, later port with practical Kotlin/Compose Multiplatform reuse. **No new functional restrictions, session duration caps, fixed numbers or silently changed business logic.**

| Product contract / approval | Still required without change |
|---|---|
| Drawing and sculpting workshops with live model | One offline-capable installed Android app, later future OS versions |
| Rules per complete named session | Total >0, summary >=0 with positive work; unlimited editable whole-minute durations subject to validity; opening groups ordered, add/edit/remove; pose/break cycle order |
| Break semantics (D-036/D-037/D-044) | All configured actual break lengths >0; initial normal 5m and special 15m remain **defaults only**, editable. Special count may be 0. Any **positive** special replacement allowed, including <= normal; replaces a scheduled normal break, not appended. Multiple automatic/manual positions preserved |
| 25/5 advisory rule (D-031) | After more than 25m accumulated posing without >=5m qualifying rest, show warning; shorter valid breaks do not reset count; require separate explicit acknowledgment and final approval. Editing clears both |
| Full schedule invariants | All phases positive, contiguous, cover total exactly, final summary inside total, no terminal break/phantom phases; deterministic automatic replacement and tie rules (D-037/D-043) |
| Incomplete final pose | Explicit remainder / even / custom 1m step options for two eligible poses with intervening original break; no automatic approval, both min 1m; don't invent breaks |
| Exact reference schedules | Example A (4x1 + 4x4 opening, break-first) and B (pose-first); 180m including 15m final summary, one 15m replacement; exact timestamps and totals per [EXAMPLES](EXAMPLES.md) |
| Session lifecycle | Preview, warnings, approve exact result; later offline save/list/edit/duplicate/rename/delete/activate presets, persistence across restart |
| Live time/audio | Monotonic elapsed countdown, pause/resume/stop, next phase/total progress, three distinct audible cues and documented real lock/background behavior |
| Visuals / outstanding details | User supplies logo later; style not frozen. **D-015** exact labeling of reserved “summary” and **D-016** cue per pose vs per group remain **OPEN**, not silently decided |

**Approved product specifications remain in** [PRODUCT_SPEC.md](PRODUCT_SPEC.md), [EXAMPLES.md](EXAMPLES.md), [SCHEDULING_RULES.md](SCHEDULING_RULES.md), [SCHEDULING_EDGE_POLICY.md](SCHEDULING_EDGE_POLICY.md) and [STEP_1_3_REVIEW.md](STEP_1_3_REVIEW.md). We preserve, not rewrite, these. Old Flutter-specific implementation/delivery lines in historical files are overridden by D-062/D-067, but **not scheduling rules**.

## 2. Current Android implementation architecture

**Native product source:** [android-native/](https://github.com/AviY99/Atelier-Time/tree/android-native-kotlin-compose/android-native) on isolated [android-native-kotlin-compose](https://github.com/AviY99/Atelier-Time/tree/android-native-kotlin-compose), based on Stage 2 tested Dart branch. Does not merge or delete historical PR #1/#2.

- **Build:** Gradle 9.6.1 / Android Gradle Plugin 9.3.1 / Kotlin/Compose compiler plugin 2.3.21 pinned to Code on the Go on-device toolchain documented by maintainers. CoGo handles Gradle daemon/Android SDK and debug APK on phone. Android Studio remains supported desktop IDE using this standard source.
- **Domain:** `android-native/app/src/main/java/com/ateliertime/domain/Models.kt` and `ScheduleEngine.kt` contain platform-free Kotlin models/algorithms (including deterministic dynamic-programming automatic break placement). **Never put Android SDK imports here**; future KMP extraction should not require rewriting scheduling mathematics.
- **UI/input:** `android-native/app/src/main/java/com/ateliertime/android/` contains Activity, Compose editor, and text-to-rules converter. UI state drives a live chronological schedule from **one** domain engine, with explicit acknowledgment/approval. Native Android persistence and timer/audio interfaces are **later** modules.
- **Source provenance:** Original **35/35 Dart scheduling** and **11/11 Dart input-adapter** tests previously screenshot-verified. The Kotlin port is new and requires its **own** execution evidence; Dart PASS does **not** imply Kotlin PASS.
- **No throwaway sample prerequisite.** This branch is the **real app code**. Build/test/debug occurs as normal work, not a preliminary feasibility spike.

## 3. Phone-first development loop (no desktop or paid CI required for routine work)

1. In the maintained **Code on the Go** Android IDE, obtain branch **`android-native-kotlin-compose`** from existing repository `AviY99/Atelier-Time`, and open the **`android-native`** folder, *not* the root Dart package, as the Gradle project. Do not modify/clean old Termux worktrees.
2. Edit real feature code in its relevant layer; commit after each coherent change to the isolated branch, maintaining the same full GitHub docs/handoff process as before.
3. Build via local CoGo Gradle tasks: **`:app:testDebugUnitTest`**, then **`:app:assembleDebug`**. Once it builds, install/open the APK using CoGo or native Android installer and inspect UI and input editing. The nested app's existing 21 Kotlin JUnit test methods should run on JVM with Gradle; **they have not run at time of authoring**.
4. Check debug output, fix errors **in Kotlin code or exact pinned toolchain**; never silently replace product behaviors merely to turn tests green. Use staged release gates, not a loop of unsupported SDK installations. No paid GitHub Actions or other build services.
5. Record every actual phone run: branch/commit, CoGo/Android/Gradle versions, task, passed/failed test count, first meaningful error(s), resulting APK location, installation and on-device behavior. Run native UI/instrumentation tests when CoGo Android SDK/test runner is available. **No claim of compiled APK or app feature until evidence exists**.

**No Gradle wrapper JAR is included:** CoGo supplies its pinned on-device Gradle. Android Studio users can generate a standard wrapper with the same version when a desktop environment is available. Direct bare `dart analyze` in Termux checks only historical Dart and **does not validate native Kotlin/Compose**.

## 4. New progressive acceptance process

| QA gate | Must pass for milestone | Present status |
|---|---|---|
| K01 **Kotlin build & pure-domain tests** | Gradle `:app:testDebugUnitTest`; exact A/B phase boundaries, three end choices, DP 97m/480m, D-031/036/037/044, deterministic no-gaps, invalid cases | **21 Kotlin tests WRITTEN, NOT EXECUTED** |
| K02 **Android Compose editor** | APK builds, installs and opens on phone; every numerical rule editable, opening groups reorder; live preview exact; invalid candidate blocked; approval invalidated after edits | **SOURCE WRITTEN; BUILD/UI UNVERIFIED** |
| K03 **Offline saved sessions** | Save/name/list/edit/copy/rename/delete/activate, survive full app/device restart, zero network required, handle corrupt data gracefully | **NOT IMPLEMENTED** |
| K04 **Live timer and three cues** | Correct active phase countdown/pause/resume/stop; monotonic timing; unique 3 alerts; no duplicates, tested across phone lock/background interruptions | **NOT IMPLEMENTED** |
| K05 **Android delivery and accessibility** | Real APK install/reopen, minimum Android API/device behaviors, touch/screen reader/large text, real workshop run and offline acceptance | **NOT VERIFIED** |
| K06 **Future portability** | Isolate Kotlin domain, migrate to Kotlin Multiplatform and optionally Compose Multiplatform only when next OS target work is approved; per-OS builds/tests later | **ARCHITECTURAL INTENT ONLY** |

No green milestone without **real** tests and user-facing application execution. Earlier Flutter eight widget tests remain **unexecuted** historical source and do not apply to Kotlin. No urgent need for the user to run another Dart test or supply large screenshot logs.

## 5. Deviation and approval control

- Product contract unchanged by D-068. Any request to change a number, rest rule, phase behavior, summary semantics, sound policy, saving or timer semantics must be explicitly identified as a **new product decision**, approved by user and added to [DECISIONS.md](DECISIONS.md).
- New coding is authorized by user's “מאושר, תתקדם”; stage reporting remains required, but do not repeatedly ask permission for already-approved Android work.
- Preserve previous pure Dart source/tests, original staged Termux files and historic branches; no automatic merges.
- Do not ask for irrelevant feasibility experiments; implement the real application and use normal QA.
- Don't claim the Kotlin scheduler tests pass merely because corresponding Dart tests passed. CoGo build output is pending.

## 6. Evidence and source references

- Android [Compose recommended UI](https://developer.android.com/topic/architecture/ui-layer), [layered architecture](https://developer.android.com/topic/architecture), [Compose setup/BOM](https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler).
- [Kotlin Multiplatform / Compose Multiplatform stable platform targets](https://kotlinlang.org/docs/multiplatform/supported-platforms.html).
- [CoGo releases](https://github.com/appdevforall/CodeOnTheGo/releases) and [on-device pinned toolchain documentation](https://github.com/appdevforall/CodeOnTheGo/blob/stage/docs/PLUGIN_AUTHORING.md).
- [AGP 9 built-in Kotlin migration](https://developer.android.com/build/migrate-to-built-in-kotlin).

**NEXT IN THIS STAGE:** User-facing Kotlin Compose editor actual compile/install/inspect on Android phone; then implement named offline presets, timer, three cues in successive real feature increments, keeping baseline requirements unchanged.
