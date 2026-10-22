# Test and acceptance plan — Atelier Time

Version: 0.3 planning baseline (2026-10-22). Tests are **planned, not executed**; no app or scheduling engine exists yet.

## Approved requirement tests: D-031 + D-036 (NOT EXECUTED)

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
- P-T11: **OPEN D-038 — NOT AN APPROVED ACCEPTANCE EXPECTATION:** Whether a positive special break may be <= its positive normal break remains unspecified. Define the expectation with the user before coding the comparison; never guess it.
- P-T12: **Approved D-036:** Strictly no zero logical break slots; do not generate 0m break phases or allow 0m configured breaks as an overrideable safety warning.

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
