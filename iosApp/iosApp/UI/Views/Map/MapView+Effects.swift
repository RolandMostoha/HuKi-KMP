@preconcurrency @_spi(Experimental) import MapboxMaps
import Shared
import SwiftUI

extension MapView {
    func handleMapEffects(_ effect: MapUiEffects, proxy: MapProxy) {
        switch onEnum(of: effect) {
        case .updateCamera(let effect):
            updateCamera(effect)
        case .showMyLocation(let effect):
            showMyLocation(effect, proxy: proxy)
        case .zoom(let effect):
            zoom(effect, proxy: proxy)
        case .resetBearing:
            resetBearing(proxy: proxy)
        }
    }

    func refitAfterStopSelection() async {
        // The selected stop's card resizes the sheet around the selection, so the camera waits for it to settle
        try? await Task.sleep(for: .milliseconds(250))
        guard !Task.isCancelled else { return }
        refitRoutePlannerCamera()
    }

    /// Turns a route plan marker's drop offset into a map location, from where the marker sits on screen.
    /// The offsets are on screen from the marker's location, as an earlier drop can still offset it.
    func dropRoutePlanMarker(_ waypoint: GpxWaypoint, from: CGSize, to target: CGSize, map: MapboxMap?) {
        guard let map else { return }
        let origin = map.point(for: waypoint.location.coordinate)
        func location(at offset: CGSize) -> Shared.Location {
            guard offset != .zero else { return waypoint.location }
            return map.coordinate(for: CGPoint(x: origin.x + offset.width, y: origin.y + offset.height)).location
        }
        onRoutePlanMarkerDragged(location(at: from), location(at: target))
    }
}
