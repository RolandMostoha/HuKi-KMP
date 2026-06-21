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
                    }
                    Spacer(minLength: 0)
                    GpxFileOptionsMenuView(
                        strings: strings,
                        onRename: onRename,
                        onShare: onShare,
                        onDelete: onDelete
                    )
                }
                .padding(.leading, 18)
                .padding(.trailing, 6)
                statsRow
                    .padding(.horizontal, 16)
            }
            .padding(.vertical, 14)
            .contentShape(Rectangle())
        }
        .buttonStyle(PressFeedbackButtonStyle())
        .accessibilityIdentifier(TestTags.shared.GPX_COLLECTION_ITEM)
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
