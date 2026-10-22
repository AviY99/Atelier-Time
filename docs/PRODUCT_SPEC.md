# Product specification — Atelier Time

Status: **Approved product scope** on 2026-10-22. Implementation has not begun.

## Purpose and audience
A straightforward installable, self-contained timer for **drawing and sculpting ateliers** where a live model holds timed poses and takes breaks. The application runs a complete predefined sequence with minimal intervention.

## User journey
1. Open an existing saved session or create a new one.
2. Enter editable numeric rules; opening pose groups are optional.
3. Generate a time-accurate list showing every pose, break, final-summary block, duration, elapsed start and end.
4. Resolve any partial final pose **during review** using one of three explicit choices; see SCHEDULING_RULES.md.
5. Approve and save the session with a user-defined name.
6. Activate a saved session, start the timer and see current phase, countdown, next phase, remaining overall duration.
7. Pause, resume or stop the timer; receive three distinct audible alerts.

## Editable session parameters
**Every numerical value appearing in the example sessions is editable per full session.** Every session/preset has its own input screen. The user can add or remove opening pose groups, regenerate the entire schedule when any number changes, review the output and approve it before saving. The examples are regression fixtures, not hardcoded presets or limits.

- Total session duration.
- Final reserved summary duration, including zero when desired.
- Optional ordered opening pose groups: count and duration per group (e.g. 4 x 1 minute, then 4 x 4 minutes).
- Nominal regular pose duration.
- Short break duration.
- Long replacement break duration and number of long replacement breaks.
- Long-break placement strategy/position (middle by default in examples, configurable).
- Recurring cycle ordering: pose then break OR break then pose.
- Last-two-pose resolution choice and custom split if used.
- Preset name.
- Sound configuration / cues as supported by approved app design.

## Scheduling principles
- Every second/minute belongs to exactly one phase; no overlaps or unexplained gaps.
- The entire generated sequence ends at the specified session duration.
- The summary is the final reserved period, not an additional period beyond the session.
- Long break **replaces** a scheduled short break; never simply append 15 minutes.
- Short opening poses flow immediately into each other in the illustrated preset; introducing opening breaks would require an explicit rule.
- Avoid a trailing break immediately before final summary when no subsequent pose exists.
- A partial last regular pose is **not silently accepted**: explicitly offer alternatives when detected.
- Show the complete updated timeline before approval. Save the finalized schedule with the preset.
- Recomputed times after a change must always be derived from the same underlying schedule.

## Partial-final-pose resolution — approved
1. **Remainder:** Keep preceding pose at full nominal duration; shorten only the final pose.
2. **Near-equal:** Keep fixed earlier stages and intervening break; split total time available for last two poses approximately 50%/50%, showing both durations and times. For whole-minute odd totals use floor/ceil.
3. **Custom slider/ruler:** User adjusts the division of that same time, with updated durations/start/end times visible immediately; initially one-minute steps, at least one minute per pose when feasible.

When there are fewer than two poses or no intervening break, show that two-pose resolution is unavailable (exact fallback/error UX will be finalized in Stage 1). Preserve fixed length of the break, total session, and final summary in all feasible alternatives.

## Saved-session management
Create, name, approve, save, list, activate, edit, duplicate, rename and delete presets. Editing an approved preset generates a new preview and requires approval. Saved sessions remain usable offline.

## Live timer
- Large current phase countdown and phase identity (pose number/type, short break, long break, summary).
- Elapsed/remaining overall session time and next phase.
- Start, pause, resume, stop.
- Sound A on end of pose (or set, subject to final alert-edge rule); Sound B on end of break; Sound C on end of entire session.
- Alerts must reflect the approved sequence, not an unapproved baseline schedule.
- Reliable elapsed-time basis, including behavior during phone lock/background, subject to OS constraints established later.

## Look and installation
- App name: **Atelier Time**.
- Same maintained **Flutter + Dart application codebase** for Android phones/tablets, Windows computers, iPhone/iPad and macOS in the **first release**.
- Non-native Flutter-rendered menus, controls and graphics **are acceptable**; native widgets are not a requirement. Prioritize cross-platform feature parity and usable responsive layouts.
- Minimal, readable interface; offline capable; self-contained installed app for each OS (separate platform-specific install packages).
- Ask user for logo picture at the branding stage; discuss visual style before committing designs.

## Non-goals not promised yet
No account service, cloud synchronization, multi-device orchestration, payments or analytics unless separately approved.

## Definition of done
Installable working app on the chosen OS, saves/loads presets, generates examples A/B exactly, permits all three end-pose choices, runs reliable timer with three distinct audio events, and passes documented acceptance tests. Document every iteration and leave a current cross-chat handoff.
