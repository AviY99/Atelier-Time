# Atelier Time — D-100 expanded sound sources and duration controls (2026-10-24)

**STATUS: Android source implemented on `android-native-kotlin-compose`, GitHub readback/static structure checked; NO phone Gradle build, install, audible runtime test, or user approval yet.** This is a new functional increment AFTER user-accepted design-frozen Android Stable 2 / D-098. Do NOT move or rewrite `stable/android-d098` or `stable/android-d086`.

## Approved user requirement

Three selectable audio source categories are offered on the existing **Sound** tab:
1. **Atelier Time sounds** — original built-in six independent melodies; only this source is enabled by default on clean and migrated installs.
2. **OS system sounds** — source-native OS audio choices (on Android use the system `RingtoneManager.ACTION_RINGTONE_PICKER`, covering notification/ringtone/alarm sources).
3. **Audio file** — user picks an audio file from a document library using the platform's standard document picker (Android `OpenDocument(audio/*)` with persisted read URI grant).

Categories are **multi-select**. Every enabled category appears as choices under each timer cue A/B/C. The selected *single* sound for each cue is played; enabling all sources does not play all sounds simultaneously. The three cues remain independently configurable; preview and automatic timer cues use the same selected choice and duration.

- **A / end of Session:** select either 5 or 10 seconds.
- **B / end of Break:** select either 5 or 10 seconds.
- **C / end of Class:** Slider 5–60 seconds inclusive, integer 1-second increments, live feedback while dragging; save chosen value on release.
- Short audio repeats for the chosen duration. The built-in 5s PCM notes repeat; external audio uses looping MediaPlayer stopped after the requested interval.
- Keep the existing supported terminology, timing logic, fixed Class end, melody IDs, private saved Class data and entire previously approved visual theme/layout unchanged.

## Implementation evidence

- Pure Kotlin `domain/SoundSettings.kt`: `SoundSource` APP/SYSTEM/FILE, per-cue `CueSound`, `SoundSettings` with independent durations and source selection validation.
- `android/MelodySettingsSection.kt`: source checkboxes; menus populated from enabled sources, original built-in melody list, native OS/File picker actions and previously selected per-cue external sources; A/B 5s or 10s chips, C 5–60s slider.
- `android/SessionEditorScreen.kt`: Android system ringtone and document-picker launchers, file display name lookup and persisted URI read permission, one private-settings update path, timed cues and preview dispatch through the same source/duration settings. UI continues within existing Sound tab; no changes to approved Editor/Timer/Design/About visual logic.
- `android/MelodyAudio.kt`: backward-compatible legacy melody preference keys plus new sound source/URI/title/duration keys; playback through existing static PCM `AudioTrack` for built-ins, and URI `MediaPlayer` for Android system/document sounds. User-readable errors on missing/inaccessible files.
- Pure JVM test definitions: `SoundSettingsTest.kt` (defaults, mixed sources, each allowed duration, invalid selections); Android instrumentation test additions in `MelodyPreferencesTest.kt` (legacy migrations and persisted multi-source configuration).
- Only source/readback inspection performed here. JVM and instrumentation tests are **WRITTEN, NOT EXECUTED** for D-100. No claim of Android audible duration precision or background/lock-screen reliability.
- Platform strategy: Preserve pure Kotlin selection model. On future iOS/Windows/macOS targets, implement OS-specific sound and document pickers, permissions, playback engines and test them separately. **Those ports are not implemented in D-100.**

## Concrete D-100 phone acceptance gates (not yet executed)

| ID | Requirement and evidence needed |
|---|---|
| S01 | Phone Gradle 9.6.1 `:app:testDebugUnitTest :app:assembleDebug` with existing ARM64 AAPT2 override succeeds for the newly pulled code; note commit SHA, console and JUnit report counts |
| S02 | Update-install Android debug APK without uninstall/data clearing; all previously saved Classes still visible |
| S03 | Clean/default settings show only Atelier Time sounds, matching existing A/B/C selections and 5/5/5s durations |
| S04 | Enable System; native ringtone chooser opens, returns an audible system sound and assigns it to cue A without changing B/C |
| S05 | Enable File; document library filters audio; chosen audio plays and remains accessible after app restart |
| S06 | Enable all three; each A/B/C menu contains the app melodies and actions to choose system or file; previously selected sounds can be reused |
| S07 | Preview for A/B plays each selection for 5 and 10 seconds; repeat/truncation working for short files and built-in 5s tune |
| S08 | C slider supports 5, 6, 30, 59 and 60 seconds; preview respects each selected duration, label updates while dragging |
| S09 | Real timed Session/Break/Class boundaries dispatch selected A/B/C sound exactly once, with selected duration and no timer/phase-clock changes |
| S10 | Preferences survive app close, reopen and APK update; switching source does not overwrite Class store or erase saved preferences |
| S11 | Removing access to a user file or an unavailable OS sound produces actionable UI error, no silent wrong cue |
| S12 | Unable to deselect the only library or a currently used library without first assigning supported choices; no invalid saved config |
| S13 | User selects and previews different built-in/system/file sounds across all A/B/C; changing settings during a running Class affects subsequent cues only |
| S14 | Existing core Timer, auto Day/Night and compact Editor visuals remain unchanged; stable Android D-098 snapshot remains immutable |
| S15 | Separate background/lockscreen/Doze cue-reliability and connected instrumentation gates remain open even if foreground audition passes |

## Next device step

From the phone's existing `android-native` Code on the Go directory (no second `cd`):

```bash
git pull --ff-only &&
set -o pipefail &&
bash "$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle" :app:testDebugUnitTest :app:assembleDebug --no-daemon --console=plain "-Pandroid.aapt2FromMavenOverride=$ANDROID_HOME/build-tools/36.0.0/aapt2" 2>&1 | tee "$HOME/atelier-d100-build.log"
```

**Only on SUCCESS**, copy `app/build/outputs/apk/debug/app-debug.apk` to Downloads as `Atelier-Time-D100.apk`, update-install without uninstall/clearing data, then run the focused sound-source/duration QA. Do not present this document or source changes as an already tested new APK.

**References:** Official Android RingtoneManager ACTION_RINGTONE_PICKER and Android OpenDocument/MediaPlayer APIs. The other platforms will need native adapters, not a claim of free cross-OS audio catalogs.
