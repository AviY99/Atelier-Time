# Atelier Time — D-104 / Timer reliability testing, first execution gate

**Date:** 2026-10-24. **Base:** user-accepted Android Stable 3 / D-103, frozen at `stable/android-d102` (`78878801ea515deccc4e64cdc62b2b3db595769a`). **Current state: first pure-Kotlin/JVM regression TEST SOURCE added; NOT EXECUTED here; Android debug APK NOT rebuilt or installed for D-104.** The next Android development APK additionally uses `versionName = "3"`, `versionCode = 2` so About reads `Version 3` on that *next* install.

## What is done now

- Audited the pre-existing `MonotonicSessionTimerTest.kt` suite: deterministic tests already cover Pause/Resume/Stop, Restart Class/Session/Break (including late-pair protection), Next compensation and boundaries, single cues, stale transitions and fixed Class deadline. Therefore **do not rewrite or repeat already accepted design/engine features**.
- Added `android-native/app/src/test/java/com/ateliertime/domain/TimerReliabilityGateTest.kt` with four new deterministic pure Kotlin/JUnit methods:
  1. **3-minute A/B/C automated transition** at 200ms UI polling: cue A precisely at 01:00, B at 02:00, C at 03:00; once per event, no repeats, schedule immutable, never negative remaining.
  2. **3-hour monotonic deadline**: different wall-clock checkpoints leave original Class end unchanged, with completion C once at the expected 180-minute deadline; saved approved timeline stays untouched.
  3. **Delayed UI** crossing multiple phases: do not replay obsolete A/B alerts, but C still emitted once at end.
  4. **Pause to fixed deadline**: explicit Class deadline completion emits C once; no duplicate from subsequent timer updates.
- Changed **only the DEVELOPMENT branch's** `android-native/app/build.gradle.kts`: `versionCode = 2`, `versionName = "3"`, for the **next Android app build** requested by user. The existing frozen Stable 3 APK/source remains unchanged. The existing About screen reads Android package `versionName`; no visual changes needed or made.

## Test execution protocol — first phone run (NOT DONE YET)

From the phone's existing Code on the Go `Atelier-Time/android-native` worktree, on its original development branch, pull the committed source, then run unit tests and build. **Do not change architecture, app signing identity, or delete stored Classes.**

```bash
git pull --ff-only &&
set -o pipefail &&
bash "$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle" :app:testDebugUnitTest :app:assembleDebug --no-daemon --console=plain "-Pandroid.aapt2FromMavenOverride=$ANDROID_HOME/build-tools/36.0.0/aapt2" 2>&1 | tee "$HOME/atelier-d104-build.log"
```

For finer-grained evidence after the build, inspect HTML reports in `app/build/reports/tests/testDebugUnitTest/index.html` or the XML under `app/build/test-results/testDebugUnitTest/`. `BUILD SUCCESSFUL` confirms only the task success, not an independently documented individual test count.

**After PASS, if proceeding with update-install**: copy `app/build/outputs/apk/debug/app-debug.apk` to `/storage/emulated/0/Download/Atelier-Time-D104.apk` (without rebuiliding), and update-install **without uninstalling or clearing app data**. In About verify **Version 3**, in Timer verify saved Classes and settings retained. Version code 2 is an actual Android package update rather than cosmetic About-only text.

### Focused manual reliability checks (next after JVM PASS)

1. **Foreground 3-minute A/B/C:** Create a separate 3m QA class, no opening groups, no special break, Summary 0, Pose 1m, regular Break 1m, cycle Pose first; use **Separate A/B/C** and recognizable different sounds. Observe A at 1m, B at 2m, C at 3m **once each**; check no leftover sound/repeat from an old cue. Do not disturb existing saved Classes. If summary0 configuration is not selectable in the editor, document the blocker instead of silently editing product rules.
2. **Pause/Resume:** Separate test class, pause early, resume while final pair still compensable, ensure original End clock stays fixed; if impossible, safe explicit error and no partial rewrite.
3. **Restart current Session/Break and Next:** Repeat a Session and a Break with adequate time to compensate. Check phases before current phase do not change, Class end unchanged when feasible, one matching cue per successful transition; verify rejection does not change timer state or sound.
4. **Long-duration field observation:** use a real multi-hour class in foreground and record observed start, next transition, last pair and end. Pure JVM accelerated 3h test is **not** equivalent to a real 3-hour device/audio/battery test.
5. **Boundary/device lifecycle:** report any phone clock changes, screen sleep, another app in front, power saver or interruptions distinctly — these are the NEXT background/Doze/foreground-service gate, **NOT passed by the JVM suite**.

### Exit criteria

This D-104 gate closes only when the phone's Gradle/JUnit test report and APK are actually verified and the requested focused manual Timer checks are explicitly user-accepted (with issues recorded). Do not claim connected `:app:connectedDebugAndroidTest` PASS or a signed Play release from this workflow.

## Branding release gate inserted

**Mandatory logo/icon approval and launcher/store asset integration BEFORE store publication:** [ICON_LOGO_PRESTORE_GATE.md](ICON_LOGO_PRESTORE_GATE.md). Current manifest has no launcher icon declaration, so this remains an independent open deliverable, not something completed by D-104 tests.

## Release/architecture boundaries

No modifications to accepted Editor/Timer/Sound/Design/About layouts, A/B/C source libraries, Class schedule engine, the frozen stable branches, signing identity or PR #3. VersionName/versionCode adjustments apply only to a **future** dev APK and do not rewrite Stable 3. No paid GitHub Actions.
