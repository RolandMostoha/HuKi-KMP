#!/bin/bash
# Simulates the OS killing HuKi in the background, then reopens it from Recents so the saved state is restored.
# Usage: android_trigger_state_restoration.sh [device-serial] [package]
# Defaults to the first emulator (else the first connected device) and the debug package.

set -euo pipefail

first_device() {
  local devices
  devices=$(adb devices | awk 'NR > 1 && $2 == "device" { print $1 }')
  echo "$devices" | grep -m1 '^emulator-' || echo "$devices" | head -n1
}

DEVICE="${1:-$(first_device)}"
PACKAGE="${2:-hu.mostoha.mobile.android.huki.debug}"

if [[ -z "$DEVICE" ]]; then
  echo "Error: No connected adb device found."
  exit 1
fi

adb_device() { adb -s "$DEVICE" "$@"; }

is_resumed() {
  adb_device shell dumpsys activity activities | grep -E "topResumedActivity|mResumedActivity" | grep -q "$PACKAGE/"
}

OLD_PID=$(adb_device shell pidof "$PACKAGE" || true)
if [[ -z "$OLD_PID" ]]; then
  echo "Error: $PACKAGE is not running on $DEVICE. Open the app first."
  exit 1
fi

# The Home key does nothing on the lock screen, so the app could not be moved to the background
adb_device shell input keyevent KEYCODE_WAKEUP
if adb_device shell dumpsys window | grep -qE "isKeyguardShowing=true|mDreamingLockscreen=true"; then
  echo "Error: $DEVICE is locked. Unlock it, open $PACKAGE and run the script again."
  exit 1
fi

# Backgrounding saves the instance state, `am kill` only kills a background process
echo "Sending $PACKAGE to the background on $DEVICE..."
adb_device shell input keyevent KEYCODE_HOME
for _ in {1..10}; do
  is_resumed || break
  sleep 0.5
done
if is_resumed; then
  echo "Error: $PACKAGE is still in the foreground, the Home key had no effect."
  exit 1
fi
sleep 1

echo "Killing process $OLD_PID like the OS would..."
adb_device shell am kill "$PACKAGE"
sleep 1
# Some devices keep the last used app out of reach of `am kill`, a debug build can be killed from its own user instead
if [[ -n "$(adb_device shell pidof "$PACKAGE" || true)" ]]; then
  adb_device shell run-as "$PACKAGE" kill -9 "$OLD_PID" 2> /dev/null || true
  sleep 1
fi

if [[ -n "$(adb_device shell pidof "$PACKAGE" || true)" ]]; then
  echo "Error: $PACKAGE is still running, the process was not killed."
  exit 1
fi

# The launcher intent brings back the existing task from Recents, restoring its saved state
echo "Reopening $PACKAGE..."
adb_device shell monkey -p "$PACKAGE" -c android.intent.category.LAUNCHER 1 > /dev/null 2>&1
sleep 2

echo "Restored in a new process: $OLD_PID -> $(adb_device shell pidof "$PACKAGE" || echo "?")"
