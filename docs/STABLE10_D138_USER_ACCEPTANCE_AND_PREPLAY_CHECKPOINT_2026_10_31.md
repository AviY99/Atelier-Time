# Stable 10 — D-138: user-accepted Android application / pre-Google-Play checkpoint

**Date:** 2026-10-31.  
**User's explicit designation (Hebrew):** "הכל עובד כנדרש ... זוהי מבחינתי גרסה 10 יציבה עם כל הבדיקות, השלמת הממשק הגרפי, סבבי תיקונים/שיפורים והתאמה לחנות גוגל טרום תהליך הפצה".

## Identity and frozen recovery point
- **Accepted product checkpoint:** **Stable 10**, Android development version **D-138**, `versionCode=32`, `versionName="D-138"`, `compileSdk=36`, `targetSdk=36`; Android application ID `com.ateliertime.android`.
- **Exact approved application source commit:** `6497db700587ac8f24a502137f085f52703b3524` on `android-native-kotlin-compose`. New **immutable-by-policy** recovery branch: `stable/android-d138-qa10-user-accepted`, initially at that exact commit.
- This is a **stable acceptance/checkpoint**, not a separate APK version bump. Do not overwrite the stable branch or release-sign/upload anything on its behalf without fresh permission.
- **Preserve earlier frozen Stable 9/D-126**, including its separately proven **285/285 (243 JVM + 42 Android instrumentation)** on D-126; never represent those as tests newly executed on D-138.

## Physical-device evidence and acceptance
- The user supplied a Samsung **Code on the Go** terminal screenshot showing `git pull` to `6497db7` and **`BUILD SUCCESSFUL`** for local D-138 `:app:assembleDebug`; APK path `/storage/emulated/0/Download/Atelier-Time-D138-API36-Candidate.apk`, plus separate local manifest.
- Build-only script correctly reported **NO INSTALL / NO TEST RUN**, no GitHub Actions, no data touched. That terminal output alone is **not** proof of automatic device instrumentation.
- User supplied an installed Home screenshot showing **Atelier Time** launcher art next to bit: Vitruvian clock ring, all four key numerals (12/3/6/9), large enough and no excessive inset. Separate My Files/APK listing image shows a smaller thumbnail inside a light Samsung/Android presentation frame. The user nevertheless explicitly approved the overall result as satisfactory.
- User reports **"everything works as required"** and design/repair/test rounds complete, designating Stable 10. Record as **user-reported functional/manual acceptance**, not as independent method-by-method regression attestation.
- The approved visual work includes the tighter launcher crop and final interface as installed. Prior D-134 diagnostic history and earlier About/privacy/Instagram/WhatsApp/timer/Sounds/Saved Classes work are **inherited from the accepted development line**; this certificate makes no new measurement of each subsystem or state.
- **No fresh D-138 connected AndroidTest/JVM result counts, signed release bundle, Play Console approval, or independent full-device matrix are in the submitted evidence.** Do not invent them or mark them PASS. Historical Stable 9 test outcomes remain valid only for their recorded D-126 source state.

## Why Stable 10 is a pre-distribution baseline, not a store release
The user has completed **the product/UI refinement and device acceptance phase**. Google Play publishing remains a separate governed release procedure:
1. Review Play policies against current app behavior, target API level and specialUse foreground-service notifications; prepare the required declaration / video if applicable.
2. Validate final privacy-policy publication and Data safety declarations, any required disclosures and content rating.
3. Prepare and approve final store graphics (including 512×512 listing icon, screenshots, feature graphic) and he-IL/en-US listings using ASO-01 drafts; assess the installer thumbnail presentation separately from the approved Home icon if needed.
4. Build a **release-signed AAB locally** with protected signing key (not the existing debug APK), run release artifact/version/signature integrity checks, and upload only with explicit user approval.
5. Complete Play Console setup, required app-access/testing declarations, appropriate testing tracks and publication review. Do not claim review, closed testing or production live status before real results.
6. Any required code or asset changes after the freeze belong in **D-139+** on the development branch, not by mutating Stable 10. Decide explicitly whether a later accepted release checkpoint is needed.

## Development and cost rules
- GitHub manages source/versioned docs only. **All Android compile/build/QA is local to Samsung Code on the Go, not Termux or GitHub Actions**; no paid cloud CI.
- Do not uninstall, clear data, alter saved user content, auto-install, or run unrequested tests. Separate permission before installs or release/store actions.
- Current branch policy: keep `main` as general documentation/handoff entry point, keep the frozen code snapshot as an immutable checkpoint, and continue any future work on `android-native-kotlin-compose`.
- Source-only earlier D-138 document is historical, superseded by **this actual build + user approval**. It must not be interpreted as the current state.
