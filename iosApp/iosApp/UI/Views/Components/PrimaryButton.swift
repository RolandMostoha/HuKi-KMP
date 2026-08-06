import Shared
import SwiftUI

struct PrimaryButton: View {
    let icon: Shared.ImageResource
    let title: String
    let action: () -> Void

    var body: some View {
        BaseButton(
            icon: icon,
            title: title,
            foregroundColor: SwiftUI.Color(SharedRes.colors().onPrimary.getUIColor()),
            backgroundColor: SwiftUI.Color(SharedRes.colors().primary.getUIColor()),
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
