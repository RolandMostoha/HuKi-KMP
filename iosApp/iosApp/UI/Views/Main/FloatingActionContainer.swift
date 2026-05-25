import Shared
import SwiftUI

struct FloatingActionContainer: View {
    let strings: Strings
    let uiState: MainUiState
    let mainActionGlassID: String
    let mainActionGlassNamespace: Namespace.ID
    let onLayersClicked: () -> Void
    let onMyLocationClicked: () -> Void

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            if uiState.isLoading {
                Button(action: {}, label: {
                    ProgressView()
                        .scaleEffect(1.1)
                        .padding(6)
                })
                .glassButtonStyle()
                .buttonBorderShape(.circle)
                .disabled(true)
                .padding(.top, 48)
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topTrailing)
            }
            GlassContainer {
                VStack {
                    Button(action: {
                        onLayersClicked()
                    }, label: {
                        Image(systemName: "map.fill")
                            .fontWeight(.bold)
                            .floatingButtonPadding(.top)
                    })
                    .glassButtonStyle()
                    .glassUnion(id: mainActionGlassID, namespace: mainActionGlassNamespace)
                    .accessibilityLabel(strings.get(id: SharedRes.strings().layers_a11y_fab))
                    Button(action: {
                        onMyLocationClicked()
                    }, label: {
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
                            .floatingButtonPadding(.bottom)
                    })
                    .glassButtonStyle()
                    .glassUnion(id: mainActionGlassID, namespace: mainActionGlassNamespace)
                    .accessibilityIdentifier(TestTags.shared.MAIN_FAB_MY_LOCATION_BUTTON)
                    .accessibilityLabel(strings.get(id: uiState.myLocationState.myLocationStatus.accessibilityId))
                }
            }
        }
    }
}
