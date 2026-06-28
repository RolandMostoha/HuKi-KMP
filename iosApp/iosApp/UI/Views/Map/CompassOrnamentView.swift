@preconcurrency import MapboxMaps
import Shared
import SwiftUI

/// Custom compass ornament that mirrors the Mapbox compass and forwards the tap so the app can react to it.
struct CompassOrnamentView: View {
    let proxy: MapProxy
    let accessibilityLabel: String
    let onTap: () -> Void

    @State private var bearing: Double = 0

    var body: some View {
        Button(action: onTap) {
            Image(uiImage: SharedRes.images().ic_my_location_compass.toUIImage()!)
                .resizable()
                .frame(width: 48, height: 48)
                .rotationEffect(.degrees(-bearing))
        }
        .buttonStyle(PressFeedbackButtonStyle())
        .accessibilityLabel(accessibilityLabel)
        .task {
            guard let map = proxy.map else { return }
            bearing = map.cameraState.bearing
            let bearingChanges = AsyncStream<Double> { continuation in
                let cancelable = map.onCameraChanged.observe { event in
                    continuation.yield(event.cameraState.bearing)
                }
                continuation.onTermination = { _ in cancelable.cancel() }
            }
            for await newBearing in bearingChanges {
                bearing = newBearing
            }
        }
    }
}
