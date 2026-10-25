# Atelier Time — Android Stable 4 (D-108 / accepted 2026-10-25)

**Authoritative status:** USER-APPROVED ANDROID STABLE 4. On 2026-10-25 user confirmed D-108 is working ("הכל תקין כעת"), and then explicitly declared "מבחינתי זאת גרסה 4 יציבה" and requested project handoff to a new conversation. This is an **accepted, frozen Android debug-build checkpoint**, NOT a store-ready or signed Production release. The user did not request any additional code or phone testing at this handoff.

## Exact source snapshot, rollback and branch policy

- GitHub repository: [`AviY99/Atelier-Time`](https://github.com/AviY99/Atelier-Time).
- **Stable 4 immutable rollback branch:** [`stable/android-d108`](https://github.com/AviY99/Atelier-Time/tree/stable/android-d108).
- **Exact frozen source commit:** [`02beb0af46719301cccb72ad4698a337ab416d73`](https://github.com/AviY99/Atelier-Time/commit/02beb0af46719301cccb72ad4698a337ab416d73).
- Active Android development branch: [`android-native-kotlin-compose`](https://github.com/AviY99/Atelier-Time/tree/android-native-kotlin-compose).
- Existing [PR #3](https://github.com/AviY99/Atelier-Time/pull/3) is **Open/Draft/unmerged** at this checkpoint. Do not merge or initiate paid GitHub Actions without explicit permission.
- Previous rollback points remain unchanged: Stable 1 `stable/android-d086`; Stable 2 `stable/android-d098` (`6d5cbab6818cae288de8367849f8d3ea53f4739f`); Stable 3 `stable/android-d102` (`78878801ea515deccc4e64cdc62b2b3db595769a`). Do NOT overwrite or move these refs.

## What is accepted and closed

1. **Approved product UI/design:** Editor, Timer, Sound, Design and About; Studio Linen (Day) / Night Blue (Night), motion, responsive components, existing approved visual layout. Do not redesign closed screens.
2. **Class editor and local offline saved Classes:** deterministic schedule engine, openings/groups/regular and special breaks, summary and final-session compensation policies. Existing saved Classes, presets and approved endings must survive all updates. No uninstall or data clearing.
3. **Timer:** running/paused/resumed, Restart Session/Break, Next within current Class, Exit, originally approved Class finish target and safe final two regular sessions compensation. User previously accepted ordinary foreground A/B/C transition timing and audio; do not redundantly demand a 3-minute cue test.
4. **Sounds (Stable 3 closed):** library selections Atelier Time, Android OS sounds, user audio; independently saved A/B/C vs Same sound for all; shared 5/10s; Separate A/B 5/10s and C 5–60s; audio preview, private persistence, source guards scoped to active mode. Approved, not open for redesign.
5. **Android background/notifications:** user observed time and cues continue with another app frontmost, Home/square button, Back-as-background in older versions, and locked screen. D-107 added quiet Class notification with Running/Paused, phase/countdown and Class remaining, optional Android 13+ POST_NOTIFICATIONS. D-108 fixes **notification remains frozen after true application exit**:
   - **Home/square:** app/timer in background, notification stays current.
   - **Recent apps overview (three bars):** leaving task in overview/background keeps notification current.
   - **Back arrow:** app closes, notification is removed.
   - **Swipe app card away in Recents:** app closes, notification is removed.
   - **Exit / Class end:** notification is cleared.
   The user reported "הכל תקין כעת" for D-108 following the specific Back/Swipe regression checks.
6. **About versioning policy:** displayed text is the real **D-build ID**, not Stable ordinal. Frozen D-108 package has `versionName = "D-108"`, Android `versionCode = 4`; About directly displays the installed PackageManager versionName, so it should show **Version D-108**. On each NEXT development APK increment, update `versionName` to the **actual next coding iteration**, starting at **`"D-110"`** because **D-109 is this Stable 4 approval/handoff milestone**, and increment `versionCode` monotonically. Do not write `Version 4` in About simply because the stable milestone is 4.

## Evidence that does—and does not—exist

- **Real Samsung Android phone**, Code on the Go terminal: initial D-108 Gradle failed at `ClassNotificationTaskService.kt:35:17 'val' cannot be reassigned`; this was precisely corrected by `Intent.addFlags(...)` at source commit [`02beb0af`](https://github.com/AviY99/Atelier-Time/commit/02beb0af46719301cccb72ad4698a337ab416d73).
- Subsequent actual **`BUILD SUCCESSFUL in 35s`** for Gradle `:app:testDebugUnitTest :app:assembleDebug`: 42 actionable tasks, 9 executed / 33 up-to-date; terminal output showed ~12 MB `/storage/emulated/0/Download/Atelier-Time-D108.apk`, Oct 25 10:09.
- User then confirmed it now works, and explicitly declared Android Stable 4.
- This substantiates **Gradle JVM unit-test task and debug APK build PASS plus user-reported on-phone functional acceptance**. It **does not** demonstrate a full connected Android `:app:connectedDebugAndroidTest` run, isolated JUnit case counts, Play release signing, exact on-device Git SHA independently read, non-Samsung devices, unconditional OS process-kill/Doze/reboot survival, or Play acceptance of the FGS type.
- D-108 introduces a **notification-only Android foreground service** with `foregroundServiceType="specialUse"`, `Service.onTaskRemoved()`, and Back/Activity-finish handling. Its **Google Play specialUse eligibility and policy declarations remain OPEN**; this is NOT an assertion that the current implementation is store-approved.

## Boundaries and immediate continuation

**No additional build, install, code or design action is required to close Stable 4.** The new conversation must start from the [new-chat handoff](CHAT_HANDOFF_AFTER_STABLE_4_2026_10_25.md) and the current [HANDOFF.md](HANDOFF.md), treating historical "pending D-108 install" notes as superseded.

Remaining scope, ordered pragmatically:
1. **Android reliability and storage:** document field evidence already accepted; add independent tests for app lifecycle, Doze/OS memory reclaim, multi-hour Class timing/sounds and restore/backup/safe corrupt JSON; do not imply already accepted Back/Home cases are untested.
2. **Android connected instrumentation:** run existing `androidTest` methods on an actual Android device with actual test counts and isolate test data from personal Classes. Do not equate Gradle JVM test task with instrumentation.
3. **Notification service & Play compliance audit:** ensure `specialUse` FGS type is suitable under the eventual Play rules or adapt type with documented policy and Android verification; do not change approved user behavior.
4. **Accessibility, localization, battery, device coverage and regression testing** for a release candidate.
5. **Android release candidate/signing, package update, privacy and license work.** Debug APK is not the final signed store artifact.
6. **MANDATORY logo + app launcher icon + store graphics user-approval gate BEFORE publishing**: [ICON_LOGO_PRESTORE_GATE.md](ICON_LOGO_PRESTORE_GATE.md). No final brand/logo or adaptive icon has yet been approved/implemented. The D-107 monochrome clock icon is **notification-only**, NOT the launcher brand.
7. **Google Play listing/publishing only after all gates and explicit user authorization.** Later ports: iOS/iPadOS, Windows, macOS reusing the platform-independent Kotlin domain; no framework rewrite or proof-of-concept distraction.

## Engineering execution environment and safe build

Authorized stack: **Kotlin + Jetpack Compose + pure Kotlin domain**, Android-first **on the Android phone with Code on the Go**, Gradle **9.6.1**, `compileSdk 36`, `targetSdk 35`, `minSdk 26`. Keep portability for future platforms without rewriting accepted code.

Development work happens on `android-native-kotlin-compose`, NOT the frozen `stable/android-d108` rollback branch. For a **future authorized** development build, run from existing phone repository `android-native` folder:

```bash
git pull --ff-only &&
set -o pipefail &&
bash "$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle" :app:testDebugUnitTest :app:assembleDebug --no-daemon --console=plain "-Pandroid.aapt2FromMavenOverride=$ANDROID_HOME/build-tools/36.0.0/aapt2" 2>&1 | tee "$HOME/atelier-d110-build.log" &&
cp app/build/outputs/apk/debug/app-debug.apk /storage/emulated/0/Download/Atelier-Time-D110.apk &&
ls -lh /storage/emulated/0/Download/Atelier-Time-D110.apk
```

**This is an example for a future D-110 update, not an instruction to build now.** Set development Gradle versionName/versionCode to the actual next build identifier **before** issuing this command. Build and test on phone with native AAPT2 override; update-install without uninstall or data wipe. Present tool results and real on-phone screenshots as separate evidence. Keep all canonical docs current: HANDOFF, DECISIONS, TEST_PLAN, ROADMAP, WORK_LOG, CHANGELOG.

## New-chat protocol

The next assistant should read **CHAT_HANDOFF_AFTER_STABLE_4_2026_10_25.md first**, verify this stable branch/commit and branch state, then continue with the next OPEN QA gate. **Do not restart from Stage 1, repitch Flutter/Dart, redo approved Sound/Design, recreate proof-of-concept, demand repeated A/B/C tests or request already-known specifications.** Past docs contain historical decisions that were later superseded. Ask for approval only for substantive new user-facing features or release/store actions, not for previously agreed process or routine bug/QA work. Close each increment with exact source commit, tests run/results, on-phone acceptance and documentation.

**Stable 4 definition:** source checkpoint + validated phone Gradle debug build + user's explicit functional approval, not a claim of completed production release QA.
