# D-134 — Run diagnostic reports: Delete all and 5-visible scrollable history (2026-10-30)

## Verified previous candidate and user request
- User states **D-133 / versionCode 27 installed and everything works**. This is user-reported acceptance, not a claim of rerunning prior 285 automated tests.
- New user request: add **Delete all** to **Run diagnostic report history**, and display **only the latest 5 in the visible list**, with a **scrollable list** to see all older reports.
- Preserve existing single-report **Export** and **Delete**, report ordering and content; do **not** discard older reports automatically.

## Implementation scope
- Android dev branch only: **D-134 / versionCode 28**, compileSdk/targetSdk **36**.
- `RunDiagnosticsArchive.deleteAll()`: lists only local private `run-*.jsonl` report files in `atelier_run_reports`, removes all matching files and returns the count; does NOT delete the directory itself, other app files, Saved Classes, Sounds, Run preferences or any exported user copies. Reports partial file-deletion failures.
- `RunDiagnosticsLibrary`: a red `Delete all` text button (disabled if no reports); destructive **AlertDialog** showing the current report count and requiring explicit confirmation or Cancel. After attempt, refresh list and report any error. Existing per-file deletion confirmation is unchanged.
- `LazyColumn` with a bounded **280dp list viewport** and rows with a minimum height of **56dp**: normally five recent reports are visible; all older reports are reachable by scrolling **inside the list**, rather than taking unlimited height on the Timer screen. Newest-first sorting inherited from current Archive. Dynamic font-size increase may naturally reduce simultaneous visible rows; the list remains scrollable and accessible.
- New isolated instrumentation test uses test-only cache-backed `RunDiagnosticsArchive`, writes eight synthetic diagnostic reports plus one unrelated file, verifies sorted order, delete-all removes all eight, unrelated file preserved and second delete-all returns zero. **Test authored, NOT EXECUTED**, prior accepted QA not rerun.
- No timer scheduling/audio/FGS/notifications, Saved Classes/Sounds semantics, privacy/Instagram/WhatsApp/Design screen or Android permission changes. No automatic bulk deletion; only button+user confirmation.

## Local release gates
- Code/docs/source prepared in GitHub; **D-134 not yet built, installed or user-tested**.
- Build **only locally in Samsung Code on the Go** from `android-native`: `git pull --ff-only && bash tools/build_d134_diagnostic_history_on_phone.sh`. Existing build helper checks branch/API36/version identity/resources, D134 list and confirmation logic, calls only `:app:assembleDebug` and exports candidate APK, log and SHA256 manifest to Downloads. No installation, paid GitHub Actions, cloud compilation or diagnostic data changes during build.
- After BUILD SUCCESSFUL, obtain separate permission before **in-place** install over D-133 (do not uninstall/clear data).
- Focused QA: with >5 reports, only five rows visible initially; swipe list to see older entries; existing Export and single Delete work; Cancel in bulk confirmation changes nothing; accepting bulk deletion removes all reports including older not visible; empty-state and disabled Delete all correct; Saved Classes and Sound settings remain intact.
- **Deletion is irreversible**, so use QA only with reports that are safe for the user to discard (or export any needed reports first). Do not advise bulk deletion of valuable diagnostic logs without the user's confirmation.
- Last explicitly designated frozen stable checkpoint remains **Stable 9 / D-126**; D-133 is latest user-confirmed installed candidate, not automatically Stable 10. ASO-01 and Play Console tasks unchanged.
