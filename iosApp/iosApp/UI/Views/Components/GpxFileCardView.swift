import Shared
import SwiftUI

struct GpxFileCardView: View {
    let strings: Strings
    let file: GpxFileItem
    let onOpen: () -> Void
    let onRename: () -> Void
    let onShare: () -> Void
    let onDelete: () -> Void

    var body: some View {
        Button(action: onOpen) {
            VStack(spacing: 8) {
                HStack(alignment: .center, spacing: 12) {
                    VStack(alignment: .leading, spacing: 6) {
                        Text(file.fileName)
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundStyle(.primary)
                            .lineLimit(1)
                        if let title = file.title {
                            Text(title)
                                .font(.system(size: 12))
                                .foregroundStyle(.secondary)
                                .lineLimit(1)
                        }
                        originChip
                    }
                    Spacer(minLength: 0)
                    GpxFileOptionsMenuView(
                        strings: strings,
                        onRename: onRename,
                        onShare: onShare,
                        onDelete: onDelete
                    )
                }
                .padding(.leading, 16)
                .padding(.trailing, 6)
                statsRow
                    .padding(.horizontal, 16)
                    .padding(.top, 4)
            }
            .padding(.vertical, 14)
            .contentShape(Rectangle())
        }
        .buttonStyle(PressFeedbackButtonStyle())
        .accessibilityIdentifier(TestTags.shared.GPX_COLLECTION_ITEM)
    }

    private var originChip: some View {
        HStack(spacing: 4) {
            Image(systemName: originSystemImage)
                .font(.system(size: 11, weight: .semibold))
            Text(strings.get(id: originLabel))
                .font(.system(size: 12, weight: .semibold))
        }
        .foregroundStyle(Color(SharedRes.colors().onSecondary.getUIColor()))
        .padding(.horizontal, 10)
        .padding(.vertical, 4)
        .background(
            Capsule().fill(Color(SharedRes.colors().secondary.getUIColor()))
        )
    }

    private var originSystemImage: String {
        switch file.origin {
        case .routePlanner: return "hand.tap.fill"
        default: return "square.and.arrow.down.fill"
        }
    }

    private var originLabel: StringResource {
        switch file.origin {
        case .routePlanner: return SharedRes.strings().gpx_collection_origin_route_planner
        default: return SharedRes.strings().gpx_collection_origin_external
        }
    }

    private var statsRow: some View {
        HStack(spacing: 8) {
            StatChipView(
                systemImage: "clock.fill",
                value: strings.get(desc: TravelTimeFormatter.shared.formatTravelTime(duration: file.travelTime))
            )
            StatChipView(
                systemImage: "location.fill",
                value: DistanceFormatter.shared.formatDistance(distance: file.totalDistance)
            )
            StatChipView(
                systemImage: "chart.line.uptrend.xyaxis",
                value: DistanceFormatter.shared.formatMeters(meters: file.incline)
            )
            StatChipView(
                systemImage: "chart.line.downtrend.xyaxis",
                value: DistanceFormatter.shared.formatMeters(meters: file.decline)
            )
        }
    }
}
