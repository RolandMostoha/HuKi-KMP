import Shared
import SwiftUI

extension MainView {
    @ViewBuilder
    func gpxControlMenu(uiState: MainUiState) -> some View {
        if uiState.sheet == nil, uiState.mapUiState.gpxDetails != nil, uiState.mapUiState.gpxLayerVisible {
            GpxControlMenu(
                strings: strings,
                isRouteVisible: uiState.mapUiState.gpxRouteVisible,
                isAllDistancesVisible: uiState.mapUiState.allDistancesVisible,
                isSlopeColorsAvailable: uiState.mapUiState.gpxDetails?.hasSlopeSegments ?? false,
                isSlopeColorsVisible: uiState.mapUiState.gpxSlopeColorsVisible,
                isReversible: uiState.mapUiState.gpxDetails?.isReversible ?? false,
                onToggleLine: {
                    viewModel.onEvent(event: MainUiEventsGpxRouteVisibilityToggled())
                },
                onToggleDistances: {
                    viewModel.onEvent(event: MainUiEventsGpxDistancesVisibilityToggled())
                },
                onToggleSlopeColors: {
                    viewModel.onEvent(event: MainUiEventsGpxSlopeColorsToggled())
                },
                onReverseDirection: {
                    viewModel.onEvent(event: MainUiEventsGpxDirectionReversed())
                },
                onOverview: {
                    viewModel.onEvent(event: MainUiEventsGpxOverviewClicked())
                },
                onClear: {
                    viewModel.onEvent(event: MainUiEventsGpxCloseClicked())
                }
            )
            .safeAreaPadding(.horizontal)
            .padding(.bottom, isLandscape ? 12 : 0)
            .ignoresSafeArea(.container, edges: isLandscape ? .bottom : [])
            .transition(.opacity)
        }
    }
}
