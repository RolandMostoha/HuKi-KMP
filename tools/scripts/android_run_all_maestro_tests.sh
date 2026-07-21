#!/usr/bin/env bash
# Runs every .maestro/maestro_*.yaml E2E test against the HuKi Android app on a
# connected device/emulator. Thin wrapper over shared_run_maestro_tests.sh that
# fills in the Android APP_ID and resolves the target device.
#
# Usage:
#   tools/scripts/android_run_all_maestro_tests.sh [device-serial]
#     device-serial  optional, default: the connected adb device (errors if multiple)

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
APP_ID="hu.mostoha.mobile.android.huki.debug"

DEVICE_ID="${1:-}"
if [ -z "$DEVICE_ID" ]; then
    CONNECTED_IDS="$(adb devices | awk 'NR>1 && $2=="device" {print $1}')"
    CONNECTED_COUNT="$(printf '%s\n' "$CONNECTED_IDS" | grep -c .)"
    if [ "$CONNECTED_COUNT" -eq 0 ]; then
        echo "Error: no connected Android device/emulator. Start an emulator or plug in a device."
        adb devices
        exit 1
    fi
    if [ "$CONNECTED_COUNT" -gt 1 ]; then
        echo "Error: multiple connected Android devices. Pass a specific one as an argument:"
        echo "  tools/scripts/android_run_all_maestro_tests.sh <device-serial>"
        echo "Connected devices:"
        adb devices
        exit 1
    fi
    DEVICE_ID="$CONNECTED_IDS"
fi

cd "$REPO_ROOT"
exec ./tools/scripts/shared_run_maestro_tests.sh "$APP_ID" "$DEVICE_ID"
