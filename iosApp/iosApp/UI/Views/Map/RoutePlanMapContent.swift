@preconcurrency import MapboxMaps
import Shared
import SwiftUI

struct RoutePlanMapContent: MapContent {
    let routePlan: RoutePlan?
    let markers: [GpxWaypoint]

    private static let layerId = "route_plan"

    var body: some MapContent {
        if let routePlan {
            let feature = Feature(geometry: .lineString(routePlan.locations.lineString))

            GeoJSONSource(id: Self.layerId)
                .data(.feature(feature))

            LineLayer(id: Self.layerId, source: Self.layerId)
                .lineWidth(SharedDimens.shared.GPX_LINE_WIDTH)
                .lineColor(SharedRes.colors().primaryOnMap.getUIColor())
                .lineBorderWidth(SharedDimens.shared.GPX_STROKE_WIDTH)
                .lineBorderColor(SharedRes.colors().mapStrokeOnMap.getUIColor())
        }
        // In Route Plans, waypoints should sit on top of my location, to always see the plan.
        // ViewAnnotations are real views above every style layer.
        ForEvery(WaypointMarkerOrder.shared.sort(waypoints: markers), id: \.location.id) { waypoint in
            MapViewAnnotation(coordinate: waypoint.location.coordinate) {
                RoutePlanMarkerView(waypoint: waypoint)
            }
            .allowOverlap(true)
            .allowOverlapWithPuck(true)
        }
    }
}

private struct RoutePlanMarkerView: View {
    let waypoint: GpxWaypoint

    private var scale: CGFloat {
        CGFloat(
            waypoint.type == .intermediate
                ? SharedDimens.shared.GPX_WAYPOINT_MARKER_SCALE
                : SharedDimens.shared.GPX_EDGE_LOCATION_MARKER_SCALE
        )
    }

    var body: some View {
        if let image = waypoint.type.icon.toUIImage() {
            Image(uiImage: image)
                .resizable()
                .frame(width: image.size.width * scale, height: image.size.height * scale)
        }
    }
}
