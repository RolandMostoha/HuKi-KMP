import Shared
import SwiftUI

struct SearchSheetView: View {
    let strings: Strings
    let onDismiss: () -> Void
    let onPlaceSelected: (Place) -> Void
    let onRecentPlaceSelected: (Place) -> Void
    let onSearchPlaceHistorySelected: (Place) -> Void
    let onDestinationSelected: (Destination) -> Void
    let onSearchDestinationSelected: (Destination) -> Void
    let onGpxFileSelected: (String) -> Void
    let onSeeAllGpxClicked: () -> Void
    let onSeeAllPlacesClicked: () -> Void
    let onSeeAllDestinationsClicked: () -> Void
    let onLocationIqClicked: () -> Void

    @State private var viewModel = KoinViewModelProvider.shared.getPlaceFinderViewModel()
    @State private var headerHeight: CGFloat = 80
    @FocusState private var isSearchFieldFocused: Bool

    var body: some View {
        Observing(viewModel.uiState) { uiState in
            ZStack(alignment: .top) {
                content(uiState: uiState)
                    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
                header(uiState: uiState)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
            .background(Color(.systemGray6))
            .onAppear {
                isSearchFieldFocused = true
            }
            .onDisappear {
                viewModel.clear()
            }
        }
    }

    @ViewBuilder
    private func content(uiState: PlaceFinderUiState) -> some View {
        let hasLocalMatches = !uiState.searchRecentPlaces.isEmpty || !uiState.searchDestinations.isEmpty
        let hasOnlineActivity = uiState.isLoading || uiState.error != nil
        let hasAnyResults = hasLocalMatches || !uiState.places.isEmpty
        let isSearching = !uiState.searchText.isEmpty
        if isSearching && (hasAnyResults || hasOnlineActivity) {
            groupedResults(uiState: uiState)
        } else if !isSearching &&
            (!uiState.destinations.isEmpty || !uiState.recentPlaces.isEmpty || !uiState.recentGpxFiles.isEmpty) {
            discoveryList(uiState: uiState)
        } else {
            Color.clear
        }
    }

    @ViewBuilder
    private func groupedResults(uiState: PlaceFinderUiState) -> some View {
        ScrollView {
            VStack(spacing: 0) {
                if !uiState.searchRecentPlaces.isEmpty {
                    RecentPlacesSectionView(
                        strings: strings,
                        places: uiState.searchRecentPlaces,
                        onPlaceSelected: { place in
                            isSearchFieldFocused = false
                            onSearchPlaceHistorySelected(place)
                        }
                    )
                }
                if !uiState.searchDestinations.isEmpty {
                    DestinationsSectionView(
                        strings: strings,
                        destinations: uiState.searchDestinations,
                        onDestinationSelected: { destination in
                            isSearchFieldFocused = false
                            onSearchDestinationSelected(destination)
                        }
                    )
                }
                OnlineResultsSectionView(
                    strings: strings,
                    places: uiState.places,
                    error: uiState.error,
                    isLoading: uiState.isLoading,
                    onPlaceSelected: { place in
                        isSearchFieldFocused = false
                        onPlaceSelected(place)
                    },
                    onRetryClicked: {
                        viewModel.onEvent(event: PlaceFinderUiEventsRetryClicked.shared)
                    },
                    onLocationIqClicked: onLocationIqClicked
                )
            }
            .padding(.bottom, 24)
        }
        .contentMargins(.top, headerHeight, for: .scrollContent)
        .scrollDismissesKeyboard(.immediately)
    }

    @ViewBuilder
    private func discoveryList(uiState: PlaceFinderUiState) -> some View {
        ScrollView {
            VStack(spacing: 0) {
                if !uiState.recentPlaces.isEmpty {
                    RecentPlacesSectionView(
                        strings: strings,
                        places: uiState.recentPlaces,
                        onPlaceSelected: { place in
                            isSearchFieldFocused = false
                            onRecentPlaceSelected(place)
                        },
                        onSeeAllClicked: {
                            isSearchFieldFocused = false
                            onSeeAllPlacesClicked()
                        }
                    )
                }
                if !uiState.recentGpxFiles.isEmpty {
                    RecentGpxSectionView(
                        strings: strings,
                        files: uiState.recentGpxFiles,
                        onFileSelected: { file in
                            isSearchFieldFocused = false
                            onGpxFileSelected(file.fileUri)
                        },
                        onSeeAllClicked: {
                            isSearchFieldFocused = false
                            onSeeAllGpxClicked()
                        }
                    )
                }
                if !uiState.destinations.isEmpty {
                    DestinationsSectionView(
                        strings: strings,
                        destinations: uiState.destinations,
                        onDestinationSelected: { destination in
                            isSearchFieldFocused = false
                            onDestinationSelected(destination)
                        },
                        title: strings.get(id: uiState.destinationsTitle),
                        onSeeAllClicked: {
                            isSearchFieldFocused = false
                            onSeeAllDestinationsClicked()
                        }
                    )
                }
            }
            .padding(.bottom, 24)
        }
        .contentMargins(.top, headerHeight, for: .scrollContent)
        .scrollDismissesKeyboard(.immediately)
    }

    @ViewBuilder
    private func header(uiState: PlaceFinderUiState) -> some View {
        searchBar(uiState: uiState)
        .padding(.bottom, 8)
        .background(
            GeometryReader { geometry in
                Color.clear
                    .onAppear { headerHeight = geometry.size.height }
                    .onChange(of: geometry.size.height) { _, newValue in
                        headerHeight = newValue
                    }
            }
        )
    }

}

private extension SearchSheetView {
    @ViewBuilder
    func searchBar(uiState: PlaceFinderUiState) -> some View {
        HStack {
            searchTextField(uiState: uiState)
            closeButton
        }
        .padding(.top, 15)
        .padding(.horizontal, 18)
    }

    @ViewBuilder
    private func searchTextField(uiState: PlaceFinderUiState) -> some View {
        SearchFieldView(
            strings: strings,
            text: Binding<String>(
                get: { uiState.searchText },
                set: { viewModel.onEvent(event: PlaceFinderUiEventsSearchTextChanged(searchText: $0)) }
            ),
            isFocused: $isSearchFieldFocused
        )
    }

    @ViewBuilder
    private var closeButton: some View {
        CloseButton(action: {
            isSearchFieldFocused = false
            viewModel.onEvent(event: PlaceFinderUiEventsSearchTextChanged(searchText: ""))
            onDismiss()
        })
    }
}
