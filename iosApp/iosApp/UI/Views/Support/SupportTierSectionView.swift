import Shared
import SwiftUI

struct SupportTierSectionView: View {
    let tiers: [SupportTier]
    let title: String
    let note: String
    let purchasingAnimal: SupportAnimal?
    let onTierClick: (SupportAnimal) -> Void

    private let strings = Strings()

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            SectionHeaderView(title: title)
                .padding(.horizontal, -10)
                .padding(.top, 28)
                .padding(.bottom, 10)
                .accessibilityAddTraits(.isHeader)
            HStack(spacing: 10) {
                ForEach(tiers, id: \.animal) { tier in
                    SupportTierTileView(
                        animal: tier.animal,
                        name: strings.get(id: tier.animal.nameRes),
                        price: tier.price?.formatted,
                        billingPeriod: tier.animal.type == .recurring
                            ? strings.get(id: SharedRes.strings().support_price_per_month)
                            : nil,
                        isActive: tier.isActive,
                        scheduledDate: tier.scheduledStartTime?.date
                            .formatted(.dateTime.month(.abbreviated).day()),
                        isPurchasing: tier.animal == purchasingAnimal,
                        isEnabled: purchasingAnimal == nil,
                        action: { onTierClick(tier.animal) }
                    )
                }
            }
            .fixedSize(horizontal: false, vertical: true)
            Text(note)
                .font(.caption)
                .foregroundStyle(.secondary)
                .padding(.horizontal, 6)
                .padding(.top, 10)
        }
    }
}

#Preview {
    SupportTierSectionView(
        tiers: [
            SupportTier(animal: .deer, price: .huf(249_000_000), isActive: false, scheduledStartTime: nil),
            SupportTier(animal: .boar, price: .huf(499_000_000), isActive: true, scheduledStartTime: nil),
            SupportTier(animal: .bear, price: .huf(999_000_000), isActive: false, scheduledStartTime: nil)
        ],
        title: "Rendszeres támogatás",
        note: "Havonta megújul.",
        purchasingAnimal: nil,
        onTierClick: { _ in }
    )
    .padding()
}

private extension ProductPrice {
    static func huf(_ amountMicros: Int64) -> ProductPrice {
        ProductPrice(amountMicros: amountMicros, currencyCode: "HUF")
    }
}
