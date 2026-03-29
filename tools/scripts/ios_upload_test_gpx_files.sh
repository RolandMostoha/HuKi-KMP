#!/bin/bash

# Use YOUR app's bundle ID from AGENTS.md
APP_BUNDLE_ID="hu.mostoha.mobile.ios.huki"
SOURCE_DIR="./tools/gpx/"

echo "Checking for booted simulator..."
if ! xcrun simctl list devices | grep -q "Booted"; then
    echo "Error: No booted simulator found."
    exit 1
fi

# Get the data container for your specific app
DEST_DIR=$(xcrun simctl get_app_container booted "$APP_BUNDLE_ID" data)

if [ -z "$DEST_DIR" ]; then
    echo "Error: Could not find container for $APP_BUNDLE_ID. Is the app installed?"
    exit 1
fi

# Ensure the Documents directory exists in your app's container
mkdir -p "$DEST_DIR/Documents"

echo "Uploading GPX files to $APP_BUNDLE_ID..."
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
