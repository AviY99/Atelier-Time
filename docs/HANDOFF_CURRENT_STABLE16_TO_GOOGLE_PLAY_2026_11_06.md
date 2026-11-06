# CURRENT AUTHORITATIVE HANDOFF — Atelier Time Stable16 / D153 / Code47 → Google Play

**Updated:** 2026-11-06. This is now the current handoff; Stable15 and all older handoffs remain historical snapshots. 

## 1. Latest accepted stable and immutable recovery

**STABLE 16 / D153 / versionCode 47** was explicitly user-accepted: **"הכל עובד , זוהי גרסה יציבה חדשה הכוללת  עיצוב שפה עיצובית של כפתורים ומתגים ושיפורי ממשק תפעול של קולות"**.

Frozen branch: [`stable/android-d153-qa16-ui-sound-user-accepted`](https://github.com/AviY99/Atelier-Time/tree/stable/android-d153-qa16-ui-sound-user-accepted). **Exact immutable Stable16 HEAD: `04270365f330af78861d54f3710048d65dadc462`**, verified through the GitHub branch API. The branch is frozen and must not be edited. App `com.ateliertime.android`, versionName D-153, versionCode 47, API36. Previous **Stable15/D147/code41** branch `stable/android-d147-qa15-ui-install-accepted` stays frozen, SHA `c10f424c0ec000eca2dd561911934841decb4522`.

**D153 local Samsung evidence:** 300/300 full executed JVM tests passed with 0 errors/failures/skips, AndroidTest import preflight PASS, art audit PASS, approved splash SHA256 verified inside APK, debug APK build successful. AndroidTest APK **compiled only; not run on a device**. User independently reports everything works and declares Stable16. Local APK: `/storage/emulated/0/Download/Atelier-Time-D153-API36-Candidate.apk`; the private manifest/build log remain in the same Downloads folder. No paid CI, user data wipe, signed AAB, Play upload or public release.

**Accepted classification:** app-wide button + ON/OFF toggle geometry, responsive compact Class/Free timer toolbar (`Keep screen awake` followed closely by identical ON/OFF; plain `Volume` label; separate Max and Mute buttons), global Sounds selection of Media/Alarm and associated alert control improvements, one selected hardware volume slider, safe Max confirmation, zero-volume Mute without changing global saved Sound ON/OFF, Alarm forcing Sound ON/locking app OFF, persistent sound settings, Sounds Help. Run & Free use the same visual controls; day/night style preserved. Prior timer and special break behavior unchanged.

## 2. Project organization / safe continuation

- DEV code and local build/test helper: `android-native-kotlin-compose`. Future modifications must start from the stable16 accepted source, using DEV only; **do not edit Stable16 or older stable branches**.
- Top-level project docs, version index, privacy, Google Play artwork on `main`. Documentation updates are allowed on main, not edits to immutable stable source snapshots.
- Current acceptance certificate: [Stable16 acceptance](STABLE16_D153_UNIFIED_BUTTONS_AND_SOUND_UI_USER_ACCEPTANCE_2026_11_06.md). D153 code context: [D153 specification](D153_UNIFIED_ON_OFF_AND_VOLUME_ACTIONS_2026_11_06.md).
- Prior acceptance records remain historical. Avoid repeating old pending-build instructions as current gates.

## 3. Remaining Google Play path

**User choices already fixed:** **FREE** app, first release via small **invite-only Internal Testing** group. Do not re-ask or change to paid/public.

Before any upload, verify Google Play Console developer verification/eligibility and readiness of its actual listing/availability. Privacy-policy URL: `https://aviy99.github.io/Atelier-Time/privacy-policy.html` and in-app About link. Approved store art ZIP remains versioned in `main/artwork/play-store/`; never regenerate accepted media without authorization. Reconfirm latest release requirements, Data Safety disclosure from actual source, and foreground service/special-use justification for persistent background timer notification. Validate package, SDK, permissions, declared sound behavior and privacy. Do not infer that D153 user acceptance implies Play Console approval.

A debug APK is not uploadable as a signed Play AAB. When authorized, prepare a locally built **signed release AAB** for versionCode47 or a later approved version using secure local signing credentials that never go to GitHub; inspect resulting bundle's metadata and assets; only then submit to invite-only Internal Testing if authorized. Record SHA, evidence and release work in GitHub docs; leave stable branch untouched. **No build or Play action automatically initiated by documenting Stable16.**

## 4. Operational constraints

Never use paid GitHub Actions/cloud build without explicit approval; the user prefers Samsung Code on the Go local build. Never install/uninstall, delete stored app data or change paid/free distribution without user authorization. Do not claim Android instrumentation tests were executed when only compiled. The stable release is a user acceptance checkpoint, not proof of policy acceptance or published app.

**Next:** plan Play release readiness against frozen Stable16 and obtain separate explicit user approval for any signed release artifact, Console upload or publishing step.
