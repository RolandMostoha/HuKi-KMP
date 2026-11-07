import Shared
import SwiftUI

struct PoweredByAttributionView: View {
    let strings: Strings
    let logo: UIImage
    let logoHeight: CGFloat
    let accessibilityLabel: String
    var font: Font = .caption
    var tintsLogo: Bool = false
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 8) {
                Text(strings.get(id: SharedRes.strings().search_powered_by))
                    .font(font)
                    .foregroundStyle(Color(.secondaryLabel))
                Image(uiImage: logo)
                    .renderingMode(tintsLogo ? .template : .original)
                    .resizable()
                    .scaledToFit()
                    .frame(height: logoHeight)
                    .foregroundStyle(.primary)
            }
        }
        .buttonStyle(.plain)
        .accessibilityLabel(accessibilityLabel)
    }
}

#Preview {
    PoweredByAttributionView(
        strings: Strings(),
        logo: SharedRes.images().img_graphhopper_logo.toUIImage()!,
        logoHeight: 10,
        accessibilityLabel: "",
        font: .caption2,
        tintsLogo: true,
        action: {}
    )
}
