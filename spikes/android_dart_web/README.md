# Android phone-first proof (D-062) — UNEXECUTED

This is an **isolated technical feasibility spike**, NOT a production UI or Android APK. The application code is written in **Dart**, reuses the exact verified root `atelier_time_core` scheduling engine and editor input adapter, and is compiled to browser JavaScript with the existing Dart SDK on Android Termux. **It has not been compiled or run yet.**

## Safely create a separate third worktree (preserve existing files)

From Termux:

```bash
git -C ~/Atelier-Time-Stage2 fetch origin spike-android-dart-web
git -C ~/Atelier-Time-Stage2 worktree add -b spike-android-dart-web ~/Atelier-Time-Spike FETCH_HEAD
cd ~/Atelier-Time-Spike/spikes/android_dart_web
dart pub get
dart compile js -O1 -o www/main.dart.js web/main.dart
dart run tool/serve.dart
```

Open **http://127.0.0.1:8765** in the Android browser while server remains running. You should see **PASS**, the actual generated Example B timeline, special break **01:25–01:40** and final summary **02:45–03:00**. Press Ctrl+C to stop the server.

If `~/Atelier-Time-Spike` already exists or the branch name is taken, **stop** instead of forcing, cleaning or deleting files. The existing `~/Atelier-Time` staged changes and `~/Atelier-Time-Stage2` tested checkout should remain untouched.

This test requires only the Dart SDK already installed on the Android device. It does not need Flutter, Node, Python, a paid CI runner or a Mac.

## Meaning of pass vs unproven requirements

A pass proves (a) the *actual* Dart scheduling code compiles to JavaScript on that phone and (b) the Android browser can execute it correctly. It does **not** prove any native APK, background timer, notifications or three audible cues.

The next gate, after this passes, is to verify a Termux Android build through the **community** Capacitor/JDK/SDK/aapt2 toolchain described in [this contributor's guide](https://github.com/rayzery/termux-capacitor-apk). Do not claim that method works on this user's phone without a real generated, installed APK. Native background/locked-screen cue reliability then needs a separate actual-device test.

The later iOS/Windows/macOS app targets remain desired. They will use target-specific builders at that later stage, not be built locally on the Android phone now. Do not port the Dart core to TypeScript or delete the existing unmerged Flutter prototype solely because this feasibility route is promising.

Current detailed requirements and exit criteria: [Android-first plan](https://github.com/AviY99/Atelier-Time/blob/main/docs/ANDROID_FIRST_PLATFORM_SPIKE.md).

## Source

- `pubspec.yaml` — Dart web package depending on local `atelier_time_core`.
- `web/main.dart` — calculates and checks Example B with `SessionEditorDraft` and `ScheduleEngine`, then renders phases.
- `www/index.html` — accessible simple browser proof page.
- `tool/serve.dart` — tiny localhost server written in Dart.

**Do not use GitHub Actions or a paid cloud builder for this spike.**
