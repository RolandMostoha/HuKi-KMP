import Shared
import SwiftUI

struct RoundTripWizardDistanceSliderView: View {
    let strings: Strings
    let distanceKm: Int
    let onDistanceCommitted: (Int) -> Void

    // The thumb's value while dragging, the route is only replanned once it is let go
    @State private var draftKm: Double?

    private let primaryColor = Color(SharedRes.colors().primary.getUIColor())
    private let minKm = Int(RoutePlanConstants.shared.ROUND_TRIP_MIN_DISTANCE_KM)
    private let maxKm = Int(RoutePlanConstants.shared.ROUND_TRIP_MAX_DISTANCE_KM)
    private static let tickLabelsKm = [1, 10, 20, 30]

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack(alignment: .firstTextBaseline) {
                Text(strings.get(id: SharedRes.strings().round_trip_wizard_distance_title))
                    .font(.system(size: 17, weight: .medium))
                Spacer()
                Text(valueText(displayedKm))
                    .font(.system(size: 22, weight: .bold).monospacedDigit())
                    .foregroundStyle(primaryColor)
                    .contentTransition(.numericText())
                    .accessibilityIdentifier(TestTags.shared.ROUND_TRIP_WIZARD_DISTANCE_VALUE)
            }
            Slider(
                value: Binding(get: { draftKm ?? Double(distanceKm) }, set: { draftKm = $0 }),
                in: Double(minKm)...Double(maxKm),
                step: 1,
                onEditingChanged: { isEditing in
                    guard !isEditing, let draftKm else { return }
                    onDistanceCommitted(Int(draftKm.rounded()))
                }
            )
            .tint(primaryColor)
            .accessibilityLabel(strings.get(id: SharedRes.strings().round_trip_wizard_distance_title))
            .accessibilityValue(valueText(displayedKm))
            // VoiceOver adjusts the value without an editing gesture, so each step is committed right away
            .accessibilityAdjustableAction { direction in
                switch direction {
                case .increment: commit(displayedKm + 1)
                case .decrement: commit(displayedKm - 1)
                @unknown default: break
                }
            }
            .accessibilityIdentifier(TestTags.shared.ROUND_TRIP_WIZARD_DISTANCE_SLIDER)
            tickLabels
        }
        .onChange(of: distanceKm) { draftKm = nil }
    }

    private var displayedKm: Int {
        draftKm.map { Int($0.rounded()) } ?? distanceKm
    }

    private var tickLabels: some View {
        GeometryReader { proxy in
            ForEach(Self.tickLabelsKm, id: \.self) { kilometers in
                Text(valueText(kilometers))
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .fixedSize()
                    .position(x: tickX(kilometers, width: proxy.size.width), y: proxy.size.height / 2)
            }
        }
        .frame(height: 16)
        .accessibilityHidden(true)
    }

    // The end labels stay inside the track, the others sit under their value
    private func tickX(_ kilometers: Int, width: CGFloat) -> CGFloat {
        let fraction = CGFloat(kilometers - minKm) / CGFloat(maxKm - minKm)
        let inset: CGFloat = 16
        return min(max(fraction * width, inset), width - inset)
    }

    private func commit(_ kilometers: Int) {
        let clamped = min(max(kilometers, minKm), maxKm)
        draftKm = nil
        onDistanceCommitted(clamped)
    }

    private func valueText(_ kilometers: Int) -> String {
        strings.get(id: SharedRes.strings().round_trip_wizard_distance_value, args: [Int32(kilometers)])
    }
}

#Preview {
    RoundTripWizardDistanceSliderView(strings: Strings(), distanceKm: 9, onDistanceCommitted: { _ in })
        .padding()
}
