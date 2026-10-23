# Atelier Time — Stage 2 Flutter session editor (unverified source)

**Status:** This is a first **code-only** responsive Flutter session editor / full timeline-preview shell. **Flutter was not compiled, analyzed, widget-tested, or built in this environment**, and no native platform folders are generated. It is **NOT an Android APK**.

- Created on [stage-2-session-editor](https://github.com/AviY99/Atelier-Time/tree/stage-2-session-editor), based on the 35/35 Android-Dart-verified core branch `stage-1-4-scheduling-core`.
- Pure Dart package `atelier_time_core` is referenced via `path: ../..`; do not replace the tested scheduler with hard-coded sample schedules.
- UI: session name, positive minute inputs, opening groups, regular/long break configs, automatic/manual replacement, cycle order, all-phase preview, explicit final-pose remainder/even/custom choice, 25/5 warning acknowledgement and in-memory approval invalidated on edits.
- UI has **no persistence/activation** (Stage 3), **no running timer/audio** (Stage 4), **no production branding/logo**, no native application package or installation/build steps completed.
- Temporary neutral Material 3 is *not* a final visual style decision.

## Flutter runtime testing (later, after an accessible Flutter SDK exists)

The user's Android Termux currently has **Dart**, not Flutter. The bare Dart package tests are independent and previously passed 35/35.

From a suitable Flutter SDK machine:

```bash
cd apps/atelier_time_flutter
flutter pub get
flutter analyze
# Generate native folders for a target platform as a separate approved build step:
# flutter create --platforms=android,ios,macos,windows .
# flutter run
```

`flutter create` must be reviewed first for changes to existing prototype source files and configuration. iOS/macOS builds require macOS/Xcode, Windows builds require Windows developer tools, Android needs Android SDK. No paid GitHub Actions runners, build services or account budget increases.

**Gate:** Actual Flutter compile/analyzer output, widget tests, input/preview accessibility review and user acceptance are required before calling Stage 2 complete. The underlying Dart core's 35 passing tests do **not** prove this Flutter UI compiles.


## 2026-10-23 — Second Stage 2 source increment (NOT tested)

- The editor now exposes stable keyed controls for session duration, break settings, ending choices/slider, explicit warning acknowledgment and approval.
- Parsing errors render an explicit **blocking input error**; mathematically invalid schedules show blocking validation errors and do not display a misleading incomplete phase timeline or permit approval.
- Phone layout keeps both the rules and full preview mounted inside a scrollable column so the ending controls remain reachable; wide screen still has independent two-pane scrolling.
- Added `flutter_test` SDK dependency and [eight written widget tests](test/session_editor_widget_test.dart) covering: hard zero-minute and non-numeric errors, exact approved Example B preview boundaries, dynamic opening group edit/remove, final-pose selection and approval invalidation, even/custom slider, D-031 rest-warning acknowledgment, and wide layout.
- **NO Flutter SDK is available to execute these tests** in this environment, and Termux Dart-only checks do not compile Flutter widgets. These are written assertions, **NOT passed tests**. The 35/35 passing original pure-Dart core is unchanged.
- `main` and PR #1 remain unmerged; draft Stage 2 PR #2 is for review only. No GitHub Actions or paid services.

### Testing when Flutter is available

```bash
cd apps/atelier_time_flutter
flutter pub get
flutter analyze
flutter test
```

On the first Flutter-capable run, correct any compilation, responsive-layout, focus/scrolling, widget-finder, or semantics errors before reporting results. Then review appearance with the user; the current neutral theme is not final branding.


## 2026-10-23 — Stage 2 form adapter extraction (D-056, UNTESTED)

- Added Flutter-independent `lib/src/editor_draft.dart` in the repository core package. Flutter editor now uses `SessionEditorDraft.toRules()` instead of duplicating minute parsing and rule construction.
- The adapter preserves raw positive/zero/negative editor values for the **approved scheduling engine** to validate; malformed text produces an explicit input error; invalid/unused manual ordinal text is **ignored only when special break count is zero**, keeping D-037 `N=0` valid. Positive D-044 special lengths remain flexible.
- Eleven additional **pure Dart** test definitions were added under root [test/editor_draft_test.dart](../../../test/editor_draft_test.dart). Unlike Flutter widget tests, these can run using the already-installed Android Termux Dart SDK.
- **All 11 new Dart tests UNEXECUTED** until verified in Termux. The previous 35/35 Stage 1 core Dart test success remains valid and the eight Flutter widget tests remain **written but unrun**.
- No flutter SDK, APK, native platform scaffolding, GitHub Actions, paid runner or merge.

On Android Termux, the user can fetch/check out the `stage-2-session-editor` branch and run `dart test test/editor_draft_test.dart` from repository root. Do not run `flutter test` in Termux without a working Flutter SDK.
