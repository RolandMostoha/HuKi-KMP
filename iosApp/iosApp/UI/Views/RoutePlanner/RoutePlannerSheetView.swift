import Shared
import SwiftUI

/// A map interaction for the planner, identified so that long-tapping the same point twice still adds two stops.
struct RoutePlannerMapEvent: Equatable {
    let id = UUID()
    let event: RoutePlannerUiEvents

    static func == (lhs: RoutePlannerMapEvent, rhs: RoutePlannerMapEvent) -> Bool {
        lhs.id == rhs.id
    }
}

struct RoutePlannerSheetHeights: Equatable {
    let expanded: CGFloat
    let minimized: CGFloat

    static let initial = RoutePlannerSheetHeights(
        expanded: Dimens.routePlannerDetentHeight,
        minimized: Dimens.routePlannerMinimizedDetentHeight
    )
}

enum RoutePlannerDetent {
    /// Keeps only the title, stats and save button
    case minimized
    /// Shows all content but keeps only a few stops
    case expanded
    /// Shows all stops and all content
    case fullScreen

    /// The header button switches between the map, where the sheet is minimized, and the stop list
    var toggled: RoutePlannerDetent {
        self == .minimized ? .expanded : .minimized
    }
}

struct RoutePlannerSheetView: View {
    let strings: Strings
    let mapEvent: RoutePlannerMapEvent?
    let onRoutePlanUpdated: (RoutePlan?, [GpxWaypoint], _ isAwaiting: Bool, _ isLoading: Bool) -> Void
    let onSessionChanged: (RoutePlannerSession) -> Void
    let onPickOnMapChanged: (Bool) -> Void
    let onMoveStopChanged: (Bool) -> Void
    let onSelectedStopChanged: (Location?) -> Void
    let onRoutePlanSaved: (String) -> Void
    let onDismissRequest: () -> Void
    let detent: RoutePlannerDetent
    let onExpandRequest: () -> Void
    let onDetentChange: (RoutePlannerDetent) -> Void
    let onHeightsChange: (RoutePlannerSheetHeights) -> Void
    let onMinimizeRequest: () -> Void
    let onInitialAppear: () -> Void
    let onSheetFrameChange: (CGRect) -> Void

    @StateObject private var holder: ViewModelHolder<RoutePlannerViewModel>
    @State private var didAppear = false
    @State private var showsSaveError = false

    private var viewModel: RoutePlannerViewModel { holder.viewModel }

    init(
        strings: Strings,
        stops: [RoutePlannerWaypoint],
        restored: RoutePlannerSession?,
        mapEvent: RoutePlannerMapEvent?,
        onRoutePlanUpdated: @escaping (RoutePlan?, [GpxWaypoint], _ isAwaiting: Bool, _ isLoading: Bool) -> Void,
        onSessionChanged: @escaping (RoutePlannerSession) -> Void,
        onPickOnMapChanged: @escaping (Bool) -> Void = { _ in },
        onMoveStopChanged: @escaping (Bool) -> Void = { _ in },
        onSelectedStopChanged: @escaping (Location?) -> Void = { _ in },
        onRoutePlanSaved: @escaping (String) -> Void,
        onDismissRequest: @escaping () -> Void,
        detent: RoutePlannerDetent = .expanded,
        onExpandRequest: @escaping () -> Void = {},
        onDetentChange: @escaping (RoutePlannerDetent) -> Void = { _ in },
        onHeightsChange: @escaping (RoutePlannerSheetHeights) -> Void = { _ in },
        onMinimizeRequest: @escaping () -> Void = {},
        onInitialAppear: @escaping () -> Void = {},
        onSheetFrameChange: @escaping (CGRect) -> Void = { _ in }
    ) {
        self.strings = strings
        self.mapEvent = mapEvent
        self.onRoutePlanUpdated = onRoutePlanUpdated
        self.onSessionChanged = onSessionChanged
        self.onPickOnMapChanged = onPickOnMapChanged
        self.onMoveStopChanged = onMoveStopChanged
        self.onSelectedStopChanged = onSelectedStopChanged
        self.onRoutePlanSaved = onRoutePlanSaved
        self.onDismissRequest = onDismissRequest
        self.detent = detent
        self.onExpandRequest = onExpandRequest
        self.onDetentChange = onDetentChange
        self.onHeightsChange = onHeightsChange
        self.onMinimizeRequest = onMinimizeRequest
        self.onInitialAppear = onInitialAppear
        self.onSheetFrameChange = onSheetFrameChange
        _holder = StateObject(wrappedValue: ViewModelHolder { owner in
            KoinViewModelProvider.shared.getRoutePlannerViewModel(owner: owner, stops: stops, restored: restored)
        })
    }

    var body: some View {
        // The effect stream and the one-shot seeding must not restart on every uiState change,
        // so they sit outside the Observing body that SwiftUI rebuilds.
        plannerContent
            .onGeometryChange(for: CGRect.self) { $0.frame(in: .global) } action: { frame in
                onSheetFrameChange(frame)
            }
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
            .onChange(of: mapEvent) { _, newMapEvent in
                if let newMapEvent { viewModel.onEvent(event: newMapEvent.event) }
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
                onDetentChange: onDetentChange,
                onHeightsChange: onHeightsChange
            )
            .onChange(of: uiState.routePlanMarkers, initial: true) { notifyRoutePlanUpdated(uiState) }
            .onChange(of: uiState.routePlan) { notifyRoutePlanUpdated(uiState) }
            .onChange(of: uiState.isAwaitingRoutePlan) { notifyRoutePlanUpdated(uiState) }
            .onChange(of: uiState.isRoutePlanLoading) { notifyRoutePlanUpdated(uiState) }
            .onChange(of: uiState.session, initial: true) { onSessionChanged(uiState.session) }
            .onChange(of: uiState.isPickingOnMap, initial: true) { onPickOnMapChanged(uiState.isPickingOnMap) }
            .onChange(of: uiState.isMovingStop, initial: true) { onMoveStopChanged(uiState.isMovingStop) }
            .onChange(of: uiState.selectedStop?.location, initial: true) {
                onSelectedStopChanged(uiState.selectedStop?.location)
            }
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
            .sheet(isPresented: renameBinding(uiState: uiState)) {
                if let stop = uiState.renamingStop {
                    renameSheet(stop: stop, uiState: uiState)
                }
            }
        }
    }

    private func renameSheet(stop: RoutePlannerWaypoint, uiState: RoutePlannerUiState) -> some View {
        let resolvedName = stop.name.map { strings.get(desc: $0) } ?? ""
        return RoutePlannerRenameStopView(
            strings: strings,
            stopNumber: (uiState.stops.firstIndex { $0.id == stop.id } ?? 0) + 1,
            resolvedName: resolvedName,
            currentName: stop.customName ?? resolvedName,
            currentComment: stop.comment ?? "",
            address: stop.placeName,
            onDone: { name, comment in
                // Keeping the resolved name as typed is no rename, so the stop follows later geocoding
                let customName = name == resolvedName ? "" : name
                viewModel.onEvent(
                    event: RoutePlannerUiEventsStopRenamed(waypointId: stop.id, name: customName, comment: comment)
                )
            },
            onCancel: { viewModel.onEvent(event: RoutePlannerUiEventsStopRenameDismissed.shared) }
        )
    }

    private func notifyRoutePlanUpdated(_ uiState: RoutePlannerUiState) {
        onRoutePlanUpdated(
            uiState.routePlan,
            uiState.routePlanMarkers,
            uiState.isAwaitingRoutePlan,
            uiState.isRoutePlanLoading
        )
    }

    private func renameBinding(uiState: RoutePlannerUiState) -> Binding<Bool> {
        Binding(
            get: { uiState.renamingStop != nil },
            set: { isVisible in
                guard !isVisible else { return }
                viewModel.onEvent(event: RoutePlannerUiEventsStopRenameDismissed.shared)
            }
        )
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
        case .close:
            onDismissRequest()
        case .openUrl(let effect):
            if let url = URL(string: effect.url) {
                UIApplication.shared.open(url)
            }
        }
    }
}
