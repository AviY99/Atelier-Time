# ASO-01 — Original user search request, full discussion decisions and authorization (2026-10-30)

## Project state and placement

- Atelier Time Android **D-130 / versionCode 24** is the current **user-installed, user-accepted development candidate** (on-device About image and both links verified; user confirmed Saved Classes/Sounds and all data preserved). **Stable 9 / D-126 remains the last user-designated frozen stable checkpoint**; do not promote Stable 10 implicitly.
- User asked how to improve discoverability for the exact Hebrew and English search expressions below. User explicitly approved **all** recommendations from the ensuing ASO discussion and requested **the original question and the full discussion above be documented**.
- **ASO-01 is approved as a new pre-launch activity BEFORE store visuals/listing finalization and the Google Play submission sequence.** Approval covers documentation, search-term analysis, localized listing drafts, rollout and measurement plan; it does NOT itself publish the listing or authorize changing Android code, Play Console account, installed APK, store releases, or billing.
- Documentation should persist in main as project-wide source of truth and mirror in Android dev. No GitHub Actions / paid cloud Android compilation.

## Original user question — exact target expressions, original order, spelling and duplicate entries retained

> שאלה , איך מוודאים שהאפליקציה תימצא בחנות בעת החיפושים הבאים

```text
שעון 
שעון לסטודיו 
שעון לסטודיו אומנות 
שעון לאטלייר
שעון לאטלייה 
שעון למודל 
שעון למידול
שעון לציור 
שעון לרישום
שעון למידול
שעון לאמנים
שעון לסשן אומנות  
שעון לסשן ציור 
שעון לסשן רישום 
שעון לסשן מידול 
שעון לסשן אומנות

טיימר 
טיימר לסטודיו 
טיימר לסטודיו אומנות 
טיימר לאטלייר
טיימר לאטלייה 
טיימר למודל 
טיימר למידול
טיימר לרישום 
טיימר לציור 
טיימר למידול
טיימר לאמנים
טיימר לסשן אומנות  
טיימר לסשן ציור 
טיימר לסשן רישום 
טיימר לסשן מידול 
טיימר לסשן אומנות

ניהול זמן 
ניהול זמן לסטודיו 
ניהול זמן לסטודיו אומנות 
ניהול זמן לאטלייר
ניהול זמן  לאטלייה 
ניהול זמן למודל 
ניהול זמן למידול
ניהול זמן לרישום 
ניהול זמן לציור 
ניהול זמן למידול
ניהול זמן לאמנים
ניהול זמן לסשן אומנות  
ניהול זמן לסשן ציור 
ניהול זמן לסשן רישום 
ניהול זמן לסשן מידול 
ניהול זמן לסשן אומנות

Clock
Studio Clock
Art Studio Clock
Atelier Clock
Atelier Clock
Model Clock
Modeling Clock
Drawing Clock
Painting Clock
Modeling Clock
Clock for Artists
Art Session Clock
Painting Session Clock
Drawing Session Clock
Modeling Session Clock
Art Session Clock

Timer
Studio Timer
Art Studio Timer
Atelier Timer
Atelier Timer
Model Timer
Modeling Timer
Drawing Timer
Painting Timer
Modeling Timer
Timer for Artists
Art Session Timer
Painting Session Timer
Drawing Session Timer
Modeling Session Timer
Art Session Timer

Time Management
Studio Time Management
Art Studio Time Management
Atelier Time Management
Atelier Time Management
Model Time Management
Modeling Time Management
Drawing Time Management
Painting Time Management
Modeling Time Management
Time Management for Artists
Art Session Time Management
Painting Session Time Management
Drawing Session Time Management
Modeling Session Time Management
Art Session Time Management
```

Original total: 96 entered nonempty lines (including duplicates); 81 unique exact expressions after outer whitespace normalization. Repeated lines and variants (e.g. אטלייר / אטלייה, Drawing/Painting/Modeling; Hebrew double-space in "ניהול זמן  לאטלייה") were retained above. The user does **not** demand that these be pasted verbatim into live listing text; the goal is discoverability for these queries.

## Discussion summary and approved decisions (all substantive recommendations)

1. **ASO / App Store Optimization** is the pre-release process, not a guaranteed ranking or Google-certified indexing method. Search placement depends on relevance, language, competition, quality, user behavior and availability. Short generic queries such as "שעון", "טיימר", "Clock", "Timer" and "Time Management" are especially competitive; never promise that Atelier Time appears at all positions or for every query.
2. **Google Play listing fields:** 30-character app title, 80-character short description, 4,000-character full description, localized per-language versions, category/tags and graphic assets. Localized Hebrew (`he-IL`) and English (`en-US`) **store listings** are separate from the Android app UI language; no Android code change is needed.
3. **Proposed Hebrew title (28 chars):** `Atelier Time - טיימר לסטודיו`. **Proposed English title (30 chars):** `Atelier Time: Art Studio Timer`. Preserve the Atelier Time brand; descriptions and store titles are drafts until entered/approved in Play Console.
4. **Proposed Hebrew short description (67 chars):** `שעון וניהול זמן לאטלייר, לרישום מודל, ציור ופיסול עם סשנים והפסקות.`
5. **Proposed English short description (73 chars):** `Pose and break timer for drawing, painting and sculpture studio sessions.`
6. **Search intent clusters to cover naturally:** (a) שעון/Clock (b) טיימר/Timer (c) ניהול זמן/Time Management (d) סטודיו/סטודיו לאמנות/אטלייר/אטלייה/Studio/Art Studio/Atelier (e) מודל/רישום מודל/Live Model/Figure Drawing/Posing (f) ציור/רישום/פיסול/מידול אמנותי/Painting/Drawing/Sculpture/Modeling and (g) סשן/שיעור/הפסקה/Art Session/Session Timer/Break Timer.
7. **Avoid keyword stuffing:** do not paste long repeated word lists into store metadata; make accurate, readable Hebrew and English prose. Use spelling variants selectively and meaningfully. In English, `modeling` must refer only to **physical sculpting/live-model work**, not misleading 3D CAD or fashion modeling claims. Likewise, avoid suggesting a generic alarm clock, smart watch, productivity/time tracking, or features not in the app.
8. **Use appropriate category and tags** after confirming available current Play Console choices; do not invent a dedicated SEO keyword input field. Screenshots and icon should accurately convey pose/work/break timeline, in-app clock, sounds and saved classes. These are still release-preparation steps.
9. **After actual publication/indexing**, search the prioritized Hebrew/English phrases on the store, preferably under relevant languages/regions/devices, and record whether shown and approximate position/date/surface. Note personalization, rollout, geography and ranking changes; no false PASS on unrun searches.
10. Monitor **Play Console Store performance / Conversion analysis**, currently focused on **clicks/CTR/visitors** rather than assuming all historical acquisition-based metrics apply (Google updated metrics in 2026). When exposed, review search terms and language/country segmentation. Do not claim keyword-level analytics will exist for every phrase or before store publication.
11. **Optional follow-up:** create Custom Store Listings targeted by search keywords or relevant audience segments **when available/eligible** in Play Console and backed by real data; a CSL changes which listing visitors see, not a guarantee of search ranking. Examples: drawing/live model vs art studio/timer, subject to no misleading copy.
12. Iterate the title/short/full copy from user search evidence and actual Play Console data. Validate lengths and Google Play metadata policies before each submission and keep original approved copy as a comparison baseline. Submit/approve publication only as a separate release action.
13. Preserve the previous release sequence, with **ASO-01 inserted BEFORE artwork/store metadata finalization**; remaining work continues: Play Console/store assets, public Privacy Policy and Data Safety, FGS specialUse declaration plus accurate evidence video, locally signed AAB, release package verification, closed testing, Play review/production, and any **explicit** stable designation.
14. **Boundaries:** no change to D-130 APK/source, timer, audio, specialUse FGS, Android permissions or data; no remote builds/GitHub Actions, no repeated previously accepted QA; no Play Console changes or charges in this documentation task.

## ASO-01 deliverables / status

| Deliverable | State |
|---|---|
| Full original user query list (including duplicates) and discussion approvals | **Recorded** |
| Policy/source-based constraints and English/Hebrew title/short/full listing drafts | **Prepared** in [listing draft](ASO01_GOOGLE_PLAY_STORE_LISTING_HE_EN_2026_10_30.md) |
| Search coverage intent mapping and risk notes | **Prepared** |
| Prelaunch ASO activity on roadmap and QA gates | **Recorded** |
| Apply text to real Play Console | **NOT DONE; separate controlled submission** |
| Actual Google Play indexing/ranking/keyword analytics | **NOT AVAILABLE before publication; post-release gates OPEN** |
| App production listing, screenshots, AAB, policy acceptance | **NOT DONE** |

## Official Google Play evidence (checked 2026-10-30)

- App title, short/full description limits and localization: https://support.google.com/googleplay/android-developer/answer/9859152
- Store listing quality and restrictions on repetitious keywords: https://support.google.com/googleplay/android-developer/answer/13393723 and https://support.google.com/googleplay/android-developer/answer/9898842
- Search discoverability principles: https://support.google.com/googleplay/android-developer/answer/4448378
- Custom store listings, including search keyword targeting: https://support.google.com/googleplay/android-developer/answer/9867158
- Store listing performance and 2026 clicks-based revisions: https://support.google.com/googleplay/android-developer/answer/9859173

## Source conversation trace

- User asked the original question above with all 96 candidate input lines in Hebrew and English.
- Assistant proposed bilingual localized titles, 80-character short copy, longer descriptions, clustered query coverage, category/graphics, post-publication search QA, Play Console measurement and optional Custom Store Listings; cautioned that ranking is not guaranteed and metadata stuffing is prohibited.
- Assistant proposed insertion of **ASO-01** ahead of artwork/store listing setup.
- User replied explicitly: **"כן ,מאשר את כל ההמלצות  , כל השיח הנל  והשאלה המקורית  שיתועדו"**.
- This document captures the full substantive dialogue, original query text and binding approval, instead of presenting all candidate expressions as an inappropriate keyword-stuffed public listing.
