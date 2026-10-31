# D-141 — user field feedback, pre-Play Android development candidate (2026-10-31)

**FINAL STATUS (2026-10-31): USER ACCEPTED — STABLE 12 FINAL / D-141, code35.** Samsung JVM suite and debug build PASS; user subsequently corrected the milestone to Stable 12 (D-141). Exact on-phone installation steps or individual test cases were not supplied. D-140 remains an unchanged historical Stable 11 recovery checkpoint. [Official Stable 12 certificate](STABLE12_D141_FINAL_USER_ACCEPTANCE_2026_10_31.md).
Android developer branch: `android-native-kotlin-compose`; `versionName D-141`, `versionCode 35`, target/compile API 36. Stable 11 / D-140 remains frozen and user-accepted at `stable/android-d140-qa11-user-accepted` (`b58634e`). Source-only GitHub updates did not modify device user data. User subsequently reports D-141 works on device; no separate post-install screenshot is held.

## Final user acceptance — 2026-10-31

The user explicitly corrected the working D-141 designation to **Stable 12 final**, describing final bug fixes and screen/menu refinement. This supersedes the **historical** earlier D-141 preinstall and unaccepted statuses written below, which are retained to document the build/test chronology. Do not repeat the completed JVM build; no APK install or device operation was performed by the GitHub connector. Stable 11 / D-140 source and archive are preserved, not rewritten. Google Play preparation is next, with all external actions approval-gated.

## D-141 local QA attempt #2 — successful tests and debug APK build (2026-10-31)

**Evidence:** User-supplied Samsung Code on the Go terminal screenshot after a successful fast-forward from `f08f146` to `6e86399` on `android-native-kotlin-compose`; output explicitly shows `ARTWORK AUDIT PASS`, `D-141 JVM tests and BUILD SUCCESSFUL`, and `No install, GitHub Actions, push, permission change, or app data wipe`. The guarded script runs `:app:testDebugUnitTest` before `:app:assembleDebug` and aborts on failure. Therefore **local JVM regression tests and the debug APK build passed**, including the previously failing tests. The screenshot does not expose a test count, detailed test XML, or a separately checked APK digest; do not invent counts.

Local output on user phone:
- `/storage/emulated/0/Download/Atelier-Time-D141-API36-Candidate.apk`
- `/storage/emulated/0/Download/Atelier-Time-D141-API36-Manifest.txt` (source commit and APK SHA-256)
- `/storage/emulated/0/Download/Atelier-Time-D141-API36-Build.log` (full local log)

**Gate:** The app **has NOT been installed**; the installed user-accepted version is still Stable 11 / D-140. Seek explicit separate authorization for an **in-place Android Update** of the D-141 APK, never uninstall/delete/reset app data, then test the seven field requirements and regression matrix. Do not declare a new Stable version until device checks and user acceptance.

## D-141 local QA attempt #1 — failed; two source fixes prepared (2026-10-31)

**Evidence:** User-provided Samsung Code on the Go terminal screenshot. Kotlin compilation reached JVM tests with nonblocking warnings. `:app:testDebugUnitTest FAILED`; Gradle `BUILD FAILED in 50s`; full log on phone at `/storage/emulated/0/Download/Atelier-Time-D141-API36-Build.log`. Do **not** claim an assembled APK, passing JVM suite, device install or acceptance.

Two identified failing tests:
1. `RunEndTimePlannerTest.originalFinishOfLegacySingleSessionStillRunsUnchanged`, assertion at test line 191. The D-141 initial planner had inadvertently allowed extending a legacy Class with only one regular work Session. **Repair:** `FinalPairCompensation.reflowInitialFinish` now forbids any changed initial finish without a legitimate last-two-Session pair separated by an existing Break; unchanged original finish remains supported.
2. `RunEndTimePropertyTest.everyFeasibleWholeMinuteEndKeepsUnrelatedPhasesAndTargetExact`, assertion at test line 36. The historical test incorrectly froze the entire early phase prefix even when D-141 inserts full regular Session+Break cycles and deliberately relocates a still-unstarted automatic special Break. **Repair:** retain strict unchanged-prefix assertions when no cycles are inserted; for inserted cycles test exact end time, contiguous phases, ≥1-minute work, original saved Class unchanged, preserved Summary and special-break count/total length, and valid final-ending shape. This is a revised test requirement, **not** permission to alter observed work or manually placed special Breaks.

Both code changes are on the development branch only. **They have not been retested.** The next authorized step is a phone-local rerun of the existing `tools/build_d141_field_feedback_on_phone.sh` gate after safe Git fast-forward. Failed tests prevent creating the candidate APK. Installation still requires separate consent.

## User field decisions — complete list

1. Reduce redundant visible explanatory text across the app. Long instructional content belongs in Help; preserve essential action names, critical constraints/errors and warnings. Shortened the Editor, Free Timer, Sounds, Saved Classes and Run UI. Existing Help dialogs remain accessible.
2. Free Timer selected HH:MM:SS wheel numerals: 2× the original type size, **only** on Free Timer; clock-approval wheel stays unchanged. Check fit on small and large-font devices.
3. Retain the sound toggle, default ON; show `Timer sounds ON` or `Timer sounds OFF`. When OFF, no **automatic timer completion** cues are played (Class or Free Timer). Explicit manual sound preview remains a distinct tester action. Clarifying OFF helper text appears below the switch.
4. **User explicitly withdrew the earlier proposal to remove manual placement.** Keep `Place special breaks automatically` ON by default; OFF still reveals a manual-position field. The field now reads `Regular breaks to replace (e.g. 2,4)` and explains that those are *ordinal slots to replace with special breaks*, NOT the count of regular breaks. Never remove the ON/OFF choice.
5. **No change whatsoever** to `Session first` / `Break first`. Preserve both existing options and behavior. Existing Example A/B and saved legacy rules must retain their exact semantics.
6. When a final-two-Session split is legal and no option has been selected, `Even` is the default, visibly selected in Editor and reflected in preview and Save. Explicit `Last gets remaining` / `Custom` are still available. Never apply `Even` when the split is not permitted; previously saved explicit choices remain intact.
7. **Confirmed source cause:** original initial finish-clock approval called legacy `FinalPairCompensation.adjust`, which changed only the final two regular work Sessions. `Next` used the unified constraint planner with automatic added Session+Break cycles. Preview and actual Run start did not use exactly the same entry point. D-141 calls `FinalPairCompensation.reflowInitialFinish` from BOTH `RunEndTimePlanner.plan` and `MonotonicSessionTimer.start`. The shared `ConstraintFuturePlanner` can now create complete regular Session+Break cycles during a substantial **initial extension**, rather than making two giant end Sessions; qualifying automatic special breaks may be redistributed into newly added regular break slots, while manual special positions stay fixed. Shortening uses the same constraint validator; impossible choices are rejected, with no mutation of a saved Class. `Next`, `Restart`, `Pause/Resume` continue to use their established protected-prefix / constrained-future policies.

## Source files modified

- `android-native/app/src/main/java/com/ateliertime/android/`: `FreeCountdownSection.kt`, `EndClockWheel.kt`, `SessionEditorScreen.kt`, `MelodySettingsSection.kt`, `SessionTimerScreen.kt`, `PresetLibrarySection.kt`.
- `android-native/app/src/main/java/com/ateliertime/domain/`: `RunEndTimePlanner.kt`, `FinalPairCompensation.kt`, `ConstraintFuturePlanner.kt`, `MonotonicSessionTimer.kt`.
- `android-native/app/src/test/java/com/ateliertime/domain/RunEndTimePlannerTest.kt`: added long-extension insertion/physical-start-preview parity and protected manual special-break regression cases.
- `android-native/app/build.gradle.kts`: D-141/code35.
- `android-native/tools/build_d141_field_feedback_on_phone.sh`: a **prepared but not executed**, local Samsung Code on the Go build gate that checks Git, exact art, JVM tests and debug APK, without installation or GitHub Actions.

## Historical QA plan established before user acceptance (now accepted by user)

1. **Separate explicit approval required before running ANY build/test** in Samsung Code on the Go. No Gradle/Android build in GitHub Actions, no paid CI, no Termux compile.
2. On the phone before a build, verify branch `android-native-kotlin-compose`, clean tracked changes, and fast-forward only from the completed phone Git reconciliation. Do not rebase/reset/force/delete `.cg`; do not ask for Git passwords/tokens. Once approved, use the prepared one-command script only on Samsung CoGo after branch confirmation.
3. **COMPLETED:** Samsung local JVM regression suite and debug APK assembly PASS on attempt #2. Exact passing test count not shown. **NEXT:** installation requires distinct user authorization; then physical-device functional/UI acceptance and preservation of Classes/Sounds.
4. UI on device: Free Timer digits twice size without clipping at normal/large font sizes; functional wheel edit/read-only states; Sounds ON/OFF semantics; manual-break OFF field help and ON default; Even selected when applicable and overridable; `Session first / Break first` remain unchanged; compact on-screen labels still make sense; Day/Night behavior.
5. Scheduling: extension at +6m (existing pair only) versus +90m (new full cycles, distributed breaks), shortening and impossible red boundary, preview matches actual Run phases, fixed physical end, Summary preserved, genuinely long special breaks protected, manual placement unchanged, repeated Next/Restart/Pause/Resume do not rewrite observed prefix. Test saved legacy presets and user Sounds/Classes preservation.
6. **COMPLETE:** User explicitly declares D-141 final Stable 12 after reporting that everything works. New frozen branch `stable/android-d141-qa12-final-user-accepted` records this final D-141 source; D-140 frozen snapshot remains intact. Resume Google Play preparation.

**This document records an already authorized Samsung local JVM build/test that succeeded. It does NOT authorize publication, payment, user data modifications, APK installation, or AAB generation.**
