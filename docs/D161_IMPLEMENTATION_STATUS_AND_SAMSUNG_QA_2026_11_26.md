# D161 — Implementation status and Samsung QA gate (2026-11-26)

**STATUS: SOURCE IMPLEMENTED ON A SEPARATE DEVELOPMENT BRANCH; LOCAL SAMSUNG BUILD / REAL JVM EXECUTION / INSTRUMENTATION RUNTIME / USER QA NOT YET VERIFIED. NEVER CLAIM STABLE20 OR PLAY RELEASE.**

Development branch: `dev/d161-preparation-pause-recovery-2026-11-24`.
Original and accepted frozen recovery point: Stable19 / D160 / Code54, `stable/android-d160-qa19-finish-dialog-ui-user-accepted`, commit `55e82d51788062368970cde7e747edc5b7e72bb1`. Original user-installed Play Internal Testing Code54 is untouched. New DEV build metadata on development branch only: **D-161 / versionCode55**, debug application ID `com.ateliertime.android.dev`.

## User-approved requested behavior coded

1. **5-second preparation between consecutive opening-group Sessions**, allocated in the **Editor** during generation. Distinct `PhaseKind.PREPARATION`, five seconds between adjacent Sessions within each group (N-1 gaps), shown in the Editor chronological timeline, graphic timeline, runtime Class timer, phase labels and notification text. Group/session durations stay fixed. The overall Class seconds and physical approved finish are unchanged; insufficient capacity blocks the Editor/Run rather than changing end time. Compatible `SessionRules.preparationSeconds` field, default 0 for *legacy already approved* saved Classes, 5 for newly edited/created plans in the Editor. A newly saved plan persists its timing option and uses the approved signature to detect tampering. Do not silently rewrite existing saved Classes.

2. **Pause warning after more than 60 seconds**, independently for Class and Free. `PauseWarningTracker` records an episode per timer; a red in-app modal identifies affected timer(s); `PauseWarningBeeper` uses repeated short tones **until the user dismisses the dialog**, not a single sound or finite-duration cue. Close silences alarm and keeps Pause active; further alerts are suppressed for the same episode, reset on later pause. Selected Media/Alarm stream and global sound setting respected; Android does not guarantee a modal in front of a background app. No new permissions.

3. **Unexpected termination / process crash recovery**: `ClassRunCheckpointStore` stores a separate private/atomic active Class snapshot, not in saved Class library. Captures effective schedule, original start + approved end, pause state, compensation, already-applied runtime Next/Restart/Repeat, and observed fragments. Writes at start, user actions and approximately every 5 seconds. User chooses **Run** on the same saved Class *before the original deadline*; only then sees **Continue / Start again / Cancel**. Continue validates matching saved plan, version and wall/monotonic clock sanity (including reboot), restores against current time without replaying missed cue bursts, updates Class clocks, notifications and diagnostics. Explicit Exit and natural/physical completion retire checkpoint. If invalid/expired, no Continue and existing saved Classes remain untouched. The separate Free Timer is not silently recreated after process death.

## Changed code modules (on D161 branch)

- `domain/Models.kt`, `ScheduleEngine.kt`, `PhaseDisplayNames.kt`, `Presets.kt`, `MonotonicSessionTimer.kt`, `ClassClockTracker.kt`, `TimerNotificationStatus.kt`, `DesignMode.kt`, `PauseWarningTracker.kt`.
- `android/EditorInput.kt`, `AndroidPresetStore.kt`, `SessionEditorScreen.kt`, `SessionTimerScreen.kt`, `TimelinePhaseChart.kt`, `PauseWarningBeeper.kt`, `ClassRunCheckpointStore.kt`.
- `app/build.gradle.kts` DEV D-161/Code55 only.
- Pure JVM test suites `D161PreparationInEditorTimelineTest.kt` (5), `D161PauseWarningTrackerTest.kt` (2), `D161ClassCrashRecoverySnapshotTest.kt` (4), `D161PreparationPresetCompatibilityTest.kt` (2), `D161ClassCrashRecoveryTest.kt` (4) plus preexisting 326 tests. The guarded QA runner requires **343+ tests** with zero failures/skips, plus AndroidTest compile and splash checksum validation.

## Mandatory QA before considering a new stable version

Guarded local-only Samsung runner: `android-native/tools/build_d161_three_upgrades_on_phone.sh` on the D161 branch. Requires exact pinned app Git tree; fails closed if source differs. Runs all JVM tests, builds **DEV** debug APK, compiles AndroidTest (does not execute instrumentation), verifies approved splash SHA in source and APK, exports `Download/Atelier-Time-D161-Code55-DEV-QA.apk`. It does not install, sign a Play AAB, upload or publish. Device command should only follow branch/source checkout verified clean.

Critical runtime scenarios for USER acceptance: Editor preview 5sec exact and hard Class finish unchanged; several opening groups; 25/5 warnings; legacy saved Class open and Edit; Run with custom finish; pause >60sec red dialog and *indefinitely repeating* beep until Close, Close leaves clock paused; simultaneous Class + Free; sound ON/OFF; process killed mid Group/Prep/Break/Next/Restart/final pair/Paused; Continue before end and no Continue at/after deadline; Exit retires recovery; original Play installation and data preserved.

**No device QA, production rollout, Play track upload, ZIP artwork replacement or official release approval is implied by this document.** The Store ZIP GitHub binary upload remains a separate pending operation and requires direct verification before marking it complete.
