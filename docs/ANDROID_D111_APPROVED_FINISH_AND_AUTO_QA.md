# D-111 — approved Class finish-time feature and automated QA gate

**Date:** 2026-10-25. **Target:** Samsung SM-S921B, Android, phone-first development in Code on the Go and ADB in Termux. **Branch:** `android-native-kotlin-compose`. **Frozen Stable 4:** D-108 `stable/android-d108` — DO NOT ALTER.

## Change request (user-approved, supersedes earlier discussions about ignoring a late deadline)

Every saved Class `Run` must first calculate and display the estimated finish clock; the user must **explicitly approve** that value or enter a different `HH:mm` finish. The selected value becomes a physical Class finish constraint whether it is equal to, earlier than, or later than the original plan. Only the last two regular work Sessions are adjusted, using the saved final-pair distribution mode. All earlier phases, intervening Break and Class summary retain their durations; the saved Class is immutable. If the target cannot be fitted, show a meaningful error, retain the approval dialog and require correction; no timer/audio/notification is started before a valid confirmation.

**End-clock input:** interpreted as the next occurrence of the chosen local HH:mm, today or tomorrow, with the resolved date displayed. **Validity:** both final Sessions must retain >=60 s, no negative deadline/overflow, single-Session Classes cannot receive arbitrary adjustments, and a late Class finish may extend the full approved Class by **at most its original duration** (maximum 2× total), to prevent accidental overnight runs. This maximum is a new explicit safety policy, not a property of the original compensation algorithm; reconsider only on further user request.

**Run starts:** The chosen absolute wall-clock target is revalidated immediately before starting against actual current time; an invalid target is rejected without a partially started run. The monotonic `elapsedRealtime()` clock is authoritative for progress and deadline even if device wall clock changes. No new Class progress survives explicit app exit/reboot (user requirement). The UI's calculated default for a legacy Class with fewer than two adjustable Sessions follows actual start time to avoid rejecting it due to one-second Compose startup latency; arbitrary user-entered changes remain subject to feasibility.

**During interruptions:** pause/stop/resume after the penultimate Session has begun retains that observed Session and the intervening Break and alters **only the not-yet-started final Session**. An impossible adjustment is rejected atomically and the user receives an error. If the last Session itself has begun, no hidden rewrite is permitted. The fixed physical Class end is enforced by the timer even when a long interruption cannot be compensated.

## Implemented source / QA

- `RunEndTimePlanner.kt`: signed earlier/later time calculation, local HH:mm validation, 2× cap, minimum duration validation.
- `MonotonicSessionTimer.kt`: already-observed phase remains immutable; late-stage resume uses `FinalPairCompensation.adjustLastOnly` instead of silently changing the completed first Session.
- `SessionEditorScreen.kt`: explicit calculated-versus-custom finish approval UI, real error previews and single error on invalid confirmation; legacy single-session default latency guard.
- `app/build.gradle.kts`: D-111 `versionCode=6`. The existing on-phone D-110 is NOT yet updated by source edits alone.
- Added `ApprovedFinishRecoveryTest.kt` (13 new deterministic JVM methods); updated one older final-pair test to correctly expect last-only recovery; retained the previous unit/property/scheduler/clock suites.
- `RunEndApprovalUiTest.kt`: 7 isolated Compose end-to-end scenarios, including reject/correct earlier/later values and explicit default confirmation. Uses randomized dedicated test storage; no intentional writes to real Classes/Sound.
- `RunEndTimeDeviceTest.kt`: 11 device-level domain/clock/package checks with no artificial real clock changes or persisted app state edits.
- Existing Android instrumentation: 7 AndroidPresetStore, 6 MelodyPreferences, 1 silent AudioTrack lifecycle.

**Source-count audit on GitHub:** **179 JVM @Test methods** across 17 files and **32 Android @Test methods** across 5 files, total **211 planned test methods**. Counts are source-level inventory, **NOT a claim that D-111 tests passed**. Other JVM property tests internally iterate hundreds of finish-time combinations. Existing D-110 baseline: **14/14 Android instrumentation PASSED on real phone** as documented in `ANDROID_QA_RECOVERY_AND_DEVICE_TESTS_2026_10_25.md`.

## Automated execution — two sequential safe gates

1. **Code on the Go terminal**, no Class running. From the known checkout:
   ```bash
   cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native
   git pull --ff-only
   bash tools/build_all_qa_on_phone.sh
   ```
   This runs `:app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest` using native CoGo Gradle/AAPT2 overrides, failing immediately on any build/test error. On success, copies `Atelier-Time-D111.apk`, `Atelier-Time-D111-Tests.apk`, and `Atelier-Time-D111-QA.sh` to shared Downloads. **No installation occurs in this gate.** The ignored/untracked CoGo `.cg/` directory must never be removed or checked in.

2. **Termux** (phone ADB authorized and Wireless debugging enabled), no Class running:
   ```bash
   bash ~/storage/downloads/Atelier-Time-D111-QA.sh
   ```
   The script checks one authorized phone, updates the normal app **in place** using `adb install -r` (preserves installed application data), verifies installed `versionName=D-111`, installs the separate test-only APK via `adb install -t -r`, verifies AndroidJUnitRunner registration, then runs `adb shell am instrument -w`. The pass gate strictly requires `OK (32 tests)`. Its output is stored locally at `~/storage/downloads/Atelier-Time-D111-QA-result.txt`. **Do not run with an active real Class**: Android instrumentation controls the app process.

### Status on 2026-10-25

**Implementation + scripts committed to development source. All D-111 JVM/Android build and execution outcomes are PENDING until user runs the two on-phone gates.** No installation, live app test, signed release or promotion to Stable 5 has been claimed. Keep D-108 frozen.

## Not safely proven by a synthetic test suite

- Real-world 3-hour background/lock screen/Doze run, sound actually audible on device, and physical long-run battery behavior.
- Changing Android's actual clock by ±120 seconds; monotonic fake-clock JVM and Android instrumented tests test logic but do not assert permission to change the device time.
- Genuine OS process reclaim, swipe of active task from Samsung Recents, and exact notification behavior during those events. Existing prior manual D-108 QA remains baseline, not silently relabelled as D-111 device PASS.
- End-to-end Android notifications require notification permission to be granted and user-observed behavior; the automated plan intentionally does not change permission settings, clear Android data, or force-stop/reboot the phone.

After device suite success: recheck real saved Classes/Sound manually, open About and confirm D-111, optionally switch off Wireless debugging; report any untested gate as OPEN.
