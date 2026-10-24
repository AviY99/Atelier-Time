# D-088 — Design tabs, persisted palettes and static time / tahini motion

## 2026-10-24 — D-088 38s Gradle PASS, device visual acceptance pending

D-088 ON-PHONE BUILD AND JVM TEST TASK PASS (user screenshot, 2026-10-24): Gradle `:app:testDebugUnitTest :app:assembleDebug` ended **BUILD SUCCESSFUL in 38s**, 42 actionable tasks (10 executed, 32 up-to-date). Screenshot visibly includes `:app:compileDebugUnitTestKotlin`, `:app:testDebugUnitTest`, `:app:packageDebug`, `:app:assembleDebug`. Thus Android Kotlin build, JVM test task, and debug APK assembly succeeded after the D-088 source changes. Exact individual test counts and Android instrumentation results are NOT shown. D-088 APK is **not yet confirmed update-installed or visually/functionally accepted**. Next gate: copy APK to Downloads, update-install (no uninstall/clear data), inspect Design Day/Night/Auto persistence and local-hour threshold, solid tahini Session fill and Break drain, no digit animation/flashing, About spelling/version/©2026/credit and short summary. Keep stable Android D-086 snapshot untouched. PR #3 remains OPEN/DRAFT, no CI Actions.

---

**2026-10-24 · SOURCE WRITTEN, PHONE GRADLE BUILD AND APK QA PENDING**

This work follows user-confirmed **Android Stable 1 D-086**, preserved unchanged on [`stable/android-d086`](https://github.com/AviY99/Atelier-Time/tree/stable/android-d086) at `d0a03d2792cc324aeeba39b6327136ba6d37fd0c`. This increment affects only UI, local design preferences, visual motion, and About; signed saved-class records, scheduling domain math, audio cues and original Class start/end clocks are not reworked. Implementation branch: `android-native-kotlin-compose`, PR #3 still Draft/unmerged, no paid CI.

## Explicit user design contract

- Day = palette A / **Studio Linen** (light parchment/card, clay/sage accents); Night = palette B / **Night Focus** (deep navy, electric blue and lavender).
- Typography = **Flow-inspired**: rounded modern sans with bold easy-to-read numeric hierarchy and tabular timer digits. The code currently uses Android's maintained `SansSerif` for glyph coverage rather than adding unverified external font assets. No font-size or digit-change animations, no blinking.
- Single solid **tahini** hue throughout the active phase's large card: during a Session an overlay **fills upward** over time; during a regular/special Break overlay **drains downward**. Matching solid tahini bar uses exact same phase fraction: Session bar fills; Break bar drains. No gradient, no pulsing, no flashing. Surface tint alpha is reduced for label contrast. When paused the computed progress is frozen; Next starts the next phase based only on the real timer's state, never on an animation timer. The moving fraction may ease for 210ms to avoid discrete jumps; text remains static.
- New **Design** tab contains three selections: Day, Night, Auto. Auto exposes a slider for local hour `12:00..23:00` when Night begins; the fixed Day return is `06:00`, stated in the UI. Default initial setting = Day, default Night start=19:00. Current hour comes from device-local calendar, updates while app open and is re-evaluated after restart. Preferences use separate app-private SharedPreferences name `atelier_design_v1`, never modifying signed presets.
- New **About** tab shows exact user-requested spelling **"Atlier Time"**, actual installed APK versionName (read from PackageManager; current Gradle config is `0.1.0-dev` until explicitly changed), copyright **© 2026**, credit **"פותח ע״י Avi Yosfan בסיוע Open AI"** and a brief Hebrew summary describing scheduling, breaks, saved Classes, timer and audio alerts.
- Five tabs (Editor / Sounds / Timer / Design / About) use `ScrollableTabRow` so no item is clipped on a narrow phone. Theme applies to the whole content including dialogs, not scheduling domain.

## Implementation and source tests

New Kotlin `domain/DesignMode.kt`: Day/Night/Auto resolution and pure phase-fill fractions. New `android/AtelierDesign.kt`: persisted options, Material3 day/night semantic colors, typography/shapes, settings/About panels. Updated `SessionEditorScreen.kt` roots all tabs in `AtelierDesignTheme`, reads local hour and changes palette, passes palette into Timer. Updated `SessionTimerScreen.kt` draws a clipping-respecting solid tahini overlay and matching LinearProgressIndicator using `DesignMotion.fillFraction`. New **7 source JUnit tests** in `domain/DesignClockTest.kt` exercise manual overrides, Auto transitions, slider changes, invalid hour handling, work fill / Break drain and clamping. These new tests have **not** yet run via phone Gradle; do not report them as passed.

## Required local CoGo validation — only NEW D-088 work

1. From existing Android Gradle folder fast-forward branch, run `:app:testDebugUnitTest :app:assembleDebug` using the already-proven Gradle 9.6.1 Bash command and native ARM64 AAPT2 override. If fails, use exact compiler `e:` messages from the log to repair; do not claim a pass in advance.
2. After success, copy debug APK to Downloads, update-install WITHOUT uninstalling or clearing app data. The stable D-086 branch is a fallback.
3. Confirm Editor/Sounds/Timer/Design/About tabs all reachable on the narrow phone; controls and dialogs themed, with no clipped tabs. Verify Day and Night contrast and visual consistency; settings survive force-stop/relaunch without modifying saved classes.
4. In Design choose Auto and move slider to a time before/after current hour to verify selected palette, including exactly the threshold hour, 06:00 morning Day, and overnight Night. For testing outside the threshold, use existing device/local clock strategy only if practical; do not demand changing device time or risk timer data. Spinner/slider should not reset active Class.
5. Run a Session, watch single-color tahini **background fill** and **bar fill**; Next to Break, watch **background drain** and **bar drain**. Pause should freeze both. No timer-digit font animation, blink, gradient or pulsing. Start/end hh:mm:ss, fixed Class deadline, A/B/C sounds and saved presets remain unchanged.
6. About must display **Atlier Time**, actual **Version**, **© 2026**, **Avi Yosfan / Open AI** and a short app description in Hebrew.
7. Separate open gates from D-087 still remain: 9 Android device-instrumentation tests unrun, audio while locked/backgrounded and Doze, accessibility and performance, signed release and later cross-platform implementation.

If tests and device QA pass, record **new design acceptance** explicitly and preserve the old Android Stable 1 checkpoint. Do not merge Draft PR #3 or publish a signed production release automatically.
