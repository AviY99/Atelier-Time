# Test and acceptance plan — Atelier Time

> **NEW D-085 CODE / PHONE QA PENDING (2026-10-24):** User says D-084 works on the phone; do NOT repeat its accepted install/test gate. D-085 real Kotlin/Compose code changes have NOT yet been compiled or run through phone Gradle. After existing CoGo `git pull --ff-only` and `:app:testDebugUnitTest :app:assembleDebug` native AAPT2 gate, update-install without clearing private signed saved Class data. Verify on the app: Class elapsed shows only own HH:mm:ss, not `/ HH:mm:ss` total; it increments continuously during Pause and does not jump on Next (Next skips current phase remaining in virtual timer only). Class end stays the original real start+approved length through Next/paused time and on Class completion; Class remaining/progress reflect physical clock, and a delayed virtual schedule ends at original Class physical deadline with one C cue. Label font-only 1.3× on Class start/end/length, numeric values retain previous bodyLarge, rows closely grouped. Session/Break start/end still show seconds; projected End should not oscillate between neighboring seconds (monotonic millis and fixed monospaced width/height). In Timer saved classes, Run/Edit visible primary row and Duplicate/Rename/Delete each visible in local secondary row on narrow phone with new differentiated styles; duplicate preserves original, rename checked, delete confirmation still present. Test persistence of saved presets/sound choices after upgrade. 4 new JVM source tests: wall elapsed during Pause/Next; stable precise End; precise milliseconds in timer; deadline completion once during Pause. 2 earlier clock test expectations changed for immutable deadline. **106 JVM @Test source methods and 9 Android instrumentation methods now written; actual D-085 Gradle run count and PASS not yet observed.** Locked-screen/background audio and instrumentation still independently open, no paid GitHub Actions, Draft PR #3 unchanged.

> **D-084 ACTUAL PHONE GRADLE GATE PASS — DEVICE GUI QA STILL OPEN (2026-10-24):** On 2026-10-24 user supplied a Code on the Go terminal screenshot of the D-084 Android build. The visible Gradle tasks include :app:compileDebugUnitTestKotlin, :app:testDebugUnitTest and :app:assembleDebug, ending BUILD SUCCESSFUL in 26s (42 actionable tasks: 8 executed, 34 up-to-date). This verifies successful Kotlin/JVM-test task and APK assembly on the phone; the screenshot does not show an exact per-test JUnit count, APK update-install, or a runtime UI acceptance of D-084. Keep D-084 device UX checks pending. The last confirmed source commit on PR #3 remains 5f0df02c169ba4134cad2a6603d2df288ad96dcc; PR #3 open Draft/unmerged. Prior D-082 user acceptance and D-083 39s Gradle pass remain separate evidence.

> **D-084 PHONE QA PENDING (2026-10-24):** Small new Compose source change after D-083's user-screenshot Gradle PASS. Verify upper-right live Clock and Class start/end use `HH:mm`; active Session/Break **Start/End** in countdown card use `HH:mm:ss` (with seconds ticking and paused End remaining `— (paused)`). Immediately under Clock inspect **three visually uniform, tightly packed rows**, in order **Class start / Class end / Class length**. Class length is approved fixed `session.rules.totalSeconds` formatted HH:mm, not remaining time or runtime compensation. Retain below the separate Class remaining, elapsed and progress bar. All prior D-083 Exit/Help/Next behavior, signed final-pair compensation, audio, saved classes must remain unaffected. Do **not** mark D-084 compiled/JUnit/assembled/APK-installed/phone PASS until new evidence. No additional JVM test method definitions for this presentation-only change (D-083 baseline: 102 written JVM methods, 9 instrumentation methods, prior phone Gradle `BUILD SUCCESSFUL in 39s`). PR #3 Draft; no paid Actions.

> **2026-10-24 PHONE GRADLE D-083 VERIFIED: User-supplied Code on the Go terminal screenshot shows :app:compileDebugUnitTestKotlin, :app:testDebugUnitTest, :app:assembleDebug, and BUILD SUCCESSFUL in 39s; 42 actionable tasks: 8 executed, 34 up-to-date. Confirms Gradle Kotlin/test task and debug APK build for the recently pulled D-083 source. Individual JUnit method count is not visible (102 written source methods, NOT a screenshot-verified 102 completed). D-083 APK update-install, four-button layout, Exit Help/color/confirmation, HH:mm timestamps, larger start/end values, final-pair Next and Next->HOME/C audio are still awaiting phone acceptance. Previous D-082 user-confirmed working. Preserve saved classes, PR #3 Draft, no GitHub Actions.**

> **D-083 SOURCE / PHONE QA PENDING — 2026-10-24:** User confirmed **D-082 installed app works** after its source sync and Gradle up-to-date gate. New D-083 source has NOT been built, Kotlin/JVM tested or update-installed. Confirm on D-083 upgrade: four Timer buttons Pause/Resume, Restart segment, Next, **Exit** all fit one phone row, Exit has different tonal color and *own contextual Help*, confirmation returns to saved Class HOME without erasing presets or playing completion cue. Right-side real Clock, Class start/end and active Session/Break start/end show **HH:mm** (no seconds); 4 start/end time values are ~1.5× larger, countdown still shows seconds. Next from early phases still preserves deadline if feasible and sounds A/B; Next from first final regular Session advances to Break without rewriting first Session, Next from intervening final Break advances to final Session by changing **only last**, and Next from final regular Session or summary immediately completes with **C** and returns saved Class HOME. When safe compensation cannot fit (long Pause), Next still advances, previously completed phases are not modified, saved plan is unchanged and Class projected end updates. Normal Restart remains A/B. New tests: 4 positive Next methods replace one obsolete late-refusal test (+3), plus 2 ClassClockTracker tests (+2) => **102 written JVM source methods**, 9 written Android instrumented methods. **Do not claim D-083 Gradle PASS or phone acceptance until observed.** Prior D-082 source test/build up-to-date PASS remains separate. No paid CI or PR merge.


> **2026-10-24 NEW D-082 CO GO GRADLE RESULT (SUPERSEDES EARLIER SOURCE-ONLY STATUS):** After phone `git pull --ff-only` fast-forward `bab39b3..b51620d`, the user supplied a screenshot from the existing `android-native` Gradle project showing combined `:app:testDebugUnitTest :app:assembleDebug` **`BUILD SUCCESSFUL in 12s`**, **42 actionable tasks, all 42 UP-TO-DATE**. Gradle reports current cached test and debug APK outputs, but did not freshly run tests/build tasks in this invocation. **Do NOT infer 97 individual tests freshly executed**; 97 refers to test source methods. D-082 APK update-install and device visual/functional acceptance are STILL PENDING. Next verify the installed D-082 card layout, enlarged Clock, phase restart A/B cues, fixed Class start/end, late-break last-only adjustment/rejection, and saved-class persistence. Previously installed D-080/D-081 APK already user-accepted; do not repeat that release. Nine instrumentation tests and background/locked-screen sound are unverified. PR #3 Draft; no paid CI.

> **CHAT CLOSED / LATEST ENTRY POINT — 2026-10-24:** The new authoritative successor-chat document is [NEXT_CHAT_HANDOFF_2026_10_24.md](NEXT_CHAT_HANDOFF_2026_10_24.md). User **confirmed the previously built Android APK already installed and works**; do not repeat this installation or its successful Gradle run. Later D-082 code (two segment clocks in active card, Clock 2×, restarting Break with B sound and final-pair adjustment) is **source-only and NOT yet Gradle/phone-validated**. Treat any future test/build as new D-082 validation, not a retry of old gates. 97 written JVM tests, 9 written Android instrumentation cases; no D-082 run verified.


> **NEW UNVERIFIED D-082 SOURCE (2026-10-24):** The last native APK installation is **already confirmed working by the user** after phone Gradle `BUILD SUCCESSFUL in 52s` at 13:03; no need to reinstall that prior release. New source moves **current Session/Break start/end** into the active countdown card's top-right (`labelSmall`, right aligned), enlarges upper-right **Clock** to `headlineSmall` (~2× previous), while retaining Class start/end and Class elapsed/remaining/progress in Class header. Restart action now works on current **Break** (regular/special/opening), dynamically labeled `Restart break`, and plays **B** on success; Restart session remains **A**. Repeat rest duration; preserve preceding phases, saved preset, original Class start/end. Recompute final two regular Sessions, or only the not-yet-started final Session when repeating the intervening Break after first final Session already completed. Reject insufficient last duration (<60s) atomically without cue. Pause/Resume and Next unchanged. 8 new JVM source tests => **97 written**, Android instrumentation 9 written; **NO D-082 tests/build/device GUI checks run yet**. Verification TODO: local Kotlin `:app:testDebugUnitTest :app:assembleDebug` once for new code, preserve installed working app until successful output; examine 5-clock placement, break repeat and cue B, late-break safety. These statuses supersede older "install pending" notes lower down for previous release.


> **LATEST PHONE DEVICE ACCEPTANCE (USER-REPORTED, 2026-10-24):** After screenshot-confirmed 13:03 native `:app:testDebugUnitTest :app:assembleDebug` **`BUILD SUCCESSFUL in 52s`**, the user explicitly states that the subsequent APK copy/update-install is **already completed and the app is working**. **Close APK installation pending for the currently delivered native build. DO NOT prescribe another installation or repeat passing local Gradle build as the next step.** This is an overall user acceptance report, *not* individual verified results for each D-080/D-081 UI action, nor evidence that Android instrumentation (9 source cases), lock/background scheduling/audio and long-run reliability have passed. Preserve their independent unverified status. Continue forward from next actionable product/test milestone.


> **REAL PHONE GRADLE PASS, 2026-10-24 13:03:** Screenshot shows native branch fast-forward `c1bf428..bab39b3`, then `:app:testDebugUnitTest :app:assembleDebug` with native ARM64 AAPT2 override; log grep returned **`67:BUILD SUCCESSFUL in 52s`**. This confirms Gradle task success and debug APK build, superseding the preceding Kotlin compile failure. Numeric number of test cases was not displayed; **do not claim 89 individual test results on screenshot alone**. APK copy/install and all runtime acceptance of D-080/D-081 still **PENDING**. Next: copy APK to Download; update-install preserving data; verify Timer saved library Run/Edit, Editor Save and overwrite confirmation, current work-Session Restart (not Class), cues and Class clocks, compact progress and friendly ending mode label.


> **PHONE COMPILE FAILURE, 2026-10-24 12:53 (D-080/D-081):** Photo displays `:app:compileDebugKotlin FAILED`, generic Kotlin compilation error, **`BUILD FAILED in 22s`**, 31 tasks (1 executed, 30 up-to-date). Missing concrete `e:` compiler lines in photo. Static GitHub source inspection found `SessionEditorScreen.kt` uses `horizontalScroll` without its import; import added in commit `bab39b3603b1693d4bc9260c4152344effe93676`, source readback verified. **Not yet phone-retested; cannot claim sole root cause**, JVM test/assemble did NOT pass in this invocation. Repeat local compile/test/assemble; if another failure, capture actual Kotlin diagnostic lines from Gradle log using `grep -nE '(^e:|^error:|Unresolved reference|Type mismatch|BUILD (FAILED|SUCCESSFUL))'`. Do not uninstall/clear previous app data.


> **D-081 — CURRENT SESSION RESTART CORRECTION (2026-10-24, SOURCE UNBUILT):** UI **Restart session** restarts only the running Session from that phase's beginning, not the Class, with earlier sessions/breaks and actual Class start unchanged; repeat time (plus pause time, net of prior Next skips) is deducted from or credited to **both final regular sessions**, retaining original Class end time, approved split and saved preset where feasible. On success play **A session-end cue**, not C class-end. Disable during Break/summary; reject impossible final-pair rewrites atomically. Clock Tracker resets only current Session start on successful restart. Editor last-pair Remainder chip renamed **Last gets remaining time**, with explanatory copy; serialized `FinalPoseMode.REMAINDER` unchanged. Five timer + one clock new JVM source tests, **89 JVM methods total**, 9 Android instrumentation source tests; **NO new Kotlin Gradle pass/APK/phone acceptance yet**, also D-080 still pending. Test restart during Session 2; pause→restart; Next→restart; Break guard; last-final safety; verify A cue and actual Class start/end remain fixed. 


> **D-080 extra safety check (2026-10-24):** Loading a saved class for Edit now retains each opening group's *exact approved identifier* via optional `OpeningDraft.originalGroupId` and `EditorInput` mapping, instead of silently regenerating different group IDs. New JVM `EditorInputTest.editingSavedClassPreservesOriginalGroupIdentifiers` was written and checked into the branch, **NOT RUN**. Total now **83 known JVM test source methods**, Android instrumentation 9; new D-080 source still not compiled on phone.


> **NEW D-080 WORKFLOW SOURCE — NOT YET BUILT/PHONE VERIFIED (2026-10-24):** Editor UI swaps sample A/B templates (does NOT rewrite historical engine fixtures), has separate Regular sessions/Regular breaks/Special breaks sections, and uses ONE final Save class action (= schedule approval and offline persistence). Exact existing title (case-insensitive) triggers an explicit Overwrite class alert; a changed, unique title creates a new saved class; cancellation does not mutate store. Timer HOME owns Run/Edit/Duplicate/Rename/Delete for saved approved classes. Run starts actual timer immediately; Edit opens Editor with identical title/rules; normal class finish and explicit End & return restore Timer home. Current segment remaining label dynamically distinguishes session vs regular/special/opening break. Top-right five-clock panel also shows compact Class remaining/elapsed and overall progress; Pause/Restart class/Next in a compact control card. `ClassSaveTargetTest.kt` adds 4 unrun JVM tests, bringing **83 known JVM test methods and 9 Android instrumentation test sources**. **All D-080 code uncompiled and uninstalled.** Next CoGo phone `git pull --ff-only`, Gradle `:app:testDebugUnitTest :app:assembleDebug` with native AAPT2, update-install preserving presets, and human UI acceptance as specified in WORK_LOG.


> **LATEST VERIFIED REAL-PHONE GRADLE RERUN (2026-10-24 12:14): PASS.** After test fixture fix for shortened end-choice, screenshot confirms `:app:testDebugUnitTest` and `:app:assembleDebug`, **`BUILD SUCCESSFUL in 23s`**, 42 tasks (4 executed, 38 up-to-date). Earlier `78 tests / 1 failed` was an older checkout and is superseded by this passing task. The screenshot does not expose JUnit XML count, exact branch SHA, APK copy or device installation; these facts must not be claimed. **Runtime acceptance still pending**: confirm five clocks, actual current phase start after Next, Pause unknown segment-end marker, Restart class preserving planned end, labels and sounds after update-install. No instrumentation PASS claim.


> **12:09 LOCAL PHONE SCREENSHOT — TEST FAILURE (2026-10-24), FIX PENDING RE-RUN:** `:app:testDebugUnitTest` reported **78 completed / 1 failed**, `BUILD FAILED in 42s`; failing `MonotonicSessionTimerTest.nextSupportsConsecutiveOpeningPosesAndLaterOpeningBreak` at line 165 (`IllegalStateException`). Inspection shows test fixture generated a one-hour class with a shortened 8m final regular session but attempted `engine.approve(engine.generate(rules))` **without the required ending choice**, so it failed before Next was called. Native-branch commit [332279384cd195b74b68d6b08881bbb74adba21d](https://github.com/AviY99/Atelier-Time/commit/332279384cd195b74b68d6b08881bbb74adba21d) makes fixture assert `needsFinalPoseChoice`, choose explicit `FinalPoseChoice.remainder()`, assert `canApprove` and only then call Next. Production engine/approval untouched; **not yet retested**. **77 tests had no reported failure, 1 failed; APK assembly/copy not verified for this invocation.** Next re-run `:app:testDebugUnitTest :app:assembleDebug` locally with native ARM64 AAPT2; inspect any new failures, do not mark PASS early.


> **2026-10-24 D-079 NEW VOCABULARY + LIVE CLASS/SEGMENT CLOCKS (UNBUILT):** Visible terminology now **Class = entire lesson**, **Session = each internal work/pose segment**, **Break = regular/special/opening-group rest**, **end-of-class summary** distinct. Editor, Sounds, saved classes, visual timeline, Timer captions updated without changing persisted Kotlin/JSON signatures. Timer's five right-side live values: Clock; Class start; Class end target; current Session/Break start; current Session/Break projected end. Pure `ClassClockTracker` uses actual wall time for live labels but leaves monotonic timer intact; Next marks actual physical start of new segment even when virtual elapsed jumps, Pause hides unknown end, Restart updates class start while keeping planned end. **8 new JVM tests written (78 known JVM source methods total), 9 Android instrumented sources; 0 new Gradle/device results for this change.** Test timing with start, Pause/Resume, Next Session→Break, Restart, summary completion; verify no preset loss on update.


> **D-078 TIMER REINTERPRETATION (2026-10-24) — SOURCE PENDING QA:** New user clarification: controls **Pause ↔ Resume**, **Restart session** (same-session restart, cue C on success, shorten final pair), **Next** (immediate next phase **within same session**, NOT next saved preset; ending pose A, break B, final summary C). Signed net time correction lets Next lengthen or shorten final two regular poses to preserve original finish clock according to approved ending choice, while summary and intervening rest stay fixed. Reject unsafe/late last-pair rewrite atomically with no cue. Added seven domain Next tests + three signed final-pair tests, corrected obsolete 10-note count assertion to current 20×250ms. **70 JVM @Test + 9 Android instrumented test methods WRITTEN; new feature NOT COMPILED/RUN ON PHONE yet.** Device acceptance: start Draw1425, Next opening Pose1→Pose2 A, break→pose B, Restart→Pose1 C, pause→resume unchanged; original End clock stable if feasible; no "next saved session" message. No update-install or audible verification yet.


> **2026-10-24 D-077 NEW SOURCE, QA REQUIRED:** Real phone screenshots show 3 tabs, running timer Now/End time, compensated final pair (02:07 recovered; 16:26+16:27 in EVEN_SPLIT) and error when Next pressed with only one saved preset. User finds tabs overlapping status bar, verbose Stop text and dull melodies. Source fixes: Android `safeDrawingPadding()` root; contextual per-button Timer Help dialogs, clarified Next = next different saved approved preset (never silently stop if none); six brighter 5s rhythms in 20×250ms slots/accented beats, enum IDs unchanged. Added 3 JVM rhythm tests. **Current 60 JVM + 9 Android instrumentation methods written; new changes NOT built/run on phone.** Check safe top/tab gap, Help content, short Stop layout, no-next warning, prior sound selection persistence, full five-second rhythm previews and automatic cues after update-install; background/lock audio remains separate.


> **LATEST REAL PHONE BUILD (2026-10-24 11:28):** After three-tab UI/stop-action source commits, screenshot shows `:app:testDebugUnitTest` and `:app:assembleDebug` succeeded, **`BUILD SUCCESSFUL in 34s`**, 42 actionable tasks (10 executed, 32 up-to-date). **Compilation + JVM Gradle task + debug APK assembly PASS.** Screenshot does not show exact Git SHA or JUnit XML counts. Updated app installation and manual UI/timer/sounds testing remain **PENDING**. Next device checks: Editor/Sounds/Timer tabs; timed session continues while editing Sounds; two Stop actions differ from Pause; restart shortens final two regular poses while retaining original expected end when feasible; next loads next saved approved preset; small Now/End clock; proportional timeline. Preserve existing app data, no instrumentation pass claim.


> **2026-10-24 NEW THREE-TAB UI / TIMER CONTROLS — NOT YET COMPILED:** User requests separate Editor/Sounds/Timer tabs; change 5s melody mid-session without losing ticking/auto cues; no Timer Test A/B/C or Editor buttons; Pause toggles Resume; **Stop and restart session** restarts same approved plan at phase 1 while shortening two final poses to preserve original end time, or errors if infeasible; **Stop and go to next saved session** selects next stored approved preset (wrap; never silently reselect only saved current preset), ready for explicit Start. Current time and expected finish clock top right, source `TimelinePhaseChart` proportional chronological graphic/legend under editor text phases. Android branch source added with **2 new JVM tests**: **57 JVM methods written**, **9 Android instrumentation** source methods, **0 new device build/result after latest commits**. Older `BUILD SUCCESSFUL in 28s` **does not validate this update**. Run Gradle and update APK before claiming a pass, test navigation continuity, audible cue after Sounds tab switch, stop-and-next with 2+ saved presets, stop-restart compensation, and clock labels. Preserve app data.


> **USER-REPORTED AUDIO PREVIEW PASS (2026-10-24):** Following APK update with the `MODE_STATIC` initialization fix, user replied **"הכל עובד"** to the test request for the three A/B/C `Play 5s` previews. Mark **manual melody previews device PASS (user-reported)**. **Do not infer automatic cue A/B/C at phase boundaries, actual timed 5.000s precision, background/locked-screen reliability, final pair interruption logic or Android instrumentation PASS.** Next: test approved 3m session (no openings, `POSE_FIRST`, 1m regular pose, 1m regular break, 0 special count, 0 summary); A at 01:00, B at 02:00, C at 03:00. Then separate approved final-pair compensation check.


> **2026-10-24 10:28 PHONE REBUILD EVIDENCE:** Following the static `AudioTrack` initialization source fix, user screenshot shows `:app:testDebugUnitTest`, `:app:assembleDebug` and **`BUILD SUCCESSFUL in 28s`**; 42 tasks (8 executed, 34 up-to-date). This verifies the Gradle tasks, NOT audible playback, device install, individual JUnit XML count, or instrumentation execution. **Next K04 gate remains audible A/B/C `Play 5s` in updated APK**, then automatic timer cue checks. Do not mark audio PASS before observation.


> **K04 LIVE AUDIO FAILURE + PRE-WRITE STATE FIX (2026-10-24):** Installed editor screenshot shows selectable five-second melodies but pressing preview produces red error `Android audio output is not initialized.`, with **no sound**. The old static `AudioTrack` initialization check was wrong: before `write()`, success is `STATE_NO_STATIC_DATA` (2), **not** `STATE_INITIALIZED` (1). Native-branch fix [f083106](https://github.com/AviY99/Atelier-Time/commit/f083106d6ce8c8d4ae414ff8dab6d320543aa71c) checks actual uninitialized state before PCM write and full initialized state after PCM write, with a new device regression [4714319](https://github.com/AviY99/Atelier-Time/commit/471431952b568b67bd58bf9a0cae714941438f01). **Source only / pending Gradle rebuild + phone retest:** old 34s Gradle PASS predates this fix; no cue/audio PASS claim. Current test sources **55 JVM + 9 instrumentation**; new Android test unexecuted. Check previews and automatic 3m A/B/C events, then pause/stop reflow and locked-screen support separately.


> **NEW PHONE BUILD SCREENSHOT — 2026-10-24 10:17:** `:app:testDebugUnitTest` and `:app:assembleDebug` complete in **`BUILD SUCCESSFUL in 34s`** (42 Gradle tasks: 10 executed, 32 up-to-date), showing Kotlin test compilation and APK packaging. Current branch has **55 JVM `@Test` methods** and **8 Android instrumentation methods** in source, but screenshot does **NOT identify the compiled commit or report actual JUnit XML per-test count**. Installed runtime confirmation for **five-second distinct selected melodies, automatic phase-end playback and final two-pose interruption adjustments** is still **PENDING**. Foreground-service/locked-screen audio remains unimplemented. Test with 3m real approved session: POSE_FIRST, pose1m/rest1m/summary0/special count0; expect A at 1m, B at 2m, C at 3m. Do not uninstall/clear storage when installing updated APK.


> **2026-10-24 NEW AUDIO/INTERRUPTION CODE — NOT YET TESTED:** User confirms app works/saved preset survives update, **but automatic transition cues were silent and DTMF previews too similar**. Added six selectable distinct 5s melodies (three independent main-editor settings, persistent in separate private preferences), Android AudioTrack playback, per-pose/break/session transition cues, plus safe runtime-only final-pair compression after Pause/Stop in Remainder/Even/Custom modes. Updated pure Kotlin tests now **55 written**, Android instrumentation **8 written** (adds isolated melody settings round-trip). Previous phone `BUILD SUCCESSFUL in 37s` predates these code commits. **0 new pass claims; no updated APK install / locked-screen validation.** Test focus: all cues triggered exactly once during foreground phase endings; three melody settings saved across app restart and audible differences; 5s duration; final-pair invariant/rest/summary/end-time preservation; exceeding 2-minute minimum refused with helpful error; stop/continue vs fresh session distinction. Rebuild with native AAPT2 override, not paid Actions.


> **LATEST 2026-10-24 PHONE GRADLE RESULT — PASS FOR UPDATED D-071 + FIRST K04 SOURCE:** Screenshot shows `:app:assembleDebug`, `:app:compileDebugUnitTestKotlin`, `:app:testDebugUnitTest`, **`BUILD SUCCESSFUL in 37s`**, 42 actionable tasks (8 executed, 34 up-to-date), using CoGo Android ARM64 native AAPT2 override as prescribed. **Debug APK assembly and JVM test Gradle gates PASS for the updated feature branch.** Exact 46 JUnit execution count is not shown independently in XML; 7 Android instrumentation tests NOT RUN. Installation of newly built APK, actual K04 timer/audio, D-071 group-break interaction, and persistence of saved sessions across update/restart/offline remain **NOT YET VERIFIED**. No claim of background/locked screen reliability.


> **NEW D-071 GROUP BREAK ACCEPTED; TEST SOURCES PENDING (2026-10-24):** User confirmed positive break after last opening group, zero allowed earlier, and reuse of existing first regular break when the two overlap. Scheduler/editor/optional saved-JSON rule implemented on live Android branch with legacy saved-signature preservation. Added **6 JVM scheduler + 2 JVM editor + 1 JVM catalog + 1 Android instrumentation** new tests. **TOTAL WRITTEN: 46 JVM / 7 Android instrumentation; these added tests HAVE NOT RUN.** Prior verified Gradle `:app:testDebugUnitTest` success only covers old checkout (32 source methods). Run phone Gradle native AAPT2 override again before claiming rule works. Example A expected exact original timestamps, including 00:20–00:25 one break; group break validation and signed persisted settings included.


> **2026-10-24 ACTUAL UI / K04 SOURCE UPDATE:** User screenshot shows Atelier Time **installed/running on Android** with Compose editor/Example A timeline, approved state and one saved selected preset. **K02 first launch/preview observed**, but full editing checks and K03 cold relaunch/offline CRUD pending. New K04 first source increment adds `MonotonicSessionTimer`, timer page/start-pause-resume-stop, three distinct Android tones and contextual editor/group help. **Five additional JVM test methods WRITTEN NOT RUN; total 37 JVM source tests, six Android instrumentation.** New code not built/installed; prior `:app:testDebugUnitTest` 32-method-era Gradle task and `:app:assembleDebug` succeeded on phone at earlier commit. The D-071 new per-opening-group break rule is logged for scheduling-policy specification; Example A remains unchanged. K04 background/locked-screen sound reliability and D-016 granularity are still pending.


> **APK FILE VERIFIED ON PHONE — 2026-10-23:** User-provided terminal screenshot confirms file `android-native/app/build/outputs/apk/debug/app-debug.apk` exists, reported **12M** and generated SHA-256 `d793f6f322000307fb4d636ff3c6d842dad5c3724517776ba3de81dedd78ae77`. `:app:assembleDebug` previously finished **BUILD SUCCESSFUL in 47s**. **APK compilation, file existence and checksum PASS**; installation, initial launch, editor/UI and offline preset storage/reopen **NOT YET VERIFIED**, as are six Android instrumentation tests. K04 not implemented.


> **LATEST K02 BUILD EVIDENCE (2026-10-23):** Android phone screenshot confirms **`:app:assembleDebug` BUILD SUCCESSFUL in 47s**, 36 Gradle tasks (18 executed, 18 up-to-date), including `:app:packageDebug`. Previous real on-phone `:app:testDebugUnitTest` **BUILD SUCCESSFUL in 34s**. Unstripped `libandroidx.graphics.path.so` warning is nonblocking. APK filesystem metadata/size/hash, installation, editor UI and K03 persistence checks remain **NOT YET VERIFIED**; exact JUnit XML count and six Android instrumentation cases are also pending. **K01 Gradle test gate PASS; K02 assembly PASS, install/UI acceptance OPEN; K03 OPEN; K04 not implemented.** No Actions or merge.


> **LATEST REAL K01 RESULT — PASS ON ANDROID PHONE (2026-10-23):** User screenshot confirms Code on the Go `:app:testDebugUnitTest` with the **native CoGo AAPT2 override** finished `BUILD SUCCESSFUL in 34s` (24 Gradle tasks, 10 executed and 14 up-to-date). Android resources, Kotlin compilation and JVM test task completed on-phone. **The screenshot does not expose exact JUnit XML per-method count**, though 32 JVM tests are defined in code. K01 unit-test Gradle gate **PASSED**; APK build `:app:assembleDebug`, real install/UI, K03 offline persistence and six instrumented device tests remain **NOT VERIFIED**. Previous failed-build notes below are historical and superseded for this task.


> **K01 FAILURE ON REAL PHONE (2026-10-23) — AAPT2 ARCHITECTURE, FIX PENDING RETEST:** Gradle `:app:testDebugUnitTest` progressed to Kotlin compile (nonblocking Elvis warning) but FAILED in `:app:processDebugResources` because `aapt2-9.3.1-15703166-linux` could not launch on ARM64 (`Syntax error: word unexpected`; `BUILD FAILED in 3m 40s`). CoGo vendor's native Android AAPT2 flag was omitted from manual Gradle invocation. [Phone runner fix dcb4887](https://github.com/AviY99/Atelier-Time/commit/dcb4887c7e18e1c88e8ac7c23e007e3408f6af51) now adds `-Pandroid.aapt2FromMavenOverride=${ANDROID_HOME}/build-tools/36.0.0/aapt2`, which still requires on-phone binary validation and real retest. **32 JVM tests NOT PASSED / acceptance OPEN; six instrumentation tests NOT RUN; no APK.**


> **2026-10-23 K01 ON-PHONE BUILD BLOCKER, CORRECTION PENDING RETEST:** User's first real CoGo Gradle `:app:testDebugUnitTest` ended `BUILD FAILED in 5m 1s` because `compileSdk=35` was lower than AndroidX Core API 36 and Compose Runtime 1.12 API 37 metadata requirements. Phone screenshot subsequently confirmed only Android SDK platforms **35 and 36** installed and no `sdkmanager` found via CLI. Changed native branch `app/build.gradle.kts` to `compileSdk=36`, Compose BOM `2026.06.01` (pre-API37 Compose 1.12), Activity Compose `1.12.4` in [commit a4b9469](https://github.com/AviY99/Atelier-Time/commit/a4b94692d14796e9090420e6f389a64eee31c3cf), preserving `targetSdk=35` and `minSdk=26`. **Source committed but NOT retested; no PASS may be claimed.**


> **PHONE TEST RUNNER PREPARED — NOT EXECUTED (2026-10-23):** [Code on the Go phone runner instructions](https://github.com/AviY99/Atelier-Time/blob/android-native-kotlin-compose/android-native/PHONE_RUNNER.md) and [restricted task runner](https://github.com/AviY99/Atelier-Time/blob/android-native-kotlin-compose/android-native/tools/phone_task_runner.py) are in the implementation branch. The GitHub-Issues **doctor** task is queued at [Issue #4](https://github.com/AviY99/Atelier-Time/issues/4) but has not been executed. The user's actual CoGo/Gradle command-line integration, all **32 JVM + 6 device instrumentation methods**, debug APK/install and persistence checks remain **UNVERIFIED**. Do not count infrastructure source creation as a test pass.


> **LATEST K03 QA CHECKPOINT — 2026-10-23, AFTER D-070:** Committed an actual Android storage safeguard against wrong-type SharedPreferences values and direct overwrite of corrupt/future-version data on branch `android-native-kotlin-compose`. Added two Android instrumentation regression methods. **32 JVM JUnit + 6 instrumentation tests written, 0 executed in this follow-up; no Android SDK/Gradle/device available here.** GitHub source readback succeeded, which does *not* verify Kotlin compilation. Acceptance tasks are still `:app:testDebugUnitTest`, `:app:assembleDebug`, phone install/relaunch and saved-session manual QA, and `:app:connectedDebugAndroidTest`. **K01–K03 unverified; K04 not implemented.** The historical 4-instrumentation count below is superseded.


> **LATEST K03 D-070:** Approved offline preset lifecycle now has a new pure Kotlin `PresetCatalog` with approval/regeneration integrity, plus local Android `SharedPreferences` JSON adapter, and Compose saved preset actions. Wrote **32 JVM JUnit methods** (16 schedule, 6 input, 10 catalog) and **4 Android device instrumentation methods** verifying persisted reload, CRUD, corrupt/future JSON. **No Kotlin, Gradle, Compose or instrumented test has been executed**. Native evidence gates remain `:app:testDebugUnitTest`, `:app:assembleDebug`, on-phone install + persistence across Activity/device restart, and `:app:connectedDebugAndroidTest` when supported. Explicitly isolate test preferences namespace to avoid erasing real user's saved data. Original Dart 35+11 pass evidence remains distinct. See [K01–K06 acceptance requirements](ANDROID_NATIVE_EXECUTION_AND_TEST.md).


> **LATEST — D-068/D-069 ANDROID KOTLIN TEST PROCESS (2026-10-23):** Approved product requirements unchanged. Primary tests now Gradle `:app:testDebugUnitTest` from nested `android-native/` within Android on-phone Code on the Go, plus `:app:assembleDebug`, actual APK installation, on-device input/preview tests, offline persistence and background timer/audio acceptance. **21 Kotlin JUnit test methods AUTHORING COMPLETE, NO EXECUTION YET.** Source/expectations in [Android native execution/test gates K01–K06](ANDROID_NATIVE_EXECUTION_AND_TEST.md) and [native tests](https://github.com/AviY99/Atelier-Time/tree/android-native-kotlin-compose/android-native/app/src/test). Historical 35/35 Dart scheduler and 11/11 adapter passes are separate verified inputs for Kotlin parity, NOT proof Kotlin/Compose works. Flutter T19–T23 source is historical and unrun. No paid cloud CI.


> **2026-10-23 — NEW D-062 ANDROID-PHONE-FIRST FEASIBILITY TESTS (NO NEW PASS RESULTS):** F02 now means working **Android phone on-device edit/compile/test/preview** and F04 prioritizes **actual Android APK built/installed on the phone**; iOS/Windows/macOS builder proof becomes a later port phase. In [ANDROID_FIRST_PLATFORM_SPIKE.md](ANDROID_FIRST_PLATFORM_SPIKE.md), first run a tiny **real Dart scheduler compiled to JS**, render canonical Example B special break 01:25–01:40 and summary 02:45–03:00 in Android browser; second verify **Termux-built Capacitor APK** (not merely browser PWA); third verify three distinct native alerts under Android lock/background. **These new proof gates are UNEXECUTED.** Previously verified 35/35 core Dart and 11/11 adapter Dart results remain unchanged.


## 2026-10-23 — Revised Android phone-first feasibility checks (D-062/D-063; NOT RUN)

**Approved priority:** Develop, debug, test, locally build/install Android on the user's **phone first**, then efficiently port to iOS/Windows/macOS later. Earlier D-061 simultaneous multi-platform toolchain gate is no longer a prerequisite for initial Android progress; original functionality remains binding.

- A01 **CoGo on-device environment:** install official ARM64 [Code on the Go](https://appdevforall.org/code-on-the-go/) current release; record release, Android version/device resources. NOT YET PERFORMED.
- A02 **Minimal Kotlin app/Android APK:** create basic Kotlin Android project, run build locally on phone, install and open the debug APK; record exact build/test logs. NOT YET PERFORMED.
- A03 **Jetpack Compose feasibility:** test an actually compiling/running simple Compose UI using supported CoGo Kotlin/Gradle version, or document fall back to XML and porting implications. NOT YET PERFORMED.
- A04 **Portable domain/API boundary:** Kotlin scheduling model no Android SDK imports, correct Example A/B regression oracles and 25/5 warning behavior matching existing Dart tests. Only proceed after A02 success.
- A05 **On-device clock/sounds pilot:** monotonic timer with visible and locked-screen behavior and all three distinct cues; validate Android runtime limitations in real device tests before claiming product quality.
- A06 **Future port readiness:** confirm reusable Kotlin domain structure and Compose Multiplatform path on later target-specific toolchains; do not imply zero migration or that CoGo can build iOS/desktop locally.

Previous evidence: Dart scheduling engine **35/35 PASS** and text-input adapter **11/11 PASS** remain valid test oracles. New Kotlin and APK test evidence **NONE** as of D-063. No paid GitHub Actions.

> **2026-10-23 — D-061 CRITICAL PLATFORM FEASIBILITY GATE:** Original Flutter-only QA plan assumed supported native target runtimes sufficed. User explicitly requires **cross-platform development, test execution, native build/signing, installation and operation**, including accessible Android Termux development. **Do not run more Flutter widget QA as a substitute for checking the entire supported toolchain.** Platform solution is under re-review; see [feasibility assessment](PLATFORM_REASSESSMENT_2026_10_23.md). Historical 35/35 Dart core and 11/11 form-adapter passes remain valid, eight Flutter tests unrun. No new stack or paid cloud build authorized.

## Newly required platform lifecycle acceptance checks (D-061; NOT EXECUTED)

- F01 shared application/code and feature parity on Android, iOS/iPadOS, Windows, macOS (not just framework marketing support).
- F02 reproducible development/edit/preview/test workflow accessible from Android Termux and other relevant workstations, avoiding unsupported SDK assumptions.
- F03 one documented automated test command with real pass/fail output in the chosen developer workflow.
- F04 real offline-capable installation method for all four OS families; **separate signed native packages remain the current spec**, and an installed browser PWA cannot be substituted without explicit approval. Document macOS/Xcode and Windows host or remote builder requirements, signing accounts and artifacts.
- F05 offline persistence across reload/restart, corrupt preset handling and recovery.
- F06 real timed pose/break/summary transitions and three distinct audible alerts across screen lock/background/interruption for each mobile OS; test actual devices, do not rely on PWA setTimeout under lock.
- F07 no-surprise build infrastructure/cost, runner availability under blocked GitHub budget; no paid cloud builds before explicit authorization.
- F08 exact schedule Examples A/B, final-pose choices, D-031/036/037/044 tests preserved and passing in any new runtime/port.

**STOP:** no Stage 2 completion or platform migration until the technical decision has a proven cross-platform lifecycle path, not a list of supported runtimes. All checklist items are planned evidence gates, **none has newly passed**.

**Current evidence — 2026-10-23 v0.8, D-052:** User's Android Termux screenshot confirms `git pull --ff-only && dart test` pulled new T09 regression (`8cf7a24..907f871`) and ended **`00:00 +35: All tests passed!`**. Earlier screenshots showed smoke PASS, `dart analyze` **No issues found!** and 34/34 tests PASS on the preceding checkout (D-048/D-050). Static analysis was **not shown rerun** after adding the 35th *test-only* change. Stage 1 Step 1.4 required acceptance core **PASS / VERIFIED on the isolated Dart branch**; no Flutter build/UI or other-platform validation. Wider Stage 1 optional/edge matrix T13/T14 remains partially covered for later QA.

## Approved requirement tests: D-031 + D-036 (some covered by user-reported passing Dart suite; remaining plan-level acceptance items require mapping)

**Latest D-036 supersedes D-031 only where the older rule allowed 0-minute breaks.** Session total must be >0; configured normal/active special break durations must be >0. The 25-minute accumulated-posing / >=5-minute qualifying rest rule remains advisory, warning-and-acknowledge for **otherwise valid** short rests/long poses. Default new-session breaks remain 5m normal and 15m special, both editable to other **positive** values.

- R-B01: A new session pre-fills normal short break at **5m** and middle/long break at **15m**.
- R-B02: Normal and active special breaks independently accept **positive** 1m, 3m, 5m, 15m, etc.; reject 0 and negative numbers with blocking validation.
- R-B03 (LATEST REVISED): Configured normal break duration **0m is invalid**, blocks preview approval and cannot be accepted via the 25/5 warning acknowledgement.
- R-B04 (LATEST REVISED): Selected special/long replacement break duration **0m is invalid**; a positive special duration is required for any actual replacement.
- R-B05: Changing either duration recalculates the entire schedule and never hardcodes 5m/15m.
- R-B06: **Approved D-037:** Zero *count* of special replacements produces no special break. It is not the same as a 0m break.
- R-B07: **Total full session duration 0m** (or negative) produces a blocking input error; positive sessions pass this basic constraint.
- R-B08: 5 consecutive 5m poses then a >=5m break reset the cumulative posing counter, no warning.
- R-B09: 25m pose -> 5m break -> next pose generates no warning.
- R-B10: 30m pose -> 5m rest triggers a **warning**, not hard-invalid, due to the exceeded 25m posing threshold.
- R-B11: 25m pose -> 3m rest -> further posing triggers warning; 3m rest does not reset the counter.
- R-B12: 20m pose -> 3m rest -> 6m pose triggers warning once cumulative posing exceeds 25m.
- R-B13: Warning cannot be waived by normal approval alone; user must explicitly acknowledge and then approve.
- R-B14: An acknowledged valid 1–4m break exception may be saved/activated without changing entered durations.
- R-B15: Editing rules, changing long-break placement or adjusting final-pose split invalidates warning acknowledgement/approval.
- R-B16: A break >=5m resets the posing counter; a valid positive break <5m does not.
- R-B17: Canonical A/B exact timing and end-pose alternatives are unchanged and generate no 25/5 warning.
- R-B18: **Approved D-037:** 0m reserved summary is permitted for an otherwise valid positive full session; no zero-length summary phase is emitted. This is distinct from invalid 0m full session.

## Approved Stage 1 Step 1.3 acceptance tests (D-037; NOT EXECUTED)

The user **approved these scheduling-policy test expectations in D-037** as described by the [approved review](STEP_1_3_REVIEW.md) and [detailed policy](SCHEDULING_EDGE_POLICY.md). They remain **PLANNED, NOT EXECUTED**, and Step 1.4 implementation is not authorized:
- P-T01: Automatic midpoint chooses the eligible replacement whose **center** is nearest work midpoint, ties to earlier eligible ordinal; A/B exact reference positions.
- P-T02: Multiple special replacements evenly distributed at work fractions j/(N+1), with deterministic ordinal ties.
- P-T03: Manual ordinal selections survive valid edits; invalid ordinals raise errors, never silently relocate.
- P-T04: Zero **count** of special breaks yields no replacement phase, not a zero-duration break.
- P-T05: Opening-only and/or zero reserved-summary sessions when mathematically valid, without zero-duration phases.
- P-T06: Reject overlarge summary, mandatory opening overruns, absent required pose following break, and impossible replacement counts.
- P-T07: Infeasible final-two-pose options disabled with reason, no inserted break/pose.
- P-T08: Explicit custom final-two split may exceed nominal pose length while preserving positive durations and total.
- P-T09: **Approved:** Exactly 25m accumulated posing at session end does not require a trailing break; >25m continuous pose creates warning.
- P-T10: **Approved:** Track warnings within one full activated session; do not automatically carry counters into a separately activated saved preset.
- P-T11: **APPROVED D-044:** Any **positive** special break may be shorter than or equal to the normal break (e.g. 1m or 3m special vs 5m normal, equal 5m vs 5m). Generate correct replacement times, 25/5 warning(s), and require acknowledgment for otherwise valid warnings. Configured 0m breaks remain a blocking error (D-036).
- P-T12: **Approved D-036:** Strictly no zero logical break slots; do not generate 0m break phases or allow 0m configured breaks as an overrideable safety warning.

## Step 1.4 source links and execution hold
- [Pure Dart scheduler](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/lib/src/scheduling.dart) (development branch, uncompiled)
- [Dart test suite](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/test/scheduling_test.dart) (written, NOT RUN)
- [Dependency-free smoke check](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/tool/smoke.dart) (written, NOT RUN)
- [Implementation status](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/docs/STAGE_1_4_PROGRESS.md)
- **Verification remains required:** actual `dart analyze`, `dart test` and smoke run, plus scalability checks. No GitHub Actions workflow or paid runner launched. D-038 is **RESOLVED by user-approved D-044**. The implementation no longer emits `pendingDecision`; positive shorter/equal special breaks are allowed, with warnings based on actual rest. Dart tests remain unexecuted.

## Additional regression sources for DP refinement (D-043; NOT RUN)
- `test/scheduling_test.dart` on [the Step 1.4 branch](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/test/scheduling_test.dart) includes a 97-minute positive session where the unmodified ordinary cycle would be invalid but **two special replacements at break ordinals 2 and 3 make the timeline feasible**; this guards against the former false rejection.
- A longer 480-minute session (P5m/B1m/L5m, five special replacements) checks deterministic ordinals **13, 26, 38, 51, 64** and replacement count without exponential search.
- An **independent Python model comparison** checked the exact DP scoring against a brute-force reference over **25,000 randomly generated small scenarios** and reproduced both canonical A/B replacement positions. **This was NOT the Dart engine or Dart tests being executed.**
- **Dart SDK remains unavailable**; no analyzer/test success claim. Do not enable Actions or paid GitHub build resources without budget clearance/authorization.

## Stage 1: deterministic scheduling engine
- T01 Example A baseline exact phase order and boundaries from EXAMPLES.md, down to each opening pose. Expect 130m posing + 35m breaks + 15m summary = 180m.
- T02 Example B baseline exact phase order and boundaries. Expect same totals and 3h exact.
- T03 Example A: remainder last two 25m + 5m break + 10m, exact 02:05–02:45 bounds.
- T04 Example A: even last two 17m + 5m break + 18m, exact 02:05–02:45 bounds.
- T05 Example A: custom sample 20m + 5m break + 15m, exact times.
- T06 Example B: remainder last two 25m + 5m break + 5m, exact 02:10–02:45 bounds.
- T07 Example B: even last two 15m + 5m break + 15m, exact times.
- T08 Example B: custom sample 18m + 5m break + 12m, exact times.
- T09 Last-two slider extremes: 1m / (T−1)m, vice versa; no zero/negative pose and stable summary.
- T10 When final regular pose fits exactly, no incomplete-pose dialog.
- T11 Long break occupies one existing short-break slot; no double break; verify number and placement.
- T12 Cycle starts with break (A) or pose (B) as specified, without a phantom trailing break.
- T13 Summary 0, varied session lengths and opening-group counts (subject to edge rule decisions).
- T14 Reject negatives, impossible counts, overlarge summary, insufficient work block, too many long breaks, and infeasible slider options with useful feedback.
- T15 Determinism: identical rules => identical approved sequence and phase IDs/bounds; no overlaps/gaps.
- T16 Full timeline sums to D and every stage end equals next stage start.
- T17 Numerical rounding, especially near-equal odd total => floor/ceil; no cumulative drift.
- T18 No silent auto-approval of a shortened final pose.

## Stage 2: editor and approval (SOURCE DRAFTED D-054; no Flutter tests run)
- T19 All editable fields accept valid values; changes force fresh preview and approval.
- T20 Preview displays each phase type, duration, start, end and the long break replacing a short one.
- T21 Three final-pose options visible with accurate times; moving ruler updates both last poses and break times instantly.
- T22 User can cancel preview; no unapproved preset is activated/saved.
- T23 Keyboard/touch accessibility, large text and native controls where feasible.

## Stage 3: preset library
- T24 Create, save, list, rename, duplicate, edit, delete and activate.
- T25 Approved final-pose selection persists after restart; editing invalidates prior approval.
- T26 Presets usable offline; corrupt data fails safely, no silent data loss.

## Stage 4: live timer and audio
- T27 Accurate current stage countdown and overall remaining time at every boundary.
- T28 Pose end uses cue A, break end cue B, complete end cue C (no duplicate/incorrect cues).
- T29 Pause freezes remaining time; resume continues correctly; stop requires sensible behavior.
- T30 App sleep/OS interruption, notification/lock-screen and audio-permission scenarios as applicable to target OS.
- T31 Clock timing is accurate across many consecutive stages; completion exactly at total deadline.
- T32 Summary runs as final 15m phase, and final completion sound occurs at its end.

## Stage 5: packaging and final acceptance
- T33 Install/uninstall app on approved platform, run without network, persistence after reboot.
- T34 Audio audible and controllable on actual device; expected lock/background behavior documented.
- T35 Usability verification with live-model drawing/sculpting use cases.
- T36 Build version, release notes, license/privacy statement and installation instructions present as appropriate.

## Required evidence
For every executed test record: test ID, code commit, target platform/config, input, expected, actual, pass/fail, date and known limitations. A plan is NOT a passed test. Report real results in WORK_LOG.md (or a dated test-results file) and HANDOFF.md. Ask before modifying user-approved behavior to make tests pass.

## Acceptance gate for Stage 1
At minimum T01–T12, T15–T18 passing on the selected core stack, with documented disposition of Stage 1 edge rules. The full reference examples must match exact phase order and timestamps. Later-stage tests can remain pending.

## Step 1.4 local execution method and approval hardening (D-046)

- [Windows verification script](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/tool/verify.ps1) and [macOS/Linux verification script](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/tool/verify.sh) run `dart tool/smoke.dart`, `dart pub get`, `dart analyze` and `dart test` with a local `atelier-time-verification.log`.
- Test code on the branch additionally asserts the approved session preserves a **full immutable safety-warning record and exact 25-minute time offset**. `ScheduleCandidate` construction is now library-private so callers cannot fabricate a candidate for approval.
- **ALL Dart checks remain UNEXECUTED.** Current container lacks Dart/Flutter SDK and cannot resolve the Dart download host. Avoid GitHub Actions or paid runners under user's billing block. Install the **Dart SDK only** on an available local test machine to unblock independent execution; share the log for error correction.

## 2026-10-23 — First real compiler execution (FAILED; fix pending retest)
- **Environment:** Android Termux Dart 3.13.7 stable `android_arm64`.
- **Command:** `dart tool/smoke.dart` on user-cloned `stage-1-4-scheduling-core`.
- **Actual:** Compiler error `lib/src/scheduling.dart:621:28`, `num` cannot be assigned to `int?` at `offsetSeconds: crossingOffset`.
- **Fix committed:** `0dedd95756b6d3984b2e16548a58599179fbf7ff`; typed integer conditional instead of `math.max` inference.
- **Next:** User runs `git pull --ff-only && dart tool/smoke.dart` from `~/Atelier-Time` (no reinstall). After smoke success, run `dart pub get`, `dart analyze`, `dart test`. **No pass evidence for updated code yet.**

## 2026-10-23 — User-reported Step 1.4 Dart suite PASS (D-049)

**Environment:** Android Termux Dart SDK 3.13.7 stable on `android_arm64`; existing development-branch clone.

**Sequence:** Initial `dart tool/smoke.dart` compile FAIL in D-047 was fixed by `0dedd95756b6d3984b2e16548a58599179fbf7ff`, followed by user-confirmed smoke PASS (D-048). User was then instructed to run `dart pub get && dart analyze && dart test`, and responded **“All tests passed!”**.

**Evidence and limits:** Record **USER-REPORTED PASS** of the written Dart unit test suite and apparent success of the preceding analyzer step if the chained command ran as supplied. The currently committed `test/scheduling_test.dart` defines **34 unit tests**; user did not supply exact runner test count, stdout, `dart analyze` transcript, or local git commit SHA. Do not call those independently verified or confuse them with a multi-platform app build.

**Acceptance audit still needed:** Map current test source to Stage 1 required T01–T12, T15–T18 and any Step 1.3 policy acceptance tests not yet covered, adding and executing missing cases within authorized Step 1.4. Flutter UI, app packaging, OS-specific builds and device timer/audio behavior belong to later authorized stages. Do not mark Stage 1.4 completed, merge the branch, or initiate Stage 2 solely based on this high-level report. GitHub Actions budget remains untouched.


## 2026-10-23 — Screenshot-verified Android analyzer and test suite (D-050)

User provided Termux screenshots from `~/Atelier-Time` after the corrected smoke run, showing the output of `dart pub get && dart analyze && dart test` on **Dart 3.13.7, Android ARM64**:

```text
Changed 49 dependencies!
Analyzing Atelier-Time...
No issues found!
Built test:test.
00:01 +34: All tests passed!
```

**Evidence status:** Screenshot-confirmed **`dart analyze`: 0 issues**, and **`dart test`: 34 passed** for currently checked-out source. No failed tests shown. This upgrades earlier D-049 user-only report to verified screenshot evidence (D-050). Smoke check was separately confirmed as passed. No GitHub Actions, paid runner or build was used.

**Stage 1 acceptance mapping against the 34 currently passed test definitions:**

| Planned check | Existing written tests | Coverage disposition |
|---|---|---|
| T01–T08 A/B exact schedules and 3 final-pose modes | Canonical Example A/B six tests, exact timelines and final-pose choices | **Covered and screenshot-reported passing** |
| T09 custom ruler split limits, minimum 1m on either pose | Custom 34m+1m; 0m custom rejected; exact valid 1m+34m **not explicitly tested** | **Partial — add opposite endpoint test before Step 1.4 completion** |
| T10 exact final fit, no adjustment prompt | `exactly fitted last pose has no incomplete choice prompt` | **Covered** |
| T11 one special break replaces one ordinary slot | A/B baseline and manual ordinal 3, multiple-placement assertions | **Covered** |
| T12 break-first and pose-first, no phantom trailing break | A/B baselines; `break-first cannot finish on a trailing break` | **Covered** |
| T15–T16 deterministic output, full exact contiguous phase coverage | `identical rules generate identical phase IDs and timeline`; `expectContiguous` invariant helper | **Covered** |
| T17 floor/ceil even split with odd available pose total | A 17+18, B 15+15 | **Covered** |
| T18 no silent shortened-pose approval | `a shortened final pose requires an explicit choice` | **Covered** |
| Additional D-031/D-036/D-044 | Hard 0-minute errors, 25/5 warning/acknowledgment, equal/shorter special breaks | **Covered by written cases** |
| Wider T13–T14 and P-T edge matrix | Representative edge cases present; many combinatorial extremes not exhaustive | **Partial / remains in QA backlog** |

**Gate:** Step 1.4 scheduling core has strong real Android Dart success. **The written test plan still calls for T09 both endpoints. Do not mark Step 1.4 complete or merge into main until remaining required edge coverage is addressed and retested.** Flutter UI, APK, cross-platform builds and Stage 2 are not covered or authorized.


## 2026-10-23 — T09 opposite endpoint regression now WRITTEN, NOT RUN (D-051)

Commit [`4b2f5d9`](https://github.com/AviY99/Atelier-Time/commit/4b2f5d9fdd8dabc6dea8dcc0204c5062c7b6916a) adds the **35th** unit test on `stage-1-4-scheduling-core`: T09 first custom adjusted pose **1 minute**, later pose **34 minutes**, intervening 5-minute short break, fixed 15-minute summary. Asserts exact Example A phase times, positive durations, 25/5 rest warning and acknowledgment. This complements existing 34m+1m and invalid 0m cases.

**Execution:** The *older 34 tests* remain screenshot-confirmed PASS (D-050), static analysis showed no issues. **New test has NOT run**; it must be tested with `git pull --ff-only && dart test` on Android Termux. No Stage 2 authorization or Actions.

## 2026-10-23 — T09 newly executed; all 35 Dart tests PASS (D-052)

- Screenshot-visible terminal: `git pull --ff-only && dart test`; branch updated `8cf7a24..907f871` including the new T09 test file and branch progress notes.
- **Observed:** `00:00 +35: All tests passed!` on Termux Dart 3.13.7 Android ARM64. This **supersedes** the earlier D-051 “new test not run” note above.
- **Previously observed** `dart analyze` `No issues found!` before the 35th test-only addition; don't claim it ran on the updated checkout. Previous smoke also passed.
- Gate mapping: required Stage 1 **T01–T12, T15–T18 COVERED/PASS by 35 source tests** with both T09 endpoints. `T13/T14` broad stress/invalid-input combinations **PARTIAL** and recorded as extended later QA; they are *not* part of minimum Stage 1 gate.
- **Outcome:** Step 1.4 **COMPLETE/VERIFIED ON DEVELOPMENT BRANCH**. Branch unmerged; full Flutter editor, persistence, timer/audio, Android APK and Windows/Apple releases unimplemented and unauthorized in this step.

## 2026-10-23 — New Stage 2 source QA checklist (D-053/D-054)

Prototype: [apps/atelier_time_flutter/lib/session_editor_page.dart](https://github.com/AviY99/Atelier-Time/blob/stage-2-session-editor/apps/atelier_time_flutter/lib/session_editor_page.dart) on isolated Stage 2 branch, integrating the **35/35 verified pure Dart scheduling core**. **No Flutter compilation or widget execution has occurred**.

- **T19 editor/regeneration — PLANNED/UNRUN:** Enter valid/invalid whole-minute totals (including 0 full session), summary 0 and overrun, pose >0, normal/special break >0, replacement count 0/positive, add/edit/remove/reorder opening groups, pose-first/break-first, automatic/manual ordinals. Every edit regenerates and invalidates prior ending selection and approval.
- **T20 preview — PLANNED/UNRUN:** Exact A/B phase timeline with durations/elapsed boundaries, chronological phase kinds and break ordinal, pose/rest/summary totals, explicit blocking errors vs D-031 advisory warnings.
- **T21 ending ruler — PLANNED/UNRUN:** Remainder/even/custom ending choices, one-minute slider endpoints, positive two-pose window, and no phantom intervening break; disable unavailable pair choices and require final selection if shortened.
- **T22 explicit approval — PLANNED/UNRUN:** Block invalid or unresolved candidates; require separate 25/5 acknowledgment; approve exact timeline in memory; changing any input/slider invalidates prior approval. No preset persistence, which belongs to Stage 3.
- **T23 responsive/accessibility — PLANNED/UNRUN:** Single-column Android phone layout, 2-pane tablet/desktop layout, keyboard/numeric fields, readable contrast/text and screen-reader labeling. Temporary Material theme is not final branding.

Run `flutter pub get`, `flutter analyze`, `flutter test`, and manual preview checks on an actual Flutter-capable environment when one is available. User's Termux Dart installation validates the pure Dart scheduling engine but **does not validate this Flutter UI**. Avoid paid GitHub Actions. PR #1 verified core and draft PR #2 prototype remain unmerged.


## 2026-10-23 — Stage 2 widget-test sources (D-055, UNEXECUTED)

[Eight Flutter widget tests](https://github.com/AviY99/Atelier-Time/blob/stage-2-session-editor/apps/atelier_time_flutter/test/session_editor_widget_test.dart) now exist on the isolated Stage 2 branch and exercise T19–T23: parsing/hard zero errors, canonical B preview, opening groups, last-pose choices and edit invalidation, 25/5 acknowledgment, and wide layouts. `flutter_test` was added under the nested app's dev dependencies.

**Test execution:** **NOT RUN**. Current assistant machine has no Flutter; user's Android Termux only has Dart. The separately verified 35/35 **core Dart** tests remain evidence for Stage 1 only and do not validate the Flutter UI. When Flutter is available: `cd apps/atelier_time_flutter && flutter pub get && flutter analyze && flutter test`. Document exact failures and correct them before closing Stage 2.


## 2026-10-23 — Stage 2 pure Dart form-input regression tests (D-056)

The isolated Stage 2 branch now contains `lib/src/editor_draft.dart`, an editor text-to-rules adapter independent of Flutter, and **11 new Dart unit-test definitions** in `test/editor_draft_test.dart`. They cover Example B fields, altered minute values, zero-duration error handling, malformed numeric inputs, ordered opening groups, manual replacement ordinals, N=0 inactive ordinals and D-044 shorter positive special breaks.

These eleven new tests have **NOT been executed**. They may run with the user's existing Android Termux Dart SDK: from the Stage 2 branch repository root use `dart pub get && dart test test/editor_draft_test.dart`. The old Stage 1 suite's 35 passing tests do not establish the new adapter's results. Separately, the eight Flutter widget tests still require a Flutter SDK and have not run.


## 2026-10-23 — Android editor-adapter test run: 11 PASS, Git checkout unresolved (D-057)

User's Termux screenshot visibly reports `Got dependencies!` and `00:00 +11: All tests passed!` following `dart pub get && dart test test/editor_draft_test.dart`. This **confirms 11 tests passed in that local working tree**.

**Provenance qualifier:** Immediately beforehand, `git fetch` of the Stage 2 remote branch succeeded, but `git switch --track origin/stage-2-session-editor` **FAILED** (`starting point ... is not a branch`). The screenshot does not show the active local branch, HEAD SHA, or tracked status of `test/editor_draft_test.dart`. The file is present in the Stage 2 GitHub branch and absent from the Stage 1 branch, so **do not assign the success to the Stage 2 checked-out commit without verification**. Request the non-mutating branch/status output before attempting a safe checkout repair. No Flutter widget tests have run; the 35 confirmed Stage 1 tests remain valid.

## 2026-10-23 — Git checkout diagnosis refined (D-058)

Second screenshot confirms `git branch --show-current` = `stage-1-4-scheduling-core` and `git rev-parse --short HEAD` = `907f871`. `git status --short --branch` lists Stage 2 additions **staged** in that Stage 1 worktree, including `A test/editor_draft_test.dart`, `A lib/src/editor_draft.dart`, and Flutter prototype files, plus `M` updates and untracked `pubspec.lock`. `git ls-files test/editor_draft_test.dart` lists the file because it is in the **index**, not because HEAD contains it. This fully explains screenshot **`+11 All tests passed!`** despite a failed branch switch. **Accurate evidence: 11 tests passed using Stage 2 code in a mixed Stage 1 working tree; clean checked-out Stage 2 branch verification pending.**

Safest fix is to preserve that original worktree and create a **separate Stage 2 Git worktree** from fresh `FETCH_HEAD`; never instruct a destructive reset or cleaning of untracked files. Once in separate clean checkout, re-run `dart pub get && dart test test/editor_draft_test.dart` and report terminal result. Eight Flutter widget tests still not executed.

## 2026-10-23 — CLEAN Stage 2 Dart adapter test verification (D-059)

Two new user screenshots show Android Termux working in **`~/Atelier-Time-Stage2`**, with `git rev-parse --short HEAD` displaying **`3bb2bad`** immediately before `dart pub get && dart test test/editor_draft_test.dart`. Test output concludes **`00:01 +11: All tests passed!`**. GitHub commit comparison verifies short SHA `3bb2bad` is the **exact tip of `stage-2-session-editor`** (`3bb2bade992fddab7dc3d7ece31d9aa58231380f`, ahead/behind 0). Thus the Stage 2 form-adapter's 11 Dart tests are **SCREENSHOT-VERIFIED PASS on the correct branch commit**. The prior D-057/D-058 mixed-worktree caveat is historical and RESOLVED for this run.

**Not yet verified:** Stage 2 branch full `dart analyze && dart test` combined result (the prior 35 Stage 1 tests were verified separately); all eight nested Flutter widget tests remain unrun due to lack of Flutter SDK. Suggested next free verification: `cd ~/Atelier-Time-Stage2 && dart analyze && dart test`. Do not treat Android Dart success as UI compilation or APK creation.


## 2026-10-23 — Stage 2 Flutter SDK errors during root analyzer (D-060)

The user ran `dart analyze && dart test` in Termux from the clean Stage 2 worktree; the analyzer produced very many diagnostics and failed, so the chained `dart test` did NOT run in that attempt. Root package is pure Dart; nested `apps/atelier_time_flutter/` needs Flutter SDK, which Termux lacks. These unavailable Flutter dependencies can produce cascading unresolved import/type errors; screenshot alone does not establish that every diagnostic has this cause.

Committed `analysis_options.yaml` on Stage 2 branch to exclude the nested Flutter app from **root-only Dart analysis**, and a separate Flutter package `analysis_options.yaml` so the app's widgets are still examined when a real Flutter SDK can run `flutter analyze`. No scheduling/adapter behavior was changed.

**Next verification pending**: user updates the Stage 2 worktree and runs `dart analyze && dart test` at the root. Expected 35 core + 11 editor adapter = 46 root Dart test definitions. **Not yet observed**. Eight Flutter widget tests also remain UNRUN; they cannot be validated by Dart alone. Earlier separate 35 and 11 passing suites remain valid evidence.
