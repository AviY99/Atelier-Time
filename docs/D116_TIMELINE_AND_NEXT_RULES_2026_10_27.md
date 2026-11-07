# Atelier Time — D-116 live timeline / Next-credit reflow

**Date:** 2026-10-27  
**Branch:** `android-native-kotlin-compose` only.  
**Status:** development candidate in source, Android Gradle/Compose build and on-device QA pending. The user's installed D-115 and frozen Stable 5/Stable 6 refs are not changed by this code.

## User requirements (latest clarification supersedes previous ambiguity)

1. `Class timeline at a glance` is an **actual history + recomputed future**, not a copy of the virtual countdown cursor. If a planned five-minute Session ends by Next after one minute, the historical colored fragment must be **one minute**; the rest of the chart must reflect the revised future. Restart retains the time spent on the prior attempt and begins a fresh current attempt. Paused time is represented separately.
2. Both **Class elapsed** and **Class remaining** live **inside** this chart, not in the top Class header. The physical progress overlay is drawn ON the chronological planning bar, leaving only one progress bar. Hide `Time by activity` on the live Timer; retain it in Editor's static preview.
3. Give the Free Countdown a distinct visible **Free** tab, preserving its state across tab navigation. Six tabs in LTR order: Editor / Timer / Free / Sound / Design / About.
4. In Sounds, **Same sound for all** means all Class A/B/C cues **and** Free Countdown sound. Each cue's existing duration limits remain; the Free countdown keeps its independent 5–60-second length. Switching to Separate restores the prior Free sound selection.
5. Only **after Next** and only when the **combined work duration** of the last two regular Sessions is **strictly greater** than `2 × regular Session + regular Break`, insert one full regular Session and regular Break **before** the adjustable final pair. Deduct their full durations from the available future work budget, then reapply the user's existing final-two split choice to the remainder. Repeat while the *strict* threshold still holds. Equality means NO insertion. Previously entered/completed phases never change.

## Short unified runtime rules (proposed product wording)

1. **Past is fact:** retain the actual chronological time already performed (including each Restart attempt); Next does not paint unworked time as completed.
2. **Future is recalculated:** keep the approved physical Class end and Summary fixed. Attribute positive/negative time differences only to adjustable future work. Reject changes that require rewriting past or a future Session shorter than one minute.
3. **Normalize the ending:** first merge a short final pair plus intervening Break if they fit within one regular Session. Otherwise, after Next, repeatedly create a full `Regular Session + Regular Break` whenever the remaining final two work durations exceed `2 × Regular Session + Regular Break`; distribute the final pair according to the saved Remainder/Even/Custom mode.

This expresses merging, extension, compensation and newly available complete regular blocks with **one invariant (fixed Class end), one protected boundary (performed past) and one final-tail normalizer**. The actual planned phases remain runtime-only; approved saved Classes/Sounds are not rewritten.

## Implementation and tests

- Pure `RuntimeTimeline` tracks sealed observed fragments plus live current/future, with a separate paused-time slice. It is projected from monotonic clock data. Chart uses it to draw actual history proportionally and future after current.
- `FinalPairCompensation.adjust(..., allowExtraRegular)` computes the strict threshold only following Next and can add multiple new runtime phases with deterministic IDs. `adjustLastOnly` protects observed dynamic suffixes.
- Free tab, shared sound policy, UI text and isolated Android preference/Compose tests updated. No APK installed or device process touched by this changeset.
- Local pure Kotlin/JUnit regression now includes threshold equality/strictness, repeated full insertion, history after Next and Restart, Next and Restart in a generated block, and Shared/Separate sound behavior.
- **Local 205/205 JVM PASS** (21 classes, JUnit4) recorded only after actual execution. **Android Gradle/Compose compilation and instrumentation PENDING**, not to be inferred from the prior D-115 `196/196` phone Gradle evidence.

## Next safe stage — Code on the Go (build only)

```bash
cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native
git switch android-native-kotlin-compose
git pull --ff-only
bash tools/build_d116_candidate_on_phone.sh
```

Do not install regular APK, test APK, clear data or run instrumentation without separate approval after actual Gradle build evidence. Existing two saved Classes/Sounds should be verified before and after any separately authorized update. `tools/termux_run_d116_qa.sh` requires a later device gate; the D-115 QA scripts are retained unchanged.
