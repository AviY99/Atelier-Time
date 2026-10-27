# D-121 — Next physical-budget correction and Stable-7 behavior comparison

**Date:** 2026-10-27
**Status:** DEVELOPMENT / UNVERIFIED. D-120 failed user acceptance. Never call D-121 Stable before a real build, JVM regression run, and device acceptance.
**Branch:** `android-native-kotlin-compose`; Android identity `D-121`/`versionCode=16`. The frozen `stable/android-d119-qa7` must remain untouched.

## The two independent clocks

Let original program duration be `T0` (whole milliseconds), start-end correction `C0`, physical elapsed time `p = monotonicNow - monotonicStart`, and virtual cursor immediately AFTER a runtime action `v`.

The new **single correction accounting identity** is:

`C_exact = C0 + p - v`

The phase engine uses whole seconds and computes `C_sec = ceil(C_exact/1000)`; the resulting **virtual** plan end is `T_virtual = T0 - C_sec*1000`. This virtual end is not the user-approved physical finish. The physical finish remains `D = physicalStart + approvedDurationMillis`, independent of `Next`.

Every reflow is rejected unless `abs((T_virtual - v) - (D - now)) < 2000ms`. The tolerance covers at most two whole-second quantizations (original selected clock fraction and signed correction); it does not permit an arbitrary extra minute. The physical deadline itself retains exact milliseconds.

When `Next` skips the remaining current phase, `v` becomes the start of the next phase. On `Restart`, `v` becomes the restarted phase start. On `Resume`, `v` is the frozen cursor. Therefore every operation uses the SAME identity rather than summing different local estimates.

## Legacy Stable-7 allocation subcases preserved inside one verified gateway

- **Source pair:** `W = F1 + F2 - C_sec`; split Remainder/Even/Custom, merge if `W + interveningBreak <= regularSession`, or insert whole regular `Session+Break` blocks when a strict threshold is exceeded.
- **Future tail:** `Q = (T0 - C_sec) - anchorStart - Summary`; `W(k)=Q-k*(regularSession+regularBreak)-regularBreak-n*(specialBreak-regularBreak)`. Previously observed prefix never changes.
- **Last only:** `deltaC = newC - (T0 - previousVirtualEnd)`; `newLastSession = oldLastSession - deltaC`, at least 60 seconds.

**Important:** The actual arithmetic subcases are still retained for compatibility; D-121 is a **single authoritative correction identity and reflow acceptance gateway**, not yet a ground-up rewrite into one arithmetic allocator. Calling that latter task complete would repeat the D-120 mistake. D-119's candidate priority (source, then active future tail, then last only) has been restored; D-120 incorrectly changed the order after the first Next.

## Screenshot discrepancy

The observed `Class start 22:52 / Class end 01:52` was still exactly three hours. The old label `Time added to ending 01:46:18` referred to earned Next credit for future sessions, not a late physical finish. The chart's `End 02:59:51` summed many individually truncated segments; it was not the Class deadline. D-121 labels Next credit explicitly and derives the chart's End label from physical elapsed plus remaining time when available.

## QA gate

New D-121 JVM regressions cover rapid consecutive Next actions, physical-vs-virtual remaining-time checks, Next/Pause/Resume/Break Restart and at-deadline finish. Tests have been **authored but not executed**. No Android/Compose build, instrumentation, app install or user acceptance has been performed for D-121. Run `tools/build_d121_candidate_on_phone.sh` only as a separately authorized build-only gate; report exact XML counts (do not assume a passing count). Never install a Tests APK. Preserve saved Classes and Sound preferences.
