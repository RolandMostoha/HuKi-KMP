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

# Format a duration in seconds as "Xm Ys" (or "Ys" under a minute)
format_duration() {
  local total=$1
  if [ "$total" -ge 60 ]; then
    echo "$((total / 60))m $((total % 60))s"
  else
    echo "${total}s"
  fi
}

# Track passed tests so we can list them if a later test fails
PASSED_TESTS=()

# Overall run timer (SECONDS is a bash builtin that counts up from assignment)
SECONDS=0

# Loop through each test file
for test_file in .maestro/maestro_*.yaml; do
  echo "--- Starting Test: $test_file ---"
  test_start=$SECONDS

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
    if [ ${#PASSED_TESTS[@]} -gt 0 ]; then
      echo ""
      echo "${GREEN}${BOLD}Already passed (${#PASSED_TESTS[@]}) — safe to skip on rerun:${RESET}"
      for passed in "${PASSED_TESTS[@]}"; do
        echo "${GREEN}  ✔ $passed${RESET}"
      done
    fi
    echo "${RED}--- Aborting Maestro run after $(format_duration "$SECONDS"). See the output above and ./maestro debug logs for details. ---${RESET}"
    exit 1
  fi

  PASSED_TESTS+=("$test_file")
  echo "${GREEN}✔ COMPLETED: $test_file ($(format_duration $((SECONDS - test_start))))${RESET}"
  sleep 5
done

echo "${GREEN}${BOLD}✅ All ${#PASSED_TESTS[@]} Maestro tests passed for $APP_ID in $(format_duration "$SECONDS").${RESET}"
