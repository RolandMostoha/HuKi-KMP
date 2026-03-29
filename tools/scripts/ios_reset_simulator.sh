#!/bin/bash

# 1. Get the ID of the currently booted simulator
# This finds the line with "Booted" and extracts the UUID inside the parentheses
BOOTED_DEVICE_ID=$(xcrun simctl list devices | grep "Booted" | grep -E -o '[0-9A-F]{8}-([0-9A-F]{4}-){3}[0-9A-F]{12}' | head -n 1)

if [ -z "$BOOTED_DEVICE_ID" ]; then
    echo "No booted simulator found. If you want to reset a specific device, please start it first or provide its name/ID."
    # Optional: List available devices so the user knows what they can boot
    # xcrun simctl list devices
    exit 1
fi

echo "Found booted simulator: $BOOTED_DEVICE_ID"

# 2. Shutdown the simulator (erase won't work while running)
echo "Shutting down..."
xcrun simctl shutdown "$BOOTED_DEVICE_ID"

# 3. Reset/Erase the simulator using the ID we captured
echo "Erasing all data and settings..."
xcrun simctl erase "$BOOTED_DEVICE_ID"

# 4. Optional: Start it back up
echo "Rebooting..."
xcrun simctl boot "$BOOTED_DEVICE_ID"

# Optional: Open the Simulator app window if it's not visible
open -a Simulator

echo "Done! Simulator $BOOTED_DEVICE_ID has been reset."
