# Stable 8 — Consolidated D-126 unified Timer + Run diagnostics: acceptance and freeze certificate

**Acceptance date:** 2026-10-28.
**User declaration:** "הכל עובד כנדרש , זאת גרסה יציבה 8 , לוגיקה ותיעוד/תחקור לוגיקה" — the user confirms the installed D-126 behaves as required and specifically names **Stable 8** as the combination of the **unified Timer logic** and **the logic documentation/diagnostic capabilities**.

## Official identity and checkpoint policy

- **User-facing stable ordinal: Stable 8.** This is the **current consolidated Stable 8** based on Android `versionName="D-126"`, `versionCode=20`. It is NOT a new ordinal Stable 9 and is NOT a request for another APK build or installation.
- **Current stable Git checkpoint:** `stable/android-d126-qa8-logic-diagnostics`. The new branch is frozen after certificate/handoff creation; it must not be developed on, advanced, or force-pushed.
- **Prior Stable 8 logic-only recovery baseline remains frozen:** `stable/android-d124-qa8` (D-124/code19). Its original acceptance certificate `docs/STABLE8_D124_QA_ACCEPTANCE_2026_10_28.md` stays intact as historical evidence for the earlier checkpoint. Do not rewrite this old branch/certificate to pretend D-126 was originally in D-124.
- **Development continues solely on:** `android-native-kotlin-compose`. New Android functionality must get a subsequent D-number and a higher versionCode. The earlier D-125 proposal to move a sub-minute remainder into Summary was **REJECTED**, remains closed, and is **not** part of this stable version.
- **Tested/installed D-126 application source:** `b2d47ca32c2f470d38e556ea7a571afaa89400e3` from the Samsung build's SHA manifest. GitHub comparison of this exact build commit against the current development branch immediately before the consolidated certificate found **no changes to production source, tests, or Gradle build identity**, only a later Termux QA driver script and project documentation. Thus the new checkpoint represents the tested APK source, plus operational/docs updates.

## Functionality accepted by the user

1. **Unified timer constraints from D-124:** `FinalPairCompensation.reflow` delegates to `ConstraintFuturePlanner.plan` for the live end-time budget and legal future allocations. Preserve the physical Class finish, already entered/observed history, fixed Summary duration, genuine Special Break constraints, manual placement constraints, minimum one minute per work Session, and allowed complete regular Session+Break insertions after Next. Accepted Next/Restart/Resume logic stays unchanged.
2. **Clean programmed timer presentation in D-126:** class clocks/phase countdown/controls and the accurate chronological actual-past/revised-future color timeline stay visible. Avoid verbose `Next credit`/final-two explanatory paragraphs during a Class. Rejected operations still show a concise reason.
3. **Complete scrollable bottom Run action history:** newest events first in a bounded window showing approximately five rows; scroll within the list back through **every event to Start**. Includes meaningful manual/automatic actions, cue requests and failures, without exposing internal scheduling IDs in the normal display. No old 30-event retention cap.
4. **Full private Run diagnostics file:** per-Run append-only `.jsonl`, including the immutable complete approved Editor rules and schedule, physical approved finish, ending choice and Sound snapshot; event time/states/phase transitions and outcomes; reflow phase changes with before/after schedule; requested (not guaranteed audible) cues, errors and end reason. Created progressively so an interrupted Run retains earlier records.
5. **Local diagnostic-library controls:** private reports remain on phone until user-initiated Android document export; deletion requires explicit confirmation. Saved Classes/Sounds storage is not modified by the recorder.

**User acceptance scope:** the user reports `"הכל עובד כנדרש"` after the approved in-place D-126 update and asks to designate the result Stable 8. Treat this as **manual functional acceptance**, not as proof that each particular scroll depth, report byte field, process-death case or sound-device matrix has been independently audited.

## Test evidence and precise exclusions

| QA gate | Recorded result |
| --- | --- |
| Actual Samsung Gradle debug build + test APK assembly | **PASS**, `BUILD SUCCESSFUL`, **72/72 Gradle tasks** |
| Actual Samsung JVM regression tests | **PASS 243/243**, failures=0, errors=0, skipped=0 |
| Main app APK integrity against build manifest | **PASS**, SHA-256 `OK` |
| AndroidTest APK integrity against build manifest | **PASS**, SHA-256 `OK` |
| In-place D-126 operation on user's Samsung | **User-confirmed manual acceptance:** `הכל עובד כנדרש` |
| Android connected instrumentation against installed D-126 | **NOT RUN / deferred**. The ADB-only runner stopped safely before installation when there were zero connected devices. Building the AndroidTest APK is not executing the tests. |
| Long-running 3-hour endurance / process-reclaim / external audio issue | No new exhaustive D-126 proof; retain historical results and monitoring without inventing coverage |
| Google Play distribution/release signing | Not part of this acceptance |

The build manifest recorded:
`version=D-126`, `code=20`, `branch=android-native-kotlin-compose`,
`commit=b2d47ca32c2f470d38e556ea7a571afaa89400e3`.
User-observed:
`Atelier-Time-D126-Candidate.apk: OK` and
`Atelier-Time-D126-Tests.apk: OK`.

**Source freeze proof:** compare GitHub files from build commit `b2d47ca32c2f470d38e556ea7a571afaa89400e3` to the consolidated Stable 8 checkpoint after freezing. Any later changes are limited to docs and optional QA driver, not `app/src/main`, `app/src/test`, `app/src/androidTest`, or `app/build.gradle.kts`.

## Workflow and outstanding non-blocking QA

- The currently working on-phone installation path is **Samsung My Files → Download → main candidate APK → Update in place**. The user explicitly rejected further wireless-ADB reconnection suggestions in this workflow. Do not uninstall, clear app data, or reset saved Classes/Sounds.
- The previously authored Android Instrumentation methods are compiled in the test APK but **not device-executed**; do not request ADB or confuse source test counts with completed tests.
- Preserve the historical D-124 checkpoint and its earlier Stable 8 certificate untouched.
- Keep the summary/acceptance in `docs/HANDOFF.md`, `docs/DECISIONS.md`, `docs/ROADMAP.md`, `docs/TEST_PLAN.md` and `android-native/README.md`.
- Future issues should be evidenced using the new exported per-Run JSONL report and explicit screenshots rather than re-opening the unified planner without a proven defect.

## References

- [D-124 unified planner acceptance](STABLE8_D124_QA_ACCEPTANCE_2026_10_28.md)
- [D-124 unified planning and QA](D124_UNIFIED_CONSTRAINT_FUTURE_PLANNER_2026_10_28.md)
- [D-126 diagnostics implementation and Samsung evidence](D126_RUN_DIAGNOSTICS_AND_SCROLLABLE_HISTORY_2026_10_28.md)
- [Handoff](HANDOFF.md)
- [Decisions](DECISIONS.md)
