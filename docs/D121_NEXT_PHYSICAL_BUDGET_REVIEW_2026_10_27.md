## QA-D121-03 — FAILED user acceptance: Restart after repeated Next (2026-10-28)

**D-121 is NOT accepted as Stable.** User-provided before/after screenshots show an eight-second Restart of an ordinary five-minute Break after several Next actions. Credit falls by eight seconds and final Even Split changes 16:40+16:41 to 16:36+16:37, which preserves total time, but an automatic genuine special Break visually jumps from near the middle to late in the class.

Source investigation confirms D-121 routed Restart through `adjustFutureTail` before legacy `adjustLastOnly`; this rebuilt the added future and could move the special. The legacy Stable-7 restart route used `adjust(original)` with locked-prefix check, otherwise `adjustLastOnly` (no tail reconstruction). A new D-122 source repair and three targeted regressions are documented in [D-122 investigation](D122_RESTART_AFTER_NEXT_PLACEMENT_REVIEW_2026_10_28.md). D-121 remains rejected even though its 225 JVM tests and Android build passed. **Do not reinstall or rename D-121 Stable.**

## QA-D121-01 — Samsung build-only evidence (2026-10-27)

**PASS (build-only).** User-supplied Samsung terminal screenshot explicitly shows:

- `BUILD SUCCESSFUL in 1m 17s`
- `72 actionable tasks: 72 executed`
- `D-121 JVM ACTUAL: tests=225 failures=0 errors=0 skipped=0`
- `BUILD ONLY PASS`
- Generated regular candidate: `/storage/emulated/0/Download/Atelier-Time-D121-Candidate.apk`
- Generated Android test package: `/storage/emulated/0/Download/Atelier-Time-D121-Tests.apk`
- Generated manifest: `/storage/emulated/0/Download/Atelier-Time-D121-QA-manifest.txt`
- `NO regular/test APK installed`

**Scope of acceptance:** Android/Gradle compilation, APK assembly and 225 local JVM tests passed on the user's phone. The Android instrumentation APK was built, **not executed**. Device behavior of rapid Next, mixed Pause/Restart, the physical-end invariant and preservation of saved Classes/Sounds **has not been accepted**. The terminal screenshot itself does not reveal the manifest's commit SHA. Status remains **D-121 development candidate; NOT Stable**.

**Gate:** Obtain explicit separate authorization before app installation/updating saved app data; no test APK installation or uninstall/downgrade. If authorized, first verify the manifest corresponds to the intended D-121 source, protect stored Classes/Sounds, and perform focused manual QA against Stable 7 behavior.

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


## Non-Android verification performed in this review

The source files were fetched again from the development branch and checked for all of the following: one `correctionAtCursor` identity referenced from Next/Resume/Restart; D-119's original candidate order; the new <2s budget gate and work-session minimum; physical-duration graph label; four new regression methods; D-121/code16 and build-only script identifiers. These checks all passed as **static inspections**, not Kotlin compilation.

The budget identity was also checked independently over 100,000 deterministic randomized mathematical inputs (physical elapsed, virtual cursor, initial correction and <1s approved-end fraction). Zero mathematical counterexamples were found; maximum observed quantization difference was 1,995ms. This verifies the stated algebra but **does not prove that every Kotlin branch calls the identity correctly**. Only a real JVM/Android regression run and user QA can provide that evidence. Do not convert these source/mathematical checks into a test PASS claim.

## Exact numerical reconciliation of the user's 22:58 screenshot

The displayed Class elapsed was `05:53 = 353s`, the displayed Next credit was `01:46:18 = 6,378s`, and the Class remaining was `02:54:07 = 10,447s`. Therefore the effective virtual cursor was `353+6,378=6,731s` (01:52:11) and the extended *virtual schedule length* was `10,800+6,378=17,178s` (04:46:18). Its virtual remainder was exactly `17,178-6,731=10,447s`, matching the physical remainder of `10,800-353=10,447s`.

Consequently the image does **not** prove that the physical end moved: it shows `22:52 -> 01:52` unchanged and perfectly reconciled remaining times. The lower timeline label `02:59:51` is 9 seconds short because it separately floors several observed/future fragments. This discrepancy is corrected at the label's physical-clock source; preserve numerical credit reconciliation. Independent verification of all runtime transitions remains mandatory despite this one consistent screenshot.
