#!/usr/bin/env bash
# Store user-approved D-139 image in versioned Git source using its exact
# already-built APK bytes, without redownloading or editing the image.
# Deliberately no build/install/data deletion/cloud CI. Git push uses user's
# existing GitHub credentials; no secrets are requested or stored by script.
set -euo pipefail
cd "$(dirname "$0")/.."
branch="android-native-kotlin-compose"
if [[ "$(git branch --show-current)" != "$branch" ]]; then
  echo "STOP: expected $branch branch." >&2; exit 2
fi
if [[ -n "$(git status --porcelain)" ]]; then
  echo "STOP: working tree has changes; resolve without deleting user files." >&2
  git status --short
  exit 2
fi
expected="c7d69a52c53a86738cc416b4c29eb621f8792cf367539732ce0ffa3639d8460d"
asset="app/src/main/assets/atelier_splash_approved_d139.png"
mkdir -p "$(dirname "$asset")"
if [[ -s "$asset" ]]; then
  actual="$(sha256sum "$asset" | awk '{print $1}')"
  if [[ "$actual" != "$expected" ]]; then
    echo "STOP: project artwork does not match the approved PNG." >&2; exit 2
  fi
  echo "Approved artwork already exists locally; checking whether its commit reached GitHub."
  if GIT_TERMINAL_PROMPT=0 git push origin "$branch"; then
    echo "PUSH SUCCESSFUL: approved image source confirmed in remote GitHub."
  else
    echo "WARNING: image available LOCALLY but NOT CONFIRMED in GitHub; build can still proceed." >&2
  fi
  exit 0
fi
downloads="/storage/emulated/0/Download"
legacy="$downloads/Atelier_Time_Splash_User_Artwork_Preview.png"
apk="$downloads/Atelier-Time-D139-API36-Candidate.apk"
if [[ -s "$legacy" ]]; then
  cp "$legacy" "$asset"
  echo "Recovered exact original approved PNG from Downloads."
else
  if [[ ! -s "$apk" ]]; then
    installed_apk="$(pm path com.ateliertime.android 2>/dev/null | sed -n 's/^package://p' | head -n 1 || true)"
    if [[ -n "$installed_apk" && -r "$installed_apk" ]]; then apk="$installed_apk"; fi
  fi
  if [[ ! -s "$apk" ]]; then
    echo "STOP: no D-139 build APK or readable installed package to recover from." >&2
    exit 2
  fi
  if ! command -v unzip >/dev/null 2>&1; then
    echo "STOP: unzip command unavailable; cannot extract APK asset." >&2; exit 2
  fi
  if ! unzip -p "$apk" "assets/atelier_splash_approved_d139.png" > "$asset"; then
    rm -f "$asset"
    echo "STOP: the APK does not contain the approved artwork." >&2; exit 2
  fi
  echo "Recovered the exact originally approved PNG from APK."
fi
actual="$(sha256sum "$asset" | awk '{print $1}')"
if [[ "$actual" != "$expected" ]]; then
  rm -f "$asset"
  echo "STOP: extracted artwork hash mismatch; nothing was committed." >&2
  exit 2
fi
git add -- "$asset"
git commit -m "assets: preserve exact approved Atelier Time D-139 splash art"
echo "Pushing only the approved image source to GitHub; never running Actions."
if ! GIT_TERMINAL_PROMPT=0 git push origin "$branch"; then
  echo "WARNING: approved art is committed LOCALLY, but remote GitHub push FAILED." >&2
  echo "Build may proceed from the verified local source; preserve this Git checkout and send a screenshot." >&2
  exit 0
fi
echo "PUSH SUCCESSFUL: approved image is now versioned on GitHub in $branch."
