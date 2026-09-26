import Shared
import SwiftUI

struct HikeRecommendationCardView: View {
    let strings: Strings
    let recommendation: HikeRecommendation
    var backgroundColor = Color(.systemGray6)
    var titleFont: Font = .caption2.weight(.semibold)
    let onClick: () -> Void

    static let horizontalPadding: CGFloat = 2

    var body: some View {
        let name = strings.get(id: recommendation.title)
        Button(
            action: onClick,
            label: {
                VStack(spacing: 8) {
                    Image(uiImage: recommendation.iconRes.toUIImage()!)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 40, height: 40)
                        .clipShape(Circle())
                    Text(name)
                        .font(titleFont)
                        .foregroundStyle(.primary)
                        .lineLimit(1)
                        .minimumScaleFactor(0.6)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
                .padding(.horizontal, Self.horizontalPadding)
                .background(backgroundColor, in: RoundedRectangle(cornerRadius: 14, style: .continuous))
            }
        )
        .buttonStyle(.plain)
        .accessibilityLabel(strings.get(id: SharedRes.strings().gpx_guide_a11y_open_collection, args: [name]))
    }
}

#Preview {
    HikeRecommendationCardView(
        strings: Strings(),
        recommendation: .kirandulastippek,
        onClick: {}
    )
    .frame(width: 120)
}
