# Product specification — Atelier Time

> **USER-APPROVED GLOSSARY D-079 (2026-10-24):** In visible Android product terminology, **Class** is the entire timed lesson, formerly called the UI "session"; **Session** is one individual timed work/posing segment, formerly called a "pose"; **Break** names all rests, qualified by type **regular / special / opening-group**. End-of-class summary is a separate reserved concluding period. No persisted model-field or JSON schema rename is permitted solely for copy changes: offline approved presets remain compatible. At top-right Timer display five real local time values updated with running clock and phase: clock now; class actual start; class planned/actual end; current Session/Break actual start; current Session/Break projected finish, substituting `— (paused)` for an indeterminate end. Next and Restart must update physical boundary clocks without changing underlying monotonic timer or original finish target. See [D-079](DECISIONS.md), [TEST_PLAN](TEST_PLAN.md). Source authored, phone QA not yet performed.


> **2026-10-24 D-073/D-074 USER REQUEST, IMPLEMENTATION SOURCE ONLY:** Phone test confirmed updated installed editor and saved preset survive APK update; existing DTMF sounds in Test A/B/C are indistinguishable and automatic phase-end alarms were **NOT heard** (unresolved defect on prior APK). User now requires three independently selectable, distinct **five-second melodies** from the main editor, one for pose end, break end, and full-session completion; choices stored offline. User also requires interrupted session continuation/restart to shorten the **last two regular poses** based on earlier chosen Remainder/Even/Custom final split. Implemented source adopts fixed originally scheduled end-time for the same continued session, preserves all earlier elapsed phases plus intervening break and reserved summary, requires >=1m per final pose; explicitly distinguishes continuing stopped session from starting a new session/deadline. This safe interpretation awaits user's phone validation; saved approved presets stay immutable. 55 JVM tests and 8 Android instrumentation tests **WRITTEN** total at this new revision, but new source **NOT YET COMPILED ON PHONE**. Original background/lock-screen notification requirement is still outstanding. See [D-073/D-074](DECISIONS.md), [TEST_PLAN](TEST_PLAN.md).


> **D-071 APPROVED 2026-10-24 — Opening-group after-break rule (new product requirement):** User explicitly confirmed: each ordered opening pose group has editable whole-minute break duration afterwards; **earlier groups may have 0 minutes**, while **after the final opening group a positive rest (>=1m) is mandatory** for newly configured group-break sessions. When BREAK_FIRST regular cycle already supplies a break matching the final group's after-break setting (e.g. Example A's 00:20–00:25 five-minute regular break), it **satisfies the rule without adding another phase**; other explicit last group rests replace the initial BREAK_FIRST rest rather than being doubled. Special break replacement ordinals refer to ordinary recurring breaks only, not custom group boundary breaks; 25/5 safety still applies. Legacy saved sessions without the added field retain exact older approved timelines and signatures. Source change written, phone rebuild pending. See [D-071](DECISIONS.md) and [review](EDITOR_LABEL_REVIEW_2026_10_24.md).


> **D-070 IMPLEMENTATION STATUS (2026-10-23):** Product requirements unchanged. The existing offline preset requirement is now **coded** in native Android Kotlin (approved-only save/update/load/duplicate/rename/delete/select, local versioned persistent JSON, integrity checking); not yet compiled or run on a phone, so **NOT ACCEPTED/VERIFIED**. 32 JVM + 4 Android instrumentation test cases written/unrun. Running timer/three distinct audio cues and lock/background behavior remain future code, not changed requirements. See [native QA plan](ANDROID_NATIVE_EXECUTION_AND_TEST.md).


> **D-068 REQUIREMENTS CONFIRMED — 2026-10-23:** The user approved Android native Kotlin implementation and requested a direct check that all existing settings and requirements are understood, recorded and unchanged. **Confirmed: NO approved product/scheduling requirement changes needed.** Existing D-031/036/037/043/044 and complete exact A/B fixtures, editable session inputs, all 3 ending choices, offline presets, timer and 3 cues stand. D-015 summary description and D-016 cue granularity remain unresolved, not silently decided. **Only the D-062 release priority (Android from phone now, others later) and D-067 framework (Kotlin Compose Gradle) change old Flutter implementation statements.** The real Kotlin source branch/draft PR #3 is underway but unbuilt/unverified. [Baseline and testing pipeline](ANDROID_NATIVE_EXECUTION_AND_TEST.md).


> **LATEST DEVELOPMENT DECISION D-064 (2026-10-23):** The approved Android-on-phone priority (D-062) will use **industry-standard Kotlin + Jetpack Compose + Gradle**, with future Kotlin Multiplatform/Compose Multiplatform portability engineered through a platform-free scheduling/domain layer. CoGo is the on-phone IDE, **not** a claim of industry-standard mobile IDE; Android Studio is official professional IDE. The user explicitly declined pre-development toy APK experiments. Real app build/test becomes the normal engineering loop. No framework code migrated yet, existing Dart scheduling 35/35 and adapter 11/11 test passes remain valid.


> **2026-10-23 — ANDROID-ON-PHONE IMPLEMENTATION DECISION D-064:** App behavior/features unchanged. **Selected Android implementation: Kotlin + Jetpack Compose + Gradle**, developed with phone-local Code on the Go. Architecture should preserve portable pure Kotlin domain for future Kotlin Multiplatform / Compose Multiplatform iOS/Windows/macOS reuse. **Code on the Go is a specialized mobile IDE, not the industry-standard desktop Android Studio.** User explicitly does not want standalone toolchain demos; actual Atelier Time development/build/QA is next. No Kotlin build or APK claimed yet; historical Dart regression evidence remains.


> **2026-10-23 — NEW USER REQUIREMENT D-062 (HIGHEST PRIORITY):** Deliver **Android-first**, with **fast efficient development, tests, browser preview and ultimately installable APK directly on the user's Android phone**. Later efficient migration/reuse on **iOS/iPadOS, Windows and macOS** remains required as a design goal but their native builds need **not** be available locally on Android from day one. This **supersedes D-061's strict all-host full-lifecycle builder constraint**, not the product features. Offline session presets, reliable active/background timer and three alerts remain mandatory; an installed PWA alone is not automatically enough. **Bounded proposed feasibility route**, not yet approved final stack: preserve existing Dart scheduling 35/35 and editor parser 11/11; compile minimal Dart browser proof in Termux; verify actual Android APK via Capacitor/Gradle; test background alerts before committing to full UI migration. See [Android-first plan](ANDROID_FIRST_PLATFORM_SPIKE.md). Historical Flutter draft remains frozen.


> **2026-10-23 — LATEST APPROVED DELIVERY PRIORITY (D-062):** Android FIRST: fast, practical **development/testing/building/installing the Android APK directly on the user's Android phone**. Design the code for efficient LATER adaptation to iOS/iPadOS, Windows and macOS, but those are **no longer initial release blockers**. The product's offline presets, live timer/sounds, and approved scheduling fixtures remain unchanged. [ANDROID_FIRST_DEVELOPMENT_PLAN.md](ANDROID_FIRST_DEVELOPMENT_PLAN.md) proposes Kotlin + CoGo pending an on-device APK proof (D-063). This supersedes the earlier D-061 demand for immediately complete cross-platform build pipelines. **Framework is not yet migrated; original Dart test passes preserved.**


> **2026-10-23 — D-061 PLATFORM FEASIBILITY HOLD:** The application requirements **remain** offline, installable, same maintained product/features on Android/iOS/Windows/macOS, reliable clock and three audio cues. User explicitly clarified that **development, automated tests, builds, signing and deployment must also be workable cross-platform**, including the currently available Android Termux environment. The historical Flutter+Dart approval is **under reconsideration** because it did not prove that complete lifecycle; no framework replacement or relaxation of separate native installers has been authorized. See [reassessment](PLATFORM_REASSESSMENT_2026_10_23.md). Pause additional Flutter-specific UI coding pending feasibility proof. Preserve verified Dart logic, documentation and tests.


Status: **Approved product scope** on 2026-10-22. Stage 1 pure-Dart scheduling core subsequently implemented and screenshot-verified 35/35 tests PASS on Android Termux (D-052), on an unmerged branch. Stage 2 user subsequently authorized initial editor/preview SOURCE work (D-053/D-054), not yet Flutter-compiled/tested. No installable app, persistence or runtime timer/audio exists.

## Purpose and audience
A straightforward installable, self-contained timer for **drawing and sculpting ateliers** where a live model holds timed poses and takes breaks. The application runs a complete predefined sequence with minimal intervention.

## User journey
1. Open an existing saved session or create a new one.
2. Enter editable numeric rules; opening pose groups are optional.
3. Generate a time-accurate list showing every pose, break, final-summary block, duration, elapsed start and end.
4. Resolve any partial final pose **during review** using one of three explicit choices; see SCHEDULING_RULES.md.
5. Approve and save the session with a user-defined name.
6. Activate a saved session, start the timer and see current phase, countdown, next phase, remaining overall duration.
7. Pause, resume or stop the timer; receive three distinct audible alerts.

## Editable session parameters
**Every numerical value appearing in the example sessions is editable per full session.** Every session/preset has its own input screen. The user can add or remove opening pose groups, regenerate the entire schedule when any number changes, review the output and approve it before saving. The examples are regression fixtures, not hardcoded presets or limits.

- Total session duration, **strictly positive**; 0 is invalid.
- Final reserved summary duration, including zero when desired.
- Optional ordered opening pose groups: count and duration per group (e.g. 4 x 1 minute, then 4 x 4 minutes).
- Nominal regular pose duration.
- Short break duration, **initial default 5 minutes**, editable to other **positive** durations (the planned whole-minute editor has a 1-minute minimum). A configured 0-minute break is invalid.
- Long replacement/middle break duration, **initial default 15 minutes**, editable to other **positive** durations, plus the number of optional long replacements. An active 0-minute special break is invalid; zero optional *count* is distinct from duration (subject to remaining Step 1.3 edge-policy approval).
- Long-break placement strategy/position (middle by default in examples, configurable).
- Recurring cycle ordering: pose then break OR break then pose.
- Last-two-pose resolution choice and custom split if used.
- Preset name.
- Sound configuration / cues as supported by approved app design.

## Scheduling principles
- Every second/minute belongs to exactly one phase; no overlaps or unexplained gaps.
- The entire generated sequence ends at the specified session duration.
- The summary is the final reserved period, not an additional period beyond the session.
- Long break **replaces** a scheduled short break; never simply append 15 minutes.
- **Latest approved D-036:** The complete session's total duration and every **configured actual break duration must be positive**. A 0-minute session or break is a **blocking input error**, never a warn-and-override exception. New-session normal/special break defaults remain editable **5m / 15m**; their minimum at whole-minute input precision is **1 minute**.
- **Break safety (approved D-031, retained by D-036):** After **25 minutes of accumulated posing without adequate rest**, a break lasting **at least 5 minutes** is recommended before posing continues. Consecutive poses accumulate; **valid 1–4-minute breaks do not reset** the counter. Legal schedules that violate this *recommendation* remain allowed **only after visible warning, explicit user acknowledgment and full schedule approval**. Editing the session invalidates the prior acknowledgment. See [SCHEDULING_EDGE_POLICY.md](SCHEDULING_EDGE_POLICY.md).
- Short opening poses flow immediately into each other in the illustrated preset; introducing opening breaks would require an explicit rule.
- Avoid a trailing break immediately before final summary when no subsequent pose exists.
- A partial last regular pose is **not silently accepted**: explicitly offer alternatives when detected.
- Show the complete updated timeline before approval. Save the finalized schedule with the preset.
- Recomputed times after a change must always be derived from the same underlying schedule.

## Partial-final-pose resolution — approved
1. **Remainder:** Keep preceding pose at full nominal duration; shorten only the final pose.
2. **Near-equal:** Keep fixed earlier stages and intervening break; split total time available for last two poses approximately 50%/50%, showing both durations and times. For whole-minute odd totals use floor/ceil.
3. **Custom slider/ruler:** User adjusts the division of that same time, with updated durations/start/end times visible immediately; initially one-minute steps, at least one minute per pose when feasible.

When there are fewer than two poses or no intervening break, show that two-pose resolution is unavailable (exact fallback/error UX will be finalized in Stage 1). Preserve fixed length of the break, total session, and final summary in all feasible alternatives.

## Saved-session management
Create, name, approve, save, list, activate, edit, duplicate, rename and delete presets. Editing an approved preset generates a new preview and requires approval. Saved sessions remain usable offline.

## Live timer
- Large current phase countdown and phase identity (pose number/type, short break, long break, summary).
- Elapsed/remaining overall session time and next phase.
- Start, pause, resume, stop.
- Sound A on end of pose (or set, subject to final alert-edge rule); Sound B on end of break; Sound C on end of entire session.
- Alerts must reflect the approved sequence, not an unapproved baseline schedule.
- Reliable elapsed-time basis, including behavior during phone lock/background, subject to OS constraints established later.

## Look and installation
- App name: **Atelier Time**.
- Same maintained **Flutter + Dart application codebase** for Android phones/tablets, Windows computers, iPhone/iPad and macOS in the **first release**.
- Non-native Flutter-rendered menus, controls and graphics **are acceptable**; native widgets are not a requirement. Prioritize cross-platform feature parity and usable responsive layouts.
- Minimal, readable interface; offline capable; self-contained installed app for each OS (separate platform-specific install packages).
- Ask user for logo picture at the branding stage; discuss visual style before committing designs.

## Non-goals not promised yet
No account service, cloud synchronization, multi-device orchestration, payments or analytics unless separately approved.

## Definition of done
Installable working app on the chosen OS, saves/loads presets, generates examples A/B exactly, permits all three end-pose choices, runs reliable timer with three distinct audio events, and passes documented acceptance tests. Document every iteration and leave a current cross-chat handoff.

## Development status (2026-10-23; D-052–D-054)

- **Verified Stage 1 scheduling core:** Android ARM64 Dart 3.13.7 screenshot 35 unit tests PASS; earlier Dart analyzer and smoke check passed. [Core PR #1](https://github.com/AviY99/Atelier-Time/pull/1) is open and unmerged.
- **Stage 2 editor:** User instructed “finish documentation and continue”; [Stage 2 plan](STAGE_2_PLAN.md) and [Flutter editor source branch](https://github.com/AviY99/Atelier-Time/tree/stage-2-session-editor) exist, [draft PR #2](https://github.com/AviY99/Atelier-Time/pull/2). Responsive input and full timeline preview are source-only and **NOT Flutter-compiled or tested**. Temporary Material theme is subject to user feedback; branding/logo comes later.
- Stage 3 offline saved presets, Stage 4 live timer/audio and installation remain **NOT IMPLEMENTED/NOT AUTHORIZED**. No paid GitHub workflows, deployments or budget increase.
