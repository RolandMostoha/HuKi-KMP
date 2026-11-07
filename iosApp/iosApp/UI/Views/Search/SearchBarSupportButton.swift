import Shared
import SwiftUI

struct SearchBarSupportButton: View {
    let supporterAnimal: SupportAnimal
    let onClick: () -> Void

    private let strings = Strings()
    private let size: CGFloat = 44

    var body: some View {
        Button(action: onClick) {
            supporterAnimal.iconImage
                .resizable()
                .scaledToFit()
                .foregroundStyle(supporterAnimal.color)
                .frame(width: size * 0.62, height: size * 0.62)
                .frame(width: size, height: size)
                .background(supporterAnimal.containerColor, in: .circle)
                .contentShape(Circle())
        }
        .buttonStyle(PressFeedbackButtonStyle())
        .accessibilityLabel(strings.get(id: SharedRes.strings().menu_a11y_open_support))
        .accessibilityValue(
            strings.get(
                id: SharedRes.strings().support_supporter_label,
                args: [strings.get(id: supporterAnimal.nameRes)]
            )
        )
        .accessibilityIdentifier(TestTags.shared.MAIN_SEARCH_BAR_SUPPORT_BUTTON)
    }
}

#Preview {
    HStack {
        SearchBarSupportButton(supporterAnimal: .owl, onClick: {})
        SearchBarSupportButton(supporterAnimal: .bear, onClick: {})
    }
    .padding()
}
