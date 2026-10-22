# Stage 1, Step 1.3 — Long-break placement and scheduling edge-case policy

**Date:** 2026-10-22  
**Status:** PARTIALLY APPROVED. The user's latest break-safety and zero-break clarification (D-031) SUPERSEDES the former 1-minute minimum attributed to D-030. The remaining placement/edge policies P-01–P-06 are still review proposals.  
**Scope:** Design rules ONLY — no algorithm implementation, Flutter project or automated tests.  
**Next gate:** Review the latest consolidated [Step 1.3 decision sheet](STEP_1_3_REVIEW.md) (including zero-duration logical break slots and 25/5 boundary nuances), approve or revise pending policies, then separately authorize Step 1.4.

This document preserves approved requirements including every number being editable, the long break **replacing** a short-break slot, A/B examples, a reserved final summary, and the three end-pose choices. **Latest approved break clarification D-031 (superseding D-030's prior claimed 1-minute minimum):** the **regular-break default remains 5 minutes**, and the **long/replacement-break default remains 15 minutes**, but **0 minutes is allowed** for a break duration; user can override the recommended **5-minute rest after 25 minutes of accumulated posing without an adequate break**. Every violation creates a warning that must be explicitly acknowledged before the schedule can be approved. Other proposals below still require user approval.

## Approved D-031 — 25-minute posing / 5-minute adequate-break warning

**User clarification dated 2026-10-22; approved requirement, not an optional proposal.**

1. Keep a **cumulative posing-duration counter** (not the number of poses) from the last **qualifying break of at least 5 minutes**, starting at zero at the beginning of the overall session. Consecutive 1-, 4-, 5- or 25-minute pose phases all contribute; boundaries between poses do not reset the counter.
2. A break of **0 minutes is allowed**, as is a shorter-than-5-minute break. A 0-duration break does not create a schedule phase or consume time. A 1-4-minute break does not meet the 5-minute recommendation and **does not reset the accumulated posing counter**.
3. If **further posing is planned** after the cumulative posing time has reached 25 minutes without an intervening break of at least 5 minutes, or if a single pose necessarily exceeds 25 minutes before such a break, show a **warning**. The warning states the affected pose interval(s), accumulated posing duration and available ways to fix the rules (insert/lengthen a break or shorten a pose) or **continue anyway**.
4. The warning is **not a hard validation error**. The user may deliberately keep a 0-minute normal break, a break shorter than 5 minutes, or a long pose by explicitly acknowledging the warning. It must be acknowledged separately from normal final schedule approval, and the acknowledged choice must be stored with the specific approved schedule revision for traceability.
5. Recalculate warnings after every change to session rules, after selecting the 50/50 or custom final-pose split, and after changing a long-break position. **Any change invalidates prior safety acknowledgement and schedule approval.**
6. Only a **positive break duration >=5 minutes** resets the accumulated posing counter to zero. The long replacement break counts as a qualifying break if its actual duration is >=5. The reserved final `summary` phase is not silently counted as a recovery break because its activity has not been defined.
7. **Proposed interpretation for review:** When a session concludes at exactly 25 minutes of accumulated posing with no further pose scheduled, do not create an extra break solely to satisfy the guideline. This rule addresses **continuing posing without sufficient rest**. For separate saved full sessions run back-to-back, carryover between sessions is **not yet specified**, because the user's phrase "accumulated sessions" may also refer to successive short pose segments within one overall session. Ask if cross-preset tracking is wanted before assuming it.
8. The full timeline still must satisfy hard mathematical rules: nonnegative durations, exact total time, no gaps or overlapping nonzero phases. Zero break slots may be **logical boundary positions**, not actual zero-length timeline phases; replacement breaks may occupy such positions, subject to review and the existing replacement-not-addition requirement.

**Examples:**
- 5 consecutive 5-minute poses, then a 5-minute break, then more posing: **no warning**.
- 25-minute pose then 5-minute break then another pose: **no warning**.
- 30-minute pose then 5-minute break: **warning** (the 5-minute break comes too late).
- 25-minute pose then 0-minute break then further pose: **warning and approval required**.
- 25-minute pose then 3-minute break then further pose: **warning and approval required**.
- A/B canonical schedules retain their exact timings and do not trigger this warning.

### Technical consequence

The `ScheduleCandidate` must be capable of containing **non-blocking warnings** and a separate explicit **warningAcknowledgement** state in the future `ApprovedSessionPreset` or its associated approval metadata. That state is not implemented in Step 1.3. Do not mistake a warning for an error, and do not silently set `approved=true` after warning presentation.

---

## Additional review refinement — not yet approved

The detailed [Step 1.3 decision sheet](STEP_1_3_REVIEW.md) proposes preserving zero-minute regular breaks as **logical break slots** rather than zero-length elapsed-time phases. This would allow manual special-break replacement of a slot even when the regular break duration is 0, permit adjusting the last two poses across an existing logical 0-minute break, and retain precise ordinal placement. It also proposes treating cumulative pose time as local to one full activated session rather than automatically carrying it over to separately activated saved presets. These are **PROPOSALS** until user approval; approved D-031 only mandates that zero-minute durations are allowed and insufficient rest must trigger acknowledged warnings.

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
| Short break duration = 0 | **APPROVED (D-031): Allowed** as a deliberate no-rest slot; show 0 in inputs but emit no zero-duration phase. Assess accumulated posing and warn on 25/5 recommendation violations; require explicit override. |
| Short break duration < 0 | Error: negative duration remains invalid; **0 is explicitly allowed** by D-031. |
| Long replacement count < 0 | Error |
| Long replacement count = 0 | **Still a proposed policy**: allow no long break; unlike a 0-minute duration, zero count means no replacement slots are selected. |
| Long replacement duration = 0 | **APPROVED D-031**: value 0 is not invalid by itself. **Proposed slot semantics:** selected replacement slot yields no break phase (0 rest); show skipped replacement explicitly in preview and evaluate safety warnings. |
| Long replacement count > 0 and long duration ≤ short duration | **REQUIRES USER REVIEW:** Do NOT reject 0 by this proposed comparison, because D-031 allows 0. Suggest treating a 'long-break slot' as an editable replacement slot even if set to 0, with a preview label showing no rest; duration less than the normal short break is permitted subject to the same 25/5 warning approval. |
| Requested N long breaks but fewer than N eligible short-break positions | Error; never silently lower N or add extra breaks |
| Work window too short for requested first break and following positive pose (break-first) | Error; cannot end with a trailing break |
| After replacements, final repeating pose would have < 1 minute or zero length | Error; ask to alter durations/count/placement rather than erase the last pose or leave a trailing break |
| Exact fit at a pose boundary, including an opening-only schedule | No partial-final-pose question |
| Summary=0 with valid working poses | Valid: final pose ends at total duration; unique final-session completion cue still required later |
| A saved preset is edited | Generate a new candidate and require fresh approval; keep original saved approved timeline unchanged |

**Approved break distinction (D-031):** durations **0 minutes and above** can be entered for normal and special breaks. The new-session defaults (normal 5 minutes, middle 15 minutes) are just prefilled editable values, not minimums. Zero means no break time, not a zero-length phase. Accumulated posing over 25 minutes without a qualifying 5-minute break produces an explicit warning, not an error. One-minute minimum for **custom-split poses** is a distinct previously approved rule. Other P-04 behaviors (such as opening-only session handling) await approval.

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
- **Already approved D-031 (newest instruction):** break time may be **0 minutes**, overriding the older 1-minute restriction described in D-030; initial values remain **normal 5 minutes** and **middle/replacement 15 minutes**, both editable. The 25-minute accumulated posing / 5-minute qualifying break rule is **a warning requiring acknowledgement**, not a hard restriction.

Once the user approves the behavior, record those choices as approved in DECISIONS.md before entering Stage 1 Step 1.4: implementation of the Dart scheduling engine and associated automated tests. Do not begin Step 1.4 without separate authorization.

**CONTINUED REVIEW 2026-10-22:** [STEP_1_3_REVIEW.md](STEP_1_3_REVIEW.md) consolidates all outstanding user-facing decision points after D-031. This step remains documentation only; the additional policies are not yet approved. **No code, Flutter scaffold, UI, audio or automated tests created.**
