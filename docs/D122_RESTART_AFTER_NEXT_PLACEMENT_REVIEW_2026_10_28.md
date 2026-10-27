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
