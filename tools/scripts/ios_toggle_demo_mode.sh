#!/bin/bash
# Toggles a clean "demo" status bar (9:41, full Wi-Fi/cellular, 100% battery) on the booted
# iOS simulator, for App Store screenshots. Uses `simctl status_bar override` / `clear`.

DEV=$(xcrun simctl list devices | grep "(Booted)" | awk -F '[()]' '{print $2}' | head -1)
if [ -z "$DEV" ]; then
  echo "No booted simulator found."
  exit 1
fi

if xcrun simctl status_bar "$DEV" list | grep -q "Time:"; then
  xcrun simctl status_bar "$DEV" clear
  echo "Demo status bar OFF"
else
  xcrun simctl status_bar "$DEV" override \
    --time "9:41" \
    --dataNetwork wifi --wifiMode active --wifiBars 3 \
    --cellularMode active --cellularBars 4 \
    --batteryState charged --batteryLevel 100
  echo "Demo status bar ON"
fi
