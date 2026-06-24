#!/usr/bin/env bash
# Runs every .maestro/maestro_*.yaml E2E test against the given app and device.
# Auto-uploads GPX fixtures via the platform-specific upload script (iOS or Android, based on APP_ID).
# Exits immediately on the first failing test and reports which one broke.
# Usage: ./tools/scripts/shared_run_maestro_tests.sh <APP_ID> <DEVICE_ID>

set -uo pipefail

# ANSI colors, only when stdout is a terminal (avoids escape codes in piped/CI logs)
if [ -t 1 ]; then
  RED=$'\033[0;31m'
  GREEN=$'\033[0;32m'
  BOLD=$'\033[1m'
  RESET=$'\033[0m'
else
  RED='' GREEN='' BOLD='' RESET=''
fi

# Arguments
APP_ID=$1
DEVICE_ID=$2

echo "--- Starting Maestro tests for $APP_ID on device $DEVICE_ID ---"

export MAESTRO_CLI_NO_ANALYTICS=1

# Loop through each test file
for test_file in .maestro/maestro_*.yaml; do
  echo "--- Starting Test: $test_file ---"

    # Setup test data based on platform
    if [[ "$APP_ID" == *".ios."* ]]; then
      echo "Uploading GPX files for iOS..."
      ./tools/scripts/ios_upload_test_gpx_files.sh
    elif [[ "$APP_ID" == *".android."* ]]; then
      echo "Uploading GPX files for Android..."
      ./tools/scripts/android_upload_test_gpx_files.sh
    fi

  # Use the variables in the maestro command
  if ! maestro test "$test_file" \
    -e APP_ID="$APP_ID" \
    --device "$DEVICE_ID" \
    --debug-output ./; then
    echo ""
    echo "${RED}${BOLD}❌ FAILED: $test_file${RESET}"
    echo "${RED}--- Aborting Maestro run. See the output above and ./maestro debug logs for details. ---${RESET}"
    exit 1
  fi

  echo "${GREEN}✔ COMPLETED: $test_file${RESET}"
  sleep 5
done

echo "${GREEN}${BOLD}✅ All Maestro tests passed for $APP_ID.${RESET}"
