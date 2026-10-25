#!/usr/bin/env bash
# D-114 Stage 2. Run in Code on the Go only; JVM tests + test APK ONLY.
# Requests test tasks only; Gradle may build dependencies. Never copies/installs\n# the regular Atelier Time APK and never wipes app data.
set -euo pipefail

cd "$(dirname "$0")/.."
if [[ "$(git branch --show-current)" != "android-native-kotlin-compose" ]]; then
    echo "ERROR: checkout must be android-native-kotlin-compose." >&2
    exit 2
fi
if [[ -n "$(git status --porcelain)" ]]; then
    echo "ERROR: working tree has local changes. Review them first." >&2
    exit 2
fi
if ! grep -Eq 'versionName[[:space:]]*=[[:space:]]*"D-114"' app/build.gradle.kts ||
   ! grep -Eq 'versionCode[[:space:]]*=[[:space:]]*9([[:space:]]|$)' app/build.gradle.kts; then
    echo "ERROR: expected D-114/versionCode 9; refusing stale or unexpected build." >&2
    exit 2
fi
: "${ANDROID_HOME:?CoGo ANDROID_HOME must point to its Android SDK}"
gradle="$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle"
aapt2="$ANDROID_HOME/build-tools/36.0.0/aapt2"
if [[ ! -f "$gradle" || ! -s "$aapt2" ]]; then
    echo "ERROR: CoGo Gradle/native ARM64 AAPT2 missing." >&2
    exit 2
fi

destination="/storage/emulated/0/Download"
mkdir -p "$destination"
log="$destination/Atelier-Time-D114-JVM-build.log"
echo "Running ALL D-114 JVM tests, then assembling AndroidTest APK (no install)."
# Force test execution even when Gradle previously considered it UP-TO-DATE.
bash "$gradle" "-Pandroid.aapt2FromMavenOverride=$aapt2" \
    :app:testDebugUnitTest :app:assembleDebugAndroidTest \
    --rerun-tasks --console=plain --no-daemon 2>&1 | tee "$log"

report_dir="app/build/test-results/testDebugUnitTest"
if [[ ! -d "$report_dir" ]]; then
    echo "ERROR: Gradle passed but JVM JUnit XML results are missing." >&2
    exit 3
fi
# Count actual JUnit results, not @Test source annotations or historical PASS.
# CoGo Python is optional; a portable awk fallback reads Gradle's JUnit XML.
if command -v python3 >/dev/null 2>&1; then
python3 - "$report_dir" <<'PY'
import pathlib, sys, xml.etree.ElementTree as ET
files = sorted(pathlib.Path(sys.argv[1]).glob("TEST-*.xml"))
if not files:
    raise SystemExit("ERROR: no JVM JUnit XML test reports.")
totals = dict.fromkeys(("tests", "failures", "errors", "skipped"), 0)
for f in files:
    root = ET.parse(f).getroot()
    suites = [root] if root.tag == "testsuite" else root.findall(".//testsuite")
    for suite in suites:
        for key in totals:
            totals[key] += int(suite.attrib.get(key, "0"))
print("D-114 JVM ACTUAL: " + " ".join(f"{k}={v}" for k,v in totals.items()))
if totals["tests"] == 0 or totals["failures"] or totals["errors"]:
    raise SystemExit("ERROR: JVM test results incomplete or failing")
if totals["skipped"]:
    print("WARNING: JVM tests skipped; inspect XML (not full PASS).")
PY
else
    awk '
        /<testsuite[[:space:]]/ {
            count++
            for (n = 1; n <= 4; n++) {
                key = (n == 1 ? "tests" : n == 2 ? "failures" : n == 3 ? "errors" : "skipped")
                expression = key "=\"[0-9]+\""
                if (match($0, expression)) {
                    value = substr($0, RSTART + length(key) + 2, RLENGTH - length(key) - 3)
                    sum[n] += value + 0
                }
            }
        }
        END {
            printf "D-114 JVM ACTUAL (XML fallback): tests=%d failures=%d errors=%d skipped=%d\n", sum[1], sum[2], sum[3], sum[4]
            if (count == 0 || sum[1] == 0 || sum[2] || sum[3]) exit 1
            if (sum[4]) print "WARNING: JVM tests skipped; inspect XML (not full PASS)."
        }
    ' "$report_dir"/TEST-*.xml
fi

mapfile -t test_apks < <(find app/build/outputs/apk/androidTest -type f -name '*.apk' | sort)
if (( ${#test_apks[@]} != 1 )) || [[ ! -s "${test_apks[0]:-}" ]]; then
    echo "ERROR: exactly one nonempty AndroidTest APK required." >&2
    exit 3
fi
test_apk="${test_apks[0]}"
count="$(grep -RhoE '@Test([[:space:]]|$)' app/src/androidTest --include='*.kt' | wc -l | tr -d '[:space:]')"
if [[ ! "$count" =~ ^[0-9]+$ || "$count" -lt 1 ]]; then
    echo "ERROR: no instrumented tests discovered in source." >&2
    exit 3
fi
cp "$test_apk" "$destination/Atelier-Time-D114-Tests.apk"
cp tools/termux_run_all_device_qa.sh "$destination/Atelier-Time-D114-QA.sh"
{
    printf 'version=D-114\n'
    printf 'branch=android-native-kotlin-compose\n'
    printf 'commit=%s\n' "$(git rev-parse HEAD)"
    printf 'test_count=%s\n' "$count"
    printf 'test_sha256=%s\n' "$(sha256sum "$destination/Atelier-Time-D114-Tests.apk" | awk '{print $1}')"
} > "$destination/Atelier-Time-D114-QA-manifest.txt"
echo "SOURCE AndroidTest methods=$count, compiled APK NOT EXECUTED on device."
ls -lh "$destination/Atelier-Time-D114-Tests.apk" \
    "$destination/Atelier-Time-D114-QA-manifest.txt" \
    "$destination/Atelier-Time-D114-JVM-build.log"
echo "No regular app APK copied/installed; no device test or user-data changes."
echo "Await explicit approval before running the Termux device stage."
