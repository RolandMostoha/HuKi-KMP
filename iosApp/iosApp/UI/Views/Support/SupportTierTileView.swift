import Shared
import SwiftUI

struct SupportTierTileView: View {
    let animal: SupportAnimal
    let name: String
    let price: String?
    let billingPeriod: String?
    var isActive = false
    var scheduledDate: String?
    var isPurchasing = false
    var isEnabled = true
    let action: () -> Void

    @Environment(\.colorScheme) private var colorScheme

    private let strings = Strings()

    var body: some View {
        Button(action: action) {
            VStack(spacing: 0) {
                animal.iconImage
                    .resizable()
                    .scaledToFit()
                    .foregroundStyle(animal.color)
                    .frame(width: 58, height: 58)
                    .padding(.top, 10)
                Text(name)
                    .font(.subheadline.weight(.bold))
                    .foregroundStyle(animal.color)
                    .lineLimit(1)
                    .minimumScaleFactor(0.8)
                    .padding(.top, 10)
                VStack(spacing: 1) {
                    pill
                        .font(.caption.weight(.semibold))
                        .monospacedDigit()
                    if let caption {
                        Text(caption)
                            .font(.caption2.weight(.medium))
                            .foregroundStyle(.secondary)
                    }
                }
                .lineLimit(1)
                .minimumScaleFactor(0.8)
                .padding(.horizontal, 10)
                .padding(.vertical, caption == nil ? 0 : 5)
                .frame(minHeight: 26)
                .background(
                    SwiftUI.Color.white.opacity(colorScheme == .dark ? 0.09 : 0.8),
                    in: caption == nil ? AnyShape(.capsule) : AnyShape(.rect(cornerRadius: 14, style: .continuous))
                )
                .padding(.top, 8)
            }
            .padding(.top, 10)
            .padding(.horizontal, 8)
            .padding(.bottom, 14)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(animal.containerColor, in: RoundedRectangle(cornerRadius: 24, style: .continuous))
            .contentShape(RoundedRectangle(cornerRadius: 24, style: .continuous))
        }
        .buttonStyle(SupportTierButtonStyle())
        .disabled((price == nil && !isActive && scheduledDate == nil) || !isEnabled)
        .accessibilityElement(children: .combine)
        .accessibilityValue(accessibilityValue)
        .accessibilityIdentifier(TestTags.shared.supportTier(animal: animal.name))
    }

    @ViewBuilder
    private var pill: some View {
        if isActive {
            Label(strings.get(id: SharedRes.strings().support_tier_active), systemImage: "checkmark.circle.fill")
                .labelStyle(.titleAndIcon)
                .foregroundStyle(SwiftUI.Color(SharedRes.colors().success.getUIColor()))
        } else if let scheduledDate {
            Label(scheduledDate, systemImage: "clock")
            .labelStyle(.titleAndIcon)
            .foregroundStyle(animal.color)
        } else if isPurchasing {
            ProgressView()
                .controlSize(.small)
                .tint(animal.color)
                .padding(.horizontal, 10)
        } else {
            Text(price ?? "–").foregroundStyle(.primary)
        }
    }

    private var accessibilityValue: String {
        if isActive {
            return strings.get(id: SharedRes.strings().support_a11y_tier_active)
        }
        if scheduledDate != nil {
            return strings.get(id: SharedRes.strings().support_a11y_tier_scheduled)
        }
        return ""
    }

    private var caption: String? {
        if isActive || isPurchasing {
            return nil
        }
        if scheduledDate != nil {
            return strings.get(id: SharedRes.strings().support_tier_scheduled)
        }
        return price == nil ? nil : billingPeriod
    }
}

private struct SupportTierButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.96 : 1)
            .animation(.easeOut(duration: 0.12), value: configuration.isPressed)
    }
}

#Preview {
    HStack(spacing: 10) {
        SupportTierTileView(
            animal: .squirrel,
            name: "Mókus",
            price: "499 Ft",
            billingPeriod: nil,
            action: {}
        )
        SupportTierTileView(
            animal: .boar,
            name: "Vaddisznó",
            price: "499 Ft",
            billingPeriod: "havonta",
            isActive: true,
            action: {}
        )
        SupportTierTileView(
            animal: .deer,
            name: "Szarvas",
            price: "249 Ft",
            billingPeriod: "havonta",
            scheduledDate: "nov. 3.",
            action: {}
        )
        SupportTierTileView(
            animal: .bear,
            name: "Medve",
            price: "999 Ft",
            billingPeriod: "havonta",
            isPurchasing: true,
            action: {}
        )
    }
    .fixedSize(horizontal: false, vertical: true)
    .padding()
}
