# D-138 — approved tighter-cropped Vitruvian launcher icon (2026-10-31)

## Approval
User inspected two before-install previews and approved the second, tighter crop ("מצויין, התקדם הלאה"). The clock ring is extremely close to the image bounds; all 12/3/6/9 remain visible. This change is **not** a new request for artwork approval.

## Scope (Android development branch only)
- Set `versionName = "D-138"`, `versionCode = 32`, retain API 36, application ID and all other app behavior.
- Commit user-approved tighter-cropped launcher image as `app/src/main/res/drawable-nodpi/atelier_vitruvian_launcher_d138.webp` (256px transparent WebP; SHA-256 `29e99fbbb17004e64b2f122df03c18d2d9c5502a533e939884c01f0771cdd66b`). Artwork comes from the approved local preview and is resized for launcher use.
- Keep adaptive foreground inset at 17dp to leave room for the system mask, while updating the foreground and both legacy fallbacks to the new crop. The original About image, previous D-137 resource, and app design/data are unchanged.
- Android launcher mask, Galaxy One UI and My Files can render differently. **Actual installed rendering still requires user screenshot acceptance**; a preview is not device QA.

## Mandatory Samsung Code on the Go flow
From the existing `Atelier-Time/android-native` checkout in **Samsung Code on the Go** (not Termux), run:

```bash
git pull --ff-only && bash tools/build_d138_approved_icon_on_phone.sh
```

The script checks branch `android-native-kotlin-compose`, clean tracked sources, API 36, icon SHA, manifest and launcher mappings. Local Gradle 9.6.1 / ARM64 AAPT2 assembles a debug APK only. Output: `/storage/emulated/0/Download/Atelier-Time-D138-API36-Candidate.apk`, local build log and SHA manifest. **No install, data clear, instrumentation, GitHub Actions, or cloud compilation.**

After the actual phone BUILD SUCCESSFUL and separately approved in-place upgrade, inspect Home/app drawer and fresh D-138 APK thumbnail / installer: crop near edge, numerals intact, no nested white border; confirm Saved Classes, Sounds and diagnostics preserved. If the Samsung mask crops too far, adjust launcher-only assets in D-139 after fresh screenshots.

Stable 9/D-126 checkpoint is frozen. D-134 remains last explicitly user-accepted working app. D-138 is source prepared only; **not locally compiled, installed, device-tested, declared stable or Play-ready**.
