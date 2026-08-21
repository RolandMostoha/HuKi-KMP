import Shared
import SwiftUI

/// A map location picked for the planner, identified so that repeated picks of the same point still register.
struct RoutePlannerPick: Equatable {
    let id = UUID()
    let location: Shared.Location

    static func == (lhs: RoutePlannerPick, rhs: RoutePlannerPick) -> Bool {
        lhs.id == rhs.id
    }
}

struct RoutePlannerSheetHeights: Equatable {
    let expanded: CGFloat
    let minimized: CGFloat
}

enum RoutePlannerDetent {
    /// Keeps only the title, stats and save button
    case minimized
    /// Shows all content but keeps only a few stops
    case expanded
    /// Shows all stops and all content
    case fullScreen
}

struct RoutePlannerSheetView: View {
    let strings: Strings
    let viewModel: RoutePlannerViewModel
    let place: Place?
    let pick: RoutePlannerPick?
    let onRoutePlanUpdated: (RoutePlan?, [GpxWaypoint]) -> Void
    let onRoutePlanSaved: (String) -> Void
    let onDismissRequest: () -> Void
    var detent: RoutePlannerDetent = .expanded
    var onExpandRequest: () -> Void = {}
    var onHeightsChange: (RoutePlannerSheetHeights) -> Void = { _ in }
    var onMinimizeRequest: () -> Void = {}
    var onExpandAfterPickRequest: () -> Void = {}
    var onInitialAppear: () -> Void = {}

    @State private var didAppear = false
    @State private var showsSaveError = false

    var body: some View {
        // The effect stream and the one-shot seeding must not restart on every uiState change,
        // so they sit outside the Observing body that SwiftUI rebuilds.
        plannerContent
            .onAppear {
                guard !didAppear else { return }
                didAppear = true
                onInitialAppear()
                if let place {
                    viewModel.onEvent(event: RoutePlannerUiEventsPlaceAdded(place: place))
                }
            }
            .task {
                for await effect in viewModel.uiEffects {
                    handleEffect(effect)
                }
            }
            .onChange(of: pick) { _, newPick in
                guard let newPick else { return }
                viewModel.onEvent(event: RoutePlannerUiEventsLocationAdded(location: newPick.location))
            }
    }

    private var plannerContent: some View {
        Observing(viewModel.uiState) { uiState in
            RoutePlannerContentView(
                strings: strings,
                uiState: uiState,
                onEvent: { event in
                    viewModel.onEvent(event: event)
                },
                detent: detent,
                onExpandRequest: onExpandRequest,
                onHeightsChange: onHeightsChange
            )
            .alert(
                strings.get(id: SharedRes.strings().route_planner_save_error_title),
                isPresented: $showsSaveError
            ) {
                Button(strings.get(id: SharedRes.strings().alert_ok), role: .cancel) {}
            } message: {
                Text(strings.get(id: SharedRes.strings().route_planner_save_error_message))
            }
            .sheet(isPresented: searchBinding(uiState: uiState)) {
                NavigationStack {
                    RoutePlannerSearchView(
                        strings: strings,
                        uiState: uiState,
                        onEvent: { event in
                            viewModel.onEvent(event: event)
                        }
                    )
                }
                .presentationDetents([.large])
                .presentationDragIndicator(.visible)
            }
        }
    }

    private func searchBinding(uiState: RoutePlannerUiState) -> Binding<Bool> {
        Binding(
            get: { uiState.isWaypointSearchVisible },
            set: { isVisible in
                guard !isVisible else { return }
                viewModel.onEvent(event: RoutePlannerUiEventsWaypointSearchDismissed.shared)
            }
        )
    }

    private func handleEffect(_ effect: RoutePlannerUiEffects) {
        switch onEnum(of: effect) {
        case .routePlanUpdated(let effect):
            onRoutePlanUpdated(effect.routePlan, effect.markers)
        case .routePlanSaved(let effect):
            onRoutePlanSaved(effect.fileUri)
        case .routePlanSaveFailed:
            showsSaveError = true
        case .minimizeSheet:
            onMinimizeRequest()
        case .expandSheet:
            onExpandAfterPickRequest()
        case .close:
            onDismissRequest()
        }
    }
}
