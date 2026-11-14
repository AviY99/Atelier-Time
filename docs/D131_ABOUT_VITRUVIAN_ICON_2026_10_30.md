# D-131 — Approved Vitruvian clock artwork in About description card (2026-10-30)

## User request and source

User provided a composite screenshot showing the **user-approved Vitruvian-man clock app icon** as the large left visual, three smaller icons, and a phone mockup. The user explicitly requests: **"אני רוצה גם לשלב אותו באיכות הגבוהה במסך about בתןך הריבוע של הטקסט של הציאור מצד שמאל"** — also integrate this exact art at good quality **on the left, within the existing About-description card**. Do not replace the About paragraph or modify a different card.

Artwork extraction: only the large left-hand full-color clock icon was taken from the user-supplied composite (not the examples or phone display). Original extracted preview is 975×975 px; the bundled, display-ready asset is a **320×320 px WebP**, optimized for a rendered 96dp side image on a high-density Samsung screen (on a ~3× display, about 288 physical px). The Git blob was byte-for-byte verified against locally generated WebP checksum; no external image hosting or online loading. The uploaded screenshot is a raster mockup, not an editable/original vector or an independent 1024×1024 master; do not misrepresent the source resolution or claim that tiny numbers remain readable at every density. This is a real bundled, offline bitmap at an adequate resolution for the requested About display.

## Implementation in Android dev branch only

- Release candidate **D-131 / versionCode 25**, compileSdk/targetSdk 36; do NOT update frozen Stable 9/D-126.
- Resource: `android-native/app/src/main/res/drawable-nodpi/atelier_vitruvian_clock.webp`, 320×320 WebP, user-supplied visual extracted from the large primary composite panel.
- File: `android-native/app/src/main/java/com/ateliertime/android/AtelierDesign.kt`.
- **Inside the FIRST ElevatedCard in About**, after the divider: replace only the previous standalone description Text with an adaptive row. `Image` on the LEFT; original full English description Text on the RIGHT; `CompositionLocalProvider(LocalLayoutDirection.Ltr)` protects image-left placement even if device is RTL.
- Image shown at 96dp (76dp if the card's content width is under 270dp) with rounded corners and `ContentScale.Fit`, so the entire icon artwork remains visible, including 12/9/3/6 and the Vitruvian figure. Image semantic description + Compose test tag `about_description_vitruvian_icon`; description text tag `about_description_text`. Text does not truncate; textAlign Start avoids huge justified gaps in the narrower column.
- Preserve About header, version/copyright/developer credit, Privacy Policy and Instagram frames/URLs, fixed Instagram avatar and all other screens. No timer, sound, Saved Classes, notifications, FGS, permission or network change.
- No launcher-icon replacement or Play Store graphics modifications are implied by the About-screen image request.

## Local-only build; explicit gates

- **SOURCE ONLY. D-131 is not built, installed or tested.** Previous user-accepted installed **D-130 / code24** remains untouched. User-approved Stable9/D-126 remains frozen.
- The new helper `android-native/tools/build_d131_about_vitruvian_icon_on_phone.sh` verifies branch, clean local source, API36 D-131/code25, both bundled image resources and existing Samsung CoGo SDK/toolchain, then runs only `:app:assembleDebug`. It writes APK, log and manifest to Downloads. **No install, device data writes, automated suites, Google Actions or cloud compilation.**
- From Samsung Code on the Go, current working dir `android-native`, after appropriate approval: `git pull --ff-only && bash tools/build_d131_about_vitruvian_icon_on_phone.sh`.
- **After local BUILD PASS**, seek separate permission for an **in-place Android install** on top of D-130 (do not uninstall/clear data).
- Focused device QA for D-131: text still readable in the About-description box with the artwork to its left, whole clock icon visible, normal narrow/RTL modes, other About cards visible, two links open, Saved Classes/Sounds remain. Do not rerun historical 285/285 QA or claim universal compatibility.
- Not a user-designated Stable 10, signed production AAB or Play release. ASO-01 preparation/Play release work remains separate.

## Scope / provenance

User-approved earlier **D-130** complete user acceptance included working About Privacy + Instagram links, static Instagram avatar and preserved Saved Classes and Sounds; D-131 builds only on that source. [D-130 acceptance](D130_FIXED_INSTAGRAM_PROFILE_AVATAR_2026_10_30.md). [Play discovery/ASO-01](ASO01_DISCOVERABILITY_REQUIREMENTS_AND_USER_APPROVAL_2026_10_30.md).

## Pending

Local Samsung build and later focused visual/user data confirmation. Do not mark D-131 PASS or Stable 10 from this source commit alone.
