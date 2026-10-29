#!/usr/bin/env python3
"""D-127 GitHub CI build report: public-safe Kotlin filename/line/category only."""
import pathlib
import re
import sys

raw_path, output_path, sha, outcome = sys.argv[1:5]
source = pathlib.Path(raw_path)
raw = source.read_text(encoding="utf-8", errors="replace") if source.is_file() else ""
patterns = [
    ("unresolved-reference", ("unresolved reference",)),
    ("experimental-api", ("experimental", "opt-in", "requires optin")),
    ("type-mismatch", ("type mismatch", "argument type mismatch")),
    ("overload-resolution", ("overload resolution",)),
    ("unmatched-overload", ("none of the following",)),
    ("too-many-arguments", ("too many arguments",)),
    ("missing-argument", ("no value passed",)),
    ("inaccessible-symbol", ("cannot access",)),
    ("syntax-error", ("expecting", "syntax")),
    ("conflicting-overload", ("conflicting overloads",)),
]
matches = []
for line in raw.splitlines():
    # Kotlin K2 uses file:///.../Foo.kt:123:4 and legacy diagnostics
    # Foo.kt: (123, 4). Never copy directories/messages to the report.
    hit = re.search(r"([A-Za-z0-9_.-]+\.kt):\s*\(?([1-9][0-9]{0,6})(?=[:\s,)]|$)", line)
    if hit is None:
        continue
    lower = line.lower()
    if not ("error:" in lower or "e: " in lower or "error " in lower):
        continue
    category = next((name for name, needles in patterns
                     if any(needle in lower for needle in needles)),
                    "other-kotlin-compiler-error")
    record = f"{hit.group(1)}:{hit.group(2)} | {category}"
    if record not in matches:
        matches.append(record)
    if len(matches) >= 80:
        break
outcome = "PASS" if outcome == "success" else "FAIL"
sha = sha if re.fullmatch(r"[a-f0-9]{40}", sha) else "UNKNOWN"
lines = [
    "Atelier Time D-127 GitHub CI Kotlin compilation report",
    "schema=compiler-locations-v1",
    "version=D-127",
    f"source_commit={sha}",
    f"build_result={outcome}",
    "raw_build_log=GITHUB_ACTIONS_ONLY",
    "diagnostics:",
    *(matches or ["no-allowlisted-kotlin-file-line-diagnostic"]),
]
pathlib.Path(output_path).write_text("\n".join(lines) + "\n", encoding="utf-8")
print("\n".join(lines))
