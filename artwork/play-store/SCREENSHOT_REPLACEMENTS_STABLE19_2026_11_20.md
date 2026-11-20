# Atelier Time — Google Play screenshot replacements for Stable19 (2026-11-20)

**Status: SEVEN NEW USER-SUPPLIED SCREENSHOTS MAPPED AND PACKAGED LOCALLY. ORIGINAL APPROVED FULL ZIP ON GITHUB IS UNCHANGED.** No app code, previously frozen Stable19/Code54 branch, source assets or public Production release was changed.

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
