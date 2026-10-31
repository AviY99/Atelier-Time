# Atelier Time — project-wide documentation reconciliation before Privacy / Data Safety

## Final independent recheck after the user's repeat request — 2026-10-31

A fresh read of the **live recursive Git trees** of `main` and `android-native-kotlin-compose` (neither response truncated) confirmed:
- **111** Markdown/HTML/text blobs in `main`, **96** in Android DEV; **92** shared paths, of which **81 are byte-identical**.
- All **11 active authority files** are byte-identical across branches: current Stable12 handoff, this reconciliation audit (before this new section), Stable12 certificate, D-141 QA report, approved store-icon record, original feature-graphic lock, final 8-phone-screenshot selection, HE/EN Play listing draft, root Privacy Policy, Data Safety readiness, and `specialUse` FGS declaration.
- Exactly **11 shared archival/cumulative paths** currently differ in SHA: `CHANGELOG.md`, `README.md`, `artwork/README.md`, `docs/D140_SPLASH_NAVIGATION_PALETTE_FIX_2026_10_31.md`, `docs/DECISIONS.md`, `docs/DOCUMENTATION_INDEX_AND_BRANCH_POLICY.md`, `docs/HANDOFF.md`, `docs/HANDOFF_CURRENT_STABLE11_TO_GOOGLE_PLAY_2026_10_31.md`, `docs/ROADMAP.md`, `docs/STEP_1_3_REVIEW.md`, `docs/TECH_DECISION_001_PLATFORM.md`. Branch-specific historic body/sequence explains these differences; current entrypoints are marked Stable12 and refer to the same handoff. Earlier in this audit the *initial* nine-difference snapshot was true at that earlier point; **11 is the final observed number**.
- Git compare: certified Stable11/D140 and Stable10/D138 branches remain exactly identical to their freeze commits. Comparing frozen Stable12 to the D-141 locally tested source, and Android DEV to frozen Stable12, shows **documentation-only differences**; no new Android source or binary was changed.
- The binding store gallery is originals `01,03,04,05,06,08,09,10`; excluded `02 About` and `07 initial Session` remain retained separately. Never revert to the obsolete first-eight ZIP. Feature graphic remains unchanged and the launcher-derived store icon remains approved.
- The authoritative handoff still says the actual Play account identity check is **pending**, draft app creation was **blocked** in the latest screenshot, and **Privacy/Data Safety** is the next preparation task. No upload, release build, app install or payment is inferred.
- This recheck is a **documentation/source audit**, not a physical device test, Google Console verification, or proof that downloadable store binary artifacts are already archived on GitHub.

**Result: NO missing current-status update or active handoff inconsistency was found.** Historical documents deliberately remain historically accurate; do not rewrite every old dated QA/proposal document to current status.

**Audit performed 2026-10-31.** Scope: the live GitHub repository `AviY99/Atelier-Time`, project-wide `main` and native Android development branch `android-native-kotlin-compose`; immutable stable branches checked, not modified. **This is an audit of current project documentation, not a claim that all historic implementation and proposal files should be rewritten.**

## Executive result

**Active project documentation is synchronized and up to date.** The concise [CURRENT authoritative Stable12 → Google Play handoff](HANDOFF_CURRENT_STABLE12_TO_GOOGLE_PLAY_2026_10_31.md) was rewritten to replace competing older "current" headers and is byte-identical in `main` and Android DEV.

- **Accepted app:** Stable 12 / D-141 / code35. Frozen `stable/android-d141-qa12-final-user-accepted`. Prior Stable 11/D-140 and Stable 10/D-138 frozen, unmodified. No app code or runtime art modified by this documentation pass.
- **Build/QA:** user-accepted, Samsung phone-local JVM tests and debug APK build passed. **Do not claim Play-ready release AAB**, a new automated run, a per-test count, or Play publication.
- **Play account:** user created/paid personal Google Play developer account; two-factor ON; identity documents submitted and verification pending, contact number verification waiting, **Create app disabled** at user's last screenshot. Do not claim verification complete.
- **User-approved store art:** 512×512 icon taken straight from Stable12 launcher; first/original 1024×500 feature graphic locked unchanged, later icon-overlay proposal rejected. Authoritative checksum/approval documents under `artwork/play-store/`.
- **User-final phone gallery:** exactly **8 genuine screenshots**, original source positions **01, 03, 04, 05, 06, 08, 09, 10** in that relative order; **About original #02 and Class Timer Start original #07 excluded from Play only**, preserved in ten-image backup. Previously proposed first 8 screenshots **superseded**.
- **Next prep:** verify public Privacy Policy URL/actual About click, assess Data Safety answers against Stable12 source, then Google Play FGS `specialUse` draft/evidence. No Data Safety/FGS submission, release-signed AAB, Play upload or public release.

## Inventory and consistency methodology

1. Read GitHub REST recursive **tree of both branches**. At audit start: `main` **113 tracked entries**, **110 Markdown/text/HTML document blobs**; Android DEV **290 entries**, **95 document blobs**. The difference is by design: `main` retains early proposals and historic source documents, while DEV carries Android code/test scripts and phone execution readmes. Do not indiscriminately copy all files across branches.
2. Compared SHA of all shared documentation paths. The **current handoff, Stable12 acceptance certificate, D-141 QA report, the three Play artwork approvals/selections, ASO draft and privacy/FGS readiness drafts are identical** in `main` and Android DEV. After the reconciliation, all mutually relevant active release documents have the same current status; some cumulative documents intentionally differ because their **older archived bodies** evolved independently.
3. Nine shared docs initially had different blob SHAs because of historical branch-specific sections: `README.md`, `CHANGELOG.md`, `artwork/README.md`, `docs/DOCUMENTATION_INDEX_AND_BRANCH_POLICY.md`, `docs/HANDOFF.md`, `docs/HANDOFF_CURRENT_STABLE11_TO_GOOGLE_PLAY_2026_10_31.md`, `docs/D140_SPLASH_NAVIGATION_PALETTE_FIX_2026_10_31.md`, `docs/STEP_1_3_REVIEW.md`, `docs/TECH_DECISION_001_PLATFORM.md`. **Different SHA is not evidence of missing current status**: entrypoints received new live status banners, historical decisions intentionally retained in place. Stable11 handoff is superseded, never silently rewritten as Stable12.
4. Read/reconciled current README, index, handoff, roadmap, decisions, test plan, work log, changelog, artwork index, architecture, scheduling rules, product spec, Samsung-only build-cost policy, the ASO original request and listing drafts, and the privacy and FGS Play review docs. The dated 2026-10-24..30 development/QA transcripts and individual stable acceptance certificates remain **historical as of their own dates**; do not destructively rewrite their contemporaneous evidence.
5. Checked internal relative Markdown links in **12 primary documents** (517 local-link occurrences before audit). Detected two historical references in `docs/ROADMAP.md` and `docs/DECISIONS.md` that pointed to a D127 shell script absent from `main`; repaired them to the existing script on Android DEV. The only remaining initially missing link target was **this audit report**, which is created in both branches as part of the reconciliation. Current handoff's relative paths were otherwise valid.
6. Verified available conversation packages by actual ZIP CRC check and SHA-256. The final **8-PNG Play upload ZIP** contains precisely:
   `01_Splash.png`, `02_Class_Editor.png`, `03_Complete_Class_Timeline.png`, `04_Saved_Classes_And_Reports.png`, `05_Class_Timer_Progress.png`, `06_Free_Timer.png`, `07_Design_Settings.png`, `08_Sound_Choices.png`. ZIP SHA-256: `6353ee0ce2c911b29973f14bab4fb620fa8ef5e5aeeed5652e7167341faa676d`. The separate complete 8-image archive with unchanged originals has SHA-256 `836688045c4fef879956a3e50ddc7c8fa417e9ea7f9116d865fc821d3eae1bad`. Earlier full ten-images backup is retained.

## Master links — current release workflow

- [CURRENT master handoff](HANDOFF_CURRENT_STABLE12_TO_GOOGLE_PLAY_2026_10_31.md)
- [Stable12 certification](STABLE12_D141_FINAL_USER_ACCEPTANCE_2026_10_31.md)
- [D-141 field fixes and phone QA](D141_FIELD_REPORTS_UI_AND_APPROVED_FINISH_REPLAN_2026_10_31.md)
- [Approved 512×512 icon](../artwork/play-store/PLAY_STORE_ICON_STABLE12_APPROVED_2026_10_31.md)
- [Locked unchanged first feature graphic](../artwork/play-store/FEATURE_GRAPHIC_FIRST_ORIGINAL_LOCK_2026_10_31.md)
- [**FINAL 8 screenshot selection**](../artwork/play-store/SCREENSHOTS_STABLE12_FINAL_8_SELECTED_2026_10_31.md)
- [Historic original set of 10](../artwork/play-store/SCREENSHOTS_STABLE12_EXACT_USER_ORDER_2026_10_31.md)
- [HE/EN Play listing copy draft](ASO01_GOOGLE_PLAY_STORE_LISTING_HE_EN_2026_10_30.md)
- [Privacy/Data Safety readiness](D128_PLAY_PRIVACY_DATA_SAFETY_READINESS_2026_10_30.md)
- [specialUse FGS draft and rationale](D128_SPECIALUSE_PLAY_CONSOLE_DECLARATION_AND_RATIONALE_2026_10_30.md)
- [Privacy Policy](../PRIVACY_POLICY.md)

## Known limitations — not falsely marked complete

- Approved **Play graphic/icon PNG binary and screenshot PNGs/ZIPs are available as downloadable conversation artifacts, but not yet saved as image binary files in GitHub**. Their precise filenames, decisions and SHA-256 are recorded. An image-asset *documentation record* is not a remotely stored image. Later durable GitHub asset upload should verify exact bytes and must not alter the locked feature graphic. The original accepted launcher WebP and in-app splash already exist in frozen Android source.
- Google Play Console account identity verification, contact-phone verification, draft-app creation, store uploads, Play Data Safety, FGS approval, release signing/AAB, and internal/closed/public rollout are **still pending as described in the current handoff**.
- A GitHub documentation audit does not independently verify availability of the live privacy-policy web URL, behavior of installed app About link, current Play Console verification state, or the final release binary. These need the targeted next steps. The user has not approved a new Android code change, build, APK install, Play upload or payment.

## Final post-reconciliation verification

- After creating this audit file: `main` contains **111** Markdown/text/HTML documents and Android DEV **96**; current handoff and audit document have **identical Git blob SHA** across the two branches.
- A further **156 relative Markdown links across seven high-priority `main` entrypoint, artwork and certification documents resolved with zero missing targets**, after creating this audit report. Historical D127 link targets in Roadmap and Decisions were corrected to the Android development branch.
- Comparing Android DEV to frozen Stable12 after the documentation edits showed **no non-documentation source or binary changes**. Frozen Stable11/D140 and Stable10/D138 still compare byte-identically with their certified commits.
- Android DEV-only `android-native/README.md`, `android-native/PHONE_RUNNER.md`, and legacy `apps/atelier_time_flutter/README.md` were separately marked with the active Stable12 handoff and **historic/non-active** build/platform status. `main` does not need copies of these development-only documents.
- All above means **the active documentation is ready to hand off to Privacy/Data Safety review**; it does **not** mean Google Play verified the account or that store PNG/ZIP binaries are remotely stored in GitHub.

## Continuation rule

Use only [the current handoff](HANDOFF_CURRENT_STABLE12_TO_GOOGLE_PLAY_2026_10_31.md) for forward-looking priorities. **Next stage is Privacy Policy and Data Safety evidence review, not more Stable12 development or another screenshot reordering.** Preserve historic documents for traceability. Any new Play Console or publication action requires explicit user permission.
