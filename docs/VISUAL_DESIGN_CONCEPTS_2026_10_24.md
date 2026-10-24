# Atelier Time — three proposed visual languages and Timer motion concepts (2026-10-24)

**STATUS: DESIGN PROPOSALS ONLY — NOT APPROVED, NOT IMPLEMENTED.** Stable operational Android baseline remains [Android Stable 1 / D-086](ANDROID_STABLE_BASELINE_D086_2026_10_24.md), native snapshot `stable/android-d086`. Any chosen direction must be implemented independently on the development branch with device/build/UX QA. This document does not change existing timer rules, Class lengths, storage, cue timing or default app theme.

## Shared design contract (all three)

- Keep Clock, fixed Class start/end, Class length and Class elapsed separate from current Session/Break countdown. No visual animation can alter the real Class elapsed, fixed Class deadline or trigger sound cues.
- Respect Session and Break as two distinct states. A running Session may VISUALLY fill a surface as time passes; an active Break may VISUALLY drain its surface as break time passes. This is presentation only.
- Ensure actual start/end hh:mm:ss in the active phase card remain fixed-width stable, do not animate position or font size. Only the LARGE remaining-time digits, progress indicator, card background and passive border can animate.
- Countdown animation follows a monotonic timer fraction `progress = clamp((phase.durationSeconds - remainingSeconds) / phase.durationSeconds, 0, 1)`; a visual fill is a DERIVED state. Pause freezes motion; Next jumps directly to the next phase with a quick crossfade, not a fictitious rapid countdown.
- Keep Pause/Resume, Restart current session/break, Next, Exit visibly distinct with clear Help, legible narrow-phone widths; preset library per-card Run/Edit and Duplicate/Rename/Delete remain.
- Offer a visible **Motion: On / Reduced / Off** control; respect system animation-scale and accessibility preferences. Never rapid-blink or strobe; use steady tint, gentle breathing at most once every 1.5–2s, never more than 3 flashes/sec; prefer NO flashing at all. Use stable contrast and text size under dynamic font scaling / TalkBack.
- Use Jetpack Compose `animate*AsState`, `AnimatedContent`, `updateTransition`, `Animatable` or `rememberInfiniteTransition` judiciously; progress should be animated toward authoritative timer state, never make the animation its own timer. Source: Android official Compose animation guide https://developer.android.com/develop/ui/compose/animation/choose-api .
- Each design includes controlled urgent-state gradient progression (e.g. final 20% / final 15–30 seconds) and slow non-flashing end warning. Audio cues continue independently.

## Concept A — Studio Linen / Calm Atelier

**Aesthetic:** quiet art studio, paper/tile materials, restrained editorial type. Best for long workshops and sustained attention.

| Role | Hex |
| --- | --- |
| Canvas / chalk | `#F7F4ED` |
| Card | `#FFFEF9` |
| Text / ink | `#29312D` |
| Session / clay | `#B86B53` |
| Break / sage | `#6F9C8B` |
| Warm warning / ochre | `#D8A653` |
| Border / linen | `#DCD4C8` |

- Typography: **Heebo** for Hebrew & UI, **DM Sans** for Latin when available, tabular monospaced **Roboto Mono** digits for the Timer. Lightweight headings / normal weight labels, clock numbers ~52–60sp, no jumping.
- Buttons: 12–14dp soft rounded-rectangle primary clay session; neutral tonal Pause/Restart; outlined Next; distinct desaturated red Exit. Focus/pressed state uses thin 2dp outline rather than scale.
- Tabs: editorial tabs, shallow cream backgrounds and a 3dp bottom line on the active tab.
- Cards/frames: warm ivory 1dp borders, 16dp corners, quiet layered paper, little or no elevation.
- Motion: remaining seconds lightly crossfade 120–160ms (not all digits); **Session** pigment surface fills upward slowly from lower-left, horizontal line slowly **fills**; **Break** sage surface drains downward and bar **empties**. Final 20% clay→ochre→burnt orange, 2s warm border breathe only in last 15s. No flashing.

## Concept B — Night Focus / Performance Studio

**Aesthetic:** precise, dark high-contrast stage dashboard.

| Role | Hex |
| --- | --- |
| App background | `#0E1624` |
| Raised surface | `#1D2B3D` |
| Text | `#F5F8FF` |
| Session / electric blue | `#4B9FFF` |
| Break / lavender | `#B59AFF` |
| Warning / amber | `#F8C76D` |
| Outline / slate | `#39495D` |

- Typography: **Heebo** for Hebrew UI, **Space Grotesk** or **IBM Plex Sans** for Latin and **IBM Plex Mono** tabular clock digits. Strong numerical focus ~58–64sp and quiet supporting metadata.
- Buttons: slightly squared 8–10dp buttons; high contrast filled Pause/Resume and Next, outlined Restart, separate warning-color Exit.
- Tabs: full-width segmented navigation strip, active filled surface, 2dp colored indicator.
- Cards/frames: 1dp illuminated outline, 14dp corners, dark raised layers, faint nonessential edge glow.
- Motion: 100–140ms number opacity/translation, no flip on precise start/end time; **Session** segmented blue bar **fills** across as small blocks illuminate, subtle dark-blue pane fills bottom-up. **Break** lavender bar **empties** segment by segment and panel shadow withdraws top-down. At final 20% blue/lavender→amber→coral soft gradient (nonflashing); below 15s border slowly brightens and returns over 2s.

## Concept C — Flow Atelier / Contemporary Kinetic

**Aesthetic:** modern energetic motion graphics and soft luminous surfaces.

| Role | Hex |
| --- | --- |
| Mist canvas | `#F4F2FC` |
| White card | `#FFFFFF` |
| Ink | `#27243D` |
| Session / iris | `#8156D9` |
| Break / teal | `#23B5AA` |
| Ending gradient / apricot | `#FFB46E` |
| Ending gradient / coral | `#F07088` |
| Border | `#DED8F0` |

- Typography: **Assistant** / **Heebo** Hebrew UI; **Outfit** numerals, numerals with tabular spacing, large ~56–62sp. Friendly bold labels, soft variable weights, strong hierarchy.
- Buttons: 22–26dp capsule primary/tonal, filled active tabs in a floating segmented bar, pill indicators and soft 18–22dp card corners.
- Motion: remaining seconds slide/roll up only within a clipped fixed-width digit cell (140–180ms). Never move physical Start/End. **Session** vertical translucent violet-to-rose gradient **fills upward** behind timer with a smooth liquid-like top edge; **Break** aqua field **empties** from top to bottom, revealing a bright white base. Companion curved gradient progress bar tracks phase time. Final 20% lavender→apricot→coral; last 10–15s slow halo pulse 1.8–2.2s without flashing.

## Decision / implementation sequence

1. User chooses A, B, C, or mix-and-match (e.g. A typography with C fill animation). Record approval as next decision; no claim that one is already accepted.
2. Build a single reusable `AtelierTheme` token layer: semantic colors `session`, `break`, `warning`, `surface`, `text`, `outline`; Compose typography, button/tab/card styles; no edits to pure Kotlin core.
3. Build visual Timer animations with a **single authoritative monotonic timer progress source**, pause/next/restart transitions and static degraded/reduced-motion fallbacks. Avoid continuous high-frame animations under screen-off/background to protect battery. Audio timing separate from graphics.
4. Validate with screenshots on small Android phone, 1.3×+ system font, light/dark preference if supported, contrast accessibility, no flashing, smooth performance, stable End clocks and fixed Class elapsed/end.
5. Preserve `stable/android-d086` immutable; only promote a later user-accepted validated visual release by explicit confirmation.

Official Compose animation API guidance: https://developer.android.com/develop/ui/compose/animation/choose-api
W3C physical reactions guideline: https://www.w3.org/WAI/WCAG22/Understanding/seizures-and-physical-reactions.html
