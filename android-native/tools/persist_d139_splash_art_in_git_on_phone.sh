#!/usr/bin/env bash
# D-140 corrective archival: recover and commit the exact approved D-139
# image into GitHub source. Does not clear data, install, build or run CI.
# Never delete or stage Code on the Go's unrelated ../.cg/ directory.
set -euo pipefail
cd "$(dirname "$0")/.."
branch="android-native-kotlin-compose"
asset="app/src/main/assets/atelier_splash_approved_d139.png"
source_jpg="artwork/sources/vitruvian_clock_user_source_2026-10-31.jpg"
expected="c7d69a52c53a86738cc416b4c29eb621f8792cf367539732ce0ffa3639d8460d"
expected_source="70cb814352ff42eacb74bcfb4a853ede73204cc0dafbbc40ebb73699a15a8653"
downloads="/storage/emulated/0/Download"

if [[ "$(git branch --show-current)" != "$branch" ]]; then
  echo "STOP: expected $branch branch" >&2; exit 2
fi
# Scope deliberately ignores untracked workspace items such as ../.cg/.
# Still protect every tracked file and only allow the one expected new asset
# inside Android source. This prevents accidentally committing build output.
if [[ -n "$(git status --porcelain --untracked-files=no)" ]]; then
  echo "STOP: tracked source changes detected, no files changed by this helper" >&2
  git status --short
  exit 2
fi
while IFS= read -r unknown; do
  if [[ "$unknown" != "$asset" ]]; then
    echo "STOP: untracked Android source file: $unknown" >&2
    echo "Resolve deliberately; do not delete workspace files" >&2
    exit 2
  fi
done < <(git ls-files --others --exclude-standard -- app/src tools)

mkdir -p "$(dirname "$asset")"
if [[ -s "$asset" ]]; then
  echo "Approved image already present in Android assets; verifying integrity"
else
  legacy="$downloads/Atelier_Time_Splash_User_Artwork_Preview.png"
  if [[ -s "$legacy" ]]; then
    cp "$legacy" "$asset"
    echo "Recovered approved image from Downloads"
  else
    if ! command -v unzip >/dev/null 2>&1; then
      echo "STOP: unzip is required to extract the original approved APK asset" >&2; exit 2
    fi
    found=false
    # Prefer the known D-139 APK; build/outputs is often preserved even after
    # the user deletes the Download PNG. No APK gets modified.
    for candidate in "$downloads/Atelier-Time-D139-API36-Candidate.apk" \
      "app/build/outputs/apk/debug/app-debug.apk"; do
      if [[ -s "$candidate" ]]; then
        if unzip -p "$candidate" assets/atelier_splash_approved_d139.png > "$asset" 2>/dev/null; then
          sha="$(sha256sum "$asset" | awk '{print $1}')"
          if [[ "$sha" == "$expected" ]]; then
            found=true
            echo "Recovered SHA-verified approved image from $candidate"
            break
          fi
        fi
        rm -f "$asset"
      fi
    done
    if [[ "$found" == false ]]; then
      # If available through Code on the Go, the installed package is another
      # source; no permission or privileged data access is requested.
      installed="$(pm path com.ateliertime.android 2>/dev/null | sed -n 's/^package://p' | head -n 1 || true)"
      if [[ -n "$installed" && -r "$installed" ]] &&
         unzip -p "$installed" assets/atelier_splash_approved_d139.png > "$asset" 2>/dev/null; then
        if [[ "$(sha256sum "$asset" | awk '{print $1}')" == "$expected" ]]; then
          found=true
          echo "Recovered approved image from installed APK"
        fi
      fi
    fi
    if [[ "$found" == false ]]; then
      rm -f "$asset"
      echo "STOP: approved image unavailable in PNG, D-139 APK or installed package" >&2
      echo "Nothing installed or changed in user data; do not invent replacement artwork" >&2
      exit 2
    fi
  fi
fi
if [[ "$(sha256sum "$asset" | awk '{print $1}')" != "$expected" ]]; then
  echo "STOP: approved artwork SHA-256 mismatch; image will NOT be committed" >&2
  exit 2
fi

# Optional historical source archive: only take the exact original upload
# from the known Download filename if present; never search personal folders.
if [[ -s "$downloads/1000099031.jpg" ]]; then
  if [[ "$(sha256sum "$downloads/1000099031.jpg" | awk '{print $1}')" == "$expected_source" ]]; then
    mkdir -p "$(dirname "$source_jpg")"
    if [[ ! -e "$source_jpg" ]]; then
      cp "$downloads/1000099031.jpg" "$source_jpg"
    fi
    if [[ "$(sha256sum "$source_jpg" | awk '{print $1}')" != "$expected_source" ]]; then
      echo "STOP: source JPG archive hash mismatch" >&2; exit 2
    fi
    echo "Found and staged exact original source JPG for art archive"
  else
    echo "NOTE: Download/1000099031.jpg does not match approved source; ignoring it"
  fi
fi

commit_paths=("$asset")
git add -- "$asset"
if [[ -s "$source_jpg" ]] &&
   [[ "$(sha256sum "$source_jpg" | awk '{print $1}')" == "$expected_source" ]]; then
  git add -- "$source_jpg"
  commit_paths+=("$source_jpg")
fi
if ! git diff --cached --quiet -- "${commit_paths[@]}"; then
  git commit -m "assets: archive exact approved D-139 splash artwork" -- "${commit_paths[@]}"
fi
if ! git cat-file -e "HEAD:android-native/$asset" 2>/dev/null; then
  echo "STOP: approved image did not enter the current Git commit" >&2; exit 2
fi
echo "Exact approved image committed to LOCAL Git source"
if GIT_TERMINAL_PROMPT=0 git push origin "$branch"; then
  echo "PUSH SUCCESSFUL — approved image is in GitHub, $branch"
else
  echo "WARNING: remote push unavailable. Image is committed LOCALLY, not confirmed in GitHub" >&2
  echo "Local D-140 build can continue; preserve checkout, no reset/clean" >&2
fi
