# Atelier Time — D156 / Code50 — Mute reaches Android minimum; zero-end volume slider touch fixed

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
