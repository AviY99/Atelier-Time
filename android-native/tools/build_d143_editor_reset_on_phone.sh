#!/usr/bin/env bash
# D-143 terminal-remainder recovery: Samsung Code on the Go LOCAL QA / debug build only.
# NEVER installs/uninstalls, clears app data, signs releases, pushes, or triggers paid CI.
set -euo pipefail
cd "$(dirname "$0")/.."

if [[ "$(git branch --show-current)" != "android-native-kotlin-compose" ]]; then
  echo 'STOP: checkout must be android-native-kotlin-compose.' >&2; exit 2
fi
# Preserve .cg and unrelated IDE caches. Refuse tracked source changes or untracked app/tools.
if [[ -n "$(git status --porcelain --untracked-files=no)" ]] ||
   [[ -n "$(git ls-files --others --exclude-standard -- app/src tools)" ]]; then
  echo 'STOP: local tracked files or untracked app/tool sources require review.' >&2
  git status --short
  exit 2
fi
for setting in 'compileSdk = 36' 'targetSdk = 36' 'versionCode = 37' 'versionName = "D-143"'; do
  if ! grep -Fq "$setting" app/build.gradle.kts; then
    echo "STOP: missing required D-143 version setting: $setting" >&2; exit 2
  fi
done
for source in \
  app/src/main/java/com/ateliertime/domain/ScheduleEngine.kt \
  app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt \
  app/src/main/java/com/ateliertime/domain/Models.kt \
  app/src/test/java/com/ateliertime/domain/TailBreakRecoveryD142Test.kt \
  app/src/test/java/com/ateliertime/android/EditorResetD143Test.kt \
  app/src/androidTest/java/com/ateliertime/android/EditorResetD143UiTest.kt; do
  if [[ ! -s "$source" ]]; then
    echo "STOP: required source not found: $source" >&2; exit 2
  fi
done

# Verify the approved splash SHA and tracked presence of the launcher/UI artwork.
bash tools/audit_artwork_on_phone.sh
: "${ANDROID_HOME:?Code on the Go Android SDK is required}"
gradle="$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle"
aapt2="$ANDROID_HOME/build-tools/36.0.0/aapt2"
if [[ ! -f "$gradle" || ! -s "$aapt2" ]]; then
  echo 'STOP: existing Code on the Go Gradle / ARM64 AAPT2 tools missing.' >&2; exit 2
fi

downloads='/storage/emulated/0/Download'
mkdir -p "$downloads"
log="$downloads/Atelier-Time-D143-API36-Build.log"
echo 'D-143: running full JVM suite (forced fresh) and assembling debug APK; NO INSTALL.'
if ! bash "$gradle" "-Pandroid.aapt2FromMavenOverride=$aapt2" \
    :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest \
    --rerun-tasks --console=plain --no-daemon > "$log" 2>&1; then
  echo 'D-143 JVM TESTS OR BUILD FAILED. Relevant local terminal diagnostics:'
  awk '/(^|[[:space:]])e: |(^|[[:space:]])error: |\.kt:[0-9]+|^FAILURE:|^BUILD FAILED|Execution failed for task|FAILED/ {if (n++ < 60) print}' "$log"
  echo "Complete PRIVATE log kept ONLY on phone: $log"
  exit 1
fi

report_dir='app/build/test-results/testDebugUnitTest'
d143_xml="$report_dir/TEST-com.ateliertime.android.EditorResetD143Test.xml"
d142_xml="$report_dir/TEST-com.ateliertime.domain.TailBreakRecoveryD142Test.xml"
if [[ ! -s "$d143_xml" || ! -s "$d142_xml" ]]; then
  echo 'STOP: D-143 or D-142 JUnit XML results missing.' >&2; exit 3
fi
if command -v python3 >/dev/null 2>&1; then
  python3 - "$report_dir" <<'PY'
import pathlib, sys, xml.etree.ElementTree as ET
files = sorted(pathlib.Path(sys.argv[1]).glob("TEST-*.xml"))
if not files: raise SystemExit("STOP: no JUnit XML results.")
totals = dict.fromkeys(("tests","failures","errors","skipped"), 0)
found = {}
for file in files:
    root = ET.parse(file).getroot()
    suites = [root] if root.tag == "testsuite" else root.findall(".//testsuite")
    for suite in suites:
        values = {k: int(suite.attrib.get(k, "0")) for k in totals}
        for key in totals: totals[key] += values[key]
        found[suite.attrib.get("name", "")] = values
for name, minimum in [
    ("com.ateliertime.android.EditorResetD143Test", 2),
    ("com.ateliertime.domain.TailBreakRecoveryD142Test", 9),
]:
    v = found.get(name)
    print(name.split(".")[-1], v)
    if not v or v["tests"] < minimum or any(v[k] for k in ("failures", "errors", "skipped")):
        raise SystemExit("STOP: required D-143/D-142 unit regression tests failed.")
print("JVM ACTUAL: " + " ".join(f"{k}={v}" for k,v in totals.items()))
if totals["tests"] == 0 or any(totals[k] for k in ("failures", "errors", "skipped")):
    raise SystemExit("STOP: full JVM suite failed or skipped tests.")
PY
else
  for xml in "$d143_xml" "$d142_xml"; do
    if grep -Eq '<(failure|error|skipped)([ >])' "$xml"; then
      echo "STOP: test failures/skips reported in $xml" >&2; exit 3
    fi
  done
  [[ "$(grep -c '<testcase ' "$d143_xml" || true)" -ge 2 ]] || exit 3
  [[ "$(grep -c '<testcase ' "$d142_xml" || true)" -ge 9 ]] || exit 3
  echo 'D-143 (2) and D-142 (9) JVM tests PASS; full Gradle JVM suite PASS.'
fi

# Compile AndroidTest APK to check UI test source, but never run or install it.
mapfile -t test_apks < <(find app/build/outputs/apk/androidTest -type f -name '*.apk' | sort)
if [[ "${#test_apks[@]}" -ne 1 || ! -s "${test_apks[0]:-}" ]]; then
  echo 'STOP: expected exactly one nonempty compiled AndroidTest APK.' >&2; exit 3
fi
apk_dir='app/build/outputs/apk/debug'
mapfile -t apks < <(find "$apk_dir" -type f -name '*.apk' | sort)
if [[ "${#apks[@]}" -ne 1 || ! -s "${apks[0]:-}" ]]; then
  echo "STOP: expected exactly one nonempty debug APK, found ${#apks[@]}." >&2; exit 3
fi
out="$downloads/Atelier-Time-D143-API36-Candidate.apk"
cp "${apks[0]}" "$out"
manifest="$downloads/Atelier-Time-D143-API36-Manifest.txt"
{
  printf 'version=D-143\nversion_code=37\ntarget_sdk=36\n'
  printf 'branch=android-native-kotlin-compose\n'
  printf 'source_commit=%s\n' "$(git rev-parse HEAD)"
  printf 'apk_sha256=%s\n' "$(sha256sum "$out" | awk '{print $1}')"
  printf 'jvm_test_state=PASSED; D143_2_AND_D142_9_TESTS_CONFIRMED\n'
  printf 'androidTest=COMPILED_NOT_EXECUTED\ndevice_install=NOT_PERFORMED\nphysical_device_QA=NOT_PERFORMED\n'
} > "$manifest"
echo "D-143 JVM TESTS AND BUILD SUCCESSFUL: $out"
echo "Manifest: $manifest"
echo "Private log: $log"
echo 'No install, device QA, cloud build, GitHub push, signing, or data clear was done.'
