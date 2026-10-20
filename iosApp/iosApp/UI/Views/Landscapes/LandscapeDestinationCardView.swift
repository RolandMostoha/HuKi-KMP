import Shared
import SwiftUI

struct LandscapeDestinationCardView: View {
    let strings: Strings
    let destination: Destination
    let isSelected: Bool
    let onClick: () -> Void

    private let surfaceColor = Color(SharedRes.colors().mapStroke.getUIColor())

    private var categoryColor: SwiftUI.Color {
        SwiftUI.Color(destination.type.colorRes.getUIColor())
    }

    var body: some View {
        Button(action: onClick) {
            VStack(alignment: .leading, spacing: 4) {
                Image(uiImage: destination.type.iconRes.toUIImage()!)
                    .renderingMode(.template)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 16, height: 16)
                    .foregroundStyle(isSelected ? categoryColor : surfaceColor)
                    .frame(width: Dimens.oktStampIconSize, height: Dimens.oktStampIconSize)
                    .background(Circle().fill(isSelected ? surfaceColor : categoryColor))
                    .accessibilityHidden(true)
                Spacer(minLength: 4)
                Text(destination.name)
                    .font(.subheadline.weight(.semibold))
                    .foregroundStyle(isSelected ? AnyShapeStyle(.white) : AnyShapeStyle(.primary))
                    .lineLimit(2)
                    .minimumScaleFactor(0.8)
                HStack(spacing: 3) {
                    Image(systemName: "mappin.and.ellipse")
                        .font(.system(size: 10, weight: .medium))
                    Text(destination.town)
                        .font(.caption)
                        .lineLimit(1)
                }
                .foregroundStyle(isSelected ? AnyShapeStyle(.white.opacity(0.85)) : AnyShapeStyle(.secondary))
            }
            .padding(10)
            .frame(
                width: Dimens.landscapeDestinationCardWidth,
                height: Dimens.landscapeDestinationCardHeight,
                alignment: .topLeading
            )
            .background(
                isSelected ? categoryColor : Color(.systemBackground),
                in: RoundedRectangle(cornerRadius: 16, style: .continuous)
            )
            .animation(.easeInOut(duration: 0.15), value: isSelected)
        }
        .buttonStyle(PressFeedbackButtonStyle())
        .accessibilityElement(children: .combine)
        .accessibilityAddTraits(isSelected ? .isSelected : [])
        .accessibilityIdentifier(TestTags.shared.LANDSCAPES_DESTINATION_CARD)
    }
}

#Preview {
    let destination = Destination(
        osmId: "1",
        relId: nil,
        name: "Kékestető",
        town: "Mátraszentimre",
        type: .peak,
        location: Location(latitude: 47.8721, longitude: 20.0102, altitude: nil),
        description: SharedRes.strings().destinations_type_peak,
        popularity: 10,
        challenges: [DestinationChallenge.highestPeaks]
    )
    HStack {
        LandscapeDestinationCardView(strings: Strings(), destination: destination, isSelected: true, onClick: {})
        LandscapeDestinationCardView(strings: Strings(), destination: destination, isSelected: false, onClick: {})
    }
    .padding()
    .background(Color(SharedRes.colors().primary.getUIColor()).opacity(0.15))
}
