# Architecture and design constraints — implemented core, Stage 2 editor underway

**Current status (2026-10-23, D-052–D-054):** Flutter + Dart and four target OS families approved. The Stage 1 pure-Dart scheduling core is **implemented and screenshot-verified 35/35 tests PASS** on isolated branch `stage-1-4-scheduling-core`; see [core integration PR #1](https://github.com/AviY99/Atelier-Time/pull/1). Stage 2 responsive editor/preview was subsequently authorized for preliminary source work and drafted on `stage-2-session-editor`; see [plan](STAGE_2_PLAN.md), [draft PR #2](https://github.com/AviY99/Atelier-Time/pull/2). **Neither PR is merged. Flutter UI has not compiled or run.** No saved presets, live timer, audio, APK, GitHub Actions or paid build.

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

The more detailed authoritative **proposal** (including user-editable opening groups, data types, draft/approval lifecycle and JSON examples) is [DATA_MODEL.md](DATA_MODEL.md). The abbreviated shape below remains introductory only.
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
- **Step 1.2 DESIGN PREPARED, NOT IMPLEMENTED:** SessionRules, opening groups, cycle, break-placement policy, candidate phases and approved preset are defined conceptually in [DATA_MODEL.md](DATA_MODEL.md). User review is pending.
- **Step 1.3 NOT AUTHORIZED:** Finalize automatic-middle positioning and edge-case behavior only after user's next approval.
- Determine sound reliability and clock behavior in background, sleep or phone lock under its restrictions.
- Confirm the precision and edge-case validation.
- Clarify whether long break count/position is explicitly selected vs automatically centered; show actual placement.
- Determine if presets are local-only initially (assumed yes).
- No server or user login is necessary for the approved behavior.

## Design sequence
Prioritize an accessible, responsive, cohesive Flutter interface rather than insisting on native widgets. Request a logo image from the user during visual design. Provide an actual working preview before asking for approval of the interface stage.
