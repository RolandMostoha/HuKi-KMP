import Shared
import SwiftUI

struct DestinationGroupRowView: View {
    let strings: Strings
    let icon: UIImage
    let iconSize: CGFloat
    let color: SwiftUI.Color
    let title: String
    let destinationCount: Int
    let destinations: [Destination]
    let isSelected: Bool
    let selectedDestinationId: String?
    let onClick: () -> Void
    let onDestinationClick: (Destination) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            header
            if isSelected, !destinations.isEmpty {
                destinationCarousel
                    .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: 22, style: .continuous)
                .fill(isSelected ? color.opacity(0.3) : .clear)
        )
    }

    private var header: some View {
        HStack(spacing: 12) {
            Image(uiImage: icon)
                .renderingMode(.template)
                .resizable()
                .scaledToFit()
                .frame(width: iconSize, height: iconSize)
                .foregroundStyle(.white)
                .frame(width: Dimens.landscapeIconSize, height: Dimens.landscapeIconSize)
                .background(Circle().fill(color))
                .accessibilityHidden(true)
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.headline)
                    .foregroundStyle(.primary)
                    .lineLimit(1)
                    .minimumScaleFactor(0.7)
                Text(destinationCountText)
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .contentShape(Rectangle())
        .onTapGesture(perform: onClick)
        .accessibilityElement(children: .combine)
        .accessibilityAddTraits(isSelected ? [.isButton, .isSelected] : .isButton)
        .accessibilityIdentifier(TestTags.shared.LANDSCAPES_ROW)
    }

    private var destinationCountText: String {
        let count = Int32(destinationCount)
        let res = count == 1
            ? SharedRes.strings().destinations_subtitle_single
            : SharedRes.strings().destinations_subtitle_pattern
        return strings.get(id: res, args: [count])
    }

    private var destinationCarousel: some View {
        ScrollViewReader { proxy in
            ScrollView(.horizontal, showsIndicators: false) {
                LazyHStack(spacing: 8) {
                    ForEach(destinations, id: \.osmId) { destination in
                        LandscapeDestinationCardView(
                            strings: strings,
                            destination: destination,
                            isSelected: destination.osmId == selectedDestinationId,
                            onClick: { onDestinationClick(destination) }
                        )
                        .id(destination.osmId)
                    }
                }
                .padding(.horizontal, 12)
            }
            .padding(.horizontal, -12)
            .onChange(of: selectedDestinationId) { _, destinationId in
                guard let destinationId else { return }
                withAnimation(.snappy) {
                    proxy.scrollTo(destinationId, anchor: .center)
                }
            }
        }
        .frame(height: Dimens.landscapeDestinationCardHeight)
    }
}

#Preview {
    DestinationGroupRowView(
        strings: Strings(),
        icon: DestinationType.peak.iconRes.toUIImage()!,
        iconSize: 24,
        color: SwiftUI.Color(DestinationType.peak.colorRes.getUIColor()),
        title: "Peak",
        destinationCount: 12,
        destinations: [],
        isSelected: true,
        selectedDestinationId: nil,
        onClick: {},
        onDestinationClick: { _ in }
    )
    .padding()
}
