#!/usr/bin/env bash
# Run the current pure-Dart Step 1.4 checks locally, without GitHub Actions.
# Usage: bash tool/verify.sh
set -euo pipefail
cd "$(dirname "$0")/.."
log_file="atelier-time-verification.log"
: > "$log_file"

if ! command -v dart >/dev/null 2>&1; then
  printf '%s\n' 'Dart SDK was not found on PATH.' \
    'Install Dart from https://dart.dev/get-dart and retry.' | tee -a "$log_file"
  exit 2
fi

run_check() {
  printf '\n>>> %s\n' "$*" | tee -a "$log_file"
  "$@" 2>&1 | tee -a "$log_file"
}

run_check dart --version
run_check dart tool/smoke.dart
run_check dart pub get
run_check dart analyze
run_check dart test
printf '\nAll locally executed Dart checks passed.\n' | tee -a "$log_file"
printf 'Log saved to %s\n' "$log_file"
