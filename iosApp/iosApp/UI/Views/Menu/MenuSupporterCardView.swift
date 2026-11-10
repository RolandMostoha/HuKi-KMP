import Shared
import SwiftUI

struct MenuSupporterCardView: View {
    let animal: SupportAnimal
    let action: () -> Void

    private let strings = Strings()
    private let shape = RoundedRectangle(cornerRadius: 26, style: .continuous)

    private var supporterLabel: String {
        strings.get(id: SharedRes.strings().support_supporter_label, args: [strings.get(id: animal.nameRes)])
    }

    var body: some View {
        Button(action: action) {
            HStack(spacing: 14) {
                animal.iconImage
                    .resizable()
                    .scaledToFit()
                    .foregroundStyle(animal.color)
                    .frame(width: 28, height: 28)
                    .frame(width: 48, height: 48)
                    .background(
                        SwiftUI.Color(.systemBackground).opacity(0.6),
                        in: RoundedRectangle(cornerRadius: 14, style: .continuous)
                    )
                VStack(alignment: .leading, spacing: 2) {
                    Text(supporterLabel.uppercased())
                        .font(.caption2.weight(.semibold))
                        .kerning(0.4)
                        .foregroundStyle(.secondary)
                    Text(strings.get(id: SharedRes.strings().menu_supporter_title))
                        .font(.headline)
                        .foregroundStyle(.primary)
                    Text(strings.get(id: SharedRes.strings().menu_supporter_message))
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }
                .multilineTextAlignment(.leading)
                .fixedSize(horizontal: false, vertical: true)
                Spacer(minLength: 0)
                Image(systemName: "chevron.forward")
                    .font(.footnote.weight(.semibold))
                    .foregroundStyle(animal.color)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background { background }
            .clipShape(shape)
            .contentShape(shape)
        }
        .buttonStyle(PressFeedbackButtonStyle())
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(strings.get(id: SharedRes.strings().menu_a11y_open_support))
        .accessibilityValue(supporterLabel)
        .accessibilityAddTraits(.isButton)
        .accessibilityIdentifier(TestTags.shared.MENU_ROW_SUPPORT)
    }

    // The overlay keeps the oversized silhouette from growing the card
    private var background: some View {
        animal.containerColor
            .overlay(alignment: .trailing) {
                animal.iconImage
                    .resizable()
                    .scaledToFit()
                    .foregroundStyle(animal.color.opacity(0.1))
                    .scaleEffect(x: animal.isIconFacingRight ? -1 : 1)
                    .frame(width: 110, height: 110)
                    .offset(x: 24, y: 12)
            }
    }
}

#Preview {
    VStack(spacing: 16) {
        MenuSupporterCardView(animal: .owl, action: {})
        MenuSupporterCardView(animal: .squirrel, action: {})
    }
    .padding()
    .background(Color(.systemGroupedBackground))
}
