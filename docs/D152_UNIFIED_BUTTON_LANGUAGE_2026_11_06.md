# Atelier Time — D-152 / Code46 — one consistent design language for buttons

> **2026-11-06 LOCAL SAMSUNG BUILD EVIDENCE — D152/Code46 PASS (screenshot).** `ANDROIDTEST IMPORT PREFLIGHT PASS`, `ARTWORK AUDIT PASS`, **300/300 JVM tests passed, 0 errors/failures/skipped**, `APK ARTWORK INCLUDED PASS` with exact approved splash SHA256 inside final APK, and `D-152 JVM TESTS AND BUILD SUCCESSFUL` producing `/storage/emulated/0/Download/Atelier-Time-D152-API36-Candidate.apk`. AndroidTest **compiled only, not executed**. No installation or user visual acceptance proved by the screenshot; frozen Stable15/D147/code41 unchanged. Next gate is in-place install and hands-on Day/Night visual/behavior/data-preservation acceptance before any Stable16 promotion. Historical 'build pending' text below is superseded by this verified status.


**Date:** 2026-11-06  
**Status:** **SOURCE CHANGES COMMITTED on `android-native-kotlin-compose`; Samsung local build, instrumented Android UI tests and device visual acceptance PENDING.**  
**Frozen accepted stable:** Stable15 / D147 / code41 `stable/android-d147-qa15-ui-install-accepted` at `c10f424c0ec000eca2dd561911934841decb4522` — **untouched**.  
**Last phone-verified checkpoint:** D151 / code45 screenshot with **300/300 JVM PASS**, 0 errors/failures/skips, `ANDROIDTEST IMPORT PREFLIGHT PASS`, `APK ARTWORK INCLUDED PASS` (exact approved GitHub PNG SHA inside final debug APK), and `D-151 JVM TESTS AND BUILD SUCCESSFUL`. User reports D151 works but its new sound buttons are too square and the older app buttons are too bulky and rounded. D151 was **not separately frozen**.

## Approved design intent / unified visual contract

The user requested a single coherent **app-wide visual language** for button widths, outlines and rounded corners, finding a middle ground between the square/slim new Sound controls and thicker/over-rounded old buttons.

D152 uses one source of truth, `android-native/app/src/main/java/com/ateliertime/android/AtelierActionStyle.kt`:

- **Corner radius:** **12dp** on all action buttons, chips and compact Sound Play/Run/Free button outlines — neither 7dp squared off nor default large Material pill shapes.
- **Visual height:** **40dp** across ordinary primary/outline/tonal/selection buttons and manually drawn compact Run/Free/Timer frames (between the previously 34dp slim controls and taller rounded Material controls); original **48dp minimum tap zone** remains on custom compact timer/preview controls. Material buttons preserve their built-in minimum interactive sizing.
- **Border:** consistent **1dp** line weight for framed/outlined/chip/preview/compact timer controls in both Day Studio Linen and Night blue themes. Fill semantics remain primary accent for affirmative Run/Pause actions, secondary for selected presets, and error for Exit/Delete actions. Text-only Help and dialog actions retain lower emphasis.
- **Width:** shared **56dp minimum width**, **8dp standard horizontal text padding**, and responsive row weights/content widths. Do **not** force a universal fixed width on buttons with different labels: same **horizontal-paddings + minimum widths + radius**, preserving real device width and large-font legibility. Equal-width presets/action rows retain their original weighted layouts.
- **No changes to behavior:** original callbacks, enabled/disabled status, timer/replay/special-break algorithms, 25/5 rule, Class Free sound routing, Sound ON/Alarm lock, selected volume bar, Max confirmation, Saved Classes, settings, approved artwork, notifications, diagnostic data and app data remain untouched.

## Code surfaces

1. **NEW `AtelierActionStyle.kt`:** centralized tokens and five lightweight Material3 wrappers `AtelierButton`, `AtelierOutlinedButton`, `AtelierTonalButton`, `AtelierTextButton`, `AtelierFilterChip`. Wrappers retain existing `onClick`, `enabled`, `modifier`, `colors`, and optional `contentPadding` contracts; apply shared shape, min width, min height and standard padding. Native Material3 semantics remain intact.
2. **App-wide adoption:** `SessionEditorScreen.kt` (39 calls, including 25 text-only Help/dialog actions), `SessionTimerScreen.kt` (4 Help/dialog text actions), `MelodySettingsSection.kt` (8 material controls), `PresetLibrarySection.kt` (11 controls), `AtelierDesign.kt` (3 controls). Prior primary/outline/tonal buttons and Sound/Editor chips now have shared radii/sizing. Special about-page WhatsApp action remains a branded icon action, not a text-button.
3. **New compact controls:** `RunningClassQuickControls.kt` and `SlimTimerAction.kt` now use the same **12dp rounded corners / 40dp visual height / 1dp stroke / 48dp click wrapper**, not former 7dp/34dp. Single Sounds-selected Media OR Alarm slider remains. All five Class action buttons, Free Timer quick presets and Start/Pause/Exit share the geometry. Run/Free Awake button and grouped Volume/Max/Sounds off frame also match.
4. **Sounds triangle Play icon:** replaced isolated `OutlinedIconButton` frame with a **40dp/12dp/1dp visually framed** triangle **inside a 48dp clickable button** with original cue/duration accessibility content and callback. Stable `sound_preview_*` tags retained; new visual `sound_preview_visual_*` tags added.
5. **Regression source:** Existing `D151SlimControlsAlarmHelpUiTest.kt` adapted to geometry tokens (40dp) rather than 34dp constants. New `D152UnifiedButtonsUiTest.kt` has **3 Android Compose UI methods** covering cross-style minimum widths/heights, same token geometry in Run/Free actions, and 40dp/48dp Sound Play preview. Android instrumentation tests are **compiled, not executed** by the local build script.
6. `app/build.gradle.kts` versionName `D-152`, versionCode **46**. No release signing or Play upload.

## Build locally on Samsung Code on the Go — one command

```bash
cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native && git pull --ff-only && bash tools/build_d152_unified_action_style_on_phone.sh
```

The new build helper runs full JVM regression suite, guardrails for shared tokens/wrappers and previously accepted D151 sound/timer safety, **compiles AndroidTest only** (no device instrumentation), builds the debug APK, and verifies SHA256 of approved GitHub-tracked splash **inside the built APK**. No paid cloud/Actions build, app install/uninstall, user-data clearing, signed Play bundle, or Play upload.

If and **only if** Samsung terminal reports success, expected `/storage/emulated/0/Download/Atelier-Time-D152-API36-Candidate.apk` with local manifest/log. User should then do an **in-place update** (without removing app) and compare real Day/Night screens: Home/Saved Classes, Editor, Sounds, Run, Free Timer, and Help/dialog buttons on phone including small screen font/layout, visible 40dp outlines, 12dp corner geometry, proper color of selected/destructive controls, clickable Sound Preview, all settings preserved. **Do not declare Stable16 until explicit user acceptance.**

**Google Play decisions remain FREE and initial invite-only Internal Testing**, no signed production AAB or publication authorized.
