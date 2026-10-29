# Atelier Time — Handoff לשיחה חדשה | D-127 Stage 7 | 2026-10-29

> **עדכון מצב מחייב — 2026-10-29, אחרי ההנדאוף:** צילום מסוף Samsung Code on the Go מאשר `BUILD SUCCESSFUL in 25s` ו-`COMPILE/BUILD PASS`: סה"כ 67 משימות Gradle (4 בוצעו, 63 מעודכנות), נבנו `Atelier-Time-D127-Candidate.apk` ו-`Atelier-Time-D127-Tests.apk` ונשמר manifest ב-Downloads. זהו **PASS לקומפילציה ואריזה בלבד** לאחר תיקון `62617aa`; **6 בדיקות AndroidTest טרם הורצו**, אין התקנה, אין בדיקות נגישות ידניות, Stage 7 עדיין פתוח. ראש ענף הפיתוח בזמן קבלת הצילום: `c1d4fbdccf16351290de4615e7e5a2db60964806`; ה-`source_commit` וה-SHA256 שב-manifest המקומי לא נקראו ולכן אין לטעון לאימות עצמאי של זהות ה-APK. **Stable 9 D-126/code20 ונתוני המשתמש נשארו ללא שינוי.** ללא GitHub Actions, ללא בנייה נוספת או שחזור 285 הבדיקות. השלב הבא מותנה ב**אישור משתמש נפרד להתקנת D-127 מעל D-126 בלי uninstall/clear data**; רק לאחריו ניתן להריץ את 6 בדיקות הנגישות הממוקדות כאשר אין טיימר פעיל ואת QA הידני. כל תיאורי FAIL/ממתין לבנייה בהמשך מסמך זה הם היסטוריים ואינם המצב העדכני.


**סטטוס מחייב בעת סגירת השיחה:** D-127/versionCode 21 היא *גרסת מקור בפיתוח*, לא גרסה יציבה, לא מותקנת ולא מאושרת. גרסה יציבה ומותקנת: **Stable 9 = D-126/versionCode 20**, תוצאות ה-QA הקודמות **285/285 PASS** (243 JVM + 42 Android instrumentation), והמשתמש אישר שה-Saved Classes וה-Sounds נשמרו. אין לגעת בגרסה היציבה או בנתונים.

## 1. נקודת ההמשך המדויקת — חשוב לקרוא ראשונה

**הצילום האחרון מהטלפון:** ניסיון בניית D-127 ב-Samsung Code on the Go, `BUILD FAILED in 19s`; **61 Gradle tasks (1 executed, 60 up-to-date)**; המשימה שנכשלה `:app:compileDebugAndroidTestKotlin`. שתי שגיאות Kotlin ב-`android-native/app/src/androidTest/java/com/ateliertime/android/AccessibilityStage7UiTest.kt`:
- שורה **115:64** — `None of the following candidates is applicable`: הביטוי `wheel.getUnclippedBoundsInRoot().width` נעזר ב-`DpRect.width`, שהרחבתו לא יובאה.
- שורה **116:83** — `'operator' modifier is required on ... Comparable.compareTo`, שגיאה נגררת מסוג הרוחב הלא תקין.

**השגיאות האלה כבר טופלו בקוד — אין לתקן אותן שוב.** תיקון אחרון `62617aa90005abfa9e1ed2845e9368c2952f3c98` בענף הפיתוח, בדיקות בלבד:
```kotlin
val wheel = compose.onNodeWithTag(tag).assertIsDisplayed()
val bounds = wheel.getUnclippedBoundsInRoot()
val actualWidth = bounds.right - bounds.left
assertTrue("$tag wheel width $actualWidth exceeds 90.dp", actualWidth <= 90.dp)
```
שישה Test methods נותרו ב-`AccessibilityStage7UiTest.kt`. הקוד נבדק בקריאה חוזרת מ-GitHub, **אך שום בניית Samsung או Android instrumentation לא בוצעו לאחר ה-commit**. אין להציג PASS. Commits של תיעוד מאוחרים יותר אינם תיקוני קוד נוספים.

שני כישלונות קודמים שנפתרו לפני האחרון:
- `MelodySettingsSection.kt`: unresolved `testTag` בשורות 158/195 — נוסף import ב-`6837de78741e1c56d9b7978096dd5cf064ca6477`.
- `AccessibilityStage7UiTest.kt`: unsupported `assertWidthIsAtMost` בשורות 16/114 — הוסר ב-`68392421f2a88bfcca1094260d11638f06182167`, אך אז התגלה כשל `DpRect.width`; התיקון האחרון הוא `62617aa`.

**השלב הבא בלבד:** בניית D-127 *בטלפון Samsung*, פעם אחת, לאחר עדכון ה-checkout. הפקודה מתוך `android-native` ב-Code on the Go:
```bash
git pull --ff-only && bash tools/build_d127_accessibility_candidate_on_phone.sh
```
הסקריפט **רק בונה** APK רגיל ו-AndroidTest APK, שומר לוג ב-`/storage/emulated/0/Download/Atelier-Time-D127-Stage7-build.log` ואם נכשל מציג אוטומטית עד 30 שורות שגיאות Kotlin במסוף. המשתמש רשאי להציג צילום מסך; **אין לבקש קובץ לוג, העלאה ידנית של דוחות, חזרה על אותו ניסיון או הרצת Gradle נוספת רק כדי לקבל שגיאות**. לצפייה בכישלון שנשמר ללא קומפילציה: `bash tools/show_d127_build_errors_on_phone.sh`.

## 2. החלטת תקציב מוחלטת — אין חריגים אוטומטיים

המשתמש חרג מתקציב GitHub החודשי וקבע: **אין להריץ GitHub Actions / cloud builds / CI / Codespaces, אין להפעיל workflow מחדש ואין לקמפל ב-GitHub, גם אם מאגר ציבורי עשוי להיות זכאי לדקות חינם.** רק Samsung Code on the Go לקומפילציה; GitHub לקוד ולתיעוד. Workflows של GitHub Actions הוסרו מהענף ומה-main; שני ניסיונות ענן היסטוריים הסתיימו ב-FAIL ואינם פעילים. **לא ליצור או להפעיל מחדש שום workflow בלי אישור תקציבי חדש ומפורש.** הנחיות מחייבות: [COST_AND_BUILD_POLICY_2026_10_29.md](COST_AND_BUILD_POLICY_2026_10_29.md).

גישת GitHub עובדת דרך מחבר ChatGPT ואפשר לתקן קוד ומסמכים ישירות. לעומת זאת, **אין לשיחה גישת Shell/ADB ישירה לטלפון**; גם הרשאת `git push` מהטלפון לפרסום דוחות לא הוכחה. אין להבטיח שהטלפון מסנכרן דוחות ל-GitHub או להעמיס על המשתמש הגדרות חדשות. ממשיכים עם סקריפט שגיאות + צילום מסוף לפי השיטה שכבר עבדה.

## 3. תכולת שלב 7 הנמצאת כבר בקוד

- ששת הטאבים והתאמה ל-fontScale מוגדל, גלילה אופקית כשנדרש.
- שדות Editor נערמים במסכים צרים/גופנים מוגדלים.
- Timer Class controls נעטפים, ו-Free Timer מתאים שלוש גלגלות HH:MM:SS לרוחב.
- Free presets אזורי לחיצה של 48dp ומצב selected עבור TalkBack.
- תיאורי נגישות ל-EndClockWheel, Timeline Canvas, Day/Night palette, Design Auto slider, Sound duration sliders.
- **שש בדיקות Android Compose accessibility ממוקדות נכתבו**, טרם הורצו.
- אין שינוי מכוון ללוגיקת הטיימר, `ConstraintFuturePlanner`, Scheduled Class, Saved Classes/Sounds, foreground service או האלגוריתמים.

## 4. רק אחרי BUILD SUCCESSFUL בטלפון

1. רשום את תוצאת ה-build וה-SHA של commit המקור על dev; עדכן את הדוח בשני הענפים.
2. **אין התקנה אוטומטית**. התקנת D-127 על גבי D-126 בטלפון מחייבת אישור משתמש נפרד ובלי uninstall/clear data; נתוני Classes/Sounds חייבים להישמר. שישה Android UI instrumentation tests זקוקים ל-D-127 מותקנת לאחר אישור; `tools/termux_run_d127_targeted_accessibility_qa.sh` מיועד לכך ושומר נתונים, אך יש להפעילו רק במסגרת אישור.
3. בדיקות ידניות ממוקדות: font scale 100/130/200%, צר/רגיל, Day/Night, TalkBack focus and labels, chart description, wheel blocked context, button touch targets, enabled/disabled/error contrast, ושימור נתונים לאחר אישור עדכון. **לא** להריץ מחדש את 285 בדיקות Stable 9 סתם.
4. רק אחרי תוצאות בפועל ואישור משתמש ניתן לסגור Stage 7 או לשקול Stable 10. שלבים 8–12 לא החלו.

## 5. ענפים, תיעוד ומניעת עבודה כפולה

- Repository: [AviY99/Atelier-Time](https://github.com/AviY99/Atelier-Time).
- **קוד Android authoritative:** `android-native-kotlin-compose`. לא לבצע שינויי מקור ב-`main`.
- **מסמכי פרויקט משותפים:** `main` ו-`android-native-kotlin-compose`; מסנכרנים תוכן זהה עם GitHub tool. `main` אינו ענף Android production.
- **Immutable stable ref:** `stable/android-d126-qa9-tests-complete` SHA `c48c890fdbe97d2d6c304babb8aec83510d0a737`. אין לדחוף אליו commits.
- כניסה: [DOCUMENTATION_INDEX_AND_BRANCH_POLICY.md](DOCUMENTATION_INDEX_AND_BRANCH_POLICY.md), [CURRENT_STATUS_AND_HANDOFF_STABLE9_2026_10_28.md](CURRENT_STATUS_AND_HANDOFF_STABLE9_2026_10_28.md), [D127_STAGE7_ACCESSIBILITY_ADAPTATION_AND_QA_2026_10_28.md](D127_STAGE7_ACCESSIBILITY_ADAPTATION_AND_QA_2026_10_28.md), [TEST_PLAN.md](TEST_PLAN.md), [HANDOFF.md](HANDOFF.md) (יומן היסטורי מצטבר). מסמך זה הוא **ה-handoff הקצר והעדכני ביותר** לאותה נקודת מעבר; הערות ישנות על CI חינמי, חמש בדיקות או כישלון שלא אובחן הן היסטוריות ובוטלו.
- אין לבצע סבבי תכנון/סקירות קוד מלאות מחדש. תחילת השיחה הבאה היא בניית Samsung **לאחר** תיקון `62617aa` ובחינת תוצאה חדשה, לא שכתוב Stage 7.

## 6. הודעת פתיחה מומלצת לשיחה הבאה

> המשך Atelier Time מנקודת ה-handoff [HANDOFF_D127_STAGE7_TO_NEXT_CHAT_2026_10_29.md](HANDOFF_D127_STAGE7_TO_NEXT_CHAT_2026_10_29.md). אמת קודם מה כבר בוצע בענף הפיתוח. התיקון האחרון `62617aa` ל-`AccessibilityStage7UiTest.kt` כבר נשמר, אך עוד לא קומפל בטלפון. אין לבנות ב-GitHub בגלל התקציב, אין לגעת ב-Stable 9 או בנתונים ואין להריץ מחדש את 285 הבדיקות. המתן לתוצאת הבנייה המקומית הבאה; אם נכשלת, אבחן צילום שגיאות מסוף ואבצע תיקון ממוקד ב-GitHub. שמור מסמכים מסונכרנים ב-main וב-dev.
