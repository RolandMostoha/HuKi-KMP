import Shared
import SwiftUI

extension MainView {
    func routePlannerSheet(stops: [RoutePlannerWaypoint], restored: RoutePlannerSession?) -> some View {
        RoutePlannerSheetView(
            strings: strings,
            stops: stops,
            restored: restored,
            pick: routePlannerPick,
            onRoutePlanUpdated: { routePlan, markers, isAwaitingRoutePlan in
                viewModel.onEvent(
                    event: MainUiEventsRoutePlanUpdated(
                        routePlan: routePlan,
                        markers: markers,
                        isAwaitingRoutePlan: isAwaitingRoutePlan
                    )
                )
            },
            onSessionChanged: { session in
                viewModel.onEvent(event: MainUiEventsRoutePlannerStateChanged(session: session))
            },
            onRoutePlanSaved: { fileUri in
                viewModel.onEvent(event: GpxUiEventsRoutePlanGpxSaved(uri: fileUri))
            },
            onDismissRequest: {
                viewModel.onEvent(event: MainUiEventsSheetDismissed())
            },
            detent: routePlannerDetent,
            onExpandRequest: { routePlannerDetent = .fullScreen },
            onHeightsChange: { heights in
                routePlannerHeights = heights
            },
            onMinimizeRequest: { routePlannerDetent = .minimized },
            onExpandAfterPickRequest: { routePlannerDetent = .expanded },
            onInitialAppear: { routePlannerDetent = .expanded }
        )
        .presentationDetents(routePlannerDetents, selection: routePlannerDetentSelection)
        .presentationDragIndicator(.visible)
        .presentationBackgroundInteraction(.enabled)
        .interactiveDismissDisabled()
    }

    private var routePlannerDetents: Set<PresentationDetent> {
        [
            presentationDetent(for: .minimized),
            presentationDetent(for: .expanded),
            presentationDetent(for: .fullScreen)
        ]
    }

    private func presentationDetent(for detent: RoutePlannerDetent) -> PresentationDetent {
        switch detent {
        case .minimized:
            return .height(routePlannerHeights.minimized)
        case .expanded:
            return .height(routePlannerHeights.expanded)
        case .fullScreen:
            return .large
        }
    }

    private var routePlannerDetentSelection: Binding<PresentationDetent> {
        Binding(
            get: { presentationDetent(for: routePlannerDetent) },
            set: { newValue in
                if newValue == .large {
                    routePlannerDetent = .fullScreen
                } else if newValue == .height(routePlannerHeights.minimized) {
                    routePlannerDetent = .minimized
                } else {
                    routePlannerDetent = .expanded
                }
            }
        )
    }
}
