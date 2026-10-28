# D-119 — complete tab titles, compact timeline legend, live Free wheels, slim selectors

**Date:** 2026-10-27
**Branch:** `android-native-kotlin-compose` only
**Android version:** `D-119`, `versionCode=14`
**Original gate:** local source/tests → publish approved dev branch → Samsung build-only → separately approved regular update → manual acceptance. **Gate now complete:** after successful corrected D-119 phone build, the user reported "הכל עובד" and explicitly designated it **Stable 7**. [Accepted checkpoint](STABLE7_D119_QA_ACCEPTANCE_2026_10_27.md).

## Phone feedback on installed D-118

The user says **everything else works**, but the navigation titles are clipped again. The colored Class timeline lacks a compact explanatory key. Free countdown shows preset buttons while running and replaces its wheels with a large text clock. A subsequent clarification specifies the Sound and Editor selection sliders should be **thinner**, not shorter or narrower.

D-118's actual Samsung build screenshot: `BUILD SUCCESSFUL in 1m25s`, 72/72 executed Gradle tasks, `D-118 JVM ACTUAL tests=211 failures=0 errors=0 skipped=0`, regular/debugTest APKs assembled. Following separate authorization the user update-installed D-118 and manually reported the functions other than the four issues above working. **No D-118 instrumentation PASS is claimed.**

## D-119 changes (combined)

1. Keep all six tabs visible and accessible in the same order, preserving exact names `Programed Timer` and `Free Timer`. Use better width shares for the two longer tabs, an automatically sized two-line label (8–11sp), explicit 14sp line height and 56dp minimum header height. No ellipsis. Test actual phone layout in both Day and Night; code alone does not prove real-device fit.
2. Directly below the Class timeline, show a single tightly spaced swatch/label row explaining Session (violet), regular Break (teal), special Break (orange), Summary (gray), plus gold Elapsed on the live chart and charcoal Pause when observed. The key never wraps; on unusually large accessibility fonts it can scroll horizontally rather than create a second line. Preserve separate 11dp elapsed / 33dp actual-history-and-revised-future bars, with no change to schedule logic or Class finish.
3. Free Timer keeps HH:MM:SS **wheel-shaped clock** in READY, RUNNING, PAUSED and FINISHED. While running/paused, the three wheels display the live remaining hours/minutes/seconds, their gestures and selection controls are read-only, and the entire quick-preset menu disappears. It returns after Exit or finish. During READY/FINISHED, manual wheels and the eight presets remain editable and preset clicks never auto-Start. Timer and Sound behavior unchanged.
4. Replace **the two Sound 5–60s sliders and Editor custom final-pair slider** with one consistent slim Material3 slider. **Post-build-failure correction:** use the proven value-based `Slider` overload (also used in Design) and apply a 0.55 vertical *rendering* scale; leave the full original hit/accessibility bounds intact. Value ranges, discrete steps, callback timing, horizontal width and preferences remain unchanged. Design tab's Day/Night slider remains as accepted. The abandoned experimental state-based implementation specifying an exact 5dp track and custom thumb **did not compile on the phone** and is not part of the corrected source.

## Observed Samsung D-119 compilation failure and repair — 2026-10-27

The user executed the original D-119 build-only script in Code on the Go. **Actual result: `:app:compileDebugKotlin` FAILED in 52s, 54/54 Gradle tasks executed; no successful D-119 APK.** A second user screenshot showed the exact Kotlin diagnostic lines from `Atelier-Time-D119-JVM-build.log`, all in `AtelierSlimSlider.kt`: `Cannot infer type for type parameter 'T'` around Compose `key` (line 34); `No parameter with name 'trackRange' found` (line 36); `Cannot infer type for value parameter 'changed'` (line 51); invalid `@Composable` invocation contexts in the custom thumb/track lambdas (lines 60 and 67), plus multiple experimental Material3 API diagnostics. These are errors in the initial D-119 slim-slider component, **not evidence of a broken schedule/timer engine**.

Corrective Android-code change is confined to `AtelierSlimSlider.kt`: replace experimental slider-state/custom-track composition with the already working **value-based Material3 Slider** and a rendering-only vertical scale. **Post-correction local regression run: 211/211 JUnit PASS**, after compiling Kotlin domain and EditorInput sources; `git diff --check` PASS. This does **not** compile Android Compose. Keep `D-119`, `versionCode=14`, and the existing build-only script so the user can retry after the corrected development branch is published. The on-device **post-fix compile and APK assembly remain unverified** until a fresh Samsung build screenshot. The installed D-118 app, two saved Classes, Sound preferences and frozen Stable 5/6 remain untouched; never install/clear data as part of this compile repair.

## Coverage / no-touch rules

- **Local JVM regression evidence:** Kotlin 2.2.0 compiler (Java 17), JUnit 4 full domain + EditorInput suite **211/211 PASS**, zero failures, exit code 0, in 22 test files. This verifies scheduling, physical Class finish, special-Break rebalancing, Next, sound routing, Free countdown and saved Classes, but **does not compile Android Compose**. Android Compose instrumentation *source* now asserts presets disappear on run/pause, read-only clock wheels remain, and the Class timeline exposes its compact legend; **Android instrumentation not executed for D-119**.
- On-phone `tools/build_d119_candidate_on_phone.sh` runs actual Gradle `testDebugUnitTest`, `assembleDebug`, `assembleDebugAndroidTest` and validates XML counts/hashes. It creates `Download/Atelier-Time-D119-Candidate.apk` / `Atelier-Time-D119-Tests.apk`, log and manifest. **Never installs either APK.** `tools/termux_run_d119_qa.sh` is a separate approval-only instrumentation runner, not part of the build gate.
- Preserve the user's two saved Classes, Sounds preferences, Free sound, Android notifications, approved physical Class finish, existing Stable 5/6 frozen branches, and all previously accepted scheduling rules. No app uninstall or data clearing; no device operation without the appropriate separate authorization.

### Correct Code on the Go directory and build-only commands

```bash
cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native
git switch android-native-kotlin-compose
git pull --ff-only
bash tools/build_d119_candidate_on_phone.sh
```

Only run after the development branch has been published with the user's permission. After successful screenshot proof, request **separate permission** before updating with `Atelier-Time-D119-Candidate.apk` through Samsung My Files → Internal storage → Download → Update. Do not install `Atelier-Time-D119-Tests.apk` as the app. Check preservation of both saved Classes and Sounds after update. No new Stable unless user explicitly designates it.
