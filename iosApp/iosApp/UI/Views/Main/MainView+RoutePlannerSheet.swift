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
            onInitialAppear: { routePlannerDetent = .expanded },
            onSheetTopChange: { top in routePlannerSheetTop.value = top }
        )
        .presentationDetents(routePlannerDetents, selection: routePlannerDetentSelection)
        .presentationDragIndicator(.visible)
        .presentationBackgroundInteraction(.enabled)
        .interactiveDismissDisabled()
        .onDisappear { routePlannerSheetTop.value = nil }
    }

    func routePlannerLongPressHint(uiState: MainUiState) -> some View {
        RoutePlannerLongPressHintOverlay(
            strings: strings,
            sheetTop: routePlannerSheetTop,
            isVisible: uiState.isRoutePlannerLongPressHintVisible && routePlannerDetent != .fullScreen,
            onDismiss: {
                viewModel.onEvent(event: MainUiEventsRoutePlannerLongPressHintDismissed.shared)
            }
        )
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

/// A reference holder, so a moving sheet re-renders only the views that read it, never MainView and its sheet.
@Observable
final class RoutePlannerSheetTop {
    var value: CGFloat?
}

private struct RoutePlannerLongPressHintOverlay: View {
    let strings: Strings
    let sheetTop: RoutePlannerSheetTop
    let isVisible: Bool
    let onDismiss: () -> Void

    @State private var isSheetSettled = false

    private let sheetSpacing: CGFloat = 10
    private let settleDelay: Duration = .milliseconds(400)

    var body: some View {
        // A GeometryReader never outgrows its proposal, so the padding cannot feed back into the layout
        GeometryReader { proxy in
            let sheetHeight = sheetTop.value.map { max(0, proxy.frame(in: .global).maxY - $0) } ?? 0
            ZStack {
                if isVisible && isSheetSettled {
                    RoutePlannerLongPressHintView(strings: strings, onDismiss: onDismiss)
                        .padding(.horizontal, 16)
                        .padding(.bottom, sheetHeight + sheetSpacing)
                        .transition(.move(edge: .bottom).combined(with: .opacity))
                }
            }
            .frame(width: proxy.size.width, height: proxy.size.height, alignment: .bottom)
        }
        .safeAreaPadding(.horizontal)
        .ignoresSafeArea(.container, edges: .bottom)
        .animation(.easeInOut(duration: 0.3), value: isVisible && isSheetSettled)
        .task(id: SettleKey(isVisible: isVisible, sheetTop: sheetTop.value)) {
            await settle()
        }
    }

    // The sheet reports its final frame while it still animates, so the hint hides until it stops moving
    private func settle() async {
        isSheetSettled = false
        guard isVisible, sheetTop.value != nil else { return }
        do {
            try await Task.sleep(for: settleDelay)
            isSheetSettled = true
        } catch {
            return
        }
    }
}

private struct SettleKey: Equatable {
    let isVisible: Bool
    let sheetTop: CGFloat?
}
