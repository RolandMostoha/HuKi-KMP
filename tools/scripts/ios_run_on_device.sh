#!/bin/bash
# Builds, installs and launches the HuKi iOS app on a connected physical iPhone via
# xcodebuild + devicectl — bypassing the Android Studio / Xcode GUI entirely.
#
# Usage:
#   tools/scripts/ios_run_on_device.sh [device-name-substring]
#     device-name-substring  optional, default "iPhone" (matches devicectl list)
#
# Env overrides:
#   DEVELOPMENT_TEAM  signing team id (default: auto-detected from your "Apple
#                     Development" codesigning certificate)
#   CONFIGURATION     Debug | Release (default: Debug)
#   NO_LAUNCH         set to 1 to install without launching
#
# Notes:
#   - DEVELOPMENT_TEAM is passed as an xcodebuild override, NOT as TEAM_ID: the
#     project appends $(TEAM_ID) to PRODUCT_BUNDLE_IDENTIFIER and ships it empty,
#     so overriding TEAM_ID would change the bundle id. Overriding DEVELOPMENT_TEAM
#     signs with the team while keeping the bundle id intact.
#   - Uses a dedicated DerivedData path so builds are incremental and isolated from
#     the Android Studio / Xcode DerivedData.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT="$REPO_ROOT/iosApp/iosApp.xcodeproj"
SCHEME="iosApp"
BUNDLE_ID="hu.mostoha.mobile.ios.huki"
CONFIGURATION="${CONFIGURATION:-Debug}"
DERIVED_DATA="$REPO_ROOT/build/ios-device-dd"
DEVICE_QUERY="${1:-iPhone}"

if [ ! -d "$PROJECT" ]; then
    echo "Error: Xcode project not found at $PROJECT"
    exit 1
fi

# --- Resolve signing team ---------------------------------------------------
if [ -z "${DEVELOPMENT_TEAM:-}" ]; then
    DEVELOPMENT_TEAM="$(security find-certificate -a -c "Apple Development" -p 2>/dev/null \
        | openssl x509 -noout -subject 2>/dev/null \
        | grep -oE 'OU=[A-Z0-9]+' | head -1 | cut -d= -f2 || true)"
fi
if [ -z "$DEVELOPMENT_TEAM" ]; then
    echo "Error: could not auto-detect a signing team. Set DEVELOPMENT_TEAM=<TEAMID> and retry."
    echo "Available identities:"
    security find-identity -v -p codesigning | grep -i "Apple Develop" || true
    exit 1
fi

# --- Resolve target device --------------------------------------------------
DEVICE_LINE="$(xcrun devicectl list devices 2>/dev/null \
    | grep -iE "$DEVICE_QUERY" | grep -iE "available" | head -1 || true)"
if [ -z "$DEVICE_LINE" ]; then
    echo "Error: no available device matching \"$DEVICE_QUERY\". Connected devices:"
    xcrun devicectl list devices 2>/dev/null || true
    exit 1
fi
DEVICE_ID="$(echo "$DEVICE_LINE" \
    | grep -oE '[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}' | head -1)"
DEVICE_NAME="$(echo "$DEVICE_LINE" | sed -E 's/[[:space:]]{2,}.*//')"

echo "Device:   $DEVICE_NAME ($DEVICE_ID)"
echo "Team:     $DEVELOPMENT_TEAM"
echo "Config:   $CONFIGURATION"
echo

# --- Build ------------------------------------------------------------------
echo "Building $SCHEME for device..."
xcodebuild \
    -project "$PROJECT" \
    -scheme "$SCHEME" \
    -configuration "$CONFIGURATION" \
    -destination "generic/platform=iOS" \
    -derivedDataPath "$DERIVED_DATA" \
    -allowProvisioningUpdates \
    DEVELOPMENT_TEAM="$DEVELOPMENT_TEAM" \
    build

APP_PATH="$(/bin/ls -d "$DERIVED_DATA/Build/Products/$CONFIGURATION-iphoneos/"*.app 2>/dev/null | head -1)"
if [ -z "$APP_PATH" ] || [ ! -d "$APP_PATH" ]; then
    echo "Error: built .app not found under $DERIVED_DATA/Build/Products/$CONFIGURATION-iphoneos/"
    exit 1
fi
echo "Built: $APP_PATH"

# --- Install ----------------------------------------------------------------
echo "Installing to $DEVICE_NAME..."
xcrun devicectl device install app --device "$DEVICE_ID" "$APP_PATH"

# --- Launch -----------------------------------------------------------------
if [ "${NO_LAUNCH:-}" != "1" ]; then
    echo "Launching $BUNDLE_ID..."
    if ! xcrun devicectl device process launch --device "$DEVICE_ID" "$BUNDLE_ID"; then
        echo "Note: launch failed (commonly because the device is locked)."
        echo "      Unlock $DEVICE_NAME and tap the app, or re-run the script."
    fi
fi

echo "Done. Installed $BUNDLE_ID on $DEVICE_NAME."
