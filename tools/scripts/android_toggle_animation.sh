#!/bin/bash
# Toggles system animations (window, transition, animator durations) on the connected Android device/emulator via adb.

current=$(adb shell settings get global window_animation_scale)

if [[ "$current" == "0" || "$current" == "0.0" ]]; then
  adb shell settings put global window_animation_scale 1.0
  adb shell settings put global transition_animation_scale 1.0
  adb shell settings put global animator_duration_scale 1.0
  echo "Animations enabled (scale 1.0)"
else
  adb shell settings put global window_animation_scale 0
  adb shell settings put global transition_animation_scale 0
  adb shell settings put global animator_duration_scale 0
  echo "Animations disabled (scale 0)"
fi
