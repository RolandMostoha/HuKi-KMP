import Shared
import SwiftUI

struct CloseButton: View {
    let action: () -> Void
    var accessibilityIdentifier: String?

    private let strings = Strings()

    var body: some View {
        Button(action: action) {
            Image(systemName: "xmark")
                .font(.system(size: 20, weight: .semibold))
                .foregroundColor(.primary)
                .padding(12)
                .glassBackground()
        }
        .accessibilityLabel(strings.get(id: SharedRes.strings().a11y_close))
        .identifier(accessibilityIdentifier)
    }
}

#Preview {
    ZStack {
        LinearGradient(colors: [.green.opacity(0.35), .mint.opacity(0.15)], startPoint: .top, endPoint: .bottom)
        CloseButton(action: {})
    }
    .ignoresSafeArea()
}
