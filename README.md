# Atelier Time — D-127 diagnostic reports

This branch is reserved for **automatically uploaded, redacted build diagnostics**.

* Reports are produced by `android-native/tools/publish_d127_build_diagnostics.sh` from the developer's Samsung Code on the Go checkout.
* The full build log is NEVER uploaded; it remains only on the device.
* Public diagnostic reports contain only a filename basename, line number and closed-category error label, plus minimal build metadata.
* No device IDs, personal files, credentials, full compiler messages or absolute filesystem paths are permitted.
* A report means a build attempt happened; only `build_result=PASS` can represent a successful compile (never user acceptance).
* Do not commit application code or copy private logs to this branch.
* The installed Stable 9 (D-126) is not affected.

**Status at branch creation:** No Samsung build report has been uploaded yet. The prior D-127 failure log remains local until the phone can publish a redacted report.

See [the publisher source](https://github.com/AviY99/Atelier-Time/blob/android-native-kotlin-compose/android-native/tools/publish_d127_build_diagnostics.sh).
