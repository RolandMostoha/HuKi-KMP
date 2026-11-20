import Shared
import SwiftUI

struct LayersItemView: View {
    let title: String
    let image: UIImage
    let selected: Bool
    var showsCheckmark = false
    let onClick: () -> Void

    private var primary: SwiftUI.Color { SwiftUI.Color(SharedRes.colors().primary.getUIColor()) }

    var body: some View {
        Button(action: onClick) {
            VStack(spacing: 8) {
                ZStack(alignment: .topTrailing) {
                    ZStack {
                        RoundedRectangle(cornerRadius: 20)
                            .fill(selected ? primary.opacity(0.08) : Color.clear)
                            .stroke(selected ? primary : Color.clear, lineWidth: 2)
                        Color.clear
                            .overlay(Image(uiImage: image).resizable().scaledToFill())
                            .clipShape(RoundedRectangle(cornerRadius: 16))
                            .padding(4)
                    }
                    .frame(width: 72, height: 72)
                    .shadow(color: Color.black.opacity(0.18), radius: 8, x: 0, y: 6)
                    .padding(6)
                    if showsCheckmark && selected {
                        Image(systemName: "checkmark.circle.fill")
                            .font(.system(size: 22, weight: .semibold))
                            .symbolRenderingMode(.palette)
                            .foregroundStyle(.white, primary)
                            .background(Circle().fill(.white))
                            .transition(.scale.combined(with: .opacity))
                    }
                }
                .animation(.spring(duration: 0.25), value: selected)
                ZStack {
                    // Reserves the semibold width, so selecting doesn't resize the item and shift its neighbours
                    label(weight: .semibold).hidden()
                    label(weight: selected ? .semibold : .regular)
                }
            }
        }
        .buttonStyle(.plain)
        .accessibilityAddTraits(selected ? .isSelected : [])
    }

    private func label(weight: Font.Weight) -> some View {
        Text(title)
            .font(.subheadline)
            .fontWeight(weight)
            .foregroundStyle(.primary)
            .multilineTextAlignment(.center)
            .lineLimit(1)
            .minimumScaleFactor(0.8)
    }
}

#Preview {
    HStack {
        LayersItemView(
            title: "Hiking Trails",
            image: SharedRes.images().ic_layers_hiking.toUIImage()!,
            selected: true,
            showsCheckmark: true,
            onClick: {}
        )
        LayersItemView(
            title: "Satellite",
            image: SharedRes.images().ic_layers_satellite.toUIImage()!,
            selected: true,
            onClick: {}
        )
    }
}
