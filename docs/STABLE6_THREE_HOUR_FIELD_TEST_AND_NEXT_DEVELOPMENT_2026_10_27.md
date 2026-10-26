# Stable 6 — real 3-hour Class field observations and next development scope
**Date:** 2026-10-27 · **Evidence type:** direct user report after an actual long Class, NOT automated test logs.
**Status:** requirements reported; follow-up design/engineering and new APK require staged approval. **No app-source change has been made by this document.**

## Unchanged stable baselines
- Current accepted stable branch: `stable/android-d114-qa6`, frozen commit `89ed2873ef3c91fe224f3723f9523d7409e75f7c`.
- Stable 5 `stable/android-d114` frozen commit `34a692a98282944a76e314efcc228dac3bd072ff`.
- D-114 on phone; 185/185 JVM and 37/37 instrumentation previously PASS, two real saved Classes and Sounds preserved. **Do not invalidate or retroactively restate those results.**
- Development branch only: `android-native-kotlin-compose`.

## Direct field observations

1. During a real Class lasting about three hours, **the Class clock was correct (user observed PASS)**.
2. On **two locked-screen occasions audible cues were not heard**, even though previously other locked-screen cues worked. For **one** missing-cue episode the user's phone volume was muted; causation for the other episode is UNKNOWN. Record as **intermittent cue delivery/audibility failure**, not proven timer failure or confirmed OS root cause.
3. The Class was paused multiple times and had **at least two restarts**. At some point a scheduled Break appeared to be missing. User cannot establish whether an inadvertent **Next** / **Restart** tap occurred or a scheduling/runtime issue; preserve **UNRESOLVED** rather than assert defect.
4. Multiple pauses/restarts reduced the remaining lengths of the final two regular Sessions. User requests a new short-pair merge policy.
5. Additional explicitly requested UX: Class end melody before optional summary, separate general-purpose countdown wheel, separate free-timer sound, and live Timer timeline-at-a-glance visualization with remaining-progress line directly above.

## Source audit performed on current D-114 code

- `MelodyAudio.kt` uses `AudioAttributes.USAGE_MEDIA` for both built-in AudioTrack and MediaPlayer system/file cues; volume routing/media level can affect audible playback. Actual cause of the second missed cue is unconfirmed. Current audio player lives in SessionEditorScreen Compose root; foreground notification service is not the owner of cue dispatch. Inspect foreground/lifecycle/Doze/audio focus before proposing a lifecycle change; adhere to OS and Play foreground-service rules.
- `SessionTimerScreen.kt` has Pause/Resume, Restart (confirmation), **Next directly calls handler with no confirmation** and Exit (confirmation). `MonotonicSessionTimer.nextPhase()` advances to next phase and may skip an intervening Break when tapped, and currently explicitly **finishes the Class when Next is pressed on the last regular Session, even when a summary phase exists**. These are source facts; they do not prove which action the user took.
- `FinalPairCompensation.adjust()` enforces two >=1 minute final regular Sessions separated by a Break, always preserves the intervening Break, and splits according to Remainder/Even/Custom. `adjustLastOnly()` protects the already-observed first final Session. New merge rule will require careful domain/runtime changes; current code does NOT implement it.
- `MonotonicSessionTimer.update()` emits ordinary pose-finished cue A on automatic final regular Session -> Summary transition, and Class-end cue C at final completion. `nextPhase()` emits C when manually advancing from last regular Session; on this path the summary is skipped. The requested behavior is for **C before the Summary begins**, without duplicate C at the end.
- `TimelinePhaseChart.kt` already renders Editor `Class timeline at a glance` from approved phases. Active runtime `MonotonicSessionTimer` owns `activePhases` privately; moving that chart into Timer must use immutable **actual runtime phase snapshots** after pause/restart/compensation, not the original saved schedule, and show a separate Class remaining line immediately above it.

## Requested next product changes (proposed acceptance tests, implementation NOT STARTED)

### A. Reliability, sound, screen behavior
- Diagnose missed locked-screen audio via instrumentation/audible device tests, volume level, sound enabled flag, media stream, DND/ringer configuration, Bluetooth output, Activity lifecycle, Doze and notification/service relationship. Do not automatically raise volume or defeat explicit user mute/DND.
- Offer pre-Class sound test and optional user-facing low/muted audio warning. Assess whether user-selectable alarm-type audio is appropriate; alert routing is subject to OS/user policies. Android `USAGE_MEDIA` is confirmed current.
- Offer an opt-in **Keep screen awake** switch during active Class and separate free countdown when UI is visible, via Android `FLAG_KEEP_SCREEN_ON`, clearing when not needed. Explicitly warn about battery/screen heat; cannot prevent manual power-button lock or enforce awake screen when another app is in foreground.

### B. Explain phase navigation
- Add ephemeral, privacy-preserving run-local **action/event trace** with wall-clock and monotonic time, current phase, next phase, source (automatic/manual), Pause/Resume/Restart/Next, and actual cue dispatch decision; no saved Classes/Sounds edits and no permanent tracking without approval.
- Guard accidental Next selection with clear next-phase target and optional short confirmation for irreversible skip; keep user-approved meanings of Next and Restart otherwise unchanged.
- Test all phase sequences after multiple pause/restart/next actions, including with summary and last-pair threshold, without forcing a repeat three-hour Class for every assertion.

### C. Merge rule for compressed final pair
- User rule: Let R be **regular Session duration** from Editor, and A+B the **current adjusted remaining final two regular Sessions** (before the pair starts). If A+B <= R, show **one final regular Session of duration A+B**, rather than a split; if A+B > R, retain the normal Editor Remainder/Even/Custom split.
- The intervening Break cannot simply disappear while promising both **merged length exactly A+B** and **unchanged absolute Class finish**. Its time must be handled by an explicit product decision. Do not silently add that break time to the merged Session or modify previously completed phases. Define behavior while either final Session / intervening Break has already begun (avoid retroactive rewrites). Do not mutate saved Class timelines.
- Proposed tests: <, =, > R; one-minute bounds, no summary, with summary, interrupted pause/restart/Next, and no duplicate/suppressed final cue.

### D. Summary and C melody
- When a Summary exists, C cue should sound **immediately before entering the Summary**, and the Summary itself must remain scheduled/run. No duplicate C cue on summary completion. When there is no Summary, C sounds at actual Class completion. Explicit Exit remains silent.
- Fix the `nextPhase()` last-regular-Session path that currently completes without showing the Summary, subject to new accepted semantics and correct final deadlines. Test automatic and manual transitions separately.

### E. Independent Free Countdown
- Permanent visible section in Timer tab, independent of saved Classes: hours/minutes/seconds wheels, standard countdown (start, pause, resume, exit), audible cue on reaching zero, validation against zero duration. No final-pair compensation or planned finish-time constraint.
- Add dedicated Free Timer sound selection in Sound tab using existing built-in/system/file selection logic, with independent persisted choice, preview and appropriate sound duration. Keep existing Class A/B/C and Shared/Separate settings unchanged, including backward compatible preference schema.
- Decide whether free timer and active Class can run simultaneously, and whether free timer must survive app background/lock/relaunch; do not invent persistence policy. Test monotonic timing, pause/resume, zero, reset, completion once and sound suppression on Exit.

### F. Live Timer timeline
- Render `Class timeline at a glance` in Timer, sharing chart palette/legend/layout with Editor; reflect live runtime-adjusted phases, highlighting current phase and remaining fractions with no mutation of the saved preset.
- Place a moving **Class remaining** progress line directly above the chart. Verify Pause, Resume, Restart, Next, compressed final sessions, summary and zero time.

## Delivery gates / next action
1. Document product corner-case decisions (especially break time on merging and free timer concurrency). User approves the scope.
2. Only then implement domain changes on `android-native-kotlin-compose` with injected-clock JVM regressions, followed by UI/audio instrumentation suites.
3. User approves on-phone build/test and separate installed-APK update; preserve two saved Classes and existing Sounds.
4. Inspect actual results, get user acceptance, **then** discuss a future stable checkpoint. Stable 6 remains frozen.

## Sources
- Android keep screen on: https://developer.android.com/develop/background-work/background-tasks/awake/screen-on
- Audio usage semantics: https://developer.android.com/reference/android/media/AudioAttributes
- Android 15 focus restrictions: https://developer.android.com/about/versions/15/behavior-changes-15
- Samsung sound troubleshooting: https://www.samsung.com/us/support/troubleshoot/TSG10013264/
