# ASO-01 — Proposed Google Play store listing (Hebrew + English) — 2026-10-30

> **APPROVED DIRECTION; DRAFT PREPARED; NOT PUBLISHED.** User approved all ASO recommendations and requested the entire original question and discussion recorded in [ASO-01 decision & original user queries](ASO01_DISCOVERABILITY_REQUIREMENTS_AND_USER_APPROVAL_2026_10_30.md). The text below is suggested store copy, not evidence of Google search indexing or store availability.

## Field limits and counts

| Field | Max | he-IL | en-US |
|---|---:|---:|---:|
| App name | 30 | 28 | 30 |
| Short description | 80 | 67 | 73 |
| Full description | 4,000 | 892 | 1191 |

These measured character counts pass; re-check the Play Console field counters at submission. Store localization does not require or imply changing Android in-app UI language, versionName or package ID.

## Hebrew — he-IL

### App name
```text
Atelier Time - טיימר לסטודיו
```

### Short description
```text
שעון וניהול זמן לאטלייר, לרישום מודל, ציור ופיסול עם סשנים והפסקות.
```

### Full description
```text
Atelier Time הוא שעון וטיימר לסטודיו לאמנות, לאטלייר ולמנחי שיעורי רישום מודל, ציור ופיסול. האפליקציה מסייעת בניהול זמן של סשן אמנות, שיעור עבודה מול מודל חי וסדנת יצירה.

תכננו שיעור לפי זמני עבודה והפסקות, עקבו אחר לוח הזמנים והציגו למשתתפים כמה זמן נותר. אפשר להגדיר סשנים של ציור ורישום, לשלב הפסקות קצרות וארוכות ולנהל את רצף השיעור לפי הצורך.

מה אפשר לעשות ב־Atelier Time?
• לתכנן Class הכולל מקטעי עבודה (Sessions) והפסקות (Breaks).
• להפעיל טיימר לשיעור ולהציג שעון, זמן שנותר והתקדמות.
• להשתמש בטיימר חופשי (Free Timer) לפי הצורך.
• לקבל סימוני צליל למעברים בין שלבים.
• לשמור ולנהל שיעורים מוכנים לשימוש חוזר.
• להתאים את תצוגת האפליקציה למצב יום או לילה.

הכלי מתאים למורים ולאמנים שעובדים בסטודיו, לאטלייה, בשיעורי רישום וציור ובסשנים של פיסול ומידול אמנותי פיזי. במקום לעקוב ידנית אחר כל תנוחה והפסקה, אפשר להתמקד בשיעור וביצירה.

ניהול הזמנים פועל במכשיר גם ללא חיבור אינטרנט.
```

## English — en-US

### App name
```text
Atelier Time: Art Studio Timer
```

### Short description
```text
Pose and break timer for drawing, painting and sculpture studio sessions.
```

### Full description
```text
Atelier Time is an art studio clock and timer for instructors, artists and ateliers. Plan and manage drawing, painting and sculpture sessions, including timed poses and breaks for live-model and figure-drawing classes.

Organize studio time around work sessions, rest periods and a planned class finish. The running timer shows the current phase, remaining time and class progress, helping artists focus on making art rather than watching the clock.

What you can do with Atelier Time:
• Build a class schedule with configurable work sessions and breaks.
• Run a visual class timer with a clock, countdown and progress display.
• Use a separate Free Timer when you do not need a prepared class.
• Hear sound cues at the relevant timer transitions.
• Save and reuse class plans and settings.
• Switch between Day and Night display modes.

Made for art studios and ateliers, the timer supports live-model posing, drawing sessions, painting lessons and physical sculpture or modeling workshops. It is designed for practical time management during art teaching and studio work—not for digital 3D modeling.

Core timing and saved classes work locally on the device without an internet connection.
```

## Coverage intent and accuracy

| Query family | Natural draft references | Caution |
|---|---|---|
| Clock / שעון | שעון, art studio clock, clock | Not an alarm clock product; no guarantee on very generic "Clock" |
| Timer / טיימר | טיימר, art studio timer, class timer, Free Timer | Do not claim generic stopwatch features |
| Time Management / ניהול זמן | ניהול זמן, studio time, time management | Class scheduling, not time-tracking analytics |
| Studio / Atelier / אטלייר / אטלייה | סטודיו לאמנות, לאטלייר, לאטלייה, studio, atelier | Both Hebrew variants supported naturally |
| Model / מודל | מודל חי, רישום מודל, live-model, figure drawing, timed poses | Artistic live model, not digital model or fashion model |
| Drawing / Painting / רישום / ציור | רישום, ציור, drawing, painting | Art lessons and classes |
| Modeling / Sculpture / מידול / פיסול | מידול אמנותי פיזי, פיסול, sculpture or modeling | Physical sculpting; NOT CAD/3D modeling |
| Art Session / סשן אומנות | סשן אמנות, סשנים, drawing sessions | Work sessions and breaks |

The exact **96 originally requested query lines**, including **81 distinct normalized expressions** and duplicates, are preserved in the [ASO-01 source record](ASO01_DISCOVERABILITY_REQUIREMENTS_AND_USER_APPROVAL_2026_10_30.md). The official listing should NOT paste those lists as repeated keywords.

## Store action / QA sequence — not performed yet

1. Choose primary store locale in Play Console and apply reviewed translations to `he-IL` and `en-US`; retain title/short/full field limit verification.
2. Select relevant category and tags from the ACTUAL Play Console options; do not invent a special keyword field.
3. Use genuine current D-130 screenshots/icon and, if required, feature graphic showing Editor, Programed Timer, Free Timer, Sound and session/break timing. Never advertise unimplemented features or desktop ports as shipped.
4. Re-check release privacy policy link, Data safety, FGS declaration/video, publisher contact and signed AAB in their separate release gates.
5. Review all translations against Google Play Metadata policy; do not repeat or mechanically stuff phrases, claim search ranks, or describe the application as digital 3D modeling software.
6. After public indexing, run a manual query matrix for high-priority Hebrew/English expressions with explicit locale, country, date, observed result and optional rank. Avoid asserting global rankings from a single device.
7. Inspect Play Console Store performance / Conversion analysis for clicks, CTR and visitors segmented by source, locale, country. Use search-term breakdowns only where available (Google reports shifted toward unique clicks in 2026).
8. Consider Custom Store Listings targeted at relevant search keywords after traffic and eligibility become clear; optional, not a guarantee of appearing or ranking for queries.

## Boundaries

ASO-01 document work only: no Play Console listing edits or publishing, APK/AAB build, app source or Android permissions, timer/audio/FGS changes, data changes or paid GitHub Actions. D-130/code24 remains accepted installed Android development candidate; Stable 9/D-126 remains frozen.

## Official Google Play references (checked 2026-10-30)
- Field limits and translations: https://support.google.com/googleplay/android-developer/answer/9859152
- Store listing best practices: https://support.google.com/googleplay/android-developer/answer/13393723
- Metadata policy: https://support.google.com/googleplay/android-developer/answer/9898842
- Search discovery: https://support.google.com/googleplay/android-developer/answer/4448378
- Custom store listings / search keywords: https://support.google.com/googleplay/android-developer/answer/9867158
- Store performance (2026 measurement changes): https://support.google.com/googleplay/android-developer/answer/9859173
