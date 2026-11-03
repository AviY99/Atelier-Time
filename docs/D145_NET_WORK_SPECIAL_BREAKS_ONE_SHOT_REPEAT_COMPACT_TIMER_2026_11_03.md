> **SOURCE REVIEW ADDENDUM 2026-11-03:** Full development JVM-test source scan identified a legacy `MonotonicSessionTimerTest.restartSpecialBreakDoesNotChangeBreakKindOrApprovedPlan` case that expected the OLD, now disallowed special-break restart. The test was updated on DEV to `restartEnteredSpecialBreakIsRejectedAtomicallyInD145`, asserting no schedule/timer mutation; unrelated ClassClockTracker formatting-only tests were left intact. This source review is **NOT** an executed test run. The D-145 local runner's `version_code=39` manifest and new-JUnit-XML fallback checks were verified and corrected before requesting the phone build. 

# D-145 / versionCode 39 — Work-weighted Specials, one-shot Repeat Last, compact controls

**Date:** 2026-11-03. **Status: SOURCE COMMITTED, NOT YET COMPILED / TESTED / INSTALLED / USER ACCEPTED.** 
**Development branch:** `android-native-kotlin-compose`. **Latest frozen accepted:** Stable13/D143/code37, branch `stable/android-d143-qa13-user-accepted` at `2a40f2d9ae79fa6eee41dcb387840d22b5caccf6` (not modified). D144/code38 local QA previously passed 265/265 and was installed; the user reported two field problems. Do not designate Stable14 until explicit device acceptance.

## User-approved changes (one integrated candidate)

1. **Runtime Special Break placement after successful Next / Restart / Repeat Last:** compute cumulative **actual work already performed** (only physically observed POSE fragments) plus **planned future work** from the newly-entered/repeated phase through Class end, excluding skipped virtual work, actual/planned breaks, pauses and Summary. For a total of N automatic Special Breaks, targets are ordered `i/(N+1)` of total cumulative NET WORK for i=1..N, not physical minutes. The `SpecialBreakRebalancer.rebalanceByWork` performs a **global order-preserving dynamic-programming assignment** of all still-unentered Specials to distinct eligible future ordinary Break slots, minimizing aggregate target error; keep the already-entered phase and everything preceding it unchanged. Only future regular Breaks can convert to Special; other future specials demote to regular preserving total breaks, durations, Class end/Summary, and contiguous schedule. Manual special placement and opening-group break slots are protected; no per-tick recalculation. A later Special keeps its **original ordinal fraction** even if earlier Specials were consumed.
2. **Consumed/entered Special Break cannot recur:** during an already entered Special, neither `Restart` nor `Repeat Last` is available; from the immediately following phase, previous Special cannot be repeated. A successful Next into Special protects it at once. It is not re-inserted after its occurrence or altered in the observed prefix. Existing 25/5 warnings/explicit acknowledgement, fixed finish and atomic reject behavior are preserved.
3. **One-shot Repeat Last:** one successful replay locks Repeat Last while that repeated Session/ordinary Break is active. Its full natural completion re-enables it; successful **Next during replay** also unlocks it, with the departed repeated phase now eligible as previous. **Restart during replay retains lock.** Any rejected operation does not change the lock or schedule. Restart otherwise repeats the current regular Session/Break in full. No special replay/restart.
4. **Five Timer controls on ONE row:** Pause/Resume, Restart, **Repeat\nLast** (two visual lines), Next, Exit, compact dimensions with one-row horizontal scrolling only for very narrow/accessibility-large layouts, minimum 48dp height. Consolidated Help control, unchanged confirmation dialogs and accessible full "Repeat Last" label.

## Implementation source

- `android-native/app/src/main/java/com/ateliertime/domain/SpecialBreakRebalancer.kt`: new `rebalanceByWork` global ordered selection, keeps existing physical midpoint routine for legacy initial-finish path.
- `android-native/app/src/main/java/com/ateliertime/domain/MonotonicSessionTimer.kt`: physical work metric; recenter future only following the 3 approved runtime commands; special no-replay; one-shot lock management.
- `android-native/app/src/main/java/com/ateliertime/android/SessionTimerScreen.kt`: 5 controls in one compact horizontal Row; Special restart unavailable; Help and TalkBack semantics.
- `android-native/app/build.gradle.kts`: versionCode **39**, versionName **D-145**.
- New JVM tests `D145NetWorkSpecialBreakTest.kt` **8 source cases**, `D145RepeatOnceSpecialSafetyTest.kt` **4 source cases**. Existing suites D144 Repeat/25:5 5, D144 numbering 4, D143 reset 2, D142 tail recovery 9.
- New **local-only** Samsung Code on the Go QA helper `android-native/tools/build_d145_net_work_specials_and_controls_on_phone.sh`.

## Mandatory QA gate (NO paid GitHub Actions, NO cloud build)

In the existing clean Samsung Code on the Go DEV checkout, execute from terminal:

```bash
cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native && git pull --ff-only && bash tools/build_d145_net_work_specials_and_controls_on_phone.sh
```

The script verifies branch, local source cleanliness and assets, exact version, **fresh FULL JVM suite**, **new 8 + 4 executed JUnit cases** and all older target suites, **0 failures/errors/skips**, compiles the debug + AndroidTest APK (instrumentation **NOT executed**), then writes `/storage/emulated/0/Download/Atelier-Time-D145-API36-Candidate.apk`, private build log and manifest including the SHA256. It **does NOT install, delete data, sign, push, or upload to Play**. If it fails, inspect the Samsung screenshot / phone log and correct source on DEV only before rebuilding.

### Subsequent field checks after separately authorized in-place install

- Recreate the actual `run 1.jsonl` action pattern (input stays private, never push raw report), verify net-work nearest legal Special placements after Next/Repeat/Restart and correct timeline/actual finished boundary.
- 0, 1, 2, 3+ specials; partial consumption; manual placement; no eligible slot; no duplicate after started Special; no selected opening-group rest; full Summary and exact finish; no rewriting work already performed.
- Repeat once → grey button → natural completion reopens; Repeat once → Next reopens with correct previous regular phase; Restart during repeat does **not** reopen; special cannot be Restarted/Repeated; rejected Repeat/Next/Restart leave the running timer unchanged.
- One-line buttons in portrait, accessibility font, RTL/Day/Night; Repeat Last two-line label and Help; previous D144 split-screen, audio after Poweramp, Media/Alarm volume, next-phase duration, opening group numbering.
- Saved Classes, Sounds, diagnostic report history and approved artwork must remain unchanged after an in-place upgrade. No uninstall or app-data clear.

**Do not claim D145 passed 277 JVM tests or BUILD SUCCESSFUL until actual Samsung test evidence arrives. Do not freeze Stable14 or publish to Play without the user's acceptance.**
