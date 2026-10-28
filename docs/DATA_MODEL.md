# Atelier Time — Data model and scheduling interfaces (Stage 1, Step 1.2)

> **IMPLEMENTED STATUS — Stable 9 / D-126 (2026-10-28):** This original Stage 1 Flutter/Dart data-model proposal is archival and not an authoritative Android JSON/Kotlin schema. Current Kotlin `SessionRules`, `ApprovedSession`, `SchedulePhase` and `ConstraintFuturePlanner` are implemented on the Android development branch. Saved Classes/Sound preferences remain compatible and preserved; D-126 adds separate private append-only per-Run diagnostic JSONL, without replacing Saved Class storage. [Current status](CURRENT_STATUS_AND_HANDOFF_STABLE9_2026_10_28.md).


**Prepared:** 2026-10-22  
**Status (historical Step 1.2 design, updated 2026-10-23):** The document originally described conceptual field names and interfaces. Since then, the pure Dart core **has been implemented and verified 35/35 tests PASS** (D-052) on `stage-1-4-scheduling-core`. Refer to [actual scheduling code](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/lib/src/scheduling.dart) for implemented API names; this earlier conceptual model is not an exact code schema lock. Stage 2 Flutter editor/preview source exists on a separate branch but has **not been Flutter-tested**.  
**Technology:** Flutter + Dart, one shared app targeting Android, Windows, iOS/iPadOS and macOS (approved TD-001).  
**Current gate update (D-052–D-054):** Step 1.3 policies approved via D-037/D-044. Stage 1 Step 1.4 **VERIFIED/COMPLETE** on branch (screenshot-confirmed **35/35 Dart tests PASS** on Android Termux 3.13.7; prior smoke and analyzer PASS). Stage 2 editor work **STARTED** under D-053 with nested Flutter source on [branch](https://github.com/AviY99/Atelier-Time/tree/stage-2-session-editor), [Stage 2 plan](STAGE_2_PLAN.md) and draft PR #2. **Flutter editor uncompiled/untested, no installed app; no GitHub Actions or paid build.**

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

**LATEST APPROVED D-036 (supersedes D-031's 0-break allowance):** The full session's `totalSeconds` and any configured actual normal/active special break must be **>0**; a 0-duration session/break is a **blocking input error**. Defaults remain `shortBreakSeconds=300` (5m) and long/special `durationSeconds=900` (15m), editable to other positive values. A count of **0 optional special replacements** is not a 0-duration break. D-031's cumulative **25-minute posing / >=5-minute break advisory warning and explicit acknowledgment** still applies for otherwise valid shorter rests or overlong poses. Zero-length logical break slot concepts have been withdrawn.

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
  shortBreakSeconds: int          // normal rest duration: default 300s, min 60s
  firstPhase: CycleFirstPhase     // starts pose-first or break-first

LongBreakPlacement
  mode: LongBreakPlacementMode
  breakOrdinals: List<int>        // 1-based, only in explicit mode; exact tie rules TBD

LongBreakRule
  count: int                      // 0 means no long breaks
  durationSeconds: int            // replacement break: default 900s, min 60s when configured
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
  issues: List<ScheduleIssue>       // errors and safety warnings are distinct
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
  acknowledgedSafetyWarningCodes: List<String> // proposed: explicit 25/5 override for this revision
  approvedAt: String?                       // timestamp for metadata, not timing math
  revision: int                             // editing requires new approval
```

The set of field names is proposed and can be refined during implementation; **the behavior is constrained by the approved requirements and the exact reference schedules**.

### Why keep rules and approved phases together?

- Rules allow the editor to reconstruct every numeric input, including new/reordered opening groups.
- Approved phases preserve exactly the sequence reviewed, including the chosen last-two-pose allocation.
- After editing a numeric field the existing approval **and safety-warning acknowledgement** become invalid: generate a new candidate, resolve partial end, obtain explicit safety-warning acknowledgement if required, and then save a new approved revision.
- The live timer consumes `approvedPhases` rather than generating a different schedule while running.
- All data is intended to be locally stored/offline in later Stage 3; the storage library, file/DB format and migration strategy are not chosen in Step 1.2.

## 3. Lifecycle and boundaries

```text
User enters/edits SessionRules (unapproved draft)
       |
       v
Validate rules -> create ScheduleCandidate (full preview)
       |
       +-- hard-invalid -> list errors, prevent approval
       +-- safety warning -> show affected times, require explicit acknowledgement, then allow approval
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
- A scheduled long break represents ONE scheduled short-break **slot replaced**, rather than an extra break at that boundary. If the entered replacement is 0, no actual break time or zero-length phase is created; the exact representation in schedule previews remains part of Step 1.3 discussion.
- All opening groups are expanded into `count` consecutive individual pose phases in the same order as inputs.
- `summarySeconds` reserves the last part of the session; it is not extra time. A **0-minute summary** (proposed) can omit the summary display phase **only if the full session total is positive**; this is not a 0-minute session.
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
- `NONPOSITIVE_TOTAL_SESSION`
- `SUMMARY_EXCEEDS_TOTAL`
- `INVALID_OPENING_GROUP`
- `INVALID_CYCLE_DURATION`
- `NONPOSITIVE_BREAK_DURATION` // latest approved D-036: 0 and negative configured breaks are invalid
- `NO_ELIGIBLE_REPLACEMENT_BREAK`
- `REPLACEMENT_BREAK_COUNT_EXCEEDS_SLOTS`
- `OPENING_GROUPS_EXCEED_WORK_BLOCK`
- `FINAL_SPLIT_UNAVAILABLE`
- `INVALID_CUSTOM_SPLIT`
- `TIMELINE_GAP_OR_OVERLAP`

**Status update:** Stage 1 Step 1.3 has **approved** the midpoint/even-spread and edge rules (D-037), preserving D-036 positive duration and D-031 25/5 warnings. Technical error-code names here remain tentative, not approved UI copy; Step 1.4 not authorized.

## 8. Stage 1 Step 1.3 approved outcomes and remaining future decisions

**Approved 25/5 advisory warning (D-031, retained by D-036):** accumulate posing across consecutive poses until a rest **>=5 minutes**; flag posing beyond 25 accumulated minutes. Display timing/details. A user may choose a **valid positive 1–4m break** or other legal rest violation only after acknowledging the warning. **Break=0 or full session=0 is a blocking error under D-036**, not an exception. Shorter-than-5m valid rests do not reset the counter. No scheduling or UI code exists; acknowledgement metadata fields are tentative.

Step 1.3's [SCHEDULING_EDGE_POLICY.md](SCHEDULING_EDGE_POLICY.md) and [approved final review](STEP_1_3_REVIEW.md) resolved most of the earlier questions below **via D-037**. The following numbered points are a **historical planning checklist**, not current open decisions; current unresolved items are identified explicitly beneath it.

1. Exact automatic-middle replacement-break placement and ties when session lengths/counts vary.
2. How to deal with multiple long breaks and explicit break-ordinal changes after edits.
3. Inputs too short to fit any pose or a complete break; opening groups exhausting work time.
4. **Resolved by approved D-036 and D-037:** configured 0-minute breaks and 0-minute full sessions invalid; 0-minute logical break-slot proposal withdrawn; warning counter resets on each activated full session (no automatic cross-preset carryover). **RESOLVED D-044:** a positive special replacement may be <= positive normal break; 0 remains invalid and 25/5 warning acknowledgment applies when needed.
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
- [x] User said “Continue” after Step 1.2 review, authorizing discussion/documentation of **Step 1.3**. The technical field names remain proposals, not an implemented API.

**STATUS UPDATE (2026-10-22): Step 1.3 formally APPROVED / COMPLETE as D-037**, subject to D-036 strict-positive session/break durations and D-031 25/5 rest warnings; D-038 special break comparison was subsequently resolved as APPROVED by D-044 (any positive duration). **Stage 1 Step 1.4 and all programming are NOT authorized.**
