# Atelier Time — D-127 Stage 7 extended automated accessibility matrix

> **CURRENT VERIFIED 2026-10-29 — D-127 STAGE 7 NEW MATRIX 8/8 PASS ON SAMSUNG (supersedes all earlier 'not compiled/not run' entries):** Actual user-supplied Termux screenshot at 19:36 shows `Time: 11.03`, `OK (8 tests)`, and `PASS: 8/8 NEW D-127 Stage 7 matrix accessibility UI tests.` The isolated `AccessibilityStage7MatrixTest` run covers synthetic font scales 100%/130%/200%, tab access, narrow Free wheels/presets, Design/Sound semantics and non-color timeline description. Its first AndroidTest compile failed on a test-only invalid `assertExists` import; source fix [`0f96055`](https://github.com/AviY99/Atelier-Time/commit/0f9605500f840cfcc9dd097778a06e7cf5622b81) was followed by Samsung `BUILD SUCCESSFUL` for **test APK only**, then this eight-test PASS. Earlier new accessibility tests: separate Samsung `6/6 PASS` (7.22s), **not rerun**. Frozen Stable 9 D-126: historical `285/285 PASS`, **not rerun**. **299 historical passing method executions across different versions, not one 299-test D-127 suite.** Test runner did NOT update the main app or clear app data; it installs only the test APK. Full matrix report remains locally at `Downloads/Atelier-Time-D127-Stage7-Matrix-Results.log`; screenshot is the evidence reviewed in chat, not remote raw-log inspection. User had already verified the in-place D-127 installation and Saved Classes/Sounds before these new runs; **post-matrix saved-data preservation has not yet been explicitly reconfirmed**. Stage 7 is **AUTOMATED QA PASS / MANUAL ACCEPTANCE OPEN**: actual TalkBack navigation/speech quality, visible Day/Night contrast and clipping at large real system font, relevant dialog focus, and post-test saved-data confirmation remain. Do not label D-127 Stable 10 without explicit user approval. No new Gradle/ADB installation requested for already-passed tests; NO GitHub Actions/cloud compilation, no full 285 rerun, no uninstall or clear data. Stable 9 branch remains unchanged. [Matrix test record](D127_STAGE7_AUTOMATED_MATRIX_2026_10_29.md).


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
