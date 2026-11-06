> **LATEST 2026-11-06 — D148 / CODE42 USER-APPROVED OPTION A SOURCE IMPLEMENTED, SAMSUNG BUILD/PHONE ACCEPTANCE PENDING (AUTHORITATIVE):** User examined 4 proposed top Run control layouts, selected **Option A upper-left** and a more compact single-image refinement, then expressly approved implementation and required **`Max both` at the far right**. On `android-native-kotlin-compose`, committed D148/code42: compact outlined `Keep Screen Awake` with a colored ON/OFF Switch; compact framed `Volumes` section with rightmost `Max both` opening the **original confirmation dialog**; draggable **Media Volume/Alarm Volume** independent Android AudioManager sliders and live percentage labels; selected class-cue audio stream highlighted in a dark filled row with **Selected** beside percentage, unselected stream transparent; tapping a row heading persists selected stream through existing SoundSettings, dragging a bar adjusts *only* its actual Android hardware stream on release. Manual slider actions supersede Max-both auto-restore ownership for that stream; errors from DND/Android volume restrictions displayed. Existing app Day/Night theme applied rather than promotional purple mockup. New `VolumeSliderMath` (8 JVM source tests), updated D147 UI compile-only tests and 2 extra D148 UI compile-only tests. **No D148 JVM execution, APK build, AndroidTest run, installation, Stable16 acceptance or Play upload yet.** Local-only (NO paid CI, NO install or data clear) QA script `android-native/tools/build_d148_selected_audio_sliders_on_phone.sh` checks new 8 JVM cases + legacy full suite, compiles AndroidTest, packages D148 debug APK and checks approved Git artwork SHA256 **inside** binary. **Next ONE phone action:** `cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native && git pull --ff-only && bash tools/build_d148_selected_audio_sliders_on_phone.sh`. Full [D148 implementation/QA specification](D148_OPTION_A_COMPACT_RUN_AUDIO_SLIDERS_MAX_BOTH_2026_11_06.md). **Stable15/D147/code41 `stable/android-d147-qa15-ui-install-accepted` `c10f424c0ec000eca2dd561911934841decb4522` remains latest **accepted frozen**. D147 confirmed 283 JVM tests PASS and user-accepted; D148 new source not yet checked by phone. FREE + initial invite-only INTERNAL TESTING decisions remain; Google Play public rollout not started. Older D147 “latest” paragraphs below are historical after this note.

# CURRENT AUTHORITATIVE HANDOFF — Atelier Time Stable15 / D147 → Google Play

**Updated:** 2026-11-05. This is the **LATEST** status and handoff. Supersedes Stable14/Stable13 handoffs' statements about latest stable and older D147 QA pending. Preserve older notes as chronology, not authoritative active instructions.

## 1. Latest stable; verification

- **STABLE 15 / D-147 / code41 is user-accepted and frozen**. User's exact acceptance: **"הכל עובד כנדרש זוהי גרסה יציבה הבאה , עדכוני ממשק והתקנה"**. User describes this as UI and installation/packaging updates.
- Native Android Kotlin/Compose, applicationId `com.ateliertime.android`, versionName `D-147`, versionCode `41`, compileSdk/targetSdk 36.
- Frozen GitHub recovery branch `stable/android-d147-qa15-ui-install-accepted` **verified at exact commit `c10f424c0ec000eca2dd561911934841decb4522`**. Do **NOT** modify, merge into, force-push, rewrite or re-document the frozen branch. Last production/build-helper commit `89d859e2e381b534edabf615972e16c75078e780`; commit comparison to the frozen branch showed only documentation changes afterward, so code/gradle/resources correspond.
- Previous **Stable14/D145/code39** frozen at `stable/android-d145-qa14-field-accepted`, SHA `73bf60996234f739243b3974ac1d02c38ad065ba`, verified unchanged when Stable15 frozen. Stable13 and all earlier checkpoints remain immutable.
- Android development branch `android-native-kotlin-compose` may advance in *future* D/versionCode only with explicit user direction. `main` maintains project-wide docs/Play assets. Do not overwrite or check out Stable15 to build features.

## 2. Precisely what Stable15 contains

D145 Stable14's prior achievements remain: multiple auto Special Breaks optimally recentered by cumulative net observed/future work following successful Next/Repeat Last/Restart; specials already entered cannot recur; one-shot Repeat Last unlocks on natural completion/Next, not Restart; fixed lesson finish/summary and 25/5 consent; user-accepted sound/background/diagnostics controls.

D146/code40 included in D147:
- Remove `Class controls` heading and extra frame; place the five action buttons (Pause, Restart, Repeat Last, Next, Exit) immediately under Timer face; Help **below**.
- Correct opening-group current/next titles to **`Group 1 · Session 1/4`**, `Group 2 · Session 1/2` etc. and `Group 1 · Break`; preserve stored counters/IDs and normal regular labels.
- Sound preview long `Play 5s` buttons replaced with compact triangular Play icons in the same row as each sound selector; spoken cue/length retained.
- Builder self-recovers **only** absent, Git-tracked approved PNG from checkout and validates its actual SHA256 against the exact PNG bytes packed in the final Android APK.

D147/code41 final correction:
- Restore original **readable** two compact upper running-Timer rows: `Keep screen awake during timers` / `Uses more battery` + screen switch, and `Class sound volume` / `Media N% · Alarm N% · using Media/Alarm` + `Max both`.
- 0dp explicit gap between two upper rows, 2dp gap in running Timer parent and 2dp top inset for Timer header; preserve at least 48dp touch targets and all prior sound/volume/screen-on behavior.
- New presentation-only `RunningClassQuickControls.kt` and 2 Compose UI AndroidTest methods **compiled only**, not executed automatically.

## 3. Successful QA evidence — not just claims

User Samsung CoGo screenshot (2026-11-05) for `android-native/tools/build_d147_readable_timer_top_controls_on_phone.sh` confirms:
- `ARTWORK AUDIT PASS`;
- **`ALL JVM ACTUAL: tests=283 errors=0 failures=0 skipped=0`**; D146 group 6/6, D145 Net Work 8/8, one-shot 4/4, D144 runtime 5/5 and labels 4/4, D143 editor reset 2/2, D142 tail 9/9; whole 283 executed;
- **`APK ARTWORK INCLUDED PASS`**, exact SHA256 of approved `assets/atelier_splash_approved_d139.png` found inside final APK;
- `D-147 JVM TESTS AND BUILD SUCCESSFUL`: locally created `/storage/emulated/0/Download/Atelier-Time-D147-API36-Candidate.apk`. Local manifest `Atelier-Time-D147-API36-Manifest.txt` and private phone log `Atelier-Time-D147-API36-Build.log` also present. No user upload of those files required.
- AndroidTest APK **compiled but tests NOT executed**; no automated instrumentation pass, no independent Play signed release proof.
- Script **did not install/uninstall, clear data, initiate paid CI, sign AAB, contact Play Console or publish**. User separately reported everything works and explicitly designated Stable15, which is **user-reported functional device acceptance**.
- **DO NOT rerun 283 JVM tests or rebuild accepted debug APK without reason.** No image/manual asset reinstall or repeated phone commands needed.

## 4. Locked Play decisions and existing approved assets

- **Price FREE** — user already decided; never reopen paid/free decision unless user asks.
- **First rollout invite-only INTERNAL TESTING** with a small group of personally invited people. Not initial Closed Testing or public Production rollout. Distinguish any later eligibility requirement for public Production from user's initial Internal Testing decision.
- Google Play developer account already exists/paid; last shown `Create app` enabled but no confirmed creation of Atelier Time app draft or uploaded signed AAB. If user provides updated screenshot, adapt.
- English en-US Play app default listing draft: `Atelier Time: Art Studio Timer`; approved Hebrew localized title `Atelier Time - טיימר לסטודיו` (from `docs/ASO01_GOOGLE_PLAY_STORE_LISTING_HE_EN_2026_10_30.md`). Use previously approved actual text; avoid re-asking name.
- Existing verified app privacy link: https://aviy99.github.io/Atelier-Time/privacy-policy.html . Policy also stored in `PRIVACY_POLICY.md` and `docs/privacy-policy.html`, linked from app About. Do not recreate.
- Approved Play store artwork archived on `main`: `artwork/play-store/Atelier_Time_Google_Play_Approved_Assets_Complete_Pack.zip` (icon, approved feature graphic, 8 approved screenshots #1,3,4,5,6,8,9,10). **Do not regenerate or modify artwork**, but confirm screenshots still describe UI that will be shipped with Stable15; replace only on user's express request if necessary.
- Reaudit Data Safety and FGS specialUse declared behavior for the **actual signed Stable15 AAB**, not historical drafts; prior docs: `docs/PLAY_DATA_SAFETY_STABLE12_SOURCE_AUDIT_2026_11_01.md`, `docs/D128_SPECIALUSE_PLAY_CONSOLE_DECLARATION_AND_RATIONALE_2026_10_30.md`, `docs/STAGE8_SPECIALUSE_FGS_POLICY_REVIEW_2026_10_30.md`.
- No evidence of signed production AAB, configured Play signing key, uploaded release, internal testing invites, Play review, public Production access or publication. Debug APK **not a store artifact**.

## 5. Next authorized workflow: store preparation (not automatic publication)

1. **Create or inspect PRIVATE Play Console app draft**: Play Console `All apps → Create app`, App, **FREE**, default en-US, existing English name; accept declarations, do not create a second draft if existing. Request only a dashboard screenshot to see actual next steps.
2. **Prepare bilingual store listing** from approved ASO and selected artwork, review policy/privacy/data use, target audience, FGS declaration and existing app behavior. Keep listing non-public.
3. When **separately approved by user**, create signed **release AAB for frozen Stable15 code41** locally on Samsung Code on the Go without paid GitHub Actions; protect keystore/signing secrets outside GitHub. Audit final merged manifest, release shrink/permissions and actual binary before submissions.
4. With **separate user authorization**, publish first only to **Internal Testing**, invite limited selected testers. Later Closed Testing/Production eligibility and external public publication remain future approval gates.
5. Future user-reported modifications: use `android-native-kotlin-compose` under next D and versionCode, full phone QA, explicit stable acceptance and new frozen branch; never edit Stable15.

## 6. Operational rules

- Preserve all existing Saved Classes, Sound settings, diagnostic history, installed app data, approved Vitruvian/launcher artworks and the private original on-device `.cg` environment.
- **Do not invoke GitHub Actions or paid cloud compilation.** All compiling and signing on user's Samsung with explicit consent. No automatic install, uninstall or data clear. No Play upload or publication without permission.
- The **latest** state is Stable15/D147/code41 accepted. Earlier D147 "build pending" and Stable14 "latest" entries are historical. Do not tell user to rerun `build_d147...` after success.
- The user favors concise Hebrew responses and actionable steps without repeating already-settled choices.
- Stable15 certificate: [`STABLE15_D147_UI_INSTALL_USER_ACCEPTANCE_2026_11_05.md`](STABLE15_D147_UI_INSTALL_USER_ACCEPTANCE_2026_11_05.md). The frozen branch and commit above are the definitive restoration identifiers.

**Quick start for next chat:** `המשך את Atelier Time לפי docs/HANDOFF_CURRENT_STABLE15_TO_GOOGLE_PLAY_2026_11_05.md ב-GitHub. Stable 15 / D-147 / code41 הוכרזה יציבה באישור המשתמש אחרי 283/283 בדיקות JVM, בנייה מקומית מוצלחת ואימות התמונה בתוך APK. הענף stable/android-d147-qa15-ui-install-accepted קפוא. מחיר FREE, תחילת הפצה Internal Testing למוזמנים בלבד. המשך בהכנות Google Play ללא בניית ענן בתשלום, שינוי Stable או מחיקת נתונים.`
