# Atelier Time — Stable 14 / D-145 — Field-tested user-accepted checkpoint

**Accepted:** 2026-11-03  
**Status:** **STABLE 14 — USER-ACCEPTED AND FROZEN**  
**App:** native Android Kotlin + Jetpack Compose; `com.ateliertime.android`; **versionName D-145 / versionCode 39**, compileSdk/targetSdk 36, minSdk 26.  
**Purpose:** **תיקוני משתמשים, שדרוגים לאלגוריתם ולתפעול מלקחי שימוש** — user-requested reliability, algorithm and operating refinements based on actual field use.

## Official immutable recovery checkpoint

**Frozen branch:** [`stable/android-d145-qa14-field-accepted`](https://github.com/AviY99/Atelier-Time/tree/stable/android-d145-qa14-field-accepted)  
**Verified frozen HEAD:** `73bf60996234f739243b3974ac1d02c38ad065ba`.  
**Latest prior stable:** Stable 13 / D-143 / code37 at `stable/android-d143-qa13-user-accepted` (SHA `2a40f2d9ae79fa6eee41dcb387840d22b5caccf6`), independently checked unchanged when freezing Stable14.

Source audit: After the last D-145 production/test-source commit `4bfcac462f5f8f1952880e77c0f8e3a685b0ffd4`, the development HEAD used as the frozen source added **documentation only** (`README.md` and `docs/`), not changes to application source, tests, resources or build configuration. Frozen source SHA has `android-native/app/build.gradle.kts` `versionName = "D-145"` / `versionCode = 39`, and contains the new timer/rebalancer/UI code.

Do **not** edit this stable branch in subsequent development or for documentation edits. Active development stays on `android-native-kotlin-compose` with a **future, separately authorized version increment**; documentation and release/Play assets live on `main`.

## Changes accepted in the D-144 → D-145 field refinement

The user reported that D-144/code38 worked apart from (a) automatic Special Break placement becoming undesirable after repeated timer operations and (b) Timer action controls using two rows. D-145 was developed as **one approved integrated change set**:

1. **Actual-work-based automatic special-rest placement.** After a successful `Next`, `Restart` or `Repeat Last`, re-evaluate unentered automatic Special Breaks against **observed work actually performed plus planned future work**, excluding completed/planned breaks, pause, summary and merely skipped virtual time. Targets for N breaks are ordinal fractions i/(N+1) of cumulative net work. Assign all remaining special breaks together to different legal future regular break slots with ordered dynamic programming, keeping actual past, fixed finish and summary unchanged; preserve manual special break positions and configured opening-group breaks. No per-tick relocation.
2. **Special-break nonrepetition.** Once a Special Break has been entered, the user cannot `Restart` or `Repeat Last` that Special Break; future automatic relocation does not recreate it. A `Repeat Last` into a currently active special is also blocked.
3. **One-shot `Repeat Last` UX.** Repeat the immediately previous eligible regular Session/Break **fully**. Lock `Repeat Last` while its replay attempt is active; unlock upon natural completion or successful `Next` (when the just-exited phase becomes the new previous); `Restart` does not clear the lock. Rejected actions leave state unchanged. Existing actual-work 25/5 consent and fixed Class finish checks remain.
4. **Compact one-row Timer actions.** Five actions in a single horizontal row: Pause/Resume, Restart, **Repeat Last** displayed in two lines within one control, Next and Exit. Compact Help access and TalkBack full control name retained; smaller screens may scroll horizontally rather than wrap.

D-144's previous user field enhancements (split-screen mitigation, sound focus/test signal after Class approval, voluntary Media/Alarm max, opening 1/4..4/4 labels, Next phase duration) remain in this accepted checkpoint.

## Verification: automated vs user report

**User-provided Samsung Code on the Go terminal screenshot of D-145 build:**
- `ARTWORK AUDIT PASS`, approved app images checked.
- `ALL JVM ACTUAL: tests=277 failures=0 errors=0 skipped=0` — full executed JVM suite.
- D145 new targeted tests: `D145NetWorkSpecialBreakTest` **8/8**, `D145RepeatOnceSpecialSafetyTest` **4/4**.
- Existing targeted suites: D144 repeat 5/5, D144 display labels 4/4, D143 Reset Editor 2/2, D142 tail recovery 9/9.
- Build script printed `D-145 JVM TESTS AND BUILD SUCCESSFUL` and created local debug candidate `/storage/emulated/0/Download/Atelier-Time-D145-API36-Candidate.apk`, with corresponding private local manifest/log in Downloads.
- Android instrumentation `AndroidTest` **compiled but was not run**; do not misstate as runtime instrumented PASS.
- The build script itself did **not** install, delete data, sign an AAB, upload or trigger paid cloud CI.

**User acceptance after build:** The user wrote **"הכל עובד כנדרש זוהי גרסה יציבה הבאה , תיקוני משתמשים , שדרוגים לאלגוריתם ולתפעול מלקחי שימוש"**. This directly authorizes recording the next Stable milestone and indicates user-reported functional acceptance on device. It does **not** establish separately captured screenshots/videos for each edge case, a byte-readback/independent APK checksum, or external Play release QA. Do not invent those artifacts or claim a new device instrumentation execution.

## Safeguards and release status

- **Stable14 frozen; Stable13/12/11/10 and all earlier frozen branches unchanged.** Do not overwrite, force-push or merge future work onto them.
- The app's saved Classes, Sounds, diagnostics, original approved artwork and user device state must remain protected. No uninstall or data clear.
- **No signed release AAB, Google Play distribution, production rollout or Play Console submission** is established by this acceptance. Google Play preparation and policies remain a separate controlled workflow.
- Approved store media archive remains on `main`, including unchanged locked feature graphic and eight selected screenshots; final release metadata/permissions/FGS/Data Safety still need review against whatever release build is chosen.
- There is no reason to rebuild the accepted debug APK or rerun already passed tests unless a future change specifically requires it.

**Authoritative continuation:** [Stable14 → Google Play handoff](HANDOFF_CURRENT_STABLE14_TO_GOOGLE_PLAY_2026_11_03.md).  
**D145 specification and QA:** [Work-based Special Breaks / Repeat / compact Timer](D145_NET_WORK_SPECIAL_BREAKS_ONE_SHOT_REPEAT_COMPACT_TIMER_2026_11_03.md).  
**Previous baseline:** [Stable13 user acceptance](STABLE13_D143_USER_ACCEPTANCE_2026_11_01.md).
