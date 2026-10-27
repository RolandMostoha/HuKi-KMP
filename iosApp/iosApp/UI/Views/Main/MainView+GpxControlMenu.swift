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
                    viewModel.onEvent(event: GpxUiEventsGpxRouteVisibilityToggled())
                },
                onToggleDistances: {
                    viewModel.onEvent(event: GpxUiEventsGpxDistancesVisibilityToggled())
                },
                onToggleSlopeColors: {
                    viewModel.onEvent(event: GpxUiEventsGpxSlopeColorsToggled())
                },
                onReverseDirection: {
                    viewModel.onEvent(event: GpxUiEventsGpxDirectionReversed())
                },
                onOverview: {
                    viewModel.onEvent(event: GpxUiEventsGpxOverviewClicked())
                },
                onClear: {
                    viewModel.onEvent(event: GpxUiEventsGpxCloseClicked())
                }
            )
            .safeAreaPadding(.horizontal)
            .padding(.bottom, isLandscape ? 12 : 0)
            .ignoresSafeArea(.container, edges: isLandscape ? .bottom : [])
            .transition(.opacity)
        }
    }
}
