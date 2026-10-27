# Stable 7 — D-119 Samsung build and user acceptance

**Approved by user:** 2026-10-27, explicit declaration: "מדובר בגרסה יציבה 7" (this is Stable 7), after the preceding report "הכל עובד" (everything works).

**Frozen checkpoint branch:** `stable/android-d119-qa7` in `AviY99/Atelier-Time`. The checkpoint must preserve the exact D-119 Android code accepted by the user; do not commit new features, change the app version, merge or force-push into this branch. Future work continues exclusively on `android-native-kotlin-compose`.

**Android package identity:** `versionName = "D-119"`, `versionCode = 14`. Stable 7 is a release *checkpoint designation*, **not** a new Android build/versionName, new install, or a Google Play production release.

## Actual evidence and sequence

1. Original D-119 candidate from development GitHub failed on Samsung at `:app:compileDebugKotlin` after 52 seconds. A screenshot of `Atelier-Time-D119-JVM-build.log` showed errors all located in `AtelierSlimSlider.kt` (experimental API, unsupported `trackRange`, lambda inference and `@Composable` overload issues). This candidate **was not installed**.
2. Corrective source kept D-119/code14 and replaced the experimental custom Material3 `SliderState`/track/thumb code with the already-used value-based Material3 `Slider` plus rendering-only `graphicsLayer(scaleY = 0.55f)`. It retains full touch target, width, steps, ranges, callbacks and sound/editor settings. This fix was published on the development branch at GitHub commit **`6fb06f361c4522b677cbad0804e89a835dc1ee30`**; no changes to class scheduling.
3. User executed the existing `bash tools/build_d119_candidate_on_phone.sh` in Code on the Go from the correct Android project directory after pulling corrected source. **Real phone screenshot:** `BUILD SUCCESSFUL in 1m 16s`, `72 actionable tasks: 72 executed`, and `D-119 JVM ACTUAL: tests=211 failures=0 errors=0 skipped=0`.
4. Build-only script produced regular `Download/Atelier-Time-D119-Candidate.apk`, Android test package `Download/Atelier-Time-D119-Tests.apk`, and `Download/Atelier-Time-D119-QA-manifest.txt`. The script explicitly reported no APK had been installed by it. The user later reported **"הכל עובד"** after the update-install stage and then explicitly designated this exact version **Stable 7**.
5. The accepted D-119 changes are fully visible top tab titles, one-line Class timeline color legend, HH:MM:SS Free Timer wheels remaining visible and read-only during run/pause while presets hide, and slimmer Sound/Editor selectors. Previously accepted Class timer, saved Class catalog, Sound settings and `Next`/special Break scheduling rules remain the required behavior.

## Verification boundaries and preservation

- **PASS:** actual Samsung Gradle Android/Kotlin compilation, regular and test APK assembly, 211 JVM tests from Gradle XML, and overall user-reported working installed D-119 application.
- **Not claimed:** connected Android instrumentation test APK execution; independent full QA of OS process death, multi-hour Doze/lock/audio delivery, device reboot recovery, external/Bluetooth interruptions, signed release/Play Store compliance, or individual persistence verification beyond the user's overall functioning report. The presence of an AndroidTest APK does not mean it was run.
- **Preserve:** user's two saved Classes and Sound choices. Never uninstall, clear app data, run instrumentation with installation, or replace this checkpoint without separate authorization.
- **Older rollbacks immutable:** Stable 5 `stable/android-d114` at `34a692a98282944a76e314efcc228dac3bd072ff`; Stable 6 `stable/android-d114-qa6` at `89ed2873ef3c91fe224f3723f9523d7409e75f7c`.
- **No software changes** were requested by the Stable 7 declaration; documentation and Git checkpoint only. Subsequent Android changes require a fresh development candidate and fresh QA, not a rewrite of Stable 7.

## Future workflow

Start new work from `android-native-kotlin-compose`, keeping Stable 7 `stable/android-d119-qa7` as an immutable recovery branch. Before any future APK update, increment Android `versionCode` beyond 14, identify its distinct D-number, build on phone, review real Gradle/JUnit output, obtain separate approval for install, and verify saved Classes/Sounds remain accessible. Do not describe local JVM tests alone as full Android compilation or old checkpoints as the currently installed code.
