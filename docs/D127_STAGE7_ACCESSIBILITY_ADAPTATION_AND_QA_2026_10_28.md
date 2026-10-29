> **CURRENT VERIFIED 2026-10-29 — D-127 Stage 7 SAMSUNG BUILD SUCCESSFUL (supersedes earlier FAIL/rebuild-pending paragraphs):** User's new Code on the Go screenshot shows `BUILD SUCCESSFUL in 25s`, 67 Gradle tasks (4 executed, 63 up-to-date), and `COMPILE/BUILD PASS`. Both D-127 candidate and AndroidTest APKs were produced in phone Downloads with local build manifest; guarded test runner was copied but NOT executed. This confirms source compile and APK packaging after test-only fix `62617aa`, **not** six Android accessibility UI test results, installation or manual TalkBack/font scale QA. GitHub dev HEAD at screenshot review was `c1d4fbdccf16351290de4615e7e5a2db60964806`; local artifact `source_commit` and SHA-256 values are recorded in the phone manifest but not independently inspected here. **Stage 7 OPEN, D-127/code21 NOT INSTALLED, Stable 9 D-126/code20 remains approved (historical 285/285 PASS), and Saved Classes/Sounds unchanged.** Next: separate explicit approval for data-preserving in-place installation, followed by only 6 new targeted AndroidTest methods with no active timer and focused manual accessibility QA. NO GitHub Actions/cloud builds, uninstall, clear data, or old-suite reruns. [Updated handoff](HANDOFF_D127_STAGE7_TO_NEXT_CHAT_2026_10_29.md).

# Atelier Time — D-127 Stage 7 accessibility implementation / QA gate

## 2026-10-29 — final handoff for new chat (supersedes dated entries below)

The user closed this conversation. **Use [HANDOFF_D127_STAGE7_TO_NEXT_CHAT_2026_10_29.md](HANDOFF_D127_STAGE7_TO_NEXT_CHAT_2026_10_29.md) as the authoritative next-chat starting point.** Latest **physical Samsung** build failed at `:app:compileDebugAndroidTestKotlin` after 19 seconds (61 tasks; 1 executed / 60 up-to-date), with `AccessibilityStage7UiTest.kt` errors at 115:64 and 116:83. **Source-only test correction already committed** as [`62617aa`](https://github.com/AviY99/Atelier-Time/commit/62617aa90005abfa9e1ed2845e9368c2952f3c98): `getUnclippedBoundsInRoot()` returns a `DpRect`, width is computed as `bounds.right - bounds.left`, then compared with `90.dp`. All six authored accessibility tests preserved. **No post-fix Samsung build or test has executed, Stage 7 remains open**. Next phone-only build from CoGo: `git pull --ff-only && bash tools/build_d127_accessibility_candidate_on_phone.sh`. Cost policy forbids GitHub Actions/cloud compilation; no install, data clearing or 285-test rerun. Frozen Stable 9 D-126/code20 remains approved and unchanged.



## 2026-10-29 — Samsung D-127 AndroidTest second compile failure, test-only fix

New local CoGo screenshot: `BUILD FAILED in 19s` with 61 Gradle tasks (1 executed, 60 up-to-date). The **only two shown Kotlin errors** are `AccessibilityStage7UiTest.kt:115:64` (no overload candidates) and `:116:83` (invalid `compareTo` operator). Both came from accessing `getUnclippedBoundsInRoot().width` without importing the `androidx.compose.ui.unit.width` extension; previous test-only fix did not compile. Corrected only AndroidTest source in [`62617aa`](https://github.com/AviY99/Atelier-Time/commit/62617aa90005abfa9e1ed2845e9368c2952f3c98): calculate `DpRect` width using `bounds.right - bounds.left`, which produces `Dp`, then compare with `90.dp`. Six Stage 7 test methods retained. GitHub readback confirms code; **post-fix Samsung build has NOT RUN**, six tests remain unexecuted, no install, and GitHub Actions remains disabled by the user's budget policy. Next: pull updated dev branch and run the established phone-only build script once; errors print locally upon failure. Stable 9 D-126 is immutable.



## 2026-10-29 — Samsung D-127 AndroidTest compile FAIL repaired in test source

A real on-phone local build screenshot shows `:app:compileDebugAndroidTestKotlin` failed after 47s. Two actual compiler errors: `AccessibilityStage7UiTest.kt:16:33` and `:114:60`, both `Unresolved reference 'assertWidthIsAtMost'`. `SessionEditorScreen.kt:245:36` (unnecessary safe call) and `:951:29` (redundant Elvis) are warnings, not build blockers. Inspected current AndroidTest source and confirmed the nonexistent test assertion is used once plus its import. **Fixed source-only** in [`6839242`](https://github.com/AviY99/Atelier-Time/commit/68392421f2a88bfcca1094260d11638f06182167): replace the unsupported `assertWidthIsAtMost` with Compose's documented `getUnclippedBoundsInRoot().width` and a standard JUnit `assertTrue(actualWidth <= 90.dp)`. All six existing tests remain; the main app code, timer and persistence were not touched. GitHub readback verified patch. **No post-fix Samsung rebuild, no AndroidTest execution, no GitHub Actions/cloud build and no installation** were performed. Next: `git pull --ff-only && bash tools/build_d127_accessibility_candidate_on_phone.sh` in the existing Samsung Code on the Go `android-native` folder. The phone build script automatically prints new Kotlin errors on failure. Stable 9 stays frozen D-126/code20. **Cost policy: Samsung-only builds, GitHub code/docs only.**



## 2026-10-29 — phone-only QA restored after user budget directive

The user explicitly reaffirmed: GitHub Actions/cloud compilation is prohibited because monthly GitHub spend already exceeded budget. **Build only on Samsung CoGo.** The workflow was removed (`0f2aab9`), unused cloud diagnostics helper removed (`b15ed1f`), and both historical GitHub Actions attempts are complete; no further cloud builds. Existing Actions run #2 logs had identified `MelodySettingsSection.kt` unresolved `testTag` at lines 158 and 195. The missing `testTag` import was fixed at `6837de7` **source-only**, with no claimed new Android compilation or test PASS.

Restored the previously successful phone terminal workflow: `android-native/tools/build_d127_accessibility_candidate_on_phone.sh` at `292cb8f` builds locally, saves the full log in `Download/Atelier-Time-D127-Stage7-build.log`, and automatically prints the first 30 relevant error lines in the terminal on failure; it **does not push, upload or run GitHub Actions**. New read-only `tools/show_d127_build_errors_on_phone.sh` at `c6e938c` prints errors from the *existing* saved log without rerunning Gradle. User may supply a screenshot of these terminal diagnostics as in prior project QA; **no raw log uploads requested**.

Remote log storage is not claimed functional: phone Git write credentials failed previously. A connected cloud-storage account with one-time Android automatic sync could be configured separately, but user connection and verified sync are required before we can access such logs. A daily notification guard monitors repository Actions activity (not actual GitHub account billing). D-127 six UI instrumentation tests still not executed, Stage 7 unaccepted, installed Stable 9 D-126/code20 unchanged.



> **CURRENT NON-NEGOTIABLE COST POLICY — 2026-10-29:** Per user instruction, **NO GitHub Actions / cloud Android builds or CI reactivation** due to monthly spending overrun. Removed workflow `.github/workflows/d127-android-compile.yml` (commit `0f2aab9`) and unused CI extractor (commit `b15ed1f`); verified two historical Actions runs completed, neither active. **Compile only locally on Samsung Code on the Go**. Source-only Kotlin fix for missing `testTag` import in `MelodySettingsSection.kt` was committed as [`6837de7`](https://github.com/AviY99/Atelier-Time/commit/6837de78741e1c56d9b7978096dd5cf064ca6477), **not yet rebuilt on phone**. Redacted diagnostic upload to GitHub requires ONE-TIME phone-side authorization; it is NOT operational yet (last git push failed); do not pretend remote reports are automatically accessible. A daily automation monitors repository Actions activity, NOT actual billed dollars; GitHub account Billing/Budgets access is not exposed by connector. [Mandatory cost/build policy](COST_AND_BUILD_POLICY_2026_10_29.md). Stable 9 D-126 unchanged.



## 2026-10-29 — GitHub-hosted automated D-127 compile and accessible reports

**Architecture change:** GitHub Actions is now the primary source-only Android compile gate. A free standard GitHub-hosted runner for this **public** repository compiles `android-native` on relevant development-branch code changes and saves full build logs in the Actions run plus a strictly redacted `d127-compiler-diagnostics` artifact (30-day retention). The GitHub connector can inspect the run, logs and artifact **directly**, without phone Git push credentials, local log handoffs or user uploads. [Workflow](https://github.com/AviY99/Atelier-Time/actions/workflows/d127-android-compile.yml) and [redaction source](https://github.com/AviY99/Atelier-Time/blob/android-native-kotlin-compose/.github/scripts/d127_extract_diagnostics.py).

**What this does and does not prove:** this is an independent **cloud build of the same Git revision**, NOT an upload of the previously failed Samsung build log. It does not install APKs, execute the old 285 tests, run the six new Android instrumentation methods, prove physical Samsung compatibility, touch app data or change Stable 9. The old on-phone Git push failed due to phone-side authorization or branch rejection; the cloud compile route avoids that dependency for future source compile diagnostics. Do not ask user to upload logs manually.

**Actual runs:** first [Actions run #1](https://github.com/AviY99/Atelier-Time/actions/runs/40889107144) automatically started, preserved its logs/artifact and failed while provisioning SDK because an obsolete action requested a nonexistent SDK package `tools` (not Kotlin source). Updated the workflow to use the hosted runner's preinstalled Android SDK. Second [run #2](https://github.com/AviY99/Atelier-Time/actions/runs/40889219808) reached Gradle/Kotlin compilation; verify its final status and fix from its exact GitHub log before attributing a compiler PASS or FAIL.



## 2026-10-29 — phone report generated but Git push FAILED; immediate handoff path

The user ran the previously prepared `--existing` reporter. Samsung screenshot confirms `PRIVATE raw log retained` and `PUBLIC redacted preview` exist under `/storage/emulated/0/Download`. **Push failed** with `UPLOAD FAILED: Git push not authorized or branch changed`. Repository review verifies the report branch exists, is not marked protected, and repository rulesets are empty; branch head remained unchanged, making missing phone-side **write authentication the likely cause, not a proven root cause**. No report is available on GitHub yet. Do not run Gradle again or rerun reporter merely to recover the same already-generated report.

Follow-up code commit [`047968f`](https://github.com/AviY99/Atelier-Time/commit/047968fd3d589406a2f2062f108ee40cb3a243ad) improves the publisher: capture private Git push output locally, print only an allowlisted reason category, show up to 20 already redacted compiler locations, and direct the user to share **only** `Atelier-Time-D127-Stage7-public-diagnostics.txt` from Downloads in this chat when phone Git push is not authorized. The assistant has connected GitHub write permissions and can upload a user-supplied, reviewed report after receipt. This does not remotely access the device or create an automatic file transport without the phone's push credentials.

D-127 latest observed build is still FAIL at `:app:compileDebugKotlin`; exact Kotlin compiler diagnostics remain unread by the assistant. Six targeted Stage 7 UI tests remain NOT RUN. Installed Stable 9 remains D-126/code20 with data intact; no edits to frozen branches.



## 2026-10-29 — diagnostics delivery branch initialized and verified

The dedicated public reporting branch [`diagnostics/d127-build-reports`](https://github.com/AviY99/Atelier-Time/tree/diagnostics/d127-build-reports) has now been **created in GitHub and independently read back** at `0695d12b2056a35a0107625848c18392029535ef`. It initially contains only a report-privacy README; **there is still no real D-127 compiler report**. Existing `publish_d127_build_diagnostics.sh` and the D-127 Gradle wrapper were already committed before this follow-up, so were not duplicated. The build wrapper invokes the redacted publisher on Gradle PASS/FAIL; actual publication requires push credentials on Samsung and has not been tested there.

**Recovery of the previous failed build without rebuilding:** run `git pull --ff-only && bash tools/publish_d127_build_diagnostics.sh --existing` from the Samsung CoGo `android-native` checkout. This reuses the old local `Download/Atelier-Time-D127-Stage7-build.log`, labels the original source SHA as unknown, and attempts to upload only allowlisted Kotlin filename/line/error categories to the already-created report branch. It does **not** rerun Gradle, install APKs, clear data or repeat old tests. Verify an actual `GITHUB UPLOAD VERIFIED` URL before treating the report as accessible. Never publish the raw log to the public repository.

D-127's last observed build remains **FAIL** at `:app:compileDebugKotlin`, and exact compiler diagnostics remain unverified until the phone's report is uploaded. Installed Stable 9 D-126/code20 remains unchanged.



## 2026-10-29 — build-report delivery repaired; past failed log still on Samsung

**Additional off-device verification:** ran an isolated synthetic AWK parse over three representative Kotlin error formats (reported only `SessionTimerScreen.kt:247 | experimental-api`, `SoundOptions.kt:12 | unresolved-reference` and `Other.kt:9 | type-mismatch`; deliberately excluded a fabricated bearer-token line). Also exercised git plumbing against a disposable local bare repository: a diagnostics-only report branch was created, its file read back and the source working tree remained clean. These tests verify the core redaction and isolated-branch mechanism, **not** Android/CoGo execution, remote GitHub permissions or D-127 compilation. The public diagnostic branch will not exist until the phone's authorized git push succeeds.



The D-127 build-only runner now keeps its raw log privately at `Download/Atelier-Time-D127-Stage7-build.log` and, on **either PASS or FAIL**, invokes [the redacted report publisher](../android-native/tools/publish_d127_build_diagnostics.sh). Only a strict allowlist of Kotlin **basename + line number + error category** and fixed build metadata can be published to GitHub's dedicated public [diagnostics/d127-build-reports](https://github.com/AviY99/Atelier-Time/tree/diagnostics/d127-build-reports) branch. **No raw log, absolute paths, source excerpts, credentials, user files, device IDs or private data** are published. Reports are locally previewable as `Download/Atelier-Time-D127-Stage7-public-diagnostics.txt`. Git plumbing uses an isolated temporary index and never pushes or commits `main`, the Android development ref or any frozen Stable branch. Git push uses existing phone credentials, rejects unexpected origin and never force-pushes; authentication/network failure is reported, not disguised as upload success, and the original build result is preserved.

The previously captured **compile FAIL log has NOT been uploaded**: it was produced before the publisher existed, and this chat has no access to Samsung Downloads. To publish just that existing log **without any Gradle rerun**, from the CoGo `android-native` checkout run `git pull --ff-only && bash tools/publish_d127_build_diagnostics.sh --existing`. The resulting public report labels the previous build SHA as unknown rather than falsely attributing the failure to today's source. Its exact compiler diagnostic (which could be sensitive in raw form) stays local. If the phone has no working GitHub push credentials, publishing will fail safely and the redacted report remains on-device.

The previously malformed, duplicated D-127 Termux six-test runner was also repaired and read back. Its exact app version/code guard, idle requirement and test-only install remain. Neither the corrected publisher/build nor runner has executed on Samsung after these changes. **Current D-127 Gradle build=FAIL (actual screenshot); root compiler error still unknown; six new UI tests=NOT RUN; app install=NOT DONE; Stage 7=OPEN.** Installed Stable 9 D-126/code20 unaffected.

## 2026-10-29 — real Samsung D-127 compilation attempt FAILED; root compiler diagnostic pending

User-supplied screenshot shows build script reached `:app:compileDebugKotlin` and **BUILD FAILED in 33s** with **53 actionable Gradle tasks (6 executed, 47 up-to-date)**. Gradle's final screen only says `Compilation error. See log for more details`; it **does not identify a Kotlin file, line or concrete diagnostic**, so no root cause or code correction is claimed. Build log is expected at `/storage/emulated/0/Download/Atelier-Time-D127-Stage7-build.log`, generated by the existing build-only runner. **Collect the existing log's Kotlin `e:` diagnostics instead of rerunning the build**. Verify/fix specific source only after inspecting those diagnostics; then compile D-127 candidate and six targeted AndroidTest methods. None of these six has yet executed. No APK installation, saved-data modification, completed Stage 7 or new stable designation is implied. Approved Stable 9 D-126/code20 remains the installed baseline. This FAIL replaces earlier statements that D-127 compilation had never been attempted; previous source-only documentation remains historical.
**Opened:** 2026-10-28. **Updated:** 2026-10-29. **Development branch:** `android-native-kotlin-compose`.
**Initial D-127 source commit:** [b9d7064](https://github.com/AviY99/Atelier-Time/commit/b9d7064cf5f9c68a288adac0a45a2909d541ee4b). **Latest accessibility-code commit (unbuilt):** [c83c197](https://github.com/AviY99/Atelier-Time/commit/c83c19738ea70bba0223acd84c46c93411276b7a) (previous b31105e).

## 2026-10-29 — further Design/Sound TalkBack completion (latest)

Read back existing D-127 work before editing; it already included responsive tabs, controls, narrow Editor/Free layouts, accessible wheels/chart and five unexecuted UI tests. Follow-up code commit [`c83c197`](https://github.com/AviY99/Atelier-Time/commit/c83c19738ea70bba0223acd84c46c93411276b7a) adds selected-state/RadioButton semantics and accessible names to Day/Night palette cards, a spoken hour label for the Design Auto slider, and spoken purpose/current-seconds descriptions for the two Sound duration sliders. It adds one isolated UI test for Day/Night selection and Auto slider label. This makes **six new Stage 7 AndroidTest methods written, zero executed**. The earlier five-test count below is dated history, not the current inventory.

**Actual verification:** all three edited files were read back from GitHub and six test declarations counted. No supported Android SDK or physical Samsung is accessible to this execution environment. **D-127/code21 is not Android-compiled, APK-built, installed, visually verified, TalkBack-tested or user-accepted.** The D-126 285/285 historical PASS does not apply to D-127. No app data was accessed, no stable refs were changed, and no full regression suite was repeated. Build only via the documented CoGo script on the supported phone/toolchain; then run six targeted D-127 tests and check 100%/130%/200% text, narrow/normal, both palettes, TalkBack focus/labels and touch targets. An in-place update to the main app requires separate explicit permission and preservation verification. Stages 8–12 remain deferred.

## 2026-10-29 — resumed QA gate review: exact-version safety check

Rechecked current GitHub dev/main and the six-test targeted Android QA protocol before doing more work. There are **no GitHub Actions workflow runs**, and no new Android/CoGo device-build logs have been supplied. Current workspace independently reconfirmed that Android SDK, Gradle, ADB and an attached Samsung are unavailable; therefore **no D-127 build, APK installation, instrumentation, visual matrix or TalkBack PASS** can be asserted.

The existing targeted Termux runner was audited and a narrow safety fix was made at [`25ea737`](https://github.com/AviY99/Atelier-Time/commit/25ea7378c1c58ad78e1a335b162623b3d142f2e5): package verification now matches the exact `versionName=D-127` and `versionCode=21` fields, rather than accepting substring matches such as `D-127x` or code `210`. This **does not install or execute anything**. Local isolated shell-condition checks returned ACCEPT for exact D-127/code21 and REJECT for D-126/code20, D-127x, code210 and missing version. These are static safety checks, **not** Android/device QA and **not** one of the six Android UI tests. The runner still refuses to replace the main app, requires idle user confirmation and installs only the test APK after a separately authorized D-127 upgrade.

**Next execution:** On the user's already configured Samsung Code on the Go checkout, run the existing **build-only** `bash tools/build_d127_accessibility_candidate_on_phone.sh` (after `git pull --ff-only`), return the generated `Atelier-Time-D127-Stage7-build.log` and manifest. Then a separately authorized in-place main-app update, six targeted UI tests, 100/130/200% and real TalkBack acceptance are required. The completed D-126 285/285 suite remains closed. Stage 7 is **SOURCE READY / DEVICE QA PENDING**, not accepted.

## 2026-10-29 — guarded D-127 build and six-test device runner prepared (NOT RUN)

- Confirmed this workspace has Java/Kotlin but **no Android SDK, Gradle, ADB, Samsung connection or outbound network access to acquire the SDK**. Android build and connected instrumentation cannot be executed here; no PASS is implied.
- Retained the pre-existing D-127 build-only script and tightened its guards: verify code ancestor `c83c197`, check exactly **six** authored `AccessibilityStage7UiTest` methods, and record `stage7_test_count=6` plus source SHA/APK hashes in its manifest. A successful build will also copy the dedicated Termux runner to `Download/Atelier-Time-D127-TargetedQA.sh`; **the script copies, but NEVER runs, installs or clears app data**.
- Added [guarded targeted Termux runner](../android-native/tools/termux_run_d127_targeted_accessibility_qa.sh), which verifies the manifest and test APK SHA-256, requires exactly one authorized ADB device, **requires D-127/versionCode21 already installed by separate explicit permission**, confirms no active Class/Free countdown and installs **only** the test APK. Runs **only the six new** `AccessibilityStage7UiTest` methods (not the previous 285), logging the actual result to Downloads and accepting PASS only upon `OK (6 tests)`. If Samsung still has D-126, it stops without replacing the main app.
- **Evidence status:** both scripts and six UI test source methods committed to dev, but none built/executed on Samsung. Real 100%/130%/200% font, narrow layouts, Day/Night, contrast, focus/TalkBack and user acceptance still outstanding. No Stable 10 assigned, no Stable 9 branch changes. Installed accepted D-126/code20 with prior 285/285 PASS and user data is preserved.
- Minimal later device gate: run `bash tools/build_d127_accessibility_candidate_on_phone.sh` inside the Android CoGo checkout for build-only logs; an in-place D-127 main app update requires separate explicit authorization and data-preservation check, after which the bundled Termux runner can run only the six targeted tests. Do **not** claim the device gate completed from these documented instructions.

## State — do not conflate versions
- **Approved and installed:** Stable 9 = D-126 / versionCode 20, 243/243 JVM + 42/42 on-device instrumentation = **285/285 PASS**. Saved Classes and Sounds were preserved.
- **New candidate:** D-127 / versionCode 21, source written and committed only. No Gradle build, APK, installation, connected AndroidTest, TalkBack exploration, screenshot acceptance, or user confirmation for D-127 has occurred.
- Five targeted Android instrumentation tests have been **AUTHORED, NOT EXECUTED**. Do not count them toward previous 285/285.
- Frozen `stable/android-d126-qa9-tests-complete` and historical Stable 8 branches remain unchanged. No app data or Samsung installation was touched.

## Implemented Stage 7 source changes
1. Six tabs: retain approved six-tab compact LTR arrangement at standard font scale. At `fontScale >= 1.3`, horizontally scroll tabs using >=104dp items and normal scaled typography instead of forcing 8–11sp small text.
2. Class Timer action and Help controls: wrap at large scale using FlowRow, retain previous weighting at ordinary font scale. Pause/Resume, Next, Restart and Exit callbacks and confirmation logic remain unchanged.
3. Custom EndClockWheel: add TalkBack contextual content descriptions for previous, current, next and read-only values; preserve stepwise feasibility boundary `canSelect` and blocked feedback.
4. Timeline Canvas: give schedule color bar equivalent textual phase-total and live elapsed/remaining description; no live announcement every second. Existing Editor chronological phase list remains.
5. Free Timer: minimum 48dp preset button height; adapt from four to two columns at large fonts; preserve all durations and manual Start.
6. Advance development identity to D-127/code21. No changes to `ConstraintFuturePlanner`, timing algorithms, Saved Classes, sound storage, background services or logs.

## QA evidence
- **Static source audit** identified high-scale tabs, small Free presets and custom Canvas/wheel semantics as targeted improvements. GitHub readback is source evidence only.
- Selected existing palette contrast ratios calculated statically: Day primary/white 5.10:1; Day secondary/white 4.81:1; Night primary/white 4.98:1; Night secondary/white 4.88:1; Night timer card/white 5.12:1; Day on-background 12.17:1; Night on-background 8.67:1. This is a sample, **not** full WCAG audit.
- **Initially 3 AndroidTest methods authored, not run:** `clockWheelButtonsHaveSpokenContextAndKeepBlockedBoundary`, `timelineHasEquivalentNonColorDescription`, `freePresetsHaveAtLeast48DpTouchHeight` in `AccessibilityStage7UiTest.kt`.

## Additional targeted Stage 7 changes — 2026-10-29

The user authorized Stage 7 as one continuous development task. Readback of the existing D-127 implementation showed two remaining responsive-layout gaps; neither was a proven device failure:

- Editor: three pairs of numeric inputs now stack vertically at Android fontScale >=1.3 or when inner content width <320dp; ordinary normal-width displays retain their approved two-column layout. All original field values, labels, setters and validation remain intact.
- Free HH:MM:SS: all three EndClockWheel columns now use measured available width (capped at their original 94dp) instead of potentially overflowing a small card. No timers, wheel constraints or sound callbacks were modified.
- Free presets now announce the selected state via Compose semantics; blocked adjacent finish-clock choices announce that the value is blocked and a tap explains why. Existing blocking and red/green behavior are unchanged.
- Two additional isolated AndroidTest methods were **written, not run**: `selectedFreePresetIsExposedToScreenReaders` and `narrowFreeTimerFitsAllThreeWheels`, bringing the Stage 7 new-method inventory to **five unexecuted tests**.
- Safe build-only script: [`android-native/tools/build_d127_accessibility_candidate_on_phone.sh`](../android-native/tools/build_d127_accessibility_candidate_on_phone.sh). It checks branch/clean tree/version and, on a supported Samsung CoGo installation, assembles a main APK and a test APK with source identity and SHA-256 manifest. It **does not run prior JVM tests, install either APK, execute instrumentation or touch app data**. Script committed but **not executed** in this environment.
- Static scope review after commits: `EndClockWheel.kt`, `FreeCountdownSection.kt`, `SessionEditorScreen.kt` and `AccessibilityStage7UiTest.kt`; no `ConstraintFuturePlanner`, persistence, services, notification, audio or stable-branch changes.

**Evidence boundary (2026-10-29):** GitHub source and test declarations exist, but Android SDK/physical Samsung are not available to this execution environment. Android Gradle/Compose compile, five new UI tests, font-scale/day-night screenshot matrix and real TalkBack must still be executed and their results documented. Earlier D-126 285/285 PASS must not be attributed to D-127. Do not declare Stage 7 accepted or create Stable 10 without real evidence and user designation.

## Completion criteria — PENDING
- Build and targeted Compose tests for D-127 in supported Android/CoGo environment; record actual logs and any corrections.
- Device screen matrix: 100%, 130%, 200% system font scale; normal and narrow widths; Day/Night; six tabs, Editor side-by-side fields, Timer clock header/buttons/help, charts, dialogs, Sound settings, wheels, Free preset controls.
- Real TalkBack: focus order, spoken button/value context, chart alternative, dialogs, navigation and avoidance of per-second unsolicited announcements.
- Verify touch target size and all relevant normal/selected/disabled/error contrast combinations.
- Approve **in-place update only** separately, then verify Saved Classes/Sounds remain intact; never uninstall/clear user data.
- Record actual PASS/FAIL and user acceptance before marking Stage 7 done or creating another stable checkpoint. **No repeated full D-126 285-test sweep without a specific regression reason.**

## Branch/document contract
Android source lives only on `android-native-kotlin-compose`. Sync shared canonical documentation to `main` without merging Android code. The official installed status remains Stable 9/D-126 until new explicit user acceptance.
