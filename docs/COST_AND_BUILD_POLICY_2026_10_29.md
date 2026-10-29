# Atelier Time — cost-safe build, evidence and GitHub budget policy
**Established:** 2026-10-29. **Authority:** user directive after monthly GitHub budget overrun.

## Non-negotiable rules
1. **NO GitHub-hosted build, Android compile, instrumentation, CI pipeline, paid runner, Codespace or auto-trigger workflow** for this project. Never enable, recreate, dispatch or retry a GitHub Actions compile without explicit new user approval and budget confirmation. Use the user's existing Samsung Code on the Go (CoGo) environment for all Android builds.
2. GitHub is **source/version control and evidence storage only**. Do not assert cloud compilation is free or account charges are zero. Reuse completed Stable 9 D-126 285/285 PASS rather than repeating prior tests.
3. All build outputs and full raw logs remain local on the phone. Generate only privacy-safe summaries for cloud synchronization: Kotlin filename basename, line, fixed error category, version and build result; never upload the raw log, filesystem paths, tokens, device IDs or user data to the public repository.
4. The existing `tools/build_d127_accessibility_candidate_on_phone.sh` (build only, no install) and `tools/publish_d127_build_diagnostics.sh` (redacted reporting) remain the preferred phone-side tools. The reporting target is `diagnostics/d127-build-reports`; **phone Git push authorization is not configured/confirmed**, so automatic remote delivery is NOT complete. A future Google Drive connector + one-time Android folder synchronization could provide another authorized transport; no connector/sync is currently confirmed. Do not promise automatic remote access until a test upload and readback pass.
5. Never install an APK, uninstall, clear data, start instrumentation or alter a Stable checkpoint without appropriate approval. Stage 7 D-127 remains unaccepted until real device QA.

## CI shutdown and evidence
- Removed `.github/workflows/d127-android-compile.yml` on dev at `0f2aab90223d77e1a1c614431a1aaf3c4a305983`; no workflows should be introduced on this branch.
- The two old GitHub Actions runs #1 `40889107144` and #2 `40889219808` have both completed with FAIL; none active at shutdown. These past runs do not prove a billed amount or explain the user's full account overage.
- The existing Run #2 GitHub log was inspected **without launching a build**: `MelodySettingsSection.kt:158:22` and `:195:29`, both unresolved `testTag` references. Added missing `androidx.compose.ui.platform.testTag` import on dev at `6837de78741e1c56d9b7978096dd5cf064ca6477`. The fix is source-only, **not yet phone compiled**.
- An automated daily condition watch checks for any new Actions runs/workflows on this repository and notifies if found. **This is an activity guard, not a dollar-cost billing monitor.**

## Billing limits and scope
- GitHub's actual billing totals, usage and settings belong to the personal account's authenticated **Settings > Billing and licensing > Budgets and alerts**; connected GitHub repository tools currently lack account billing/budget endpoints. Therefore the assistant **cannot verify the current overage amount or enforce a GitHub account hard spending cap**.
- Account owner should set an explicit product-level Actions budget with **Stop usage when budget limit is reached** and enable budget alerts; inspect other metered product budgets too if this month's overrun was elsewhere. The GitHub billing budget controls metered charges; alerts alone do not hard-stop usage. Previously incurred charges may still appear. Never claim this has been configured until verified by the account owner.
- A daily workflow-run watch **cannot replace** these account billing controls. No new paid work or workflow should be started while the reported budget overrun is unresolved.

## Restart here
1. Read this policy first plus `docs/CURRENT_STATUS_AND_HANDOFF_STABLE9_2026_10_28.md`.
2. Validate that no GitHub Actions workflow is active and that Stable 9 remains immutable.
3. Compile locally on Samsung once the source-only import fix is pulled. No new GitHub Actions runs.
4. Resolve authorized **one-time** upload transport for sanitized phone diagnostics and prove it with a readback. Keep original raw logs locally; do not require repeated manual uploads.
