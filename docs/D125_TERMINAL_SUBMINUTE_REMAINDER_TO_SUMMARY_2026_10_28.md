# D-125 — Terminal sub-minute remainder to Summary (user-requested rule, design only)

**2026-10-28. FINAL STATUS: CLOSED / NOT ADOPTED BY USER. NO IMPLEMENTATION.** D-124 installed and user has reported broad manual improvement; Stable 7 remains frozen. Do not mark this feature implemented or tested merely because its design is recorded.

## Requested change — only at the far end of the mutable future

Retain a **60-second minimum for every actual work Session**, including the last. Only when a successful **Next** action re-plans the **final unstarted runtime tail**, and full regular Session `P` + regular Break `B` block(s) would otherwise leave a **strictly positive final work remainder of 1–59 seconds**, append that remainder to the *runtime Summary* instead of generating an illegal sub-minute work Session or leaving all surplus in a giant terminal Session.

Do not perform this during Editor generation or saved Class approval, Run end-time selection before starting, any observed/started phase, or arbitrary mid-Class intermediate residue. The approved Class template and all other existing Sessions/Breaks must remain unchanged. No new artificial waiting phase. If there is no configured Summary, the requested transfer has no existing target and requires separate policy before enabling.

**Specific existing case:** `test` uses `P=60s`, `B=60s`, approved Summary `60s`. A 165s unstarted work tail can be represented as `60s work Session + 60s regular Break + Summary(60s+45s) = 225s total including original Summary` rather than `165s work Session + Summary(60s)`. Physical Class end is identical. This leaves a regular Break immediately before Summary (no intervening work Session), which must be treated as an explicit *end-of-tail exception*, not an unnoticed change to all Editor cycles.

**Important invariant change:** D-120 approved **fixed Summary duration**. This feature expressly relaxes that invariant for a runtime-only tail transfer of **up to 59 seconds**, without changing Summary in the saved Class or moving the hard physical finish. The initial Summary duration remains a minimum. Any already-started Summary is immutable. The current source `ConstraintFuturePlanner.validate` requires exact equality and `allocateLast` requires `P+B+60` for a new cycle: both would require a controlled generic change, not a per-Class exception.

**Cues and completion:** Audio cue C must still be emitted once at entry to Summary, including when the immediately preceding phase is a Break. Natural transitions, paused/resumed states and Next at the terminal Break must not double-emit A/B/C, prematurely finish the Class, or change the deadline. If Next tries to jump into Summary before the allowed boundary, reject atomically. This can change the wall-clock moment at which C plays (earlier by up to 59s versus the old fixed-Summary plan), but not the physical Class finish.

**General feasibility:** Prefer legal full Session/Break/Session patterns (last Session >=60s) over transferring a remainder; move the 1–59s remainder to Summary only when a full cycle otherwise leaves an illegal terminal work segment. Never transfer an intermediate leftover from an earlier phase; never exceed the specified <60-second transfer for a single reflow. Define how to re-account previously accrued runtime Summary extension on repeated Next and preserve it across Restart/Resume without doubling it.

## Engineering gate (not yet performed)

Implement as D-125 development candidate with versionCode increment, preserving all D-124 tests. Add property tests spanning P/B ratios, 1..59 second remainder, exact 0 and 60 boundaries, no Summary, many Next, terminal Break immediately before Summary, transitions and C exactly once, Pause/Resume/Restart with prior Summary extension, immutable observed history and original physical deadline. Test existing 3-hour and genuine-special-break controls. Require full actual Samsung JVM/Android build and **separate approval** before update-install. No modifications to frozen Stable 7 or installed D-124 without approval.

## CLOSED — USER REJECTED THE PROPOSED RULE (2026-10-28)

**Authoritative decision:** The user explicitly rejected D-125 as unnecessary. The observed long final Session is an extreme condition of repeating regular Sessions and Breaks at their one-minute minimum, and real-life resolution is deliberately left to the user's situational judgement, with multiple possible choices. Do **not** implement automatic transfer of a sub-minute tail remainder into Summary. Do **not** extend runtime Summary or weaken the 60-second minimum for any Session. Preserve the current D-124 behavior, exact physical Class finish, existing Editor/Summary and saved Classes. No D-125 source patch, APK build, or installation was made. D-125 is **CLOSED / NOT ADOPTED**, not pending engineering work.

The user considers the currently installed D-124 **good at this stage**; this is positive device feedback, **not** an explicit designation of a new numbered stable checkpoint. Frozen Stable 7 remains unchanged.
