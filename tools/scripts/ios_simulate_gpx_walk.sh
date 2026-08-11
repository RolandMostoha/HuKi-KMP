#!/bin/bash
# Simulates "walking" along a GPX track on the booted iOS simulator by feeding its
# <trkpt> points to `simctl location start`, which interpolates between waypoints and
# emits location updates carrying course + speed — so the compass / bearing puck works.
#
# Usage: ios_simulate_gpx_walk.sh <path-to.gpx> [speed-mps] [step]
#   speed-mps : travel speed in meters/sec (default 1.4 ≈ walking)
#   step      : use every Nth trackpoint to thin out dense tracks (default 1)
#
# Example: ios_simulate_gpx_walk.sh tools/gpx/okt_15.gpx 15 10
#
# Runs in the background on the simulator; stop it with:
#   xcrun simctl location booted clear

set -euo pipefail

GPX_FILE="${1:-}"
SPEED="${2:-1.4}"
STEP="${3:-1}"

if [ -z "$GPX_FILE" ] || [ ! -f "$GPX_FILE" ]; then
    echo "Usage: ios_simulate_gpx_walk.sh <path-to.gpx> [speed-mps] [step]"
    exit 1
fi

BOOTED_DEVICE_ID=$(xcrun simctl list devices | grep "(Booted)" | awk -F '[()]' '{print $2}' | head -1)

if [ -z "$BOOTED_DEVICE_ID" ]; then
    echo "No booted simulator found. Boot a simulator first."
    exit 1
fi

# Extract "lat,lon" from every <trkpt lat="..." lon="..."> in document order.
COORDS=$(grep -oE '<trkpt[^>]*lat="[^"]*"[^>]*lon="[^"]*"' "$GPX_FILE" \
    | sed -E 's/.*lat="([^"]*)".*lon="([^"]*)".*/\1,\2/' \
    | awk -v step="$STEP" 'NR % step == 1 || step == 1')

TOTAL=$(echo "$COORDS" | grep -c . || true)

if [ "$TOTAL" -lt 2 ]; then
    echo "Need at least 2 <trkpt> points in $GPX_FILE (found $TOTAL after step $STEP)."
    exit 1
fi

echo "Walking $BOOTED_DEVICE_ID along $GPX_FILE ($TOTAL waypoints, step $STEP, ${SPEED} m/s)."
echo "Stop with: xcrun simctl location $BOOTED_DEVICE_ID clear"

echo "$COORDS" | xcrun simctl location "$BOOTED_DEVICE_ID" start --speed="$SPEED" --interval=1 -
