import Shared
import SwiftUI

struct SecondaryButton: View {
    let icon: Shared.ImageResource
    let title: String
    let action: () -> Void

    var body: some View {
        BaseButton(
            icon: icon,
            title: title,
            foregroundColor: SwiftUI.Color(SharedRes.colors().primary.getUIColor()),
            backgroundColor: SwiftUI.Color(SharedRes.colors().primaryContainer.getUIColor()),
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
