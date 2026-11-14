# D-118 — descriptive timer tabs and Free countdown quick presets

> **Observed phone addendum, 2026-10-27:** Samsung `BUILD SUCCESSFUL in 1m25s`, 72/72 tasks, **211/211 JVM PASS**, regular + AndroidTest APKs assembled. The user separately approved regular update-install and reports the other functions work, but tabs still clip, color legend is missing, Free running mode should hide presets and keep countdown wheels, and Sound/Editor selection tracks are too thick. **D-119** is the new development fix; the old source-only status below is historical. [Current D119 handoff](D119_COMPACT_CONTROLS_AND_WHEELS_2026_10_27.md).

**Date:** 2026-10-27
**Branch:** `android-native-kotlin-compose` (development only)
**App version:** `D-118`, Android `versionCode=13`
**Stage:** source implementation; build and device QA pending.

## User acceptance of D-117

After the D-117 on-phone Gradle build (72/72 tasks, 209/209 JVM PASS, both APKs assembled), the user reported **"הכל עובד כעת"** — all works now. Treat this as user-reported manual functional acceptance of D-117 fixes, **not** D-117 Android instrumentation execution or a declaration of Stable 7. Stable 6 (`stable/android-d114-qa6`) and Stable 5 (`stable/android-d114`) remain frozen.

## D-118 changes

1. Rename the former `Timer` tab to **`Programed Timer`**, and the former `Free` tab to **`Free Timer`**, matching the user's exact spelling. Keep all six tabs simultaneously visible; use explicit two-line text in the existing compact equal-width navigation so long titles are never horizontally truncated. Existing internal tab types and saved Classes are unchanged.
2. In **Free Timer**, add eight selectable quick durations **above Start/Pause/Resume/Exit**:

   | 1m | 4m | 5m | 10m |
   | --- | --- | --- | --- |
   | 15m | 25m | 30m | 1h |

   Tapping a preset sets the three HH:MM:SS wheels only, visibly selecting the chosen duration. **Start remains a separate deliberate action**; choosing a preset never plays audio, resets a live countdown, or edits a saved Class. Presets are visible but disabled when the Free countdown is running or paused. While READY/FINISHED, users can still adjust the wheels manually. A one-hour preset sets exactly `01:00:00`.
3. Android Compose test selectors were updated to the two renamed tabs; the Free test now selects the 1m preset and verifies that the 1h button is disabled during the run.

## Evidence and safeguards

- Actual local Kotlin/JUnit execution: **211/211 PASS**, 22 suites; 209 D-117 regressions plus two quick-preset duration/mapping tests. `FreeCountdownTimer` remains unmodified apart from read-only preset definitions; countdown sounds and deadlines are unchanged.
- Local guarded shell scripts pass `bash -n`, and code passes `git diff --check`. No local Android SDK/Gradle build was executed, so **Compose compilation and real-device visual testing remain pending**.
- Device build-only path after dev branch publication:

  ```bash
  cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native
  git switch android-native-kotlin-compose
  git pull --ff-only
  bash tools/build_d118_candidate_on_phone.sh
  ```

  Output: `Download/Atelier-Time-D118-Candidate.apk`, `Atelier-Time-D118-Tests.apk`, QA manifest and JVM log. **No APK is installed by this script.** Explicit authorization is needed separately for app update and instrumentation; always preserve the existing two saved Classes and Sounds, never uninstall or clear data.

## Focused phone acceptance once approved and installed

- All six complete tab labels visible, including the full `Programed Timer` and `Free Timer` two-line names, in Day and Night.
- Free Timer shows all eight quick presets in two compact rows above action buttons. Tap 25m → wheels read `00:25:00`; tap 1h → `01:00:00`; pressing Start uses the selected value. During Running/Paused, quick preset buttons cannot reset the active countdown. Exit returns to edit mode.
- Programed Class Timer, sounds, and both saved Classes are unchanged. D-117 special Break centering and actual-history chart still work.

Do not label D-118 as stable before the user's real-device acceptance. AndroidTest instrumentation is a separate approval gate.
