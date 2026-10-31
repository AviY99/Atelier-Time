#!/usr/bin/env bash
# D-140 + pre-Play audit of Git source artwork, no network, no changes.
set -euo pipefail
cd "$(dirname "$0")/.."
expected="c7d69a52c53a86738cc416b4c29eb621f8792cf367539732ce0ffa3639d8460d"
expected_original="70cb814352ff42eacb74bcfb4a853ede73204cc0dafbbc40ebb73699a15a8653"
asset="app/src/main/assets/atelier_splash_approved_d139.png"
source_jpg="../artwork/sources/vitruvian_clock_user_source_2026-10-31.jpg"
if [[ ! -s "$asset" ]]; then
  echo "FAIL: approved startup art not included in Android project" >&2; exit 2
fi
actual="$(sha256sum "$asset" | awk '{print $1}')"
if [[ "$actual" != "$expected" ]]; then
  echo "FAIL: startup art differs from user-approved image" >&2; exit 2
fi
# If staged/committed it is no longer a transient untracked file.
if ! git ls-files --error-unmatch -- "$asset" >/dev/null 2>&1; then
  echo "FAIL: startup PNG is not tracked in local Git" >&2; exit 2
fi
for resource in \
  app/src/main/res/drawable-nodpi/atelier_vitruvian_clock.webp \
  app/src/main/res/drawable-nodpi/atelier_vitruvian_launcher_d138.webp \
  app/src/main/res/drawable-nodpi/instagram_profile_avatar.webp \
  app/src/main/res/drawable/ic_whatsapp_share.xml \
  app/src/main/res/drawable/ic_atelier_class_status.xml \
  app/src/main/res/drawable/atelier_splash_system_safe_icon.xml; do
  [[ -s "$resource" ]] || { echo "FAIL missing $resource" >&2; exit 2; }
  git ls-files --error-unmatch -- "$resource" >/dev/null || exit 2
done
if [[ -s "$source_jpg" ]]; then
  [[ "$(sha256sum "$source_jpg" | awk '{print $1}')" == "$expected_original" ]] ||
    { echo "FAIL: wrong archived source JPG" >&2; exit 2; }
  echo "OK: original artistic JPG archived locally and exact"
else
  echo "NOTE: original JPG archival optional, not required to build APK"
fi
echo "ARTWORK AUDIT PASS: required app images are tracked locally and approved SHA matches"
echo "Remote GitHub presence still requires successful push, not just a local commit"
