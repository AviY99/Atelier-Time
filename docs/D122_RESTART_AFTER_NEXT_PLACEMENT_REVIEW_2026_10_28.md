## QA-D122-03 — APK verification command failed due to manifest script typo (2026-10-28)

User ran the version/code/branch/hash verifier following the 228/228 build; no `APK VERIFIED — PASS` appeared. Inspection of `tools/build_d122_candidate_on_phone.sh` confirmed a toolchain metadata defect: it correctly requires `versionCode=17` in Gradle, but wrote `code=16` into the generated `Atelier-Time-D122-QA-manifest.txt`. The previous verification therefore short-circuited on `grep -qx 'code=17'` **before** comparing hashes. This is not evidence of an invalid APK, but the APK must still be verified independently.

Corrected the script in development branch so *future* D-122 builds report `code=17`. Existing on-phone manifest from the earlier build naturally still contains `code=16`; do NOT silently edit it or claim it matches. To validate existing build without rebuilding, compare the actual candidate APK's `sha256sum` with the unchanged stored `app_sha256`, and inspect the real embedded `package` version via Android `aapt2 dump badging`. Do not install until both match and separate approval is obtained. No application source, Stable 7 branch, or installed phone data were changed by this script-only fix.

## QA-D122-02 — Samsung build-only rerun PASS (2026-10-28)

User-supplied real Samsung Code on the Go terminal screenshot confirms:
- `BUILD SUCCESSFUL in 1m 15s`; `72 actionable tasks: 72 executed`.
- `D-122 JVM ACTUAL: tests=228 failures=0 errors=0 skipped=0`.
- `BUILD ONLY PASS`.
- Regular candidate `/storage/emulated/0/Download/Atelier-Time-D122-Candidate.apk`.
- Android instrumentation APK `/storage/emulated/0/Download/Atelier-Time-D122-Tests.apk`.
- QA manifest `/storage/emulated/0/Download/Atelier-Time-D122-QA-manifest.txt`.
- Terminal explicitly states `NO regular/test APK installed`.

The two legacy Restart tests and three newly added D-122 regression tests are part of the 228 executed JVM tests. This **closes the build-only QA gate**, not the product acceptance gate. The screenshot does not show the manifest's commit SHA; verify APK SHA-256 against the manifest before any installation. Android instrumentation was assembled, not executed. Manual Samsung testing must verify repeated Next, Restart ordinary break after many Next, genuine special-break preservation, unchanged saved Class end, and preserved Classes/Sounds. D-122 remains a development candidate; frozen D-119 Stable 7 is unchanged. Explicit approval for D-122 installation is still required.

## QA-D122-01 — FAILED on Samsung (2026-10-28)

User-supplied Code on the Go screenshot shows `228 tests completed, 2 failed`, `:app:testDebugUnitTest FAILED`, `BUILD FAILED in 1m 18s` with 64 actionable tasks executed. The build-only gate did not pass; no D-122 candidate should be installed or called stable.

**Unknown until JUnit XML is reviewed:** exact failing test names, assertions, and exception stack traces. Do not guess that both failures are from the three newly added tests, do not weaken tests to force green. Retrieve `app/build/test-results/testDebugUnitTest/TEST-*.xml` errors from the phone and diagnose before code changes. D-121 remains installed development; frozen Stable 7 remains unchanged.

# D-122 — Restart-after-Next regression: forensic comparison and repair

**Date:** 2026-10-28. **Status:** development SOURCE FIX, NOT YET BUILT/TESTED; not Stable. `versionCode=17`, `versionName=D-122`.
**Basis:** user's paired screenshots of D-121 installed on Samsung, showing Restart of a regular five-minute Break after repeated Next.

## Observed evidence (right = before Restart; left = after)

| Field | Before | After |
|---|---|---|
| Break start | 00:07:37 | 00:07:45 |
| Break length | 05:00 | 05:00 |
| Next credit | 03:58:21 | 03:58:13 |
| Final Sessions (Even) | 16:40 + 16:41 | 16:36 + 16:37 |
| Added regular Sessions + Breaks | 8 | 8 |

The eight seconds of restarted Break time legitimately reduce the available ending budget by eight seconds and do NOT change the hard physical deadline. However, the **automatic genuine special Break visibly jumps from near the middle to much later in the Class**. This is not an acceptable side effect of restarting the CURRENT regular Break.

## Exact legacy Stable 7 Restart path

`MonotonicSessionTimer.restartCurrentSegmentKeepingEndTime()` in D-119:
1. `C_new = C_old + ceil((elapsedInCurrentPhase + pausedMillis)/1000)`.
2. First try `FinalPairCompensation.adjust(original, C_new, allowExtraRegular=nextOccurred)`.
3. Accept only when **all phases through the current one are exactly the same**.
4. Otherwise use `FinalPairCompensation.adjustLastOnly(active, C_new)` (NO `adjustFutureTail` fallback).
5. Validate the current phase identity/start and atomically reset its cursor. The approved physical Class finish remains fixed.

On a rebuilt program with previously added regular blocks, the first attempt normally fails prefix equality; the fallback changes only the final unstarted work Session:

`oldCorrection = originalTotal - activeVirtualEnd`
`deltaC = newCorrection - oldCorrection`
`newLast = oldLast - deltaC`

For the screenshots, where the credit shrinks by eight seconds, legacy behavior would usually preserve first final 16:40 and shorten last from 16:41 to **16:33** (the actual branch depends on precise in-memory state). That differs from D-121's 16:36 + 16:37 split and preserves already-placed specials.

## D-121 regression root cause

D-121's `reflow()` chose `adjust(original)` then **`adjustFutureTail(active)`** then `adjustLastOnly(active)`. `adjustFutureTail` re-creates unstarted added blocks and places genuine specials towards the end of the reconstructed tail. `SpecialBreakRebalancer.rebalance()` runs for Next but **not** for Restart. D-121 validated whole-lesson time and protected prefix but did not enforce an unchanged future structure for Restart/Resume. As a result a tiny correction could change break placement and/or the chosen final-pair distribution.

## D-122 source repair

- `ReflowBoundary.preserveExistingTail` is set by Restart and Resume; Next does not set it.
- Under this contract no `adjustFutureTail` candidates are considered. Only `adjust(original)` if its entire structure matches, or `adjustLastOnly(active)`.
- Validate *before commit*: same number/order/identity/type of all phases; all phases BEFORE the final adjustable pair must be byte-for-byte unchanged; outside the final pair, phase durations are unchanged. Thus added Sessions and genuine special Breaks cannot jump during Restart or Resume.
- Hard deadline millisecond budget validator from D-121 remains in force; no change to Next policy, approved Editor rules, or Stable 7 source.
- New `D122RestartPlacementRegressionTest` tests a three-hour program with approximately four hours of Next credit and inserted regular blocks. It checks Restart at +8 sec, Resume at +6 sec, intact special-break index/identity, intact inserted-block count, unchanged observed prefix, and correct final-work-only deduction.

## Release gate (pending)

Neither the new regression tests nor the Android app have been executed in this D-122 change set. **Do not treat source code or mathematical arguments as a PASS.** First run `bash tools/build_d122_candidate_on_phone.sh` from the development branch using the user-authorized build-only procedure and obtain actual JUnit XML totals. Installation requires separate approval. D-121 remains the installed development candidate until user action; D-119 Stable 7 remains unchanged and closed in GitHub.

## QA-D122-01 diagnosis from exact failing JUnit XML and corrective change (2026-10-28)

The user supplied the XML `<failure>` output. Exactly two **pre-existing** JUnit tests failed, not the three new D-122 regression methods:

1. `ClassClockTrackerTest.restartingCurrentSessionResetsOnlyItsClockNotClassBoundary` (at the invocation of Restart, test line 158).
2. `MonotonicSessionTimerTest.restartCurrentSessionKeepsEarlierPhasesAndOriginalClassDeadline` (at Restart, test line 113).

Both ended with `IllegalArgumentException: The approved finish cannot fit the unstarted future without rewriting completed Sessions or protected Breaks.` from `FinalPairCompensation.reflow`. These scenarios restart an ordinary **Session 2 at physical 40m, virtual start 30m, with NO prior Next**. Stable 7 succeeds with signed correction +10m, virtual reset to 30m, virtual total 170m and fixed physical finish 180m.

**Precise root cause:** D-122 accidentally set `preserveExistingTail=true` unconditionally for Restart/Resume. Its structural validator forbids lawful changes to the untouched final pair layout (including merge and phase-count changes) **even when no Next had ever altered the future**. This caused both old tests to fail.

**Corrective source change, still pending actual Kotlin run:**

- `preserveExistingTail = nextHasOccurred` in Restart and Resume; only post-Next restarts require strong identity/order/placement preservation.
- A separate `noActiveTailRebuild = true` applies to Restart and Resume **both before and after Next**. It preserves Stable-7 candidate priority: `adjust(original)` first, then `adjustLastOnly(active)`, but **never `adjustFutureTail`** for these actions.
- Next remains permitted to use `adjustFutureTail` with its existing real-remaining-time gate.
- The mandatory physical budget `abs(virtualRemaining - physicalRemaining) < 2000ms` is unchanged.
- No tests were deleted, renamed, ignored or weakened. Both pre-existing failures must pass alongside all three D-122 special-placement tests.

**Next QA step:** rerun `tools/build_d122_candidate_on_phone.sh` on development branch with full JVM suite and Android APK assembly, then provide actual JUnit totals. Expected test count is 228 from the failed run, but a pass has **not yet** been observed. D-122 is blocked from installation pending results. Stable 7 remains frozen and D-121 remains the installed development candidate.
