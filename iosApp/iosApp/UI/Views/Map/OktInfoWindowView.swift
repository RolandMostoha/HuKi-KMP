import Shared
import SwiftUI

struct OktInfoWindowView: View {
    let strings: Strings
    let info: OktInfoWindowData
    let onPlaceDetailsClick: () -> Void

    private let oktBlue = Color(SharedRes.colors().oktBlue.getUIColor())
    private let primary = Color(SharedRes.colors().primary.getUIColor())

    var body: some View {
        let shape = InfoWindowShape(
            cornerRadius: Dimens.infoWindowCornerRadius,
            tailWidth: Dimens.infoWindowTailWidth,
            tailHeight: Dimens.infoWindowTailHeight
        )
        HStack(alignment: .center, spacing: 10) {
            VStack(alignment: .leading, spacing: 4) {
                Text(strings.get(desc: info.title))
                    .font(.subheadline.weight(.bold))
                    .foregroundStyle(.primary)
                if let description = info.marker.description_ {
                    Text(description)
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                        .fixedSize(horizontal: false, vertical: true)
                }
                if let distance = info.distance, let travelTime = info.travelTime {
                    HStack(spacing: 6) {
                        pill(systemImage: "location.fill", text: distance)
                        pill(systemImage: "clock.fill", text: strings.get(desc: travelTime))
                    }
                    .padding(.top, 2)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            Button(action: onPlaceDetailsClick) {
                Image(systemName: "arrow.triangle.turn.up.right.diamond.fill")
                    .font(.title3)
                    .foregroundStyle(primary)
                    .frame(width: 44, height: 44)
                    .background(Circle().fill(primary.opacity(0.15)))
                    .contentShape(Circle())
            }
            .buttonStyle(PressFeedbackButtonStyle())
            .accessibilityLabel(strings.get(id: SharedRes.strings().okt_info_window_navigate))
            .accessibilityIdentifier(TestTags.shared.OKT_INFO_WINDOW_PLACE_DETAILS_BUTTON)
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 10)
        .padding(.bottom, Dimens.infoWindowTailHeight)
        .frame(width: Dimens.oktInfoWindowWidth)
        .fixedSize(horizontal: false, vertical: true)
        .background(
            shape
                .fill(Color(.systemBackground))
                .shadow(color: .black.opacity(0.2), radius: 6, y: 2)
        )
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(TestTags.shared.OKT_INFO_WINDOW)
    }

    private func pill(systemImage: String, text: String) -> some View {
        Label(text, systemImage: systemImage)
            .font(.caption.weight(.semibold))
            .foregroundStyle(oktBlue)
            .lineLimit(1)
            .padding(.horizontal, 10)
            .padding(.vertical, 5)
            .background(oktBlue.opacity(0.15), in: Capsule())
    }
}

#Preview {
    OktInfoWindowView(
        strings: Strings(),
        info: OktInfoWindowData(
            marker: OktMarker(
                id: "OKTPH_02",
                location: Location(latitude: 47.33, longitude: 16.5, altitude: nil),
                type: .stamp,
                name: "Hét-forrás",
                description: "Hét-forrás - A forrás melletti esőbeálló bejárati oszlopán. (OKTPH_02)"
            ),
            title: RawStringDesc(string: "Hét-forrás stamping point"),
            distance: "4.2 km",
            travelTime: RawStringDesc(string: "1h 20m")
        ),
        onPlaceDetailsClick: {}
    )
    .padding()
}
