#!/usr/bin/env bash
# D-121 candidate gate for Code on the Go. Build only: NO installation,
# data reset, device command, or Stable-branch write.
set -euo pipefail

cd "$(dirname "$0")/.."
if [[ "$(git branch --show-current)" != "android-native-kotlin-compose" ]]; then
    echo "ERROR: development branch required." >&2
    exit 2
fi
if [[ -n "$(git status --porcelain --untracked-files=no)" ]] ||
   [[ -n "$(git ls-files --others --exclude-standard -- app/src tools)" ]]; then
    echo "ERROR: review local source modifications before building." >&2
    git status --short
    exit 2
fi
if ! grep -Eq 'versionName[[:space:]]*=[[:space:]]*"D-121"' app/build.gradle.kts ||
   ! grep -Eq 'versionCode[[:space:]]*=[[:space:]]*15([[:space:]]|$)' app/build.gradle.kts; then
    echo "ERROR: D-121/versionCode 15 source required." >&2
    exit 2
fi
: "${ANDROID_HOME:?Code on the Go Android SDK required}"
gradle="$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle"
aapt2="$ANDROID_HOME/build-tools/36.0.0/aapt2"
if [[ ! -f "$gradle" || ! -s "$aapt2" ]]; then
    echo "ERROR: Code on the Go Gradle or ARM64 AAPT2 missing." >&2
    exit 2
fi

downloads="/storage/emulated/0/Download"
mkdir -p "$downloads"
log="$downloads/Atelier-Time-D121-JVM-build.log"
echo "Building D-121 candidate + ALL JVM tests + AndroidTest APK; NO INSTALL."
bash "$gradle" "-Pandroid.aapt2FromMavenOverride=$aapt2" \
    :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest \
    --rerun-tasks --console=plain --no-daemon 2>&1 | tee "$log"

report_dir="app/build/test-results/testDebugUnitTest"
if command -v python3 >/dev/null 2>&1; then
python3 - "$report_dir" <<'PY'
import pathlib, sys, xml.etree.ElementTree as ET
files = sorted(pathlib.Path(sys.argv[1]).glob('TEST-*.xml'))
if not files:
    raise SystemExit('ERROR: missing JUnit XML reports.')
totals = {key: 0 for key in ('tests', 'failures', 'errors', 'skipped')}
for file in files:
    root = ET.parse(file).getroot()
    suites = [root] if root.tag == 'testsuite' else root.findall('.//testsuite')
    for suite in suites:
        for key in totals:
            totals[key] += int(suite.attrib.get(key, '0'))
print('D-121 JVM ACTUAL: ' + ' '.join(f'{k}={v}' for k,v in totals.items()))
if totals['tests'] == 0 or any(totals[k] for k in ('failures', 'errors', 'skipped')):
    raise SystemExit('FAIL: JVM suite incomplete; inspect XML and build log.')
PY
else
    if [[ ! -d "$report_dir" ]]; then echo "ERROR: missing JUnit XML." >&2; exit 3; fi
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
            printf "D-121 JVM ACTUAL: tests=%d failures=%d errors=%d skipped=%d\n", sum[1], sum[2], sum[3], sum[4]
            if (count == 0 || sum[1] == 0 || sum[2] || sum[3] || sum[4]) exit 1
        }
    ' "$report_dir"/TEST-*.xml
fi

mapfile -t regular_apks < <(find app/build/outputs/apk/debug -type f -name '*.apk')
mapfile -t test_apks < <(find app/build/outputs/apk/androidTest -type f -name '*.apk')
if [[ "${#regular_apks[@]}" -ne 1 || "${#test_apks[@]}" -ne 1 ]]; then
    echo "ERROR: expected exactly one debug app APK and one test APK." >&2
    exit 3
fi
regular="$downloads/Atelier-Time-D121-Candidate.apk"
tests="$downloads/Atelier-Time-D121-Tests.apk"
cp "${regular_apks[0]}" "$regular"
cp "${test_apks[0]}" "$tests"
test_count="$(grep -R -E '^[[:space:]]*@Test' app/src/androidTest/java | wc -l | tr -d ' ')"
if [[ "$test_count" -lt 1 ]]; then
    echo "ERROR: no instrumentation test methods found." >&2
    exit 3
fi
manifest="$downloads/Atelier-Time-D121-QA-manifest.txt"
{
    printf 'version=D-121\ncode=16\nbranch=android-native-kotlin-compose\n'
    printf 'commit=%s\n' "$(git rev-parse HEAD)"
    printf 'test_count=%s\n' "$test_count"
    printf 'app_sha256=%s\n' "$(sha256sum "$regular" | awk '{print $1}')"
    printf 'test_sha256=%s\n' "$(sha256sum "$tests" | awk '{print $1}')"
} > "$manifest"
echo "BUILD ONLY PASS: regular candidate=$regular; test APK=$tests; manifest=$manifest"
echo "NO regular/test APK installed; obtain separate permission before phone changes."
