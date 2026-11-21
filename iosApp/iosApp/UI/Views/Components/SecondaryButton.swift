import Shared
import SwiftUI

struct SecondaryButton: View {
    let icon: Shared.ImageResource
    let title: String
    var size: ButtonSize = .regular
    var iconPlacement: ButtonIconPlacement = .leading
    let action: () -> Void

    var body: some View {
        BaseButton(
            icon: icon,
            title: title,
            foregroundColor: SwiftUI.Color(SharedRes.colors().primary.getUIColor()),
            backgroundColor: SwiftUI.Color(SharedRes.colors().primaryContainer.getUIColor()),
            size: size,
            iconPlacement: iconPlacement,
            action: action
        )
    }
}

#Preview {
    SecondaryButton(
        icon: .system("map.fill"),
        title: "Show on map",
        action: {}
    )
    .padding()
}

#Preview("Small") {
    SecondaryButton(
        icon: .system("map.fill"),
        title: "Show on map",
        size: .small,
        action: {}
    )
    .padding()
}

#Preview("Inline icon") {
    HStack(spacing: 12) {
        SecondaryButton(icon: .system("arrow.clockwise"), title: "Regenerate", iconPlacement: .inline, action: {})
        SecondaryButton(icon: .system("square.and.arrow.down.fill"), title: "Save", iconPlacement: .inline, action: {})
    }
    .padding()
}
