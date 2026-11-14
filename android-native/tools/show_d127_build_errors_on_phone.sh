#!/usr/bin/env bash
# Show D-127 compiler errors from an EXISTING LOCAL log. NO network or build.
# Read-only: never installs, updates GitHub, clears data, or reruns Gradle.
set -euo pipefail
log="/storage/emulated/0/Download/Atelier-Time-D127-Stage7-build.log"
if [[ ! -s "$log" ]]; then
  echo "NO LOCAL D-127 LOG: $log"
  exit 2
fi
echo "=============================================================="
echo "ATELIER TIME D-127 — SAVED COMPILER ERRORS (NO NEW BUILD)"
echo "=============================================================="
echo "Source: $log"
echo "Showing up to 30 diagnostic lines. The original full log stays on phone."
# Kotlin K2: 'e: file:///.../File.kt:158:22 Unresolved reference ...'
# Older Kotlin, Gradle and other errors are included too.
set +e
awk '
  /(^|[[:space:]])e: |(^|[[:space:]])error: |\.kt:[0-9]+(:[0-9]+)?|^FAILURE:|^BUILD FAILED|Execution failed for task|Compilation error|Could not resolve/ {
    if (count < 30) print
    count++
  }
  END {
    if (count == 0) print "No compiler diagnostic lines found. The build may have failed before Kotlin compilation."
    if (count > 30) printf "Additional %d matching lines remain in saved full log.\n", count - 30
  }
' "$log"
set -e
echo "=============================================================="
echo "If needed, photograph THIS summary screen. Do not rerun the build."
