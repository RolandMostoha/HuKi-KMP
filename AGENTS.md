# Project Context: Kotlin Multiplatform (KMP)

## Project Overview
- **Domain**: Hiking application for Hungarian landscapes, trails, destinations.
- **Type**: Kotlin Multiplatform (KMP).
- **Target Platforms**: Android, iOS.
- **KMP approach**: "Do not share UI", so iOS UI is written in SwiftUI.
- **UI Frameworks**: Jetpack Compose for Android, SwiftUi for iOS.
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

## Technology Stack
- MapBox: Used for map view.
- Androidx ViewModel: ViewModel bridge for KMP.
- Koin: Used for DI.
- KMP-NativeCoroutines: Coroutine bridge from KMP suspend/Flow to Swift Async/AsyncSequence.
- moko-resources: Share resources (String, Colors, Images, Fonts) between iOS/Android.
- Turbine: Unit test flows `Flow.test { awaitItem() }`.
- Kotest: Unit test assertions, like `shouldBe`.
- Maestro: E2E UI testing for Android + iOS.

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

## KMP
- Common First: Logic must reside in `commonMain` whenever possible.
- No Java in Common: Strictly avoid `java.*` imports in `commonMain`.
- Prefer KMP libraries for wrapping platform-specific code.
- Prefer interface-based injection via Koin DI for platform-specific code.
- Expect/Actual: Use `expect`/`actual` if you want to call the function from anywhere in your code, without having to inject an instance e.g. `log("message")`, `strings("id")`.
- Use `kotlinx-datetime` for time.
- Resources: Use the `shared/src/commonMain/moko-resources` (Moko-resources) for strings and images to ensure cross-platform compatibility.

## Unit tests
- Use `Given X, when Y, then Z`
- You may skip `given` where it's not suitable (ex: without input parameters)
- Use Kotest assertions
- Use Turbine for `Flow` testing

## E2E UI testing
- Test cases are written in Maestro `yaml` files under `./maestro/*.yaml`
- Global, reusable flows are under under `./maestro/subflows/*.yaml`
- Wherever possible, write one test case `yaml` for both Android+iOS : "written-once, test both"
- For shared test tags, use the `TestTags` object

### Jetpack Compose - Android
- Naming convention for whole pages: `[X]Screen`
- Naming convention for content in pages `[X]Content` (to have stateless, previewable Composables): 
- Package for reusable UI components: `/ui/components`
- UI Package for features: `/ui/features/[feature]/`
- Always have Previews for Composables
- Only pass ViewModel to the hosting Screen's Composable
- UI Components:Keep Composables stateless by hoisting state to ViewModels.

### SwiftUI - iOS
- Use dedicated Liquid Glass components and styles where possible.
- If needed, add API wrappers for Liquid Glass styles, e.g. `if #available(iOS 26, *)`

### Code Quality & Linting
### Android
- **Formatting:** Use **ktlint**. Refer to `.editorconfig` in the root for specific formatting rules.
- **Static Analysis:** Use **Detekt**. Strictly follow the rules defined in `tools/quality/HuKi-detekt.yml`.
### iOS
- **Formatting:** Use **SwiftLint**
