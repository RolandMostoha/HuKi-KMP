import Shared
import SwiftUI

struct DestinationTypeChipView: View {
    let strings: Strings
    let type: DestinationType
    var font: Font = .system(size: 13, weight: .semibold)
    var horizontalPadding: CGFloat = 12
    var verticalPadding: CGFloat = 7
    var contentColor: SwiftUI.Color?
    var containerColor: SwiftUI.Color = SwiftUI.Color(.systemBackground)

    var body: some View {
        HStack(spacing: 6) {
            Image(uiImage: type.iconRes.toUIImage()!)
                .renderingMode(.template)
                .resizable()
                .scaledToFit()
                .frame(width: 16, height: 16)
            Text(strings.get(id: type.title))
                .font(font)
                .lineLimit(1)
        }
        .foregroundStyle(contentColor ?? SwiftUI.Color(type.colorRes.getUIColor().categoryTint()))
        .padding(.horizontal, horizontalPadding)
        .padding(.vertical, verticalPadding)
        .background(containerColor, in: .capsule)
    }
}

#Preview {
    VStack(spacing: 12) {
        DestinationTypeChipView(strings: Strings(), type: .peak)
        DestinationTypeChipView(
            strings: Strings(),
            type: .river,
            font: .system(size: 11, weight: .semibold),
            horizontalPadding: 10,
            verticalPadding: 6
        )
    }
    .padding()
    .background(SwiftUI.Color(.systemGray5))
}
