> **2026-10-29 — D-127 Stage 7 QA preparation (supersedes earlier five/six-test notes):** six isolated Android UI tests authored, **zero compiled/executed**; guarded [build-only](https://github.com/AviY99/Atelier-Time/blob/android-native-kotlin-compose/android-native/tools/build_d127_accessibility_candidate_on_phone.sh) and [targeted six-test runner](https://github.com/AviY99/Atelier-Time/blob/android-native-kotlin-compose/android-native/tools/termux_run_d127_targeted_accessibility_qa.sh) committed **but not run**. The runner refuses to replace installed D-126 and only works with a separately approved D-127 in-place upgrade. No Android SDK/ADB/device here; device QA and TalkBack/visual acceptance remain PENDING. Stable 9 D-126/code20 and its historical 285 PASS remain untouched. [Detailed Stage 7 gate](D127_STAGE7_ACCESSIBILITY_ADAPTATION_AND_QA_2026_10_28.md).

> **Stage 7 D-127 follow-up, 2026-10-29:** source commit [`c83c197`](https://github.com/AviY99/Atelier-Time/commit/c83c19738ea70bba0223acd84c46c93411276b7a) improves Design palette selected-state/TalkBack, Design Auto slider and Sound duration-slider descriptions. **Six targeted Android UI tests are authored, none compiled or run.** D-127/code21 remains **SOURCE ONLY / QA PENDING**; no APK built/installed. User-approved Stable 9 stays D-126/code20 (historical 285/285 PASS); frozen stable branches and saved data untouched. [Details](D127_STAGE7_ACCESSIBILITY_ADAPTATION_AND_QA_2026_10_28.md).

> **Latest development note — 2026-10-29 (supersedes the older D-127 note below):** Stage 7 D-127/code21 UI source refined for large-font Editor field pairs, narrow Free HH:MM:SS wheels and spoken preset/blocked-value states. **Five new Android UI tests are WRITTEN, NOT RUN**; a safe build-only script exists but has NOT BEEN EXECUTED. **D-127 has not been compiled, installed, run on Samsung or accepted**. Current installed user-approved release remains **Stable 9 = D-126/code20**, with its original 285/285 PASS and preserved Saved Classes/Sounds. Frozen stable refs unchanged. [2026-10-29 D-127 detailed QA gate](D127_STAGE7_ACCESSIBILITY_ADAPTATION_AND_QA_2026_10_28.md).

> **Active development update (2026-10-28):** Stage 7 D-127/code21 accessibility changes are **SOURCE COMMITTED / QA PENDING** on `android-native-kotlin-compose`: responsive tabs/controls, semantic wheels/chart, 48dp Free presets, 3 newly authored **UNRUN** Android UI tests. **No D-127 build/device QA/APK install**; accepted installed Stable 9 is **still D-126/code20**, with its prior 285/285 PASS. [Stage 7 record](D127_STAGE7_ACCESSIBILITY_ADAPTATION_AND_QA_2026_10_28.md).

# Atelier Time — authoritative current project status and handoff

**תיעוד רשמי / Start here:** [מפת מסמכים, תפקידי הענפים ונוהל Handoff](DOCUMENTATION_INDEX_AND_BRANCH_POLICY.md). זהו ההנדאוף המקוצר והקנוני; המסמך `HANDOFF.md` הוא יומן מצטבר והיסטורי.


**Last confirmed:** 2026-10-28. **Authoritative release:** **Stable 9 = D-126 / versionCode 20**. All older “current/pending” headings in dated historical documents are superseded by this entry. **Primary development** lives on the Android Kotlin/Compose branch `android-native-kotlin-compose`; the default `main` branch remains a project documentation and historical-reference surface, **not** a claim that Android code was merged into `main`.

## Approved current product

**Atelier Time** runs natively on Android (Kotlin + Jetpack Compose), developed and built on the Samsung using Code on the Go. The user-approved Stable 9 is the same installed D-126 app previously accepted as Stable 8: a unified physical-deadline-aware Timer planner based on `ConstraintFuturePlanner`, preserved past and fixed Summary, approved Session/Break rules, live timeline, and per-Run diagnostic logging. D-126 separates operational clock/controls from forensics, adds a full in-app scrollable actions log (about five newest events visible; scroll to Start), stores append-only app-private JSONL snapshots/events/phase reallocations/sound cue requests, and offers deliberate Android document export and confirmed report deletion. Existing Saved Classes and Sounds are preserved.

**No new Android binary was compiled or installed for Stable 9:** the stable ordinal increased because all planned automated QA and user verification passed. Installed app remains `D-126` / `versionCode 20`, built from production source commit `b2d47ca32c2f470d38e556ea7a571afaa89400e3`.

## Actual verified QA

| Gate | Observation |
| --- | --- |
| Gradle/Android main + AndroidTest APK build on Samsung | `BUILD SUCCESSFUL`; 72 tasks PASS |
| JUnit JVM regression suite | **243/243 PASS**; failures/errors/skips = 0 |
| Both original APK SHA-256 checks against build manifest | **PASS** |
| Android Instrumentation on physical Samsung | **42/42 PASS**, `OK (42 tests)`, 21.697 seconds |
| **Combined automated tests** | **285/285 PASS** across JVM and Android device suites |
| User manual functional acceptance | **PASS**, “הכל עובד כנדרש” |
| Saved Classes/Sounds after AndroidTest | **PASS**, user confirmed “הכל נשמר” |

The first physical AndroidTest run had 4/42 failures from test-only private-storage and virtual-clock synchronization issues; after AndroidTest-only changes, all 42 passed. The corrected test-only APK was built from `07816e22014e2ff6593757cbd33879344ada1e80`. Instrumentation updated **only** the test package; the main D-126 app was not replaced or cleared. Do not overstate unmeasured long-duration Doze, spontaneous process reclaim, actual audibility across all devices, or Google Play compliance.

## Stable checkpoints and preservation

- **Current stable release certificate:** [Stable 9 — 285 automated tests](STABLE9_D126_285_AUTOMATED_QA_ACCEPTANCE_2026_10_28.md).
- **Frozen Stable 9 branch:** [`stable/android-d126-qa9-tests-complete`](https://github.com/AviY99/Atelier-Time/tree/stable/android-d126-qa9-tests-complete). Official checkpoint, do not advance.
- **Earlier frozen Stable 8:** [`stable/android-d126-qa8-logic-diagnostics`](https://github.com/AviY99/Atelier-Time/tree/stable/android-d126-qa8-logic-diagnostics). Original Stable 8 D-124 [`stable/android-d124-qa8`](https://github.com/AviY99/Atelier-Time/tree/stable/android-d124-qa8) remains intact.
- **Android development only:** [`android-native-kotlin-compose`](https://github.com/AviY99/Atelier-Time/tree/android-native-kotlin-compose). Do not mutate accepted source/installed Classes/Sounds without a distinct approved change.
- **Rejected:** D-125 proposal to transfer a sub-minute remainder into Summary. Fixed Summary duration and minimum one-minute work Session remain applicable.

## Operational handoff and technical sources

Read [HANDOFF.md](HANDOFF.md) first for cumulative decisions and the specific next-chat operating contract. For exact project baseline see [DECISIONS.md](DECISIONS.md), [ROADMAP.md](ROADMAP.md), and [TEST_PLAN.md](TEST_PLAN.md). Evidence and implementation:

- [Stable 9 acceptance certificate](STABLE9_D126_285_AUTOMATED_QA_ACCEPTANCE_2026_10_28.md)
- [Stable 8 D-126 functionality and immutable checkpoint](STABLE8_D126_UNIFIED_TIMER_AND_RUN_DIAGNOSTICS_QA_ACCEPTANCE_2026_10_28.md)
- [42/42 on-device instrumentation and post-test data preservation](STABLE8_D126_ANDROID_INSTRUMENTATION_QA_PASS_2026_10_28.md)
- [D-126 diagnostics implementation](D126_RUN_DIAGNOSTICS_AND_SCROLLABLE_HISTORY_2026_10_28.md)
- [Initial four AndroidTest failures, test-only remediation, successful retest](D126_INSTRUMENTATION_42_TEST_FAILURE_TRIAGE_2026_10_28.md)
- [D-124 unified timer planning contract](D124_UNIFIED_CONSTRAINT_FUTURE_PLANNER_2026_10_28.md)
- [Native Android source README](https://github.com/AviY99/Atelier-Time/blob/android-native-kotlin-compose/android-native/README.md)

**Installation/QA preference:** Samsung My Files for an authorized **in-place** main-APK update; do not force an ADB workflow when not needed. Termux ADB is used for user-approved Android instrumentation when available. Never uninstall the main app, clear app data, delete saved presets, or modify sounds. No further D-126 build, APK installation, QA rerun or Stable 9 ordinal/version change is currently required.

## Next planned stages

7. Accessibility/Android display adaptability: font scaling, screen sizes, contrast, touch areas, TalkBack.
8. Android hardening and security/privacy/policy (including foreground-service permissions).
9. Signed release/AAB and upgrade/data preservation.
10. Final logo/launcher icon and store assets — still requires user artwork approval.
11. Google Play listing and internal/closed testing, then separate publication decision.
12. Research future platform adaptations (iOS/desktop) without rewriting timer semantics prematurely.

**Evidence policy:** Historical documents retain the facts of the time they were written; do not erase failed attempts or incorrectly promote unexecuted tests. This 2026-10-28 document supersedes old headings claiming a previous release is still “current”.

## הנחיית פתיחה מחייבת לשיחה חדשה באותו פרויקט

**השיחה הקודמת נסגרה בהוראת המשתמש; זהו מסמך ההעברה הרשמי.** בשיחה החדשה באותו פרויקט יש להתחיל מקריאת הקובץ הזה, ולא להסתמך על תקצירי שיחה חלקיים או על כותרות היסטוריות ב-`HANDOFF.md`.

**טקסט פתיחה מוצע לשיחה החדשה:**

> המשך את פרויקט Atelier Time מנקודת Stable 9 (D-126/code20). פתח קודם את `docs/DOCUMENTATION_INDEX_AND_BRANCH_POLICY.md` ואת `docs/CURRENT_STATUS_AND_HANDOFF_STABLE9_2026_10_28.md` בענף `main` או בענף הפיתוח. אמת את מצב GitHub בפועל. כל 285 הבדיקות הושלמו בהצלחה והמשתמש אישר שהמחלקות והצלילים נשמרו. הצג בקצרה את מצב הפרויקט, מבנה התיעוד וצעדי ההמשך, ואז המשך רק לפי בחירתי. אל תבנה, תתקין, תמחק נתונים או תשנה את Stable 9 ללא אישור מפורש.

**משימות חובה לעוזר בתחילת השיחה החדשה:**

1. **קריאת אמת:** קרא את `docs/DOCUMENTATION_INDEX_AND_BRANCH_POLICY.md` ואת המסמך הנוכחי; רק לפי הצורך פתח `HANDOFF.md`, `DECISIONS.md`, `ROADMAP.md`, `TEST_PLAN.md` ותעודת Stable 9. אל תציג מסמך היסטורי כסטטוס עדכני.
2. **הסבר קצר על הענפים:** `main` הוא שער הכניסה ומרכז התיעוד העדכני וההיסטורי; `android-native-kotlin-compose` הוא מקור אמת לקוד Android ולתיעוד העבודה; `stable/android-d126-qa9-tests-complete` הוא צילום מצב קפוא. **לא כל ענף מקבל מחדש עותקים של מסמכים חדשים, ולא מבצעים merge של כל קוד הפיתוח ל-`main`.**
3. **תאימות תיעוד:** המסמכים המשותפים המרכזיים ב-`main` ובענף הפיתוח כבר סונכרנו; לפני דיווח על סטטוס חדש אמת Git blob SHA בשני הענפים. אם נדרש סנכרון, העבר רק את קובצי התיעוד הרלוונטיים בשמירת מבנה הענפים.
4. **נקודת עבודה:** שלבים 1–6 ומבחני D-126 **סגורים**; הצע להתחיל בשלב 7 (נגישות והתאמת תצוגה), עם סקירה ממוקדת ללא שינוי קוד לפני אישור. שלבים 8–12 נשארים בתוכנית. אין לפתוח מחדש את לוגיקת הטיימר המאוחדת או תחקור D-126 בלי ממצא חדש.
5. **בטיחות ונוהל:** אין לגעת ב-APK המותקן, Saved Classes, Sounds או בנתוני האפליקציה; אין להסיר/לנקות נתונים; אין להתקין APK או להפעיל בדיקת מכשיר חדשה בלי צורך ואישור. שמור על ענפי Stable 8/9 קפואים.
6. **סגירת שיחה עתידית:** עדכן קודם את התיעוד הקנוני בענף הפיתוח, אחר כך סנכרן ל-`main` את המסמכים המשותפים ובדוק זהות, ולבסוף קבע במסמך Handoff החדש מה בדיוק בוצע/נותר. **אל תבטיח עדכון GitHub שלא בוצע בפועל.**

**הסטטוס המאושר בסיום שיחה זו:** Stable 9 = D-126/code20 מותקנת, 243/243 JVM + 42/42 Android Instrumentation = 285/285 PASS, 72 Gradle tasks PASS, שני APK SHA-256 תקינים, Saved Classes/Sounds נשמרו לפי אישור משתמש. לא נדרש APK חדש או מספר גרסה נוסף.
