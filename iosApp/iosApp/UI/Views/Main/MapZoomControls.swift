import Shared
import SwiftUI

struct MapZoomControls: View {
    let strings: Strings
    let onZoomInClicked: () -> Void
    let onZoomOutClicked: () -> Void

    private let zoomGlassID: String = "map_zoom_glass_id"

    @Namespace private var zoomGlassNamespace

    var body: some View {
        GlassContainer {
            VStack {
                Button(action: {
                    onZoomInClicked()
                }, label: {
                    Image(systemName: "plus")
                        .font(.title3)
                        .fontWeight(.bold)
                        .frame(width: Dimens.zoomControlIconSize, height: Dimens.zoomControlIconSize)
                        .floatingButtonPadding(.top)
                })
                .glassButtonStyle()
                .glassUnion(id: zoomGlassID, namespace: zoomGlassNamespace)
                .accessibilityIdentifier(TestTags.shared.MAIN_FAB_ZOOM_IN_BUTTON)
                .accessibilityLabel(strings.get(id: SharedRes.strings().map_zoom_in_a11y))
                Button(action: {
                    onZoomOutClicked()
                }, label: {
                    Image(systemName: "minus")
                        .font(.title3)
                        .fontWeight(.bold)
                        .frame(width: Dimens.zoomControlIconSize, height: Dimens.zoomControlIconSize)
                        .floatingButtonPadding(.bottom)
                })
                .glassButtonStyle()
                .glassUnion(id: zoomGlassID, namespace: zoomGlassNamespace)
                .accessibilityIdentifier(TestTags.shared.MAIN_FAB_ZOOM_OUT_BUTTON)
                .accessibilityLabel(strings.get(id: SharedRes.strings().map_zoom_out_a11y))
            }
        }
    }
}

#Preview {
    ZStack {
        Color.green
        MapZoomControls(
            strings: Strings(),
            onZoomInClicked: {},
            onZoomOutClicked: {}
        )
    }
}
