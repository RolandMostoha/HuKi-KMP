import Shared
import SwiftUI

struct OktSectionRowView: View {
    let strings: Strings
    let section: OktSectionItem
    let isSelected: Bool
    let isReversed: Bool
    let selectedMarkerId: String?
    let onClick: () -> Void
    let onStartClick: () -> Void
    let onWebsiteClick: () -> Void
    let onReverseClick: () -> Void
    let onStampClick: (OktMarker) -> Void

    private let oktBlue = Color(SharedRes.colors().oktBlue.getUIColor())

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 12) {
                header
                menu
            }
            if isSelected, !stamps.isEmpty {
                stampCarousel
                    .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: 22, style: .continuous)
                .fill(isSelected ? oktBlue.opacity(0.15) : .clear)
        )
    }

    private var header: some View {
        HStack(spacing: 12) {
            OktSectionBadgeView(prefix: section.badgePrefix, number: section.badgeNumber, isSelected: isSelected)
            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 8) {
                    if isReversed {
                        Image(systemName: "arrow.left.arrow.right")
                            .font(.subheadline.weight(.semibold))
                            .foregroundStyle(.primary)
                            .transition(.opacity)
                            .accessibilityHidden(true)
                    }
                    Text(section.name)
                        .font(.headline)
                        .foregroundStyle(.primary)
                        .lineLimit(1)
                        .minimumScaleFactor(0.6)
                }
                OktSectionStatsView(strings: strings, routeStats: section.routeStats)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .contentShape(Rectangle())
        .onTapGesture(perform: onClick)
        .accessibilityElement(children: .combine)
        .accessibilityAddTraits(isSelected ? [.isButton, .isSelected] : .isButton)
        .accessibilityAction(named: strings.get(id: SharedRes.strings().okt_menu_start), onStartClick)
        .accessibilityValue(isReversed ? strings.get(id: SharedRes.strings().okt_a11y_reversed_state) : "")
        .accessibilityIdentifier(TestTags.shared.OKT_SECTION_ROW)
    }

    private var stamps: [OktStampItem] {
        isReversed ? section.reversedStamps : section.stamps
    }

    private var menu: some View {
        Menu {
            Button(action: onWebsiteClick) {
                Label {
                    Text(strings.get(id: SharedRes.strings().okt_menu_details))
                    Text(strings.get(id: SharedRes.strings().okt_menu_details_subtitle))
                } icon: {
                    Image(systemName: "safari")
                }
            }
            Button(action: onStartClick) {
                Label(strings.get(id: SharedRes.strings().okt_menu_start), systemImage: "play.fill")
            }
            if !section.stamps.isEmpty {
                Button(action: onReverseClick) {
                    Label {
                        Text(strings.get(id: SharedRes.strings().okt_menu_reverse))
                        Text(strings.get(id: SharedRes.strings().okt_menu_reverse_subtitle))
                    } icon: {
                        Image(systemName: "arrow.left.arrow.right")
                    }
                }
                .accessibilityIdentifier(TestTags.shared.OKT_SECTION_REVERSE)
            }
        } label: {
            Image(systemName: "ellipsis")
                .font(.body.weight(.semibold))
                .foregroundStyle(oktBlue)
                .frame(width: 40, height: 40)
                .background(Circle().fill(oktBlue.opacity(isSelected ? 0.2 : 0.1)))
                .contentShape(Circle())
        }
        .accessibilityLabel(strings.get(id: SharedRes.strings().okt_a11y_section_menu, args: [section.name]))
        .accessibilityIdentifier(TestTags.shared.OKT_SECTION_MENU)
    }

    private var stampCarousel: some View {
        ScrollViewReader { proxy in
            ScrollView(.horizontal, showsIndicators: false) {
                LazyHStack(spacing: 8) {
                    ForEach(stamps, id: \.marker.id) { stamp in
                        OktStampCardView(
                            strings: strings,
                            stamp: stamp,
                            isSelected: stamp.marker.id == selectedMarkerId,
                            onClick: { onStampClick(stamp.marker) }
                        )
                        .id(stamp.marker.id)
                    }
                }
                .padding(.horizontal, 12)
            }
            .padding(.horizontal, -12)
            .onChange(of: selectedMarkerId) { _, markerId in
                guard let markerId else { return }
                withAnimation(.snappy) {
                    proxy.scrollTo(markerId, anchor: .center)
                }
            }
        }
        .frame(height: Dimens.oktStampCardHeight)
    }
}

private struct OktSectionBadgeView: View {
    let prefix: String
    let number: String?
    let isSelected: Bool

    private let oktBlue = Color(SharedRes.colors().oktBlue.getUIColor())
    private let onOktBlue = Color(SharedRes.colors().onOktBlue.getUIColor())

    var body: some View {
        OktBadgeText(prefix: prefix, number: number, fontSize: 12)
            .foregroundStyle(isSelected ? onOktBlue : oktBlue)
            .padding(.horizontal, 4)
            .frame(width: Dimens.oktBadgeSize, height: Dimens.oktBadgeSize)
            .background(Circle().fill(isSelected ? oktBlue : oktBlue.opacity(0.15)))
            .accessibilityHidden(true)
    }
}

private struct OktSectionStatsView: View {
    let strings: Strings
    let routeStats: RouteStats

    var body: some View {
        ViewThatFits(in: .horizontal) {
            stats(font: .subheadline, spacing: 10)
            stats(font: .footnote, spacing: 8)
            stats(font: .caption, spacing: 6)
            stats(font: .caption2, spacing: 4)
        }
        .foregroundStyle(.secondary)
    }

    private func stats(font: Font, spacing: CGFloat) -> some View {
        let travelTime = TravelTimeFormatter.shared.formatTravelTimeCompact(duration: routeStats.travelTime)
        return HStack(spacing: spacing) {
            stat(systemImage: "clock.fill", value: travelTime)
            stat(
                systemImage: "location.fill",
                value: DistanceFormatter.shared.formatDistance(distance: routeStats.distance)
            )
            stat(
                systemImage: "chart.line.uptrend.xyaxis",
                value: DistanceFormatter.shared.formatElevation(meters: routeStats.incline)
            )
            stat(
                systemImage: "chart.line.downtrend.xyaxis",
                value: DistanceFormatter.shared.formatElevation(meters: routeStats.decline)
            )
        }
        .font(font)
        .lineLimit(1)
        .minimumScaleFactor(0.8)
    }

    private func stat(systemImage: String, value: String) -> some View {
        HStack(spacing: 2) {
            Image(systemName: systemImage)
                .font(.caption2.weight(.semibold))
                .imageScale(.small)
            Text(value)
        }
    }
}
