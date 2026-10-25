# Atelier Time — ordered QA 3→4: unexpected-stop recovery, then Android instrumentation

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
