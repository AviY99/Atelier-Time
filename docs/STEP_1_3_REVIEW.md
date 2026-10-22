# Atelier Time — Step 1.3 consolidated scheduling decision review (v0.9)

**Date:** 2026-10-22  
**Status:** Requirements **D-031 (25m/5m advisory warnings)** and **D-036 (zero total session and zero configured breaks are INVALID)** approved. All other proposals remain **AWAITING USER APPROVAL**. **Step 1.4 programming NOT AUTHORIZED.**

This document supersedes the previous v0.8 review that suggested zero-minute break inputs, logical zero-minute break slots, and two-pose splitting across a 0-minute break. **Those proposals are withdrawn.** See [full edge-policy design](SCHEDULING_EDGE_POLICY.md), [decisions](DECISIONS.md), [fixed examples](EXAMPLES.md), and [data model](DATA_MODEL.md).

## Approved requirements — no further vote needed

- Flutter+Dart, one shared app for Android, Windows, iOS/iPadOS, macOS; every number in each example is editable in each future full session editor.
- **Full session duration must be greater than zero. Every configured ordinary or active long/special break must have duration greater than zero.** In the planned whole-minute editor, the smallest valid positive duration is 1 minute. A 0-minute break/session is a **hard error**, not a safety warning that can be acknowledged.
- The initial regular-break duration is 5m and long/special replacement duration 15m; both may be changed to other **positive** numbers. The count of optional special breaks may be 0 if that policy is approved, which means no special break—not a break of 0m.
- Accumulate posing durations across consecutive poses without adequate rest. A **break >=5m** resets the posing counter; a **valid 1–4m break does not**. After 25m accumulated posing, rest >=5m is recommended before further posing; overlong continuous poses generate warnings.
- Warning exceptions for legal but insufficient rest are permitted **only after explicit acknowledgment and explicit schedule approval**. No changing input numbers in secret. Any edit invalidates both acknowledgment and approval.
- Long break replaces an ordinary break; 180m Example A/B full sequences and last-two split options remain canonical.

## Proposals needing user sign-off — four groups

### 1) Placement policy (P-01, P-02, P-03)

- One long replacement: choose an existing **positive-duration** normal break whose replaced center falls closest to halfway through work time (exclude summary). Earlier break wins ties. Must reproduce Example A long 01:20–01:35 and Example B long 01:25–01:40.
- Multiple replacements: evenly spaced over the working period at fractions `j/(N+1)` of work time using distinct eligible breaks, deterministic earlier-ordinal ties, never silently reduce the requested count.
- Manual override: choose eligible short-break ordinals, preserving their intended positions through valid edits; invalid ordinals cause explicit error, never silent relocation.
- A proposed `specialBreakCount=0` indicates no long replacement, not a 0m break.
- **Unresolved:** Whether a positive configured special break may be shorter than or equal to the normal positive break, despite the label "long."

### 2) Validation (P-04)

- **Already approved hard errors:** Full session <=0m, or any configured actual break <=0m.
- Proposed hard errors: negative/overlarge summary; nonpositive pose duration/count; opening groups exceed work; more long replacements than eligible break slots; no room for required next pose; no valid positive final phase.
- Proposed valid: summary duration 0 as long as total session duration is **positive**; valid opening-only sessions; zero *count* of special breaks with no special phases.
- Do **not** generate a zero-duration timeline phase or an artificial skipped-break slot.
- A mathematically legal configuration with 1–4m breaks or over-25m posing has a **warning plus explicit override**, not an input error.

### 3) Safety-warning boundary and scope (partly approved + proposed refinements)

- **Approved:** Add consecutive pose times. Short rests 1–4m do not reset cumulative posing; breaks >=5m reset it. Show explicit acknowledgment when recommendations are exceeded.
- **Proposed:** A session ending at exactly 25m of uninterrupted posing needs no appended break if no further poses follow. A 30m final pose still warns, since it exceeded 25m.
- **Proposed:** Track the counter **within one activated complete Atelier Time session**. Do not automatically carry posing durations over from one separately activated saved preset to another unless separately approved; user wording "accumulated sessions" is potentially broader and still open.
- If the user edits or changes the last-two-pose split, recompute warnings and require reapproval.

### 4) Final-two-pose allocation and full preview (P-05, P-06)

- Three already approved ending modes: remainder, near-equal (floor/ceil) and custom ruler in 1m steps, preserving the existing **positive** interposing break and total time.
- **Proposed:** If two eligible last poses with a positive intervening break do not exist, disable unavailable equal/custom modes with explanatory text rather than invent an extra break/pose.
- **Proposed:** Explicit custom splits may exceed the usual nominal pose length if both resulting poses remain positive.
- Every preview displays actual complete timestamps, replacement choices, warnings, hard errors, summary, total and final-pose alternatives. Edits invalidate approval.

## Example validation cases (not implemented)

| Input/session fragment | Expected |
|---|---|
| Total session 0m | Blocking error |
| Normal break 0m | Blocking error |
| Selected special replacement break 0m | Blocking error |
| Normal break 1m (or 3m), 25m posing then more posing | Valid input, **rest warning** requiring acknowledgment |
| 25m posing, 5m break, more posing | No warning |
| Five consecutive 5m poses, 5m break | No warning |
| 30m pose, then 5m break | Warning, since posing exceeded 25m |
| Optional special replacement **count** 0 | Proposed valid: no special replacement, distinct from a 0m break |
| Summary 0m with overall session >0m | Proposed valid (no final summary phase), distinct from 0m session |

## Status / next step

The most recent user clarification approves the **positive session/positive break rule**, not the four policy groups above. Present them for user acceptance or revision. **Do not treat the instruction “Continue” as permission to implement Step 1.4**. No programming, automated test execution or Flutter build has begun.
