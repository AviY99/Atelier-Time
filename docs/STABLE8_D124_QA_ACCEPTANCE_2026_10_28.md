# Stable 8 — D-124 unified Timer engine: user acceptance and freeze certificate

**Acceptance date:** 2026-10-28.
**Authoritative user declaration:** "מבחינתי ,זאת כעת גרסה 8 יציבה שבמרכזה איחוד סופי של לוגיקת טיימר" — the user explicitly designates the installed, working D-124 app **Stable 8**, with unified Timer logic as the release centerpiece.
**Stable checkpoint branch:** `stable/android-d124-qa8` in `AviY99/Atelier-Time`; immutable after creation. Continue future changes only in `android-native-kotlin-compose`.
**Android package identity:** `versionName = "D-124"`, `versionCode = 19`. Stable 8 is a *QA/release checkpoint ordinal*, not another Android APK, versionCode bump, separate app, or Play Store release.
**Built / tested source commit (from APK SHA manifest):** `09c63d42d5b7ae3ddc13b8755a2262b7b0e5c9a7`. The compare from this commit to the development tip immediately before preparing this certificate showed seven later commits affecting **only** `docs/D124_UNIFIED_CONSTRAINT_FUTURE_PLANNER_2026_10_28.md` and `docs/D125_TERMINAL_SUBMINUTE_REMAINDER_TO_SUMMARY_2026_10_28.md`, no application, test or build code. The Stable 8 branch is created from the documented development snapshot containing exactly this tested code plus documentation of acceptance and rejected ideas.

## Central release achievement: final unification of runtime scheduling

- Runtime calls to `FinalPairCompensation.reflow` route into the same `ConstraintFuturePlanner.plan` for starting an adjusted Class and the `Next`, `Restart`, `Pause/Resume` transition contracts. This replaces the D-120/D-121 action-dependent scheduling dispatch; legacy `adjust`, `adjustFutureTail` and `adjustLastOnly` remain as compatibility/test helper functions, **not independent runtime allocation dispatch paths**.
- The planner uses actual unstarted future and protected observed prefix, physical remaining-time budget, approved final distribution, minimum 60-second work Sessions, ordinary/special Break constraints, fixed Summary, fixed physical Class deadline, and atomic rejection when constraints cannot be satisfied.
- `Next` can synthesize complete future regular Session+Break cycles instead of letting all credit become an arbitrarily long final Session. Restart/Resume preserve the structure already created. Genuine `L>B` specials and approved manual placements remain protected; `L<=B` receives the approved ordinary-runtime treatment without modifying saved configurations.
- The known 6-minute saved Class `test` with 1-minute Sessions/Breaks produced a much improved manually observed run after repeated Next; multiple added Session+Break cycles appeared. A 2:45 final Session is **correct** where another `1:00 Session + 1:00 Break` would leave only a forbidden 0:45 last Session. User explicitly accepts this extreme edge case and chooses to manage it situationally, not by a new automatic rule.
- The suggested `D-125` transfer of a <60-second terminal residue to Summary is **REJECTED / CLOSED**; no D-125 production source or install. Summary remains fixed; the last Session retains a >=60s minimum.

## Evidence from Samsung and acceptance

1. First D-124 build-only attempt ran 243 JVM tests with four new-fixture failures. The fixture construction was repaired without weakening or suppressing tests; this historical run is not treated as a PASS.
2. **Successful user-supplied Samsung Code on the Go result:** `BUILD SUCCESSFUL in 1m 36s`, **72/72 Gradle tasks**, `D-124 JVM ACTUAL: tests=243 failures=0 errors=0 skipped=0`. Both regular and AndroidTest APKs assembled. AndroidTest *assembly is not execution*.
3. User executed the SHA-256 check against `/storage/emulated/0/Download/Atelier-Time-D124-Candidate.apk` and `Atelier-Time-D124-QA-manifest.txt`; terminal reported **`D-124 APK VERIFIED - PASS`**. Manifest: `version=D-124`, `code=19`, `branch=android-native-kotlin-compose`, `commit=09c63d42d5b7ae3ddc13b8755a2262b7b0e5c9a7`.
4. User explicitly authorized an in-place update preserving Saved Classes/Sounds, then supplied screenshots from the running D-124 Timer and stated "בעקרון הכל עבד", including the improved `test` scenario. User subsequently explicitly declared **Stable 8**.
5. The locally saved Class `test` is shown running after the update. Do not infer from this alone a full byte-for-byte preservation audit of every Saved Class or Sound selection; no separate exhaustive verification was reported.

## Evidence boundaries and known monitoring

- **PASS:** genuine Android/Gradle compilation and APK assembly, **243/243 JVM**, candidate SHA-256 equality, installed-device operation observed by user and explicit user designation Stable 8.
- **NOT CLAIMED:** connected Android instrumentation execution, formal model checking of all possible scheduling trajectories, overnight/Doze/lock-screen endurance for D-124, independent device-exact persistence audit of every Saved Class/Sound, production distribution signing, or universal proof of defect-free timer behavior.
- **Open monitoring:** the earlier single Poweramp last-song audio anomaly (`QA-D122-05`) was not reproduced after Force Stop or normal close; it remains intermittent/monitoring, not a reason to relabel the current timer engine as failed or claim a sound fix.
- **No release-time app mutation:** Stable 8 creation is GitHub documentation and an immutable source checkpoint only. No new APK, install, uninstall, `pm clear`, sound or saved Class change is requested or executed.

## Future development policy

Do not modify/merge/force-push `stable/android-d124-qa8`; it is the reproducible Stable 8 checkpoint. Keep prior `stable/android-d119-qa7` (Stable 7), Stable 6 and Stable 5 intact. Develop future changes on `android-native-kotlin-compose` as a **new D-number and Android versionCode > 19**, preserve the current runtime rules unless a new product decision is approved, require source tests plus Samsung build/APK hash and explicit installation consent for future releases. Never treat the rejected D-125 rule as pending.

**Review references:** [D-124 build and device QA](D124_UNIFIED_CONSTRAINT_FUTURE_PLANNER_2026_10_28.md), [D-125 rejected decision](D125_TERMINAL_SUBMINUTE_REMAINDER_TO_SUMMARY_2026_10_28.md), [D-120 contract](D120_TIMER_LOGIC_UNIFICATION_DECISIONS_2026_10_27.md), [Stable 7](STABLE7_D119_QA_ACCEPTANCE_2026_10_27.md).
