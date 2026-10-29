import Shared
import SwiftUI

struct MapAltitudeView: View {
    let altitude: Int32

    private let strings = Strings()
    private let haloWidth: CGFloat = 1

    var body: some View {
        let formattedAltitude = DistanceFormatter.shared.formatMeters(meters: altitude)
        let icon = Text("▲").font(.system(size: 14, weight: .semibold))
        let label = Text("\(icon) \(formattedAltitude)").font(.system(size: 12, weight: .semibold)).tracking(0.4)
        label
            .foregroundStyle(.black)
            .background {
                ForEach(0..<8, id: \.self) { index in
                    let angle = Double(index) * .pi / 4
                    label
                        .foregroundStyle(.white)
                        .offset(x: cos(angle) * haloWidth, y: sin(angle) * haloWidth)
                }
            }
            .accessibilityElement(children: .ignore)
            .accessibilityLabel(
                strings.get(id: SharedRes.strings().my_location_a11y_altitude, args: [formattedAltitude])
            )
            .accessibilityIdentifier(TestTags.shared.MAIN_ALTITUDE)
    }
}

#Preview {
    MapAltitudeView(altitude: 1124)
        .padding()
        .background(Color(red: 0.85, green: 0.91, blue: 0.78))
}
