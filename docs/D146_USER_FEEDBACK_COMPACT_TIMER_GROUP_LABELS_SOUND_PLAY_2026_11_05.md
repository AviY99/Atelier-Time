> **2026-11-05 — D146 APPROVED ARTWORK AUTO-RECOVERY AND FINAL APK VERIFICATION (source committed, build pending):** User rightly objected to manual restore from a phone directory; approved image already lives in GitHub under `android-native/app/src/main/assets/atelier_splash_approved_d139.png` (verified Git blob `8950a9c509bbc2c2bac02d18c0b9bfdfca06eabc`, 2,903,002 bytes, approved SHA256 `c7d69a52c53a86738cc416b4c29eb621f8792cf367539732ce0ffa3639d8460d`). CoGo screenshot showed `git pull --ff-only` FAST-FORWARD succeeded, but working-tree tracked PNG was **deleted**, causing the first D146 local QA gate `STOP: local source changed` **before compiling**; untracked `../.cg/` was unrelated. **FIXED ON ANDROID DEV ONLY:** `android-native/tools/build_d146_compact_field_ui_on_phone.sh` now detects the missing tracked PNG and restores **ONLY THAT FILE** from the *already pulled Git HEAD*, compares its Git blob SHA, and then applies all existing source/art audit; it refuses to overwrite any modified file. After assembling the local debug APK it reads the actual ZIP entry `assets/atelier_splash_approved_d139.png` and verifies full SHA256 matches the approved source, printing `APK ARTWORK INCLUDED PASS` only on success. `audit_artwork_on_phone.sh` misleading historical Git push message corrected; GitHub presence already established. This is normal offline Android SDK packaging from the Git checkout; **NO unrelated local art file, file-picker/upload, paid CI, automatic install, data clearing, or Play publish**. **NEW NEXT COMMAND (once, from Samsung CoGo):** `cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native && git pull --ff-only && bash tools/build_d146_compact_field_ui_on_phone.sh`. **Do not ask user to manually `git restore`, download art, or upload it again.** D146/code40 not yet built or accepted; Stable14/D145/code39 still frozen/accepted, unchanged.

# Atelier Time — D-146 / code40 — User field feedback, compact Timer and Sound controls

**Status: DEVELOPMENT SOURCE COMMITTED / PHONE BUILD AND ACTUAL QA PENDING.**  
**Dated:** 2026-11-05.  
**Code only on:** `android-native-kotlin-compose`.  
**Last frozen user-accepted:** **Stable 14 / D-145 / code39**, branch `stable/android-d145-qa14-field-accepted` at `73bf60996234f739243b3974ac1d02c38ad065ba`. Frozen branch untouched.

## User-requested D-146 changes

1. **Timer: controls immediately under clock card.** Remove the redundant heading `Class controls` and the extra surrounding controls card. Group the timer face and its five action buttons with only 4dp separation. Position the single `Help` control **below** the action row, aligned to the end. Pause/Resume, Restart, Repeat Last, Next, Exit stay a single row, preserve dialogs/25-5 warnings, action behavior, accessibility names, >=48dp action hit height and horizontal-scroll narrow-device fallback. Move incidental run error text below the action controls, so it does not break adjacency.
2. **Running Timer top bar: compact Screen awake + Max both.** During a running or paused Class, combine `Screen on` switch (full spoken label `Keep screen awake during timers; uses more battery`), `M xx% · A yy%` with the chosen Media/Alarm stream, and unchanged `Max both` confirmation into **one compact row**. Keep old full explanatory toggle in Timer home before a Class starts. Preserve the explicit opt-in and automatic return-of-volume safety behavior, no automatic system-volume setting.
3. **Opening Group names for both current and upcoming phase.** The existing single `PhaseDisplayNames.title` formatter now renders `Group 1 · Session 1/4`, `Group 1 · Session 2/4` etc., not just `Session 1 · 1/4`. Opening-group breaks become `Group 1 · Break` / `Group 2 · Break`. Other ordinary Session numbering, saved phase IDs/numbers, special breaks and summary are unchanged. This formatter feeds both the Timer's CURRENT headline and `Next:` upcoming label, along with confirmation prompts. Unknown legacy group IDs retain a safe `Break · opening group` fallback.
4. **Sound picker buttons:** In `MelodySettingsSection.SoundPicker`, replace long `Play 5s/10s` text buttons with one **48dp outlined triangular Play icon** directly next to the current sound-picker button, still in a single row. Full spoken label includes cue and preview seconds. No changes to sound selection, stored settings, A/B/C/Free time lengths or audio playback logic.

## Source changes and test additions

- `android-native/app/src/main/java/com/ateliertime/android/SessionTimerScreen.kt` — paired timer face/actions and labels; added stable Compose testTags.
- `android-native/app/src/main/java/com/ateliertime/android/SessionEditorScreen.kt` — compact running-class controls and accessibility semantics.
- `android-native/app/src/main/java/com/ateliertime/domain/PhaseDisplayNames.kt` — explicit Group numbers in opening session and group rest labels.
- `android-native/app/src/main/java/com/ateliertime/android/MelodySettingsSection.kt` — triangular icon, picker+preview testTags and full TalkBack descriptions.
- `android-native/app/src/test/java/com/ateliertime/domain/D144PhaseDisplayNamesTest.kt` — four legacy tests adapted to D146 display expectations, with additional group break checks.
- **New** `android-native/app/src/test/java/com/ateliertime/domain/D146GroupTimerLabelsTest.kt` — **6 JVM test methods**, including multi-group counters, regular-session numbers, legacy-safe break, summary.
- **New** `android-native/app/src/androidTest/java/com/ateliertime/android/D146CompactUserFeedbackUiTest.kt` — **2 Android UI tests** for actual frame/controls/help geometry and sound Play icon placement+semantics. **Compile only in the local build, not executed automatically**; no user data touched.
- Android version `versionCode 40`, `versionName "D-146"`, no changes to applicationId, SDK levels or runtime business algorithms.
- New Samsung local-only build script `android-native/tools/build_d146_compact_field_ui_on_phone.sh`. It verifies branch/clean checkout, assets, all **full JVM suite**, gate for new 6/6 plus previous targeted suites, builds debug APK and **compiles (does not execute)** AndroidTest, creates sha256 manifest. No install, uninstall, data clear, paid GitHub Actions/cloud build, release signing or Play upload.

## One local QA command (pending user execution)

```bash
cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native && git pull --ff-only && bash tools/build_d146_compact_field_ui_on_phone.sh
```

Expected if passing: `/storage/emulated/0/Download/Atelier-Time-D146-API36-Candidate.apk`; private build log `Atelier-Time-D146-API36-Build.log` and manifest in Download. **Do not claim any D146 test count/build/phone UI PASS until screenshot.** D145 historical full suite is 277/277; D146 adds 6 separate unit tests, so the aggregate could be 283 if all pass, but that count is not a reported execution result.

After the user supplies a successful local build screenshot, separately authorize in-place install retaining Saved Classes, Sounds and report history. Verify visual placement in portrait, both Day/Night, screen on/off, sound assignment shared/separate, volume Max both confirmation, Group names for current/Next, and icon Play/cue duration. Only then consider user acceptance and freezing the next stable checkpoint.

## Google Play continuity

**Commercial decision remains FREE; initial distribution remains invite-only Internal Testing**, not Production or initial Closed Testing. No Console app creation, store listing, AAB, upload, Play review or release was performed by this D146 change. Do not continue packaging a superseded binary without confirming whether user prefers D146 or frozen Stable14 for the first signed Play AAB. **Frozen Stable14 stays untouched**, source for D146 only on DEV; update documentation only on `main`/DEV.
