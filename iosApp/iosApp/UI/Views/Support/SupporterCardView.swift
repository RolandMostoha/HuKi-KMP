import Shared
import SwiftUI

struct SupporterCardView: View {
    let animal: SupportAnimal
    let celebrate: Bool
    let choices: [SupporterChoice]
    let onChoose: (SupportAnimal) -> Void

    @Environment(\.colorScheme) private var colorScheme
    @State private var isIconShown: Bool
    @State private var isTextShown: Bool
    @State private var isSilhouetteShown: Bool
    @State private var burstProgress: CGFloat = 0

    private let strings = Strings()
    private let minChooserChoices = 2

    init(
        animal: SupportAnimal,
        celebrate: Bool = false,
        choices: [SupporterChoice] = [],
        onChoose: @escaping (SupportAnimal) -> Void = { _ in }
    ) {
        self.animal = animal
        self.celebrate = celebrate
        self.choices = choices
        self.onChoose = onChoose
        _isIconShown = State(initialValue: !celebrate)
        _isTextShown = State(initialValue: !celebrate)
        _isSilhouetteShown = State(initialValue: !celebrate)
    }

    var body: some View {
        let animalName = strings.get(id: animal.nameRes)
        VStack(alignment: .leading, spacing: 0) {
            ZStack {
                HeartBurstView(progress: burstProgress, color: animal.color)
                    .accessibilityHidden(true)
                animal.iconImage
                    .resizable()
                    .scaledToFit()
                    .foregroundStyle(animal.color)
                    .frame(width: 48, height: 48)
                    .id(animal)
                    .transition(.scale(scale: 0.5).combined(with: .opacity))
                    .accessibilityHidden(true)
                    .frame(width: 72, height: 72)
                    .background(
                        SwiftUI.Color.white.opacity(colorScheme == .dark ? 0.09 : 1),
                        in: RoundedRectangle(cornerRadius: 22, style: .continuous)
                    )
                    .overlay(alignment: .bottomTrailing) { purchaseCountBadge }
                    .animation(.spring(duration: 0.3), value: purchaseCount)
                    .scaleEffect(isIconShown ? 1 : 0.01)
                    .rotationEffect(.degrees(isIconShown ? 0 : -20))
            }
            Text(strings.get(id: SharedRes.strings().support_supporter_label, args: [animalName]).uppercased())
                .font(.footnote.weight(.semibold))
                .foregroundStyle(animal.color)
                .contentTransition(.opacity)
                .padding(.top, 14)
                .riseIn(isShown: isTextShown, delay: 0.2)
            Text(strings.get(id: SharedRes.strings().support_supporter_title, args: [animalName]))
                .font(.title2.weight(.bold))
                .foregroundStyle(.primary)
                .contentTransition(.opacity)
                .fixedSize(horizontal: false, vertical: true)
                .padding(.top, 4)
                .accessibilityAddTraits(.isHeader)
                .riseIn(isShown: isTextShown, delay: 0.29)
            Text(strings.get(id: SharedRes.strings().support_supporter_message))
                .font(.subheadline)
                .lineSpacing(4)
                .foregroundStyle(.secondary)
                .fixedSize(horizontal: false, vertical: true)
                .padding(.top, 8)
                .riseIn(isShown: isTextShown, delay: 0.38)
            if choices.count >= minChooserChoices {
                SupportAnimalChooserView(choices: choices, selectedAnimal: animal, onChoose: onChoose)
                    .padding(.top, 18)
                    .riseIn(isShown: isTextShown, delay: 0.47)
            }
        }
        .padding(.vertical, 22)
        .padding(.horizontal, 20)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background { silhouette }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(TestTags.shared.SUPPORT_SUPPORTER_CARD)
        .sensoryFeedback(.success, trigger: burstProgress) { _, newValue in newValue > 0 }
        .task {
            guard celebrate else { return }
            withAnimation(.spring(response: 0.55, dampingFraction: 0.5)) {
                isIconShown = true
            }
            isTextShown = true
            withAnimation(.easeOut(duration: 0.7).delay(0.1)) {
                isSilhouetteShown = true
            }
            try? await Task.sleep(for: .milliseconds(120))
            withAnimation(.easeOut(duration: 0.9)) {
                burstProgress = 1
            }
        }
    }

    private var purchaseCount: Int32 {
        choices.first { $0.animal == animal }?.purchaseCount ?? 1
    }

    @ViewBuilder
    private var purchaseCountBadge: some View {
        if purchaseCount >= minShownPurchaseCount {
            SupportPurchaseCountBadgeView(
                purchaseCount: purchaseCount,
                color: animal.color,
                ringColor: animal.containerColor
            )
            .offset(x: 6, y: 6)
            .transition(.scale.combined(with: .opacity))
            .accessibilityElement(children: .ignore)
            .accessibilityLabel(
                strings.get(id: SharedRes.strings().support_a11y_purchase_count, args: [purchaseCount])
            )
            .accessibilityIdentifier(TestTags.shared.SUPPORT_SUPPORTER_PURCHASE_COUNT)
        }
    }

    private var silhouette: some View {
        let shape = RoundedRectangle(cornerRadius: 26, style: .continuous)
        return shape
            .fill(animal.containerColor)
            .overlay(alignment: .topTrailing) {
                animal.iconImage
                    .resizable()
                    .scaledToFit()
                    .foregroundStyle(animal.color.opacity(0.1))
                    // Mirrored to face into the card
                    .scaleEffect(x: animal.isIconFacingRight ? -1 : 1)
                    .frame(width: 200, height: 200, alignment: .top)
                    .id(animal)
                    .transition(.offset(x: 50).combined(with: .opacity))
                    .offset(x: isSilhouetteShown ? 48 : 88)
                    .opacity(isSilhouetteShown ? 1 : 0)
                    .accessibilityHidden(true)
            }
            .clipShape(shape)
    }
}

private struct HeartBurstView: View, Animatable {
    var progress: CGFloat
    let color: SwiftUI.Color

    private let heartCount = 9
    private let distance: CGFloat = 64

    var animatableData: CGFloat {
        get { progress }
        set { progress = newValue }
    }

    var body: some View {
        ZStack {
            ForEach(0..<heartCount, id: \.self) { index in
                let angle = 2 * .pi * Double(index) / Double(heartCount) - .pi / 2
                let radius = distance * progress * (index.isMultiple(of: 2) ? 1 : 0.75)
                Image(systemName: "heart.fill")
                    .font(.system(size: index.isMultiple(of: 2) ? 13 : 9))
                    .foregroundStyle(color)
                    .scaleEffect(min(progress / 0.3, 1))
                    .offset(x: cos(angle) * radius, y: sin(angle) * radius)
            }
        }
        .opacity(progress > 0 && progress < 1 ? 1 - progress : 0)
    }
}

private extension View {
    func riseIn(isShown: Bool, delay: Double) -> some View {
        opacity(isShown ? 1 : 0)
            .offset(y: isShown ? 0 : 12)
            .animation(.easeOut(duration: 0.4).delay(delay), value: isShown)
    }
}

#Preview {
    SupporterCardView(animal: .owl, celebrate: true)
        .padding()
}

#Preview("Purchase count") {
    SupporterCardView(
        animal: .squirrel,
        choices: [SupporterChoice(animal: .squirrel, purchaseCount: 3)]
    )
    .padding()
}

#Preview("Chooser") {
    SupporterCardView(
        animal: .boar,
        choices: [
            SupporterChoice(animal: .squirrel, purchaseCount: 2),
            SupporterChoice(animal: .owl, purchaseCount: 1),
            SupporterChoice(animal: .boar, purchaseCount: 1)
        ]
    )
    .padding()
}
