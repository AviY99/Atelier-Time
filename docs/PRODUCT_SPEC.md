# Product specification — Atelier Time

> **2026-10-23 — D-061 PLATFORM FEASIBILITY HOLD:** The application requirements **remain** offline, installable, same maintained product/features on Android/iOS/Windows/macOS, reliable clock and three audio cues. User explicitly clarified that **development, automated tests, builds, signing and deployment must also be workable cross-platform**, including the currently available Android Termux environment. The historical Flutter+Dart approval is **under reconsideration** because it did not prove that complete lifecycle; no framework replacement or relaxation of separate native installers has been authorized. See [reassessment](PLATFORM_REASSESSMENT_2026_10_23.md). Pause additional Flutter-specific UI coding pending feasibility proof. Preserve verified Dart logic, documentation and tests.


Status: **Approved product scope** on 2026-10-22. Stage 1 pure-Dart scheduling core subsequently implemented and screenshot-verified 35/35 tests PASS on Android Termux (D-052), on an unmerged branch. Stage 2 user subsequently authorized initial editor/preview SOURCE work (D-053/D-054), not yet Flutter-compiled/tested. No installable app, persistence or runtime timer/audio exists.

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

- Total session duration, **strictly positive**; 0 is invalid.
- Final reserved summary duration, including zero when desired.
- Optional ordered opening pose groups: count and duration per group (e.g. 4 x 1 minute, then 4 x 4 minutes).
- Nominal regular pose duration.
- Short break duration, **initial default 5 minutes**, editable to other **positive** durations (the planned whole-minute editor has a 1-minute minimum). A configured 0-minute break is invalid.
- Long replacement/middle break duration, **initial default 15 minutes**, editable to other **positive** durations, plus the number of optional long replacements. An active 0-minute special break is invalid; zero optional *count* is distinct from duration (subject to remaining Step 1.3 edge-policy approval).
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
- **Latest approved D-036:** The complete session's total duration and every **configured actual break duration must be positive**. A 0-minute session or break is a **blocking input error**, never a warn-and-override exception. New-session normal/special break defaults remain editable **5m / 15m**; their minimum at whole-minute input precision is **1 minute**.
- **Break safety (approved D-031, retained by D-036):** After **25 minutes of accumulated posing without adequate rest**, a break lasting **at least 5 minutes** is recommended before posing continues. Consecutive poses accumulate; **valid 1–4-minute breaks do not reset** the counter. Legal schedules that violate this *recommendation* remain allowed **only after visible warning, explicit user acknowledgment and full schedule approval**. Editing the session invalidates the prior acknowledgment. See [SCHEDULING_EDGE_POLICY.md](SCHEDULING_EDGE_POLICY.md).
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

## Development status (2026-10-23; D-052–D-054)

- **Verified Stage 1 scheduling core:** Android ARM64 Dart 3.13.7 screenshot 35 unit tests PASS; earlier Dart analyzer and smoke check passed. [Core PR #1](https://github.com/AviY99/Atelier-Time/pull/1) is open and unmerged.
- **Stage 2 editor:** User instructed “finish documentation and continue”; [Stage 2 plan](STAGE_2_PLAN.md) and [Flutter editor source branch](https://github.com/AviY99/Atelier-Time/tree/stage-2-session-editor) exist, [draft PR #2](https://github.com/AviY99/Atelier-Time/pull/2). Responsive input and full timeline preview are source-only and **NOT Flutter-compiled or tested**. Temporary Material theme is subject to user feedback; branding/logo comes later.
- Stage 3 offline saved presets, Stage 4 live timer/audio and installation remain **NOT IMPLEMENTED/NOT AUTHORIZED**. No paid GitHub workflows, deployments or budget increase.
