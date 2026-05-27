#!/bin/bash
# Pushes every tools/gpx/*.gpx file to /sdcard/Download on the connected Android device/emulator.
# Used to seed test GPX fixtures before Maestro E2E runs or manual GPX import testing.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

SOURCE_DIR="$SCRIPT_DIR/../gpx"
DEST_DIR="/sdcard/Download"

# Check if adb is available
if ! command -v adb &> /dev/null; then
    echo "Error: adb could not be found. Please ensure Android SDK platform-tools are in your PATH."
    exit 1
fi

# Check if the source directory exists
if [ ! -d "$SOURCE_DIR" ]; then
    echo "Error: Source directory '$SOURCE_DIR' does not exist."
    exit 1
fi

# Ensure the device is connected
adb get-state 1>/dev/null 2>&1
if [ $? -ne 0 ]; then
    echo "Error: No Android device or emulator found."
    exit 1
fi

# Upload GPX files
echo "Uploading GPX files..."
gpx_files=("$SOURCE_DIR"/*.gpx)

if [ -e "${gpx_files[0]}" ]; then
    for file in "${gpx_files[@]}"; do
        filename=$(basename "$file")
        echo "Pushing $filename..."
        adb push "$file" "$DEST_DIR/$filename"
    done
    echo "Done! Files are located in $DEST_DIR"
else
    echo "No .gpx files found in $SOURCE_DIR"
fi