import Shared
import SwiftUI
import UIKit

struct FloatingActionContainer: View {
    let strings: Strings
    let uiState: MainUiState
    let mainActionGlassID: String
    let mainActionGlassNamespace: Namespace.ID
    let onLayersClicked: () -> Void
    let onDiscoverClicked: () -> Void
    let onMyLocationClicked: () -> Void
    let onMyLocationLongClicked: () -> Void
    let onZoomInClicked: () -> Void
    let onZoomOutClicked: () -> Void
    let onSearchTap: () -> Void
    let onMenuClick: () -> Void

    @Environment(\.layoutMode) private var layoutMode

    private var isWideLayout: Bool { layoutMode.isWide }

    private var isFollowingLiveCompass: Bool {
        if case .followingLiveCompass = onEnum(of: uiState.myLocationState.myLocationStatus) {
            return true
        }
        return false
    }

    private var isZoomControlsVisible: Bool {
        uiState.mapZoomControlsAlwaysVisible || isFollowingLiveCompass
    }

    var body: some View {
        Group {
            if isWideLayout {
                HStack(alignment: .bottom, spacing: 16) {
                    VStack(alignment: .leading, spacing: 20) {
                        discoverFab
                        searchBar
                    }
                    Spacer(minLength: 0)
                    fabControls
                }
            } else {
                VStack(spacing: 20) {
                    HStack(alignment: .bottom) {
                        discoverFab
                            .padding(.leading, 11)
                        Spacer()
                        fabControls
                    }
                    searchBar
                }
            }
        }
        .animation(.smooth(duration: 0.3), value: uiState.sheet)
        .animation(.smooth(duration: 0.3), value: uiState.isSearchBarVisible)
        .animation(.smooth(duration: 0.3), value: isZoomControlsVisible)
    }

    @ViewBuilder
    private var fabControls: some View {
        if uiState.sheet == nil {
            VStack(alignment: .trailing, spacing: 16) {
                if isZoomControlsVisible {
                    MapZoomControls(
                        strings: strings,
                        onZoomInClicked: onZoomInClicked,
                        onZoomOutClicked: onZoomOutClicked
                    )
                    .transition(.scale.combined(with: .opacity))
                }
                fabColumn
            }
            .transition(.move(edge: .trailing).combined(with: .opacity))
        }
    }

    private var isDiscoverFabVisible: Bool {
        uiState.sheet == nil && uiState.isSearchBarVisible
    }

    @ViewBuilder
    private var discoverFab: some View {
        if isDiscoverFabVisible {
            VStack {
                Button(action: onDiscoverClicked) {
                    Image(systemName: "backpack.fill")
                        .fontWeight(.bold)
                        .frame(width: Dimens.discoverFabSize, height: Dimens.discoverFabSize)
                        .contentShape(Circle())
                }
                .buttonStyle(.plain)
                .glassBackground(.regular, in: Circle(), interactive: true)
                .accessibilityIdentifier(TestTags.shared.MAIN_FAB_DISCOVER_BUTTON)
                .accessibilityLabel(strings.get(id: SharedRes.strings().discover_a11y_fab))
            }
            .transition(.move(edge: .leading).combined(with: .opacity))
        }
    }

    @ViewBuilder
    private var searchBar: some View {
        if uiState.sheet == nil && uiState.isSearchBarVisible {
            SearchBarView(
                strings: strings,
                onSearchTap: onSearchTap,
                onMenuClick: onMenuClick
            )
            .frame(maxWidth: isWideLayout ? AdaptiveLayout.searchBarMaxWidth : .infinity, alignment: .leading)
            .padding(.leading, isWideLayout ? 0 : 11)
            .padding(.trailing, 11)
            .transition(.move(edge: .bottom).combined(with: .opacity))
        }
    }

    @ViewBuilder
    private var fabColumn: some View {
        GlassContainer {
                VStack {
                    Button(action: {
                        onLayersClicked()
                    }, label: {
                        Group {
                            if uiState.isGpxLoading {
                                ProgressView()
                            } else {
                                Image(systemName: "map.fill")
                                    .fontWeight(.bold)
                            }
                        }
                        .floatingButtonPadding(.top)
                    })
                    .glassButtonStyle()
                    .glassUnion(id: mainActionGlassID, namespace: mainActionGlassNamespace)
                    .disabled(uiState.isGpxLoading)
                    .accessibilityLabel(strings.get(id: SharedRes.strings().layers_a11y_fab))
                    Button(action: {
                        onMyLocationClicked()
                    }, label: {
                        Group {
                            if uiState.isMyLocationLoading {
                                ProgressView()
                                    .tint(Color(SharedRes.colors().primary.getUIColor()))
                            } else {
                                let imageSystemName = switch onEnum(of: uiState.myLocationState.myLocationStatus) {
                                case .default, .notAvailable:
                                    "location.north"
                                case .following:
                                    "location.fill"
                                case .followingLiveCompass:
                                    "location.north.line.fill"
                                }
                                Image(systemName: imageSystemName)
                                    .fontWeight(.bold)
                                    .foregroundColor(Color(SharedRes.colors().primary.getUIColor()))
                            }
                        }
                        .floatingButtonPadding(.bottom)
                    })
                    .glassButtonStyle()
                    .glassUnion(id: mainActionGlassID, namespace: mainActionGlassNamespace)
                    .disabled(uiState.isMyLocationLoading)
                    .highPriorityGesture(
                        LongPressGesture().onEnded { _ in
                            guard !uiState.isMyLocationLoading else { return }
                            UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                            onMyLocationLongClicked()
                        }
                    )
                    .accessibilityIdentifier(TestTags.shared.MAIN_FAB_MY_LOCATION_BUTTON)
                    .accessibilityLabel(strings.get(id: uiState.myLocationState.myLocationStatus.a11yId))
            }
        }
    }
}
