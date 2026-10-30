# D-135 — Vitruvian Clock launcher icon in APK and on Android home screen (2026-10-30)

## Reported issue and verified root cause
- User supplied Samsung build screenshot **D-134 / versionCode28 BUILD SUCCESSFUL** and explicitly said **"הכל עובד"**; current user report confirms D-134 works. The user's subsequent screenshot/report: the **APK and the installed Android app still show the generic system icon**, although the approved clock artwork appears in About.
- Source inspection proved: the Android manifest had `android:label="Atelier Time"` but **no `android:icon` and no `android:roundIcon`** attributes. Therefore launcher/package manager could not locate an explicit icon for the app.
- Approved image already bundled at `android-native/app/src/main/res/drawable-nodpi/atelier_vitruvian_clock.webp` (320×320 WebP; originally extracted from the user's approved large visual). Reuse EXACT original image, with NO illustration regeneration, remote fetch, redesign or app theme change.

## D-135 source changes
- `versionName = "D-135"`, `versionCode = 29`; Android SDK 36 unchanged.
- `AndroidManifest.xml` application now defines `android:icon="@mipmap/ic_launcher"` and `android:roundIcon="@mipmap/ic_launcher_round"` so Android package installer, launcher, settings/app listing and other surfaces can resolve an actual app icon.
- `res/mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml`: adaptive icon with dedicated foreground and parchment background to support different OEM launcher masks (minSdk 26, so Android 8+ supported).
- `res/drawable/atelier_launcher_foreground.xml`: wraps the already bundled approved `atelier_vitruvian_clock.webp` in an 18dp inset per side of the 108dp icon layer to reduce risk of clipping 12/9/3/6 under masks.
- `res/drawable/atelier_launcher_background.xml`: solid parchment background only; **approved artwork content is unchanged**.
- No new file/permission/network use at runtime, and no change to About icon, other About cards, timer, FGS, audio, Saved Classes, Sounds or Run reports.
- This fixes launcher-icon **wiring**. Actual appearance, manufacturer mask and tinting must be visually checked after build and install; do not claim the screenshot/installed-icon check is PASS before it happens.

## Build/release gates
- SOURCE-ONLY in GitHub development branch; **D-135 not locally compiled, installed or user QA-verified** at documentation time.
- Local build ONLY in Samsung Code on the Go from `android-native`: `git pull --ff-only && bash tools/build_d135_launcher_icon_on_phone.sh`.
- Build helper checks branch, clean source, D-135/code29/API36, manifest launcher attributes, adaptive XML layers, original bundled artwork and the prior D-134 diagnostics/WhatsApp regression guards. Calls only `:app:assembleDebug`, saves APK/log/SHA256 manifest in Downloads. **No install, tests, paid GitHub Actions or cloud Android build**.
- After BUILD PASS: request permission for **in-place installation** over D-134; never uninstall or clear application data. Verify launcher icon on **home screen, app drawer and Android package installation screen**; verify full approved clock image (especially 12/9/3/6 and Vitruvian figure) under Samsung mask and on-screen small size; check About remains unchanged and Saved Classes/Sounds/Run reports persist.
- Some launchers may cache an old shortcut icon: if a pinned shortcut still shows old artwork after an in-place update, inspect the app drawer first, then re-add the home-screen shortcut **without uninstalling the app**.
- **D-134/code28 is the latest user-reported functional installed Android candidate with outstanding generic-icon defect.** The last explicitly designated frozen Stable checkpoint remains **Stable 9/D-126** (historical 285/285 tests PASS); do not label D-135 or D-134 Stable 10 without express user decision.

## Official Android implementation guidance
- Android adaptive icon documentation: https://developer.android.com/develop/ui/compose/system/icon_design_adaptive (foreground/background, safe zone, `android:icon` and `roundIcon` manifest entries).
