# Atelier Time — on-phone execution bridge (K01–K03)

## 2026-10-23 — Required native AAPT2 override for CLI Gradle on ARM64

User's **real first SDK-36 project run** on phone reached Kotlin compilation but Gradle task `:app:processDebugResources` failed with `aapt2-9.3.1-15703166-linux ... Syntax error: word unexpected (expecting ")")`. This is Maven Linux AAPT2, inappropriate for native Android ARM64. CoGo's own [GradleBuildService.kt](https://github.com/appdevforall/CodeOnTheGo/blob/stage/app/src/main/java/com/itsaky/androidide/services/builder/GradleBuildService.kt) injects `-Pandroid.aapt2FromMavenOverride` when building from its **GUI**, but the earlier plain terminal Gradle invocation omitted it. Its [Environment.java](https://github.com/appdevforall/CodeOnTheGo/blob/stage/common/src/main/java/com/itsaky/androidide/utils/Environment.java) defines native AAPT2 at `$ANDROID_HOME/build-tools/36.0.0/aapt2`.

The restricted phone runner now validates the local binary in `doctor` and automatically adds the same Gradle CLI option to unit-test, APK and instrumentation tasks. This is a **tool invocation fix**, not a product/Gradle model change; it must be verified by an actual on-phone rerun. No global `gradle.properties` changes or destructive Gradle cache clearing are needed.

From the already-open `android-native/` terminal folder, first check the existing CoGo binary:

```sh
"$ANDROID_HOME/build-tools/36.0.0/aapt2" version
```

If this prints the AAPT2 version successfully, rerun the **same real project tests** with the native override:

```sh
bash "$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle" :app:testDebugUnitTest --no-daemon --console=plain "-Pandroid.aapt2FromMavenOverride=$ANDROID_HOME/build-tools/36.0.0/aapt2"
```

Do not claim any Kotlin tests have passed until the Gradle task reports verified results. A separate Kotlin warning `SessionEditorScreen.kt:207` about an unnecessary Elvis operator is not the AAPT2 build failure.

---

**Status: source committed, runner not yet started or verified against GitHub tasks on the user's device.** A user screenshot from the real CoGo terminal on 2026-10-23 verifies that `bash "$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle" --version` succeeds (Gradle 9.6.1, embedded Kotlin 2.3.21, Java 21.0.10, ARM64). Direct execution of that Gradle script produced `Permission denied`. The runner now invokes CoGo's managed script **through Bash** instead of requiring an executable `gradle` command in `PATH`. Python 3.12.12 and Git were also observed on-device. **No Android Gradle project build, APK, UI or stored-session test has run yet.**

This is the real Android project, not a sample app. The phone runs the tasks itself. A small Python process in the **Code on the Go embedded terminal** polls **GitHub Issues** for tasks created by repository owner `AviY99` and posts limited results back. It does **not** run GitHub Actions or expose SSH/network ports. It accepts only:
- `doctor`: inspect visible CLI Java, Git, Gradle, Android SDK and ADB.
- `unit-tests`: `:app:testDebugUnitTest`.
- `debug-apk`: `:app:assembleDebug`.
- `instrumentation`: `:app:connectedDebugAndroidTest` (requires working on-device instrumentation/ADB).

No general shell commands, file deletion, branch reset, signing-key export, APK installation or unapproved operations can be requested through the issue format. Builds only use the exact **current HEAD** of `android-native-kotlin-compose` and a dedicated `~/.atelier-time-device/checkout`; historical Termux worktrees remain untouched.

## 1. Prepare the phone (one time)

Install/upgrade [official Code on the Go](https://appdevforall.org/code-on-the-go/) for Android ARM64 and complete its built-in SDK/toolchain onboarding. In CoGo's Git/project workflow, clone `https://github.com/AviY99/Atelier-Time`, select branch `android-native-kotlin-compose`, and open the nested `android-native/` folder as the Android Gradle project (not the Dart repository root).

Inside **CoGo's own integrated terminal**, check `python3 --version` and `git --version`. If missing and its Termux-compatible package manager is available, install just the missing tools using `pkg install python git`. Do not reinstall Android SDK/Gradle over CoGo's managed toolchain, and do not run these commands in the historical, separate Dart Termux worktree.

For the phone runner itself, obtain a **separate checkout** to avoid mixing with user edits:

```sh
git clone --single-branch --branch android-native-kotlin-compose https://github.com/AviY99/Atelier-Time.git "$HOME/Atelier-Time-DeviceBridge"
cd "$HOME/Atelier-Time-DeviceBridge"
python3 android-native/tools/phone_task_runner.py --doctor-local
```

The last command only inventories the **real** phone CLI environment. It reports whether `git`, `java`, `gradle`, and `ANDROID_HOME` or `ANDROID_SDK_ROOT` exist in CoGo's terminal. **If CLI Gradle is absent, do not assume the bridge can build** simply because the CoGo GUI can; inspect the real terminal output and configure supported access to the existing toolchain before scheduling remote Gradle tasks.

## 2. Authorize only GitHub Issues (one time)

On the phone, open GitHub Settings → Developer settings → Personal access tokens → Fine-grained tokens → Generate new token. Select only repository `AviY99/Atelier-Time`. Set **Issues: Read and write** (Metadata read is automatic); no Actions, Secrets, Administration or Contents write permission is required. Use an expiration date.

Then, in CoGo terminal from `$HOME/Atelier-Time-DeviceBridge`:

```sh
python3 android-native/tools/phone_task_runner.py --setup
```

Paste the token at the **hidden prompt on the phone only**; never send it in ChatGPT or commit it to GitHub. It is saved under `~/.atelier-time-device/token` with file permissions `0600`. Deleting this file or revoking the token stops its authorization.

## 3. Start or stop the bridge

```sh
python3 android-native/tools/phone_task_runner.py --poll
```

Leave that terminal process running while asking ChatGPT to run a named project task. The phone polls GitHub about every 90 seconds and reports the exit code and a limited, filtered log excerpt to the corresponding **public** GitHub Issue. The full result is kept on the phone under `~/.atelier-time-device/results/`. Press **Ctrl+C** to stop. The process is **not** a guaranteed always-on daemon and Android may suspend/kilI it when backgrounded. No background work by ChatGPT is implied; ChatGPT reads the posted results when the conversation resumes.

A task is created as a GitHub Issue opened by `AviY99` with the exact title `[ATELIER-DEVICE] unit-tests` (or another allowed task), and body:

```text
ATELIER_DEVICE_TASK_V1
{"task":"unit-tests","sha":"<40-character-current-branch-head-sha>"}
```

The runner checks the title, author, allowlisted task name, SHA syntax and actual remote branch head; it refuses stale revisions or a dirty dedicated checkout. It posts an execution claim before the task and does not blindly replay already claimed work. It closes the issue after posting a result. The `doctor` task uses the same request protocol but does not build anything.

**Security/privacy:** The GitHub repository and its issue comments are public. Logs may contain local paths and diagnostic project text. The bridge filters potentially sensitive lines but cannot guarantee automatic redaction of every secret. Never store passwords, tokens or private data in Gradle output; review before enabling this system. Disable by stopping the runner and revoking the token. Build scripts from the versioned repository run on the phone with the normal user permissions of the CoGo terminal; only the trusted project branch should be built.

## 4. Actual acceptance remains unchanged

In order: run `unit-tests` (32 Kotlin JVM methods), `debug-apk` (real debug APK), phone install/relaunch and preset save/load/CRUD/offline manual checks, then `instrumentation` (6 Android tests) where supported. Record logs and evidence in the canonical docs on `main`. Until successful output and device evidence exist, K01–K03 remain **UNVERIFIED**, K04 timer/sounds **NOT IMPLEMENTED**, and PR #3 remains a draft. Do not enable GitHub Actions, erase prior Termux directories or merge PR #3 automatically.
