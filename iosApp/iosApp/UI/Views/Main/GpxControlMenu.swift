import Shared
import SwiftUI

struct GpxControlMenu: View {
    let strings: Strings
    var isRouteVisible: Bool = true
    var isAllDistancesVisible: Bool = false
    var onToggleLine: () -> Void = {}
    var onToggleDistances: () -> Void = {}
    var onOverview: () -> Void = {}
    var onClear: () -> Void = {}

    @State private var isExpanded = false
    @Namespace private var glassNamespace

    private let menuGlassID = "gpx_control_menu_glass"
    private let destructiveIconColor = Color(SharedRes.colors().errorOnMap.getUIColor())

    var body: some View {
        ZStack(alignment: .bottomLeading) {
            // Any interaction outside the panel (tap or scroll) collapses the menu
            Color.clear
                .ignoresSafeArea()
                .contentShape(Rectangle())
                .onTapGesture { collapse() }
                .simultaneousGesture(
                    DragGesture(minimumDistance: 10).onChanged { _ in collapse() }
                )
                .allowsHitTesting(isExpanded)
            menuGlass
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottomLeading)
    }

    @ViewBuilder
    private var menuGlass: some View {
        if #available(iOS 26.0, *) {
            GlassEffectContainer(spacing: 24) {
                if isExpanded {
                    gpxPanel
                        .glassEffect(in: RoundedRectangle(cornerRadius: 28, style: .continuous))
                        .glassEffectID(menuGlassID, in: glassNamespace)
                } else {
                    gpxControlFab
                        .glassEffect(.regular.interactive(), in: Circle())
                        .glassEffectID(menuGlassID, in: glassNamespace)
                }
            }
        } else {
            Group {
                if isExpanded {
                    gpxPanel
                        .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 28, style: .continuous))
                } else {
                    gpxControlFab
                        .background(.ultraThinMaterial, in: Circle())
                }
            }
        }
    }

    private var gpxControlFab: some View {
        Button {
            expand()
        } label: {
            Image(systemName: "slider.horizontal.3")
                .fontWeight(.bold)
                .frame(width: 56, height: 56)
                .contentShape(Circle())
        }
        .buttonStyle(PressFeedbackButtonStyle())
        .accessibilityLabel(strings.get(id: SharedRes.strings().gpx_control_a11y_fab))
        .accessibilityIdentifier(TestTags.shared.MAIN_FAB_GPX_CONTROL_BUTTON)
    }

    private var gpxPanel: some View {
        VStack(alignment: .leading, spacing: 4) {
            menuRow(
                icon: isRouteVisible ? "eye" : "eye.slash",
                title: strings.get(
                    id: isRouteVisible
                        ? SharedRes.strings().gpx_control_route_visibility_hide
                        : SharedRes.strings().gpx_control_route_visibility_show
                ),
                description: strings.get(id: SharedRes.strings().gpx_control_route_visibility_title),
                action: onToggleLine
            )
            .accessibilityIdentifier(TestTags.shared.MAIN_FAB_GPX_TOGGLE_LINE_BUTTON)
            menuRow(
                icon: "ruler",
                title: strings.get(
                    id: isAllDistancesVisible
                        ? SharedRes.strings().gpx_control_distances_hide
                        : SharedRes.strings().gpx_control_distances_show
                ),
                description: strings.get(id: SharedRes.strings().gpx_control_distances_title),
                action: onToggleDistances
            )
            .accessibilityIdentifier(TestTags.shared.MAIN_FAB_GPX_TOGGLE_DISTANCES_BUTTON)
            menuRow(
                icon: "rectangle.expand.diagonal",
                title: strings.get(id: SharedRes.strings().gpx_control_overview_desc),
                description: strings.get(id: SharedRes.strings().gpx_control_overview_title),
                action: onOverview
            )
            .accessibilityIdentifier(TestTags.shared.MAIN_FAB_GPX_OVERVIEW_BUTTON)
            menuRow(
                icon: "trash",
                title: strings.get(id: SharedRes.strings().gpx_control_clear_title),
                description: strings.get(id: SharedRes.strings().gpx_control_clear_desc),
                isDestructive: true,
                action: onClear
            )
            .accessibilityIdentifier(TestTags.shared.MAIN_FAB_GPX_CLEAR_BUTTON)
        }
        .padding(.vertical, 12)
        .padding(.horizontal, 8)
        .frame(width: 320)
    }

    private func menuRow(
        icon: String,
        title: String,
        description: String,
        isDestructive: Bool = false,
        action: @escaping () -> Void
    ) -> some View {
        Button {
            action()
        } label: {
            HStack(spacing: 16) {
                Image(systemName: icon)
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundStyle(isDestructive ? destructiveIconColor : .black)
                    .frame(width: 46, height: 46)
                    .background(Circle().fill(.white))
                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .font(.headline)
                        .foregroundStyle(isDestructive ? destructiveIconColor : .primary)
                    Text(description)
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
                Spacer(minLength: 0)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 10)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }

    private func expand() {
        withAnimation(.smooth(duration: 0.35)) { isExpanded = true }
    }

    private func collapse() {
        guard isExpanded else { return }
        withAnimation(.smooth(duration: 0.35)) { isExpanded = false }
    }
}
