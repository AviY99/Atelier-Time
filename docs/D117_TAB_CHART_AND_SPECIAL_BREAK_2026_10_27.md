# D-117 — tab labels, two adjacent bars, future special-Break rebalancing

**Date:** 2026-10-27
**Branch:** `android-native-kotlin-compose` only
**App identity:** `versionName=D-117`, `versionCode=12`
**Status:** source candidate. Kotlin/JVM 209/209 PASS locally; CoGo Android build, APK installation and phone acceptance **not yet run**. The user reported D-116 installed and working except the three problems below. Stable 5/6 frozen.

## User-observed D-116 defects and approved D-117 behavior

1. **Six top-tab titles cut off** (screenshot shows Editor/Sound/Design truncated). Replace Material Tab's internal horizontal padding with six truly equal-width selectable cells and compact full labels; maintain LTR order `Editor | Timer | Free | Sound | Design | About` and semantic selected-tab accessibility.
2. **Elapsed conflated with the schedule bar.** Two bars, vertically adjacent: *Elapsed* is an independent 11dp physical-Class progress bar; the colorful actual-past/updated-future schedule is 33dp (75% of prior 44dp, elapsed thickness exactly one third). There is no elapsed overlay on the colored schedule. The completed left portion uses **measured actual performed durations** (e.g. Next after one minute of a five-minute Session renders one minute); the right portion is reflowed prospectively. Class elapsed/remaining stay inside the card; live `Time by activity` remains hidden.
3. **Automatic special Break drifts too early after many Next presses** as regular Session+Break blocks accumulate near the ending. After Next, compare the center of each *unstarted* automatic special Break with the corresponding fraction of the **physical Class duration** (halfway for one special; evenly spaced targets for several). Move the Break only by exchanging it with an available future, ordinary numbered Break, then re-time the unstarted phases. Choose the feasible slot with least center error, not a forced exact midpoint. Preserve all already-entered/completed phases, physical Class end, Summary, number and durations of Breaks, and stored approved Class. A manually numbered special Break never moves. If no future eligible Break exists, retain the current position rather than rewriting history.

## Runtime normalizer integrity

`SpecialBreakRebalancer` works entirely on the unscheduled portion and retains the exact total duration. Moving a special Break changes the identity/kind and length of future Break slots relative to the saved plan. Therefore subsequent Next operations use `FinalPairCompensation.adjustFutureTail` if a complete saved-plan reflow would modify an already performed phase. The incremental normalizer protects the Next destination and all preceding runtime history while reusing the D-116 strict `A+B > 2×regular Session + regular Break` rule and one-minute minimum. The approved Class record is never modified.

## Evidence and gates

- Local Kotlin compiler 2.2.0 / JUnit4: **209/209 PASS**, 22 test suites, 0 failures; 205 D-116 regressions plus 4 new tests for midpoint improvement, manual/observed immutability, protected reflow and many Next presses.
- `git diff --check` and `bash -n` D-117 scripts PASS.
- **Still pending:** actual CoGo Gradle Compose/Android tests, D-117 update-install, physical screenshot/tab sizing, sustained Next/special-break behavior and full Android instrumentation. D-116 on-phone `BUILD SUCCESSFUL in 1m19s`, 205/205 JVM PASS, user-reported operation **cannot** be attributed to D-117.

### Next permitted stage — build only, on Samsung in Code on the Go

```bash
cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native
git switch android-native-kotlin-compose
git pull --ff-only
bash tools/build_d117_candidate_on_phone.sh
```

Produces `Download/Atelier-Time-D117-Candidate.apk`, `Atelier-Time-D117-Tests.apk`, and D-117 manifest/log, with no APK installation or data wipe. Wait for build result and separate user approval before update-install; never uninstall, `pm clear`, overwrite either saved Class or Sounds. Test-only instrumentation via `tools/termux_run_d117_qa.sh` is a separate later authorization gate and requires working ADB.

### Focused post-install visual/manual acceptance

1. All six complete tab titles visible in the installed layout, both Day and Night.
2. Timer card: **11dp Elapsed above 33dp timeline**, adjacent; Class elapsed/remaining inside; historical slices after Next/Restart reflect reality; revised future ahead. No live Time-by-activity legend.
3. Execute several **confirmed** Next actions in an expendable test Class: automatic special Break should remain as near the middle as future valid Break slots allow, rather than being left near the beginning with newly added regular cycles after it. Once the special Break starts, it must not jump or disappear. Manual Break placement remains fixed.
4. Original calculated physical Class end unchanged after skips; original two saved Classes and Sounds retained. Do not use production classes for test edits.

Do not declare D-117 or any new Stable milestone until user phone acceptance and separate QA evidence.
