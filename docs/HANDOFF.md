# ATELIER TIME — CURRENT PROJECT HANDOFF (v0.3)

**Date:** 2026-10-22  
**Project:** Atelier Time  
**Repository:** https://github.com/AviY99/Atelier-Time (existing public repo, default main)  
**Current status:** Approved product scope; documentation baseline complete and all 11 files successfully fetched from GitHub. No app code or installed build exists. Awaiting explicit approval to start Stage 1.

## User intent
Build a simple self-contained installed app for live-model sessions in **drawing and sculpting ateliers**. Native menus/controls wherever possible, preferably offline. Set numerical timing rules, auto-generate complete sequence, approve it, save named presets, activate and run a countdown with sounds. App name is **Atelier Time**. User will provide logo picture when asked at the relevant design stage; graphic appearance must be discussed rather than invented. Log ALL decisions and progress in GitHub; update this handoff after every iteration so a new chat can resume immediately.

## Approved core requirements
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
The decision register is docs/DECISIONS.md (D-001 through D-018). All product features above are approved; platform/OS, tech stack, branding, exact midpoint tie-breaks, edge cases, summary's activity and set/pose alert nuance require discussion at appropriate stages. Avoid treating these as finalized. The GitHub repository was already present, so it was reused.

## Roadmap and current gate
**Stage 0 COMPLETE:** Documentation baseline committed to existing GitHub repository, all 11 files verified readable (this iteration).  
**Stage 1 (NOT AUTHORIZED):** Confirm target platform/stack, implement deterministic scheduling engine and validations including all three ending choices; automate Example A/B regression tests; report real results.  
**Stage 2:** Native-feeling UI editor, preview/approval and split ruler; discuss visual design.  
**Stage 3:** Offline saved presets.  
**Stage 4:** Playback timer, three sounds, interruptions.  
**Stage 5:** Device QA, installability and accessibility.  
**Stage 6:** Versioned release and maintenance handoff.

Only begin Stage 1 after the user explicitly says yes. Each next stage should be separately reviewed/approved.

## Documentation operation instructions for next chat
1. Read this handoff, docs/PRODUCT_SPEC.md, docs/SCHEDULING_RULES.md, docs/EXAMPLES.md, docs/ROADMAP.md, docs/DECISIONS.md, docs/TEST_PLAN.md and docs/WORK_LOG.md.
2. Verify GitHub repository current state before writing; reuse existing files, never create duplicate repo.
3. Maintain same decisions and exactly reproduce reference schedules.
4. For any implementation iteration commit source/tests, update decision log, work log, test results, changelog and THIS HANDOFF; link commits and clearly separate verified behavior from proposed behavior.
5. If current stage still pending approval, stop and request approval rather than proceeding with app coding.
6. Logo and graphic style only discussed at relevant stage after user image supplied.

**Immediate next action:** show the user the completed GitHub documentation and working stages and ask: “Do you approve starting Stage 1 — technical platform choice and scheduling engine with tests?”
