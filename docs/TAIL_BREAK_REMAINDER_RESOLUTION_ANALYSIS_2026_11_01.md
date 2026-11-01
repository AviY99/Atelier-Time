# Atelier Time — terminal-break remainder error: verified analysis and 3-choice UX proposal

**Date:** 2026-11-01. **Status: ANALYSIS / DESIGN PROPOSAL ONLY; NOT APPROVED CODE CHANGE.** Latest user-accepted Android version is **Stable 12 / D-141**, frozen at `stable/android-d141-qa12-final-user-accepted`. No source, app install, tests/builds, versionCode, stored presets or stable branches were modified by this investigation.

## User report / exact case

User sent two screenshots of the Class editor (`testGoogle`) and asked why changing the required **opening-group break from 10 to 5 minutes** results in:

> A break would not leave one full minute for a following pose.

Configuration in screenshots:
- Total Class **180 minutes**, Summary **15 minutes** ⇒ work end `W=165`.
- Opening group: **4 × 1-minute poses**, explicit group break **5 minutes** (the failing case).
- Standard: **25-minute regular poses**, **5-minute regular breaks**, `Session first`.
- One **15-minute special break**, manual replacement of **regular-break ordinal 3**; automatic placement OFF.

The exact frozen code is `android-native/app/src/main/java/com/ateliertime/domain/ScheduleEngine.kt`. Its `build` function, lines 329–356, places an ordinary/special break only if a subsequent pose can have **at least 60 seconds**. At lines 337–340:

```kotlin
if (cursor.toLong() + duration + SECONDS_PER_MINUTE > end) {
    return Attempt(phases,
        "A break would not leave one full minute for a following pose.")
}
```

After 4 opening poses, the 5-minute opening break, and the regular/special segments, **the last complete regular pose runs from minute 139 to minute 164**. Work ends at **minute 165**. The engine expects the next phase to be a regular 5-minute break, but only **R = 1 minute** remains. Thus the rejection is a **terminal-break placement issue**, **not** a 25/5 safety-rest warning and **not** a general ban on 5-minute opening rests.

With the opening rest changed to **6 minutes**, the final pose ends exactly at minute 165 and the entire plan is legal. The working 10-minute-rest screenshot is consistent with a shortened/split final regular pair.

## It is NOT always one minute

For this exact error branch, let `W = totalSeconds − summarySeconds`, `cursor` be the time after the last full regular pose and `B` the duration of the **next attempted break**. Since the branch is entered only while `cursor < W` and rejects if `cursor+B+60 > W`:

```
R = W - cursor           (positive terminal residual)
error iff 0 < R < B + 60 seconds
```

For valid whole-minute initial rules, **R ∈ {1, 2, ..., B_minutes} minutes**. Thus with an attempted **5-minute regular break**, R may be **1–5 minutes**; with a **15-minute special break**, **1–15 minutes**. Other validation branches (opening-group rest, manual replacement slots, or nonminute input) are separate and must not be mistaken for this exact terminal-break case.

The same sample, fixed opening rest **5 minutes**, summary **15 minutes**, special manually at ordinal 3, changing ONLY total Class minutes:

| Class total | Work end | Terminal R | Result |
|---:|---:|---:|---|
| 180 | 165 | 1 | Error |
| 181 | 166 | 2 | Error |
| 182 | 167 | 3 | Error |
| 183 | 168 | 4 | Error |
| 184 | 169 | 5 | Error |
| 185 | 170 | 6 | Valid: 5-minute rest plus a 1-minute following pose |

This table was **analytically traced against the exact frozen `ScheduleEngine.build` branch**, not claimed as an executed Android/JVM test. The user-facing error currently hides `R` and does not offer resolution controls.

## Proposed action dialog — only this exact diagnosed error

When a terminal break is rejected, display a **localized numeric explanation** showing `R`, the attempted break duration `B`, and remaining Class/summary budget, followed by **three explicit user choices**. Do not show this dialog for unrelated schedule errors; do not parse arbitrary English error strings. Add structured issue metadata (error code + remainder seconds, break type and phase IDs / placement) and keep the existing issue text as fallback.

**Choice 1 — Return to Editor with a verified concrete recommendation.**
- For the screenshot example: **increase Opening group 1 → Break after group from 5 to 6 minutes**. Recalculate and show a preview; do NOT automatically alter the form.
- More generally, search candidate minimal changes of opening-break duration / eligible earlier phase lengths, respecting zero-rest, `BREAK_FIRST` shared-slot semantics, special-count/manual ordinals, rest rules and summary budget. Suggest a value **only if a dry-run of the full engine confirms feasibility**; otherwise explain that no safe one-field adjustment exists.
- User may edit freely; previous Save/approval/warning acknowledgement must be invalidated and recalculated.

**Choice 2 — Extend the final regular pose by the remaining `R`.**
- Screenshot example: end pose **25 → 26 minutes** (139–165). Retain total 180, 15-minute Summary and all already scheduled phases/required specials. Omit the attempted terminal break: it has **not begun**.
- **This exceeds the configured nominal regular-pose length (25 minutes)** and may trigger the **25/5 rest safety warning** (`restWarnings` at `ScheduleEngine.kt` lines 377–404). Show the new duration and demand the normal explicit safety acknowledgement if needed. Do not silently remove or override the warning.
- This is **not supported by existing initial `ScheduleEngine.generate`/StoredPreset model**, which currently caps a terminal regular pose at the nominal regular duration. A future accepted implementation needs a persisted explicit tail-resolution choice, deterministic timeline generation, signature/restoration handling and targeted timer/next/replan regression checks. Do NOT implement by merely appending a hidden minute or corrupting the prior preset.
- If it would eliminate a **mandatory manually requested special break** or conflict with any hard rule, disable this choice and send user back to Editor instead.

**Choice 3 — Increase the final Summary by `R` (reallocate time, not extend class end).**
- Screenshot example: Summary **15 → 16 minutes**, work end **165 → 164**; final 25-minute pose ends at 164 and Summary occupies 164–180. Class still ends at **180 minutes**, without inserting a final break.
- After confirmation, reflect the effective Summary value in **Editor rules** and saved preset, never silently display 15 while actually using 16. Rebuild the full timeline; preserve special break count/placement and correct source-of-truth values.
- If the fully recomputed plan invalidates manually specified break ordinals or other constraints, disable option and request Editor correction. For `summarySeconds = 0`, increasing it is an explicit visible user change, not an invisible new Summary.

**Common acceptance contract**
- No automatic resolution; use clearly labelled preview/difference + **Apply** then separate existing end-clock/Class Save approval as applicable.
- Preview must preserve physical total time and summary/pose breakdown, no overlapping/missing phases, all phases positive and contiguous; show rest warnings with existing acknowledgement gate.
- Do not silently change opening groups, manual-special ordinals, automatic-placement policy, notification/audio behavior, saved history or approved existing classes.
- A new saved revision must revalidate on reopen/duplicate, and preserve existing Stable12 serialized signatures/legacy presets. Never rewrite previously accepted classes.
- Add JVM tests for R=1..B (and B+1 boundary), R=1..15 special, 25/5 warning at 26+, no silent mandatory-special deletion, manual/auto placement, `BREAK_FIRST` shared initial rest, 0/positive summary, choice persistence and runtime planner / end-clock behavior. Test both invalid and successfully resolved histories, without running paid GitHub Actions.
- Distinguish this terminal-break case from `addOpeningGroupBreak` insufficient room (`ScheduleEngine.kt` lines 264–280), other invalid inputs and separate time-extension/replan code.
- No release code change or new D/versionCode without a new user approval. Target future development iteration after approval, not Stable12 source mutation.

## Immediate non-code workaround for this user example

Existing Stable12 Editor permits **opening group break 5→6 minutes**, *or* **Summary 15→16 minutes** with the rest unchanged. Both yield a valid schedule on the examined input and keep the complete Class at **180 minutes**. Last-pose extension **25→26** is not supported without new code and safety consent.

**Decision requested next:** Should the full 3-choice terminal-remainder dialog be implemented in a new development iteration with the above guards, leaving frozen Stable12 intact?

Source: [Frozen ScheduleEngine](https://github.com/AviY99/Atelier-Time/blob/stable/android-d141-qa12-final-user-accepted/android-native/app/src/main/java/com/ateliertime/domain/ScheduleEngine.kt).
