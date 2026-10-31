# Atelier Time — D-115 unified development changeset
**Date:** 2026-10-27  
**Status:** development candidate only; Android/Compose build and on-device QA PENDING.  
**Branch:** `android-native-kotlin-compose` only; `stable/android-d114-qa6` (Stable 6) and `stable/android-d114` (Stable 5) remain frozen.

## User authorization and scope

The user asked to implement **all** change requests from the real three-hour Stable 6 field test in **one combined development iteration**. Separate consent remains mandatory for phone operations that affect the installed application, especially regular APK installation and Android instrumentation. No user's saved Class, Sounds or existing installed APK is altered by these source edits.

## Delivered in D-115 source

1. **Final two Sessions:** when the future final regular Sessions A and B, **including the Break between them**, satisfy `A + Break + B <= Editor regular Session`, the three phases are represented as **one continuous final regular Session** with length `A + Break + B`. The Break disappears, its minutes become Session minutes, and the original approved absolute finish remains unchanged. For `> Regular Session`, the approved Editor split (Remainder/Even/Custom) remains. Exactly equal merges. A merged Session has a one-minute minimum. Saved timelines never change, and already-started/completed final phases cannot be retroactively merged/re-written. Earlier segments and optional Summary retain their lengths.
2. **C melody and Summary:** automatic and manual final work Session → Summary now emit C *on entering Summary*. Summary remains a real phase; completion/Next from Summary does not repeat C. Without Summary, C is emitted at Class finish. Next on the last regular Session no longer silently skips Summary.
3. **Accidental Next protection and diagnostics:** Next now requires confirmation with the intended next phase. A bounded, run-local, user-visible Recent Class actions history records wall/monotonic timestamps, transition source, phase IDs and whether cue playback was requested/alerts disabled. It is not permanently stored and does not rewrite Classes/Sounds. This assists diagnosis of the *undetermined* missing-Break field observation; it does not claim a confirmed scheduler defect.
4. **Free countdown:** a permanent Timer-tab section with independent hour/minute/second scroll wheels, Start, Pause, Resume and Exit; monotonic countdown; exactly one completion event; silent cancellation. It can run concurrently with a Class. State lasts while the application process remains active; it is not saved/restarted after genuine app exit or process death. It has no Editor/finish adjustment rules.
5. **Free timer sound:** the Sounds tab exposes its own built-in/system/file selector and 5–60 second duration, independent of Shared/Separate Class A/B/C choices. Settings are stored under new preference keys without removing or repurposing existing A/B/C keys. Free and Class cues have separate playback player instances.
6. **Live Timer glance:** Timer renders the existing color-coded `Class timeline at a glance` using the **actual runtime** phases after compensation, with a physical `Class remaining` progress line directly above the timeline, plus an active-phase outline and virtual phase-time cursor.
7. **Audibility and auto-lock:** opt-in `Keep screen awake during timers` in Timer uses Android Activity `FLAG_KEEP_SCREEN_ON` only while a Class/free timer is active and the app is visible. The system may still turn off the screen in background, and the power button can lock manually. Adds current volume-at-zero warnings and a pre-Run sound preview. Sounds has an explicit opt-in **Use alarm volume for Class A/B/C**, routed via Android `USAGE_ALARM` instead of `USAGE_MEDIA`. This does not override a muted alarm stream, DND, OS audio policies, Bluetooth routing or audio focus denial. Short-lived audio focus is requested and released for each cue. Nothing automatically turns volume up.
8. **Package identity:** candidate `versionName=D-115`, `versionCode=10`; Stable 6 remains D-114/9. New guarded CoGo candidate-build and Termux D-115 test scripts are prepared without altering the historical D-114 QA scripts.

## Verification and evidence boundary

- **LOCAL JVM PASS 196/196:** The D-115 pure Kotlin domain and pure `EditorInput` classes were compiled with Kotlin compiler 2.2.0 / Java 17, and all 20 JUnit4 test classes were executed locally through `org.junit.runner.JUnitCore`: actual terminal result `OK (196 tests)`, time `0.151s`, exit code 0. This is an independently executed *local non-Android* suite, not a CoGo Gradle result and not phone instrumentation. Historical Stable 6's 185/37 cannot be reused as D-115 results.
- New regression source covers `<`, `=`, `>` merge threshold, one-minute merged limit, end-clock/summary preservation, automatic and manual C cue timing, free monotonic pause/resume/zero/single completion, independent sound settings, isolated Android preference persistence and Compose free-timer controls.
- **NOT YET VERIFIED:** Android Gradle/Compose compilation; regular and AndroidTest APKs; Android instrumentation on Samsung; locked-screen/Doze audio and alarm-volume behavior; foreground/background free-timer delivery; 3-hour D-115 field test; two saved Classes and Sounds preservation after a D-115 installation. Do not label them PASS without real evidence.

## Safe next gate — with separate phone approval

1. Confirm no active Class or free timer and verify existing two saved Classes and Sounds.
2. In CoGo checkout of `android-native-kotlin-compose`, after `git pull --ff-only`, run `bash tools/build_d115_candidate_on_phone.sh` from `android-native/`. This **builds** the regular D-115 candidate and AndroidTest APK and runs JVM tests, but does **not** install either.
3. Inspect actual build/JUnit XML results and APK SHA-256 manifest. Get explicit consent **before** installing the regular D-115 APK. Use a non-destructive update only; never `pm clear`, uninstall, delete Classes or Sounds.
4. After installation and data-preservation checks, obtain **another** approval before installing D-115 test-only APK / running Termux instrumentation via the guarded `tools/termux_run_d115_qa.sh`; save actual raw log and PASS/FAIL counts.
5. Perform focused user-confirmed real-device tests: mute/media/alarm/DND/Bluetooth and lock, Keep screen awake, Next confirmation/history, interrupted final-pair merge and preserved end, Summary/C melody, free countdown in foreground/background, timeline visuals. Keep unresolved root causes explicitly OPEN until observed.

## Android platform basis

- Screen-on Activity flag and limitations: https://developer.android.com/develop/background-work/background-tasks/awake/screen-on
- Audio usage and stream semantics: https://developer.android.com/reference/android/media/AudioAttributes
- Audio focus/background restrictions: https://developer.android.com/media/optimize/audio-focus
