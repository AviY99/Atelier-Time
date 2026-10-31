#!/usr/bin/env bash
# D-140: local Code on the Go build for corrected white Android splash and palette-matched navigation bars.
# No installation, instrumentation, data modification, GitHub Actions or push.
set -euo pipefail
cd "$(dirname "$0")/.."
if [[ "$(git branch --show-current)" != "android-native-kotlin-compose" ]]; then
  echo "STOP: Android development branch required." >&2; exit 2
fi
if [[ -n "$(git status --porcelain --untracked-files=no)" ]] ||
   [[ -n "$(git ls-files --others --exclude-standard -- app/src tools)" ]]; then
  echo "STOP: local source changes detected; inspect first." >&2
  git status --short
  exit 2
fi
config="app/build.gradle.kts"
for expected in 'compileSdk = 36' 'targetSdk = 36' 'versionCode = 34' 'versionName = "D-140"'; do
  if ! grep -Fq "$expected" "$config"; then
    echo "STOP: expected D-140/API 36 configuration missing: $expected" >&2; exit 2
  fi
done
for image in "app/src/main/res/drawable-nodpi/instagram_profile_avatar.webp" "app/src/main/res/drawable-nodpi/atelier_vitruvian_clock.webp" "app/src/main/res/drawable-nodpi/atelier_vitruvian_launcher_d138.webp" "app/src/main/res/drawable/ic_whatsapp_share.xml"; do
  if [[ ! -s "$image" ]]; then
    echo "STOP: bundled image is missing: $image" >&2; exit 2
  fi
done
# Gate exact interim share and reliable WhatsApp link + Android fallback.
source_file="app/src/main/java/com/ateliertime/android/AtelierDesign.kt"
for phrase in 'Follow the developer on Instagram:' 'https://wa.me/?text=' 'Intent.createChooser(send, "Share Atelier Time")' 'onClick = { shareAtelierTimeOnWhatsApp(context) }'; do
  if ! grep -Fq "$phrase" "$source_file"; then
    echo "STOP: D-140 WhatsApp intent/fallback/card click missing: $phrase" >&2; exit 2
  fi
done
# D-140 scope safeguards: list is scrollable and bulk deletion
# cannot proceed without an explicit in-app confirmation dialog.
archive_file="app/src/main/java/com/ateliertime/android/RunDiagnostics.kt"
library_file="app/src/main/java/com/ateliertime/android/RunDiagnosticsLibrary.kt"
for rule in 'fun deleteAll(): Int' 'val savedReports = reports()'; do
  if ! grep -Fq "$rule" "$archive_file"; then
    echo "STOP: D-140 scoped diagnostics deletion missing: $rule" >&2; exit 2
  fi
done
for rule in 'LazyColumn(' 'heightIn(max = 280.dp)' 'pendingDeleteAll' 'Delete all reports'; do
  if ! grep -Fq "$rule" "$library_file"; then
    echo "STOP: D-140 diagnostics list/confirmation missing: $rule" >&2; exit 2
  fi
done
# STOP if the APK could still receive the Android generic application icon.
manifest="app/src/main/AndroidManifest.xml"
for required in 'android:icon="@mipmap/ic_launcher"' 'android:roundIcon="@mipmap/ic_launcher_round"'; do
  if ! grep -Fq "$required" "$manifest"; then
    echo "STOP: launcher icon/roundIcon missing from AndroidManifest.xml: $required" >&2
    exit 2
  fi
done
for resource in \
    "app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml" \
    "app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml" \
    "app/src/main/res/drawable/atelier_launcher_foreground.xml" \
    "app/src/main/res/drawable/atelier_launcher_background.xml" \
    "app/src/main/res/drawable-nodpi/atelier_vitruvian_launcher_d138.webp" \
    "app/src/main/res/mipmap-anydpi/ic_launcher.xml" \
    "app/src/main/res/mipmap-anydpi/ic_launcher_round.xml"; do
  if [[ ! -s "$resource" ]]; then
    echo "STOP: approved Vitruvian launcher resource missing: $resource" >&2
    exit 2
  fi
done
for resource in \
    "app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml" \
    "app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml"; do
  if ! grep -Fq '@drawable/atelier_launcher_foreground' "$resource" ||
      ! grep -Fq '@drawable/atelier_launcher_background' "$resource"; then
    echo "STOP: adaptive icon layers missing or modified: $resource" >&2
    exit 2
  fi
done
if ! grep -Fq '@drawable/atelier_vitruvian_launcher_d138' "app/src/main/res/drawable/atelier_launcher_foreground.xml"; then
  echo "STOP: approved artwork not referenced by launcher icon foreground." >&2
  exit 2
fi
# D-140 keeps the 17dp adaptive inset while the cropped artwork fills the image.
# D-136 enlarged too far and cut off the clock ring and numerals.
if ! grep -Fq 'android:inset="17dp"' "app/src/main/res/drawable/atelier_launcher_foreground.xml"; then
  echo "STOP: D-140 adaptive foreground (17dp) is not in source." >&2
  exit 2
fi
# Exact SHA-256 of the approved 256px WebP (cropped from the user-approved preview).
expected_icon_sha="29e99fbbb17004e64b2f122df03c18d2d9c5502a533e939884c01f0771cdd66b"
actual_icon_sha="$(sha256sum "app/src/main/res/drawable-nodpi/atelier_vitruvian_launcher_d138.webp" | awk '{print $1}')"
if [[ "$actual_icon_sha" != "$expected_icon_sha" ]]; then
  echo "STOP: D-140 approved icon digest mismatch." >&2; exit 2
fi
# Cross-check nonadaptive fallback icon sources.
for fallback in app/src/main/res/mipmap-anydpi/ic_launcher.xml app/src/main/res/mipmap-anydpi/ic_launcher_round.xml; do
  if ! grep -Fq '@drawable/atelier_vitruvian_launcher_d138' "$fallback"; then
    echo "STOP: fallback icon still uses old white artwork: $fallback" >&2; exit 2
  fi
done
# Gate regression: Android's first system splash is white (never parchment).
for style in app/src/main/res/values/atelier_splash_styles.xml app/src/main/res/values-v31/atelier_splash_styles.xml; do
  if ! grep -Fq '@android:color/white' "$style"; then
    echo "STOP: first Android system splash must remain white in $style" >&2; exit 2
  fi
done
# D-140 must follow selected/auto/preview palette instead of a fixed splash tone.
editor_file="app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt"
for required in 'LaunchedEffect(activity, designPalette)' '0xFFF7F4ED' '0xFF304D70' 'isAppearanceLightNavigationBars = day' 'isNavigationBarContrastEnforced = false'; do
  if ! grep -Fq "$required" "$editor_file"; then
    echo "STOP: palette navigation-bar synchronization missing: $required" >&2; exit 2
  fi
done
# D-140: accept ONLY the exact artwork approved in this conversation.
# The image is transiently included in APK assets; source Git remains clean.
downloads="/storage/emulated/0/Download"
source_art="$downloads/Atelier_Time_Splash_User_Artwork_Preview.png"
approved_art_sha="c7d69a52c53a86738cc416b4c29eb621f8792cf367539732ce0ffa3639d8460d"
if [[ ! -s "$source_art" ]]; then
  echo "STOP: approved splash preview is missing from $source_art" >&2
  echo "Save the original approved 1080x2340 PNG in Download under that exact filename, then rerun." >&2
  exit 2
fi
actual_art_sha="$(sha256sum "$source_art" | awk '{print $1}')"
if [[ "$actual_art_sha" != "$approved_art_sha" ]]; then
  echo "STOP: supplied art does not match the exact user-approved PNG (SHA-256 mismatch)." >&2
  exit 2
fi
if ! grep -Fq 'Theme.AtelierTime.Launch' app/src/main/AndroidManifest.xml ||
   ! grep -Fq 'android:windowSplashScreenAnimatedIcon' app/src/main/res/values-v31/atelier_splash_styles.xml ||
   ! grep -Fq 'atelier_splash_approved_d139.png' app/src/main/java/com/ateliertime/android/MainActivity.kt; then
  echo "STOP: splash screen source/theme is incomplete." >&2
  exit 2
fi
asset_dir="app/src/main/assets"
asset_path="$asset_dir/atelier_splash_approved_d139.png"
if [[ -e "$asset_path" ]]; then
  echo "STOP: temporary splash asset already exists; inspect local source to avoid overwriting." >&2
  exit 2
fi
created_asset_directory=false
if [[ ! -d "$asset_dir" ]]; then
  mkdir -p "$asset_dir"
  created_asset_directory=true
fi
cleanup_approved_asset() {
  rm -f "$asset_path"
  if [[ "$created_asset_directory" == "true" ]]; then rmdir "$asset_dir" 2>/dev/null || true; fi
}
trap cleanup_approved_asset EXIT
cp "$source_art" "$asset_path"
: "${ANDROID_HOME:?Samsung Code on the Go Android SDK is required}"
gradle="$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle"
aapt2="$ANDROID_HOME/build-tools/36.0.0/aapt2"
if [[ ! -f "$gradle" || ! -s "$aapt2" ]]; then
  echo "STOP: existing Gradle 9.6.1 and ARM64 AAPT2 toolchain required." >&2; exit 2
fi
downloads="/storage/emulated/0/Download"
mkdir -p "$downloads"
log="$downloads/Atelier-Time-D140-API36-Build.log"
echo "D-140 / targetSdk 36: building debug APK locally, NO INSTALL / NO TEST RUN."
echo "No GitHub Actions, no Git push, no user-data or stable-branch changes."
echo "Approved full-frame artwork SHA-256 verified and included in this APK only."
if ! bash "$gradle" "-Pandroid.aapt2FromMavenOverride=$aapt2" \
    :app:assembleDebug --console=plain --no-daemon > "$log" 2>&1; then
  echo "BUILD FAILED: relevant compiler lines from the local log:"
  awk '/(^|[[:space:]])e: |(^|[[:space:]])error: |\.kt:[0-9]+|^FAILURE:|^BUILD FAILED|Execution failed for task/ { if (n++ < 35) print }' "$log"
  echo "Saved full log: $log"
  exit 1
fi
apk_dir="app/build/outputs/apk/debug"
mapfile -t apks < <(find "$apk_dir" -type f -name '*.apk')
if [[ "${#apks[@]}" -ne 1 ]]; then
  echo "STOP: expected one debug APK; found ${#apks[@]}." >&2; exit 3
fi
out="$downloads/Atelier-Time-D140-API36-Candidate.apk"
cp "${apks[0]}" "$out"
{
  printf 'version=D-140\ncode=34\ntargetSdk=36\n'
  printf 'branch=android-native-kotlin-compose\n'
  printf 'source_commit=%s\n' "$(git rev-parse HEAD)"
  printf 'app_sha256=%s\n' "$(sha256sum "$out" | awk '{print $1}')"
  printf 'artwork_sha256=c7d69a52c53a86738cc416b4c29eb621f8792cf367539732ce0ffa3639d8460d\n'
  printf 'state=COMPILED_ONLY; NOT_INSTALLED; NOT_DEVICE_TESTED\n'
} > "$downloads/Atelier-Time-D140-API36-Manifest.txt"
echo "BUILD SUCCESSFUL — D-140 debug APK saved: $out"
echo "Manifest: $downloads/Atelier-Time-D140-API36-Manifest.txt"
echo "No APK installed, no data touched, no prior QA rerun."
