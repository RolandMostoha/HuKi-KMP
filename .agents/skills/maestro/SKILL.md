---
name: maestro
description: Write and run Maestro E2E flows for the HuKi KMP app on Android emulator or iOS simulator. Use when the user asks to run E2E tests, write a new Maestro flow, debug a failing flow, verify a feature on device/simulator via UI automation, or add coverage for a new screen. Covers project-specific app IDs, TestTags, runner scripts, and the existing flow conventions under `.maestro/`.
---

# Maestro (HuKi KMP)

E2E UI testing for both Android and iOS from a single YAML flow ("written-once, test both").

## Project setup

- **Flows live in** `.maestro/maestro_*.yaml` (one feature per file). Reusable subflows go in `.maestro/subflows/*.yaml`.
- **App IDs** (passed as `${APP_ID}`):
  - Android: `hu.mostoha.mobile.android.huki`
  - iOS: `hu.mostoha.mobile.ios.huki`
- **Every flow starts with** `appId: ${APP_ID}` so the same YAML runs on both platforms.

## Running flows

### Run the whole suite

```bash
./tools/scripts/shared_run_maestro_tests.sh <APP_ID> <DEVICE_ID>
```

Auto-uploads GPX fixtures (iOS or Android, based on `APP_ID`) before running. Debug output is written to the repo root.

### Run a single flow

```bash
export MAESTRO_CLI_NO_ANALYTICS=1
maestro --no-ansi test .maestro/maestro_menu.yaml \
  -e APP_ID=hu.mostoha.mobile.android.huki \
  --device <DEVICE_ID>
```

### Getting a device ID

- **Android emulator**: `adb devices` → use the `emulator-XXXX` ID. Boot one from Android Studio if none is running.
- **iOS simulator**: `./tools/scripts/ios_get_booted_device_id.sh` (prints the UUID of the booted simulator). Boot from Xcode if none is running.

### Before running on a fresh install

The runner script already calls these per-platform, but if you're running a single flow that needs GPX fixtures:

- Android: `./tools/scripts/android_upload_test_gpx_files.sh`
- iOS: `./tools/scripts/ios_upload_test_gpx_files.sh`

## Writing flows — project conventions

### Prefer TestTag IDs over text matching

Strings vary by language (`android_toggle_language.sh` toggles EN/HU). Always select by `id:` when a tag exists. Tags are defined once in `shared/src/commonMain/kotlin/hu/mostoha/mobile/kmp/huki/util/TestTags.kt` and applied via `Modifier.testTag(...)` (Android) or `.accessibilityIdentifier(...)` (iOS).

```yaml
# Good — survives language toggle
- tapOn:
    id: MENU_BACK_BUTTON

# Avoid for navigation-critical taps — breaks under HU locale
- tapOn: "Menu"
```

Text assertions are fine for verifying user-visible copy (and when no tag exists), but anchor navigation on IDs.

If you need a tag that doesn't exist yet, add it to `TestTags.kt` first, wire it into the Composable/SwiftUI view, then write the flow.

### Standard flow skeleton

```yaml
appId: ${APP_ID}
---
- launchApp:
    clearState: true
    permissions: { all: allow }
- extendedWaitUntil:
    visible:
      id: MAP_MAPBOX
    timeout: 10000

# ...feature steps...
```

`MAP_MAPBOX` is the map host on the main screen — wait for it before interacting, because the map render is the slowest cold-start step.

### Platform gotchas

- **iOS Liquid Glass / safe area**: tap targets near the top/bottom edges can be partially covered by translucent bars. Prefer `id:` targets; if you must tap by text, scroll the element to center first.
- **Permissions**: grant up-front via `launchApp.permissions: { all: allow }`. Location is required for `MAIN_FAB_MY_LOCATION_BUTTON` flows.
- **Map ready ≠ tiles loaded**: `MAP_MAPBOX` visible only means the host is composed. For map-content interactions, add a short `waitForAnimationToEnd` or assert on a known overlay (e.g. `MAIN_SCALE_BAR`).
- **GPX-dependent flows**: assume the upload script has already populated `/sdcard/Download` (Android) or the iOS app Documents container. Don't re-upload inside flows.
- **`setLocation`**: works on simulator/emulator, but Mapbox may need a frame to react — follow with `waitForAnimationToEnd` before asserting on the location FAB.

### Reference flows

When writing a new flow, start by reading the closest existing one:

- Menu + navigation: `.maestro/maestro_menu.yaml`
- Search: `.maestro/maestro_search.yaml`
- Layers: `.maestro/maestro_layers.yaml`
- My location: `.maestro/maestro_my_location.yaml`
- GPX: `.maestro/maestro_gpx.yaml`

## Debugging

- Add `- takeScreenshot: name` at suspect steps; screenshots land in `./debug-output-*/`.
- Use `maestro studio` for an interactive REPL against the running app — fastest way to discover the correct selectors.
- If `assertVisible` flakes, switch to `extendedWaitUntil` with an explicit `timeout`.

## Command reference

Full list at https://docs.maestro.dev/reference/commands-available.md. Frequently used in this project:

| Command                              | Use for                                       |
|--------------------------------------|-----------------------------------------------|
| `launchApp`                          | Start the app with permissions + `clearState` |
| `tapOn`                              | Tap by `id:` (preferred) or text              |
| `assertVisible` / `assertNotVisible` | Verify UI state                               |
| `extendedWaitUntil`                  | Wait for slow elements (map, network)         |
| `inputText` / `eraseText`            | Search box, forms                             |
| `scroll` / `scrollUntilVisible`      | Settings lists, search results                |
| `back`                               | Native back navigation                        |
| `setLocation`                        | Fake GPS for location-based features          |
| `takeScreenshot`                     | Debug a failing flow                          |
| `runFlow`                            | Compose a flow from subflows                  |

### Querying Maestro docs

If a command's behavior is unclear, query the docs directly:

```
GET https://docs.maestro.dev/reference/commands-available.md?ask=<question>
```

The question should be specific and self-contained. Returns a direct answer plus relevant excerpts.
