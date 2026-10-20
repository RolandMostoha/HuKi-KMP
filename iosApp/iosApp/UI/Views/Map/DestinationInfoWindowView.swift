import Shared
import SwiftUI

struct DestinationInfoWindowView: View {
    let strings: Strings
    let destination: Destination
    let onPlaceDetailsClick: () -> Void

    private static let descriptionLineLimit = 4

    private var chipTint: SwiftUI.Color {
        SwiftUI.Color(destination.type.colorRes.getUIColor().categoryTint())
    }

    var body: some View {
        let shape = InfoWindowShape(
            cornerRadius: Dimens.infoWindowCornerRadius,
            tailWidth: Dimens.infoWindowTailWidth,
            tailHeight: Dimens.infoWindowTailHeight
        )
        VStack(alignment: .leading, spacing: 4) {
            HStack(alignment: .top, spacing: 10) {
                VStack(alignment: .leading, spacing: 4) {
                    DestinationTypeChipView(
                        strings: strings,
                        type: destination.type,
                        font: .caption2.weight(.semibold),
                        horizontalPadding: 8,
                        verticalPadding: 3,
                        contentColor: chipTint,
                        containerColor: chipTint.opacity(0.15)
                    )
                    .padding(.bottom, 2)
                    Text(destination.name)
                        .font(.headline.weight(.bold))
                        .foregroundStyle(.primary)
                        .lineLimit(2)
                    Label(destination.town, systemImage: "mappin.and.ellipse")
                        .font(.caption.weight(.medium))
                        .foregroundStyle(.secondary)
                        .lineLimit(1)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                InfoWindowPlaceDetailsButton(strings: strings, onClick: onPlaceDetailsClick)
                    .accessibilityIdentifier(TestTags.shared.LANDSCAPES_INFO_WINDOW_PLACE_DETAILS_BUTTON)
            }
            Text(strings.get(id: destination.description_))
                .font(.footnote)
                .foregroundStyle(.secondary)
                .lineLimit(Self.descriptionLineLimit)
                .fixedSize(horizontal: false, vertical: true)
                .padding(.top, 2)
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 12)
        .padding(.bottom, Dimens.infoWindowTailHeight)
        .frame(width: Dimens.destinationInfoWindowWidth)
        .fixedSize(horizontal: false, vertical: true)
        .background(
            shape
                .fill(SwiftUI.Color(.systemBackground))
                .shadow(color: .black.opacity(0.2), radius: 6, y: 2)
        )
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(TestTags.shared.LANDSCAPES_INFO_WINDOW)
    }
}

#Preview {
    DestinationInfoWindowView(
        strings: Strings(),
        destination: Destination(
            id: "1",
            name: "Fátyol-vízesés",
            town: "Szilvásvárad",
            type: .river,
            location: Location(latitude: 48.0855, longitude: 20.3889, altitude: nil),
            description: SharedRes.strings().destinations_description,
            popularity: 8,
            challenges: []
        ),
        onPlaceDetailsClick: {}
    )
    .padding()
}
