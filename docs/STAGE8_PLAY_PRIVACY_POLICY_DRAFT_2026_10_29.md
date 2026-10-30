# Atelier Time — Google Play privacy policy draft (NOT YET A PUBLISHED POLICY)

> **2026-10-30 D-128 source-audit clarification (DRAFT, not public):** Local JSONL Run diagnostic reports include the approved Class name/rules and events, sound selection metadata (but not user-selected audio content URIs), plus **Android API version, device manufacturer/model, app version and timezone**. Files stay in app-private storage unless a user explicitly chooses Export and a document-provider destination; that chosen destination may be managed by another app or cloud provider. The final public policy must disclose this local processing and user-directed export transparently. This draft has NOT been published or approved for release; see [D-128 Data safety source review](D128_PLAY_PRIVACY_DATA_SAFETY_READINESS_2026_10_30.md) and [approved `specialUse` rationale](D128_SPECIALUSE_PLAY_CONSOLE_DECLARATION_AND_RATIONALE_2026_10_30.md). No code/UI/Play action taken.

**Draft created 2026-10-29 for Stage 8 policy preparation.** Do not submit this Markdown file to Play as the live privacy-policy webpage. Before submission, the publisher must verify the final release binary/SDK manifests, add a valid developer privacy contact/method, publish a non-editable publicly accessible HTTPS webpage, link it in Play Console, and make policy text/link accessible in the app. This draft does **not** authorize a network service or an app code/UI change.

## Draft privacy policy text (English for prospective store submission)

**Atelier Time — Privacy Policy**

Atelier Time is an offline class-timer app for painting and sculpture sessions. It does not require an account. When installed for the first time, it has no previously saved classes or run history of that user.

**Information processed locally:** Classes and schedule choices you save, sound selections (including references to audio files you explicitly choose), and Run diagnostic histories generated when you use the timer. These are stored in the app's private storage on your device to support app functions, history and troubleshooting.

**Data collection and sharing:** The reviewed development version has no in-app advertising, analytics upload, cloud sync or developer-server transmission. Atelier Time does not automatically transmit the saved classes, sound choices or Run reports to its developer. If you explicitly export a Run report, Android lets you select the destination; you control any subsequent sharing of the exported file.

**Permission and access:** On compatible Android versions, Atelier Time may ask your permission to display active-Class status notifications; declining notifications does not prevent the Class timer or sound cues. The app can keep the display on during an active timer when you turn on that option. You can select sounds built into the app, Android system sounds, or specific personal audio files through Android's system file selector. The app does not request broad storage access.

**Retention and deletion:** Data remains in app storage for ongoing use across ordinary app updates unless you choose to delete it or remove the app. You can delete saved content and Run reports using relevant in-app controls. There is no obligation or special feature to preserve app-private history after uninstall. Explicitly exported files are outside app-private storage and can be deleted at their chosen location.

**Security:** The reviewed version stores saved content in private Android application storage and disables Android app backup. User-chosen file access uses Android's document selector rather than broad storage permissions.

**Developer and contact:** [PUBLISHER PRIVACY CONTACT / CONTACT MECHANISM TO BE SUPPLIED BEFORE RELEASE]

**Last updated:** [FINAL PUBLICATION DATE]

*The final public text and Play Data safety statement must reflect the actual published binary, bundled libraries, and privacy practices. No Play submission has been made.*
