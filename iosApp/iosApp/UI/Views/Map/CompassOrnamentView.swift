@preconcurrency import MapboxMaps
import Shared
import SwiftUI

/// Custom compass ornament that mirrors the Mapbox compass and forwards the tap so the app can react to it.
struct CompassOrnamentView: View {
    let bearing: Double
    let accessibilityLabel: String
    let onTap: () -> Void

    private let strings = Strings()
    private let northColor = SwiftUI.Color(SharedRes.colors().compassNorth.getUIColor())

    var body: some View {
        Button(action: onTap) {
            ZStack {
                Image(uiImage: SharedRes.images().ic_my_location_compass_needle.toUIImage()!)
                    .resizable()
                VStack {
                    letter(SharedRes.strings().map_compass_north, color: northColor)
                    Spacer(minLength: 0)
                    letter(SharedRes.strings().map_compass_south)
                }
                .padding(.vertical, 4)
                HStack {
                    letter(SharedRes.strings().map_compass_west)
                    Spacer(minLength: 0)
                    letter(SharedRes.strings().map_compass_east)
                }
                .padding(.horizontal, 5)
            }
            .rotationEffect(.degrees(-bearing))
            .frame(width: 56, height: 56)
            .contentShape(Circle())
        }
        .buttonStyle(PressFeedbackButtonStyle())
        .glassBackground(.regular, in: Circle(), interactive: true)
        .accessibilityLabel(accessibilityLabel)
    }

    private func letter(_ id: StringResource, color: SwiftUI.Color = .primary) -> some View {
        Text(strings.get(id: id))
            .font(.system(size: 8, weight: .bold))
            .foregroundStyle(color)
            .fixedSize()
            .rotationEffect(.degrees(bearing))
    }
}

#Preview {
    CompassOrnamentView(bearing: 30, accessibilityLabel: "Compass", onTap: {})
        .padding()
        .background(SwiftUI.Color(.systemGray5))
}

#Preview("Dark Mode") {
    CompassOrnamentView(bearing: 30, accessibilityLabel: "Compass", onTap: {})
        .padding()
        .background(SwiftUI.Color(.systemGray5))
        .preferredColorScheme(.dark)
}
