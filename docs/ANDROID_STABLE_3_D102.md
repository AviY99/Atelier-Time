# Atelier Time — Android Stable 3 (D-102; accepted D-103)

> **D-106 — CURRENT VERSION-DISPLAY POLICY / FOREGROUND TIMER TEST ACCEPTANCE (2026-10-24):** User replaces earlier About `Version 3` request: **About must show the real development build identifier, e.g. `Version D-104` for this D-104 APK generation; every future build must use its corresponding D-number**. On native DEVELOPMENT branch only, Android Gradle `versionName = "D-104"` (numeric package `versionCode = 2` stays independent), and `AtelierAboutPanel()` now shows installed `PackageManager.versionName` verbatim instead of extracting digits. **This change was SOURCE COMMITTED AFTER the screenshot-confirmed D-104 34s build; the already built/copied D-104 APK still has `versionName = "3"` and is NOT retroactively updated. No new build/install has occurred.** User also confirms the audible A/B/C cues and normal time/phase transitions were already checked and work. **Do not require another redundant 3-minute foreground A/B/C check** for the unchanged Timer engine. This is prior user-reported manual acceptance, NOT new D-104 APK install confirmation or device lock/background/Doze reliability. Next testing priority: locked screen/background/Doze and related notification/audio survival, plus separate persistence and instrumentation checks; saved Classes unchanged. Prior `Version 3` requirement in historical docs is SUPERSEDED; frozen Stable 3 `stable/android-d102` untouched, no PR merge or paid CI. Code and decision: `android-native/app/build.gradle.kts` + `android-native/app/src/main/java/com/ateliertime/android/AtelierDesign.kt`.

**User acceptance / date:** 2026-10-24. User explicitly reports: "ההתקנה לא התקדמה עקב בעיית זכרון בנייד , נפתרה הבעיה ונבדק והכל עובד" and declares this **Stable 3** closing the **Sound** feature/design scope. This is user-reported **on-phone D-102 installation and focused functional acceptance**.

## Frozen Git checkpoint

- **Name:** Atelier Time Android Stable 3.
- **Immutable rollback branch:** [`stable/android-d102`](https://github.com/AviY99/Atelier-Time/tree/stable/android-d102).
- **Exact frozen Android source:** [`78878801ea515deccc4e64cdc62b2b3db595769a`](https://github.com/AviY99/Atelier-Time/commit/78878801ea515deccc4e64cdc62b2b3db595769a).
- **Superseded rollback anchors remain frozen:** Stable 1 `stable/android-d086`; Stable 2 `stable/android-d098` at `6d5cbab6818cae288de8367849f8d3ea53f4739f`.
- **Development:** `android-native-kotlin-compose`; [PR #3](https://github.com/AviY99/Atelier-Time/pull/3) is still Open/Draft/unmerged at verification. Do not auto-merge it or introduce billable Actions.

## Verified evidence and resolution

1. **Build evidence:** user terminal screenshot shows `BUILD SUCCESSFUL in 29s`, 42 Gradle tasks (8 executed, 34 up-to-date), including `:app:testDebugUnitTest` and `:app:assembleDebug`. The 12M APK named `Atelier-Time-D102.apk` was copied to Downloads (Oct 24 20:39). That screenshot alone does not prove individual JUnit test counts, connected Android instrumentation runs, or the local source SHA.
2. **Installation failure was NOT a source/compile/signing bug:** the phone had insufficient free storage; user resolved the memory/storage condition, installed and tested successfully. Earlier "App not installed" was not evidence of an actual signing mismatch. No APK rebuild, signature change, uninstall or data wipe was required by the reported resolution.
3. **Functional acceptance:** "הכל עובד" after D-102 installed. This closes D-102 **active-only audio source deselection guard** (Shared: only one sound active; Separate: A/B/C active; dormant mode selections preserved and needed sources reenabled upon return) and retains accepted D-100/D-101 choices.
4. **Sound feature scope closed:** three enabled sound-library choices (Atelier Time default, native OS picker, user audio/document picker), compact source checkboxes; single shared A/B/C sound with 5/10-second duration, or independent A and B 5/10-second selections plus C 5–60s integer slider; preview, chosen cue routing, persistent private sound preferences, and safeguards against disabling any currently active library. No more Sound redesign without explicit new instruction.
5. **Design scope:** previously closed under Stable 2; remains closed. Native Kotlin + Jetpack Compose on-device workflow and independent Kotlin domain remain binding.

## One explicit NEXT BUILD requirement — About Version 3

**User requests:** "חשוב ב about הבא לעדכן version ל 3" — update the displayed **About → Version** to **3 on the next Android APK update/build**, not by rewriting the frozen accepted D-102 checkpoint.

**Verified implementation dependency:** `AtelierAboutPanel()` in `android-native/app/src/main/java/com/ateliertime/android/AtelierDesign.kt` reads Android's installed `versionName` from `PackageManager` and displays the numeric portion. As of the D-102 checkpoint, `android-native/app/build.gradle.kts` has `versionName = "0.1.0-dev"` and `versionCode = 1`; therefore changing text only in the About UI is the WRONG way to satisfy this. At the **next user-authorized new APK increment**, update the **versionName source of truth to `"3"`** and evaluate versionCode increment for a normal upgrade package (without changing signing identity). Check installed About reads precisely `Version 3`; document the exact versionCode used and update-install preserving private data. **This About/versionName change is explicitly PENDING and is NOT included in frozen Stable 3.** Do not rebuild solely to rewrite the already accepted APK unless the user requests immediate delivery.

## Remaining engineering work (outside frozen design and Sound acceptance)

- Android connected instrumentation tests (written test sources, no confirmed connected device run).
- Foreground service, locked/background/Doze cue reliability, interruptions, timing and multi-hour accuracy.
- Offline/JSON corrupt data recovery, restart/reboot stress, accessibility/dynamic type.
- Formal release signing, packaging and privacy/licensing/release procedures.
- Later OS-specific Kotlin/Compose Multiplatform work for iOS/iPadOS, Windows and macOS. No alternate framework or toy proof-of-concept required.

## Change control and current next action

Treat `stable/android-d102` as an immutable exact-code checkpoint. Do not move that branch, alter frozen prior checkpoints, merge PR #3, or change the approved app design, scheduling or audio semantics without explicit user direction. No further phone build is needed to close D-102. At the **next development increment**, implement and verify the About **Version 3** requirement and ensure all changes remain after this frozen Stable 3 point.

Canonical project status: [HANDOFF.md](HANDOFF.md), [DECISIONS.md](DECISIONS.md), [TEST_PLAN.md](TEST_PLAN.md), [ROADMAP.md](ROADMAP.md), [WORK_LOG.md](WORK_LOG.md).
