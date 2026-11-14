# ATELIER TIME — HANDOFF משיחה סגורה לשיחה חדשה
**תאריך:** 25.10.2026 · **מעמד:** Stable 5 אושרה; השלב הבא יבוצע בשיחה חדשה בלבד.

## 1. סטטוס מחייב בתחילת השיחה החדשה

- **Stable 5 היא D-114** — המשתמש דיווח לאחר בדיקה מעשית בטלפון: **"הכל עובד , זאת גרסה יציבה 5"**.
- **ענף קפוא ומאומת:** `stable/android-d114`, commit **`34a692a98282944a76e314efcc228dac3bd072ff`**. `versionName = "D-114"`, `versionCode = 9`. **לעולם לא לשנות, לדחוף קומיט או להזיז ענף זה.**
- **Stable 4 קפואה בנפרד:** `stable/android-d108`, commit `02beb0af46719301cccb72ad4698a337ab416d73`.
- **ענף עבודה יחיד לפיתוח ובדיקות:** `android-native-kotlin-compose` ב־`AviY99/Atelier-Time`. עבודות תיעוד כבר הוספו לענף זה אחרי יצירת Stable 5; ה־SHA של ענף הפיתוח יתקדם, אך ה־SHA של Stable 5 חייב להישאר קבוע.
- **שלב 1 (פיצ'ר Run/end time): אושר והסתיים. שלב 2 (כל הבדיקות האוטומטיות האפשריות): טרם התחיל.** המשתמש דרש *לבקש אישור בין השלבים* וגם שהשלב הבא יבוצע **רק בשיחה החדשה**. אין להפעיל בדיקות בשיחה הסגורה.

## 2. מה עבר אישור ב־Stable 5

ב־Saved Classes → Run, **לפני כל הפעלה**, נפתח מסך אישור שעת סיום. `Calculated finish` הוא ערך חי המתעדכן בזמן ההמתנה, כולל שניות. המשתמש יכול לאשר את השעה המחושבת ללא שינוי, או לבחור **Change finish time** כדי לחשוף שני גלגלי גלילה נפרדים (שעות / דקות). עצם הצגת הגלגלים אינה מקבעת את שעת הסיום; רק שינוי מספר בפועל עושה זאת.

אפשר לבחור שעה מוקדמת, שווה או מאוחרת כאשר `RunEndTimePlanner` מסוגל לשנות את **שני הסשנים הרגילים האחרונים בלבד**; יתר המבנה, ההפסקה שביניהם, סיכום השיעור וה־Classes השמורים אינם משתנים. לכל סשן סופי יש מינימום דקה. הגלגל נעצר **לפני** בחירה בערך אדום בלתי אפשרי, גם בגלילה מהירה, ומאפשר חזרה לירוק; הערך האדום הקרוב מוצג בגבול. ניסיון לגלול/להקיש מעבר לגבול מציג **הסבר אדום מיידי עם השעה שנחסמה והסיבה**, הנעלם אחרי בחירה תקינה או חזרה ל־**Use calculated finish**. `Confirm & Run` מחייב אישור ובדיקת היתכנות מחדש בזמן ההתחלה. הגבלות של ריצה מאושרת נאכפות מול שעון פיזי מונוטוני והקיזוזים המאושרים; אין שינוי ב־Classes/Sounds הנשמרים.

פיצ'רי Stable 4 שהמשתמש כבר אישר נשארים: הרצה וצלילים ברקע אחרי Home/מעבר לאפליקציה אחרת/נעילת מסך; סגירת אפליקציה בפועל ע״י Back או Recents swipe מפסיקה את ה־Class ומסירה התראה; פתיחה מחדש/ריסטארט מכשיר **לא** משחזרים Class פעיל, אבל שומרים Classes/Sounds. יש להבחין בין סגירה אמיתית לרקע רגיל.

**ראיות:** המשתמש הציג `:app:assembleDebug` **BUILD SUCCESSFUL in 11s**, קובץ `/storage/emulated/0/Download/Atelier-Time-D114.apk` בגודל כ־12MB; המשתמש דיווח שכל התפקוד עובד ואישר Stable 5. **לא** קיימת ראיית Android instrumentation מלאה ל־D-114; אישור ידני אינו שקול לבדיקה אוטומטית.

## 3. תשתית העבודה בטלפון

- מכשיר Android **Samsung SM-S921B**.
- **Code on the Go (CoGo)** לבניית Kotlin + Jetpack Compose על אותו הטלפון; repo root:
  `/storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time`
- Android Gradle project: `android-native/`, ענף `android-native-kotlin-compose`.
- Gradle דרך:
  `bash "$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle"`
  עם פרמטר ARM64 AAPT2:
  `"-Pandroid.aapt2FromMavenOverride=$ANDROID_HOME/build-tools/36.0.0/aapt2"`.
- **Termux נפרד** מותקן עם `android-tools` (`adb 1.0.41`) ו־`~/storage/downloads` זמין.
- אפשר להתחבר ב־Android Developer options → Wireless debugging: Pairing בטלפון במסך מפוצל עם Termux, ואז `adb connect <PHONE_WIFI_IP>:<CURRENT_CONNECT_PORT>` ו־`adb devices -l`. חיבור קודם עבר במצב `device` עם `10.0.0.6:34545`; **IP והפורט עשויים להשתנות, אין להניח שהם עדיין תקפים**. קוד pairing בן שש ספרות מקלידים רק ב־Termux; לא מפרסמים.
- ה־CoGo SDK `$ANDROID_HOME/platform-tools/adb` הוא קובץ אפס בתים; **לא להשתמש בו, לא לשנות אותו ולא למחוק SDK**. Termux הוא ADB המוכח.
- אין להריץ `pm clear`, `adb uninstall`, force-stop המשבש Class אמיתי, או פעולות מחיקת נתונים. `adb install -r` מעדכן APK ומשמר נתוני אפליקציה, אבל קודם לוודא שאין Class פעיל.

## 4. מצב הבדיקות הידוע ומה חסר

- **D-110 בלבד:** במכשיר האמיתי ב־Termux הורץ
  `adb shell am instrument -w com.ateliertime.android.test/androidx.test.runner.AndroidJUnitRunner`
  עם תוצאת **`OK (14 tests)`**, זמן `0.295`. חלוקה: 7 `AndroidPresetStoreTest`, 6 `MelodyPreferencesTest`, 1 `StaticAudioTrackLifecycleTest`. זו הוכחה ל־D-110 בלבד, **לא** ל־D-114.
- **D-114 מקור הבדיקות בענף:** 5 קובצי AndroidTest עם **32 מתודות `@Test`**: 7 PresetStore, 6 MelodyPreferences, 1 AudioTrack, 11 `RunEndTimeDeviceTest`, 7 `RunEndApprovalUiTest`. הנתון הוא **ספירת קוד**, לא PASS.
- **פער קונקרטי לפני הרצת AndroidTest:** `android-native/app/src/androidTest/java/com/ateliertime/android/RunEndApprovalUiTest.kt` עדיין משתמש ב־`run_end_override` / `run_end_clock` ובהזנת טקסט `HH:mm`. ב־D-114 אין פקדים אלה. יש להתאים את בדיקות Compose UI לתגיות:
  `run_end_dialog`, `run_end_calculated`, `run_end_change_finish`, `run_end_wheels`, `run_end_hour`, `run_end_minute`, `run_end_blocked_reason`, `run_end_reset_calculated`, `run_end_selected`, `run_end_confirm`.
  בדוק Live-tick; גלגלת מוסתרת בתחילה; פתיחה לפי בקשה; גבול אדום, כולל swipe מהיר וטאפ; סיבת חסימה; ניקוי ההודעה כשחוזרים לירוק; Cancel ללא הפעלה; אישור שעת ברירת מחדל; התאמות מוקדמות/מאוחרות; שימור נתונים. לשמור על namespaced `SharedPreferences` מבודדים בבדיקות UI.
- קיימות בדיקות JVM ל־ScheduleEngine, MonotonicSessionTimer, FinalPairCompensation, RunEndTimePlanner, RunEndTimeProperty, ClassClockTracker, PresetCatalog, SoundSettings, NotificationExitPolicy ועוד. יש להריץ את **כולן** ולא להסתפק בדוגמאות נבחרות; להוסיף בדיקות גבולות/שעון מונוטוני/שינויים אחרי Pause, Restart, Next והגנה על חומרים שמורים.
- **סקריפטים ישנים!** `android-native/tools/build_all_qa_on_phone.sh`, `termux_run_all_device_qa.sh`, `qa_build_d111.sh`, `qa_device_d111.sh` מקודדים `D-111` והם **אינם כשירים להרצה ישירה נגד D-114**. הראשון אף מוודא רק D-111 והשני מתקין D-111 ועשוי לשנמך גרסה אם אפשר. **לעדכן/להחליף** סקריפטים בענף הפיתוח לפני הרצה ולוודא גרסה, מסלול APK, test count הנוכחי, שמירת הנתונים ומצב התקן.
- בדיקות ADB בטוחות לאחר אישור: metadata/package/version, הרשאות, רישום השירות וה־instrumentation, לוגים של קריסות, הודעות תהליך (ללא חשיפת נתונים), התנהגות UI והרשאות במסגרת טסטים מבודדים. **Home/Back/Recents/notification ונעילה** יכולים לדרוש מסך פעיל ומעורבות משתמש; אין לטעון שהם אומתו אוטומטית אם לא נאספה ראיה מפורשת.
- **עדיין פתוח:** מדידה ארוכה של Class כ־3 שעות; מבחן שינוי שעון מערכת אמיתי ±120 שניות; Doze/סוללה ונעילת מסך לאורך זמן; שמע בפועל; ניקוי התראות אחרי הריגת תהליך בלתי צפויה. בדיקות יחידה עם שעון מדומה אינן מחליפות מבחנים אלה. אין להזיז את שעון המכשיר האמיתי או להפריע לריצה ללא אישור ספציפי.

## 5. תכנית שלב 2 — אחרי אישור מפורש בשיחה החדשה בלבד

1. לקרוא מסמך זה ו־`docs/HANDOFF.md`, `docs/TEST_PLAN.md`, `docs/ANDROID_QA_RECOVERY_AND_DEVICE_TESTS_2026_10_25.md`; לוודא SHA של `stable/android-d114` ושכל שינוי יהיה **רק בענף העבודה**.
2. לבקש **אישור מפורש** של המשתמש להתחיל Stage 2; לא להמציא כבר בדיקות PASS.
3. לבצע audit לטסטים קיימים/סקריפטים, לתקן תרחישי Compose ישנים, ולהוסיף כיסוי אוטומטי לכל מה שאפשר מבלי לפגוע בנתוני המשתמש. לבדוק test source לפני run, לא רק `@Test` count.
4. ב־CoGo אחרי `git pull --ff-only`, להריץ בדיקות JVM ובניית test APK בלבד; אין צורך לשנות או להתקין APK רגיל אם קוד הייצור D-114 לא השתנה. פקודת בסיס:
   ```bash
   cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native
   bash "$HOME/.cg/gradle-dists/gradle-9.6.1/bin/gradle" \
     "-Pandroid.aapt2FromMavenOverride=$ANDROID_HOME/build-tools/36.0.0/aapt2" \
     :app:testDebugUnitTest :app:assembleDebugAndroidTest
   ```
5. להעתיק AndroidTest APK לתיקיית Downloads ממסלול שנמצא בפועל ע״י `find app/build/outputs/apk/androidTest -type f -name '*.apk'`, להתקין **רק חבילת הבדיקות** דרך Termux (`adb install -t -r`) על APK שתואם לחתימת יישום D-114 המותקן. לוודא `adb devices -l` מחזיר `device`, שאין Class פעיל, ולבדוק `pm list instrumentation`.
6. להריץ `adb shell am instrument -w com.ateliertime.android.test/androidx.test.runner.AndroidJUnitRunner`, לשמור לוג מלא ולדווח **מניין actual PASS/FAIL**, כולל חריגות. אם נכשל — לא למסך צילומי ניחוש, אלא לתקן רק בענף הפיתוח; Stable 5 נשארת קפואה.
7. להוסיף ADB probes בטוחים, ולתעד בנפרד בדיקות שאי אפשר לבצע אוטומטית. אין להכניס בדיקות שינוי שעה/Force stop/Recovery למכשיר בלי אישור ברור ומנגנון החזרת מצב.
8. להציע למשתמש אישור לפני כל שלב משנה המשפיע על אפליקציה מותקנת או מעורבות משתמש. לעדכן `docs/TEST_PLAN.md`, `docs/WORK_LOG.md`, `docs/HANDOFF.md`, ולהבחין בין מקור שנכתב, build PASS, instrumentation PASS ו־QA ידני.

## 6. גבולות עבודה מחייבים

- לא לייצר Stable 6 בלי בקשת משתמש.
- לא לעשות בדיקות או בנייה בשיחה שהמשתמש הודיע שהיא הסתיימה.
- אין להריץ סקריפט גרסה ישן ללא התאמה.
- אין להתקין APK רגיל, לאפס הרשאות או למחוק נתוני Classes/Sounds לצורך בדיקות ללא צורך ואישור.
- לא להשתמש בענף Stable לצורכי בדיקות ופיתוח; לבדוק snapshot לפני כל שינוי GitHub.
- תקשורת בעברית, הוראות קצרות ומדויקות; כל פקודות CoGo מתחילות ב־`cd` המפורש; לא לחזור על צעדים שכבר הוכחו.
- המלצה לתפעול: בשיחה הבאה להתחיל ב־**אישור שלב 2**, ואחריו תיקון הבדיקות הבלתי תואמות והכנת מסלול הרצה אוטומטי מרוכז. לא לבקש מהמשתמש לחזור על בדיקות שביצע ללא הצדקה.

## 7. קישורי מקור

- Repo: https://github.com/AviY99/Atelier-Time
- Stable 5: https://github.com/AviY99/Atelier-Time/tree/stable/android-d114
- Stable 4: https://github.com/AviY99/Atelier-Time/tree/stable/android-d108
- Development: https://github.com/AviY99/Atelier-Time/tree/android-native-kotlin-compose
- Master handoff: https://github.com/AviY99/Atelier-Time/blob/android-native-kotlin-compose/docs/HANDOFF.md
- This frozen-stage handoff: https://github.com/AviY99/Atelier-Time/blob/android-native-kotlin-compose/docs/HANDOFF_STABLE5_D114_TO_AUTOMATED_QA_2026_10_25.md
