# Atelier Time — Optional post-release upgrades: Studio Display, Mirroring and Remote Control

**Decision date:** 2026-11-08. **Classification:** **OPTIONAL FUTURE UPGRADES / RESEARCH APPROVED, IMPLEMENTATION NOT APPROVED OR STARTED.**  
**Baseline:** Stable 17 / D-157 / versionCode 51, immutable recovery branch `stable/android-d157-qa17-user-accepted` at `f6866dca809f6214c6d1f88c1adf8f6592163ada`.  
**User decision:** Accepted the recommendation to publish the currently accepted app first and investigate/build both larger external display and remote timer control afterwards. These items are **NOT blocking requirements for the first Google Play release or invite-only Internal Testing**. They must not delay Play readiness, signing, or initial distribution.

## Two desired capabilities (both are options, not current app features)

1. **Display / Mirroring (external screens):** Show the running Atelier Time countdown on a desktop computer, smart TV, projector, tablet or another phone, while primary control stays on the instructor's phone. **Immediate no-code option:** test native Android/Samsung screen mirroring (Smart View, compatible Google Cast, external HDMI) or desktop `scrcpy`/compatible phone-to-PC facilities; availability depends on specific sender/receiver support, Wi-Fi/network, Android security and screen-lock behavior. Mirroring reproduces the phone UI, may have black bars and may be interrupted when the phone sleeps; **no new native app functionality is implied**.
2. **Optional purpose-built Studio Display + Remote Control:** A readable large-screen *view-only* timer display showing remaining time, current session/pose, next item and run/pause state; optionally a paired **control-authorized** desktop or second phone can issue Start/Pause/Resume, Next, Restart (and other explicitly permitted commands) to the **primary-phone timer**. Prefer one authoritative running timer and state snapshots instead of independent drifting countdowns or different schedules. Consider opening a browser view through a QR/local network session link so other devices do not necessarily install the Android app. Design/technology choice is **not yet made**.

## Research and safeguards required before implementation

- Investigate whether ordinary OS mirroring is adequate using the current D157 APK. Check latency, portrait-to-landscape usability, selected-audio routing and alerts, phone screen lock, background operation, Wi-Fi dropouts and reconnect; do not assume Smart View/Google Cast or Android-to-Android receivers are universally supported.
- For dedicated remote displays/control, evaluate local Wi-Fi browser-client architecture versus other supported transports and compatibility with smart TVs. Pair with explicit instructor consent, short-lived session code/QR and distinct **VIEW** vs **CONTROL** roles; allow revoke/disconnect. Do not expose classes, diagnostic logs or other personal data to unpaired screens.
- Primary phone remains the time/audio/state authority. Avoid duplicate sound cues or timer drift. Remote commands require deterministic validation/acknowledgment and conflict handling; interrupted network must **not pause or reset the active timer**. Establish behavior during Next/Restart/replay, low battery, device lock, host app background, multiple viewers and competing controllers.
- Android network permissions (including local-network behavior depending on targeted Android API/version) and possible Google Play Data Safety/Privacy Policy disclosures must be re-reviewed **only when implementing a networked feature**. Do not add unnecessary permissions or internet connectivity during the first Stable17 release.
- Maintain existing Saved Classes/Sounds/history, the Android foreground timer/notification rules and approved artwork. New device QA and full JVM suite, AndroidTest where feasible, signed-bundle inspection and user acceptance are required for any future new version.
- Future implementation must use a **new DEV revision** (possibly D158 or later; **no number assigned now**), never edit Stable17 or previous frozen branches. No code change, cloud build, paid CI, release artifact, device installation or Play upload is authorized by this planning decision.

## Agreed order and gates

1. **Current / mandatory for first release:** Complete Stable17 Code51 Google Play Console readiness, policy/FGS/Data Safety verification, locally signed AAB with separately approved signing and invite-only Internal Testing. Public Production publication is a separate gate.
2. **Optional exploratory tests, no app code:** Test existing D157 mirror to compatible Windows PC / smart TV / projector; if possible test remote desktop control through PC mirroring. Report device-specific limitations.
3. **Optional later product specification:** Define Studio Display layout and visibility, pairing and role rules, whether LAN-only is acceptable, network/disconnect behavior, compatible TVs/phones, audio destination and UI/UX. Get explicit approval before implementation.
4. **Optional later implementation and QA:** Implement viewer first, optionally remote controller second, test on real paired devices, then freeze the next user-accepted stable and publish a separate Google Play update with a higher versionCode; no need to rebuild the first release just because these features are planned.

**Priority/status:** `OPTIONAL_POST_RELEASE` · `RESEARCH_ONLY` · `NOT_IMPLEMENTED` · `NOT_A_PLAY_LAUNCH_BLOCKER`.

**Primary Play handoff:** [Stable17 → Google Play](HANDOFF_CURRENT_STABLE17_TO_GOOGLE_PLAY_2026_11_07.md).  
**Current acceptance:** [Stable17](STABLE17_D157_VOLUME_AND_SOUND_USER_ACCEPTANCE_2026_11_07.md).  
