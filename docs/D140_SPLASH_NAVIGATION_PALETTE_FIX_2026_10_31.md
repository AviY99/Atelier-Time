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


## 2026-10-31: follow-up on missing Download file / reproducible source archive

The user deleted the standalone approved PNG from Downloads. The first D-140 attempt stopped in a **pre-build source-file check**, with **NO compilation, install or data change**. This exposed a prior workflow mistake: the exact approved image was not committed in GitHub along with D-139, so the APK build depended on an unrelated temporary Download file. The original 1080×2340 PNG is still embedded verbatim in the user's already-built/installed D-139 APK asset (subject to SHA verification).

Developer branch now includes `tools/persist_d139_splash_art_in_git_on_phone.sh`. It can recover the **exact byte-identical original image** from the D-139 APK in Downloads, or the readable installed package, verify SHA `c7d69a52c53a86738cc416b4c29eb621f8792cf367539732ce0ffa3639d8460d`, then commit and attempt `git push` on the existing Android development branch. It does not access user data, uninstall apps, perform builds or run paid GitHub Actions. Git credentials may be required for the user's device to push. **Only a successful push actually preserves the asset in remote GitHub.** Until verified, never claim that the PNG is already in the remote source.

D-140 builder additionally accepts the image from an existing tracked `app/src/main/assets/atelier_splash_approved_d139.png`, or original Download PNG, or re-extracts it from the D-139 APK with strict hash verification. Thus D-140 can build from the user's already-present D-139 APK without re-downloading the artwork even if Git push credentials are unavailable. If neither source exists it stops safely. No extra Android version increment: still D-140/code34.

Recommended single Code on the Go command when GitHub push authentication is configured:

```bash
git pull --ff-only && bash tools/persist_d139_splash_art_in_git_on_phone.sh && bash tools/build_d140_navbar_fix_on_phone.sh
```

If the Git push fails, the image remains in the phone's Git working copy; do not claim remote storage. Resolve permissions on GitHub before retrying; never ask user to share passwords/tokens. Alternatively the builder itself can extract from the D-139 APK.
