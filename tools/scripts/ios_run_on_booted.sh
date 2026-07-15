#!/bin/bash
# Builds, installs and launches the HuKi iOS app on the currently booted iOS
# Simulator via xcodebuild + simctl — bypassing the Android Studio / Xcode GUI.
#
# Usage:
#   tools/scripts/ios_run_on_booted.sh [simulator-udid]
#     simulator-udid  optional, default: the currently booted simulator
#
# Env overrides:
#   CONFIGURATION  Debug | Release (default: Debug)
#   NO_LAUNCH      set to 1 to install without launching
#
# Notes:
#   - The Simulator signs with "Sign to Run Locally"; no DEVELOPMENT_TEAM needed.
#   - Uses a dedicated DerivedData path so builds are incremental and isolated from
#     the Android Studio / Xcode DerivedData.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT="$REPO_ROOT/iosApp/iosApp.xcodeproj"
SCHEME="iosApp"
BUNDLE_ID="hu.mostoha.mobile.ios.huki"
CONFIGURATION="${CONFIGURATION:-Debug}"
DERIVED_DATA="$REPO_ROOT/build/ios-sim-dd"

if [ ! -d "$PROJECT" ]; then
    echo "Error: Xcode project not found at $PROJECT"
    exit 1
fi

# --- Resolve target simulator -----------------------------------------------
DEVICE_ID="${1:-}"
if [ -z "$DEVICE_ID" ]; then
    DEVICE_ID="$(xcrun simctl list devices booted \
        | grep -oE '[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}' | head -1 || true)"
fi
if [ -z "$DEVICE_ID" ]; then
    echo "Error: no booted simulator found. Boot one (open Simulator or 'xcrun simctl boot <udid>')"
    echo "       or pass a UDID explicitly. Available simulators:"
    xcrun simctl list devices available || true
    exit 1
fi
DEVICE_NAME="$(xcrun simctl list devices | grep "$DEVICE_ID" | sed -E 's/ *\(.*//; s/^ *//' | head -1)"

echo "Simulator: ${DEVICE_NAME:-unknown} ($DEVICE_ID)"
echo "Config:    $CONFIGURATION"
echo

# --- Build ------------------------------------------------------------------
echo "Building $SCHEME for simulator..."
xcodebuild \
    -project "$PROJECT" \
    -scheme "$SCHEME" \
    -configuration "$CONFIGURATION" \
    -destination "platform=iOS Simulator,id=$DEVICE_ID" \
    -derivedDataPath "$DERIVED_DATA" \
    build

APP_PATH="$(/bin/ls -d "$DERIVED_DATA/Build/Products/$CONFIGURATION-iphonesimulator/"*.app 2>/dev/null | head -1)"
if [ -z "$APP_PATH" ] || [ ! -d "$APP_PATH" ]; then
    echo "Error: built .app not found under $DERIVED_DATA/Build/Products/$CONFIGURATION-iphonesimulator/"
    exit 1
fi
echo "Built: $APP_PATH"

# --- Install ----------------------------------------------------------------
echo "Installing to ${DEVICE_NAME:-simulator}..."
xcrun simctl install "$DEVICE_ID" "$APP_PATH"

# --- Launch -----------------------------------------------------------------
if [ "${NO_LAUNCH:-}" != "1" ]; then
    echo "Launching $BUNDLE_ID..."
    xcrun simctl launch "$DEVICE_ID" "$BUNDLE_ID"
fi

echo "Done. Installed $BUNDLE_ID on ${DEVICE_NAME:-$DEVICE_ID}."
