import Shared
import SwiftUI

struct IconTitleCardView: View {
    let image: Image
    let title: String
    let onClick: () -> Void

    private static let imageHeight: CGFloat = 46

    @ScaledMetric(relativeTo: .headline) private var titleSize: CGFloat = 15
    @Environment(\.isEnabled) private var isEnabled

    var body: some View {
        Button(action: onClick) {
            VStack(spacing: 8) {
                image
                    .renderingMode(.original)
                    .resizable()
                    .scaledToFit()
                    .frame(height: Self.imageHeight)
                    .accessibilityHidden(true)
                Text(title)
                    .font(.system(size: titleSize, weight: .semibold))
                    .foregroundStyle(.primary)
                    .lineLimit(1)
                    .minimumScaleFactor(0.7)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .padding(.horizontal, 6)
            .background(Color(.tertiarySystemFill), in: RoundedRectangle(cornerRadius: 14, style: .continuous))
            .contentShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
            .opacity(isEnabled ? 1 : 0.4)
        }
        .buttonStyle(PressFeedbackButtonStyle())
    }
}

#Preview {
    HStack(spacing: 12) {
        let images = SharedRes.images()
        IconTitleCardView(image: images.ic_hungary_colored.swiftUIImage, title: "Landscapes", onClick: {})
        IconTitleCardView(image: images.ic_destinations_by_category.swiftUIImage, title: "Categories", onClick: {})
            .disabled(true)
        IconTitleCardView(image: images.ic_destinations_in_list.swiftUIImage, title: "List", onClick: {})
    }
    .padding()
}
