import Shared
import SwiftUI

struct BaseButton: View {
    let systemImage: String
    let title: String
    let foregroundColor: SwiftUI.Color
    let backgroundColor: SwiftUI.Color
    var verticalPadding: CGFloat = 12
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.headline)
                .fontWeight(.semibold)
                .lineLimit(1)
                .frame(maxWidth: .infinity)
                .overlay(alignment: .leading) {
                    Image(systemName: systemImage)
                        .font(.system(size: 15, weight: .semibold))
                }
                .padding(.horizontal, 24)
                .padding(.vertical, verticalPadding)
                .foregroundStyle(foregroundColor)
                .background(backgroundColor, in: .capsule)
        }
        .buttonStyle(PressFeedbackButtonStyle())
    }
}
