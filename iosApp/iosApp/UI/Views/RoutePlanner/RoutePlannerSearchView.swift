import Shared
import SwiftUI

struct RoutePlannerSearchView: View {
    let strings: Strings
    let uiState: RoutePlannerUiState
    let onEvent: (RoutePlannerUiEvents) -> Void

    @State private var viewModel = KoinViewModelProvider.shared.getPlaceFinderViewModel()
    @State private var showLocationIq = false
    @FocusState private var isSearchFieldFocused: Bool

    var body: some View {
        Observing(viewModel.uiState) { placeFinderState in
            VStack(spacing: 0) {
                header
                searchField(placeFinderState: placeFinderState)
                content(placeFinderState: placeFinderState)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
            .background(Color(.systemGray6))
            .toolbar(.hidden, for: .navigationBar)
            .navigationDestination(isPresented: $showLocationIq) {
                LocationIqView()
            }
            .onAppear { isSearchFieldFocused = true }
            // Pushing LocationIq also fires onDisappear, and clear() cancels the scope irreversibly.
            .onDisappear { if !showLocationIq { viewModel.clear() } }
            .accessibilityElement(children: .contain)
            .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_SEARCH_SCREEN)
        }
    }

    private func searchField(placeFinderState: PlaceFinderUiState) -> some View {
        SearchFieldView(
            strings: strings,
            text: Binding<String>(
                get: { placeFinderState.searchText },
                set: { viewModel.onEvent(event: PlaceFinderUiEventsSearchTextChanged(searchText: $0)) }
            ),
            isFocused: $isSearchFieldFocused,
            accessibilityIdentifier: TestTags.shared.ROUTE_PLANNER_SEARCH_FIELD
        )
        .padding(.top, 20)
        .padding(.horizontal, 18)
        .padding(.bottom, 8)
    }

    @ViewBuilder
    private func content(placeFinderState: PlaceFinderUiState) -> some View {
        let hasLocalMatches = !placeFinderState.searchRecentPlaces.isEmpty ||
            !placeFinderState.searchDestinations.isEmpty
        let hasOnlineActivity = placeFinderState.isLoading || placeFinderState.error != nil
        let hasAnyResults = hasLocalMatches || !placeFinderState.places.isEmpty
        let isSearching = !placeFinderState.searchText.isEmpty
        if isSearching && (hasAnyResults || hasOnlineActivity) {
            searchResults(placeFinderState: placeFinderState)
        } else if isSearching {
            Color.clear
        } else {
            discovery(placeFinderState: placeFinderState)
        }
    }

    private func searchResults(placeFinderState: PlaceFinderUiState) -> some View {
        ScrollView {
            VStack(spacing: 0) {
                if !placeFinderState.searchRecentPlaces.isEmpty {
                    RecentPlacesSectionView(
                        strings: strings,
                        places: placeFinderState.searchRecentPlaces,
                        onPlaceSelected: addPlace
                    )
                }
                if !placeFinderState.searchDestinations.isEmpty {
                    DestinationsSectionView(
                        strings: strings,
                        destinations: placeFinderState.searchDestinations,
                        onDestinationSelected: addDestination
                    )
                }
                OnlineResultsSectionView(
                    strings: strings,
                    places: placeFinderState.places,
                    error: placeFinderState.error,
                    isLoading: placeFinderState.isLoading,
                    onPlaceSelected: addPlace,
                    onRetryClicked: {
                        viewModel.onEvent(event: PlaceFinderUiEventsRetryClicked.shared)
                    },
                    onLocationIqClicked: {
                        isSearchFieldFocused = false
                        showLocationIq = true
                    }
                )
            }
            .padding(.bottom, 24)
        }
        .scrollDismissesKeyboard(.immediately)
    }

    private func discovery(placeFinderState: PlaceFinderUiState) -> some View {
        ScrollView {
            VStack(spacing: 0) {
                if !placeFinderState.recentPlaces.isEmpty {
                    RecentPlacesSectionView(
                        strings: strings,
                        places: placeFinderState.recentPlaces,
                        onPlaceSelected: addPlace
                    )
                }
                if !placeFinderState.destinations.isEmpty {
                    DestinationsSectionView(
                        strings: strings,
                        destinations: placeFinderState.destinations,
                        onDestinationSelected: addDestination,
                        title: strings.get(id: placeFinderState.destinationsTitle)
                    )
                }
                actions
            }
            .padding(.bottom, 24)
        }
        .scrollDismissesKeyboard(.immediately)
    }

    private var actions: some View {
        VStack(spacing: 12) {
            SecondaryButton(
                icon: .system("location.fill"),
                title: strings.get(id: SharedRes.strings().route_planner_pick_my_location),
                action: { onEvent(RoutePlannerUiEventsMyLocationAdded.shared) }
            )
            .disabled(uiState.myLocation == nil)
            .opacity(uiState.myLocation == nil ? 0.5 : 1)
            .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_SEARCH_MY_LOCATION_BUTTON)
            SecondaryButton(
                icon: .system("hand.tap.fill"),
                title: strings.get(id: SharedRes.strings().route_planner_pick_on_map),
                action: { onEvent(RoutePlannerUiEventsPickOnMapClicked.shared) }
            )
            .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_SEARCH_PICK_ON_MAP_BUTTON)
        }
        .padding(.horizontal, 16)
        .padding(.top, Dimens.sectionSpacing)
    }

    private var header: some View {
        HStack(spacing: 14) {
            Text(strings.get(id: SharedRes.strings().route_planner_add_stop_title))
                .font(.system(size: 24, weight: .bold))
                .lineLimit(1)
                .frame(maxWidth: .infinity, alignment: .leading)
            CloseButton(
                action: { onEvent(RoutePlannerUiEventsWaypointSearchDismissed.shared) },
                accessibilityIdentifier: TestTags.shared.ROUTE_PLANNER_SEARCH_CLOSE_BUTTON
            )
        }
        .padding(.leading, 24)
        .padding(.trailing, 16)
        .padding(.top, 20)
    }

    private func addPlace(_ place: Place) {
        isSearchFieldFocused = false
        onEvent(RoutePlannerUiEventsSearchPlaceAdded(place: place))
    }

    private func addDestination(_ destination: Destination) {
        isSearchFieldFocused = false
        onEvent(RoutePlannerUiEventsSearchDestinationAdded(destination: destination))
    }
}

#Preview("Discovery") {
    NavigationStack {
        RoutePlannerSearchView(
            strings: Strings(),
            uiState: RoutePlannerUiState(
                routeProfile: .onTrails,
                waypoints: [],
                routePlan: nil,
                isRoutePlanLoading: false,
                routePlanError: nil,
                isWaypointSearchVisible: true,
                waypointSearchTargetId: nil,
                isPickingOnMap: false,
                myLocation: Location(latitude: 47.72, longitude: 18.89, altitude: nil)
            ),
            onEvent: { _ in }
        )
    }
}

#Preview("No known location") {
    NavigationStack {
        RoutePlannerSearchView(
            strings: Strings(),
            uiState: RoutePlannerUiState(
                routeProfile: .onTrails,
                waypoints: [],
                routePlan: nil,
                isRoutePlanLoading: false,
                routePlanError: nil,
                isWaypointSearchVisible: true,
                waypointSearchTargetId: nil,
                isPickingOnMap: false,
                myLocation: nil
            ),
            onEvent: { _ in }
        )
    }
}
