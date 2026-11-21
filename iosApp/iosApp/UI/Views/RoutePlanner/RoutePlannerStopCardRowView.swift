import Shared
import SwiftUI

struct RoutePlannerStopCardAction: Identifiable {
    let id: String
    let systemImage: String
    let title: String
    let action: () -> Void
}

enum RoutePlannerStopCard {
    static let actionsHeight: CGFloat = 50
    static let actionsSpacing: CGFloat = 4
    static let bottomPadding: CGFloat = 12

    static func height(rowHeight: CGFloat) -> CGFloat {
        rowHeight + actionsSpacing + actionsHeight + bottomPadding
    }
}

struct RoutePlannerStopCardRowView<Icon: View>: View {
    let strings: Strings
    let title: String
    let hasConnectorAbove: Bool
    let hasConnectorBelow: Bool
    let isRemovable: Bool
    var rowHeight: CGFloat = 44
    let actions: [RoutePlannerStopCardAction]
    var onTitleClicked: (() -> Void)?
    let onRemoveClicked: () -> Void
    /// Outside a List the card draws its own background, which a List row takes as its row background
    var isStandalone = false
    @ViewBuilder let icon: () -> Icon

    private let primaryColor = Color(SharedRes.colors().primary.getUIColor())
    private let cardInset: CGFloat = 4
    // Concentric with the inset grouped section's ~26 pt corners
    private let cardCornerRadius: CGFloat = 22

    var body: some View {
        if isStandalone {
            card
                .padding(.horizontal, Self.standalonePadding)
                .background(cardBackground)
        } else {
            card
                .listRowBackground(
                    cardBackground
                        .padding(cardInset)
                        .background(Color(.tertiarySystemFill))
                )
        }
    }

    private var cardBackground: some View {
        RoundedRectangle(cornerRadius: cardCornerRadius, style: .continuous)
            .fill(primaryColor.opacity(0.08))
            .strokeBorder(primaryColor.opacity(0.35), lineWidth: 1)
    }

    private var card: some View {
        HStack(alignment: .top, spacing: 14) {
            VStack(spacing: 0) {
                RoutePlannerWaypointIconColumn(
                    hasConnectorAbove: hasConnectorAbove,
                    hasConnectorBelow: hasConnectorBelow,
                    icon: icon
                )
                .frame(height: rowHeight)
                RoutePlannerWaypointConnector()
                    .opacity(hasConnectorBelow ? 1 : 0)
            }
            .frame(width: 20)
            VStack(spacing: RoutePlannerStopCard.actionsSpacing) {
                titleRow
                HStack(spacing: 10) {
                    ForEach(actions) { action in
                        actionButton(action)
                    }
                }
                .padding(.bottom, RoutePlannerStopCard.bottomPadding)
            }
        }
        .frame(height: RoutePlannerStopCard.height(rowHeight: rowHeight))
        .accessibilityElement(children: .contain)
    }

    private static var standalonePadding: CGFloat { 12 }

    private var titleRow: some View {
        HStack(spacing: 14) {
            Text(title)
                .font(.system(size: 15, weight: .semibold))
                .foregroundStyle(primaryColor)
                .lineLimit(1)
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
                .contentShape(.rect)
                .onTapGesture { onTitleClicked?() }
            if isRemovable {
                Button(action: onRemoveClicked) {
                    Image(systemName: "xmark")
                        .font(.system(size: 15, weight: .regular))
                        .foregroundStyle(Color(.secondaryLabel))
                        .frame(width: 44, height: rowHeight)
                        .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .accessibilityLabel(strings.get(id: SharedRes.strings().route_planner_a11y_delete_stop))
                .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_DELETE_STOP_BUTTON)
            }
        }
        .frame(height: rowHeight)
    }

    private func actionButton(_ action: RoutePlannerStopCardAction) -> some View {
        Button(action: action.action) {
            VStack(spacing: 4) {
                // Symbols differ in their own height, so a shared box keeps the icons and titles on one line
                Image(systemName: action.systemImage)
                    .font(.system(size: 15, weight: .semibold))
                    .frame(height: 18)
                Text(action.title)
                    .font(.system(size: 12, weight: .semibold))
                    .lineLimit(1)
                    .allowsTightening(true)
            }
            .foregroundStyle(primaryColor)
            .frame(maxWidth: .infinity, minHeight: RoutePlannerStopCard.actionsHeight)
            .background(Color(.secondarySystemGroupedBackground), in: .rect(cornerRadius: 12, style: .continuous))
            .contentShape(.rect(cornerRadius: 12, style: .continuous))
        }
        .buttonStyle(.plain)
        .accessibilityIdentifier(action.id)
    }
}

struct RoutePlannerEmptyStopIcon: View {
    var body: some View {
        // Fixed colors like the waypoint markers, so the dashes stay visible on the white fill in dark mode too
        Circle()
            .fill(Color(red: 1, green: 1, blue: 0.97))
            .strokeBorder(Color(white: 0.1), style: StrokeStyle(lineWidth: 1.5, dash: [3, 2.5]))
            .frame(width: 20, height: 20)
    }
}

struct RoutePlannerCollapsedEmptyStopRowView: View {
    let strings: Strings
    let hasConnectorAbove: Bool
    let hasConnectorBelow: Bool
    let isRemovable: Bool
    var rowHeight: CGFloat = 44
    let onClicked: () -> Void
    let onRemoveClicked: () -> Void

    var body: some View {
        HStack(spacing: 14) {
            RoutePlannerWaypointIconColumn(hasConnectorAbove: hasConnectorAbove, hasConnectorBelow: hasConnectorBelow) {
                RoutePlannerEmptyStopIcon()
            }
            Text(strings.get(id: SharedRes.strings().route_planner_add_stop_title))
                .font(.system(size: 15, weight: .semibold))
                .foregroundStyle(Color(SharedRes.colors().primary.getUIColor()))
                .lineLimit(1)
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
                .contentShape(.rect)
                .onTapGesture(perform: onClicked)
                .accessibilityAddTraits(.isButton)
            if isRemovable {
                Button(action: onRemoveClicked) {
                    Image(systemName: "xmark")
                        .font(.system(size: 15, weight: .regular))
                        .foregroundStyle(Color(.secondaryLabel))
                        .frame(width: 44, height: rowHeight)
                        .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .accessibilityLabel(strings.get(id: SharedRes.strings().route_planner_a11y_delete_stop))
                .accessibilityIdentifier(TestTags.shared.ROUTE_PLANNER_DELETE_STOP_BUTTON)
            }
        }
    }
}

#Preview {
    List {
        RoutePlannerStopCardRowView(
            strings: Strings(),
            title: "Új megálló",
            hasConnectorAbove: true,
            hasConnectorBelow: false,
            isRemovable: true,
            actions: [
                RoutePlannerStopCardAction(id: "pick", systemImage: "scope", title: "Kijelölés a térképen") {},
                RoutePlannerStopCardAction(id: "search", systemImage: "magnifyingglass", title: "Kereső") {}
            ],
            onRemoveClicked: {},
            icon: { RoutePlannerEmptyStopIcon() }
        )
        RoutePlannerStopCardRowView(
            strings: Strings(),
            title: "Háromkő-hegy",
            hasConnectorAbove: false,
            hasConnectorBelow: true,
            isRemovable: true,
            actions: [
                RoutePlannerStopCardAction(id: "move", systemImage: "arrow.up.and.down.and.arrow.left.and.right",
                                           title: "Áthelyezés") {},
                RoutePlannerStopCardAction(id: "replace", systemImage: "magnifyingglass", title: "Kereső") {},
                RoutePlannerStopCardAction(id: "rename", systemImage: "bubble.and.pencil", title: "Átnevezés") {}
            ],
            onRemoveClicked: {},
            icon: { Image(systemName: "smallcircle.filled.circle") }
        )
    }
}
