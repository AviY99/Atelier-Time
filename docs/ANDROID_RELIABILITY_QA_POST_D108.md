# Android reliability QA gate — after Stable 4 (D-108)

## 2026-10-25 — QA Stage 4 ADB availability: phone screenshot confirmed

- The user executed a read-only check in Code on the Go terminal, from the existing `Atelier-Time/android-native` checkout: `find "$HOME" "$ANDROID_HOME" -type f -name adb -size +0c 2>/dev/null | head -n 10`. **Output: no nonempty ADB candidates** under either search root (not a system-wide filesystem proof).
- `ls -l "$ANDROID_HOME/platform-tools/adb"` shows a **0-byte regular file** with mode `-rw-------`; this is not an executable binary. Do **not** apply chmod or overwrite the CoGo SDK.
- The prior screenshot proved `:app:assembleDebugAndroidTest` **BUILD SUCCESSFUL in 34s**, 50 tasks; this is a compilation/package result only. **None of the 14 Android instrumentation tests has run; no instrumented-test PASS has been claimed**.
- Current authoritative Android guidance runs instrumentation through a connected target (Gradle `connectedDebugAndroidTest` or `adb shell am instrument -w`). Official CoGo debugger documentation describes a debugger pairing workflow but **does not demonstrate an instrumentation launcher for external projects**. Do not conflate debugger pairing with AndroidJUnitRunner execution.
- **Non-invasive supported option:** use a **separate official Termux installation** (F-Droid source) with the `android-tools` package, pair its ADB client via Android's Wireless debugging on the **same phone** (Android 11+ and Wi-Fi), then use ADB to install the already-built **test APK only** and run its JUnit instrumentation runner, preserving the existing D-110 app and CoGo managed SDK. This path is reported as a documented option, **not yet installed, paired or executed**. The user should not post transient wireless-pairing codes/screenshots with pairing PIN. Avoid untrusted adb binaries and do not alter developer security settings beyond user-approved wireless debugging; switch it off afterward.
- **User safety gate:** perform only while Atelier Time has no running Class. The 7 preset and 6 melody tests use separate random test preference namespaces, but a connected instrumentation run still briefly stops/restarts the application process; never uninstall or clear actual user data.
- Other standard option if the separate-phone client is unwanted: a trusted PC with official Android SDK Platform Tools connected to the phone. This is optional, not a mandate to change the approved phone-first development setup.

References: https://developer.android.com/studio/test/command-line ; https://developer.android.com/tools/adb ; https://github.com/termux/termux-app ; https://f-droid.org/packages/com.termux/ ; https://appdevforall.org/code-on-the-go-release-notes-26-40/

> **2026-10-25 — Stage 4 AndroidTest APK COMPILE/PACKAGE PASS on user's CoGo phone (screenshot):** `:app:compileDebugAndroidTestKotlin`, `:app:packageDebugAndroidTest`, `:app:assembleDebugAndroidTest` completed, terminal shows **BUILD SUCCESSFUL in 34s; 50 actionable tasks (30 executed, 20 up-to-date)**. These Gradle tasks assemble the Android instrumentation package but DO **NOT** invoke device tests; the **14 `androidTest` source methods remain NOT RUN**. The previously observed `$ANDROID_HOME/platform-tools/adb` is a 0-byte, non-executable placeholder; `adb` not found on PATH. Before requesting `:app:connectedDebugAndroidTest`, discover a supported usable ADB/device path rather than modify CoGo SDK, add untrusted binaries, install test package by hand, or infer a PASS. No new regular app build/update, no user-data changes, D-110 still approved and D-108 Stable 4 frozen. [Ordered QA documentation](ANDROID_QA_RECOVERY_AND_DEVICE_TESTS_2026_10_25.md).

> **2026-10-25 — USER PRIORITY: QA Stage 3 THEN Stage 4 (D-110), NO NEW APP BUILD.** [Active-Class recovery source audit and safe phone test protocol](ANDROID_QA_RECOVERY_AND_DEVICE_TESTS_2026_10_25.md) is complete. App uses Compose `remember` for active Class, Timer and tracker; no persisted active-run checkpoint, no BOOT_COMPLETED receiver and FGS is notification-only `START_NOT_STICKY`. Therefore **resume after process death/reboot is not implemented**, and immediate notification cleanup on unexpected process death is not guaranteed (new Activity launch dismisses stale status). A **normal reboot DURING an active test Class** has **NOT** yet been checked (older reboot PASS covered Classes/Sound when no Class running). OS memory reclaim needs system-level transport and is still **BLOCKED**. **Do not mark Stage 3 as device PASS**. Once actual 3b user observation returns, Stage 4 can validate instrumentation transport; existing CoGo adb path is a 0-byte file and 14 androidTest source methods remain **UNEXECUTED**. No app code/Gradle/installed APK/Stable 4 change.

> **LATEST QA UPDATE — 2026-10-25 — **D-110 USER PHONE CONFIRMATION: "הכל תקין" (everything is OK)**, reported directly in reply to instructions for opening the freshly built `Atelier-Time-D110.apk` as an **update**, checking About, and ensuring saved Classes/Sound choices survive. **Status: user-reported D-110 functional acceptance / installation workflow OK**, not independently captured screenshots of About or a database inspection. The preceding Code on the Go Gradle `:app:testDebugUnitTest :app:assembleDebug` **BUILD SUCCESSFUL in 47s**, 42 tasks; package prepared as `versionName="D-110"`, `versionCode=5`. **No explicit real-device ±120s manual/system clock correction test was reported**; therefore the targeted wall-clock correction QA remains **OPEN ON DEVICE**, although source-level Kotlin tests and virtual clock simulation passed. Full 3-hour real-device/Doze and QA-R01 connected instrumentation also remain open/blocked. **D-108 Stable 4 frozen unchanged; D-110 is not declared Stable 5, no new build or deployment performed in this documentation update.**** [D-110 evidence](ANDROID_D110_MONOTONIC_DEADLINE.md).

## QA-R03 follow-on / D-110 source correction (not yet device verified)

- **D-110 / 2026-10-25 — MONOTONIC CLASS DEADLINE FIX IN SOURCE, NOT YET BUILT:** QA-R03 identified Class remaining/elapsed display drift after ±120s wall-clock correction and a more serious wall-clock-based physical Class finish check. The narrow fix uses `SystemClock.elapsedRealtime()` (already the Timer clock) for physical Class elapsed/remaining and Class deadline enforcement, retaining Clock/Start/End wall-time labels and all approved controls/cues/UI. Added 3 Kotlin JVM test methods for clock corrections, Pause/Next and fixed original deadline. Prepared next actual APK `versionName="D-110"`, `versionCode=5`. Assistant-side extracted Kotlin clock harness PASSED 7 targeted groups and a 3-hour/10,801-checkpoint simulation; **full CoGo Gradle `:app:testDebugUnitTest :app:assembleDebug`, real D-110 APK and device acceptance NOT EXECUTED**. No edit to frozen `stable/android-d108` or production user data. [Scope and execution plan](ANDROID_D110_MONOTONIC_DEADLINE.md).

## QA-R03 next-gate result — accelerated runtime reliability (2026-10-25)

- **QA-R03 2026-10-25 / ACCELERATED KOTLIN RELIABILITY TESTS — COMPLETED WITH ONE OPEN DEFECT CANDIDATE:** [Report](ANDROID_QA_R03_ACCELERATED_2026_10_25.md). In local Kotlin/JVM (extracted D-108 timing paths, not APK), 150 seeded 3h simulations / **2,483,558 virtual updates** passed exact monotonic/deadline/one C-cue expectations; 30-day sparse samples and 0ms/5,999ms/6,000ms boundary tests passed current policy. Simulating a **±120-second device wall-clock correction** left monotonic engine remaining correct but changed **Class remaining/elapsed** wall-based values by 120s; static screen-source review confirms the UI consumes these fields. This is a **simulated, source-supported display discrepancy**, not an Android phone defect confirmed in vivo. Do not modify accepted UI/timer without targeted follow-on scope. Actual three-hour device/audio/Doze gate OPEN; connected instrumentation BLOCKED; normal reboot saved-data PASS. **No APK, code or stable-ref changes.**
- **Next engineering gate:** triage wall-clock-correction display behavior (do not silently change approved UI); real long Android QA remains unexecuted.

## QA-R02a — accelerated Kotlin/JVM three-hour timing simulation (2026-10-25)

**Outcome: PASS for the extracted uninterrupted timekeeping path in a Kotlin/JVM harness — NOT a real 3-hour Android-device test.**

- Baseline: frozen D-108 Kotlin logic at `02beb0af46719301cccb72ad4698a337ab416d73`. Assistant compiled and ran an isolated Kotlin harness with the production `MonotonicSessionTimer` logic for `start/view/update/elapsedMs/phaseIndex`, the D-108 `ClassClockTracker` clock calculation, and an independently constructed canonical Example B 180-minute phase fixture.
- Excluded: full Android APK, Android OS scheduler/Doze/process reclaim/audio playback, exact `ScheduleEngine` fixture generation, and Pause/Resume/Next/Restart paths (those are omitted or stubbed). All four runs use a **virtual monotonic clock** and a separate **mathematical reference clock**, not a physical standard clock. Results therefore prove arithmetic and alert-state invariants in the exercised path, **not real-device elapsed-time accuracy**.
- 200ms periodic ticking: **54,001 updates**, PASS; 6 A / 5 B / 1 C, scheduled completion exactly 10,800,000ms, calculated deadline drift 0ms.
- 1000ms periodic ticking: **10,801 updates**, PASS; 6 A / 5 B / 1 C, completion exactly at target, calculated deadline drift 0ms.
- Deterministic random update spacing 120–2200ms: **9,346 updates**, PASS; 6 A / 5 B / 1 C, same Class target, detection of final completion at 10,800,887ms (887ms later than target); max intermediate phase-cue detection lag 1845ms due to sparse updates, not deadline drift.
- One-hour simulated UI update silence between Class hour 1 and hour 2: **7,202 updates**, PASS for specified missed-cue suppression; 5 A / 4 B / 1 C (skipped alerts not replayed), Class completes once at target.
- Across all sampled observations Class-clock mathematical deadline discrepancy 0ms. No regression to signed preset; no new build on phone.
- Source of evidence: compiled Kotlin/JVM test console output in this assistant execution, with downloadable runnable harness/report in the conversation. No independent Android-connected JUnit reports or external-clock chronometer observation.

**Gate disposition: QA-R02a SIMULATED PASS; QA-R02b real-phone multi-hour wall-clock/audio/battery/Doze = OPEN/UNVERIFIED.** Do not promote the simulated result to full QA-R02 PASS or Stable 5, and do not require user to repeat already-approved short A/B/C or Back/Swipe functional tests.

## QA-R04 — real-device reboot persistence observation (2026-10-25)

**Result: PASS for reboot persistence subcase only — user-reported acceptance.**

- Device: the user's existing Android phone, with previously accepted D-108 debug APK; exact OS build and device model were not separately captured for this check.
- Scenario: ordinary phone restart, then reopen Atelier Time and verify existing saved Classes and Sound settings/selections.
- Expected: previously saved Classes and Sound choices remain after reboot, without uninstalling/clearing app storage.
- Actual user feedback: **"הכל נשמר לאחר ריסטארט לנייד"** ("Everything was preserved after restarting the phone").
- Evidence: explicit user's device observation in chat; not independent connected-instrumentation output, screen recording or JUnit assertion.
- **PASS: saved Classes and Sound preferences persist across a normal device reboot.**
- **Not tested / no inferred PASS:** recovery of a Class/timer that was running before reboot; restoration after unexpected OS process kill/Doze; corrupt/future-schema handling on the real device; data survival during a future versioned APK update. These remain separately open.
- No source/Gradle version modifications, APK rebuild/install, data reset, or repeat of previously approved A/B/C/Back/Home/Swipe scenarios.

**Next reliability step:** QA-R02 — verify real multi-hour session wall-clock drift/deadline and unattended boundary behavior during a normal future Class, avoiding redundant short A/B/C tests. Do not equate existing injected-monotonic-clock JVM tests with actual multi-hour Android-device endurance.

**Prepared:** 2026-10-25. **Status:** SOURCE AUDIT COMPLETE; DEVICE EXECUTION NOT STARTED. This is documentation and evidence planning only; it does not constitute a new APK, a passed test, or a change to accepted product behavior.

## Fixed baseline and non-regression boundary

- Android Stable 4 is user accepted: `stable/android-d108` at `02beb0af46719301cccb72ad4698a337ab416d73`.
- Continue on `android-native-kotlin-compose`, Kotlin + Jetpack Compose and Code on the Go on the Android phone.
- **Do not reopen** approved Day/Night/Editor/Timer/Sound UI, ordinary transitions A/B/C, Home/other-app/lock-screen background playback, or D-108 Back/Recents-swipe notification cleanup.
- Do not uninstall the existing app, clear app data, overwrite user Classes/preferences, move any frozen stable ref, merge draft PR #3, or run paid Actions.
- Current accepted package has `versionName = "D-108"`, `versionCode = 4`. For the next **actual APK** use its real coding D identifier (next reserved D-110 unless subsequently superseded) and a strictly larger versionCode. Documentation-only activity requires **no new build**.

## QA-R01 field observation — 2026-10-25, 13:14 Asia/Jerusalem (user screenshot)

**Evidence scope: environment inspection only; NOT a connected test run.**

- In Code on the Go terminal, `git branch --show-current` displays `android-native-kotlin-compose` (the first multi-command paste is visually garbled, but the branch text is visible).
- `git rev-parse --short HEAD` displays `02beb0a`, matching the frozen D-108 source and demonstrating that the phone checkout has **not yet pulled the documentation-only post-D108 commits**.
- `git status --short` displays `?? ../../.cg/`. This is an **untracked path** whose location and function have not been independently verified. Do not add/delete/clean it. Next use `git rev-parse --show-toplevel` to establish the worktree root.
- `command -v adb` yields no path, and `adb devices -l` returns `bash: adb: command not found`. No connected Android instrumentation runner is currently available through the terminal PATH.
- This is **QA-R01 DEVICE RUN BLOCKED / TOOL ACCESS NOT VERIFIED**, **not** an app test failure and **not** proof that `$ANDROID_HOME/platform-tools/adb` or any CoGo-integrated device runner is absent.
- **Safe next action:** inspect the Git worktree root and check whether `$ANDROID_HOME/platform-tools/adb` is executable; do not install platform tools, pair devices, or attempt Gradle connected tests yet. Existing D-108 accepted APK and personal data remain untouched.

## QA-R01 follow-up — second Code on the Go screenshot (2026-10-25)

**Observed directly from screenshot (not an instrumentation PASS):**

- `git rev-parse --show-toplevel`: `/storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time`.
- Git checkout remains at short SHA `02beb0a`. No pull of documentation-only changes was performed on the phone.
- `ls -l "$ANDROID_HOME/platform-tools/adb"` returned a **0-byte** regular file without execute bits (`-rw-------`), inside CoGo's SDK. `adb` in the current terminal PATH is also absent.
- Therefore the SDK path is **not an executable ADB binary**; changing `PATH` or using `chmod` alone would not fix the zero-byte file. Do **not** replace platform tools, install a toolchain, change app code or issue a connected Gradle test yet.
- The existing `?? ../../.cg/` untracked status entry is left untouched; its role is unknown.
- **QA-R01 result: BLOCKED — no usable terminal ADB/device test transport verified.** The 14 declared instrumented test methods have not executed and are neither PASS nor FAIL. A CoGo-integrated alternative runner could be investigated later if supported, without treating this blocker as an app defect.
- Other independent gates can continue without this harness: next recommended minimally disruptive user-device gate is **QA-R04 reboot persistence using the already installed D-108**, limited to checking existing saved Class names and Sound choices before/after one ordinary phone reboot. Do not run it until baseline values are recorded; do not clear application storage or run unapproved recovery semantics. Active timer restart after OS death/reboot is **not** an accepted capability and must not be implied.

## Source audit of existing connected Android instrumentation tests

Read the Android sources from Stable 4 / the identical native development branch. These are **test methods present in source**, NOT results or confirmed discoverable tests on the phone:

| Source | Declared `@Test` methods | Scope / data safeguards |
| --- | ---: | --- |
| `app/src/androidTest/java/com/ateliertime/android/AndroidPresetStoreTest.kt` | 7 | Round-trip save/reload, CRUD, old/new opening-group format, corrupted JSON/wrong-type/future-version preservation; random private test SharedPreferences filename and `@After` deletion. No production `atelier_time_presets` namespace. |
| `app/src/androidTest/java/com/ateliertime/android/MelodyPreferencesTest.kt` | 6 | Audio selection and preference persistence/compatibility; random isolated test SharedPreferences file and `@After` cleanup. |
| `app/src/androidTest/java/com/ateliertime/android/StaticAudioTrackLifecycleTest.kt` | 1 | Android real AudioTrack MODE_STATIC initialization and short silent PCM test; not a playback/cue acceptance test. |
| **Total** | **14 source tests** | Exact number **executed/discovered** and PASS/FAIL remain unknown until an actual connected run. |

`app/build.gradle.kts` declares `androidx.test.runner.AndroidJUnitRunner` and Android instrumentation dependencies. Do not infer that a connected Android device, `adb`, test APK installation or a runner invocation is available in Code on the Go without checking the real terminal/device first. Historical runner notes counting 6 tests are superseded by the current 14 `@Test` source methods; no claim is made about runtime discovery.

**Test-data isolation caveat:** The two persistence suites use unique test names and delete their own files in `@After`. That prevents intentional contact with real Class/sound preferences, but abrupt test-process death can leave test-only preference files. Before execution, independently verify no test calls `clearApplicationUserData`, `pm clear`, `uninstall`, or production preference names.

## Next execution gate: QA-R01 connected instrumentation

1. On the existing CoGo checkout, confirm branch and commit using `git branch --show-current` and `git rev-parse HEAD`. Ensure there are no uncommitted local modifications before pulling any documentation changes.
2. Check whether CoGo's **existing** Android tooling can expose a connected instrumentation target (for example check `adb devices -l` only if `adb` exists). Do not install a new toolchain or create a build merely to check availability.
3. If a valid device target is available, run the existing `:app:connectedDebugAndroidTest` under CoGo with its proven **native ARM64 AAPT2** override. This may compile/install a separate test APK; it is **not necessary for the handoff itself**. Stop if test installation threatens user app data or device pairing is unavailable.
4. Preserve the real Gradle log and XML/JUnit reports; record actual test count, passed/failed/skipped, exact source HEAD, Android device/OS and test APK outcome. Label **BLOCKED** when the runner or device isn't accessible, not PASS.
5. Validate preservation of existing saved Classes and Sound preferences separately without re-running accepted audible A/B/C and Back/Home/Swipe acceptance.

Suggested future CoGo terminal invocation (ONLY when a connected device is confirmed, from `android-native/`, not for current handoff):

```sh
set -o pipefail
bash "$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle" :app:connectedDebugAndroidTest --no-daemon --console=plain "-Pandroid.aapt2FromMavenOverride=$ANDROID_HOME/build-tools/36.0.0/aapt2" 2>&1 | tee "$HOME/atelier-connected-androidTest.log"
```

## Following reliability gates (UNVERIFIED)

- **QA-R02:** long-running multi-hour Class on a real phone: actual timer deadline, observed A/B/C at novel long-run boundaries, drift/thermal/battery behavior. Existing injected-clock JVM regression is NOT real 3-hour device proof.
- **QA-R03:** Android Doze, battery restrictions and unexpected **OS process reclaim**; explicitly distinguish OS-initiated death from user-approved Back/Recents removal. The current notification-only foreground service does not own or promise to restart timer/audio. First define desired recovery behavior before changing it.
- **QA-R04:** persistence across device reboot and versioned update; corruption/future-schema behavior using **isolated test records** only. Never inject malformed data into actual user Classes.
- **QA-R05:** foreground-service `specialUse` Google Play policy audit (not accepted merely because debug works).
- **QA-R06:** accessibility, other devices, release signing/permissions/privacy/licensing. **MANDATORY user-approved logo and launcher/store icon assets before any store listing/publication**; see `ICON_LOGO_PRESTORE_GATE.md`.

## Evidence recording / decision gate

For each newly executed test record: **test ID; commit SHA; date; actual phone + Android version; exact command or procedure; isolated input; expected and observed outcome; PASS/FAIL/BLOCKED; logs/report reference; effect on real saved data; known limitation**. Record actual evidence in `docs/WORK_LOG.md` and update `docs/TEST_PLAN.md`, `docs/ROADMAP.md` and `docs/HANDOFF.md` only when the result exists. No accepted UI, sounds, timer semantics or lifecycle behavior are to be changed without a new identified defect and appropriate approval.

**Next immediate work:** QA-R01 — verify the device instrumentation execution path without building/reinstalling for the handoff. No such device check has been performed by this GitHub-source-only audit.
