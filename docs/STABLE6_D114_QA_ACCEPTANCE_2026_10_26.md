## 2026-10-26 — STABLE 6 FROZEN REF VERIFIED (post-freeze dev-only record)

Stable 6 was created and read back successfully from GitHub: **`stable/android-d114-qa6`**, exact **commit `89ed2873ef3c91fe224f3723f9523d7409e75f7c`**. The preserved earlier Stable 5 branch `stable/android-d114` remained at **`34a692a98282944a76e314efcc228dac3bd072ff`**. Stable 6 includes D-114 `versionName="D-114"`, `versionCode=9`, all 222 previously passing automated test sources and the recorded QA/acceptance documentation. Comparing tested development commit `3e78db0d014ec79f55ad41dd07f25c982a90173b` through the Stable 6 snapshot found **documentation-only intervening changes**. No production Kotlin version bump or new regular application APK was generated; phone installation unchanged. Freeze is intentionally immutable; this extra exact-SHA note was written **only in development branch after the freeze**.

Stable 6 acceptance details: [STABLE6_D114_QA_ACCEPTANCE_2026_10_26.md](STABLE6_D114_QA_ACCEPTANCE_2026_10_26.md). Remaining long-run/Doze/system-clock, actual audio, OS-reclaim, and ADB probes were not included in the 222 automatic test result; continue separately only with appropriate approval.

# ATELIER TIME — Stable 6 acceptance (D-114, automated QA-verified)

**Acceptance date:** 2026-10-26  
**Decision authority:** explicit user statement in conversation: "זאת כרגע גרסה 6 יציבה לאור מעבר מספר רב של בדיקות בהצלחה".  
**Milestone status:** **Stable 6 — approved** after the real D-114 Stage 2 automated QA results and post-run persistence verification.

## Interpretation of Stable 6

- Stable 6 is a new **verified stability checkpoint**, promoted **without production-code changes** after Stage 2 testing of D-114.
- The **installed APK remains D-114** (`versionName = "D-114"`, `versionCode = 9`). This is not D-115, not a new APK and not a request to rebuild or reinstall the application. Do **not** bump the version merely to relabel the QA milestone; a newly compiled binary would require its own validation.
- **Stable 6 frozen branch:** `stable/android-d114-qa6` — the immutable snapshot containing this acceptance record and the canonical updated QA/status documentation. Never advance/rewrite the branch; use GitHub's branch-ref SHA to identify the exact frozen commit.
- **Previous Stable 5 stays separately frozen:** `stable/android-d114` at `34a692a98282944a76e314efcc228dac3bd072ff`; do not modify or remove it.
- **Development continues only on** `android-native-kotlin-compose` after this freeze. Subsequent development must not silently modify the Stable 6 snapshot.

## Evidence that supports promotion

| Gate | Observed evidence | Status |
|---|---|---|
| CoGo Gradle + JVM | `BUILD SUCCESSFUL in 1m`; `D-114 JVM ACTUAL: tests=185 failures=0 errors=0 skipped=0` from actual JUnit XML | **PASS 185/185** |
| AndroidTest APK build | `:app:assembleDebugAndroidTest` completed, AndroidTest APK ~1.1 MB placed in Downloads | **BUILD PASS** |
| Real Samsung Android instrumentation | Termux `am instrument -w` runner ended `Time: 20.602`, `OK (37 tests)`, `PASS: 37 Android instrumentation methods on D-114` | **PASS 37/37** |
| Saved Classes | Two saved Classes existed before tests; user confirmed both preserved unchanged afterwards | **MANUAL USER PASS** |
| Sounds | User confirmed Sounds settings unchanged after instrumentation | **MANUAL USER PASS** |
| Frozen source consistency | GitHub comparison of tested dev commit `3e78db0d014ec79f55ad41dd07f25c982a90173b` to development branch on promotion preparation showed subsequent changes only in QA/status documentation, not app source | **SOURCE AUDIT PASS** |

**Combined: 222/222 executed automated tests PASS**. Historical D-110 `OK (14 tests)` is a separate old result, not counted again. The full Android instrumentation output resides on the user's phone at `~/storage/downloads/Atelier-Time-D114-Instrumentation.log`; the terminal screenshot was supplied in conversation. The user-accepted original D-114 UI remains a prior manual acceptance.

## Strict scope and exclusions

- No production Kotlin modifications, no installation of the regular app, no clearing storage, no deletion/replacement of saved Classes or Sounds as part of Stage 2.
- 222/222 test success **does not establish** true multi-hour (approx. 3-hour) runtime under lock/battery optimization, Doze, an actual device clock change ±120 seconds, physical audio quality, Home/Back/Recents/lifecycle behavior in all Android OS conditions, unexpected process reclaim/stale notification behavior or a complete read-only ADB metadata/crash-log survey. These remain explicitly **not tested in Stage 2** and must not be invented as PASS.
- Any remaining follow-up QA happens on the development branch, with explicit permission before user-device-impacting tests. This Stable 6 milestone does not authorize a new APK build/version or rollout.

## Supporting documentation

- [D-114 Stage 2 QA runbook and real results](ANDROID_D114_STAGE2_QA_2026_10_25.md)
- [Master handoff](HANDOFF.md)
- [Test plan](TEST_PLAN.md)
- [Work log](WORK_LOG.md)
- [Stable 5 to QA handoff](HANDOFF_STABLE5_D114_TO_AUTOMATED_QA_2026_10_25.md) (historical; Stage 2 subsequently completed)
