import Shared
import SwiftUI

struct LayersSheetView: View {
    let strings: Strings
    let selectedBaseLayer: BaseLayer
    let isHikingLayerSelected: Bool
    let isGpxLayerSelected: Bool
    let onBaseLayerSelected: (BaseLayer) -> Void
    let onHikingLayerSelected: () -> Void
    let onGpxLayerSelected: () -> Void
    let onDismissRequest: () -> Void

    var body: some View {
        VStack(alignment: .center) {
            ZStack {
                Text(strings.get(id: SharedRes.strings().layers_base_layers_title, args: []))
                    .font(.title2)
                    .fontWeight(.bold)
                    .frame(maxWidth: .infinity, alignment: .center)

                HStack {
                    Spacer()
                    Button(action: onDismissRequest) {
                        Image(systemName: "xmark")
                            .font(.system(size: 18, weight: .semibold))
                            .foregroundColor(.primary)
                            .padding(12)
                            .background(
                                Circle().fill(Color(.systemGray5))
                            )
                    }
                    .buttonStyle(.plain)
                    .contentShape(Circle())
                    .padding(.trailing, 8)
                    .accessibilityIdentifier(TestTags.shared.LAYERS_CLOSE)
                }
            }
            .padding(.top, 26)
            .padding(.horizontal, 16)

            HStack(spacing: 36) {
                ForEach(BaseLayer.allCases, id: \.self) { baseLayer in
                    LayersItemView(
                        title: strings.get(id: baseLayer.title, args: []),
                        image: baseLayer.image.toUIImage()!,
                        selected: selectedBaseLayer == baseLayer,
                        onClick: { onBaseLayerSelected(baseLayer) }
                    )
                }
            }
            .padding(.horizontal, 24)
            .padding(.top, 4)

            Text(strings.get(id: SharedRes.strings().layers_overlay_layers_title, args: []))
                .font(.title2)
                .fontWeight(.bold)
                .padding(.top, 12)

            HStack(spacing: 56) {
                LayersItemView(
                    title: strings.get(id: SharedRes.strings().layers_overlay_hiking_title, args: []),
                    image: SharedRes.images().ic_layers_hiking.toUIImage()!,
                    selected: isHikingLayerSelected,
                    onClick: { onHikingLayerSelected() }
                )
                LayersItemView(
                    title: strings.get(id: SharedRes.strings().layers_overlay_gpx_title, args: []),
                    image: SharedRes.images().ic_layers_gpx.toUIImage()!,
                    selected: isGpxLayerSelected,
                    onClick: { onGpxLayerSelected() }
                )
            }
            .padding(.horizontal, 24)
            .padding(.top, 8)

            Spacer()
        }
        .padding(.bottom, 18)
    }
}
