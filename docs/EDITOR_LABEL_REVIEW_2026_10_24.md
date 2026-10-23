# Atelier Time — editor terminology and opening-group break review
Date: 2026-10-24. Status: user review in progress; terminology source is committed on Android branch, no compile after this change.

## Labels (first-pass wording, not frozen)

| Previous UI | Proposed UI | Explanation |
|---|---|---|
| Session name | Session title | Name of saved/working plan |
| Total (min) | Total session duration (min) | Includes all poses, breaks and optional reserved end-of-session summary |
| Summary (min) | End-of-session summary (min) | Final reserved time, included in total, may be zero; exact activity/meaning D-015 still open |
| Opening pose groups | Opening pose groups | Ordered opening sets; editable/reorder/remove |
| Group n | Opening group n | Name of numbered opening set |
| Count | Number of poses | Quantity of poses in this opening group |
| Each (min) | Each pose duration (min) | Duration of one pose within group |
| Regular rhythm and breaks | Regular poses and breaks | Post-opening cycle inputs |
| Pose (min) | Regular pose (min) | Recurring pose duration |
| Normal rest (min) | Regular break (min) | Configured ordinary rest, strictly positive |
| Special rest (min) | Special break (min) | Replacement duration, strictly positive |
| Special count | Number of special breaks | Number of replacements, may be zero |
| Repeating sequence | Repeating pose/break cycle | Post-opening pattern |
| Full schedule preview | Complete session timeline | Elapsed offsets through total, not wall-clock |
| Final pose: choose an ending | Final regular pose: choose an ending | Remainder/even/custom split |
| Final summary | End-of-session summary | Printed in preview and timer |
| Saved sessions · offline | Saved session presets | Reusable approved sequences |
| Save approved | Save approved session | Create a saved preset |
| Update approved | Overwrite saved preset | Replace the selected saved version |
| Save as new | Save as new preset | Keep existing saved version |
| Load | Load & edit | Restore existing approved rules into editor |
| Activate | Select preset | Select stored plan without starting timer |

Implemented in the first-pass Compose source along with Help dialogs for each opening group and each major section. User can request changes to any wording; English is the current UI language.

## Opening-group breaks — user-requested rule (D-071)

The user specifically requested a break after every opening group, permitting **zero** at a boundary before the final group while requiring a **strictly positive** break after the final opening group. The current canonical Example A has no gap between 4×1 and 4×4 groups (0 between them), and already has a 5-minute scheduled break immediately after the last group (00:20–00:25) because of break-first pattern. The proposed interpretation is that this existing break **counts**, instead of inventing a second adjacent break.

Before changing `OpeningPoseGroup`, scheduling DP, editor inputs, and persisted versioned JSON, confirm:
- Break after each earlier group is an editable whole-minute `0 or more` value (zero yields no phase); final group effective after-break is `>=1 minute`.
- Do not double the last group's break when an already-scheduled first repeating-cycle break occupies exactly that boundary.
- Clarify whether special-break replacement ordinals include group-boundary breaks, as this can affect the canonical A special-break timestamp and saved approvals.
- Preserve the **old** A/B fixtures as historical oracle and explicitly approve any revised expected boundaries; maintain total duration and non-terminal-break rule.

No untested change to the scheduling engine or saved schema was silently made in this iteration.

## Timer first increment

On Android source branch: platform-neutral monotonic elapsed clock; three distinct tone presets; Start/Pause/Resume/Stop; approved current/next phase; elapsed/remaining session time; separate timer screen accessible from an approved schedule. **Background/locked-screen sound guarantees not yet implemented; D-016 per-pose vs group sound boundary still open; new source and five JVM unit tests have not been phone compiled or executed.**
