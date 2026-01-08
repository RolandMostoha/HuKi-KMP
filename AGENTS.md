# Project Context: Kotlin Multiplatform (KMP)

## Project Overview
- **Domain:** Hiking application.
- **Type:** Kotlin Multiplatform Mobile (KMP).
- **Primary Framework:** Compose Multiplatform for UI.
- **Target Platforms:** Android, iOS.
- **KMP approach**: "Do not share UI", iOS UI written in SwiftUI.
- **Module Structure:** 
- - `:composeApp`: Android native code.
- - `:iosApp`: iOS native code.
- - `:shared`: Shared kotlin code.

## Architectural Standards

## Technology Stack
- MapBox: Used for map view.
- Androidx ViewModel: ViewModel bridge for KMP
- Koin: Used for DI.
- KMP-NativeCoroutines: Coroutine bridge from KMP suspend/Flow to Swift Async/AsyncSequence.

## Architecture
- UDF (Unidirectional Data Flow), MVI
- UiState - Immutable data class describing the UI state for a screen at a point in time
- UiEvents - Intents / Actions / Inputs that trigger UI state changes

## Best Practices
### Android
#### Jetpack Compose
- Naming convention for whole pages: `[X]Screen`
- Naming convention for content in pages (to have stateless, previewable Composables): `[X]Content`
- Package for reusable UI components: `/ui/components`
- UI Package for features: `/ui/features/[feature]/`
- Always have Previews for Composables
- Only pass ViewModel to the hosting Screen's Composable

## Code Quality & Linting
### Android
- **Formatting:** We use **ktlint**. Refer to `.editorconfig` in the root for specific formatting rules.
- **Static Analysis:** We use **Detekt**. Strictly follow the rules defined in `tools/quality/HuKi-detekt.yml`.
### iOS
- **Formatting:** We use **SwiftLint**

## Coding Rules & Constraints
1. **Common First:** Logic must reside in `commonMain` whenever possible.
2. **Expect/Actual:** Use `expect`/`actual` keywords only when a library wrapper isn't available. Prefer interface-based injection via Koin for platform-specific code.
3. **No Java in Common:** Strictly avoid `java.*` imports in `commonMain`. Use `kotlinx-datetime` for time.
4. **UI Components:** Keep Composables stateless by hoisting state to ViewModels.
5. **Resources:** Use the `composeRes` (Moko-resources or standard Compose resources) for strings and images to ensure cross-platform compatibility.
