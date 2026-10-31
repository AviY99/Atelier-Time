> **ATELIER TIME STABLE 11 / D-140 (2026-10-31, PRE-PLAY CHECKPOINT):** User-confirmed working Android app and approved intro art are frozen at `stable/android-d140-qa11-user-accepted` (`b58634e439b16bac3f45010d64a23003b7f69777`), code34/API36. Full original PNG saved byte-identically in Android GitHub source; no phone download dependency. Phone Git local `84f1afb` requires safe guarded rebase with backup; no force/reset or new credentials. The application is not yet submitted to Google Play. [Stable 11 certificate](../docs/STABLE11_D140_USER_ACCEPTANCE_AND_GIT_RECONCILIATION_2026_10_31.md).

# Atelier Time — versioned visual artwork

This directory is the source/artwork inventory for **Atelier Time**. Source ownership and hashes are tracked separately from application runtime assets.

**Approved runtime images belong in `android-native/app/src/main/res/` or `android-native/app/src/main/assets/` and must be committed to GitHub. Never make Android builds depend on random files in Samsung Downloads.** No image in this archive modifies frozen Stable 10.

- Approved D-139/D-140 startup portrait (1080 × 2340, original PNG, exact SHA-256 `c7d69a52c53a86738cc416b4c29eb621f8792cf367539732ce0ffa3639d8460d`): `android-native/app/src/main/assets/atelier_splash_approved_d139.png`. **VERIFIED IN GITHUB (2026-10-31):** The user's separately uploaded `archive-d139-artwork` branch held byte-identical PNG `Atelier_Time_Splash_User_Artwork_Preview-2.png` (Git blob `8950a9c509bbc2c2bac02d18c0b9bfdfca06eabc`; 2,903,002 bytes), identical to approved SHA-256. Its exact blob has now been installed in the Android development branch at the permanent runtime filename `atelier_splash_approved_d139.png`, without recompression or resizing. The extra uploaded branch copy may be retained as a historical record; the working app reads the fixed runtime path.
- Original supplied sepia Vitruvian clock drawing (1172 × 1536, exact JPG SHA-256 `70cb814352ff42eacb74bcfb4a853ede73204cc0dafbbc40ebb73699a15a8653`): archive path `artwork/sources/vitruvian_clock_user_source_2026-10-31.jpg`. Optional original, not part of the APK. May only be committed after exact hash check; it is **not yet present remotely** as of this document update.
- D-138 accepted launcher icon: `android-native/app/src/main/res/drawable-nodpi/atelier_vitruvian_launcher_d138.webp`.
- Original About/clock art: `android-native/app/src/main/res/drawable-nodpi/atelier_vitruvian_clock.webp`.
- Earlier D-137 icon resource: `android-native/app/src/main/res/drawable-nodpi/atelier_vitruvian_launcher_clean.webp` (historical; not a new asset dependency).
- Instagram avatar: `android-native/app/src/main/res/drawable-nodpi/instagram_profile_avatar.webp`.
- WhatsApp and status notification icons are vector XML in `android-native/app/src/main/res/drawable/`.
- The app's included cue tones are generated in `MelodyAudio.kt`. User-selected file/system sounds are private user preferences and **must not** be committed.
- Future Google Play artwork (store icon, screenshots, feature graphic) belongs under `artwork/play-store/` when created, with approval and SHA/date recorded here.

## Rules

1. Each final graphic gets a source path, dimensions, SHA-256 and approval record.
2. Native app assets are version-controlled under Android source; the archive links to them instead of duplicating multi-megabyte binaries.
3. Source/original work is optional to app runtime but must be archived intact once available, without recompression or artistic edits.
4. Git must exclude build outputs, keys, logs, private user classes, report archives and personal audio files.
5. During a local phone build, run `bash tools/audit_artwork_on_phone.sh` from `android-native` after the image recovery helper; it stops if the mandatory approved splash image is absent or hash-mismatched.
6. The frozen Stable 10 branch remains at `6497db700587ac8f24a502137f085f52703b3524`.
