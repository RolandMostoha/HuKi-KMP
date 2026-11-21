import Shared
import SwiftUI

extension MainView {
    /// Keeps a strip of the map visible above the expanded planner, except in landscape where the height is scarce.
    var routePlannerMaxExpandedHeight: CGFloat {
        guard screenHeight > 0 else { return .infinity }
        return screenHeight - topSafeAreaInset - (isLandscape ? 0 : Dimens.routePlannerMapPeekHeight)
    }

    var isRoutePlannerAtMaxHeight: Bool {
        routePlannerDetent == .expanded && routePlannerHeights.expanded >= routePlannerMaxExpandedHeight - 1
    }

    func routePlannerSheet(
        stops: [RoutePlannerWaypoint],
        restored: RoutePlannerSession?,
        mode: RoutePlannerMode
    ) -> some View {
        RoutePlannerSheetView(
            strings: strings,
            stops: stops,
            restored: restored,
            mode: mode,
            mapEvent: routePlannerMapEvent,
            onRoutePlanUpdated: { routePlan, markers, isAwaiting, isLoading in
                onRoutePlanUpdated(routePlan, markers, isAwaiting: isAwaiting, isLoading: isLoading, mode: mode)
            },
            onSessionChanged: { session in
                viewModel.onEvent(event: MainUiEventsRoutePlannerStateChanged(session: session))
            },
            onPickOnMapChanged: { isPickingOnMap in
                viewModel.onEvent(event: MainUiEventsRoutePlannerPickOnMapChanged(isPickingOnMap: isPickingOnMap))
            },
            onMoveStopChanged: { isMovingStop in
                viewModel.onEvent(event: MainUiEventsRoutePlannerMoveStopChanged(isMovingStop: isMovingStop))
            },
            onSelectedStopChanged: { location in
                viewModel.onEvent(event: MainUiEventsRoutePlannerSelectedStopChanged(location: location))
            },
            onRoutePlanSaved: { fileUri in
                viewModel.onEvent(event: GpxUiEventsRoutePlanGpxSaved(uri: fileUri))
            },
            onDismissRequest: {
                viewModel.onEvent(event: MainUiEventsSheetDismissed())
            },
            detent: routePlannerDetent,
            maxExpandedHeight: routePlannerMaxExpandedHeight,
            onDetentChange: { routePlannerDetent = $0 },
            onHeightsChange: { heights in
                routePlannerHeights = heights
            },
            onMinimizeRequest: { routePlannerDetent = .minimized },
            onInitialAppear: { routePlannerDetent = .expanded },
            onSheetFrameChange: { frame in routePlannerSheetFrame.value = frame }
        )
        .presentationDetents(routePlannerDetents, selection: routePlannerDetentSelection)
        .presentationDragIndicator(.visible)
        .presentationBackgroundInteraction(.enabled)
        .interactiveDismissDisabled()
        .background(RoutePlannerDetentInvalidator(heights: routePlannerHeights))
        .onDisappear { routePlannerSheetFrame.value = nil }
    }

    // The mode lets the map drop a late plan from the closing sheet of the other mode
    private func onRoutePlanUpdated(
        _ routePlan: RoutePlan?,
        _ markers: [GpxWaypoint],
        isAwaiting: Bool,
        isLoading: Bool,
        mode: RoutePlannerMode
    ) {
        viewModel.onEvent(
            event: MainUiEventsRoutePlanUpdated(
                routePlan: routePlan,
                markers: markers,
                isAwaitingRoutePlan: isAwaiting,
                isRoutePlanLoading: isLoading,
                mode: mode
            )
        )
    }

    func routePlannerHint(uiState: MainUiState) -> some View {
        RoutePlannerHintOverlay(
            strings: strings,
            sheetFrame: routePlannerSheetFrame,
            hint: isRoutePlannerAtMaxHeight ? nil : uiState.visibleRoutePlannerHint,
            onDismiss: {
                viewModel.onEvent(event: MainUiEventsRoutePlannerHintDismissed.shared)
            }
        )
    }

    private var routePlannerDetents: Set<PresentationDetent> {
        [
            presentationDetent(for: .minimized),
            presentationDetent(for: .expanded)
        ]
    }

    private func presentationDetent(for detent: RoutePlannerDetent) -> PresentationDetent {
        switch detent {
        case .minimized:
            return .custom(RoutePlannerMinimizedDetent.self)
        case .expanded:
            return .custom(RoutePlannerExpandedDetent.self)
        }
    }

    private var routePlannerDetentSelection: Binding<PresentationDetent> {
        Binding(
            get: { presentationDetent(for: routePlannerDetent) },
            set: { newValue in
                routePlannerDetent = newValue == .custom(RoutePlannerMinimizedDetent.self) ? .minimized : .expanded
            }
        )
    }
}

// Stable detent identities that resize in place, since a changed .height(_) detent makes the sheet reselect and jump.
// The detent context cannot see the view's state, so the heights are handed over through this main-thread-only holder.
private enum RoutePlannerDetentHeights {
    nonisolated(unsafe) static var current = RoutePlannerSheetHeights.initial
}

private struct RoutePlannerMinimizedDetent: CustomPresentationDetent {
    static func height(in context: Context) -> CGFloat? {
        RoutePlannerDetentHeights.current.minimized
    }
}

private struct RoutePlannerExpandedDetent: CustomPresentationDetent {
    static func height(in context: Context) -> CGFloat? {
        min(RoutePlannerDetentHeights.current.expanded, context.maxDetentValue)
    }
}

// Custom detents resolve their height once, so the sheet is told to re-resolve them whenever the heights change
private struct RoutePlannerDetentInvalidator: UIViewRepresentable {
    let heights: RoutePlannerSheetHeights

    func makeUIView(context: Context) -> InvalidatingView {
        InvalidatingView()
    }

    func updateUIView(_ uiView: InvalidatingView, context: Context) {
        RoutePlannerDetentHeights.current = heights
        guard uiView.appliedHeights != heights else { return }
        uiView.invalidateDetents(for: heights)
    }

    final class InvalidatingView: UIView {
        private(set) var appliedHeights: RoutePlannerSheetHeights?

        // The first heights can arrive before the view joins the sheet, so they are applied once it does
        override func didMoveToWindow() {
            super.didMoveToWindow()
            guard window != nil else { return }
            invalidateDetents(for: RoutePlannerDetentHeights.current)
        }

        func invalidateDetents(for heights: RoutePlannerSheetHeights) {
            guard let sheet = sheetPresentationController else { return }
            appliedHeights = heights
            sheet.animateChanges { sheet.invalidateDetents() }
        }
    }
}

private extension UIView {
    var sheetPresentationController: UISheetPresentationController? {
        let controller = sequence(first: self as UIResponder, next: \.next)
            .compactMap { $0 as? UIViewController }
            .first
        return sequence(first: controller, next: { $0?.parent })
            .compactMap { $0?.sheetPresentationController }
            .first
    }
}

/// A reference holder, so a moving sheet re-renders only the views that read it, never MainView and its sheet.
@Observable
final class RoutePlannerSheetFrame {
    var value: CGRect?
}

private struct RoutePlannerHintOverlay: View {
    let strings: Strings
    let sheetFrame: RoutePlannerSheetFrame
    let hint: RoutePlannerHint?
    let onDismiss: () -> Void

    @State private var isSheetSettled = false

    private let sheetSpacing: CGFloat = 10
    private let settleDelay: Duration = .milliseconds(400)

    var body: some View {
        // A GeometryReader never outgrows its proposal, so the padding cannot feed back into the layout
        GeometryReader { proxy in
            let frame = proxy.frame(in: .global)
            let sheet = sheetFrame.value ?? frame
            ZStack {
                if let hint, isSheetSettled {
                    // Spans exactly the sheet's width, so it reads as part of the floating sheet
                    RoutePlannerHintView(strings: strings, hint: hint, onDismiss: onDismiss)
                        .padding(.leading, max(0, sheet.minX - frame.minX))
                        .padding(.trailing, max(0, frame.maxX - sheet.maxX))
                        .padding(.bottom, max(0, frame.maxY - sheet.minY) + sheetSpacing)
                        .transition(.move(edge: .bottom).combined(with: .opacity))
                }
            }
            .frame(width: proxy.size.width, height: proxy.size.height, alignment: .bottom)
        }
        .ignoresSafeArea(.container, edges: [.horizontal, .bottom])
        .animation(.easeInOut(duration: 0.3), value: isSheetSettled ? hint : nil)
        .task(id: SettleKey(hint: hint, sheetFrame: sheetFrame.value)) {
            await settle()
        }
    }

    // The sheet reports its final frame while it still animates, so the hint hides until it stops moving
    private func settle() async {
        isSheetSettled = false
        guard hint != nil, sheetFrame.value != nil else { return }
        do {
            try await Task.sleep(for: settleDelay)
            isSheetSettled = true
        } catch {
            return
        }
    }
}

private struct SettleKey: Equatable {
    let hint: RoutePlannerHint?
    let sheetFrame: CGRect?
}
