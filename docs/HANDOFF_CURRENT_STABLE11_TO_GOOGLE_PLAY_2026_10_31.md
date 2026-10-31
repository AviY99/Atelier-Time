> **NEW FIELD-FEEDBACK WORK (2026-10-31):** Stable 11 / D-140 remains the last **user-accepted frozen** checkpoint. A source-only, **not built/not tested/not installed** D-141/code35 development candidate addresses seven user findings (UI copy, Free digits, Timer sounds switch, manual special-break position clarity, default Even, and initial end-time cycle reflow). **Session-first / Break-first choice is unchanged.** Full scope, QA gates and user approvals: [D-141 work record](D141_FIELD_REPORTS_UI_AND_APPROVED_FINISH_REPLAN_2026_10_31.md). Store publication tasks are temporarily behind D-141 device QA. Do not claim D-141 stable or run the prepared local build script without separate permission. No GitHub Actions, install, Play action, account access or payment.

# Atelier Time — authoritative handoff after Stable 11 (2026-10-31)

> **CURRENT STATUS — READ THIS FIRST.** This document supersedes conflicting historical "latest", "pending", "needs rebase" or "image not in GitHub" statements in older sections of `docs/HANDOFF.md` and legacy D-139/D-140 build records. Those older entries are kept solely as audit history. **Start here in a new project conversation; do not repeat finished tasks.**

## 1. Product and accepted checkpoint

- Project: **Atelier Time**, native Android Kotlin/Jetpack Compose, application ID `com.ateliertime.android`.
- **Current accepted version: Stable 11 = D-140, versionCode 34, compileSdk/targetSdk 36.** The user explicitly confirmed: **"הכל תקין בגרסה 140"** after device build, installation and checking.
- **Immutable-by-policy frozen code branch:** `stable/android-d140-qa11-user-accepted`, commit **`b58634e439b16bac3f45010d64a23003b7f69777`**. **Do not modify** it. Earlier Stable 10/D-138 remains frozen at `stable/android-d138-qa10-user-accepted` (`6497db7`).
- `android-native-kotlin-compose` is the **only ongoing Android development branch**. `main` is the shared project/documentation index, NOT a substitute for the native Android source checkout. Future code changes should be D-141/code35 or later, only as actually required.
- D-140 accepted appearance: first Android system splash **white with correctly sized central icon**; second **static approved sepia portrait**, with *Atelier* above clock and *Time* below, all artwork/geometry/numerals intact; Android navigation controls follow **Day/Night/AUTO** backgrounds rather than retaining parchment tint. Launcher icon and About artwork retained.
- Functional acceptance is user-reported manual/device QA, backed by **`ARTWORK AUDIT PASS` and `BUILD SUCCESSFUL — D-140`** Samsung CoGo screenshot. Do **not** claim a fresh 285/285 automated test run on D-140; historical 285/285 PASS belongs to Stable 9/D-126.
- **No signed release AAB, Play Console submission, closed testing approval or production distribution is claimed.** This is pre-distribution.

## 2. Approved artwork — completely archived for runtime

- The exact approved 1080×2340 portrait PNG is versioned in Android development and Stable 11 at `android-native/app/src/main/assets/atelier_splash_approved_d139.png`.
- SHA-256 `c7d69a52c53a86738cc416b4c29eb621f8792cf367539732ce0ffa3639d8460d`; Git blob `8950a9c509bbc2c2bac02d18c0b9bfdfca06eabc`; size 2,903,002 bytes. This was verified against the user's GitHub upload in `archive-d139-artwork`; no re-rendering, recompression, or extra manual Download dependency.
- Required Android icons, About art, Instagram avatar, notification/WhatsApp icons are already in source. `artwork/README.md` inventories them. The optional high-resolution source JPG is **not yet known to be archived** but is not an APK build dependency. User-selected private sounds and Saved Classes must remain on the user's device.
- **Do not ask the user to re-upload any approved portrait, regenerate the picture, enter GitHub passwords or create tokens.** GitHub connector already supports repository writing.

## 3. Phone Git synchronization — FINISHED

- User supplied a Samsung **Code on the Go** terminal screenshot showing successful guarded fetch/rebase to `origin/android-native-kotlin-compose` at `7979af6` and final `## android-native-kotlin-compose...origin/android-native-kotlin-compose` without ahead/behind.
- Git message: **`dropping 84f1afb... -- patch contents already upstream`**. Identical local image commit was safely skipped; a `backup/d140-pre-sync-...` local branch was created by the previously chained command. `?? ../.cg/` is an unrelated untracked Code on the Go workspace; **leave it alone**.
- **The one-time reconciliation is closed.** Later docs-only GitHub commits may make remote HEAD ahead of the phone checkout: at the **next actual phone development/build task**, confirm clean tracked files and perform a regular `git pull --ff-only` from the Android checkout. Do not instruct repeated rebase, force/reset/clean/push, authentication or another local build now.
- No install, data wipe, unrequested tests, GitHub Actions or paid cloud build. Android compile remains **local to Samsung Code on the Go**, not Termux.

## 4. Source of truth, documents and branch policy

- This file is the short authoritative current handoff. The very long `docs/HANDOFF.md` and `docs/DOCUMENTATION_INDEX_AND_BRANCH_POLICY.md` contain chronological audit records; their *older* statements about D-140 being unbuilt, artwork absent, or Git rebase pending **are historical and no longer current**.
- Stable certificate: `docs/STABLE11_D140_USER_ACCEPTANCE_AND_GIT_RECONCILIATION_2026_10_31.md`.
- Approved-art inventory: `artwork/README.md`.
- ASO drafts: `docs/ASO01_DISCOVERABILITY_REQUIREMENTS_AND_USER_APPROVAL_2026_10_30.md` and `docs/ASO01_GOOGLE_PLAY_STORE_LISTING_HE_EN_2026_10_30.md`.
- Store/compliance preparation: `docs/D128_STAGE8_ANDROID_PLAY_POLICY_AUDIT_2026_10_29.md`, `docs/D128_PLAY_PRIVACY_DATA_SAFETY_READINESS_2026_10_30.md`, `docs/D128_SPECIALUSE_PLAY_CONSOLE_DECLARATION_AND_RATIONALE_2026_10_30.md`, `docs/STAGE8_SPECIALUSE_FGS_POLICY_REVIEW_2026_10_30.md`, `PRIVACY_POLICY.md`.
- Keep user-approved frozen checkpoints unchanged. New code changes only in the Android development branch; document accepted checkpoints in both development and `main`.

## 5. Next steps — pre-Google-Play to distribution

Current goal: **move from completed Stable 11 to Google Play preparation**. Start with a concise status/plan drawn from existing documents; avoid redoing previous art, icon, app, Git, or Stable11 work.

1. **Play Console developer account** — determine account existence/verification and whether mandatory testing requirements apply. Do not assume registration complete or initiate payments without permission.
2. **Store listing/ASO** — review and finalize already drafted Hebrew + English store title, descriptions, audience, categories and discoverability terms, with user approval.
3. **Play artwork** — design/approve 512×512 store icon, 1024×500 feature graphic and genuine app screenshots; archive final images in repository. Show **previews before using** any new graphics.
4. **Privacy + Data safety** — verify public live privacy-policy URL and About placement; complete declarations according to actual app behavior and permissions.
5. **Foreground service** — finalize Play Console `specialUse` rationale and any requested evidence/video; validate current Play policy before submission.
6. **Release-readiness audit** — permissions, background behavior, notification UX, Android target APIs, privacy, saved user data and signing/update constraints.
7. **Release signing + AAB** — create and safeguard upload key **off GitHub**, create signed release AAB only with user approval and **local Samsung Code on the Go**; keep debug builds distinct.
8. **Release artifact tests**, Internal Testing, applicable Closed Testing, Play Console review, production approval/publication and post-launch monitoring. Do not claim test counts, acceptance, uploads or publication until evidenced.

**Execution rules:** Do not ask the user to repeat known information. Do not request account passwords, tokens or secrets. Do not run GitHub Actions / paid CI. Do not auto-install, uninstall, clear data or change permissions without permission. For any code modification, edit GitHub directly and give **one** Samsung Code on the Go build command after checking branch state. For visual work, preview first and obtain explicit approval. All future changes D-141+ leave Stable 11 frozen.

## 6. Next conversation opener

`המשך את פרויקט Atelier Time לפי docs/HANDOFF_CURRENT_STABLE11_TO_GOOGLE_PLAY_2026_10_31.md ב-GitHub. Stable 11 / D-140 מאושרת וקפואה, התמונה מגובה וה-Git בטלפון סונכרן. אל תחזור על שלבים שהושלמו ואל תבקש ממני סיסמאות. הצג את המצב והתחל בהכנות Google Play לפי סדר ההנדאוף, בלי לבצע תשלומים, העלאות או שינויים באפליקציה ללא אישור.`
