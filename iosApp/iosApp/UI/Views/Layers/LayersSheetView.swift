import Shared
import SwiftUI

struct LayersSheetView: View {
    let strings: Strings
    let selectedBaseLayer: BaseLayer
    let isHikingLayerSelected: Bool
    let isBikeLayerSelected: Bool
    let isGpxLayerSelected: Bool
    let onBaseLayerSelected: (BaseLayer) -> Void
    let onHikingLayerSelected: () -> Void
    let onBikeLayerSelected: () -> Void
    let onGpxLayerSelected: () -> Void
    let onDismissRequest: () -> Void

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                HStack(spacing: 14) {
                    Text(strings.get(id: SharedRes.strings().layers_title))
                        .font(.title2)
                        .fontWeight(.bold)
                        .lineLimit(1)
                        .accessibilityAddTraits(.isHeader)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    CloseButton(action: onDismissRequest)
                }
                .padding(.top, 20)
                sectionTitle(strings.get(id: SharedRes.strings().layers_base_layers_title))
                HStack(alignment: .top) {
                    ForEach(BaseLayer.allCases, id: \.self) { baseLayer in
                        LayersItemView(
                            title: strings.get(id: baseLayer.title),
                            image: baseLayer.image.toUIImage()!,
                            selected: selectedBaseLayer == baseLayer,
                            onClick: { onBaseLayerSelected(baseLayer) }
                        )
                        .frame(maxWidth: .infinity)
                    }
                }
                Divider()
                    .padding(.top, 16)
                sectionTitle(strings.get(id: SharedRes.strings().layers_overlay_layers_title))
                HStack(alignment: .top, spacing: 16) {
                    LayersItemView(
                        title: strings.get(id: SharedRes.strings().layers_overlay_bike_title),
                        image: SharedRes.images().ic_layers_bike.toUIImage()!,
                        selected: isBikeLayerSelected,
                        showsCheckmark: true,
                        onClick: { onBikeLayerSelected() }
                    )
                    LayersItemView(
                        title: strings.get(id: SharedRes.strings().layers_overlay_hiking_title),
                        image: SharedRes.images().ic_layers_hiking.toUIImage()!,
                        selected: isHikingLayerSelected,
                        showsCheckmark: true,
                        onClick: { onHikingLayerSelected() }
                    )
                    LayersItemView(
                        title: strings.get(id: SharedRes.strings().layers_overlay_gpx_title),
                        image: SharedRes.images().ic_layers_gpx.toUIImage()!,
                        selected: isGpxLayerSelected,
                        showsCheckmark: true,
                        onClick: { onGpxLayerSelected() }
                    )
                }
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 18)
        }
    }

    private func sectionTitle(_ text: String) -> some View {
        Text(text)
            .font(.headline)
            .accessibilityAddTraits(.isHeader)
            .padding(.top, 16)
            .padding(.bottom, 12)
    }
}

#Preview {
    LayersSheetView(
        strings: Strings(),
        selectedBaseLayer: .tuhu,
        isHikingLayerSelected: true,
        isBikeLayerSelected: true,
        isGpxLayerSelected: false,
        onBaseLayerSelected: { _ in },
        onHikingLayerSelected: {},
        onBikeLayerSelected: {},
        onGpxLayerSelected: {},
        onDismissRequest: {}
    )
}
