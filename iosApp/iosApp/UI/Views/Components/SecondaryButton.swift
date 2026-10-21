import Shared
import SwiftUI

struct SecondaryButton: View {
    let icon: Shared.ImageResource
    let title: String
    var size: ButtonSize = .regular
    let action: () -> Void

    var body: some View {
        BaseButton(
            icon: icon,
            title: title,
            foregroundColor: SwiftUI.Color(SharedRes.colors().primary.getUIColor()),
            backgroundColor: SwiftUI.Color(SharedRes.colors().primaryContainer.getUIColor()),
            size: size,
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
