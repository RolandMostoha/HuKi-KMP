import Shared
import SwiftUI

struct BaseButton: View {
    let icon: Shared.ImageResource
    let title: String
    let foregroundColor: SwiftUI.Color
    let backgroundColor: SwiftUI.Color
    var size: ButtonSize = .regular
    var iconPlacement: ButtonIconPlacement = .leading
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            content
                .padding(.horizontal, 24)
                .padding(.vertical, size.verticalPadding)
                .foregroundStyle(foregroundColor)
                .background(backgroundColor, in: .capsule)
        }
        .buttonStyle(PressFeedbackButtonStyle())
    }

    @ViewBuilder
    private var content: some View {
        switch iconPlacement {
        case .leading:
            titleText
                .frame(maxWidth: .infinity)
                .overlay(alignment: .leading) { iconImage }
        case .inline:
            HStack(spacing: 8) {
                iconImage
                titleText
            }
            .frame(maxWidth: .infinity)
        }
    }

    private var titleText: some View {
        Text(title)
            .font(size.font)
            .fontWeight(.semibold)
            .lineLimit(1)
    }

    private var iconImage: some View {
        icon.swiftUIImage
            .renderingMode(.template)
            .resizable()
            .scaledToFit()
            .frame(width: size.iconSize, height: size.iconSize)
    }
}

/// Where a button's icon sits: pinned to the leading edge of a wide button, or next to the title of a narrow one.
enum ButtonIconPlacement {
    case leading
    case inline
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
