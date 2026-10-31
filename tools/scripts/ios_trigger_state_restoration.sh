#!/bin/bash
# Simulates the OS killing HuKi in the background, then reopens it so the SceneStorage session is restored.
# Usage: ios_trigger_state_restoration.sh [simulator-udid]
# Defaults to the booted simulator.

set -euo pipefail

DEVICE="${1:-booted}"
APP_BUNDLE_ID="hu.mostoha.mobile.ios.huki"

app_pid() {
  xcrun simctl spawn "$DEVICE" launchctl list | awk -v app="UIKitApplication:$APP_BUNDLE_ID" 'index($3, app) == 1 { print $1 }'
}

OLD_PID=$(app_pid)
if [[ -z "$OLD_PID" || "$OLD_PID" == "-" ]]; then
  echo "Error: $APP_BUNDLE_ID is not running on $DEVICE. Open the app first."
  exit 1
fi

# Moving to the background writes the session to SceneStorage, a force-quit from the app switcher would discard it
echo "Sending $APP_BUNDLE_ID to the background..."
xcrun simctl launch "$DEVICE" com.apple.Preferences > /dev/null
sleep 2

echo "Terminating process $OLD_PID like the OS would..."
xcrun simctl terminate "$DEVICE" "$APP_BUNDLE_ID"
sleep 1

echo "Reopening $APP_BUNDLE_ID..."
NEW_PID=$(xcrun simctl launch "$DEVICE" "$APP_BUNDLE_ID" | awk '{ print $2 }')

echo "Restored in a new process: $OLD_PID -> $NEW_PID"
