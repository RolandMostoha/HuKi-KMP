import Shared
import SwiftUI

struct SecondaryButton: View {
    let systemImage: String
    let title: String
    let action: () -> Void

    var body: some View {
        BaseButton(
            systemImage: systemImage,
            title: title,
            foregroundColor: SwiftUI.Color(SharedRes.colors().primary.getUIColor()),
            backgroundColor: SwiftUI.Color(SharedRes.colors().primaryContainer.getUIColor()),
            verticalPadding: 14,
            action: action
        )
    }
}

#Preview {
    SecondaryButton(
        systemImage: "map.fill",
        title: "Show on map",
        action: {}
    )
    .padding()
}
