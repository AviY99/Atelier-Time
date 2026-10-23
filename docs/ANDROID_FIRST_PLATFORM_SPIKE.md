# Atelier Time — Android-phone-first feasibility and migration plan

**Updated:** 2026-10-23  
**Decision:** D-062 — USER-APPROVED REQUIREMENT CHANGE; technology *candidate* remains under a bounded empirical test, not finally approved.
**Highest priority:** fast, repeatable design/code/test/live-preview/**installable Android build on the Android PHONE**, while keeping later iOS/iPadOS, Windows and macOS conversions efficient through code reuse. Those future native builds do **not** need to run locally on the Android phone from day one.

## Requirements revised by user (supersede D-061's strict all-host native-builder requirement)

1. Android on-device development is the immediate first-class environment. Avoid relying on Android Studio desktop, Mac, paid CI, or an unsupported patched Flutter SDK merely to verify initial features.
2. A fast edit → compile → test → preview cycle on the Android phone is essential.
3. Produce an actual installable Android APK on the phone; a browser-only proof does **not** close that requirement.
4. Architect now for efficient **later** conversion to iOS/iPadOS, Windows and macOS. Target-specific signing/build infrastructure is acceptable **later**, provided domain logic and UI can be shared wherever feasible.
5. Keep requirements for offline local presets, reliable timed sessions, three different audio cues, and background/locked-device behavior. Do not silently reinterpret a browser tab as a finished native timer.
6. Reuse proven artifacts: Stage 1 Dart scheduling engine **35/35** tests passed, Stage 2 Flutter-free Dart form adapter **11/11** tests passed on the clean Stage 2 commit. Do not discard or port these without evidence.
7. No GitHub Actions/paid cloud builds or surprise toolchain costs. Preserve the unmerged Stage 1/2 branches and historical decisions.

## Option shortlisting: efficiency on the phone + future reuse

| Candidate | Phone edit/preview/test | Android APK on phone | Dart core reuse | Later iOS/Windows/macOS | Risk/assessment |
|---|---|---|---|---|---|
| **Dart web UI + Capacitor** | **Promising:** existing Termux Dart; official Dart-to-JS compiler; web browser preview | **Promising but UNVERIFIED HERE:** community Termux JDK/Android SDK/aapt2/Gradle recipe | **Excellent:** import existing `atelier_time_core` directly into browser-compiled Dart | Share Dart business logic and web frontend; Capacitor for iOS via Mac, Tauri/Electron/browser for desktops later; adapter-specific native alerts | **First bounded feasibility spike** |
| Flutter community-ported to Termux | Existing Dart core; Flutter UI reuse | Unofficial Android ARM64 fork/toolchain, compatibility changes and heavy SDK | Excellent | Excellent target OS coverage from proper builders later | Potentially efficient ONLY if a tiny Flutter APK can be reproducibly built and installed on phone; lack of official development host is major risk |
| Native Android Kotlin/Compose (Code on the Go) | Maintained on-device Android IDE may provide strong native Android dev | More direct Android path, test on real device | Would need port of Dart logic to Kotlin (or keep separate layer) | Kotlin Multiplatform can share logic, but UI/OS adapters and migration must be proven | Better native background support, **poor short-term reuse of verified Dart** |
| Godot Android editor/GDScript | Official Android editor exists; touch UX is experimental | Current Android export support exists but app is a timer/business UI rather than a game | Requires Dart → GDScript port | Godot runs on mobile and desktop, but native alerts/persistence may need plugins | Not favored: unnecessary rewrite and editor constraints |
| TypeScript PWA + Capacitor | Easy Node/npm/Termux | Similar community Capacitor Termux build path | Requires Dart → TypeScript port and re-testing all time computations | Strong web reuse; native adapters later | Less favorable than Dart web **if** existing Dart code compiles and runs successfully |

**Recommended order (technical recommendation, NOT final architecture decision):** verify the smallest possible **Dart-to-JS web proof** that imports the exact existing scheduling core. If successful, build a minimal **Capacitor debug APK on the same phone** and install it; assess Android native notifications/alert reliability with screen locked. Only then approve a full UI migration.

## Bounded Step A — small Dart web proof, committed now

**Isolated branch:** [spike-android-dart-web](https://github.com/AviY99/Atelier-Time/tree/spike-android-dart-web), created from the Stage 2 branch. No modifications to the verified scheduler or adapter.

**Code:** [spikes/android_dart_web](https://github.com/AviY99/Atelier-Time/tree/spike-android-dart-web/spikes/android_dart_web):
- `pubspec.yaml` depends on existing root `atelier_time_core` and official `package:web`.
- `web/main.dart` instantiates **real Example B** through `SessionEditorDraft` and `ScheduleEngine`, checks exactly one long break **01:25–01:40** and summary **02:45–03:00**, and renders every generated phase to DOM.
- `www/index.html` is a simple responsive browser page.
- `tool/serve.dart` serves the compiled output from localhost:8765 using the **existing Dart SDK**, no Python, Node, Flutter or Mac required.

**IMPORTANT:** Code is **WRITTEN, NOT RUN / NOT COMPILED** in assistant environment. Do not claim this step has succeeded until user actually executes it on Android Termux.

**Non-destructive phone commands:**
```bash
git -C ~/Atelier-Time-Stage2 fetch origin spike-android-dart-web
git -C ~/Atelier-Time-Stage2 worktree add -b spike-android-dart-web ~/Atelier-Time-Spike FETCH_HEAD
cd ~/Atelier-Time-Spike/spikes/android_dart_web
dart pub get
dart compile js -O1 -o www/main.dart.js web/main.dart
dart run tool/serve.dart
```
Then open **http://127.0.0.1:8765** in the Android browser while the Termux server runs. Expected proof is a real schedule, **PASS** indicator, 01:25–01:40 special break and 02:45–03:00 summary. If branch/worktree already exists, **do not force or delete**, inspect safely. `Ctrl+C` stops server.

## Step B — real Android APK on-phone (NOT STARTED)

If Step A works, prototype only the tooling for Capacitor/Gradle Android APK using a **community documented** Termux recipe (not a vendor-official supported Android development host):
- [Termux Capacitor APK recipe](https://github.com/rayzery/termux-capacitor-apk) documents Termux OpenJDK 21, Android SDK platform/build tools, native Termux `aapt2` override, Gradle/Capacitor; tested on one Samsung Android 16 device.
- Capacitor can wrap static HTML/JavaScript output; it does **not** require converting the Dart source to TypeScript to run the compiled JS bundle.
- Need success evidence: generated APK, actual installed launch, **native permission/notification test**, run offline, package ID/signing process, build time and storage. Do not assume guide works on user's phone or install unverified third-party binaries.

**No APK exists yet.** Installing the SDK/Gradle toolchain has potentially large storage/time costs, but no GitHub Actions/paid CI is needed for the attempted local route.

## Step C — clock/audio reliability and efficient future port (NOT STARTED)

- Verify time source uses elapsed monotonic timestamps + immutable schedule boundaries, not counting timer callbacks under background suspension.
- Confirm actual Android background notification/alarm delivery + three distinct cues under screen lock via native adapter; a browser WebView alone cannot promise this.
- Separate core schedule, editor model and UI from native OS alarms, notifications, persistence, packaging.
- Later iOS target likely requires a Mac/Xcode builder; Windows/macOS can use a desktop WebView wrapper such as Tauri/Electron if suitable. Actual conversion effort and native plugin parity must be validated; do not promise automatic installers for the other three OSs.

## Exit criteria BEFORE adopting a new app stack

- Proof A: `dart compile js` on the **user's actual Android ARM64 Termux**, browser loads actual Dart schedule correctly.
- Proof B: real debug APK generated **on that same phone**, installed and launched, with core schedule visible.
- Proof C: minimally viable Android native timed notification/alert under screen lock using an appropriate adapter, not just web timers.
- Proof D: explicit design note for future iOS/Windows/macOS wrappers, shared code estimate, unavoidable signing tools and costs.
- User approves the demonstrated stack. Until then, **DO NOT delete original Flutter draft or rewrite entire UI**; Stage 2's former Flutter branch remains frozen pending proof.

## References

- [Official Dart JS compiler](https://dart.dev/tools/dart-compile), [official web interop](https://dart.dev/interop/js-interop/package-web).
- [Termux community Capacitor APK procedure](https://github.com/rayzery/termux-capacitor-apk) — one contributed method, **not official vendor support**.
- [AndroidIDE project's archived/unsupported status](https://androidide.com/) — avoid treating old AndroidIDE as maintained; successor needs separate assessment.
- [Godot official Android editor](https://godotengine.org/download/android/) — editor is still described as experimental/touch-unoptimized, but an alternative.
- [Capacitor official website](https://capacitorjs.com/), [Tauri official website](https://tauri.app/).
