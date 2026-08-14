import Shared
import SwiftUI

struct RoutePlannerRouteStatusView: View {
    let strings: Strings
    let error: InfoViewData?
    let routeStats: RouteStats?
    let isPlanExpected: Bool
    let onRetryClicked: () -> Void

    static let rowHeight: CGFloat = 44

    var body: some View {
        if let error {
            InfoView(
                strings: strings,
                infoViewData: error,
                primaryActionText: strings.get(id: SharedRes.strings().route_planner_error_retry),
                onPrimaryActionClick: onRetryClicked
            )
            .accessibilityElement(children: .contain)
            .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_ERROR)
        } else if let routeStats {
            RouteStatsRowView(strings: strings, routeStats: routeStats, style: .compact)
                .frame(minHeight: Self.rowHeight)
                .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_STATS)
        } else if isPlanExpected {
            Color.clear
                .frame(height: Self.rowHeight)
                .accessibilityHidden(true)
        }
    }
}

#Preview("Error") {
    RoutePlannerRouteStatusView(
        strings: Strings(),
        error: NetworkError.noInternet.toInfoViewData(),
        routeStats: nil,
        isPlanExpected: true,
        onRetryClicked: {}
    )
    .padding()
}
