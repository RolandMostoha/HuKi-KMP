import Shared
import SwiftUI

struct PrimaryButton: View {
    let icon: Shared.ImageResource
    let title: String
    var size: ButtonSize = .regular
    var iconPlacement: ButtonIconPlacement = .leading
    let action: () -> Void

    var body: some View {
        BaseButton(
            icon: icon,
            title: title,
            foregroundColor: SwiftUI.Color(SharedRes.colors().onPrimary.getUIColor()),
            backgroundColor: SwiftUI.Color(SharedRes.colors().primary.getUIColor()),
            size: size,
            iconPlacement: iconPlacement,
            action: action
        )
    }
}

#Preview {
    PrimaryButton(
        icon: .system("location.north.fill"),
        title: "Show on map",
        action: {}
    )
    .padding()
}

#Preview("Small") {
    PrimaryButton(
        icon: .system("map.fill"),
        title: "Show on map",
        size: .small,
        action: {}
    )
    .padding()
}

#Preview("Inline icon") {
    HStack(spacing: 12) {
        PrimaryButton(icon: .system("arrow.clockwise"), title: "Regenerate", iconPlacement: .inline, action: {})
        PrimaryButton(icon: .system("square.and.arrow.down.fill"), title: "Save", iconPlacement: .inline, action: {})
    }
    .padding()
}
