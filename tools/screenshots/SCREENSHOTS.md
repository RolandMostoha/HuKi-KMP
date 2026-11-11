# App Store / Play Store screenshots

Each screenshot is one store asset: a map camera framing plus the simulated GPS
position for the location puck, plus the UI state (theme + open sheet) to show.

## Global setup

- **Demo mode**: ON — clean status bar (`ios_toggle_demo_mode.sh` / `android_toggle_demo_mode.sh`)
- **Languages**: capture every screenshot in both `en-US` and `hu-HU`
  (`ios_toggle_language.sh` / `android_toggle_language.sh`)
- Open the debug camera panel by tapping the map scale bar (debug builds only, see `FeatureFlags.DEBUG_CAMERA_PANEL_ENABLED`).
  The panel shows the live camera readout **and** a `lat,lon,zoom` input with a go button
  that jumps the camera there — read a framing off the readout, then reapply it any time.
    - **Camera** → type the scene's `lat,lon,zoom` into the panel input and tap go
    - **Location** → `ios_fix_location.sh <lat,lon>` / `adb emu geo fix <lon> <lat>`
    - **Theme** → `ios_toggle_dark_mode.sh` / `android_toggle_dark_mode.sh`
- File naming: [iOS/iPad/Android]_store_screenshot_[X]_[HU/EN].png
- Location (iOS): `iosApp/fastlane/screenshots/en-US/` and `iosApp/fastlane/screenshots/hu/` — this is
  the `deliver` layout, so these files ARE the App Store assets; there is no copy/sync step. `deliver`
  infers the device class from pixel size (iPhone 6.3" = 1206x2622, iPad 13" = 2064x2752) and orders
  screenshots by filename, so keep the numeric prefix.
- The AppMockUp project files in `tools/screenshots/appmockup/` stay here as the design sources that
  produce those PNGs.
- iPad uses its own camera framing per scene (different aspect ratio than iPhone/Android) — see the
  **Camera (iPad)** row per screenshot below.

Two camera scenes are reused across the screenshots: **danube_bend** and **okt_15**.

---

## Screenshot — Default map - Dunakanyar

- light mode
- default opened app state, no sheet

|               | Lat/Long/Zoom           |
|---------------|-------------------------|
| Camera        | 47.78055,18.93411,11.63 |
| Camera (iPad) | 47.78286,18.93539,12.63 |
| Location      | 47.78951,18.93471       |

---

## Screenshot — Navigation

- dark mode.
- OUTDOORS layer
- GPX - just a simple route plan from My location
- Markers, My location marker, Info window font sizes + padding are increased temporarily

|               | Lat/Long/Zoom           |
|---------------|-------------------------|
| Camera        | 47.71840,18.89840,16.50 |
| Camera (iPad) | -                       |
| Location      | 47.71755,18.89761       |

---

## Screenshot — GPX tracks

- [tihanyi_kortura.gpx](../gpx/tihanyi_kortura.gpx) is opened.
- OUTDOORS layer

---

## Screenshot — Route Planner

- From Dobogókő to Rám-hegy
- OUTDOORS layer

---

## Screenshot — Destinations

- Börzsöny landscape selected
- OUTDOORS layer
- Csóványos destination selected

---


## Screenshot — OKT

- OKT 17 opened, Törökmező turistaház stamp selected

|          | Lat/Long/Zoom     |
|----------|-------------------|
| Camera   | TBD               |
| Location | 47.80728,18.94138 |

## Video demo

Pre-requisites:

- Debug build (the camera panel opens from the scale bar)
- MAP_FOLLOW_ANIM_DURATION: Duration = 1000.milliseconds
- Scene #1 is set
- No debug panel is shown
- Dark mode is ON
- Julianus_kilátótorony.gpx is imported so it's available from recents
