# Atelier Time — Data model and scheduling interfaces (Stage 1, Step 1.2)

**Prepared:** 2026-10-22  
**Status:** Step 1.2 **design prepared for review**. Technical shape below is a **proposal**, not implemented source code and not a change to previously approved user behavior. No Flutter scaffold or engine exists.  
**Technology:** Flutter + Dart, one shared app targeting Android, Windows, iOS/iPadOS and macOS (approved TD-001).  
**Next gate:** Get explicit approval before Step 1.3 (long-break positioning/edge-case policy). Do not code or build in Step 1.2.

## 1. Product-to-model mapping

The two 3-hour session examples are **input/output reference fixtures**, not hard-coded application choices. **Every number** in the examples is an independent editable field on each session's eventual input screen. A session may contain zero, one, two or more opening pose groups; groups can be added, reordered or removed. The entire schedule is recalculated whenever a rule changes, and modified saved sessions require a fresh preview and approval.

| Product field | Model field | Example A | Example B |
|---|---|---:|---:|
| Name | `name` | User-defined | User-defined |
| Total session duration | `totalSeconds` | 10,800 (180m) | 10,800 |
| Reserved final summary | `summarySeconds` | 900 (15m) | 900 |
| Ordered introductory groups | `openingGroups[]` | 4×60s, 4×240s | [] |
| Recurring pose length | `cycle.poseSeconds` | 1,500 (25m) | 1,500 |
| Recurring short break | `cycle.shortBreakSeconds` | 300 (5m) | 300 |
| Cycle begins with | `cycle.firstPhase` | break | pose |
| Replacement long break | `longBreaks.durationSeconds` | 900 (15m) | 900 |
| Number of replacements | `longBreaks.count` | 1 | 1 |
| Location of replacement(s) | `longBreaks.placement` | approved slot after pose 10 | approved slot after pose 3 |
| End adjustment | `endPoseResolution` | remainder/even/custom | remainder/even/custom |

User entry can display minutes; internal `int` seconds avoid floating-point arithmetic and support accurate countdowns later. For **currently approved whole-minute pose splitting**, custom and near-equal results remain multiples of 60 seconds, with minimum 60 seconds per pose when feasible. There is no requirement to expose second-resolution inputs in the first UI.

## 2. Proposed Dart-domain types (concept only, NOT executable implementation)

Naming is provisional; do not interpret sample fields as an already-created API.

```text
enum CycleFirstPhase { pose, break }
enum LongBreakPlacementMode { automaticMiddle, explicitBreakOrdinals }
enum PhaseKind { pose, shortBreak, longBreak, summary }
enum PoseSource { openingGroup, repeatingCycle }
enum EndPoseMode { noneNeeded, unresolved, remainder, equalSplit, customSplit }
enum IssueSeverity { error, warning }

OpeningPoseGroup
  id: String                      // stable id for editing/reordering
  count: int                      // editable, positive when included
  poseSeconds: int                // editable, strictly positive
  label: String?                  // optional user-visible name later

RegularCycle
  poseSeconds: int                // regular pose duration
  shortBreakSeconds: int          // normal rest duration
  firstPhase: CycleFirstPhase     // starts pose-first or break-first

LongBreakPlacement
  mode: LongBreakPlacementMode
  breakOrdinals: List<int>        // 1-based, only in explicit mode; exact tie rules TBD

LongBreakRule
  count: int                      // 0 means no long breaks
  durationSeconds: int            // replacement duration; editable
  placement: LongBreakPlacement

SessionRules
  schemaVersion: int
  id: String                      // stable local draft identity
  name: String                    // editable preset/session name
  totalSeconds: int
  summarySeconds: int
  openingGroups: List<OpeningPoseGroup>    // any length, ordered, editable
  cycle: RegularCycle
  longBreaks: LongBreakRule

SchedulePhase
  id: String                      // stable within a candidate schedule
  kind: PhaseKind
  startOffsetSeconds: int         // elapsed since start, not wall-clock time
  endOffsetSeconds: int           // exclusive end; duration = end-start
  poseNumber: int?                // sequential across all poses
  poseSource: PoseSource?         // present for poses
  openingGroupId: String?         // if pose from opening group
  replacedShortBreakOrdinal: int? // present for an actual long replacement

ScheduleIssue
  severity: IssueSeverity
  code: String                    // machine-readable stable error code
  message: String                 // human-readable for future UI
  relatedField: String?           // e.g. 'summarySeconds'

ScheduleCandidate
  sourceRules: SessionRules
  phases: List<SchedulePhase>
  issues: List<ScheduleIssue>
  needsEndPoseResolution: bool
  endPoseOptions: EndPoseChoiceAvailability
  generationVersion: int          // planned for reproducibility; exact scheme TBD

EndPoseChoiceAvailability
  remainderAvailable: bool
  equalSplitAvailable: bool
  customSplitAvailable: bool
  unavailableReasons: Map<String,String>  // without inventing extra poses/breaks

EndPoseResolution
  mode: EndPoseMode
  firstOfLastTwoPoseSeconds: int?  // only for custom split
  // no chosen adjustment until the user explicitly selects one when needed

ApprovedSessionPreset
  schemaVersion: int
  id: String
  name: String
  approvedRules: SessionRules
  endPoseResolution: EndPoseResolution
  approvedPhases: List<SchedulePhase>       // immutable approved snapshot
  approvedAt: String?                       // timestamp for metadata, not timing math
  revision: int                             // editing requires new approval
```

The set of field names is proposed and can be refined during implementation; **the behavior is constrained by the approved requirements and the exact reference schedules**.

### Why keep rules and approved phases together?

- Rules allow the editor to reconstruct every numeric input, including new/reordered opening groups.
- Approved phases preserve exactly the sequence reviewed, including the chosen last-two-pose allocation.
- After editing a numeric field the existing approval becomes invalid: generate a new candidate, resolve partial end, obtain approval and then save a new approved revision.
- The live timer consumes `approvedPhases` rather than generating a different schedule while running.
- All data is intended to be locally stored/offline in later Stage 3; the storage library, file/DB format and migration strategy are not chosen in Step 1.2.

## 3. Lifecycle and boundaries

```text
User enters/edits SessionRules (unapproved draft)
       |
       v
Validate rules -> create ScheduleCandidate (full preview)
       |
       +-- invalid -> list errors, prevent approval
       |
       +-- final pose incomplete -> offer three explicit options
       |       remainder / near 50-50 / custom ruler
       |       recalculate affected phase start/end values
       |
       +-- no incomplete pose -> no split prompt
       |
       v
User reviews entire timeline and explicitly approves
       |
       v
ApprovedSessionPreset (rules + chosen resolution + frozen approved phases)
       |
       +-- edit -> new draft; original approved schedule not silently altered
       |
       +-- activate -> runtime timer (Stage 4, NOT part of Step 1.2)
```

The app's eventual input/editor screens (Stage 2) and persistent preset store (Stage 3) are **not implemented** here. This design ensures those later features can use the shared Dart model.

## 4. Phase semantics and invariants

- Each generated phase is a half-open interval `[startOffsetSeconds, endOffsetSeconds)`, permitting exact adjoining boundaries.
- The first phase starts at 0; the final phase ends at `totalSeconds`; neighboring phases meet exactly.
- Every pose has a positive duration and a consecutive pose number.
- A scheduled long break represents ONE scheduled short break **replaced** by the longer duration. Do not retain a short break immediately next to the replacement as if both were scheduled.
- All opening groups are expanded into `count` consecutive individual pose phases in the same order as inputs.
- `summarySeconds` reserves the last part of the session; it is not extra time. A zero-length summary requires no zero-duration display phase (this proposed representation does not resolve other scheduling edge rules).
- `ScheduleIssue` records impossible input combinations rather than inventing times or silently truncating required opening poses.
- Transitions between phases provide future sound-trigger candidates, but sound choices and per-pose-versus-per-set behavior remain undecided.

## 5. Incomplete final pose — approved three options, modeled without UI

If the baseline generator leaves a shortened last repeating pose, the candidate is marked `needsEndPoseResolution=true`. The model must make each choice explicit in the preview; the current rule does **not** auto-approve a choice.

For a feasible `pose / break / pose` final window:
- Let `S` = start of earlier pose, `E` = end of last pose (= `totalSeconds-summarySeconds`), `Q` = unchanged intervening break, and `T=E-S-Q` = two poses' total time.
- **Remainder:** keep preceding pose at nominal duration; last pose receives remaining time.
- **Equal split:** split as close to 50/50 as the approved 1-minute precision allows, giving the extra whole minute to the second pose when `T` is odd in minutes.
- **Custom:** store the first pose's selected duration; compute second pose from `T-first`. Future slider controls are an interface concern only.
- Move the between-pose break to immediately follow the new first pose without changing its duration. Keep every earlier phase, work-block endpoint and summary unchanged.
- When the final structure has fewer than two eligible poses, or either split is impossible, show a reason via `endPoseOptions`; do not insert an invented pose or break.

The exact fallback/error behavior for infeasible combinations remains an open later-stage decision. `EndPoseResolution.noneNeeded` means the original schedule fits exactly; `unresolved` means the user has not approved a necessary resolution.

## 6. Canonical example configurations for future test fixtures

These are **illustrative serialized data-model examples**, not hardcoded app defaults. The third 5-minute break slot is the 15-minute replacement in BOTH approved reference schedules. The future automatic-middle algorithm must be designed separately in Step 1.3 to preserve their displayed outputs.

### Example A

```json
{
  "schemaVersion": 1,
  "id": "example-a",
  "name": "Drawing or sculpting — opening short poses",
  "totalSeconds": 10800,
  "summarySeconds": 900,
  "openingGroups": [
    {"id": "group-1", "count": 4, "poseSeconds": 60},
    {"id": "group-2", "count": 4, "poseSeconds": 240}
  ],
  "cycle": {"poseSeconds": 1500, "shortBreakSeconds": 300, "firstPhase": "break"},
  "longBreaks": {
    "count": 1,
    "durationSeconds": 900,
    "placement": {"mode": "explicitBreakOrdinals", "breakOrdinals": [3]}
  }
}
```

### Example B

```json
{
  "schemaVersion": 1,
  "id": "example-b",
  "name": "Drawing or sculpting — long poses",
  "totalSeconds": 10800,
  "summarySeconds": 900,
  "openingGroups": [],
  "cycle": {"poseSeconds": 1500, "shortBreakSeconds": 300, "firstPhase": "pose"},
  "longBreaks": {
    "count": 1,
    "durationSeconds": 900,
    "placement": {"mode": "explicitBreakOrdinals", "breakOrdinals": [3]}
  }
}
```

The prescribed outputs remain exactly those in [EXAMPLES.md](EXAMPLES.md):
- A last two poses: remainder 25+10m, equal 17+18m, custom sample 20+15m; 5m break unchanged.
- B last two poses: remainder 25+5m, equal 15+15m, custom sample 18+12m; 5m break unchanged.
- Both: 130m posing + 35m breaks + 15m summary = 180m.
- Do not use the explicit third-break fixture to bypass designing the actual automatic-middle rule in Step 1.3.

## 7. Error/validity model (shape only; business-policy decisions later)

Proposed machine-readable issue codes:
- `INVALID_TOTAL_DURATION`
- `SUMMARY_EXCEEDS_TOTAL`
- `INVALID_OPENING_GROUP`
- `INVALID_CYCLE_DURATION`
- `NO_ELIGIBLE_REPLACEMENT_BREAK`
- `REPLACEMENT_BREAK_COUNT_EXCEEDS_SLOTS`
- `OPENING_GROUPS_EXCEED_WORK_BLOCK`
- `FINAL_SPLIT_UNAVAILABLE`
- `INVALID_CUSTOM_SPLIT`
- `TIMELINE_GAP_OR_OVERLAP`

Stage 1 Step 1.3 must explicitly resolve ambiguous product-visible edge cases and midpoint tie-breaking before algorithm implementation. Error-code names are tentative, not approved user-facing copy.

## 8. Still open: do not silently decide

1. Exact automatic-middle replacement-break placement and ties when session lengths/counts vary.
2. How to deal with multiple long breaks and explicit break-ordinal changes after edits.
3. Inputs too short to fit any pose or a complete break; opening groups exhausting work time.
4. Whether zero-length breaks are allowed; whether long duration must exceed short duration.
5. What to show if only one last pose, or no intervening break, remains for end resolution.
6. Precise activity/label for “summary”; audio at every pose or just set end.
7. Exact on-disk preset serialization format and file/DB technology (Stage 3).
8. Which platform-specific mechanisms keep audible cues functioning during OS suspension (Stage 4/5).

Nothing in this data-model proposal overwrites approved user requirements or the example schedules.

## 9. Step 1.2 completion checklist

- [x] Define model entities and source-level field types on paper (not compiled)
- [x] Cover every configurable numeric input and optional number of opening groups
- [x] Specify generated phase representation and validation issue format
- [x] Represent explicit final-pose choice and immutable approved timeline
- [x] Include A/B example fixture payloads with exact reference expected results
- [x] Distinguish engineering proposals from approved behavior and unresolved edge rules
- [ ] User review/approval of this data model and permission to proceed to **Step 1.3**

**STOP: Stage 1 Step 1.3 and all programming are NOT authorized by the instruction to continue Step 1.2.**
