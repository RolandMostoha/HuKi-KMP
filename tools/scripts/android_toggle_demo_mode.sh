#!/bin/bash
# Toggles Android SystemUI Demo Mode on the connected device/emulator, for Play Store screenshots.
# Shows a clean status bar (9:41, full Wi-Fi/mobile, 100% battery, no notifications).
# Uses `sysui_demo_allowed` as the on/off state marker.

if [[ "$(adb shell settings get global sysui_demo_allowed | tr -d '\r')" == "1" ]]; then
  adb shell am broadcast -a com.android.systemui.demo -e command exit >/dev/null
  adb shell settings put global sysui_demo_allowed 0
  echo "Demo mode OFF"
else
  adb shell settings put global sysui_demo_allowed 1
  adb shell am broadcast -a com.android.systemui.demo -e command enter >/dev/null
  adb shell am broadcast -a com.android.systemui.demo -e command clock -e hhmm 0941 >/dev/null
  adb shell am broadcast -a com.android.systemui.demo -e command network -e wifi show -e level 4 >/dev/null
  adb shell am broadcast -a com.android.systemui.demo -e command network -e mobile show -e level 4 -e datatype none >/dev/null
  adb shell am broadcast -a com.android.systemui.demo -e command battery -e level 100 -e plugged false >/dev/null
  adb shell am broadcast -a com.android.systemui.demo -e command notifications -e visible false >/dev/null
  echo "Demo mode ON"
fi
