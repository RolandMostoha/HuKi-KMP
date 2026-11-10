import Shared
import SwiftUI

/// A single purchase isn't counted, the badge appears from the second one.
let minShownPurchaseCount: Int32 = 2

struct SupportBadgeView<Content: View>: View {
    let color: SwiftUI.Color
    let ringColor: SwiftUI.Color
    @ViewBuilder let content: () -> Content

    var body: some View {
        content()
            .frame(minWidth: 20, minHeight: 20)
            .background(color, in: Capsule())
            .padding(2)
            .background(ringColor, in: Capsule())
    }
}

struct SupportPurchaseCountBadgeView: View {
    let purchaseCount: Int32
    let color: SwiftUI.Color
    let ringColor: SwiftUI.Color

    private let strings = Strings()

    var body: some View {
        SupportBadgeView(color: color, ringColor: ringColor) {
            Text(strings.get(id: SharedRes.strings().support_purchase_count, args: [purchaseCount]))
                .font(.caption2.weight(.bold))
                .foregroundStyle(.white)
                .padding(.horizontal, 5)
        }
    }
}

#Preview {
    SupportPurchaseCountBadgeView(
        purchaseCount: 3,
        color: SupportAnimal.squirrel.color,
        ringColor: SupportAnimal.squirrel.containerColor
    )
    .padding()
    .background(SupportAnimal.squirrel.containerColor)
}
