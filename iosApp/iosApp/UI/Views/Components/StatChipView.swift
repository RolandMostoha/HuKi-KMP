import Shared
import SwiftUI

struct StatChipView: View {
    let systemImage: String
    let value: String
    var label: String?
    var style: StatChipStyle = .compact
    var backgroundColor = Color(.tertiarySystemFill)

    private let primary = Color(SharedRes.colors().primary.getUIColor())

    var body: some View {
        let isStandard = style == .standard
        let isInline = style == .inline
        let isCompact = style == .compact
        let layout: AnyLayout = isStandard
            ? AnyLayout(VStackLayout(spacing: 10))
            : AnyLayout(HStackLayout(spacing: isInline ? 6 : 3))
        layout {
            Image(systemName: systemImage)
                .font(.system(size: isStandard ? 18 : (isInline ? 15 : 14), weight: .semibold))
                .foregroundStyle(primary)
            Text(UiFormatter.formatStatValue(value, smallFont: .system(size: isCompact ? 11 : 12, weight: .medium)))
                .font(.system(size: isStandard ? 18 : (isInline ? 16 : 14), weight: isCompact ? .heavy : .bold))
                .foregroundStyle(.primary)
                .multilineTextAlignment(.center)
                .lineLimit(1)
                .minimumScaleFactor(isStandard ? 1 : 0.6)
        }
        .frame(maxWidth: isInline ? nil : .infinity)
        .padding(.horizontal, isStandard ? 8 : (isInline ? 0 : 4))
        .padding(.vertical, isStandard ? 16 : (isInline ? 2 : 12))
        .background(
            RoundedRectangle(cornerRadius: isStandard ? 18 : 38, style: .continuous)
                .fill(isInline ? Color.clear : backgroundColor)
        )
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(label.map { "\($0), \(value)" } ?? value)
    }
}

enum StatChipStyle {
    /// Compact horizontal pill (icon next to value), used in dense lists.
    case compact
    /// Standard stacked tile (icon above value), used in detail layouts.
    case standard
    /// Compact layout without the pill, for rows inside a card.
    case inline
}

#Preview {
    VStack(spacing: 16) {
        HStack(spacing: 12) {
            StatChipView(systemImage: "clock.fill", value: "7h 28m", label: "Travel time", style: .standard)
            StatChipView(systemImage: "location.fill", value: "24.6 km", label: "Distance", style: .standard)
        }
        HStack(spacing: 8) {
            StatChipView(systemImage: "clock.fill", value: "7h 28m", label: "Travel time")
            StatChipView(systemImage: "location.fill", value: "24.6 km", label: "Distance")
        }
    }
    .padding()
}
