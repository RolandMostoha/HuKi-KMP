import Shared
import SwiftUI

struct DestinationCardView: View {
    let strings: Strings
    let destination: Destination
    let onClick: () -> Void

    @State private var expanded = false

    private let cardWidth: CGFloat = 180
    private let cardHeight: CGFloat = 190

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
        VStack(alignment: .leading, spacing: 0) {
            HStack(alignment: .center, spacing: 2) {
                if !expanded {
                    typeChip
                }
                Spacer(minLength: 0)
                expandButton
            }
            Spacer(minLength: 12)
            textBlock
        }
        .padding(14)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        .animation(.easeInOut(duration: 0.2), value: expanded)
    }

    private var typeChip: some View {
        HStack(spacing: 6) {
            Image(uiImage: destination.type.iconRes.toUIImage()!)
                .renderingMode(.template)
                .resizable()
                .scaledToFit()
                .frame(width: 16, height: 16)
                .foregroundStyle(chipTint)
            Text(strings.get(id: destination.type.title))
                .font(.system(size: 11, weight: .semibold))
                .foregroundStyle(chipTint)
                .lineLimit(1)
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 6)
        .background(SwiftUI.Color(.systemBackground), in: .capsule)
    }

    private var textBlock: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(destination.name)
                .font(.system(size: 17, weight: .bold))
                .foregroundStyle(.white)
                .lineLimit(1)
                .minimumScaleFactor(0.7)
            HStack(alignment: .center, spacing: 4) {
                Image(systemName: "mappin.and.ellipse")
                    .font(.system(size: 11, weight: .medium))
                Text(destination.town)
                    .font(.system(size: 11, weight: .medium))
                    .lineLimit(1)
                    .padding(.leading, 2)
            }
            .foregroundStyle(.white.opacity(0.9))
            Text(strings.get(id: destination.description_))
                .font(.system(size: 12))
                .foregroundStyle(.white.opacity(0.85))
                .padding(.top, 4)
                .lineLimit(expanded ? nil : 1)
                .fixedSize(horizontal: false, vertical: true)
        }
    }

    private var expandButton: some View {
        Button(action: { expanded.toggle() }, label: {
            Image(systemName: expanded ? "chevron.up" : "chevron.down")
                .font(.system(size: 12, weight: .bold))
                .foregroundStyle(.white)
                .frame(width: 24, height: 24)
                .background(.black.opacity(0.28), in: .circle)
        })
        .buttonStyle(.plain)
        .accessibilityLabel(strings.get(id: SharedRes.strings().destinations_a11y_info))
        .accessibilityIdentifier(TestTags.shared.DESTINATION_EXPAND_BUTTON)
    }

    private var typeColor: SwiftUI.Color {
        SwiftUI.Color(destination.type.colorRes.getUIColor())
    }

    private var chipTint: SwiftUI.Color {
        SwiftUI.Color(destination.type.colorRes.getUIColor().adaptiveTint())
    }

    private var gradientOverlay: some View {
        LinearGradient(
            colors: [.clear, .black.opacity(0.75)],
            startPoint: .top,
            endPoint: .bottom
        )
    }
}
