# Atelier Time — Stage 2 session editor and preview: implementation plan

> **2026-10-23 PAUSE — D-061:** Stage 2 Flutter implementation was started before the full cross-platform development/build pipeline was validated. Stop further Flutter-specific source work, UI/widget test expansion and platform packaging pending [cross-platform feasibility re-assessment](PLATFORM_REASSESSMENT_2026_10_23.md) and a revised approved platform decision. Draft PR #2 remains open but UNVERIFIED; no automatic merges. Tested Dart scheduling and form-adapter source is preserved. No assumption that a PWA can fulfill locked-screen audio or native installer requirements.


**Started for preparation:** 2026-10-23, following the user's explicit instruction **“finish documentation and continue.”**  
**Stage 1 core status:** **D-052 COMPLETE/VERIFIED ON TESTED BRANCH**; Android Termux Dart 3.13.7 screenshot: **35/35 `dart test` pass**. Earlier `dart analyze` **No issues found!** on the 34-test version; the 35th test was a test-only addition.  
**Source integration:** [Stage 1 PR #1](https://github.com/AviY99/Atelier-Time/pull/1) proposes the tested core for `main`; **not merged**.  
**Stage 2 working branch:** [stage-2-session-editor](https://github.com/AviY99/Atelier-Time/tree/stage-2-session-editor), branched from the tested Dart core, separate from `main`.  
**Status:** EARLY DESIGN + CODING, not complete; no Flutter/Android SDK or build/test runtime available in assistant container, and user presently has **Dart only** on Termux. No GitHub Actions or paid services.

## Scope: per-session rules and schedule preview ONLY

**In scope:** a responsive Flutter input screen in a temporary neutral theme, using the already-tested Dart engine through a local package dependency; editable named session, positive full duration, reserved summary, arbitrary ordered opening groups with editable positive count and duration, nominal pose duration, configurable positive normal and special breaks, optional number of special replacements, automatic/manual replacement strategy and cycle order. Show all phases with accurate start, end, length, pose index, kind, break ordinal and warnings. Permit last-two-pose remainder/even/custom 1-minute slider resolution when feasible, requiring explicit safety acknowledgment separate from approval. Any input edit invalidates approval and regenerates preview.

**Out of scope / later gates:** named preset persistence or reactivation (Stage 3), live countdown/audio/notifications/background timing (Stage 4), production visuals/brand/logo (user supplies image later), installers/APK and cross-device testing (Stage 5), any cloud account/backend or paid GitHub Actions. An in-memory approval indicator is **not** saving or activating a preset.

## Interaction contract

1. **Edit inputs** — every numeric example value is editable per session; no immutable 25m/5m/15m restrictions except user-approved positive durations. `0` full session and actual break are blocking errors, never safety warnings (D-036). Zero optional special *count* and zero reserved summary are legal with positive remaining work (D-037).
2. **Opening groups** — add, modify, remove and reorder conceptually, with group count and pose duration positive, and no silent corrections.
3. **Cycle/placement** — choose pose-first/break-first; automatic deterministic midpoint/even fraction or explicitly entered break ordinals; changing any rule invalidates approval.
4. **Preview** — show chronological offsets, duration, kind and totals; clear blocking issues before approval. A shortened final pose is unresolved until one of the three explicit choices is selected. If split unavailable, disable infeasible alternatives rather than inventing phases.
5. **25/5 warning** — valid breaks shorter than 5m are legal and don't reset cumulative posing; warn when posing exceeds 25m without >=5m rest. Separate checkbox must acknowledge warnings before approval (D-031). A positive special break shorter/equal/longer than normal is allowed (D-044).
6. **Approval** — final explicit action approves the exact generated `ScheduleCandidate` into in-memory `ApprovedSession`; editing anything clears acknowledgment and approval. No Save/Activate button until Stage 3.

## Proposed first responsive layout (provisional, not a branding decision)

- **Phone:** one scrollable column with header, editable sections (Session, Opening poses, Repeating rhythm, Special breaks), ending options, warning/error summary and preview phase list; prominent approval action after complete preview.
- **Tablet/desktop:** two-pane layout (inputs on left, live preview on right), without requiring native OS controls.
- **Visuals:** temporary Material 3 defaults, accessible text and large touch targets. Final colors/logo/brand not selected. User retains review of visual style before any design is frozen.

## Deliverables & acceptance

- A nested Flutter prototype package under `apps/atelier_time_flutter/` which imports `atelier_time_core` by relative `path: ../..`, preserving the pure-Dart core and its 35 passing tests.
- Model-backed UI preview, input validation and explicit last-pose choice / warning confirmation.
- Stage 2 T19–T23 acceptance checklist (editor and approval, full preview, ruler, cancel/edited invalidation, touch/accessibility).
- When a Flutter SDK/build environment becomes available, generate required Android/iOS/macOS/Windows platform projects, run `flutter analyze` and Flutter/widget tests **without GitHub Actions**. Until then, status remains **source drafted, NOT FLUTTER COMPILED/TESTED**.

## Approval gate

The user's instruction to “finish documentation and continue” is interpreted as permission to **start the next Stage 2 editor/preview work**, not to skip the feature-review process, automatically merge PR #1, use paid workflows or begin Stages 3–6. The temporary design is subject to review. Record further approvals explicitly. Preserve all Stage 1 examples and safety rules.

**Immediate task:** author initial editor/preview source on the isolated Stage 2 branch, update this plan, work log and [HANDOFF.md](HANDOFF.md), and accurately report missing Flutter SDK rather than inventing builds.


## 2026-10-23 — Second source increment / review checkpoint (D-055)

**Implementation branch:** [stage-2-session-editor](https://github.com/AviY99/Atelier-Time/tree/stage-2-session-editor), [draft PR #2](https://github.com/AviY99/Atelier-Time/pull/2). The nested Flutter editor is still **source-only**.

### Added and revised

- Stable widget keys for session/regular-break fields, opening group fields, selected final-pose mode and slider, warning acknowledgment and approval.
- Distinguish malformed text inputs from engine validation errors; disallow approval of either and hide any partial invalid timeline.
- Keep mobile edit and preview columns mounted together in one scrollable page so the latest errors and ending controls are reachable. Wide-layout separate columns remain.
- Added `flutter_test` SDK dependency and **eight draft widget tests** in [session_editor_widget_test.dart](https://github.com/AviY99/Atelier-Time/blob/stage-2-session-editor/apps/atelier_time_flutter/test/session_editor_widget_test.dart): initial invalid/unresolved approval state; malformed/zero inputs; exact Example B break/summary boundaries; add/change/remove opening group; remainder and custom/even ending, editing invalidation; 25/5 warning acknowledgment; two-pane layout.
- Verified by source inspection against the actual Dart core API; no changes made to the tested pure Dart core.

### Verification / gate

The assistant runtime has no Flutter SDK; user's Termux installs only the pure **Dart** SDK. `flutter pub get`, `flutter analyze`, `flutter test` and native-app runs have **NOT** been executed. **No widget tests have passed yet**. Static text structural checks are not Flutter compilation.

Next Step 2 QA: when a real Flutter-capable environment is available, run the three commands from `apps/atelier_time_flutter/` and correct resulting compile/test problems. Then have the user review visual direction. Do not initiate GitHub Actions/paid builds, auto-merge PRs, or work on Stages 3+.


## 2026-10-23 — Third Stage 2 increment: tested-core-compatible pure Dart form adapter (D-056)

**Scope:** Continue the already-authorized Stage 2 editor/preview work without Flutter execution, paid GitHub Actions, PR merges or later stages.

**Source changes on `stage-2-session-editor`:**
- Created [lib/src/editor_draft.dart](https://github.com/AviY99/Atelier-Time/blob/stage-2-session-editor/lib/src/editor_draft.dart), a Flutter-free immutable text-to-`SessionRules` adapter, and exported it through `lib/atelier_time.dart`.
- Editor `_readRules` delegates to `SessionEditorDraft.toRules()`. User-entered whole-minute fields, opening groups and cycle choices are preserved; malformed values throw `FormatException` and core scheduling constraints still return blocking validation errors. The adapter does not invent defaults or approval.
- An inactive manual ordinal field does not block an **N=0** schedule, even when the manual input contains nonnumeric old text; active manual ordinals are parsed explicitly and then checked by the scheduler.
- Added [11 pure Dart test definitions](https://github.com/AviY99/Atelier-Time/blob/stage-2-session-editor/test/editor_draft_test.dart): exact Example B, custom values, D-036 zero-duration hard failures, malformed text, opening group order, active/manual/inactive ordinals, and D-044 3m special replacement warning.

**Verification:** **11 new pure Dart tests WRITTEN BUT UNEXECUTED**. They are runnable on the user's existing Android Termux Dart 3.13.7 without Flutter and without any GitHub Actions charges. Root Stage 1 scheduling engine remains unchanged and previously passed 35/35; eight Flutter widget tests remain written/unexecuted. Source read-back and basic delimiter check are not compilation.

**Next action:** User may (when convenient) fetch the Stage 2 branch into Termux and execute `dart test test/editor_draft_test.dart`, then report any errors for correction. Stage 2 UI compilation and widget tests still require a Flutter SDK. Do not mark Stage 2 complete or begin presets/timer.

## 2026-10-23 — Stage 2 clean Dart test milestone (D-059)

The user created a separate Git worktree `~/Atelier-Time-Stage2`; screenshots show HEAD `3bb2bad`, exactly the remote `stage-2-session-editor` tip per GitHub compare, and **`+11 All tests passed!`** for `dart test test/editor_draft_test.dart`. The earlier mixed-index test-run attribution is resolved. The Flutter-independent editor adapter is now **verified by 11 passing tests** on this exact branch commit, without reinstalling Dart or using GitHub Actions. **No full Stage 2 `dart analyze && dart test` result and no Flutter widget execution yet**. User's earlier dirty `~/Atelier-Time` worktree is preserved intentionally. Stage 2 remains in progress.


## 2026-10-23 — Analyzer-scope blocker, fix committed for Termux (D-060)

The user supplied a very long error screenshot after `dart analyze && dart test` from the clean Stage 2 Android Termux worktree. With only Dart installed, root analysis erroneously traversed the nested Flutter package and hit unavailable `package:flutter`/`package:flutter_test` SDK imports. The chained `dart test` did not run.

**Stage 2 branch changes:** root `analysis_options.yaml` now excludes the separate Flutter subpackage `apps/atelier_time_flutter/**`; the nested Flutter app also has its own `analysis_options.yaml` to allow full Flutter analysis when the Flutter SDK is available. This is a toolchain separation, not a hidden runtime-code change or a claim of Flutter correctness. See official [Dart analysis configuration](https://dart.dev/tools/analysis).

**Next check:** User can update the existing `~/Atelier-Time-Stage2` worktree and run `dart analyze && dart test` using bare Dart. The expected combined suite has 46 test definitions, but this specific updated commit has **NOT** been analyzed/tested on device yet. The independent Flutter widget suite remains UNEXECUTED and requires Flutter SDK separately. No Actions or paid services.
