import Shared
import SwiftUI

struct GpxDetailsSheetView: View {
    let strings: Strings
    let gpxDetails: GpxDetails
    let onStartClick: () -> Void
    let onDismissRequest: () -> Void

    var body: some View {
        VStack(spacing: 20) {
            Capsule()
                .fill(Color(.systemGray3))
                .frame(width: 36, height: 5)
                .padding(.top, 4)
            ZStack(alignment: .topTrailing) {
                VStack(spacing: 4) {
                    if gpxDetails.title != nil {
                        Text(gpxDetails.fileName)
                            .font(.system(size: 14, weight: .regular))
                            .foregroundStyle(Color(SharedRes.colors().primary.getUIColor()))
                            .lineLimit(2)
                            .multilineTextAlignment(.leading)
                            .frame(maxWidth: .infinity, alignment: .leading)
                    }
                    Text(gpxDetails.title ?? gpxDetails.fileName)
                        .font(.system(size: 16, weight: .bold))
                        .lineLimit(2)
                        .multilineTextAlignment(.leading)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .accessibilityIdentifier(TestTags.shared.GPX_DETAILS_TITLE)
                }
                .frame(maxWidth: .infinity)
                .padding(.trailing, 56)
                Button(action: onDismissRequest) {
                    Image(systemName: "xmark")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(.primary)
                        .padding(12)
                        .background(
                            Circle().fill(Color(.systemGray5))
                        )
                }
                .buttonStyle(.plain)
                .contentShape(Circle())
                .accessibilityLabel(strings.get(id: SharedRes.strings().a11y_close))
            }
            GpxStatsRowView(
                strings: strings,
                gpxDetails: gpxDetails
            )
            Button(action: onStartClick) {
                Label {
                    Text(strings.get(id: SharedRes.strings().gpx_details_start))
                        .font(.headline)
                        .fontWeight(.semibold)
                } icon: {
                    Image(systemName: "location.north.fill")
                        .font(.system(size: 15, weight: .semibold))
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 8)
            }
            .buttonStyle(.borderedProminent)
            .tint(Color(SharedRes.colors().primary.getUIColor()))
            .clipShape(Capsule())
        }
        .padding(.top, 10)
        .padding(.horizontal, 16)
        .padding(.bottom, 10)
    }
}

private struct GpxStatsRowView: View {
    let strings: Strings
    let gpxDetails: GpxDetails

    var body: some View {
        HStack(spacing: 12) {
            GpxStatCardView(
                label: strings.get(id: SharedRes.strings().gpx_details_travel_time),
                value: strings.get(desc: TravelTimeFormatter.shared.formatTravelTime(duration: gpxDetails.travelTime)),
                systemImage: "clock.fill"
            )
            GpxStatCardView(
                label: strings.get(id: SharedRes.strings().gpx_details_distance),
                value: DistanceFormatter.shared.formatDistance(distance: gpxDetails.totalDistance),
                systemImage: "location.fill"
            )
            GpxStatCardView(
                label: strings.get(id: SharedRes.strings().gpx_details_incline),
                value: DistanceFormatter.shared.formatMeters(meters: gpxDetails.incline),
                systemImage: "chart.line.uptrend.xyaxis"
            )
            GpxStatCardView(
                label: strings.get(id: SharedRes.strings().gpx_details_decline),
                value: DistanceFormatter.shared.formatMeters(meters: gpxDetails.decline),
                systemImage: "chart.line.downtrend.xyaxis"
            )
        }
    }
}

private struct GpxStatCardView: View {
    let label: String
    let value: String
    let systemImage: String

    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: systemImage)
                .font(.system(size: 18, weight: .semibold))
                .foregroundStyle(Color(SharedRes.colors().primary.getUIColor()))
            Text(
                UiFormatter.formatStatValue(
                    value,
                    smallFont: .system(size: 12, weight: .medium)
                )
            )
                .font(.system(size: 18, weight: .bold))
                .multilineTextAlignment(.center)
                .lineLimit(1)
        }
        .frame(maxWidth: .infinity)
        .padding(.horizontal, 8)
        .padding(.vertical, 16)
        .background(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .fill(Color(.systemGray6))
        )
        .accessibilityElement(children: .ignore)
        .accessibilityLabel("\(label), \(value)")
    }
}
