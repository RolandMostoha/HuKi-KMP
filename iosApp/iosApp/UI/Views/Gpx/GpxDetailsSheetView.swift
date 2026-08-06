import Shared
import SwiftUI

struct GpxDetailsSheetView: View {
    let strings: Strings
    let gpxDetails: GpxDetails
    let onStartClick: () -> Void
    let onNavigateToStart: () -> Void
    let onNavigateToEnd: () -> Void
    let onDismissRequest: () -> Void

    private var hasEndWaypoint: Bool {
        gpxDetails.waypoints.contains { $0.type == .end }
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                header
                GpxStatsRowView(strings: strings, gpxDetails: gpxDetails)
                VStack(spacing: 12) {
                    PrimaryButton(
                        icon: .system("location.north.fill"),
                        title: strings.get(id: SharedRes.strings().gpx_details_start),
                        action: onStartClick
                    )
                    .accessibilityIdentifier(TestTags.shared.GPX_DETAILS_START_BUTTON)
                    navigationButtons
                }
            }
            .padding(.top, 24)
            .padding(.horizontal, 16)
            .padding(.bottom, 16)
        }
        .scrollBounceBehavior(.basedOnSize)
    }

    private var header: some View {
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
                    .background(Circle().fill(Color(.systemGray5)))
            }
            .buttonStyle(.plain)
            .contentShape(Circle())
            .accessibilityLabel(strings.get(id: SharedRes.strings().a11y_close))
            .accessibilityIdentifier(TestTags.shared.GPX_DETAILS_CLOSE_BUTTON)
        }
    }

    @ViewBuilder
    private var navigationButtons: some View {
        VStack(spacing: 12) {
            NavigationButton(
                title: strings.get(id: SharedRes.strings().gpx_details_navigation_to_start),
                action: onNavigateToStart
            )
            .accessibilityIdentifier(TestTags.shared.GPX_DETAILS_NAV_START_BUTTON)
            if hasEndWaypoint {
                NavigationButton(
                    title: strings.get(id: SharedRes.strings().gpx_details_navigation_to_end),
                    action: onNavigateToEnd
                )
                .accessibilityIdentifier(TestTags.shared.GPX_DETAILS_NAV_END_BUTTON)
            }
        }
    }
}

private struct NavigationButton: View {
    let title: String
    let action: () -> Void

    private var primaryColor: SwiftUI.Color {
        SwiftUI.Color(SharedRes.colors().primary.getUIColor())
    }

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.headline)
                .fontWeight(.semibold)
                .lineLimit(1)
                .frame(maxWidth: .infinity)
                .overlay(alignment: .leading) {
                    Image(uiImage: SharedRes.images().ic_maps_navigation.toUIImage()!)
                        .renderingMode(.template)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 22, height: 22)
                }
                .padding(.horizontal, 24)
                .padding(.vertical, 14)
                .foregroundStyle(primaryColor)
                .background(SwiftUI.Color(SharedRes.colors().primaryContainer.getUIColor()), in: .capsule)
        }
        .buttonStyle(PressFeedbackButtonStyle())
    }
}

private struct GpxStatsRowView: View {
    let strings: Strings
    let gpxDetails: GpxDetails

    var body: some View {
        HStack(spacing: 12) {
            StatChipView(
                systemImage: "clock.fill",
                value: strings.get(desc: TravelTimeFormatter.shared.formatTravelTime(duration: gpxDetails.travelTime)),
                label: strings.get(id: SharedRes.strings().gpx_details_travel_time),
                style: .large
            )
            StatChipView(
                systemImage: "location.fill",
                value: DistanceFormatter.shared.formatDistance(distance: gpxDetails.totalDistance),
                label: strings.get(id: SharedRes.strings().gpx_details_distance),
                style: .large
            )
            StatChipView(
                systemImage: "chart.line.uptrend.xyaxis",
                value: DistanceFormatter.shared.formatMeters(meters: gpxDetails.incline),
                label: strings.get(id: SharedRes.strings().gpx_details_incline),
                style: .large
            )
            StatChipView(
                systemImage: "chart.line.downtrend.xyaxis",
                value: DistanceFormatter.shared.formatMeters(meters: gpxDetails.decline),
                label: strings.get(id: SharedRes.strings().gpx_details_decline),
                style: .large
            )
        }
    }
}
