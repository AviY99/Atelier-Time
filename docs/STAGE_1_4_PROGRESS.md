# Stage 1 Step 1.4 — First coding increment (IN PROGRESS)

**Started:** 2026-10-22 after the user's “Continue” following Step 1.3 approval.  
**Development branch:** [stage-1-4-scheduling-core](https://github.com/AviY99/Atelier-Time/tree/stage-1-4-scheduling-core)  
**GitHub Actions:** **NOT started; no workflow files created or invoked** because the user reports their GitHub metered budget is blocking other project work.  
**Status:** Source and tests committed, but **Dart tests have NOT BEEN RUN**; the available execution environment has no Dart or Flutter SDK. **Step 1.4 is NOT complete**.

## 2026-10-22 — D-044 approved; continued Step 1.4 implementation

**User approval:** “allow any positive special break duration, even shorter than or equal to the normal break, with the already approved 25/5 rest warnings. Zero-minute breaks remain invalid. Record this decision in GitHub and continue only the authorized Step 1.4 coding and testing work.”

- D-038 resolved by **approved D-044**. The pure Dart engine accepts **L > 0 regardless of whether L < B, L = B or L > B**, where normal `B>0`; no `pendingDecision` status remains in the code.
- Exact automatic replacement positioning now works with all signed differences `L−B` (negative, zero or positive), preserving full-session duration and placement objectives. Manual positioning uses the same accepted duration rule.
- Extended [written regression tests](../test/scheduling_test.dart) for normal=5m with special=1m (valid warning+acknowledgment), special=3m manual (valid warning), special=5m (no warning if qualifying rest), and two 3m replacements. Extended [dependency-free smoke test](../tool/smoke.dart) for both short and equal lengths.
- **Independent Python-only verification**, not Dart execution: virtual-timeline dynamic programming matched exhaustive scoring across **25,000 randomly sampled small cases** with negative, zero, and positive duration differences; a separate exhaustive **actual real-schedule simulation** matched in **4,000 further randomly sampled cases**. Also rechecked canonical A/B break ordinals #3 and shorter/equal examples. No product inputs or canonical fixture values were changed.
- **Dart toolchain remains unavailable** (`dart` and `flutter` not installed; download access fails on DNS). `dart analyze`, `dart test`, `dart run tool/smoke.dart` and Flutter builds are **NOT RUN**; do not claim they passed. Test files are **source only**.
- The user's GitHub budget reportedly remains blocked; **no Actions/workflows/paid runners** used, and no merge into `main`. **Step 1.4 IN PROGRESS**; Stage 2 and later not authorized.

## Code files

- [pubspec.yaml](../pubspec.yaml): smallest standalone Dart core package, no Flutter dependencies.
- [lib/atelier_time.dart](../lib/atelier_time.dart): public API for future Flutter app.
- [lib/src/scheduling.dart](../lib/src/scheduling.dart): core engine with integer-second timeline, opening groups, configurable first phase, short/special break replacement, manual/automatic placement, validation, three final-pose resolutions, explicit approval boundary, accumulated rest warnings and D-044 flexible positive special-break durations.
- [test/scheduling_test.dart](../test/scheduling_test.dart): *unexecuted* automated tests for exact approved A/B schedules, all three ending options, deterministic IDs, contiguous phases, error cases, manual replacement, 25/5 warning+acknowledgement and approved D-044 shorter/equal-positive special replacements with 25/5 warning acknowledgment.
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
3. **D-037:** nearest-work-midpoint automatic long-break center, earlier ordinal tie, N-break even spread and manual ordinals; long break replaces not supplements short break. The selector now uses exact dynamic programming across eligible ordinals to optimize the approved objective (D-043).
4. **D-038 RESOLVED BY APPROVED D-044:** any special break duration >0 is valid, including shorter than or equal to the normal break. The actual timeline is regenerated; D-031 rest warnings/acknowledgment apply. A 0-minute break remains invalid per D-036.
5. **No persistence, Flutter UI, runtime timer, sound, app packaging, device testing or executable release** in this increment. These belong to later user-approved stages.

## Known limits and future work within Step 1.4

- **The Dart toolchain is absent**, so syntax, compilation, analyzer and behavior **have not been verified by execution**. Correct any issues revealed by an actual Dart run before considering Step 1.4 complete.
- Verify exact output for canonical Examples A/B across all end-pose options (both were coded as tests, but not executed).
- Exact dynamic programming replaces the former exhaustive search, but Dart runtime and large-input performance verification still need execution. No arbitrary user-input cap has been approved.
- Assess edge cases such as many opening groups, extreme numbers, absence of final pair and manual break ordinal shifts with actual tests.
- Confirm correct immutable preset/safety-acknowledgement serialization in later persistence stage; only in-memory approved snapshots currently exist.
- Do not create GitHub Actions workflows or invoke paid metered build resources while budget block remains unresolved.
- **Do not advance to Stage 2, persistent presets, Flutter UI or any later implementation step without its separate gate.**

## Required next action

Continue to verify and fix **this Step 1.4 code**, ideally with a Dart-capable local runner at no GitHub metered cost. Request the narrow D-038 decision when relevant; otherwise preserve it as pending. Update root handoff and log; do not claim tests passed. This stage is not yet complete.

## 2026-10-22 — Second coding increment: exact dynamic-programming break placement

**User instruction:** “Continue, also explain your open question.” This authorizes continued work **inside Stage 1 Step 1.4 only** and explanation of D-038; it does **NOT** approve D-038, Github Actions, Flutter UI or any later stage.

**Bug found in first implementation:** The first automatic-break optimizer enumerated all special-break combinations (exponential growth) and used an unreplaced ordinary timeline to bound feasible slot ordinals. A nominal ordinary timeline can be invalid from a trailing-break condition **even when replacing two breaks yields a completely valid timeline**, causing a false rejection.

**Fix on this branch:**
- Replaced exhaustive enumeration with an exact deterministic dynamic-programming selector, storing `O(N×M)` candidate states for N special breaks and M eligible positive ordinary break slots, with some additional ordinal-path copying overhead.
- Uses a virtual ordinary-break timeline shortened by `N×(L−B)` to calculate all eligible break ordinals when `L>B` (the current allowed branch, while D-038 remains open).
- Scores each selected replacement by the approved P-01/P-02 distance of its **actual special-break center** from the target work fraction; retains earliest-ordinal tie-breaking. Rebuilds and checks the full real timeline before returning.
- Adds regression tests for the old false-invalid case (97-minute session with 15m opening posing, P12/B4/L17, two special breaks at ordinals 2 and 3) and an 8-hour multi-break case (automatic ordinals 13,26,38,51,64), in addition to original A/B fixtures.
- **Independent design check only:** A Python reimplementation of the old brute-force objective and new dynamic-programming recurrence produced identical optimal choices in **25,000 random small whole-minute configurations**, comparing only scenarios with a valid virtual ordinary timeline. It also derived A break #3 at 80–95m and B break #3 at 85–100m. This is **NOT execution, analysis or verification of the actual Dart implementation**.

**D-038 OPEN:** If short break is 5m and special replacement is 3m, both durations are positive but the special is shorter than the ordinary. The user has not specified whether to permit it. The engine continues to mark positive special <= normal as `pendingDecision` rather than choosing an unapproved rule. The recommended direction (allow any positive special value with 25/5 warnings) is only a recommendation, NOT approved policy.

**Still blocked:** Dart SDK absent locally; no `dart analyze`, `dart test`, smoke run or Flutter build. No GitHub Actions workflows created/invoked due the user's reported budget block. No merge into main. Stage 1 Step 1.4 remains **IN PROGRESS**, Stage 2 **NOT AUTHORIZED**.

