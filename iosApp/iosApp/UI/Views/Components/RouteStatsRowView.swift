import Shared
import SwiftUI

struct RouteStatsRowView: View {
    let strings: Strings
    let travelTime: String
    let distance: String
    let incline: String
    let decline: String
    var style: StatChipStyle = .standard

    private static let emptyValue = "-"

    init(
        strings: Strings,
        travelTime: String,
        distance: String,
        incline: String,
        decline: String,
        style: StatChipStyle = .standard
    ) {
        self.strings = strings
        self.travelTime = travelTime
        self.distance = distance
        self.incline = incline
        self.decline = decline
        self.style = style
    }

    init(strings: Strings, routeStats: RouteStats?, style: StatChipStyle = .standard) {
        self.strings = strings
        self.style = style
        if let routeStats {
            let travelTimeDesc = TravelTimeFormatter.shared.formatTravelTime(duration: routeStats.travelTime)
            self.travelTime = strings.get(desc: travelTimeDesc)
            self.distance = DistanceFormatter.shared.formatDistance(distance: routeStats.distance)
            self.incline = DistanceFormatter.shared.formatMeters(meters: routeStats.incline)
            self.decline = DistanceFormatter.shared.formatMeters(meters: routeStats.decline)
        } else {
            self.travelTime = Self.emptyValue
            self.distance = Self.emptyValue
            self.incline = Self.emptyValue
            self.decline = Self.emptyValue
        }
    }

    var body: some View {
        HStack(spacing: style == .standard ? 12 : 8) {
            StatChipView(
                systemImage: "clock.fill",
                value: travelTime,
                label: strings.get(id: SharedRes.strings().gpx_details_travel_time),
                style: style
            )
            StatChipView(
                systemImage: "location.fill",
                value: distance,
                label: strings.get(id: SharedRes.strings().gpx_details_distance),
                style: style
            )
            StatChipView(
                systemImage: "chart.line.uptrend.xyaxis",
                value: incline,
                label: strings.get(id: SharedRes.strings().gpx_details_incline),
                style: style
            )
            StatChipView(
                systemImage: "chart.line.downtrend.xyaxis",
                value: decline,
                label: strings.get(id: SharedRes.strings().gpx_details_decline),
                style: style
            )
        }
    }
}

#Preview("Standard") {
    RouteStatsRowView(
        strings: Strings(),
        travelTime: "7h 28m",
        distance: "24.6 km",
        incline: "820 m",
        decline: "760 m"
    )
    .padding()
}

#Preview("Compact") {
    RouteStatsRowView(
        strings: Strings(),
        travelTime: "7h 28m",
        distance: "24.6 km",
        incline: "820 m",
        decline: "760 m",
        style: .compact
    )
    .padding()
}

#Preview("Empty") {
    VStack(spacing: 16) {
        RouteStatsRowView(strings: Strings(), routeStats: nil)
        RouteStatsRowView(strings: Strings(), routeStats: nil, style: .compact)
    }
    .padding()
}
