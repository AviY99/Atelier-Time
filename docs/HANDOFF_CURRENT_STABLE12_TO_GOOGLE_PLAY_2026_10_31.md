> **STORE SCREENSHOTS IN EXACT USER ORDER (2026-10-31):** The user supplied **10 genuine Stable12 screens** and instructed **do not change their sequence**. All 10 are preserved byte-for-byte in a downloadable master ZIP, plus 10 proportionally scaled/side-padded 1080×1920 Play-ready PNGs; a separate phone ZIP contains items **01–08 in unchanged order** because official Google Play limits phone screenshots to **8 per device type**. Items 09–10 remain numbered and preserved as reserve, **not deleted or reordered**. [Exact original order, per-file SHA-256 and handling rules](../artwork/play-store/SCREENSHOTS_STABLE12_EXACT_USER_ORDER_2026_10_31.md). User-approved 512×512 Play icon and locked first feature graphic remain unchanged. **Binary screenshots/ZIPs are conversation artifacts, NOT GitHub-stored image files.** Play developer identity verification pending in last screenshot, no store upload or publication.

> **GOOGLE PLAY PREPARATION UPDATE (2026-10-31):** User's personal Play Console account was created and payment completed. Identity documents uploaded and awaiting Google's approval; `Create app` is disabled pending account verification. **512×512 Play icon sourced from the existing approved Stable12 app launcher was explicitly APPROVED**; [approval and SHA record](../artwork/play-store/PLAY_STORE_ICON_STABLE12_APPROVED_2026_10_31.md). The first feature graphic remains locked and unchanged; [lock record](../artwork/play-store/FEATURE_GRAPHIC_FIRST_ORIGINAL_LOCK_2026_10_31.md). Store PNGs are available as downloadable conversation artifacts but **are not yet committed as binaries to GitHub**. Next: genuine Stable12 screenshots and identity verification; no store upload, AAB, app rebuild or payment authorization.

# Atelier Time — CURRENT authoritative handoff: Stable 12 / D-141 → Google Play

**Date:** 2026-10-31. **Start here for new conversations.** This handoff **supersedes** the historical `HANDOFF_CURRENT_STABLE11_TO_GOOGLE_PLAY_2026_10_31.md` and older status text. Keep older records as audit history; never repeat finished builds, old rebase instructions or image recovery work.

## 1. Accepted project state

- **Latest FINAL accepted version: Stable 12 / D-141**, `versionName=D-141`, `versionCode=35`, package `com.ateliertime.android`, compile/target API 36, min API 26, native Android Kotlin/Jetpack Compose.
- The user reported **everything works** and explicitly designated **Stable 12** following the final bug fixes and UI/menu refinements; this corrects the user's earlier message mentioning Stable 11. **Stable 11 remains D-140**, not D-141.
- **Frozen Stable 12 source branch:** `stable/android-d141-qa12-final-user-accepted`. **Samsung Code on the Go JVM-test/build source:** commit `6e86399807aa11d674f333bbd1cfb1fcb6bd25f7`. Commits after this up to freeze change only documentation. [Official Stable 12 certificate](STABLE12_D141_FINAL_USER_ACCEPTANCE_2026_10_31.md).
- **Historical immutable branches:** `stable/android-d140-qa11-user-accepted` (Stable 11 / D-140, `b58634e439b16bac3f45010d64a23003b7f69777`) and `stable/android-d138-qa10-user-accepted` (Stable 10 / D-138). Do not change any stable branch.
- Latest user-provided Samsung screenshot confirms successful `git pull --ff-only`, `ARTWORK AUDIT PASS` and **D-141 JVM tests and BUILD SUCCESSFUL**; local debug APK `/storage/emulated/0/Download/Atelier-Time-D141-API36-Candidate.apk`, with local build log/manifest. Exact JVM test count not provided. Subsequent user acceptance is self-reported; separate installation screenshot and detailed case-by-case device test report not provided. Do not invent them or rerun accepted work.
- **D-141 user-approved changes:** concise UI text with Help available; Free Timer main digits ×2; `Timer sounds ON/OFF` (ON default; OFF mutes auto timer sounds); keep automatic/manual special-break placement with ON default and clear OFF replacement-slot input; **Session first / Break first unchanged**; `Even` default where eligible for final pair; initial finish-time extension using unified future planner to distribute full regular Sessions and Breaks. [Full D-141 work and QA record](D141_FIELD_REPORTS_UI_AND_APPROVED_FINISH_REPLAN_2026_10_31.md).
- **Not yet done:** signed release AAB, Google Play Console submission, mandatory store testing (if applicable), or publication.

## 2. Preserved artwork and on-phone data

- Approved splash image **already Git-tracked** at `android-native/app/src/main/assets/atelier_splash_approved_d139.png`; SHA-256 `c7d69a52c53a86738cc416b4c29eb621f8792cf367539732ce0ffa3639d8460d`, Git blob `8950a9c509bbc2c2bac02d18c0b9bfdfca06eabc`. Launcher, About, Instagram, WhatsApp/status images are also in source; see `artwork/README.md`. Do not ask for artwork reupload or regenerate approved pieces.
- Saved user Classes, Sounds, diagnostic reports and history reside on phone. No uninstall, reinstall, reset/clear-data or permission changes without express consent.
- `android-native-kotlin-compose` is the only Android **development** branch. `main` is shared documentation index, **not** the Android build branch; Stable branches are frozen. Future code changes must receive a new D/versionCode with approval. Do not run GitHub Actions / paid cloud builds; Android builds only locally on Samsung Code on the Go, when separately authorized.
- The old phone Git rebase was completed and D-141 `git pull --ff-only` succeeded. Do not repeat rebase/reset/force push. Extra `../.cg/` belongs to Code on the Go, leave untouched. No password/token request.

## 3. Next work — Google Play preparation (not publication)

Reuse existing ASO and compliance drafts rather than rebuilding completed work:
1. **Play Console account and current eligibility/testing rules:** confirm developer account state, verification and whether personal-account closed-test requirements apply. Research and guidance only until user authorizes any account/payment action.
2. **Store listing / ASO:** finalize draft Hebrew and English title, short/long description, target audience, category, keywords. Existing `docs/ASO01_DISCOVERABILITY_REQUIREMENTS_AND_USER_APPROVAL_2026_10_30.md` and `docs/ASO01_GOOGLE_PLAY_STORE_LISTING_HE_EN_2026_10_30.md`.
3. **Play artwork:** obtain user approval for store icon 512×512, feature graphic 1024×500, actual app screenshots. Show previews **before** using/uploading assets. Preserve approved runtime art.
4. **Privacy / Data safety:** verify already drafted `PRIVACY_POLICY.md`, the public live policy URL and About link, and actual data/permission behavior.
5. **Android foreground service Play declaration:** review existing `docs/D128_SPECIALUSE_PLAY_CONSOLE_DECLARATION_AND_RATIONALE_2026_10_30.md` and `docs/STAGE8_SPECIALUSE_FGS_POLICY_REVIEW_2026_10_30.md`, verify current Play requirements and any required demonstration.
6. **Final release-readiness audit:** permissions, background playback/notifications, API target, app data, export, retention and signing/upgrades.
7. **Signing and AAB:** secure upload key **off GitHub** and prepare signed release AAB *locally on Samsung Code on the Go* only after explicit approval; don't confuse debug APK with Play release.
8. **Release tests / internal or mandatory closed testing / Play review / production release** only with explicit approvals. No cost/payment, upload, Play signing action or publication is currently authorized.

## 4. Documents and workflow

- **Source of truth:** this current handoff, [Stable 12 acceptance certificate](STABLE12_D141_FINAL_USER_ACCEPTANCE_2026_10_31.md), [D-141 QA record](D141_FIELD_REPORTS_UI_AND_APPROVED_FINISH_REPLAN_2026_10_31.md).
- Historical D-140 certificate `docs/STABLE11_D140_USER_ACCEPTANCE_AND_GIT_RECONCILIATION_2026_10_31.md` remains correct **for Stable 11 only**. A transient `STABLE11_D141_FINAL_USER_ACCEPTANCE_2026_10_31.md` was created before the user corrected the number; it is marked **SUPERSEDED** and must not be treated as current.
- `docs/DOCUMENTATION_INDEX_AND_BRANCH_POLICY.md` and `docs/HANDOFF.md` are cumulative historical indexes; newest heading determines current state.
- **Next interaction:** Present accepted status + next concrete Google Play prep step concisely; obtain consent before any external account/payment, release build/upload or app change. Do not ask again for completed QA, original images, passwords or Git sync.

**New-chat opener:** `המשך את Atelier Time לפי docs/HANDOFF_CURRENT_STABLE12_TO_GOOGLE_PLAY_2026_10_31.md ב-GitHub. Stable 12 / D-141 היא הגרסה היציבה הסופית שאושרה; Stable 11 / D-140 קפואה. בדיקות JVM ובניית D-141 הצליחו, והממצאים תוקנו. אל תחזור על עבודה שהושלמה. הצג מצב ותוכנית Google Play, ללא תשלום, בנייה או העלאה בלי אישור.`
