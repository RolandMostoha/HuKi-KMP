import Shared
import SwiftUI

struct LayersItemView: View {
    let title: String
    let image: UIImage
    let selected: Bool
    let onClick: () -> Void

    var body: some View {
        VStack(spacing: 8) {
            Button(action: onClick) {
                ZStack {
                    RoundedRectangle(cornerRadius: 20)
                        .fill(selected ? Color(SharedRes.colors().primary.getUIColor()).opacity(0.08) : Color.clear)
                        .stroke(selected ? Color(SharedRes.colors().primary.getUIColor()) : Color.clear, lineWidth: 2)
                        .frame(width: 72, height: 72)

                    Image(uiImage: image)
                        .resizable()
                        .scaledToFill()
                        .frame(width: 64, height: 64)
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                }
                .shadow(color: Color.black.opacity(0.18), radius: 8, x: 0, y: 6)
            }
            .buttonStyle(PlainButtonStyle())

            Text(title)
                .font(.system(size: 13))
                .fontWeight(selected ? .semibold : .regular)
                .multilineTextAlignment(.center)
                .onTapGesture(perform: onClick)
        }
    }
}
