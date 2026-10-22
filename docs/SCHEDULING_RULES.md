# Scheduling contract and algorithm design

Status: Core product behavior approved. **Step 1.3 placement and edge-case behavior is proposed for review** in [SCHEDULING_EDGE_POLICY.md](SCHEDULING_EDGE_POLICY.md); no scheduling engine or tests implemented.

## Time vocabulary
All times displayed as elapsed from the session start (00:00). Internally prefer integer duration units and monotonic elapsed-time measurement; no floating-point accumulation. Example durations are whole minutes. Preserve the ability to adopt seconds later without altering invariants.

- `D`: total session length
- `F`: final summary duration
- `W = D-F`: work block length (all poses + breaks)
- `O`: sequence of optional opening pose groups
- `P`: nominal repeating pose duration
- `B`: short break duration — **new-session default 5 minutes, editable; minimum 1 minute for an actual break**
- `L`: middle/long replacement break duration — **new-session default 15 minutes, editable; minimum 1 minute for an actual break**
- `N`: configured count of long breaks
- `cycleStart`: pose-first or break-first

The examples set `D=180, F=15, W=165, P=25, B=5, L=15, N=1`. Example A opening groups 4×1 then 4×4 and break-first; Example B no openings and pose-first. **D-030:** 5 and 15 minutes are editable new-session break defaults, not fixed constraints. A configured break duration of 0 is invalid (minimum 1 minute); the UI/engine must reject it instead of interpreting it as a skip-break instruction.

## Baseline generator
1. Validate every configured duration and count before scheduling. Reject negative durations, summary longer than total, and sequences that cannot legally fit.
2. Add opening poses in their configured order, no automatic breaks between poses unless specifically configured.
3. Add repeating poses and interleaving scheduled short breaks according to cycleStart, stopping at work-block boundary. Never insert a break that would terminate the work block without a pose afterwards.
4. Select eligible short-break slot(s) for replacement by long break(s), typically near the middle; **do not add a second break** at that boundary. Since replacement consumes `L-B` extra time, regenerate or account for this before fixing final pose length.
5. Show long-break placement in preview. A deterministic nearest-midpoint tie-break rule remains an implementation detail to lock and test in Stage 1; the two approved examples below are the reference schedules.
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

## Stage 1 open engineering edge cases

The candidate resolutions are documented as **unapproved proposals** in [SCHEDULING_EDGE_POLICY.md](SCHEDULING_EDGE_POLICY.md). They require the user's approval before implementation. Example A/B expected results remain unchanged.
How to handle (a) zero final summary, (b) multiple long breaks, (c) work block too short for opening poses, (d) break-first with very short work block, (e) how to specify a deliberately break-free cycle **without zero-duration breaks**, (f) final incomplete pose with fewer than two poses, (g) midpoint tie-breaks, (h) whether longer breaks must exceed short-break length, (i) custom pose that exceeds nominal length, (j) set-end vs every-pose sound. **Minimum 1 minute per actual break is already approved**. Propose explicit behavior, tests, and request approval if user-facing choices change.
