import Shared
import SwiftUI

struct SearchSheetView: View {
    let strings: Strings
    let onDismiss: () -> Void
    let onPlaceSelected: (Place) -> Void
    let onDestinationSelected: (Destination) -> Void
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
                loadingIndicator(uiState: uiState)
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
        if !uiState.places.isEmpty {
            resultsList(places: uiState.places)
        } else if let error = uiState.error {
            ScrollView {
                attributionRow
                InfoView(
                    strings: strings,
                    infoViewData: error,
                    primaryActionText: strings.get(id: SharedRes.strings().search_error_retry),
                    onPrimaryActionClick: {
                        viewModel.onEvent(event: PlaceFinderUiEventsRetryClicked.shared)
                    }
                )
                .padding(.top, 32)
                .padding(.horizontal, 16)
            }
            .contentMargins(.top, headerHeight, for: .scrollContent)
        } else if uiState.isLoading {
            ScrollView {
                attributionRow
            }
            .contentMargins(.top, headerHeight, for: .scrollContent)
        } else if uiState.searchText.isEmpty &&
            (!uiState.topDestinations.isEmpty || !uiState.recentPlaces.isEmpty || !uiState.recentGpxFiles.isEmpty) {
            discoveryList(uiState: uiState)
        } else {
            Color.clear
        }
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
                            onPlaceSelected(place)
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
                if !uiState.topDestinations.isEmpty {
                    DestinationsSectionView(
                        strings: strings,
                        destinations: uiState.topDestinations,
                        onDestinationSelected: { destination in
                            isSearchFieldFocused = false
                            onDestinationSelected(destination)
                        },
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

    @ViewBuilder
    private func loadingIndicator(uiState: PlaceFinderUiState) -> some View {
        if uiState.isLoading || !uiState.places.isEmpty || uiState.error != nil {
            ProgressView()
                .controlSize(.regular)
                .opacity(uiState.isLoading ? 1 : 0)
                .frame(height: 28)
                .frame(maxWidth: .infinity, alignment: .center)
                .padding(.top, headerHeight + 10)
                .frame(maxHeight: .infinity, alignment: .top)
                .allowsHitTesting(false)
        }
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
        let queryBinding = Binding<String>(
            get: { uiState.searchText },
            set: { viewModel.onEvent(event: PlaceFinderUiEventsSearchTextChanged(searchText: $0)) }
        )

        TextField(
            "",
            text: queryBinding,
            prompt: Text(strings.get(id: SharedRes.strings().search_input_placeholder))
                .foregroundStyle(Color(.secondaryLabel))
        )
        .font(.system(size: 18, weight: .regular))
        .foregroundStyle(.secondary)
        .submitLabel(.search)
        .textContentType(.fullStreetAddress)
        .focused($isSearchFieldFocused)
        .tint(Color(SharedRes.colors().primary.getUIColor()))
        .padding(.leading, 48)
        .padding(.trailing, uiState.searchText.isEmpty ? 16 : 44)
        .padding(.vertical, 14)
        .glassBackground()
        .overlay(alignment: .leading) {
            Image(systemName: "magnifyingglass")
                .font(.system(size: 18, weight: .semibold))
                .foregroundStyle(.primary)
                .padding(.leading, 18)
        }
        .overlay(alignment: .trailing) {
            if !uiState.searchText.isEmpty {
                clearButton
            }
        }
        .contentShape(Capsule())
        .onTapGesture {
            isSearchFieldFocused = true
        }
    }

    @ViewBuilder
    private var clearButton: some View {
        Button(action: {
            viewModel.onEvent(event: PlaceFinderUiEventsSearchTextChanged(searchText: ""))
        }, label: {
            Image(systemName: "xmark.circle.fill")
                .font(.system(size: 20))
                .foregroundStyle(.primary)
                .padding(.trailing, 14)
        })
        .buttonStyle(.plain)
        .accessibilityLabel(strings.get(id: SharedRes.strings().a11y_clear_text))
    }

    @ViewBuilder
    private var closeButton: some View {
        Button(action: {
            isSearchFieldFocused = false
            viewModel.onEvent(event: PlaceFinderUiEventsSearchTextChanged(searchText: ""))
            onDismiss()
        }, label: {
            Image(systemName: "xmark")
                .font(.system(size: 20, weight: .semibold))
                .foregroundColor(.primary)
                .padding(12)
                .glassBackground()
        })
        .accessibilityLabel(strings.get(id: SharedRes.strings().a11y_close))
    }

    @ViewBuilder
    private var attributionRow: some View {
        HStack(spacing: 8) {
            Spacer()
            Button(action: onLocationIqClicked) {
                HStack(spacing: 8) {
                    Text(strings.get(id: SharedRes.strings().search_powered_by))
                        .font(.caption)
                        .foregroundStyle(Color(.secondaryLabel))
                    Image(uiImage: SharedRes.images().ic_location_iq_logo.toUIImage()!)
                        .resizable()
                        .scaledToFit()
                        .frame(height: 16)
                }
            }
            .buttonStyle(.plain)
            .accessibilityLabel(strings.get(id: SharedRes.strings().menu_a11y_open_location_iq))
        }
        .frame(height: 28)
        .padding(.horizontal, 24)
        .padding(.top, 10)
    }

    @ViewBuilder
    private func resultsList(places: [Place]) -> some View {
        ScrollView {
            attributionRow
            LazyVStack(spacing: 0) {
                ForEach(Array(places.enumerated()), id: \.element.osmId) { index, place in
                    SearchResultItem(place: place, onClick: {
                        isSearchFieldFocused = false
                        onPlaceSelected(place)
                    })
                    if index < places.count - 1 {
                        Divider()
                            .padding(.leading, 76)
                    }
                }
            }
            .animation(.easeInOut(duration: 0.2), value: places.map(\.osmId))
            .background(Color(.systemBackground))
            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
            .padding(.horizontal, 16)
            .padding(.top, 4)
            .padding(.bottom, 24)
        }
        .contentMargins(.top, headerHeight, for: .scrollContent)
        .scrollDismissesKeyboard(.immediately)
    }
}
