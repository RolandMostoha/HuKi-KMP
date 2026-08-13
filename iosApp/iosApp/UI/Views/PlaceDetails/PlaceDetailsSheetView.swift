import Shared
import SwiftUI

struct PlaceDetailsSheetView: View {
    let strings: Strings
    let placeDetails: PlaceDetails
    let onRoutePlanClick: () -> Void
    let onMapsNavigationClick: () -> Void
    let onDismissRequest: () -> Void
    var onHeightChange: (CGFloat) -> Void = { _ in }

    var body: some View {
        VStack(spacing: 20) {
            header
            VStack(spacing: 12) {
                PrimaryButton(
                    icon: .system("point.topright.arrow.triangle.backward.to.point.bottomleft.scurvepath.fill"),
                    title: strings.get(id: SharedRes.strings().place_details_route_plan),
                    action: onRoutePlanClick
                )
                .accessibilityIdentifier(TestTags.shared.PLACE_DETAILS_ROUTE_PLAN_BUTTON)
                SecondaryButton(
                    icon: SharedRes.images().ic_maps_navigation,
                    title: strings.get(id: SharedRes.strings().place_details_maps_navigation),
                    action: onMapsNavigationClick
                )
                .accessibilityIdentifier(TestTags.shared.PLACE_DETAILS_MAPS_NAVIGATION_BUTTON)
            }
            .padding(.horizontal, 24)
        }
        .padding(.top, 24)
        .padding(.bottom, 8)
        .fixedSize(horizontal: false, vertical: true)
        .onGeometryChange(for: CGFloat.self) { $0.size.height } action: { height in
            onHeightChange(height)
        }
        .frame(maxHeight: .infinity, alignment: .top)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(TestTags.shared.PLACE_DETAILS_SHEET)
    }

    private var header: some View {
        HStack(spacing: 14) {
            content
                .frame(maxWidth: .infinity, alignment: .leading)
                .transition(.opacity)
            Button(action: onDismissRequest) {
                Image(systemName: "xmark")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(.primary)
                    .padding(12)
                    .background(Circle().fill(Color(.systemGray5)))
            }
            .buttonStyle(.plain)
            .contentShape(Circle())
            .accessibilityLabel(strings.get(id: SharedRes.strings().a11y_close))
            .accessibilityIdentifier(TestTags.shared.PLACE_DETAILS_CLOSE_BUTTON)
        }
        .padding(.leading, 24)
        .padding(.trailing, 16)
        .animation(.smooth(duration: 0.3), value: placeDetails)
    }

    @ViewBuilder
    private var content: some View {
        switch onEnum(of: placeDetails) {
        case .loading:
            loadingContent
        case .unresolved(let unresolved):
            unresolvedContent(for: unresolved)
        case .placeLoaded(let placeLoaded):
            placeContent(for: placeLoaded.place)
        }
    }

    private var loadingContent: some View {
        HStack(spacing: 14) {
            ProgressView()
                .controlSize(.regular)
                .frame(width: 40, height: 40)
            Text(strings.get(id: SharedRes.strings().place_details_loading))
                .font(.system(size: 16))
                .foregroundStyle(Color(.secondaryLabel))
                .lineLimit(1)
        }
        .accessibilityIdentifier(TestTags.shared.PLACE_DETAILS_LOADING)
    }

    private func unresolvedContent(for unresolved: PlaceDetails.Unresolved) -> some View {
        headerContent(
            title: CoordinateFormatter.shared.formatCoordinates(location: unresolved.location),
            icon: OsmTypeMapperKt.toPlaceIconRes(osmType: nil).toUIImage()!,
            iconBackgroundColor: SharedRes.colors().colorPlaceCategoryFallback.getUIColor(),
            address: nil,
            distance: unresolved.distance
        )
    }

    private func placeContent(for place: Place) -> some View {
        headerContent(
            title: place.name,
            icon: iconImage(for: place),
            iconBackgroundColor: backgroundColor(for: place),
            address: place.address,
            distance: place.distance
        )
    }

    private func headerContent(
        title: String,
        icon: UIImage,
        iconBackgroundColor: UIColor,
        address: String?,
        distance: String?
    ) -> some View {
        HStack(spacing: 14) {
            VStack(spacing: 3) {
                ZStack {
                    Circle()
                        .fill(SwiftUI.Color(iconBackgroundColor))
                        .frame(width: 40, height: 40)
                    Image(uiImage: icon)
                        .renderingMode(.template)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 22, height: 22)
                        .foregroundStyle(.white)
                }
                if let distance, !distance.isEmpty {
                    Text(distance)
                        .font(.system(size: 12, weight: .medium))
                        .foregroundStyle(SwiftUI.Color(iconBackgroundColor.categoryTint()))
                        .lineLimit(1)
                        .padding(.top, 2)
                        .fixedSize()
                        .accessibilityIdentifier(TestTags.shared.PLACE_DETAILS_DISTANCE)
                }
            }
            VStack(alignment: .leading, spacing: 3) {
                Text(title)
                    .font(.system(size: 20, weight: .bold))
                    .foregroundStyle(.primary)
                    .lineLimit(1)
                    .minimumScaleFactor(13.0 / 20.0)
                    .accessibilityIdentifier(TestTags.shared.PLACE_DETAILS_TITLE)
                if let address, !address.isEmpty {
                    Text(address)
                        .font(.system(size: 14))
                        .foregroundStyle(Color(.secondaryLabel))
                        .lineLimit(2)
                        .accessibilityIdentifier(TestTags.shared.PLACE_DETAILS_ADDRESS)
                }
            }
        }
    }

    private func backgroundColor(for place: Place) -> UIColor {
        if let category = place.placeCategory {
            return category.categoryColorRes.getUIColor()
        }
        return SharedRes.colors().colorPlaceCategoryFallback.getUIColor()
    }

    private func iconImage(for place: Place) -> UIImage {
        if let category = place.placeCategory {
            return category.iconRes.toUIImage()!
        }
        return OsmTypeMapperKt.toPlaceIconRes(osmType: place.osmType).toUIImage()!
    }
}

#Preview("Loading") {
    PlaceDetailsSheetView(
        strings: Strings(),
        placeDetails: PlaceDetails.Loading(
            location: Location(latitude: 47.7181, longitude: 18.8948, altitude: nil)
        ),
        onRoutePlanClick: {},
        onMapsNavigationClick: {},
        onDismissRequest: {}
    )
}

#Preview("Unresolved") {
    PlaceDetailsSheetView(
        strings: Strings(),
        placeDetails: PlaceDetails.Unresolved(
            location: Location(latitude: 47.7181, longitude: 18.8948, altitude: nil),
            distance: "12.4 km"
        ),
        onRoutePlanClick: {},
        onMapsNavigationClick: {},
        onDismissRequest: {}
    )
}

#Preview("Loaded") {
    PlaceDetailsSheetView(
        strings: Strings(),
        placeDetails: PlaceDetails.PlaceLoaded(
            place: Place(
                osmId: "1",
                location: Location(latitude: 47.7181, longitude: 18.8948, altitude: nil),
                name: "Dobogókő",
                placeSource: .longTapOnMap,
                address: "Pilisszentkereszt, Pest, Hungary",
                placeCategory: .peak,
                osmType: .node,
                distance: "12.4 km",
                boundingBox: nil
            )
        ),
        onRoutePlanClick: {},
        onMapsNavigationClick: {},
        onDismissRequest: {}
    )
}
