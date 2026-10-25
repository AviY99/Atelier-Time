# Atelier Time — QA-R03 accelerated reliability tests (2026-10-25)

**Source baseline:** frozen Android D-108, `stable/android-d108` at `02beb0af46719301cccb72ad4698a337ab416d73`.
**Environment:** assistant-side Kotlin/JVM, synthetic monotonic and wall clocks. **No on-device execution, no APK build, no real-time 3-hour observation.**
**Reproducible artifact:** `Atelier_Time_QA_R03_Results_and_Repro.zip` made available in this conversation with extracted Kotlin timing-path source and `src/QAR03.kt`, compiler instructions, and stdout. This repository report summarizes the execution; the ZIP itself has not been uploaded into GitHub.

## Scope and results

Compiled the archived QA-R02 harness using `kotlinc src/*.kt -include-runtime -d qa-r03.jar` and ran `java -cp qa-r03.jar com.ateliertime.domain.QAR03Kt`. The harness contains source-equivalent D-108 timer methods `start`, `view`, `update`, `elapsedMs`, `phaseIndex`, and `ClassClockTracker` calculation, checked against the corresponding frozen-source text. It uses the independently hand-specified three-hour Example B fixture. **Other runtime methods (pause/Next/restart/compensation) are stubbed/out of scope and were NOT tested. This is not a complete production source build.**

| ID | Virtual test | Observation | Status |
|---|---|---|---|
| R03-01 | first pose boundary, exact timestamp | one A cue, no duplicate | PASS (simulation) |
| R03-02 | tick delayed +5,999 ms | A cue emitted once (integer-second truncation) | PASS current policy |
| R03-03 | tick delayed +6,000 ms | stale A cue suppressed | PASS current policy |
| R03-04 | 150 seeded virtual 3-hour runs, irregular 50–2,500 ms intervals | 2,483,558 total updates; countdown and scheduled end match independent virtual reference; exactly one final C per run; maximum intermediate cue detection latency 2,446 ms | PASS (simulation) |
| R03-05 | 30-day virtual duration sampled at 7 points | remaining time/deadline exact for selected samples, one completion | PASS (limited simulation; not supported 30-day device test) |
| R03-06 | simulated wall clock shifted +120s at elapsed 1h | timer remaining follows monotonic reference; `ClassClockTracker.classRemainingSeconds` differs by 120s | **FAIL (display discontinuity, simulated)** |
| R03-07 | wall clock shifted -120s at elapsed 1h | symmetric 120s mismatch | **FAIL (display discontinuity, simulated)** |

Actual JVM stdout includes:

```text
first boundary at +0ms: PASS cue=POSE_FINISHED extra_update=none
boundary at +5,999ms: PASS cue=POSE_FINISHED extra_update=none
boundary at +6,000ms: PASS cue=none extra_update=none
wall-clock forward 2 minutes: OBSERVED_ENGINE_PASS_DISPLAY_DIVERGENCE engine_error=0s display_disagreement=120s
wall-clock backward 2 minutes: OBSERVED_ENGINE_PASS_DISPLAY_DIVERGENCE engine_error=0s display_disagreement=120s
30-day sparse virtual clock: PASS 7 samples, exact deadline, one final completion
150 seeded 3-hour irregular scheduler runs: PASS ticks=2483558 max_observed_edge_detection_lag_ms=2446 final_C_once=true
```

## Source-supported issue: Class overview displayed duration vs monotonic timer

In frozen D-108 `ClassClockTracker.observe()`, the remaining and elapsed Class display values are calculated from `nowEpochMs` and stored wall-clock start/deadline. In the same build, `MonotonicSessionTimer` advances using a monotonic `nowMillis()`. `SessionTimerScreen.kt` displays **Class remaining** using `classClockTimes.classRemainingSeconds` and **Class elapsed** using `classClockTimes.classElapsedSeconds`. This establishes a potential user-visible discontinuity after a wall-clock adjustment (manual correction, network-time correction), while the internal timer continues normally. **The real installed phone has NOT been tested with a clock change.** No claim that approved normal operation is broken.

**Disposition:** register as **QA-R03-WALLCLOCK candidate defect / simulated reproduction**, pending targeted change and real-device verification. Suggested future narrow fix (not implemented): calculate elapsed/remaining Class *durations* from monotonic timer state and retain epoch for clock-of-day start/end labels. Any future APK change must use the actual D-ID (next D-110 unless later) and increment `versionCode`.

## Unchanged gates

- **QA-R02b real 3h Android device/audio/Doze:** OPEN, untested.
- **QA-R01 connected Android instrumentation:** BLOCKED; zero-byte/non-executable CoGo ADB file, 14 source test methods not run.
- **QA-R04 Class/Sound saved-data survival across ordinary reboot:** USER-OBSERVED PASS.
- **Android Stable 4:** immutable; no change to Android Kotlin source, Gradle, UI, Sounds, timer actions, Play policy, installed app, user data, or PR #3.
