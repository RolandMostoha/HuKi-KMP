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
  maestro --no-ansi test "$test_file" \
    -e APP_ID="$APP_ID" \
    --device "$DEVICE_ID" \
    --debug-output ./
    
  echo "--- Finished Test: $test_file ---"
  sleep 5
done
