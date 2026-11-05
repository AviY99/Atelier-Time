# Atelier Time — Stable 15 / D-147 / versionCode 41

**Accepted:** 2026-11-05  
**Classification (user's wording):** **"עדכוני ממשק והתקנה"** — UI and installation/packaging improvements.  
**Status:** **STABLE 15 — USER-ACCEPTED, FROZEN; LATEST ACCEPTED SOURCE.**  
**App:** Native Android Kotlin/Jetpack Compose, package `com.ateliertime.android`, versionName `D-147`, versionCode **41**, compile/target API 36.

## Frozen recovery point — DO NOT MODIFY

- Branch: [`stable/android-d147-qa15-ui-install-accepted`](https://github.com/AviY99/Atelier-Time/tree/stable/android-d147-qa15-ui-install-accepted)
- Exact verified HEAD: **`c10f424c0ec000eca2dd561911934841decb4522`**
- Previous accepted Stable14 / D145 / code39: `stable/android-d145-qa14-field-accepted`, SHA `73bf60996234f739243b3974ac1d02c38ad065ba`, confirmed unchanged when freezing Stable15.
- Last D147 source/build-helper commit: `89d859e2e381b534edabf615972e16c75078e780`; comparison to frozen HEAD showed **only** `README.md`, `CHANGELOG.md` and `docs/` changed after that. No app code/build helper/resources changes between final source and frozen HEAD.
- Frozen Stable15 and all older stable branches are immutable: **no feature work, README edits, merges, force pushes or release signing on them**. Ongoing code stays on `android-native-kotlin-compose`, project-wide docs and Google Play media on `main`.

## Accepted integrated features

**D146/code40 UI improvement baseline, included in D147:**
1. Timer actions immediately below timer card, without redundant `Class controls` heading; five controls remain one row (Pause/Resume, Restart, Repeat Last, Next, Exit); Help below.
2. Opening-session current and next title explicitly show `Group 1 · Session 1/4`, etc.; group break `Group 1 · Break` and correct second/multiple group numbering. The underlying session IDs/timer counters are unchanged.
3. Sound picker retains a single row per cue, replacing long `Play 5s` labels with a triangular Play icon, 48dp touch target and screen-reader label including sound cue/seconds.
4. Approved startup illustration `atelier_splash_approved_d139.png` is already tracked in GitHub: local builder restores **only if worktree copy missing** from current Git HEAD, never overrides a modified local image; verifies original SHA256 **inside the final APK**. No external phone art, manual copy or extra upload.

**D147/code41 corrective improvement, included in Stable15:**
5. Running Timer top controls restored full, understandable text in **two closely packed rows**, not D146's cryptic single-row `Screen on`, `M` / `A` abbreviations: `Keep screen awake during timers` + `Uses more battery` beside the switch; `Class sound volume` + `Media N% · Alarm N% · using Media/Alarm` beside unchanged `Max both` button. 0dp explicit gap between rows, each control >=48dp height; 2dp parent gap and Timer top inset for compact visible runner. Screen/volume behavior and confirmation unchanged.
6. Preserve all D145 prior stable timer algorithms: special-break ordering by net observed/planned work, 0/1/2/3+ specials, one-shot Repeat Last and Next unlock, nonrepeat entered Specials, 25/5 safety, exact Class finish. No timer algorithm change in D146/D147.
7. `RunningClassQuickControls.kt` is the presentation-only component. Added 2 Compose AndroidTest UI methods for the full labels, compact alignment and callbacks; previous D146 UI tests remain.

## Local QA / user acceptance evidence

User provided screenshot of Samsung Code on the Go terminal after invoking `android-native/tools/build_d147_readable_timer_top_controls_on_phone.sh`:
- **`ALL JVM ACTUAL: tests=283 errors=0 failures=0 skipped=0`** — full freshly executed JVM suite including D146 group labels (6/6), D145 net-work specials (8/8) and replay rules (4/4), D144 timer (5/5), D144 display labels (4/4), D143 reset (2/2), D142 tail (9/9).
- **`ARTWORK AUDIT PASS`** for tracked approved images; **`APK ARTWORK INCLUDED PASS: assets/atelier_splash_approved_d139.png, SHA256 matched.`** — checked bytes inside the compiled APK.
- **`D-147 JVM TESTS AND BUILD SUCCESSFUL`**; local debug APK `/storage/emulated/0/Download/Atelier-Time-D147-API36-Candidate.apk`, local manifest `Atelier-Time-D147-API36-Manifest.txt` and private build log `Atelier-Time-D147-API36-Build.log`.
- **AndroidTest compiled, but instrumentation was not executed.** Never count these two device UI test methods as executed.
- Script does **not** install/uninstall, erase data, launch paid GitHub Actions, sign release AAB or submit to Play.
- After the successful build the user explicitly stated **"הכל עובד כנדרש זוהי גרסה יציבה הבאה , עדכוני ממשק והתקנה"**. This is the required device functional acceptance and Stable15 declaration, not independently stored per-screen video evidence. Do not claim a separate automated physical-device UI test matrix.

## Google Play status and decisions

**Price: FREE. First distribution: invite-only INTERNAL TESTING with personally invited small group.** These were chosen earlier; never reopen the choices without user request. Stable15 is the most recent accepted source to use for a **separately authorized signed release AAB**. A debug APK is **not** a Play publishing artifact.

No signed production AAB, internal-track upload, store review, Play release or public launch established. Existing privacy policy, approved Play store graphics/screenshots, bilingual listing/ASO, Data Safety/FGS drafts remain available from prior project docs. Reaudit against actual Stable15 release AAB/manifest before submission; release signing should be done locally on phone without GitHub paid CI and the signing key must never be pushed to GitHub. Never erase existing saved Classes, Sounds or diagnostic records.

**Next authoritative handoff:** [`HANDOFF_CURRENT_STABLE15_TO_GOOGLE_PLAY_2026_11_05.md`](HANDOFF_CURRENT_STABLE15_TO_GOOGLE_PLAY_2026_11_05.md).  
**D147 source/spec:** [Readable compact top controls](D147_RESTORE_FULL_TIMER_TOP_LABELS_COMPACT_TWO_ROWS_2026_11_05.md).  
**Previous stable:** [Stable14 certificate](STABLE14_D145_USER_ACCEPTANCE_AND_FIELD_REFINEMENTS_2026_11_03.md).
