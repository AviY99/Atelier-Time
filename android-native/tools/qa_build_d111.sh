#!/usr/bin/env bash
# RETIRED legacy D-111 runner. Intentionally fail closed: never install an old app.
echo "STOP: D-111 QA runner retired after Stable 5 D-114." >&2
echo "Use android-native/tools/build_all_qa_on_phone.sh in Code on the Go." >&2
echo "Use android-native/tools/termux_run_all_device_qa.sh in Termux ONLY after separate approval." >&2
exit 2
