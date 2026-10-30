# D-133 — Compact About footer cards and reliable WhatsApp sharing (2026-10-30)

## Device evidence and requested fixes
After D-132 was locally built and installed, the user supplied paired Day/Night About screenshots. Screenshot shows Version D-132, Night links clearly readable, Vitruvian icon with outer white matte removed, and new **Share with a friend** WhatsApp frame as requested. User explicitly confirmed **Saved Classes, Sounds and all other app functions remain operational**. Two specific problems remain:
1. New third frame reaches under the bottom navigation area on this device. Slightly reduce **Privacy Policy and Instagram** card heights so all three fit in the usual About view; preserve frame styles, text and links.
2. Tapping WhatsApp's icon does not open WhatsApp; correct sharing behavior to launch WhatsApp's recipient picker with a prefilled temporary message and the **actual Instagram profile URL**, not the unpublished Play listing.

## D-133 implementation — source-only
- versionName **D-133**, Android versionCode **27**, compileSdk/targetSdk **36**. No new permissions.
- In `AtelierAboutExternalLinkCard`, reduce vertical padding **14dp → 8dp** and inter-element spacing **6dp → 2dp** for both existing Privacy/Instagram footer frames. Horizontal padding, typography, full content, link color, destinations, Instagram avatar and outline style stay unchanged.
- Entire third `OutlinedCard` receives a click action, preserving the original 56dp WhatsApp IconButton's separate click target, so touching anywhere in the card initiates sharing.
- Replace D-132's package-restricted `ACTION_SEND` first-attempt with documented WhatsApp **`https://wa.me/?text=`** prefilled-message `ACTION_VIEW` addressed to `com.whatsapp`, then `com.whatsapp.w4b`. If neither can handle it, open `Intent.createChooser(ACTION_SEND, "Share Atelier Time")` as Android share-sheet fallback. Catch unavailable app/security errors, show brief toast only if no sharing target can open.
- Text shared in D-133:
```text
Atelier Time — an art studio timer for artists
Follow the developer on Instagram:
https://www.instagram.com/aviyosfan/
```
- Absolutely **do not** substitute a Play details URL until the store listing is actually published/verified. Sending remains solely the user's manual action after picking the recipient.
- No change to original Night contrast fix, edge-only Vitruvian matte correction, Day layout, timer scheduling, notifications, FGS, audio or data storage.
- References: https://faq.whatsapp.com/5913398998672934 (WhatsApp's wa.me/?text=); https://developer.android.com/develop/ui/compose/sharing/send (Android ACTION_SEND/share sheet).

## Status and QA gates
- **D-133 SOURCE READY ONLY. NOT BUILT, INSTALLED OR DEVICE-TESTED.**
- Installed **D-132** is confirmed by screenshot and user. Saved Classes/Sounds and other app behavior user-confirmed, but WhatsApp tap explicitly failed and third card overflowed; therefore D-132 is not fully accepted.
- Last fully user-accepted development version before those incomplete About checks was D-130; immutable Stable9/D-126 is last explicitly designated stable checkpoint.
- Build **locally in Samsung Code on the Go only** from `android-native`: `git pull --ff-only && bash tools/build_d133_compact_about_whatsapp_on_phone.sh`. Only `:app:assembleDebug` to Downloads; no install, paid Actions, cloud Android build or QA suites.
- After build PASS, request separate approval for in-place upgrade over D-132; **never uninstall or clear data**.
- Targeted user checks: Day/Night three cards visible, bottom Share frame fully in usable About area at standard font; Privacy/Instagram URLs still open; tapping WhatsApp icon **and** the third card opens WhatsApp chat recipient chooser with exact three-line payload; do not send for QA; Saved Classes/Sounds preserved.
- Overflow in larger font/device sizes is still handled by existing vertical scrolling; no forced shrink of dynamic font/scaling.
- No Stable 10 promotion or store release implied.
