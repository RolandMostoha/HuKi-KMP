import MapboxMaps
import Shared

extension Shared.Location {
    var coordinate: CLLocationCoordinate2D {
        CLLocationCoordinate2D(
            latitude: latitude,
            longitude: longitude
        )
    }
}

extension Shared.BaseLayer {
    var mapStyle: MapStyle {
        switch self {
        case .outdoors:
            return .outdoors
        case .street:
            return .streets
        case .satellite:
            return .satellite
        }
    }
}
