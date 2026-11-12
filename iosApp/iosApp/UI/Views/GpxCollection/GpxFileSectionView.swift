import Shared
import SwiftUI

struct GpxFileSectionView: View {
    let strings: Strings
    let section: GpxFileSection
    let onEvent: (GpxCollectionUiEvents) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            header
            VStack(spacing: 0) {
                ForEach(Array(section.items.enumerated()), id: \.element.file.fileUri) { index, item in
                    let file = item.file
                    GpxFileCardView(
                        strings: strings,
                        file: file,
                        landscape: item.landscape,
                        onOpen: { onEvent(GpxCollectionUiEventsFileClicked(file: file)) },
                        onReplan: { onEvent(GpxCollectionUiEventsReplanClicked(file: file)) },
                        onRename: {},
                        onShare: { onEvent(GpxCollectionUiEventsShareClicked(file: file)) },
                        onDelete: { onEvent(GpxCollectionUiEventsDeleteClicked(file: file)) }
                    )
                    if index != section.items.count - 1 {
                        Divider().padding(.leading, 16)
                    }
                }
            }
            .background(
                Color(.secondarySystemGroupedBackground),
                in: RoundedRectangle(cornerRadius: 16, style: .continuous)
            )
        }
        .padding(.horizontal, 16)
        .padding(.top, 12)
    }

    private var header: some View {
        HStack {
            Text(headerText)
                .font(.subheadline.weight(.semibold))
                .foregroundStyle(.secondary)
                .textCase(.uppercase)
            Spacer(minLength: 8)
            if let trackCount {
                Text(trackCount)
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            }
        }
        .padding(.horizontal, 4)
        .accessibilityElement(children: .combine)
        .accessibilityIdentifier(TestTags.shared.GPX_COLLECTION_SECTION_HEADER)
    }

    private var trackCount: String? {
        if case .day = onEnum(of: section.header) { return nil }
        let count = section.items.count
        let res = count == 1
            ? SharedRes.strings().gpx_collection_subtitle_single
            : SharedRes.strings().gpx_collection_subtitle_pattern
        return strings.get(id: res, args: [count])
    }

    private var headerText: String {
        switch onEnum(of: section.header) {
        case .day(let header):
            return dayText(header.day)
        case .distance(let header):
            return strings.get(id: header.range.label)
        case .landscape(let header):
            return strings.get(id: header.landscape.nameRes)
        case .outsideLandscapes:
            return strings.get(id: SharedRes.strings().gpx_collection_landscape_other)
        }
    }

    private func dayText(_ day: GpxFileDay) -> String {
        switch onEnum(of: day) {
        case .today:
            return strings.get(id: SharedRes.strings().gpx_collection_date_today)
        case .yesterday:
            return strings.get(id: SharedRes.strings().gpx_collection_date_yesterday)
        case .date(let date):
            return date.label
        }
    }
}

private extension GpxDistanceRange {
    var label: StringResource {
        switch self {
        case .over20: return SharedRes.strings().gpx_collection_distance_over_20
        case .from10To20: return SharedRes.strings().gpx_collection_distance_10_20
        case .from5To10: return SharedRes.strings().gpx_collection_distance_5_10
        case .under5: return SharedRes.strings().gpx_collection_distance_under_5
        }
    }
}

extension [GpxFileSection] {
    /// Sorts the landscape sections by their localized name, keeping the other ones last.
    func sortedByLandscapeName(strings: Strings) -> [GpxFileSection] {
        let landscapes = filter { $0.header is GpxFileHeaderLandscape }
        let others = filter { !($0.header is GpxFileHeaderLandscape) }
        return landscapes.sorted { first, second in
            first.landscapeName(strings: strings).localizedStandardCompare(second.landscapeName(strings: strings))
                == .orderedAscending
        } + others
    }
}

private extension GpxFileSection {
    func landscapeName(strings: Strings) -> String {
        guard let header = header as? GpxFileHeaderLandscape else { return "" }
        return strings.get(id: header.landscape.nameRes)
    }
}
