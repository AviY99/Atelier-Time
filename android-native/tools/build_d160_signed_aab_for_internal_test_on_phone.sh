#!/usr/bin/env bash
# Atelier Time Stable19 / D-160 / Code54 — guarded local Samsung release AAB.
# Adapted from Stable18 D158 helper. PREPARED ONLY: requires separate approval to run.
# PREPARED SCRIPT ONLY: running it requires separate owner approval.
# Builds/tests/signs on Samsung Code on the Go. No installation, upload or CI.
set -euo pipefail
cd "$(dirname "$0")/.."

fail() { echo "STOP: $*" >&2; exit 2; }
require_line() {
  grep -Fq -- "$2" "$1" || fail "Expected source declaration absent: $2 ($1)"
}
show_errors() {
  grep -E '(^| )e: |(^| )error: |\.kt:[0-9]+|^FAILURE:|^BUILD FAILED|Execution failed for task|FAILED' "$log" | head -60 || true
}

# An immutable app tree is more reliable than the moving DEV branch's HEAD.
expected_app_tree="717ea00fe4304cdcea4e53e04a1eaab0a378e559"
[[ "$(git branch --show-current)" == "android-native-kotlin-compose" ]] ||
  fail "Use the DEV branch, never a frozen stable branch."
[[ "$(git rev-parse HEAD:android-native/app 2>/dev/null || true)" == "$expected_app_tree" ]] ||
  fail "App tree differs from approved frozen Stable19 D160 / Code54."
# Never alter or auto-restore application source during a release build.
# Missing tracked assets must be reviewed separately, never silently replaced.
art="app/src/main/assets/atelier_splash_approved_d139.png"
expected_art_blob="8950a9c509bbc2c2bac02d18c0b9bfdfca06eabc"
[[ "$(git rev-parse "HEAD:android-native/$art" 2>/dev/null || true)" == "$expected_art_blob" ]] ||
  fail "Frozen Stable19 approved splash blob mismatch."
[[ -f "$art" && -s "$art" && ! -L "$art" ]] ||
  fail "Approved splash missing or invalid. No automatic restore."
icon="app/src/main/res/drawable-nodpi/atelier_vitruvian_launcher_d138.webp"
expected_icon_blob="84e9a0523693ba06e7634d1c5043455a0eb90d03"
[[ "$(git rev-parse "HEAD:android-native/$icon" 2>/dev/null || true)" == "$expected_icon_blob" ]] ||
  fail "Frozen Stable19 launcher icon blob mismatch."
[[ -f "$icon" && -s "$icon" && ! -L "$icon" ]] ||
  fail "Approved launcher icon missing or invalid."
[[ "$(git hash-object "$icon")" == "$expected_icon_blob" ]] ||
  fail "Launcher source differs from accepted artwork."

[[ -z "$(git status --porcelain --untracked-files=no)" ]] ||
  fail "Tracked working-tree/index changes exist. Inspect; do not auto-reset."
[[ -z "$(git ls-files --others --exclude-standard -- app/src app/build.gradle.kts)" ]] ||
  fail "Untracked app/src files could contaminate the release."

config="app/build.gradle.kts"
manifest="app/src/main/AndroidManifest.xml"
domain="app/src/main/java/com/ateliertime/domain/TimerNotificationStatus.kt"
service="app/src/main/java/com/ateliertime/android/ClassNotificationTaskService.kt"
for item in 'applicationId = "com.ateliertime.android"' \
  'versionCode = 54' 'versionName = "D-160"' \
  'compileSdk = 36' 'targetSdk = 36' 'minSdk = 26' \
  'applicationIdSuffix = ".dev"'; do
  require_line "$config" "$item"
done
for item in 'android.permission.POST_NOTIFICATIONS' \
  'android.permission.MODIFY_AUDIO_SETTINGS' \
  'android.permission.FOREGROUND_SERVICE' \
  'android.permission.FOREGROUND_SERVICE_SPECIAL_USE' \
  'android:foregroundServiceType="specialUse"' \
  'android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE' \
  'User-started Class and Free Timer countdown statuses'; do
  require_line "$manifest" "$item"
done
require_line "$domain" 'const val CLASS_ID = 107'
require_line "$domain" 'const val FREE_ID = 109'
require_line "$service" 'ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE'

[[ -f "$art" && -s "$art" && ! -L "$art" ]] ||
  fail "Approved splash missing, empty or symlinked."
[[ "$(git hash-object "$art")" == "$expected_art_blob" ]] ||
  fail "Approved splash differs from frozen Stable19; refusing to overwrite."
[[ -n "$(printenv ANDROID_HOME 2>/dev/null || true)" ]] || fail "ANDROID_HOME missing."
gradle="$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle"
aapt2="$ANDROID_HOME/build-tools/36.0.0/aapt2"
[[ -f "$gradle" && -s "$aapt2" ]] || fail "CoGo Gradle 9.6.1 / SDK36 AAPT2 missing."
for cmd in git python3 jarsigner keytool sha256sum; do
  command -v "$cmd" >/dev/null || fail "Missing local command: $cmd"
done
key="$HOME/.atelier-time-signing/atelier-upload.p12"
[[ -f "$key" && -s "$key" && ! -L "$key" ]] ||
  fail "Original local upload key unavailable. Do NOT create or upload a new key."
# Ask ONCE, immediately BEFORE running tests. The secret stays in this
# bash process's memory (not exported, not written to disk, not sent to GitHub).
# Do not let shell tracing expose a private password if the caller enabled it.
set +x
if [[ ! -r /dev/tty ]] || ! ( : </dev/tty ) 2>/dev/null; then
  fail "Interactive Samsung terminal unavailable; cannot safely read upload password."
fi
printf 'Upload key password (hidden, then press Enter): ' >/dev/tty
if ! IFS= read -r -s signing_password </dev/tty; then
  printf '\n' >/dev/tty
  fail "Password input failed; stopping before build."
fi
printf '\n' >/dev/tty
[[ -n "$signing_password" ]] ||
  fail "Empty password; stopping before build."
echo "Upload key password received privately. Continuing tests and build."

dest="/storage/emulated/0/Download"
[[ -d "$dest" ]] || fail "Samsung Downloads directory missing."
out="$dest/Atelier-Time-D160-Code54-InternalTest-SIGNED.aab"
[[ ! -e "$out" && ! -L "$out" ]] ||
  fail "Signed output already exists. Do not overwrite."
umask 077
stamp="$(date +%Y%m%d-%H%M%S)-$$"
log="$dest/Atelier-Time-D160-Code54-ReleaseBuild-$stamp.log"
verifylog="$dest/Atelier-Time-D160-Code54-Signature-$stamp.log"
certlog="$dest/Atelier-Time-D160-Code54-UploadCertificate-$stamp.log"
partial="$dest/.Atelier-Time-D160-Code54-InternalTest-$stamp.partial.aab"
cleanup() {
  # Erase the shell variable on exit; only remove this run's temporary AAB.
  unset signing_password
  if [[ -n "$partial" && -e "$partial" ]]; then rm -f -- "$partial"; fi
}
trap cleanup EXIT

echo "Frozen Stable19 D160 app tree verified; no app source changes or Play upload."
echo "Run D160 JVM tests anew, then COMPILE (not execute) AndroidTest and build release AAB."
if ! bash "$gradle" "-Pandroid.aapt2FromMavenOverride=$aapt2" \
  :app:testDebugUnitTest --rerun-tasks --no-daemon --console=plain >"$log" 2>&1; then
  show_errors
  fail "JVM tests failed; inspect private log: $log"
fi
python3 - app/build/test-results/testDebugUnitTest <<'PY_TEST'
import pathlib, sys, xml.etree.ElementTree as ET
totals = dict.fromkeys(("tests", "failures", "errors", "skipped"), 0)
classes = {}
for path in pathlib.Path(sys.argv[1]).glob("TEST-*.xml"):
    root = ET.parse(path).getroot()
    suites = [root] if root.tag == "testsuite" else root.findall(".//testsuite")
    for suite in suites:
        counts = {name: int(suite.attrib.get(name, "0")) for name in totals}
        classes[suite.attrib.get("name", "")] = counts
        for name in totals:
            totals[name] += counts[name]
print("D160 JVM ACTUAL:", totals)
background = classes.get("com.ateliertime.domain.D158BackgroundNotificationsTest")
print("D160 INHERITED D158 BACKGROUND REGRESSIONS:", background)
if totals["tests"] < 326 or any(totals[k] for k in ("failures", "errors", "skipped")):
    raise SystemExit("STOP: JVM suite incomplete, failing or skipped.")
if not background or background["tests"] < 8 or any(background[k] for k in ("failures", "errors", "skipped")):
    raise SystemExit("STOP: D160 background regression suite incomplete or failing.")
print("D160 FULL JVM PASS")
PY_TEST

if ! bash "$gradle" "-Pandroid.aapt2FromMavenOverride=$aapt2" \
  :app:assembleDebugAndroidTest :app:bundleRelease \
  --no-daemon --console=plain >>"$log" 2>&1; then
  show_errors
  fail "AndroidTest compilation or release bundle build failed: $log"
fi
[[ -n "$(find app/build/outputs/apk/androidTest -type f -name '*.apk' -print -quit 2>/dev/null)" ]] ||
  fail "AndroidTest APK compilation artifact missing; tests were NOT executed."
unsigned="app/build/outputs/bundle/release/app-release.aab"
[[ -s "$unsigned" ]] || fail "Unsigned release AAB is missing."
# Record resolved release dependencies for follow-up Data Safety audit.
if ! bash "$gradle" "-Pandroid.aapt2FromMavenOverride=$aapt2" \
  :app:dependencies --configuration releaseRuntimeClasspath \
  --no-daemon --console=plain >>"$log" 2>&1; then
  show_errors
  fail "Release dependency report failed: $log"
fi

# Read packaged protobuf AndroidManifest.xml with stdlib only (AAPT2 Resources.proto).
# This checks actual AAB bytes, not merely source grep or Gradle config.
python3 - "$unsigned" "$art" "$icon" <<'PY_AAB'
import hashlib, pathlib, sys, zipfile
bundle, artwork, icon = map(pathlib.Path, sys.argv[1:])
def check(ok, message):
    if not ok:
        raise SystemExit("STOP: " + message)
approved = "c7d69a52c53a86738cc416b4c29eb621f8792cf367539732ce0ffa3639d8460d"
check(hashlib.sha256(artwork.read_bytes()).hexdigest() == approved, "source splash SHA256 mismatch")
check(icon.is_file() and icon.stat().st_size > 0, "launcher source missing")
icon_sha = hashlib.sha256(icon.read_bytes()).hexdigest()
with zipfile.ZipFile(bundle) as z:
    names = z.namelist()
    check(len(names) == len(set(names)), "duplicate bundle ZIP entries")
    check("base/manifest/AndroidManifest.xml" in names, "missing packaged manifest")
    check("BundleConfig.pb" in names, "missing bundle config")
    check(any(n.startswith("base/dex/") and n.endswith(".dex") for n in names), "missing app DEX")
    check(any(n.startswith("base/res/mipmap") for n in names), "missing launcher icon resources")
    check(any(n.startswith("base/res/mipmap") and n.endswith("/ic_launcher.xml") for n in names),
          "launcher XML resource missing")
    check(any(n.startswith("base/res/mipmap") and n.endswith("/ic_launcher_round.xml") for n in names),
          "round launcher XML resource missing")
    icon_entries = [n for n in names if n.startswith("base/res/") and
                    n.endswith("/atelier_vitruvian_launcher_d138.webp")]
    check(len(icon_entries) == 1, "approved launcher WebP missing or duplicated")
    check(hashlib.sha256(z.read(icon_entries[0])).hexdigest() == icon_sha,
          "packaged launcher WebP differs from approved source")
    asset = "base/assets/atelier_splash_approved_d139.png"
    check(asset in names and hashlib.sha256(z.read(asset)).hexdigest() == approved,
          "approved artwork not included byte-identically in AAB")
    check(not any(n.upper().startswith("META-INF/") and n.upper().endswith(".SF") for n in names),
          "unexpected pre-signed release AAB")
    packed_manifest = z.read("base/manifest/AndroidManifest.xml")

def read_varint(data, pos):
    value = shift = 0
    while True:
        check(pos < len(data) and shift <= 70, "invalid protobuf varint")
        b = data[pos]
        pos += 1
        value |= (b & 127) << shift
        if b < 128:
            return value, pos
        shift += 7

def fields(data):
    pos = 0
    while pos < len(data):
        tag, pos = read_varint(data, pos)
        number, wire = tag >> 3, tag & 7
        check(number > 0, "invalid protobuf field")
        if wire == 0:
            val, pos = read_varint(data, pos)
        elif wire == 1:
            check(pos + 8 <= len(data), "invalid protobuf 64-bit field")
            val, pos = data[pos:pos + 8], pos + 8
        elif wire == 2:
            count, pos = read_varint(data, pos)
            check(pos + count <= len(data), "invalid protobuf length")
            val, pos = data[pos:pos + count], pos + count
        elif wire == 5:
            check(pos + 4 <= len(data), "invalid protobuf 32-bit field")
            val, pos = data[pos:pos + 4], pos + 4
        else:
            check(False, "unsupported protobuf wire type")
        yield number, wire, val

def parse_attribute(data):
    attrs = {n: v.decode("utf-8") for n, w, v in fields(data)
             if w == 2 and n in (1, 2, 3)}
    return (attrs.get(1, ""), attrs.get(2, "")), attrs.get(3, "")

def parse_element(data):
    element = {"tag": None, "attributes": {}, "children": []}
    for number, wire, val in fields(data):
        if wire != 2:
            continue
        if number == 3:
            element["tag"] = val.decode("utf-8")
        elif number == 4:
            key, value = parse_attribute(val)
            element["attributes"][key] = value
        elif number == 5:
            child = parse_node(val)
            if child is not None:
                element["children"].append(child)
    return element

def parse_node(data):
    for number, wire, val in fields(data):
        if number == 1 and wire == 2:
            return parse_element(val)
    return None

A = "http://schemas.android.com/apk/res/android"
root = parse_node(packed_manifest)
check(root is not None and root["tag"] == "manifest", "unreadable compiled AAB manifest")
attr = lambda el, namespace, name: el["attributes"].get((namespace, name))
children = lambda el, tag: [e for e in el["children"] if e["tag"] == tag]
check(attr(root, "", "package") == "com.ateliertime.android",
      "release package is not com.ateliertime.android")
check(attr(root, A, "versionCode") == "54", "packaged versionCode is not 54")
check(attr(root, A, "versionName") == "D-160", "packaged versionName is not D-160")
sdk = children(root, "uses-sdk")
check(len(sdk) == 1 and attr(sdk[0], A, "minSdkVersion") == "26"
      and attr(sdk[0], A, "targetSdkVersion") == "36",
      "packaged minSdk/targetSdk differ from 26/36")
permissions = {attr(el, A, "name") for el in children(root, "uses-permission")}
required = {
    "android.permission.POST_NOTIFICATIONS",
    "android.permission.MODIFY_AUDIO_SETTINGS",
    "android.permission.FOREGROUND_SERVICE",
    "android.permission.FOREGROUND_SERVICE_SPECIAL_USE",
}
# AndroidX Core's internal dynamic-receiver permission is generated by its
# merged library manifest. Only accept this exact extra package-scoped name
# AND only when the packaged AAB declares it with signature protection.
internal_receiver = "com.ateliertime.android.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"
missing = required - permissions
unexpected = permissions - required - {internal_receiver}
check(not missing and not unexpected,
      "unexpected or missing release permissions: missing=" + repr(missing)
      + ", unexpected=" + repr(unexpected))
if internal_receiver in permissions:
    declarations = [el for el in children(root, "permission")
                    if attr(el, A, "name") == internal_receiver]
    check(len(declarations) == 1
          and attr(declarations[0], A, "protectionLevel") == "signature",
          "AndroidX internal receiver permission is not declared signature-only")
    print("ANDROIDX INTERNAL RECEIVER SIGNATURE PERMISSION VERIFIED")
apps = children(root, "application")
check(len(apps) == 1, "unexpected compiled application nodes")
app = apps[0]
check(attr(app, A, "label") == "Atelier Time", "incorrect release app label")
check(attr(app, A, "allowBackup") == "false", "backup policy unexpectedly changed")
check(attr(app, A, "icon") is not None and attr(app, A, "roundIcon") is not None,
      "launcher icons missing from release manifest")
services = [e for e in children(app, "service")
            if (attr(e, A, "name") or "").endswith("ClassNotificationTaskService")]
check(len(services) == 1, "notification service missing or duplicated")
service = services[0]
check(attr(service, A, "foregroundServiceType") == "specialUse",
      "incorrect FGS type in packaged manifest")
check(attr(service, A, "exported") == "false", "FGS unexpectedly exported")
props = [e for e in children(service, "property")
         if attr(e, A, "name") == "android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"]
check(len(props) == 1 and "Class and Free Timer" in (attr(props[0], A, "value") or ""),
      "specialUse subtype does not describe both timers")
print("AAB PACKAGE / VERSION / SDK / MANIFEST / FGS / PERMISSIONS / SPLASH / LAUNCHER PASS")
print("Release package: com.ateliertime.android; D-160 / Code54; targetSdk36")
print("Release permissions:", ", ".join(sorted(permissions)))
PY_AAB

echo "Signing with existing local upload key using the password entered at script start."
# Jarsigner can request both keystore and key password. For the existing PKCS12
# keystore, supply the SAME password to both stdin prompts. No CLI arguments,
# environment variables, temporary password files or additional user prompts.
if ! printf '%s\n%s\n' "$signing_password" "$signing_password" | \
  jarsigner -keystore "$key" -storetype PKCS12 \
  -sigalg SHA256withRSA -digestalg SHA-256 \
  -signedjar "$partial" "$unsigned" atelier-upload; then
  unset signing_password
  fail "Signing failed. No output may be uploaded."
fi
unset signing_password
[[ -s "$partial" ]] || fail "Signed temporary AAB missing."
jarsigner -verify -verbose -certs "$partial" >"$verifylog" 2>&1 ||
  fail "jarsigner verification failed. DO NOT UPLOAD."
grep -qi 'jar verified' "$verifylog" ||
  fail "jarsigner did not report a verified bundle."
python3 - "$unsigned" "$partial" <<'PY_SIGN'
import pathlib, sys, zipfile, hashlib
unsigned, signed = map(pathlib.Path, sys.argv[1:])
with zipfile.ZipFile(unsigned) as original, zipfile.ZipFile(signed) as z:
    src_files, files = original.namelist(), z.namelist()
    if len(files) != len(set(files)):
        raise SystemExit("STOP: duplicate signed AAB ZIP entries")
    if "base/manifest/AndroidManifest.xml" not in files:
        raise SystemExit("STOP: signed AAB lost its manifest")
    if not any(p.upper().startswith("META-INF/") and p.upper().endswith(".SF") for p in files):
        raise SystemExit("STOP: signed AAB JAR signature .SF missing")
    if not any(p.upper().startswith("META-INF/") and p.upper().endswith((".RSA", ".EC", ".DSA")) for p in files):
        raise SystemExit("STOP: signed AAB signature block missing")
    for filename in src_files:
        if filename not in files:
            raise SystemExit("STOP: signed AAB lost file " + filename)
        if hashlib.sha256(original.read(filename)).digest() != hashlib.sha256(z.read(filename)).digest():
            raise SystemExit("STOP: signing changed payload " + filename)
print("SIGNED AAB SIGNATURE STRUCTURE AND PAYLOAD INTEGRITY PASS")
PY_SIGN
keytool -printcert -jarfile "$partial" >"$certlog" 2>&1 ||
  fail "Could not extract upload certificate from signed bundle."
grep -Eq 'SHA256:|SHA-256:' "$certlog" ||
  fail "Upload-certificate SHA256 fingerprint absent; inspect certificate log."
[[ ! -e "$out" && ! -L "$out" ]] || fail "Output appeared during signing; refusing overwrite."
mv -n -- "$partial" "$out"
[[ -s "$out" && ! -e "$partial" ]] || fail "Could not finalize new AAB safely."

echo "SIGNED RELEASE AAB PREPARED FOR HUMAN REVIEW — NOT UPLOADED:"
echo "$out"
sha256sum "$out"
ls -lh "$out"
grep -E 'SHA256:|SHA-256:' "$certlog" | head -3 || true
echo "Compare SHA-256 fingerprint to Play Console App integrity Upload key certificate before upload."
echo "Build log: $log"
echo "Signature report: $verifylog"
echo "Certificate report: $certlog"
echo "AndroidTest COMPILED, NOT EXECUTED."
echo "NO install, NO Play upload, NO cloud CI, NO source edits, NO app data deletion."
