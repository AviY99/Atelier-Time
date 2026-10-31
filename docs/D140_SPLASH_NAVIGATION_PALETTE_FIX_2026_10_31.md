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

**Implementation clarification (latest):** The persistence helper attempts an **unattended, non-interactive** `git push`. If GitHub credentials are unavailable, it emits a clear warning, keeps the exact file and commit in the user's local Git checkout, and permits the subsequent local D-140 build to proceed. Only a verified successful push means the source asset is actually in remote GitHub; screenshots/logs should be checked. No `GitHub Actions` workflows are present in the development branch's `.github/workflows` folder (no folder found). Do not treat missing upload as a failed Android compilation.


## D-140 media audit and Code on the Go untracked `.cg/` fix (2026-10-31)

Latest phone screenshot shows D-140 stopped BEFORE compilation at `STOP: working tree has changes`, printing only `?? ../.cg/`. That is the Samsung Code on the Go generated workspace directory, **not** a changed app resource. Do not delete/clean that directory. Earlier helper incorrectly rejected any untracked item anywhere. The revised helper checks tracked source changes and unexpected untracked items **only inside `android-native/app/src` and `android-native/tools`**; it ignores unrelated `.cg/` while preserving all files. The verified original user-approved splash is extracted from the existing D-139 APK and SHA checked; exact JPEG source is archived only if the original byte-identical JPG is already available in Download. It commits and attempts to push the runtime PNG to GitHub; a failed push is reported separately and local build remains possible. The app source/assets now have an inventory at `artwork/README.md` and a deterministic local check `android-native/tools/audit_artwork_on_phone.sh`. No user files or Samsung workspace files are ever deleted by the helper.

**Run in Samsung Code on the Go from `Atelier-Time/android-native`:**

```bash
git pull --ff-only && bash tools/persist_d139_splash_art_in_git_on_phone.sh && bash tools/audit_artwork_on_phone.sh && bash tools/build_d140_navbar_fix_on_phone.sh
```

The art audit verifies that the approved asset is **tracked in local Git**. Only a successful push (or separate GitHub verification) proves it is in remote GitHub. Build is offline/local, no GitHub Actions, no APK install, no data removal. D-140 is still not compiled or tested as of this document update; Stable 10 is frozen.


## Final visual-asset archive confirmation — 2026-10-31

The user uploaded the exact approved PNG to a separate GitHub branch `archive-d139-artwork`. Its browser upload name was `Atelier_Time_Splash_User_Artwork_Preview-2.png` (not the runtime path). Git tree returned **blob SHA `8950a9c509bbc2c2bac02d18c0b9bfdfca06eabc`**, **size 2,903,002 bytes**, which matches the original approved PNG's actual Git blob SHA and SHA-256 `c7d69a52c53a86738cc416b4c29eb621f8792cf367539732ce0ffa3639d8460d`. The exact existing GitHub blob was reused directly at `android-native/app/src/main/assets/atelier_splash_approved_d139.png` on development (no new pixels, conversion or image generation). As a result the Android source now contains the final runtime art and builds no longer depend on a file in Downloads. The user confirmed D-140 built and operates correctly, including navigation bar color correction.

**Local checkout caveat:** the phone's local branch includes an earlier independent commit `84f1afb` with the same file bytes; simply doing `git pull --ff-only` after this remote archive commit would reject divergent ancestry. Before any next update, a safe `git pull --rebase origin android-native-kotlin-compose` on a clean tracked working tree should recognize/drop the already-applied identical image change. Inspect Git status; do not delete the local image or force push. Never request user account password/token for this already-completed remote archive.


## D-140 post-test Stable 11 acceptance (2026-10-31)
The user reports D-140 works correctly after build and device testing; Stable 11 is now frozen at b58634e439b16bac3f45010d64a23003b7f69777. Its PNG exists in GitHub source; historical source-only/missing-asset claims above are superseded. The phone's local duplicate image-only commit must be safely rebased (see Stable 11 certificate); D-140's full automated test suite has not been evidenced.
