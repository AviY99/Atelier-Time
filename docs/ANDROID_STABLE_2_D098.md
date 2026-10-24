# Atelier Time — Android Stable 2 (D-098)

**Declared and user-accepted:** 2026-10-24. **Approval:** User explicitly reported "הכל עובד" after the D-098 update-install, and declared this the **second stable version with the design aspects closed**.

## Frozen source checkpoint

- **Name:** Atelier Time Android Stable 2
- **Frozen checkpoint branch:** [`stable/android-d098`](https://github.com/AviY99/Atelier-Time/tree/stable/android-d098).
- **Exact source commit:** [`6d5cbab6818cae288de8367849f8d3ea53f4739f`](https://github.com/AviY99/Atelier-Time/commit/6d5cbab6818cae288de8367849f8d3ea53f4739f).
- **Original working branch:** `android-native-kotlin-compose`; [PR #3](https://github.com/AviY99/Atelier-Time/pull/3) remains **OPEN / DRAFT / UNMERGED** as of checkpoint verification.
- **Earlier Stable 1 stays unchanged:** `stable/android-d086` at `d0a03d2792cc324aeeba39b6327136ba6d37fd0c`.
- **Technology:** native Kotlin + Jetpack Compose, developed locally on Android via Code on the Go with Gradle 9.6.1 and the established ARM64 AAPT2 override. Pure Kotlin domain retained for later multiplatform reuse.

## Device/build acceptance and scope

- D-098 phone screenshot verified `BUILD SUCCESSFUL in 36s`, 42 actionable Gradle tasks (7 executed, 35 up-to-date), with `:app:testDebugUnitTest` and `:app:assembleDebug`, plus the debug APK copied to Downloads (12M). Exact individual JUnit case count and local checkout SHA were not shown in that screenshot.
- The user subsequently reported **"הכל עובד"** and explicitly accepted the second stable build after update-install. This closes the D-098 **manual phone acceptance** for the smaller and denser Editor chronological timeline text list (`bodyMedium`, 2dp between phase rows), and affirms the previously accepted D-097 visual UI behavior.
- **Design closed / frozen by user decision:** five visible LTR tabs (Editor | Timer | Sound | Design | About); approved Studio Linen Day and Night Blue palette, sun/moon cards and slider-only Auto; Day on left / Night on right within the slider preview, with actual clock-based Auto theme policy preserved; approved typography and compact clock rows; Tahini elapsed-motion visuals (elapsed bars fill for Class, Session and Break; Session background fills, Break background drains); English About wording and hierarchy; compact Editor activities-and-times list. No further visual redesign or theme alterations without a NEW explicit user requirement.
- All approved timer timing, fixed Class deadline, audio cues and persistent offline Classes remain preserved. The app is a live atelier Class timer, not primarily a planning app.

## Boundaries — what “stable” does NOT claim

This is **a user-accepted Android debug-APK functional/design checkpoint**, NOT a signed production or Play Store release. It does NOT establish passed connected Android instrumentation tests, lock-screen/background/Doze cue reliability, exhaustive persistence-corruption/reboot tests, accessibility/dynamic-type coverage, battery/long-duration tests, or iOS/desktop packaging. Keep these as separate engineering/QA backlog items, not as design tasks to reopen.

## Change control

Treat `stable/android-d098` as an **immutable rollback checkpoint**. Do not rewrite/move it or the D-086 stable branch; do not merge PR #3, add paid GitHub Actions, change signing/versionName, distribute a production artifact, or refactor architecture without user approval. Future improvements belong on separate development commits/branches and must not alter the accepted design implicitly. No new APK build or install is required to record this acceptance.

## Source of truth

Consult [HANDOFF.md](HANDOFF.md), [DECISIONS.md](DECISIONS.md), [TEST_PLAN.md](TEST_PLAN.md), [WORK_LOG.md](WORK_LOG.md), [ROADMAP.md](ROADMAP.md) and [CHANGELOG](../CHANGELOG.md). Historical older D-097/D-098 "pending" notes are superseded by this accepted Stable 2 checkpoint.
