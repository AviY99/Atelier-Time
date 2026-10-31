## 2026-10-30 — D-136 full-size launcher icon (source-only)

**D-136 LAUNCHER ICON SCALE FIX — 2026-10-30 (LATEST; CODE30 SOURCE ONLY):** User's screenshots of Android app/home and APK/file installer show **D-135/code29 launcher picture too small, nested white square + outer mask**. User explicitly instructed that this correction is **D-136 (D-135 already exists)**. Android development branch now changes only `atelier_launcher_foreground.xml` bitmap inset **+18dp → -3dp**, enlarging the approved Vitruvian clock art to the OEM adaptive mask and visually hiding its white raster edge, while keeping 12/9/3/6/figure visible in a rounded-square mockup. All original launcher manifest references/background, **About artwork**, all other app features and data unchanged; versionName **D-136**, versionCode **30**, targetSdk36. This is a source/preflight change, not a guarantee for all manufacturer masks; **D-136 NOT BUILT/INSTALLED/DEVICE QA-VERIFIED**. Local Samsung CoGo-only build from `android-native`: `git pull --ff-only && bash tools/build_d136_launcher_icon_scale_on_phone.sh`; build only, no install, cloud Action, paid CI or data clear. After BUILD PASS, separately approved in-place install and home/launcher/APK icon check. Google Play 512px icon submission remains separate. Stable 9/D-126 immutable recovery checkpoint; ASO-01/release declarations unchanged. [D-136 details](docs/D136_ENLARGE_VITRUVIAN_LAUNCHER_ICON_2026_10_30.md).

## 2026-10-30 — D-135 launcher icon manifest/root-cause fix (source only)

**D-135 VITRUVIAN ANDROID LAUNCHER ICON — 2026-10-30 (LATEST; SOURCE-READY, NOT BUILT):** User supplied Samsung screenshot proving **D-134/code28/targetSdk36 local BUILD SUCCESSFUL**, then confirmed **"הכל עובד"** (the app works). User immediately reported a remaining defect: **the app/installed APK still has no correct Vitruvian launcher icon**. Verified root cause by reading `android-native/app/src/main/AndroidManifest.xml`: `<application>` lacked BOTH `android:icon` and `android:roundIcon`, despite the approved Vitruvian 320×320 WebP artwork already bundled and displayed in About. **D-135/code29** Android development source now declares `@mipmap/ic_launcher` and `@mipmap/ic_launcher_round`, with Android 8+ adaptive icon XML layers, an 18dp inset around the same user-approved WebP and warm parchment background; image itself, About, timer/FGS/audio, diagnostics, Saved Classes/Sounds, permissions and ASO-01 unchanged. A Samsung CoGo build-only helper is committed; **D-135 NOT built, installed or runtime-verified**. Current user-reported working D-134/code28 remains installed with this icon defect; frozen user-designated Stable 9/D-126 remains immutable and no Stable 10 promotion. Next: Samsung Code on the Go, from `android-native`, `git pull --ff-only && bash tools/build_d135_launcher_icon_on_phone.sh`; then obtain separate consent for **in-place install** without uninstall/clear data, and visually verify launcher/app-drawer/package-installer icon plus data preservation. No paid GitHub Actions or cloud build. [D-135 specific technical record](docs/D135_ANDROID_LAUNCHER_ICON_2026_10_30.md).

## 2026-10-30 — D-134 device build PASS and user acceptance

**D-134 — USER ACCEPTANCE / BUILD PASS (2026-10-30; CURRENT):** User-supplied Samsung Code on the Go screenshot verifies `BUILD SUCCESSFUL` for **D-134/versionCode28/targetSdk36**, local candidate APK `/storage/emulated/0/Download/Atelier-Time-D134-API36-Candidate.apk`; that build-only script itself did **not install or run tests**. The user's immediate follow-up **"הכל עובד" ("everything works")** reports the D-134 app working as requested. Record **D-134 as the latest user-accepted Android development version** based on that confirmation, including the requested **Run diagnostic reports: confirmed Delete all and a scrollable five-row visible history**, while retaining Export/Delete and Saved Classes/Sounds. This is USER acceptance, **not a claim of independently observed D-134 installation, executed automated instrumentation (the new isolated AndroidTest is authored but NOT run), full regression tests, signed release or Google Play publication**. No new build, install, deletion or QA performed by assistant. Frozen **Stable 9/D-126** remains last explicitly user-designated stable recovery (historical 285/285 pass); do not name Stable 10 without explicit designation. The D-134 candidate code and docs remain unchanged; next is Google Play preparation/approved ASO-01. [D-134 record](docs/D134_DIAGNOSTIC_REPORTS_DELETE_ALL_FIVE_SCROLL_2026_10_30.md).

## 2026-10-30 — D-134 diagnostic history Delete all + five visible rows

**D-134 RUN DIAGNOSTIC REPORTS — 2026-10-30 (LATEST SOURCE-READY STATUS):** User reports **D-133/versionCode27 was installed and everything works**, resolving D-132 About/WhatsApp issues. New user request: **Delete all** button in the Run diagnostic report history and show **only the five newest reports in the visible list**, scroll inside the report list to access every older one. **D-134/versionCode28 Android dev source** now adds (1) bounded 280dp LazyColumn showing ~5 rows normally with all reports retained and scrollable, (2) explicit, count-aware confirmation dialog for irreversible **Delete all**, enabled only if reports exist, and (3) scoped `RunDiagnosticsArchive.deleteAll()` deleting ONLY private `run-*.jsonl` reports under `atelier_run_reports`, not Saved Classes/Sounds/other files or exported copies. Existing per-file Export/Delete retained. New isolated Android instrumented test for deleting eight reports while retaining unrelated file was WRITTEN **but NOT RUN**. **D-134 SOURCE ONLY: NOT BUILT, INSTALLED OR DEVICE QA-VERIFIED.** Current user-installed/accepted development candidate remains D-133/code27; frozen explicitly designated stable checkpoint remains **Stable 9/D-126 (historical 285/285 QA PASS)**. Next: local Samsung Code on the Go only, `git pull --ff-only && bash tools/build_d134_diagnostic_history_on_phone.sh` from `android-native`; then separate permission for an in-place installation without uninstall/clear data. During QA, only delete reports the user agrees to discard; Delete all is irreversible. No FGS/timer/audio/saved-data, permissions, About/ASO, cloud Android Actions or paid build changes. [D-134 requirements/implementation/QA](docs/D134_DIAGNOSTIC_REPORTS_DELETE_ALL_FIVE_SCROLL_2026_10_30.md)

## 2026-10-30 — D-133 About cards and WhatsApp sharing repair (source ready)

**D-133 ABOUT COMPACT LAYOUT + WHATSAPP SHARE REPAIR — 2026-10-30 (LATEST):** User installed D-132/code26 and supplied Day/Night About screenshots showing repaired Night link contrast, removed white matte and third WhatsApp card. User explicitly confirms **Saved Classes, Sounds and the rest of the application still work**, but the 3rd card extends below visible bottom and tapping WhatsApp does NOT open it, so **D-132 is NOT fully QA-accepted**. D-133/versionCode27 Android development source now (a) reduces vertical padding 14→8dp and spacing 6→2dp ONLY in Privacy Policy and Instagram footer cards so the third card fits better, (b) makes the entire 3rd About Share card tappable while retaining WhatsApp icon action, and (c) uses WhatsApp's documented **https://wa.me/?text=** prefilled draft URL for WhatsApp and WhatsApp Business, with Android ACTION_SEND chooser fallback. Temporary message links ONLY to **https://www.instagram.com/aviyosfan/** until Play publication; no automatic send. D-133 code/build helper committed, **NOT BUILT, INSTALLED OR DEVICE-TESTED**. User-installed D-132 remains, Stable 9/D-126 frozen; D-130 last fully user-accepted functional candidate. No source outside About, no new permissions, FGS/timer/audio/saved data changes, paid cloud builds or Actions. Next: local Samsung CoGo `git pull --ff-only && bash tools/build_d133_compact_about_whatsapp_on_phone.sh` from `android-native`; only after BUILD PASS perform separately user-approved in-place install, then focused Day/Night/share/link/data checks. [D-133 source/QA record](docs/D133_COMPACT_ABOUT_AND_WHATSAPP_SHARE_2026_10_30.md).

## 2026-10-30 — D-132 temporary Instagram share behavior corrected

**D-132 USER'S EXACT INTERIM SHARE LINK — 2026-10-30 (LATEST; supersedes older D-132 Play-link notes below):** D-132/versionCode26 source has all three requested About changes prepared: (1) Night Privacy/Instagram links use high-contrast white `onSurface`, retaining Day brown, (2) Vitruvian About picture's ONLY edge-connected neutral-white matte is made transparent **without globally removing pale details of the art**, (3) the third `OutlinedCard` is labeled **Share with a friend** and includes a right-aligned WhatsApp icon. **Crucial approved temporary behavior:** tapping it opens WhatsApp (or WhatsApp Business; system chooser fallback) with `Atelier Time — an art studio timer for artists`, `Follow the developer on Instagram:` and **https://www.instagram.com/aviyosfan/**; it DOES NOT share the unpublished Google Play URL. Replace that link only AFTER Play publication with a later approved change. Android sharing is user-initiated; it does not auto-send. Existing D-131/code25 remains installed; D-132 has **NOT been locally built, installed or QA-tested**. Build on Samsung Code on the Go only from `android-native`: `git pull --ff-only && bash tools/build_d132_about_night_and_whatsapp_share_on_phone.sh`, then separate in-place install authorization. No new INTERNET/contacts permission, FGS/timer/audio/data changes, paid GitHub Actions or cloud compile. Stable 9/D-126 remains frozen, D-130 last fully accepted version. [D-132 implementation record](docs/D132_ABOUT_NIGHT_CONTRAST_VITRUVIAN_TRANSPARENCY_WHATSAPP_SHARE_2026_10_30.md).

## 2026-10-30 — D-132 Night links, About transparency and share card

**D-132 — ABOUT NIGHT LINKS, VITRUVIAN MATTE AND WHATSAPP SHARE (2026-10-30):** User's D-131 Night screenshot identified dark unreadable Privacy/Instagram labels and an opaque white background around the Vitruvian image. User additionally ordered a third bottom About card, matching the Privacy and Instagram cards: “Share with a friend” with a WhatsApp icon to open a share of the Google Play URL. Android D-132/code26 source addresses all three. WhatsApp share uses explicit ACTION_SEND with WhatsApp/Business preference and system chooser fallback; shared `https://play.google.com/store/apps/details?id=com.ateliertime.android` is a **future Play URL, not currently published**. Existing bundled Vitruvian bitmap is made transparent for About display in memory; Night links become white, Day unchanged. No new permissions, unrelated app changes, GitHub Actions, paid builds or releases. **D-132 NOT BUILT/INSTALLED/QA-VERIFIED**; D-131/code25 currently installed, D-130/code24 last user-complete acceptance, Stable 9/D-126 frozen. Samsung build: `git pull --ff-only && bash tools/build_d132_about_night_and_whatsapp_share_on_phone.sh` from `android-native`; separate in-place install approval only after local build PASS. [Full implementation and QA plan](docs/D132_ABOUT_NIGHT_CONTRAST_VITRUVIAN_TRANSPARENCY_WHATSAPP_SHARE_2026_10_30.md).

## 2026-10-30 — D-131 Vitruvian clock artwork inside About description

**D-131 VITRUVIAN ABOUT ARTWORK — 2026-10-30 (SOURCE PREPARED, NOT DEVICE QA):** The user supplied and approved the Vitruvian-Man clock icon from the large left panel of the composite artwork and explicitly requested its high-quality version INSIDE the top About DESCRIPTION card on the LEFT of the unchanged paragraph. Android dev branch now contains real bundled 320×320 WebP asset `android-native/app/src/main/res/drawable-nodpi/atelier_vitruvian_clock.webp` (Git blob verified), source `AtelierDesign.kt` with left artwork/right complete description and LTR layout even on RTL, version **D-131/versionCode25**, and Samsung CoGo build-only helper `android-native/tools/build_d131_about_vitruvian_icon_on_phone.sh`. **No build/install/user QA is claimed for D-131.** D-130/code24 is still the installed, user-approved version; Stable 9/D-126 remains the last frozen recovery checkpoint. Existing Instagram portrait, Privacy/Instagram links, saved data, timer/FGS/audio/permissions and ASO-01 are unchanged. No paid GitHub Android Actions or cloud builds. [Exact D-131 requirements and local QA gates](docs/D131_ABOUT_VITRUVIAN_ICON_2026_10_30.md).

## 2026-10-30 — ASO-01: app discovery specification and Hebrew/English Google Play drafts

Recorded original 96 user-provided query lines and the user's explicit approval of all ASO recommendations in [ASO-01 requirements](docs/ASO01_DISCOVERABILITY_REQUIREMENTS_AND_USER_APPROVAL_2026_10_30.md); prepared validated localized titles and descriptions in [ASO-01 listing draft](docs/ASO01_GOOGLE_PLAY_STORE_LISTING_HE_EN_2026_10_30.md). Release preparation now includes ASO-01 before graphics/store metadata; post-launch search QA, clicks-based Store performance review and optional keyword-targeted Custom Store Listings follow after public indexing. No APK/Play Console changes, no paid Actions. D-130 accepted; frozen Stable 9 unchanged.

## 2026-10-30 — D-130 user acceptance PASS

**D-130 USER ACCEPTANCE COMPLETE — 2026-10-30:** Following the D-130/code24 local Samsung BUILD SUCCESSFUL and user-supplied About screenshot showing the right-hand circular Instagram image, the user explicitly confirmed: **both About external links work; all Saved Classes, Sounds, and other data were preserved; everything works as required**. Therefore **D-130/versionCode24 is the current installed, user-accepted Android development candidate**; the focused About/image/link and post-upgrade data-preservation acceptance gates are **PASS**. This is *not* a claim of newly executed automated regression tests, store approval, signed release AAB, or a user-designated Stable 10. Most recently designated frozen stable checkpoint remains **Stable 9 / D-126 (historical 285/285 QA PASS)**. D-130 image is a fixed bundled offline resource, with no network refresh/INTERNET permission. No extra app code, build, install, paid GitHub Actions, or saved-data changes were performed for this documentation-only update. [D-130 acceptance record](docs/D130_FIXED_INSTAGRAM_PROFILE_AVATAR_2026_10_30.md).

## 2026-10-30 — D-130 user screenshot confirms local installation and avatar layout

**D-130 DEVICE SCREENSHOT QA — 2026-10-30 (latest):** User supplied a Samsung About-screen screenshot after the local D-130 BUILD PASS. Screenshot shows **Version D-130** in the running app, the user's bundled circular Instagram photo visibly on the RIGHT of the existing Instagram card, the Privacy Policy and Instagram cards, and no visible clipping. **Installed/visual About QA PASS by screenshot.** The screenshot alone does **not** verify tapping either link in D-130 or post-upgrade Saved Classes/Sounds preservation; these remain confirmation points. (Both links were separately user-confirmed working in D-129.) Do not claim automated D-130 tests, full regression PASS, Google Play acceptance, or Stable 10. Stable 9/D-126 remains frozen. No code changes/builds/reinstall/paid GitHub Actions for this docs-only record. [D-130 QA record](docs/D130_FIXED_INSTAGRAM_PROFILE_AVATAR_2026_10_30.md).

## 2026-10-30 — D-130 static Instagram profile photo (source only)

Prepared user-supplied profile photo as an offline, 64dp circular About Instagram image on the right; D-130/code24 source and local build helper ready. D-129/code23 installed and links user-confirmed PASS. D-130 not yet built/installed. No timer, sound, FGS, permission or online functionality changes. Stable 9 unchanged. [Record](docs/D130_FIXED_INSTAGRAM_PROFILE_AVATAR_2026_10_30.md).

## 2026-10-30 — GitHub Pages verified live in user screenshot; About policy URL switched to static HTML

User provided GitHub Pages settings screenshot verifying the public site at **https://aviy99.github.io/Atelier-Time/** is live. Source `main/docs/privacy-policy.html` is the public HTML policy intended for **https://aviy99.github.io/Atelier-Time/privacy-policy.html**, with privacy email **Avi_yosfan@walla.com**; `docs/index.html` links/redirects site root there. Android development About Privacy Policy card updated from the GitHub Markdown URL to this GitHub Pages HTML URL in [commit ab54d30](https://github.com/AviY99/Atelier-Time/commit/ab54d30647b13c2e72106d1410c70b2cd6d0f06c); separate Instagram card unchanged. External direct-page fetch was unavailable to the assistant: **do not mark link clicking or Google Play approval verified**. No local Android build/device install, no timer/audio/FGS/permissions/user-data changes, no Play submission, no cloud Android build; installed D-128 remains unchanged and Stable 9 immutable.

## 2026-10-30 | privacy contact provided, About links enabled in source

User supplied Instagram profile and privacy email; completed public, source-backed [canonical privacy policy](PRIVACY_POLICY.md) with direct **Avi_yosfan@walla.com** contact, and prepared `docs/privacy-policy.html` as static HTML source for optional dedicated hosting. Android dev `AtelierAboutPanel()` now has active Privacy Policy and Instagram buttons in two separate bottom cards, Privacy first. Exact targets are `https://github.com/AviY99/Atelier-Time/blob/main/PRIVACY_POLICY.md` and `https://www.instagram.com/aviyosfan/` (cleaned share URL). Deployed standalone web hosting is NOT complete: Vercel project creation returned 403; GitHub page is publicly readable but Play acceptance is unverified. **SOURCE-ONLY, UNBUILT/UNINSTALLED**; no app timing/audio/FGS/manifest/permissions/user data/Stable 9 changes, no paid Actions. Next: local build with version increment once approved, targeted About external-link QA, final Play acceptance.

## 2026-10-30 — Privacy Policy published on GitHub; both About links activated in source

User supplied `https://www.instagram.com/aviyosfan?srtk=b3N5b3o5dm1sM3Nr` as their own Instagram profile. The canonical profile destination was recorded without the optional tracking query: `https://www.instagram.com/aviyosfan/`. Prepared a complete English privacy policy, publishing [public readable Markdown](PRIVACY_POLICY.md) on `main` and Android branch and [independent static HTML source](docs/privacy-policy.html) in both branches. Publicly readable policy includes local Class/Sound/Run storage, device metadata included in diagnostics, voluntary user export, retention/deletion, no developer cloud upload, and Instagram/GitHub inquiry contact methods (no invented email). GitHub Pages unavailable; a new static Vercel project was blocked by project-creation permission 403. No paid/cloud GitHub builds. In dev source `AtelierDesign.kt`, activated **both separate bottom About link cards**: Privacy Policy `https://github.com/AviY99/Atelier-Time/blob/main/PRIVACY_POLICY.md`, Instagram `https://www.instagram.com/aviyosfan/` (commit `f8638007b293cddfd361d1384980a1e35cd74f27`). **Not built or installed:** previously accepted D-128/code22 APK remains on user's phone; source branch has unbuilt UI work requiring next authorized version/build. Google Play acceptance of the GitHub-hosted privacy URL and final privacy contact, final release manifest/SDK/Play Data safety, actual link QA are release gates, not claimed complete. Timer, FGS, audio, notifications, permissions and user data unchanged.

## 2026-10-30 — About footer Privacy Policy and Instagram frames placed (unbuilt source)

Partially completed earlier UI decision is now represented in **Android development source**, not only docs. `AtelierAboutPanel()` in `AtelierDesign.kt` has **two separate bottom-of-About OutlinedCard frames** after the existing About info card: Privacy Policy followed by Instagram. Both external links remain **disabled with explicit pending text** until a valid, final published privacy policy URL and user's exact verified Instagram profile URL are provided. New source edit [925742f](https://github.com/AviY99/Atelier-Time/commit/925742fb8178b7a975f895ca02c990da3d98628c). [Release order, acceptance criteria and missing URLs](docs/D128_ABOUT_PRIVACY_INSTAGRAM_RELEASE_PLACEMENT_2026_10_30.md). No fabricated link, no Play compliance claim, no app build/install/test, no user data edits, and no timer/audio/background/FGS/permission changes. Installed D-128/code22 and immutable Stable 9 remain unchanged; new source is not yet released.

## 2026-10-30 — Approved About footer: Privacy Policy and Instagram (documentation only)

User approved two distinct framed, full-width link cards at the **bottom of the existing About tab**, after the current identity/description card, in this order: **Privacy Policy** and **Instagram**. Verified location in `android-native/app/src/main/java/com/ateliertime/android/AtelierDesign.kt` inside `AtelierAboutPanel()` scrollable Column. Release steps updated: finalize/publicly host privacy policy, obtain exact Instagram URL, then insert both links together before store graphics/FGS declaration/locally signed AAB/Play review. See [approved placement and full release sequence](docs/D128_ABOUT_PRIVACY_INSTAGRAM_RELEASE_PLACEMENT_2026_10_30.md). Both URLs still pending; no placeholder live links, code changes, build, installation or Play submission.

## 2026-10-30 — D-128 privacy/Data safety documentary source inventory

Following user-approved retention of current `specialUse` behavior and completion of the [FGS declaration rationale](docs/D128_SPECIALUSE_PLAY_CONSOLE_DECLARATION_AND_RATIONALE_2026_10_30.md), prepared [privacy/Data safety release inventory](docs/D128_PLAY_PRIVACY_DATA_SAFETY_READINESS_2026_10_30.md). Source review confirmed no explicit INTERNET permission in current app source manifest, private Saved Class/Sound settings, private diagnostic JSONL logs including Android model/manufacturer/API/timezone, user-controlled SAF Export, and no explicit ads/cloud/analytics dependencies in app Gradle. Clarified [unpublished privacy policy draft](docs/STAGE8_PLAY_PRIVACY_POLICY_DRAFT_2026_10_29.md) to describe locally stored diagnostic device metadata and export destination. Final merged-manifest/SDK check, hosted policy contact/link, in-app access, Data safety and signed AAB remain future gates; no declaration submitted. **Documentation only:** no code, build, install, tests, cloud CI or user data changes.

## 2026-10-30 — **D-128 APPROVED FGS STRATEGY — 2026-10-30**; Play rationale documented, NO CODE CHANGE

User clarified and approved that Atelier Time's **core is unattended reliable live-class operation**, not continuous phone monitoring. The ongoing, up-to-date, non-stale status notification and background cue/timer continuity are essential to the product, not optional UI decoration. Current FGS runs in the same process as UI-owned clock/audio and improves process importance, besides notification and task-removal cleanup; no claim that Service computes time/plays audio or guarantees immunity to all OS stops. User approved **keep existing D-128 source/manifest/FGS unchanged** and proceed with policy justification instead of removing FGS or rewriting timer ownership. [Full Play Console declaration, official-policy assessment, video storyboard and limitations](docs/D128_SPECIALUSE_PLAY_CONSOLE_DECLARATION_AND_RATIONALE_2026_10_30.md). Play approval, video/link, hosted privacy policy, signed AAB and release remain OPEN. No app code, compilation, APK installation, tests, user data, cloud Actions or stable branches touched.

## 2026-10-30 — Documentation alignment and read-only specialUse Google Play review

User authorized documentation synchronization and a review **before any app code change**. Verified the D-128/code22 Samsung local build, in-place install, retained Saved Classes/Sounds and normal UI acceptance from existing user QA. Repaired prominent handoff/documentation index and both branch READMEs to distinguish **installed D-128 development candidate** from frozen **Stable 9 D-126** and historical pending-build text. Reviewed exact D-128 manifest, `ClassNotificationTaskService`, `ActiveClassStatusNotification`, `MainActivity`, `SessionEditorScreen` and audio ownership against current Android/Play FGS rules. Technical `specialUse` declarations match; **Play policy non-deferrability remains unresolved** because the service mirrors status and handles task exit rather than owning the timer/audio. Documented truthful declaration route and nontrivial redesign alternatives in [Stage 8 specialUse policy report](docs/STAGE8_SPECIALUSE_FGS_POLICY_REVIEW_2026_10_30.md). **No app code/permission edits, recompilation, installation, data clearing, paid Actions, test rerun, signed release, Play submission or stable promotion.** Common canonical markdown updated identically on `main` and `android-native-kotlin-compose`; branch-specific README history retained.

## 2026-10-30 — D-128 accepted user QA after installation

After the user's confirmed in-place installation of locally built D-128 (targetSdk 36/code22), the user replied **"כן הכל נבדק ותקין"** to the post-install check on Saved Classes, Sounds and unclipped UI. Record user-observed PASS for preservation and visuals in their Samsung, without inventing independent OS-version proof, Android 16-specific coverage, instrumentation, Play acceptance or Stable 10 designation. No new source/build/device operation and no repeat of previously accepted lifecycle/timer/audio suites. Stage 8 agreed technical/device gate closed; Play release policy/signing/branding work remains pending. [D-128 audit record](docs/D128_STAGE8_ANDROID_PLAY_POLICY_AUDIT_2026_10_29.md).

## 2026-10-30 — D-128 installation confirmed by user (runtime QA not yet confirmed)

After the Samsung `BUILD SUCCESSFUL` for local `D-128 / API 36` candidate and authorization to update in place, the user wrote **"הותקן"**. This confirms user-reported installation of D-128/code22 over D-127/code21. The user has **not yet stated** whether Saved Classes/Sounds/history remain present, whether the layout has clipping, or whether the phone runs Android 16. No such PASS is claimed; no new screenshot/ADB check, build, Google Play action or instrumented tests. Stable 9 remains frozen. See [Stage 8 D-128 record](docs/D128_STAGE8_ANDROID_PLAY_POLICY_AUDIT_2026_10_29.md).

## 2026-10-30 — D-128 Samsung build-only verification PASS

The user shared a real Samsung Code on the Go terminal screenshot of `tools/build_d128_api36_on_phone.sh`: `BUILD SUCCESSFUL — D-128 debug APK saved`. Local paths reported: `Downloads/Atelier-Time-D128-API36-Candidate.apk` and `Downloads/Atelier-Time-D128-API36-Manifest.txt`. This is `assembleDebug` compile/packaging evidence only; manifest content/source commit and APK SHA were not read independently. No APK install, no device runtime tests, no Cloud/GitHub Actions, no data changes, no prior suite reruns; installed D-127 and Stable 9 unchanged. In-place update requires explicit separate user authorization. See [D-128 Stage 8 QA record](docs/D128_STAGE8_ANDROID_PLAY_POLICY_AUDIT_2026_10_29.md) and [latest handoff](docs/HANDOFF_D127_STAGE7_TO_NEXT_CHAT_2026_10_29.md).

## 2026-10-30 — D-128 source-only: target Android 16 / API 36

User-approved minimum API update on Android development branch: `compileSdk=36` unchanged; `targetSdk: 35 → 36`, `versionCode: 21 → 22`, `versionName: D-127 → D-128`. No timer, sound, permission, foreground service, notification, or UI logic changes. A Samsung Code on the Go **build-only** helper (`android-native/tools/build_d128_api36_on_phone.sh`) checks D-128 identity, builds **only** a debug APK, prints relevant compiler errors, and writes APK/SHA/log into Downloads; never installs, runs tests or triggers cloud builds. **SOURCE COMMITTED / BUILD & ON-DEVICE QA NOT RUN.** Installed D-127/code21, app data and Stable 9 frozen ref unaffected. Android 16 target-dependent Back and edge-to-edge compatibility remains to verify on a compatible device when available; Play `specialUse` and signed store publication gates remain open. [Stage 8 record](docs/D128_STAGE8_ANDROID_PLAY_POLICY_AUDIT_2026_10_29.md).

**2026-10-29 D-127 build-report workflow repair:** D-127's build runner now invokes a privacy-minimized GitHub diagnostic publisher on both build PASS and FAIL (no publishing of the raw log). Reports target the public, separate `diagnostics/d127-build-reports` branch; writing requires existing authenticated git push on Samsung. Previous compiler-failure log can be reported **without rebuilding** from CoGo `android-native` with `git pull --ff-only && bash tools/publish_d127_build_diagnostics.sh --existing`. The damaged duplicated Termux target QA runner was repaired. **Not run on phone yet**; real Kotlin compile failed in screenshot, diagnostic line still missing, six Stage7 UI tests unexecuted, Stable 9/D-126 unaffected. [Evidence and commands](docs/D127_STAGE7_ACCESSIBILITY_ADAPTATION_AND_QA_2026_10_28.md).

## 2026-10-29 — D-127 six-test build/instrumentation safety gate (source only)

D-127/code21 has six **authored but unexecuted** accessibility Compose AndroidTest methods. Build-only CoGo script now verifies the code commit ancestry and six-method inventory, emits version/commit/hash/test-count manifest, and copies a guarded Termux test runner to Downloads. Runner refuses to run unless D-127/code21 is **already installed** from a separately authorized in-place upgrade, the main app is IDLE, test APK SHA-256 matches, and exactly one authorized ADB device is connected. It installs ONLY the test APK, runs ONLY `AccessibilityStage7UiTest`, and requires actual `OK (6 tests)` before reporting PASS. Neither script has been executed. This environment has no Android SDK, Gradle, ADB or Samsung access, so device compilation/instrumentation, 100%/130%/200% text and TalkBack tests remain open; no PASS claimed, and no historic D-126 285/285 suite rerun. No installed app/data or frozen Stable 9 modification. [Full Stage 7 QA record](docs/D127_STAGE7_ACCESSIBILITY_ADAPTATION_AND_QA_2026_10_28.md).

## 2026-10-29 — D-127 Design/Sound accessibility follow-up (SOURCE ONLY)

Committed [`c83c197`](https://github.com/AviY99/Atelier-Time/commit/c83c19738ea70bba0223acd84c46c93411276b7a): Design palette choice cards expose selected RadioButton state and accessible Day/Night names; the Design automatic night-start hour and Sound Class C / Free sound-duration sliders gain spoken labels. Sixth focused `AccessibilityStage7UiTest` added for Design selection and Auto slider. Existing five new methods retained. **Total: six authored, zero executed.** Only source and AndroidTest were edited in this follow-up; existing D-127 build-only runner remains **unexecuted**. Readback from GitHub confirmed source and test declarations. Android SDK/physical device not connected, therefore no compile, APK, AndroidTest, 100/130/200% screen matrix, TalkBack or user approval. Installed Stable 9 D-126/code20 retains its historical 285/285 PASS and user data; no stable snapshot or main APK change. Execute only D-127-specific QA on compatible Android/CoGo device when available; main-app in-place installation remains a separately authorized action. [D-127 Stage 7 record](docs/D127_STAGE7_ACCESSIBILITY_ADAPTATION_AND_QA_2026_10_28.md).

## 2026-10-29 — D-127 Stage 7 accessibility source refinement (NOT BUILT/INSTALLED)

Additional responsive Editor numeric input pairs and narrow HH:MM:SS wheel layout, spoken blocked-value and Free preset selection semantics; two extra targeted Compose tests (five total **unexecuted** new D-127 tests). Safe build-only runner added and **not run**. Older D-127 changes remain; installed Stable 9/D-126, 285 prior PASS, Saved Classes/Sounds and immutable branches unchanged. [QA state](docs/D127_STAGE7_ACCESSIBILITY_ADAPTATION_AND_QA_2026_10_28.md).

## 2026-10-28 — D-127 Stage 7 accessibility development (NOT DEVICE QA)

Development source commit `b9d7064` adds font-responsive tabs and controls, TalkBack descriptions for custom wheels and schedule chart, 48dp Free preset targets, and three authored but **UNRUN** Compose UI tests. Dev candidate D-127/code21 is **not built or installed**; Stable 9 D-126/code20, its 285/285 past PASS and preserved data remain unchanged. See [Stage 7](docs/D127_STAGE7_ACCESSIBILITY_ADAPTATION_AND_QA_2026_10_28.md).

# Changelog — Atelier Time

## 2026-10-28 — Stable 9 D-126: completed 285/285 tests and user-data preservation

The user promoted already-installed D-126/code20 to Stable 9 after the Samsung confirmed all planned automated testing: 243 JVM unit tests PASS, 42 physical-device Android instrumentation PASS, Gradle 72 tasks succeeded and both APK SHA-256 checks passed. The first connected test run reported four test-only failures corrected in the AndroidTest directory; the second run returned `OK (42 tests)` in 21.697s. User explicitly verified Saved Classes/Sounds retained unchanged. No production source changes, main-app APK update or reset. Stable 9 source/QA checkpoint `stable/android-d126-qa9-tests-complete` frozen. [Current status](docs/CURRENT_STATUS_AND_HANDOFF_STABLE9_2026_10_28.md), [QA certificate](docs/STABLE9_D126_285_AUTOMATED_QA_ACCEPTANCE_2026_10_28.md).


## 2026-10-25 — D-111: confirmed-finish Run and full automated QA prepared (DEVICE GATES PENDING)

- **Feature-first user direction:** on Run show calculated finish, require explicit approval or HH:mm override (earlier/later), and use signed changes to only the final two work Sessions. Block infeasible early targets (each final Session must retain >=1 minute), unrealistic late targets (2× original Class maximum) and invalid clock inputs with corrective feedback. The selected Class finish is a physical monotonic deadline. During pauses after the first final Session begins, only the unstarted last Session can be changed; completed phases remain immutable. Legacy one-Session default Run tolerates UI confirmation/start latency without enabling arbitrary custom adjustments.
- **Code:** D-111 `versionName=D-111`, `versionCode=6` on `android-native-kotlin-compose`. Changes to `RunEndTimePlanner.kt`, `MonotonicSessionTimer.kt`, `SessionEditorScreen.kt`; an obsolete test expectation was corrected to last-only compensation. Frozen D-108 Stable 4 not changed.
- **Coverage ready, NOT RUN:** 179 JVM `@Test` methods and 32 Android `@Test` methods (7 isolated Compose UI + 11 new finish/device + 7 preset persistence + 6 sound preferences + 1 silent AudioTrack). This inventory is source-audited, not a PASS claim. D-110's previous 14/14 on-device PASS remains the older baseline.
- **Safe 2-stage runners committed:** `android-native/tools/build_all_qa_on_phone.sh` (CoGo Gradle JVM tests + app/AndroidTest APKs, copy to Downloads), then `android-native/tools/termux_run_all_device_qa.sh` (ADB in-place update to D-111, assert installed version, install test APK, run AndroidJUnitRunner and require `OK (32 tests)`, save output locally). Only execute with NO active Class. No `uninstall`, `pm clear`, data resets, or modifications to stable refs.
- **Next action:** user runs `cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native && git pull --ff-only && bash tools/build_all_qa_on_phone.sh` in Code on the Go, then `bash ~/storage/downloads/Atelier-Time-D111-QA.sh` in Termux if gate 1 passes. **Do not call D-111 built/tested/installed until device output confirms it.**
- **Detailed handoff + acceptance gates:** [D-111 approved finish and automated QA](ANDROID_D111_APPROVED_FINISH_AND_AUTO_QA.md). Long actual 3-hour Doze/audio, OS process reclaim/Back/Recents notification checks and real ±120s wall-clock QA remain explicitly separate device follow-ups; no false PASS claims.

## 2026-10-25 — D-110 user phone acceptance

- 2026-10-25 — **D-110 USER PHONE CONFIRMATION: "הכל תקין" (everything is OK)**, reported directly in reply to instructions for opening the freshly built `Atelier-Time-D110.apk` as an **update**, checking About, and ensuring saved Classes/Sound choices survive. **Status: user-reported D-110 functional acceptance / installation workflow OK**, not independently captured screenshots of About or a database inspection. The preceding Code on the Go Gradle `:app:testDebugUnitTest :app:assembleDebug` **BUILD SUCCESSFUL in 47s**, 42 tasks; package prepared as `versionName="D-110"`, `versionCode=5`. **No explicit real-device ±120s manual/system clock correction test was reported**; therefore the targeted wall-clock correction QA remains **OPEN ON DEVICE**, although source-level Kotlin tests and virtual clock simulation passed. Full 3-hour real-device/Doze and QA-R01 connected instrumentation also remain open/blocked. **D-108 Stable 4 frozen unchanged; D-110 is not declared Stable 5, no new build or deployment performed in this documentation update.**

## 2026-10-25 — D-110 clock-correction reliability fix (source only)

- **D-110 / 2026-10-25 — MONOTONIC CLASS DEADLINE FIX IN SOURCE, NOT YET BUILT:** QA-R03 identified Class remaining/elapsed display drift after ±120s wall-clock correction and a more serious wall-clock-based physical Class finish check. The narrow fix uses `SystemClock.elapsedRealtime()` (already the Timer clock) for physical Class elapsed/remaining and Class deadline enforcement, retaining Clock/Start/End wall-time labels and all approved controls/cues/UI. Added 3 Kotlin JVM test methods for clock corrections, Pause/Next and fixed original deadline. Prepared next actual APK `versionName="D-110"`, `versionCode=5`. Assistant-side extracted Kotlin clock harness PASSED 7 targeted groups and a 3-hour/10,801-checkpoint simulation; **full CoGo Gradle `:app:testDebugUnitTest :app:assembleDebug`, real D-110 APK and device acceptance NOT EXECUTED**. No edit to frozen `stable/android-d108` or production user data. [Scope and execution plan](ANDROID_D110_MONOTONIC_DEADLINE.md).

## 2026-10-25 — Post-Android Stable 4 QA documentation sync (no build)

- Synchronized up-to-date Stable 4 handoff/checkpoint, user-required pre-store logo/icon gate, current project decision/test/roadmap records from authoritative `main` into `android-native-kotlin-compose`.
- Added [Android reliability QA gate](docs/ANDROID_RELIABILITY_QA_POST_D108.md), reviewing 14 source-level connected test methods and separating unexecuted Android instrumentation / multi-hour / Doze / process-kill / persistence gates from already-approved D-108 functional QA.
- Preserved frozen `stable/android-d108` and all Kotlin/Gradle app sources. **No new APK, no updated versionCode, no claimed test PASS, no new product decision.** Future actual APK must reflect the real D iteration (next planned D-110) and monotonic Android versionCode.

## 2026-10-25 — D-109 Android Stable 4 explicitly approved; frozen checkpoint and new-chat handoff

Following the screenshot-verified D-108 35s Gradle debug build/JVM test task PASS and user's reported installation/functional acceptance `"הכל תקין כעת"`, the user explicitly declared **Android Stable 4** and asked to close this conversation with a self-contained handoff. **Created immutable `stable/android-d108`** exactly at development source SHA `02beb0af46719301cccb72ad4698a337ab416d73`; prior Stable 1/2/3 checkpoints preserved. D-108 bugfix accepted: ongoing Class notification remains live on Home/Recents overview and is removed on Back or Recents task swipe-away. Native Kotlin/Jetpack Compose, closed Design/Sound/Timer behavior and phone APK data remain unchanged. **This is user-accepted debug Stable 4, not Play release signing/FGS specialUse certification/connected instrumentation/Doze/reboot/stress proof.** Main docs updated; canonical [Stable 4 record](docs/ANDROID_STABLE_4_D108.md) and [standalone new-chat handoff](docs/CHAT_HANDOFF_AFTER_STABLE_4_2026_10_25.md). User-mandated approved app logo/launcher icon gate still before store publishing; further QA/Release gates remain OPEN. No code, Gradle build or APK install performed for handoff itself; only GitHub docs and frozen rollback branch.

---

## 2026-10-25 — D-108 phone acceptance: notification dismissal fix CLOSED

Following the successful D-108 on-phone Gradle compile/JVM-test-task/debug APK build (35s, 42 tasks: 9 executed/33 up-to-date), the user reports **"הכל תקין כעת"** and accepts the notification lifecycle repair. **Mark D-108 FUNCTIONAL USER QA PASS / CLOSED**: existing notification remains active when app goes to background via Home or Recents overview, while Back/task-swipe closes app and clears the formerly stale notification. The one-line Kotlin fix `Intent.addFlags(...)` resolved the previous compile failure; no further rebuild requested. Acceptance is user-reported, not per-step captured device footage; Android instrumentation, Doze/process-kill, Play `specialUse` foreground-service policy review, data recovery and branded app logo remain independently OPEN. Frozen Stable 3 `stable/android-d102` unchanged, no Stable 4 branch created. [D-108 acceptance](docs/NOTIFICATION_EXIT_CLEANUP_D108.md).

---

## 2026-10-25 — D-108 successful 35s phone rebuild, APK ready; installation QA pending

Following correction of Kotlin compiler `ClassNotificationTaskService.kt:35:17 'val' cannot be reassigned` to Intent `addFlags(...)`, the user's phone screenshot now verifies **`BUILD SUCCESSFUL in 35s`**, 42 Gradle tasks (9 executed, 33 up-to-date), successful `:app:testDebugUnitTest` and `:app:assembleDebug`, and copied ~12M `/storage/emulated/0/Download/Atelier-Time-D108.apk` (Oct 25 at 10:09). Earlier D-108 `BUILD FAILED in 25s` is superseded by this successful rebuild. **D-108 install/notification dismissal QA STILL PENDING**: after update-install preserving app data, verify About Version D-108; Home/Recents overview preserve live notification, Back and swipe-out remove notification. Individual JUnit counts/Android instrumentation not shown. Frozen Stable 3 untouched, Play FGS specialUse permission policy and pre-store app icon/logo still to review.

---

## 2026-10-25 — D-108 targeted compile-error fix committed; retest pending

User's actual on-device `grep` from `~/atelier-d108-build.log` reveals the **only shown Kotlin diagnostic**: `ClassNotificationTaskService.kt:35:17 'val' cannot be reassigned`. Exact source check ties it to `onStartCommand(..., flags: Int, ...)` shadowing the Intent.flags property inside `Intent.apply`. Source-only fix in Android development branch commit [`02beb0af`](https://github.com/AviY99/Atelier-Time/commit/02beb0af46719301cccb72ad4698a337ab416d73) uses `addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)` rather than assigning to the immutable `flags` argument. **NO D-108 retest BUILD SUCCESSFUL has occurred yet**. Next phone pull, JVM Gradle unit tests and APK assemble, then update-install to QA Back/Recents notification removal. No changes to timer, audio, design or frozen Stable 3.

---

> **D-108 ACTUAL PHONE COMPILE GATE FAILED (2026-10-25).** User screenshot confirms `:app:compileDebugKotlin` failed with `Compilation error. See log for more details`, **BUILD FAILED in 25s**, **31 actionable tasks: 6 executed, 25 up-to-date**. Screenshot does not show the Kotlin `e:` filename/line diagnostic. The full output was saved by `tee` to `$HOME/atelier-d108-build.log` on the user phone. Requested precise first compiler errors using `grep -n -E '(^e:|error:|Unresolved reference|Type mismatch|Cannot access)' "$HOME/atelier-d108-build.log" | head -50` WITHOUT rebuilding. Do NOT claim that D-108 builds, or its APK was copied/installed, or that Back/Recents notification cleanup works yet. Source code fix is pending compiler diagnostic. D-107 installed active background notification remains previously user-tested but has stale-on-Back/swipe defect; frozen Stable 3 remains unchanged.

## 2026-10-25 — D-108 Back and Recents swipe notification dismissal (source-only; device gate open)

Real D-107 phone QA finds the in-background notification works with Home/square and Recents overview, but notification freezes after real app exit through Android Back and after swiping the task out of Recents. D-108 source now ensures that Back explicitly finishes the Activity, and guards notification dismissal on Activity `isFinishing && !isChangingConfigurations` (not on Home, task overview or rotation). For Recents swipe, a small notification-only Android service receives `onTaskRemoved` and cancels the same notification ID, without modifying Compose Timer/audio state. Added Android 14+ `specialUse` FGS manifest and permission declarations (Play policy type review pending), three pure-JVM exit-policy tests authored but not executed on device, next app `versionName=D-108` and `versionCode=4`. **No new on-phone build/APK install or user QA has yet occurred; D-107 stale notification defect is not claimed resolved.** Stable 3 unchanged. [D-108 status and QA](docs/NOTIFICATION_EXIT_CLEANUP_D108.md).

---

## 2026-10-25 — D-107 44s on-phone Gradle/JVM build PASS; notification UX open

User terminal screenshot confirms `BUILD SUCCESSFUL in 44s` for Android `:app:testDebugUnitTest :app:assembleDebug` (42 tasks: 21 executed, 21 up-to-date) and successful copy of ~12M `Atelier-Time-D107.apk` to Downloads, Oct 25 09:16. **Only build and JVM test task PASS**; exact JUnit case count and connected instrumentation not shown. **New APK update-install and user acceptance of notification permission, notification shade content, countdown, Pause/Resume and Exit cleanup still unconfirmed.** Next install the already copied D-107 APK as an update, preserving user Classes/sounds, and verify About Version D-107 and notification; no need for another build. Frozen Stable 3 unchanged; final branded launcher/logo remains future pre-store work.

---

## 2026-10-25 — D-107 requested ongoing Class status notification (source only)

User explicitly asked to add the missing ongoing Android notification after accepting locked-screen/background operation of existing Timer and A/B/C audio. Committed native development source: new optional LOW-importance silent notification channel with active Class name, current Session/Break and phase/Class remaining, a running countdown and Paused/Stopped state; tapping reopens the Activity; cancels at Exit/end. Android 13+ notification permission requested only at Run/Start; denial never blocks Timer/audio. Five new JVM test methods written, not yet run, for pure Kotlin notification status. Functional monochrome STATUS icon is not the pending launcher/Play-store brand logo. Next debug build `versionName = "D-107"`, `versionCode = 3`; **NO D-107 Gradle/JVM PASS, APK install or live notification QA verified yet**. No FGS or new assurance after Recents swipe/process death/Doze. Stable 3 remains frozen; PR #3 Draft/unmerged. [Details and phone gate](docs/ONGOING_CLASS_NOTIFICATION_D107.md).

---

## 2026-10-24 — D-106 real D-build About version and no redundant foreground sound tests

User superseded prior `About Version 3` policy: show the **actual D-development iteration**, currently `Version D-104`, and use matching D-number in future APKs. Only Android DEVELOPMENT files were changed: Gradle `versionName = "D-104"` (`versionCode = 2` unchanged), and Compose About now displays the full installed `PackageManager.versionName` rather than extracting digits. **No new build/install** following this source change; the already assembled `Atelier-Time-D104.apk` remains the earlier Version 3 build. User also confirms normal sound cues and timing/phase transitions had been previously tested and work; **do not repeat the same foreground 3-minute A/B/C acceptance test**. Locked/background/Doze cue reliability, offline persistence and connected Android instrumentation are still separate OPEN QA gates. Stable 3 checkpoint and icon/logo pre-store approval remain unchanged.

---

## 2026-10-24 — D-104 34s phone JVM test/build PASS, APK copied; install/QA pending

Screenshot from phone Code on the Go shows **BUILD SUCCESSFUL in 34s** for `:app:testDebugUnitTest :app:assembleDebug`, 42 Gradle tasks (9 executed, 33 up-to-date). A ~12M `Atelier-Time-D104.apk` was copied to `/storage/emulated/0/Download/` at 21:25 on Oct 24. This is evidence of JVM Gradle test task and debug APK assembly PASS, **not** Android connected instrumentation, per-test JUnit count, actual update-install, installed About Version 3, saved-data retention or live 3-minute A/B/C audio QA. No rebuild needed: next install existing APK as an update without uninstall or clearing saved Classes; check About `Version 3`, data and A 01:00 / B 02:00 / C 03:00. D-103 Stable 3 and icon/logo pre-store requirement unchanged.

---

## 2026-10-24 — D-104 Timer reliability tests & next About Version 3; D-105 pre-store icon stage

After user accepted Android Stable 3, inserted mandatory pre-store **logo + launcher/store icon** design, approval and installed icon validation gate, before any Google Play submission. AndroidManifest currently has no launcher icon. Four new pure Kotlin/JVM tests added in `TimerReliabilityGateTest.kt`: accelerated 200ms 3-minute A/B/C boundaries, 3-hour monotonic deadline, delayed UI cue suppression, and one-time paused deadline C. These have **NOT been executed** in current conversation; existing Timer code/sounds/UI unchanged. As explicitly promised, NEXT development APK uses `versionName = "3"` and `versionCode = 2` via `android-native/app/build.gradle.kts`, so installed About will read Version 3 after next update. APK not yet built/installed, no user acceptance claimed. Frozen Stable 3 `stable/android-d102` untouched; PR #3 Draft/unmerged. Next phone run Gradle JVM tests+assemble with CoGo override, capture XML/test PASS and update-install preserving Classes. Specifications: [Timer QA](docs/TIMER_RELIABILITY_D104.md), [logo gate](docs/ICON_LOGO_PRESTORE_GATE.md).

---

## 2026-10-24 — D-103 Android Stable 3 accepted, Sound closed; About Version 3 on next build

The user reported that D-102's initial install failure came from insufficient free storage on the Android phone; after freeing storage, **installation succeeded and all requested Sound behavior worked**. User explicitly names this **Android Stable 3**, closing the Sounds feature/UI area including D-100 libraries/durations, D-101 compact Shared or Separate A/B/C modes, and D-102 active-mode-only source-disable guard. New frozen branch [`stable/android-d102`](https://github.com/AviY99/Atelier-Time/tree/stable/android-d102) anchors exact source [`78878801ea515deccc4e64cdc62b2b3db595769a`](https://github.com/AviY99/Atelier-Time/commit/78878801ea515deccc4e64cdc62b2b3db595769a). Prior screenshot confirms `BUILD SUCCESSFUL in 29s`, unit-test Gradle task, APK assembled and copied; user now explicitly confirms installation/functional PASS, not connected Android instrumentation, individual JUnit totals or background/Doze. **NEXT BUILD MUST SHOW `Version 3` IN ABOUT.** Verified `AtelierAboutPanel()` in `AtelierDesign.kt` uses the numeric part of Android PackageManager `versionName`; the latest Gradle build config still says `versionName = "0.1.0-dev"` and `versionCode = 1`. At the next code/build increment change the source-of-truth `versionName` to `"3"`, evaluate upgrading `versionCode` appropriately, update-install/test About while preserving data. Do not rebuild or mutate the already accepted Stable 3 checkpoint solely for documentation. Stable 1/2 remain frozen; PR #3 remains Draft/unmerged. See [D-103 stable checkpoint](docs/ANDROID_STABLE_3_D102.md).

---

## 2026-10-24 — D-102 phone build 29s PASS; install and checkbox regression QA pending

Phone screenshot verifies `BUILD SUCCESSFUL in 29s` for `:app:testDebugUnitTest` and `:app:assembleDebug` (42 Gradle tasks: 8 executed, 34 up-to-date), and a 12M `/storage/emulated/0/Download/Atelier-Time-D102.apk` copy dated October 24, 20:39. Screenshot does not show exact commit or individual JUnit case count. No user confirmation yet of installing D-102 or fixed same-for-all checkbox validation; only D-101 is installed and confirmed with that known defect. Next update-install D-102 without clearing saved Classes and verify dormant-mode source uncheck succeeds, mode switching re-enables needed sources, active source still cannot be disabled. Frozen Stable 2 unchanged.

---

## 2026-10-24 — D-102 active-only sound source guard — source fix, on-phone Gradle/QA pending

User screenshot from installed D-101 confirms compact Sound UI works overall but the source-disable error is triggered by a sound used only in the *hidden* mode. Current D-102 dev source now checks only Shared's single active sound or Separate's active A/B/C; persisted dormant choices do not block disabling their source. On mode switch, required libraries are auto-reenabled so the preserved sound can play. New pure JVM tests and Android preference persistence instrumentation test sources cover both mode directions and the still-required active/last-source protection. **Tests are written, not run; D-102 not yet phone built, installed or approved.** D-101 is accepted with this identified defect. User-approved `stable/android-d098` remains untouched. [Reproduction and QA](docs/SOUND_ACTIVE_SOURCE_D102.md).

---

## D-101 phone build pass — 2026-10-24

**D-101 build evidence, 2026-10-24:** Phone screenshot verifies Gradle `BUILD SUCCESSFUL in 42s`, 42 tasks (8 executed, 34 up-to-date), `:app:testDebugUnitTest`, `:app:assembleDebug`, and copied `Atelier-Time-D101.apk` (12M, Downloads). Individual JUnit method count/local SHA not visible. **Installation and manual Sound UI/audio acceptance pending**; D-100 last phone-accepted build. Do not rerun build; install existing APK as update preserving stored Classes. Stable 2 unchanged. Details: [SOUND_COMPACT_D101.md](docs/SOUND_COMPACT_D101.md).

---

## 2026-10-24 — D-100 user accepted; D-101 compact shared/separate Sound UI, source-only

**LATEST D-101 / 2026-10-24 — COMPACT SOUND SCREEN SOURCE IMPLEMENTED; PHONE BUILD/INSTALL/UX QA PENDING:** User confirms installed D-100 **"הכל עובד כנדרש"** (manual phone acceptance; not Android connected-instrumentation or locked-screen evidence). New explicit Sound-only UX requirement: denser Sound tab, all source checkboxes displayed compactly in one row, horizontally aligned mode buttons and selection/Play buttons. Two modes: **Same sound for all** (one sound for A/B/C and **one shared 5 or 10 second playback duration for ALL events, including Class end**) or **Separate A/B/C** (only then show independent A/B 5/10s and C 5–60s slider). Existing D-100 installs default to separate mode to preserve choices. Separate sounds/lengths and shared sound/length persist independently and survive toggles/restarts; Android system/file pickers feed the currently visible mode. Native code committed on `android-native-kotlin-compose`, [current D-101 tip](https://github.com/AviY99/Atelier-Time/commit/19806988458e50cdcf7366ada91320ab3c03aa86) (GitHub readback verified); added JVM/instrumentation regression **test source, NOT EXECUTED**. **D-101 has NOT yet passed phone Gradle/JVM test task, APK installation or user visual/audio QA.** Next gate: same already-proven native CoGo Gradle 9.6.1 test+assemble with ARM64 AAPT2 override, then update-install preserving Classes, verify sound mode presentation, shared 5/10s effective duration for all cues and restore A/B/C settings. Canonical QA spec [SOUND_COMPACT_D101.md](docs/SOUND_COMPACT_D101.md). User-approved Stable 2 `stable/android-d098` remains frozen, Draft PR #3 unmerged, no paid CI.

---

## 2026-10-24 — D-100 phone build successful, installation and audio QA pending

**D-100 actual phone build evidence (2026-10-24):** User screenshot shows `:app:testDebugUnitTest`, `:app:assembleDebug`, **BUILD SUCCESSFUL in 41s**, 42 Gradle tasks (**8 executed, 34 up-to-date**) in the existing `android-native` CoGo terminal. This upgrades prior D-100 'source only' status to **Android Gradle JVM-test TASK and debug APK assembly PASS**; screenshot does not identify local checkout SHA or actual per-test JUnit count. **NO D-100 APK update-install or runtime audio source/duration validation is yet reported.** Do not rerun Gradle. Next copy `app/build/outputs/apk/debug/app-debug.apk` to `/storage/emulated/0/Download/Atelier-Time-D100.apk`, update-install without uninstalling or clearing Classes, and check built-in/system/file sound sources and independent A/B 5/10s, C 5–60s audio selections. `stable/android-d098` remains frozen. [Detailed S01–S15 QA checklist](docs/SOUND_SOURCES_D100.md).

---

## 2026-10-24 — D-100 extended sound sources & playback durations (source only)

D-100 Android **source implemented; not built or accepted on phone**. User requested three simultaneously enable-able source categories in Sound: existing Atelier Time melodies (default), native platform sounds, and audio files. Each A/B/C dropdown offers currently enabled categories (one selected sound per event), with independent duration A=Session end 5/10s, B=Break end 5/10s, C=Class end slider 5–60s every second. Current native commits created pure domain `SoundSettings`, private preference migration keeping existing stored melodies, Android `RingtoneManager` picker, `OpenDocument` file picker with persisted URI read access, `MediaPlayer` external playback, duration-aware existing PCM `AudioTrack` clips, and unit/instrumentation test definitions. Source files were read back; simple brace/parenthesis balances clean, but **neither real Gradle compile nor JUnit executions nor phone APK/manual playback were performed for this iteration**. No claim of audio duration precision or OS lock/background reliability. **Stable 2 D-098 and its source snapshot were left unchanged; no redesign of other tabs, preset changes, signing or paid CI.** Next phone build/test/QA is documented in [SOUND_SOURCES_D100.md](SOUND_SOURCES_D100.md).

---

## 2026-10-24 — D-099 acceptance: Android Stable 2 / D-098 design freeze

2026-10-24 — User explicitly reports **"הכל עובד"** after installing D-098 and declares it the **second stable version** with design closed. The D-098 compact Editor chronological list (bodyMedium with 2dp phase-row spacing) is now **user-accepted in the installed app**, superseding previous APK install/QA pending notes. Screenshot already showed D-098 **BUILD SUCCESSFUL in 36s**, 42 Gradle tasks (7 executed / 35 up-to-date), :app:testDebugUnitTest and :app:assembleDebug, plus 12M APK copied to Downloads. Frozen Android **Stable 2** checkpoint created at [`stable/android-d098`](https://github.com/AviY99/Atelier-Time/tree/stable/android-d098), exact source `6d5cbab6818cae288de8367849f8d3ea53f4739f`. Stable 1 `stable/android-d086` unchanged; Android dev PR #3 stays Open/Draft/unmerged. Design contract now **closed by explicit user approval**, including approved Day/Night/Auto, five tabs, Timer display, About and Editor timeline; do not alter without new user direction. This is a debug APK user-accepted checkpoint, not signed production release; connected instrumentation, locked/background, accessibility/reboot/long-run/device stress tests are still separate open QA. No extra code/build, new app version number, release signing or CI was initiated. See [stable checkpoint dossier](docs/ANDROID_STABLE_2_D098.md).

---

## 2026-10-24 — D-098 36s Gradle build pass / debug APK copied; installation and UI QA pending

**D-098 PHONE BUILD VERIFIED (2026-10-24; user terminal screenshot):** From the existing `android-native` Code on the Go worktree, the combined Gradle command reached `:app:testDebugUnitTest`, `:app:packageDebug`, and `:app:assembleDebug`, ending **`BUILD SUCCESSFUL in 36s`**, **42 actionable tasks: 7 executed, 35 up-to-date**. The same terminal output verifies `ls -lh /storage/emulated/0/Download/Atelier-Time-D098.apk` returned a **12M APK**, dated **Oct 24 19:22**. This verifies successful JVM-test task + debug APK assembly and copying into Downloads, **NOT installation or user visual acceptance** of the new compact Editor timeline. The screenshot does not expose individual JUnit counts or the local Git SHA. **Next and only current D-098 gate:** open the already-copied APK in Downloads, update-install **without uninstalling or clearing saved Classes**, and visually inspect **Editor → Complete class timeline**: `bodyMedium` smaller activity/time rows, 2dp between rows, readable without overlap/clipping. Prior D-097 APK was user-confirmed installed and working. No repeat Gradle build needed. Preserve the previously approved app behavior, Draft PR #3 and frozen `stable/android-d086`; no paid GitHub Actions.

---

## 2026-10-24 — D-097 accepted on phone; D-098 compact Editor timeline source committed

- **D-097 accepted:** In the new conversation following its confirmed 45-second Gradle PASS and APK assembly, the user explicitly reports **"הכל עובד כנדרש"** for the updated installed D-097. This closes focused phone acceptance of Auto Design Day-left / Night-right full-palette preview and adjacent Timer time/length labels. Does NOT verify connected instrumentation or locked-screen/background reliability.
- **D-098 new small Editor-only request:** Make the chronological activities-and-times text list under `Complete class timeline` one typography step smaller and reduce spacing between consecutive rows. In `android-native/app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt`, wrap only `candidate.phases.forEach` in `Column(verticalArrangement = Arrangement.spacedBy(2.dp))` (rather than inheriting parent 12dp between rows); set row text `style = MaterialTheme.typography.bodyMedium` (was default `bodyLarge`). Existing phase values, order, time format, chart and surrounding Editor layout are preserved; timer domain, presets and theme untouched.
- **Git evidence:** Android branch `android-native-kotlin-compose`, [commit `6d5cbab`](https://github.com/AviY99/Atelier-Time/commit/6d5cbab6818cae288de8367849f8d3ea53f4739f), source readback confirms the 2dp local Column and bodyMedium. PR #3 remains Draft/unmerged; stable D-086 frozen.
- **D-098 validation NOT YET EXECUTED:** No new local phone Gradle JVM tests/assembly, no new APK install, no user acceptance. Next: phone `git pull --ff-only`, proven Gradle 9.6.1 `:app:testDebugUnitTest :app:assembleDebug` with ARM64 AAPT2 override, copy APK to Download as `Atelier-Time-D098.apk`, update-install **without uninstall or clearing saved Classes**, verify ONLY compact text timeline remains readable without clipping. Never mistake D-097 acceptance for D-098 acceptance.

---

## 2026-10-24 — D-097 phone Gradle PASS 45s; APK and visual acceptance pending

**D-097 PHONE GRADLE GATE PASSED (user screenshot, 2026-10-24):** Terminal in the existing Android `android-native` worktree shows `:app:compileDebugUnitTestKotlin`, `:app:testDebugUnitTest`, `:app:packageDebug`, `:app:assembleDebug`, followed by **BUILD SUCCESSFUL in 45s**, with **42 actionable tasks: 8 executed, 34 up-to-date**. This confirms Kotlin compilation, the JVM unit-test Gradle task, and debug APK generation for the D-097 build the user ran; screenshot itself does not expose a Git SHA or count individual executed tests. **No D-097 update-install, Auto slider Day-left/Night-right preview, or tightened timer label placement has yet been user-confirmed.** Source currently at GitHub Android draft branch HEAD `660492902406190492eb79e6dbfd047dbc2ece5a`, PR #3 OPEN/DRAFT/unmerged. Prior D-096 was explicitly user-accepted as working; Night palette user-approved. Next copy built `app/build/outputs/apk/debug/app-debug.apk` from existing `android-native` directory into Downloads as `Atelier-Time-D097.apk` and **update-install without uninstalling or clearing stored classes**. Visually check preview palette starts Day at slider left (hours 12–17) and flips Night on right (18–23), plus Start/End/Session or Break length labels immediately adjacent to values. The saved Auto transition hour and clock-driven behavior outside Design remain separate from preview; do not imply the Day/Night automatic scheduling itself changes at the slider midpoint. Stable `stable/android-d086` branch unchanged, no paid CI.

---

## 2026-10-24 — D-097 Auto slider visual preview and Timer clock label proximity

**D-097 2026-10-24 ACTIVE STATUS:** User explicitly reports **D-096 installed and working** (“הכל עובד”) and previously approved the current Night colors. D-096 phone screenshot shows Break with dynamic Break length and compact triplet, but user asks two remaining refinements. **Design Auto slider currently displaying the opposite palette:** user wants its **left half and start to show Day**, with the FULL Design-screen palette switching to **Night after the slider crosses its midpoint toward the right**, independent of actual current device hour. Android draft branch now has pure `DesignClock.sliderPreview(hour)` mapping hours `12..17 => Day`, `18..23 => Night`, two additional WRITTEN JVM test methods, and hoisted `autoPreviewHour` to theme the full Design tab accordingly while dragging. **Real clock scheduling is preserved**: outside Design, Auto still switches using persisted Night-start hour with Day from 06:00. Drag previews the hour and palette; the hour setting is persisted only on thumb release. Label reads `Currently showing: Day/Night`; smaller note explains outside-Design live time behavior. **Timer Start, End, Session length/Break length labels are now only 4dp from their displayed clock values**, no 100dp fixed value slot (done in native commit `3e628e459c1762d5c2725acde320542596845d5b`), keeping HH:mm:ss monospace and existing compact vertical triplet; user has not yet validated this specific layout after the newer source. Latest Android code commit `7adce6434ac5390ff7901784ee89443f19b178bd`, source read-back verified. **D-097 still UNBUILT/UNTESTED/NOT APK-INSTALLED on phone.** D-096 had real 46s Gradle PASS + on-phone user acceptance; don't call that D-097 PASS. All approved Day/Night palette colors, pure timer scheduling, A/B/C cues and saved Classes remain unchanged. Keep Android PR #3 Open/Draft/unmerged; immutable stable snapshot `stable/android-d086`; no paid GitHub Actions. Next from current `android-native` CoGo worktree: `git pull --ff-only`, proven Gradle 9.6.1 `:app:testDebugUnitTest :app:assembleDebug` with ARM AAPT2 override. On success, update-install WITHOUT uninstall/clear presets, verify automatic slider preview moves Day-left to Night-right, actual schedule outside Design still clock-driven, and labels immediately adjacent to aligned times.

---

## 2026-10-24 — D-096 on-phone Gradle gate passed; APK/UI acceptance next

**D-096 PHONE GRADLE BUILD + JVM TEST TASK PASSED — 2026-10-24:** User screenshot from existing Code on the Go Android `android-native` directory shows `:app:compileDebugUnitTestKotlin`, `:app:testDebugUnitTest`, `:app:packageDebug`, and `:app:assembleDebug`, followed by **`BUILD SUCCESSFUL in 46s`**. **42 actionable tasks: 8 executed, 34 up-to-date.** This establishes successful Kotlin/Android build, Gradle unit-test task and debug APK assembly for D-096 at Android branch head `91338c817519d20fc3d3c53aa1ce1f53fe35a89f`. Exact executed JUnit method count is not shown. **D-096 has not yet been confirmed update-installed or visually/behaviorally user accepted on phone.** Next copy `app/build/outputs/apk/debug/app-debug.apk` to `Download/Atelier-Time-D096.apk` and update-install without uninstall/clear data, then check Day sun/Night moon cards and palette swatches, Auto-only slider with Day sun left/Night moon right and hour readout, compact Class start/end/length and Session/Break start/end/length, unchanged approved Night palette and previous timing behavior. PR #3 remains open Draft/unmerged, stable `stable/android-d086` untouched; no paid GitHub Actions.

---

## 2026-10-24 — D-096 Day/Night visual picker, Auto graphic slider and phase length

**D-096 — SOURCE CODE IMPLEMENTED, REAL PHONE BUILD/UX QA OPEN (2026-10-24).** User explicitly approves current Night palette colors (from installed D-095) but finds current Design tab UX insufficient; user also requests timer's active Start/End/Length values grouped tightly and add **Session length** or **Break length** below End. Changed only Android Compose UI in `AtelierDesign.kt` and `SessionTimerScreen.kt`. Design: two separately clickable visual palette cards with custom drawn **Day sun** and **Night moon/stars**, theme name and preview swatches for approved palette colors; no slider on fixed Day/Night cards. A separate full-width **Auto** filter chip reveals the Night-start hour slider ONLY when selected, with sun graphic physically to its LEFT, moon graphic to its RIGHT, and live `Night begins at HH:00` text responsive to slider thumb, using the previously approved start range 12–23 and fixed return to Day 06:00. Mode persistence, already-approved Night/Day color values and real-time hour switching unchanged. Timer: for active Session/Break, append current active schedule phase **durationSeconds** as `Session length` or `Break length` row under End; shorten vertical spacing of Start/End/Length group inside phase card to 0dp, with `WallClockRow(compact=true)` 20dp min height and 18/20sp line heights while keeping original font size and fixed-width HH:mm:ss Start/End. Also compact Class start/end/length triplet below Clock in same way; leave summary without fake session/break length. Session/Break and Class progress bars still fill with elapsed; Session card background fills, Break background drains in tahini; no timer logic or saved class/audio changes. GitHub static readback and basic source brace/import checks passed; **NO new D-096 phone Gradle `testDebugUnitTest` or `assembleDebug`, installation or user acceptance yet.** Prior D-095 phone screenshot shows Gradle 41s PASS, Night palette is now user-approved, but does not validate D-096. Next: user CoGo `git pull --ff-only`, known Gradle9.6.1 JVM test+assemble with native AAPT2 override, update-install without uninstall/clearing private classes; inspect sun/moon choices, Auto slider symbols/time, period-specific label + lengths, compact vertical rows in both themes. Stable `stable/android-d086` remains frozen, Android PR #3 OPEN/DRAFT, no paid GitHub Actions.

---

## D-095 on-phone Gradle PASS — installation and visual QA pending

**D-095 user phone Gradle gate PASSED — 2026-10-24:** Screenshot in Code on the Go shows `:app:testDebugUnitTest` followed by `BUILD SUCCESSFUL in 41s`, with 42 actionable tasks (**8 executed, 34 up-to-date**) and successful `:app:assembleDebug` packaging. This confirms Kotlin compilation, completion of the JVM unit-test Gradle task and debug APK generation after D-095 slider/elapsed-bar source fixes. **Exact individual JUnit count not shown. D-095 APK update-install and visual/functional phone acceptance NOT YET VERIFIED.** Previously accepted five-tab order should not be retested. Next: copy generated APK to Downloads and update-install without uninstall or clearing saved Classes. Check Design Auto bar Day on left/Night on right, actual switch hour unchanged; Session/Break/Class progress bars fill with elapsed, while Session background fills and Break background drains; Night labels white/card contrasted, About equal-size identity top line and bold second-line credit. D-093 screenshot defects are not closed until accepted on device. Stable `stable/android-d086` preserved, PR #3 open/Draft/unmerged, no paid GitHub Actions.

---

## 2026-10-24 — D-095 Design Auto slider / elapsed progress correction

**D-095 / 2026-10-24 USER CLARIFICATION SUPERSEDES D-094 BAR INTERPRETATION:** Inverted coloration referred ONLY to the Auto Night-start slider in **Design tab**. Material3 default Slider colors the left active track, but that represents Day (before the selected Night transition); user wants the Night-colored segment **right** of selected hour. Updated only slider appearance: explicit Day neutral color on left/active, Night blue on right/inactive, with `Day · 12:00` and `Night · 23:00` captions; same 12–23 hour threshold, local clock, persistence and touch behavior. Separately ALL **Class, Session and Break progress bars track elapsed time**, from empty at beginning to full at end. D-094 had mistakenly made them show remaining; corrected using pure `DesignMotion.elapsedFraction` and updated the existing source JVM regression test. Session animated CARD BACKGROUND still FILLS, Break background still DRAINS, in solid tahini. Retain D-094 white Night root text, brighter Night Timer card and corrected About fonts/credit. GitHub source written; **NO D-095 new CoGo Gradle build / test run / APK update-install / phone QA yet.** D-093 screenshot supplied previous build PASS in 52s but UX issues, not acceptance for new source. D-094 new source was not built either. Test next one combined D-095 phone Gradle `:app:testDebugUnitTest :app:assembleDebug` via already-proven Gradle 9.6.1 Bash and ARM AAPT2 override, then update-install preserving private saved classes. Check Day/Night Auto slider Night zone to right, drag changes threshold without reversing time logic, Session+Break horizontal bars gradually FILL, Class bar FILL, Session card backdrop FILL, Break card backdrop DRAIN, Night headings white and card contrast, About top row same font and bold second line. Do not repeat previously accepted five-tab ordering or alter domain timer logic. PR #3 remains OPEN/DRAFT, stable/android-d086 remains untouched, no paid Actions.

---

## 2026-10-24 — D-094 Night root readability, remaining progress and About font alignment

**D-094 2026-10-24 USER PHONE SCREENSHOTS/ACTIVE HANDOFF:** User installed/ran D-093; real screenshots confirm tabs and blue Night, but D-093 is NOT visually accepted: black root Text outside cards (Editor heading, Timer Class clock), active Timer card too similar to Night canvas, progress bars showing elapsed rather than remaining and confusing trailing stop dot, About credit needs bold and the name / Version / ©2026 need identical font size on first line. Source repaired on current Android draft branch: root `Surface(color=background, contentColor=onBackground)` now supplies white LocalContentColor to non-card text in Night; active Night Timer only uses lighter #4D7194 card (white text contrast approx 5.12:1 on card) on canvas #304D70; both tiny Class and main phase bars now show remaining time, starting full and draining to zero, rendered using solid tahini custom bars without M3 stop dot. Background visualization still fills Session and drains Break, independently. Added pure `DesignMotion.remainingFraction()` and one authored JUnit source regression test; About first row name/Version/© all share exact same responsive font size 13sp (<300dp) or 15sp, semibold; second line `Developed by Avi Yosfan with OpenAI` remains centered one-line auto-fitted and NOW bold. Previous user-approved Day palette and timer engine/audio/private saved Classes unchanged. All D-094 code is in GitHub Android branch, NOT YET phone Gradle-built/JVM-tested/assembled/update-installed/device visually accepted. D-093 52s Gradle PASS pertains to old source only. PR #3 OPEN/DRAFT, snapshot stable/android-d086 unchanged. Next: from already-open Android CoGo directory, `git pull --ff-only` then known local Gradle `:app:testDebugUnitTest :app:assembleDebug` (Gradle 9.6.1 + Android ARM64 AAPT2 override), fix only actual errors if present; then update-install without clearing presets. Check both Editor/Timer Night root white labels, increased card contrast, remaining bars drain as expected at two timestamps, session backdrop fills & break backdrop drains, About uniform first row and bold second line. No need to redo already accepted tab ordering.

---

## 2026-10-24 — D-093 phone Gradle PASS; Night/About device UX acceptance pending

**D-093 PHONE GRADLE GATE PASS — 2026-10-24:** The user's actual Code on the Go terminal screenshot shows `:app:compileDebugUnitTestKotlin`, `:app:testDebugUnitTest`, `:app:packageDebug`, and `:app:assembleDebug` ending **BUILD SUCCESSFUL in 52s**, 42 actionable tasks (**8 executed, 34 up-to-date**). This verifies Android Kotlin compile, Gradle JVM unit-test task success, and debug APK packaging for latest D-093 source. The screenshot does NOT report an exact per-test JUnit method count and does NOT show D-093 APK update-install or Night/About UI acceptance. The previously accepted five visible left-to-right tabs do not need retesting. Next: copy existing `android-native/app/build/outputs/apk/debug/app-debug.apk` into Downloads as D-093, update-install without uninstall or clearing saved Classes, examine the softened blue Night buttons, white text, blue-toned material surfaces/dialogs, and one-line centered `Developed by Avi Yosfan with OpenAI` in About. Maintain D-092 as last installed user-reported base; D-086 frozen stable branch unaffected. PR #3 stays Open/Draft/unmerged; no paid GitHub Actions.

---

## 2026-10-24 — D-093 Night theme repair and About credit

**D-093 ACTIVE (2026-10-24):** User installed D-092 and explicitly confirms all five tabs in the approved LTR order work. User reports **Night still visually wrong**, partly near-black surfaces and dark button text; About second-line developer credit must be concise **“Developed by Avi Yosfan with OpenAI”**, without “ChatGPT” or parentheses, centered and single line. Root cause in source: D-092 only themed `background`, `surface`, `surfaceVariant`; Material3 ElevatedCard/dialog/menu tonal **surfaceContainerLow/High/etc** used uncustomized near-black defaults. D-093 Android draft branch now sets all major Night tonal surface roles to blue, softens filled primary buttons to `#4F729C` (white contrast 4.98:1) and secondary to `#716C99` (white contrast 4.88:1), and assigns white to text roles. Night-specific white foreground helpers applied to primary/outlined/tonal/text Buttons, all filter-chip choices, five tabs, text fields, error texts, and Timer help labels; D-092 Day light scheme was compared and verified **byte-for-byte unchanged**. Night background remains user-approved `#304D70`, cards `#3B5A7A`, variant `#4A6A8A`; tahini unchanged. Short About credit auto-fits width to one line; identity top row and true numeric version/©2026 remain intact. No timer domain, saved class, audio, Class clocks, or mode persistence logic changed. **D-093 source has NOT been Gradle compiled/JVM test task run/assembled, update-installed or phone visually accepted**; D-092 phone success is only for tabs, not Night/About. Need one new Code on the Go `git pull --ff-only && Gradle :app:testDebugUnitTest :app:assembleDebug` using known gradle9.6.1 AAPT2 override, fix actual build diagnostics if any, then update-install preserving saved app data, verify both theme palettes, Night all labels/controls white, non-black ElevatedCards, and About one-line credit. Stable branch `stable/android-d086` must remain immutable, PR #3 Draft and no paid CI.

---

## 2026-10-24 — D-092 phone Gradle PASS; updated APK manual acceptance pending

**D-092 PHONE GRADLE GATE PASS (2026-10-24):** User-supplied Code on the Go screenshot shows `:app:compileDebugUnitTestKotlin`, `:app:testDebugUnitTest`, `:app:packageDebug` and `:app:assembleDebug` followed by **BUILD SUCCESSFUL in 38s**, 42 actionable tasks (**8 executed, 34 up-to-date**). This verifies Android Kotlin compilation, successful JVM unit-test Gradle task and Debug APK assembly for the D-089–D-092 integrated changes (all five fixed LTR tabs, English About layout/text/real numeric version, approved Night Blue backgrounds). Individual JUnit case count is not displayed; device instrumentation is not shown. **APK update-install and UI/device functional acceptance are STILL PENDING**, as are locked/background audio gates; D-088 remains the most recently user-accepted installed revision and stable Android D-086 branch remains frozen. Do not rebuild now; next copy from existing `android-native/app/build/outputs/apk/debug/app-debug.apk` into Downloads, update-install without uninstalling or clearing stored Classes, verify About one-row name/Version/©2026, centered short credit + justified live-timer description, five tabs visible LTR, Night Blue backgrounds only with unchanged controls/tahini, and Day unchanged. PR #3 OPEN/DRAFT/unmerged, no paid CI.

---

## 2026-10-24 — D-092 Approved Night Blue backgrounds and About attribution

**D-092 ACTIVE USER APPROVAL — 2026-10-24:** Night Blue is NOW APPROVED and implemented in Android development branch source `AtelierDesign.kt`, commit `151aeeb51c3d754f48e0bddeed49786402a1396c`: change ONLY Night background `#304D70`, card/surface `#3B5A7A`, and secondary surfaceVariant `#4A6A8A`. Day, primary/secondary buttons/tab colors, text/outline/error, tahini and timer/domain/audio unchanged. User requested shorter About second line reflecting human-directed AI assistance; researched official OpenAI guidance (human credit + AI disclosure, do not imply OpenAI partnership): implemented **Developed by Avi Yosfan · with ChatGPT (OpenAI)**. In About first line remains name / Version actual numeric / © 2026 LTR; About heading and credit centered; truthful English live-timer paragraph justified on both sides. Fixed Kotlin raw Regex for numeric version that incorrectly used doubled backslashes in prior source. This updated D-089–D-092 code is GitHub readback verified but **has not yet been phone Gradle-built/JVM-tested/APK update-installed or user-device accepted**. D-088 is the last user-accepted installed functionality; `stable/android-d086` frozen at d0a03d2; PR #3 Draft/unmerged, no CI. Next run a single previously working local phone Gradle `:app:testDebugUnitTest :app:assembleDebug` after `git pull --ff-only` then update-install preserving saved data; visually check all five tabs, both palettes, About header/version/credit center and justified description.

---

## 2026-10-24 — D-091 About identity row; Night Blue proposal unimplemented

**D-091 · 2026-10-24 STATUS / SOURCE ONLY:** User asks About's first line to contain Atlier Time, numeric actual installed `Version`, and `© 2026` together, LTR and visible on narrow phone. Implemented in `AtelierDesign.kt` on Android development branch (commit `aa454a1e47d2cbbaaf73fde50c9c58c510cc0f52`, readback verified): weighted auto-size app name, fixed single-line Version and copyright, credit separately below (bodyMedium) and truthful live atelier timer English summary lower (bodySmall). No D-091 on-phone Gradle/build/install/screenshot acceptance yet. User requests *even softer, visibly more blue-toned* dark Night surfaces than older unapproved D-089/090 proposals: **Night Blue proposed only**, background `#304D70`, card `#3B5A7A`, secondary/variant surface `#4A6A8A`, **not changed in source until approved**. ALL existing button/tab/selected/accent/text/outline/tahini colors untouched; Day unchanged. See [D-091 design note](D091_ABOUT_HEADER_AND_NIGHT_BLUE_BACKGROUND_PROPOSAL.md). Existing D-088 is phone-accepted and stable D-086 source is frozen; D-089/D-090/D-091 code is still awaiting phone QA, PR #3 Draft/unmerged, no paid Actions.

---

## 2026-10-24 — D-090 corrected About description & typography (device QA pending)

- Explicitly positions Atelier Time as a LIVE atelier class timer rather than a planning tool, with larger numeric Version, a single auto-fitted English name/credit line and smaller copyright/description. Android code changed in `AtelierDesign.kt`; source commit `cd9514bc9`. No new build/device acceptance observed.
- Proposes lighter Night Mist **backgrounds only**, `#283746` (canvas), `#344759` (cards), `#405569` (secondary). Not approved or coded; all button/accent colors preserved. See [D-090 proposal](docs/D090_ABOUT_REFINEMENT_NIGHT_BACKGROUNDS_PROPOSAL.md).

## 2026-10-24 — D-089 full-width five tabs and English About (device QA pending)

**D-089 / 2026-10-24 ACTIVE STATUS:** User confirms installed **D-088 fully working** ("הכל עובד"), superseding earlier D-088 APK/UI pending statements; real prior phone Gradle `BUILD SUCCESSFUL in 38s` remains recorded. New native Kotlin UI code: fixed **non-scrollable, single-row 5-way LTR TabRow** in exact visual order **Editor | Timer | Sound | Design | About** (internal SOUNDS enum, display label Sound, compact 11sp, `LocalLayoutDirection.Ltr` even for Hebrew devices), replacing horizontally swiped `ScrollableTabRow`. **About now English-only**, displays exactly `Atlier Time`, **numeric only APK version derived from installed PackageManager metadata** (for `0.1.0-dev` display `0.1.0` without changing app build version), `© 2026`, `Developed by Avi Yosfan with assistance from Open AI`, and brief English class scheduling/timer summary. No timer, saved class, sound, or Day palette changes. User finds currently implemented Night too dark; **lighter Night Soft proposal is NOT APPROVED and NOT APPLIED**: background #1B2636, surface #29384B, raised #35465B, text #F2F5FA, secondary #D0D9E5, blue #8AB8F0, lavender #B9A8EA, border #60748C; tahini #D5BD94 as before. **D-089 source not yet phone Gradle compiled/JVM tested/assembled/installed/device QA-accepted.** When user approves Night Soft, implement in a separate subsequent increment; not part of current D-089 diff. Frozen stable/android-d086 stays exactly at d0a03d2; accepted D-088 remains current installed working phone baseline; PR #3 still OPEN/DRAFT/unmerged, no paid Actions.

---

## 2026-10-24 — D-088 38s Gradle PASS, device visual acceptance pending

D-088 ON-PHONE BUILD AND JVM TEST TASK PASS (user screenshot, 2026-10-24): Gradle `:app:testDebugUnitTest :app:assembleDebug` ended **BUILD SUCCESSFUL in 38s**, 42 actionable tasks (10 executed, 32 up-to-date). Screenshot visibly includes `:app:compileDebugUnitTestKotlin`, `:app:testDebugUnitTest`, `:app:packageDebug`, `:app:assembleDebug`. Thus Android Kotlin build, JVM test task, and debug APK assembly succeeded after the D-088 source changes. Exact individual test counts and Android instrumentation results are NOT shown. D-088 APK is **not yet confirmed update-installed or visually/functionally accepted**. Next gate: copy APK to Downloads, update-install (no uninstall/clear data), inspect Design Day/Night/Auto persistence and local-hour threshold, solid tahini Session fill and Break drain, no digit animation/flashing, About spelling/version/©2026/credit and short summary. Keep stable Android D-086 snapshot untouched. PR #3 remains OPEN/DRAFT, no CI Actions.

---

## 2026-10-24 — D-088 Day/Night/Auto and About integrated in Android source

**CURRENT 2026-10-24 D-088 — DESIGN SOURCE IMPLEMENTED; NEW PHONE BUILD/QA PENDING.** User approved Day (A Studio Linen), Night (B Night Focus), Flow-inspired rounded modern type; no font/digit motion or blink. Active Session's timer-card **solid tahini fill grows** and bar grows, Break's same-colored **fill drains** and bar drains, no gradient. New Design tab with Day/Night/Auto; Auto Night threshold chosen by hour slider 12–23, return to Day 06:00; offline SharedPreferences separate from saved Class catalog and live switching using device-local time. New About tab uses exact requested “Atlier Time”, installed version metadata (currently `0.1.0-dev`), © 2026, “פותח ע״י Avi Yosfan בסיוע Open AI” and short Hebrew functional description. Five scrollable tabs. Pure Kotlin new DesignClock/DesignMotion with **7 new written JVM test methods** (NOT executed). Feature code in `android-native-kotlin-compose`, user-approved stable branch `stable/android-d086` frozen unchanged at `d0a03d2`. **D-088 NOT yet Gradle built, tested, update-installed, UI verified**, no exact JUnit execution evidence. See [D-088 implementation & phone QA](D088_DESIGN_IMPLEMENTATION_AND_QA.md), [D-088 decision](DECISIONS.md), [stable baseline](ANDROID_STABLE_BASELINE_D086_2026_10_24.md). No paid Actions; PR #3 Draft/unmerged.

---

## 2026-10-24 — Stable Android D-086 accepted; D-087 design proposals pending selection

**CURRENT AUTHORITATIVE CHECKPOINT — 2026-10-24 / D-087:** User reported D-086 fully working on Android phone ("הכל עובד"), following screenshot-confirmed `:app:testDebugUnitTest :app:assembleDebug` BUILD SUCCESSFUL in 36s (42 tasks: 7 executed, 35 up-to-date). Declared **Atelier Time Android Stable 1 — D-086** (user-accepted installed functional baseline; not signed production or store release). Frozen source checkpoint branch `stable/android-d086` points to **`d0a03d2792cc324aeeba39b6327136ba6d37fd0c`**; current Android dev branch / open Draft PR #3 remain unmerged and should not be modified for accepted stable source. No CI/actions, release tag, binary publication or app `versionName` change. **Previously pending D-086 install/phone visual-acceptance statements below are historical and superseded by this newer direct user report.** Stable baseline docs: [ANDROID_STABLE_BASELINE_D086_2026_10_24.md](ANDROID_STABLE_BASELINE_D086_2026_10_24.md). Outstanding: 9 written Android instrumentation methods not executed on device; background/lock/doze audio reliability; lifecycle/time edge cases; accessibility, motion/contrast, performance/battery; signed release/distribution; future iOS/desktop multiplatform efforts; older D-015/D-016 wording only if change proposed. **Design exploration is authorized, but no visual concept has been approved or implemented:** three styles and animation plans in [VISUAL_DESIGN_CONCEPTS_2026_10_24.md](VISUAL_DESIGN_CONCEPTS_2026_10_24.md), with safe nonflashing/reduced-motion fallback. Keep accepted functions/domain/stored classes unchanged.

---

## 2026-10-24 — D-086 native Gradle gate passed, device UI gate open

- User phone terminal screenshot confirms **BUILD SUCCESSFUL in 36s**, 42 tasks (7 executed, 35 up-to-date), with `:app:testDebugUnitTest` and `:app:assembleDebug` completed. Exact test count not shown.
- Debug APK ready for update-install and visual check of two-line remainder choice plus Even and Custom, all inside the width of the phone. Existing D-085 installed app is user-accepted; no D-086 install/appearance acceptance yet. No CI or merge.

## 2026-10-24 — D-086 Editor ending-option chip fits phone width (device QA pending)

- User confirmed prior D-085 Android release works. In Editor → Complete class timeline, fixed the long "Last gets remaining time" choice by breaking its label into two centered lines and giving the three FilterChip options proportionate widths (1.5:0.8:1.1) within a fixed full-width row instead of horizontal scrolling.
- No change to selected ending option, scheduling, Save, test definitions, audio, signed storage or Timer operation. Code commit `d0a03d2` on draft Android branch; **new D-086 compilation/build/device layout acceptance still pending**.

## 2026-10-24 — D-085 live Class clock and Timer/list polish (new-source QA pending)

- User reports D-084 working on phone. Changed Class elapsed to real physical elapsed (no appended Class total); elapsed continues while paused/after Next, Class target end remains unchanged; Class remaining and progress use actual wall time. At deadline a Class still running/paused ends with one C cue.
- Increased only Class start/end/length label fonts 1.3×, numeric values unchanged. Stabilized Segment End with precise monotonic milliseconds and fixed-width monospaced seconds to prevent End card text bounce.
- Organized each saved Class card into two visible rows: Run/Edit; Duplicate/Rename/Delete, with distinct button styles, no horizontal scrolling.
- Added 4 JVM test source methods / updated earlier deadline expectations: **106 written JVM tests, 9 instrumented methods**. **D-085 new source not yet compiled, tested, APK-installed or device QA-accepted.** Retain saved data; PR #3 stays Draft; no paid CI.

## 2026-10-24 — D-084 segmented clock precision and tighter Class header (device QA pending)

- User feedback: restore `HH:mm:ss` for active Session/Break Start/End while preserving `HH:mm` for Clock and Class start/end, and put **Class length** below Class end as the third equally styled compact line under Clock.
- Android Compose source only: two timestamp formatters, separate immutable approved Class duration in HH:mm, three `WallClockRow` labels with 0dp row spacing. Existing Class remaining/elapsed/progress and phase countdown stay intact.
- New source commit `5f0df02c169ba4134cad2a6603d2df288ad96dcc`; **unbuilt, untested, not yet accepted on phone** for D-084. D-083 earlier phone Gradle PASS does not validate this delta. No app-data or timer-engine changes; Draft PR #3 retained, no paid Actions.

## 2026-10-24 — D-083 compact Timer controls and non-blocking Next (new-source QA pending)

- User confirmed D-082 updated Android app working on phone (“הכל עובד”); prior CoGo Gradle `BUILD SUCCESSFUL in 12s` with 42/42 UP-TO-DATE tasks.
- New Android branch source: replace long Exit label with distinct-color **Exit** in same four-button row as Pause/Restart/Next, with separate Exit help and confirmation. Display real wall-clock values in HH:mm without seconds; enlarge phase/Class start and end time values approximately 1.5×.
- Next can advance through the final pair without rewriting already-observed work, falls back to advancing and adjusting projected end when compensation cannot fit, and finishes/returns to saved Classes with melody C on the final regular work Session or summary. Other Next A/B cues, saved plans and user settings preserved.
- Added 5 net JVM test definitions (total **102 written**, 9 Android instrumentation methods). D-083 **not yet compiled, unit-tested, installed or manually QA-verified on phone**. PR #3 remains open Draft; no paid GitHub Actions.

## 2026-10-23 — K03 offline preset management in Android native source / HANDOFF v3.3

- User authorized the next stage; continued real Android Kotlin/Compose/Gradle code on `android-native-kotlin-compose` draft PR #3.
- Implemented pure Kotlin `PresetCatalog` / storage contract with approved-only save/update/load/duplicate/rename/delete/activate selection and timeline regeneration/full-revision consistency verification.
- Implemented versioned offline private Android `SharedPreferences` JSON collection persistence, no silent data loss on corrupt/unknown schema, isolated test storage; integrated full Compose saved-sessions UI with delete confirmation. Fixed whole-minute overflow input.
- Added 10 Kotlin preset unit tests + 1 Kotlin input-overflow test + 4 Android isolated instrumentation cases; now **32 JVM + 4 Android instrumentation test methods WRITTEN, NONE EXECUTED**. No APK, phone QA, Gradle unit tests, GitHub Actions or paid runners.
- K03 **CODED BUT UNVERIFIED**; K04 monotonic timer and three distinct audible cues remain next. Updated D-070, docs/roadmap/test plan, native README and self-contained HANDOFF v3.3; product requirements unchanged.


## 2026-10-23 — First native Android Kotlin app increment / HANDOFF v3.2 (D-068/D-069)

- User approved implementing Android-first Kotlin/Jetpack Compose/Gradle and reconfirmed all existing Atelier Time **functional and scheduling rules remain unchanged**; old D-062 release/toolchain pivot only.
- New isolated [android-native-kotlin-compose](https://github.com/AviY99/Atelier-Time/tree/android-native-kotlin-compose) branch and draft [PR #3](https://github.com/AviY99/Atelier-Time/pull/3). Real `android-native/` Gradle Android source: Compose session editor, fully editable opening/break/rhythm rules, timeline preview, 3 ending options, 25/5 advisory warnings and explicit in-memory approval. Pure Kotlin deterministic engine includes D-043 DP replacement.
- **16 Kotlin scheduler + 5 editor input JUnit methods written, none run. No Android APK/Flutter run or device validation claimed.** Previous screenshot-confirmed Dart 35+11 tests preserved as parity fixture oracles. Later offline presets and timer/3 sounds remain unimplemented.
- Added [requirements freeze + Android CoGo development/QA contract](docs/ANDROID_NATIVE_EXECUTION_AND_TEST.md) with K01–K06 gates; updated decisions D-068/D-069, roadmap, architecture, product, Stage 2, test plan, work log and handoff v3.2.
- No GitHub Actions/paid builds, PR merge or destruction of old branches/user files.


## 2026-10-23 — Android industry-standard decision, no toy spikes / Handoff v3.1

- At user's demand for a firm industry-standard choice rather than new experiments, selected **Kotlin + Jetpack Compose + Gradle** for Android-first app built on phone with **Code on the Go** (specialized mobile IDE; Android Studio is standard desktop IDE). Future Kotlin Multiplatform/Compose Multiplatform sharing for iOS/Windows/macOS (D-067).
- Sourced official Google May 2026 Compose First, JetBrains Multiplatform stability and migration docs, maintained Code on the Go 26.43 and Reddit developer/Termux community discussions; archived AndroidIDE not recommended.
- Existing duplicate/conflicting D-064 GitHub rows reconciled as D-064/D-065/D-066 history plus **D-067 authoritative**; older unexecuted Dart-to-JS/Capacitor spike remains historical only. **No disposable prototype builds required**; next work is real Atelier Time native Android implementation and ordinary QA.
- Preserved Dart scheduling 35 tests PASS, form adapter 11 tests PASS, unmerged historical PRs. No Kotlin APK yet, no GitHub Actions or paid build. Updated docs and HANDOFF v3.1.


## 2026-10-23 — Phone-first Dart reuse proof and Kotlin alternative reconciled / Handoff v3.0 (D-064)

- User-approved requirement D-062 now prioritizes fast, efficient Android development/build/install ON PHONE, with iOS/Windows/macOS ports efficiently later; D-061 strict universal build host condition superseded.
- Evaluated supported on-phone Kotlin / Code on the Go candidate (D-063) versus preserving Dart core via Dart-to-JS and community Termux Capacitor APK pipeline (D-064). **Neither Android APK route has passed on user's phone**. Kotlin would require porting verified Dart scheduler; Dart web may reuse it but native Android alerts remain a major unknown.
- Committed small UNEXECUTED browser feasibility project on isolated [spike-android-dart-web](https://github.com/AviY99/Atelier-Time/tree/spike-android-dart-web), checking canonical Example B using unchanged Dart core, plus pure Dart local server. No installable APK or migrated production UI.
- Deduplicated same-number D-062 entries in the decision register, recorded D-064, updated Android-first plans, docs and self-contained handoff v3.0. No GitHub Actions costs, migrations, or PR merges.


## 2026-10-23 — Android PHONE-first requirement and unexecuted Dart-web feasibility spike / Handoff v2.9

- User relaxed D-061 universal all-host native-build requirement: **fast efficient Android development directly on the phone FIRST**, with later efficient iOS/Windows/macOS reuse (D-062). Offline scheduling/presets/background-alert product features remain required.
- Recommended **bounded Dart web → Capacitor Android APK** candidate, not finalized: officially supported Dart JS compilation can potentially reuse already-verified 35/35 scheduler and 11/11 adapter; community Termux Capacitor build guide exists but is unverified on user's device.
- Created isolated [spike-android-dart-web](https://github.com/AviY99/Atelier-Time/tree/spike-android-dart-web) with nested Dart web package, canonical Example B browser schedule, plain Dart localhost server and non-destructive Termux instructions. **NOT COMPILED/RUN; NO APK BUILT**.
- New [Android-first feasibility plan](docs/ANDROID_FIRST_PLATFORM_SPIKE.md), updated platform assessment, historic TD-001, product specification, architecture, roadmap, test plan, Stage 2 plan, decision register and self-contained handoff v2.9.
- Preserved old branches/PRs and all confirmed test evidence; no GitHub Actions, paid services, UI migration or native audio/timer implementation.


## 2026-10-23 — Android-first, develop/build on the phone; portability later / Handoff v2.9

- User changed primary delivery requirement: **develop, debug, test, build and install Android directly on Android phone efficiently**, design for efficient future iOS/Windows/macOS port, no simultaneous four-platform first release obligation (D-062).
- Research verified maintained Android phone IDE [Code on the Go](https://appdevforall.org/code-on-the-go/) v26.43 (Oct 20, 2026) supports Kotlin/Java Android application builds on device; JetBrains Kotlin/Compose Multiplatform is a prospective future port path, NOT verified in this toolchain.
- Preferred candidate Kotlin Android + CoGo (D-063) awaits one successfully installed minimal on-device APK. No Kotlin project, UI or APK build is claimed. No broad migration until proven; Kotlin requires porting original verified Dart logic.
- New [Android-first plan](docs/ANDROID_FIRST_DEVELOPMENT_PLAN.md) and updated decision register, tech decision, product spec, architecture, roadmap, Stage 2 plan, platform reassessment and self-contained HANDOFF v2.9.
- Preserved prior **35/35 Dart scheduler**, **11/11 Dart adapter** passes and both unmerged PRs; no paid CI/Actions, auto-merge, data deletion or Stage 3+ work.


## 2026-10-23 — Full-lifecycle platform reassessment / Handoff v2.8 (D-061)

- At the user's demand, re-evaluated Flutter against **cross-platform development, tests, builds, signing, installation, and live operation**, not only app runtime.
- Official Flutter supports the four deployment targets but **not Android Termux as an official Flutter development host**; Apple iOS builds/signing require macOS/Xcode and desktop targets need their toolchains.
- Documented [PLATFORM_REASSESSMENT_2026_10_23.md](docs/PLATFORM_REASSESSMENT_2026_10_23.md), including F01–F08 hard criteria, Flutter/PWA/web+native wrappers/Expo/cloud-host comparison, PWA timer/background/installer risks and a required pre-code feasibility proof.
- **Original Flutter platform suitability reopened (D-061); further Stage 2 Flutter-specific implementation PAUSED**, draft PRs remain unmerged. No unilateral replacement, migration, new APK, GitHub Actions or paid builders.
- Prior 35/35 pure Dart scheduling and 11/11 pure Dart adapter successes preserved. Updated architecture, product, TD-001, roadmap, Stage 2 plan, decisions, README and self-contained HANDOFF v2.8.


## 2026-10-23 — Stage 2 Dart/Flutter analyzer isolation / Handoff v2.7

- Investigated user screenshot showing massive `dart analyze` failures on Android Termux Stage 2. Distinct nested Flutter package imports require Flutter SDK; bare Dart cannot resolve them. Chained `dart test` did not start after analyzer failure.
- Added root analyzer exclusion `apps/atelier_time_flutter/**` and separate nested Flutter analysis options on Stage 2 branch. **Does not suppress later Flutter SDK testing** and makes no changes to schedule/adapter behavior.
- **Post-fix analyzer and combined 46 Dart tests NOT YET RUN**; prior separately verified 35 Stage 1 and 11 Stage 2 passes unaffected. Eight Flutter widget tests still UNRUN, no APK.
- Recorded D-060 and updated test plan, roadmap, Stage 2 plan, README, branch README, work log and handoff v2.7. No GitHub Actions/paid runner, budget change, merge or Stage 3+.


## 2026-10-23 — Clean Stage 2 worktree verified 11/11 / Handoff v2.6

- New screenshots show `~/Atelier-Time-Stage2` checkout HEAD `3bb2bad` and `dart test test/editor_draft_test.dart` concluding **`+11: All tests passed!`**.
- GitHub commit comparison establishes `3bb2bad` is exactly Stage 2 branch tip `3bb2bade992fddab7dc3d7ece31d9aa58231380f`; D-057/D-058 checkout provenance problem resolved using separate worktree, original staged files untouched.
- Stage 1 35 verified core tests remain historical separate evidence. Full Stage 2 analyzer/root suite and eight Flutter widget tests still outstanding. No GitHub Actions/paid CI, APK, branch merges or later-stage programming.
- Recorded decision D-059, test plan, roadmap, Stage 2 plan and complete handoff v2.6.


## 2026-10-23 — Termux branch/index diagnosis clarified / Handoff v2.5

- User's read-only Git screenshot proves checkout remains `stage-1-4-scheduling-core` at `907f871`, despite successful fetch and failed Stage 2 switch. Stage 2 files including the 11-test adapter are **staged additions** in the old Stage 1 working tree; `pubspec.lock` remains untracked.
- Earlier screenshot of `+11 All tests passed!` is real execution from a **mixed Stage 1 HEAD plus staged Stage 2 files**, not a verified clean Stage 2 commit.
- Recommended non-destructive repair: separate `~/Atelier-Time-Stage2` Git worktree based on fetched Stage 2 commit, leaving all original staged/untracked user files untouched; confirm HEAD and rerun 11 tests there. No code changes or forced cleanup.
- Logged D-058, updated QA plan, work log and self-contained handoff v2.5. No Flutter widget tests, paid Actions, merges, APK or later-stage programming.


## 2026-10-23 — Termux editor-draft tests pass; branch switch discrepancy / Handoff v2.4

- Screenshot from user's Android Termux shows **`00:00 +11: All tests passed!`** after `dart pub get && dart test test/editor_draft_test.dart` (D-057).
- The immediately preceding fetch of Stage 2 branch succeeded, but **`git switch --track origin/stage-2-session-editor` failed** (`starting point ... is not a branch`). Exact local branch/HEAD and tracked status of test file therefore remain unconfirmed; do not falsely state the checkout switched or attribute the test run to a particular commit.
- GitHub confirms `test/editor_draft_test.dart` exists on Stage 2 branch but not the Stage 1 branch. Next safe step is to inspect local Git status/branch/HEAD and repair the checkout if needed, then rerun targeted tests once on identified Stage 2 commit.
- No code changes, paid Actions, PR merges, APK, Stage 3 or Stage 4. Previous Stage 1 35/35 remains verified; eight Flutter widget tests are still unexecuted.


## 2026-10-23 — Stage 2 Flutter-independent editor-input tests / Handoff v2.3

- Continued Stage 2 on isolated `stage-2-session-editor` branch after user asked to continue.
- Extracted Flutter UI text parsing into pure Dart `lib/src/editor_draft.dart`, exported from `lib/atelier_time.dart`. Preserves user-entered minutes, ordered opening groups, cycle and special placement; the scheduling engine still validates positivity and warnings. Ignores inactive manual ordinal text when special-break count is zero.
- Added **11 unexecuted plain Dart test definitions** for editor input conversion (`test/editor_draft_test.dart`) runnable with Android Termux Dart, without Flutter.
- **New adapter/tests NOT compiled or run** in the assistant runtime. Previously verified Stage 1 core 35 tests PASS; eight Flutter widget test cases still NOT run. No paid GitHub Actions, builds, merge, persisted preset or timer development.
- Updated project decision D-056, Stage 2 plan, roadmap, test plan, work log and handoff v2.3.


## 2026-10-23 — Stage 2 UI safety and widget-test source / Handoff v2.2

- Continued authorized Stage 2 `stage-2-session-editor` work; added stable widget identifiers for inputs, approval, endings, warning controls.
- Blocked malformed inputs and suppressed misleading partial timelines for invalid candidates. Updated mobile scroll layout while preserving two-pane wide display.
- Added **eight Flutter widget-test definitions** and `flutter_test` dev dependency, covering stage T19–T23 requirements and canonical Example B preview. Corrected short-rest warning fixture to a valid zero-summary session.
- **FLUTTER TESTS NOT EXECUTED**: no accessible Flutter SDK. Previously verified Stage 1 Dart core remains 35/35 PASS on Android Termux; it was not changed.
- Recorded D-055 and updated roadmap/QA/Stage 2 plan and handoff v2.2. PR #1 / draft PR #2 remain unmerged; no GitHub Actions, paid builds, APK, persisted presets, live timer or later-stage work.



## 2026-10-23 — Stage 1 integration proposed; Stage 2 editor prototype begun / Handoff v2.1

- User explicitly asked to **finish documentation and continue** after verified Stage 1 Step 1.4 completion (D-052). Recorded continuation decision D-053 and source/checkpoint D-054.
- Opened [verified Dart core PR #1](https://github.com/AviY99/Atelier-Time/pull/1), **not merged**. Created isolated [Stage 2 editor branch](https://github.com/AviY99/Atelier-Time/tree/stage-2-session-editor), nested Flutter prototype source under `apps/atelier_time_flutter/` consuming existing core via path dependency, and [draft PR #2](https://github.com/AviY99/Atelier-Time/pull/2).
- Added [Stage 2 input/preview and QA plan](docs/STAGE_2_PLAN.md) with all user-approved fields, full phase preview, three ending choices, and 25/5 warning acknowledgment; provisional neutral styling only. Stage 3 presets and Stage 4 timer/audio remain unstarted.
- Stage 1 Dart evidence remains **35/35 user-screenshot-confirmed passing**. Stage 2 Flutter source only underwent noncompiler structural check; **NO Flutter SDK, `flutter analyze`, Flutter unit/widget tests, APK or app build**. No GitHub Actions/paid workflow, budget change, branch merge or Stage 3+ coding.
- Updated DECISIONS, ROADMAP, TEST_PLAN, PRODUCT_SPEC, README, WORK_LOG, CHANGELOG and self-contained HANDOFF v2.1.



## 2026-10-23 — Stage 1 Step 1.4 pure Dart core verified / Handoff v2.0 (D-052)

- User screenshot confirms branch update `8cf7a24..907f871` and **`00:00 +35: All tests passed!`** under Android Termux Dart 3.13.7. The 35th T09 1m+34m regression passed along with all prior tests.
- Earlier screenshot proved `dart analyze` **No issues found!** for the prior 34-test checkout, plus prior successful smoke check. The new 35th test-only revision was **not separately re-analyzed**.
- **Stage 1 Step 1.4 scheduling core/required test gate VERIFIED and COMPLETE ON ISOLATED BRANCH.** Wider T13/T14 edge combinations remain a later QA backlog; no installed app or cross-platform build evidence.
- **No merge to main; no Stage 2 authorization. No GitHub Actions, paid runners, user budget modification, Flutter screens, Android APK or deployment.** Handoff v2.0 and decision D-052 preserve the exact stop point.



## 2026-10-23 — Stage 1 Step 1.4 T09 edge regression added / Handoff v1.9

- Screenshot-confirmed Android `dart analyze`: **No issues found!** and `dart test`: **34 tests all passed** (D-050). Separate smoke check passed.
- Required Stage 1 T09 custom final-pose endpoint audit identified untested 1m+34m (the complementary 34m+1m and zero-duration rejection were covered). Added a **35th** regression test including exact phase times and 25/5 warning acknowledgement on the same isolated code branch, commit `4b2f5d9fdd8dabc6dea8dcc0204c5062c7b6916a`.
- **35th test NOT EXECUTED YET**. User's free Android Termux command: `git pull --ff-only && dart test`. Retain 34-test screenshot PASS, no new success claim.
- Decision D-051, test plan, roadmap, README, branch progress, WORK_LOG and HANDOFF v1.9 updated. Stage 1 Step 1.4 still in final QA; no main merge, Stage 2 authorization, GitHub Actions, paid runners, builds or budget modification.


## 2026-10-23 — Screenshot-verified 34 Dart tests / Handoff v1.8

- Received Termux screenshots proving `dart pub get` succeeded, `dart analyze` reports **No issues found!**, and `dart test` ends **00:01 +34: All tests passed!** on Android Dart 3.13.7. The earlier smoke check also passed.
- Elevated verification to **screenshot-confirmed results D-050**, superseding user-report-only status D-049. This is not a multi-platform Flutter application build.
- Acceptance audit of the 34 tests identifies one explicit Stage 1 T09 custom split endpoint not yet tested (1m+34m); 34m+1m and 0m rejection already covered. No new tests or code committed in this log iteration, so original 34-pass evidence remains valid.
- **Step 1.4 remains in final QA review**, branch unmerged, Stage 2 not authorized. No GitHub Actions, paid runners, budget changes, UI, or APK.



## 2026-10-23 — Full Android Dart unit suite user-reported passing / Handoff v1.7

- Following user-confirmed Termux smoke test PASS (D-048), user reports **“All tests passed!”** after requested `dart pub get && dart analyze && dart test` on Android Dart 3.13.7 (D-049). Written suite currently defines **34 tests**. No full analyzer/test transcript or local commit ID supplied; preserve as **USER-REPORTED PASS**, not independently reproduced.
- Step 1.4 scheduling implementation has real positive test feedback. Remains in acceptance/coverage review; do not merge as final or start Stage 2 without appropriate checkpoints.
- No GitHub Actions, paid runners, builds, Flutter UI, APK or budget modifications. Updated test plan, roadmap, work log and self-contained HANDOFF v1.7.


## 2026-10-23 — Android Dart smoke pass / Handoff v1.6

- User confirms **“Scheduling smoke checks succeeded.”** on Android Termux Dart 3.13.7 following Step 1.4 warning-offset integer fix. This is a **user-confirmed successful Dart smoke run**, including compilation and the smoke assertions.
- Previous D-047 `num`/`int?` compiler error is no longer observed in that smoke run.
- Next checkpoint: `dart pub get && dart analyze && dart test`. **Full analyzer and unit-test results are still pending; do not call Stage 1 Step 1.4 complete.**
- D-048 and v1.6 handoff recorded; no Actions, paid runners, builds, branch merge, or Stage 2 authorization.


## 2026-10-23 — Stage 1 Step 1.4 first real Android compiler feedback / Handoff v1.5

- User successfully ran Dart SDK **3.13.7** in Android Termux and downloaded the development branch. The first smoke invocation **FAILED compilation** with `num` supplied to `int?` at `offsetSeconds: crossingOffset` in `lib/src/scheduling.dart`.
- Fixed the integer typing issue with conditional integer arithmetic; committed to `stage-1-4-scheduling-core` as `0dedd95756b6d3984b2e16548a58599179fbf7ff`.
- **Updated code NOT YET retested**. Next step for Termux: `git pull --ff-only && dart tool/smoke.dart`, followed (if successful) by Dart analyzer and test suite.
- D-047 and handoff v1.5 updated. No GitHub Actions, paid runner, merge, Stage 2 code or unverified success claim.



## 2026-10-22 — Stage 1 Step 1.4 verification preparation / Handoff v1.4

- Diagnosed missing Dart SDK and DNS resolution failures in assistant's container. This blocked **actual Dart test execution**, not source-code drafting.
- On isolated development branch, restricted `ScheduleCandidate` construction to engine/library code and retained full timestamped rest-warning acknowledgment records in immutable approved sessions; added unexecuted regression assertions.
- Added local no-GitHub-Actions helpers `tool/verify.ps1` and `tool/verify.sh` and `.gitignore` for test logs/caches. Minimum install for this pure-Dart step is **Dart SDK only** on a computer capable of execution.
- Recorded D-046, updated roadmap, README, test plan, work log and handoff v1.4. **No Dart compiler/analyzer/test run, build, paid Actions usage, merge or Stage 2 work. Step 1.4 remains IN PROGRESS**.

---


## 2026-10-22 — Stage 1 Step 1.4 D-044 support / Handoff v1.3

- User **approved D-044**, resolving former D-038: any **positive** special break can replace a normal break even if shorter/equal (examples regular 5m, special 1m/3m/5m), with the D-031 25/5 warning and acknowledgement when necessary. D-036 still blocks all 0-minute sessions/actual breaks.
- Updated Dart scheduling core on isolated `stage-1-4-scheduling-core` branch to accept negative, zero and positive `L−B` without `pendingDecision`.
- Added written regression and dependency-free smoke test source for shorter/equal special breaks and valid warning-override flows.
- Independent Python-based placement comparisons matched **25,000 virtual-schedule random cases** plus **4,000 additional exhaustive real-timeline cases**. This is **not Dart test execution**.
- **No Dart SDK/network access for download; no Dart compiler, `dart analyze`, `dart test`, smoke script execution, GitHub Actions, paid runners, build or merge**. Reported GitHub budget remains unchanged; Stage 1 Step 1.4 still **IN PROGRESS** and no Stage 2 authorization.
- Updated decision/policy/test docs, roadmap, README, work log, and self-contained HANDOFF v1.3.


## 2026-10-22 — Stage 1 Step 1.4 DP fix / Handoff v1.2

- Continued authorized pure Dart coding on `stage-1-4-scheduling-core` after user requested “Continue” and explanation of open D-038.
- Explained **D-038**: whether positive special break may be shorter or equal to positive regular break (e.g. special 3m versus regular 5m). Recommendation to allow with 25/5 warnings is **not a decision**. Code preserves `pendingDecision`.
- Fixed exponential automatic-break selection and a false-invalid-placement case using exact dynamic programming with original midpoint/even placement objective and deterministic earlier-ordinal ties.
- Added regression sources for a formerly false-invalid 97m case and an 8-hour five-special-break case. An **independent Python model** matched exhaustive search over 25,000 sampled configurations and canonical A/B positions, but **Dart tests have NOT run**.
- Source remains on isolated branch; no Dart/Flutter SDK, no GitHub Actions, budget change, compile, merge, UI or app build. **Step 1.4 remains IN PROGRESS; Stage 2 is not authorized.**


## 2026-10-22 — Stage 1 Step 1.4 first source increment / Handoff v1.1

- User's **“Continue”** after the blocked GitHub budget discussion authorized **Step 1.4 only** (D-040). Created isolated development branch [stage-1-4-scheduling-core](https://github.com/AviY99/Atelier-Time/tree/stage-1-4-scheduling-core).
- Committed pure Dart scheduling engine, standalone package manifest/export, canonical 3h A/B and 25/5 regression test code, dependency-free smoke script, and branch progress documentation.
- D-036 positive session/break rule retained; D-031 25/5 warning retained; D-038 positive-special-versus-regular-break comparison explicitly returns **pendingDecision**, not falsely accepted/rejected.
- **GitHub Actions not used or configured** due reported $12/month budget block (D-041).
- **Dart and Flutter SDKs unavailable** in current environment; **NO compilation, Dart analyzer, tests or app builds run**. Simple bracket/string/comment structural check only. Engine's multi-break search still needs performance verification.
- Source is on **development branch only**, not merged. Stage 1 Step 1.4 **IN PROGRESS / NOT COMPLETE**; no Stage 2 authorization. Updated root docs and handoff to v1.1.


## 2026-10-22 — Stage 1 Step 1.3 APPROVED / Handoff v1.0

- **Explicit approval:** User approved all remaining documented scheduling proposals **P-01–P-06** subject to **D-036**, and specifically prohibited beginning Stage 1 Step 1.4.
- Registered **D-037** Step 1.3 completion, **D-038** the one explicitly unresolved positive special-break-versus-normal-break comparison, and **D-039** Step 1.4 stop gate.
- Confirmed automatic closest-to-work-midpoint break placement (tie earlier), evenly spaced multiple special replacements, manual ordinals, validation, custom ending options, full preview and warnings with user acknowledgement.
- Approved zero *count* of special replacements and zero final summary for otherwise valid positive sessions. **Zero total session or actual break remains a hard error; no zero-length break slots.**
- Set 25/5 warning counter to **one full activated session** (no automatic cross-preset carryover); an overall 25m ending needs no invented break, while over-25m posing warns.
- Updated current docs, TEST_PLAN accepted future tests, README, roadmap, WORK_LOG and self-contained HANDOFF v1.0.
- **Documentation-only commits/read-back. No source code, automated test execution, build, UI, or installation. Step 1.4 NOT AUTHORIZED.**


## 2026-10-22 — Stage 1 Step 1.3 clarification / Handoff v0.9
- **Latest approved D-036:** A 0-minute full session or configured normal/active special break is invalid; previous D-031 permission for zero-minute breaks superseded. Positive whole-minute break minimum is 1 minute.
- **Retained D-031:** Accumulated 25m of posing calls for >=5m rest before continuing; valid shorter breaks and long poses trigger a warning requiring explicit user acknowledgement rather than automatic rejection.
- Withdrawn: the proposed zero-minute logical break-slot model from the earlier Step 1.3 review. Updated current policy, review, data model, product specification, tests, roadmap, README and handoff. Historical changelog entries remain for traceability, not current rules.
- Other Step 1.3 technical policies still require user approval. **No executable code or tests; Step 1.4 remains on hold.**


## 2026-10-22 — Stage 1 Step 1.3 consolidated review / Handoff v0.8
- After the user's “Continue,” consolidated remaining scheduling decisions into [STEP_1_3_REVIEW.md](docs/STEP_1_3_REVIEW.md).
- Proposed logical 0-minute regular/special break slots without elapsed zero-length phases, plus manual replacement ordinals, warning approval after final-pose changes, and boundary/cross-preset interpretation.
- Linked and refined Step 1.3 policy and pending tests; added decision-log entries D-034/D-035, revised roadmap, README, work log and handoff.
- **Approved 25/5 warning with 0-minute break override D-031 remains unchanged; all new details remain pending user sign-off. No code, automated tests or build.**


## 2026-10-22 — Stage 1 Step 1.3 safety clarification / Handoff v0.7
- **Approved D-031:** A 5-minute break is recommended after 25 minutes of accumulated posing without an adequate break. Accumulate consecutive pose phases; a rest under 5 minutes does not reset the counter.
- **Permitted:** A user can choose a 0-minute break, and can approve a schedule violating the 25/5 recommendation **after an explicit warning acknowledgement**; the app must not silently adjust the entered durations.
- **Superseded:** Prior hard minimum of 1 minute attributed to D-030; break defaults stay 5m regular / 15m special, both editable.
- Updated policy, product specification, data model, schedule rules, decision register and QA tests; documented pending cross-session and zero-length special replacement slot semantics.
- Updated progress roadmap, work log and handoff to v0.7. Other Step 1.3 proposals are not yet approved.
- **No executable code/tests, Flutter setup or app builds; Step 1.4 remains on hold.**


## 2026-10-22 — Stage 1 Step 1.3 / Handoff v0.6
- User authorized discussion/documentation of scheduling placement and edge policies by saying “Continue” after Step 1.2.
- Added [SCHEDULING_EDGE_POLICY.md](docs/SCHEDULING_EDGE_POLICY.md): six **proposed** rules covering midpoint long-break replacement, multiple evenly-spaced long breaks, manual ordinals, invalid configuration handling and final-pose edge cases.
- Preserved both approved example schedules exactly; these rule proposals are **not yet user-approved**.
- Updated D-026 through D-029, roadmap, scheduling contract, work log, README and current cross-chat handoff.
- **No Dart/Flutter source code, test executions or installable builds; Step 1.4 remains blocked.**


## 2026-10-22 — Stage 1 Step 1.2 / Handoff v0.5
- User's “Continue” authorized only the **data-model design** following approved Step 1.1.
- Added [DATA_MODEL.md](docs/DATA_MODEL.md) with proposed Dart session rules, variable-length editable opening pose groups, phase intervals, long-break replacement representation, validation issues, final-pose option model, and immutable approved preset shape.
- Included future test fixture input payloads for approved Example A and B; canonical expected schedules unchanged.
- Recorded decisions/gates D-023 to D-025; updated roadmap, architecture, README, work log and handoff.
- **No code or tests; Step 1.3 and engine implementation remain on hold.**


## 2026-10-22 — Stage 1 Step 1.1 / Handoff v0.4
- **Approved:** Flutter + Dart as one shared application codebase for Android phones/tablets, Windows, iOS/iPadOS and macOS in first release.
- **Clarified:** Native menus/graphics/widgets are optional; non-native Flutter UI is acceptable. Distinct OS-specific installation packages remain necessary.
- **Reaffirmed:** Every numeric rule from the examples is configurable through a per-session editor, including optional pose groups.
- Added approved [TD-001](docs/TECH_DECISION_001_PLATFORM.md) and updated product, architecture, decisions, roadmap, work log, README and handoff.
- **STOP:** Step 1.2 not approved. No code, Flutter scaffold, automated tests or builds created.


## 2026-10-22 — Documentation baseline 0.3
- Located the existing `AviY99/Atelier-Time` GitHub repository and initialized project documentation.
- Consolidated drawing/sculpting product specification, configurable numeric rules, review/approval, offline presets, live timer and three cues.
- Canonicalized both complete 180-minute examples and the three incomplete-final-pose alternatives with exact times.
- Added scheduling invariants, planned QA tests, six planned implementation stages following documentation Stage 0, decision register and reusable handoff.
- Verified all 11 new documentation files can be read back from GitHub.
- **No implemented app functionality or automated test results yet.** Stage 1 requires separate approval.

## Handoff history
- v0.1: Initial concept and example sequences drafted in chat.
- v0.2: Three-choice end-pose handling specified, time-ruler prototype shown in chat.
- v0.3: User approved requirements and authorized GitHub documentation setup.
- v0.4: User approved Flutter/Dart four-platform choice and non-native UI clarification for Stage 1 Step 1.1 only.
- v0.5: Step 1.2 documented the proposed data model.
- v0.6: Step 1.3 placement and edge-policy proposals prepared for review.
- v0.7: D-031 25/5 warning with historical zero-minute allowance, **later superseded by D-036**.
- v0.8: Initial review including now-withdrawn zero-minute logical break slots.
- v0.9: D-036 forbids 0-minute full session and configured breaks.
- v1.0: Stage 1 Step 1.3 approved as D-037; strict stop before Step 1.4. Current source of truth: docs/HANDOFF.md.
