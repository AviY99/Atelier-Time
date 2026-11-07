# Atelier Time — D157 / Code51 — compact one-row volume control, shared zero warning, separate Media/Alarm Help

> **FINAL SOURCE PREFLIGHT UPDATE 2026-11-07:** Reopened current GitHub D157/code51 rather than repeating previously committed work. Fixed a genuine AndroidTest compile issue in `D157UnifiedVolumeRowAndHelpUiTest.kt`: Compose's `DpRect.width` was compared with `0f`; now correctly compares against `0.dp`. Added a dedicated Class warning UI test, complementing the existing Free warning test, and strengthened the Samsung local build script to refuse reintroduction of these mistakes. Verified source-level contract checks for 12dp shared track/no end-stop dot, left two-line stream+Volume labels, right enlarged percentage, both 56dp hit areas, two independent Helps, same one-line warning, versionCode51, immediate Max/no restoration, approved-art-in-APK check. **Static checks PASS; Android/Kotlin/Gradle build and actual device display NOT yet run on D157.** D156 user's supplied phone log independently confirms **318/318 JVM tests, no failures**, and working D156 functional behavior, but this must not be misreported as D157's test result.


**Date:** 2026-11-07. **DEV branch:** `android-native-kotlin-compose`. **D157 status:** source committed; static inspections completed; **NO Samsung Gradle build or device verification yet**. Do not declare stable before explicit user acceptance.

**Latest frozen user-accepted stable:** Stable16 / D153 / Code47, unchanged branch `stable/android-d153-qa16-ui-sound-user-accepted`, SHA `04270365f330af78861d54f3710048d65dadc462`.

## User's actual D156 result and D157 request

User supplied screenshot of completed D156 Samsung local build: **318 JVM tests, 0 errors, 0 failures, 0 skipped, exact approved splash SHA256 packaged in APK, local debug APK built**. User's screenshots of D156 installed on Samsung: Sound/volume behavior is working, including actual Alarm device minimum 6% and Free countdown; however both volume bars are visually oversized. User additionally requested that the lower Sounds Help contain only Media and Alarm output explanation; one identical centered one-line zero warning below the slider in both Timers; one horizontal row: two lines `Media/Alarm` + `Volume` at left, the bar immediately after it at three quarters its previous visual thickness, percent at right about twice former text size. In Free Timer at 6% Alarm, the Material slider displayed a confusing nearly-zero thumb and a tiny end-stop dot at the far right even though the numeric percent was correct; fix must preserve real hardware levels and easy drag at zero.

## Source changes

- **One shared layout** in `RunningClassQuickControls.kt` for Programmed/Class Run and Free Timer. Its volume area is a single 56dp hit-height horizontal Row: fixed 52dp two-line `Media|Alarm` and `Volume` label on LEFT; weighted Material Slider; right-aligned `headlineSmall` (24sp, exactly 2× the Material default 12sp label-medium) percent on RIGHT. Both screens pass actual selected stream percent and selected-volume-zero flag into the SAME Composable.
- `AtelierSlimSlider.kt` new optional `volumeVisual=true` branch for timer volume ONLY. Uses the Android/Compose Material3 Slider interaction engine (drag, touch, accessibility) with a **custom single thumb and custom 12dp track**, 75% of the previous approximate 16dp Material track, with correctly normalized filled track. Removes Material default large track-to-thumb gap and misleading right-edge stop dot. No repaint based on a different value from the slider state. All Sound-duration sliders retain original visual via the default `volumeVisual=false` branch. 56dp touch area retained in both Timers and easy-zero interaction from D156 retained.
- `TimerVolumeZeroWarning` now renders identical short centered, one-line message directly **under** the shared horizontal bar for either running Timer: `Sound volume is zero. Timer alert may be silent.`. For Class before running, same one-line helper appears near Awake even when the volume bar is absent. D156's actual-level warning logic is unchanged: warning only at selected hardware stream index zero; protected positive Alarm min does not show zero warning.
- `MelodySettingsSection.kt`: upper Help `sound_choices_help` explains sound libraries, shared/separate cue selection, length. Lower Help `sound_output_help` now has its own state/dialog titled `Media and Alarm` and **ONLY** explains the selected Android Media vs Alarm channel, device-dependent usual Alarm positive minimum, Mute/OFF to device permitted min, Sound ON restores previous positive or 25%, and no other-stream changes. No duplicate full generic Help.
- Earlier AndroidTest UI sources D147–D151 updated to assert separate label lines rather than old combined literal `Media Volume`/`Alarm Volume`, so previous assumptions no longer cause false test failures.
- New `D157UnifiedVolumeRowAndHelpUiTest.kt` (AndroidTest **compile-only until explicitly run on device**) checks both timer modes at 6%, label/sliding bar/large percent spatial order, >=56dp hit target, unified warning, and that upper and lower Helps show non-overlapping explanatory content.
- Gradle candidate `versionName = "D-157"`, `versionCode = 51`. **No changes to audio volume algorithm** from D156: Mute/slider zero honor Android minimum, Sound ON/OFF sync actual output, Max immediate without confirmation, never restore volume after timer exit, persisted positive volume/25% fallback.
- Local-only guarded Samsung build helper: `android-native/tools/build_d157_compact_volume_row_and_help_on_phone.sh`. Checks exact code51 and required sources, old D156 minimum/Max/no-restore guards, 12dp custom track/timer-only style, both warning callbacks, distinct Helps and new UI test source, runs all JVM tests, compiles AndroidTest APK without executing device UI tests, assembles debug APK and verifies original artwork SHA256 from inside final APK. No paid Actions, cloud signing, Play upload, uninstall or data wipe.

## One local Samsung Code on the Go build command

```bash
cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native && git pull --ff-only && bash tools/build_d157_compact_volume_row_and_help_on_phone.sh
```

Upon **actual build PASS**, candidate debug APK: `/storage/emulated/0/Download/Atelier-Time-D157-API36-Candidate.apk`. Update over D156 without uninstall or clearing app data.

## Acceptance checks

1. On Programmed Timer and Free Timer, select **Alarm** and verify exact Android min (e.g., 6% on reported phone) displays with thumb+active track near the LEFT, **no independent dot at the RIGHT**, not a false high-volume indication. Percent is big at right; two-line Alarm/Volume left, same single row, track visually 3/4 of prior thickness.
2. Switch to **Media**, repeat at 0, small positive and Max in both Timers. Slider is easy to grab at far left; same visual layout/logic in both.
3. At selected stream hardware 0, both Timer warnings are **identical**, centered, one line directly below bar; warning is hidden at positive protected Alarm minimum.
4. Lower Sounds Help explains **ONLY Media/Alarm channel and Android minimum**; upper Sounds Help explains only sound libraries, choice modes and cue durations; no duplicate content.
5. Preserve D156 accepted behavior: Mute to Android allowed minimum, Sound ON/OFF linked to actual volume, Sound ON restores last positive/else 25%, Max immediate and stays Max across Exit, no voice cue/timer logic regression, protected Alarm min acts according to device, saved Classes/sounds survive.

**Status integrity:** D156 phone screenshot proves local 318/318 JVM run and D156 APK; it does NOT prove D157 built or app tested. D157 requires its own Samsung build log and visual user approval. No promotion to Stable17 without explicit user approval. Google Play plan unchanged: FREE, invite-only Internal Testing; no signing/upload authorization.
