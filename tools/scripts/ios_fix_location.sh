#!/bin/bash
# Fixes a wedged simulator location daemon (GPS spins forever) by setting a concrete coordinate.
# `simctl location clear` does NOT fix it — only `set` does. No data wipe.

# Default to Dobogókő; override with: ios_fix_location.sh <lat>,<lon>
LOCATION="${1:-47.7168079,18.8950729}"

# Every booted simulator is fixed, so a second booted device can't silently take the update
BOOTED_DEVICE_IDS=$(xcrun simctl list devices | grep "(Booted)" | awk -F '[()]' '{print $2}')

if [ -z "$BOOTED_DEVICE_IDS" ]; then
    echo "No booted simulator found. Boot a simulator first."
    exit 1
fi

for DEVICE_ID in $BOOTED_DEVICE_IDS; do
    echo "Setting location on $DEVICE_ID to $LOCATION ..."
    xcrun simctl location "$DEVICE_ID" set "$LOCATION"
done

echo "Done. Tap the location button in Apple Maps / the app to confirm the fix landed."
