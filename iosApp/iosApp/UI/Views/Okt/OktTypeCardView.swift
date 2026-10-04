import Shared
import SwiftUI

struct OktTypeCardView: View {
    let strings: Strings
    let type: OktType
    let onClick: () -> Void

    @ScaledMetric(relativeTo: .headline) private var titleSize: CGFloat = 15

    var body: some View {
        let title = strings.get(id: type.title)
        let subtitle = strings.get(id: type.subtitle)
        Button(action: onClick) {
            VStack(spacing: 6) {
                type.icon.swiftUIImage
                    .resizable()
                    .scaledToFit()
                    .frame(height: 44)
                    .accessibilityHidden(true)
                Text(title)
                    .font(.system(size: titleSize, weight: .semibold))
                    .foregroundStyle(.primary)
                Text(subtitle)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .lineLimit(1)
                    .minimumScaleFactor(0.7)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 12)
            .padding(.horizontal, 6)
            .background(Color(.tertiarySystemFill), in: RoundedRectangle(cornerRadius: 14, style: .continuous))
        }
        .buttonStyle(.plain)
        .accessibilityLabel("\(title), \(strings.get(id: type.longSubtitle))")
    }
}

#Preview {
    HStack(spacing: 12) {
        OktTypeCardView(strings: Strings(), type: .okt, onClick: {})
        OktTypeCardView(strings: Strings(), type: .rpddk, onClick: {})
        OktTypeCardView(strings: Strings(), type: .akt, onClick: {})
    }
    .padding()
}
