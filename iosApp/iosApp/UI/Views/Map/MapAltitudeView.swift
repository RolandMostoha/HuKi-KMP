import Shared
import SwiftUI

struct MapAltitudeView: View {
    let altitude: Int32
    let baseLayer: BaseLayer

    @Environment(\.colorScheme) private var colorScheme

    private let strings = Strings()
    private let haloWidth: CGFloat = 1.5
    private let haloSamples = 16

    private var isDarkMap: Bool {
        baseLayer.isDarkMap(isDarkMode: colorScheme == .dark)
    }

    var body: some View {
        let formattedAltitude = DistanceFormatter.shared.formatMeters(meters: altitude)
        let icon = Text("▲").font(.system(size: 14, weight: .semibold))
        let label = Text("\(icon) \(formattedAltitude)").font(.system(size: 12, weight: .semibold)).tracking(0.4)
        label
            .foregroundStyle(isDarkMap ? .white : .black)
            .background {
                ZStack {
                    ForEach(0..<haloSamples, id: \.self) { index in
                        let angle = Double(index) * 2 * .pi / Double(haloSamples)
                        label
                            .foregroundStyle(isDarkMap ? .black : .white)
                            .offset(x: cos(angle) * haloWidth, y: sin(angle) * haloWidth)
                    }
                }
                // The offscreen layer clips to its bounds, so leave room for the halo
                .padding(haloWidth)
                .drawingGroup()
                .padding(-haloWidth)
            }
            .animation(.stateChange, value: isDarkMap)
            .accessibilityElement(children: .ignore)
            .accessibilityLabel(
                strings.get(id: SharedRes.strings().my_location_a11y_altitude, args: [formattedAltitude])
            )
            .accessibilityIdentifier(TestTags.shared.MAIN_ALTITUDE)
    }
}

#Preview {
    VStack(spacing: 0) {
        MapAltitudeView(altitude: 1124, baseLayer: .outdoors)
            .padding()
            .frame(maxWidth: .infinity)
            .background(Color(red: 0.85, green: 0.91, blue: 0.78))
        MapAltitudeView(altitude: 103, baseLayer: .outdoors)
            .padding()
            .frame(maxWidth: .infinity)
            .background(Color(red: 0.16, green: 0.18, blue: 0.16))
            .environment(\.colorScheme, .dark)
    }
}
