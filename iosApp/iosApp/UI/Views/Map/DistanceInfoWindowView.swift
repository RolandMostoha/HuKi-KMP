import Shared
import SwiftUI

struct DistanceInfoWindowView: View {
    let info: DistanceInfoWindowData

    private let strings = Strings()

    var body: some View {
        let shape = InfoWindowShape(
            cornerRadius: Dimens.infoWindowCornerRadius,
            tailWidth: Dimens.infoWindowTailWidth,
            tailHeight: Dimens.infoWindowTailHeight
        )
        VStack(spacing: Dimens.infoWindowLineSpacing) {
            if let name = info.name {
                titleText(name)
            }
            if let label = info.label {
                titleText(strings.get(desc: label))
            }
            if let distance = info.distance, let travelTime = info.travelTime {
                HStack(spacing: 4) {
                    Text(distance)
                        .fontWeight(.bold)
                        .foregroundStyle(Color(SharedRes.colors().primaryStrong.getUIColor()))
                    Text(verbatim: "·")
                        .accessibilityHidden(true)
                    Text(strings.get(desc: travelTime))
                }
                .font(.system(size: Dimens.infoWindowDetailsFontSize, weight: .medium))
                .foregroundStyle(Color(.secondaryLabel))
            }
        }
        .padding(.horizontal, Dimens.infoWindowHorizontalPadding)
        .padding(.vertical, Dimens.infoWindowVerticalPadding)
        .padding(.bottom, Dimens.infoWindowTailHeight)
        .background(
            shape
                .fill(Color(.systemBackground))
                .shadow(color: .black.opacity(0.2), radius: 6, y: 2)
        )
        .accessibilityElement(children: .combine)
        .accessibilityIdentifier(TestTags.shared.MAP_DISTANCE_INFO_WINDOW)
    }

    private func titleText(_ text: String) -> some View {
        Text(text)
            .font(.system(size: Dimens.infoWindowNameFontSize, weight: .bold))
            .foregroundStyle(Color(.label))
            .multilineTextAlignment(.center)
            .lineLimit(2)
            .frame(maxWidth: Dimens.infoWindowNameMaxWidth)
            .fixedSize(horizontal: false, vertical: true)
    }
}

struct InfoWindowShape: Shape {
    let cornerRadius: CGFloat
    let tailWidth: CGFloat
    let tailHeight: CGFloat

    func path(in rect: CGRect) -> Path {
        let radius = cornerRadius
        let bodyBottom = rect.maxY - tailHeight
        let tipY = rect.maxY
        let tailHalf = tailWidth / 2
        var path = Path()
        path.move(to: CGPoint(x: rect.minX, y: rect.minY + radius))
        path.addArc(
            tangent1End: CGPoint(x: rect.minX, y: rect.minY),
            tangent2End: CGPoint(x: rect.minX + radius, y: rect.minY),
            radius: radius
        )
        path.addArc(
            tangent1End: CGPoint(x: rect.maxX, y: rect.minY),
            tangent2End: CGPoint(x: rect.maxX, y: rect.minY + radius),
            radius: radius
        )
        path.addArc(
            tangent1End: CGPoint(x: rect.maxX, y: bodyBottom),
            tangent2End: CGPoint(x: rect.maxX - radius, y: bodyBottom),
            radius: radius
        )
        path.addLine(to: CGPoint(x: rect.midX + tailHalf, y: bodyBottom))
        path.addLine(to: CGPoint(x: rect.midX, y: tipY))
        path.addLine(to: CGPoint(x: rect.midX - tailHalf, y: bodyBottom))
        path.addArc(
            tangent1End: CGPoint(x: rect.minX, y: bodyBottom),
            tangent2End: CGPoint(x: rect.minX, y: bodyBottom - radius),
            radius: radius
        )
        path.addLine(to: CGPoint(x: rect.minX, y: rect.minY + radius))
        path.closeSubpath()
        return path
    }
}

#Preview {
    VStack(spacing: 16) {
        DistanceInfoWindowView(
            info: DistanceInfoWindowData(
                id: 0,
                location: Location(latitude: 47.5, longitude: 19.0, altitude: nil),
                name: "Dobogókő",
                label: nil,
                distance: "4.2 km",
                travelTime: RawStringDesc(string: "1 h 20 min"),
                priority: 3
            )
        )
        DistanceInfoWindowView(
            info: DistanceInfoWindowData(
                id: 1,
                location: Location(latitude: 47.5, longitude: 19.0, altitude: nil),
                name: "Rám-szakadék alsó bejárata, Dömös felől",
                label: ResourceStringDesc(stringRes: SharedRes.strings().gpx_distance_info_window_start),
                distance: nil,
                travelTime: nil,
                priority: 2
            )
        )
        DistanceInfoWindowView(
            info: DistanceInfoWindowData(
                id: 2,
                location: Location(latitude: 47.5, longitude: 19.0, altitude: nil),
                name: nil,
                label: nil,
                distance: "850 m",
                travelTime: RawStringDesc(string: "15 min"),
                priority: 1
            )
        )
    }
}
