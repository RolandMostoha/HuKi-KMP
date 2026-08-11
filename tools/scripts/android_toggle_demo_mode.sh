#!/bin/bash
# Toggles Android SystemUI Demo Mode on the connected device/emulator, for Play Store screenshots.
# Mirrors Developer options > System UI demo mode: `sysui_demo_allowed` = "Enable demo mode",
# `sysui_tuner_demo_on` = "Show demo mode". SystemUI applies its own clean status bar defaults:
# 4:00, full Wi-Fi, full battery, no notifications.

if [[ "$(adb shell settings get global sysui_tuner_demo_on | tr -d '\r')" == "1" ]]; then
  adb shell settings put global sysui_tuner_demo_on 0
  adb shell settings put global sysui_demo_allowed 0
  echo "Demo mode OFF"
else
  adb shell settings put global sysui_demo_allowed 1
  adb shell settings put global sysui_tuner_demo_on 1
  echo "Demo mode ON"
fi
