#!/bin/bash
# Simulates "walking" along a GPX track on the connected Android emulator by stepping
# the location through each <trkpt> with a delay between points.
#
# Usage: android_simulate_gpx_walk.sh <path-to.gpx> [delay-seconds] [step]
#   delay-seconds : pause between points (default 3)
#   step          : use every Nth trackpoint to speed up dense tracks (default 1)
#
# Example: android_simulate_gpx_walk.sh tools/gpx/gpx_test_with_comments.gpx 5 10
#
# Note: requires an Android emulator (adb emu geo fix). Physical devices do not
# support mock locations via adb.

set -euo pipefail

GPX_FILE="${1:-}"
DELAY="${2:-3}"
STEP="${3:-1}"

if [ -z "$GPX_FILE" ] || [ ! -f "$GPX_FILE" ]; then
    echo "Usage: android_simulate_gpx_walk.sh <path-to.gpx> [delay-seconds] [step]"
    exit 1
fi

DEVICE=$(adb devices | grep -w "device" | grep "emulator-" | head -n 1 | awk '{print $1}')

if [ -z "$DEVICE" ]; then
    echo "No booted emulator found. Boot an Android emulator first (adb emu geo fix needs an emulator)."
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
echo "Walking $DEVICE along $GPX_FILE ($TOTAL points, step $STEP, ${DELAY}s delay). Ctrl-C to stop."

i=0
echo "$COORDS" | while IFS= read -r point; do
    i=$((i + 1))
    if [ $(( (i - 1) % STEP )) -ne 0 ]; then
        continue
    fi
    lat="${point%%,*}"
    lon="${point##*,}"
    # adb emu geo fix expects longitude first, then latitude.
    adb -s "$DEVICE" emu geo fix "$lon" "$lat"
    echo "[$i/$TOTAL] set $point"
    sleep "$DELAY"
done

echo "Done."
