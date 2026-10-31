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
    let pick: RoutePlannerPick?
    let onRoutePlanUpdated: (RoutePlan?, [GpxWaypoint], Bool) -> Void
    let onSessionChanged: (RoutePlannerSession) -> Void
    let onRoutePlanSaved: (String) -> Void
    let onDismissRequest: () -> Void
    let detent: RoutePlannerDetent
    let onExpandRequest: () -> Void
    let onHeightsChange: (RoutePlannerSheetHeights) -> Void
    let onMinimizeRequest: () -> Void
    let onExpandAfterPickRequest: () -> Void
    let onInitialAppear: () -> Void

    @StateObject private var holder: ViewModelHolder<RoutePlannerViewModel>
    @State private var didAppear = false
    @State private var showsSaveError = false

    private var viewModel: RoutePlannerViewModel { holder.viewModel }

    init(
        strings: Strings,
        stops: [RoutePlannerWaypoint],
        restored: RoutePlannerSession?,
        pick: RoutePlannerPick?,
        onRoutePlanUpdated: @escaping (RoutePlan?, [GpxWaypoint], Bool) -> Void,
        onSessionChanged: @escaping (RoutePlannerSession) -> Void,
        onRoutePlanSaved: @escaping (String) -> Void,
        onDismissRequest: @escaping () -> Void,
        detent: RoutePlannerDetent = .expanded,
        onExpandRequest: @escaping () -> Void = {},
        onHeightsChange: @escaping (RoutePlannerSheetHeights) -> Void = { _ in },
        onMinimizeRequest: @escaping () -> Void = {},
        onExpandAfterPickRequest: @escaping () -> Void = {},
        onInitialAppear: @escaping () -> Void = {}
    ) {
        self.strings = strings
        self.pick = pick
        self.onRoutePlanUpdated = onRoutePlanUpdated
        self.onSessionChanged = onSessionChanged
        self.onRoutePlanSaved = onRoutePlanSaved
        self.onDismissRequest = onDismissRequest
        self.detent = detent
        self.onExpandRequest = onExpandRequest
        self.onHeightsChange = onHeightsChange
        self.onMinimizeRequest = onMinimizeRequest
        self.onExpandAfterPickRequest = onExpandAfterPickRequest
        self.onInitialAppear = onInitialAppear
        _holder = StateObject(wrappedValue: ViewModelHolder { owner in
            KoinViewModelProvider.shared.getRoutePlannerViewModel(owner: owner, stops: stops, restored: restored)
        })
    }

    var body: some View {
        // The effect stream and the one-shot seeding must not restart on every uiState change,
        // so they sit outside the Observing body that SwiftUI rebuilds.
        plannerContent
            .onAppear {
                guard !didAppear else { return }
                didAppear = true
                onInitialAppear()
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
            .onChange(of: uiState.routePlanMarkers, initial: true) { notifyRoutePlanUpdated(uiState) }
            .onChange(of: uiState.routePlan) { notifyRoutePlanUpdated(uiState) }
            .onChange(of: uiState.isAwaitingRoutePlan) { notifyRoutePlanUpdated(uiState) }
            .onChange(of: uiState.session, initial: true) { onSessionChanged(uiState.session) }
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

    private func notifyRoutePlanUpdated(_ uiState: RoutePlannerUiState) {
        onRoutePlanUpdated(uiState.routePlan, uiState.routePlanMarkers, uiState.isAwaitingRoutePlan)
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
