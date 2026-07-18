#!/bin/bash
# Toggles the booted iOS simulator's GLOBAL language between Hungarian (hu-HU) and English (en-US).
# Writes AppleLanguages/AppleLocale to the device's global domain (NSGlobalDomain), so the setting
# persists across app reinstalls and Xcode runs (unlike a per-launch -AppleLanguages argument).
# Then relaunches the HuKi app so the change is visible immediately.

bundle="hu.mostoha.mobile.ios.huki"

DEV=$(xcrun simctl list devices | grep "(Booted)" | awk -F '[()]' '{print $2}' | head -1)
if [ -z "$DEV" ]; then
  echo "No booted simulator found."
  exit 1
fi

current=$(xcrun simctl spawn "$DEV" defaults read -g AppleLanguages 2>/dev/null | sed -n '2p' | grep -oE '[A-Za-z-]+' | head -1)

if [[ "$current" == hu* ]]; then
  languages='en-US hu-HU'
  locale="en_US"
  label="English (en-US)"
else
  languages='hu-HU en-US'
  locale="hu_HU"
  label="Hungarian (hu-HU)"
fi

# shellcheck disable=SC2086
xcrun simctl spawn "$DEV" defaults write -g AppleLanguages -array $languages
xcrun simctl spawn "$DEV" defaults write -g AppleLocale -string "$locale"

# Relaunch the app so it picks up the new global language at process start.
xcrun simctl terminate "$DEV" "$bundle" 2>/dev/null
xcrun simctl launch "$DEV" "$bundle" >/dev/null 2>&1

echo "Language set to $label"
