#!/bin/bash
# Simulates "walking" along a GPX track on the booted iOS simulator by stepping
# the location through each <trkpt> with a delay between points.
#
# Usage: ios_simulate_gpx_walk.sh <path-to.gpx> [delay-seconds] [step]
#   delay-seconds : pause between points (default 3)
#   step          : use every Nth trackpoint to speed up dense tracks (default 1)
#
# Example: ios_simulate_gpx_walk.sh tools/gpx/gpx_test_with_comments.gpx 5 10

set -euo pipefail

GPX_FILE="${1:-}"
DELAY="${2:-3}"
STEP="${3:-1}"

if [ -z "$GPX_FILE" ] || [ ! -f "$GPX_FILE" ]; then
    echo "Usage: ios_simulate_gpx_walk.sh <path-to.gpx> [delay-seconds] [step]"
    exit 1
fi

BOOTED_DEVICE_ID=$(xcrun simctl list devices | grep "(Booted)" | awk -F '[()]' '{print $2}' | head -1)

if [ -z "$BOOTED_DEVICE_ID" ]; then
    echo "No booted simulator found. Boot a simulator first."
    exit 1
fi

# Extract "lat,lon" from every <trkpt lat="..." lon="..."> in document order.
COORDS=$(grep -oE '<trkpt[^>]*lat="[^"]*"[^>]*lon="[^"]*"' "$GPX_FILE" \
    | sed -E 's/.*lat="([^"]*)".*lon="([^"]*)".*/\1,\2/')

if [ -z "$COORDS" ]; then
    echo "No <trkpt> points found in $GPX_FILE."
    exit 1
fi

TOTAL=$(echo "$COORDS" | wc -l | tr -d ' ')
echo "Walking $BOOTED_DEVICE_ID along $GPX_FILE ($TOTAL points, step $STEP, ${DELAY}s delay). Ctrl-C to stop."

i=0
echo "$COORDS" | while IFS= read -r point; do
    i=$((i + 1))
    if [ $(( (i - 1) % STEP )) -ne 0 ]; then
        continue
    fi
    xcrun simctl location "$BOOTED_DEVICE_ID" set "$point"
    echo "[$i/$TOTAL] set $point"
    sleep "$DELAY"
done

echo "Done."
