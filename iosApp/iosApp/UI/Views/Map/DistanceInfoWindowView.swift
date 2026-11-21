import Shared
import SwiftUI

struct DistanceInfoWindowView: View {
    let info: DistanceInfoWindowData

    @State private var isDescriptionExpanded = false

    private let strings = Strings()
    private let primaryColor = Color(SharedRes.colors().primaryStrong.getUIColor())

    var body: some View {
        let shape = InfoWindowShape(
            cornerRadius: Dimens.infoWindowCornerRadius,
            tailWidth: Dimens.infoWindowTailWidth,
            tailHeight: Dimens.infoWindowTailHeight
        )
        VStack(alignment: hasDescription ? .leading : .center, spacing: Dimens.infoWindowLineSpacing) {
            if hasDescription {
                HStack(spacing: Dimens.infoWindowCommentButtonSpacing) {
                    VStack(alignment: .leading, spacing: Dimens.infoWindowLineSpacing) {
                        titles
                        details
                    }
                    commentButton
                }
            } else {
                titles
                details
            }
            if isDescriptionShown, let description = info.description_ {
                DistanceInfoWindowDescription(text: description)
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
        // A comment button stays its own element, which combining would swallow
        .accessibilityElement(children: hasDescription ? .contain : .combine)
        .accessibilityIdentifier(TestTags.shared.MAP_DISTANCE_INFO_WINDOW)
    }

    private var hasDescription: Bool {
        info.description_ != nil
    }

    private var isDescriptionShown: Bool {
        hasDescription && isDescriptionExpanded
    }

    @ViewBuilder
    private var titles: some View {
        if let name = info.name {
            titleText(name)
        }
        if let label = info.label {
            titleText(strings.get(desc: label))
        }
    }

    @ViewBuilder
    private var details: some View {
        if let distance = info.distance, let travelTime = info.travelTime {
            HStack(spacing: 4) {
                Text(distance)
                    .fontWeight(.bold)
                    .foregroundStyle(primaryColor)
                Text(verbatim: "·")
                    .accessibilityHidden(true)
                Text(strings.get(desc: travelTime))
            }
            .font(.system(size: Dimens.infoWindowDetailsFontSize, weight: .medium))
            .foregroundStyle(Color(.secondaryLabel))
        }
    }

    private var commentButton: some View {
        Button {
            isDescriptionExpanded.toggle()
        } label: {
            Image(systemName: isDescriptionExpanded ? "text.bubble.fill" : "text.bubble")
                .font(.system(size: Dimens.infoWindowCommentIconSize, weight: .medium))
                .foregroundStyle(primaryColor)
                .frame(width: Dimens.infoWindowCommentButtonSize, height: Dimens.infoWindowCommentButtonSize)
                .background(primaryColor.opacity(0.12), in: .circle)
                .contentShape(.circle)
        }
        .buttonStyle(.plain)
        .accessibilityLabel(
            strings.get(
                id: isDescriptionExpanded
                    ? SharedRes.strings().gpx_distance_info_window_a11y_hide_comment
                    : SharedRes.strings().gpx_distance_info_window_a11y_show_comment
            )
        )
        .accessibilityIdentifier(TestTags.shared.MAP_DISTANCE_INFO_WINDOW_COMMENT_BUTTON)
    }

    private func titleText(_ text: String) -> some View {
        Text(text)
            .font(.system(size: Dimens.infoWindowNameFontSize, weight: .bold))
            .foregroundStyle(Color(.label))
            .multilineTextAlignment(hasDescription ? .leading : .center)
            .lineLimit(2)
            .modifier(WrappingWidth(maxWidth: Dimens.infoWindowNameMaxWidth))
    }
}

// Mapbox resizes the annotation at once, so only the text fades in instead of animating the layout
private struct DistanceInfoWindowDescription: View {
    let text: String

    @State private var isVisible = false

    var body: some View {
        Text(text)
            .font(.system(size: Dimens.infoWindowDetailsFontSize))
            .foregroundStyle(Color(.secondaryLabel))
            .lineLimit(4)
            .modifier(WrappingWidth(maxWidth: Dimens.infoWindowDescriptionMaxWidth))
            .opacity(isVisible ? 1 : 0)
            .onAppear {
                withAnimation(.easeOut(duration: 0.2)) { isVisible = true }
            }
    }
}

// Mapbox measures view annotations with an unspecified size, so the text gets a real width to wrap within
private struct WrappingWidth: ViewModifier {
    let maxWidth: CGFloat

    func body(content: Content) -> some View {
        WrappingWidthLayout(maxWidth: maxWidth) { content }
    }
}

private struct WrappingWidthLayout: Layout {
    let maxWidth: CGFloat

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        guard let subview = subviews.first else { return .zero }
        return subview.sizeThatFits(ProposedViewSize(width: width(of: subview, proposal: proposal), height: nil))
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        guard let subview = subviews.first else { return }
        subview.place(
            at: CGPoint(x: bounds.midX, y: bounds.midY),
            anchor: .center,
            proposal: ProposedViewSize(width: bounds.width, height: bounds.height)
        )
    }

    private func width(of subview: LayoutSubview, proposal: ProposedViewSize) -> CGFloat {
        min(subview.sizeThatFits(.unspecified).width, proposal.width ?? maxWidth, maxWidth)
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
                priority: 3,
                description: nil
            )
        )
        DistanceInfoWindowView(
            info: DistanceInfoWindowData(
                id: 3,
                location: Location(latitude: 47.5, longitude: 19.0, altitude: nil),
                name: "Rám hegy",
                label: nil,
                distance: "3.1 km",
                travelTime: RawStringDesc(string: "54 perc"),
                priority: 1,
                description: "Forrás a nyereg alatt, itt érdemes vizet tölteni."
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
                priority: 2,
                description: nil
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
                priority: 1,
                description: nil
            )
        )
    }
}
