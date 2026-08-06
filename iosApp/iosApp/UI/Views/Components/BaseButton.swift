import Shared
import SwiftUI

struct BaseButton: View {
    let icon: Shared.ImageResource
    let title: String
    let foregroundColor: SwiftUI.Color
    let backgroundColor: SwiftUI.Color
    let action: () -> Void

    private let verticalPadding: CGFloat = 14
    private let iconSize: CGFloat = 20

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.headline)
                .fontWeight(.semibold)
                .lineLimit(1)
                .frame(maxWidth: .infinity)
                .overlay(alignment: .leading) {
                    icon.swiftUIImage
                        .renderingMode(.template)
                        .resizable()
                        .scaledToFit()
                        .frame(width: iconSize, height: iconSize)
                }
                .padding(.horizontal, 24)
                .padding(.vertical, verticalPadding)
                .foregroundStyle(foregroundColor)
                .background(backgroundColor, in: .capsule)
        }
        .buttonStyle(PressFeedbackButtonStyle())
    }
}
