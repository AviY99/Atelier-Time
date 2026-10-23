# Atelier Time — editor terminology and opening-group break review
Date: 2026-10-24. Status: D-071 group-break interpretation explicitly APPROVED by user ("כן") and implemented in source, NOT YET phone-compiled. All field names remain provisional pending terminology review.

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

## Opening-group breaks — APPROVED D-071 and source implemented

User confirmed: every opening group gets an editable after-group rest. Zero-minute rest is allowed at earlier boundaries; **last opening group requires a positive rest**. When BREAK_FIRST is selected and the last group rest equals the configured first regular break, use **the same break** (Example A's existing 00:20–00:25) rather than insert a duplicate. A different explicit last-group break **replaces** the initial repeating-break slot, rather than adding an adjacent break; group-specific rests are not counted as replaceable regular break ordinals.

- Group model: `OpeningPoseGroup.breakAfterSeconds: Int?`. `null` represents **legacy stored rules**, avoiding changes to old approval signatures and Example A/B historical fixture inputs. New drafts use numeric explicit durations.
- Example A UI: first group `0m`, second `5m`, `BREAK_FIRST`; expected historical time boundaries identical.
- Between groups, `breakAfterSeconds > 0` creates a tagged normal-rest phase with `openingGroupId` and no regular `breakOrdinal`; `0` creates no phase. All group rests count for the existing 25m/5m advisory rule.
- Last group: explicit `0` or negative blocked; rest and at least one subsequent pose must fit the work window. This preserves the no-trailing-break rule. A custom positive final group rest is inserted once and the beginning regular-break slot suppressed.
- Automatic special replacement chooses only **ordinary break ordinals**, excluding tagged group-specific rests. Positive replacement of a shared first break remains possible when its value equals normal break duration.
- Preset JSON remains schema version `1`: optional `breakAfterSeconds` per group; missing field loads `null`; signatures include explicit group-rest values only, preserving the old signature for all-legacy groups. Unedited existing approved presets remain readable and retain the old schedule.
- New tests: Example A parity, earlier zero/positive break, custom last rest and no duplicate, pose-first insertion, explicit last-zero/negative rejection, required subsequent pose, text-adapter parsing, versioned preset integrity, Android store round-trip.

**Source implementation is uncompiled and these new tests have not been run on the phone.** Prior Kotlin test and APK PASS apply only to previous revisions.


## Timer first increment

On Android source branch: platform-neutral monotonic elapsed clock; three distinct tone presets; Start/Pause/Resume/Stop; approved current/next phase; elapsed/remaining session time; separate timer screen accessible from an approved schedule. **Background/locked-screen sound guarantees not yet implemented; D-016 per-pose vs group sound boundary still open; new source and five JVM unit tests have not been phone compiled or executed.**
