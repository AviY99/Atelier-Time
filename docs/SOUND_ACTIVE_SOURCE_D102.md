# D-102 — Disable only sound libraries unused by the active Sound mode

> **D-102 PHONE BUILD PASS / INSTALL & FOCUSED UX QA PENDING (2026-10-24):** User-supplied Code on the Go screenshot confirms `:app:assembleDebug` and `:app:testDebugUnitTest`, **BUILD SUCCESSFUL in 29s**, 42 actionable tasks (8 executed, 34 up-to-date). The same screenshot verifies the resulting **12M** `Atelier-Time-D102.apk` already copied to `/storage/emulated/0/Download/` (Oct 24 20:39). Exact local commit SHA and individual test results/JUnit XML are not shown. **No D-102 update-install or actual active-source checkbox behavior has yet been confirmed.** Do not repeat the build or copy. Next: update-install existing APK without uninstall or clearing stored Classes, then in Same sound for all select an Atelier Time sound and disable Audio files when it is only used in hidden Separate A/B/C (should succeed without error); switch to Separate (needed source auto-enables and prior chosen sound returns); ensure active sound's source still cannot be unchecked. Stable 2 rollback unchanged.

**Date:** 2026-10-24. **Status:** D-102 passed on-phone Gradle unit-test task and APK build, and APK was copied to Downloads; update-install and user QA are still pending.

## Evidence and user report

User installed D-101 and reported **"הכל עובד"**, with one localized logic defect: on the compact Sound tab, deselecting **Audio files** (Custom/file source) displays the error "Change the selected sounds before disabling this source." even when the currently active **Same sound for all** choice is an Atelier Time sound. The hidden Separate A/B/C selection is still associated with Audio files. The guard was too broad: it tested all stored cue selections in both sound modes rather than only those relevant to the currently visible mode.

The user's D-101 screenshot documents all three libraries checked, Same sound for all selected and an Atelier Time sound visibly active, together with the displayed error. All other reported D-101 behavior remains user-accepted.

## Correct behavior

- **Same sound for all:** only the shared sound (or current A sound when the shared choice has never been set) is active. A library can be disabled even when hidden Separate A/B/C choices reference it.
- **Separate A/B/C:** only A/B/C selections are active. A library can be disabled even when a hidden shared choice references it.
- Disabling the library of any *active* selected sound remains blocked. The final enabled library must remain enabled.
- Choices and durations in the hidden mode stay stored with their original source/URI. On switching modes, automatically re-enable any source library needed by that mode's stored choices so they are playable again and their checkboxes accurately reflect the active selection.
- Loading sound preferences must not resurrect libraries needed only by the hidden mode. Saved Class/preset data is never modified.
- Do not change the approved compact Sound layout, timers, class schedule, three source pickers, 5/10s shared durations, or 5–60s Separate C slider.

## Files changed (Android development branch only)

1. `android-native/app/src/main/java/com/ateliertime/domain/SoundSettings.kt`: `activeSounds()`, `activeSourcesEnabled()`, active-only source validation, and source re-enable for the selected mode during `setAssignmentMode`.
2. `android-native/app/src/main/java/com/ateliertime/android/MelodyAudio.kt`: `MelodyPreferences.loadSettings()` restores sources required by the current mode only; `saveSettings()` accepts dormant choices referencing disabled libraries but still validates active choices.
3. `android-native/app/src/test/java/com/ateliertime/domain/SoundSettingsTest.kt`: source-only JUnit regressions for both modes, mode switching and active sound/last-library protection.
4. `android-native/app/src/androidTest/java/com/ateliertime/android/MelodyPreferencesTest.kt`: source-only device persistence regressions for both directions and reopened preferences.

## Phone gate (open)

1. On phone, from existing Code on the Go `android-native` worktree, `git pull --ff-only` and execute the proven local Gradle 9.6.1 `:app:testDebugUnitTest :app:assembleDebug` using Android ARM64 AAPT2 override. Do not use paid GitHub Actions.
2. If PASS, update-install `Atelier-Time-D102.apk` **without uninstall or clearing saved Classes**.
3. **Shared:** choose Atelier Time, enable Audio files, set a Separate B sound to an audio file, return to Shared, deselect Audio files. Expected: **checkbox unchecked without red error**, current shared sound still works. Close/reopen; Audio files stays unchecked; B's saved audio file remains.
4. Switch back to Separate: Audio files auto-enables because the saved B uses it; B continues to play its prior file. Repeat reverse case with a hidden shared OS sound and active Separate Atelier Time A/B/C sounds.
5. Attempt to disable the source of an active sound: the guard must still show an error and leave that source enabled. Attempt to disable the last library: also blocked. Other D-101 behavior remains unchanged.

**Note:** User has accepted D-101 visually and functionally except this issue, but **D-102 is not yet built/device accepted**. Stable rollback branches `stable/android-d098` and `stable/android-d086` remain immutable, Draft PR #3 stays unmerged.
