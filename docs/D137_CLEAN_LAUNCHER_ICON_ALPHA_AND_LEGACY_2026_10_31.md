# D-137: Clean Vitruvian launcher alpha, unclipped clock and fallback (2026-10-31)

## User evidence / superseding D-136
Samsung screenshot: after D-136 build/install, home and installation surfaces crop the outer black clock ring and 12/3/6/9; Samsung My Files APK icon still renders the old white nested square. D-136 icon QA **FAIL**, despite the rest of the application not being reported broken.

## Root cause and source design
Original `app/src/main/res/drawable-nodpi/atelier_vitruvian_clock.webp` is 320x320 WebP with an opaque white matte beyond the parchment rounded-square image. D-136's -3dp inset expands the entire image beyond the Android adaptive mask. Official Android guidance specifies a 108dp foreground with a centered 66dp safe zone: https://developer.android.com/develop/ui/compose/system/icon_design_adaptive

## D-137 implementation: Android dev only
- `versionName=D-137`, `versionCode=31`, targetSdk36.
- Dedicated `app/src/main/res/drawable-nodpi/atelier_vitruvian_launcher_clean.webp`: 320x320 extended WebP VP8X+ALPH+VP8; original VP8 color art bytes preserved exactly, with a feathered rounded rectangle alpha mask around its parchment edge to hide the white external matte. Original About WebP is **untouched**.
- Adaptive foreground drawable references new cleaned WebP and uses a **17dp** inset, sized to preserve all clock numbers/complete outer ring within center safe zone; no hard square white framing.
- Additional `res/mipmap-anydpi/ic_launcher.xml` and `ic_launcher_round.xml` nonadaptive bitmap fallback reference the new cleaned image. The current `res/mipmap-anydpi-v26` adaptive icon layers, background and Manifest icon references are unchanged.
- White-background problem may also be affected by Samsung My Files thumbnail caching/third-party APK icon extraction. Do not claim the fallback guarantees refresh; use the **newly named D137 APK**, not previously cached D135/D136 filenames.
- No change to class timing, FGS, notifications, audio, About design/art, Saved Classes, Sounds, diagnostics, data, permissions, Play listing or privacy.

## Strict local build / QA gate
**D-137 prepared in GitHub source only. NOT compiled, installed or screenshot tested.**
1. Samsung Code on the Go, cwd `android-native`: `git pull --ff-only && bash tools/build_d137_clean_launcher_icon_on_phone.sh`.
2. Helper checks D137/code31/target36, clean resources/adaptive inset, old functional scope, then calls **only** `:app:assembleDebug`. Saves `Atelier-Time-D137-API36-Candidate.apk`, log and SHA256 manifest in Downloads. Never installs or clears data. No cloud Android build/GitHub Actions.
3. Following a real BUILD PASS, **separately request** approval for in-place install/update without app uninstall or data clear.
4. User visually checks app drawer/home, APK installer, My Files *D137* APK preview, all four numerals and ring visible, no nested white square, image pleasantly sized beside adjacent icons, About unchanged; Saved Classes/Sounds/diagnostic reports preserved.
5. If fail, stop and use evidence for next targeted asset revision. Store 512x512 icon, signed Play AAB, and Play approvals are separate.

## Stable and acceptance
Frozen Stable 9/D-126 unchanged. D-134 last explicitly confirmed fully working version; D-136 icon failed user screenshot acceptance. Do not call D-137 stable, PASS or Play-ready until genuine device evidence.
