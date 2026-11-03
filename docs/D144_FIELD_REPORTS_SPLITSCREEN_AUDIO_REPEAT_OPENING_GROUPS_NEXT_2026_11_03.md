# Atelier Time — user field reports and proposed D-144 scope

**Date:** 2026-11-03. **Status: triage/solution proposal ONLY — not implemented, no D-144 build, no install, no user approval of a complete design yet.** The current user-accepted frozen recovery is **Stable13 / D-143 / code37** at `stable/android-d143-qa13-user-accepted` (`2a40f2d9ae79fa6eee41dcb387840d22b5caccf6`). Never write to this branch. Future source changes only on `android-native-kotlin-compose`, with a newly authorized D-number/code38.

## Six reports from real users (user's wording preserved in substance)

1. **Split screen:** Enter two-app split view with Atelier Time, then exit split screen: the active countdown stops.
2. **First cue inaudible:** First use after another music player (**Poweramp**) has been used and closed sometimes has no audible cue, cause not yet established. Suggest a brief, original 2–3-note sound test at app splash or first Class start to make volume audible.
3. **Alarm sound too quiet:** Alarm stream was about 25% while general/media was at max. When Class sounds use Alarm, physical volume keys may adjust Media instead. User asks for a Timer Run control to set both Media and Alarm to maximum.
4. **Repeat Last:** After accidental Next or if a Session/Break just completed, no way to replay preceding phase. User requests a `Repeat Last` control and the existing end-time compensation, as with Restart.
5. **Opening session numbering:** Each opening group should have one outer group number and display sub-index `1/4, 2/4, 3/4, 4/4` rather than all opening phases showing a flat continuous pose number; subsequent regular numbering needs a consistent display scheme.
6. **Upcoming duration:** On Timer's existing `Next:` line, include actual length of the next Session/Break adjacent to its label, including after runtime adjustments.

## Read-only code triage against frozen D-143 (not yet proven on affected hardware)

### #1 — Split-screen is high priority (potentially interrupts a real Class)

- `MainActivity.kt` sets content anew in `onCreate` and contains lifecycle cleanup on actual finish; source manifest does not explicitly handle window size/config changes.
- `SessionEditorScreen.kt` stores `playingSession`, `liveTimer`, `timerView`, `classClockTracker`, `eventHistory`, and per-run diagnostics in Compose `remember`, while `DisposableEffect` closes the sound players and clears active status upon disposal. Android split-screen/return can recreate the Activity on configuration changes, losing `remember` state and inadvertently ending live timer/audio/notification. This is a **code-supported suspected failure mechanism, not a device-proven root cause**.
- Proposed durable fix: separate live Class session/timer/phase clock/audio/report from short-lived Composables into an Activity-scoped `ViewModel` or explicit session coordinator that survives configuration recreation, with correct notification ownership and lifecycle. Restoring after actual process death is a distinct policy/technical concern; do not promise guaranteed background survival. A guarded `configChanges` workaround may lessen churn but **alone is not sufficient** to guarantee all recreation cases.
- Tests: split enter/exit, resize, orientation, home/return, lock/unlock, background, Android navigation Back vs Recents swipe, running/paused, FGS notification cleanup, user Classes/Sounds unchanged; no changes to approved `specialUse` architecture without separate review.

### #2 — No cue after Poweramp (audio focus / output test needed)

- `MelodyAudio.kt` uses `AudioFocusRequest(AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)` and calls `requestAudioFocus`; on `AUDIOFOCUS_REQUEST_FAILED` it reports an error **but still proceeds to construct and play** AudioTrack or MediaPlayer. Its audio-focus-change listener ignores updates, and it doesn't retry a delayed grant.
- For apps targeting Android 15+ (we target API 36), Android can deny focus if app is not the top app and is not running an FGS; other active apps/focus owners can also interfere. Poweramp correlation is **not proof of causation**. `SessionEditorScreen.kt` reports playback error to `melodyError` and Run diagnostics; running Timer currently renders `timerError ?: diagnosticError`, so cue failure may not be prominent enough.
- Proposed: instrument focus result, volume stream, mute/DND state and audio route safely in private Run diagnostics without recording other apps' identity or user audio. Explicitly handle failed focus (no blind play), recheck focus on first cue, user-visible failed-cue notice, and a **user-triggered brief original 2–3-note 'Test sound'** before confirming the first Class. Optional non-intrusive brief test immediately after Run only after consent; **do not automatically play on splash**. The jingle is diagnostic and cannot substitute for fixing focus/volume.

### #3 — Alarm/media volume

- The settings have a separate toggle `classAlertsUseAlarmVolume` (default Media) that sends Class cues as `AudioAttributes.USAGE_ALARM` when selected; the current Timer only warns if the chosen stream is **zero**, not if it is low. Consequently Media max does not imply Alarm max.
- Proposed Timer Run UI: a compact **Class volume** control near `Keep screen awake`, showing **Media** and **Alarm** percentages and clearly highlighting which stream the current Class sounds use. Provide a button to launch/expand native Android volume controls or an explicit Sound test.
- User requested `Max both` (Media+Alarm) with one touch. It is technically possible to request stream changes via AudioManager on supported devices but affects **system-wide volumes for other apps**; Android's own docs discourage programmatic stream volume adjustments and DND/fixed-volume restrictions may block them. Recommend **no automatic forced max** on app launch or Run. If implemented after specific approval: explicit confirmation/warning, `MODIFY_AUDIO_SETTINGS` reviewed, capture original levels, optionally restore on Exit/normal finish *only if not manually changed meanwhile*, respect DND and device limits, never bypass safety cap; show real state if unsuccessful. First preference: active-stream volume display/test + Android system volume panel.

### #4 — Repeat Last is a NEW scheduling feature, not simple `Next` reversal

- `MonotonicSessionTimer.kt` has `restartCurrentSegmentKeepingEndTime()` for the **current** phase, and `nextPhase()` to skip forward. It intentionally freezes observed/completed phases and invokes `FinalPairCompensation`; it has no supported rewind.
- Proposed semantics: **Repeat Last** means add a fresh attempt of the immediately preceding completed/just-skipped Session or Break with its original duration; distinguish from Undo Next (which would restore unused remaining time). On click show exact previous phase title/length and confirm that the currently active phase is being interrupted. Add new **runtime-only** phase attempt, never mutate the historical approved Class, preserving physical finish; compensate available future regular Sessions via existing planning constraints. If cannot preserve mandatory breaks, 1-minute minimum, immutable completed phases, 25/5 safety policy or the approved finish clock, reject without changing the timeline and show clear reason.
- Decision needed: repeat prior phase in full rather than only undone remainder, treatment of current phase after repeat (restart current phase from beginning or resume remainder), and behavior near Summary/final two Sessions. Recommend full preceding phase attempt, then return to the interrupted phase with remaining duration **if feasible**, otherwise refuse; no rewind of physical elapsed time. Tests must cover Next mistake, ordinary completion, consecutive repeats, break/session types, manual special breaks, first/last phase, summary, pause and diagnostics/audio cue ordering.

### #5 — Opening grouping is presentation-only unless user requests schedule logic changes

- `ScheduleEngine.build` already stores `poseSource=OPENING`, `openingGroupId` and ordered `openingGroups`; `poseNumber` is currently a flat unique ordinal in timeline/diagnostics. `SessionTimerScreen.phaseLabel` displays that flat number.
- Propose a **display-only** label resolver derived from the immutable Class's opening-group membership / phase index: e.g. `Opening 1 · 1/4`, `Opening 1 · 2/4`, `Opening 1 · 3/4`, `Opening 1 · 4/4`; next opening group `Opening 2 · 1/N`. Regular Sessions keep their established count or a clearly agreed separate sequence. Apply consistent labels to current, next, timeline, and Run history/notification where appropriate; do not change `poseNumber`, persisted signatures, timing, audio cues or phase IDs.
- Tests: multiple groups, single Session group, legacy opening groups, in-session split/reflow, dynamic Next/Restart, narrow screens and accessibility.

### #6 — Next duration is a small UI improvement

- `SessionTimerScreen.kt` currently renders `Text("Next: " + phaseLabel(view.nextPhase))`; `TimerView.nextPhase` is populated from the real **active runtime phase list**.
- Proposed text `Next: Break · regular (05:00)` or `Next: Session 8 (25:00)`, using **`view.nextPhase.durationSeconds`**, not the original preset duration. If no next phase, display `Next: Class end`. Summary may use `Next: Class summary (15:00)`.
- Tests: normal, summary, final compensation, changes after Next/Restart/Repeat Last, no next phase and large font.

## Recommended risk-first sequencing

1. **P0:** split-screen survival / lifecycle plus audio focus/first-cue reliability; device QA with Poweramp and background/lock flows.
2. **P1:** user-controlled volume setup and explicit test cue; then Repeat Last with its own pure domain test plan before connecting UI. Do not publish an unsafe/untested rewind.
3. **P2:** Opening labels / Next phase length (display only).
4. Run **full local Samsung JVM suite + compile AndroidTest**, then **explicitly approved** in-place install and real device regression. Freeze a new Stable checkpoint only after user approval. No GitHub Actions, paid cloud, premature Google Play AAB upload, or user data clearing.

## Sources checked

- Android split-screen/Activity configuration changes: https://developer.android.com/develop/ui/views/layout/support-multi-window-mode
- Android lifecycle and State: https://developer.android.com/guide/topics/resources/runtime-changes
- Audio focus / Android 15+ top app or FGS limitation: https://developer.android.com/media/optimize/audio-focus
- Volume-control stream and risk of global stream changes: https://developer.android.com/media/platform/output
- Android AudioManager setStreamVolume limitations: https://developer.android.com/reference/android/media/AudioManager

## Status and authority

The user provided six reports, and the engineering triage proposed above is **not approval to implement the specific volume automation, first-run jingle or runtime rewind semantics**. Get approval for D-144 scope before modifying the Android DEV production source. Existing Stable13/D-143, original artwork, saved classes and Google Play preparation are unaffected.
