# D-140 — Android system splash white + Day/Night navigation-bar correction

**Date:** 2026-10-31. **Scope:** source-only correction after user tested D-139.  
**Version:** D-140 / code 34 / API 36.  
**Stable reference unchanged:** Stable 10 / D-138 at `6497db700587ac8f24a502137f085f52703b3524`.

## Confirmed D-139 device behavior and defect

The user supplied three Samsung screenshots: (1) the brief Android system opening had a parchment-colored rounded frame with a correctly centered small Vitruvian icon, (2) the approved portrait image showed completely and clearly with Atelier above and Time below, (3) the app itself worked. **Only defect:** the Android 3-button navigation bar kept the same parchment `#C8A77C` color after the portrait opening, even when switching in-app Day/Night; user asked why the first Android screen was no longer white/transparent.

Source audit showed D-139 set `android:windowBackground` **and** `android:windowSplashScreenBackground` to `#C8A77C` in the launch theme, with no subsequent synchronization with the user's saved `DesignPalette`. With Android 15+ enforced edge-to-edge 3-button navigation, the window background can influence the navigation bar behind the buttons. The system splash is single-color and must not be assumed transparent.

## D-140 targeted correction

1. Retain the D-139 approved full-frame PNG, hash, layout, unchanged illustration, icon and approximately 900ms Compose portrait opening.
2. Set both base and `values-v31` Android **system** splash window background to **white**. Retain its correctly inset small clock icon. White is deliberate: a transparent platform splash is not guaranteed.
3. During the full-screen parchment portrait, MainActivity uses the parchment window/3-button backing for a visually seamless full-frame opening.
4. After the portrait, `SessionEditorScreen` observes the existing **resolved `designPalette`** (including manual Day, Night, AUTO time changes and Design preview), and updates the window background, navigation-bar color, three-button contrast scrim and icon brightness via AndroidX `WindowCompat`. Colors are **Day `#F7F4ED`** and **Night `#304D70`**, matching existing `AtelierDesignTheme`. Existing root `Surface` already draws the matching palette background under safe system insets.
5. No edits to timer audio/FGS/notifications, Saved Classes, reports, privacy, About, default selection, service lifecycle, launcher icon, or release signing.

References: https://developer.android.com/develop/ui/views/launch/splash-screen/ and https://developer.android.com/about/versions/15/behavior-changes-15 .

## Samsung Code on the Go build (no cloud)

Keep the **exact previously user-approved PNG** `Download/Atelier_Time_Splash_User_Artwork_Preview.png` (SHA-256 `c7d69a52c53a86738cc416b4c29eb621f8792cf367539732ce0ffa3639d8460d`) on the phone. D-140 build script validates and packages it temporarily; the PNG **has not been committed as a binary to GitHub**.

From `Atelier-Time/android-native` in **Samsung Code on the Go**, execute:

```bash
git pull --ff-only && bash tools/build_d140_navbar_fix_on_phone.sh
```

Creates `/storage/emulated/0/Download/Atelier-Time-D140-API36-Candidate.apk` and local log/manifest; **no auto-install, uninstallation, data reset, external Android build, GitHub Actions, or automated device QA**.

## Focused acceptance checklist after user-initiated in-place install

1. First Android icon-only splash has a **white** backdrop and no clipped numerals.
2. Second approved Atelier/Time portrait appears complete, with parchment background as in D-139.
3. App opens normally with navigation bar backed by light Day surface; select Night → bar becomes blue with legible white buttons; return Day → light bar with dark buttons. Switch AUTO and verify the Design preview and actual clock transition where possible.
4. Day/Night across **all six tabs**, Android task switching/back, screen lock/unlock, Saved Classes, Sounds, diagnostics and data are not regressed.
5. No full automated test suite was rerun on D-140; screenshot review and test outcomes await user. Stable 10 stays frozen.

Do not declare D-140 stable or Play-ready before explicit user acceptance.
