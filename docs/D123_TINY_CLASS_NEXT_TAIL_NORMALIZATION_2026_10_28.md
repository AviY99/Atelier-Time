# D-123 — Normalize saved Class `test` after early and late Next (development candidate)

**Status: SOURCE CHANGES COMMITTED; BUILD AND DEVICE QA NOT YET RUN. Do not install before a passing QA gate and explicit device approval.**

## User's precise observation

The locally saved Class named `test` has correct countdown arithmetic and an unchanged physical Class end. The defect is that early consecutive **Next** actions, and especially continuing through the last planned Break, progressively accumulate the recovered time into **one oversized Session** instead of recovering regular Session / Break cycles. The defect is particularly exposed when normal Sessions are very short compared with Breaks.

The defect is **structural reflow**, not time-budget accounting. Do not change the physical Class deadline, signed correction, the immutable saved plan, or the 25/5 warning.

## Test fixture from Samsung screenshots

- Name `test`, total 6 min (360 s), Summary 1 min (60 s).
- Opening group: one 60-second Session and one required 60-second break.
- Regular Session 60 s, ordinary Break 60 s, cycle Break first.
- One automatic Special Break 60 s (`L=B`); at runtime it counts as an ordinary-length rest, not a protected genuine long Special.
- Saved plan: 0–1 Session 1; 1–2 Special Break #1; 2–3 Session 2; 3–4 ordinary Break #2; 4–5 Session 3; 5–6 Summary.
- The Class should keep precisely its original end and fixed Summary after *every* successful Next/Restart.
- If credits are too small to permit another complete regular Session + Break **and another Session of at least 60 s**, an extended terminal Session is temporarily inevitable, not by itself a bug. As soon as the budget permits a cycle, split it. For `P=60s,B=60s` this means terminal work >= 180s; for `P=60s,B=180s` it means terminal work >= 300s.

## Root-cause review

- `FinalPairCompensation.adjust()` can expand the original last regular Session, because its more conservative insertion test reserves an additional full final pair and intervening Break.
- `adjustFutureTail()` assumes the original final pair remains the end and may be infeasible with a protected Next target.
- `adjustLastOnly()` can preserve the locked past and clock by extending only the last regular Session, **but does not split an oversized last Session back into regular cycles**.
- It is not safe to simply always rebuild all phases from the saved Class: doing so would erase observed history and break D-122's frozen special-break/inserted-block invariants.

## Isolated D-123 implementation

- `FinalPairCompensation.splitExtendedTerminalPoseAfterNext()` runs **only after successful `reflow` in `Next`**. It sees the selected, budget-validated future layout, finds the last regular work Session, then splits its time into:
  - full regular work Session `P`,
  - ordinary Break `B`,
  - repeat while enough time remains for a final Session of at least 60 seconds.
- During the split, retain the original terminal work phase ID and start for the first split Session (especially when it is the newly entered Next target); subsequent work/break segments use unique `runtime-tail-*` identifiers. Preserve all earlier phases, the entire completed prefix, genuine Special Breaks and Summary length. Total virtual duration, correction and physical deadline are **not changed**.
- End-of-Class display uses the actual last two runtime regular Sessions, even if one is newly inserted.
- Restart's final-Session guard checks the **actual** final regular Session, not merely the last pose ID in the originally approved Class, so a formerly last-but-now-split Session can restart as long as future recovery remains safe.
- The existing D-122 `preserveExistingTail` guard and last-only recovery remain. No change to saved Classes, Audio, or Android lifecycle.

## Added automated regression cases

`D123TinyClassLateNextSplitTest` adds **five** test methods:
1. Four early Next events through the last planned Break create regular future Sessions+Breaks instead of one giant Session, preserving exact past and Summary.
2. More Next events through newly inserted tail Breaks re-split an oversized future work Session while freezing observed prefix.
3. Restart a newly nonterminal work Session, recover only from future time, preserve Class finish.
4. Without any Next credit, attempting Restart with two minimum-length final Sessions must be rejected atomically.
5. Repeat with 60-second Sessions and **180-second Breaks**; require splitting already on the earlier Next that reaches the last planned Break, before consuming that Break, once `P+B+60s` fits.

Existing expected JVM count: **228 + 5 = 233**; this is a *count expectation*, **not a measured result**. All old D-122 tests must continue to pass.

## Verification gates

- On Samsung Code on the Go, after `git pull --ff-only` on `android-native-kotlin-compose`, run `bash tools/build_d123_candidate_on_phone.sh`.
- The script is **build only**: reruns JVM tests, assembles debug app and Android instrumentation test APK, writes the QA manifest with `version=D-123, code=18` and APK SHA-256, and explicitly **does not install**.
- Inspect actual test count, failures, errors and skips (expect 233, 0, 0, 0); if anything fails, debug it **without deleting/weaking tests**.
- Only then verify downloaded APK hash against manifest, separately obtain installation approval, install as update without uninstall/clear data, and manually test saved `test` and the older three-hour Class.
- Manual QA: pressing Next early and repeatedly; after last programmed Break, ensure recovered time reappears as repeated work/rest cycles whenever feasible; Restart on split work and Break; one-minute minimum; locked observed segments; equal-length special remains ordinary at runtime; correct cue per transition; all hard physical deadlines unchanged; saved Classes and Sounds intact.
- D-119 `stable/android-d119-qa7` is **frozen** and must not be modified.

## Limitations and review findings

Source-only analysis predicts the mismatch and plausible repair. The exact original "strange" trace was not supplied, so the added deterministic fixture represents the user's saved Class and description. Neither local Gradle/Android build nor Samsung manual reproduction has been executed for D-123 yet. Any tests passing will establish logical invariants, but only on-phone UI testing can accept behavior.
