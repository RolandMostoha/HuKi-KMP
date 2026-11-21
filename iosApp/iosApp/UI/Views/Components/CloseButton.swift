import Shared
import SwiftUI

struct CloseButton: View {
    let action: () -> Void
    var accessibilityIdentifier: String?
    var iconSize: CGFloat?

    private let strings = Strings()

    var body: some View {
        GlassIconButton(
            systemName: "xmark",
            accessibilityLabel: strings.get(id: SharedRes.strings().a11y_close),
            accessibilityIdentifier: accessibilityIdentifier,
            iconSize: iconSize,
            action: action
        )
    }
}

#Preview {
    ZStack {
        LinearGradient(colors: [.green.opacity(0.35), .mint.opacity(0.15)], startPoint: .top, endPoint: .bottom)
        CloseButton(action: {})
    }
    .ignoresSafeArea()
}
