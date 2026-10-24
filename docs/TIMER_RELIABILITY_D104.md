# Atelier Time — D-104 / Timer reliability testing, first execution gate

> **D-106 — CURRENT VERSION-DISPLAY POLICY / FOREGROUND TIMER TEST ACCEPTANCE (2026-10-24):** User replaces earlier About `Version 3` request: **About must show the real development build identifier, e.g. `Version D-104` for this D-104 APK generation; every future build must use its corresponding D-number**. On native DEVELOPMENT branch only, Android Gradle `versionName = "D-104"` (numeric package `versionCode = 2` stays independent), and `AtelierAboutPanel()` now shows installed `PackageManager.versionName` verbatim instead of extracting digits. **This change was SOURCE COMMITTED AFTER the screenshot-confirmed D-104 34s build; the already built/copied D-104 APK still has `versionName = "3"` and is NOT retroactively updated. No new build/install has occurred.** User also confirms the audible A/B/C cues and normal time/phase transitions were already checked and work. **Do not require another redundant 3-minute foreground A/B/C check** for the unchanged Timer engine. This is prior user-reported manual acceptance, NOT new D-104 APK install confirmation or device lock/background/Doze reliability. Next testing priority: locked screen/background/Doze and related notification/audio survival, plus separate persistence and instrumentation checks; saved Classes unchanged. Prior `Version 3` requirement in historical docs is SUPERSEDED; frozen Stable 3 `stable/android-d102` untouched, no PR merge or paid CI. Code and decision: `android-native/app/build.gradle.kts` + `android-native/app/src/main/java/com/ateliertime/android/AtelierDesign.kt`.

> **D-104 ON-PHONE GRADLE PASS — 2026-10-24, user screenshot:** Existing Code on the Go `android-native` worktree ran `:app:assembleDebug` and `:app:testDebugUnitTest` to **BUILD SUCCESSFUL in 34s**; 42 actionable tasks (9 executed, 33 up-to-date). Terminal `ls -lh` shows `/storage/emulated/0/Download/Atelier-Time-D104.apk` successfully copied (~12M; Oct 24 21:25). This confirms the JVM unit-test task and debug APK assembly succeeded; individual test case counts, exact local Git HEAD SHA and Android connected instrumentation were **not** shown. **D-104 APK update-install, About `Version 3` on device, saved Class persistence and real 3-minute A/B/C foreground cue QA are STILL PENDING.** Next: install the existing Downloads APK as an update **without uninstall/data clearing**; verify About Version 3, saved Classes/sound settings and 01:00 A / 02:00 B / 03:00 C cues once. Do not rebuild or copy again. Stable 3 `stable/android-d102` stays frozen; mandatory branding/icon gate remains before store publication. Historical 'source only/not yet built' notes below are superseded by this build evidence.

**Date:** 2026-10-24. **Base:** user-accepted Android Stable 3 / D-103, frozen at `stable/android-d102` (`78878801ea515deccc4e64cdc62b2b3db595769a`). **Current status: screenshot-verified on-phone JVM test task and debug build PASS, APK copied to Downloads; APK install and manual Timer/About QA not yet confirmed.** The next Android development APK additionally uses `versionName = "3"`, `versionCode = 2` so About reads `Version 3` on that *next* install.

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
