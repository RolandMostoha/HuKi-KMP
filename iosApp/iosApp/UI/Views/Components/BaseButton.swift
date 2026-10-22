import Shared
import SwiftUI

struct BaseButton: View {
    let icon: Shared.ImageResource
    let title: String
    let foregroundColor: SwiftUI.Color
    let backgroundColor: SwiftUI.Color
    var size: ButtonSize = .regular
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(size.font)
                .fontWeight(.semibold)
                .lineLimit(1)
                .frame(maxWidth: .infinity)
                .overlay(alignment: .leading) {
                    icon.swiftUIImage
                        .renderingMode(.template)
                        .resizable()
                        .scaledToFit()
                        .frame(width: size.iconSize, height: size.iconSize)
                }
                .padding(.horizontal, 24)
                .padding(.vertical, size.verticalPadding)
                .foregroundStyle(foregroundColor)
                .background(backgroundColor, in: .capsule)
        }
        .buttonStyle(PressFeedbackButtonStyle())
    }
}

enum ButtonSize {
    case regular
    case small

    var font: Font {
        switch self {
        case .regular: .headline
        case .small: .subheadline
        }
    }

    var iconSize: CGFloat {
        switch self {
        case .regular: 20
        case .small: 16
        }
    }

    var verticalPadding: CGFloat {
        switch self {
        case .regular: 14
        case .small: 8
        }
    }
}
