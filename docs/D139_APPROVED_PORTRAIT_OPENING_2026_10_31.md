# D-139 — user-approved full-frame opening artwork, separate Android system icon

**Date:** 2026-10-31. **Branch:** `android-native-kotlin-compose`. **Development version:** D-139 / Android versionCode 33 / targetSdk 36.  
**Status:** source implementation prepared; **not compiled, installed or device QA accepted**. Last stable remains user-frozen **Stable 10 / D-138**, commit `6497db700587ac8f24a502137f085f52703b3524`.

## Approved art — origin and exact identity

The user supplied a new sepia parchment Vitruvian clock drawing with 1–12 around the ring and Leonardo-style notes, rejected prior redrawn/blurred mockups, and **explicitly approved** the complete portrait-layout preview on 2026-10-31. Approved preview includes antique-styled **Atelier** above and **Time** below, with its original geometric square/circle fully visible, not clipped. **Use this exact image; do not regenerate or approximate the art.**

- Conversation artifact name: `Atelier_Time_Splash_User_Artwork_Preview.png`
- Original artwork is `1000099031.jpg` supplied by the user.
- Approved exact PNG dimensions: **1080×2340**, RGB, 2,903,002 bytes.
- **Required SHA-256:** `c7d69a52c53a86738cc416b4c29eb621f8792cf367539732ce0ffa3639d8460d`.
- Source image remains an approved conversation artifact, **NOT yet committed as a GitHub binary resource**. This is a material limitation: the project source alone cannot reproduce the approved art without the exact PNG. Never claim it is embedded in GitHub.
- The Samsung local-only build helper checks the file in Download by its exact SHA and temporarily places a copy into `app/src/main/assets/atelier_splash_approved_d139.png` for packaging. The source tree is cleaned by the EXIT trap after build. Do not run arbitrary APK build commands that bypass this verification.

## What the Android code changes

1. No alteration whatsoever to D-138 launcher icons, home grid or About image.
2. New separate Android 12+ platform system-splash `Theme.AtelierTime.Launch` uses color `#C8A77C` from the approved parchment image and conservative padded system icon to avoid chopping clock numerals under its mandatory circular mask.
3. Next, the `MainActivity` Compose **static full-screen portrait scene** shows the exact artwork with `ContentScale.Fit`, preserving the complete square and every numeral, for around 900 ms on a genuine cold Activity creation only (not rotation/recreation).
4. The existing `SessionEditorScreen()` is then displayed without modifying timer, notifications, service lifecycle, audio, saved classes, diagnostics, permissions, privacy or About.
5. Android itself still displays a brief non-removable icon-only splash before the custom full-frame picture. This is an OS platform limit; the full portrait picture cannot be placed directly into the icon-only Android system splash.

## Local build and acceptance gate

**First save the exact approved 1080×2340 PNG from the conversation into Samsung Downloads** under the exact basename `Atelier_Time_Splash_User_Artwork_Preview.png`; file hash must match the one above. No additional cropping or re-encoding is permitted.

From Samsung **Code on the Go**, in `Atelier-Time/android-native`:

```bash
git pull --ff-only && bash tools/build_d139_splash_on_phone.sh
```

Only `:app:assembleDebug` runs locally using existing Gradle/AAPT2. Output candidate: `Download/Atelier-Time-D139-API36-Candidate.apk` with log and SHA manifest. It does **not** auto-install, instrument, push, upload, clear data, or run GitHub Actions.

**Do not call D-139 accepted or Stage 9 complete until the user approves a separate, data-preserving in-place installation and confirms:** the first short Android splash no longer clips the ring/numerals; the full approved image including “Atelier” and “Time” follows without missing edges; the ordinary app appears normally; restart vs rotation behavior; Saved Classes/Sounds/diagnostic reports and existing FGS/audio/background rules remain intact. Preserve frozen Stable 10 unchanged.

## Next action for project integrity

The approved PNG should ideally be committed as a binary project asset before final release; current connector does not support direct transfer of conversation-local image bytes into GitHub. The current build gate is a secure, reproducible local workaround, not a claim that the GitHub repository already contains the PNG. Neither Play release nor any further stable freeze is authorized.
