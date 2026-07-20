#!/bin/bash
# Toggles the booted iOS simulator's appearance between Dark and Light mode.

DEV=$(xcrun simctl list devices | grep "(Booted)" | awk -F '[()]' '{print $2}' | head -1)
if [ -z "$DEV" ]; then
  echo "No booted simulator found."
  exit 1
fi

if [ "$(xcrun simctl ui "$DEV" appearance)" == "dark" ]; then
  xcrun simctl ui "$DEV" appearance light
  echo "Appearance set to Light"
else
  xcrun simctl ui "$DEV" appearance dark
  echo "Appearance set to Dark"
fi
