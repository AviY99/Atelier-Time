## 2026-10-25 — D-114 Stage 2 approved / source audit and safe QA preparation

User approved Stage 2 after the mandatory Stable 5 handoff. **A second permission checkpoint is mandatory for installed-app/device operations.** Stable 5 ref `stable/android-d114` confirmed unchanged at `34a692a98282944a76e314efcc228dac3bd072ff`; changes only in `android-native-kotlin-compose`.

**Source-only work completed:** replaced obsolete D-111 typed HH:mm Compose test interactions with D-114 live/wheel/tap/blocked-swipe/red-reason/green-reset, Confirm/Cancel and isolated persisted-Class assertions; expanded UI suite 7→12. Added six deterministic JVM finish-clock/boundary cases. Full current source counts: **185 JVM @Test across 18 files; 37 device @Test across 5 files; @Ignore=0**. Migrated `build_all_qa_on_phone.sh` to CoGo all-JVM + test-only APK/manifest with digest; migrated `termux_run_all_device_qa.sh` to strict installed D-114 preflight + test-only install + real instrumentation log, guarded with explicit no-active-Class confirmation. Retired `qa_build_d111.sh` and `qa_device_d111.sh` as fail-closed D-111 paths. No Classes/Sounds deletion, no regular APK installation.

**Not yet executed:** CoGo Gradle JVM task, AndroidTest APK build, phone test package install, on-device JUnit instrumentation, ADB read-only probes. All D-114 automated outcomes **PENDING**, no fabricated PASS. Old D-110 `OK (14 tests)` remains historical only. Full runbook & future actual result ledger: [ANDROID_D114_STAGE2_QA_2026_10_25.md](ANDROID_D114_STAGE2_QA_2026_10_25.md).

# Atelier Time — ordered QA 3→4: unexpected-stop recovery, then Android instrumentation

## 2026-10-25 — STABLE 5 APPROVED AND FROZEN: D-114

**User decision:** After personally testing D-114 on their Android phone, user explicitly stated: "הכל עובד , זאת גרסה יציבה 5" (everything works; this is Stable 5). This supersedes the previous Stage 1 acceptance-only status. The user accepted the complete D-114 Run finish-time feature, including live calculated end, user opt-in hours/minutes wheels, hard red-boundary stop and explanation when attempted selection is infeasible.

- **Stable 5 source snapshot:** `stable/android-d114` at exact commit **`34a692a98282944a76e314efcc228dac3bd072ff`**, created from the development branch HEAD *before* this documentation update. Readback from GitHub confirms the branch ref, `versionName = "D-114"` and `versionCode = 9`. **Treat this branch and commit as immutable; never develop on it, force-push it or merge further changes into it.**
- **Stable 4 remains untouched:** `stable/android-d108` is still at **`02beb0af46719301cccb72ad4698a337ab416d73`**. No Play Store, production distribution or signing is implied by Stable 5.
- The user previously supplied **D-114 on-phone Gradle `:app:assembleDebug` BUILD SUCCESSFUL in 11s**, resulting in a 12M debug APK in Downloads. User confirms installed application works. **This is a user-accepted product milestone, NOT a claim that all D-114 automated tests have run.** A D-110 baseline AndroidTest run yielded `OK (14 tests)`, but this cannot be attributed to D-114.
- **Strict stage gate:** Stage 1 feature implementation and personal device validation are completed and accepted. User earlier instructed "between the stages request approval"; therefore **Stage 2 — upgrade and execute ALL feasible automated tests (JVM + AndroidTest Compose UI/device + safe ADB checks) — remains PENDING EXPLICIT USER APPROVAL.** Do not start tests or mutate source before asking. Existing `RunEndApprovalUiTest.kt` still references retired typed HH:mm controls and will need migration to D-114 wheel tags on the **development branch only** when authorized. Tests must use isolated preferences and avoid clearing saved Classes/Sounds.
- Future work stays on `android-native-kotlin-compose`; fork/branch from Stable 5 only for emergency patches, never edit the frozen snapshot. Handoff and current-status documentation must distinguish the accepted Stable 5 from the not-yet-run D-114 automated QA.

## 2026-10-25 — D-110 Stage 4 Android instrumentation: 14/14 DEVICE PASS

**DEVICE RESULT VERIFIED FROM USER TERMUX SCREENSHOT (2026-10-25, ~18:08 Israel):** On Samsung Galaxy SM-S921B (ADB transport `10.0.0.6:34545`, `device`), the previously assembled 1.1 MB AndroidTest APK was installed successfully (`adb install -t -r ~/storage/downloads/Atelier-Time-D110-Tests.apk` → `Success`). The user executed:

```bash
adb shell am instrument -w com.ateliertime.android.test/androidx.test.runner.AndroidJUnitRunner
```

**Actual AndroidJUnitRunner output:**

```text
com.ateliertime.android.AndroidPresetStoreTest:.......
com.ateliertime.android.MelodyPreferencesTest:......
com.ateliertime.android.StaticAudioTrackLifecycleTest:.

Time: 0.295

OK (14 tests)
```

**Acceptance:** Stage 4 connected Android instrumentation **PASS: 14 / 14**, zero observed failures (7 preset storage, 6 melody preferences, 1 silent AudioTrack lifecycle); actual phone execution, not merely compilation. No app source edit, no replacement of target D-110 regular APK, no `pm clear` / `adb uninstall` or intentional modification of real saved Classes/Sound; tests used isolated randomized preference namespaces. Stable 4 D-108 frozen and D-110 remains development/approved-on-phone, **not** implicitly promoted to Stable 5. **Out of scope and still not device-verified:** active physical Class ±120 s wall-clock adjustment, full 3-hour endurance/Doze/audio, and unexpected OS process-reclaim notification cleanup; the approved user-closure/reboot semantics remain unchanged. After QA, turn off Developer options → Wireless debugging; users can reopen Atelier Time normally. Earlier pre-PASS entries in these docs are chronological historical progress, superseded by this result.

## 2026-10-25 — AndroidTest test-only package installation PASS; actual execution pending

- User screenshot in Termux confirms `ls -lh ~/storage/downloads/Atelier-Time-D110-Tests.apk` reports **1.1 MB**, readable through existing shared-storage link (the earlier optional `termux-setup-storage` interaction aborted with directory left intact; no need to repeat it).
- `adb install -t -r ~/storage/downloads/Atelier-Time-D110-Tests.apk` output shows `Performing Streamed Install` and **`Success`**. This is **TEST APK INSTALL PASS**, not instrumentation run PASS.
- ADB had already shown **`10.0.0.6:34545 ... device ... model:SM_S921B`**, so execute next with installed test package `com.ateliertime.android.test` and project-specified `androidx.test.runner.AndroidJUnitRunner`:
  `adb shell am instrument -w com.ateliertime.android.test/androidx.test.runner.AndroidJUnitRunner`
- Expected passing outcome: `OK (14 tests)`; capture actual device output/failures instead of asserting result prematurely. User should leave Atelier Time closed, no active Class. No regular app APK reinstall, data clear, or Stable 4 edits.

## 2026-10-25 — AndroidTest APK successfully copied to Downloads

- 2026-10-25 — **Test APK copied to shared Downloads, user screenshot PASS.** In Code on the Go terminal under `/storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native`, the user ran `APK="$(find app/build/outputs/apk/androidTest -type f -name '*.apk' -print -quit)"`, then guarded `cp "$APK" /storage/emulated/0/Download/Atelier-Time-D110-Tests.apk`. The resulting `ls -lh` shows a **1.1 MB APK at** `/storage/emulated/0/Download/Atelier-Time-D110-Tests.apk`, dated Oct 25 18:03. The copy step passed without rebuilding. **Next:** in Termux grant shared-storage access via `termux-setup-storage`, then `adb install -t -r ~/storage/downloads/Atelier-Time-D110-Tests.apk`. Android ADB transport confirmed via `adb devices -l` on device `10.0.0.6:34545` Samsung SM-S921B. Installing just the AndroidTest APK must not uninstall or clear target app data; make sure no Class active before instrumenting. **Install and all 14 Android instrumentation tests remain NOT YET VERIFIED**.

## 2026-10-25 — Wireless ADB transport CONFIRMED on user's phone

- User supplied actual Termux `adb devices -l` output: `10.0.0.6:34545         device product:e1sxeea model:SM_S921B device:e1s transport_id:1`. This proves connected/authorized Samsung SM-S921B, **ADB device-state PASS**. Previous entries below are historical and superseded as to current connection status.
- Next action: in Code on the Go checkout find actual existing `app/build/outputs/apk/androidTest/**/app-*-androidTest.apk` (path needs local confirmation), copy **test APK only** to `/storage/emulated/0/Download/Atelier-Time-D110-Tests.apk` without rebuilding or altering installed app. From Termux, verify accessibility and run `adb install -t -r` for **the test APK**, verify registered instrumentation runner, execute `adb shell am instrument -w com.ateliertime.android.test/androidx.test.runner.AndroidJUnitRunner` only when no Class is running. Capture results; **14 instrumentation tests are still NOT RUN and are NOT marked PASS**.
- No changes to D-110 app code, frozen Stable 4, or saved Classes/Sound, and no force-stop/uninstall/data clear commands issued.

## 2026-10-25 — User reports successful external-IP ADB pairing/connection

- User message: **"הצליח מחובר"** ("Succeeded, connected") after switching from localhost attempts to the phone's external/Wi-Fi IP route in Termux with Samsung split-screen for Wireless debugging.
- **PAIRING/CONNECTION USER-REPORTED SUCCESS**. A device-state readout `adb devices -l` has not yet been supplied, so **`device` authorization not independently screenshot-verified** and **14 Android instrumentation tests still NOT RUN**.
- Next safe gate: `adb devices -l`, verify connected phone in `device` state. Next inspect the *existing* Android test APK output at `android-native/app/build/outputs/apk/androidTest/app-debug-androidTest.apk`; do not assume it exists at another path or install until exact artifact path is checked. On the connected device, install the test-only package preserving already-installed D-110; launch its declared `androidx.test.runner.AndroidJUnitRunner` to collect real Android `INSTRUMENTATION_STATUS` results.
- **Protection:** app data in saved Classes/Sound remains untouched intentionally by existing test namespaces; nevertheless verify tests and package names, and do not launch instrumentation during a live Class because the runner may restart the target app process. No Android app source, Gradle version, stable branches or APK rebuilt by this note. Turn off Wireless debugging after tests.

## 2026-10-25 — Correct PIN still fails: use on-device loopback diagnostic

- User explicitly reports correct Wireless debugging pairing PIN but repeated pairing failures (`protocol fault (couldn't read status message): Success`). **Do not assume user typo**: a Termux issue publicly reports the exact failure, though a specific older report was closed as likely mistyped PIN: https://github.com/termux/termux-packages/issues/24984.
- Termux ADB client/server is already verified working, and both target and Termux are **the same phone**. Prefer localhost (`127.0.0.1`) to avoid Wi-Fi/vpn/routing variations, following documented same-device Termux ADB instructions: https://gist.github.com/kairusds/1d4e32d3cf0d6ca44dc126c1a383a48d .
- **First attempt without re-pairing:** From Wireless debugging *main screen*, get the current **connection port** and try `adb connect 127.0.0.1:<CONNECTION_PORT>`, then `adb devices -l`; failed pairing attempts might not have enrolled a key, so this may return authentication failure, which is useful evidence.
- If not connected: open *Pair device with pairing code* and keep its popup visible using Samsung split-screen. Use the **new pairing port**, not the connection port: `adb pair 127.0.0.1:<PAIRING_PORT>` and privately enter the code at the interactive prompt. Pairing and connection use different ports; do not send PINs, paste PINs into shell arguments/history or screenshot codes. Pair only in a trusted Wi-Fi context.
- If it still fails, STOP repeating guessing: record the exact error and `adb server-status` output, then choose an alternative Android instrumented-test transport. **No automated device tests executed so far**, no changes to installed D-110, no need to modify CoGo SDK or saved Classes.

## QA Stage 4 — Termux Wireless ADB pairing failed; corrective procedure (user screenshot, 2026-10-25)

- `adb pair IP:PORT` was initially entered with the literal placeholder instead of a real endpoint; a later proper-shaped `adb pair <actual-host>:<port>` attempt ended with `error: protocol fault (couldn't read status message): Success`. This does **NOT** confirm successful pairing. The screenshot shows at least one interactive PIN entry with fewer than six digits.
- Official Android ADB Wi-Fi pairing codes are six digits including leading zeros; the pairing port exists **only while the Pair device with pairing code dialog remains open** and is distinct from the connection port on the Wireless debugging main page. Generate a fresh code/port, keep the pairing popup alive in Samsung split-screen, enter all six digits into Termux privately, and look specifically for `Successfully paired`. Do not reuse or disclose screenshots containing one-time pairing codes.
- Termux documented an identical protocol-fault error in github.com/termux/termux-packages/issues/24984; issue author attributed their repro to a likely mistyped PIN. Thus verify endpoint and six-digit input before investigating binary versions. If `Successfully paired`, next `adb connect` to the **different** Wireless debugging connection port and require `adb devices -l` in `device` state.
- **State: PAIRING NOT VERIFIED; 14 instrumentation methods still NOT RUN.** No app code, QA test APK, installed D-110, or saved data modified by this troubleshooting.

## QA Stage 4 — Termux ADB daemon running (2026-10-25, user screenshot)

- User executed `adb start-server && adb devices -l` in Termux.
- Output explicitly shows `* daemon started successfully`, followed by `List of devices attached` with **no devices listed**.
- Status: **ADB CLIENT/SERVER PASS; DEVICE PAIRING AND CONNECTION PENDING**. Blank list before Wireless debugging pairing is expected and is not an Atelier Time test failure. No claim of instrumentation execution.
- Next safe action: Android Settings > Developer options > Wireless debugging > Pair device with pairing code, keeping transient pairing code private. In Termux run `adb pair <phone-IP>:<pairing-port>`; enter PIN in Termux only; after successful pairing run `adb connect <phone-IP>:<connection-port>` with the distinct connection port shown on Wireless debugging main screen, then `adb devices -l` and require a device in `device` state before any `am instrument` command.
- Do not send one-time PIN, disable Wireless debugging again after device QA; do not touch the CoGo SDK, installed D-110 or saved Classes.

## QA Stage 4 — Termux android-tools installation verified

- 2026-10-25 — **Termux ADB tool successfully installed, verified by user screenshots.** `pkg install -y android-tools && adb version` installed `android-tools 37.0.0p1-3` (plus `fmt` and `libprotobuf`), and reported `Android Debug Bridge version 1.0.41`, package version `37.0.0p1-android-tools`, binary `/data/data/com.termux/files/usr/bin/adb`, Android ARM64. Earlier `cannot connect to daemon at tcp:5037: Connection refused` appeared during setup, but `adb version` worked. **ADB server startup, Wireless Debugging pairing, device connection, and instrumentation execution NOT YET VERIFIED**; no test APK installed by this action. Next safe step: `adb start-server && adb devices -l`, then pair with Settings > Developer options > Wireless debugging on the same device, avoiding sharing one-time pairing code. Do not change CoGo SDK or installed D-110, and do not run tests during a real Class.

## 2026-10-25 — QA Stage 4 ADB availability: phone screenshot confirmed

- The user executed a read-only check in Code on the Go terminal, from the existing `Atelier-Time/android-native` checkout: `find "$HOME" "$ANDROID_HOME" -type f -name adb -size +0c 2>/dev/null | head -n 10`. **Output: no nonempty ADB candidates** under either search root (not a system-wide filesystem proof).
- `ls -l "$ANDROID_HOME/platform-tools/adb"` shows a **0-byte regular file** with mode `-rw-------`; this is not an executable binary. Do **not** apply chmod or overwrite the CoGo SDK.
- The prior screenshot proved `:app:assembleDebugAndroidTest` **BUILD SUCCESSFUL in 34s**, 50 tasks; this is a compilation/package result only. **None of the 14 Android instrumentation tests has run; no instrumented-test PASS has been claimed**.
- Current authoritative Android guidance runs instrumentation through a connected target (Gradle `connectedDebugAndroidTest` or `adb shell am instrument -w`). Official CoGo debugger documentation describes a debugger pairing workflow but **does not demonstrate an instrumentation launcher for external projects**. Do not conflate debugger pairing with AndroidJUnitRunner execution.
- **Non-invasive supported option:** use a **separate official Termux installation** (F-Droid source) with the `android-tools` package, pair its ADB client via Android's Wireless debugging on the **same phone** (Android 11+ and Wi-Fi), then use ADB to install the already-built **test APK only** and run its JUnit instrumentation runner, preserving the existing D-110 app and CoGo managed SDK. This path is reported as a documented option, **not yet installed, paired or executed**. The user should not post transient wireless-pairing codes/screenshots with pairing PIN. Avoid untrusted adb binaries and do not alter developer security settings beyond user-approved wireless debugging; switch it off afterward.
- **User safety gate:** perform only while Atelier Time has no running Class. The 7 preset and 6 melody tests use separate random test preference namespaces, but a connected instrumentation run still briefly stops/restarts the application process; never uninstall or clear actual user data.
- Other standard option if the separate-phone client is unwanted: a trusted PC with official Android SDK Platform Tools connected to the phone. This is optional, not a mandate to change the approved phone-first development setup.

References: https://developer.android.com/studio/test/command-line ; https://developer.android.com/tools/adb ; https://github.com/termux/termux-app ; https://f-droid.org/packages/com.termux/ ; https://appdevforall.org/code-on-the-go-release-notes-26-40/

## Stage 4 progress — real phone AndroidTest assemble PASS

**2026-10-25 — Stage 4 AndroidTest APK COMPILE/PACKAGE PASS on user's CoGo phone (screenshot):** `:app:compileDebugAndroidTestKotlin`, `:app:packageDebugAndroidTest`, `:app:assembleDebugAndroidTest` completed, terminal shows **BUILD SUCCESSFUL in 34s; 50 actionable tasks (30 executed, 20 up-to-date)**. These Gradle tasks assemble the Android instrumentation package but DO **NOT** invoke device tests; the **14 `androidTest` source methods remain NOT RUN**. The previously observed `$ANDROID_HOME/platform-tools/adb` is a 0-byte, non-executable placeholder; `adb` not found on PATH. Before requesting `:app:connectedDebugAndroidTest`, discover a supported usable ADB/device path rather than modify CoGo SDK, add untrusted binaries, install test package by hand, or infer a PASS. No new regular app build/update, no user-data changes, D-110 still approved and D-108 Stable 4 frozen.

**Next action:** investigate supported test device transport. A built test APK alone is insufficient to assert instrumentation execution.

## Authoritative correction — user-defined end-of-run semantics

**USER-APPROVED INTERRUPTION RULE (2026-10-25, clarified during active Timer):** If Atelier Time is actually **closed/terminated while a Class Timer is running**, or the **phone shuts down/reboots while it runs**, that Class run ends. **On a subsequent app launch the Timer MUST NOT remain running, automatically resume, restore elapsed progress, or replay missed audio cues.** Open at the saved-Class library / idle Timer; preserved saved Classes and Sound preferences remain available. This does **NOT** change the already accepted behavior when the user merely presses Android **Home**, switches apps or **locks the screen** without terminating the app: Timer/audio/background notification continue as previously approved. Existing Back / Recents-swipe closure behavior remains frozen. Do not build persistent active-run recovery features or demand a reboot-during-run test to prove a feature the user explicitly rejects. Source audit of D-110 already found no saved active Timer state; no user-device crash or running-reboot test has been executed, and immediate cleanup of stale OS notifications after unexpected **process** death remains an independent reliability check. Prior ordinary reboot persistence of stored Classes/Sound remains PASS. Proceed to Stage 4 Android automated tests.

**Stage 3 disposition:** user product requirement RESOLVED: termination means no active run on restart. D-110 code/source audit CONFORMS to no automatic resume. Unexpected process-death/notification behavior has not been verified on a device; do not label the unrun reboot-during-active-Class or OS reclaim tests PASS. The earlier optional Stage 3 phone reboot test below is **NOT a prerequisite to Stage 4** and should not be imposed merely to test unwanted auto-resume.

**Date:** 2026-10-25. **Installed baseline:** D-110, user states "הכל תקין". **Stable rollback:** frozen D-108 `stable/android-d108`. **Changes in this checkpoint:** documentation only, no app code edits, no APK, no settings/data edits.

## Stage 3 first: active-Class recovery and process-reclaim audit

### Direct code findings (D-110 development branch)

1. `SessionEditorScreen.kt`: `playingSession`, `timerView`, `launchRunRequested` are Compose `remember` state; `MonotonicSessionTimer` and `ClassClockTracker` also live only inside the UI process. There is **no persistent active-Class run checkpoint and no restore-on-process-launch path**. Saved Class library and Sound choices are stored separately in dedicated persistence classes. **Expected from code after process death or reboot:** any in-progress Class **will not automatically resume**. Starting a saved Class again is a **new** run. This is a code-level conclusion, **not a phone-tested recovery PASS**.
2. `ClassNotificationTaskService.kt`: the service only mirrors notification status, does not own clock/audio, and uses `START_NOT_STICKY`. On Recents swipe it calls `onTaskRemoved` to remove notification, as already user accepted in D-108. On unexpected full-process kill `onDestroy` is **not guaranteed**, so immediate notification removal is **not guaranteed**. The notification itself has `setTimeoutAfter(classRemainingSeconds * 1000 + 2000)` from `ActiveClassStatusNotification.kt`, so it could remain stale until the original deadline if the task is not reopened; actual notification lifecycle depends on OS.
3. `MainActivity.onCreate`: after a genuinely new launch (`savedInstanceState == null`) it dismisses any stale prior notification. No `BOOT_COMPLETED` receiver is declared in `AndroidManifest.xml`, so reboot does not trigger Class recovery.
4. **Android OS semantics:** OS memory reclaim, user Back, Recents swipe, Force stop and phone reboot are distinct transitions. Do not count existing Back/Swipe PASS or prior idle-phone reboot retention PASS as an OS-process-reclaim PASS.

### Status of Stage 3

| Gate | Evidence | Status |
|---|---|---|
| 3a: inspect process-death and reboot recovery capability | inspected D-110 Android source | SOURCE AUDIT COMPLETE; automatic Class resume NOT IMPLEMENTED |
| 3b: phone reboot while a test Class is active | user has not yet executed this distinct scenario | DEVICE TEST OPEN |
| 3c: actual unexpected Android OS process kill (not Back/Swipe/Force stop) | no adb/system-level device transport in CoGo; cannot remotely trigger | DEVICE TEST BLOCKED |
| 3d: no ghost notification, saved Classes and Sound intact | source predicts cleanup on new Activity launch and existing prefs survive normal reboot, but needs active-run phone evidence | DEVICE TEST OPEN |

**Safe user-executed 3b check:** Only when no real lesson is needed, start a saved Class from Timer, note it is running, **restart the phone normally without pressing Back/Stop or swiping the app away**; when Android finishes rebooting, look at notification shade *before opening Atelier Time* and note any Atelier Time running-status notification; then open Atelier Time > Timer and note whether the previous run automatically resumed or returned to the saved Class list, and confirm the saved Class and Sound settings remain. Do not uninstall, clear storage or create/delete personal presets. This deliberately interrupts the test Class and should not be done during a real lesson. After evidence, record observed phone behavior versus these code-level expectations; do not declare unrequested auto-resume requirement to be implemented without explicit product approval.

**Proposed minimal policy, pending user choice if behavior needs development:** fail safely on unexpected full-process termination: never auto-play expired cues or silently restart a Class; on next genuine launch, return to saved Classes and clear stale status; keep saved data intact. A true "resume active Class after process death/reboot" feature would require new persistent in-progress run data and explicit user decision; do not invent this feature or classify its absence as a software test PASS/FAIL without agreed requirement.

## Stage 4 only after Stage 3 user device check: Android instrumentation

**Existing source:** 14 Android `@Test` methods: 7 isolated preset persistence, 6 isolated melody preferences, 1 silent real `AudioTrack` lifecycle. The suites were source-audited; none is known to have executed on the phone.

**Current CoGo blocker proven by screenshot:** `adb` absent from PATH; `$ANDROID_HOME/platform-tools/adb` is a **0-byte non-executable placeholder**, not a working adb. Therefore `:app:connectedDebugAndroidTest` has NO verified connected-device transport and must not be claimed as an executed test. Do not repair via `chmod` or overwrite CoGo's managed SDK; do not install untrusted ADB binaries.

**Next non-destructive Stage 4 gate:** inspect available supported Android-on-device debugger routes (a valid platform `adb` binary or CoGo device runner). If no actual device runner exists, `:app:assembleDebugAndroidTest` can independently compile the instrumentation APK, but this is **compile-only**, not Android test execution. It does not need to replace the installed D-110 app. Actual connected runs need a verified Android target, may install a separate instrumentation APK; verify unique test-only preference namespaces and cleanup before running.

**No phone test output exists for 3b/3c/4; do not claim PASS.**
