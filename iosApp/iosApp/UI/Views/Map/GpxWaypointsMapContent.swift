@preconcurrency @_spi(Experimental) import MapboxMaps
import Shared
import SwiftUI

struct GpxWaypointsMapContent: MapContent {
    let waypoints: [GpxWaypoint]
    let baseLayer: Shared.BaseLayer
    let colorScheme: ColorScheme
    let onWaypointClicked: (GpxWaypoint) -> Void

    var body: some MapContent {
        PointAnnotationGroup(WaypointMarkerOrder.shared.sort(waypoints: waypoints), id: \.location.id) { waypoint in
            PointAnnotation(coordinate: waypoint.location.coordinate)
                .image(
                    MarkerIcons.shared.waypointSymbol(
                        type: waypoint.type,
                        isDarkMode: colorScheme == .dark,
                        baseLayer: baseLayer
                    )
                    .annotationImage(for: colorScheme)
                )
                .iconSize(
                    waypoint.type == .intermediate
                        ? SharedDimens.shared.GPX_WAYPOINT_MARKER_SCALE
                        : SharedDimens.shared.GPX_EDGE_LOCATION_MARKER_SCALE
                )
                .iconEmissiveStrength(MapLighting.shared.OVERLAY_EMISSIVE_STRENGTH)
                .onTapGesture {
                    onWaypointClicked(waypoint)
                }
        }
    }
}
