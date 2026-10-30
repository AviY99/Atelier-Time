> **D-130 DEVICE SCREENSHOT QA — 2026-10-30 (latest):** User supplied a Samsung About-screen screenshot after the local D-130 BUILD PASS. Screenshot shows **Version D-130** in the running app, the user's bundled circular Instagram photo visibly on the RIGHT of the existing Instagram card, the Privacy Policy and Instagram cards, and no visible clipping. **Installed/visual About QA PASS by screenshot.** The screenshot alone does **not** verify tapping either link in D-130 or post-upgrade Saved Classes/Sounds preservation; these remain confirmation points. (Both links were separately user-confirmed working in D-129.) Do not claim automated D-130 tests, full regression PASS, Google Play acceptance, or Stable 10. Stable 9/D-126 remains frozen. No code changes/builds/reinstall/paid GitHub Actions for this docs-only record. [D-130 QA record](D130_FIXED_INSTAGRAM_PROFILE_AVATAR_2026_10_30.md).

# D-130 — Bundled, fixed Instagram avatar (2026-10-30)

## User decision and verified previous release
- User confirmed **D-129 was installed and both About links work**. D-129 / versionCode 23 is therefore the last installed, user-accepted development candidate, **not a newly designated Stable 10**.
- User supplied the exact Instagram profile photo and explicitly rejected automatic online image refresh. Use only a one-time, bundled fixed image.
- Most recently designated immutable stable checkpoint remains **Stable 9 / D-126**, with historical 285/285 QA PASS.

## Changes in Android development source only
- **D-130 / versionCode 24**, compileSdk/targetSdk 36, on `android-native-kotlin-compose`.
- Embed optimized user-supplied profile artwork (192 × 192 WebP, ~3 KB), at `android-native/app/src/main/res/drawable-nodpi/instagram_profile_avatar.webp`.
- In the existing About Instagram footer card only: show the 64dp circular avatar to the **right** of the existing English text and Instagram link. Force left-to-right layout for that one card so photo stays right under RTL settings. Keep existing tags, add `about_instagram_avatar`.
- The Privacy Policy card, both destination URLs and the rest of the app remain unchanged.
- **No INTERNET permission, network request, cache refresh, remote photo dependency, FGS/timer/audio/notification changes**.

## Verification and release gates
- Source prepared and committed only. **No D-130 compile, install or Android device QA is claimed.**
- Build **locally on Samsung Code on the Go only**, from the `android-native` directory after `git pull --ff-only`:
  `bash tools/build_d130_static_instagram_avatar_on_phone.sh`
- Build helper runs only `:app:assembleDebug`, saves candidate APK, diagnostic log and hash manifest in Downloads. It does not install/test/push data or start GitHub Actions.
- When build PASS is confirmed, seek separate authorization before data-preserving in-place install on D-129; no uninstall/clear data.
- Targeted device checks: About image is circular and to the right, visible Day/Night and RTL/narrow screen, two original About links still open, and saved Classes/Sounds remain intact. No repetition of old 285/6/8 test suites without regression evidence.
- Store signing, AAB, Play FGS declaration/video, Data safety, artwork and Google Play review remain pending; no Stable 10 promotion without user consent.
