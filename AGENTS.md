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
- `MapBox`: Used for map view.

## Code Quality & Linting
- **Formatting:** We use **ktlint**. Refer to `.editorconfig` in the root for specific formatting rules.
- **Static Analysis:** We use **Detekt**. Strictly follow the rules defined in `tools/quality/HuKi-detekt.yml`.

## Coding Rules & Constraints
1. **Common First:** Logic must reside in `commonMain` whenever possible.
2. **Expect/Actual:** Use `expect`/`actual` keywords only when a library wrapper isn't available. Prefer interface-based injection via Koin for platform-specific code.
3. **No Java in Common:** Strictly avoid `java.*` imports in `commonMain`. Use `kotlinx-datetime` for time.
4. **UI Components:** Keep Composables stateless by hoisting state to ViewModels.
5. **Resources:** Use the `composeRes` (Moko-resources or standard Compose resources) for strings and images to ensure cross-platform compatibility.
