# Architecture and design constraints (pre-implementation)

Status: **Not a chosen technology or finished architecture.** This is a planning boundary pending Stage 1 approval and target-OS decision.

## Required layers
1. **Rules/input validation**: validate editable counts, durations and cycle order.
2. **Pure deterministic schedule engine**: emit immutable ordered phases with ID/type, planned duration and elapsed start/end; no UI or sound responsibilities; uses integer time.
3. **Approval/allocator**: when last pose incomplete, provide remainder, near-equal, custom slider allocation, recompute all affected elapsed times, and commit only an approved schedule.
4. **Preset persistence**: named versioned data containing rules, allocated schedule and choice; reliable offline storage with migration path.
5. **Playback clock**: derive live state from actual elapsed clock and immutable scheduled phase boundaries (avoid accumulating timer callback delays); support pause/resume and interruption.
6. **Sound adapter**: route pose-end, break-end and final completion to three different sound cues; avoid double alerts.
7. **Native-feeling UI**: local platform controls for editor, chronological review, split slider, preset library, live timer and settings.
8. **Packaging**: installed offline app on approved platform; platform features and background/lock-screen audio investigated before promising behavior.

## Proposed data shape (illustrative, not schema lock)
```text
SessionRules
  title, totalDuration, summaryDuration,
  openingGroups[{count,poseDuration}],
  regularPoseDuration, shortBreakDuration,
  longBreakDuration, longBreakCount, longBreakPlacement,
  cycleStartsWith

SchedulePhase
  id, kind = pose|short_break|long_break|summary,
  duration, startsAt, endsAt, poseNumber?

ApprovedPreset
  schemaVersion, id, name, sourceRules,
  finalPoseChoice = remainder|equal|custom|none,
  customSplit?, approvedSchedule[]
```

## Required algorithm boundaries
The long break REPLACES a normal break. The end-pose resolution is user-selected during preview and must preserve the intervening break, final summary and total duration. The 180-minute example schedules are exact regression oracles.

## Technical issues for Stage 1
- Confirm target OS(s); pick the smallest maintainable UI/runtime for it.
- Determine sound reliability and clock behavior in background, sleep or phone lock under its restrictions.
- Confirm the precision and edge-case validation.
- Clarify whether long break count/position is explicitly selected vs automatically centered; show actual placement.
- Determine if presets are local-only initially (assumed yes).
- No server or user login is necessary for the approved behavior.

## Design sequence
Basic native UX first; request a logo image from the user during visual design. Provide an actual working preview before asking for approval of the interface stage.
