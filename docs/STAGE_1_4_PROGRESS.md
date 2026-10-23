# Stage 1 Step 1.4 — Scheduling core COMPLETE / VERIFIED on isolated branch (D-052)

**Started:** 2026-10-22 after the user's “Continue” following Step 1.3 approval.  
**Development branch:** [stage-1-4-scheduling-core](https://github.com/AviY99/Atelier-Time/tree/stage-1-4-scheduling-core)  
**GitHub Actions:** **NOT started; no workflow files created or invoked** because the user reports their GitHub metered budget is blocking other project work.  
**CURRENT STATUS 2026-10-23:** **35/35 `dart test` PASS, screenshot-confirmed on user Android Termux Dart 3.13.7**, after fetching the T09 35th test. Earlier `dart analyze` **No issues found!** on 34-test checkout and smoke check passed. The 35th test-only revision was not shown re-analyzed. **Step 1.4 pure Dart scheduling engine and required acceptance checks COMPLETE/VERIFIED ON THIS BRANCH.** The branch remains unmerged; Flutter UI/app/build and Stage 2 are NOT authorized. Older status entries below describe historical, superseded interim states.

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
- Uses a virtual ordinary-break timeline adjusted by `N×(L−B)` for any signed difference between positive break durations, as now allowed by D-044.
- Scores each selected replacement by the approved P-01/P-02 distance of its **actual special-break center** from the target work fraction; retains earliest-ordinal tie-breaking. Rebuilds and checks the full real timeline before returning.
- Adds regression tests for the old false-invalid case (97-minute session with 15m opening posing, P12/B4/L17, two special breaks at ordinals 2 and 3) and an 8-hour multi-break case (automatic ordinals 13,26,38,51,64), in addition to original A/B fixtures.
- **Independent design check only:** A Python reimplementation of the old brute-force objective and new dynamic-programming recurrence produced identical optimal choices in **25,000 random small whole-minute configurations**, comparing only scenarios with a valid virtual ordinary timeline. It also derived A break #3 at 80–95m and B break #3 at 85–100m. This is **NOT execution, analysis or verification of the actual Dart implementation**.

**Historical note (superseded by approved D-044):** D-038 had previously asked about 5m normal versus 3m special. User has since approved it. The engine now permits the shorter positive replacement and applies 25/5 warnings.

**Still blocked:** Dart SDK absent locally; no `dart analyze`, `dart test`, smoke run or Flutter build. No GitHub Actions workflows created/invoked due the user's reported budget block. No merge into main. Stage 1 Step 1.4 remains **IN PROGRESS**, Stage 2 **NOT AUTHORIZED**.


## 2026-10-22 — Additional Step 1.4 hardening and free local verification

**User asked:** “What to do to solve ur failure? Can you continue”. Continued **Step 1.4 ONLY**. No GitHub Actions or later-stage work.

**SDK failure diagnosed in the current environment:**
- `dart` and `flutter` binaries are absent from `PATH`, and no local SDK was found in common installed locations.
- DNS cannot resolve `storage.googleapis.com`, `github.com` or `dart.dev` from the local command environment; direct `curl` of the Dart stable SDK version URL fails with `curl: (6) Could not resolve host`. Thus the SDK cannot be installed from this environment in the normal way; it is **not evidence of a failing Dart build**.
- GitHub connector calls can still commit/read files. Do **not** use Actions or any budget-consuming runner under the reported GitHub billing block.

**Further code improvements on the branch:**
- `ScheduleCandidate._` is now library-private: callers cannot fabricate an approvable candidate through the public Dart API. Only `ScheduleEngine.generate` produces the review candidate.
- `ApprovedSession` retains immutable `acknowledgedWarnings` including exact timestamps and messages, with `acknowledgedWarningCodes` as a convenience getter. This avoids losing repeated safety warnings with the same code.
- Added source-only regression assertion that warnings persist with their 25-minute offset and cannot be cleared from the approved snapshot. This test is **written, not run**.
- Added `tool/verify.ps1` for Windows PowerShell and `tool/verify.sh` for macOS/Linux, each running the smoke check, `dart pub get`, `dart analyze`, and `dart test`, writing `atelier-time-verification.log`. New `.gitignore` prevents local test output/caches from being committed. **Scripts have not been run against a Dart SDK**.
- Documented that the **minimal required development install for Step 1.4 is the Dart SDK**, not Flutter, Android Studio, Visual Studio or Xcode. Installing Dart on a user's computer does **not** install it in ChatGPT's separate container; the user must run a local verification script and share its log if no accessible Dart runtime can be made available to the assistant.

**Next action before marking Step 1.4 complete:** Execute `dart tool/smoke.dart`, `dart pub get`, `dart analyze`, and `dart test` on an actual Dart SDK (locally or a non-billable Dart-capable machine), then correct any real errors. Do not claim test passes based on the Python model comparison or delimiter checks. Keep the Step 1.4 branch isolated until verified; Stage 2 remains unapproved.



## 2026-10-23 — User's real Android Dart compiler result: FAIL, fix committed

- User ran **Dart 3.13.7 stable on Android ARM64 in Termux**, cloned this branch successfully and ran `dart tool/smoke.dart`.
- **Observed FAIL (before fix):** `lib/src/scheduling.dart:621:28: Error: The argument type 'num' can't be assigned to the parameter type 'int?'` at `offsetSeconds: crossingOffset`. This was a genuine **Dart compile error**, not a test pass or Termux installation failure.
- Cause: `math.max(0, _maxUnbrokenPoseSeconds - previousAccumulated)` was typed as `num` rather than `int` in this Dart context.
- **Code fixed on branch:** commit `0dedd95756b6d3984b2e16548a58599179fbf7ff` replaces `math.max` with an integer-typed conditional; warning-offset calculation remains the same. The **corrected Dart code has not yet been executed**.
- **Next user command from `~/Atelier-Time`:** `git pull --ff-only && dart tool/smoke.dart`. Share the full result. On success: `dart pub get`, `dart analyze`, `dart test`. Dart SDK already installed; no reinstall required.
- No Actions/paid runs, no merge, no Flutter UI, no Stage 2. Stage 1 Step 1.4 stays **IN PROGRESS** until real Dart tests pass.



## 2026-10-23 — Android Dart screenshots confirm 34 unit tests passing

User supplied actual Termux screenshots of the `dart pub get && dart analyze && dart test` sequence following the compiler warning-offset fix:

```text
Changed 49 dependencies!
Analyzing Atelier-Time...
No issues found!
Built test:test.
00:01 +34: All tests passed!
```

This is **SCREENSHOT-CONFIRMED Dart analyzer success and 34 passing tests on Android ARM64 Dart 3.13.7** (D-050), stronger than the earlier user-report-only D-049. Prior smoke script also passed. No GitHub Actions or paid CI involved. Git commit SHA of the user's local checkout was not visible in the screenshot, and this is NOT a Flutter UI or APK build.

**Acceptance audit:** Of required Stage 1 T01–T12 and T15–T18, the existing suite explicitly covers canonical A/B, normal/even/custom endings, 34m+1m custom extreme, rejected zero-minute pose, exact-fit behavior, replacement ordinals, determinism and contiguous timeline, and 25/5 advisory/positive-break rules. **Missing specific T09 counterpart:** valid 1m+34m custom ending. Before Step 1.4 is marked complete, add that test and run Dart suite again; broad T13–T14 edge matrix remains partially covered and can be tracked separately. Do not claim Stage 1.4 COMPLETE based solely on the original 34 successes.

**Gate remains:** Step 1.4 on isolated branch; no merge, Stage 2, UI, APK, Actions, paid runners or budget changes.


## 2026-10-23 — T09 missing endpoint added as 35th test (D-051)

Following screenshot-verified `dart analyze` **No issues found!** and `dart test` **+34 All tests passed!** on Android Termux Dart 3.13.7 (D-050), the Stage 1 minimum T09 acceptance audit found the positive **1m+34m custom final-two-pose split** was not explicitly tested (34m+1m was).

**Added only a test**, no scheduling engine changes: `test/scheduling_test.dart` now has **35** unit test definitions. New test `T09: custom minimum first pose keeps a positive 34m last pose` asserts exact Example A times: 125–126 pose, 126–131 rest, 131–165 pose, 165–180 summary; also confirms D-031 warning and affirmative acknowledgment. Commit `4b2f5d9fdd8dabc6dea8dcc0204c5062c7b6916a`.

**New test NOT RUN YET.** Existing original 34 PASS screenshot evidence remains valid; no verified 35-test pass exists. User's existing Termux clone can fetch and run using `git pull --ff-only && dart test`. No paid Actions, UI, builds, main merge or Stage 2. **Step 1.4 remains in final QA.**

## 2026-10-23 — Stage 1 Step 1.4 verification gate COMPLETE (D-052)

**User screenshot (Android Termux, Dart 3.13.7 ARM64):**

```text
~/Atelier-Time $ git pull --ff-only && dart test
...
8cf7a24..907f871  stage-1-4-scheduling-core -> origin/stage-1-4-scheduling-core
...
Fast-forward
  docs/STAGE_1_4_PROGRESS.md
  test/scheduling_test.dart
...
00:00 +35: All tests passed!
```

The T09 opposite **1m+34m** final-two-pose regression has run and **PASSED** together with all 34 existing tests. Previous screenshots on prior 34-test version showed `dart analyze` **No issues found!**, `dart test` **+34 All tests passed!**, and smoke check passed. **Do not claim a new analyzer run following the test-only addition**.

**Acceptance:** Stage 1 required T01–T12 and T15–T18 represented and passing. T13/T14 extended input combinations and testing on other operating systems are later QA; not asserted fully covered. **Step 1.4 scheduling core COMPLETE on this branch**; repository `main` still holds docs and not the Dart source. No GitHub Actions, paid runners, Android APK, installed Flutter app, timer/audio, preset persistence or Stage 2 authorization. **Stop before merge and Stage 2 until explicit user approval.**
