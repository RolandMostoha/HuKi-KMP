import Shared
import SwiftUI

struct DestinationCardView: View {
    let strings: Strings
    let destination: Destination
    let onClick: () -> Void

    @State private var expanded = false

    private let cardWidth: CGFloat = 180
    private let cardHeight: CGFloat = 190
    private let contentPadding: CGFloat = 14
    private let expandedTextTopInset: CGFloat = -6

    var body: some View {
        ZStack {
            typeColor
            gradientOverlay
            content
        }
        .frame(width: cardWidth, height: cardHeight)
        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        .contentShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        .onTapGesture { onClick() }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(TestTags.shared.DESTINATION_CARD)
    }

    private var content: some View {
        ZStack(alignment: .top) {
            textBlock
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottomLeading)
                .padding(.top, expanded ? expandedTextTopInset : 0)
            HStack(alignment: .center, spacing: 2) {
                if !expanded {
                    DestinationTypeChipView(
                        strings: strings,
                        type: destination.type,
                        font: .system(size: 11, weight: .semibold),
                        horizontalPadding: 10,
                        verticalPadding: 6
                    )
                }
                Spacer(minLength: 0)
                expandButton
            }
        }
        .padding(contentPadding)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        .animation(.easeInOut(duration: 0.2), value: expanded)
    }

    private var textBlock: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(destination.name)
                .font(.system(size: 17, weight: .bold))
                .foregroundStyle(.white)
                .lineLimit(1)
                .minimumScaleFactor(0.7)
                .layoutPriority(1)
            HStack(alignment: .center, spacing: 4) {
                Image(systemName: "mappin.and.ellipse")
                    .font(.system(size: 11, weight: .medium))
                Text(destination.town)
                    .font(.system(size: 11, weight: .medium))
                    .lineLimit(1)
                    .padding(.leading, 2)
            }
            .foregroundStyle(.white.opacity(0.9))
            .layoutPriority(1)
            Text(strings.get(id: destination.description_))
                .font(.system(size: 12))
                .foregroundStyle(.white.opacity(0.85))
                .padding(.top, expanded ? 0 : 4)
                .lineLimit(expanded ? nil : 1)
        }
    }

    private var expandButton: some View {
        Button(action: { expanded.toggle() }, label: {
            Image(systemName: expanded ? "chevron.up" : "chevron.down")
                .font(.system(size: 12, weight: .bold))
                .foregroundStyle(.white)
                .frame(width: 24, height: 24)
                .background(.black.opacity(0.6), in: .circle)
        })
        .buttonStyle(.plain)
        .accessibilityLabel(strings.get(id: SharedRes.strings().destinations_a11y_info))
        .accessibilityIdentifier(TestTags.shared.DESTINATION_EXPAND_BUTTON)
    }

    private var typeColor: SwiftUI.Color {
        SwiftUI.Color(destination.type.colorRes.getUIColor())
    }

    private var gradientOverlay: some View {
        LinearGradient(
            colors: [.clear, .black.opacity(0.75)],
            startPoint: .top,
            endPoint: .bottom
        )
    }
}
