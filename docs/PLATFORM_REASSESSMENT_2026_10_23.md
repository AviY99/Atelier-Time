# Atelier Time — platform and toolchain re-assessment (2026-10-23)

> **2026-10-23 — HISTORICAL D-061 CONSTRAINT SUPERSEDED BY D-062:** User relaxed the universal native-build-host requirement. **Current hard priority** is fast and efficient Android development/preview/testing/building **directly on the Android phone**, keeping iOS/Windows/macOS later ports efficient through shared code; future OS-specific builder availability is acceptable and **not a day-one blocker**. The analysis below documents why the original all-host requirement failed, but is NOT the current product gate. See [new Android-first plan](ANDROID_FIRST_PLATFORM_SPIKE.md), including a Dart code-reuse browser proof and a phone-built Capacitor APK proof before any framework approval.


> **LATEST 2026-10-23 D-062 USER REVISION:** Strict native-build-all-four-OSs-now constraint is NO LONGER current. Revised goal: efficiently develop/debug/build/install **Android first directly on user's phone**, design for efficient LATER iOS/Windows/macOS adaptation. D-063 proposes **Kotlin + Code on the Go** as a candidate requiring actual installed APK before migration; see [ANDROID_FIRST_DEVELOPMENT_PLAN.md](ANDROID_FIRST_DEVELOPMENT_PLAN.md). This earlier comprehensive lifecycle audit is retained historically, but its immediate multi-target feasibility STOP condition is superseded as a blocker to **Android-only proof**.


**Status: FEASIBILITY AUDIT / PLATFORM SELECTION REOPENED — NO replacement stack approved.**
**Trigger:** User explicitly rejected the original platform assessment: the system must be cross-platform **throughout development, tests, builds, and deployment**, not merely at runtime. All four original end-user OS targets remain required.

## Executive conclusion

The approved Flutter+Dart decision in [TD-001](TECH_DECISION_001_PLATFORM.md) was supported for the **running application** but was **not validated for the complete development-to-release toolchain**. This is an engineering and process failure, not a user installation problem. Freeze new Flutter UI expansion; **do not delete verified Dart core code or its test evidence**. Do not declare a replacement technology approved until an end-to-end route is proven.

There is a critical feasibility limit: **building/signing/distributing native iOS applications still needs macOS/Xcode and Apple distribution prerequisites**, and Flutter native Windows/macOS applications likewise need their relevant host toolchains. Alternative native frameworks (Capacitor and Tauri) do **not** remove those toolchain restrictions. If the requirement is interpreted as “create signed native Android, iOS, Windows, and macOS installables locally on any arbitrary OS, including Android Termux, without remote builders,” **none of the verified options meets that condition**. The requirements must either permit per-target remote/host builders, or permit installable browser PWAs instead of native installer packages, or be renegotiated. Do NOT falsely present a workaround as officially supported.

## Requirements to evaluate as a single indivisible contract

| ID | Hard acceptance requirement | Evidence needed before new stack approval |
|---|---|---|
| F01 | One maintainable source/application behavior across Android phones/tablets, iOS/iPadOS, Windows, macOS | Same product/features and regression fixtures, with documented limited platform adapters |
| F02 | Development workflow accessible on every needed developer environment, **including user's Android Termux** | Reproducible edit/build-preview/test steps from Android; no dependency on unsupported SDK hacks for the standard workflow |
| F03 | Automated tests executable and actionable from that development workflow | Successful parser/core/UI tests with actual logs, not planned tests |
| F04 | Reproducible distribution/installation for **all four** OS families | Describe and successfully produce each installation artifact or install pathway, including signing/accounts/host/remote runner |
| F05 | Offline usability and locally stored named presets | Real offline and persistence/restart testing per target |
| F06 | Reliable phase timing and three distinct audible cues, including lock/background requirements | Real OS/device acceptance checks; no browser background-timer assumptions |
| F07 | Budget-safe, repeatable CI/build without unexpected cloud expenditure | Cost model, runner ownership, hard limits, plan when account budget exhausted |
| F08 | Preserve approved schedules A/B, D-031 25/5 warnings, D-036 positivity, D-037 placement and D-044 positive special breaks | Port/regression oracle tests; no silent behavioral redesign |

Note **F02** is a new explicit hard interpretation supplied by user: portability covers authoring/testing/releasing workflow, not only target binaries. **F04** has two distinct valid meanings (“install via browser home-screen/PWA” versus “independent signed native installation package”) and the product specification currently requires the latter. This difference is consequential; do not choose one silently.

## Candidate evaluation against all lifecycle stages

| Candidate | Develop / preview / test from Android Termux | Four end-user OS families | Signed native per-OS installers | Background/locked-device alerts | Assessment |
|---|---|---|---|---|---|
| **A. Flutter+Dart with official SDK** | **NO**: Android is not an officially supported Flutter development host; current Termux runs Dart core only. Community Termux Flutter ports are unofficial, not a supported release pipeline. | **YES** supported deployment targets | **Requires** macOS+Xcode for Apple and OS-specific desktop builders | Possible with OS-specific work and real device tests; **unproven** | **Fails F02 as locally required**, unless remote official hosts become accepted |
| **B. TypeScript web app / offline PWA** | **PLAUSIBLE** via Node/npm/Vite on Termux; **must demonstrate**, not assume | **YES** modern-browser/PWA user experience on four OS families | **NO** independent native APK/IPA/EXE/DMG by default; installed web app instead | **Risk**: browser hidden timers are throttled/suspended; wake lock may help while visible, not a reliable background alert guarantee | Only meets contract if native-artifact and locked-device behavior are relaxed or solved and verified |
| **C. Web source + native bridges** (PWA + Capacitor mobile + Tauri/Electron desktop) | Web source/tests possible from Android with suitable Node; native packagers **not supported there across all targets** | Potentially yes with multiple platform wrappers | Still macOS/Xcode for iOS, Windows tooling for native Windows and appropriate macOS packaging/signing | Can implement via native plugins, but must verify per OS | Good candidate **only if remote platform-specific builders are permitted**; additional maintenance |
| **D. React Native + Expo / desktop extensions** | Expo web/JS editing possible, native build in Expo remote service (quota/cost); not identical native desktop workflow | Android+iOS primary; Windows/macOS require extra integrations not provided as the same standard Expo output | Requires EAS or OS-specific desktop/Apple builders | Native APIs may help with correct permissions | Does not remove host/build restrictions; more desktop maintenance risk |
| **E. Flutter with cloud dev/CI on official hosts** | Android used as **remote client**, not as local official Flutter SDK host; usable only with provided remote machines/network/budget | Yes as in A | macOS and Windows remote builders, signing accounts and artifacts required | Must be tested | Technically possible architecture if remote infrastructure and cost limits are approved; **not proven here**, and user reported GitHub budget blocked |

No candidate is certified end-to-end. Do not infer that “single codebase” means “one machine can build signed binaries for every OS.”

## Verified source facts (official documentation)

1. [Flutter official FAQ](https://docs.flutter.dev/resources/faq): supported development hosts are **Windows, macOS, Linux, ChromeOS**, not Android/Termux. [Supported deployment platforms](https://docs.flutter.dev/reference/supported-platforms) lists Android/iOS/macOS/Windows for execution. These are different notions.
2. [Flutter platform integration](https://docs.flutter.dev/platform-integration): iOS and macOS setup macOS-only; Windows setup Windows-only; target-specific toolchains.
3. [Flutter iOS deployment](https://docs.flutter.dev/deployment/ios): app build/release **requires Xcode and macOS**.
4. [Tauri prerequisites](https://v2.tauri.app/start/prerequisites/): macOS/Xcode for iOS, Windows C++ tooling for Windows, Android SDK for Android.
5. [Capacitor setup](https://capacitorjs.com/docs/getting-started/environment-setup): iOS builds require macOS/Xcode; optional cloud builders exist but are external infrastructure.
6. [MDN PWA installability](https://developer.mozilla.org/en-US/docs/Web/Progressive_web_apps/Guides/Making_PWAs_installable) and [installing](https://developer.mozilla.org/en-US/docs/Web/Progressive_web_apps/Guides/Installing): browsers can install PWAs on modern mobile and desktop systems, but a PWA is **still a browser-based app**, not a platform-native installation package; conditions vary by browser/OS.
7. [MDN Page Visibility API](https://developer.mozilla.org/en-US/docs/Web/API/Page_Visibility_API): background/inactive browser timers can be throttled. [Screen Wake Lock](https://developer.mozilla.org/en-US/docs/Web/API/Screen_Wake_Lock_API) helps prevent display sleep **while active**, not guaranteed background scheduling.
8. [Apple background execution](https://developer.apple.com/documentation/xcode/configuring-background-execution-modes): normal iOS apps are typically suspended in background; special modes are constrained. Must prove required alerts on actual devices.
9. [Expo cloud build](https://expo.dev/services/build): remote native iOS/Android builds possible without local Mac; platform is an external service with cost/availability considerations, and not a native Windows/macOS output solution.
10. [Termux Node package](https://github.com/termux/termux-packages/blob/master/packages/nodejs/build.sh): Node/npm source-tooling is available for on-device web development; feasibility of our specific package stack remains **untested**.

## Consequences for the existing repository

- **Keep and safeguard:** approved product requirements and exact Examples A/B, 35/35 screenshot-confirmed Dart scheduler tests on the Stage 1 branch, 11/11 screenshot-confirmed Dart form-adapter tests on the correct Stage 2 commit. Their success is real and not invalidated by Flutter SDK host limitations.
- **Freeze:** adding more Flutter UI/widget code on `stage-2-session-editor`, draft PR #2, and any automatic PR merges/builds. Eight Flutter widget tests have never run.
- **No automatic migration:** Choosing a TypeScript/web route would require **porting** scheduling core and fixtures; the validated Dart code cannot simply run as TypeScript. Cost/time of port must be evaluated before approval.
- **Reopen TD-001 / D-019 only on feasibility grounds:** Historical approval stands as recorded, but **Flutter cannot be claimed validated for the revised end-to-end requirement**. This audit does **not** itself select, implement or approve a replacement.
- **No spend:** no GitHub Actions/paid CI/cloud build without explicit permission and verified cost ceiling.

## Required new platform decision gate

**Gate 0 — literal compatibility:** Is the hard requirement (a) one fully local build machine on **every** development OS, including Android, producing **native signed artifacts for all four targets**, OR (b) a unified workflow **operated from any device** with documented platform-specific/remote builders as needed? These are different. The former is not met by any verified candidate, primarily because of mandatory Apple native build/signing infrastructure.

**Gate 1 — installation semantics:** Must each OS have its own independent signed native installer as the approved spec currently says, or is a truly offline-capable, browser-installed PWA accepted? Do not silently relax.

**Gate 2 — timing semantics:** Determine required behavior with screen off, background, and lock-screen for each OS, and prove timing and three distinct audio alerts; PWA may be unacceptable.

**Gate 3 — empirical proof before coding:** A small feasibility spike must demonstrate supported authoring/test workflow from Android Termux or an explicitly accepted remote environment; a working install path on all four end-user systems; offline behavior and timing/alert constraints; and explicit build/account cost. Record actual commands, failures, devices, artifacts and links. **No longer draft a substantial UI first and test SDK later.**

**Current decision: STOP PLATFORM-SPECIFIC IMPLEMENTATION until the user confirms any allowed relaxation or external build infrastructure.** Preserve the existing repository and documentation, no source deletions. Once clarified, make a verifiable technical selection and seek explicit approval.

### Accountability

The earlier platform review documented target OS support and acknowledged macOS/Xcode for Apple and Windows tooling, but failed to verify the development host available to the user and the entire build/release path before implementation began. The resulting delayed Flutter UI feedback, repeated Termux testing and confusion were preventable. This document corrects the acceptance criteria; it does not erase earlier history.
