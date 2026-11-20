# Atelier Time — Google Play screenshot replacements for Stable19 (2026-11-20)

## 2026-11-20 — COMPLETE ORIGINAL ARCHIVE RECEIVED AND MERGED (LATEST VERIFIED STATUS)

The user supplied the **actual original approved ZIP** as a conversation attachment. SHA-256 on these bytes **exactly matched** the original approved receipt:
`289de4efa7f5f7f9e51995265d964931c9ffa6aa059a30c3d6c105aa5a48b80f`.

An updated **complete** archive was built and fully tested, containing all 13 original paths plus three extra documentation/backup paths, with **seven PNG screenshot entries 02–08 replaced** and active `SHA256_MANIFEST.json` refreshed. Unchanged original `README_FIRST.txt` was preserved, the original `SHA256_MANIFEST.json` was additionally preserved as `SHA256_MANIFEST_ORIGINAL_2026_11_01.json`, and the new ZIP contains `SCREENSHOT_REPLACEMENTS_SOURCE_MAP_2026_11_20.json` and `README_UPDATE_2026_11_20.txt`.

**Deliverable:** `Atelier_Time_Google_Play_Approved_Assets_Complete_Pack_UPDATED_2026_11_20.zip` (conversation artifact) — **15,475,464 bytes**, SHA-256:
`597a8ea993b989b2cbc1cbd51339a28a79e63ba58b50ea96d18d41688cc1f726`.
An identical copy with the original base filename `Atelier_Time_Google_Play_Approved_Assets_Complete_Pack.zip` was prepared for a manual GitHub in-place replacement, with the **same** updated SHA-256.

**Preservation verified by comparing ORIGINAL and UPDATED archive content bytes** for each untouched entry:
- Icon `01_Store_Icon/Atelier_Time_Google_Play_Icon_Stable12_APPROVED_512x512.png`: `2e40dd769dbd04201efd3286d335f1d4f289f554280a7f68443b24069fbc030e`.
- Locked feature graphic `02_Locked_Feature_Graphic/Atelier_Time_Google_Play_Feature_Graphic_ORIGINAL_LOCKED_1024x500.png`: `c2921bdb6d61cb24619201f3c20e8ac5bb811569a84c034acf1abf14fec4c04e`.
- Slot 01 `03_Phone_Screenshots_Upload_In_This_Order/01_Splash.png`: `3d8d3013b1ffc3668fd66717bd45c214a80f8d20ede1441a31091e51d8b2aae6`.
- Nested 10-original-screenshot backup `04_Original_10_Screens_Backup/Atelier_Time_Stable12_All_10_Screens_Exact_Order.zip`: `d9de1b691aa35879d8d7de56eee6dde95216750175b292c818b6957c2ad941e7`.
- Historical `README_FIRST.txt` unchanged byte-for-byte.

**Checks:** ZIP CRC PASS; all eight slot files accounted for; seven replacement PNGs exactly 1080×1920; updated SHA-256 manifest matches **all** listed assets; no dropped original paths, no duplicate entries; original filename copy passed ZIP test.

**The full updated binary ZIP is NOT YET COMMITTED TO GITHUB.** Connected GitHub methods in this environment provide UTF-8 text-file writes or blobs requiring raw base64 to be provided in tool-call content, but no file-upload action accepting a mounted local binary file reference. Do **not** claim the GitHub archive was replaced. The next step is to upload the prepared same-name ZIP from the user's phone using GitHub's **Add file → Upload files** at `artwork/play-store/`, commit as the same filename to the intended branch and verify the GitHub blob/file afterwards. The original GitHub binary blob SHA-1 `506a9b9e6dd27b7d4531e71d1053096de1d3e6b0` remains the reference until replacement is confirmed. **No code/build/stable branches/public Play publication were changed.**

---

**HISTORICAL STATUS BEFORE FULL ORIGINAL ZIP WAS RECEIVED:** SEVEN NEW USER-SUPPLIED SCREENSHOTS MAPPED AND PACKAGED LOCALLY. ORIGINAL APPROVED FULL ZIP ON GITHUB IS UNCHANGED.** No app code, previously frozen Stable19/Code54 branch, source assets or public Production release was changed.

## User request and preservation rule

The user provided seven updated photos/screenshots to **replace slots 02–08** in the approved Google Play screenshots set, while preserving all old ZIP entries not designated for replacement, in particular **01_Splash.png**, the approved **512×512 app icon**, the original **locked 1024×500 feature graphic**, the original screenshot backups and any other supporting assets. Never delete or recreate approved non-screenshot art.

The original approved ZIP remains at:
`artwork/play-store/Atelier_Time_Google_Play_Approved_Assets_Complete_Pack.zip`
- GitHub binary blob SHA-1: `506a9b9e6dd27b7d4531e71d1053096de1d3e6b0`
- Original ZIP SHA-256 from verified 2026-11-01 receipt: `289de4efa7f5f7f9e51995265d964931c9ffa6aa059a30c3d6c105aa5a48b80f`

**This GitHub ZIP HAS NOT been replaced yet.** GitHub connector can read metadata but returns no bytes for this 15,290,196-byte ZIP; `fetch_blob` rejects the binary with a Unicode decode error. The connected GitHub writing commands support text files and Git blobs with supplied content, but provide no direct bridge for importing the locally assembled replacement ZIP bytes. GitHub raw download also failed from the current sandbox. This prevented a safe in-place ZIP rebuild and binary commit. Do not misrepresent a local replacement-only ZIP as the complete original pack.

## Exact 8-slot list: original 01 preserved, user-provided 02–08 replace

| Slot | Exact archive filename | User source / action |
|---|---|---|
| 01 | `01_Splash.png` | **PRESERVE** original approved splash, no replacement |
| 02 | `02_Class_Editor.png` | `1000109174.jpg` |
| 03 | `03_Complete_Class_Timeline.png` | `1000109156.jpg` |
| 04 | `04_Saved_Classes_And_Reports.png` | `1000109158.jpg` |
| 05 | `05_Class_Timer_Progress.png` | `1000109162.jpg` |
| 06 | `06_Free_Timer.png` | `1000109166.jpg` |
| 07 | `07_Design_Settings.png` | `1000109170.jpg` |
| 08 | `08_Sound_Choices.png` | `1000109168.jpg` |

All files for slots 02–08 belong in the existing ZIP folder:
`03_Phone_Screenshots_Upload_In_This_Order/`.

## Local replacement-only ZIP prepared

Prepared from the actual seven conversation upload JPGs, using the same named target slots, with each **rescaled proportionally and centered without cropping into 1080×1920 RGB PNG (24-bit, no alpha)** so screenshots meet the Google Play `max long edge <= 2 × short edge` rule that some original user JPG ratios do not meet. This process does not change texts/UI elements within the screen, although neutral side margins are added where necessary. No AI-generated screenshots or app-code edits.

Local user-download artifact name: `Atelier_Time_7_Updated_Play_Screenshots_REPLACEMENTS_ONLY_2026-11-20.zip`.
- Size: **4,068,806 bytes**
- SHA-256: `5997a4289d8c36a8e40bae1f2635f3adfeb02293cad33511794ee6956aca33d6`.
- ZIP integrity test **PASS**; exactly seven correctly numbered 1080×1920 RGB PNGs, plus `README_REPLACEMENTS_FIRST.txt` and `SCREENSHOT_UPDATE_MANIFEST.json` with per-file source and output hashes.
- **No `01_Splash.png`, icon, feature graphic or backup images are present in this replacement-only ZIP** because they must come from the unchanged original ZIP in an eventual verified merge. NEVER upload this replacement ZIP as though it were a complete archive.

**Next safe operation:** acquire the verified original full ZIP bytes and rebuild the complete pack, replace only those seven screenshot entries; update any internal SHA manifest for those replacements, retain every untouched original archive entry byte-for-byte when extracted, run CRC/size/hash checks (including 01 Splash and icon/graphic/backup), then upload the complete new ZIP back to GitHub (or user completes binary upload in GitHub UI). Verify the new GitHub blob hash and read it back. Only then mark the GitHub ZIP as updated and revise this document. In the meantime users can use the replacement-only PNGs directly for the seven new phone screenshot slots in the Google Play Asset Library; the original Splash remains slot 01.

**Store asset reference:** https://support.google.com/googleplay/android-developer/answer/9866151
**Prior archive approval receipt:** `artwork/play-store/GOOGLE_PLAY_APPROVED_ASSETS_ARCHIVE_VERIFIED_2026_11_01.md`

**Safety:** no app build, AAB, signing key, old Stable branch, original ZIP or Play Production publication was changed.
