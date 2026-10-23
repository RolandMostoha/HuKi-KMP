@preconcurrency @_spi(Experimental) import MapboxMaps
import Shared
import SwiftUI

struct PlaceMarkersMapContent: MapContent {
    let mapUiState: MapUiState
    let colorScheme: ColorScheme
    let onRouteStopClicked: (PlaceDetails) -> Void

    private var routeStops: [PlaceDetails] {
        // The shown place gets its own, non-tappable pin
        mapUiState.routeStops.filter { $0.location != mapUiState.placeDetails?.location }
    }

    var body: some MapContent {
        ForEvery(routeStops, id: \.location.id) { stop in
            placeMarker(at: stop.location, accessibilityName: stop.toDisplayName()) { onRouteStopClicked(stop) }
        }
        if let placeDetails = mapUiState.placeDetails {
            placeMarker(at: placeDetails.location, accessibilityName: nil, onTap: nil)
        }
    }

    private func placeMarker(
        at location: Shared.Location,
        accessibilityName: String?,
        onTap: (() -> Void)?
    ) -> MapViewAnnotation {
        MapViewAnnotation(coordinate: location.coordinate) {
            PlaceMarkerView(
                baseLayer: mapUiState.baseLayer,
                colorScheme: colorScheme,
                accessibilityName: accessibilityName,
                onTap: onTap
            )
        }
        .variableAnchors([ViewAnnotationAnchorConfig(anchor: .bottom)])
        .allowOverlap(true)
        .allowOverlapWithPuck(true)
        .priority(Int(MapConstants.shared.PLACE_MARKER_PRIORITY))
    }
}

private struct PlaceMarkerView: View {
    let baseLayer: Shared.BaseLayer
    let colorScheme: ColorScheme
    let accessibilityName: String?
    let onTap: (() -> Void)?

    private let strings = Strings()

    var body: some View {
        let image = MarkerIcons.shared
            .placeSymbol(isDarkMode: colorScheme == .dark, baseLayer: baseLayer)
            .appearanceImage(for: colorScheme)
        let scale = CGFloat(SharedDimens.shared.PLACE_MARKER_SCALE)
        Image(uiImage: image)
            .resizable()
            .frame(width: image.size.width * scale, height: image.size.height * scale)
            .contentShape(Rectangle())
            .onTapGesture { onTap?() }
            .allowsHitTesting(onTap != nil)
            .accessibilityHidden(accessibilityName == nil)
            .accessibilityLabel(
                accessibilityName.map { strings.get(id: SharedRes.strings().map_a11y_route_stop, args: [$0]) } ?? ""
            )
            .accessibilityAddTraits(.isButton)
            .accessibilityIdentifier(TestTags.shared.MAP_ROUTE_STOP_MARKER)
    }
}
