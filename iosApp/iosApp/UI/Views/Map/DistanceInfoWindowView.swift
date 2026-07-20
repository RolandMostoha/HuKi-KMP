import Shared
import SwiftUI

struct DistanceInfoWindowView: View {
    let info: DistanceInfoWindowData

    private let strings = Strings()

    var body: some View {
        let shape = DistanceInfoWindowShape(
            cornerRadius: Dimens.infoWindowCornerRadius,
            tailWidth: Dimens.infoWindowTailWidth,
            tailHeight: Dimens.infoWindowTailHeight,
            lineWidth: Dimens.infoWindowBorder
        )
        VStack(spacing: 0) {
            Text(info.distance)
                .font(.system(size: 13, weight: .bold))
                .foregroundStyle(Color(SharedRes.colors().primaryStrong.getUIColor()))
            Text(strings.get(desc: info.travelTime))
                .font(.system(size: 11))
                .foregroundStyle(Color(.secondaryLabel))
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 6)
        .padding(.bottom, Dimens.infoWindowTailHeight)
        .background(shape.fill(Color(.systemBackground)))
        .overlay(
            shape.stroke(
                Color(SharedRes.colors().mapStroke.getUIColor()),
                style: StrokeStyle(lineWidth: Dimens.infoWindowBorder, lineCap: .round, lineJoin: .round)
            )
        )
        .accessibilityElement(children: .combine)
        .accessibilityLabel(strings.get(id: SharedRes.strings().gpx_distance_info_window_a11y))
        .accessibilityIdentifier(TestTags.shared.MAP_DISTANCE_INFO_WINDOW)
        // Mapbox is light-only, so keep the window in its light appearance regardless of app theme
        .environment(\.colorScheme, .light)
    }
}

private struct DistanceInfoWindowShape: Shape {
    let cornerRadius: CGFloat
    let tailWidth: CGFloat
    let tailHeight: CGFloat
    let lineWidth: CGFloat

    func path(in rect: CGRect) -> Path {
        let radius = cornerRadius
        let bodyBottom = rect.maxY - tailHeight
        let tipY = rect.maxY - lineWidth / 2
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
    DistanceInfoWindowView(
        info: DistanceInfoWindowData(
            location: Location(latitude: 47.5, longitude: 19.0, altitude: nil),
            distance: "4.2 km",
            travelTime: RawStringDesc(string: "1 h 20 min")
        )
    )
}
