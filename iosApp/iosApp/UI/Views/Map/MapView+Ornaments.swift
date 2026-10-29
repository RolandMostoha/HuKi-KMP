import Shared
import SwiftUI

extension MapView {
    var altitudeOrnament: some View {
        ZStack {
            if let altitude = uiState.myLocationState.altitude {
                MapAltitudeView(altitude: altitude.int32Value)
                    .transition(.opacity)
            }
        }
        .padding(.top, 42)
        .padding(.leading, 16)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        .allowsHitTesting(false)
        .animation(.smooth(duration: 0.3), value: uiState.myLocationState.altitude == nil)
    }

    func isFacingNorth(_ bearing: Double) -> Bool {
        let tolerance = MapConstants.shared.MAP_FACING_NORTH_TOLERANCE
        let rotation = abs(bearing)
        return rotation >= 360.0 - tolerance || rotation <= tolerance
    }
}
