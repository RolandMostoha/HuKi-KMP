# App Store / Play Store screenshots

Each screenshot is one store asset: a map camera framing plus the simulated GPS
position for the location puck, plus the UI state (theme + open sheet) to show.

## Global setup

- **Demo mode**: ON — clean status bar (`ios_toggle_demo_mode.sh` / `android_toggle_demo_mode.sh`)
- **Languages**: capture every screenshot in both `en-US` and `hu-HU`
  (`ios_toggle_language.sh` / `android_toggle_language.sh`)
- Enable the debug camera panel with `DEBUG_SHOW_CAMERA_PANEL = true` (see `FeatureFlags`).
  The panel shows the live camera readout **and** a `lat,lon,zoom` input with a go button
  that jumps the camera there — read a framing off the readout, then reapply it any time.
    - **Camera** → type the scene's `lat,lon,zoom` into the panel input and tap go
    - **Location** → `ios_fix_location.sh <lat,lon>` / `adb emu geo fix <lon> <lat>`
    - **Theme** → `ios_toggle_dark_mode.sh` / `android_toggle_dark_mode.sh`
- File naming: [iOS/Android]_store_screenshot_[X]_[HU/EN].png
- Location: tools/screenshots/[iOS/Android]/

Two camera scenes are reused across the screenshots: **danube_bend** and **okt_15**.

---

## Screenshot #1 — default map (danube_bend)

- light mode
- default opened app state, no sheet

|          | Lat/Long/Zoom           |
|----------|-------------------------|
| Camera   | 47.78055,18.93411,11.63 |
| Location | 47.78995,18.93374       |

---

## Screenshot #2 — okt_15

- light mode
- Base Map / GPX sheet open.
- tools/gpx/okt_15.gpx opened

|          | Lat/Long/Zoom           |
|----------|-------------------------|
| Camera   | 47.61895,18.94269,10.54 |
| Location | 47.78995,18.93374       |

---

## Screenshot #3 — Layers

- dark mode.
- Base Map / Layers sheet open
- Satellite layer selected.

|          | Lat/Long/Zoom           |
|----------|-------------------------|
| Camera   | 47.71670,18.89799,15.55 |
| Location | 47.718252,18.898026     |

---

## Screenshot #4 — destinations

Light mode. Destinations view.

|          | Lat/Long/Zoom |
|----------|---------------|
| Camera   | TBD           |
| Location | TBD           |
