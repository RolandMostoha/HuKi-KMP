#!/usr/bin/env bash
# Applies the standard HuKi emulator setup on a connected Android device/emulator:
#   - disables stylus handwriting (stops the "Try out your stylus" onboarding on text fields)
#   - adds Hungarian (hu-HU) as secondary system language after English (en-US)
#   - switches navigation to 3-button instead of gestures
#   - adds the Dark theme tile to Quick Settings
#   - enables Developer options
#
# The system language list only takes effect after a reboot, so the script reboots
# the device at the end and waits for it to come back.
#
# Usage:
#   tools/scripts/android_setup_emulator.sh [device-serial] [--no-reboot]
#     device-serial  optional, default: the connected adb device (errors if multiple)

set -euo pipefail

REBOOT=true
DEVICE_ID=""
for arg in "$@"; do
    case "$arg" in
        --no-reboot) REBOOT=false ;;
        *) DEVICE_ID="$arg" ;;
    esac
done

if [ -z "$DEVICE_ID" ]; then
    CONNECTED_IDS="$(adb devices | awk 'NR>1 && $2=="device" {print $1}')"
    CONNECTED_COUNT="$(printf '%s\n' "$CONNECTED_IDS" | grep -c . || true)"
    if [ "$CONNECTED_COUNT" -eq 0 ]; then
        echo "Error: no connected Android device/emulator. Start an emulator or plug in a device."
        adb devices
        exit 1
    fi
    if [ "$CONNECTED_COUNT" -gt 1 ]; then
        echo "Error: multiple connected Android devices. Pass a specific one as an argument:"
        printf '%s\n' "$CONNECTED_IDS"
        exit 1
    fi
    DEVICE_ID="$CONNECTED_IDS"
fi

ADB=(adb -s "$DEVICE_ID")

echo "Setting up $DEVICE_ID"

"${ADB[@]}" shell settings put secure stylus_handwriting_enabled 0
echo "  Stylus handwriting disabled"

"${ADB[@]}" shell settings put system system_locales "en-US,hu-HU"
echo "  System languages set to English (en-US), Hungarian (hu-HU)"

"${ADB[@]}" shell cmd overlay enable-exclusive --category com.android.internal.systemui.navbar.threebutton
echo "  Navigation set to 3-button"

CURRENT_TILES="$("${ADB[@]}" shell settings get secure sysui_qs_tiles | tr -d '\r')"
if [ "$CURRENT_TILES" = "null" ] || [ -z "$CURRENT_TILES" ]; then
    echo "  Quick Settings tiles unavailable — skipped Dark theme tile"
elif [[ ",$CURRENT_TILES," == *",dark,"* ]]; then
    echo "  Dark theme tile already in Quick Settings"
else
    IFS=',' read -r -a TILES <<< "$CURRENT_TILES"
    NEW_TILES=("${TILES[@]:0:2}" dark "${TILES[@]:2}")
    # single quotes: custom tile specs contain parentheses that the device shell would parse
    "${ADB[@]}" shell cmd statusbar set-tiles "'$(IFS=','; echo "${NEW_TILES[*]}")'"
    echo "  Dark theme tile added to Quick Settings"
fi

"${ADB[@]}" shell settings put global development_settings_enabled 1
"${ADB[@]}" shell settings put global adb_enabled 1
echo "  Developer options enabled"

if [ "$REBOOT" = true ]; then
    echo "Rebooting to apply the language list..."
    "${ADB[@]}" reboot
    "${ADB[@]}" wait-for-device
    until [ "$("${ADB[@]}" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do
        sleep 2
    done
    echo "Device is back up. Setup complete."
else
    echo "Skipped reboot — the language list applies after the next restart."
fi
