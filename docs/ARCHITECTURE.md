# Architecture and design constraints (pre-implementation)

Status: **Flutter + Dart and initial target OS approved in Stage 1 Step 1.1.** The detailed software architecture and data structures remain proposals pending Step 1.2 approval; no code has been created. See [TD-001](TECH_DECISION_001_PLATFORM.md).

## Required layers
1. **Rules/input validation**: validate editable counts, durations and cycle order.
2. **Pure deterministic schedule engine**: emit immutable ordered phases with ID/type, planned duration and elapsed start/end; no UI or sound responsibilities; uses integer time.
3. **Approval/allocator**: when last pose incomplete, provide remainder, near-equal, custom slider allocation, recompute all affected elapsed times, and commit only an approved schedule.
4. **Preset persistence**: named versioned data containing rules, allocated schedule and choice; reliable offline storage with migration path.
5. **Playback clock**: derive live state from actual elapsed clock and immutable scheduled phase boundaries (avoid accumulating timer callback delays); support pause/resume and interruption.
6. **Sound adapter**: route pose-end, break-end and final completion to three different sound cues; avoid double alerts.
7. **Shared cross-platform UI**: Flutter-rendered controls are acceptable for the per-session editor (all numbers editable), chronological review, split slider, preset library, live timer and settings; native OS widgets are optional, not required.
8. **Packaging**: platform-specific installable/offline builds for approved Android, Windows, iOS/iPadOS and macOS targets from the shared codebase; investigate platform limitations for background/lock-screen audio rather than promising behavior prematurely.

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
- **Step 1.1 COMPLETE:** Flutter + Dart, one application codebase for Android/Windows/iOS-iPadOS/macOS; platform-specific compiled packages; native graphics/menus optional.
- **Step 1.2 NOT STARTED:** Define the exact source-level Dart data structures and serialization interfaces only when separately authorized.
- Determine sound reliability and clock behavior in background, sleep or phone lock under its restrictions.
- Confirm the precision and edge-case validation.
- Clarify whether long break count/position is explicitly selected vs automatically centered; show actual placement.
- Determine if presets are local-only initially (assumed yes).
- No server or user login is necessary for the approved behavior.

## Design sequence
Prioritize an accessible, responsive, cohesive Flutter interface rather than insisting on native widgets. Request a logo image from the user during visual design. Provide an actual working preview before asking for approval of the interface stage.
