# Stage 1 Step 1.4 — First coding increment (IN PROGRESS)

**Started:** 2026-10-22 after the user's “Continue” following Step 1.3 approval.  
**Development branch:** [stage-1-4-scheduling-core](https://github.com/AviY99/Atelier-Time/tree/stage-1-4-scheduling-core)  
**GitHub Actions:** **NOT started; no workflow files created or invoked** because the user reports their GitHub metered budget is blocking other project work.  
**Status:** Source and tests committed, but **Dart tests have NOT BEEN RUN**; the available execution environment has no Dart or Flutter SDK. **Step 1.4 is NOT complete**.

## Code files

- [pubspec.yaml](../pubspec.yaml): smallest standalone Dart core package, no Flutter dependencies.
- [lib/atelier_time.dart](../lib/atelier_time.dart): public API for future Flutter app.
- [lib/src/scheduling.dart](../lib/src/scheduling.dart): core engine with integer-second timeline, opening groups, configurable first phase, short/special break replacement, manual/automatic placement, validation, three final-pose resolutions, explicit approval boundary, accumulated rest warnings and pending-policy status.
- [test/scheduling_test.dart](../test/scheduling_test.dart): *unexecuted* automated tests for exact approved A/B schedules, all three ending options, deterministic IDs, contiguous phases, error cases, manual replacement, 25/5 warning+acknowledgement and D-038 unresolved behavior.
- [tool/smoke.dart](../tool/smoke.dart): dependency-free smoke checks, also *unexecuted*.

## Local commands for future verification (only when a Dart SDK is available)

```bash
dart --version
dart --enable-asserts run tool/smoke.dart
dart pub get
dart analyze
dart test
```

These run locally and require no GitHub Actions minutes; `dart pub get` needs package access to fetch `test`. A future cloud workflow requires a separate explicit decision and budget check, and no one should claim it ran.

## Current implementation policy

1. **D-036:** 0-minute complete sessions and configured breaks are errors. The 5m/15m break defaults remain editable to other positive minute durations.
2. **D-031:** after 25 accumulated posing minutes without >=5m rest, warning with separate acknowledgement; 1–4m breaks are valid but do not reset the pose counter.
3. **D-037:** nearest-work-midpoint automatic long-break center, earlier ordinal tie, N-break even spread and manual ordinals; long break replaces not supplements short break. The current automatic solver exhaustively enumerates feasible ordinal combinations to optimize the approved objective.
4. **D-038 NOT APPROVED:** for specialBreakSeconds <= shortBreakSeconds but both are positive, the engine emits **pendingDecision** instead of falsely calling the case valid or invalid. Request the user's decision before finishing that branch of implementation.
5. **No persistence, Flutter UI, runtime timer, sound, app packaging, device testing or executable release** in this increment. These belong to later user-approved stages.

## Known limits and future work within Step 1.4

- **The Dart toolchain is absent**, so syntax, compilation, analyzer and behavior **have not been verified by execution**. Correct any issues revealed by an actual Dart run before considering Step 1.4 complete.
- Verify exact output for canonical Examples A/B across all end-pose options (both were coded as tests, but not executed).
- The automatic placement optimizer's exhaustive enumeration could be slow on unusually large multi-break inputs; assess performance or switch to exact optimized search before production use. No arbitrary user-input cap has been approved.
- Assess edge cases such as many opening groups, extreme numbers, absence of final pair and manual break ordinal shifts with actual tests.
- Confirm correct immutable preset/safety-acknowledgement serialization in later persistence stage; only in-memory approved snapshots currently exist.
- Do not create GitHub Actions workflows or invoke paid metered build resources while budget block remains unresolved.
- **Do not advance to Stage 2, persistent presets, Flutter UI or any later implementation step without its separate gate.**

## Required next action

Continue to verify and fix **this Step 1.4 code**, ideally with a Dart-capable local runner at no GitHub metered cost. Request the narrow D-038 decision when relevant; otherwise preserve it as pending. Update root handoff and log; do not claim tests passed. This stage is not yet complete.
