# D-090 — About copy/typography correction + proposed softer Night backgrounds
Date: 2026-10-24

## User intent and approval boundary

The user clarified that Atelier Time is **not primarily a class planning application**. Its main purpose is a **live timer for an art atelier**, reflecting the current state of a class and easing day-to-day class operation, including sessions, breaks, time remaining and sound cues. Correct About wording accordingly. Also requested: prominent enlarged numeric Version, app name and developer credit **on the same single line and slightly smaller than Version**, all remaining text one typographic hierarchy lower. No need to modify domain schedule functionality; this is About descriptive copy only.

**Committed to Android development source, phone Gradle/build/QA still pending for D-090:** `AtelierDesign.kt` About panel now uses `headlineMedium` for `Version <numeric installed version>` (actual numeric PackageManager extraction preserved), single-line width-fitted `Atlier Time · Developed by Avi Yosfan with Open AI` via Material3 TextAutoSize (10–14sp), then smaller `© 2026` in bodySmall and a concise English description that accurately describes a live studio timer, real-time class progress, sessions, breaks, time remaining and timely sound alerts, without claiming the primary role is planning. The app title spelling remains exactly **Atlier Time** as user requested, without silently changing the Android applicationId or product/project identity. Other Android code/time/audio/tab order unchanged.

## Night background-only refinement — PROPOSAL, NOT APPROVED, NOT APPLIED

The user rejected changing button colors or accent color meanings and requests a gentler night feel **only in backgrounds**. Existing Night palette accent colors and other role values remain unchanged: primary `#83BCFF`, lavender secondary `#C5B1FF`, solid tahini `#D5BD94`, error and warning colors, selected tabs/buttons, text `#F5F8FF`, outline `#8493A6`, etc. Day palette unchanged.

Compare current vs proposed only for background roles:

| Surface role | Current Night | Proposed Night Mist |
|---|---|---|
| App canvas / background | `#0E1624` | `#283746` |
| Normal card / surface | `#1D2B3D` | `#344759` |
| Secondary/sunken surface / surfaceVariant | `#26384C` | `#405569` |

The proposed shades are still desaturated blue-gray night colors, lighter than the previous unapproved Night Soft suggestion `#1B2636` canvas and `#29384B` card. No button/container/selected-tab accent changes: the user explicitly asked for backgrounds only. Overlay and tahini bar stay one identical hue and continue fill Session/drain Break. No gradients, flashes, or timer number animations.

**Approval required** before applying Night Mist palette to code. The current Night color values remain in `AtelierDesign.kt`, and no changed Night palette was built or installed. A single future phone Gradle/QA pass after approval can validate About + Night background colors together.

## QA / documentation gates

- D-088 is user-confirmed working after real Gradle 38s PASS. D-089 fixed five visible LTR tabs in source + English About, but is not yet phone Gradle-tested or device QA-accepted.
- D-090 changes only About UI copy/type, plus proposes (without implementing) Night Mist backgrounds. Confirm on the phone after a new build: five tabs Editor/Timer/Sound/Design/About visible without scrolling; About actual Version large, name/developer on one line without clipping, copyright and accurate smaller English description, legible on narrow display and in Day/Night; saved Classes and live timer unchanged.
- Night Mist is *not* a new accepted theme until user approval, source implementation, and separate actual device acceptance.
- Stable accepted D-086 snapshot branch `stable/android-d086` remains immutable, and D-088 is user-accepted working. PR #3 remains Draft/unmerged, no paid GitHub Actions.
