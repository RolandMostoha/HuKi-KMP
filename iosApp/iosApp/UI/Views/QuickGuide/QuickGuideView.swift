import Shared
import SwiftUI

enum QuickGuideRoute: Hashable {
    case quickGuide
}

struct QuickGuideView: View {
    let onGpxGuideClicked: () -> Void

    @Environment(\.dismiss) private var dismiss
    @Environment(\.colorScheme) private var colorScheme
    @StateObject private var holder = ViewModelHolder(KoinViewModelProvider.shared.getQuickGuideViewModel)
    private var viewModel: QuickGuideViewModel { holder.viewModel }

    private let strings = Strings()
    private let primary = Color(SharedRes.colors().primary.getUIColor())
    private let primaryContainer = Color(SharedRes.colors().primaryContainer.getUIColor())

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                myLocationSection
                routePlannerSection
                travelTimeSection
                slopeSection
                gpxSection
            }
            .padding(.bottom, 24)
            .readableWidth()
        }
        .background(Color(.systemGroupedBackground))
        .task {
            viewModel.onEvent(event: QuickGuideUiEventsScreenViewed.shared)
        }
        .accessibilityIdentifier(TestTags.shared.QUICK_GUIDE_SCREEN_ROOT)
        .navigationTitle(strings.get(id: SharedRes.strings().quick_guide_title))
        .navigationBarTitleDisplayMode(.large)
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) { backButton }
        }
    }

    private var backButton: some View {
        Button(
            action: { dismiss() },
            label: {
                Label(
                    strings.get(id: SharedRes.strings().quick_guide_a11y_back),
                    systemImage: "chevron.backward"
                )
                .fontWeight(.semibold)
                .foregroundStyle(.primary)
            }
        )
        .labelStyle(.iconOnly)
        .accessibilityIdentifier(TestTags.shared.QUICK_GUIDE_BACK_BUTTON)
    }

    private var myLocationSection: some View {
        GuideSectionView(title: strings.get(id: SharedRes.strings().quick_guide_my_location_section)) {
            GuideStepView(
                title: strings.get(id: SharedRes.strings().quick_guide_my_location_following_title),
                message: strings.get(id: SharedRes.strings().quick_guide_my_location_following_message),
                leading: { GestureDemoFabView(systemImage: "location.fill", gesture: .click) }
            )
            GuideStepDividerView(leadingInset: 88)
            GuideStepView(
                title: strings.get(id: SharedRes.strings().quick_guide_my_location_navigation_title),
                message: strings.get(id: SharedRes.strings().quick_guide_my_location_navigation_message),
                leading: { GestureDemoFabView(systemImage: "location.north.line.fill", gesture: .longClick) }
            )
        }
    }

    private var routePlannerSection: some View {
        GuideSectionView(title: strings.get(id: SharedRes.strings().quick_guide_route_planner_section)) {
            GuideStepView(
                title: strings.get(id: SharedRes.strings().quick_guide_route_planner_step_1_title),
                message: strings.get(id: SharedRes.strings().quick_guide_route_planner_step_1_message),
                leading: { GuideStepBadgeView(number: 1) }
            )
            GuideStepDividerView()
            GuideStepView(
                title: strings.get(id: SharedRes.strings().quick_guide_route_planner_step_2_title),
                message: strings.get(id: SharedRes.strings().quick_guide_route_planner_step_2_message),
                leading: { GuideStepBadgeView(number: 2) }
            )
            GuideStepDividerView()
            GuideStepView(
                title: strings.get(id: SharedRes.strings().quick_guide_route_planner_step_3_title),
                message: strings.get(id: SharedRes.strings().quick_guide_route_planner_step_3_message),
                leading: { GuideStepBadgeView(number: 3) },
                content: { routePlanButtonExample }
            )
        }
    }

    private var routePlanButtonExample: some View {
        PrimaryButton(
            icon: .system("point.topright.arrow.triangle.backward.to.point.bottomleft.scurvepath.fill"),
            title: strings.get(
                id: SharedRes.strings().place_details_route_plan_count,
                args: [Int32(3)]
            ),
            size: .small,
            action: {}
        )
        .padding(.top, 8)
        .padding(.bottom, 4)
        .allowsHitTesting(false)
        .accessibilityHidden(true)
    }

    private var travelTimeSection: some View {
        GuideSectionView(title: strings.get(id: SharedRes.strings().quick_guide_travel_time_section)) {
            GuideStepView(
                title: strings.get(id: SharedRes.strings().quick_guide_travel_time_time_title),
                message: strings.get(id: SharedRes.strings().quick_guide_travel_time_time_message),
                leading: {
                    StatChipView(
                        systemImage: "clock.fill",
                        value: strings.get(desc: QuickGuideExamples.shared.travelTime),
                        style: .standard,
                        backgroundColor: Color(.quaternarySystemFill)
                    )
                    .frame(width: 76)
                    .accessibilityHidden(true)
                }
            )
            noStopsCallout
                .padding([.horizontal, .bottom], 14)
        }
    }

    private func stepIcon(_ systemName: String) -> some View {
        Image(systemName: systemName)
            .font(.system(size: 18, weight: .semibold))
            .foregroundStyle(primary)
            .frame(width: 40, height: 40)
            .background(primaryContainer, in: Circle())
            .accessibilityHidden(true)
    }

    private var noStopsCallout: some View {
        HStack(alignment: .top, spacing: 10) {
            Image(systemName: "info.circle.fill")
                .font(.system(size: 18, weight: .semibold))
                .foregroundStyle(primary)
                .accessibilityHidden(true)
            VStack(alignment: .leading, spacing: 3) {
                Text(strings.get(id: SharedRes.strings().quick_guide_travel_time_no_stops_title))
                    .font(.subheadline.weight(.bold))
                    .foregroundStyle(.primary)
                Text(strings.get(id: SharedRes.strings().quick_guide_travel_time_no_stops_message))
                    .font(.footnote)
                    .foregroundStyle(.primary)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(12)
        .background(primaryContainer, in: RoundedRectangle(cornerRadius: 12, style: .continuous))
        .accessibilityElement(children: .combine)
    }

    private var slopeColorScale: SlopeColorScale {
        let traits = UITraitCollection(userInterfaceStyle: colorScheme == .dark ? .dark : .light)
        let flatColor = SharedRes.colors().primary.getUIColor().resolvedColor(with: traits).rgb
        return SlopeColorScale(flatColor: flatColor)
    }

    private var slopeSection: some View {
        let colorScale = slopeColorScale
        return GuideSectionView(title: strings.get(id: SharedRes.strings().quick_guide_slope_section)) {
            GuideStepView(
                title: strings.get(id: SharedRes.strings().quick_guide_slope_coloring_title),
                message: strings.get(
                    id: SharedRes.strings().quick_guide_slope_message,
                    args: [Int32(SlopeConstants.shared.HIGH_SLOPE_PERCENT)]
                ),
                leading: { stepIcon("chart.line.uptrend.xyaxis") },
                content: {
                    VStack(alignment: .leading, spacing: 16) {
                        slopeBar(colorScale)
                        slopeLegendTable(colorScale)
                    }
                    .padding(.top, 12)
                }
            )
        }
    }

    private func slopeBar(_ colorScale: SlopeColorScale) -> some View {
        let stops = colorScale.gradientStops().map { stop in
            Gradient.Stop(color: Color(rgb: stop.rgb), location: CGFloat(stop.fraction))
        }
        return HStack(spacing: 10) {
            Text(SlopeLegend.shared.minLabel)
                .font(.subheadline.weight(.semibold))
            RoundedRectangle(cornerRadius: 6, style: .continuous)
                .fill(LinearGradient(stops: stops, startPoint: .leading, endPoint: .trailing))
                .frame(height: 12)
            Text(SlopeLegend.shared.maxLabel)
                .font(.subheadline.weight(.semibold))
        }
        .accessibilityHidden(true)
    }

    private func slopeLegendTable(_ colorScale: SlopeColorScale) -> some View {
        Grid(alignment: .leading, horizontalSpacing: 12, verticalSpacing: 8) {
            GridRow {
                Text(strings.get(id: SharedRes.strings().quick_guide_slope_level_column))
                Text(strings.get(id: SharedRes.strings().quick_guide_slope_difficulty_column))
            }
            .font(.caption.weight(.bold))
            ForEach(SlopeLegend.shared.rows, id: \.label) { row in
                GridRow {
                    HStack(spacing: 8) {
                        Circle()
                            .fill(Color(rgb: colorScale.colorRgb(slopePercent: row.slopePercent)))
                            .frame(width: 10, height: 10)
                        Text(row.label)
                            .font(.subheadline.weight(.semibold))
                    }
                    Text(strings.get(id: row.description_))
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
                .accessibilityElement(children: .combine)
            }
        }
    }

    private var gpxSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            GuideSectionTitleView(text: strings.get(id: SharedRes.strings().quick_guide_gpx_section))
            NavigationRowCardView(
                systemImage: "point.bottomleft.forward.to.point.topright.scurvepath",
                title: strings.get(id: SharedRes.strings().menu_item_gpx_guide),
                subtitle: strings.get(id: SharedRes.strings().menu_item_gpx_guide_description),
                onClick: onGpxGuideClicked
            )
            .accessibilityIdentifier(TestTags.shared.QUICK_GUIDE_GPX_GUIDE_ROW)
        }
        .padding(.horizontal, 16)
        .padding(.top, 20)
    }
}

#Preview {
    NavigationStack {
        QuickGuideView(onGpxGuideClicked: {})
    }
}
