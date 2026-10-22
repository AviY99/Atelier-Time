# Stage 1, Step 1.3 — Long-break placement and scheduling edge-case policy

**Date:** 2026-10-22  
**Status:** PARTIALLY APPROVED: user approved the minimum break and break defaults on 2026-10-22 (see D-030). Other Step 1.3 rules P-01–P-06 remain REVIEW PROPOSALS.  
**Scope:** Design rules ONLY — no algorithm implementation, Flutter project or automated tests.  
**Next gate:** User approval of these rules before entering the next technical step (Step 1.4: scheduling engine implementation and tests).

This document preserves already approved requirements, notably all numbers editable per full session, long breaks **replacing** short breaks, 180-minute examples A/B, a reserved final summary, and the three end-pose allocation choices. **Approved break rules (D-030):** every configured break duration has a strict **minimum of 1 minute (60 seconds)**; the new-session defaults are **5 minutes for regular short breaks** and **15 minutes for the middle/replacement break**. Both defaults are user-editable, never hardcoded constraints. A user may select **zero long-break replacements** without creating a zero-duration break. This approved clarification overrides the older P-04 zero-short-break suggestion. Other proposals below still require approval.

## 1. Agreed scheduling principles (already approved)

- Work window is `W = totalSessionSeconds − finalSummarySeconds`.
- Ordered introductory pose groups are individually expanded; group counts and durations are editable.
- After the opening section, the regular cycle begins with either a break or a pose, as configured.
- One long break takes the place of one scheduled short break. Never place both at the same boundary.
- A long break goes between poses, never interrupts a pose.
- A user can review the complete elapsed-time schedule and the placement of every break before approval.
- A shortened final pose requires the explicit choice of remainder, near-equal last-two split or adjustable custom split when feasible. The reserved summary and total duration are not changed by that choice.
- Both canonical Example A and Example B position their sole long replacement at short-break ordinal **3**, after cycle starts (see [EXAMPLES.md](EXAMPLES.md)).

## 2. Proposal P-01 — Automatic 'middle' placement (one long break)

The target is the **midpoint of the work window**, `W/2`, not the midpoint of the whole session including summary.

Eligible candidates are **existing scheduled short-break positions** such that the final complete schedule:
1. keeps opening pose groups intact,
2. places that long break **between two positive-duration poses**,
3. never ends the work window on a break,
4. keeps total duration and final summary exactly as configured.

For each eligible slot, tentatively replace the short-break duration with the requested long-break duration and calculate the resulting timeline (including a possible shorter final repeating pose). Choose the feasible candidate whose **long break's center** is closest to `W/2`. If two slots tie, choose the earlier slot (smaller ordinal). Display the placement and time in review.

**Reference-check illustration (165m work):**
- A (4×1m + 4×4m then break-first 5/25): third short break starts at 01:20; with replacement it runs **01:20–01:35**, midpoint 01:27:30, five minutes after the work-window center 01:22:30. This is the nearest feasible break in the canonical example.
- B (25/5 pose-first): third short break starts 01:25; with replacement it runs **01:25–01:40**, midpoint 01:32:30, ten minutes after work-window center. The canonical example remains unchanged.

The *starting times* of candidate breaks may move when prior long breaks are inserted; comparisons are made on each fully proposed timeline, not just an unmodified short-break list. This is a decision specification, not yet an implemented algorithm.

## 3. Proposal P-02 — Two or more long breaks

`count=0` is valid: no long replacements.

For `count=N>1`, select **N distinct existing short-break slots**. Long breaks should be **approximately evenly spread across the work window**, with their centers targeting fractions `j/(N+1)` of W for `j=1...N`.

Compare only feasible final schedules. Choose the placement whose ordered long-break centers minimize the sum of absolute differences from those targets; ties choose the lexicographically earlier ascending list of short-break ordinals. Do not silently reduce N if requested N is impossible.

All selected replacement breaks have the same configured long-break duration for now; differing long-break durations would require a future separate product decision.

This placement rule optimizes position only. The chosen schedule still must preserve entire approved opening poses and valid final-pose resolution options; no earlier pose should be silently shortened. User can switch to manual placement.

### Complexity note
The algorithm implementation may use an efficient search/optimization method; this policy does **not** require checking every combination blindly on large schedules. Whichever implementation is chosen must produce deterministic results and identical outputs across all four platforms. Precision and feasibility must be tested in Step 1.4.

## 4. Proposal P-03 — Manual long-break positioning

Let user choose the **ordinal of a normal scheduled break** (1st, 2nd, 3rd, etc.) to replace. For multiple long breaks, user chooses N **distinct** ordinals in ascending order. Display each selected ordinal and its actual computed elapsed time in the full preview.

Example A: choosing break #3 gives 01:20–01:35. Example B: choosing break #3 gives 01:25–01:40. The same ordinal has different clock times because opening pose groups differ.

On editing parameters that invalidate a previously selected ordinal, show an error and request a new choice. **Do not silently relocate** a manually selected long break. Previously approved saved schedule remains frozen until replacement is explicitly approved.

## 5. Proposal P-04 — Input ranges and special cases

Every numerical field is editable, but not every combination can generate a meaningful legal session. The following are proposed validation behaviors; they require user approval before implementation.

| Condition | Proposed behavior |
|---|---|
| Total duration ≤ 0 | Error, cannot approve |
| Final summary duration < 0 or ≥ total session | Error: require a positive work period |
| Final summary exactly 0 | Accept: no summary phase; final completion occurs at total time |
| Opening pose-group count ≤ 0, duration ≤ 0 | Error on an included group; remove group if it is not needed |
| Total opening-group duration > work window | Error; do not shorten required opening poses |
| Opening poses exactly use the entire work window | Allow an **opening-only** session, if no long breaks are requested; do not add trailing break |
| Regular pose duration ≤ 0 | Error |
| Short break duration = 0 | **APPROVED (D-030): Error.** If a break is configured its duration must be **at least 1 minute**. Zero is not a valid shortcut for removing breaks; the rules for a break-free cycle, if needed, require a separate explicit design decision. |
| Short break duration < 0 | **APPROVED (D-030): Error.** Break duration cannot be below 1 minute. |
| Long replacement count < 0 | Error |
| Long replacement count = 0 | **Still a proposed policy**: allow no long break, independently of the 1-minute minimum for any actual break duration. No zero-minute break is produced. |
| Long replacement duration = 0 or < 1 minute | **APPROVED (D-030): Error** when that break is configured. An actual long/middle break must be at least 1 minute. |
| Long replacement count > 0 and long duration ≤ short duration | **PROPOSAL ONLY:** Error because a 'long' break should exceed the short break it replaces. The minimum-1-minute rule is approved; the *strictly longer* comparison is **not yet approved**. |
| Requested N long breaks but fewer than N eligible short-break positions | Error; never silently lower N or add extra breaks |
| Work window too short for requested first break and following positive pose (break-first) | Error; cannot end with a trailing break |
| After replacements, final repeating pose would have < 1 minute or zero length | Error; ask to alter durations/count/placement rather than erase the last pose or leave a trailing break |
| Exact fit at a pose boundary, including an opening-only schedule | No partial-final-pose question |
| Summary=0 with valid working poses | Valid: final pose ends at total duration; unique final-session completion cue still required later |
| A saved preset is edited | Generate a new candidate and require fresh approval; keep original saved approved timeline unchanged |

**Approved break distinction:** a break duration of **0 minutes is invalid**; the 5-minute short and 15-minute middle values are just editable input defaults, never mandated lengths. The proposed one-minute minimum for a repeating **pose** derives separately from the previously approved minute-based custom slider. Internal time may use integer seconds, but the first editor works in minutes. Other P-04 behaviors, including opening-only sessions and optional break count, await approval.

## 6. Proposal P-05 — Final incomplete-pose edge behavior

The already approved three-choice rule applies if the final repeating pose is shorter than its configured regular duration.

- If the preceding regular pose and an intervening break are present and there is at least one minute for each of the last two poses, show **all three** options.
- **Remaining time:** keep earlier pose full length and leave final pose shortened.
- **Near 50/50:** floor/ceil in whole minutes (extra minute to last pose).
- **Custom ruler:** the first of the final two poses receives an integer number of minutes between 1 and `availablePoseMinutes-1`, and the second receives the remainder. The break stays fixed-length and moves in time as the split changes.
- If there is only one pose, or the last-two-pose + break structure does not exist, show the **remaining** option if viable. Clearly mark 50/50 and ruler unavailable and explain why; do not invent poses/breaks.
- If no legal positive final pose can be generated, the schedule is invalid and must be edited; never present a fake valid solution.

A change to last-two-pose durations may yield a first pose longer than its nominal cycle duration if the user drags the ruler far. **Proposal:** allow this as an explicit custom split when both poses stay positive, since the user deliberately requested that division; display the actual durations.

## 7. Proposal P-06 — Review and decision transparency

For every candidate, show:
- Session total, work-period length and final summary duration;
- Total posing and break time;
- Number and duration of long break replacements;
- Exact chronological stages with elapsed start/end/duration;
- The currently chosen automatic/manual placement;
- Warnings/errors and the field(s) to adjust when invalid;
- All feasible final-pose options, with updated times before approving.

If user changes a numerical rule or chosen break ordinal, previous generated preview/approval is invalidated. No hidden automatic changes or premature saving.

## 8. Regression examples and design-only verification

The canonical 3-hour A and B baseline schedules from [EXAMPLES.md](EXAMPLES.md) must be reproduced **exactly**:
- A: long break **01:20–01:35**, last pose in remainder mode **02:35–02:45**.
- B: long break **01:25–01:40**, last pose in remainder mode **02:40–02:45**.
- The final summary stays **02:45–03:00** in both.
- A 50/50: **17 + 18 minutes**; B 50/50: **15 + 15 minutes**.
- A custom sample **20 + 15 minutes**; B custom sample **18 + 12 minutes**.
- Total of every chosen schedule **180 minutes**, never 190.

This Step 1.3 has verified the proposals **manually against the known reference timings**. No scheduling engine exists and no executable tests have been run.

## 9. Open approvals / next gate

Please review and approve/revise the proposed policies P-01 through P-06. All are **proposals**, not newly user-approved product requirements yet. In particular:

- Is automatic middle placement by **closest long-break midpoint** acceptable?
- Should two or more long breaks be spread evenly by default?
- Should invalid parameter combinations be **shown with an explanation instead of silently 'fixing' the numbers**?
- Should a custom ruler be permitted to make one of the final two poses **longer than the nominal pose length**, provided both remain positive?
- **Already approved, not awaiting another vote:** minimum duration of an actual break is **1 minute**; pre-filled **regular break 5 minutes** and **middle break 15 minutes**, all editable. This replaces the prior zero-minute-break proposal.

Once the user approves the behavior, record those choices as approved in DECISIONS.md before entering Stage 1 Step 1.4: implementation of the Dart scheduling engine and associated automated tests. Do not begin Step 1.4 without separate authorization.

**STOP: Documentation only. No program code, Flutter scaffold, UI, audio or automated tests created.**
