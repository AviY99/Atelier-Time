# Stable 9 — D-126: all scheduled automated QA gates passed

**Release checkpoint designation date:** 2026-10-28.

**Authoritative user statement:** "זאת גרסה 9 יציבה בה הושלמו בדיקות" — the user explicitly promotes the *already-installed and accepted* D-126 application from the previous Stable 8 to **Stable 9**, based on the completion of automated testing and preservation checks.

## Identity and scope

- **Current stable checkpoint:** `Stable 9`, Git branch `stable/android-d126-qa9-tests-complete`. This is a **QA certification milestone**, not a different compiled Android application.
- **Android binary remains:** `versionName="D-126"`, `versionCode=20`, package `com.ateliertime.android`. **No new APK build/install, Android version bump, or saved data mutation is required.**
- **Installed production source build commit (verified on phone manifest):** `b2d47ca32c2f470d38e556ea7a571afaa89400e3`. D-126 consolidated logic + diagnostics remained unchanged from this source.
- **Corrected instrumentation-test-only source build commit (verified in actual phone runner log):** `07816e22014e2ff6593757cbd33879344ada1e80`. Its differences from the already-approved Stable 8 are confined to AndroidTest test classes; no `app/src/main` or Gradle build identity changes.
- **Immutable earlier recovery points:** `stable/android-d126-qa8-logic-diagnostics` (user-accepted Stable 8) and `stable/android-d124-qa8` (older original Stable 8 logic baseline). Preserve both untouched.
- **Development branch:** `android-native-kotlin-compose`. Future work uses new source/version milestones only when actually required.

## Capabilities covered

- Consolidated unified runtime Timer logic from D-124: `ConstraintFuturePlanner` physical finish, protected historical timeline, legal future reallocations, fixed Summary and validated Session/Break constraints.
- D-126 Timer interface: operational clocks, phase display, timeline and controls without internal planning wall-of-text.
- Complete scrollable chronological Run action history, approximately five rows visible, scroll back through all actions to Start.
- App-private per-Run `.jsonl` forensic diagnostics including Editor rules, approved timeline, time/action/reflow details, sound requests/errors, export and explicit delete.

## Actual Samsung QA evidence

| Check | Result |
| --- | --- |
| Samsung Gradle compilation/debug APK + AndroidTest package build | **PASS** — `BUILD SUCCESSFUL`, 72/72 tasks |
| JVM tests | **PASS 243/243** — zero failures/errors/skips |
| Installed D-126 main APK / test APK SHA-256 | **PASS** — both checked against build manifest |
| Physical Samsung Android Instrumentation retest | **PASS 42/42**, `OK (42 tests)`, 21.697 seconds |
| **Total automated tests across JVM and Android gates** | **285/285 PASS** |
| Main application during AndroidTest retest | Installed D-126/code20 confirmed; **only the test APK was replaced** |
| Existing Saved Classes and Sounds after the real-device tests | **PASS — user confirmed "הכל נשמר"** |
| Product manual acceptance | **PASS — user confirmed "הכל עובד כנדרש"** |

The first connected Android test execution produced **38 PASS and 4 FAIL**, caused by test-storage isolation and Compose virtual-clock synchronization. Both test-only issues were corrected in the development branch. **The repeated connected test execution passed 42/42**, without modifying production Android source or the installed application. Retain the original failure record for traceability.

## Evidence references

- [Physical-phone Android 42/42 PASS and saved-data preservation](STABLE8_D126_ANDROID_INSTRUMENTATION_QA_PASS_2026_10_28.md)
- [Initial four-failure analysis, AndroidTest-only repairs, successful retest](D126_INSTRUMENTATION_42_TEST_FAILURE_TRIAGE_2026_10_28.md)
- [Previous Stable 8 D-126 acceptance and feature specification](STABLE8_D126_UNIFIED_TIMER_AND_RUN_DIAGNOSTICS_QA_ACCEPTANCE_2026_10_28.md)
- [D-126 functional diagnostic design](D126_RUN_DIAGNOSTICS_AND_SCROLLABLE_HISTORY_2026_10_28.md)

## Limits and next work

This certificate records **completed automated build/JVM/connected Android testing** plus **manual user preservation verification**. It is not evidence of every possible 3-hour Doze, OS process reclaim, external audio routing, accessibility matrix, publication signing or Google Play policy review. Those are separate later stages, not reasons to mislabel the concluded 285/285 QA result.

**Next roadmap stages remain:** accessibility and device adaptation; Android security/privacy/platform policy hardening; signed Release/AAB; branding/assets; Google Play listing and rollout; eventual other-platform feasibility.

**Freeze contract:** create `stable/android-d126-qa9-tests-complete` from the documented development snapshot containing these QA records and the test-only corrections. Do not push future commits onto the stable checkpoint, and do not alter the earlier Stable 8 branches.
