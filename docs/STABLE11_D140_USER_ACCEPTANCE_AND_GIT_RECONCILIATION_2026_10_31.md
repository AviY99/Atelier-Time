# Stable 11 — D-140: user-accepted release-candidate checkpoint

**Date:** 2026-10-31. **Acceptance:** user reports “הכל תקין בגרסה 140” and then explicitly authorizes freezing Stable 11 and Git synchronization (“בצע 1 ו 2”).

## Exact stable identification
- Android version **D-140**, `versionName="D-140"`, `versionCode=34`, `compileSdk=36`, `targetSdk=36`; package `com.ateliertime.android`.
- Frozen source branch `stable/android-d140-qa11-user-accepted` at **`b58634e439b16bac3f45010d64a23003b7f69777`**. It contains the accepted D-140 code and **exact user-approved portrait PNG** in `android-native/app/src/main/assets/atelier_splash_approved_d139.png`.
- Original image SHA-256: `c7d69a52c53a86738cc416b4c29eb621f8792cf367539732ce0ffa3639d8460d`; verified Git blob `8950a9c509bbc2c2bac02d18c0b9bfdfca06eabc`, 2,903,002 bytes. User uploaded on `archive-d139-artwork`; verified exact bytes were committed in the correct Android resource path; no rendering, resize or recompression.
- Do not advance the frozen Stable 11 branch; future changes go to D-141+ on Android development. Frozen Stable10/D138 at `6497db700587ac8f24a502137f085f52703b3524` remains unchanged.

## Evidence and QA limits
- The Samsung **Code on the Go** screenshot confirms `ARTWORK AUDIT PASS`, SHA verification and **`BUILD SUCCESSFUL — D-140`**, producing `/storage/emulated/0/Download/Atelier-Time-D140-API36-Candidate.apk` and local manifest.
- The user confirms “הכל תקין בגרסה 140” after installation and testing. Accepted design includes a **white Android system icon-only splash**, the **approved full-portrait Atelier/Time image**, and navigation-bar backing that follows **Day/Night** rather than retaining parchment color. Retain user-approved timer, Sounds, Saved Classes, About, diagnostic history and background behavior.
- This is **user/manual device acceptance**, not proof that fresh D-140 Android instrumentation or JVM regression suites ran. Historical **285/285 PASS is for Stable9/D126 only**. No signed release AAB, Play Console review, or production publishing is claimed.

## Step 1: synchronize phone-local Git (PENDING until user executes)
- On the phone, a separately created local commit `84f1afb` included the exact PNG. Its `git push` failed due to GitHub password authentication, while the connected GitHub source later received the **same exact image** in commit `b58634e439b16bac3f45010d64a23003b7f69777` (plus docs). Both commits descend from the same earlier Android developer head; histories differ even though approved image bytes are identical.
- **Remote repository art/source is already synchronized. The phone's local Git history is not yet reconciled.** We cannot run a Git command in Samsung Code on the Go from the GitHub connector, so require a single explicit phone-side read/rebase.
- Before rebasing: fetch only (no push/credentials), confirm correct branch, **clean tracked tree** (ignore untracked `../.cg/`), verify the same Git blob for PNG on local and remote, and create a **backup local branch** preserving original commit. A test using two branches adding byte-identical files showed Git automatically drops the duplicate commit when rebasing.
- Exact command from `Atelier-Time/android-native`:

```bash
git fetch origin android-native-kotlin-compose && \
test "$(git branch --show-current)" = android-native-kotlin-compose && \
test -z "$(git status --porcelain --untracked-files=no)" && \
test "$(git rev-parse HEAD:android-native/app/src/main/assets/atelier_splash_approved_d139.png)" = "$(git rev-parse origin/android-native-kotlin-compose:android-native/app/src/main/assets/atelier_splash_approved_d139.png)" && \
git branch "backup/d140-pre-sync-$(git rev-parse --short HEAD)" && \
git rebase origin/android-native-kotlin-compose && \
git status --short --branch
```

- If any guard or rebase fails, stop and share output, do **not** delete assets, use `git clean`, force push, reset, uninstall or clear app data. Rebase is Git history maintenance only; no build needed. Until confirmed, do not state phone Git is synchronized or use `git pull --ff-only` on its diverged history.
- Do not request passwords or tokens: GitHub remote asset archive has already been completed by the assistant's connected GitHub action. Local fetch previously worked in CoGo.
- No GitHub Actions or cloud compilation permitted. Phone build/test/install remain Samsung Code on the Go actions with separate user permission.

## Next steps
Google Play Developer account, ASO listings, artwork, privacy/Data Safety, foregroundService `specialUse` declaration, release signing, local AAB and required test tracks/review. This certificate is the frozen **pre-distribution stable checkpoint**, not a Play publication.
