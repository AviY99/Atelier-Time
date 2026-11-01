# D-142 — terminal break remainder: three user-approved resolution choices

**Date: 2026-11-01. Status: implementation committed to Android DEV; compilation, JVM tests, physical-device UI QA and user acceptance PENDING.** This is NOT yet a stable version, not a Play release, and not a declaration that D-142 tests passed.

## User approval / binding design

After detailed review of a terminal-break error on the `testGoogle` Class, the user expressly approved the three-option solution:
1. **Return to Editor**, with a concrete verified recommendation of a setting to increase for the exact remainder.
2. **Extend the last regular Session** by the exact missing remainder, with the existing 25-minute accumulated-pose / 5-minute rest **warning and explicit acknowledgement**, not an absolute 25-minute ban.
3. **Extend the final Summary** by the exact remainder while maintaining the originally requested **total Class duration**.

No silent correction, no breaking/suppressing mandatory manually requested special breaks, and no touching frozen Stable12/D-141. The old version remains the last user-accepted and verified Stable release.

## Domain finding

The rejected break occurs when the schedule cursor has positive leftover `R` but `R < nextBreakDuration + 60s`. For minute-granular initial rules, the actual remainder can range from **1 through B minutes** where B is the attempted break duration (e.g., 1–5 minutes for a normal 5-minute break, 1–15 for a requested special 15-minute break). Source code remains at `ScheduleEngine.kt`; the full root-cause and caution matrix are in [the approved proposal](TAIL_BREAK_REMAINDER_RESOLUTION_ANALYSIS_2026_11_01.md).

## Implemented in Android development branch ONLY

- `android-native/app/build.gradle.kts`: `versionName = "D-142"`, `versionCode = 36` (Stable12 still D-141/code35).
- `domain/Models.kt`: typed `TailBreakRemainder` error context, and a default-0 explicit `SessionRules.tailPoseExtensionSeconds`; legacy rule instances unchanged.
- `domain/ScheduleEngine.kt`: exposes actual remaining seconds, attempted break length/ordinal/special status, including when the automatic planner's virtual timeline identifies the same terminal remainder. **Only** an exact previously selected last-Session extension may consume the gap; never silently omit a manually protected special or a future required replacement ordinal. A different or stale extension fails validation. Existing nominal regular Session length, original final pair controls and Summary clock remain otherwise unchanged.
- `android/SessionEditorScreen.kt`: displays a **Choose how to resolve** button only for diagnosed terminal-break errors. Modal has three selectable actions, a suggested opening-group rest adjustment **only after dry-run validation**, and fully regenerated preview for last Session / Summary choices, including 25/5 warning. A separate Apply action writes proposed changes to editor draft, invalidates old approval and warning acknowledgement and requires a fresh **Save class**; selecting Editor alone changes nothing. Previously chosen tail extension is visible with a **Remove** control.
- `android/EditorInput.kt`: converts new Editor tail override to a checked whole-minute value.
- `android/AndroidPresetStore.kt`: serializes optional `tailPoseExtensionSeconds` only when positive and reads missing field as zero, maintaining v1 schema for existing presets.
- `domain/Presets.kt`: adds the nonzero extension to the **new revision signature** while preserving **exactly the old signature field sequence for existing records**. Existing saved classes are not rewritten.
- Added local JVM regression suite: `app/src/test/java/com/ateliertime/domain/TailBreakRecoveryD142Test.kt`. Covers R=1–5 on regular break, R=1–15 on attempted special, exact screenshot, Summary reallocation, 26+ minute last Session/rest warning, manual special protection, wrong/stale extension rejection, 5+1 minute legal boundary, 5→6 opening rest and save/reopen signature invariants. **Tests created, NOT executed.**

## Recheck on Samsung Code on the Go (separate next QA gate)

No GitHub Actions, paid cloud builds, force pushes or device data wiping. When user authorizes local build and is ready:
1. Safely update the local phone checkout of `android-native-kotlin-compose` (no force/reset), and confirm `D-142`/code36 in Gradle.
2. Run existing local approved **artwork audit** and **JVM unit tests**, including `TailBreakRecoveryD142Test`; build debug APK locally only. Save a diagnostic build log in the established repository-accessible workflow. An exception or build failure is not a pass.
3. Test the user's exact `testGoogle` input: 180m total / 15m Summary / four 1m opening poses +5m rest / regular 25m +5m / one 15m special manually replacing ordinal3 / Session first. Confirm modal says **R=1m**, suggested opening rest **5→6m**, final Session +1 preview (26m) with rest warning, Summary **15→16m** with 180m total unchanged, and explicit Save/acknowledgement.
4. Test R=2,3,4,5 and special break 1–15; never lose user-selected specials or exceed Class duration. Change an input after choosing an override to confirm its earlier approval/override invalidates. Save / edit / duplicate / run and confirm timeline and selected sounds/class data survive in-place update.
5. Regression: Example A/B, normal final `Even`/`Custom`, automatic special placement, break-first shared initial break, start/Next/restart/background notifications/cues and end-clock reflow. Validate UI on real Samsung. Preserve existing saved classes and personal sounds.
6. Only after actual tests pass **and user accepts** can D-142 be designated a new stable checkpoint. Never modify `stable/android-d141-qa12-final-user-accepted` and never imply D-142 has been submitted to Google Play.

## Scope and remaining external release gates

All Google Play graphics, Privacy Policy/installed About link, Data Safety source audit and FGS `specialUse` declaration drafts remain as previously documented. Google identity verification was last known pending; no AAB, signing, upload, app Play draft or publishing action occurred as part of this development change.

**Reference handoff:** [Current Stable12 → Play handoff, now noting the D-142 pending QA candidate](HANDOFF_CURRENT_STABLE12_TO_GOOGLE_PLAY_2026_10_31.md). Do not accidentally treat D-142 as user-accepted Stable12.
