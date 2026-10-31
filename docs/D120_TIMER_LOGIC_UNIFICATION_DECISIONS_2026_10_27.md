## QA-D120-02 — BLOCKED / user-reported Next regression (2026-10-27)

**D-120 is NOT accepted.** Following the successful Samsung build-only result, the user reported and illustrated that using `Next` appeared to add time beyond the fixed Class finish. This is a **user-observed acceptance failure**, not a independently re-run deterministic reproduction of a changed wall-clock deadline. Do not claim the installed build, exact device actions, or changed `classEndEpochMs` until verified.

**Source-level diagnosis:** `MonotonicSessionTimer.nextPhase()` computes a negative signed correction when a phase is skipped, and `FinalPairCompensation.adjust()` / `adjustFutureTail()` increase the virtual schedule end (`totalSeconds = originalDuration - correction`). This transformation existed in D-119 and is only valid if an exactly matching virtual-cursor jump absorbs the same credit, preserving the *physical* finish. `ClassClockTracker.afterNext()` keeps its physical deadline, but the new `reflow().valid()` verifies internal continuity / protected phases / Summary, **not** the invariant `physicalNow + effectiveFutureDuration == approvedPhysicalDeadline` nor the displayed future phase end labels against that deadline. The D-120 consolidation remains a dispatching wrapper around three older calculation strategies, not a fully singular planner. Passing 221 JVM tests is not proof of this missing invariant or UI correctness.

**Release gate now FAIL / BLOCKED** until a targeted deterministic Next sequence reproduces or rules out actual physical-end drift; tests must compare the unchanged `classEndEpochMs`, the virtual-versus-physical relation, phase-end predictions and live timeline after every Next, including repeated Next + Pause + Restart, with the full user-observed case. Avoid equating an increased *virtual* `totalSeconds` with an actual moved `Class end` before validating which label changed. Review and repair on development branch only, with new QA. Keep Stable 7 frozen; do not recommend uninstall/downgrade on the phone because it risks stored Classes/Sounds.

# D-120 — Approved timer logic unification and behavior contract

**Date:** 2026-10-27

**Status:** Product rules approved by the user after Stable 7 Timer Logic Review. Implementation/QA is D-120 development work only.

**Branch:** `android-native-kotlin-compose`. Never modify `stable/android-d119-qa7` or installed D-119.
**Basis:** `HANDOFF_AFTER_STABLE7_D119_TO_TIMER_LOGIC_REVIEW_2026_10_27.md`, `STABLE7_D119_QA_ACCEPTANCE_2026_10_27.md`, and the user's subsequent approvals in the project conversation.

## User-approved product decisions

1. **One physical Class finish target.** The approved Class finish is a hard target. `Next` must not terminate the Class before that target unless the user explicitly chooses to end the Class. A failed reflow must not silently change the target. Preserve the Summary's approved duration and the observed past. A proposed action that cannot respect these invariants must be rejected atomically with a clear explanation; do not invent extra waiting phases or stretch the Summary without approval.
2. **Genuine special breaks outrank final-pair merging.** Preserve an eligible special break and its approved manual placement ahead of merging the final two regular Sessions. A merge that would remove a protected special break is not allowed; try the feasible non-merged split, otherwise reject the proposed time adjustment. Automatic genuine special breaks may be relocated only among unstarted eligible positions and must retain their count.
3. **Duration-based runtime distinction.** `L > B` means a genuine special break, subject to the special-break preservation and relocation rules. `0 < L <= B` is valid and keeps its **actual configured duration L** and its initially approved editor placement. During runtime adjustments (`Next`, `Restart`, `Pause`/`Resume`), it is treated as an ordinary break: no special relocation or special-preservation privilege, and it may participate in regular final-pair merging. Neither case changes stored/approved rules. This decision resolves the older D-038 open comparison.
4. **Unify runtime recalculation.** Consolidate the three separate `FinalPairCompensation.adjust`, `adjustFutureTail`, and `adjustLastOnly` reflow paths behind one planner that accepts an immutable observed prefix/current-phase boundary, remaining physical time budget, ending choice, and break constraints. Do not change user-visible rules merely to simplify code.
5. **Planning-only 25/5 warning.** The cumulative 25 minutes of work / qualifying 5-minute rest rule is an Editor preview warning and acknowledgement rule, not a runtime timer control. It never blocks, reallocates, or changes a running Class. The actual-duration rest warnings in the Editor remain unchanged.

## Timing and implementation invariants

- Use monotonic `Long` milliseconds as the authoritative runtime time domain. Keep fractional seconds across repeated Pause/Resume/Restart/Next; round only for presentation and whole-minute planning constraints. The original physical deadline is not derived from the virtual phase cursor.
- An observed fragment is immutable. Runtime reflow can change only the unobserved flexible suffix; any already-started Session/Break cannot move backward or be rewritten.
- All generated future phases remain positive, contiguous, and satisfy at least one minute per work Session. Initial approved schedule and saved Classes/Sounds remain untouched.
- Summary duration is fixed. Sound C is emitted at most once on the end-of-work/Summary boundary.
- The `Next` rule for inserting a full `regular Session + regular Break` uses a **strictly greater than** threshold (`two regular Sessions + one regular Break`); equality inserts nothing. Initial finish approval alone must never insert extra blocks.
- Real special-break count and manual positions are protected; short/equal configured replacements are ordinary for runtime-reflow purposes while retaining their duration.
- There must be a single accountable Class termination decision (explicit Exit or physical deadline), except for explicitly approved future exceptions.

## Review evidence and regressions required before an APK

The Stable 7 source review identified a confirmed per-Pause ceil bug: ten 100ms pauses were charged as ten seconds instead of one, allowing an approximately nine-second early virtual finish in a 180-minute Class. The D-120 acceptance suite must include repeated sub-second interruptions, long pauses, exact deadline boundaries, final-pair restarts, repeated Next with inserted regular blocks, manual special break in the final pair (both `L>B` and `L<=B`), strict insertion threshold equality, physical finish after uncompensated actions, Summary/C once, Free Timer near-zero pause, and overnight/DST display cases.

The prior D-119 Samsung evidence (211 JVM tests, Gradle build, user working report) remains historical and does not prove D-120. Development validation must be recorded separately. Do not build/install/test on the user's Samsung or replace APK without the relevant separate approval. Any future phone candidate needs a new D-number and `versionCode > 14`.

## Gate

**Source-only implementation checkpoint:** `D-120`, Android `versionCode=15` on `android-native-kotlin-compose`. On 2026-10-27, a local Kotlin 2.2.0/JDK17 compilation and JUnit4 run of all pure domain + EditorInput tests returned **221 tests / 0 failures**, including 10 new D-120 deterministic regressions. The prior unmodified D-119 baseline independently returned 211/211 in the same local environment. `git diff --check` and new D-120 build-script syntax are separate source checks. These results are **not Android Gradle/Compose compilation, connected instrumentation, Samsung build or user acceptance**.

Product contract approved; implementation and automated regressions remain a **development candidate, not Stable**. The new `tools/build_d120_candidate_on_phone.sh` is prepared for a later separately authorized build-only gate. Stable 7 stays the rollback checkpoint.

## QA-D120-01 — actual Samsung Android build-only evidence (2026-10-27)

**PASS — build-only, user-provided real phone terminal screenshot.** The user ran the D-120 build-only gate in Code on the Go; the visible terminal output explicitly shows:

- `BUILD SUCCESSFUL in 1m 26s`; `72 actionable tasks: 72 executed`.
- `D-120 JVM ACTUAL: tests=221 failures=0 errors=0 skipped=0`.
- `BUILD ONLY PASS`, with three generated files in `/storage/emulated/0/Download`: `Atelier-Time-D120-Candidate.apk`, `Atelier-Time-D120-Tests.apk`, and `Atelier-Time-D120-QA-manifest.txt`.
- The output explicitly states `NO regular/test APK installed`.

**Evidence scope:** Actual on-phone Gradle build/test run and assembly of both APKs. The screenshot does not show the manifest's commit hash and does not prove Android connected instrumentation execution, installed-app manual acceptance, persistence across an update, background/Doze/endurance tests or user acceptance of D-120 as Stable. The earlier *source-only* gate paragraph is retained as historical status before this phone build.

**Next approval gate:** Seek separate explicit permission before installing/updating the regular D-120 candidate APK; protect the user's two saved Classes and Sound selections and never install the Tests APK as the application. D-119 Stable 7 remains closed, installed, and recoverable until an separately authorized update.
