# Scheduling contract and algorithm design

Status: Core product behavior and **Step 1.3 scheduling placement/edge policies APPROVED (D-037)**. See [SCHEDULING_EDGE_POLICY.md](SCHEDULING_EDGE_POLICY.md) and [approved review](STEP_1_3_REVIEW.md). D-036 invalidates 0-minute sessions/configured breaks. **No scheduling engine or tests implemented; Step 1.4 not authorized.**

## Time vocabulary
All times displayed as elapsed from the session start (00:00). Internally prefer integer duration units and monotonic elapsed-time measurement; no floating-point accumulation. Example durations are whole minutes. Preserve the ability to adopt seconds later without altering invariants.

- `D`: total session length
- `F`: final summary duration
- `W = D-F`: work block length (all poses + breaks)
- `O`: sequence of optional opening pose groups
- `P`: nominal repeating pose duration
- `B`: short break duration — **new-session default 5 minutes**, editable to any valid **positive** duration; 0 is invalid (D-036).
- `L`: middle/long replacement break duration — **new-session default 15 minutes**, editable to any valid **positive** duration when configured; 0 is invalid (D-036).
- `N`: configured count of long breaks
- `cycleStart`: pose-first or break-first

The examples set `D=180, F=15, W=165, P=25, B=5, L=15, N=1`. Example A opening groups 4×1 then 4×4 and break-first; Example B no openings and pose-first. The 5m/15m values remain editable *positive* defaults. **D-036 is the latest correction:** a full session of 0 and a configured break duration of 0 are **hard errors** (initial whole-minute inputs require at least 1m per break). D-031 still requires a user-acknowledged **warning**, not a hard error, for legitimate sessions violating the **25m posing / >=5m rest** recommendation.

## Baseline generator
1. Validate every configured duration and count before scheduling. Reject negative durations, summary longer than total, and sequences that cannot legally fit.
2. Add opening poses in their configured order, no automatic breaks between poses unless specifically configured.
3. Add repeating poses and interleaving scheduled short breaks according to cycleStart, stopping at work-block boundary. Never insert a break that would terminate the work block without a pose afterwards.
4. Select eligible short-break slot(s) for replacement by long break(s), typically near the middle; **do not add a second break** at that boundary. Since replacement consumes `L-B` extra time, regenerate or account for this before fixing final pose length.
5. Show long-break placement in preview. **Approved D-037:** choose eligible existing short-break position closest to W/2 by proposed **replacement interval center**, tie earlier ordinal; for N>1 distribute centers roughly at j/(N+1), and allow manual ordinal overrides. Reproduce both canonical schedules exactly. Implementation/test remains future Step 1.4.
6. If the final repeating pose is shorter than nominal, flag it for user selection of one of the three approved resolutions; do not auto-approve.
7. Append the final summary of exactly `F` and enforce `sum(all durations) = D` and unbroken contiguous interval coverage.

## Two-pose resolution
Where the last two poses are separated by an unchanged break:
- Let `S` be the start of the earlier of the last two poses, `E=W` its work-block end, `Q` the intervening break duration, and `T=E-S-Q` available combined pose time.
- **Option 1**: first pose remains the planned regular duration `P`; last pose becomes `T-P`.
- **Option 2**: first pose `floor(T/2)`; last pose `ceil(T/2)`.
- **Option 3**: choose first duration `x` by ruler and let last duration be `T-x`.
- Start/end boundaries become: first pose `[S,S+x]`, break `[S+x,S+x+Q]`, last pose `[S+x+Q,E]`.
- At proposed one-minute increments require `1 <= x <= T-1`; exact slider implementation and constraints to be checked in Stage 1.
- If no valid two-pose + interposing-break structure exists, do not invent one; explain why options 2/3 are unavailable.
- In all cases preserve previously fixed phases, `D`, `F`, `Q`, and all other breaks. Preview the complete change before user approval.

## Reference placements
- **Example A:** opening 00:00–00:20, long break 01:20–01:35, summary 02:45–03:00; last two poses span 02:05–02:45 with intervening 5-minute break.
- **Example B:** long break 01:25–01:40, summary 02:45–03:00; last two poses span 02:10–02:45 with intervening 5-minute break.
See EXAMPLES.md for the entire time-ordered lists and each alternative.

## Invariants to verify for every generated preview and saved preset
1. Each duration is a nonnegative integer (poses strictly positive).
2. No overlaps/gaps; stage 1 begins at zero and final end is D.
3. Final summary occupies exactly `[D-F,D]`.
4. Every long replacement replaces one eligible short break; no double break.
5. The last-two split preserves their combined time and the intervening break length.
6. Sound transitions are derived from actual schedule edges.
7. The preview and live timer use an identical persisted, approved schedule.
8. **Approved break-safety warning:** count posing cumulatively until a break lasting **at least 5 minutes**; when posing continues after 25 accumulated minutes, flag the violation and require explicit acknowledgement before approval. A pose exceeding 25 minutes also warns. **Valid 1–4-minute breaks** do not reset the counter. A configured **0-minute break or total session is a blocking input error**, not an overrideable rest warning. Revisions invalidate warning acknowledgement. See [SCHEDULING_EDGE_POLICY.md](SCHEDULING_EDGE_POLICY.md).

## Stage 1 open engineering edge cases

**Step 1.3 proposals P-01–P-06 have been approved** in [SCHEDULING_EDGE_POLICY.md](SCHEDULING_EDGE_POLICY.md) / [STEP_1_3_REVIEW.md](STEP_1_3_REVIEW.md) as D-037. The one deliberately unresolved positive-special-break <= normal comparison remains **OPEN D-038**; other later-stage semantics (audio, summary meaning) also remain open. Example A/B expectations unchanged. No code may begin until Step 1.4 is separately authorized.
**Approved D-037:** (a) zero summary with positive work, (b) multiple long breaks, (c) reject overlong opening groups, (d) reject break-first without room for a following pose, (e) zero optional special-break *count* (no 0-minute break), (f) disable infeasible final-two options, (g) midpoint and earlier ties, (i) positive custom split longer than nominal when explicitly selected, (k) **per-activated-full-session** warning counter, no cross-preset carryover. **OPEN D-038:** (h) positive special break <= positive normal. (j) per-pose versus per-set sound is for a later stage. **D-036** forbids 0-minute sessions/configured breaks. **D-031** retains the 25/5 advisory warning and explicit acknowledgment.
