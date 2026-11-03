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
            placeMarker(at: stop.location, accessibilityName: stop.toDisplayName(), animateIn: false) {
                onRouteStopClicked(stop)
            }
        }
        if let placeDetails = mapUiState.placeDetails {
            placeMarker(at: placeDetails.location, accessibilityName: nil, animateIn: true, onTap: nil)
        }
    }

    private func placeMarker(
        at location: Shared.Location,
        accessibilityName: String?,
        animateIn: Bool,
        onTap: (() -> Void)?
    ) -> MapViewAnnotation {
        MapViewAnnotation(coordinate: location.coordinate) {
            PlaceMarkerView(
                baseLayer: mapUiState.baseLayer,
                colorScheme: colorScheme,
                accessibilityName: accessibilityName,
                animateIn: animateIn,
                onTap: onTap
            )
            // A new place gets a fresh view, so its pop-in animation runs again
            .id(location.id)
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
    let animateIn: Bool
    let onTap: (() -> Void)?

    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var isShown = false
    private let strings = Strings()

    var body: some View {
        let image = MarkerIcons.shared
            .placeSymbol(isDarkMode: colorScheme == .dark, baseLayer: baseLayer)
            .appearanceImage(for: colorScheme)
        let scale = CGFloat(SharedDimens.shared.PLACE_MARKER_SCALE)
        Image(uiImage: image)
            .resizable()
            .frame(width: image.size.width * scale, height: image.size.height * scale)
            .scaleEffect(isShown || !animateIn ? 1 : 0, anchor: .bottom)
            .onAppear {
                guard animateIn else { return }
                withAnimation(reduceMotion ? nil : .spring(duration: 0.45, bounce: 0.4)) { isShown = true }
            }
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
