import Shared
import SwiftUI

struct OktStampCardView: View {
    let strings: Strings
    let stamp: OktStampItem
    let isSelected: Bool
    let onClick: () -> Void

    private let oktBlue = Color(SharedRes.colors().oktBlue.getUIColor())
    private let onOktBlue = Color(SharedRes.colors().onOktBlue.getUIColor())

    var body: some View {
        Button(action: onClick) {
            VStack(alignment: .leading, spacing: 4) {
                HStack(alignment: .top) {
                    SharedRes.images().ic_okt_stamp.swiftUIImage
                        .renderingMode(.template)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 16, height: 16)
                        .foregroundStyle(oktBlue)
                        .frame(width: Dimens.oktStampIconSize, height: Dimens.oktStampIconSize)
                        .background(Circle().fill(isSelected ? onOktBlue : oktBlue.opacity(0.15)))
                    Spacer(minLength: 4)
                    Text(stamp.legDistance)
                        .font(.caption.weight(.semibold))
                        .foregroundStyle(secondaryStyle)
                        .lineLimit(1)
                        .accessibilityLabel(
                            strings.get(id: SharedRes.strings().okt_a11y_stamp_distance, args: [stamp.legDistance])
                        )
                }
                Spacer(minLength: 4)
                Text(stamp.marker.name)
                    .font(.subheadline.weight(.semibold))
                    .foregroundStyle(isSelected ? AnyShapeStyle(onOktBlue) : AnyShapeStyle(.primary))
                    .lineLimit(2)
                    .minimumScaleFactor(0.8)
                Text(stamp.tag)
                    .font(.caption2.monospaced())
                    .foregroundStyle(secondaryStyle)
                    .lineLimit(1)
                    .truncationMode(.middle)
            }
            .padding(10)
            .frame(width: Dimens.oktStampCardWidth, height: Dimens.oktStampCardHeight, alignment: .topLeading)
            .background(
                isSelected ? oktBlue : Color(.systemBackground),
                in: RoundedRectangle(cornerRadius: 16, style: .continuous)
            )
            .animation(.easeInOut(duration: 0.15), value: isSelected)
        }
        .buttonStyle(PressFeedbackButtonStyle())
        .accessibilityElement(children: .combine)
        .accessibilityAddTraits(isSelected ? .isSelected : [])
        .accessibilityIdentifier(TestTags.shared.OKT_STAMP_CARD)
    }

    private var secondaryStyle: AnyShapeStyle {
        isSelected ? AnyShapeStyle(onOktBlue.opacity(0.8)) : AnyShapeStyle(.secondary)
    }
}

#Preview {
    HStack {
        OktStampCardView(
            strings: Strings(),
            stamp: OktStampItem(
                marker: OktMarker(
                    id: "OKTPH_02",
                    location: Location(latitude: 47.33, longitude: 16.5, altitude: nil),
                    type: .stamp,
                    name: "Hét-forrás",
                    description: nil
                ),
                tag: "OKTPH_02",
                legDistance: "+8 km"
            ),
            isSelected: true,
            onClick: {}
        )
        OktStampCardView(
            strings: Strings(),
            stamp: OktStampItem(
                marker: OktMarker(
                    id: "OKTPH_01_DDKPH_01_2",
                    location: Location(latitude: 47.35, longitude: 16.43, altitude: nil),
                    type: .stamp,
                    name: "Írott-kő",
                    description: nil
                ),
                tag: "OKTPH_01_DDKPH_01_2",
                legDistance: "+43 m"
            ),
            isSelected: false,
            onClick: {}
        )
    }
    .padding()
    .background(Color(SharedRes.colors().oktBlue.getUIColor()).opacity(0.15))
}
