# Atelier Time — authoritative current project status and handoff

**Last confirmed:** 2026-10-28. **Authoritative release:** **Stable 9 = D-126 / versionCode 20**. All older “current/pending” headings in dated historical documents are superseded by this entry. **Primary development** lives on the Android Kotlin/Compose branch `android-native-kotlin-compose`; the default `main` branch remains a project documentation and historical-reference surface, **not** a claim that Android code was merged into `main`.

## Approved current product

**Atelier Time** runs natively on Android (Kotlin + Jetpack Compose), developed and built on the Samsung using Code on the Go. The user-approved Stable 9 is the same installed D-126 app previously accepted as Stable 8: a unified physical-deadline-aware Timer planner based on `ConstraintFuturePlanner`, preserved past and fixed Summary, approved Session/Break rules, live timeline, and per-Run diagnostic logging. D-126 separates operational clock/controls from forensics, adds a full in-app scrollable actions log (about five newest events visible; scroll to Start), stores append-only app-private JSONL snapshots/events/phase reallocations/sound cue requests, and offers deliberate Android document export and confirmed report deletion. Existing Saved Classes and Sounds are preserved.

**No new Android binary was compiled or installed for Stable 9:** the stable ordinal increased because all planned automated QA and user verification passed. Installed app remains `D-126` / `versionCode 20`, built from production source commit `b2d47ca32c2f470d38e556ea7a571afaa89400e3`.

## Actual verified QA

| Gate | Observation |
| --- | --- |
| Gradle/Android main + AndroidTest APK build on Samsung | `BUILD SUCCESSFUL`; 72 tasks PASS |
| JUnit JVM regression suite | **243/243 PASS**; failures/errors/skips = 0 |
| Both original APK SHA-256 checks against build manifest | **PASS** |
| Android Instrumentation on physical Samsung | **42/42 PASS**, `OK (42 tests)`, 21.697 seconds |
| **Combined automated tests** | **285/285 PASS** across JVM and Android device suites |
| User manual functional acceptance | **PASS**, “הכל עובד כנדרש” |
| Saved Classes/Sounds after AndroidTest | **PASS**, user confirmed “הכל נשמר” |

The first physical AndroidTest run had 4/42 failures from test-only private-storage and virtual-clock synchronization issues; after AndroidTest-only changes, all 42 passed. The corrected test-only APK was built from `07816e22014e2ff6593757cbd33879344ada1e80`. Instrumentation updated **only** the test package; the main D-126 app was not replaced or cleared. Do not overstate unmeasured long-duration Doze, spontaneous process reclaim, actual audibility across all devices, or Google Play compliance.

## Stable checkpoints and preservation

- **Current stable release certificate:** [Stable 9 — 285 automated tests](STABLE9_D126_285_AUTOMATED_QA_ACCEPTANCE_2026_10_28.md).
- **Frozen Stable 9 branch:** [`stable/android-d126-qa9-tests-complete`](https://github.com/AviY99/Atelier-Time/tree/stable/android-d126-qa9-tests-complete). Official checkpoint, do not advance.
- **Earlier frozen Stable 8:** [`stable/android-d126-qa8-logic-diagnostics`](https://github.com/AviY99/Atelier-Time/tree/stable/android-d126-qa8-logic-diagnostics). Original Stable 8 D-124 [`stable/android-d124-qa8`](https://github.com/AviY99/Atelier-Time/tree/stable/android-d124-qa8) remains intact.
- **Android development only:** [`android-native-kotlin-compose`](https://github.com/AviY99/Atelier-Time/tree/android-native-kotlin-compose). Do not mutate accepted source/installed Classes/Sounds without a distinct approved change.
- **Rejected:** D-125 proposal to transfer a sub-minute remainder into Summary. Fixed Summary duration and minimum one-minute work Session remain applicable.

## Operational handoff and technical sources

Read [HANDOFF.md](HANDOFF.md) first for cumulative decisions and the specific next-chat operating contract. For exact project baseline see [DECISIONS.md](DECISIONS.md), [ROADMAP.md](ROADMAP.md), and [TEST_PLAN.md](TEST_PLAN.md). Evidence and implementation:

- [Stable 9 acceptance certificate](STABLE9_D126_285_AUTOMATED_QA_ACCEPTANCE_2026_10_28.md)
- [Stable 8 D-126 functionality and immutable checkpoint](STABLE8_D126_UNIFIED_TIMER_AND_RUN_DIAGNOSTICS_QA_ACCEPTANCE_2026_10_28.md)
- [42/42 on-device instrumentation and post-test data preservation](STABLE8_D126_ANDROID_INSTRUMENTATION_QA_PASS_2026_10_28.md)
- [D-126 diagnostics implementation](D126_RUN_DIAGNOSTICS_AND_SCROLLABLE_HISTORY_2026_10_28.md)
- [Initial four AndroidTest failures, test-only remediation, successful retest](D126_INSTRUMENTATION_42_TEST_FAILURE_TRIAGE_2026_10_28.md)
- [D-124 unified timer planning contract](D124_UNIFIED_CONSTRAINT_FUTURE_PLANNER_2026_10_28.md)
- [Native Android source README](https://github.com/AviY99/Atelier-Time/blob/android-native-kotlin-compose/android-native/README.md)

**Installation/QA preference:** Samsung My Files for an authorized **in-place** main-APK update; do not force an ADB workflow when not needed. Termux ADB is used for user-approved Android instrumentation when available. Never uninstall the main app, clear app data, delete saved presets, or modify sounds. No further D-126 build, APK installation, QA rerun or Stable 9 ordinal/version change is currently required.

## Next planned stages

7. Accessibility/Android display adaptability: font scaling, screen sizes, contrast, touch areas, TalkBack.
8. Android hardening and security/privacy/policy (including foreground-service permissions).
9. Signed release/AAB and upgrade/data preservation.
10. Final logo/launcher icon and store assets — still requires user artwork approval.
11. Google Play listing and internal/closed testing, then separate publication decision.
12. Research future platform adaptations (iOS/desktop) without rewriting timer semantics prematurely.

**Evidence policy:** Historical documents retain the facts of the time they were written; do not erase failed attempts or incorrectly promote unexecuted tests. This 2026-10-28 document supersedes old headings claiming a previous release is still “current”.
