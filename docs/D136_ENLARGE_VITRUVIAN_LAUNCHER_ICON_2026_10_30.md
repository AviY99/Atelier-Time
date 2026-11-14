# D-136 — Full-size Vitruvian launcher icon, without the inset picture-in-picture (2026-10-30)

## User request and evidence
The user supplied Samsung home screen, installed app/APK preview and file list screenshots showing that the D-135 Vitruvian launcher image is much too small inside a white inner square and an additional outer adaptive icon frame. User explicitly corrected release numbering: **D-135 already exists; make this D-136.**

## Root cause
The D-135 adaptive launcher icon already has proper manifest `android:icon`/`roundIcon` and `mipmap-anydpi-v26` layers. Its source `app/src/main/res/drawable/atelier_launcher_foreground.xml` wraps the approved bitmap in an **18dp inset per side of the 108dp foreground layer**, shrinking the bitmap to ~72dp, visibly exposing the user's original near-white raster margin plus the separate beige background. This is not a missing icon or issue with the About screen.

## D-136 change (development branch only)
- Android **D-136 / versionCode 30 / targetSdk 36**. D-135/code29 is retained in history.
- Change **only the adaptive launcher foreground inset** from `+18dp` to `-3dp`, letting the OS's native adaptive icon squircle/circle shape clip the thin outer white raster margin while bringing the Vitruvian clock ring almost to the icon edge. The approved figure/12/3/6/9 source bitmap, parchment backdrop, manifest icon references, adaptive icon XML files, About artwork and all other app logic remain unchanged.
- The base bitmap may still contain pale edge pixels; the D-136 change deliberately hides these via the launcher mask **rather than falsely claiming the original WebP has alpha transparency**. This relies on actual device launcher clipping; check Samsung home, APK preview and app drawer in focused QA.
- Preflight comparison simulated `+18dp`, `0dp`, `-3dp` and `-6dp` inside a rounded-square Android-like mask. `-3dp` removes the visible white nested frame while keeping all clock numerals; `-6dp` risks clipping the top numeral. Simulation is **not** device QA or proof for every OEM mask.
- Future Google Play store listing requires an independent compliant high-resolution **512×512 square icon** sourced from the approved artwork; changing the Android launcher resource does not itself upload/approve Play Store assets.

## Build and user acceptance gates
- **D-136 SOURCE COMMITTED ONLY; not yet compiled, installed, or user-validated.** No cloud Android build/GitHub Actions.
- On Samsung Code on the Go, working directory `android-native`: `git pull --ff-only && bash tools/build_d136_launcher_icon_scale_on_phone.sh`. The helper checks D-136/code30/API36, manifest icon resources and exact -3dp foreground, and calls only `:app:assembleDebug`. APK/log/hash manifest go to Downloads. **It does not install or alter app data.**
- After BUILD SUCCESSFUL, perform a separate user-approved **in-place update** over D-135 (never uninstall/clear data).
- Focused checks after upgrade: home launcher icon nearly fills its mask without nested white picture-in-picture, 12/9/3/6 remain visible, APK file preview uses the same icon, About image still unchanged; Classes, Sounds and diagnostics retained. If a pinned home shortcut caches old icon, inspect app drawer first and refresh the shortcut without uninstalling.
- D-134/code28 was confirmed user functional. D-135/code29 is present in repository and observed in current launcher screenshots but **no separate D-135 QA completion statement should be invented**.
- Frozen Stable 9/D-126 remains last user-designated stable checkpoint. ASO-01, privacy, Play listing, timing/audio/FGS and all data unchanged.

## Relevant source
Android official adaptive icons and mask guidance: https://developer.android.com/develop/ui/views/launch/icon_design_adaptive
