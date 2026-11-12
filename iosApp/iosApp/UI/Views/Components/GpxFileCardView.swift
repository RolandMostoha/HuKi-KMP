import Shared
import SwiftUI

struct GpxFileCardView: View {
    let strings: Strings
    let file: GpxFileItem
    let landscape: GpxLandscape?
    let onOpen: () -> Void
    let onReplan: () -> Void
    let onRename: () -> Void
    let onShare: () -> Void
    let onDelete: () -> Void

    var body: some View {
        Button(action: onOpen) {
            VStack(spacing: 8) {
                HStack(alignment: .center, spacing: 12) {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(file.fileName)
                            .font(.system(size: 17, weight: .semibold))
                            .foregroundStyle(.primary)
                            .lineLimit(1)
                        if let title = file.title {
                            Text(title)
                                .font(.system(size: 14))
                                .foregroundStyle(.secondary)
                                .lineLimit(1)
                        }
                    }
                    Spacer(minLength: 0)
                    GpxFileOptionsMenuView(
                        strings: strings,
                        isReplanVisible: file.isReplannable,
                        onReplan: onReplan,
                        onRename: onRename,
                        onShare: onShare,
                        onDelete: onDelete
                    )
                }
                .padding(.leading, 16)
                .padding(.trailing, 12)
                statsRow
                    .padding(.horizontal, 16)
                tagRow
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 16)
                    .padding(.top, 2)
            }
            .padding(.vertical, 14)
            .contentShape(Rectangle())
        }
        .buttonStyle(PressFeedbackButtonStyle())
        .accessibilityIdentifier(TestTags.shared.GPX_COLLECTION_ITEM)
    }

    private var tagRow: some View {
        FlowLayout(spacing: 6) {
            tag(
                icon: Image(systemName: originSystemImage).font(.system(size: 13, weight: .semibold)),
                text: strings.get(id: originLabel),
                foreground: Color(SharedRes.colors().onSecondary.getUIColor()),
                background: Color(SharedRes.colors().secondary.getUIColor())
            )
            if file.isRoundTrip {
                tag(
                    icon: Image(systemName: "arrow.trianglehead.counterclockwise")
                        .font(.system(size: 13, weight: .semibold)),
                    text: strings.get(id: SharedRes.strings().route_planner_round_trip),
                    foreground: Color(SharedRes.colors().onSecondary.getUIColor()),
                    background: Color(SharedRes.colors().secondary.getUIColor())
                )
                .accessibilityIdentifier(TestTags.shared.GPX_COLLECTION_ITEM_ROUND_TRIP_TAG)
            }
            if let landscape {
                tag(
                    icon: Image(uiImage: landscape.iconRes.toUIImage()!)
                        .renderingMode(.template)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 15, height: 15),
                    text: strings.get(id: landscape.nameRes),
                    foreground: .white,
                    background: Color(landscape.colorRes.getUIColor())
                )
                .accessibilityIdentifier(TestTags.shared.GPX_COLLECTION_ITEM_LANDSCAPE_TAG)
            }
        }
    }

    private func tag(
        icon: some View,
        text: String,
        foreground: SwiftUI.Color,
        background: SwiftUI.Color
    ) -> some View {
        HStack(spacing: 5) {
            icon
            Text(text)
                .font(.system(size: 14, weight: .semibold))
                .lineLimit(1)
        }
        .foregroundStyle(foreground)
        .padding(.horizontal, 12)
        .padding(.vertical, 6)
        .background(Capsule().fill(background))
    }

    private var originSystemImage: String {
        switch file.origin {
        case .routePlanner: return "hand.tap.fill"
        case .bundled: return "point.topright.arrow.triangle.backward.to.point.bottomleft.scurvepath.fill"
        default: return "square.and.arrow.down.fill"
        }
    }

    private var originLabel: StringResource {
        switch file.origin {
        case .routePlanner: return SharedRes.strings().gpx_collection_origin_route_planner
        case .bundled: return SharedRes.strings().gpx_collection_origin_bundled
        default: return SharedRes.strings().gpx_collection_origin_external
        }
    }

    private var statsRow: some View {
        HStack(spacing: 0) {
            StatChipView(
                systemImage: "clock.fill",
                value: strings.get(desc: TravelTimeFormatter.shared.formatTravelTime(duration: file.travelTime)),
                style: .inline
            )
            Spacer(minLength: 8)
            StatChipView(
                systemImage: "location.fill",
                value: DistanceFormatter.shared.formatDistance(distance: file.totalDistance),
                style: .inline
            )
            Spacer(minLength: 8)
            StatChipView(
                systemImage: "chart.line.uptrend.xyaxis",
                value: DistanceFormatter.shared.formatMeters(meters: file.incline),
                style: .inline
            )
            Spacer(minLength: 8)
            StatChipView(
                systemImage: "chart.line.downtrend.xyaxis",
                value: DistanceFormatter.shared.formatMeters(meters: file.decline),
                style: .inline
            )
        }
    }
}
