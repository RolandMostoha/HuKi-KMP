import Shared
import SwiftUI

struct PrimaryButton: View {
    let systemImage: String
    let title: String
    let action: () -> Void

    var body: some View {
        BaseButton(
            systemImage: systemImage,
            title: title,
            foregroundColor: SwiftUI.Color(SharedRes.colors().onPrimary.getUIColor()),
            backgroundColor: SwiftUI.Color(SharedRes.colors().primary.getUIColor()),
            verticalPadding: 12,
            action: action
        )
    }
}

#Preview {
    PrimaryButton(
        systemImage: "location.north.fill",
        title: "Show on map",
        action: {}
    )
    .padding()
}
