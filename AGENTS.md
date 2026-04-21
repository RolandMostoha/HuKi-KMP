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

Chores is a checklist which should be checked for every new feature implementation.

If the point is not involved in a feature implementation, it can be skipped.

- Unit tests
- Instrumentation tests (e.g. Repository tests)
- UI tests (Maestro E2E) - should work on both platforms
- Lint passes — ktlint, Detekt, SwiftLint
- Feature code review by AI
- What happens in offline mode?
- Dark mode (Colors)
- Device landscape mode
- Translations
- Permissions denied / not-granted paths
- Accessibility labels (e.g. strings.a11y_close)
- Docs updated — AGENTS.md / README.md

## Technology Stack
- **MapBox**: Used for the map engine.
  - Mapbox version for Android and iOS: `11.20.1`
  - Always make sure the Mapbox API / SDK functions exist and available
  - Android: MapBox is used with Jetpack Compose
  - Android API reference: https://docs.mapbox.com/android/maps/api/latest/
  - iOS: MapBox is used with SwiftUI
  - iOS API reference: https://docs.mapbox.com/ios/maps/api/latest/documentation/mapboxmaps/
- Androidx ViewModel: ViewModel bridge for KMP.
- Androidx Material3: Theme, UI Components.
- Koin: Used for DI.
- Turbine: Unit test flows `Flow.test { awaitItem() }`.
- Kotest: Unit test assertions, like `shouldBe`.
- Mokkery: The mocking library for KMP.
- Maestro: E2E UI testing for Android + iOS.

### KMP multiplatform libraries 
- SKIE - Swift - Kotlin interop tools
  - SwiftUI Observing pattern to eliminate ViewModel wrappers.
  - Coroutine bridge from KMP suspend/Flow to Swift Async/AsyncSequence.
  - Swift style + Exhaustive switching enums.
  - Global functions.
- moko-resources: Shared Strings, Colors, Images (SVG), Fonts.
  - Strings location: `shared/src/commonMain/moko-resources/base/`
  - Images location: `shared/src/commonMain/moko-resources/images/`
  - Colors location: `shared/src/commonMain/moko-resources/colors/`
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

## Coding Rules & Constraints

### General
- Don't fight the framework → use the native side best practices
- Common First: Business logic must reside in `commonMain` whenever possible.
- Prefer official + community KMP libraries for wrapping platform-specific code

### KMP
- No Java in Common: Strictly avoid `java.*` imports in `commonMain`.
- Prefer interface-based injection via Koin DI for platform-specific code.
- Expect/Actual: Use `expect`/`actual` if you want to call the function from anywhere in your code, without having to inject an instance e.g. `log("message")`, `strings("id")`.
- Use `kotlinx-datetime` for time.
- Resources: Use the `shared/src/commonMain/moko-resources` (Moko-resources) for shared strings, colors, fonts.
- SharedDimens: dimension values which shared as a 1-1 mapping with Android DP vs iOS Point

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

### E2E UI testing
- Test cases are written in Maestro `yaml` files under `./maestro/*.yaml`
- Global, reusable flows are under under `./maestro/subflows/*.yaml`
- Wherever possible, write one test case `yaml` for both Android+iOS : "written-once, test both".
- For shared test tags, use the `TestTags` object.

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

### SwiftUI - iOS
- Look as native as possible - Liquid Glass
- Use dedicated Liquid Glass components and styles where possible.
- If needed, add API wrappers for Liquid Glass styles, e.g. `if #available(iOS 26, *)`
- Entry Point: `HukiApp` + `MainView`
- UI Package for features: `/UI/Views/[feature]/`
- Don't use unnecessary blank lines between UI components.

### Gradle KTS & Libraries
- Use alphabetical order in libs.versions.toml, per section.

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