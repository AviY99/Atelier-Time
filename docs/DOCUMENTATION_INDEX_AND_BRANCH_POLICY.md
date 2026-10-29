> **Latest Stage 7 development update (2026-10-29):** D-127/code21 accessibility source has further responsive Editor/Free wheel and TalkBack improvements, plus **five authored but unexecuted new Android UI tests**. CoGo build-only runner committed, NOT EXECUTED. Stage 7 device/compile/visual/TalkBack QA still PENDING; accepted installed Stable 9 remains D-126/code20. [Stage 7 evidence and gates](D127_STAGE7_ACCESSIBILITY_ADAPTATION_AND_QA_2026_10_28.md).

> **Current development candidate:** Stage 7 accessibility D-127/code21 source on Android dev, **QA/build/installation pending**, no new stable designation. See [Stage 7 QA record](D127_STAGE7_ACCESSIBILITY_ADAPTATION_AND_QA_2026_10_28.md). Main remains documentation/history entrypoint; installed accepted Stable 9 remains D-126/code20.

# Atelier Time — תיעוד קנוני, ענפים והעברת שיחה
# Atelier Time — canonical documentation index and branch policy

**Updated:** 2026-10-29. **Current user-approved stable:** **Stable 9 = Android D-126 / versionCode 20**; **285/285 automated tests PASS**, user-confirmed Saved Classes and Sounds preserved after tests. This is a QA designation of the already installed D-126 app, not a new APK build or Android version bump.

## Start here — one reliable reading order

1. **[Current status + compact cross-chat handoff](CURRENT_STATUS_AND_HANDOFF_STABLE9_2026_10_28.md)** — ~6 KB: current release, what is actually installed, QA evidence, what is approved or rejected, the next task, and no-repeat safety rules. **PRIMARY entrypoint for every new chat.**
2. **[Cumulative handoff and technical history](HANDOFF.md)** — long historical record; the current top section takes precedence over earlier dated "latest" entries. Use only when a specific historic detail matters.
3. **[Decisions](DECISIONS.md)** — accepted, rejected, open product/engineering decisions. The newest decision supersedes a conflicting older historical statement.
4. **[Roadmap](ROADMAP.md)** — completed gates and next stages; do not treat older "not yet built" paragraphs as current.
5. **[Test plan](TEST_PLAN.md)** — what tests actually ran, what they establish and what remains unverified.
6. **[Latest immutable Stable 9 acceptance certificate](STABLE9_D126_285_AUTOMATED_QA_ACCEPTANCE_2026_10_28.md)**, plus [42/42 real-device QA evidence and data preservation](STABLE8_D126_ANDROID_INSTRUMENTATION_QA_PASS_2026_10_28.md).
7. **[Current architectural overview](ARCHITECTURE.md)** and **[product requirements](PRODUCT_SPEC.md)**; for live scheduling semantics prefer [unified D-124 planner contract](D124_UNIFIED_CONSTRAINT_FUTURE_PLANNER_2026_10_28.md) over superseded Dart/Flutter examples.
8. **[Chronological change log](../CHANGELOG.md)** and **[work log](WORK_LOG.md)** — audit trails, not substitutes for the current-status handoff.

Other version-specific design, test and handoff documents remain historical evidence and are linked from the current documents when relevant; they are **not** all independent authorities.

## Git branches: what belongs where

| Ref | Role | Android source | Documentation | Policy |
| --- | --- | --- | --- | --- |
| [`main`](https://github.com/AviY99/Atelier-Time/tree/main) | **Default project entrypoint / general cumulative documentation / earlier historical work** | **Not the authoritative current D-126 Kotlin/Compose source.** Contains legacy project history; not auto-merged with Android dev. | Keep the concise current-status page, handoff pointer, latest acceptance and QA certificates, decisions, roadmap, test plan, product/architecture and documentation map up to date; preserve older files as historical archives. | **Docs-only targeted updates** for release/handoff sync; no broad merges or claims Android main binary is released from here. |
| [`android-native-kotlin-compose`](https://github.com/AviY99/Atelier-Time/tree/android-native-kotlin-compose) | **Only active Android development branch** | **Canonical current implementation**, JVM/Android tests, Compose UI, Gradle scripts, new changes after user authorization. | Working/canonical current documents: status, handoff, decisions, tests, roadmap, detailed feature QA and release evidence. Keep shared current documents synchronized with `main`. | New work and fixes here. Distinguish source-written, built, installed and user-accepted QA. |
| [`stable/android-d126-qa9-tests-complete`](https://github.com/AviY99/Atelier-Time/tree/stable/android-d126-qa9-tests-complete) | **Latest accepted Stable 9 recovery/checkpoint snapshot** | Exact approved D-126 production source plus committed test-only QA corrections, not a new APK/changed runtime. | Snapshot of evidence/handoff **as of freeze**; may not include later administrative cross-project documentation index. | **IMMUTABLE**: do not add docs, develop or force-push after freeze. New evidence on dev + main and a new stable checkpoint only upon explicit release designation. |
| [`stable/android-d126-qa8-logic-diagnostics`](https://github.com/AviY99/Atelier-Time/tree/stable/android-d126-qa8-logic-diagnostics) | Historical Stable 8 (D-126, logic + diagnostics) | Frozen | Frozen | Do not modify. |
| [`stable/android-d124-qa8`](https://github.com/AviY99/Atelier-Time/tree/stable/android-d124-qa8) | Historical original Stable 8 (D-124, unified logic only) | Frozen | Frozen | Do not modify. |
| Older `stable/*`, `stage-*`, `spike-*` | Historic milestones/experiments | Period-specific | Period-specific | Preserve, do not retrofit newer status or treat as active work. |

**Answer to "Does main accumulate all handoffs?":** `main` **accumulates the latest *canonical current status* and links to handoffs/release certificates**, along with retained project history. It is **NOT a dump of every intermediate Android source/build or every temporary handoff**. The full in-progress source and its detailed work records live on the development branch. Immutable stable branches preserve the state at their individual acceptance dates; they are **not updated retroactively**.

## Release versus branch versus application version

- **Stable ordinal** is a user-approved QA checkpoint (Stable 9).
- **Android versionName/versionCode** identify the installed binary (`D-126` / `20`). Stable 8 → Stable 9 did **not** install another APK.
- **Build-source SHA** identifies the exact application compiled on the Samsung (`b2d47ca32c2f470d38e556ea7a571afaa89400e3`).
- **Test-only source SHA** identifies the corrected AndroidTest suite that passed 42/42 (`07816e22014e2ff6593757cbd33879344ada1e80`).
- **Stable branch commit SHA** identifies the archived source/test/docs snapshot, not the APK's binary hash. All identities must be separately recorded and must never be conflated.
- The finished Stable 9 gate: **243/243 JVM + 42/42 connected Android = 285/285 PASS; 72 Gradle tasks PASS; both APK SHA-256 comparisons OK; manual post-test Saved Classes/Sounds PASS**. Prolonged Doze/process reclaim, multi-device accessibility, store-policy and signed Release still remain separate untested stages.

## Mandatory documentation workflow — each approved change

1. **Before changing behavior**: record a decision and acceptance criteria in `docs/DECISIONS.md` and the relevant feature/spec, on dev branch; keep Stable snapshots untouched.
2. **When implementing**: change Android code/tests only on dev; update source-focused documentation and `docs/TEST_PLAN.md`, with status **SOURCE / QA PENDING**, never claim PASS prematurely.
3. **After real tests**: record device/log evidence, test count, SHA/version, any failures and preservation check in a feature-specific QA document, and update the current top sections of `docs/HANDOFF.md`, `docs/ROADMAP.md`, `docs/DECISIONS.md`, `docs/TEST_PLAN.md`, `CHANGELOG.md` and `docs/WORK_LOG.md` as applicable. Historic failures remain in chronology.
4. **After user accepts a new Stable milestone**: create a dated certificate and **new immutable** `stable/...` branch from verified dev source/docs. Do not use `main` as the source of the Android APK.
5. **Synchronize to `main`**: copy only relevant current canonical markdown and certificates from dev, update root `README.md` to point to the compact current-status handoff and this index. **Do not merge dev→main wholesale**, overwrite legacy history or modify frozen stable refs.
6. **Verify**: check same Git blob SHA (identical contents) for the shared current handoff, decisions, roadmap, test plan, release certificate and current-status page in both `main` and dev; list any intentionally branch-specific docs. Confirm `main` and dev still point to expected separate code, and installed phone data remains intact.
7. **New chat**: open this index + current-status handoff **first**; review latest accepted version, next step, unexecuted tests, safe install/data rules. Avoid repeating settled questions or old QA.

## Next planned work from Stable 9

7. Accessibility / device layouts (font scaling, screen sizes, contrast, touch targets, TalkBack).
8. Android platform security/privacy, foreground/background service permissions and release-policy review.
9. Signed release build, AAB and upgrade/data migration acceptance.
10. Approved launcher icon, branding and store artwork.
11. Google Play listing, internal/closed testing, separate publication decision.
12. Later iOS / desktop feasibility without a speculative timer rewrite.

**Safety**: no main-app uninstall, `pm clear`, Saved Classes/Sounds deletion, unsolicited ADB pairing, reinstall or new APK just to edit docs. Stable 9 remains frozen unless the user expressly authorizes a new milestone.
