# ATELIER TIME — CURRENT CROSS-CHAT PROJECT HANDOFF (v2.7)

**Updated:** 2026-10-23  
**Repository:** https://github.com/AviY99/Atelier-Time (existing public repo; default `main`)  
**Technology APPROVED:** **Flutter + Dart**, **ONE shared application codebase** for first release on **Android phones/tablets, Windows computers, iOS/iPadOS and macOS**. Different install artifacts per OS are acceptable; native-looking menus/graphics are OPTIONAL. No web or Linux release required initially.  
**CURRENT AUTHORITATIVE STATUS — 2026-10-23 (D-052–D-060):** Stage 0 and Stage 1 Steps 1.1–1.4 COMPLETE/VERIFIED (`stage-1-4-scheduling-core`, **35/35 core Dart tests PASS** on Android Termux Dart 3.13.7; earlier analyzer/smoke PASS). Stage 2 editor/preview work authorized (D-053), source on [stage-2-session-editor](https://github.com/AviY99/Atelier-Time/tree/stage-2-session-editor) and [draft PR #2](https://github.com/AviY99/Atelier-Time/pull/2) open/unmerged. [Stage 1 PR #1](https://github.com/AviY99/Atelier-Time/pull/1) open/unmerged. User's clean `~/Atelier-Time-Stage2` separate worktree at **`3bb2bad`** matched remote branch tip and showed **11/11 adapter Dart tests PASS** (D-059). **Latest D-060:** user tried combined `dart analyze && dart test`; root analyzer flooded Termux with errors, consistent with scanning nested Flutter UI (`apps/atelier_time_flutter/`) requiring Flutter SDK whereas Termux only has Dart. Since analyzer failed, chained full `dart test` did NOT run. Committed **root `analysis_options.yaml`** excluding nested Flutter package from pure-Dart analysis and an **independent nested Flutter `analysis_options.yaml`** retaining full Flutter verification for a Flutter-capable environment. **FIX NOT YET RETESTED**; rerun after `git pull --ff-only`. Earlier 35/35+11/11 successes remain valid, no 46-test combined pass yet, and **eight Flutter widget test sources remain UNCOMPILED/UNRUN**. Original mixed `~/Atelier-Time` worktree intentionally preserved. No APK, Stage 3 saved presets, Stage 4 live timer/audio, PR merges, GitHub Actions/paid builds or budget changes. D-031/036/037/044 remain authoritative product rules.

## Latest Stage 2 analyzer failure / isolated SDK scopes (D-060, 2026-10-23)

**User message (Hebrew):** “זה רק עשירית מהשגיאות” (this is only a tenth of the errors). Attached extremely narrow tall terminal screenshot of large Dart analyzer diagnostic output after trying `dart analyze && dart test` on the verified `~/Atelier-Time-Stage2` checkout. Do not ask for hundreds more screenshots or say the Dart tests failed. With `&&`, **`dart test` DID NOT run after the nonzero analyzer exit**.

**Root cause indicated by verified repository structure:** Root `pubspec.yaml` defines a **pure Dart** `atelier_time_core` package with `test` dependency. Nested `apps/atelier_time_flutter/pubspec.yaml` is a separate Flutter package needing `flutter: sdk: flutter` and `flutter_test: sdk: flutter`. User's Android Termux has **Dart SDK ONLY**, so analyzer cannot resolve these Flutter SDK dependencies if it traverses nested UI files. The screenshot resolution is too small to verify each particular diagnostic; do not guarantee all errors are Flutter-generated until post-fix analysis.

**Stage 2 code-only fix committed on remote branch:**
- [root analysis_options.yaml](https://github.com/AviY99/Atelier-Time/blob/stage-2-session-editor/analysis_options.yaml) includes `analyzer: exclude: - apps/atelier_time_flutter/**`, so bare-Dart root analysis should cover core/adapter/tests/tools without traversing the nested Flutter application.
- [nested Flutter analysis_options.yaml](https://github.com/AviY99/Atelier-Time/blob/stage-2-session-editor/apps/atelier_time_flutter/analysis_options.yaml) has independent scope (exclude only build/generated cache), keeping UI/widget tests analyzable later with a **real Flutter SDK** from the app directory. This is not an attempt to hide Flutter errors from Flutter analysis.
- **No scheduling engine, adapter or Flutter behavior changed**. Official documentation: https://dart.dev/tools/analysis.

**NO post-fix Dart analyzer/test results yet.** The earlier Stage 1 35/35 and clean Stage 2 adapter 11/11 tests remain separately verified. Eight Flutter widget tests still not executed. The root suite should discover 35+11=**46 tests**, but NEVER claim 46 PASS before user reports exact output. No GitHub Actions/paid services, APK or Stage 3+.

**Next Android Termux command (existing clean worktree):**
```bash
cd ~/Atelier-Time-Stage2
git pull --ff-only
dart analyze && dart test
```
If error persists, ask only for the first **10–20 lines** (or an image showing first error), not the entire flood. Alternative targeted diagnostics are `dart analyze lib` and `dart analyze test` (avoids UI subtree) if the root exclusion behaves unexpectedly; resolve any genuine Dart package errors separately. The nested Flutter UI will still require proper Flutter tooling. Update this handoff after the result.

---

## Latest Stage 2 clean worktree verified (D-059, 2026-10-23)

**User supplied new Termux screenshots:** A separate `~/Atelier-Time-Stage2` folder exists and `git rev-parse --short HEAD` output is **`3bb2bad`**. Running `dart pub get && dart test test/editor_draft_test.dart` in this folder downloaded dependencies successfully and ended **`00:01 +11: All tests passed!`**.

**GitHub cross-check:** `compare_commits` with base `3bb2bad` and head `stage-2-session-editor` returned `status: identical`, `ahead_by: 0`, `behind_by: 0`, full SHA **`3bb2bade992fddab7dc3d7ece31d9aa58231380f`**. This **resolves D-057/D-058**: the 11 independent Stage 2 Dart adapter tests have now truly passed on the correct latest checked-out Stage 2 code, not merely old Stage 1 with staged additions. Original `~/Atelier-Time` mixed/staged worktree remains preserved; **do not reset/clean it**.

**Verification scope:** Stage 1 pure Dart core 35/35 tests passed separately earlier (D-052). Stage 2 parser tests **11/11 screenshot-verified PASS** at exact Stage 2 HEAD. **Full Stage 2 root `dart analyze && dart test` and eight Flutter widget tests have NOT been verified on this revision.** Flutter UI still requires a proper Flutter SDK, and no Flutter platform project/APK/installer exists.

**Budget-free next step:** Using the **already working** clean Stage 2 folder, run:
```bash
cd ~/Atelier-Time-Stage2
dart analyze && dart test
```
The full root Dart test discovery should cover both core scheduling and the editor adapter, but **do not claim combined pass until actual output received**. This does NOT run the nested Flutter UI tests. No GitHub Actions, paid builds, PR merges, or Stage 3/4 work.

---
## Latest Termux diagnosis — D-058: staged Stage 2 files on Stage 1 HEAD (2026-10-23)

**Full second screenshot evidence (read-only user commands):**

```text
## stage-1-4-scheduling-core...origin/stage-1-4-scheduling-core
A  apps/atelier_time_flutter/README.md
A  apps/atelier_time_flutter/lib/main.dart
A  apps/atelier_time_flutter/lib/session_editor_page.dart
A  apps/atelier_time_flutter/pubspec.yaml
A  apps/atelier_time_flutter/test/session_editor_widget_test.dart
M  docs/STAGE_1_4_PROGRESS.md
M  lib/atelier_time.dart
A  lib/src/editor_draft.dart
A  test/editor_draft_test.dart
?? pubspec.lock
git branch --show-current -> stage-1-4-scheduling-core
git rev-parse --short HEAD -> 907f871
git ls-files test/editor_draft_test.dart -> test/editor_draft_test.dart
```

**Meaning:** `git switch --track` failed previously and left Stage 2 contents staged on the Stage 1 checkout. `git ls-files` includes staged additions, so it does NOT mean the test file is committed in Stage 1 HEAD. The prior `+11 All tests passed!` is a genuine local test run using Stage 2 files, but **not validated on a clean Stage 2 Git branch**. Do not tell the user the test failed or that switch succeeded.

**Safest next instruction: create an entirely separate Stage 2 worktree while preserving existing dirty/staged and untracked files**. From Termux:
```bash
git -C ~/Atelier-Time fetch origin stage-2-session-editor
git -C ~/Atelier-Time worktree add -b stage-2-session-editor ~/Atelier-Time-Stage2 FETCH_HEAD
cd ~/Atelier-Time-Stage2
git status --short --branch
git rev-parse --short HEAD
dart pub get && dart test test/editor_draft_test.dart
```
If worktree path/local branch already exists, **STOP and inspect**; never force/delete. The first original `~/Atelier-Time` and its staged files are untouched by this approach. Only after clean checkout succeeds may report a verified Stage 2 commit/test pass. The 8 Flutter widget tests still require Flutter, not available in Termux. No paid GitHub Actions, merge or Stage 3+.

---
## New screenshot evidence and Git checkout issue — D-057 (2026-10-23)

**User terminal screenshot shows:**
- `git pull --ff-only && dart test` on `stage-1-4-scheduling-core`, `+35: All tests passed!` (historical previously verified Stage 1);
- `git fetch origin stage-2-session-editor:refs/remotes/origin/stage-2-session-editor` succeeded, showing remote ref `origin/stage-2-session-editor`;
- `git switch --track origin/stage-2-session-editor` **FAILED**, fatal `cannot set up tracking information; starting point 'origin/stage-2-session-editor' is not a branch`;
- Despite switch failure, `dart pub get && dart test test/editor_draft_test.dart` says `Got dependencies!` then **`00:00 +11: All tests passed!`**.

**Correct reading:** real 11-test suite PASS in the user's local filesystem, but no proof that the Git HEAD/branch changed to Stage 2. Tool lookup confirmed `test/editor_draft_test.dart` is **present on GitHub Stage 2**, **absent from GitHub Stage 1**, so the user's local file state is uncertain; do not assert impossible details or silently assume success/failure of branch migration.

**Safe next step:** Ask user to run only non-mutating Git commands from `~/Atelier-Time`:
```bash
git status --short --branch
git branch --show-current
git rev-parse --short HEAD
git ls-files test/editor_draft_test.dart
```
Based on output, safely establish a local Stage 2 branch using previously fetched commit (e.g., if working tree clean and no local branch exists, `git fetch origin stage-2-session-editor` followed by `git switch -c stage-2-session-editor --no-track FETCH_HEAD`; confirm with `git branch --show-current`, `git rev-parse --short HEAD`). **Do not switch/overwrite untracked files blindly.** Only then attribute a new `dart test test/editor_draft_test.dart` PASS to the verified Stage 2 commit. No Flutter widget tests have run; user does NOT need SDK reinstall or GitHub Actions.

## Latest Stage 2 progress — D-056 plain-Dart editor adapter (2026-10-23)

**User instruction:** “Can you continue?” Continued the existing authorized Stage 2 branch. No change to approved scheduling algorithm or later-stage scope.

**Branch source committed:**
- [lib/src/editor_draft.dart](https://github.com/AviY99/Atelier-Time/blob/stage-2-session-editor/lib/src/editor_draft.dart): new Flutter-independent `SessionEditorDraft` and `OpeningGroupDraft` models, with immutable text input snapshots. They parse whole-minute fields, ordered opening groups, trimmed session name, cycle order, and active manual special-break ordinal text into existing `SessionRules`.
- [lib/atelier_time.dart](https://github.com/AviY99/Atelier-Time/blob/stage-2-session-editor/lib/atelier_time.dart): public export of the adapter alongside the untouched scheduling engine.
- [Flutter editor](https://github.com/AviY99/Atelier-Time/blob/stage-2-session-editor/apps/atelier_time_flutter/lib/session_editor_page.dart): delegates `_readRules()` to `SessionEditorDraft.toRules()`, avoiding duplicate form-parsing logic. `N=0` requested special breaks correctly ignores inactive manual-ordinal text even if stale/invalid; active ordinal parsing remains strict. Zero and negative configured durations reach the scheduler to be reported as hard validation errors, NOT silently corrected.
- [test/editor_draft_test.dart](https://github.com/AviY99/Atelier-Time/blob/stage-2-session-editor/test/editor_draft_test.dart): **11 new pure Dart test SOURCES** for canonical B mapping, custom field values, D-036 zeros, invalid text, opening groups order, manual ordinals, inactive ordinal case, D-044 shorter break and D-031 warning.

**Verification:** GitHub file read-back and a primitive bracket/quote check succeeded, but these do **not constitute Dart compilation**. **The 11 new plain-Dart tests have NOT run.** User has Termux Dart 3.13.7 and can now verify the Stage 2 adapter without installing Flutter. The original 35 tests passed earlier on the Stage 1 branch and remain valid there. EIGHT Flutter widget tests still await a real Flutter SDK and **have NOT run**. No Android APK.

**Budget-free next verification, optional user action:** From the existing Termux `~/Atelier-Time` folder currently on Stage 1 branch, switch once to Stage 2:

```bash
git fetch origin stage-2-session-editor
git switch --track origin/stage-2-session-editor
dart pub get
dart test test/editor_draft_test.dart
```

If the branch is already local, `git switch stage-2-session-editor` instead of repeating `--track`. Ask for terminal result to fix genuine Dart errors. **Do not run `flutter test` or claim Flutter UI validation from this check.** The nested Flutter source still requires a proper Flutter toolchain. No Actions, merges, app deployments or Stage 3–6.

---

## Latest Stage 2 source / tests update — D-055 (2026-10-23)

**User instruction:** “continue” followed earlier authorized Stage 2 work. Continued only existing Stage 2 editor/preview branch; no new policy or release authorization assumed.

**Changes on [stage-2-session-editor](https://github.com/AviY99/Atelier-Time/tree/stage-2-session-editor):**
- [Flutter editor source](https://github.com/AviY99/Atelier-Time/blob/stage-2-session-editor/apps/atelier_time_flutter/lib/session_editor_page.dart) now gives controls stable `ValueKey` identifiers, shows malformed numeric inputs as explicit **blocking input errors**, hides partial/unapprovable timeline phases on engine validation errors, and keeps the full phone editor/preview mounted in one scrollable page. Wide screen remains two-pane.
- `apps/atelier_time_flutter/pubspec.yaml` now has a `flutter_test` SDK dev dependency.
- [Eight Flutter widget tests](https://github.com/AviY99/Atelier-Time/blob/stage-2-session-editor/apps/atelier_time_flutter/test/session_editor_widget_test.dart) **WRITTEN, NOT RUN**: T19 0/non-numeric and 0 normal break validation; T20 exact Example B long break 01:25–01:40 and summary 02:45–03:00; T19 editable/removable opening groups; T21 ending remainder/even/custom slider and revision-invalidated approval; T22 qualifying-rest warning acknowledgement and reset; T23 wide two-column layout.
- Widget 25/5 warning fixture uses a **positive 51-minute complete session, 0 reserved summary**, 25m pose + 1m normal break + 25m pose; the 0 summary is legal under D-037, and **0 configured breaks or complete sessions remain invalid**.
- Pure Dart scheduling core untouched; its **35/35 screenshot-verified passing tests** (D-052) are unaffected. No test rerun needed until source/core changes require it.

**Verification limit:** Assistant runtime has no Flutter SDK (`flutter` and `dart` absent); user's Android Termux has **bare Dart 3.13.7**, which is NOT enough to compile Flutter widgets. No `flutter pub get`, `flutter analyze`, widget tests, APK, emulator execution or target OS builds have run. Treat structural source review as **not compilation**. Do not claim eight widgets passed. **Stage 2 IN PROGRESS, NOT COMPLETE.** No GitHub Actions/paid resources, no merges, no Stage 3+.

**Safe next work:** Use [STAGE_2_PLAN.md](STAGE_2_PLAN.md) and [Stage 2 branch README](https://github.com/AviY99/Atelier-Time/blob/stage-2-session-editor/apps/atelier_time_flutter/README.md); obtain a Flutter-capable free test environment, run `cd apps/atelier_time_flutter && flutter pub get && flutter analyze && flutter test`, fix real errors, then discuss provisional visual style with the user before finalizing. Never re-request already authorized Stage 2 initiation or ask them to repeat all 35 passing Dart core tests.

---

## Stage 2 continuation — current active work (D-053/D-054, 2026-10-23)

**The user explicitly directed:** “What's going on u dont respond to actions ??? I want you to finish documentation and continue”. This follows completed D-052 Stage 1 and authorizes beginning Stage 2 source/plan now (D-053). Do not ask again to start Stage 2 design/source. Final visual styling and branding have NOT been chosen; use provisional neutral appearance and request a review when available.

**GitHub integration and isolation:**
- **Stage 1 verified Dart core**: [PR #1](https://github.com/AviY99/Atelier-Time/pull/1) proposes adding the tested package to `main`; **open, not merged**, no Actions invoked. The main branch contains authoritative documentation and separate commits. Avoid automatic merges or bypassing the review gate.
- **Stage 2 draft Flutter editor**: [branch `stage-2-session-editor`](https://github.com/AviY99/Atelier-Time/tree/stage-2-session-editor) based on `stage-1-4-scheduling-core`, [draft PR #2](https://github.com/AviY99/Atelier-Time/pull/2) targets the core branch. This stacking preserves the 35 tested Dart core regressions without needing an immediate merge.
- **Requirements/design contract**: [STAGE_2_PLAN.md](STAGE_2_PLAN.md) on `main`. Initial nested Flutter app code: `apps/atelier_time_flutter/pubspec.yaml`, `lib/main.dart`, `lib/session_editor_page.dart`, and README on Stage 2 branch. Flutter's path dependency points `../..` to the tested Dart core package.

**Stage 2 source implemented (NOT run):** responsive one-column phone / two-pane wide input and preview screen, per-session name and total/summary, positive pose/regular/special break lengths and special break count, editable ordered opening groups with add/remove/reorder, pose-first/break-first, automatic/manual special break ordinals, exact chronological preview phases and totals, errors distinct from 25/5 advisory warnings, remainder/even/custom final-pose choice with 1m slider resolution, separate safety acknowledgement, explicit in-memory approved snapshot invalidated on any edit. This prototype **cannot save, activate or time** sessions; those belong to Stage 3/4. Material 3 colors/layout are provisional, not user-approved branding.

**Verification:** Connector read-back verified Flutter source files were committed; basic lexical delimiters were balanced in Dart UI files. **NO Flutter SDK in assistant environment, NO `flutter pub get`, `flutter analyze`, widget tests, emulator run, platform scaffolding, APK or installed app**. Android Termux has **Dart only**, which does NOT run the Flutter UI; do not ask user to rerun 35 Dart tests or install unrelated tools without a specific next testing task. Stage 2 T19–T23 tests are **PLANNED, NOT RUN**.

**Next safe work within authorized Stage 2:** review UI source/acceptance checklist; add appropriate widget-test source if useful; obtain Flutter-capable testing environment without paid GitHub Actions; run Flutter analyzer/widget tests and correct actual issues; discuss appearance/logo when ready. **Do NOT begin Stages 3–6 or auto-merge PRs** without distinct permission. Update [docs/HANDOFF.md](HANDOFF.md) after each authorized iteration.

---

## Stage 1 Step 1.4 — COMPLETED/VERIFIED on development branch (D-052)

**Authoritative latest milestone:** User's Android Termux screenshot (2026-10-23) includes:

```text
~/Atelier-Time $ git pull --ff-only && dart test
...
8cf7a24..907f871  stage-1-4-scheduling-core -> origin/stage-1-4-scheduling-core
Fast-forward
  docs/STAGE_1_4_PROGRESS.md
  test/scheduling_test.dart
...
00:00 +35: All tests passed!
```

- This is **screenshot-confirmed 35/35 passing Dart unit tests on Android ARM64 Dart 3.13.7** on the branch after adding required T09 valid custom first=1m / last=34m edge case. Earlier screenshot-confirmed `dart analyze` **No issues found!** refers to the immediately preceding 34-test version, before test-only changes; earlier smoke test also passed.
- **Minimum Stage 1 test gate fulfilled:** T01–T12, T15–T18 map to the 35 passing automated cases, including canonical A/B exact 3h sessions, break replacement, three final-pose choices and both 1m split endpoints, positivity checks, 25/5 advisory warning+acknowledgment, deterministic contiguous schedules. Wider T13/T14 edge-matrix combinations **remain only partially tested** and belong to future QA; do not claim exhaustive inputs, other operating systems or Flutter application verification.
- **Stage 1 Step 1.4 COMPLETE ON ISOLATED BRANCH** [`stage-1-4-scheduling-core`](https://github.com/AviY99/Atelier-Time/tree/stage-1-4-scheduling-core); [engine](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/lib/src/scheduling.dart), [35 tests](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/test/scheduling_test.dart), [branch verification log](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/docs/STAGE_1_4_PROGRESS.md). **No merge to `main`** yet. Stage 2 editor/preview work was subsequently authorized by D-053 and started as source-only prototype (D-054); Stage 3 presets, Stage 4 timer/audio and builds remain separate.
- **HISTORICAL GATE (D-052, since superseded by D-053):** Stage 1 stopped after verification awaiting explicit next instruction. The user has since told us to “finish documentation and continue,” authorizing Stage 2 early editor/preview work, not a merge or paid Actions. Do not ask user to reinstall Dart or rerun the already proven 35 core tests.

**Historical notes below are preserved for change history. Any “NOT RUN,” “IN PROGRESS,” “T09 pending,” or “SDK unavailable” in earlier dated sections refers to earlier iterations only and is superseded by this current result.**



## Historical iteration D-046 — SDK blocker diagnosis and local test helpers

**User question:** “What to do to solve ur failure? Can you continue?” YES: continued **only** authorized Step 1.4 source hardening and test preparation, not Stage 2 or billing-dependent Actions.

**Environment/root cause:** In assistant Linux container, `dart` and `flutter` are absent and none found in local installations. Command-line DNS for `storage.googleapis.com`, `github.com`, `dart.dev` fails; `curl` cannot fetch SDK binaries. This is a **missing test toolchain / network access problem**, not a confirmed Dart engine compile/test failure.

**Completed on isolated branch** [stage-1-4-scheduling-core](https://github.com/AviY99/Atelier-Time/tree/stage-1-4-scheduling-core):
- Engine [lib/src/scheduling.dart](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/lib/src/scheduling.dart): made `ScheduleCandidate._` constructor private to its Dart library; callers cannot construct an approvable fake schedule. `ApprovedSession` now contains immutable full `acknowledgedWarnings` (including actual elapsed time) and keeps `acknowledgedWarningCodes` for UI access.
- Expanded [test/scheduling_test.dart](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/test/scheduling_test.dart) with **unexecuted** test checking preserved warning timestamp and immutable lists.
- Added [tool/verify.ps1](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/tool/verify.ps1) (Windows PowerShell) and [tool/verify.sh](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/tool/verify.sh) (macOS/Linux Bash) plus `.gitignore`. These scripts run `dart --version`, `dart tool/smoke.dart`, `dart pub get`, `dart analyze`, `dart test`, and write `atelier-time-verification.log`; they do NOT use GitHub Actions.
- **NO source was merged into `main`; NO Dart test/analyzer/compiler/smoke execution took place** because SDK is missing. No paid runners, CI, builds, or budget changes. **Step 1.4 remains IN PROGRESS**.

**Minimal user-assisted unblock when necessary:** Install **ONLY the Dart SDK** from https://dart.dev/get-dart on a computer (Flutter is NOT needed to test this pure Dart scheduling core); download/clone the `stage-1-4-scheduling-core` branch; run `powershell -ExecutionPolicy Bypass -File tool\verify.ps1` on Windows from repo root, or `bash tool/verify.sh` on macOS/Linux; share `atelier-time-verification.log` for fixes. Installing Dart on the user's machine **does not** install it inside the assistant's container. No passwords or secrets should be shared.

**Next code gate:** Fix actual errors revealed by Dart execution before declaring Step 1.4 complete. Continue this step without another authorization; **Stage 2 and other activities remain blocked**.

## Historical iteration D-051 — T09 35th regression added (subsequently PASSED D-052)

- The user supplied screenshots showing *prior 34* unit tests PASS and Dart analyzer 0 issues on Android Termux; those results remain verified.
- The Stage 1 Step 1.4 acceptance check required both positive custom-pose ruler extremes. Test 34m+1m was already passing, but the inverse **1m+34m** endpoint was missing.
- **New 35th test on development branch** [commit `4b2f5d9`](https://github.com/AviY99/Atelier-Time/commit/4b2f5d9fdd8dabc6dea8dcc0204c5062c7b6916a) asserts Example A 125–126m first pose, 126–131m rest, 131–165m final pose, 165–180m reserved summary; invalid 0m case remains blocked and 25/5 warning still requires acknowledgment.
- The **35th test has NOT run**. One budget-free Termux rerun is needed: `git pull --ff-only && dart test`. Do not claim 35 PASS until evidence received. No GitHub Actions, main merge, UI/Android APK or Stage 2 work.

## Historical verified checkpoint — Android Dart 34/34 tests PASS (D-050, superseded by 35/35 D-052)

**User submitted three actual Termux screenshots** after running `dart pub get && dart analyze && dart test` on **Dart SDK 3.13.7 (Android ARM64)** in the `~/Atelier-Time` working tree on `stage-1-4-scheduling-core`. Visible final output:

```text
Changed 49 dependencies!
Analyzing Atelier-Time...
No issues found!
Built test:test.
00:01 +34: All tests passed!
```

**Test evidence is SCREENSHOT-VERIFIED:** Dart analyzer reports zero issues and all 34 written unit tests have passed. Earlier smoke script also reported `Scheduling smoke checks succeeded.` following the D-047 integer-type fix. This supersedes earlier “not run,” “retest pending,” or “user-reported only” statuses appearing as historical notes below or in older docs. The screenshots do not display the exact local checked-out commit SHA, so do not invent it; no non-Android Dart or Flutter UI/build verification has been performed.

**Current Stage 1 acceptance audit:** The original 34 passing tests cover canonical Example A/B, three final-pose choices, deterministic/contiguous timing, replacement breaks, hard zero rules and 25/5 warnings. Required T09 missing 1m+34m counterpart **was added as the 35th test** in branch commit `4b2f5d9fdd8dabc6dea8dcc0204c5062c7b6916a` (D-051); existing 34m+1m and 0m rejection were already covered. **This 35th test is UNEXECUTED**, so screenshot proof is for prior 34 only. User can run `git pull --ff-only && dart test` on Android Termux. T13/T14 wider edge matrix remains partial; defer broader QA decisions without inventing new requirements. No Stage 2 authority has been granted. No GitHub Actions/paid work, no merge to `main`. **Stage 1 Step 1.4 = final QA review, not completed.**

### Immediate next task after this handoff

The **35th T09 endpoint test is already committed** on `stage-1-4-scheduling-core`. Ask the user to run `git pull --ff-only && dart test` in their existing Android Termux `~/Atelier-Time` directory, then inspect/pass/fix new evidence. Do not ask for Dart reinstallation. Keep main and all later Flutter/UI stages untouched until separately approved.

## Historical iteration D-047 — initial compile failed and was fixed

**User has succeeded installing Dart on Android via Termux**: Dart SDK 3.13.7 stable, `android_arm64`. User cloned branch `stage-1-4-scheduling-core` into Termux's `~/Atelier-Time` and ran **`dart tool/smoke.dart`**.

**Real observed test outcome on old branch:** **Dart COMPILE FAILED** at `lib/src/scheduling.dart:621:28` with `The argument type 'num' can't be assigned to the parameter type 'int?'`. The warning's `offsetSeconds:` argument was `crossingOffset`, which had type `num` because `math.max(0, _maxUnbrokenPoseSeconds - previousAccumulated)` was not inferred as int in user's Dart 3.13.7 compiler.

**Fix already committed to branch**: [commit `0dedd95756b6d3984b2e16548a58599179fbf7ff`](https://github.com/AviY99/Atelier-Time/commit/0dedd95756b6d3984b2e16548a58599179fbf7ff) replaces `math.max` with integer-only condition `secondsUntilThreshold > 0 ? secondsUntilThreshold : 0`, preserving exact warning threshold. D-047 logged.

**CRITICAL:** The code **has NOT been retested since this fix**. No smoke pass, no `dart analyze` or `dart test` pass yet. Do not claim compile/test success. The older failure is now a real engineering finding, not just missing SDK.

**Next user instruction:** They are **already in `~/Atelier-Time` on Termux**, so no reinstall or re-clone required. Run:
```bash
git pull --ff-only && dart tool/smoke.dart
```
If smoke succeeds, run `dart pub get && dart analyze && dart test`, share output/error; if any command fails, continue source corrections within already-authorized Step 1.4. No Actions/paid runners, no main merge, no Stage 2.

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

## Approved D-044 — positive shorter/equal special replacements (2026-10-22)

The user explicitly approved: **“allow any positive special break duration, even shorter than or equal to the normal break, with the already approved 25/5 rest warnings. Zero-minute breaks remain invalid.”**

- **D-044 resolves formerly OPEN D-038.** A selected special break of **L>0** can be shorter, equal, or longer than positive ordinary B. It replaces B (it does NOT add to B). D-036 prohibits 0-minute sessions/configured breaks; D-031 requires the 25/5 rest warning and affirmative acknowledgement when applicable.
- **Branch code:** [scheduling engine](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/lib/src/scheduling.dart) accepts all positive special lengths and generalizes exact DP placement for negative, zero, and positive `L-B`. The old `pendingDecision` rejection is gone.
- **Tests written NOT RUN:** [regression suite](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/test/scheduling_test.dart) and [smoke script](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/tool/smoke.dart) cover normal=5m with special=1m/3m/5m, multiple shorter replacements, warnings and explicit approval. A/B canonical cases unchanged.
- **Independent Python design checks** matched brute force across **25,000 virtual-schedule random cases** and **4,000 additional real-schedule exhaustive cases**, including `L<B`, `L=B` and `L>B`. These do **NOT verify Dart code**.
- **No Dart/Flutter SDK, no executable Dart tests, analyzer, smoke execution or build.** SDK download DNS unavailable. GitHub Actions/paid runners intentionally not run owing to user-reported account budget block. Branch unmerged. **Step 1.4 remains IN PROGRESS; Stage 2 NOT authorized.**

## Historical Step 1.4 start — coding branch, GitHub budget protection

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
- **[TEST_PLAN.md](TEST_PLAN.md)** lists canonical T01–T18 and P-T/R-B edge cases; **current D-052 evidence: 35/35 Step 1.4 Dart unit tests PASS on Android ARM64** after the added T09 endpoint, prior analyzer no issues, prior smoke PASS. T13/T14 broader edge cases remain a future QA backlog.
- **[PRODUCT_SPEC.md](PRODUCT_SPEC.md)** approved product scope, **[SCHEDULING_RULES.md](SCHEDULING_RULES.md)** core contract, **[ROADMAP.md](ROADMAP.md)** gates, **[DECISIONS.md](DECISIONS.md)** chronological register through **D-060**, **[WORK_LOG.md](WORK_LOG.md)** iterations, **[CHANGELOG.md](../CHANGELOG.md)** history. See branch-only [Stage 1.4 progress](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/docs/STAGE_1_4_PROGRESS.md).
- **Decision precedence:** D-036 supersedes D-031's previous 0-minute break allowance, but D-031 25/5 warning remains. D-037 approves P-01–P-06 and within-one-session warning scope, with zero summary / zero special-break **count** permitted. **D-038 resolved by D-044**; engine now accepts positive shorter/equal replacements, with D-031 warnings. D-045 records unexecuted source/tests. **D-039** was the prior Step 1.4 gate; **D-040** subsequently authorizes Step 1.4 only; **D-041** prohibits GitHub Actions/paid workflow use under budget block; **D-042** documents unexecuted branch code. D-030/D-032/D-034/D-035 are earlier historical/proposal records, NOT permission for zero-minute break slots.
- GitHub is the authoritative store. After every iteration, update decisions/roadmap/work log/changelog and **THIS HANDOFF**; verify GitHub read-back and link concrete commits. Do not create a duplicate repo.

## Stage gate and immediate next action — CURRENT 2026-10-23

**Stage 0 COMPLETE. Stage 1 Step 1.1 platform choice COMPLETE; Step 1.2 conceptual data model DOCUMENTED; Step 1.3 policies APPROVED; Step 1.4 pure-Dart core and 35 required tests VERIFIED/COMPLETE (D-052).**

**Stage 2 STARTED (D-053/D-054), NOT COMPLETE.** The user directly instructed finishing documentation and continuing. Stage 1 [PR #1](https://github.com/AviY99/Atelier-Time/pull/1) and Stage 2 [draft PR #2](https://github.com/AviY99/Atelier-Time/pull/2) are OPEN, not merged. Provisional Flutter editor source is on `stage-2-session-editor` and has never been compiled or run. No release/APK, no Flutter platform scaffolding, no paid Actions, no Stage 3–6 work. D-031, D-036, D-037, D-044 remain product invariants.

**Immediate next action in a future chat:** User's clean `~/Atelier-Time-Stage2` worktree at `3bb2bad` correctly ran **11/11 adapter tests PASS**, Stage 1 has **35/35 core PASS** (D-052/D-059). Then combined `dart analyze && dart test` FLOODED errors on Termux; most likely bare-Dart analyzer traversed nested Flutter package requiring Flutter SDK, so chained `dart test` **did not run**. D-060 committed root analyzer exclusion of `apps/atelier_time_flutter/**` plus independent nested Flutter analysis options to retain full later Flutter checking. Fix NOT YET TESTED. Ask user to run `cd ~/Atelier-Time-Stage2 && git pull --ff-only && dart analyze && dart test`; if still failing obtain only first errors. Do not request all screenshots, delete local files, reinstall Dart, merge PRs, use GitHub Actions or start Stage 3+; Flutter UI and eight widget tests are still UNRUN. Document actual result and update HANDOFF.
