# Atelier Time — mandatory icon and logo gate before store publication

> **CURRENT GATE (2026-10-28):** Stable 9/D-126 Timer logic and automated QA are complete. The final launcher icon, brand mark and Play Store artwork still require separate user approval before public distribution; do not mistake successful QA for publication clearance. Remaining workflow stages 7–12 are in [current roadmap](ROADMAP.md), with [release status](CURRENT_STATUS_AND_HANDOFF_STABLE9_2026_10_28.md).


**Added:** 2026-10-24, user directive after approving Android Stable 3. **Status: planned/not designed or implemented.** This is a **required step immediately BEFORE any store listing/publication** and must NOT be skipped. The user has not approved logo artwork yet.

## Product placement

Correct delivery order is: **Timer & reliability QA → background/locked-screen/audio QA → persistence/instrumentation/accessibility QA → signed Android candidate/internal release validation → icon/logo and store artwork approval gate → store listing submission/publishing**, with final release testing repeated if assets change the installed build. Creating artwork does not imply permission to publish.

The approved design and Sound layouts are frozen in Stable 3. The new logo/icon is a user-requested exception for *branding assets only*, not permission to redesign existing screens.

## Creative brief and required assets

- Establish a distinctive, legible **Atelier Time identity** related to timed drawing/sculpture/atelier sessions rather than a generic stopwatch icon. Show design options for user selection/approval before implementation. Do not treat a temporary Android default robot or OS icon as an approved final app icon.
- Maintain visual compatibility with approved Studio Linen (Day) and Night Blue aesthetics, while remaining distinguishable at small icon sizes and in light/dark wallpaper themes. Avoid illegible small lettering.
- Android launcher **adaptive icon** foreground/background layers with Android 13+ **monochrome** layer for themed icon support, plus appropriate legacy/mipmap fallbacks and installed `android:icon`/optional `android:roundIcon` manifest wiring.
- Standalone **logo/wordmark** source master suitable for future iOS, Windows, and macOS packaging. Keep licensing/rights to use the artwork in distribution; retain editable vector/source or equivalent exportable master.
- Google Play **hi-res store icon:** 512×512 pixels, 32-bit sRGB PNG (alpha supported), <=1024KB; follow full-square keylines and avoid manually baked store mask/shadow.
- Google Play **feature graphic:** 1024×500 px JPEG or 24-bit PNG, no alpha. Prepare accurate actual-app screenshots for Play listing; do not invent UI or imply an untested production capability.

## Approval and QA gate

1. Review logo/icon concepts and select/approve one via explicit user confirmation.
2. Export launcher layers, monochrome and fallback variants plus store-resolution image and feature graphic.
3. Integrate source assets on Android development branch only; **never modify frozen stable rollback commits or branches**.
4. Build/install signed release candidate; confirm launcher, Settings, task switcher, Android 13+ themed icon, alternative masks and light/dark wallpapers render correctly on the phone.
5. Inspect legibility at small sizes; verify no clipping, unwanted padding, wrong transparency, or accidental Android default robot.
6. Confirm store images meet Play Console requirements and rights are documented. After icon approval and all other Production Ready gates, proceed to store submission ONLY after explicit authorization.

**Baseline audit (2026-10-24):** Current native `android-native/app/src/main/AndroidManifest.xml` has label `Atelier Time` but no `android:icon` or `android:roundIcon` attribute. The icon/branding feature is indeed missing, rather than a completed hidden stage.

**Official references:**
- Android adaptive icons and monochrome: https://developer.android.com/develop/ui/compose/system/icon_design_adaptive
- Play Store app icon: https://developer.android.com/distribute/google-play/resources/icon-design-specifications
- Google Play preview assets: https://support.google.com/googleplay/android-developer/answer/9866151

**Gate status:** NOT STARTED; no images generated, no user design selection, no APK image changes, no store publication.
