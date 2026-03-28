#!/bin/bash

# App bundle ID from AGENTS.md
APP_BUNDLE_ID="hu.mostoha.mobile.ios.huki"

echo "Checking for booted simulator..."
if ! xcrun simctl list devices | grep -q "Booted"; then
    echo "Error: No booted simulator found."
    exit 1
fi

echo "Uninstalling $APP_BUNDLE_ID from booted simulator..."

# Attempt to uninstall the app
if xcrun simctl uninstall booted "$APP_BUNDLE_ID"; then
    echo "Successfully uninstalled $APP_BUNDLE_ID."
else
    echo "Error: Failed to uninstall $APP_BUNDLE_ID. Is the app installed?"
    exit 1
fi
