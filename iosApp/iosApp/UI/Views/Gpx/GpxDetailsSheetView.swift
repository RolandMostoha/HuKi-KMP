import Shared
import SwiftUI

struct GpxDetailsSheetView: View {
    let strings: Strings
    let gpxDetails: GpxDetails
    let isRouteVisible: Bool
    let onStartClick: () -> Void
    let onRouteVisibilityClick: () -> Void
    let onNavigateToStart: () -> Void
    let onNavigateToEnd: () -> Void
    let onShareClick: () -> Void
    let onReverseDirectionClick: () -> Void
    let onDismissRequest: () -> Void
    /// Collapsed sheet height: everything down to the route visibility button plus a peek of the next one.
    var onCollapsedHeightChange: (CGFloat) -> Void = { _ in }
    /// Expanded sheet height: the whole content.
    var onExpandedHeightChange: (CGFloat) -> Void = { _ in }

    private static let contentSpace = "gpxDetailsContent"

    @State private var routeVisibilityButtonMaxY: CGFloat = 0
    @State private var contentHeight: CGFloat = 0
    @State private var bottomSafeAreaInset: CGFloat = 0

    private var hasStartNavigation: Bool {
        gpxDetails.mapsNavigationTarget(type: .start) != nil
    }

    private var hasEndNavigation: Bool {
        gpxDetails.mapsNavigationTarget(type: .end) != nil
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                header
                RouteStatsRowView(strings: strings, routeStats: gpxDetails.toRouteStats())
                VStack(spacing: 12) {
                    PrimaryButton(
                        icon: .system("location.north.fill"),
                        title: strings.get(id: SharedRes.strings().gpx_details_start),
                        action: onStartClick
                    )
                    .accessibilityIdentifier(TestTags.shared.GPX_DETAILS_START_BUTTON)
                    SecondaryButton(
                        icon: .system(isRouteVisible ? "eye.slash" : "eye"),
                        title: strings.get(
                            id: isRouteVisible
                                ? SharedRes.strings().gpx_control_route_visibility_hide
                                : SharedRes.strings().gpx_control_route_visibility_show
                        ),
                        action: onRouteVisibilityClick
                    )
                    .accessibilityIdentifier(TestTags.shared.GPX_DETAILS_ROUTE_VISIBILITY_BUTTON)
                    .onGeometryChange(for: CGFloat.self) {
                        $0.frame(in: .named(Self.contentSpace)).maxY
                    } action: { maxY in
                        routeVisibilityButtonMaxY = maxY
                    }
                    if gpxDetails.isReversible {
                        SecondaryButton(
                            icon: .system("arrow.left.arrow.right"),
                            title: strings.get(id: SharedRes.strings().gpx_control_direction_reverse),
                            action: onReverseDirectionClick
                        )
                        .accessibilityIdentifier(TestTags.shared.GPX_DETAILS_REVERSE_DIRECTION_BUTTON)
                    }
                    navigationButtons
                    SecondaryButton(
                        icon: .system("square.and.arrow.up"),
                        title: strings.get(id: SharedRes.strings().gpx_details_share),
                        action: onShareClick
                    )
                    .accessibilityIdentifier(TestTags.shared.GPX_DETAILS_SHARE_BUTTON)
                }
            }
            .padding(.top, 24)
            .padding(.horizontal, 16)
            .padding(.bottom, 16)
            .coordinateSpace(.named(Self.contentSpace))
            .onGeometryChange(for: CGFloat.self) { $0.size.height } action: { height in
                contentHeight = height
            }
        }
        .scrollBounceBehavior(.basedOnSize)
        .onGeometryChange(for: CGFloat.self) {
            $0.safeAreaInsets.bottom
        } action: { bottom in
            bottomSafeAreaInset = bottom
        }
        .onChange(of: collapsedHeight) {
            guard routeVisibilityButtonMaxY > 0, contentHeight > 0 else { return }
            onCollapsedHeightChange(collapsedHeight)
        }
        .onChange(of: expandedHeight) {
            guard contentHeight > 0 else { return }
            onExpandedHeightChange(expandedHeight)
        }
    }

    // Content keeps showing through the bottom safe area below the detent height.
    private var expandedHeight: CGFloat {
        contentHeight - bottomSafeAreaInset
    }

    private var collapsedHeight: CGFloat {
        min(routeVisibilityButtonMaxY + Dimens.gpxDetailsCollapsedPeek - bottomSafeAreaInset, expandedHeight)
    }

    private var header: some View {
        ZStack(alignment: .topTrailing) {
            VStack(spacing: 4) {
                if gpxDetails.title != nil {
                    Text(gpxDetails.fileName)
                        .font(.system(size: 14, weight: .regular))
                        .foregroundStyle(Color(SharedRes.colors().primary.getUIColor()))
                        .lineLimit(2)
                        .multilineTextAlignment(.leading)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
                HStack(spacing: 8) {
                    if let jelSymbol = gpxDetails.jelSymbol {
                        Image(uiImage: jelSymbol.icon.toUIImage()!)
                            .resizable()
                            .frame(width: Dimens.gpxDetailsJelSymbolSize, height: Dimens.gpxDetailsJelSymbolSize)
                            .accessibilityHidden(true)
                    }
                    Text(gpxDetails.title ?? gpxDetails.fileName)
                        .font(.system(size: 16, weight: .bold))
                        .lineLimit(2)
                        .multilineTextAlignment(.leading)
                        .accessibilityIdentifier(TestTags.shared.GPX_DETAILS_TITLE)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            .frame(maxWidth: .infinity)
            .padding(.trailing, 56)
            CloseButton(
                action: onDismissRequest,
                accessibilityIdentifier: TestTags.shared.GPX_DETAILS_CLOSE_BUTTON
            )
        }
    }

    @ViewBuilder
    private var navigationButtons: some View {
        VStack(spacing: 12) {
            if hasStartNavigation {
                NavigationButton(
                    title: strings.get(id: SharedRes.strings().gpx_details_navigation_to_start),
                    action: onNavigateToStart
                )
                .accessibilityIdentifier(TestTags.shared.GPX_DETAILS_NAV_START_BUTTON)
            }
            if hasEndNavigation {
                NavigationButton(
                    title: strings.get(id: SharedRes.strings().gpx_details_navigation_to_end),
                    action: onNavigateToEnd
                )
                .accessibilityIdentifier(TestTags.shared.GPX_DETAILS_NAV_END_BUTTON)
            }
        }
    }
}

private struct NavigationButton: View {
    let title: String
    let action: () -> Void

    private var primaryColor: SwiftUI.Color {
        SwiftUI.Color(SharedRes.colors().primary.getUIColor())
    }

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.headline)
                .fontWeight(.semibold)
                .lineLimit(1)
                .frame(maxWidth: .infinity)
                .overlay(alignment: .leading) {
                    Image(uiImage: SharedRes.images().ic_maps_navigation.toUIImage()!)
                        .renderingMode(.template)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 22, height: 22)
                }
                .padding(.horizontal, 24)
                .padding(.vertical, 14)
                .foregroundStyle(primaryColor)
                .background(SwiftUI.Color(SharedRes.colors().primaryContainer.getUIColor()), in: .capsule)
        }
        .buttonStyle(PressFeedbackButtonStyle())
    }
}
