import SwiftUI

struct GlassIconButton: View {
    let systemName: String
    let accessibilityLabel: String
    var accessibilityIdentifier: String?
    var iconSize: CGFloat?
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(systemName: systemName)
                .font(.system(size: 20, weight: .semibold))
                .foregroundColor(.primary)
                .frame(width: iconSize, height: iconSize)
                .padding(12)
                .glassBackground()
        }
        .accessibilityLabel(accessibilityLabel)
        .identifier(accessibilityIdentifier)
    }
}

#Preview {
    ZStack {
        LinearGradient(colors: [.green.opacity(0.35), .mint.opacity(0.15)], startPoint: .top, endPoint: .bottom)
        HStack {
            GlassIconButton(systemName: "gearshape", accessibilityLabel: "", iconSize: 24, action: {})
            GlassIconButton(systemName: "xmark", accessibilityLabel: "", iconSize: 24, action: {})
        }
    }
    .ignoresSafeArea()
}
