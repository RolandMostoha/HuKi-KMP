import Shared
import SwiftUI

struct DestinationRowView: View {
    let strings: Strings
    let destination: Destination
    var rank: Int?
    var landscapeText: String?
    var distanceText: String?
    var onClick: () -> Void = {}

    private let primaryColor = SwiftUI.Color(SharedRes.colors().primary.getUIColor())

    private var subtitle: String {
        if let landscapeText {
            return "\(landscapeText) · \(destination.town)"
        }
        return destination.town
    }

    var body: some View {
        HStack(spacing: 14) {
            if let rank {
                Text("\(rank)")
                    .font(.system(size: 13, weight: .semibold))
                    .monospacedDigit()
                    .lineLimit(1)
                    .minimumScaleFactor(0.7)
                    .foregroundStyle(rank <= 3 ? primaryColor : SwiftUI.Color(.secondaryLabel))
                    .frame(width: 22, alignment: .center)
            }
            ZStack {
                Circle()
                    .fill(SwiftUI.Color(destination.type.colorRes.getUIColor()))
                    .frame(width: 40, height: 40)
                Image(uiImage: destination.type.iconRes.toUIImage()!)
                    .renderingMode(.template)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 22, height: 22)
                    .foregroundStyle(.white)
            }
            VStack(alignment: .leading, spacing: 2) {
                Text(destination.name)
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundStyle(.primary)
                    .lineLimit(1)
                Text(subtitle)
                    .font(.system(size: 13))
                    .foregroundStyle(SwiftUI.Color(.secondaryLabel))
                    .lineLimit(1)
            }
            Spacer(minLength: 8)
            if let distanceText {
                Text(distanceText)
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundStyle(primaryColor)
                    .lineLimit(1)
            }
        }
        .padding(.leading, 16)
        .padding(.trailing, 16)
        .padding(.vertical, 12)
        .contentShape(Rectangle())
        .onTapGesture { onClick() }
        .accessibilityElement(children: .combine)
        .accessibilityAddTraits(.isButton)
        .accessibilityIdentifier(TestTags.shared.DESTINATIONS_ITEM)
    }
}
