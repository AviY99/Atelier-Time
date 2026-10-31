# D-141 — user field feedback, pre-Play Android development candidate (2026-10-31)

**Status: SOURCE CHANGED / NOT BUILT / NOT INSTALLED / NOT USER-ACCEPTED.**
Android developer branch: `android-native-kotlin-compose`; `versionName D-141`, `versionCode 35`, target/compile API 36. Stable 11 / D-140 remains frozen and user-accepted at `stable/android-d140-qa11-user-accepted` (`b58634e`). The existing user phone APK and locally saved Classes/Sounds have not been touched.

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

## QA required before any user acceptance

1. **Separate explicit approval required before running ANY build/test** in Samsung Code on the Go. No Gradle/Android build in GitHub Actions, no paid CI, no Termux compile.
2. On the phone before a build, verify branch `android-native-kotlin-compose`, clean tracked changes, and fast-forward only from the completed phone Git reconciliation. Do not rebase/reset/force/delete `.cg`; do not ask for Git passwords/tokens. Once approved, use the prepared one-command script only on Samsung CoGo after branch confirmation.
3. Run all JVM regression tests during the approved local build and review actual failures. Tests have been added to source **but not run yet**. Do not claim a passing test count. Produce debug candidate only; do **not** install without a second user authorization.
4. UI on device: Free Timer digits twice size without clipping at normal/large font sizes; functional wheel edit/read-only states; Sounds ON/OFF semantics; manual-break OFF field help and ON default; Even selected when applicable and overridable; `Session first / Break first` remain unchanged; compact on-screen labels still make sense; Day/Night behavior.
5. Scheduling: extension at +6m (existing pair only) versus +90m (new full cycles, distributed breaks), shortening and impossible red boundary, preview matches actual Run phases, fixed physical end, Summary preserved, genuinely long special breaks protected, manual placement unchanged, repeated Next/Restart/Pause/Resume do not rewrite observed prefix. Test saved legacy presets and user Sounds/Classes preservation.
6. D-141 may be promoted to a new stable checkpoint **only after explicit device QA and user acceptance**. Stable 11 remains immutable; Google Play preparation resumes after resolving the field findings.

**No deployment, publication, account/payment, user data modification, APK installation, Android compilation or AAB generation is authorized by this document.**
