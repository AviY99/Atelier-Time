## QA-D122-05 — user reports timer logic acceptable; open intermittent audio-source mismatch (2026-10-28)

**User observation, not yet reproduced:** The user reports D-122 Next/Restart behavior generally tested and working on the installed phone. However, after listening to a song in Poweramp the previous evening, an Atelier Time Session cue played a snippet of that song the following day. Visiting Sounds and reselecting the Session sound restored the expected cue. Do not mark sound playback as fully accepted or declare a new Stable build without resolving or explicitly accepting this anomaly. D-119 Stable 7 branch remains frozen.

**Source review:** App melodies are synthesized in `MelodyAudioPlayer` via `AudioTrack`; system and user-file sounds use the saved URI and `MediaPlayer.setDataSource(context, Uri.parse(uri))` for every cue. Preferences persist `source`, `uri`, and `displayName` strings; a file selected via `OpenDocument` requests persistable read permission, while Android ringtone choices come from `RingtoneManager.ACTION_RINGTONE_PICKER`. The player also requests short-lived media/audio focus using `AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK`.

**Hypotheses, not conclusions:** A dynamic/stale external sound URI or provider mapping, versus an independent Poweramp playback/audio-focus interaction. Source inspection alone cannot identify which occurred. The user has reselected the sound, so the original persisted URI may no longer be accessible as evidence.

**Next diagnostic:** Ask whether the affected Session A cue was selected from Atelier Time (built-in), System sounds, or Audio file, and whether the song was audible only during the cue or resumed afterward. If built-in, investigate audio focus/external player rather than claiming a URI mismatch. If system/file, verify persisted URI semantics and sound content on next launch without changing the selection, and consider stable app-owned copy of user audio after explicit choice (or clear inaccessible-file error). Avoid speculative edits to timer logic, uninstall, or data clearing. No code changed in response to this sound report.

## QA-D122-04 — candidate SHA-256 verified on Samsung (2026-10-28)

The user ran the simplified verification after the old D-122 manifest `code=16` typo was discovered, and the screenshot explicitly shows `D-122 APK VERIFIED - PASS`. This confirms the existing `Atelier-Time-D122-Candidate.apk` SHA-256 equals the `app_sha256` stored in the original on-phone QA manifest. Earlier QA-D122-02 recorded 228/228 passing JVM tests and a successful build-only Android assembly.

**Evidence limitations:** The final simplified command checked hash equality only; it did not successfully inspect the APK's embedded package version or its signing identity. The build script's Gradle-source check required D-122/code17, but the old manifest incorrectly recorded code16. No APK installation or user acceptance is established by this screenshot.

**Next gate:** Request/obtain explicit permission before installing as an Android *update* (not uninstall). Android's installer must accept the package/signing compatibility without data clearing. If it refuses or requests uninstall, stop; do not delete existing Classes/Sound preferences. After installation, verify About D-122 and saved settings, then reproduce Next-added sessions + Restart + special-break position while Class end remains immutable. The D-119 Stable 7 branch stays frozen.

## QA-D122-03 — APK verification command failed due to manifest script typo (2026-10-28)

User ran the version/code/branch/hash verifier following the 228/228 build; no `APK VERIFIED — PASS` appeared. Inspection of `tools/build_d122_candidate_on_phone.sh` confirmed a toolchain metadata defect: it correctly requires `versionCode=17` in Gradle, but wrote `code=16` into the generated `Atelier-Time-D122-QA-manifest.txt`. The previous verification therefore short-circuited on `grep -qx 'code=17'` **before** comparing hashes. This is not evidence of an invalid APK, but the APK must still be verified independently.

Corrected the script in development branch so *future* D-122 builds report `code=17`. Existing on-phone manifest from the earlier build naturally still contains `code=16`; do NOT silently edit it or claim it matches. To validate existing build without rebuilding, compare the actual candidate APK's `sha256sum` with the unchanged stored `app_sha256`, and inspect the real embedded `package` version via Android `aapt2 dump badging`. Do not install until both match and separate approval is obtained. No application source, Stable 7 branch, or installed phone data were changed by this script-only fix.

## QA-D122-02 — Samsung build-only rerun PASS (2026-10-28)

User-supplied real Samsung Code on the Go terminal screenshot confirms:
- `BUILD SUCCESSFUL in 1m 15s`; `72 actionable tasks: 72 executed`.
- `D-122 JVM ACTUAL: tests=228 failures=0 errors=0 skipped=0`.
- `BUILD ONLY PASS`.
- Regular candidate `/storage/emulated/0/Download/Atelier-Time-D122-Candidate.apk`.
- Android instrumentation APK `/storage/emulated/0/Download/Atelier-Time-D122-Tests.apk`.
- QA manifest `/storage/emulated/0/Download/Atelier-Time-D122-QA-manifest.txt`.
- Terminal explicitly states `NO regular/test APK installed`.

The two legacy Restart tests and three newly added D-122 regression tests are part of the 228 executed JVM tests. This **closes the build-only QA gate**, not the product acceptance gate. The screenshot does not show the manifest's commit SHA; verify APK SHA-256 against the manifest before any installation. Android instrumentation was assembled, not executed. Manual Samsung testing must verify repeated Next, Restart ordinary break after many Next, genuine special-break preservation, unchanged saved Class end, and preserved Classes/Sounds. D-122 remains a development candidate; frozen D-119 Stable 7 is unchanged. Explicit approval for D-122 installation is still required.

## QA-D122-01 — FAILED on Samsung (2026-10-28)

User-supplied Code on the Go screenshot shows `228 tests completed, 2 failed`, `:app:testDebugUnitTest FAILED`, `BUILD FAILED in 1m 18s` with 64 actionable tasks executed. The build-only gate did not pass; no D-122 candidate should be installed or called stable.

**Unknown until JUnit XML is reviewed:** exact failing test names, assertions, and exception stack traces. Do not guess that both failures are from the three newly added tests, do not weaken tests to force green. Retrieve `app/build/test-results/testDebugUnitTest/TEST-*.xml` errors from the phone and diagnose before code changes. D-121 remains installed development; frozen Stable 7 remains unchanged.

# D-122 — Restart-after-Next regression: forensic comparison and repair

**Date:** 2026-10-28. **Status:** development SOURCE FIX, NOT YET BUILT/TESTED; not Stable. `versionCode=17`, `versionName=D-122`.
**Basis:** user's paired screenshots of D-121 installed on Samsung, showing Restart of a regular five-minute Break after repeated Next.

## Observed evidence (right = before Restart; left = after)

| Field | Before | After |
|---|---|---|
| Break start | 00:07:37 | 00:07:45 |
| Break length | 05:00 | 05:00 |
| Next credit | 03:58:21 | 03:58:13 |
| Final Sessions (Even) | 16:40 + 16:41 | 16:36 + 16:37 |
| Added regular Sessions + Breaks | 8 | 8 |

The eight seconds of restarted Break time legitimately reduce the available ending budget by eight seconds and do NOT change the hard physical deadline. However, the **automatic genuine special Break visibly jumps from near the middle to much later in the Class**. This is not an acceptable side effect of restarting the CURRENT regular Break.

## Exact legacy Stable 7 Restart path

`MonotonicSessionTimer.restartCurrentSegmentKeepingEndTime()` in D-119:
1. `C_new = C_old + ceil((elapsedInCurrentPhase + pausedMillis)/1000)`.
2. First try `FinalPairCompensation.adjust(original, C_new, allowExtraRegular=nextOccurred)`.
3. Accept only when **all phases through the current one are exactly the same**.
4. Otherwise use `FinalPairCompensation.adjustLastOnly(active, C_new)` (NO `adjustFutureTail` fallback).
5. Validate the current phase identity/start and atomically reset its cursor. The approved physical Class finish remains fixed.

On a rebuilt program with previously added regular blocks, the first attempt normally fails prefix equality; the fallback changes only the final unstarted work Session:

`oldCorrection = originalTotal - activeVirtualEnd`
`deltaC = newCorrection - oldCorrection`
`newLast = oldLast - deltaC`

For the screenshots, where the credit shrinks by eight seconds, legacy behavior would usually preserve first final 16:40 and shorten last from 16:41 to **16:33** (the actual branch depends on precise in-memory state). That differs from D-121's 16:36 + 16:37 split and preserves already-placed specials.

## D-121 regression root cause

D-121's `reflow()` chose `adjust(original)` then **`adjustFutureTail(active)`** then `adjustLastOnly(active)`. `adjustFutureTail` re-creates unstarted added blocks and places genuine specials towards the end of the reconstructed tail. `SpecialBreakRebalancer.rebalance()` runs for Next but **not** for Restart. D-121 validated whole-lesson time and protected prefix but did not enforce an unchanged future structure for Restart/Resume. As a result a tiny correction could change break placement and/or the chosen final-pair distribution.

## D-122 source repair

- `ReflowBoundary.preserveExistingTail` is set by Restart and Resume; Next does not set it.
- Under this contract no `adjustFutureTail` candidates are considered. Only `adjust(original)` if its entire structure matches, or `adjustLastOnly(active)`.
- Validate *before commit*: same number/order/identity/type of all phases; all phases BEFORE the final adjustable pair must be byte-for-byte unchanged; outside the final pair, phase durations are unchanged. Thus added Sessions and genuine special Breaks cannot jump during Restart or Resume.
- Hard deadline millisecond budget validator from D-121 remains in force; no change to Next policy, approved Editor rules, or Stable 7 source.
- New `D122RestartPlacementRegressionTest` tests a three-hour program with approximately four hours of Next credit and inserted regular blocks. It checks Restart at +8 sec, Resume at +6 sec, intact special-break index/identity, intact inserted-block count, unchanged observed prefix, and correct final-work-only deduction.

## Release gate (pending)

Neither the new regression tests nor the Android app have been executed in this D-122 change set. **Do not treat source code or mathematical arguments as a PASS.** First run `bash tools/build_d122_candidate_on_phone.sh` from the development branch using the user-authorized build-only procedure and obtain actual JUnit XML totals. Installation requires separate approval. D-121 remains the installed development candidate until user action; D-119 Stable 7 remains unchanged and closed in GitHub.

## QA-D122-01 diagnosis from exact failing JUnit XML and corrective change (2026-10-28)

The user supplied the XML `<failure>` output. Exactly two **pre-existing** JUnit tests failed, not the three new D-122 regression methods:

1. `ClassClockTrackerTest.restartingCurrentSessionResetsOnlyItsClockNotClassBoundary` (at the invocation of Restart, test line 158).
2. `MonotonicSessionTimerTest.restartCurrentSessionKeepsEarlierPhasesAndOriginalClassDeadline` (at Restart, test line 113).

Both ended with `IllegalArgumentException: The approved finish cannot fit the unstarted future without rewriting completed Sessions or protected Breaks.` from `FinalPairCompensation.reflow`. These scenarios restart an ordinary **Session 2 at physical 40m, virtual start 30m, with NO prior Next**. Stable 7 succeeds with signed correction +10m, virtual reset to 30m, virtual total 170m and fixed physical finish 180m.

**Precise root cause:** D-122 accidentally set `preserveExistingTail=true` unconditionally for Restart/Resume. Its structural validator forbids lawful changes to the untouched final pair layout (including merge and phase-count changes) **even when no Next had ever altered the future**. This caused both old tests to fail.

**Corrective source change, still pending actual Kotlin run:**

- `preserveExistingTail = nextHasOccurred` in Restart and Resume; only post-Next restarts require strong identity/order/placement preservation.
- A separate `noActiveTailRebuild = true` applies to Restart and Resume **both before and after Next**. It preserves Stable-7 candidate priority: `adjust(original)` first, then `adjustLastOnly(active)`, but **never `adjustFutureTail`** for these actions.
- Next remains permitted to use `adjustFutureTail` with its existing real-remaining-time gate.
- The mandatory physical budget `abs(virtualRemaining - physicalRemaining) < 2000ms` is unchanged.
- No tests were deleted, renamed, ignored or weakened. Both pre-existing failures must pass alongside all three D-122 special-placement tests.

**Next QA step:** rerun `tools/build_d122_candidate_on_phone.sh` on development branch with full JVM suite and Android APK assembly, then provide actual JUnit totals. Expected test count is 228 from the failed run, but a pass has **not yet** been observed. D-122 is blocked from installation pending results. Stable 7 remains frozen and D-121 remains the installed development candidate.

## QA-D122-05 follow-up — user confirms sound A was an Atelier Time BUILT-IN melody (2026-10-28)

The user's answer explicitly identifies the affected cue as `A · End of session`, source `Atelier Time` (`SoundSource.APP`). This materially changes the diagnosis: `MelodyAudioPlayer.play()` renders the built-in melody to 16-bit PCM and plays it via `AudioTrack`; the APP branch never reads a song/URI from Poweramp or Android content storage. Therefore **a Poweramp music fragment cannot originate from the built-in melody PCM rendering itself** on the current code path.

Investigate audio-routing/focus or selection-state mismatch, not a stale external URI as the leading explanation. `MelodyAudioPlayer` requests `AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK` and uses media-stream audio attributes. Android explicitly permits the prior media app to **continue playing quietly mixed** with the new cue under this policy. This is a plausible concurrent-audio mechanism, but it does **not** prove the reported previous-day song could spontaneously resume or why reselecting APP fixed it. Check whether Poweramp was actually playing/paused at the incident time and whether the Sounds mode was `SHARED` or `SEPARATE`. `SoundSettings.soundFor()` returns `sharedSound` for A/B/C whenever shared mode is active even if the dormant separate A choice was APP.

Proposed isolated regression, only after explicit implementation approval: add precise source and audio-focus diagnostics (without logging song paths or private URIs), compare media playback with Poweramp stopped/playing, test `AUDIOFOCUS_GAIN_TRANSIENT` to pause rather than duck background playback, and obey focus grant before issuing any cue. No timer logic or saved Classes should change. No code change performed for this report. D-122 remains timer-functionally accepted by the user's report but sound QA OPEN; not a new Stable declaration.

## QA-D122-05 further clarification — SEPARATE A/B/C confirmed (2026-10-28)

User explicitly confirms that at the time of the Poweramp music-fragment anomaly the app was in **Separate A/B/C** mode and cue A used the **Atelier Time built-in** sound. `SoundSettings.soundFor(TimerCue.POSE_FINISHED)` returns `poseEnd` directly in SEPARATE mode, not `sharedSound`; `MelodyAudioPlayer.play` then renders the APP melody via app-owned PCM `AudioTrack` without opening a URI. A stale sound URI or shared-mode fallback is **not a supported primary explanation** for this reported case.

Source-level risk persists: `AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK` allows an actively playing Poweramp stream to continue quietly and mix with app cue. The current code also continues to play even when `requestAudioFocus()` returns something other than `AUDIOFOCUS_REQUEST_GRANTED`. Android documentation recommends not starting playback without acquired focus. **This could explain mixed simultaneous audio, but cannot by itself explain restarting a track that was genuinely stopped since the prior evening.** Rechoosing the APP sound should not modify Poweramp state, so causal link remains unconfirmed.

Before code change, establish whether Poweramp was visibly PLAYING or PAUSED at incident time. Then run controlled tests with Poweramp fully stopped, paused and playing, using existing Preview A and an actual timer cue. Suggested isolated proposed patch, subject to separate approval: for short Atelier Time cues use transient focus that temporarily pauses other players rather than MAY_DUCK, obey focus-grant result, and release it at the end, while preserving the user's audio-mixing preference if intentional. Do not alter timer logic, preset storage, or Stable 7. Do not characterize D-122 sound issue as resolved.

## QA-D122-05 additional observation — Poweramp was closed; LAST played song heard (2026-10-28)

The user confirms Poweramp was **closed** before the Atelier Time Session A cue. The unexpected sound was a clip of **the last song Poweramp had played before closing**. Mode is SEPARATE and A's selected source is BUILT-IN `SoundSource.APP` (both previously confirmed).

Source inspection: the APP cue is generated mathematically as PCM with `AudioTrack`, with no file read, Poweramp intent, or external media URI. Therefore if the auditory identification is accurate, the old track must originate outside the APP cue-rendering branch (e.g., a remaining/background media playback session or an external playback command). On Android, closing the music player's UI does not prove its playback service or media session was terminated. Android's `AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK` can permit coexistence with another audio stream; Poweramp community reports mention unexpected playback on media/audio-focus/volume events even when the UI appears closed. These are **hypotheses**, not a reproduced cause. A reselected built-in A somehow mitigating it is still unexplained.

**Recommended next diagnostic without code or data changes**: Compare preview of APP A with (1) Poweramp simply exited/paused in UI, (2) Poweramp explicitly STOPPED using its own playback/notification control, and (3) Poweramp force-stopped in Android App Info (no clear data). During any recurrence inspect whether the media notification, Quick Settings or Poweramp 'Last Processed Commands' shows an unsolicited Play/Resume event. Do not assume that changing to `AUDIOFOCUS_GAIN_TRANSIENT` alone will resolve the issue; it can lead to a paused player's resumption when temporary focus is released. A future narrow audio-only code proposal could add non-sensitive audio event/source diagnostics, obey focus-grant result and choose playback-focus semantics based on the intended behavior. Require authorization and reproducible QA; never alter timer timing logic or frozen Stable 7 for this issue.

**Status:** QA-D122-05 OPEN. D-122 user-reported timer functionality working; sound anomaly unresolved. No code changes for this report.

## QA-D122-05 Force-stop diagnostic result (2026-10-28)

**User-observed PASS:** After Android Settings > Apps > Poweramp > Force stop (as requested in the diagnostic procedure), the user reports that the built-in Atelier Time Session A sound plays correctly.

**Interpretation:** A clean Poweramp-stopped condition produces expected audio; this supports—but does **not prove**—an external playback-session or audio-focus interaction as the cause of the earlier incident. It neither isolates the exact mechanism nor demonstrates that all ordinary App A cues work across device lifecycle states. It does not justify changing the cue source or timer logic.

**Next controlled reproduction:** Play a song briefly in Poweramp, close/exit it **using the same method as the previous evening** (without Force stop), leave Atelier Time's source/mode unchanged (SEPARATE, A=APP), then use Sounds > Preview A. Record whether the result is built-in A, last Poweramp track, or both, and whether Poweramp's media notification or playback indicator is visible. No app code changes; sound QA remains open. D-119 Stable 7 frozen; D-122 timer user acceptance not affected.

## QA-D122-05 controlled ordinary-close reproduction — PASS, monitor (2026-10-28)

User confirmed the second controlled test is **normal**: they played a song briefly in Poweramp, closed Poweramp in the usual way without Force Stop, did not reselect Atelier Time's built-in sound A, and previewed A in Sounds. The output was the correct Atelier Time sound. Previous controlled test also **PASS** after Android Force Stop of Poweramp. The original incident was a snippet of the last Poweramp song on the following day, while Poweramp was apparently closed, in Separate A/B/C mode with APP built-in sound A selected.

**Disposition: INTERMITTENT / NOT REPRODUCED — MONITORING.** Neither controlled reproduction triggers the problem; the precise source, interaction and persistence conditions remain unknown. Avoid assigning causality to Poweramp or Audio Focus without evidence. Do not claim the original bug has been fixed. No code modification warranted now, and no timer-logic modifications. If repeated, capture whether Poweramp is actually playing/paused in media controls, cue source and timing, and whether cue preview versus automatic A differs, before reselecting sound. Reassess Android audio focus changes only with reproducible evidence and independent acceptance tests.

D-122 timer functioning reported sound by user; sound monitor does not assert full regression coverage or a new stable release. D-119 Stable 7 remains frozen.

## QA-D122-06 — six-minute saved Class `test` as high-stress edge case (2026-10-28)

User supplied Samsung screenshots showing an actual locally saved Class named **`test`** and reported it behaves strangely during repeated **Restart** and **Next**. The screenshots show the structure and saved-class listing, **NOT** a before/after execution trace or error message. Thus no actual incorrect outcome has yet been reproduced. Do not claim a specific on-device failure.

Reconstructed editor values from screenshot:
- Total Class **6 min** (=360s); summary **1 min** (=60s); work **5 min**.
- One opening group with one **1-minute** Session, and a **1-minute** required break after group.
- Standard regular Session **1 min**, regular Break **1 min**, **BREAK_FIRST** ordering after opening.
- One automatic **Special Break 1 min**, equal to B. According to approved D-120 behavior, this is an ordinary runtime replacement (`L <= B`), while saved/Editor timeline may retain `SPECIAL_BREAK` label/kind.
- Approved timeline: `00:00–01:00 opening Session 1`, `01:00–02:00 Special Break #1 (60s)`, `02:00–03:00 regular Session 2`, `03:00–04:00 regular Break #2 (60s)`, `04:00–05:00 regular Session 3`, `05:00–06:00 fixed Summary (60s)`.

**Source-level boundary diagnosis:** `FinalPairCompensation.adjust` selects the last two `PoseSource.REGULAR` Sessions (Session 2 and Session 3). Both start at the **60-second minimum**, so `available = 120s - correction`. For any small positive correction (>0s), keeping BOTH sessions at least 60s is impossible, and the optional merge branch cannot save this arrangement unless its exact `mergedLength=120s - correction + 60s` is at most the regular `poseSeconds=60s`. Thus Restart before earning time credits through Next is normally rejected atomically, even for a few seconds' delay. After early Next, signed credit makes some restarts feasible; Restart can appear to switch from rejected to accepted depending on credit. These constraints are expected based on source, not independently run or user-confirmed.

**Other expectations:** Next during the last regular Session cannot jump over it to the fixed Summary before the permitted boundary. A successful earlier Next can extend the future last Session while preserving Class physical end. An equal-length "Special" break can be treated as ordinary for compensation despite UI identity. There is a **test coverage gap**: existing JVM tests exercise L==B with 25-minute pose and other structures, but not the specific 6-minute 1/1/1 structure with repeated Next+Restart and minute-minimum edges.

**Suggested deterministic QA matrix (source-predicted, not verified on phone):**
1. Start `test`; after ~8 seconds in opening Session 1, Restart current Session. Expect rejection and unchanged chart/deadline because neither final regular Session can shrink below 60s.
2. Start a **fresh run**; after ~5 seconds in opening Session 1 press Next to equal-length Special Break #1. Expect success, unchanged physical Class end, longer final regular Session absorbing approximately 55s credit. Repeated Next only succeeds while the unstarted tail can absorb credits with original history protected.
3. After scenario 2, wait ~5 seconds and press Restart on Special Break #1. Expect success while remaining credit is sufficient, and final Session decreases approximately by the seconds elapsed since Next. Confirm fixed Class end.
4. On the final regular Session, an early Next to Summary must be refused; at the natural boundary Summary remains 60s. A Restart of the last final Session must be refused.
5. Repeat with rapid subsecond Next and inspect observed-vs-unstarted timeline, cue count, error messages; ensure no past segment rewrite, no jump of any genuinely protected special, and no data modification.

**No source changes performed** in response to these screenshots. Before any fix, capture the *exact actions*, timestamps (rough seconds from Start), chart before/after, rejection text, actual versus expected behavior. Preserve user data and D-119 Stable 7; do not infer that 228 passing tests prove this untested edge case. Label `QA-D122-06 OPEN / exact reproduction pending`.
