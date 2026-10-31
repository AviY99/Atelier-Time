# Atelier Time — Stable 12 / D-141 — final user-accepted Android checkpoint

**Date:** 2026-10-31  
**Status:** **STABLE 12, D-141, USER-ACCEPTED — FINAL BUG FIXES AND SCREEN/MENU REFINEMENTS.**  
**Package:** `com.ateliertime.android` | `versionName D-141` | `versionCode 35` | compile/target API 36, min 26.  
**Device-tested source commit (local Samsung CoGo build):** `6e86399807aa11d674f333bbd1cfb1fcb6bd25f7`.  
**Official immutable acceptance branch:** `stable/android-d141-qa12-final-user-accepted` (created after documentation acceptance updates; all changes since local tested commit are **documentation only**).  
**Prior user-accepted stable:** `stable/android-d140-qa11-user-accepted` at `b58634e439b16bac3f45010d64a23003b7f69777` — **Stable 11 / D-140 unchanged**. Stable 10/D-138 remains frozen.  
**Authoritative next-work handoff:** [Stable 12 → Google Play](HANDOFF_CURRENT_STABLE12_TO_GOOGLE_PLAY_2026_10_31.md).

## User acceptance

After a Samsung local D-141 debug build and test success, the user first said everything works, then **explicitly corrected the milestone designation to Stable 12**:

> "הכל עובד !!! זאת גרסה יציבה 12 תיקון באגים וטיוב מסכים ותפריטים סופי"

This latest correction **supersedes** the previous message erroneously naming this D-141 checkpoint Stable 11. **Do not relabel Stable 11 / D-140 or overwrite either branch.** D-141 is final **Stable 12**.

User's Samsung Code on the Go screenshot confirms successful clean `git pull --ff-only` through source `6e86399`, `ARTWORK AUDIT PASS`, and `D-141 JVM tests and BUILD SUCCESSFUL`, creating `/storage/emulated/0/Download/Atelier-Time-D141-API36-Candidate.apk`, with local manifest/build log. An earlier D-141 test attempt failed two cases; these were fixed and the user reran the full local JVM test/build script successfully. The screenshot **does not disclose an exact test count**. The user reports that everything works, but **no independent post-install screenshot, granular seven-case test results or separately verified APK digest are in the chat evidence**. Android connected instrumentation was not newly evidenced. Historical 285/285 success belongs to Stable 9/D-126, not this build.

## Seven user-approved changes in D-141

1. Reduce excessive on-screen explanations; detailed instructions belong in Help, except essential warnings.
2. Double the main Free Timer HH:MM:SS wheel numerals only, leaving finish approval wheel unchanged.
3. `Timer sounds ON/OFF`: ON default; OFF visibly communicates automatic end sounds are muted while timing still runs.
4. Keep automatic and manual special-break placement. Automatic defaults ON; OFF shows an explicitly labeled input for regular-break *positions to replace* with special breaks (not a count). Preserve saved manual placements.
5. **Do not change** `Session first` / `Break first` choices or behavior.
6. `Even` is the default final-two-Session choice when a valid split is available; other explicit choices retained.
7. Initial finish-clock changes use the shared constrained future planner for full normal Session+Break insertions when extending substantially, rather than two giant final Sessions. Existing special-break protection, saved class immutability, legacy one-Session guard, exact finish and runtime Next/Restart boundaries maintained. Source-level regression tests were added and passed in the second local JVM run.

## Recovery, artwork and release limits

- D-141 `versionCode=35` is the **accepted development/debug checkpoint**, not a Google Play release artifact.
- Accepted art and splash asset `android-native/app/src/main/assets/atelier_splash_approved_d139.png` are already tracked; SHA-256 `c7d69a52c53a86738cc416b4c29eb621f8792cf367539732ce0ffa3639d8460d`. No reupload, regeneration or icon replacement required.
- Only `android-native-kotlin-compose` may receive future source changes, with a **new** D-number/code, if and when expressly approved. Frozen stable references must never be edited.
- Do not uninstall, wipe data, recompile, run GitHub Actions or request Git passwords/tokens. Existing user Saved Classes, Sounds and history should remain undisturbed.
- No release-signed AAB, Play Console upload, payment, store approval, production publication, or formal Play track testing is claimed. **Next phase:** Google Play preparation per the handoff, beginning with account/policy status and previously drafted ASO/privacy/FGS work; every external action requires separate user authorization.

## Documentation correction note

An erroneous `docs/STABLE11_D141_FINAL_USER_ACCEPTANCE_2026_10_31.md` was briefly created after the user's *earlier* naming. It has been explicitly marked **SUPERSEDED**; its historical text is not the current milestone authority. This Stable 12 certificate and the new Stable 12 handoff are authoritative.
