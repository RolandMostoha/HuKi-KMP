#!/bin/bash
# Toggles Android SystemUI Demo Mode on the connected device/emulator, for Play Store screenshots.
# Mirrors Developer options > System UI demo mode: `sysui_demo_allowed` = "Enable demo mode",
# `sysui_tuner_demo_on` = "Show demo mode". The settings alone don't change the status bar, so the
# demo broadcasts the Settings toggle sends are replayed too: clock at <major version>:00, full
# Wi-Fi, mobile and battery, no notifications.

demo() {
  adb shell am broadcast -a com.android.systemui.demo -e command "$@" >/dev/null
}

if [[ "$(adb shell settings get global sysui_tuner_demo_on | tr -d '\r')" == "1" ]]; then
  demo exit
  adb shell settings put global sysui_tuner_demo_on 0
  adb shell settings put global sysui_demo_allowed 0
  echo "Demo mode OFF"
else
  major_version="$(adb shell getprop ro.build.version.release | tr -d '\r' | cut -d. -f1)"
  demo_time="$(printf '%02d00' $((major_version % 24)))"

  adb shell settings put global sysui_demo_allowed 1
  adb shell settings put global sysui_tuner_demo_on 1
  demo enter
  demo clock -e hhmm "$demo_time"
  demo network -e wifi show -e level 4 -e fully true
  demo network -e mobile show -e datatype 3g -e level 4 -e fully true
  demo battery -e level 100 -e plugged false
  demo notifications -e visible false
  echo "Demo mode ON"
fi
