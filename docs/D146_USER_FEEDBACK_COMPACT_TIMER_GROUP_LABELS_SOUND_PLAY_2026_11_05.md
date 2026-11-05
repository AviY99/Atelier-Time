# D-146 / Code40 — field UX refinements, pending local phone QA

The complete D-146 user-feedback specification, source/test inventory, safety constraints and local QA procedure are documented on main: https://github.com/AviY99/Atelier-Time/blob/main/docs/D146_USER_FEEDBACK_COMPACT_TIMER_GROUP_LABELS_SOUND_PLAY_2026_11_05.md .

Current DEV source implements: compact one-row Timer controls directly below clock without heading; Help below; combined Screen on and Max both during Run; Group N · Session i/j and Group N · Break; sound-preview triangle in the existing one-row sound picker.

**NOT BUILT / NOT USER-ACCEPTED.** Build/test ONLY on Samsung CoGo:
```bash
cd /storage/emulated/0/CodeOnTheGoProjects/Atelier-Time-Android/Atelier-Time/android-native && git pull --ff-only && bash tools/build_d146_compact_field_ui_on_phone.sh
```

Stable14/D145/code39 frozen at `stable/android-d145-qa14-field-accepted` remains latest accepted; no paid GitHub build, app install/uninstall, data deletion or Google Play submission. Play commercial model stays **FREE**, first test distribution **Internal Testing to invited people**.
