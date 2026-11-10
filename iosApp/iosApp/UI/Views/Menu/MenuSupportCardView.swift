import Shared
import SwiftUI

struct MenuSupportCardView: View {
    let title: String
    let message: String
    let buttonTitle: String
    let accessibilityLabel: String
    let testTag: String
    let action: () -> Void

    private let cardColor = SwiftUI.Color(.systemBackground)

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            SupportAnimalBadgesView(ringColor: cardColor, size: 36)
            Text(title)
                .font(.title3.weight(.bold))
                .foregroundStyle(.primary)
                .padding(.top, 14)
            Text(message)
                .font(.subheadline)
                .lineSpacing(2)
                .foregroundStyle(.secondary)
                .fixedSize(horizontal: false, vertical: true)
                .padding(.top, 4)
            supportButton
                .padding(.top, 16)
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(cardColor, in: RoundedRectangle(cornerRadius: 26, style: .continuous))
    }

    private var supportButton: some View {
        BaseButton(
            icon: .system("heart.fill"),
            title: buttonTitle,
            foregroundColor: .white,
            backgroundColor: SwiftUI.Color(SharedRes.colors().supportRed.getUIColor()),
            action: action
        )
        .accessibilityLabel(accessibilityLabel)
        .accessibilityIdentifier(testTag)
    }
}

#Preview {
    MenuSupportCardView(
        title: "HuKi támogatása",
        message: "Támogatásod hozzájárul a HuKi fenntartásához és fejlesztéséhez.",
        buttonTitle: "Támogatom",
        accessibilityLabel: "Támogatás megnyitása",
        testTag: "",
        action: {}
    )
    .padding()
    .background(Color(.systemGroupedBackground))
}
