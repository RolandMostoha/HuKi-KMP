#!/usr/bin/env bash
# Runs the route planner camera regression flow on an Android device and an iOS simulator while recording the screen
# and the device log (logcat with epoch timestamps / the app's unified log), streamed so a full buffer drops nothing.
# Per platform it builds a contact sheet of the settled screenshots and a frame strip per action from the recording,
# so double camera moves, sheet jumps and flashes show up too. Compare them before and after a planner / sheet /
# camera change. Keeps the apps' state (no clearState), so imported GPX files stay.
# Usage: ./tools/scripts/shared_run_route_planner_regression.sh [android|ios|both] [android-serial] [ios-udid]

set -uo pipefail

PLATFORMS=${1:-both}
ANDROID_SERIAL=${2:-$(adb devices | awk 'NR>1 && $2=="device" && $1 ~ /^emulator-/ {print $1; exit}')}
IOS_UDID=${3:-$(xcrun simctl list devices booted | grep -oE '[0-9A-F-]{36}' | head -1)}

ROOT=$(cd "$(dirname "$0")/../.." && pwd)
FLOW="$ROOT/.maestro/regression/maestro_route_planner_camera.yaml"
OUT="$ROOT/build/regression/route_planner/$(date +%Y%m%d_%H%M%S)"
# Android's screenrecord stops after 3 minutes, so it records in back-to-back segments
ANDROID_SEGMENT_S=170
export MAESTRO_CLI_NO_ANALYTICS=1
STATUS=0
RECORDER_PID=""
LOG_PID=""

now() { python3 -c 'import time; print(f"{time.time():.3f}")'; }

start_recording() {
  local platform=$1 device=$2 dir=$3
  : > "$dir/recordings.txt"
  if [ "$platform" = android ]; then
    adb -s "$device" logcat -c
    adb -s "$device" logcat -v epoch > "$dir/device_log.txt" 2>&1 &
    LOG_PID=$!
    (
      i=0
      while [ ! -f "$dir/stop" ]; do
        echo "$(now) $dir/recording_$i.mp4" >> "$dir/recordings.txt"
        adb -s "$device" shell screenrecord --time-limit "$ANDROID_SEGMENT_S" "/sdcard/huki_regression_$i.mp4"
        i=$((i + 1))
      done
    ) &
  else
    xcrun simctl spawn "$device" log stream --level info --style syslog \
      --predicate 'process == "HuKi"' > "$dir/device_log.txt" 2>&1 &
    LOG_PID=$!
    echo "$(now) $dir/recording_0.mp4" >> "$dir/recordings.txt"
    xcrun simctl io "$device" recordVideo --force "$dir/recording_0.mp4" >/dev/null 2>&1 &
  fi
  RECORDER_PID=$!
  sleep 2
}

stop_recording() {
  local platform=$1 device=$2 dir=$3
  touch "$dir/stop"
  if [ "$platform" = android ]; then
    adb -s "$device" shell pkill -INT screenrecord >/dev/null 2>&1
    wait "$RECORDER_PID" 2>/dev/null
    sleep 1
    local i=0
    while adb -s "$device" shell ls "/sdcard/huki_regression_$i.mp4" >/dev/null 2>&1; do
      adb -s "$device" pull -q "/sdcard/huki_regression_$i.mp4" "$dir/recording_$i.mp4"
      adb -s "$device" shell rm "/sdcard/huki_regression_$i.mp4"
      i=$((i + 1))
    done
  else
    kill -INT "$RECORDER_PID" 2>/dev/null
    wait "$RECORDER_PID" 2>/dev/null
  fi
  kill "$LOG_PID" 2>/dev/null
  echo "$platform: device log $dir/device_log.txt"
}

run_platform() {
  local platform=$1 app_id=$2 device=$3
  if [ -z "$device" ]; then
    echo "No $platform device found, skipping"
    STATUS=1
    return
  fi
  echo "--- $platform on $device ---"
  local dir="$OUT/$platform"
  mkdir -p "$dir"
  start_recording "$platform" "$device" "$dir"
  if ! maestro --device "$device" test -e APP_ID="$app_id" --test-output-dir "$dir" "$FLOW"; then
    echo "$platform: flow FAILED, see $dir"
    STATUS=1
  fi
  stop_recording "$platform" "$device" "$dir"

  command -v ffmpeg >/dev/null || { echo "ffmpeg not found, no contact sheet or strips"; return; }
  local shot_dir count
  shot_dir=$(find "$dir" -type d -name takeScreenshot | head -1)
  if [ -n "$shot_dir" ]; then
    count=$(find "$shot_dir" -name "*.png" | wc -l | tr -d ' ')
    ffmpeg -v error -y -pattern_type glob -i "$shot_dir/*.png" \
      -vf "scale=360:-2,tile=5x$(((count + 4) / 5))" -frames:v 1 "$OUT/${platform}_contact_sheet.png" || true
    echo "$platform: $OUT/${platform}_contact_sheet.png"
  fi
  local commands
  commands=$(find "$dir" -name commands.json | head -1)
  if [ -n "$commands" ]; then
    python3 "$ROOT/tools/scripts/shared_build_action_strips.py" "$commands" "$dir/recordings.txt" \
      "$OUT/${platform}_strips"
  fi
}

case "$PLATFORMS" in
  android) run_platform android hu.mostoha.mobile.android.huki.debug "$ANDROID_SERIAL" ;;
  ios) run_platform ios hu.mostoha.mobile.ios.huki "$IOS_UDID" ;;
  *)
    run_platform android hu.mostoha.mobile.android.huki.debug "$ANDROID_SERIAL"
    run_platform ios hu.mostoha.mobile.ios.huki "$IOS_UDID"
    ;;
esac

echo "Results: $OUT"
exit $STATUS
