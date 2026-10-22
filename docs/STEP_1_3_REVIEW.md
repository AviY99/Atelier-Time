# Atelier Time — Stage 1 Step 1.3 APPROVED scheduling decision (v1.0)

**User approval:** 2026-10-22  
**State:** **STEP 1.3 COMPLETED AND APPROVED** — user expressly approved the remaining policies as documented in the former v0.9 review. **D-036 overrides all older allowances for 0-minute sessions/breaks.**  
**Approval scope:** Rules and documentation ONLY. **STEP 1.4 NOT AUTHORIZED. No application code, Flutter scaffolding, tests or builds have started.**

This document is the consolidated **approved** Step 1.3 design. See [detailed approved policy](SCHEDULING_EDGE_POLICY.md), [decision log](DECISIONS.md), [examples](EXAMPLES.md), and [data-model proposal](DATA_MODEL.md). The earlier proposal for logical zero-duration break slots is **WITHDRAWN**.

## 1. Approved duration and warning policy

- **Complete session total must be > 0.** A 0-minute session is a **blocking error**.
- **Every configured ordinary break and active long/special replacement break must have positive duration**: 0 minutes is a **blocking error**, never an overrideable warning. With a whole-minute input editor, minimum actual break duration is 1 minute.
- New-session defaults are **5 minutes normal** and **15 minutes special**; both remain editable to other **positive** values. These example values are not hard-coded restrictions.
- **0 special replacements is allowed:** count 0 means no long replacement is requested, **not** a zero-duration break.
- **0-minute final reserved summary is allowed** if the full session and actual work remain positive: simply omit the summary phase. It is distinct from a 0-minute session.
- **D-031 remains approved:** after 25 minutes of accumulated posing without at least 5 minutes of rest, further posing violates the recommendation. Consecutive poses accumulate; **valid 1–4m rests** do not reset the pose counter, while breaks >=5m reset it. Any longer continuous pose exceeding 25 minutes also warns.
- A schedule that violates the **25/5 rest recommendation** may still be approved if all numeric inputs are valid: show the affected intervals, require an **explicit separate warning acknowledgment**, then normal schedule approval. Recomputing the sequence invalidates the prior acknowledgment.
- **A 0-minute break or session is never subject to this override** and must be corrected.

## 2. Approved P-01–P-03: placement of long/special replacement breaks

**P-01 One replacement:** select an eligible existing positive normal break whose **replacement interval center** is closest to the midpoint of **working time W**, excluding reserved summary. Ties go to the earlier eligible break ordinal. Candidate must leave a positive pose after the break, preserve opening poses and total time. The approved A/B fixtures must result in long break **A 01:20–01:35**, **B 01:25–01:40**.

**P-02 Multiple replacements:** for N>1, select **N distinct feasible existing normal break slots** approximately evenly spaced across W. Target their centers at fractions j/(N+1) of W, minimize the sum of absolute timing errors, and resolve ties by the earliest ascending ordinal list. Never silently reduce N or invent break positions. The configured replacement duration is shared by those N breaks unless the user later approves a different feature.

**P-03 Manual choice:** user may select specific eligible normal-break ordinals to replace; if future edits invalidate selected ordinals, display an error and ask the user to choose again. Never silently relocate manually selected breaks. A long break **replaces**, not supplements, the original short break.

## 3. Approved P-04: valid and invalid combinations

- **Hard errors:** nonpositive total session or configured actual break; negative fields/counts; a negative final summary or summary that consumes all session time; nonpositive count/duration of any included opening group; opening groups exceeding available work time; nonpositive regular pose; more requested replacements than feasible slots; break-first with no room for a positive following pose; any schedule that would end on a phantom break or lack a positive required pose.
- **Allowed:** 0-minute final summary with positive work time; zero **count** of special replacements; opening-only session when its groups fill the full work block exactly; exact fit ending on a pose without a final-shortening dialog.
- Never truncate specified opening poses, invent poses/breaks, append a trailing phantom break, auto-relocate manual positions, or silently change input numbers just to make the schedule work.
- Valid **positive** breaks of 1–4m, and over-25m posing, trigger the 25/5 **advisory warning** if needed, not a hard input error.

**OPEN D-038 — outstanding separate decision, explicitly NOT resolved by this approval:** whether a *positive* special/long break is allowed to be shorter than or equal to its replaced normal break. The prior review specifically labeled it **“Unresolved.”** Do **not** silently choose a policy; raise it before implementing that validation case. No other approved rule changes by leaving this narrow question open.

## 4. Approved 25/5 boundary and activation scope

- Posing time sums across all consecutive poses **within one complete activated session**, including opening groups and adjusted last-two poses.
- A break >=5m resets the counter; a valid 1–4m break does not.
- If an overall session ends **exactly at 25m** accumulated posing, with no further poses, **do not invent an additional break**. A pose continuing beyond 25m (such as 30m) triggers a warning even if it is the last pose.
- The app initially evaluates this warning **within each activated full session**; do **not** automatically carry pose minutes into a *different* saved preset activated later. Tracking across distinct presets is **out of current scope**, requiring new explicit approval to add.
- Warning analysis must be recalculated after changing parameters, placement or the chosen final-pose split.

## 5. Approved P-05–P-06: final poses and preview approval

- If the final repeating pose is incomplete and the last two eligible positive repeating poses are separated by a **positive existing break Q**, offer (1) remainder, (2) near-equal floor/ceil split at whole-minute precision, and (3) custom ruler split at 1-minute increments, >=1m per adjusted pose.
- Preserve all earlier phases, combined pose time, Q, total W and reserved summary. Move Q when adjusting the pair's timing but do not alter its duration.
- **Allow custom first pose > nominal P** if the user explicitly selects it and both resulting pose durations remain positive. Apply the normal 25/5 warning analysis to the result.
- If no eligible pair or positive intervening break exists, **disable unavailable adjustment choices and explain**, never invent a break or pose to enable them. Do not silently approve any incomplete final pose.
- Preview shows the full chronological phase list with start/end/duration, posing/rest totals, exact replacement placements, all feasible ending options, **hard errors separated from 25/5 warnings**, and any needed explicit safety acknowledgment.
- Any edit or resolution change invalidates the existing preview approval and warning acknowledgment. A saved approved revision uses the exact timeline accepted by the user.

## 6. Reference and verification commitments

Canonical examples remain fixed:
- **Example A:** 4×1m then 4×4m opening; recurring **break-first**, 25m poses, 5m breaks; third break replaced by 15m **01:20–01:35**. Last two pose options **25+10**, **17+18**, sample custom **20+15** with unchanged intervening 5m break. Summary 02:45–03:00.
- **Example B:** no openings, **pose-first** 25m/5m; third break replaced by 15m **01:25–01:40**. Last two pose options **25+5**, **15+15**, sample custom **18+12**. Summary 02:45–03:00.
- Both baseline sessions total **180 minutes = 130m posing + 35m breaks + 15m summary**.
- Tests have only been **planned**, not run: see [TEST_PLAN.md](TEST_PLAN.md). Exact schedule/time correctness must be measured when Step 1.4 is independently authorized.

## 7. Approval boundary

**Step 1.3 is complete.** User authorized recording documentation approval only. **Do not** start **Step 1.4**, Dart scheduling engine, Flutter project, automated tests, dependencies, build pipelines, screens or timer unless the user explicitly authorizes that next sub-step.

**Current next action:** Confirm completed documentation and GitHub read-back, link this approval record and [HANDOFF.md](HANDOFF.md), and stop. At the user's next instruction, seek authorization for Step 1.4, while preserving the single narrow open positive-special-break-vs-normal-break length question for future resolution before the relevant implementation case.
