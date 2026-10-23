# Atelier Time

> **2026-10-23 — D-061 PLATFORM DECISION REOPENED:** The user requires cross-platform compatibility **from development and automated testing through native builds/signing, installation, and live operation**, not just a shared Flutter runtime on four targets. The original Flutter recommendation did NOT verify Android Termux as a supported developer host or establish viable Apple/Windows/macOS builders. **Pause further Stage 2 Flutter-specific coding and PR merges.** See [full documented feasibility assessment](docs/PLATFORM_REASSESSMENT_2026_10_23.md) and [updated handoff](docs/HANDOFF.md). No PWA/native-wrapper replacement has been approved. Previously verified 35/35 core Dart and 11/11 Stage 2 form-adapter test evidence is preserved; eight Flutter widget tests have never run. No GitHub Actions/paid builds.


A planned offline-capable, installable posing/break timer for live-model **drawing and sculpting** ateliers.

**Project status (2026-10-23, D-052–D-054):** **Stage 1 Step 1.4 COMPLETE on tested branch** — Android Termux Dart 3.13.7 screenshot verified **35/35 Dart unit tests PASS**; prior smoke test and analyzer passed. [Stage 1 integration PR #1](https://github.com/AviY99/Atelier-Time/pull/1) is OPEN, **not merged**. At the user's instruction to **“finish documentation and continue”**, **Stage 2 session editor/preview started**: [implementation plan](docs/STAGE_2_PLAN.md), [uncompiled Flutter prototype branch](https://github.com/AviY99/Atelier-Time/tree/stage-2-session-editor), [draft PR #2](https://github.com/AviY99/Atelier-Time/pull/2). **Flutter source is NOT tested/built**, no APK, saved presets, live timer/audio, GitHub Actions or paid builds. No later stages authorized.

Atelier Time lets an organizer configure pose and break rules, generate and approve a chronological schedule, save reusable presets, and run a timer with three distinct sound cues. If the last pose would be incomplete, schedule review offers: (1) keep the remaining short final pose; (2) split the time of the last two poses as evenly as possible; or (3) adjust their split with a ruler/slider.

## Documentation

- [Product requirements](docs/PRODUCT_SPEC.md)
- [Schedule model and algorithms](docs/SCHEDULING_RULES.md)
- [Complete 3-hour example schedules](docs/EXAMPLES.md)
- [Delivery stages and approval gates](docs/ROADMAP.md)
- [Stage 2 editor/preview development plan](docs/STAGE_2_PLAN.md)
- [Decision register](docs/DECISIONS.md)
- [Design and technical approach](docs/ARCHITECTURE.md)
- [Step 1.2 — Dart data-model design](docs/DATA_MODEL.md)
- [Step 1.3 — approved scheduling placement and edge-case policies](docs/SCHEDULING_EDGE_POLICY.md)
- [Step 1.3 — approved decision record](docs/STEP_1_3_REVIEW.md)
- [Approved framework and platform decision](docs/TECH_DECISION_001_PLATFORM.md)
- [Acceptance and test plan](docs/TEST_PLAN.md)
- [Iteration record](docs/WORK_LOG.md)
- [Current handoff to a new chat](docs/HANDOFF.md)
- [Change history](CHANGELOG.md)

## Important project boundaries

- Existing GitHub repository used throughout. Tested pure Dart scheduling engine and a separate **source-only Flutter editor prototype** now exist on isolated branches; neither is merged to `main`.
- **Approved:** One Flutter + Dart codebase for Android, Windows, iOS/iPadOS and macOS in the first release; platform-specific compiled packages. Native OS menus/graphics are optional.
- **Latest approved D-036:** A full session or a configured normal/active special break of **0 minutes is invalid** (blocking error). Positive 1–4-minute breaks remain allowed; **D-031 warning and explicit user acknowledgment** apply to a mathematically legal schedule exceeding the 25-minute posing / 5-minute rest recommendation. Regular/special defaults remain editable positive 5m/15m. Logical 0-minute break-slot proposals were withdrawn.
- **Approved Step 1.3:** Automatic midpoint/earlier-tie replacement, evenly spaced multiple long breaks, manual positioning, whole-timeline validations/approvals, and 25/5 warning counter **within one full activated session** (no automatic carryover between distinct saved presets).
- **D-038 resolved by user-approved D-044:** Any **positive** special break may replace a normal break, even when shorter or equal (for example normal 5m, special 3m). The engine permits it and applies the approved 25/5 rest warning/acknowledgment. **Zero-minute configured breaks and total sessions remain invalid (D-036).** Dart core code and all **35 unit tests were executed successfully on Android Termux** (D-052).
- **Step 1.4 VERIFIED/COMPLETE** on the isolated core branch, 35/35 Android Dart tests passing. **Stage 2 STARTED** with a neutral Flutter editor source; it remains **uncompiled/unbuilt**, and visual design is provisional. Stages 3–6, distribution and production branding remain unapproved. No paid GitHub Actions.
- Every number in both timing examples is configurable through a future per-session input screen; pose groups can be added/removed.
- “Summary” is a named final reserved time block; its specific activity has not been further defined.
- Do not silently convert open technical assumptions into approved product rules.
- Every iteration must update the decision log, work log, change log and handoff with evidence and next actions.

## Step 1.4 — budget-free Dart verification

The development branch [stage-1-4-scheduling-core](https://github.com/AviY99/Atelier-Time/tree/stage-1-4-scheduling-core) now contains a safer immutable schedule approval API and local verification helpers:
- Windows: [tool/verify.ps1](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/tool/verify.ps1).
- macOS/Linux: [tool/verify.sh](https://github.com/AviY99/Atelier-Time/blob/stage-1-4-scheduling-core/tool/verify.sh).
- Each helper calls the pure Dart smoke test, `dart pub get`, `dart analyze` and `dart test`, writing `atelier-time-verification.log`.

**Not yet executed:** The assistant's local runtime does not contain Dart/Flutter, and DNS fails for SDK download hosts; no Github Actions or paid runners were used. To run the tests on your computer, install only the [official Dart SDK](https://dart.dev/get-dart), download/clone the development branch, run the appropriate helper and share the generated log. The SDK installed on a user's PC is not automatically available in ChatGPT's separate environment. No Flutter interface or Stage 2 work has been authorized.

### Android Dart verification (2026-10-23)

The user installed **Dart 3.13.7 on Android ARM64 via Termux**, successfully ran `dart tool/smoke.dart` after the warning-offset compile fix, and later reported **“All tests passed!”** after the requested `dart pub get && dart analyze && dart test` command chain. This is **user-reported test success**, not an attached test log or independently observed analyzer trace. The test source currently defines **34 Dart unit tests**. No Android APK, Flutter app, cross-platform build or later-stage screen exists. Stage 1 Step 1.4 **remains in final acceptance audit**, on the isolated branch only.


**Latest verification:** Actual Android Termux screenshots confirm `dart analyze` **No issues found!** and `dart test` **34 passing**. No separate APK, Flutter application or non-Android build has been produced. Final Step 1.4 QA coverage review still needs the valid 1m+34m custom-split endpoint before formal completion.


**Acceptance coverage checkpoint D-051:** A 35th unit test for the missing valid 1m+34m final-pose split was added on the development branch after the screenshots. **34 tests have screenshot-confirmed PASS; the new 35th test has NOT run.** To retest in existing Android Termux clone: `git pull --ff-only && dart test`.

**Final Step 1.4 verification (D-052):** Screenshot-confirmed `00:00 +35: All tests passed!` on Android Termux after branch update `8cf7a24..907f871`. Existing 34-test analyzer screenshot reported no issues; updated test-only version was not re-analyzed in supplied evidence. **Step 1.4 core complete on branch, but not merged.** Future changes require separate user approval.


## Stage 2 pure Dart editor-input tests (D-056; 2026-10-23)

The [Stage 2 branch](https://github.com/AviY99/Atelier-Time/tree/stage-2-session-editor) now includes a Flutter-independent [text-to-schedule rules adapter](https://github.com/AviY99/Atelier-Time/blob/stage-2-session-editor/lib/src/editor_draft.dart) used by the Flutter editor. This enables [11 new form-input regression tests](https://github.com/AviY99/Atelier-Time/blob/stage-2-session-editor/test/editor_draft_test.dart) to be run with the existing Android Termux Dart SDK. **These 11 tests have not run yet.** The scheduling engine remains unchanged; its Stage 1 screenshot-verified 35 passing tests are a separate result. The eight Flutter widget tests also remain unexecuted because there is no Flutter SDK.

From a Stage 2 branch checkout, a free targeted command is `dart test test/editor_draft_test.dart`. No GitHub Actions, paid runner or app build is needed for that test.

**D-059 verification update (2026-10-23):** The user's clean Android Termux `~/Atelier-Time-Stage2` worktree at exact GitHub branch HEAD `3bb2bad` completed **11/11 pure-Dart editor-adapter tests PASS**, screenshot-confirmed. This supersedes the earlier “11 tests have not run” wording above. A combined `dart analyze && dart test` for Stage 2 is still outstanding, and all eight Flutter widget tests remain unexecuted pending Flutter SDK. No APK/build or paid workflow.


## Dart SDK vs Flutter SDK — Stage 2 analyzer fix (D-060)

The root package is **pure Dart**; `apps/atelier_time_flutter/` is a separate package that requires a **Flutter SDK**. Termux currently has bare Dart only. Running root `dart analyze` before the package boundaries were configured caused large numbers of Flutter SDK diagnostic errors; the chained `dart test` therefore did NOT execute.

The Stage 2 development branch now has root `analysis_options.yaml` excluding the nested Flutter package from bare-Dart analysis and a separate `apps/atelier_time_flutter/analysis_options.yaml` for proper later Flutter toolchain verification. **No actual new post-fix analyzer or 46-test pass has been observed yet**. Android Termux rerun after updating the Stage 2 worktree: `dart analyze && dart test`. For Flutter code, eventually run `flutter analyze && flutter test` inside the nested app on a Flutter-capable machine. This does not change approved scheduling logic or imply an installable APK.
