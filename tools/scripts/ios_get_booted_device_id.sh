#!/bin/bash

# Extract the UUID of the booted simulator into a variable
BOOTED_DEVICE_ID=$(xcrun simctl list devices | grep "(Booted)" | awk -F '[()]' '{print $2}')

# Check if a booted device was found
if [ -z "$BOOTED_DEVICE_ID" ]; then
    echo "No booted simulator found."
    exit 1
fi

# Print the variable separately
echo "Booted Device UUID: $BOOTED_DEVICE_ID"
