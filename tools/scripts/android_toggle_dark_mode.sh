#!/bin/bash
# Toggles dark mode (night UI mode) on the connected Android device/emulator via adb.

if [[ $(adb shell "cmd uimode night") == "Night mode: yes" ]]; then
  adb shell "cmd uimode night no"
else
  adb shell "cmd uimode night yes"
fi