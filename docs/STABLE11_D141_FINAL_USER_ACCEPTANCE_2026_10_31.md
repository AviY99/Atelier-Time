> **SUPERSEDED — WRONG STABLE NUMBER.** After the user explicitly corrected the milestone, **D-141 is Stable 12 (not Stable 11)**. This earlier D-141/Stable-11 document is retained **only as audit history**. The authoritative certificate is [STABLE12_D141_FINAL_USER_ACCEPTANCE_2026_10_31.md](STABLE12_D141_FINAL_USER_ACCEPTANCE_2026_10_31.md), with immutable branch `stable/android-d141-qa12-final-user-accepted`. **Stable 11 remains D-140**. No code/branch changes were made to Stable 11 as part of this correction.

# Atelier Time — Stable 11 FINAL / D-141 user-accepted checkpoint

**Date:** 2026-10-31  
**Status:** **USER-ACCEPTED STABLE 11 — FINAL BUG FIXES AND UI / MENU REFINEMENT.**  
**Native Android identity:** `D-141`, `versionCode=35`, application ID `com.ateliertime.android`, compile/target SDK 36.  
**Exact Samsung Code on the Go test/build source:** commit `6e86399807aa11d674f333bbd1cfb1fcb6bd25f7` on `android-native-kotlin-compose`.  
**Official frozen branch (after creation):** `stable/android-d141-qa11-final-user-accepted`.  
**Prior frozen milestone retained untouched:** `stable/android-d140-qa11-user-accepted` at `b58634e439b16bac3f45010d64a23003b7f69777` — historical initial Stable 11 / D-140, superseded by the user's *final* Stable 11 / D-141 refinement, **not** relabeled Stable 12.

## 1. Acceptance evidence and its limits

1. The user's Samsung Code on the Go screenshot confirms `git pull --ff-only` to `6e86399`, `ARTWORK AUDIT PASS`, **`D-141 JVM tests and BUILD SUCCESSFUL`**, and a debug APK at `/storage/emulated/0/Download/Atelier-Time-D141-API36-Candidate.apk`. Its same-name manifest and build log were saved on the phone. The build command did not install. An earlier local attempt failed two tests; those were repaired and the second full local JVM run passed.
2. After receiving the built candidate, the user stated **"הכל עובד !!! זאת גרסה יציבה 11 תיקון באגים וטיוב מסכים ותפריטים סופי"** and explicitly designates this functioning release **Stable 11 final**. This is a user report of working/device acceptance; there is **no separate post-installation screenshot** or detailed seven-item checklist attached, so do not invent installation steps, test cases, APK SHA or per-test counts.
3. No new independent Android instrumentation run is claimed for D-141; the historical `285/285` is specific to Stable 9 / D-126. **No Play-signed AAB, upload, Google Play submission or publication is claimed.**

## 2. Seven approved improvements

1. Removed redundant on-screen explanations, preserving Help, critical warnings and essential status.
2. Doubled selected Free Timer HH:MM:SS numeral size while leaving the Class finish approval wheel unchanged.
3. Clearly labeled `Timer sounds ON/OFF`: ON default; OFF silences automatic Class and Free Timer completion cues, with a visible explanation; the timer still runs.
4. Kept the automatic/manual **special-break placement** switch; ON is default. When OFF, the field now says which numbered **regular-break slots to replace** with special breaks, not how many breaks exist. Saved manual placements remain valid.
5. Kept both `Session first` and `Break first` exactly as before — user's explicit instruction.
6. `Even` is the default two-ending-Session split where feasible; other splits remain selectable, saved explicit selections preserved.
7. The initial approved Class finish-clock extension now uses the shared constrained future planner, allowing newly added regular Session+Break cycles instead of stretching just two giant final Sessions; fixed approved physical finish, protected special breaks, original saved presets and runtime Next/Restart constraints retained. Legacy one-work-Session classes still reject unsupported finish changes. Regressions cover +90 minute extension, exact preview vs real start, manual special placement, and minute-boundary constraints.

## 3. Source/branch continuity and safety

- The binary-verified code commit is `6e86399807aa11d674f333bbd1cfb1fcb6bd25f7`. Later commits on Android DEV **only updated documentation**, not the APK sources. The final frozen branch captures the same Android source plus the acceptance docs; compare against the binary-tested commit before freezing.
- Android source development continues only on `android-native-kotlin-compose`, with any subsequent code change requiring a **new D-identifier/versionCode**, not silently rewriting accepted D-141.
- The exact user-approved portrait image in `android-native/app/src/main/assets/atelier_splash_approved_d139.png` remains Git-tracked with SHA-256 `c7d69a52c53a86738cc416b4c29eb621f8792cf367539732ce0ffa3639d8460d`.
- Freeze all stable branches; do not alter, rebase or push to them, delete saved user Classes/Sounds, reinstall or compile without a genuine need and user authorization.
- **Next workflow:** Google Play preparation under `docs/HANDOFF_CURRENT_STABLE11_TO_GOOGLE_PLAY_2026_10_31.md`, starting with Play Console account requirements. Existing ASO, privacy/FGS drafts and artwork must be reused rather than recreated. No registration/payment, store upload or publication without explicit user approval.
