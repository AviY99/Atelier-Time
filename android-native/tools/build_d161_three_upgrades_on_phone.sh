#!/usr/bin/env bash
# D161/Code55: local Samsung DEV-only QA, never a Play signing/upload operation.
set -euo pipefail
cd "$(dirname "$0")/.."
fail() { echo "STOP: $*" >&2; exit 2; }
[[ "$(git branch --show-current)" == "dev/d161-preparation-pause-recovery-2026-11-24" ]] ||
  fail "Wrong branch: switch to the dedicated D161 development branch."
[[ "$(git rev-parse HEAD:android-native/app 2>/dev/null || true)" == "381201a39527e2f6e97c88dfc53fd9312407506d" ]] ||
  fail "App source changed since D161 QA script was prepared; review before build."
for line in 'versionCode = 55' 'versionName = "D-161"' 'applicationIdSuffix = ".dev"'             'targetSdk = 36' 'compileSdk = 36'; do
  grep -Fq "$line" app/build.gradle.kts || fail "Missing version/package guard: $line"
done
root="app/src/main/java/com/ateliertime"
for entry in "$root/domain/PauseWarningTracker.kt"              "$root/android/PauseWarningBeeper.kt"              "$root/android/ClassRunCheckpointStore.kt"; do
  [[ -s "$entry" ]] || fail "Missing D161 code: $entry"
done
for test in D161PreparationInEditorTimelineTest D161PauseWarningTrackerTest             D161ClassCrashRecoverySnapshotTest D161ClassCrashRecoveryTest             D161PreparationPresetCompatibilityTest; do
  [[ -s "app/src/test/java/com/ateliertime/domain/$test.kt" ]] ||
    fail "Missing regression test: $test"
done
for check in 'PhaseKind.PREPARATION' 'classCheckpointStore?.write'              'pause_over_one_minute_red_warning'              'run_interrupted_class_recovery_dialog'              'saveActiveClassCheckpoint()'; do
  grep -Fq "$check" "$root/android/SessionEditorScreen.kt"     "$root/domain/ScheduleEngine.kt" || fail "Missing D161 integration: $check"
done
art="app/src/main/assets/atelier_splash_approved_d139.png"
expected_git="8950a9c509bbc2c2bac02d18c0b9bfdfca06eabc"
expected_sha="c7d69a52c53a86738cc416b4c29eb621f8792cf367539732ce0ffa3639d8460d"
[[ "$(git rev-parse "HEAD:android-native/$art" 2>/dev/null || true)" == "$expected_git" ]] ||
  fail "Approved artwork Git object changed."
if [[ ! -f "$art" && ! -L "$art" ]]; then git restore --source=HEAD --worktree -- "$art"; fi
[[ -f "$art" && ! -L "$art" && "$(git hash-object "$art")" == "$expected_git" ]] ||
  fail "Approved splash source missing/changed."
[[ -z "$(git status --porcelain --untracked-files=no)" ]] ||
  fail "Unexpected tracked local source edits."
gradle="$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle"
aapt2="$ANDROID_HOME/build-tools/36.0.0/aapt2"
[[ -f "$gradle" && -s "$aapt2" ]] ||
  fail "Gradle 9.6.1 or Android SDK36 AAPT2 is missing."
outdir="/storage/emulated/0/Download"
[[ -d "$outdir" ]] || fail "Download directory missing."
apk_out="$outdir/Atelier-Time-D161-Code55-DEV-QA.apk"
[[ ! -e "$apk_out" ]] ||
  fail "D161 candidate already exists; do not silently overwrite it."
log="$outdir/Atelier-Time-D161-Code55-QA-$(date +%Y%m%d-%H%M%S).log"
echo "D161: full JVM tests, DEV debug APK and AndroidTest COMPILE; no install."
if ! bash "$gradle" "-Pandroid.aapt2FromMavenOverride=$aapt2"     :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest     --no-daemon --console=plain >"$log" 2>&1; then
  echo "D161 BUILD FAILED:"
  grep -E '(^| )e: |(^| )error: |\.kt:[0-9]+|^FAILURE:|^BUILD FAILED|Execution failed for task|FAILED'     "$log" | head -65 || true
  echo "Full log: $log"
  exit 3
fi
python3 - app/build/test-results/testDebugUnitTest <<'PY'
import pathlib, sys, xml.etree.ElementTree as ET
counts = dict.fromkeys(("tests","failures","errors","skipped"),0)
classes={}
for f in pathlib.Path(sys.argv[1]).glob("TEST-*.xml"):
    root=ET.parse(f).getroot()
    for suite in ([root] if root.tag=="testsuite" else root.findall(".//testsuite")):
        n={k:int(suite.attrib.get(k,"0")) for k in counts}
        classes[suite.attrib.get("name","")]=n
        for k in counts: counts[k]+=n[k]
print("D161 JVM ACTUAL: "+" ".join(k+"="+str(v) for k,v in counts.items()))
for klass, expected in [
  ("D158BackgroundNotificationsTest",8),
  ("D161PreparationInEditorTimelineTest",5),
  ("D161PauseWarningTrackerTest",2),
  ("D161ClassCrashRecoverySnapshotTest",4),
  ("D161ClassCrashRecoveryTest",4),
  ("D161PreparationPresetCompatibilityTest",2),
]:
    record=classes.get("com.ateliertime.domain."+klass)
    print(klass, record)
    if not record or record["tests"] < expected or any(
        record[k] for k in ("failures","errors","skipped")):
        raise SystemExit("STOP: required regression suite missing/failed: "+klass)
if counts["tests"] < 343 or any(counts[k] for k in ("failures","errors","skipped")):
    raise SystemExit("STOP: full JVM regression tests failed or incomplete.")
print("D161 FULL JVM PASS")
PY
apk="app/build/outputs/apk/debug/app-debug.apk"
[[ -s "$apk" ]] || fail "DEV APK missing."
[[ -n "$(find app/build/outputs/apk/androidTest -type f -name '*.apk' -print -quit 2>/dev/null)" ]] ||
  fail "AndroidTest APK was not compiled."
python3 - "$apk" "$art" "$expected_sha" <<'PY'
import hashlib, pathlib, sys, zipfile
apk, art, expected = sys.argv[1:]
if hashlib.sha256(pathlib.Path(art).read_bytes()).hexdigest() != expected:
    raise SystemExit("STOP: original splash source hash changed.")
with zipfile.ZipFile(apk) as z:
    name="assets/atelier_splash_approved_d139.png"
    if name not in z.namelist() or hashlib.sha256(z.read(name)).hexdigest()!=expected:
        raise SystemExit("STOP: approved splash missing or modified inside APK.")
print("D161 SPLASH HASH PASS")
PY
cp -n "$apk" "$apk_out"
[[ -s "$apk_out" ]] || fail "DEV APK copy failed."
sha256sum "$apk_out"
echo "D161 LOCAL BUILD PASS: $apk_out"
echo "D161 LOG: $log"
echo "AndroidTest COMPILED, NOT EXECUTED. No app install, uninstall, key signing, AAB, Play upload or public release."
echo "Debug package com.ateliertime.android.dev does NOT replace Play-signed com.ateliertime.android."
