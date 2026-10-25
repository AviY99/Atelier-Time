# Atelier Time — Run end-time constraint: options and draft contract (D-110+)

## 2026-10-25 — D-114 Stage 1: explain RED wheel blocking (source updated; phone QA pending)

- User confirmed **D-113 runs correctly** except at a blocked red finish boundary there is no explanation for why the wheel stopped. Do not repeat prior feature redesign: D-113 live calculated finish, on-demand wheels and hard red boundary remain accepted.
- `EndClockWheel.kt` now invokes `onBlocked(candidate)` both for blocked swipe and blocked tap on a red neighboring value, while still refusing to move into red; rapid drags check each step and never jump across invalid ranges.
- `SessionEditorScreen.kt` uses `RunEndTimePlanner.plan` to generate the exact rejection reason for the attempted candidate hour/minute, and immediately renders a **red explanatory message** under the wheel (`run_end_blocked_reason`) with attempted HH:mm and corrective instruction. A permitted green move, `Use calculated finish`, or a newly opened Run clears the message. Calculated finish keeps moving while the dialog is open, wheels remain hidden until `Change finish time`, and `Confirm & Run` still validates/blocks impossible endings.
- Gradle version advanced to `versionName="D-114"`, `versionCode=9` on **android-native-kotlin-compose** development branch only. GitHub source readback verifies both blocked callbacks, message display, clearing logic and version. Frozen `stable/android-d108` unaffected; saved Class and Sounds persistence code untouched.
- **Status: source change completed; D-114 on-device build, APK install and user acceptance NOT YET VERIFIED.** User requested approval checkpoint between Stage 1 finish UX and Stage 2 all automated QA; do not run tests until user explicitly approves. Next phone command: git pull --ff-only; use CoGo Gradle `:app:assembleDebug` (no tests), copy APK to Downloads/Atelier-Time-D114.apk, update-install with Termux ADB `adb install -r`. Test red boundary swipe/tap gives visible explanation and other accepted features remain.

## 2026-10-25 — D-113 Stage 1 UX refinement: stopped-at-red wheel and on-demand picker

**User correction to D-112 (user says all else works great):** (1) An hour/minute wheel must hard-stop at the first infeasible RED neighboring value and NOT be allowed to scroll/choose deeper into red; user can reverse toward feasible GREEN. (2) Hide both wheels initially: show them ONLY after explicit Change finish time selection. Calculated finish stays live while deciding. No comprehensive automated QA until Stage 1 approval.

**Code now in development branch, versionCode=8 / versionName=D-113:**
- `EndClockWheel.kt`: independent hour/minute swipe wheels with `canSelect(candidate)` checked for EACH adjacent step, including fast drags (no skipping invalid boundary). Next/previous candidate shown red when blocked, green when feasible; invalid red step is visible but not selected. `RunEndTimePlanner.plan` remains authority; no mutation of saved Classes.
- `SessionEditorScreen.kt`: no wheels on opening `Run` approval. `Change finish time` (testTag `run_end_change_finish`) reveals them. Important follow-up safeguard: revealing the picker does NOT freeze/truncate the continuously calculated finish; custom absolute HH:mm becomes active only on the first accepted wheel movement. `Use calculated finish` resets to live default and hides wheels. `Confirm & Run` still validates before activation; explicit default recomputes at actual start. No direct unapproved Start shortcut.
- `app/build.gradle.kts`: versionCode=8 and versionName=D-113. GitHub source readback checked the wheel gating, canSelect bindings, on-demand visibility and version; **no D-113 CoGo build/APK installation, real-device screen acceptance or Stage 2 QA run has yet been observed.**

**Next Stage 1 on user's Samsung phone:** In Code on the Go, git pull --ff-only, run only `:app:assembleDebug` using established Gradle/AAPT2, copy to Download/Atelier-Time-D113.apk, update-install with `adb install -r` from Termux (preserve saved Classes/Sounds), verify the two UX requirements, then request user's explicit acceptance of Stage 1. D-108 Stable 4 frozen. Existing RunEndApprovalUiTest targets old text-input tags and must be revised only in Stage 2 after approval; previous 14/14 Android device PASS pertained to D-110, not D-113.

## 2026-10-25 — D-113: requested finish-wheel UX refinement (Stage 1, source only)

**User phone confirmation for D-112:** 'הכל עובד מצויין' ('everything works great') except the two specified UI refinements. This is acceptance of existing D-112 behavior other than the new wheel requirement; do not treat D-113 as tested.

**New exact requirements:** (1) Hour and minute wheels should be displayed **ONLY AFTER** the user elects to change/update the live Calculated finish; accepting default finish uses no wheel. (2) At every boundary where a candidate hour/minute would make the Class end infeasible, the **neighboring candidate is displayed RED but may not be scrolled/tapped into**; the wheel hard-stops at the last feasible GREEN selection and allows reversing into green values. The previous behavior that let users select red values (only then disabling Run) is insufficient.

**Implemented on development branch:**
- `SessionEditorScreen.kt`: The confirmation dialog initially displays continuously updated Calculated finish plus a `Change finish time` button (test tag `run_end_change_finish`) but no wheel. Pressing the button copies the live suggested HH:mm into the chosen custom time, then reveals the two wheels; `Use calculated finish` resets/hides them. Each prospective neighboring hour/minute is evaluated by the unchanged `RunEndTimePlanner.plan` against the same Class/session/actual current time and other selected wheel. `Confirm & Run` remains disabled if the overall selected finish becomes infeasible (including if wall time advances) and continues to revalidate at actual start.
- `EndClockWheel.kt`: New `canSelect(Int): Boolean` gate checks EVERY adjacent step in a swipe, including fast multi-step swipes, rather than jumping over red candidates. The first invalid candidate stops traversal, is shown RED as an adjacent number, cannot be selected even by tapping, and user can swipe/tap back into feasible GREEN candidates. The selected number displays green when valid, red if a previously selected fixed time becomes invalid as the clock advances.
- `app/build.gradle.kts`: bumped `versionCode=8`, `versionName="D-113"`. Frozen Stable 4 D-108 untouched. User data/saved Classes/Sounds unchanged by source edits; no Android functional/domain/notification/sound logic modified.

**Verification:** GitHub source update only; **D-113 has NOT been built on phone, installed, or verified by user.** User should pull development branch in existing CoGo checkout, run `:app:assembleDebug` ONLY, copy APK as `Atelier-Time-D113.apk`, update-install via Termux `adb install -r`, and inspect default/no wheels, changed/wheels, red neighbor hard-stop via swipe and tap, backwards into green, and continuously updating Calculated finish. DO NOT start Stage 2 exhaustive automated tests without explicit approval after Stage 1 device acceptance. Previously authored Compose instrumentation test still references the OLD typed HH:mm UI; it must be replaced during Stage 2 preparation.

## 2026-10-25 — D-112 phone APK build PASSED; install and UX acceptance pending

- User screenshot from Code on the Go confirms `app:assembleDebug` **BUILD SUCCESSFUL in 13s**, 36 actionable tasks (12 executed, 24 up-to-date).
- New `/storage/emulated/0/Download/Atelier-Time-D112.apk` exists, **12M**, screenshot timestamp Oct 25 20:53.
- Non-blocking Kotlin warning at `SessionEditorScreen.kt:676:29` about redundant Elvis operator; build and packaging succeed.
- Stage 1 now **source implemented + build PASS**. **Update-install is not yet verified**, and actual UI live clock + green/red independent hour/minute wheels are not yet phone verified.
- Next: from Termux `adb install -r ~/storage/downloads/Atelier-Time-D112.apk`, after closing any live Class; expect `Success`. Verify About D-112; saved Classes/Sounds remain; live Calculated finish increases while dialog open; both wheels change and feasible/infeasible selection changes green/red with Confirm disabled for red; reset calculated finish and cancel. Ask for user approval before stage 2 comprehensive tests.
- D-108 Stable 4 frozen; no Stage 2 automated tests requested or run for D-112.

## 2026-10-25 — D-112: live calculated finish and swipe hour/minute picker (STAGE 1 ONLY)

**User's clarified requirement:** In the pre-Run approval dialog, `Calculated finish` must keep advancing with the wall clock until the user confirms, rather than stay frozen at the original Run tap. Replace awkward typed HH:mm plus override switch with visible independently scrollable hour and minute wheels. A feasible selection shows GREEN; an infeasible one shows RED and blocks `Confirm & Run` until corrected back to a feasible (green) time. Keep mandatory explicit approval and compensation only through the last two regular Sessions.

**Implemented in development branch:**
- `SessionEditorScreen.kt`: `PendingRunApproval` no longer snapshots a finish time. The dialog recomputes the calculated finish from `wallClockMillis` (root updates every 200ms), shows seconds (`dd/MM HH:mm:ss`), and uses the updated actual-clock finish when confirming the calculated default. Once either wheel is moved, the chosen clock (HH:mm) remains fixed, while the calculated value still advances independently. The selected finish date is resolved by `RunEndTimePlanner.nextOccurrence` and previewed. `RunEndTimePlanner.plan` is evaluated live for green/red coloring and error messages; Confirm & Run has `enabled = isValid` and revalidates against `System.currentTimeMillis()` immediately before any preset activation or timer start. 'Use calculated finish' resets wheels/default.
- Added `android-native/app/src/main/java/com/ateliertime/android/EndClockWheel.kt`: two simple independently vertically swipeable numeric wheels (hours 00–23, minutes 00–59), with adjacent tappable previous/next values, stable pointer gesture and accessibility test tags. Parent Surface tint changes green/red according to feasibility.
- `app/build.gradle.kts` bumped monotonically to `versionCode=7`, `versionName=D-112` so installed D-111 (or D-110) is clearly distinguished from updated source. All changes confined to development branch. Frozen Stable 4 D-108 untouched; no user Classes/Sounds data altered by GitHub edits.
- Existing backend `FinalPairCompensation`, `RunEndTimePlanner` 1-minute minimums, schedule invariants, monotonic physical deadline and no auto-resume on app closure/reboot remain unchanged. Unfeasible wheel values remain selectable only for correction; RUN is always disabled while red.

**Verification gate:** GitHub write/readback confirms source and version; **no CoGo compiler/device build, install, or UI verification has yet run for D-112**. The previous D-111 assembleDebug PASS was for the *old* typed-time dialog. User must pull, run `:app:assembleDebug` only, copy APK to Downloads, update-install, verify live calculated finish/scroll hours/scroll minutes/green-red block/reset, then approve Stage 1. Do NOT start Stage 2 automated tests before explicit approval.

**Stage 2 pending on approval:** Existing `RunEndApprovalUiTest.kt` targets old text input test tags `run_end_override` / `run_end_clock` and MUST be updated to the wheel tags (`run_end_hour`, `run_end_minute`, `run_end_confirm`, `run_end_invalid`, `run_end_reset_calculated`, `run_end_calculated`) before instrumented tests run. Add cases for live tick, green/red transition, invalid disabled, reset and user-confirmed deadline preservation. Then run all feasible JVM, device and ADB suites, preserving isolated data and D-108 Stable 4.

## 2026-10-25 — Stage 1 D-111 Run end-time feature: source checkpoint; approval gate

**User-requested ordering:** Stage 1 implement/repair end-time feature, obtain user approval; Stage 2 only AFTER that approval run every feasible automated test.

- Preexisting development branch implementation already calculates a suggested absolute Class finish before Run, shows a confirm-or-edit dialog, accepts earlier/equal/later fixed end time (not optional/unlimited), previews adjustments restricted to the final two regular Sessions, blocks infeasible HH:mm times with a corrective error, and reevaluates at actual start. Saved presets never change. The selected finish uses the monotonic physical deadline, and original pause/Next/restart routines compensate in the last eligible Session(s). Single-Session legacy Classes can use only their unaltered default duration.
- **Actual gap corrected now:** removed the old direct Start action and button from SessionEditorScreen.kt and SessionTimerScreen.kt. It could bypass finish-time confirmation while the Timer was READY. The READY UI now displays 'Starting approved Class...' only; new runs must pass saved-library Run -> Confirm Class finish time.
- The existing Gradle file is already versionCode=6 and versionName=D-111. GitHub source readback verified no dangling onStart parameter/callback and Run approval dialog remains present.
- **Stage 1: SOURCE READY; not compiled, installed, or accepted on the phone.** Installed user-approved D-110 remains unchanged; frozen Stable 4 D-108 remains untouched. Do not claim a new APK or a test PASS.
- **Stage 1 next on phone:** git pull --ff-only in the existing CoGo checkout, assemble the debug APK only (:app:assembleDebug), copy it to Downloads and update-install without wiping existing saved Classes or Sounds. User validates the before-Run dialog and approves Stage 1. No tests before user confirms next stage.
- Already-authored but not yet executed for D-111: RunEndTimePlannerTest, RunEndTimePropertyTest (JVM), RunEndTimeDeviceTest and RunEndApprovalUiTest (device), plus all existing unit and instrumentation suites. The earlier D-110 device result 14/14 PASS is historical baseline and must not be misrepresented as D-111 verification.

**Status: HISTORICAL DRAFT BELOW; D-111 source is implemented as described above; phone build/acceptance and Stage 2 tests pending.**
**User request: 2026-10-25.** Implement the feature first, then prepare and run the complete feasible automated QA suite. Preserve frozen Stable 4 `stable/android-d108` and use development branch only. Do not add production code or trigger tests prior to choosing the behavioral contract.

## User-requested behavior

When pressing Run on a saved Class, **before** beginning any live countdown/sounds, calculate planned finish based on *the actual current local time* and the stored Class duration including summary. Ask for **No limit** or a specified **latest finish clock time**. If No limit, preserve old behavior exactly. If end-time limit is earlier, shorten only eligible last two regular work Sessions (or only the last when the first is already completed), preserving break lengths, earlier phases, terminal summary, one-minute minimums and the approved saved preset; if impossible, show a specific notice and block starting. If end-time limit is later, start at planned lengths, and if later pauses/restarts make the upper cap relevant, try compensating with remaining eligible final Session(s).

No persisted change to saved Class definition and no automatic resume after actual app closure/reboot.

## Relevant audited code at D-110

- `SessionEditorScreen.kt`: Saved-Class Run currently immediately sets `playingSession` and `launchRunRequested`, with `LaunchedEffect(liveTimer, launchRunRequested)` calling `clock.start()` and `classClockTracker.newClass`; no Run preflight UI. The button entry point must be intercepted **before** assigning a playing session / starting timer or posting notifications/playing cues.
- `ClassClockTracker.kt`: `newClass` sets an **original** epoch and monotonic deadline; `hasReachedClassDeadline` compares elapsedRealtime and the UI loop calls `finishAtDeadline`. Thus an upper cap **later than original finish requires explicit runtime policy and deadline integration**; merely showing a time-picker would not implement requested behavior.
- `MonotonicSessionTimer.kt`: `pause`/`resume`, `restartCurrentSegmentKeepingEndTime`, `nextPhase` and `finishAtDeadline` already implement original-deadline handling. Rewrite must maintain monotonic physical deadline logic and avoid rewriting already-reached phases.
- `FinalPairCompensation.kt`: `adjust()` redistributes signed correction across the last two regular poses using saved ending mode REMAINDER/EVEN/CUSTOM; last one has at least **60 seconds each** and summary/intervening break lengths preserved. `adjustLastOnly()` specifically allows changing last final pose only after first has been completed.
- `SessionTimerScreen.kt`: shows Class start/end/length, time recovered and adjusted final work Session lengths. New mode should clearly separate *forecast*, *target* and *latest limit*, and labels must remain consistent through pause, Next, restart and clock correction.

## Product alternatives for consideration

**A. Strict hard cutoff (not recommended as the sole mechanism).** Never end after limit; automatically terminate whichever phase is live at the deadline. Guarantees clock bound but can cut a work Session or summary and is incompatible with the product restriction to adjust *only* the two final Sessions in the event of arbitrarily long interruptions. If offered, make this an explicit optional fallback with clear consent.

**B. Soft deadline/target.** Try to fit last one/two work Sessions into requested clock, but allow overruns with a warning, without forcibly ending a Class mid-phase. Non-destructive but does not guarantee a strict latest finish.

**C. Hybrid upper cap / preserve original target (recommended draft).** Keep the originally calculated finish as the preferred end; selected limit is an upper bound, not a forced extension. If limit is earlier, preflight-adjust only final eligible work Sessions and treat that as preferred and latest deadline. If limit is later, preserve the original planned target and normal compensation behavior while feasible; if original end can no longer be kept, spend the time buffer up to chosen limit, but adjust the final eligible one/two work Sessions whenever necessary to respect it. If bounded fitting becomes impossible while running, never silently rewrite already-started phases or delete intervening break/summary; present a blocking decision as soon as detected: **(1) Explicitly stop at specified time (early terminate active phase) or (2) Continue and allow an overrun**. Need product decision on fallback; auto-guaranteeing cutoff is impossible without relaxing immutability/phase-minimum conditions.

**D. Flexible reserve/elastic buffer.** For later-than-planned limit, let pauses consume time buffer BEFORE reducing final work Sessions; can be kinder to user but is a deliberate departure from current preserve-original-deadline semantics. Same infeasible-in-flight choice required.

## Suggested UI

- Run tap → **Run setup** dialog, show: `Now 17:00`, `Class length 03:00`, `Planned finish 20:00` (illustrative only).
- Mutually exclusive: **No end-time limit** (default) / **Finish no later than** time-picker (HH:mm, explicit next-day when necessary).
- Under selected earlier limit: show **preview** of adjusted final pose lengths before confirming; validate using domain preflight engine **before** starting any clock/cues.
- Under later limit: show `Preferred finish 20:00 · Latest 20:30 · Buffer 30m`; not an instruction to extend a Class to 20:30.
- On impossible preflight: explicit reason and minimum feasible finish; **Run disabled**. Offer editing the time or using No limit, **never quietly rewrite stored Class or cut Summary/Breaks**.
- During a run show `Latest finish` separately from calculated actual finish or original target. If user deliberately skips a phase, do not *force* delayed ending; maintain approved Next behavior and avoid surprising extension.
- All run limit selections apply to **that run only**, not saved Classes; default No limit each new Run.

## Invariants and QA acceptance

1. No limit = exact D-110 status quo (same timer deadline, pauses, Next, restart, notifications, cue semantics).
2. Earlier limit, feasible: before Run, no side effects; preview matches runtime final pair; original phases excluding eligible last pair unchanged, including summary and all breaks; minimum regular work Session >= 60 s.
3. Earlier limit, infeasible: precise error and no timer start/cue/notification/process change; preserve saved preset.
4. Later limit: natural run keeps original times; normal pause/restart semantics unchanged where possible; only spending slack when original finish truly not feasible, by explicit policy.
5. At start of final pair, freeze elapsed/completed phases. When first of final two is completed, only last unfinished Session can shrink; after last Session has begun, never silently modify it.
6. Accumulated pauses >available compensation: deterministic and visible handling according to fallback contract; **never** crash, silently distort old phases, claim cap guaranteed if not.
7. Wall-clock label uses real local time and handles midnight/datetime and DST; physical duration and deadline computed against monotonic Android `elapsedRealtime` so system clock changes cannot skip/restart cues or abruptly change total time.
8. Test pause/resume, restart session, restart break between final Sessions, Next, skipped phases, offline/phone lock, app termination/relaunch, summary, notification, sound cues, min 1 minute, feasible/infeasible boundary, overflow/midnight, clock jump, time picker and before-run gating.
9. Only after feature product contract is approved and implemented, run *all feasible automated QA*, including existing 14 androidTest tests plus new deterministic JVM and UI/instrumentation tests, and safe Termux ADB observation probes. Device-side destructive scenarios need explicit isolation/notification; do not clear real saved Classes/Sounds.

## Open product decisions

- Choose **C (recommended)** or D (buffer used before work-time compression) for a later cap.
- If the cap becomes impossible during Run, choose **explicit early stop vs allow overrun**, or authorize a hard automatic stop of the current phase (non-preferred).
- Whether a user-adjusted cap can be changed while paused/running; simpler first version keeps it fixed for the run, with a visible infeasibility warning/choice.

**No code changes / no APK build / no automated tests triggered by this draft.**
