#!/usr/bin/env bash
# D-142 terminal-remainder recovery: Samsung Code on the Go LOCAL QA / debug build only.
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
for setting in 'compileSdk = 36' 'targetSdk = 36' 'versionCode = 36' 'versionName = "D-142"'; do
  if ! grep -Fq "$setting" app/build.gradle.kts; then
    echo "STOP: missing required D-142 version setting: $setting" >&2; exit 2
  fi
done
for source in \
  app/src/main/java/com/ateliertime/domain/ScheduleEngine.kt \
  app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt \
  app/src/main/java/com/ateliertime/domain/Models.kt \
  app/src/test/java/com/ateliertime/domain/TailBreakRecoveryD142Test.kt; do
  if [[ ! -s "$source" ]]; then
    echo "STOP: required source not found: $source" >&2; exit 2
  fi
done

# Verify byte-exact approved splash/launcher artwork, not merely the presence of a file.
bash tools/audit_artwork_on_phone.sh
: "${ANDROID_HOME:?Code on the Go Android SDK is required}"
gradle="$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle"
aapt2="$ANDROID_HOME/build-tools/36.0.0/aapt2"
if [[ ! -f "$gradle" || ! -s "$aapt2" ]]; then
  echo 'STOP: existing Code on the Go Gradle / ARM64 AAPT2 tools missing.' >&2; exit 2
fi

downloads='/storage/emulated/0/Download'
mkdir -p "$downloads"
log="$downloads/Atelier-Time-D142-API36-Build.log"
echo 'D-142: running full JVM suite (forced fresh) and assembling debug APK; NO INSTALL.'
if ! bash "$gradle" "-Pandroid.aapt2FromMavenOverride=$aapt2" \
    :app:testDebugUnitTest :app:assembleDebug \
    --rerun-tasks --console=plain --no-daemon > "$log" 2>&1; then
  echo 'D-142 JVM TESTS OR BUILD FAILED. Relevant local terminal diagnostics:'
  awk '/(^|[[:space:]])e: |(^|[[:space:]])error: |\\.kt:[0-9]+|^FAILURE:|^BUILD FAILED|Execution failed for task|FAILED/ {if (n++ < 60) print}' "$log"
  echo "Complete PRIVATE log kept ONLY on phone: $log"
  exit 1
fi

report_dir='app/build/test-results/testDebugUnitTest'
d142_xml="$report_dir/TEST-com.ateliertime.domain.TailBreakRecoveryD142Test.xml"
if [[ ! -s "$d142_xml" ]]; then
  echo 'STOP: new D-142 JVM tests have no executed JUnit XML report.' >&2; exit 3
fi
if command -v python3 >/dev/null 2>&1; then
  python3 - "$report_dir" <<'PY'
import pathlib, sys, xml.etree.ElementTree as ET
root = pathlib.Path(sys.argv[1])
files = sorted(root.glob("TEST-*.xml"))
if not files:
    raise SystemExit("STOP: no JVM JUnit XML reports.")
totals = {"tests": 0, "failures": 0, "errors": 0, "skipped": 0}
d142 = None
for file in files:
    node = ET.parse(file).getroot()
    suites = [node] if node.tag == "testsuite" else node.findall(".//testsuite")
    for suite in suites:
        stats = {key: int(suite.attrib.get(key, "0")) for key in totals}
        for key in totals:
            totals[key] += stats[key]
        if suite.attrib.get("name") == "com.ateliertime.domain.TailBreakRecoveryD142Test":
            d142 = stats
print("JVM ACTUAL: " + " ".join(f"{k}={v}" for k,v in totals.items()))
print("D-142 ACTUAL: " + (" ".join(f"{k}={v}" for k,v in d142.items()) if d142 else "NOT FOUND"))
if not d142 or d142["tests"] < 9 or d142["failures"] or d142["errors"] or d142["skipped"]:
    raise SystemExit("STOP: 9 new D-142 tests must execute with zero failures/skips.")
if not totals["tests"] or totals["failures"] or totals["errors"] or totals["skipped"]:
    raise SystemExit("STOP: JVM suite incomplete, failing, or skipped.")
PY
else
  # Older CoGo installs may not have Python; fail closed on missing/failed D-142 XML.
  count="$(grep -c '<testcase ' "$d142_xml" || true)"
  if [[ "$count" -lt 9 ]] || grep -Eq '<(failure|error|skipped)([ >])' "$d142_xml"; then
    echo "STOP: D-142 XML has only $count cases or reports failures/skips." >&2; exit 3
  fi
  echo "D-142 JUnit XML: $count executed testcases; Gradle full JVM suite PASS."
fi

apk_dir='app/build/outputs/apk/debug'
mapfile -t apks < <(find "$apk_dir" -type f -name '*.apk' | sort)
if [[ "${#apks[@]}" -ne 1 || ! -s "${apks[0]:-}" ]]; then
  echo "STOP: expected exactly one nonempty debug APK, found ${#apks[@]}." >&2; exit 3
fi
out="$downloads/Atelier-Time-D142-API36-Candidate.apk"
cp "${apks[0]}" "$out"
manifest="$downloads/Atelier-Time-D142-API36-Manifest.txt"
{
  printf 'version=D-142\nversion_code=36\ntarget_sdk=36\n'
  printf 'branch=android-native-kotlin-compose\n'
  printf 'source_commit=%s\n' "$(git rev-parse HEAD)"
  printf 'apk_sha256=%s\n' "$(sha256sum "$out" | awk '{print $1}')"
  printf 'jvm_test_state=PASSED; D142_9_TESTS_CONFIRMED\n'
  printf 'device_install=NOT_PERFORMED\nphysical_device_QA=NOT_PERFORMED\n'
} > "$manifest"
echo "D-142 JVM TESTS AND BUILD SUCCESSFUL: $out"
echo "Manifest: $manifest"
echo "Private log: $log"
echo 'No install, device QA, cloud build, GitHub push, signing, or data clear was done.'
