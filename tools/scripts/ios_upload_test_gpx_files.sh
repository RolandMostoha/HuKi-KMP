#!/bin/bash
# Copies every tools/gpx/*.gpx file into the HuKi iOS app's Documents container on an iOS simulator.
# Used to seed test GPX fixtures before Maestro E2E runs or manual GPX import testing.
#
# Always targets a simulator (never a connected physical device — simctl cannot reach one).
# Usage:
#   tools/scripts/ios_upload_test_gpx_files.sh [simulator-udid]
#     simulator-udid  optional, default: the currently booted simulator

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Use YOUR app's bundle ID from AGENTS.md
APP_BUNDLE_ID="hu.mostoha.mobile.ios.huki"
SOURCE_DIR="$SCRIPT_DIR/../gpx"

# Resolve the target simulator: explicit UDID argument, else the currently booted one.
DEVICE_ID="${1:-}"
if [ -z "$DEVICE_ID" ]; then
    echo "Checking for booted simulator..."
    DEVICE_ID=$(xcrun simctl list devices | grep "(Booted)" | awk -F '[()]' '{print $2}' | head -1)
fi

if [ -z "$DEVICE_ID" ]; then
    echo "Error: No booted simulator found. Boot one in Xcode or with: xcrun simctl boot <udid>"
    exit 1
fi

# Verify the UDID is an actual simulator (guards against passing a physical device UDID).
if ! xcrun simctl list devices | grep -q "$DEVICE_ID"; then
    echo "Error: $DEVICE_ID is not a known simulator. This script only targets simulators."
    exit 1
fi

# Get the data container for your specific app on the resolved simulator
DEST_DIR=$(xcrun simctl get_app_container "$DEVICE_ID" "$APP_BUNDLE_ID" data)

if [ -z "$DEST_DIR" ]; then
    echo "Error: Could not find container for $APP_BUNDLE_ID on $DEVICE_ID. Is the app installed?"
    exit 1
fi

# Ensure the Documents directory exists in your app's container
mkdir -p "$DEST_DIR/Documents"

echo "Uploading GPX files to $APP_BUNDLE_ID on simulator $DEVICE_ID..."
gpx_files=("$SOURCE_DIR"/*.gpx)

if [ -e "${gpx_files[0]}" ]; then
    for file in "${gpx_files[@]}"; do
        filename=$(basename "$file")
        echo "Pushing $filename..."
        cp "$file" "$DEST_DIR/Documents/"
    done
    echo "Done! Files are in $DEST_DIR/Documents/"
else
    echo "No .gpx files found in $SOURCE_DIR"
fi
