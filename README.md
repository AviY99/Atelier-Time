# Atelier Time

A planned offline-capable, installable posing/break timer for live-model **drawing and sculpting** ateliers.

**Project status (2026-10-23):** Stage 1 Steps 1.1–1.3 design/architecture complete and **Step 1.4 pure Dart scheduling core VERIFIED/COMPLETE ON THE ISOLATED [development branch](https://github.com/AviY99/Atelier-Time/tree/stage-1-4-scheduling-core) (D-052)**. User's Android Termux Dart 3.13.7 screenshot shows **35/35 unit tests passing**, including both T09 final-pose split endpoints. Earlier smoke checks passed, and `dart analyze` reported **No issues found!** on the preceding 34-test version (not rerun after the 35th test-only addition). Broader edge/target-OS QA remains later work. **Code is not merged into `main`. Stage 2, Flutter UI, runtime timer/audio, offline preset library, APK, GitHub Actions, paid runners, and multi-platform builds are NOT authorized/started.**

Atelier Time lets an organizer configure pose and break rules, generate and approve a chronological schedule, save reusable presets, and run a timer with three distinct sound cues. If the last pose would be incomplete, schedule review offers: (1) keep the remaining short final pose; (2) split the time of the last two poses as evenly as possible; or (3) adjust their split with a ruler/slider.

## Documentation

- [Product requirements](docs/PRODUCT_SPEC.md)
- [Schedule model and algorithms](docs/SCHEDULING_RULES.md)
- [Complete 3-hour example schedules](docs/EXAMPLES.md)
- [Delivery stages and approval gates](docs/ROADMAP.md)
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

- An existing GitHub repository is being used. No application implementation has started.
- **Approved:** One Flutter + Dart codebase for Android, Windows, iOS/iPadOS and macOS in the first release; platform-specific compiled packages. Native OS menus/graphics are optional.
- **Latest approved D-036:** A full session or a configured normal/active special break of **0 minutes is invalid** (blocking error). Positive 1–4-minute breaks remain allowed; **D-031 warning and explicit user acknowledgment** apply to a mathematically legal schedule exceeding the 25-minute posing / 5-minute rest recommendation. Regular/special defaults remain editable positive 5m/15m. Logical 0-minute break-slot proposals were withdrawn.
- **Approved Step 1.3:** Automatic midpoint/earlier-tie replacement, evenly spaced multiple long breaks, manual positioning, whole-timeline validations/approvals, and 25/5 warning counter **within one full activated session** (no automatic carryover between distinct saved presets).
- **D-038 resolved by user-approved D-044:** Any **positive** special break may replace a normal break, even when shorter or equal (for example normal 5m, special 3m). The engine permits it and applies the approved 25/5 rest warning/acknowledgment. **Zero-minute configured breaks and total sessions remain invalid (D-036).** Dart code and tests are written but **not yet executed**.
- **Step 1.4** is restricted to the code/test core on a development branch, **without GitHub Actions, paid runners or deploys**. Dart 3.13.7 on Android passes analyzer and 34 written tests; final QA acceptance and cross-platform app builds remain pending. Flutter UI/scaffold, timer, distribution/signing and later stages remain unauthorized.
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
