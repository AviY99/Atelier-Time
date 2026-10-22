# Stage 1 Step 1.3 — consolidated scheduling-policy review (v0.8)

**Prepared:** 2026-10-22
**State:** Documentation-only review; the user's latest “Continue” requests progress within Step 1.3, **not authorization for Step 1.4**.
**Source of truth for approved behavior:** [SCHEDULING_RULES.md](SCHEDULING_RULES.md), [PRODUCT_SPEC.md](PRODUCT_SPEC.md), [DECISIONS.md](DECISIONS.md), and both fixed examples [EXAMPLES.md](EXAMPLES.md).
**Detailed prior proposals:** [SCHEDULING_EDGE_POLICY.md](SCHEDULING_EDGE_POLICY.md), P-01–P-06.
**Important:** Items labelled **PROPOSAL** below are not yet product requirements; request user approval before implementing.

## A. Approved baseline requirements (do not vote again)

1. Flutter + Dart shared app for Android, Windows, iOS/iPadOS and macOS, with non-native UI allowed.
2. **All numbers are individually editable per full session**; opening groups can be added or removed.
3. Long/special breaks replace the corresponding short-break slot rather than adding an extra break.
4. All schedule phases and the final reserved summary must add to the configured total; user explicitly approves the generated chronological schedule.
5. Three final-incomplete-pose choices during review (remainder, near-equal last-two division, custom time-ruler split) when feasible.
6. **D-031:** **25 minutes accumulated posing / 5 minutes recommended break**. A **0-minute normal or special break value is permitted**; any schedule that exceeds the recommendation requires a clearly visible warning and the user's **separate, affirmative acknowledgment** prior to schedule approval. A rest shorter than 5 minutes does not reset accumulated pose time. The defaults 5m short and 15m special remain editable.
7. Example A has long break 01:20–01:35; Example B has long break 01:25–01:40, both exactly three hours. Existing example timings must not change.

## B. Proposed precise rest-warning interpretation — for approval

### B-1. Scope and counter (PROPOSAL for details)

- A continuous sequence of posing inside one configured **full session** has an accumulated pose counter, initialized to zero at session start.
- Add each pose's actual elapsed duration, including short opening poses and the chosen final-pose split. The **number of poses** is irrelevant.
- A contiguous rest lasting **at least 5 minutes** resets the counter. A 0–4-minute rest does not; neither a pose boundary nor a summary phase resets it.
- An overlong single pose (>25m) produces a warning even when the next phase is a >=5m break. A pose starting after already reaching 25 accumulated minutes without a 5m break also produces a warning.
- A pose that ends exactly at 25m and is followed immediately by a >=5m rest **does not** violate the rule.
- **PROPOSAL**: No extra break is required solely because the overall session ends at exactly 25m. If it ends **after** passing 25m, warn even if no more pose follows.
- **PROPOSAL**: Treat each activated full preset as a separate session for this warning; *do not silently carry pose minutes into a distinct saved preset*. If the user wants separate preset-to-preset accumulation, that is a **separate feature decision**, not assumed here.

### B-2. Warning and approval (refinement consistent with D-031)

- Calculate all warnings on the **final preview after all split and break choices** (intermediate previews may show provisional warnings).
- Show the first violation time, which pose/pose group caused it, accumulated posing length, and the corresponding break settings. Group repeat violations into understandable intervals instead of producing an alert on every minute.
- Require the user to choose **“Acknowledge and approve despite warning”** (or explicit equivalent) instead of treating normal “Approve” as a warning waiver. User may alternatively edit the rule.
- The saved approved schedule revision records its warnings and an explicit acknowledgment. Any edit to duration/count/placement/split invalidates both prior acknowledgment and approval; warning analysis is rerun.
- No break insertion, pose shortening, or automatic rule changes may be performed secretly.

### B-3. Time-boundary cases

| Schedule fragment | Warning? | Rationale |
|---|---|---|
| Five 5m poses, then 5m rest | No | 25m poses, then qualifying break |
| One 25m pose, then 5m rest, then pose | No | Rest resets counter |
| One 30m pose, then 5m rest | Yes | Exceeded 25m before rest |
| One 25m pose, then 0m slot, then another pose | Yes | Zero rest does not reset counter |
| One 20m pose, then 3m rest, then 6m pose | Yes | 3m rest insufficient; cumulative posing reaches 26m |
| One 20m pose, then 5m rest, then 20m pose | No | Adequate rest resets counter |
| Session contains a final 25m pose and then ends | **Proposed: No** | No further posing after threshold |
| Session ends after a 30m continuous pose | **Proposed: Yes** | Overlong pose already violated threshold |

## C. Proposed zero-minute break slot representation (P-04 clarification)

A requested `shortBreakSeconds=0` means a **logical break slot** remains between regular poses, but its elapsed duration is zero. Such slots are not emitted as zero-length `SchedulePhase` objects (they are rule metadata / boundary markers), because the active timeline must contain only positive-duration phases.

**Why keep logical slots?**
- Preserves both cycle patterns (pose-first/break-first) without inventing rest time.
- Makes a slot selectable by 1-based ordinal for manual/special replacement, even if its nominal duration is 0.
- Allows special replacement of a zero-duration normal slot with a positive 15m rest, without violating replacement-not-addition semantics.
- Allows a selected special slot to be set to 0m, also consuming no rest time, while remaining shown in the preview as “special break skipped (0m)” with its ordinal. The label is a proposed UI concept, not final design.
- For the last two poses, if there is a logical zero-minute break slot between them, allow the two-pose near-equal/custom allocation with intervening break length `Q=0` when both adjusted poses are positive. **This requires explicit approval**, because the prior canonical examples all contain a positive 5m intervening break.
- When an entire opening group flows directly into another (no configured break slot), do not fabricate a slot. The zero-break policy affects configured regular cycles only.
- No timeline phase can have negative duration. A terminal break slot without a subsequent positive-duration pose remains disallowed (subject to P-04 general validation policy).

A zero-minute input is **not the same as** `longBreakCount=0`, which means no selected special break slots.

## D. Placement policy to be approved (existing P-01/P-02/P-03 with safety separation)

- **One special break:** choose the feasible short-break slot whose *replacement break center* is closest to `W/2` (working-time midpoint). Tie -> earliest eligible ordinal.
- **Multiple special breaks:** target centers near evenly spaced `j/(N+1)` fractions of W, choose deterministic feasible distinct slots; tie -> ascending earliest ordinal list.
- **Manual:** user selects ordinal(s), no silent relocation when future edits invalidate a slot.
- **Zero-minute special break:** proposed center is the zero-duration slot boundary, not an actual 0-length phase; placement can still be selected.
- **Safety warnings do NOT silently override manual placement or rearrange automatic placement**. The 25/5 warning is reported on the resulting timeline, with explicit approval available. If the user wants to move a break to improve rest compliance, that is an explicit edit followed by a fresh preview.

Known A/B results must remain invariant. No implementation has validated the optimization algorithm yet.

## E. Other pending P-04/P-05/P-06 details

- Invalid mathematical schedules (negative durations, summary consuming the full session, impossible mandatory opening poses, no positive final pose, nonexistent selected slot) are **errors** that block approval. Insufficient rest alone is only a **warning**, never an error.
- `summarySeconds=0` is valid; the entire work schedule finishes at the requested total, without a 0-length summary phase.
- `count=0` long/special slots is valid; opening-only sessions are valid if opening poses exactly fill the work window.
- A user-chosen special break shorter than a nominal short break (including 0) is **allowed** under the zero-break override, subject to warnings. Do not enforce `L>B` merely because the UI calls it “long”; consider a more neutral “special break” label in UI later.
- When a final regular pose is shorter than nominal and the required last-two-pose structure is unavailable, show only feasible ending options. Do not invent phases to manufacture a split.
- **PROPOSAL:** The custom split may intentionally create a pose longer than the nominal regular pose duration if the two affected poses are both positive and combined pose time, intermediate slot and total schedule are preserved.
- The exact summary activity, per-pose/set alert semantics, visual layout, audio files and distribution methods remain later-stage decisions.

## F. Requested approval gate

The user has already approved D-031. The next approval should concern **only** still-proposed P-01–P-06 and refinements in sections B–E:
1. Auto midpoint and evenly-spaced multi-break choices; manual ordinal selection;
2. Zero-minute **logical slot** semantics and the option to distribute time across last two poses separated by a 0m logical slot;
3. Warning scope within a **single full session by default**; no cross-preset carryover without a later request; exact-25m end handling;
4. Mathematical validation versus non-blocking 25/5 warning, and custom-split flexibility.

Do not treat “Continue” alone as product approval of every new policy; mark them REVIEW PROPOSALS until the user explicitly approves them. **Step 1.4 (Dart engine, tests and Flutter scaffold) is NOT authorized.**
