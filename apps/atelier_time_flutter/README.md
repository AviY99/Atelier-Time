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
