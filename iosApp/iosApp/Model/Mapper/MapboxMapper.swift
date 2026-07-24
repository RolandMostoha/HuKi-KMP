import MapboxMaps
import Shared
import SwiftUI

extension Shared.Location {
    var coordinate: CLLocationCoordinate2D {
        CLLocationCoordinate2D(
            latitude: latitude,
            longitude: longitude
        )
    }
}

extension Shared.Location {
    var id: String {
        String(format: "%.6f,%.6f,%.2f", latitude, longitude, (altitude?.doubleValue ?? 0.0))
    }
}

extension Array where Element == Shared.Location {
    var coordinates: [CLLocationCoordinate2D] {
        map(\.coordinate)
    }
}

extension Array where Element == Shared.Location {
    var lineString: LineString {
        LineString(self.coordinates)
    }
}

extension Shared.ContentPadding {
    func edgeInsets(isLandscape: Bool) -> SwiftUI.EdgeInsets {
        switch self {
        case .mapGpx:
            return isLandscape ? Dimens.gpxContentPaddingLandscape : Dimens.gpxContentPaddingPortrait
        }
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

extension Shared.ImageResource {
    var annotationImage: PointAnnotation.Image {
        PointAnnotation.Image(image: self.toUIImage()!, name: self.assetImageName)
    }
}
