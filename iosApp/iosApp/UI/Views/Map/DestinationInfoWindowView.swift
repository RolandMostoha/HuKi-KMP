import Shared
import SwiftUI

struct DestinationInfoWindowView: View {
    let strings: Strings
    let destination: Destination
    let onPlaceDetailsClick: () -> Void
    let onRouteClick: () -> Void
    let onWebsiteClick: () -> Void

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
            if destination.routeGpxFile != nil || destination.website != nil {
                VStack(spacing: 6) {
                    if destination.routeGpxFile != nil {
                        SecondaryButton(
                            icon: .system("point.topright.arrow.triangle.backward.to.point.bottomleft.scurvepath.fill"),
                            title: strings.get(id: SharedRes.strings().destinations_action_open_route),
                            size: .small,
                            action: onRouteClick
                        )
                        .accessibilityIdentifier(TestTags.shared.LANDSCAPES_INFO_WINDOW_ROUTE_BUTTON)
                    }
                    if destination.website != nil {
                        SecondaryButton(
                            icon: .system("globe"),
                            title: strings.get(id: SharedRes.strings().destinations_action_open_website),
                            size: .small,
                            action: onWebsiteClick
                        )
                        .accessibilityIdentifier(TestTags.shared.LANDSCAPES_INFO_WINDOW_WEBSITE_BUTTON)
                    }
                }
                .padding(.top, 10)
            }
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
            description: SharedRes.strings().bukk_fatyol_vizeses,
            popularity: 8,
            challenges: [],
            routeGpxFile: nil,
            website: nil
        ),
        onPlaceDetailsClick: {},
        onRouteClick: {},
        onWebsiteClick: {}
    )
    .padding()
}

#Preview("Route") {
    DestinationInfoWindowView(
        strings: Strings(),
        destination: Destination(
            id: "1",
            name: "Jági tanösvény",
            town: "Pilisszentiván",
            type: .educationalTrail,
            location: Location(latitude: 47.6120670, longitude: 18.8845000, altitude: nil),
            description: SharedRes.strings().budapest_jagi_tanosveny,
            popularity: 4,
            challenges: [],
            routeGpxFile: SharedRes.files().edut_jagi_tanosveny_gpx,
            website: nil
        ),
        onPlaceDetailsClick: {},
        onRouteClick: {},
        onWebsiteClick: {}
    )
    .padding()
}

#Preview("Website") {
    DestinationInfoWindowView(
        strings: Strings(),
        destination: Destination(
            id: "1",
            name: "Zselici Csillagpark",
            town: "Zselickisfalud",
            type: .observatory,
            location: Location(latitude: 46.2363960, longitude: 17.7651457, altitude: nil),
            description: SharedRes.strings().zselic_csillagpark,
            popularity: 7,
            challenges: [],
            routeGpxFile: nil,
            website: "https://zselicicsillagpark.hu"
        ),
        onPlaceDetailsClick: {},
        onRouteClick: {},
        onWebsiteClick: {}
    )
    .padding()
}
