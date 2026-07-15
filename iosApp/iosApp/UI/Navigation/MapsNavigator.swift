import Shared
import UIKit

enum MapsNavigator {
    static func openDirections(to location: Location) {
        let coordinates = String(format: "%.6f,%.6f", location.latitude, location.longitude)
        let googleMaps = URL(string: "comgooglemaps://?daddr=\(coordinates)")!
        if UIApplication.shared.canOpenURL(googleMaps) {
            UIApplication.shared.open(googleMaps)
        } else if let appleMaps = URL(string: "https://maps.apple.com/?daddr=\(coordinates)") {
            UIApplication.shared.open(appleMaps)
        }
    }
}
