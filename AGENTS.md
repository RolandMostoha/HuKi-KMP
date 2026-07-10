# Project Context: Kotlin Multiplatform (KMP)

## Build Commands

### Android
```bash
./gradlew :composeApp:assembleDebug           # Build debug APK
./gradlew :shared:testDebugUnitTest           # Run unit tests
./gradlew :composeApp:ktlintCheck             # Run KtLint
./gradlew :composeApp:detekt                  # Run Detekt
./gradlew :composeApp:lint                    # Run Android Lint
./gradlew :composeApp:connectedAndroidTest    # Run instrumented tests (requires emulator/device)
```
- Android Studio: **"All Instrumented Tests"** run config (`.idea/runConfigurations/All_Instrumented_Tests.xml`) runs every instrumented test in `composeApp` on a connected device/emulator with the native test UI.

### iOS
Build on Booted device:
```bash
./xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -configuration Debug \
  -destination "platform=iOS Simulator,id=$(xcrun simctl list devices | grep Booted | awk -F '[()]' '{print $2}')"
```
Lint:
```bash
(cd iosApp && swiftlint)
```

### Shared
- Build: `./gradlew :shared:compileKotlinIosArm64`
- Tests: `./gradlew :shared:testDebugUnitTest`

## Utility Scripts

### iOS
- `ios_get_booted_device_id.sh` — print the UUID of the currently booted iOS simulator.
- `ios_reset_simulator.sh` — factory-reset the booted simulator (shutdown + erase + reboot). Use for: "reset simulator", "wipe simulator", "clean simulator state".
- `ios_fix_location.sh [lat,lon]` — fix a wedged location (defaults to Dobogókő). Use when the simulator loses GPS signal.
- `ios_remove_app.sh` — uninstall `hu.mostoha.mobile.ios.huki` from the booted simulator. Use for: "remove app", "uninstall app on iOS".
- `ios_upload_test_gpx_files.sh` — copy every `tools/gpx/*.gpx` into the iOS app's Documents container on the booted simulator. Needed for Maestro tests on iOS where GPX import is necessary.
- `ios_toggle_language.sh` — toggle the booted simulator's **global** language between Hungarian (`hu-HU`) and English (`en-US`) by writing `AppleLanguages`/`AppleLocale` to `NSGlobalDomain` (persists across app reinstalls / Xcode runs), then relaunching the app. Use for: "switch language (iOS)", "toggle language on iOS".
- `ios_run_on_device.sh [device-name]` — build, install and launch the app on a connected physical iPhone via `xcodebuild` + `devicectl`

### Android
- `android_toggle_dark_mode.sh` — toggle the connected device/emulator's night mode. Use for: "toggle dark mode (Android)".
- `android_toggle_internet.sh` — toggle Wi-Fi + cellular data together on the connected device. Use for: "toggle internet", "go offline" / "go online" on Android (e.g. testing offline mode chore).
- `android_toggle_language.sh` — toggle the HuKi app's per-app language between Hungarian (`hu-HU`) and English (`en-US`).
- `android_upload_test_gpx_files.sh` — `adb push` every `tools/gpx/*.gpx` into `/sdcard/Download`. Use for: "upload test gpx files (Android)", or before running Maestro tests on Android.

### Cross-platform
- `android_run_all_maestro_tests.sh [device-serial]` — run all `.maestro/maestro_*.yaml` tests against the Android app. Defaults to the first connected adb device. Use for: "run maestro tests (Android)", "run E2E tests (Android)".
- `ios_run_all_maestro_tests.sh [simulator-udid]` — run all `.maestro/maestro_*.yaml` tests against the iOS app. Defaults to the currently booted simulator. Use for: "run maestro tests (iOS)", "run E2E tests (iOS)".
- `shared_run_maestro_tests.sh <APP_ID> <DEVICE_ID>` — engine that runs all `.maestro/maestro_*.yaml` tests against the given app and device, auto-uploading GPX fixtures based on whether `APP_ID` contains `.ios.` or `.android.`. Prefer the platform wrappers above, which fill in the APP_ID and booted device for you.

## Project Overview
- **Domain**: Hiking application for Hungarian landscapes, trails, destinations.
- **Type**: Kotlin Multiplatform (KMP).
- **Target Platforms**: Android, iOS.
- **KMP approach**: "Do not share UI", so iOS UI is written in SwiftUI.
- **UI Frameworks**: Jetpack Compose for Android, SwiftUI for iOS.
- **Target Platform APIs**:
  - Android: minSdk=26, targetSdk=36
  - iOS: Xcode=26.1.1+, Deployment Target=18.2
- **Package IDs**:
  - Android: `hu.mostoha.mobile.android.huki`
  - iOS: `hu.mostoha.mobile.ios.huki`
- **Project Structure**:
  - `:composeApp`: Android native code.
  - `:iosApp`: iOS native code.
  - `:shared`: Shared kotlin code.
    - `:shared:commonMain`: Common code.
    - `:shared:androidMain`: Android specific shared code.
    - `:shared:iosMain`: iOS specific shared code.
- **Supported app languages**: English, Hungarian.
- **Supported device orientations**: Portrait and Landscape.

## Chores

Chores is a checklist which should be checked for every "feature complete" code review.

- Unit tests
- Instrumentation tests (e.g. Repository tests)
- UI tests (Maestro E2E) - should work on both platforms
- Lint passes — ktlint, Detekt, SwiftLint
- Compose Previews
- Potential re-usable UI components
- Independent UI styling - Material Design / SwiftUI guideline
- Dark mode (Colors)
- Device landscape mode
- Translations
- Accessibility labels (e.g. strings.a11y_close)
- TestTag IDs for Maestro element targets
- Always ask: what happens with this feature in offline mode? -> for a hiking app offline mode is crucial
- Permissions denied / not-granted paths
- Docs updated — AGENTS.md / README.md

## Technology Stack
- **MapBox**: Used for the map engine.
  - Mapbox version for Android and iOS: `11.20.1`
  - Always make sure the Mapbox API / SDK functions exist and available
  - Android: MapBox is used with Jetpack Compose
  - Android API reference: https://docs.mapbox.com/android/maps/api/latest/
  - iOS: MapBox is used with SwiftUI
  - iOS API reference: https://docs.mapbox.com/ios/maps/api/latest/documentation/mapboxmaps/
- **Location**: `LocationMonitoringService` (commonMain) is the shared location source — a `locationUpdates` stream plus a one-shot `lastKnownLocation()`.
  - Android: backed by Mapbox's `LocationService`. It uses Google's Fused Location Provider automatically when `play-services-location` is on the classpath.
  - iOS: backed by CoreLocation (`CLLocationManager`).
- Androidx ViewModel: ViewModel bridge for KMP.
- Androidx Material3: Theme, UI Components.
- Koin: Used for DI.
- Turbine: Unit test flows `Flow.test { awaitItem() }`.
- Kotest: Unit test assertions, like `shouldBe`.
- Mokkery: The mocking library for KMP.
- Maestro: E2E UI testing for Android + iOS.

## Git Workflow

### Branch Naming

```
feature/<short_description>
fix/<short_description>
```

- Use lowercase snake_case for the description.
- Examples: `feature/gpx`, `feature/search`, `fix/my_location_permissions`

### Commit Messages

```
feat(Scope): short description in lowercase
```

- **Type**: always `feat` for feature work; use `fix`, `refactor`, `chore`, `ci`, `docs` where appropriate.
- **Scope**: PascalCase, matching the feature or module name (e.g. `GPXDetails`, `Search`, `CI`, `Logger`).
- **Description**: lowercase, imperative mood, no trailing period.
- Examples:
    - `feat(Search): add LocationIQ autocomplete with Ktor`
    - `feat(GPX): add GPX Details bottom sheet`
    - `fix(Logger): trim long lists from UiState logging`
    - `ci(CI): cancel previous in-progress GitHub workflows`

### KMP multiplatform libraries 
- SKIE - Swift - Kotlin interop tools
  - SwiftUI Observing pattern to eliminate ViewModel wrappers.
  - Coroutine bridge from KMP suspend/Flow to Swift Async/AsyncSequence.
  - Swift style + Exhaustive switching enums.
  - Global functions.
- moko-resources: Shared Strings, Colors, Images (SVG), Fonts.
  - Strings 
    - Location: `shared/src/commonMain/moko-resources/base/`
    - Usage: `SharedRes.strings().*`
  - Images 
    - Location: `shared/src/commonMain/moko-resources/images/`
    - Usage: `SharedRes.images().*`
  - Colors 
    - Location: `shared/src/commonMain/moko-resources/colors/`
    - Usage: `SharedRes.colors().*`
- Kermit: Logging.
  - E.g. `Logger.e(exception) { "Network: Failed serialization." }`
  - E.g. `Logger.d { "Map: Camera moved to $latLng" }`
- filekit: File handling (used for reading/writing GPX files).
- Ktor: Networking, Rest APIs.
- kotlin-serialization: Serialization. 
- Spatial K
  - `:gpx` GPX parsing 
  - `:turf` Geo utilities: distance, bearing etc.
  - `:units` Unit conversions, e.g. `5.kilometers`.

## Architecture
- UDF (Unidirectional Data Flow), MVI
- ViewModel - Bridge between UI and business logic.
- UiState - Immutable data class describing the UI state for a screen at a point in time.
- UiEvents - Intents / Actions / Inputs that trigger UI state changes.
- UiEffects - One-shot events (Toasts, Navigation).

```
UI → UiEvent → ViewModel → UiState
                  ↓
               UiEffect
```

### Architecture rules
- One ViewModel per screen.
- UiState = StateFlow
- UiEffect = Channel → Flow

### Navigation
- **Native per platform, shared ViewModel/state.** Each platform owns its own back stack using its native API.
  - Android: Jetpack `androidx.navigation.compose` — single `NavHost` hosted by `RootNavHost`.
  - iOS: SwiftUI `NavigationStack` with a `@State NavigationPath`, declared in `MainView`.

## Coding Rules & Constraints

### General
- Don't fight the framework → use the native side best practices, avoid platform anti-patterns
- Common First: Business logic must reside in `commonMain` whenever possible.
- Prefer official + community KMP libraries for wrapping platform-specific code
- Use comments only if necessary. If necessary, preferred: 1 line, max: 2 lines. If need more than 3 lines: ask.
- Don't use comments for Composables/SwiftUI views. Previews are much better than comments.

### KMP
- No Java in Common: Strictly avoid `java.*` imports in `commonMain`.
- Prefer interface-based injection via Koin DI for platform-specific code.
- Expect/Actual: Use `expect`/`actual` if you want to call the function from anywhere in your code, without having to inject an instance e.g. `log("message")`, `strings("id")`.
- Use `kotlinx-datetime` for time.
- Use `kotlin.time.Duration` for duration.
- Resources: Use the `shared/src/commonMain/moko-resources` (Moko-resources) for shared strings, colors, fonts.
- SharedDimens: dimension values which shared as a 1-1 mapping with Android DP vs iOS Point

### Mappers
- Type mappers between layers (data↔domain, domain↔domain, platform↔domain) are **top-level extension functions** named `to<Target>()`, grouped by the domain concept they map in `model/mapper/<Concept>Mapper.kt` (e.g. `GpxMapper.kt`, `MapboxMapper.kt`).
- Do **not** place mappers on the model classes themselves: a `model/data` class must not import `model/domain` types (and vice versa), so co-locating a mapper in the model file leaks a cross-layer dependency.
- Keep mappers out of repositories/ViewModels — they belong in `model/mapper` so they stay reusable and unit-testable.
- Use an injectable Mapper **class** (Koin) only when the mapping needs a dependency (formatter, locale, clock, resource provider).

### Unit tests
- Use `Given X, When Y, Then Z`
- Use test functions like:
```
val [input] = X

val [actual] = operation(X)

[actual] shouldBe [expected]
```
- Use Kotest assertions
- Use Turbine for `Flow` testing
- For pure input → output mappings with many cases (parsers, enum, error mappers, etc.), use parametrized tests, pattern: a single `@Test` that iterates a `testCases()` list of `TestCase(input, result)` from the companion object. See `NetworkErrorMapperTest` as reference.

### E2E UI testing
- Test cases are written in Maestro `yaml` files under `./maestro/*.yaml`
- Global, reusable flows are under under `./maestro/subflows/*.yaml`
- Wherever possible, write one test case `yaml` for both Android+iOS : "written-once, test both".
- For shared test tags, use the `TestTags` object.
- Important: the tests where GPX import is needed (e.g. maestro_gpx), use [platform]_upload_test_gpx_files.sh to pre-load GPX fixtures before running the tests.

### Jetpack Compose - Android
- Look as native as possible - Material3
- Entry Point: `HuKiApplication` + `MainActivity` + `MainScreen`
- Naming convention for whole pages: `[X]Screen`
- Naming convention for content in pages `[X]Content` (to have stateless, previewable Composables): 
- Package for reusable UI components: `/ui/components`
- UI Package for features: `/ui/features/[feature]/`
- Use @Preview whenever possible.
- Only pass ViewModel to the hosting Screen's Composable
- UI Components: keep Composables stateless.
- Avoid fully qualified symbols in code when a normal import can be used, e.g. prefer `Alignment.CenterVertically` over `androidx.compose.ui.Alignment.CenterVertically`.
- Don't use unnecessary blank lines between UI components.
- Always use animations for UI transitions, avoid flashing transitions.
- Always respect edge-to-edge `windowInsets` for screens.
- Prefer icons from the official Google Font icon set: https://fonts.google.com/icons.
- Prefer official Material3 components instead of custom views
- Whenever makes sense, extract UI components to separate classes -> `@Composable` functions.

### SwiftUI - iOS
- Look as native as possible - Liquid Glass
- Use dedicated Liquid Glass components and styles where possible.
- If needed, add API wrappers for Liquid Glass styles, e.g. `if #available(iOS 26, *)`
- Entry Point: `HukiApp` + `MainView`
- UI Package for features: `/UI/Views/[feature]/`
- Don't use unnecessary blank lines between UI components.
- Always use animations for UI transitions, avoid flashing transitions.
- Always respect edge-to-edge `.safeArea` for screens.
- Prefer icons from the official Apple SF Symbols icon set: https://developer.apple.com/sf-symbols/.
- Prefer official SwiftUi components instead of custom views
- Whenever makes sense, extract UI components to separate classes → E.g. `struct DestinationPreviewCard: View`

### Gradle KTS & Libraries
- Use alphabetical order in libs.versions.toml, per section.

### Good to know
- `shared/**/data/Destinations.kt` is a large (~3000-line) static data. Read it with `grep`/ranged reads rather than loading the whole.
- `tools/planning/PLANNING.md` is the live plan board — roadmap, backlog, bugs, and per-feature task lists (with status legend). Read it before starting feature or bugfix work to "see ahead".

## Code Quality & Linting
### Android
- **Formatting:** Use **ktlint**. Refer to `.editorconfig` in the root for specific formatting rules.
- **Static Analysis:** Use **Detekt**. Strictly follow the rules defined in `tools/quality/HuKi-detekt.yml`.
### iOS
- **Formatting:** Use **SwiftLint**

## Secrets
- Always check `.aiexclude`
- Secrets live in `secrets.properties` at the repo root (gitignored, `.aiexclude`d).
- `GenerateSecretsTask` pastes property values verbatim into a generated `Secrets.kt` `object` as `const val` declarations.
- **Convention**: values in `secrets.properties` MUST be valid Kotlin string literals, e.g: `LOCATION_IQ_API_KEY="pk.abc123"`.
- Unquoted values will produce a `Secrets.kt` that fails to compile.