import Shared
import SwiftUI

struct RecentGpxSectionView: View {
    let strings: Strings
    let files: [GpxFileItem]
    let onFileSelected: (GpxFileItem) -> Void
    let onSeeAllClicked: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            SectionHeaderView(
                title: strings.get(id: SharedRes.strings().search_recent_gpx_title),
                actionText: strings.get(id: SharedRes.strings().see_all),
                onActionClick: onSeeAllClicked,
                actionAccessibilityId: TestTags.shared.RECENT_GPX_SEE_ALL_BUTTON
            )
            VStack(spacing: 0) {
                ForEach(Array(files.enumerated()), id: \.offset) { index, file in
                    RecentGpxItemView(
                        strings: strings,
                        file: file,
                        onClick: { onFileSelected(file) }
                    )
                    if index < files.count - 1 {
                        Divider()
                            .padding(.leading, 70)
                    }
                }
            }
            .padding(.vertical, 2)
            .background(Color(.systemBackground))
            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
            .padding(.horizontal, 16)
        }
        .padding(.top, Dimens.sectionSpacing)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(TestTags.shared.RECENT_GPX_SECTION)
    }
}

private struct RecentGpxItemView: View {
    let strings: Strings
    let file: GpxFileItem
    let onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            HStack(spacing: 14) {
                ZStack {
                    Circle()
                        .fill(SwiftUI.Color(SharedRes.colors().primary.getUIColor()))
                        .frame(width: 40, height: 40)
                    Image(uiImage: SharedRes.images().ic_gpx.toUIImage()!)
                        .renderingMode(.template)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 22, height: 22)
                        .foregroundStyle(.white)
                }
                VStack(alignment: .leading, spacing: 8) {
                    Text(file.fileName)
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundStyle(.primary)
                        .lineLimit(1)
                    statsRow
                }
                Spacer(minLength: 0)
            }
            .padding(.leading, 16)
            .padding(.trailing, 8)
            .padding(.vertical, 12)
            .contentShape(Rectangle())
        }
        .buttonStyle(PressFeedbackButtonStyle())
        .accessibilityIdentifier(TestTags.shared.RECENT_GPX_ITEM)
    }

    private var statsRow: some View {
        HStack(spacing: 13) {
            gpxStat(
                systemImage: "clock.fill",
                value: strings.get(desc: TravelTimeFormatter.shared.formatTravelTime(duration: file.travelTime))
            )
            gpxStat(
                systemImage: "location.fill",
                value: DistanceFormatter.shared.formatDistance(distance: file.totalDistance)
            )
            gpxStat(
                systemImage: "chart.line.uptrend.xyaxis",
                value: DistanceFormatter.shared.formatMeters(meters: file.incline)
            )
            gpxStat(
                systemImage: "chart.line.downtrend.xyaxis",
                value: DistanceFormatter.shared.formatMeters(meters: file.decline)
            )
        }
    }

    private func gpxStat(systemImage: String, value: String) -> some View {
        HStack(spacing: 3) {
            Image(systemName: systemImage)
                .font(.system(size: 11))
                .foregroundStyle(.secondary)
            Text(value)
                .font(.system(size: 12))
                .foregroundStyle(.secondary)
                .lineLimit(1)
        }
    }
}
