import Shared
import SwiftUI

struct PressFeedbackButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .opacity(configuration.isPressed ? 0.55 : 1.0)
            .scaleEffect(configuration.isPressed ? 0.96 : 1.0)
            .animation(.easeOut(duration: 0.12), value: configuration.isPressed)
    }
}

extension View {
    @ViewBuilder
    func glassButtonStyle() -> some View {
        if #available(iOS 26, *) {
            buttonStyle(.glass)
        } else {
            buttonStyle(.borderedProminent)
                .foregroundStyle(.primary)
                .tint(.clear)
        }
    }
}

struct GlassContainer<Content: View>: View {
    @Environment(\.colorScheme) private var colorScheme
    @ViewBuilder var content: () -> Content

    var body: some View {
        if #available(iOS 26.0, *) {
            GlassEffectContainer {
                self.content()
            }
            .id(colorScheme)
            .transition(.opacity)
            .animation(.easeInOut(duration: 0.2), value: colorScheme)
        } else {
            content()
                .background(
                    RoundedRectangle(cornerRadius: 40, style: .continuous)
                        .fill(.ultraThinMaterial)
                )
                .overlay(
                    RoundedRectangle(cornerRadius: 40, style: .continuous)
                        .strokeBorder(.white.opacity(0.15), lineWidth: 1)
                )
                .shadow(color: .black.opacity(0.08), radius: 12, x: 0, y: 6)
        }
    }
}

extension View {
    @ViewBuilder
    func glassBackground() -> some View {
        if #available(iOS 26.0, *) {
            glassEffect(in: Capsule())
        } else {
            background(.ultraThinMaterial, in: Capsule())
                .overlay(Capsule().strokeBorder(.white.opacity(0.15), lineWidth: 1))
                .shadow(color: .black.opacity(0.08), radius: 12, x: 0, y: 6)
        }
    }

    @ViewBuilder
    func glassBackground(_ style: GlassStyle = .clear, in shape: some Shape) -> some View {
        if #available(iOS 26.0, *) {
            glassEffect(style.glass, in: shape)
        } else {
            background(style.fallbackMaterial, in: shape)
        }
    }
}

enum GlassStyle {
    case clear
    case regular

    @available(iOS 26.0, *)
    var glass: Glass {
        switch self {
        case .clear: return .clear
        case .regular: return .regular
        }
    }

    var fallbackMaterial: Material {
        switch self {
        case .clear: return .ultraThinMaterial
        case .regular: return .regularMaterial
        }
    }
}

extension View {
    @ViewBuilder
    func glassUnion(id: String, namespace: Namespace.ID) -> some View {
        if #available(iOS 26.0, *) {
            glassEffectUnion(id: id, namespace: namespace)
        } else {
            self
        }
    }
}

struct FloatingButtonPadding: ViewModifier {
    let type: FloatingButtonType

    func body(content: Content) -> some View {
        switch type {
        case .top:
            content
                .padding(.top, 16)
                .padding(.bottom, 6)
                .padding(.horizontal, 5)
        case .bottom:
            content
                .padding(.top, 6)
                .padding(.bottom, 16)
                .padding(.horizontal, 5)
        }
    }
}

enum FloatingButtonType {
    case top
    case bottom
}

extension View {
    func floatingButtonPadding(_ type: FloatingButtonType) -> some View {
        modifier(FloatingButtonPadding(type: type))
    }
}

extension View {
    // Keeps SwiftUI's own identity when no test tag applies, rather than stamping an empty one.
    @ViewBuilder
    func identifier(_ identifier: String?) -> some View {
        if let identifier {
            accessibilityIdentifier(identifier)
        } else {
            self
        }
    }
}
