# ATELIER TIME — CURRENT PROJECT HANDOFF (v0.6)

**Date:** 2026-10-22  
**Project:** Atelier Time  
**Repository:** https://github.com/AviY99/Atelier-Time (existing public repo, default main)  
**Current status:** Stage 0 complete; Step 1.1 Flutter/Dart platform decision approved; Step 1.2 data-model design documented; **Step 1.3 scheduling edge/placement RULE PROPOSALS PREPARED FOR USER APPROVAL** following the user's latest “Continue”. **Step 1.4 coding NOT AUTHORIZED.** No Dart/Flutter application source, scaffold, automated test executions or installed build exists.

## User intent
Build a simple self-contained installed app for live-model sessions in **drawing and sculpting ateliers**. Prioritize a single codebase and consistent cross-platform functionality; native menus/controls are OPTIONAL and a Flutter-rendered interface is acceptable. Offline operation required. Set numerical timing rules, auto-generate complete sequence, approve it, save named presets, activate and run a countdown with sounds. App name is **Atelier Time**. User will provide logo picture when asked at the relevant design stage; graphic appearance must be discussed rather than invented. Log ALL decisions and progress in GitHub; update this handoff after every iteration so a new chat can resume immediately.

## Approved technical platform — Stage 1 Step 1.1 COMPLETE (2026-10-22)
- **Flutter + Dart**; **one shared maintained application codebase** for the first release on **Android phones/tablets, Windows computers, iOS/iPadOS (iPhone/iPad), and macOS**.
- Platform-specific compiled app/install packages are expected; thin platform-specific services are permissible for OS audio/notification/integration limitations, without separate application implementations.
- Native UI is NOT a requirement; non-native Flutter graphics/menus/controls are fine. Cross-platform feature parity has higher priority.
- Linux/web not required in the first release. Specific CI, signing, distribution and build machine choices are open.
- Authoritative decision: [TD-001](TECH_DECISION_001_PLATFORM.md), register IDs D-019 through D-022.
- User later said **“Continue”** after Step 1.2, authorizing **Step 1.3 documentation/discussion only**. See [SCHEDULING_EDGE_POLICY.md](SCHEDULING_EDGE_POLICY.md). **Step 1.4 and any coding are on hold; Step 1.3 policies require approval first.**

## Stage 1 Step 1.2 — documented design proposal (2026-10-22)
- Full design specification: [DATA_MODEL.md](DATA_MODEL.md). It proposes Dart-domain records for SessionRules, any number of ordered OpeningPoseGroups, RegularCycle, LongBreakRule/Placement, candidate SchedulePhases, errors/warnings, three EndPoseResolution methods, and frozen ApprovedSessionPreset after explicit approval.
- Every number in the approved examples remains editable in each eventual session input screen; optional opening groups can be added, removed or reordered. Example A and B input JSON drafts are provided **only as canonical test fixtures**, not mandatory presets or hardcoded limits.
- Proposed technical representation: integer seconds and elapsed start/end offsets. User-facing approved custom split step remains one minute, with minimum one minute per pose when feasible. Phases occupy contiguous half-open time ranges; work ends before reserved summary.
- Crucial distinction: the records/schema/field names are DESIGN PROPOSALS, not implemented/compiled Dart code and not independent user approval of new edge behaviors.
- Still unresolved and reserved for proposed Step 1.3: exact automatic-middle replacement break choice and ties, multiple long break positions, infeasible schedule inputs, short/zero breaks and insufficient last-two-pose cases. No defaults quietly settled.
- Iteration 0.5 created decisions D-023 through D-025. The user **subsequently authorized Step 1.3 design** via “Continue”; the current gate is **approval of Step 1.3 policy proposals and explicit Step 1.4 authorization**, not another Step 1.3 request.

## Stage 1 Step 1.3 — proposed scheduling policies (2026-10-22)
- **Specification:** [SCHEDULING_EDGE_POLICY.md](SCHEDULING_EDGE_POLICY.md), proposal P-01 to P-06, **NOT YET USER-APPROVED AS BEHAVIOR**.
- P-01: automatic **one long break** replaces the eligible short break whose replacement interval **center is nearest the work-window midpoint**; ties go to earlier eligible break. Both canonical examples still replace the **third** short break (A at 01:20–01:35; B at 01:25–01:40).
- P-02: when N long replacement breaks requested, distribute their centers approximately evenly over the work window using target fractions j/(N+1), deterministic earlier-ordinal tie-breaking, with feasibility enforcement. No silent reduction of N.
- P-03: manual break positioning by distinct numbered short-break slots; editing rules must not silently relocate manually chosen slots.
- P-04: explicit validation of impossible inputs (no negative or zero total, summary cannot consume full session, opening poses cannot overrun work, long break longer than short, reject infeasible replacements); propose 0-minute summary valid, 0-minute short break disables breaks and forbids replacements, opening-only sessions valid if they fit exactly. All are **proposed, not approved**.
- P-05: last incomplete pose still triggers the approved three-option dialog; missing final-two-pose + break structure disables the infeasible split choices with explanations. Propose allowing custom split first pose to exceed regular duration if explicitly selected.
- P-06: full preview of actual placements, all phase timings and validation messages before approval; any edited numeric rule invalidates approval.
- Reference fixtures [EXAMPLES.md](EXAMPLES.md) remain fixed. The document is an engineering proposal, NOT executable scheduling code or a passing test report.
- Decisions D-026 (authorized design), D-027/D-028 (proposals), D-029 (Step 1.4 hold).

## Approved core requirements
- **Each new or edited session has its own numeric input screen; every number in the examples can be changed**, opening pose groups can be added/removed; edits recalculate the whole schedule, which must be re-approved. Example A/B are test fixtures, not hardcoded fixed session choices.
- Editable total session and final reserved summary durations.
- Configurable count/duration of optional opening pose groups.
- Repeated pose/short-break cycle, pose-first or break-first.
- Configurable regular pose length, short break, long replacement break length/count and placement.
- Long break **replaces** one short break, rather than adding extra break time.
- Preview full ordered phase list with duration and elapsed start/end; explicit approval before saving and activating.
- Saved presets: create/list/edit/rename/duplicate/delete/activate, available offline.
- On an incomplete final pose, ask during **review** for one of 3 solutions: (1) keep final remainder, (2) split last two poses as close as possible to 50/50, (3) use time ruler/slider to divide last two. Show both durations and all updated times; preserve interposing break and total/summary. One-minute slider steps and one-minute minimum per pose are accepted initial rules, when feasible.
- Running timer displays phase, large countdown, next phase and overall time, pause/resume/stop. Three different audible cues: pose/set ending, break ending, whole sequence ending.

## Approved example A (180 minutes)
- 165 minute work block + 15 minute summary.
- 4×1-minute poses, 4×4-minute poses, then 5m break / 25m pose repeating, with one 15m break replacing middle short break.
- 00:00–00:20 opening poses; 00:20–00:25 short break; 00:25–00:50 pose; 00:50–00:55 break; 00:55–01:20 pose; **01:20–01:35 long break**; 01:35–02:00 pose; 02:00–02:05 break; then last-two-poses window **02:05–02:45** with 35 pose minutes separated by 5m break; 02:45–03:00 summary.
- Ending options: remainder **25+10** (break 02:30–02:35); near-even **17+18** (break 02:22–02:27); custom example **20+15** (break 02:25–02:30).

## Approved example B (180 minutes)
- 165 minute work block + 15 minute summary.
- 25-minute pose / 5-minute break repeating, with one 15m replacement break midway.
- 00:00–00:25 pose; 00:25–00:30 break; 00:30–00:55 pose; 00:55–01:00 break; 01:00–01:25 pose; **01:25–01:40 long break**; 01:40–02:05 pose; 02:05–02:10 break; last-two-poses window **02:10–02:45** with 30 pose minutes and 5m break; 02:45–03:00 summary.
- Ending options: remainder **25+5** (break 02:35–02:40); even **15+15** (break 02:25–02:30); custom example **18+12** (break 02:28–02:33).

Full per-pose tables in docs/EXAMPLES.md. Each reference baseline totals **130m posing + 35m breaks + 15m summary = 180m**. Never alter these regression examples without explicit approval.

## Core invariants
Stage intervals contiguous, no overlap/gap. Total exactly D, summary last F. Long break replaces short break at one eligible slot, not appended. Final-two-pose alternatives preserve combined pose time and intervening break, fixed lead-in, final summary and total. Do not silently auto-shorten and accept; user chooses during preview. A preview, saved preset and runtime use one consistent approved sequence.

## Decisions and unresolved details
The decision register is docs/DECISIONS.md (D-001 through D-029). Target OS/framework are approved. Step 1.2 data model remains the current concept; Step 1.3 P-01–P-06 are **awaiting explicit user approval** (not settled defaults). Also open: distribution/build/signing, branding, exact summary activity and per-pose versus per-set audio. Reuse the existing GitHub repository.

## Roadmap and current gate
**Stage 0 COMPLETE:** Documentation baseline committed to existing GitHub repository, all 11 files verified readable (this iteration).  
**Stage 1 — Step 1.1 COMPLETE:** Flutter/Dart and OS targets approved. **Step 1.2 DOCUMENTED:** [DATA_MODEL.md](DATA_MODEL.md). **Step 1.3 PROPOSALS PREPARED, USER APPROVAL PENDING:** [SCHEDULING_EDGE_POLICY.md](SCHEDULING_EDGE_POLICY.md). **Step 1.4 NOT AUTHORIZED:** Dart schedule generator/test implementation requires separate permission.  
**Stage 2:** Cross-platform Flutter UI including per-session numeric input editor, preview/approval and split ruler; discuss visual design.  
**Stage 3:** Offline saved presets.  
**Stage 4:** Playback timer, three sounds, interruptions.  
**Stage 5:** Device QA, installability and accessibility.  
**Stage 6:** Versioned release and maintenance handoff.

User approved Step 1.1, then “Continue” to begin Step 1.2, and then “Continue” to begin Step 1.3. Both continuations authorized only the corresponding **documentation/design** steps. Step 1.3 behavior proposals have not yet been formally approved; **Step 1.4, source code/scaffolding and testing are NOT authorized**. Each next sub-step requires separate explicit permission.

## Documentation operation instructions for next chat
1. Read this handoff, [TD-001](TECH_DECISION_001_PLATFORM.md), [DATA_MODEL.md](DATA_MODEL.md), [SCHEDULING_EDGE_POLICY.md](SCHEDULING_EDGE_POLICY.md), docs/PRODUCT_SPEC.md, docs/SCHEDULING_RULES.md, docs/EXAMPLES.md, docs/ROADMAP.md, docs/DECISIONS.md, docs/TEST_PLAN.md and docs/WORK_LOG.md.
2. Verify GitHub repository current state before writing; reuse existing files, never create duplicate repo.
3. Maintain same decisions and exactly reproduce reference schedules.
4. For any implementation iteration commit source/tests, update decision log, work log, test results, changelog and THIS HANDOFF; link commits and clearly separate verified behavior from proposed behavior.
5. If current stage still pending approval, stop and request approval rather than proceeding with app coding.
6. Logo and graphic style only discussed at relevant stage after user image supplied.

**Immediate next action:** Present proposed policies P-01 through P-06 to user and ask **approval or changes**. Then ask for separate permission to begin **Step 1.4 (Dart scheduling engine and regression tests)**. Until that permission is explicitly given, **STOP**: no Dart/Flutter project, source code, tests or UI.
