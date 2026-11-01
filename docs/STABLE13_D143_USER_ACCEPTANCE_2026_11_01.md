# Atelier Time — Stable 13 / D-143 — user-accepted checkpoint following field bug fixes

**Date:** 2026-11-01  
**Status:** **STABLE 13 — USER-ACCEPTED**  
**Android app:** native Kotlin/Jetpack Compose; `applicationId com.ateliertime.android`; **versionName D-143 / versionCode 37**, compile/target SDK 36, minSdk 26.  
**Acceptance basis:** The user first installed D-142 and reported its three-option terminal-remainder recovery worked, then requested an Editor reset after confirmed selections became locked. They explicitly approved D-143's first-in-row distinctly colored Reset Editor and, after the local build and in-place install, responded **"הכל עובד כנדרש"** ("Everything works as required") and requested declaring the next stable release. This constitutes **user acceptance**, not an independent automated full-device test record.

**Frozen branch (official accepted recovery ref):** `stable/android-d143-qa13-user-accepted`, branched from Android DEV after this source/documentation acceptance record. GitHub branch SHA is verified separately after freezing; this historical certificate intentionally does not embed a self-referential commit hash.  
**Pre-acceptance Android DEV source snapshot:** `665d5294e75a23e2eece578751f432f76c8f31ee` (last documentation commit before the user accepted). Production source had no further changes after this SHA at acceptance.  
**Previous accepted frozen recovery:** Stable12 / D-141 / code35 at `stable/android-d141-qa12-final-user-accepted`; Stable11/D-140 and Stable10/D-138 untouched. D-142/code36 is an intermediate development candidate and is not separately designated a stable release.

## User-reported fixes included in this checkpoint

1. **D-142 — terminal-break remainder resolution.** The previously opaque "A break would not leave one full minute for a following pose" condition now exposes actual remaining time `R` (regular 5m attempted break: 1–5m; special attempted 15m: 1–15m) and provides **three explicit choices**: return to Editor with validated suggested setting; extend last regular Session by the precise residual, subject to 25/5 rest-warning acknowledgment; or extend the final Summary by precisely the residual while keeping total Class length unchanged. Protect manually requested special breaks and existing approved saved-class signatures/legacy data. The user explicitly reported that the new choices in installed D-142 work.
2. **D-143 — full Editor reset.** A distinctly filled **Reset Editor** is FIRST in the **same one-line row** before **Example A** and **Example B**, per user approval. Reset requires confirmation, with Cancel leaving the draft untouched; Confirm restores all original fresh-start fields, opening groups, automatic/manual placement, final-session choices, chosen remainder correction, warning acknowledgment and draft Save/overwrite state. The reset does **not** delete or modify saved Classes, Sounds, reports, visual preferences or a running timer.
3. D-143 initially failed **AndroidTest compilation** because of two invalid imports in `EditorResetD143UiTest.kt`; both were removed in commit `f63fc1c24d3d80c4b12c1bf6144999bb6392d8bd`. The following local phone build succeeded; this was a test-source fix, not a production logic change.

## Evidence: accurate distinction between tests and observations

- **D-143 Samsung Code on the Go, user screenshot:** `ARTWORK AUDIT PASS`; **full JVM suite 256 tests, failures 0, errors 0, skipped 0**; targeted `EditorResetD143Test` **2/2 PASS**; prior `TailBreakRecoveryD142Test` **9/9 PASS**. `D-143 JVM TESTS AND BUILD SUCCESSFUL`.
- `app:assembleDebugAndroidTest` completed as part of the successful build, so AndroidTest source **compiled but instrumented tests were not executed** on a device. Do not claim AndroidTest pass.
- Build created **local debug APK** `/storage/emulated/0/Download/Atelier-Time-D143-API36-Candidate.apk`, with private build log and SHA manifest in Downloads. No independent readback of APK bytes or test APK was supplied to ChatGPT.
- User was instructed to install as an **in-place update preserving existing data**, then reported **"הכל עובד כנדרש"** and requested the new stable designation. Specific UI video/screenshots and a separately enumerated per-feature device regression matrix were **not supplied**; report user acceptance faithfully.
- No GitHub Actions/cloud builds, signed release AAB, Google Play upload, data deletion, forced Git operation or software publication occurred.

## Recovery and ongoing development rules

- **Freeze Stable 13 / D-143 code** in the dedicated immutable `stable/android-d143-qa13-user-accepted` branch. Do not edit that branch for future fixes, packaging or documentation; use Android DEV for a new user-authorized D-number/code.
- Frozen Stable12/D-141, Stable11/D-140 and earlier stable refs remain intact. Personal Classes, Sounds, diagnostics and original approved artwork are not to be erased or replaced.
- This **internal app checkpoint is not a signed/store-distributed release**. Google Play developer identity status was last known pending; Privacy Policy link already works, approved Play graphics stored in GitHub ZIP; Data Safety form and FGS `specialUse` demonstration, final release AAB audit/signing and actual Console submission remain separate approval gates.
- Google Play artwork was approved around Stable12. Do **not assume a D-143 app and screenshots have already been submitted** or that store screenshots must be regenerated without a specific review/approval.

**Next operational handoff:** [Stable13 → Google Play](HANDOFF_CURRENT_STABLE13_TO_GOOGLE_PLAY_2026_11_01.md).  
**Technical details:** [D-142](D142_TERMINAL_BREAK_RESOLUTION_IMPLEMENTATION_AND_QA_2026_11_01.md) · [D-143](D143_RESET_EDITOR_ONE_ROW_2026_11_01.md).
