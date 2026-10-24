# D-091 — About identity header and bluer, softer Night proposal

> **SUPERSEDED BY APPROVED D-092 — 2026-10-24:** User explicitly approved the proposed Night Blue **background only** colors `#304D70` canvas, `#3B5A7A` cards, `#4A6A8A` secondary surfaces. These exact three roles have now been implemented in `AtelierDesign.kt` on the Android draft branch, source commit `151aeeb51c3d754f48e0bddeed49786402a1396c`. No button/accent/tahini, Day or timer algorithm changes. About credit is shortened to **"Developed by Avi Yosfan · with ChatGPT (OpenAI)"** after reviewing OpenAI's published human-credit/AI disclosure and brand guidance. Title and credit centered; English description justified with smaller type; existing top row still name / real numeric Version / © 2026. Previously incorrect raw-regex double escaping for version extraction fixed. **D-092 new source NOT yet Gradle compiled/JVM-tested/APK installed/device QA accepted.** This page's earlier "proposal not approved" statements are historical as of D-092; see [D-092](DECISIONS.md).

Date: 2026-10-24. **D-091 ABOUT CHANGE COMMITTED AS SOURCE ONLY / ANDROID BUILD + PHONE UI QA PENDING. Night palette is a PROPOSAL, NOT CODED.**

## Approved About layout correction

User explicitly requests **Atlier Time**, actual numeric **Version**, and **© 2026 on the same TOP ROW** of the About card; English-only About remains the chosen copy language. Implemented in the Android Compose source `AtelierDesign.kt` on branch `android-native-kotlin-compose`, commit `aa454a1e47d2cbbaaf73fde50c9c58c510cc0f52`:

- Single `Row(fillMaxWidth)` inside `CompositionLocalProvider(LocalLayoutDirection provides Ltr)`; three entries left-to-right: **Atlier Time** (auto-fitting within a `weight(1f)` cell), **Version ${actualInstalledNumericVersion}** (labelMedium) and **© 2026** (labelSmall). Small 6dp gaps, max one line for each and no horizontal scroll.
- Developer credit is immediately below: `Developed by Avi Yosfan with assistance from Open AI` (bodyMedium). Below the divider is a smaller bodySmall English description focused on live atelier class timing, sessions, breaks, remaining time, cue-driven operation, not primarily a class planner. Actual APK `versionName` remains unchanged and numeric extraction is retained.
- Narrow-screen/dynamic-font acceptance check required: all three header values must be visible with legible name sizing, no overlapping. The source has **NOT** been phone Gradle compiled or APK-installed since the D-089/D-090/D-091 UI changes. D-088 remains last user-accepted working release (D-086 snapshot branch frozen separately).

## Night Blue — revised background-only proposal, approval required

User finds previous proposed **Night Mist** still too near-black/dark and wants a **gentler, more blue-toned nocturnal feel**. Supersede the older unapproved D-089 Night Soft and D-090 Night Mist proposals. Recommend new **Night Blue** with only THREE surface-role changes:

| Semantic background role | Night currently in source | D-090 unapproved Night Mist | D-091 *NEW* Night Blue proposal |
|---|---|---|---|
| app background / canvas | `#0E1624` | `#283746` | **`#304D70`** |
| normal card / surface | `#1D2B3D` | `#344759` | **`#3B5A7A`** |
| secondary / variant surface | `#26384C` | `#405569` | **`#4A6A8A`** |

All Night button and selected-tab accent colors, text foregrounds, outline, error palette, Day palette and solid tahini hue are **untouched**. Examples of unchanged Night values: `primary #83BCFF`, `secondary #C5B1FF`, `onSurface #F5F8FF`, `outline #8493A6`, tahini `#D5BD94`. No gradient, flashing, or numeric animations.

**Night Blue not approved and not implemented**. Must obtain explicit approval before changing `AtelierDesign.kt` Night colorScheme background/surface/surfaceVariant. After approval run ONE combined local CoGo Android `:app:testDebugUnitTest :app:assembleDebug` and update-install; verify About header, five visible tabs and new background-only Night appearance. Preserve existing saved Classes without uninstall/clear data. Existing `stable/android-d086` unchanged, PR #3 Draft and no paid GitHub Actions.
