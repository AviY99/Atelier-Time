# Test and acceptance plan — Atelier Time

Version: 0.7 Android screenshot-confirmed test evidence and Stage 1 coverage audit (2026-10-23). **Step 1.4 source/test files exist** on branch `stage-1-4-scheduling-core`. User installed **Dart 3.13.7 on Android Termux** and attempted `dart tool/smoke.dart`; compilation **FAILED** due to a `num` → `int?` mismatch at warning offset, corrected in development branch commit `0dedd95756b6d3984b2e16548a58599179fbf7ff`. **User now confirms the corrected Dart smoke check PASSED on Termux (D-048)**. **User subsequently reports “All tests passed!” after `dart pub get && dart analyze && dart test` (D-049)**. This is a **user-reported pass of the branch's written suite** (34 Dart `test(...)` definitions); specific terminal logs/results and analyzer output were not provided. This does **NOT certify every separately planned acceptance item**, Flutter UI, builds or cross-platform compatibility. No GitHub Actions/bills/builds. No Flutter app yet.

## Approved requirement tests: D-031 + D-036 (some covered by user-reported passing Dart suite; remaining plan-level acceptance items require mapping)

**Latest D-036 supersedes D-031 only where the older rule allowed 0-minute breaks.** Session total must be >0; configured normal/active special break durations must be >0. The 25-minute accumulated-posing / >=5-minute qualifying rest rule remains advisory, warning-and-acknowledge for **otherwise valid** short rests/long poses. Default new-session breaks remain 5m normal and 15m special, both editable to other **positive** values.

- R-B01: A new session pre-fills normal short break at **5m** and middle/long break at **15m**.
- R-B02: Normal and active special breaks independently accept **positive** 1m, 3m, 5m, 15m, etc.; reject 0 and negative numbers with blocking validation.
- R-B03 (LATEST REVISED): Configured normal break duration **0m is invalid**, blocks preview approval and cannot be accepted via the 25/5 warning acknowledgement.
- R-B04 (LATEST REVISED): Selected special/long replacement break duration **0m is invalid**; a positive special duration is required for any actual replacement.
- R-B05: Changing either duration recalculates the entire schedule and never hardcodes 5m/15m.
- R-B06: **Approved D-037:** Zero *count* of special replacements produces no special break. It is not the same as a 0m break.
- R-B07: **Total full session duration 0m** (or negative) produces a blocking input error; positive sessions pass this basic constraint.
- R-B08: 5 consecutive 5m poses then a >=5m break reset the cumulative posing counter, no warning.
- R-B09: 25m pose -> 5m break -> next pose generates no warning.
- R-B10: 30m pose -> 5m rest triggers a **warning**, not hard-invalid, due to the exceeded 25m posing threshold.
- R-B11: 25m pose -> 3m rest -> further posing triggers warning; 3m rest does not reset the counter.
- R-B12: 20m pose -> 3m rest -> 6m pose triggers warning once cumulative posing exceeds 25m.
- R-B13: Warning cannot be waived by normal approval alone; user must explicitly acknowledge and then approve.
- R-B14: An acknowledged valid 1–4m break exception may be saved/activated without changing entered durations.
- R-B15: Editing rules, changing long-break placement or adjusting final-pose split invalidates warning acknowledgement/approval.
- R-B16: A break >=5m resets the posing counter; a valid positive break <5m does not.
- R-B17: Canonical A/B exact timing and end-pose alternatives are unchanged and generate no 25/5 warning.
- R-B18: **Approved D-037:** 0m reserved summary is permitted for an otherwise valid positive full session; no zero-length summary phase is emitted. This is distinct from invalid 0m full session.

## Approved Stage 1 Step 1.3 acceptance tests (D-037; NOT EXECUTED)

The user **approved these scheduling-policy test expectations in D-037** as described by the [approved review](STEP_1_3_REVIEW.md) and [detailed policy](SCHEDULING_EDGE_POLICY.md). They remain **PLANNED, NOT EXECUTED**, and Step 1.4 implementation is not authorized:
- P-T01: Automatic midpoint chooses the eligible replacement whose **center** is nearest work midpoint, ties to earlier eligible ordinal; A/B exact reference positions.
- P-T02: Multiple special replacements evenly distributed at work fractions j/(N+1), with deterministic ordinal ties.
- P-T03: Manual ordinal selections survive valid edits; invalid ordinals raise errors, never silently relocate.
- P-T04: Zero **count** of special breaks yields no replacement phase, not a zero-duration break.
- P-T05: Opening-only and/or zero reserved-summary sessions when mathematically valid, without zero-duration phases.
- P-T06: Reject overlarge summary, mandatory opening overruns, absent required pose following break, and impossible replacement counts.
- P-T07: Infeasible final-two-pose options disabled with reason, no inserted break/pose.
- P-T08: Explicit custom final-two split may exceed nominal pose length while preserving positive durations and total.
- P-T09: **Approved:** Exactly 25m accumulated posing at session end does not require a trailing break; >25m continuous pose creates warning.
- P-T10: **Approved:** Track warnings within one full activated session; do not automatically carry counters into a separately activated saved preset.
- P-T11: **APPROVED D-044:** Any **positive** special break may be shorter than or equal to the normal break (e.g. 1m or 3m special vs 5m normal, equal 5m vs 5m). Generate correct replacement times, 25/5 warning(s), and require acknowledgment for otherwise valid warnings. Configured 0m breaks remain a blocking error (D-036).
- P-T12: **Approved D-036:** Strictly no zero logical break slots; do not generate 0m break phases or allow 0m configured breaks as an overrideable safety warning.

## Step 1.4 source links and execution hold
- [Pure Dart scheduler](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/lib/src/scheduling.dart) (development branch, uncompiled)
- [Dart test suite](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/test/scheduling_test.dart) (written, NOT RUN)
- [Dependency-free smoke check](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/tool/smoke.dart) (written, NOT RUN)
- [Implementation status](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/docs/STAGE_1_4_PROGRESS.md)
- **Verification remains required:** actual `dart analyze`, `dart test` and smoke run, plus scalability checks. No GitHub Actions workflow or paid runner launched. D-038 is **RESOLVED by user-approved D-044**. The implementation no longer emits `pendingDecision`; positive shorter/equal special breaks are allowed, with warnings based on actual rest. Dart tests remain unexecuted.

## Additional regression sources for DP refinement (D-043; NOT RUN)
- `test/scheduling_test.dart` on [the Step 1.4 branch](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/test/scheduling_test.dart) includes a 97-minute positive session where the unmodified ordinary cycle would be invalid but **two special replacements at break ordinals 2 and 3 make the timeline feasible**; this guards against the former false rejection.
- A longer 480-minute session (P5m/B1m/L5m, five special replacements) checks deterministic ordinals **13, 26, 38, 51, 64** and replacement count without exponential search.
- An **independent Python model comparison** checked the exact DP scoring against a brute-force reference over **25,000 randomly generated small scenarios** and reproduced both canonical A/B replacement positions. **This was NOT the Dart engine or Dart tests being executed.**
- **Dart SDK remains unavailable**; no analyzer/test success claim. Do not enable Actions or paid GitHub build resources without budget clearance/authorization.

## Stage 1: deterministic scheduling engine
- T01 Example A baseline exact phase order and boundaries from EXAMPLES.md, down to each opening pose. Expect 130m posing + 35m breaks + 15m summary = 180m.
- T02 Example B baseline exact phase order and boundaries. Expect same totals and 3h exact.
- T03 Example A: remainder last two 25m + 5m break + 10m, exact 02:05–02:45 bounds.
- T04 Example A: even last two 17m + 5m break + 18m, exact 02:05–02:45 bounds.
- T05 Example A: custom sample 20m + 5m break + 15m, exact times.
- T06 Example B: remainder last two 25m + 5m break + 5m, exact 02:10–02:45 bounds.
- T07 Example B: even last two 15m + 5m break + 15m, exact times.
- T08 Example B: custom sample 18m + 5m break + 12m, exact times.
- T09 Last-two slider extremes: 1m / (T−1)m, vice versa; no zero/negative pose and stable summary.
- T10 When final regular pose fits exactly, no incomplete-pose dialog.
- T11 Long break occupies one existing short-break slot; no double break; verify number and placement.
- T12 Cycle starts with break (A) or pose (B) as specified, without a phantom trailing break.
- T13 Summary 0, varied session lengths and opening-group counts (subject to edge rule decisions).
- T14 Reject negatives, impossible counts, overlarge summary, insufficient work block, too many long breaks, and infeasible slider options with useful feedback.
- T15 Determinism: identical rules => identical approved sequence and phase IDs/bounds; no overlaps/gaps.
- T16 Full timeline sums to D and every stage end equals next stage start.
- T17 Numerical rounding, especially near-equal odd total => floor/ceil; no cumulative drift.
- T18 No silent auto-approval of a shortened final pose.

## Stage 2: editor and approval
- T19 All editable fields accept valid values; changes force fresh preview and approval.
- T20 Preview displays each phase type, duration, start, end and the long break replacing a short one.
- T21 Three final-pose options visible with accurate times; moving ruler updates both last poses and break times instantly.
- T22 User can cancel preview; no unapproved preset is activated/saved.
- T23 Keyboard/touch accessibility, large text and native controls where feasible.

## Stage 3: preset library
- T24 Create, save, list, rename, duplicate, edit, delete and activate.
- T25 Approved final-pose selection persists after restart; editing invalidates prior approval.
- T26 Presets usable offline; corrupt data fails safely, no silent data loss.

## Stage 4: live timer and audio
- T27 Accurate current stage countdown and overall remaining time at every boundary.
- T28 Pose end uses cue A, break end cue B, complete end cue C (no duplicate/incorrect cues).
- T29 Pause freezes remaining time; resume continues correctly; stop requires sensible behavior.
- T30 App sleep/OS interruption, notification/lock-screen and audio-permission scenarios as applicable to target OS.
- T31 Clock timing is accurate across many consecutive stages; completion exactly at total deadline.
- T32 Summary runs as final 15m phase, and final completion sound occurs at its end.

## Stage 5: packaging and final acceptance
- T33 Install/uninstall app on approved platform, run without network, persistence after reboot.
- T34 Audio audible and controllable on actual device; expected lock/background behavior documented.
- T35 Usability verification with live-model drawing/sculpting use cases.
- T36 Build version, release notes, license/privacy statement and installation instructions present as appropriate.

## Required evidence
For every executed test record: test ID, code commit, target platform/config, input, expected, actual, pass/fail, date and known limitations. A plan is NOT a passed test. Report real results in WORK_LOG.md (or a dated test-results file) and HANDOFF.md. Ask before modifying user-approved behavior to make tests pass.

## Acceptance gate for Stage 1
At minimum T01–T12, T15–T18 passing on the selected core stack, with documented disposition of Stage 1 edge rules. The full reference examples must match exact phase order and timestamps. Later-stage tests can remain pending.

## Step 1.4 local execution method and approval hardening (D-046)

- [Windows verification script](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/tool/verify.ps1) and [macOS/Linux verification script](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/tool/verify.sh) run `dart tool/smoke.dart`, `dart pub get`, `dart analyze` and `dart test` with a local `atelier-time-verification.log`.
- Test code on the branch additionally asserts the approved session preserves a **full immutable safety-warning record and exact 25-minute time offset**. `ScheduleCandidate` construction is now library-private so callers cannot fabricate a candidate for approval.
- **ALL Dart checks remain UNEXECUTED.** Current container lacks Dart/Flutter SDK and cannot resolve the Dart download host. Avoid GitHub Actions or paid runners under user's billing block. Install the **Dart SDK only** on an available local test machine to unblock independent execution; share the log for error correction.

## 2026-10-23 — First real compiler execution (FAILED; fix pending retest)
- **Environment:** Android Termux Dart 3.13.7 stable `android_arm64`.
- **Command:** `dart tool/smoke.dart` on user-cloned `stage-1-4-scheduling-core`.
- **Actual:** Compiler error `lib/src/scheduling.dart:621:28`, `num` cannot be assigned to `int?` at `offsetSeconds: crossingOffset`.
- **Fix committed:** `0dedd95756b6d3984b2e16548a58599179fbf7ff`; typed integer conditional instead of `math.max` inference.
- **Next:** User runs `git pull --ff-only && dart tool/smoke.dart` from `~/Atelier-Time` (no reinstall). After smoke success, run `dart pub get`, `dart analyze`, `dart test`. **No pass evidence for updated code yet.**

## 2026-10-23 — User-reported Step 1.4 Dart suite PASS (D-049)

**Environment:** Android Termux Dart SDK 3.13.7 stable on `android_arm64`; existing development-branch clone.

**Sequence:** Initial `dart tool/smoke.dart` compile FAIL in D-047 was fixed by `0dedd95756b6d3984b2e16548a58599179fbf7ff`, followed by user-confirmed smoke PASS (D-048). User was then instructed to run `dart pub get && dart analyze && dart test`, and responded **“All tests passed!”**.

**Evidence and limits:** Record **USER-REPORTED PASS** of the written Dart unit test suite and apparent success of the preceding analyzer step if the chained command ran as supplied. The currently committed `test/scheduling_test.dart` defines **34 unit tests**; user did not supply exact runner test count, stdout, `dart analyze` transcript, or local git commit SHA. Do not call those independently verified or confuse them with a multi-platform app build.

**Acceptance audit still needed:** Map current test source to Stage 1 required T01–T12, T15–T18 and any Step 1.3 policy acceptance tests not yet covered, adding and executing missing cases within authorized Step 1.4. Flutter UI, app packaging, OS-specific builds and device timer/audio behavior belong to later authorized stages. Do not mark Stage 1.4 completed, merge the branch, or initiate Stage 2 solely based on this high-level report. GitHub Actions budget remains untouched.


## 2026-10-23 — Screenshot-verified Android analyzer and test suite (D-050)

User provided Termux screenshots from `~/Atelier-Time` after the corrected smoke run, showing the output of `dart pub get && dart analyze && dart test` on **Dart 3.13.7, Android ARM64**:

```text
Changed 49 dependencies!
Analyzing Atelier-Time...
No issues found!
Built test:test.
00:01 +34: All tests passed!
```

**Evidence status:** Screenshot-confirmed **`dart analyze`: 0 issues**, and **`dart test`: 34 passed** for currently checked-out source. No failed tests shown. This upgrades earlier D-049 user-only report to verified screenshot evidence (D-050). Smoke check was separately confirmed as passed. No GitHub Actions, paid runner or build was used.

**Stage 1 acceptance mapping against the 34 currently passed test definitions:**

| Planned check | Existing written tests | Coverage disposition |
|---|---|---|
| T01–T08 A/B exact schedules and 3 final-pose modes | Canonical Example A/B six tests, exact timelines and final-pose choices | **Covered and screenshot-reported passing** |
| T09 custom ruler split limits, minimum 1m on either pose | Custom 34m+1m; 0m custom rejected; exact valid 1m+34m **not explicitly tested** | **Partial — add opposite endpoint test before Step 1.4 completion** |
| T10 exact final fit, no adjustment prompt | `exactly fitted last pose has no incomplete choice prompt` | **Covered** |
| T11 one special break replaces one ordinary slot | A/B baseline and manual ordinal 3, multiple-placement assertions | **Covered** |
| T12 break-first and pose-first, no phantom trailing break | A/B baselines; `break-first cannot finish on a trailing break` | **Covered** |
| T15–T16 deterministic output, full exact contiguous phase coverage | `identical rules generate identical phase IDs and timeline`; `expectContiguous` invariant helper | **Covered** |
| T17 floor/ceil even split with odd available pose total | A 17+18, B 15+15 | **Covered** |
| T18 no silent shortened-pose approval | `a shortened final pose requires an explicit choice` | **Covered** |
| Additional D-031/D-036/D-044 | Hard 0-minute errors, 25/5 warning/acknowledgment, equal/shorter special breaks | **Covered by written cases** |
| Wider T13–T14 and P-T edge matrix | Representative edge cases present; many combinatorial extremes not exhaustive | **Partial / remains in QA backlog** |

**Gate:** Step 1.4 scheduling core has strong real Android Dart success. **The written test plan still calls for T09 both endpoints. Do not mark Step 1.4 complete or merge into main until remaining required edge coverage is addressed and retested.** Flutter UI, APK, cross-platform builds and Stage 2 are not covered or authorized.
