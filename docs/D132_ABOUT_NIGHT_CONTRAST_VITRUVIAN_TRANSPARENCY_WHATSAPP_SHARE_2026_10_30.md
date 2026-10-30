# D-132 — About Night contrast, Vitruvian matte and WhatsApp share (2026-10-30)

## User requests and background
User installed D-131, supplied screenshots of Night and Day About, identified Night's dark-on-blue links and the white opaque extra background surrounding the Vitruvian picture. They requested corrections. The follow-up **adds a third card beneath Privacy Policy and Instagram**, framed and surfaced exactly like these cards, labeled **Share with a friend**, with a WhatsApp icon that opens WhatsApp sharing of the app's Google Play link.

## Implemented in D-132 source (versionCode 26, targetSdk 36)
1. Existing Privacy Policy and Instagram TextButton links keep their existing Day brown accent but take Night's white `onSurface` color instead of dark-blue `primary`. No change to link targets or text.
2. The original local 320px Vitruvian WebP from D-131 stays untouched and offline. For display only, a remembered in-memory Android bitmap removes near-white matte pixels and partially transparent edges, leaving the image/circle/parchment details. No new INTERNET permission, file writes, resource download or unrelated visual changes.
3. New bottom `AtelierAboutWhatsAppShareCard` uses the same `OutlinedCard` and padding as the existing footer frames, with left text and right WhatsApp icon from the CC0 Simple Icons SVG path. UI test tags: `about_share_card`, `about_whatsapp_share`.
4. Tap calls Android `Intent.ACTION_SEND` (`text/plain`, user-driven only) containing `Atelier Time — art studio timer for artists` and **https://play.google.com/store/apps/details?id=com.ateliertime.android** (matching the app's fixed `applicationId`). First tries WhatsApp `com.whatsapp`, then WhatsApp Business `com.whatsapp.w4b`, then Android sharesheet. No contact access, app package queries, or automatic sending. Only the user chooses recipients/sends.
5. **Google Play has not published this app**, so this Play detail link is a **future canonical URL**; it cannot be relied on as a working download page until actual Google Play publication. A brief caution is visible in the new card. Do not claim that public link or successful WA share has been device-tested.

## Delivery gates
- D-132 CODE PREPARED ONLY, **not built, installed or device QA-verified**. D-131/code25 remains current user-installed version (screenshots show two visual issues). D-130/code24 remains last version with explicit full user acceptance; frozen Stable 9/D-126 historical 285/285 PASS unchanged.
- Local Samsung Code on the Go only, working dir `android-native`: `git pull --ff-only && bash tools/build_d132_about_night_and_whatsapp_share_on_phone.sh`. Script assembles debug APK and writes build log and hash manifest to Downloads, no auto-install, no GitHub Actions/paid CI, no old QA reruns.
- Only after local BUILD PASS: authorize and do an **in-place update over D-131**, never uninstall/clear data. Focused QA: two original links readable/clickable in Night and Day; Vitruvian image shows the actual card blue/lilac at transparent outside edges; Share card third with matching frame and correct label/icon; tapping icon opens WhatsApp composer with exact Play URL but does not send; verify fallback when WhatsApp not installed if applicable; Saved Classes and Sound settings preserved.
- No changes to timer, FGS, notifications, scheduling, audio, class history, privacy statement or ASO-01. Do not call D-132 Stable 10 or report Play publication.

## Source references
Android share intents: https://developer.android.com/training/sharing/send
WhatsApp logo path: https://raw.githubusercontent.com/simple-icons/simple-icons/develop/icons/whatsapp.svg (Simple Icons; CC0 artwork, brand trademark retained by owner)
