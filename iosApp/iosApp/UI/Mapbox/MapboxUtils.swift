@preconcurrency import MapboxMaps
import Shared
import SwiftUI

extension Viewport {

    /// Camera target for an `updateCamera` effect: a single centered point or a route fitted to bounds.
    /// `isLandscape` selects orientation-specific overview padding so it always fits the viewport (portrait
    /// reserves bottom space for the sheet; landscape stays shallow to fit the short map).
    static func target(for effect: MapUiEffectsUpdateCamera, isLandscape: Bool = false) -> Viewport {
        switch onEnum(of: effect.target) {
        case .center(let target):
            return .camera(
                center: target.location.coordinate,
                zoom: target.zoom?.cgFloat,
                bearing: effect.bearing?.cgFloat ?? 0,
                pitch: effect.pitch?.cgFloat ?? 0
            )
        case .bounds(let target):
            let padding = effect.contentPadding?.edgeInsets(isLandscape: isLandscape) ?? .init()
            return .overview(
                geometry: target.locations.lineString,
                bearing: effect.bearing?.cgFloat ?? 0,
                pitch: effect.pitch?.cgFloat ?? 0,
                geometryPadding: padding,
                maxZoom: target.maxZoom?.doubleValue
            )
        }
    }

    /// North-up target that keeps the current center, zoom and pitch.
    static func resetBearingTarget(cameraState: CameraState) -> Viewport {
        return .camera(
            center: cameraState.center,
            zoom: cameraState.zoom,
            bearing: 0,
            pitch: cameraState.pitch
        )
    }

    /// Follow-puck target for the given status; preserves the current zoom while already following,
    /// otherwise applies at least the default follow zoom. Returns nil for non-follow statuses.
    func followLocationTarget(_ myLocationStatus: MyLocationStatus, cameraZoom: CGFloat?) -> Viewport? {
        let defaultZoom = CGFloat(MapConstants.shared.FOLLOW_LOCATION_ZOOM_LEVEL)
        let currentZoom = cameraZoom ?? defaultZoom
        let zoom = followPuck != nil
            ? currentZoom
            : max(currentZoom, defaultZoom)

        switch onEnum(of: myLocationStatus) {
        case .following:
            return .followPuck(
                zoom: zoom,
                bearing: .constant(0.0),
                pitch: 0.0
            )
        case .followingLiveCompass:
            return .followPuck(
                zoom: zoom,
                bearing: .heading,
                pitch: MapConstants.shared.FOLLOW_LOCATION_LIVE_COMPASS_PITCH
            )
        default:
            return nil
        }
    }

    /// One-step zoom target clamped to the min/max zoom, keeping follow-puck mode while following.
    func zoomTarget(_ effect: MapUiEffectsZoom, cameraState: CameraState) -> Viewport {
        let step = effect.zoomIn ? MapConstants.shared.MAP_ZOOM_STEP : -MapConstants.shared.MAP_ZOOM_STEP
        let newZoom = min(
            max(cameraState.zoom + step, MapConstants.shared.MAP_MIN_ZOOM),
            MapConstants.shared.MAP_MAX_ZOOM
        )
        if let followPuck = followPuck {
            return .followPuck(
                zoom: newZoom,
                bearing: followPuck.bearing,
                pitch: followPuck.pitch
            )
        } else {
            return .camera(
                center: cameraState.center,
                zoom: newZoom,
                bearing: cameraState.bearing,
                pitch: cameraState.pitch
            )
        }
    }
}
