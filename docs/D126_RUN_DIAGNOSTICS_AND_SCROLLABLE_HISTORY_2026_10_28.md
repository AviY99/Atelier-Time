# D-126 — Run diagnostics, full scrollable actions and focused Timer view

**Date:** 2026-10-28. **Status: SOURCE IMPLEMENTED ON DEVELOPMENT BRANCH; SAMSUNG GRADLE/APK QA NOT YET RUN.** Stable 8 `stable/android-d124-qa8` is frozen; no APK installation or real user-data changes occurred.

## User-approved product policy

The user accepts a permanent separation of three views into the same Run data:
1. **Normal Timer view** is operational only: live physical clocks/end, current phase, progress, unchanged actual-past/forecast-future timeline, and established controls. Do not show internal final-pair redistribution, `Next credit`, or algorithm-explanation paragraphs in the running UI. A rejected operation still displays a short explanatory error.
2. **Bottom action history** contains **EVERY meaningful event from Run Start**, in latest-first order. A bounded scroll window shows approximately **five recent actions**; scrolling allows reaching Start. No drop at 30 entries and no six-entry truncation. Human terms `Session` / `Regular Break` / `Summary`, action, time, cue-request label, and failures; technical internal IDs and uptime belong in the diagnostic file, not the normal log.
3. **Per-Run full diagnostic** is private append-only `.jsonl` with `run_start` snapshot containing all Editor `SessionRules`, opening groups, special-break placement, approved schedule, end choice, warnings, approved physical end and initial A/B/C sound configuration. Each significant action/automatic transition records precise wall and Android monotonic timestamps, before/after TimerView, physical remaining budget, correction, audio cue *requested* (not verified audible), generic planner constraints, phase IDs added/removed, and the complete before/after schedule on structure changes. Includes rejected operations, sound-setting changes, sound errors and run-end reason. Interrupted runs remain as incomplete files, not falsely marked successful. No 200ms tick stream.
4. **Local history control:** reports listed in Timer HOME after Saved Classes, with explicit export through Android Storage Access Framework and explicit deletion confirmation; storage private until user exports. Never auto-upload; never record selected audio URI/path. Existing Saved Classes/Sounds data are not modified by this feature.

**Final clarification from the user:** The bottom action history is NOT a six-item preview with only a hidden file; its entire Run event list is scrollable in situ, with five entries visible by default.

## Source implementation

- `android-native/app/src/main/java/com/ateliertime/android/SessionTimerScreen.kt`: removed explanatory final-pair text (not timer state), inserted bounded lazy history list `class_action_history_scroll`; 5-ish events visible, all accessible until Start.
- `android-native/app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt`: removed 30-entry history cap; one logical event produces human history and detailed report; Start/Next/Restart/Pause/Resume/Automatic/Deadline/Exit and failed attempts; late async sound-playback errors included. No changes to Timer's scheduling math or timeline renderer.
- `android-native/app/src/main/java/com/ateliertime/android/RunDiagnostics.kt`: new Android-private per-Run append-only schema v1; exact editor and schedule snapshots, sound config and planner constraints; safe file listing/export/delete.
- `android-native/app/src/main/java/com/ateliertime/android/RunDiagnosticsLibrary.kt`: private history management and SAF export.
- `android-native/app/src/androidTest/java/com/ateliertime/android/RunDiagnosticsInstrumentedTest.kt`: isolated instrumentation test sources for complete/abandoned reports; **WRITTEN, NOT RUN**.
- `android-native/app/build.gradle.kts`: development-only `D-126`, `versionCode 20`. D-125's earlier subminute-Summary rule remains REJECTED and NOT implemented.
- `android-native/tools/build_d126_candidate_on_phone.sh`: build/test/manifest gate **ONLY**, no install, data clear or device testing.

## Release/QA gates

1. Review source code and verify no changes in `domain/ConstraintFuturePlanner.kt`, `domain/MonotonicSessionTimer.kt`, `domain/ScheduleEngine.kt` or `TimelinePhaseChart.kt` from Stable 8.
2. On Samsung Code on the Go, **ONLY after approving build**, run `bash tools/build_d126_candidate_on_phone.sh` from `android-native`. Record actual Gradle/JVM/AndroidTest APK results, JSON manifest, SHA-256, and fix actual compilation/test issues on development branch. Do not call written AndroidTest tests executed simply because `:app:assembleDebugAndroidTest` passed.
3. Test UI five-event height, scroll through >30 actions back to Start, newest-first behavior while scrolling old history, no duplicate/missed transitions, rejected Next, original Timer layout unchanged, and that report JSON lines correctly reflect a Run with Editor input and altered future.
4. Android instrumentation: run the test-only APK with explicit user permission, keeping real Saved Classes/Sounds untouched. Tests target the instrumentation package's isolated private files.
5. Validate actual persistent data across app restart, export via SAF and confirmed delete on isolated diagnostic records. Confirm physical end, cue count, app resume/background and performance with >100 events; no background surprise installs.
6. **Only after real tests/approval:** optional separate in-place update install of D-126 to user's phone, confirm all existing Saved Classes and Sound settings retained. Stable 8 remains immutable recovery checkpoint.

## Limits and design tradeoffs

- This logging does not prove audio was heard, does not guarantee logging after the OS abruptly terminates the app, and does not constitute OS/process recovery. It persists *each completed record* as a separate line.
- Exact user-device acceptance and Android compilation are **PENDING**. A source-level review is not a substitute.
- A failure of private storage must not abort an otherwise legal timer operation. A visible diagnostic warning is shown.
- Files are retained until user explicitly deletes them; future retention/size policy requires separate user approval.
- Future engineering may enrich the reason for rejected candidate allocations at the pure-planner boundary; this change currently records the relevant numerical constraints, failures and before/after plans rather than claiming a trace of every internal alternative.

**Source baseline:** Stable 8 D-124 user-accepted source commit `09c63d42d5b7ae3ddc13b8755a2262b7b0e5c9a7` and immutable checkpoint branch `stable/android-d124-qa8`. All modifications in D-126 are development-only.

## Additional source audit and test hardening (2026-10-28)

- Start-event recording now passes the immutable Editor-approved phase list as the **before** snapshot. When a separately approved Run end-time changes the initial runtime allocation, the event saves the actual adjusted schedule as the **after** snapshot; previously the report could contain only the editor-approved plan and omit the runtime start reflow.
- Added a third isolated Android instrumentation test for a Run with **50 Next event records**, checking 53 JSONL records, ordered sequence IDs, the original Editor schedule and a different actual initial schedule. Test source is **WRITTEN, NOT EXECUTED**.
- GitHub compare against frozen Stable 8 confirms **no changes** to any `domain/` scheduling or monotonic timer implementation or to `TimelinePhaseChart.kt`. Changes remain in UI, diagnostics, test sources, packaging identity, docs and build-only script.
- **No Samsung build/JVM/instrumentation execution or APK update-install yet.** Do not claim this D-126 candidate is accepted or stable; preserve Stable 8.

## Pre-build static audit — corrected test fixtures (2026-10-28)

The three newly authored RunDiagnostics AndroidTest fixtures initially used 600-second Class / 60-second Summary, P=120s, B=60s. With 540 seconds of work, the Editor schedule would end with an illegal Break lacking the minimum one-minute following Session; `engine.generate(... FinalPoseChoice.remainder())` would therefore fail before diagnostics assertions. **Corrected all three fixtures** to a 660-second Class (600-second work): `120 + 60 + 120 + 60 + 120 + 60 + 60 = 600s`, with the final 60-second Session legal and shortened, so the explicit `REMAINDER` approval is required and valid. Corresponding class-end epochs were updated. The 50-event test additionally applies -60s initial correction to represent a 720-second Run and verifies the planned runtime timeline differs from the Editor snapshot. Source-level arithmetic and APIs checked; actual Samsung Gradle compilation and instrumentation **remain pending**, never claim PASS without logs.

Build-only next command from the Android project directory after fast-forward pull: `bash tools/build_d126_candidate_on_phone.sh`. The script cannot install either APK or erase Saved Classes/Sounds; keep the installed Stable 8 unchanged. Capture full `D-126 JVM ACTUAL`, `BUILD SUCCESSFUL` and the generated manifest before discussing installation.

## Samsung build-only QA evidence — PASS (2026-10-28)

The user ran the documented `git pull --ff-only && bash tools/build_d126_candidate_on_phone.sh` build-only gate in Code on the Go and supplied a terminal screenshot. **Observed actual terminal evidence**:
- `BUILD SUCCESSFUL in 1m 21s`, **72 actionable tasks: 72 executed**.
- `D-126 JVM ACTUAL: tests=243 failures=0 errors=0 skipped=0`.
- `BUILD ONLY PASS`: regular candidate `/storage/emulated/0/Download/Atelier-Time-D126-Candidate.apk`; AndroidTest APK `/storage/emulated/0/Download/Atelier-Time-D126-Tests.apk`; QA manifest `/storage/emulated/0/Download/Atelier-Time-D126-QA-manifest.txt`.
- Build script explicitly reports **no APK installed**. Assembled AndroidTest APK does **not** mean tests have executed on a real Android device.

**Next quality gate:** read the QA manifest and compare the SHA-256 of the two local APKs with the manifest, verify the recorded source commit is approved, then arrange real-device instrumentation and manual UI/diagnostics checks **only after separate authorization** for a controlled in-place D-126 app upgrade and test APK installation. **Do not** uninstall the installed Stable 8, clear app data, or run an instrumentation install without separate permission. The new AndroidTest targets D-126 code and cannot be meaningfully executed against an unchanged D-124 app. Saved Classes/Sounds preservation must be verified after any user-authorized update. Stable 8 checkpoint remains frozen.

**QA status:** D-126 Gradle/JVM/AndroidTest-package assembly PASS; physical Android instrumentation, full scroll-to-Start, 50+ events and export/deletion in real app remain PENDING.
