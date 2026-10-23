@preconcurrency @_spi(Experimental) import MapboxMaps
import Shared
import SwiftUI

struct GpxWaypointsMapContent: MapContent {
    let waypoints: [GpxWaypoint]
    let jelSymbol: JelSymbol?
    let baseLayer: Shared.BaseLayer
    let colorScheme: ColorScheme
    let onWaypointClicked: (GpxWaypoint) -> Void

    var body: some MapContent {
        PointAnnotationGroup(WaypointMarkerOrder.shared.sort(waypoints: waypoints), id: \.location.id) { waypoint in
            PointAnnotation(coordinate: waypoint.location.coordinate)
                .image(markerImage(for: waypoint))
                .iconSize(
                    waypoint.type == .intermediate
                        ? SharedDimens.shared.GPX_WAYPOINT_MARKER_SCALE
                        : SharedDimens.shared.GPX_EDGE_LOCATION_MARKER_SCALE
                )
                .iconEmissiveStrength(MapLighting.shared.OVERLAY_EMISSIVE_STRENGTH)
                .symbolSortKey(waypoint.type == .intermediate ? 1 : 0)
                .onTapGesture {
                    onWaypointClicked(waypoint)
                }
        }
    }

    private func markerImage(for waypoint: GpxWaypoint) -> PointAnnotation.Image {
        let isDarkMode = colorScheme == .dark
        if let jelSymbol = MarkerIcons.shared.waypointJelSymbol(type: waypoint.type, jelSymbol: jelSymbol) {
            return jelSymbol.markerImage(
                background: MarkerIcons.shared.jelMarkerFrame(isDarkMode: isDarkMode, baseLayer: baseLayer),
                for: colorScheme
            )
        }
        return MarkerIcons.shared.waypointSymbol(type: waypoint.type, isDarkMode: isDarkMode, baseLayer: baseLayer)
            .annotationImage(for: colorScheme)
    }
}
