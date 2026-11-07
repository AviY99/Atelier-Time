#!/usr/bin/env bash
# D-127: publish ONLY allowlisted compiler locations/categories to a GitHub
# diagnostics branch. Never publish the raw log, usernames, paths or tokens.
# Uses existing git push credentials; does not alter the checkout, main, or stable.
set -euo pipefail

if [[ $# -ne 1 ]] || [[ "$1" != "--existing" && "$1" != "--failed" &&
                         "$1" != "--passed" ]]; then
  echo "Usage: bash tools/publish_d127_build_diagnostics.sh --existing|--failed|--passed"
  exit 2
fi
result="FAIL"
[[ "$1" == "--passed" ]] && result="PASS"
cd "$(dirname "$0")/.."
[[ "$(git branch --show-current)" == "android-native-kotlin-compose" ]] || {
  echo "DIAGNOSTICS NOT UPLOADED: Android development branch required."
  exit 3
}
remote="$(git remote get-url origin)"
case "$remote" in
  https://github.com/AviY99/Atelier-Time|https://github.com/AviY99/Atelier-Time.git|git@github.com:AviY99/Atelier-Time|git@github.com:AviY99/Atelier-Time.git) ;;
  *) echo "DIAGNOSTICS NOT UPLOADED: unexpected GitHub origin; no report sent."; exit 3 ;;
esac

downloads="/storage/emulated/0/Download"
log="$downloads/Atelier-Time-D127-Stage7-build.log"
report="$downloads/Atelier-Time-D127-Stage7-public-diagnostics.txt"
[[ -s "$log" ]] || {
  echo "DIAGNOSTICS NOT UPLOADED: existing build log not found at $log."
  exit 3
}

# Fail closed: publish only fixed metadata and Kotlin filename/line and
# a category from a closed allowlist. All raw compiler messages and absolute
# paths stay in the private Downloads log. The repo is PUBLIC.
stamp="$(date -u +%Y%m%dT%H%M%SZ)"
source_sha="$(git rev-parse HEAD)"
# A previously captured log predates this reporter; its build SHA was not
# recorded on failed builds. Never falsely attribute it to the updated HEAD.
[[ "$1" == "--existing" ]] && source_sha="UNKNOWN_ORIGINAL_BUILD_SHA"
{
  echo "Atelier Time public, redacted D-127 Android build diagnostics"
  echo "schema=compiler-locations-v1"
  echo "version=D-127"
  echo "source_commit=$source_sha"
  echo "timestamp_utc=$stamp"
  echo "build_result=$result"
  echo "full_raw_log=LOCAL_ONLY_NOT_PUBLISHED"
  echo "diagnostics:"
  awk '
    function kind(s) {
      s=tolower(s)
      if (index(s,"unresolved reference")) return "unresolved-reference"
      if (index(s,"experimental") || index(s,"opt-in")) return "experimental-api"
      if (index(s,"type mismatch") || index(s,"argument type mismatch")) return "type-mismatch"
      if (index(s,"overload resolution")) return "overload-resolution"
      if (index(s,"none of the following")) return "unmatched-overload"
      if (index(s,"too many arguments")) return "too-many-arguments"
      if (index(s,"no value passed")) return "missing-argument"
      if (index(s,"cannot access")) return "inaccessible-symbol"
      if (index(s,"syntax") || index(s,"expecting")) return "syntax-error"
      if (index(s,"conflicting overloads")) return "conflicting-overload"
      return "other-kotlin-compiler-error"
    }
    {
      if (!match($0, /[A-Za-z0-9_.-]+\.kt:[[:space:]]*\(?[0-9]+/)) next
      entry=substr($0,RSTART,RLENGTH)
      file=entry
      sub(/:.*/, "", file)
      line=entry
      sub(/^.*\.kt:[[:space:]]*\(?/, "", line)
      if (file !~ /^[A-Za-z0-9_.-]+\.kt$/ || line !~ /^[0-9]+$/) next
      if (++n <= 80) print file ":" line " | " kind($0)
    }
    END {
      if (n==0) print "no-allowlisted-kotlin-file-line-diagnostic"
      if (n>80) print "additional-diagnostics-omitted"
    }
  ' "$log"
} > "$report"
echo "PRIVATE raw log retained: $log"
echo "PUBLIC redacted preview: $report"

# Git plumbing writes a separate report-only branch with a temporary index.
# It never commits to or pushes the active development/main/stable branches.
export GIT_TERMINAL_PROMPT=0
ref="refs/heads/diagnostics/d127-build-reports"
if ! remote_line="$(git ls-remote origin "$ref")"; then
  echo "UPLOAD FAILED: cannot contact GitHub with existing git credentials."
  exit 4
fi
parent="$(printf '%s\n' "$remote_line" | awk 'NR==1{print $1}')"
temp_index="$(mktemp)"
rm -f "$temp_index"
trap 'rm -f "$temp_index"' EXIT
export GIT_INDEX_FILE="$temp_index"
if [[ -n "$parent" ]]; then
  if ! git fetch --quiet --no-tags origin "$ref"; then
    echo "UPLOAD FAILED: cannot retrieve existing diagnostics branch."
    exit 4
  fi
  parent="$(git rev-parse FETCH_HEAD)"
  git read-tree "$parent^{tree}"
else
  git read-tree --empty
fi

short_sha="$(git rev-parse --short=12 HEAD)"
outpath="D127/$stamp-$short_sha-$result.txt"
blob="$(git hash-object -w "$report")"
git update-index --add --cacheinfo 100644 "$blob" "$outpath"
tree="$(git write-tree)"
if [[ -n "$parent" ]]; then
  commit="$(printf 'QA: redacted D-127 build %s\n' "$result" |
    GIT_AUTHOR_NAME="Atelier Build Reporter" GIT_AUTHOR_EMAIL="atelier-build@users.noreply.github.com" \
    GIT_COMMITTER_NAME="Atelier Build Reporter" GIT_COMMITTER_EMAIL="atelier-build@users.noreply.github.com" \
    git commit-tree "$tree" -p "$parent")"
else
  commit="$(printf 'QA: first redacted D-127 build %s\n' "$result" |
    GIT_AUTHOR_NAME="Atelier Build Reporter" GIT_AUTHOR_EMAIL="atelier-build@users.noreply.github.com" \
    GIT_COMMITTER_NAME="Atelier Build Reporter" GIT_COMMITTER_EMAIL="atelier-build@users.noreply.github.com" \
    git commit-tree "$tree")"
fi
# Never force-push. Capture private Git stderr locally rather than hiding
# all reasons behind "not authorized or branch changed". Never echo raw stderr
# because credential helpers/remotes may include sensitive details.
push_log="$(mktemp)"
trap 'rm -f "$temp_index" "$push_log"' EXIT
if git push --porcelain origin "$commit:$ref" >"$push_log" 2>&1; then
  echo "GITHUB UPLOAD VERIFIED: https://github.com/AviY99/Atelier-Time/blob/diagnostics/d127-build-reports/$outpath"
else
  if grep -Eiq 'authentication failed|could not read username|permission denied|not authorized|write access|http[^[:space:]]* 403|403 forbidden|403|repository not found|password authentication' "$push_log"; then
    echo "UPLOAD FAILED: GitHub write credentials are missing or unauthorized on this phone."
    echo "A successful git pull proves read access, NOT git push authorization."
  elif grep -Eiq 'non-fast-forward|fetch first|stale info|rejected|remote rejected' "$push_log"; then
    echo "UPLOAD FAILED: remote diagnostics branch changed or server rejected the update."
    echo "Do not force-push. Retry only after reviewing repository permissions."
  else
    echo "UPLOAD FAILED: Git could not publish (reason undetermined; private stderr retained only on phone until script exits)."
  fi
  echo "NO BUILD RERUN REQUIRED. The SAFE text report is already available at:"
  echo "$report"
  echo "Attach ONLY that .txt file to this ChatGPT conversation; it contains"
  echo "allowlisted Kotlin filename/line/category metadata, not your raw log."
  echo "When provided in chat, the connected assistant can commit the safe report"
  echo "to GitHub without needing write credentials on your phone."
  echo "Redacted diagnostic summary:"
  awk '/^diagnostics:/{in_diag=1;next} in_diag && (count++ < 20){print}' "$report"
  echo "Original build output remains local: $log"
  exit 5
fi
