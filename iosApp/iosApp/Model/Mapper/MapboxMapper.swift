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
        String(format: "%.6f,%.6f,%.2f", latitude, longitude, altitude ?? 0)
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
    var edgeInsets: SwiftUI.EdgeInsets {
        SwiftUI.EdgeInsets(
            top: Double(self.top),
            leading: Double(self.left),
            bottom: Double(self.bottom),
            trailing: Double(self.right)
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

extension Shared.ImageResource {
    var annotationImage: PointAnnotation.Image {
        PointAnnotation.Image(image: self.toUIImage()!, name: self.assetImageName)
    }
}
