#!/usr/bin/env bash
# Runs every .maestro/maestro_*.yaml E2E test against the HuKi iOS app on a
# booted simulator. Thin wrapper over shared_run_maestro_tests.sh that fills in
# the iOS APP_ID and resolves the target simulator.
#
# Usage:
#   tools/scripts/ios_run_all_maestro_tests.sh [simulator-udid]
#     simulator-udid  optional, default: the currently booted simulator

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
APP_ID="hu.mostoha.mobile.ios.huki"

DEVICE_ID="${1:-}"
if [ -z "$DEVICE_ID" ]; then
    DEVICE_ID="$(xcrun simctl list devices | grep "(Booted)" | awk -F '[()]' '{print $2}' | head -1)"
fi
if [ -z "$DEVICE_ID" ]; then
    echo "Error: no booted iOS simulator. Boot one in Xcode or with: xcrun simctl boot <udid>"
    exit 1
fi

cd "$REPO_ROOT"
exec ./tools/scripts/shared_run_maestro_tests.sh "$APP_ID" "$DEVICE_ID"
