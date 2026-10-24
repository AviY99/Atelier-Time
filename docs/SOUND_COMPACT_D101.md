# D-101 — Compact Sound screen and shared/separate sound choice

> **D-101 PHONE BUILD GATE PASSED (2026-10-24, user terminal screenshot):** On-phone Gradle reached `:app:assembleDebug` and `:app:testDebugUnitTest`, ending **BUILD SUCCESSFUL in 42s** with **42 actionable tasks: 8 executed, 34 up-to-date**. The same screenshot shows an existing **12M** `/storage/emulated/0/Download/Atelier-Time-D101.apk` dated **Oct 24 20:15**. The screenshot confirms Kotlin/Gradle build and JVM unit-test task, debug APK creation and successful copy to Downloads; **it does not show the individual JUnit method count or local Git commit SHA**. **D-101 update-install, real-phone compact Sound layout, shared 5/10s playback, Separate A/B/C visibility, preference-preservation and audible runtime acceptance are NOT YET USER-VERIFIED.** Next: install the existing Downloads APK **as update only, without uninstalling or deleting saved Classes**, check the two modes and retained sounds/durations. Do **not** rebuild or recopy the already prepared APK. D-100 remains last accepted installed build; Stable 2 D-098 snapshot frozen. PR #3 stays Draft/unmerged; no paid CI.

**Date:** 2026-10-24. **Status:** Android phone Gradle/JVM test task + debug APK assembly PASS; APK copied to Downloads. Update-install and visual/audio acceptance PENDING. User confirmed preceding **D-100 installed and working** ("הכל עובד כנדרש") before requesting these targeted Sound UX changes.

## Approved request

Only the existing **Sound** tab may change. The user asks for a denser layout, selection and playback buttons arranged horizontally, more compact multi-source checkboxes, and a new two-mode choice:

- **Same sound for all** — a single sound selection from any enabled library (Atelier Time, Android OS or audio file). All timer cues A/B/C use that one choice and a **single 5-second or 10-second duration**, also for Class end. The independent A/B/C editor sections and the Class 5–60s slider are **hidden** in this mode.
- **Separate A/B/C** — display the independent sections only after this mode is chosen. Preserve A Session end and B Break end sound/duration controls (each 5 or 10 seconds), and C Class end sound with a 5–60 second one-second-step slider.
- **Libraries** — show the three multi-select source boxes in one compact horizontal strip, with accessible source names. Keep a nonempty set and existing source restrictions.
- **Compact layout** — both mode buttons in one row; sound-choice dropdown and Play preview in a single row; in separate mode A/B event headings and the 5/10-second chips share a row, followed by the sound/Play row; C label and live duration above compact horizontal slider. Sound tab margins and inter-component spacing reduced only inside Sound.
- **Persistence and compatibility:** existing D-100 installations load in `SEPARATE` mode by default to avoid silently changing choices. The independent A/B/C sounds and duration values persist even in shared mode. The shared sound and shared duration persist separately, including an OS sound/file URI, and switching modes restores the previous choices without overwriting them. A shared sound initially falls back to current A if no shared choice has ever been saved.
- Audio source chooser and file document-picker controls, PCM/MediaPlayer, timers, event cues, approved saved Classes and all other accepted screens remain unchanged. This is a targeted user-authorized amendment to the Stable 2 design freeze, **not** a general redesign.

## Code changes

1. `android-native/app/src/main/java/com/ateliertime/domain/SoundSettings.kt`: `SoundAssignmentMode` enum; shared sound and shared 5/10s duration; `soundFor` and `secondsFor` use shared values for all cues only when enabled. `chooseForVisibleMode` updates shared selection without overwriting A/B/C.
2. `android-native/app/src/main/java/com/ateliertime/android/MelodyAudio.kt`: private SharedPreferences preserves new `soundAssignmentMode`, `sharedSource`, `sharedUri`, `sharedTitle`, `sharedMelody`, and `sharedSeconds` keys alongside the original A/B/C settings.
3. `android-native/app/src/main/java/com/ateliertime/android/MelodySettingsSection.kt`: compact layout, switchable SHARED vs SEPARATE display, shared 5/10s duration controls, three-column source checkbox strip, horizontally aligned choose/Play buttons, original separate lengths.
4. `android-native/app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt`: system/document picker choices now update current visible sound mode; Sound tab vertical spacing shortened; only Sound wording compressed.
5. `android-native/app/src/test/java/com/ateliertime/domain/SoundSettingsTest.kt`: JVM tests written for shared duration 5/10 on all events, mode switching retaining old A/B/C sounds and C duration, invalid shared durations, source independence.
6. `android-native/app/src/androidTest/java/com/ateliertime/android/MelodyPreferencesTest.kt`: device-test source written for migration and persistence of shared mode/selection/duration.
  
**Evidence:** Source committed and verified by GitHub readback plus simple balanced-delimiter inspection; **NO Gradle compilation/JUnit execution, device installation or UX acceptance yet for D-101**. Do not claim a new PASS, exact individual test count or signed release.

## Next focused phone acceptance

1. From the existing Code on the Go `android-native` worktree, pull the new Android dev branch, run proven Gradle 9.6.1 `:app:testDebugUnitTest :app:assembleDebug` with native ARM64 AAPT2 override.
2. Upon PASS, copy the new debug APK as `Atelier-Time-D101.apk` into Downloads and update-install **without uninstalling or clearing saved Classes**.
3. In Sound, confirm all three library checkboxes fit compactly, mode buttons in one row, preview alongside dropdown. No clipped interactive labels on the real phone.
4. Activate **Same sound for all**: only one chooser and 5/10s controls visible; no separate A/B/C sections or 5–60s slider; preview and real A/B/C events use the selected shared sound and 5s/10s length. Check switching shared length from 5 to 10.
5. Activate **Separate A/B/C**: the three independent sections expand; Class 5–60s slider works and previews, including live label; previously saved independent sound choices and durations return unchanged.
6. Change individual choices, then shared choice (including system/file picker), switch back and forth; verify retained choices after app restart and update, and verify previously saved Classes unaffected.
7. Keep separate later checks for Android instrumentation execution, background/Doze cue reliability and release signing; visual acceptance is not those test results.

## Project rules

`stable/android-d098` and earlier `stable/android-d086` remain immutable. No changes to domain scheduling, approved Class timings, A/B/C cue semantics, other screen design, production signing, old merges, or paid GitHub Actions. GitHub PR #3 remains open/draft/unmerged unless explicitly authorized.

