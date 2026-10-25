# Atelier Time — מסמך HANDOFF לשיחה חדשה אחרי Android Stable 4

**תאריך סגירה: 25.10.2026 | מצב הפרויקט: Stable 4 מאושרת | D-108 נסגרה.**

זהו מסמך ההמשך המחייב לשיחה החדשה. הוא נועד למנוע אובדן הקשר, חזרה על עבודה שכבר אושרה או שינוי החלטות ארכיטקטוניות ומוצריות. בשיחה הנוכחית המשתמש אישר במפורש: **"הכל תקין כעת"** לאחר התקנת/בדיקת D-108; לאחר מכן הצהיר: **"מבחינתי זאת גרסה 4 יציבה"** וביקש לסיים את השיחה ולהכין הנדאוף.

## 1. סטטוס מדויק ומקורות

| רכיב | ערך מחייב |
|---|---|
| ריפו | [AviY99/Atelier-Time](https://github.com/AviY99/Atelier-Time) |
| נקודת שחזור יציבה נוכחית | [stable/android-d108](https://github.com/AviY99/Atelier-Time/tree/stable/android-d108) — **Stable 4** |
| SHA של Stable 4 | [02beb0af46719301cccb72ad4698a337ab416d73](https://github.com/AviY99/Atelier-Time/commit/02beb0af46719301cccb72ad4698a337ab416d73) |
| ענף הפיתוח | `android-native-kotlin-compose`, בשלב הסגירה הוא בדיוק SHA של Stable 4 |
| PR | [#3](https://github.com/AviY99/Atelier-Time/pull/3) — Open/Draft, לא ממוזג |
| Stable 3 | `stable/android-d102` — SHA `78878801ea515deccc4e64cdc62b2b3db595769a` |
| Stable 2 | `stable/android-d098` — SHA `6d5cbab6818cae288de8367849f8d3ea53f4739f` |
| Stable 1 | `stable/android-d086` |
| גרסת Android המותקנת שאושרה | **D-108** |
| הגדרות APK | `versionName = "D-108"`, `versionCode = 4` |
| תיעוד יציבה | [ANDROID_STABLE_4_D108.md](ANDROID_STABLE_4_D108.md) |
| מפרט בדיקות D-108 | [NOTIFICATION_EXIT_CLEANUP_D108.md](NOTIFICATION_EXIT_CLEANUP_D108.md) |

**אין לבנות מחדש, להתקין מחדש, למחוק נתונים או להזיז ענפים יציבים עבור עצם פעולת ההנדאוף.**

## 2. מה סגור ומאושר — לא לבדוק שוב כנקודת פתיחה

1. **מבנה מוצר וממשק:** Editor / Timer / Sound / Design / About. העיצוב, פלטת Day/Night והסידור המאושר סגורים מ־Stable 2. לא לשנות ללא בקשה חדשה.
2. **שיעורים שמורים:** ניהול Classes והגדרות זמנים, פתיחה, הפסקות, סיכום, סיום ופיצוי בשני המקטעים הרגילים האחרונים. השמירה המקומית צריכה להישמר בכל Update. אין לבצע Uninstall או Clear Data.
3. **פעולות Timer:** Start, Pause/Resume, Restart Session/Break, Next בתוך השיעור, Exit; אין לשנות סמנטיקה קיימת ללא בקשת משתמש.
4. **מנגנון הצלילים — Stable 3:** Atelier Time / OS sounds / Audio files, מצב Shared לעומת Separate A/B/C, בחירות ומשך הנגינה, התראות A/B/C בזמן מעבר, שמירת הגדרות. המשתמש בדק ואישר. **לא לדרוש בדיקת A/B/C של 3 דקות מחדש.**
5. **עבודה ברקע:** מעבר לאפליקציה אחרת, לחיצה על Home/ריבוע, מסך נעול, תפריט Recents — משתמש דיווח שהטיימר והצלילים פועלים כרצוי.
6. **התראת סטטוס:** D-107 הוסיף התראה שקטה של שיעור פעיל עם שם Class, מצב Running/Paused, המקטע והזמן שנותר. D-108 תיקן את הישארותה לאחר סגירה.
7. **D-108 תיקון וסגירה:** לחצן **Back** סוגר את האפליקציה ומסיר את ההתראה; **Swipe out** מתוך Recents סוגר ומסיר את ההתראה. **Home/Recents overview** משאירים אותה פעילה כששיעור עובד ברקע. המשתמש: "הכל תקין כעת". **D-108 התקבלה; Stable 4 אושרה במפורש.**
8. **מסך About** צריך להציג את *מזהה פיתוח D האמיתי*, לא את מספר נקודת היציבות. D-108 מציגה `Version D-108`; גרסת פיתוח עתידית תהיה D-109/הבאה בפועל, ו־`versionCode` חייב לגדול בהתאם לדרישות Android.

## 3. הוכחות בנייה ובדיקות — גבולות אמת

- בנייה ראשונה של D-108 **נכשלה** בהודעה `ClassNotificationTaskService.kt:35:17 'val' cannot be reassigned`; נמצא `flags` מקומי של `onStartCommand` שהסתיר את `Intent.flags`; תוקן שימוש ב־`addFlags(...)` ב־commit `02beb0af`.
- בנייה חוזרת בטלפון ב־Code on the Go: **`BUILD SUCCESSFUL in 35s`**, ‏42 משימות (9 executed / 33 up-to-date), עם `:app:testDebugUnitTest` ו־`:app:assembleDebug`. תוצר הועתק ל־`/storage/emulated/0/Download/Atelier-Time-D108.apk`, כ־12MB.
- אחרי הבנייה המשתמש אישר שהכול עובד והכריז על Stable 4.
- **לא בוצעה/תועדה** הרצת `:app:connectedDebugAndroidTest` על מכשיר; אין מספר JUnit מדויק בלתי תלוי, אין גרסת Release חתומה, אין בדיקות Doze/OS process-kill/reboot מלאות, ואין אישור Play לשירות קדמי `specialUse`. אסור להציגן כ־PASS.
- נקודת השחזור היא קוד ה־GitHub המדויק; צילום המסך לא כלל SHA מקומי, לכן הייחוס ל־SHA נובע מענף הפיתוח ששימש ונקודת השחזור, לא מזיהוי SHA בטלפון באופן בלתי תלוי.

## 4. ארכיטקטורה וכללי פיתוח שאסור לשנות

- **Android-first, פיתוח ובנייה ישירות בטלפון Android** בעזרת **Code on the Go**.
- **Kotlin + Jetpack Compose**, מנוע Domain ב־Kotlin טהור להכנה לפלטפורמות עתידיות. לא לבצע מעבר ל־Flutter, לא להחליף Framework ולא לבנות דוגמאות היתכנות מיותרות.
- Gradle 9.6.1, compileSdk=36, targetSdk=35, minSdk=26. במסוף CoGo משתמשים בדגל AAPT2 native על ARM64: `-Pandroid.aapt2FromMavenOverride=$ANDROID_HOME/build-tools/36.0.0/aapt2`.
- **לשנות קוד רק בענף** `android-native-kotlin-compose` או בענף עבודה חדש שמבוסס עליו. ענפי `stable/android-d086`, `d098`, `d102`, `d108` הם נקודות שחזור קפואות.
- אין למזג PR #3, להפעיל GitHub Actions בתשלום, ליצור חתימת Release/להעלות לחנות או למחוק נתונים ללא הוראת המשתמש.
- לשמור על ההגדרות, Classes, הבחירות והתקדמות התזמון; כל APK חדש יותקן כ־Update, לא Uninstall.
- לדווח לפי כל שלב: שינויי קוד מדויקים + Git SHA, תוצאת Gradle ממשית אם הורצה, בדיקות ידניות שאושרו, והפניות למסמכים מעודכנים. אין לטעון ל־PASS לפני דיווח אמיתי.
- מסמכי `docs/ROADMAP.md` מכילים גם שלבי Flutter/Dart היסטוריים שכבר בוטלו; יש לקרוא ראשית את הסטטוס העדכני בראש הקובץ.

## 5. שלבי ההמשך הפתוחים — סדר מומלץ

| עדיפות | שלב פתוח | הערות |
|---|---|---|
| 1 | **QA אמינות שלא נבדקה עדיין** | OS process kill, Doze/battery restrictions, multi-hour scheduling and cues, חוסר זיכרון וחידוש עבודה; **לא** לדרוש שוב Back/Home/Swipe או A/B/C שכבר אושרו |
| 2 | **בדיקות Android Instrumentation אמיתיות** | להפעיל `androidTest` על הטלפון, לתעד לכל תרחיש PASS/FAIL ולבודד נתוני בדיקות מהשיעורים השמורים |
| 3 | **Persistency/Crash recovery** | ניסויי עדכון, restart/reboot, שמירת Classes/Audio, טיפול ב־JSON פגום וגרסאות פורמט; לא למחוק נתונים אמיתיים |
| 4 | **סקירת Foreground Service ו־Google Play** | D-108 משתמש בסוג `specialUse` לצורכי התראה וניקוי Recents; לבדוק התאמה למדיניות Play ודרישות Android לפני Release. אין אישור פרסום עדיין |
| 5 | **נגישות ואיכות** | גודל גופן, קורא מסך, מסכי מכשירים נוספים, צריכת סוללה, זיכרון ומבדקי רגרסיה |
| 6 | **Release candidate חתום** | חתימה, מזהי גרסאות, מדיניות פרטיות ורישיונות, בדיקות Update/חזרה לגרסה קודמת במידה שרלוונטי |
| 7 | **לוגו ואייקון אפליקציה — חובה לפני פרסום** | עיצוב ובחירת משתמש, Adaptive Icon/Monochrome/Legacy, אייקון Play בגודל 512×512 ו־Feature Graphic בגודל 1024×500. ראו [ICON_LOGO_PRESTORE_GATE.md](ICON_LOGO_PRESTORE_GATE.md). **האייקון הקטן בהתראת D-107 אינו לוגו האפליקציה** |
| 8 | **פרסום בחנות רק באישור מפורש** | אין להעלות ל־Google Play ללא השלמת שערי QA/תאימות/מיתוג ובקשת המשתמש |
| 9 | **המשך לפלטפורמות אחרות** | iOS/iPadOS, Windows, macOS, תוך ניצול לוגיקה קיימת; בשלב מאוחר יותר |

מומלץ להתחיל בשיחה החדשה מ־**מיפוי והתקדמות בדיקות אמינות שטרם בוצעו**, ולהציע בדיקה הבאה בעלת ערך אמיתי — **לא** להריץ שוב תהליכים שכבר אושרו או לשנות מסכים/צלילים.

## 6. פקודת Android עתידית בלבד (אין להריץ עד שינוי בקוד)

לאחר עדכון מזהה D ו־versionCode בפיתוח, ומרגע שיש שינוי שמחייב build, מתוך `android-native` במסוף Code on the Go:

```bash
git pull --ff-only &&
set -o pipefail &&
bash "$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle" :app:testDebugUnitTest :app:assembleDebug --no-daemon --console=plain "-Pandroid.aapt2FromMavenOverride=$ANDROID_HOME/build-tools/36.0.0/aapt2" 2>&1 | tee "$HOME/atelier-d109-build.log" &&
cp app/build/outputs/apk/debug/app-debug.apk /storage/emulated/0/Download/Atelier-Time-D109.apk &&
ls -lh /storage/emulated/0/Download/Atelier-Time-D109.apk
```

זו דוגמה בלבד לגרסת D-109 עתידית. **אין לשנות את D-108 המאושרת או להריץ בנייה כרגע.**

## 7. איך לפתוח את השיחה החדשה

באותו פרויקט **Atelier time**, פתח שיחה חדשה והדבק:

> אנחנו ממשיכים את פיתוח Atelier Time מאחרי **Android Stable 4 (D-108)** שאושרה בטלפון. קרא תחילה את מסמך ההנדאוף `docs/CHAT_HANDOFF_AFTER_STABLE_4_2026_10_25.md`, את `docs/ANDROID_STABLE_4_D108.md` ואת `docs/HANDOFF.md` ב־GitHub `AviY99/Atelier-Time`. נקודת השחזור הקפואה היא `stable/android-d108` ב־SHA `02beb0af46719301cccb72ad4698a337ab416d73`, ענף העבודה `android-native-kotlin-compose`. אל תחזור על בדיקות UI, צלילים, Timer בסיסי, Home/Back/Recents ו־Notification שכבר אושרו. המשך משערי הבדיקות הפתוחים לפי התיעוד, בדוק תחילה מה באמת חסר, ופעל במנות קטנות עם תיעוד ובנייה בטלפון רק אם יש שינוי קוד. זכור שהלוגו והאייקון הם שלב חובה לפני פרסום בחנות. אל תפתח נושא מחדש ואל תשנה טכנולוגיה או גרסה יציבה ללא הנחיה.

## 8. היכן לחפש את המידע בפעם הבאה

- [Current project handoff](HANDOFF.md)
- [Stable 4 authoritative checkpoint](ANDROID_STABLE_4_D108.md)
- [Notification regression gate D-108](NOTIFICATION_EXIT_CLEANUP_D108.md)
- [Test plan](TEST_PLAN.md)
- [Roadmap](ROADMAP.md)
- [Decision register](DECISIONS.md)
- [Work log](WORK_LOG.md)
- [App icon / logo pre-store gate](ICON_LOGO_PRESTORE_GATE.md)
- [Phone execution procedure](../android-native/PHONE_RUNNER.md)

**Closing assertion:** Stable 4 is accepted and frozen. This conversation is closed after documentation; the next conversation is responsible only for newly authorized engineering work and remaining QA/release gates.
