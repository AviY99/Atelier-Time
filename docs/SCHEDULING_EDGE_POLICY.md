# Stage 1 Step 1.3 — Scheduling and edge-case policies

**Updated:** 2026-10-22  
**Status:** Partly approved: latest user correction **D-036** requires positive full-session and configured break durations; the **D-031 accumulated posing 25m / qualifying break 5m safety warning** remains approved. Placement and further edge policies P-01–P-06 are **PROPOSALS for user review**.  
**Scope:** Documentation only. **Step 1.4 programming is NOT authorized.**

The latest decision **D-036 supersedes only D-031's earlier allowance of a 0-minute break**. Earlier discussion of 0-minute *logical break slots* was explicitly withdrawn, not approved. A 0-minute **full session** or **configured normal/special break** is an **error**, never an acknowledged safety-warning exception. No numeric duration is hardcoded as the only permissible positive value: the new-session defaults are 5m normal and 15m special.

## 1. Approved invariants and vocabulary

- Total session time `D` must be **positive**. Work time is `W = D − F`, where `F` is an optional final reserved-summary period (whether F=0 is acceptable remains a separate edge-policy proposal, not the same as a zero-length *session*).
- Opening pose groups have editable ordered positive pose counts and positive durations; every number in Examples A/B is editable per complete session.
- Normal break `B` and active special/long replacement break `L` each have **strictly positive duration**. The minute-based initial editor therefore requires **at least 1 minute** for a configured break.
- A special/long break **replaces** an existing scheduled normal break, rather than appending one; it is placed between poses.
- **0 special break replacements** (count `N=0`) may represent *no special break*, not a 0-minute break. This zero-count setting is still part of the Step 1.3 proposal for sign-off.
- All actual timeline phases have positive length, contiguous non-overlapping intervals, and exactly sum to `D`. User reviews the complete timeline and explicitly approves before saving/activating.
- If the final repeating pose is incomplete, offer the approved remainder, near-equal and custom-ruler choices where structurally feasible. No silent acceptance.
- The approved Example A and B schedules are unchanged, including each third ordinary break being replaced by a 15-minute special break.

## 2. Approved D-031 + D-036: accumulated posing/rest warning

The **25/5 rule is an advisory schedule constraint**, separate from hard mathematical/input validity.

1. Count **total posing duration since the last break lasting at least 5 minutes**. Consecutive short poses count together; pose boundaries alone do not reset the counter.
2. A valid **1–4-minute** rest does **not** meet the five-minute recommendation and therefore does not reset this counter. A break of **5 minutes or longer** resets it.
3. When posing continues after the counter reaches 25 minutes without a qualifying break, or a pose itself continues beyond 25 minutes, show a clear **warning with the affected times/pose intervals**. A break inserted after the threshold cannot retroactively remove the warning.
4. **User may keep a valid shorter rest** (e.g. 1 or 3 minutes) or a longer pose by explicitly acknowledging the warning in the schedule preview before ordinary approval. Never adjust the user's numbers silently.
5. **A 0-minute break is not an exception**: it is a blocking validation **error** under D-036, and acknowledgment cannot make it valid.
6. Recompute warnings after edits, after choosing a final-two-pose allocation, and after changing a special-break placement. Prior warning acknowledgment and schedule approval are invalidated by edits.
7. A special/long break of at least 5 minutes is also a qualifying rest. The undefined `summary` period is not assumed to be recovery rest.

**Examples:**

| Sequence | Result |
|---|---|
| 5×5m poses → 5m rest → further pose | No warning |
| 25m pose → 5m rest → further pose | No warning |
| 30m pose → 5m rest | Warning; explicit acknowledgment may approve |
| 25m pose → 3m rest → another pose | Warning; explicit acknowledgment may approve |
| 20m pose → 3m rest → 6m pose | Warning at accumulated 25m; explicit acknowledgment may approve |
| 25m pose → **0m break** → another pose | **Hard error**, not an overrideable safety warning |
| **0m total session** | **Hard error**, no schedule can be approved |

**Pending boundary policy (not separately approved):** exactly 25m posing at the *end* of a full session does not require an invented trailing break. A 30m uninterrupted final pose already exceeds the limit and warns. It is still unresolved whether counters should continue across separately activated *saved presets*; do not assume cross-preset accumulation.

## 3. P-01 (PROPOSAL): Automatic single replacement-break position

- Compute work midpoint `W/2`, excluding the final summary.
- Select from **existing scheduled positive-duration short breaks** for which the tentative complete schedule remains feasible, has a positive-duration pose after the break, preserves opening poses and exactly ends at `D`.
- Replace the short break with configured `L>0`, calculate candidate actual positions, and minimize the distance of the **center of the replacement break** to `W/2`. On a tie, pick the earlier eligible ordinal. Show actual time in preview.
- **A:** third break 01:20–01:35. **B:** third break 01:25–01:40. The exact canonical schedules are non-negotiable regression fixtures.
- No candidate is chosen solely to avoid a 25/5 warning if it violates the above placement rule; warn on the resulting full schedule.

## 4. P-02 (PROPOSAL): Multiple replacement breaks

If `N>1`, choose N **distinct eligible normal-break slots** whose replacement centers are approximately evenly distributed at `j/(N+1)` of work time, for `j=1...N`. Minimize ordered sum of absolute timing deviations, with lexicographically earlier ordinal set resolving ties. Never silently reduce N or add imaginary breaks. The configured replacement duration is common to all N unless separately approved later. `N=0` means no replacement (not a zero-duration rest).

## 5. P-03 (PROPOSAL): Manual placement

The user selects actual existing break-slot ordinals (1st, 2nd, 3rd...) to replace; for multiple replacements, select N distinct ordinals. The preview displays the actual elapsed time. If an edit makes an ordinal impossible, show an error and request correction; **do not silently relocate**. An invalid 0-minute configured break does **not** form an eligible slot.

## 6. P-04 (PROPOSAL): Numeric and structural edge policies

These proposals remain subject to explicit user approval **except** the positive-time restrictions marked APPROVED.

| Case | Proposed behavior/status |
|---|---|
| `D <= 0` | **APPROVED D-036: Hard error**, never start/approve |
| Normal or active special break `<=0` | **APPROVED D-036: Hard error**, never override through 25/5 warning |
| Negative counts/durations | Error |
| Final summary `F<0` or `F>=D` | Proposed error: need positive work block |
| `F=0`, with `D>0` and a valid work schedule | Proposed valid: no summary phase |
| Invalid opening group count or pose duration `<=0` | Error; user can remove unused group |
| Opening groups exceed work window | Error; do not silently truncate required poses |
| Opening groups exactly fill work time | Proposed valid **opening-only** session if no following break/pose is needed |
| Repeating regular pose duration `<=0` | Error |
| `N=0` special replacements | Proposed valid; no special break emitted |
| `N<0` or `N` exceeds eligible ordinary breaks | Error; no silent adjustment |
| Special break positive but not longer than normal break | **Unresolved:** minimum positive is approved; whether it must be strictly longer than `B` still needs approval |
| Break-first but no room for following positive pose | Proposed error; never end with trailing break |
| No valid positive last repeating pose remains | Proposed error; no invented poses |
| Exactly fitted last pose | No final-pose-shortening choice necessary |
| 25/5 advisory violation with otherwise positive legal inputs | **APPROVED D-031: warning** + explicit acknowledgment, *not* input error |

A whole-minute input UI means the smallest positive configured break is **1 minute**; positive sub-minute support is not authorized or required now.

## 7. P-05 (PROPOSAL): Final incomplete-pose edge options

Where the last two positive repeating poses have a **positive-duration intervening break**, `Q>0`, preserve their combined posing duration `T` and unchanged break duration. Show three options if feasible:
- **Remainder:** keep preceding regular pose full nominal duration; final receives leftover positive time.
- **Near-even:** first floor(T/2), second ceil(T/2), at one-minute precision.
- **Custom:** user selects the first pose length from 1 through T−1 whole minutes; second is the rest; update actual break start/end without changing `Q` or session total.

If no preceding eligible pose or positive intervening break exists, do not invent a split structure: disable infeasible options and explain. **Proposed** permit a custom first pose longer than nominal if both remain positive.

## 8. P-06 (PROPOSAL): Preview, warnings, validation and approval

- Show all editable rules, exact phase timestamps, posing/rest totals, number/positions of special breaks, and full final-pose alternative times.
- Clearly distinguish **blocking errors** (`D=0`, active break `B=0`, invalid math) from **non-blocking safety warnings** (valid 1–4-minute break or over-25-minute posing).
- Explicitly acknowledge each affected schedule revision's safety warnings before approving; never silently waive a warning.
- Changes regenerate the entire preview and invalidate both prior approval and safety-warning acknowledgment.
- The timer later consumes only the saved immutable approved sequence; no implementation exists now.

## 9. Next gate

Pending approval: P-01–P-06, treatment of exactly-25-minute session endings, scope of accumulation across distinct saved presets, and whether a positive special break may be shorter than the ordinary break. **Withdrawal:** all proposed "zero-minute logical break slot" behavior from older Step 1.3 drafts is invalid after D-036 and must not be implemented.

Read the consolidated [Step 1.3 review sheet](STEP_1_3_REVIEW.md) for a compact summary of this same **current** policy. **Stage 1 Step 1.4 code, testing, Flutter scaffolding and UI remain unapproved.**
