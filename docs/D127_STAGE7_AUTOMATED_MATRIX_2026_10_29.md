# Atelier Time — D-127 Stage 7 extended automated accessibility matrix

**Created:** 2026-10-29. **Status:** source committed; **NOT YET COMPILED OR EXECUTED ON SAMSUNG**. This extension follows the verified D-127 Samsung build and the six existing accessibility UI instrumentation methods, which returned **OK (6 tests)** in 7.22 seconds on the user's Samsung. The user confirmed their in-place D-127 installation and retained saved Classes/Sounds before the six-test run. Nothing in this extension renames the installed app or Stable 9.

## Decision and safety boundary

The user requested automation rather than personally performing all 34 proposed accessibility checks. We have implemented **eight additional isolated Compose instrumentation tests**, not a claim to automate all 34 human-acceptance items. Their scope is responsive layout, selectable tabs, Free wheel/preset geometry, and screen-reader semantics for Design, Sounds and the Timeline at enlarged text. The test class is `android-native/app/src/androidTest/java/com/ateliertime/android/AccessibilityStage7MatrixTest.kt`.

- **Production Android source and version remain unchanged:** D-127/versionCode21; no main APK update is needed.
- All new screen tests run inside a Compose test activity, with synthetic font scales and locally isolated randomized preference names. They do **not** modify Android font settings, install the main app, start a real Class/Free countdown, play audio, erase Saved Classes/Sounds or alter the stable branch. The root editor may read existing Design preferences but the tests do not change or save them.
- Termux runs **only the new test class**, never the previous six D-127 methods or the older 285 Stable 9 methods. Android instrumentation temporarily owns the app process, so the user must confirm no active timer before executing.
- **Only Samsung Code on the Go is authorized to compile**; no GitHub Actions/cloud build or Codespaces, and no unattended build/install.
- No UI-automation claims until an actual phone PASS. Build and test outputs stay in the phone's Downloads folder. A screenshot of the terminal's final summary is sufficient.

## Eight NEW tests, distinct from the previous six

| Method | Automated evidence attempted |
| --- | --- |
| `font100AllSixTabsRemainReachable` | Six navigation tab semantics and selected Editor at ordinary font |
| `font130CanScrollToLastTabAndSelectIt` | Enlarged navigation remains horizontally scrollable, About and Editor selectable |
| `font200CanScrollToLastTabAndSelectIt` | Maximum simulated font-scale navigation remains reachable |
| `font200NarrowFreeWheelsStayInsideCardAndPresetsRemainWide` | 280dp Free timer at 200%, three wheel bounds and two-column preset sizing; disabled Exit in READY |
| `font130NarrowFreePresetsUseTwoColumns` | 280dp Free timer at 130%, two-column preset sizing and three wheels |
| `font200DesignPaletteAndAutoSliderHaveSpokenNames` | 320dp Design at 200%, palette selection/description and Auto slider accessibility text |
| `font200SoundSlidersRetainSpokenPurposeAndSeconds` | 320dp Sound settings at 200%, two slider descriptions including seconds |
| `font200NarrowTimelineRetainsNonColorAlternative` | 280dp timeline at 200%, Canvas alternative includes Session, both breaks, Summary; legend exists |

**Limits:** Synthetic font scale is a deterministic Compose coverage technique, **not proof that the Samsung's actual 100/130/200% system-font modes, TalkBack audio output or visually rendered pixels are acceptable**. Scaled text might be cropped in ways simple geometry tests do not detect. Real TalkBack focus/navigation, readability, contrast across colors and states, dialogs, the full Editor field layout, active Class Timer button wrapping, and final post-test preservation still need narrowed human/screenshot verification. If these eight tests identify a failure, fix the precise source defect on development only; keep Stable 9 immutable.

## Execution — no previous test reruns

**1. Samsung Code on the Go** — in the existing `Atelier-Time/android-native` directory:

```bash
git pull --ff-only && bash tools/build_d127_stage7_matrix_on_phone.sh
```

This compiles **only the AndroidTest APK** (`:app:assembleDebugAndroidTest`), captures build diagnostics locally, checks that exactly eight new and six prior test methods exist and writes:

- `Download/Atelier-Time-D127-Stage7-Matrix-Tests.apk`
- `Download/Atelier-Time-D127-Stage7-Matrix-Manifest.txt` (source SHA + APK SHA-256)
- `Download/Atelier-Time-D127-Stage7-MatrixQA.sh` (guarded Termux runner)
- `Download/Atelier-Time-D127-Stage7-Matrix-Build.log`

The build script **does not install or execute anything**. If it fails, **do not rerun Gradle** just to inspect errors: the first Kotlin/Gradle diagnostics are automatically printed in the terminal.

**2. Termux**, with already paired wireless ADB connected (verify exactly one `adb devices` entry), and **NO active Class or Free Timer**:

```bash
bash ~/storage/downloads/Atelier-Time-D127-Stage7-MatrixQA.sh --run
```

The runner checks D-127/code21 installed, AndroidTest APK hash/version/source manifest, device authorization and `IDLE` user confirmation. It updates **only the test APK**, selects `com.ateliertime.android.AccessibilityStage7MatrixTest` explicitly, and requires **`OK (8 tests)`** to claim PASS. The separate results log remains at `Download/Atelier-Time-D127-Stage7-Matrix-Results.log`. A failing `STOP` is a safety gate, not a test failure.

## Release state

**Stable 9 D-126/code20 remains frozen**, historical 285/285 PASS. **D-127's original six focused tests remain PASS**. The eight incremental matrix tests are **NOT RUN** and Stage 7 stays **OPEN** until successful local new-matrix execution, focused human checks, confirmed Saved Classes/Sounds preservation after tests and user acceptance. No Stable 10 designation yet.

[Stage 7 QA source of truth](D127_STAGE7_ACCESSIBILITY_ADAPTATION_AND_QA_2026_10_28.md) · [Current handoff](HANDOFF_D127_STAGE7_TO_NEXT_CHAT_2026_10_29.md)
