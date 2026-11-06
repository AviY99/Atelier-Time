# Atelier Time — D156 / Code50 — Mute reaches Android minimum; zero-end volume slider touch fixed

> **AUTHORITATIVE REVISION — 2026-11-06 (new user request after the first D156 source pass).** D156 / Code50 now includes an additional mandatory change: **Max operates IMMEDIATELY in both Programmed/Class Timer and Free Timer without any consent/warning dialog, and the chosen maximum NEVER auto-restores to an old level** when the timer finishes, when the user exits, or when the activity is destroyed. This explicitly supersedes all older D150–D155 descriptions of Max confirmation and baseline restoration, including the text in earlier revisions of this document below. Removed `showConfirmMaxVolume`, `maxVolumeFromFreeTimer`, `class_volume_max_dialog`, `class_volume_max_confirm` and ALL `ClassVolumeControl.restoreIfUnchanged()` calls and prior media/alarm baseline fields. Both timer toolbars now call the SAME `applySelectedVolumeMax()` which reads back the actually selected Android stream and immediately synchronizes Sound ON/OFF; no other channel is modified. The Samsung build script **fails** if any of those old Max confirmation/restore mechanisms reappear. **Still not compiled/built/installed or runtime accepted; source-only checks to date.**


**Date:** 2026-11-06  
**Branch:** `android-native-kotlin-compose`. **Candidate source committed; Samsung build, full Android JVM test suite and AndroidTest compilation not yet verified for D156. User installation/acceptance pending.**
**Latest frozen user-approved:** **Stable16 / D153 / Code47**, immutable `stable/android-d153-qa16-ui-sound-user-accepted` SHA `04270365f330af78861d54f3710048d65dadc462`. Never edit/freeze D156 without separate acceptance. D155 / Code49 was **installed by the user, who found a further regression**.

## Evidence and exact root cause

The user screenshot of **D155** in running Programmed Timer with **Alarm Volume 100%** after pressing Mute showed a large red error `Android enforces a positive minimum on this sound channel.` — the value remained 100%, rather than being lowered to the actual Android minimum. The code had `check(canMuteSelected(useAlarm))` in `ClassVolumeControl.muteSelected`, preventing the system `setStreamVolume` call. **D156 removes that false rejection:** a Mute request always targets Android's minimum for the currently selected stream via `VolumeSliderMath.selectedTarget(0, minIndex, max)`; after the device accepts the operation, the UI reads back the actual resulting level. If the minimum is positive, Sound necessarily stays ON and the UI shows actual positive percentage; if zero is allowed, Sound goes OFF and the zero-warning shows. Do not display a false "successful OFF" state. Both timers use the same controller and visual slider.

The user also reported the shared volume slider is hard to grip when it displays zero. D155 used `AtelierSlimSlider` scaled vertically to 55%, in a minimum 32dp row and only 8dp horizontal inset, with Material thumb at the far left edge. **D156:** keeps the ordinary compact controls unchanged; for *only* the Class+Free volume bar, use unscaled Material3 Slider drawing, **56dp minimum touch height**, **24dp horizontal inset** at each end, and a continuous drag (`steps = 0`). The accessible volume slider remains one per timer, chosen only from the Sounds tab. Android hardware has discrete volume indices; even a small positive slider selection maps to at least **one actual Android step**, so the slider does not get stuck visually showing zero when a positive level is requested.

## Implemented changes

- `android-native/app/src/main/java/com/ateliertime/android/ClassVolumeControl.kt`: `muteSelected` no longer exits on Android positive minimum; instead invokes `setPercent(stream(useAlarm), 0)` and checks actual Android readback. `setSelectedSoundEnabled` OFF also targets real selected stream minimum. Slider calls shared minimum-aware pure mapping, including first positive step. Existing Max explicit user consent, saved positive history/25% fallback, ability to select only Media OR Alarm, and unchanged other hardware stream retained.
- `android-native/app/src/main/java/com/ateliertime/domain/VolumeSliderMath.kt`: new `selectedTarget(percent, min, max)`, rejecting invalid device range, clamping zero to the real device min and any positive percent to at least 1 or device min. Pure mapping has eight dedicated tests in `android-native/app/src/test/java/com/ateliertime/domain/D156DeviceMinimumVolumeTest.kt`.
- `android-native/app/src/main/java/com/ateliertime/android/AtelierSlimSlider.kt`: new `compactVisual = true` default to preserve all previous slim slider visuals; the volume slider opts out only.
- `android-native/app/src/main/java/com/ateliertime/android/RunningClassQuickControls.kt`: shared Class/Free slider gets `compactVisual = false`, `heightIn(min = 56.dp)`, `padding(horizontal = 24.dp)`, `steps = 0`. New Compose UI tests in `D156VolumeHitTargetUiTest.kt` check >=56dp touch strip, attempt to drag up from 0%, and verify Free Alarm uses same touch target. **AndroidTest compiled only on local helper, not executed automatically.**
- `android-native/app/build.gradle.kts`: versionCode **50**, versionName **D-156**.
- `android-native/tools/build_d156_minimum_mute_and_volume_touch_on_phone.sh`: fail-closed Samsung-local builder, verifies correct version/source/approved art, **specifically refuses old Mute guard**, checks true min-target math and touch-area geometry, requires 8 new pure JVM tests and all prior critical suites to execute with no errors/failures/skips, compiles AndroidTest source, builds debug APK and verifies approved splash SHA256 *inside final APK*. No cloud/paid GitHub Actions, signing, install/uninstall/data clearing, or Play upload.

## Test integrity — do NOT overclaim

- Developer inspected the actual GitHub source files and ran static consistency checks for regression logic, UI touch settings and script output/version. A separate local Kotlin compiler check of the selected volume target logic passed several minimum/0/first-step/invalid-input cases. **This does not substitute for phone Gradle build or on-device interaction QA.**
- D156 AndroidTest test methods were added as regression source and are compiled by the phone build script; the builder does not automatically execute them on the device.
- The screenshot from **D155 shows a real regression; D155 is NOT stable**. The new D156 fix must be locally built and manually verified before declaring anything else.

## One Samsung Code on the Go command

```bash
cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native && git pull --ff-only && bash tools/build_d156_minimum_mute_and_volume_touch_on_phone.sh
```

**Only upon real build success:** `/storage/emulated/0/Download/Atelier-Time-D156-API36-Candidate.apk` plus local D156 manifest and private build log. Install as an **in-place update over D155, do not uninstall/wipe saved Classes**.

Minimum visual/user QA matrix:
1. In Sounds, choose **Alarm**; enter Programmed Timer with Alarm at 100%; tap **Mute** → actual Alarm level must drop to the Android-permitted minimum (it may remain positive), without the previous red blocker message. Sound remains ON if minimum >0; zero warning only if actually 0. Do the same in Free Timer.
2. From Media at 0%, grab/drag the thumb from the LEFT end in each Timer → it should move easily and reach a positive Android level and turn Sound ON; warning disappears. Verify a tiny positive drag is not quantized back to zero. Tap Mute → actual Media 0, Sound OFF, warning returns. Test both timer views.
3. Repeat Alarm drag at low end; slider cannot request below device minimum; show actual level/percentage rather than misleading 0. If device allows Alarm 0, mirror Media completely.
4. Sound tab ON restores the last positive selected-stream level, or 25% with no prior value. Sound OFF lowers zero-capable stream to zero; protected channel respects Android minimum. Route selection does not change the other channel. Confirm Max warning still precedes the selected-channel maximum.
5. Check saved class and sound settings survive in-place update, timed cue playback follows Sound state, and display styles remain consistent.

**Google Play remains FREE with invite-only Internal Testing as initially agreed; no signed AAB or store upload.**

## Final D156 acceptance matrix — user-requested unified contract

This section is **authoritative** and supersedes any obsolete text above describing Max confirmation or automatic restore. One selected Android stream (Media or Alarm), and ONE shared implementation for both Class and Free:

| Selection / action | Actual selected device stream target | Global Sound | 0-volume warning | Other stream / exit |
|---|---|---|---|---|
| Media or Alarm, supported zero, Sound OFF | 0 | OFF after actual 0 | Visible at 0 | Other untouched |
| Media or Alarm, protected positive min, Sound OFF | OFF toggle unavailable; device min is authoritative | ON at actual min >0 | Not shown | Other untouched |
| Sound ON at selected 0 | Last saved positive device index; else 25% (at least 1 index) | ON after actual positive level | Hidden | Other untouched |
| Mute, zero-capable stream | 0 | OFF | Visible | Other untouched |
| **Mute, positive-min stream** | **Android minimum >0, not unchanged maximum** | **ON** because still positive | Not shown | Other untouched |
| Slider to 0, zero-capable | 0 | OFF | Visible | Other untouched |
| Slider to low/positive level | Actual hardware index >=1 and >=device min | ON after actual positive | Hidden | Other untouched |
| **Max click in either Timer** | **100% immediately, no dialog/consent/warning** | ON after actual maximum | Hidden | **Never restore previous volume** |
| Class finish, Free finish, Exit, Activity dispose | **No change to hardware volume** | From actual selected volume | From actual 0 vs >0 | **No restoration** |
| Route switch Media ↔ Alarm | Select which hardware stream is observed; **change neither** | Recompute from new selected stream | Recompute from new selected stream | No cross-channel change |
| Phone hardware buttons | Android's actual selected level | Reconciled on app refresh | From actual level | No auto-level write |

**Explicit regression gates built into `build_d156_minimum_mute_and_volume_touch_on_phone.sh`:** fail if `ClassVolumeControl.muteSelected` restores old D155 refusal `check(canMuteSelected(...))`; fail if VolumeSliderMath stops mapping a 0 request to actual Android minimum, if 56dp/24dp/continuous unscaled shared Timer slider is removed, or if Max consent/dialog flags or **any restore-on-exit method and Media/Alarm baseline** reappear. Enforce exactly 2 timer callbacks calling the same immediate Max function; 8 pure `D156DeviceMinimumVolumeTest` methods and prior JVM tests plus AndroidTest source compilation are required before debug APK is created. These checks do not prove on-device behavior until the user's Samsung runs them.

**Phone QA must include:** Alarm at maximum → Mute → actual minimum (not red restriction); Media at zero → thumb drag from far left → audible positive Android step; Sound tab OFF → selected zero where supported; Sound ON → previous positive or 25% fallback; Max **once** in Class and **once** in Free must update immediately **without any confirmation**, and **keep 100% after leaving both timers**; both warning states reflect true zero; changing output route must not change the other physical stream. No silent restore on session completion or activity disposal. **D156 still requires local build and user test; not a stable release.**

## Independent pre-build check (not an Android APK build)

On 2026-11-06, an **isolated native Kotlin compiler smoke check** was run in the assistant's container on copied equivalent pure implementations of `VolumeSliderMath` and `MediaSoundRestorePolicy` (not a clone/build of the Android Gradle project). It compiled successfully with `kotlinc` and printed `D156 pure Kotlin smoke: PASS (12 volume mappings, 3 invalid ranges, 5 restoration checks)`. This validates representative 0→device minimum, first positive step, Max=100% math and last-positive/25%-fallback cases. It **does not** run the repository's JUnit suite, compile Android Compose sources, execute Compose instrumentation, validate Android's `AudioManager` acceptance on Samsung, or verify the packaged APK. The official complete Gradle/JVM and APK audit still requires the one Samsung command above, and subsequent user visual/functional acceptance.
