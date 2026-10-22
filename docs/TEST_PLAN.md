# Test and acceptance plan — Atelier Time

Version: 0.3 planning baseline (2026-10-22). Tests are **planned, not executed**; no app or scheduling engine exists yet.

## Proposed additional tests from Step 1.3 (UNAPPROVED policies)

Only adopt the following as binding acceptance tests after user approval of the policies in [SCHEDULING_EDGE_POLICY.md](SCHEDULING_EDGE_POLICY.md). **None have been executed.**
- P-T01: Automatically choose the nearest feasible replacement-break **center** to work midpoint; A/B must choose ordinal #3 and exact 15-minute spans.
- P-T02: When distances tie, earlier eligible break ordinal is selected.
- P-T03: For N>1, select distinct feasible replacement breaks spread at target fractions of work period, deterministic result; no silent reduction of count.
- P-T04: Manually selected break ordinals survive valid edits or raise an explicit placement error when invalid; do not silently relocate.
- P-T05: Accept a zero-duration final summary and valid opening-only configuration; do not emit a zero-length phase.
- P-T06: Explicit errors for invalid total/summary/pose durations, too many replacement breaks and terminal break without following pose.
- P-T07: If no feasible last-two-pose split exists, mark equal/custom unavailable with explanation; remainder only if valid.
- P-T08: If the ruler explicitly chooses a last-two split beyond nominal pose duration, preserve total time and all invariant boundaries (pending user approval).

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
