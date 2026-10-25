# Android reliability QA gate — after Stable 4 (D-108)

**Prepared:** 2026-10-25. **Status:** SOURCE AUDIT COMPLETE; DEVICE EXECUTION NOT STARTED. This is documentation and evidence planning only; it does not constitute a new APK, a passed test, or a change to accepted product behavior.

## Fixed baseline and non-regression boundary

- Android Stable 4 is user accepted: `stable/android-d108` at `02beb0af46719301cccb72ad4698a337ab416d73`.
- Continue on `android-native-kotlin-compose`, Kotlin + Jetpack Compose and Code on the Go on the Android phone.
- **Do not reopen** approved Day/Night/Editor/Timer/Sound UI, ordinary transitions A/B/C, Home/other-app/lock-screen background playback, or D-108 Back/Recents-swipe notification cleanup.
- Do not uninstall the existing app, clear app data, overwrite user Classes/preferences, move any frozen stable ref, merge draft PR #3, or run paid Actions.
- Current accepted package has `versionName = "D-108"`, `versionCode = 4`. For the next **actual APK** use its real coding D identifier (next reserved D-110 unless subsequently superseded) and a strictly larger versionCode. Documentation-only activity requires **no new build**.

## Source audit of existing connected Android instrumentation tests

Read the Android sources from Stable 4 / the identical native development branch. These are **test methods present in source**, NOT results or confirmed discoverable tests on the phone:

| Source | Declared `@Test` methods | Scope / data safeguards |
| --- | ---: | --- |
| `app/src/androidTest/java/com/ateliertime/android/AndroidPresetStoreTest.kt` | 7 | Round-trip save/reload, CRUD, old/new opening-group format, corrupted JSON/wrong-type/future-version preservation; random private test SharedPreferences filename and `@After` deletion. No production `atelier_time_presets` namespace. |
| `app/src/androidTest/java/com/ateliertime/android/MelodyPreferencesTest.kt` | 6 | Audio selection and preference persistence/compatibility; random isolated test SharedPreferences file and `@After` cleanup. |
| `app/src/androidTest/java/com/ateliertime/android/StaticAudioTrackLifecycleTest.kt` | 1 | Android real AudioTrack MODE_STATIC initialization and short silent PCM test; not a playback/cue acceptance test. |
| **Total** | **14 source tests** | Exact number **executed/discovered** and PASS/FAIL remain unknown until an actual connected run. |

`app/build.gradle.kts` declares `androidx.test.runner.AndroidJUnitRunner` and Android instrumentation dependencies. Do not infer that a connected Android device, `adb`, test APK installation or a runner invocation is available in Code on the Go without checking the real terminal/device first. Historical runner notes counting 6 tests are superseded by the current 14 `@Test` source methods; no claim is made about runtime discovery.

**Test-data isolation caveat:** The two persistence suites use unique test names and delete their own files in `@After`. That prevents intentional contact with real Class/sound preferences, but abrupt test-process death can leave test-only preference files. Before execution, independently verify no test calls `clearApplicationUserData`, `pm clear`, `uninstall`, or production preference names.

## Next execution gate: QA-R01 connected instrumentation

1. On the existing CoGo checkout, confirm branch and commit using `git branch --show-current` and `git rev-parse HEAD`. Ensure there are no uncommitted local modifications before pulling any documentation changes.
2. Check whether CoGo's **existing** Android tooling can expose a connected instrumentation target (for example check `adb devices -l` only if `adb` exists). Do not install a new toolchain or create a build merely to check availability.
3. If a valid device target is available, run the existing `:app:connectedDebugAndroidTest` under CoGo with its proven **native ARM64 AAPT2** override. This may compile/install a separate test APK; it is **not necessary for the handoff itself**. Stop if test installation threatens user app data or device pairing is unavailable.
4. Preserve the real Gradle log and XML/JUnit reports; record actual test count, passed/failed/skipped, exact source HEAD, Android device/OS and test APK outcome. Label **BLOCKED** when the runner or device isn't accessible, not PASS.
5. Validate preservation of existing saved Classes and Sound preferences separately without re-running accepted audible A/B/C and Back/Home/Swipe acceptance.

Suggested future CoGo terminal invocation (ONLY when a connected device is confirmed, from `android-native/`, not for current handoff):

```sh
set -o pipefail
bash "$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle" :app:connectedDebugAndroidTest --no-daemon --console=plain "-Pandroid.aapt2FromMavenOverride=$ANDROID_HOME/build-tools/36.0.0/aapt2" 2>&1 | tee "$HOME/atelier-connected-androidTest.log"
```

## Following reliability gates (UNVERIFIED)

- **QA-R02:** long-running multi-hour Class on a real phone: actual timer deadline, observed A/B/C at novel long-run boundaries, drift/thermal/battery behavior. Existing injected-clock JVM regression is NOT real 3-hour device proof.
- **QA-R03:** Android Doze, battery restrictions and unexpected **OS process reclaim**; explicitly distinguish OS-initiated death from user-approved Back/Recents removal. The current notification-only foreground service does not own or promise to restart timer/audio. First define desired recovery behavior before changing it.
- **QA-R04:** persistence across device reboot and versioned update; corruption/future-schema behavior using **isolated test records** only. Never inject malformed data into actual user Classes.
- **QA-R05:** foreground-service `specialUse` Google Play policy audit (not accepted merely because debug works).
- **QA-R06:** accessibility, other devices, release signing/permissions/privacy/licensing. **MANDATORY user-approved logo and launcher/store icon assets before any store listing/publication**; see `ICON_LOGO_PRESTORE_GATE.md`.

## Evidence recording / decision gate

For each newly executed test record: **test ID; commit SHA; date; actual phone + Android version; exact command or procedure; isolated input; expected and observed outcome; PASS/FAIL/BLOCKED; logs/report reference; effect on real saved data; known limitation**. Record actual evidence in `docs/WORK_LOG.md` and update `docs/TEST_PLAN.md`, `docs/ROADMAP.md` and `docs/HANDOFF.md` only when the result exists. No accepted UI, sounds, timer semantics or lifecycle behavior are to be changed without a new identified defect and appropriate approval.

**Next immediate work:** QA-R01 — verify the device instrumentation execution path without building/reinstalling for the handoff. No such device check has been performed by this GitHub-source-only audit.
