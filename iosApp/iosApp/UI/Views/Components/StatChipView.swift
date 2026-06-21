import Shared
import SwiftUI

struct StatChipView: View {
    let systemImage: String
    let value: String
    var label: String?
    var style: StatChipStyle = .compact

    private let primary = Color(SharedRes.colors().primary.getUIColor())

    var body: some View {
        let isLarge = style == .large
        let layout: AnyLayout = isLarge
            ? AnyLayout(VStackLayout(spacing: 12))
            : AnyLayout(HStackLayout(spacing: 3))
        layout {
            Image(systemName: systemImage)
                .font(.system(size: isLarge ? 18 : 12, weight: .semibold))
                .foregroundStyle(primary)
            Text(UiFormatter.formatStatValue(value, smallFont: .system(size: isLarge ? 12 : 9, weight: .medium)))
                .font(.system(size: isLarge ? 18 : 12, weight: isLarge ? .bold : .heavy))
                .foregroundStyle(.primary)
                .multilineTextAlignment(.center)
                .lineLimit(1)
                .minimumScaleFactor(isLarge ? 1 : 0.7)
        }
        .frame(maxWidth: .infinity)
        .padding(.horizontal, isLarge ? 8 : 4)
        .padding(.vertical, isLarge ? 16 : 12)
        .background(
            RoundedRectangle(cornerRadius: isLarge ? 18 : 14, style: .continuous)
                .fill(Color(.systemGray6))
        )
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(label.map { "\($0), \(value)" } ?? value)
    }
}

enum StatChipStyle {
    /// Compact horizontal pill (icon next to value), used in dense lists.
    case compact
    /// Larger stacked tile (icon above value), used in detail layouts.
    case large
}
