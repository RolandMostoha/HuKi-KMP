import Shared
import SwiftUI

struct SupportAnimalChooserView: View {
    let choices: [SupporterChoice]
    let selectedAnimal: SupportAnimal
    let onChoose: (SupportAnimal) -> Void

    @Environment(\.colorScheme) private var colorScheme

    private let strings = Strings()

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(strings.get(id: SharedRes.strings().support_chooser_title))
                .font(.subheadline.weight(.bold))
                .foregroundStyle(.primary)
                .padding(.horizontal, 16)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(alignment: .top, spacing: 6) {
                    ForEach(choices, id: \.animal) { choice in
                        SupportAnimalChoiceView(
                            choice: choice,
                            isSelected: choice.animal == selectedAnimal,
                            badgeRingColor: badgeRingColor,
                            onTap: { onChoose(choice.animal) }
                        )
                    }
                }
                .padding(.vertical, 3)
            }
            .contentMargins(.horizontal, 16, for: .scrollContent)
            .padding(.top, 12)
        }
        .padding(.vertical, 16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(panelColor, in: RoundedRectangle(cornerRadius: 20, style: .continuous))
        .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
        .accessibilityIdentifier(TestTags.shared.SUPPORT_ANIMAL_CHOOSER)
    }

    private var panelColor: SwiftUI.Color {
        SwiftUI.Color.white.opacity(colorScheme == .dark ? 0.06 : 0.6)
    }

    private var badgeRingColor: SwiftUI.Color {
        selectedAnimal.containerColor.mix(with: .white, by: colorScheme == .dark ? 0.06 : 0.6)
    }
}

private struct SupportAnimalChoiceView: View {
    let choice: SupporterChoice
    let isSelected: Bool
    let badgeRingColor: SwiftUI.Color
    let onTap: () -> Void

    private let strings = Strings()

    var body: some View {
        let name = strings.get(id: choice.animal.nameRes)
        let isCountShown = choice.purchaseCount >= minShownPurchaseCount
        Button(action: onTap) {
            VStack(spacing: 6) {
                ZStack {
                    Circle()
                        .strokeBorder(isSelected ? choice.animal.color : .clear, lineWidth: 2.5)
                    choice.animal.iconImage
                        .resizable()
                        .scaledToFit()
                        .foregroundStyle(choice.animal.color)
                        .frame(width: 32, height: 32)
                        .frame(width: 55, height: 55)
                        .background(choice.animal.containerColor, in: Circle())
                }
                .frame(width: 64, height: 64)
                .overlay(alignment: .bottomLeading) {
                    if isCountShown {
                        SupportPurchaseCountBadgeView(
                            purchaseCount: choice.purchaseCount,
                            color: choice.animal.color,
                            ringColor: badgeRingColor
                        )
                    }
                }
                .overlay(alignment: .bottomTrailing) {
                    if isSelected {
                        SupportBadgeView(color: choice.animal.color, ringColor: badgeRingColor) {
                            Image(systemName: "checkmark")
                                .font(.system(size: 10, weight: .bold))
                                .foregroundStyle(.white)
                        }
                        .transition(.scale.combined(with: .opacity))
                    }
                }
                Text(name)
                    .font(.caption.weight(isSelected ? .bold : .medium))
                    .foregroundStyle(isSelected ? .primary : .secondary)
                    .lineLimit(1)
            }
            .frame(width: 76)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .animation(.spring(duration: 0.3), value: isSelected)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(name)
        .accessibilityValue(
            isCountShown
                ? strings.get(id: SharedRes.strings().support_a11y_purchase_count, args: [choice.purchaseCount])
                : ""
        )
        .accessibilityAddTraits(isSelected ? [.isButton, .isSelected] : .isButton)
        .accessibilityIdentifier(TestTags.shared.supportAnimalChoice(animal: choice.animal.name))
    }
}

#Preview {
    SupportAnimalChooserView(
        choices: [
            SupporterChoice(animal: .squirrel, purchaseCount: 3),
            SupporterChoice(animal: .owl, purchaseCount: 1),
            SupporterChoice(animal: .boar, purchaseCount: 1)
        ],
        selectedAnimal: .boar,
        onChoose: { _ in }
    )
    .padding()
    .background(SupportAnimal.boar.containerColor)
}
