#!/usr/bin/env bash
# Build/sign release AAB for frozen Stable17 D157 Code51, locally on Samsung.
# Never upload secrets to GitHub or use paid cloud builds. Does NOT publish/install.
set -euo pipefail
cd "$(dirname "$0")/.."
fail() { echo "STOP: $*" >&2; exit 2; }
[[ "$(git branch --show-current)" == "android-native-kotlin-compose" ]] || fail "Wrong branch."
expected_app="86499958484781c0712004f18bc2d9b593800e44"
[[ "$(git rev-parse HEAD:android-native/app 2>/dev/null || true)" == "$expected_app" ]] || fail "App source differs from frozen Stable17."
[[ -z "$(git status --porcelain --untracked-files=no)" ]] || fail "Tracked changes in worktree; inspect before proceeding."
for value in 'applicationId = "com.ateliertime.android"' 'compileSdk = 36' 'targetSdk = 36' 'versionCode = 51' 'versionName = "D-157"'; do
  grep -Fq "$value" app/build.gradle.kts || fail "Missing Gradle setting $value"
done
art="app/src/main/assets/atelier_splash_approved_d139.png"
[[ -f "$art" && ! -L "$art" ]] || fail "Original splash not found."
[[ "$(git hash-object "$art")" == "8950a9c509bbc2c2bac02d18c0b9bfdfca06eabc" ]] || fail "Splash differs from approved Git art."
[[ -n "$ANDROID_HOME" ]] || fail "ANDROID_HOME not set."
gradle="$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle"
aapt2="$ANDROID_HOME/build-tools/36.0.0/aapt2"
[[ -f "$gradle" && -s "$aapt2" ]] || fail "Gradle or aapt2 not found."
command -v jarsigner >/dev/null || fail "Missing jarsigner."
command -v python3 >/dev/null || fail "Missing python3."
key="$HOME/.atelier-time-signing/atelier-upload.p12"
[[ -s "$key" && ! -L "$key" ]] || fail "Upload Key not found."
dest="/storage/emulated/0/Download"
[[ -d "$dest" ]] || fail "Downloads not found."
out="$dest/Atelier-Time-D157-Code51-InternalTest-SIGNED.aab"
[[ ! -e "$out" ]] || fail "Output already exists. Will not overwrite."
umask 077
log="$dest/Atelier-Time-D157-Code51-ReleaseBuild-$(date +%Y%m%d-%H%M%S).log"
echo "D157 Code51 exact frozen app source verified. Building release AAB locally..."
if ! bash "$gradle" "-Pandroid.aapt2FromMavenOverride=$aapt2" :app:bundleRelease --no-daemon --console=plain >"$log" 2>&1; then
  echo "GRADLE RELEASE FAILED. Errors:"
  grep -E '(^| )e: |(^| )error: |\.kt:[0-9]+|^FAILURE:|^BUILD FAILED|Execution failed for task|FAILED' "$log" | head -35 || true
  echo "Build log: $log"
  exit 3
fi
unsigned="app/build/outputs/bundle/release/app-release.aab"
[[ -s "$unsigned" ]] || fail "Missing unsigned release AAB."
python3 - "$unsigned" "$art" <<'PY'
import hashlib, pathlib, sys, zipfile
bundle, art = sys.argv[1:]
expected = "c7d69a52c53a86738cc416b4c29eb621f8792cf367539732ce0ffa3639d8460d"
if hashlib.sha256(pathlib.Path(art).read_bytes()).hexdigest() != expected:
    raise SystemExit("STOP: original approved splash SHA mismatch.")
with zipfile.ZipFile(bundle) as z:
    asset = "base/assets/atelier_splash_approved_d139.png"
    if asset not in z.namelist() or hashlib.sha256(z.read(asset)).hexdigest() != expected:
        raise SystemExit("STOP: approved splash not present byte-identically in AAB.")
print("AAB ARTWORK INCLUDED PASS")
PY
echo "Enter your private keystore password when prompted. Never send it in chat."
if ! jarsigner -keystore "$key" -storetype PKCS12 -sigalg SHA256withRSA -digestalg SHA-256 -signedjar "$out" "$unsigned" atelier-upload; then
  fail "Signing failed. DO NOT UPLOAD partial output."
fi
[[ -s "$out" ]] || fail "Signed AAB missing."
verifylog="$dest/Atelier-Time-D157-Code51-AAB-Signature-Verify.log"
jarsigner -verify -verbose "$out" >"$verifylog" 2>&1 || fail "Signature verification failed. DO NOT UPLOAD."
grep -qi 'jar verified' "$verifylog" || fail "Signature verification message missing. DO NOT UPLOAD."
python3 - "$out" <<'PY'
import sys, zipfile
with zipfile.ZipFile(sys.argv[1]) as z:
    names = z.namelist()
    if "base/manifest/AndroidManifest.xml" not in names:
        raise SystemExit("STOP: missing base manifest.")
    if not any(p.upper().startswith("META-INF/") and p.upper().endswith(".SF") for p in names):
        raise SystemExit("STOP: signed AAB missing JAR signature entries.")
print("AAB STRUCTURE AND SIGNATURE ENTRIES PASS")
PY
echo "SIGNED AAB READY FOR REVIEW — NOT UPLOADED: $out"
sha256sum "$out"
ls -lh "$out"
echo "Build log: $log"
echo "Signature verification log: $verifylog"
echo "No install, app data clear, cloud build, signing secret upload, or Play submission."
