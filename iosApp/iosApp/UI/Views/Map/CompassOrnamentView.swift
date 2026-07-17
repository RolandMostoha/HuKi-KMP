@preconcurrency import MapboxMaps
import Shared
import SwiftUI

/// Custom compass ornament that mirrors the Mapbox compass and forwards the tap so the app can react to it.
struct CompassOrnamentView: View {
    let bearing: Double
    let accessibilityLabel: String
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            Image(uiImage: SharedRes.images().ic_my_location_compass.toUIImage()!)
                .resizable()
                .frame(width: 48, height: 48)
                .rotationEffect(.degrees(-bearing))
        }
        .buttonStyle(PressFeedbackButtonStyle())
        .accessibilityLabel(accessibilityLabel)
    }
}
