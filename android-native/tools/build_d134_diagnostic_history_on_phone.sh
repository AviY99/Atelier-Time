#!/usr/bin/env bash
# D-134 API36: local-only build for diagnostic report list of five and confirmed Delete all.
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
for expected in 'compileSdk = 36' 'targetSdk = 36' 'versionCode = 28' 'versionName = "D-134"'; do
  if ! grep -Fq "$expected" "$config"; then
    echo "STOP: expected D-134/API 36 configuration missing: $expected" >&2; exit 2
  fi
done
for image in "app/src/main/res/drawable-nodpi/instagram_profile_avatar.webp" "app/src/main/res/drawable-nodpi/atelier_vitruvian_clock.webp" "app/src/main/res/drawable/ic_whatsapp_share.xml"; do
  if [[ ! -s "$image" ]]; then
    echo "STOP: bundled image is missing: $image" >&2; exit 2
  fi
done
# Gate exact interim share and reliable WhatsApp link + Android fallback.
source_file="app/src/main/java/com/ateliertime/android/AtelierDesign.kt"
for phrase in 'Follow the developer on Instagram:' 'https://wa.me/?text=' 'Intent.createChooser(send, "Share Atelier Time")' 'onClick = { shareAtelierTimeOnWhatsApp(context) }'; do
  if ! grep -Fq "$phrase" "$source_file"; then
    echo "STOP: D-134 WhatsApp intent/fallback/card click missing: $phrase" >&2; exit 2
  fi
done
# D-134 source-only safeguards: list is scrollable and bulk deletion
# cannot proceed without an explicit in-app confirmation dialog.
archive_file="app/src/main/java/com/ateliertime/android/RunDiagnostics.kt"
library_file="app/src/main/java/com/ateliertime/android/RunDiagnosticsLibrary.kt"
for rule in 'fun deleteAll(): Int' 'val savedReports = reports()'; do
  if ! grep -Fq "$rule" "$archive_file"; then
    echo "STOP: D-134 scoped diagnostics deletion missing: $rule" >&2; exit 2
  fi
done
for rule in 'LazyColumn(' 'heightIn(max = 280.dp)' 'pendingDeleteAll' 'Delete all reports'; do
  if ! grep -Fq "$rule" "$library_file"; then
    echo "STOP: D-134 diagnostics list/confirmation missing: $rule" >&2; exit 2
  fi
done
: "${ANDROID_HOME:?Samsung Code on the Go Android SDK is required}"
gradle="$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle"
aapt2="$ANDROID_HOME/build-tools/36.0.0/aapt2"
if [[ ! -f "$gradle" || ! -s "$aapt2" ]]; then
  echo "STOP: existing Gradle 9.6.1 and ARM64 AAPT2 toolchain required." >&2; exit 2
fi
downloads="/storage/emulated/0/Download"
mkdir -p "$downloads"
log="$downloads/Atelier-Time-D134-API36-Build.log"
echo "D-134 / targetSdk 36: building debug APK locally, NO INSTALL / NO TEST RUN."
echo "No GitHub Actions, no Git push, no user-data or stable-branch changes."
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
out="$downloads/Atelier-Time-D134-API36-Candidate.apk"
cp "${apks[0]}" "$out"
{
  printf 'version=D-134\ncode=28\ntargetSdk=36\n'
  printf 'branch=android-native-kotlin-compose\n'
  printf 'source_commit=%s\n' "$(git rev-parse HEAD)"
  printf 'app_sha256=%s\n' "$(sha256sum "$out" | awk '{print $1}')"
  printf 'state=COMPILED_ONLY; NOT_INSTALLED; NOT_DEVICE_TESTED\n'
} > "$downloads/Atelier-Time-D134-API36-Manifest.txt"
echo "BUILD SUCCESSFUL — D-134 debug APK saved: $out"
echo "Manifest: $downloads/Atelier-Time-D134-API36-Manifest.txt"
echo "No APK installed, no data touched, no prior QA rerun."
