#!/usr/bin/env bash
# D158/Code52: local Samsung QA only; distinct .dev APK never replaces Play D157.
set -euo pipefail
cd "$(dirname "$0")/.."
fail() { echo "STOP: $*" >&2; exit 2; }
[[ "$(git branch --show-current)" == "android-native-kotlin-compose" ]] || fail "Wrong branch."
[[ "$(git rev-parse HEAD:android-native/app 2>/dev/null || true)" == "df85d462774cf59a8cd159c47353b5592ff6eea7" ]] ||
  fail "Unexpected D158 app source tree. Review before building."
for item in 'versionCode = 52' 'versionName = "D-158"' 'applicationIdSuffix = ".dev"' 'targetSdk = 36' 'compileSdk = 36'; do
  grep -Fq "$item" app/build.gradle.kts || fail "Missing $item"
done
grep -Fq 'playingSession?.rules' app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt ||
  fail "Class notification not group-aware."
grep -Fq 'freeStatusNotification.update' app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt ||
  fail "Free status not connected."
art="app/src/main/assets/atelier_splash_approved_d139.png"
expected="8950a9c509bbc2c2bac02d18c0b9bfdfca06eabc"
[[ "$(git rev-parse "HEAD:android-native/$art" 2>/dev/null || true)" == "$expected" ]] ||
  fail "Approved splash Git object missing."
if [[ ! -e "$art" && ! -L "$art" ]]; then git restore --source=HEAD --worktree -- "$art"; fi
[[ -f "$art" && ! -L "$art" && "$(git hash-object "$art")" == "$expected" ]] ||
  fail "Approved splash image missing or altered."
[[ -z "$(git status --porcelain --untracked-files=no)" ]] ||
  fail "Unexpected tracked source changes."
gradle="$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle"
aapt2="$ANDROID_HOME/build-tools/36.0.0/aapt2"
[[ -f "$gradle" && -s "$aapt2" ]] || fail "Gradle 9.6.1 / SDK36 AAPT2 unavailable."
dest="/storage/emulated/0/Download"
[[ -d "$dest" ]] || fail "Downloads missing."
out="$dest/Atelier-Time-D158-Code52-DEV-QA.apk"
[[ ! -e "$out" ]] || fail "Candidate already exists; will not overwrite."
log="$dest/Atelier-Time-D158-Code52-QA-$(date +%Y%m%d-%H%M%S).log"
echo "D158: full JVM + debug APK + AndroidTest COMPILE. NO INSTALL."
if ! bash "$gradle" "-Pandroid.aapt2FromMavenOverride=$aapt2" \
    :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest \
    --no-daemon --console=plain >"$log" 2>&1; then
  echo "D158 BUILD FAILED:"
  grep -E '(^| )e: |(^| )error: |\.kt:[0-9]+|^FAILURE:|^BUILD FAILED|Execution failed for task|FAILED' "$log" | head -65 || true
  echo "Full private log: $log"
  exit 3
fi
python3 - app/build/test-results/testDebugUnitTest <<'PY'
import pathlib, sys, xml.etree.ElementTree as ET
totals = dict.fromkeys(("tests","failures","errors","skipped"), 0)
classes = {}
for f in pathlib.Path(sys.argv[1]).glob("TEST-*.xml"):
    root = ET.parse(f).getroot()
    for suite in ([root] if root.tag == "testsuite" else root.findall(".//testsuite")):
        counts = {k: int(suite.attrib.get(k, "0")) for k in totals}
        classes[suite.attrib.get("name", "")] = counts
        for k in totals: totals[k] += counts[k]
print("D158 ALL JVM ACTUAL: " + " ".join(k+"="+str(v) for k,v in totals.items()))
target = classes.get("com.ateliertime.domain.D158BackgroundNotificationsTest")
print("D158 BACKGROUND REGRESSIONS:", target)
if not target or target["tests"] < 8 or any(target[k] for k in ("failures","errors","skipped")):
    raise SystemExit("STOP: D158 regression tests missing or failed.")
if totals["tests"] < 326 or any(totals[k] for k in ("failures","errors","skipped")):
    raise SystemExit("STOP: JVM tests incomplete, failed or skipped.")
print("D158 FULL JVM PASS")
PY
apk="app/build/outputs/apk/debug/app-debug.apk"
[[ -s "$apk" ]] || fail "Debug APK absent."
[[ -n "$(find app/build/outputs/apk/androidTest -type f -name '*.apk' -print -quit 2>/dev/null)" ]] ||
  fail "AndroidTest APK not compiled."
python3 - "$apk" "$art" <<'PY'
import hashlib, pathlib, sys, zipfile
apk, art = sys.argv[1:]
expected = "c7d69a52c53a86738cc416b4c29eb621f8792cf367539732ce0ffa3639d8460d"
if hashlib.sha256(pathlib.Path(art).read_bytes()).hexdigest() != expected:
    raise SystemExit("STOP: approved artwork source SHA256 changed.")
with zipfile.ZipFile(apk) as z:
    path = "assets/atelier_splash_approved_d139.png"
    if path not in z.namelist() or hashlib.sha256(z.read(path)).hexdigest() != expected:
        raise SystemExit("STOP: approved artwork not included byte-identically in APK.")
print("D158 APK ARTWORK INCLUDED PASS")
PY
cp -n "$apk" "$out"
[[ -s "$out" ]] || fail "Candidate copy failed."
echo "D158 JVM TESTS AND BUILD SUCCESSFUL: $out"
echo "Build log: $log"
sha256sum "$out"
echo "AndroidTest COMPILED, NOT EXECUTED; no APK install, Play upload or cloud CI."
echo "Debug package com.ateliertime.android.dev preserves installed Play data."
