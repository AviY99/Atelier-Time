# Atelier Time — Stable 17 / D-157 / Code 51 — User acceptance and recovery checkpoint

**Date:** 2026-11-07 (Asia/Jerusalem).  
**Status:** **STABLE 17 — explicitly user-accepted, frozen on GitHub.**  
**Application:** `com.ateliertime.android` · Android Kotlin/Compose · `versionName = "D-157"` · `versionCode = 51` · target/compile SDK 36.

## Authoritative user acceptance

The user reported in the new Atelier Time project conversation after installing the D157 candidate APK: **"כעת בדקתי אחרי התקנת apk והכל תקין ... והיא גרסה יציבה חדשה מס 17"** ("I have now checked after installing the APK and everything is working ... it is a new stable version number 17"). This is **user-reported real-device installation and functionality acceptance**. It is not a claim of assistant-run device instrumentation.

**Important version-number correction:** D-151 is **Code 45**, per the existing D151 source/spec; the last built and accepted candidate is **D-157 / Code 51**, per current Android Gradle and D157 build report. The previous official Stable16 was **D-153 / Code47** — not D157.

## Exact frozen source checkpoint

- New immutable recovery branch: [`stable/android-d157-qa17-user-accepted`](https://github.com/AviY99/Atelier-Time/tree/stable/android-d157-qa17-user-accepted).
- Independently verified DEV head selected for freeze: **`f6866dca809f6214c6d1f88c1adf8f6592163ada`**, from `android-native-kotlin-compose` (D157/code51). Preserve this branch unchanged.
- Existing Stable16 branch: `stable/android-d153-qa16-ui-sound-user-accepted` at `04270365f330af78861d54f3710048d65dadc462`, unchanged.
- The D157 candidate debug APK was assembled locally on the user's Samsung before the build helper's *post-success* duplicate shell tail was removed on DEV. The later fix removed only the helper's malformed duplicate after the success line; app code/Gradle version were not modified by that cleanup. The locally generated private manifest contains a `source_commit` but that value was not supplied in the current conversation; do **not** claim exact APK-to-frozen-SHA binary provenance or a GitHub-hosted release artifact.
- `main` retains top-level documents and Play assets; DEV remains the place for future app changes. Do not edit any frozen Stable branch.

## Actual QA / APK evidence

Existing Samsung terminal screenshot, recorded in the D157 implementation document and 2026-11-07 GitHub handoff:
- **Full JVM tests: 318 executed, 0 errors, 0 failures, 0 skipped.**
- Debug app APK built; AndroidTest test APK/source **compiled**, but device instrumentation tests **were not executed**.
- `APK ARTWORK INCLUDED PASS`: approved `assets/atelier_splash_approved_d139.png` SHA256 matched **inside** the debug APK.
- Local candidate: `/storage/emulated/0/Download/Atelier-Time-D157-API36-Candidate.apk`; local manifest: `Atelier-Time-D157-API36-Manifest.txt`. APK/manifest/build log remain phone-local, **not GitHub attachments**.
- The script subsequently showed `line 480: syntax error near unexpected token 'then'` **after** a successful Gradle/test/APK/artwork/manifest sequence; duplicate trailing code was removed and source re-read on GitHub. That error is not a JVM/Gradle/APK failure.
- **New acceptance on 2026-11-07:** user states installed APK now runs correctly. Specific test-by-test physical observations beyond that blanket confirmation were not supplied; do not invent them.

## What Stable17 contains

D154–D157 refinements on top of Stable16: bidirectional selected Media/Alarm hardware-volume and Sound synchronization; Android-enforced stream minimum respected (positive Alarm minimum must not be presented as zero); Class/Free Mute targets actual allowed minimum, Max takes effect immediately and is not restored after timer exit; one shared compact volume row with left two-line Media/Alarm + Volume labels, custom **12dp** slider track (with 56dp touch area), and **24sp** percent on the right; shared single-line centered actual-zero warning; independent upper sound-choice/duration Help and lower Media/Alarm output/minimum Help. Core timer planning, class/sound persistence, approved artwork and Play assets remain unchanged by the Stable17 promotion.

## Release boundary / next tasks

The stability designation is a **recovery and user-acceptance checkpoint**, **not** a Google Play release. Commercial choice stays **FREE**; initial distribution **invite-only Internal Testing**. No cloud/paid GitHub Android build, uninstall/clear data, locally signed release AAB, Play Console submission, or public release has been initiated by this documentation. Next, when separately authorized: confirm Console eligibility, Privacy/Data Safety, foreground-service specialUse documentation, approved store assets, build/sign/inspect a release AAB locally for the approved Code51 (or a later explicitly accepted version), then invite testers.

**Canonical continuation:** [Stable17 handoff](docs/HANDOFF_CURRENT_STABLE17_TO_GOOGLE_PLAY_2026_11_07.md).  
**D157 QA and implementation:** [D157 details](D157_COMPACT_VOLUME_ROW_UNIFIED_WARNING_AND_HELP_2026_11_07.md).  
**Predecessor:** [Stable16 acceptance](STABLE16_D153_UNIFIED_BUTTONS_AND_SOUND_UI_USER_ACCEPTANCE_2026_11_06.md).
