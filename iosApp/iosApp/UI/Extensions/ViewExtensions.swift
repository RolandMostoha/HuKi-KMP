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

extension UIImage {
    func resized(to newSize: CGSize) -> UIImage {
        return UIGraphicsImageRenderer(size: newSize).image { _ in
            self.draw(in: CGRect(origin: .zero, size: newSize))
        }
    }
}

struct GlassContainer<Content: View>: View {
    @ViewBuilder var content: () -> Content

    var body: some View {
        if #available(iOS 26.0, *) {
            GlassEffectContainer {
                self.content()
            }
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
