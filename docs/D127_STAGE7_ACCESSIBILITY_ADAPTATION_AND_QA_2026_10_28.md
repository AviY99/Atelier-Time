# Atelier Time — D-127 Stage 7 accessibility implementation / QA gate
**Date:** 2026-10-28. **Development branch:** `android-native-kotlin-compose`.
**Production source commit:** [b9d7064](https://github.com/AviY99/Atelier-Time/commit/b9d7064cf5f9c68a288adac0a45a2909d541ee4b).

## State — do not conflate versions
- **Approved and installed:** Stable 9 = D-126 / versionCode 20, 243/243 JVM + 42/42 on-device instrumentation = **285/285 PASS**. Saved Classes and Sounds were preserved.
- **New candidate:** D-127 / versionCode 21, source written and committed only. No Gradle build, APK, installation, connected AndroidTest, TalkBack exploration, screenshot acceptance, or user confirmation for D-127 has occurred.
- Three new Android instrumentation tests have been **AUTHORED, NOT EXECUTED**. Do not count them toward previous 285/285.
- Frozen `stable/android-d126-qa9-tests-complete` and historical Stable 8 branches remain unchanged. No app data or Samsung installation was touched.

## Implemented Stage 7 source changes
1. Six tabs: retain approved six-tab compact LTR arrangement at standard font scale. At `fontScale >= 1.3`, horizontally scroll tabs using >=104dp items and normal scaled typography instead of forcing 8–11sp small text.
2. Class Timer action and Help controls: wrap at large scale using FlowRow, retain previous weighting at ordinary font scale. Pause/Resume, Next, Restart and Exit callbacks and confirmation logic remain unchanged.
3. Custom EndClockWheel: add TalkBack contextual content descriptions for previous, current, next and read-only values; preserve stepwise feasibility boundary `canSelect` and blocked feedback.
4. Timeline Canvas: give schedule color bar equivalent textual phase-total and live elapsed/remaining description; no live announcement every second. Existing Editor chronological phase list remains.
5. Free Timer: minimum 48dp preset button height; adapt from four to two columns at large fonts; preserve all durations and manual Start.
6. Advance development identity to D-127/code21. No changes to `ConstraintFuturePlanner`, timing algorithms, Saved Classes, sound storage, background services or logs.

## QA evidence
- **Static source audit** identified high-scale tabs, small Free presets and custom Canvas/wheel semantics as targeted improvements. GitHub readback is source evidence only.
- Selected existing palette contrast ratios calculated statically: Day primary/white 5.10:1; Day secondary/white 4.81:1; Night primary/white 4.98:1; Night secondary/white 4.88:1; Night timer card/white 5.12:1; Day on-background 12.17:1; Night on-background 8.67:1. This is a sample, **not** full WCAG audit.
- **3 new AndroidTest methods authored, not run:** `clockWheelButtonsHaveSpokenContextAndKeepBlockedBoundary`, `timelineHasEquivalentNonColorDescription`, `freePresetsHaveAtLeast48DpTouchHeight` in `AccessibilityStage7UiTest.kt`.

## Completion criteria — PENDING
- Build and targeted Compose tests for D-127 in supported Android/CoGo environment; record actual logs and any corrections.
- Device screen matrix: 100%, 130%, 200% system font scale; normal and narrow widths; Day/Night; six tabs, Editor side-by-side fields, Timer clock header/buttons/help, charts, dialogs, Sound settings, wheels, Free preset controls.
- Real TalkBack: focus order, spoken button/value context, chart alternative, dialogs, navigation and avoidance of per-second unsolicited announcements.
- Verify touch target size and all relevant normal/selected/disabled/error contrast combinations.
- Approve **in-place update only** separately, then verify Saved Classes/Sounds remain intact; never uninstall/clear user data.
- Record actual PASS/FAIL and user acceptance before marking Stage 7 done or creating another stable checkpoint. **No repeated full D-126 285-test sweep without a specific regression reason.**

## Branch/document contract
Android source lives only on `android-native-kotlin-compose`. Sync shared canonical documentation to `main` without merging Android code. The official installed status remains Stable 9/D-126 until new explicit user acceptance.
