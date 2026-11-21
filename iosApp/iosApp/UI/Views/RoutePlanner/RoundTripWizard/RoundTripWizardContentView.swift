import Shared
import SwiftUI

struct RoundTripWizardContentView: View {
    let strings: Strings
    let uiState: RoutePlannerUiState
    let onEvent: (RoutePlannerUiEvents) -> Void
    var detent: RoutePlannerDetent = .expanded
    var onDetentChange: (RoutePlannerDetent) -> Void = { _ in }
    var onHeightsChange: (RoutePlannerSheetHeights) -> Void = { _ in }

    @State private var expandedHeight = Dimens.routePlannerDetentHeight
    @State private var minimizedHeight = Dimens.routePlannerMinimizedDetentHeight

    private static let stackSpacing: CGFloat = 16
    private static let blockCornerRadius: CGFloat = 26

    var body: some View {
        contentStack(isExpanded: detent != .minimized)
            .animation(.stateChange, value: uiState.isRoutePlanLoading)
            .animation(.stateChange, value: uiState.routePlanError)
            .animation(.stateChange, value: uiState.routeStats)
            .animation(.stateChange, value: detent)
            .animation(.stateChange, value: uiState.selectedStop?.id)
            // Both detents are measured on hidden copies, so the sheet knows its other height before it switches
            .background(alignment: .top) { heightReader(isExpanded: true) { expandedHeight = $0 } }
            .background(alignment: .top) { heightReader(isExpanded: false) { minimizedHeight = $0 } }
            .onChange(of: heights, initial: true) { _, newHeights in
                onHeightsChange(newHeights)
            }
            .frame(maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .top)
            .accessibilityElement(children: .contain)
            .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_SHEET)
    }

    private var heights: RoutePlannerSheetHeights {
        RoutePlannerSheetHeights(expanded: expandedHeight, minimized: minimizedHeight)
    }

    private func contentStack(isExpanded: Bool) -> some View {
        VStack(spacing: Self.stackSpacing) {
            RoutePlannerHeaderView(
                strings: strings,
                title: strings.get(id: SharedRes.strings().round_trip_wizard_title),
                expandAccessibilityLabel: strings.get(id: SharedRes.strings().round_trip_wizard_a11y_expand),
                minimizeAccessibilityLabel: strings.get(id: SharedRes.strings().round_trip_wizard_a11y_minimize),
                detent: detent,
                onDetentChange: onDetentChange,
                onAttributionClicked: { onEvent(RoutePlannerUiEventsGraphhopperAttributionClicked.shared) },
                onCloseClicked: { onEvent(RoutePlannerUiEventsCloseClicked.shared) }
            )
            if isExpanded {
                RoutePlannerProfilePickerView(
                    strings: strings,
                    selectedProfile: uiState.routeProfile,
                    onProfileSelected: { onEvent(RoutePlannerUiEventsProfileSelected(routeProfile: $0)) }
                )
                .padding(.horizontal, 16)
                .transition(.opacity.combined(with: .move(edge: .bottom)))
            }
            routeBlock(isExpanded: isExpanded)
            statusRegion
            actionsRow
        }
        .padding(.top, 20)
        .fixedSize(horizontal: false, vertical: true)
    }

    private func routeBlock(isExpanded: Bool) -> some View {
        VStack(spacing: 12) {
            if isExpanded, let start = uiState.roundTripStart {
                RoundTripWizardStartCardView(
                    strings: strings,
                    start: start,
                    isExpanded: uiState.isStopExpanded(stop: start),
                    onEvent: onEvent
                )
                .transition(.opacity.combined(with: .move(edge: .bottom)))
            }
            RoundTripWizardDistanceSliderView(
                strings: strings,
                distanceKm: Int(
                    uiState.roundTrip?.distanceKm ?? RoutePlanConstants.shared.ROUND_TRIP_DEFAULT_DISTANCE_KM
                ),
                onDistanceCommitted: { onEvent(RoutePlannerUiEventsRoundTripDistanceChanged(distanceKm: Int32($0))) }
            )
            .padding(.horizontal, 4)
        }
        .padding(12)
        .background(
            Color(.tertiarySystemFill),
            in: RoundedRectangle(cornerRadius: Self.blockCornerRadius, style: .continuous)
        )
        .padding(.horizontal, 16)
    }

    private var statusRegion: some View {
        ZStack {
            RoutePlannerRouteStatusView(
                strings: strings,
                error: uiState.routePlanError,
                routeStats: uiState.routeStats,
                isPlanExpected: uiState.isRoutePlanExpected,
                onRetryClicked: { onEvent(RoutePlannerUiEventsRetryClicked.shared) }
            )
            .padding(.horizontal, 16)
            .opacity(uiState.isRoutePlanLoading ? 0 : 1)
            .allowsHitTesting(!uiState.isRoutePlanLoading)
            .accessibilityHidden(uiState.isRoutePlanLoading)
            if uiState.isRoutePlanLoading {
                RoutePlannerLoadingView(strings: strings)
                    .transition(.opacity)
            }
        }
    }

    private var actionsRow: some View {
        HStack(spacing: 12) {
            SecondaryButton(
                icon: .system("arrow.clockwise"),
                title: strings.get(id: SharedRes.strings().round_trip_wizard_regenerate),
                iconPlacement: .inline,
                action: { onEvent(RoutePlannerUiEventsRoundTripRegenerateClicked.shared) }
            )
            .disabled(!uiState.isRegenerateEnabled)
            .opacity(uiState.isRegenerateEnabled ? 1 : 0.5)
            .accessibilityIdentifier(TestTags.shared.ROUND_TRIP_WIZARD_REGENERATE_BUTTON)
            PrimaryButton(
                icon: .system("square.and.arrow.down.fill"),
                title: strings.get(id: SharedRes.strings().round_trip_wizard_save),
                iconPlacement: .inline,
                action: { onEvent(RoutePlannerUiEventsSaveRouteClicked.shared) }
            )
            .disabled(!uiState.isSaveEnabled)
            .opacity(uiState.isSaveEnabled ? 1 : 0.5)
            .accessibilityIdentifier(TestTags.shared.ROUND_TRIP_WIZARD_SAVE_BUTTON)
        }
        .padding(.horizontal, 16)
    }

    private func heightReader(isExpanded: Bool, onHeight: @escaping (CGFloat) -> Void) -> some View {
        contentStack(isExpanded: isExpanded)
            .hidden()
            .accessibilityHidden(true)
            .transaction { $0.animation = nil }
            .onGeometryChange(for: CGFloat.self) { $0.size.height } action: { height in
                onHeight(height)
            }
    }
}
