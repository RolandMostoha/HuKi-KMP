import Shared
import SwiftUI

struct FloatingActionContainer: View {
    let strings: Strings
    let uiState: MainUiState
    let mainActionGlassID: String
    let mainActionGlassNamespace: Namespace.ID
    let onLayersClicked: () -> Void
    let onMyLocationClicked: () -> Void
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
                    searchBar
                    Spacer(minLength: 0)
                    fabControls
                }
            } else {
                VStack(spacing: 16) {
                    HStack {
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
                    .accessibilityIdentifier(TestTags.shared.MAIN_FAB_MY_LOCATION_BUTTON)
                    .accessibilityLabel(strings.get(id: uiState.myLocationState.myLocationStatus.a11yId))
            }
        }
    }
}
