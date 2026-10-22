# ATELIER TIME — CURRENT CROSS-CHAT PROJECT HANDOFF (v1.4)

**Updated:** 2026-10-22  
**Repository:** https://github.com/AviY99/Atelier-Time (existing public repo; default `main`)  
**Technology APPROVED:** **Flutter + Dart**, **ONE shared application codebase** for first release on **Android phones/tablets, Windows computers, iOS/iPadOS and macOS**. Different install artifacts per OS are acceptable; native-looking menus/graphics are OPTIONAL. No web or Linux release required initially.  
**Status:** Stage 0 and Stage 1 Steps 1.1–1.3 approved/documented, including P-01–P-06 (D-037). **Stage 1 Step 1.4 NOW AUTHORIZED AND IN PROGRESS (D-040)** on isolated branch [stage-1-4-scheduling-core](https://github.com/AviY99/Atelier-Time/tree/stage-1-4-scheduling-core). **Pure Dart scheduling engine, test sources and smoke tool exist ON THAT BRANCH ONLY**, not yet merged to main. **NO Dart SDK, no analyzer/test execution, no GitHub Actions workflows or builds, and no Flutter UI/scaffold/timer/audio/installed app.** User's GitHub metered budget is blocked by another project; do not invoke billable Actions (D-041). **Step 1.4 NOT COMPLETE**. D-038 is RESOLVED by **approved D-044** (any positive special replacement duration, including shorter/equal). **Latest refinements D-043/D-045/D-046:** automatic replacement placement uses exact dynamic programming; approved warnings persist with full timestamps in immutable session snapshots; candidate construction is library-private to prevent forged approvals. Dart tests are written but NOT RUN. No SDK in the assistant's container, and DNS cannot fetch it. Budget-free Windows/macOS/Linux local verification scripts are now on the development branch.

## Step 1.4 additional work — SDK blocker diagnosis and local test helpers (D-046)

**User question:** “What to do to solve ur failure? Can you continue?” YES: continued **only** authorized Step 1.4 source hardening and test preparation, not Stage 2 or billing-dependent Actions.

**Environment/root cause:** In assistant Linux container, `dart` and `flutter` are absent and none found in local installations. Command-line DNS for `storage.googleapis.com`, `github.com`, `dart.dev` fails; `curl` cannot fetch SDK binaries. This is a **missing test toolchain / network access problem**, not a confirmed Dart engine compile/test failure.

**Completed on isolated branch** [stage-1-4-scheduling-core](https://github.com/AviY99/Atelier-Time/tree/stage-1-4-scheduling-core):
- Engine [lib/src/scheduling.dart](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/lib/src/scheduling.dart): made `ScheduleCandidate._` constructor private to its Dart library; callers cannot construct an approvable fake schedule. `ApprovedSession` now contains immutable full `acknowledgedWarnings` (including actual elapsed time) and keeps `acknowledgedWarningCodes` for UI access.
- Expanded [test/scheduling_test.dart](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/test/scheduling_test.dart) with **unexecuted** test checking preserved warning timestamp and immutable lists.
- Added [tool/verify.ps1](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/tool/verify.ps1) (Windows PowerShell) and [tool/verify.sh](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/tool/verify.sh) (macOS/Linux Bash) plus `.gitignore`. These scripts run `dart --version`, `dart tool/smoke.dart`, `dart pub get`, `dart analyze`, `dart test`, and write `atelier-time-verification.log`; they do NOT use GitHub Actions.
- **NO source was merged into `main`; NO Dart test/analyzer/compiler/smoke execution took place** because SDK is missing. No paid runners, CI, builds, or budget changes. **Step 1.4 remains IN PROGRESS**.

**Minimal user-assisted unblock when necessary:** Install **ONLY the Dart SDK** from https://dart.dev/get-dart on a computer (Flutter is NOT needed to test this pure Dart scheduling core); download/clone the `stage-1-4-scheduling-core` branch; run `powershell -ExecutionPolicy Bypass -File tool\verify.ps1` on Windows from repo root, or `bash tool/verify.sh` on macOS/Linux; share `atelier-time-verification.log` for fixes. Installing Dart on the user's machine **does not** install it inside the assistant's container. No passwords or secrets should be shared.

**Next code gate:** Fix actual errors revealed by Dart execution before declaring Step 1.4 complete. Continue this step without another authorization; **Stage 2 and other activities remain blocked**.

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
- **D-037 RESOLVED:** an exactly 25m ending needs no trailing invented break, and pose-time warning counter **does not auto-carry** between separate saved preset activations. **D-038 now RESOLVED via D-044:** positive special breaks may be shorter/equal/longer than positive normal breaks; D-031 warning and D-036 zero-duration ban remain in force.
- **Zero-length logical break slots and any final-two split across such a slot were WITHDRAWN by D-036. Do not implement.**

## Latest approved D-044 — positive shorter/equal special replacements (2026-10-22)

The user explicitly approved: **“allow any positive special break duration, even shorter than or equal to the normal break, with the already approved 25/5 rest warnings. Zero-minute breaks remain invalid.”**

- **D-044 resolves formerly OPEN D-038.** A selected special break of **L>0** can be shorter, equal, or longer than positive ordinary B. It replaces B (it does NOT add to B). D-036 prohibits 0-minute sessions/configured breaks; D-031 requires the 25/5 rest warning and affirmative acknowledgement when applicable.
- **Branch code:** [scheduling engine](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/lib/src/scheduling.dart) accepts all positive special lengths and generalizes exact DP placement for negative, zero, and positive `L-B`. The old `pendingDecision` rejection is gone.
- **Tests written NOT RUN:** [regression suite](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/test/scheduling_test.dart) and [smoke script](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/tool/smoke.dart) cover normal=5m with special=1m/3m/5m, multiple shorter replacements, warnings and explicit approval. A/B canonical cases unchanged.
- **Independent Python design checks** matched brute force across **25,000 virtual-schedule random cases** and **4,000 additional real-schedule exhaustive cases**, including `L<B`, `L=B` and `L>B`. These do **NOT verify Dart code**.
- **No Dart/Flutter SDK, no executable Dart tests, analyzer, smoke execution or build.** SDK download DNS unavailable. GitHub Actions/paid runners intentionally not run owing to user-reported account budget block. Branch unmerged. **Step 1.4 remains IN PROGRESS; Stage 2 NOT authorized.**

## Stage 1 Step 1.4 — newly started coding branch, user budget blocked

- **Authorization D-040:** The user said “Continue” after asking how to work with minimum intervention while GitHub's $12 metered budget was exhausted on another project. This explicitly started Step 1.4 only.
- **Branch:** [stage-1-4-scheduling-core](https://github.com/AviY99/Atelier-Time/tree/stage-1-4-scheduling-core), forked from the existing `main`. All new code/test files are on this branch **only**.
- **Dart package files:** `pubspec.yaml`, `lib/atelier_time.dart`, `lib/src/scheduling.dart`. Pure Dart, no Flutter or paid runner requirements.
- **Test SOURCE:** `test/scheduling_test.dart` (canonical Example A/B complete precise timelines, all three final-pose alternatives, schedule invariants, manual/automatic special break selection, D-036 positive durations, D-031 warnings and explicit acknowledgment, D-044 shorter/equal positive replacement approval). `tool/smoke.dart` runs a subset with only Dart SDK, no external packages. Source files are committed and read back, **but have not executed**.
- **Branch-only status:** [docs/STAGE_1_4_PROGRESS.md](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/docs/STAGE_1_4_PROGRESS.md) details code, assumptions and limits, including the newly fixed optimizer (D-043).
- **Environment:** Git available, but **no Dart or Flutter SDK** in current execution environment. No successful code compilation, `dart analyze`, `dart test`, smoke execution, Actions run or installer. A structural bracket/comment/string check passed, **not a compiler test**.
- **Budget safety D-041:** No `.github/workflows` additions, Actions invocations, billable build/deployment or user budget changes. User reports GitHub billing limit blocks other project. Continue with branch source/offline checks until a safe Dart execution environment is available.
- **D-038 RESOLVED by D-044:** normal rest 5m, special rest 3m is approved; any positive special duration is legal. The code no longer reports `pendingDecision`. D-031 rest warnings/acknowledgment and D-036 invalid 0m sessions/breaks remain mandatory. **D-043 improvement:** replaced exponential combination enumeration with exact dynamic programming over adjusted virtual break-slot timing, avoiding false-rejection of valid schedules. Additional test sources cover a 97m formerly false-invalid case and 480m with five long replacements. Independent **Python-only** comparison with exhaustive search matched 25,000 random small cases and canonical A/B placements; this does **NOT** execute/verify Dart. Dart SDK is still absent; Dart compilation, static analysis, actual tests and broader performance tests remain pending.
- **Stage 1.4 remains IN PROGRESS**; no Flutter app screens, live timer, saved preset database, audio, cross-platform builds or later-stage work.

## Design model, tests and GitHub artifacts

- **[TECH_DECISION_001_PLATFORM.md](TECH_DECISION_001_PLATFORM.md)** records approved Flutter + Dart, four release targets and allowance for non-native UI.
- **[DATA_MODEL.md](DATA_MODEL.md)** defines only **conceptual** Dart types: `SessionRules`, `OpeningPoseGroup`, `RegularCycle`, `LongBreakRule/Placement`, `SchedulePhase`, `ScheduleCandidate`, `EndPoseResolution`, `ApprovedSessionPreset`, `ScheduleIssue`. Internally integer seconds; whole-minute UI initially; records are not yet executable Dart.
- **[TEST_PLAN.md](TEST_PLAN.md)** includes canonical T01–T18, R-B01..R-B18, P-T01..P-T12 (including approved D-044 P-T11). **Step 1.4 has now added real Dart test SOURCE on a development branch, but NO Dart tests have actually been RUN or passed.** Never substitute source presence or a syntax-bracket check for execution.
- **[PRODUCT_SPEC.md](PRODUCT_SPEC.md)** approved product scope, **[SCHEDULING_RULES.md](SCHEDULING_RULES.md)** core contract, **[ROADMAP.md](ROADMAP.md)** gates, **[DECISIONS.md](DECISIONS.md)** chronological register through **D-046**, **[WORK_LOG.md](WORK_LOG.md)** iterations, **[CHANGELOG.md](../CHANGELOG.md)** history. See branch-only [Stage 1.4 progress](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/docs/STAGE_1_4_PROGRESS.md).
- **Decision precedence:** D-036 supersedes D-031's previous 0-minute break allowance, but D-031 25/5 warning remains. D-037 approves P-01–P-06 and within-one-session warning scope, with zero summary / zero special-break **count** permitted. **D-038 resolved by D-044**; engine now accepts positive shorter/equal replacements, with D-031 warnings. D-045 records unexecuted source/tests. **D-039** was the prior Step 1.4 gate; **D-040** subsequently authorizes Step 1.4 only; **D-041** prohibits GitHub Actions/paid workflow use under budget block; **D-042** documents unexecuted branch code. D-030/D-032/D-034/D-035 are earlier historical/proposal records, NOT permission for zero-minute break slots.
- GitHub is the authoritative store. After every iteration, update decisions/roadmap/work log/changelog and **THIS HANDOFF**; verify GitHub read-back and link concrete commits. Do not create a duplicate repo.

## Stage gate and exact next action

Stage 0 COMPLETE; Stage 1 **1.1 COMPLETE**, **1.2 DESIGN DOCUMENTED**, **1.3 APPROVED / COMPLETE (D-037)**; **1.4 STARTED / IN PROGRESS ON BRANCH (D-040, D-042), NOT VERIFIED OR COMPLETE**. The user reports GitHub metered-budget blocking (D-041). **Technical source refinement D-043 performed while Actions avoided; SDK and Dart test execution still unavailable.** Stage 2 and later NOT AUTHORIZED. Later Stage 2 per-session responsive Flutter editor/preview; Stage 3 offline presets; Stage 4 monotonic timer/audio; Stage 5 device QA/installability; Stage 6 release.

**Immediate next response in a new chat:** D-044 explicitly APPROVED any positive special replacement duration (e.g. normal 5m, special 3m); do not ask for D-038 again. **Continue only authorized Step 1.4** without re-requesting start. Dart tests still need a suitable SDK, but SDK is absent and container DNS cannot download it. No GitHub Actions/paid runner due reported budget block. Stage 2 remains NOT AUTHORIZED. Read [branch progress](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/docs/STAGE_1_4_PROGRESS.md), inspect [Dart engine source](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/lib/src/scheduling.dart), [tests](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/test/scheduling_test.dart), and [smoke script](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/tool/smoke.dart). **Step 1.4 is NOT complete:** actual Dart runtime/analyzer/test execution is required; Dart/Flutter SDK was unavailable here. Avoid Github Actions or any charged resources due user's exhausted budget. Work can continue only within Stage 1 Step 1.4; **do not start Stage 2 or other work without approval**. D-038 is resolved by D-044; any positive special duration is approved. Do not interpret this as allowing 0m breaks. Keep this main handoff up-to-date with branch progress; merge only after verified and appropriately authorized. Logo will be supplied by user later.
