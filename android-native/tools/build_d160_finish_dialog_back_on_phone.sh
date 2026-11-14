#!/usr/bin/env bash
# D160/Code54: local Samsung QA for revised finish-approval layout. DEV-only APK, NO Play upload.
set -euo pipefail
cd "$(dirname "$0")/.."
fail() { echo "STOP: $*" >&2; exit 2; }
[[ "$(git branch --show-current)" == "android-native-kotlin-compose" ]] || fail "Wrong branch."
[[ "$(git rev-parse HEAD:android-native/app 2>/dev/null || true)" == "717ea00fe4304cdcea4e53e04a1eaab0a378e559" ]] ||
  fail "Unexpected D160 app source tree. Review before building."
for item in 'versionCode = 54' 'versionName = "D-160"' 'applicationIdSuffix = ".dev"' 'targetSdk = 36' 'compileSdk = 36'; do
  grep -Fq "$item" app/build.gradle.kts || fail "Missing $item"
done
grep -Fq 'playingSession?.rules' app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt ||
  fail "Class notification not group-aware."
grep -Fq 'freeStatusNotification.update' app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt ||
  fail "Free status not connected."
# UI-only D160 regression guards; confirm the picker engine remains present.
ui="app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt"
for item in '"Calculated finish: "' '.testTag("run_end_calculated")' \
  'style = MaterialTheme.typography.titleMedium' \
  '.testTag("run_end_actions")' \
  'Text(if (finishPickerVisible) "Back" else "Change finish time"' \
  'Final two Sessions:'; do
  grep -Fq "$item" "$ui" || fail "Missing D160 finish dialog UI contract: $item"
done
! grep -Fq 'Text(request.session.rules.name)' "$ui" ||
  fail "Class-name line was not removed from finish dialog."
! grep -Fq '.testTag("run_end_reset_calculated")' "$ui" ||
  fail "Redundant calculated-finish reset button still present."
! grep -Fq '.testTag("run_end_calculated_value")' "$ui" ||
  fail "Oversized split calculated time still present."
! grep -Fq 'Text("Test Class alert sound")' "$ui" ||
  fail "Obsolete in-dialog audio preview still present."
test_ui="app/src/androidTest/java/com/ateliertime/android/RunEndApprovalUiTest.kt"
for item in 'd160FinishDialogRestoresOneUniformCalculatedLineAndBottomActions' \
  'd160ChangeBecomesBackAndReturnsToLiveCalculatedFinish'; do
  grep -Fq "$item" "$test_ui" || fail "D160 AndroidTest source missing: $item"
done
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
out="$dest/Atelier-Time-D160-Code54-FinishDialog-DEV-QA.apk"
[[ ! -e "$out" ]] || fail "Candidate already exists; will not overwrite."
log="$dest/Atelier-Time-D160-Code54-QA-$(date +%Y%m%d-%H%M%S).log"
echo "D160: full JVM + debug APK + AndroidTest COMPILE. NO INSTALL."
if ! bash "$gradle" "-Pandroid.aapt2FromMavenOverride=$aapt2" \
    :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest \
    --no-daemon --console=plain >"$log" 2>&1; then
  echo "D160 BUILD FAILED:"
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
print("D160 ALL JVM ACTUAL: " + " ".join(k+"="+str(v) for k,v in totals.items()))
target = classes.get("com.ateliertime.domain.D158BackgroundNotificationsTest")
print("D160 INHERITED BACKGROUND REGRESSIONS:", target)
if not target or target["tests"] < 8 or any(target[k] for k in ("failures","errors","skipped")):
    raise SystemExit("STOP: Inherited D158 background regressions missing or failed.")
if totals["tests"] < 326 or any(totals[k] for k in ("failures","errors","skipped")):
    raise SystemExit("STOP: JVM tests incomplete, failed or skipped.")
print("D160 FULL JVM PASS")
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
print("D160 APK ARTWORK INCLUDED PASS")
PY
cp -n "$apk" "$out"
[[ -s "$out" ]] || fail "Candidate copy failed."
echo "D160 JVM TESTS AND BUILD SUCCESSFUL: $out"
echo "Build log: $log"
sha256sum "$out"
echo "AndroidTest COMPILED, NOT EXECUTED; no APK install, Play upload or cloud CI."
echo "Debug package com.ateliertime.android.dev preserves installed Play data."
