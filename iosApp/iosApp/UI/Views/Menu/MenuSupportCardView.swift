import Shared
import SwiftUI

struct MenuSupportCardView: View {
    let title: String
    let message: String
    let accessibilityLabel: String
    let testTag: String
    let action: () -> Void

    private let cardColor = SwiftUI.Color(.systemBackground)
    private let shape = RoundedRectangle(cornerRadius: 26, style: .continuous)

    var body: some View {
        Button(action: action) {
            HStack(spacing: 14) {
                VStack(alignment: .leading, spacing: 0) {
                    // Pulls the invisible card-colored ring out so the circles line up with the text
                    SupportAnimalBadgesView(ringColor: cardColor, size: 32)
                        .padding(.leading, -3)
                    Text(title)
                        .font(.headline)
                        .foregroundStyle(.primary)
                        .padding(.top, 8)
                    Text(message)
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                        .padding(.top, 2)
                }
                .multilineTextAlignment(.leading)
                .fixedSize(horizontal: false, vertical: true)
                Spacer(minLength: 0)
                Image(systemName: "chevron.forward")
                    .font(.footnote.weight(.semibold))
                    .foregroundStyle(SwiftUI.Color(SharedRes.colors().supportRed.getUIColor()))
            }
            .padding(16)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(cardColor, in: shape)
            .contentShape(shape)
        }
        .buttonStyle(PressFeedbackButtonStyle())
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(accessibilityLabel)
        .accessibilityHint(message)
        .accessibilityAddTraits(.isButton)
        .accessibilityIdentifier(testTag)
    }
}

#Preview {
    MenuSupportCardView(
        title: "HuKi támogatása",
        message: "Segíti a HuKi fejlesztését és fenntartását.",
        accessibilityLabel: "Támogatás megnyitása",
        testTag: "",
        action: {}
    )
    .padding()
    .background(Color(.systemGroupedBackground))
}
