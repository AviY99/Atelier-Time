# ATELIER TIME — CURRENT CROSS-CHAT PROJECT HANDOFF (v1.0)

> **CURRENT AUTHORITATIVE STATE — 2026-10-25, D-109 / ANDROID STABLE 4:** Android D-108 user-accepted and frozen at [`stable/android-d108`](https://github.com/AviY99/Atelier-Time/tree/stable/android-d108), commit `02beb0af46719301cccb72ad4698a337ab416d73`. The historical Flutter/Dart and early pre-build status below is **SUPERSEDED** by [Stable 4 handoff](CHAT_HANDOFF_AFTER_STABLE_4_2026_10_25.md), [Stable 4 checkpoint](ANDROID_STABLE_4_D108.md), and the [next open Android reliability QA gate](ANDROID_RELIABILITY_QA_POST_D108.md). Kotlin + Jetpack Compose / Code on the Go is approved. No further app build or re-test of accepted UI, sounds, timers or Back/Swipe notifications is needed for the handoff. Connected Android instrumentation, Doze/process kill, long-run, reboot/storage recovery and Play FGS policy are not yet passed. Real D-ID and increasing `versionCode` are mandatory for each future APK, as is logo/icon user approval before Play publication.

**Updated:** 2026-10-22  
**Repository:** https://github.com/AviY99/Atelier-Time (existing public repo; default `main`)  
**Technology APPROVED:** **Flutter + Dart**, **ONE shared application codebase** for first release on **Android phones/tablets, Windows computers, iOS/iPadOS and macOS**. Different install artifacts per OS are acceptable; native-looking menus/graphics are OPTIONAL. No web or Linux release required initially.  
**Status:** Stage 0 documentation complete; Stage 1 Step 1.1 platform choice approved; Step 1.2 conceptual Dart data model documented; **Stage 1 Step 1.3 P-01–P-06 scheduling rules APPROVED / COMPLETE** under user decision **D-037**. **One specifically unresolved question D-038** (whether positive special break <= positive normal break) remains deliberately OPEN and should not be guessed. **Stage 1 Step 1.4 programming/tests NOT AUTHORIZED** (D-039). **NO app source code, Flutter project/scaffold, working timer, automated executed tests, UI, audio or installable build exists.**

## Latest mandatory precedence — 0-minute sessions/breaks

**D-036 (2026-10-22) is the LATEST approved clarification and overrides any contrary older note:**

> User: “You are correct, Break of 0 or session of 0 is not valid.”

Therefore:
- A **complete session total MUST be >0**. A 0-minute session is a **hard validation ERROR** and cannot be preview-approved, saved or activated.
- Every **configured actual regular/short break or active long/special replacement break MUST have duration >0**. A break lasting 0 minutes is a **hard validation ERROR**; it cannot be waived by the 25/5 advisory warning mechanism. In the planned whole-minute editor, the smallest valid configured break is **1 minute**.
- New-session input defaults: **normal/short break 5 minutes**; **long/special replacement break 15 minutes**. Both remain independently editable to **other POSITIVE values**.
- The previous D-031 approval of 0-minute breaks and the follow-up proposal for “logical 0-minute break slots” are **SUPERSEDED/WITHDRAWN**. Do not implement them. Old work log, changelog or superseded decision text is history, **not current authority**.
- **APPROVED D-037:** Zero **number** of optional special replacements means none are scheduled (NOT a 0-duration break). A zero-duration **reserved final summary** `F=0` is permitted when the complete session and work block are positive; it is different from an invalid complete-session duration `D=0`.

**D-031 remains APPROVED insofar as it defines the 25/5 advisory warning:**
- After **25 minutes of accumulated posing without a qualifying break**, recommend a **>=5-minute rest** before posing continues. Add together durations of consecutive poses, including consecutive 1-minute or 4-minute opening poses; simply reaching a pose boundary does **not** reset the counter.
- Only a **break >=5 minutes** resets the posing counter. A **valid positive 1–4-minute break does not** reset it. A single pose running past 25 minutes also triggers a warning.
- A legal schedule violating the 25/5 recommendation shows a clear **WARNING** and requires **explicit separate user acknowledgment plus ordinary schedule approval** before saving/activating. Never silently alter entered numbers.
- An edit, placement change or final-pose split change invalidates the prior warning acknowledgment and ordinary approval; recompute from the final full timeline.
- A **0-minute break/session is an ERROR, not a warning**: a user cannot override it.
- **APPROVED D-037:** Accumulated posing/rest-warning counter is **within one complete activated session**; do **not automatically carry it across separately activated saved presets**. A full session ending at **exactly 25 minutes** of uninterrupted accumulated posing needs no invented trailing break; an uninterrupted pose exceeding 25m still warns. Separate cross-preset carryover would be a new feature requiring separate approval.

## User purpose and full approved features

**Atelier Time** is an offline-capable installable timer for **live-model drawing and sculpting ateliers**. Organizer configures, previews and approves an exact complete sequence, saves reusable named sessions, activates the approved schedule and runs a timer with sounds. The user supplies a logo/image later during visual design; **do not invent it** now.

**Every numeric parameter in BOTH examples is editable for EACH full session**, not hardcoded: total duration, final reserved summary, any number of ordered opening pose groups with user-editable pose counts/durations (can add, remove, reorder), recurring regular pose length, normal break duration, special replacement length and count/placement, cycle pose-first or break-first, and final-pose allocation. Editing regenerates an entirely new preview, needing approval. Future Stage 2 editor must permit changing all these numbers.

Opening poses flow consecutively in the canonical Example A (no default breaks between them). An active long/special break **REPLACES** a scheduled short-break slot; never append another break at that boundary. The final reserved summary is included **inside** the total; its exact activity remains undefined. No trailing normal break without a following pose. All actual phase time ranges are contiguous, non-overlapping and strictly positive, covering the total exactly.

If the final repeating pose is incomplete, offer during review:
1. **Remainder**: keep previous nominal pose full length; last pose takes leftover.
2. **Near-equal**: distribute the combined available posing time of the last two poses as floor/ceil at whole-minute precision.
3. **Custom ruler/slider**: divide that combined time in whole-minute steps, >=1m for each pose where feasible.

Two-pose formula: `S` = start of earlier final pose, `E` = work-window end, `Q` = duration of the existing intervening **positive** break, `T=E−S−Q`. For selected first pose length `x`, phases are first pose `[S,S+x)`, intervening break `[S+x,S+x+Q)`, final pose `[S+x+Q,E)`. All preceding phases, Q, session total, and summary remain unchanged. If structurally impossible, do not create fictitious poses/breaks.

Saved-session operations later: create/name, review/approve, save, list, activate, edit/reapprove, duplicate, rename and delete; usable offline. Runtime later: current phase and countdown, next phase, elapsed/remaining time, pause/resume/stop, three distinct audible cues (pose/set ending; break ending; entire sequence ending). Exact per-opening-pose vs per-set sound nuance remains OPEN. Timing should use monotonic elapsed clock, with later platform-specific notification/audio tests.

## Two canonical THREE-HOUR reference fixtures — never change without approval

Each example has **D=180m**, final summary **F=15m**, work `W=165m`, normal pose `P=25m`, normal break `B=5m`, one long replacement `L=15m`. Both approved final baselines total **130m posing + 35m breaks + 15m summary**. Every number is a configurable *example input*, NOT a product restriction.

**Example A — opening 4×1m, then 4×4m; recurring break-first:**

```text
00:00–00:01 Pose 1 1m
00:01–00:02 Pose 2 1m
00:02–00:03 Pose 3 1m
00:03–00:04 Pose 4 1m
00:04–00:08 Pose 5 4m
00:08–00:12 Pose 6 4m
00:12–00:16 Pose 7 4m
00:16–00:20 Pose 8 4m
00:20–00:25 Short break 5m
00:25–00:50 Pose 9 25m
00:50–00:55 Short break 5m
00:55–01:20 Pose 10 25m
01:20–01:35 LONG replacement break 15m
01:35–02:00 Pose 11 25m
02:00–02:05 Short break 5m
02:05–02:30 Pose 12 25m
02:30–02:35 Short break 5m
02:35–02:45 Pose 13 10m (partial)
02:45–03:00 Summary 15m
```

A end-pair (S=02:05, E=02:45, Q=5m, T=35m):
- Remainder 25+10: Pose12 02:05–02:30, break 02:30–02:35, Pose13 02:35–02:45.
- Near-even 17+18: Pose12 02:05–02:22, break 02:22–02:27, Pose13 02:27–02:45.
- Custom sample 20+15: Pose12 02:05–02:25, break 02:25–02:30, Pose13 02:30–02:45.

**Example B — no opening poses, recurring pose-first:**

```text
00:00–00:25 Pose 1 25m
00:25–00:30 Short break 5m
00:30–00:55 Pose 2 25m
00:55–01:00 Short break 5m
01:00–01:25 Pose 3 25m
01:25–01:40 LONG replacement break 15m
01:40–02:05 Pose 4 25m
02:05–02:10 Short break 5m
02:10–02:35 Pose 5 25m
02:35–02:40 Short break 5m
02:40–02:45 Pose 6 5m (partial)
02:45–03:00 Summary 15m
```

B end-pair (S=02:10, E=02:45, Q=5m, T=30m):
- Remainder 25+5: Pose5 02:10–02:35, break 02:35–02:40, Pose6 02:40–02:45.
- Near-even 15+15: Pose5 02:10–02:25, break 02:25–02:30, Pose6 02:30–02:45.
- Custom sample 18+12: Pose5 02:10–02:28, break 02:28–02:33, Pose6 02:33–02:45.

Full reference inputs and tables: [EXAMPLES.md](EXAMPLES.md). Neither example has a 25/5 warning.

## Stage 1 Step 1.3 — policies P-01–P-06 APPROVED / COMPLETE (D-037)

**Read latest [STEP_1_3_REVIEW.md](STEP_1_3_REVIEW.md) and [SCHEDULING_EDGE_POLICY.md](SCHEDULING_EDGE_POLICY.md).**

- **P-01 APPROVED:** one long break chosen at the eligible ordinary break whose **replacement interval center** is closest to midpoint of working time `W/2`; tie earlier ordinal; must reproduce A/B (each third short break).
- **P-02 APPROVED:** multiple long replacements distributed approximately evenly (targets `j/(N+1)` work time), in feasible distinct break slots; deterministic earlier-ordinal ties. Zero optional special replacement *count* means none, not a 0m break.
- **P-03 APPROVED:** manual selection by normal break ordinals; errors if editing invalidates a chosen ordinal; never silent relocation.
- **P-04 APPROVED:** mathematically invalid edge inputs (opening groups overlong, summary consuming total, no positive final phase, trailing phantom breaks or excessive replacement count) are hard errors; `F=0` summary, opening-only positive sessions that fit exactly and zero optional replacement *count* are valid. **D-036:** actual session and active normal/special break durations must be positive.
- **P-05 APPROVED:** if final-two-pose structure is unavailable, disable infeasible split options; permit explicitly chosen custom first pose >nominal if both poses stay positive; do not insert extra break or pose.
- **P-06 APPROVED:** preview displays every stage and validation; distinguish blocking errors from advisory 25/5 warnings; require explicit user acknowledgment stored per approved revision, and invalidate approval on edits.
- **D-037 RESOLVED:** an exactly 25m ending needs no trailing invented break, and pose-time warning counter **does not auto-carry** between separate saved preset activations. **ONLY narrow OPEN D-038:** whether a **positive** special break may be shorter than/equal to the regular **positive** break. This was expressly labeled unresolved in the review, so no acceptance/rejection behavior may be invented without user decision.
- **Zero-length logical break slots and any final-two split across such a slot were WITHDRAWN by D-036. Do not implement.**

## Design model, tests and GitHub artifacts

- **[TECH_DECISION_001_PLATFORM.md](TECH_DECISION_001_PLATFORM.md)** records approved Flutter + Dart, four release targets and allowance for non-native UI.
- **[DATA_MODEL.md](DATA_MODEL.md)** defines only **conceptual** Dart types: `SessionRules`, `OpeningPoseGroup`, `RegularCycle`, `LongBreakRule/Placement`, `SchedulePhase`, `ScheduleCandidate`, `EndPoseResolution`, `ApprovedSessionPreset`, `ScheduleIssue`. Internally integer seconds; whole-minute UI initially; records are not yet executable Dart.
- **[TEST_PLAN.md](TEST_PLAN.md)** includes canonical T01–T18, approved-but-unexecuted R-B01..R-B18 for D-036 positive inputs + D-031 25/5 warnings, and approved P-T01..P-T12 expectations **except open P-T11 (D-038)**. **NO tests have run; do not claim passes.**
- **[PRODUCT_SPEC.md](PRODUCT_SPEC.md)** approved product scope, **[SCHEDULING_RULES.md](SCHEDULING_RULES.md)** core contract, **[ROADMAP.md](ROADMAP.md)** gates, **[DECISIONS.md](DECISIONS.md)** chronological register through **D-039**, **[WORK_LOG.md](WORK_LOG.md)** iterations, **[CHANGELOG.md](../CHANGELOG.md)** history.
- **Decision precedence:** D-036 supersedes D-031's previous 0-minute break allowance, but D-031 25/5 warning remains. D-037 approves P-01–P-06 and within-one-session warning scope, with zero summary / zero special-break **count** permitted. **D-038** is the sole named open break-length comparison. **D-039** blocks Step 1.4 without new authorization. D-030/D-032/D-034/D-035 are earlier historical/proposal records, NOT permission to create zero-minute break slots.
- GitHub is the authoritative store. After every iteration, update decisions/roadmap/work log/changelog and **THIS HANDOFF**; verify GitHub read-back and link concrete commits. Do not create a duplicate repo.

## Stage gate and exact next action

Stage 0 COMPLETE; Stage 1 **1.1 COMPLETE**, **1.2 DESIGN DOCUMENTED**, **1.3 APPROVED / COMPLETE (D-037)**; **1.4 NOT AUTHORIZED (D-039)**. Later Stage 2 per-session responsive Flutter editor and preview; Stage 3 offline presets; Stage 4 monotonic timer/audio; Stage 5 multi-device QA/installability; Stage 6 release.

**Immediate next response in a new chat:** Confirm Step 1.3 completed and link the [approved review](STEP_1_3_REVIEW.md), [decisions](DECISIONS.md), and this current handoff. **STOP.** When the user is ready, seek **explicit permission to start Step 1.4** (Dart scheduling engine, test suite/scaffold), and obtain user guidance on **D-038 positive special-vs-normal break length** before implementing that comparison. **No Flutter scaffolding, Dart source code, scheduling engine, UI, tests or builds are authorized by Step 1.3 approval.** User supplies logo only at branding stage.
