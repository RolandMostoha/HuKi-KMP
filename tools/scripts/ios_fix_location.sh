#!/bin/bash
# Fixes a wedged simulator location daemon (GPS spins forever) by setting a concrete coordinate.
# `simctl location clear` does NOT fix it — only `set` does. No data wipe.

# Default to Dobogókő; override with: ios_fix_location.sh <lat>,<lon>
LOCATION="${1:-47.7168079,18.8950729}"

BOOTED_DEVICE_ID=$(xcrun simctl list devices | grep "(Booted)" | awk -F '[()]' '{print $2}')

if [ -z "$BOOTED_DEVICE_ID" ]; then
    echo "No booted simulator found. Boot a simulator first."
    exit 1
fi

echo "Setting location on $BOOTED_DEVICE_ID to $LOCATION ..."
xcrun simctl location "$BOOTED_DEVICE_ID" set "$LOCATION"

echo "Done. Tap the location button in Apple Maps / the app to confirm the fix landed."
