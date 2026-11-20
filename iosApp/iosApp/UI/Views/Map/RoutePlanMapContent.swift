@preconcurrency @_spi(Experimental) import MapboxMaps
import Shared
import SwiftUI

struct RoutePlanMapContent: MapContent {
    let mapUiState: MapUiState
    let colorScheme: ColorScheme
    var onMarkerDragChanged: (Bool) -> Void = { _ in }
    var onMarkerDropped: (_ waypoint: GpxWaypoint, _ from: CGSize, _ to: CGSize) -> Void = { _, _, _ in }
    var onMarkerTapped: (GpxWaypoint) -> Void = { _ in }

    private static let layerId = "route_plan"

    private var markers: [GpxWaypoint] { mapUiState.routePlanMarkers }

    var body: some MapContent {
        if let routePlan = mapUiState.routePlan {
            let feature = Feature(geometry: .lineString(routePlan.locations.lineString))

            GeoJSONSource(id: Self.layerId)
                .data(.feature(feature))

            LineLayer(id: Self.layerId, source: Self.layerId)
                .lineWidth(SharedDimens.shared.GPX_LINE_WIDTH)
                .lineColor(SharedRes.colors().primary.getUIColor().resolved(for: colorScheme))
                .lineBorderWidth(SharedDimens.shared.MAP_LINE_STROKE_WIDTH)
                .lineBorderColor(SharedRes.colors().mapStroke.getUIColor().resolved(for: colorScheme))
                .lineCap(.round)
                .lineJoin(.round)
                .lineColorUseTheme(.none)
                .lineBorderColorUseTheme(.none)
                .lineEmissiveStrength(MapLighting.shared.OVERLAY_EMISSIVE_STRENGTH)
        }
        // In Route Plans, waypoints should sit on top of my location, to always see the plan.
        // ViewAnnotations are real views above every style layer.
        ForEvery(WaypointMarkerOrder.shared.sort(waypoints: markers), id: \.location.id) { waypoint in
            MapViewAnnotation(coordinate: waypoint.location.coordinate) {
                RoutePlanMarkerView(
                    waypoint: waypoint,
                    isSelected: waypoint == mapUiState.selectedRoutePlanMarker,
                    isRoutePlanLoading: mapUiState.isRoutePlanLoading,
                    onDragChanged: onMarkerDragChanged,
                    onDropped: { from, target in onMarkerDropped(waypoint, from, target) },
                    onTapped: { onMarkerTapped(waypoint) }
                )
            }
            .allowOverlap(true)
            .allowOverlapWithPuck(true)
            .ignoreCameraPadding(true)
        }
    }
}

private struct RoutePlanMarkerView: View {
    let waypoint: GpxWaypoint
    let isSelected: Bool
    let isRoutePlanLoading: Bool
    let onDragChanged: (Bool) -> Void
    let onDropped: (_ from: CGSize, _ to: CGSize) -> Void
    let onTapped: () -> Void

    @State private var dragOffset: CGSize = .zero
    @State private var dropOffset: CGSize = .zero
    @State private var isDragging = false
    @GestureState private var isGestureActive = false

    private let strings = Strings()

    @Environment(\.colorScheme) private var colorScheme

    private var scale: CGFloat {
        CGFloat(
            waypoint.type == .intermediate
                ? SharedDimens.shared.GPX_WAYPOINT_MARKER_SCALE
                : SharedDimens.shared.GPX_EDGE_LOCATION_MARKER_SCALE
        )
    }

    private var markerScale: CGFloat {
        let selectedScale = isSelected ? CGFloat(SharedDimens.shared.ROUTE_PLAN_SELECTED_MARKER_SCALE) : 1
        return isDragging ? selectedScale * 1.25 : selectedScale
    }

    var body: some View {
        let image = waypoint.type.icon.appearanceImage(for: colorScheme)
        let size = CGSize(width: image.size.width * scale, height: image.size.height * scale)
        ZStack {
            if isSelected {
                RoutePlanMarkerPulse(diameter: max(size.width, size.height))
            }
            Image(uiImage: image)
                .resizable()
                .frame(width: size.width, height: size.height)
                .scaleEffect(markerScale)
                .animation(.toggle, value: isDragging)
                .animation(.selection, value: isSelected)
        }
        // A drop keeps the marker where it landed while the route replans, then it snaps to where the route puts it
        .offset(dragOffset)
        .onTapGesture(perform: onTapped)
        .gesture(dragGesture)
        .accessibilityAddTraits(.isButton)
        .accessibilityAction(named: Text(strings.get(id: SharedRes.strings().route_planner_a11y_select_stop)), onTapped)
        .onChange(of: isRoutePlanLoading) { _, isLoading in
            guard !isLoading, !isDragging else { return }
            dragOffset = .zero
            dropOffset = .zero
        }
        .onChange(of: isGestureActive) { _, isActive in
            // A cancelled gesture never calls onEnded, which would leave the map without panning
            if !isActive && isDragging { cancelDrag() }
        }
        .onDisappear {
            // The replanned route can replace this view mid-drag, which never ends the gesture
            if isDragging { onDragChanged(false) }
        }
        // The pulse grows past the marker, so the annotation reserves room for it around the same center.
        // The room stays the same either way, as a resized annotation is placed by its old size for a frame.
        .frame(
            width: size.width * RoutePlanMarkerPulse.maxScale,
            height: size.height * RoutePlanMarkerPulse.maxScale
        )
    }
}

private extension RoutePlanMarkerView {
    var dragGesture: some Gesture {
        LongPressGesture(minimumDuration: 0.3)
            .sequenced(before: DragGesture(minimumDistance: 0, coordinateSpace: .global))
            .updating($isGestureActive) { _, state, _ in state = true }
            .onChanged { value in
                guard case .second(true, let drag) = value else { return }
                if !isDragging {
                    isDragging = true
                    onDragChanged(true)
                    UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                }
                dragOffset = dropOffset + (drag?.translation ?? .zero)
            }
            .onEnded { value in
                if case .second(true, let drag?) = value, drag.translation != .zero {
                    onDropped(dropOffset, dragOffset)
                    dropOffset = dragOffset
                } else {
                    dragOffset = dropOffset
                }
                isDragging = false
                onDragChanged(false)
            }
    }

    func cancelDrag() {
        dragOffset = dropOffset
        isDragging = false
        onDragChanged(false)
    }
}

private extension CGSize {
    static func + (lhs: CGSize, rhs: CGSize) -> CGSize {
        CGSize(width: lhs.width + rhs.width, height: lhs.height + rhs.height)
    }
}

private struct RoutePlanMarkerPulse: View {
    let diameter: CGFloat

    static let maxScale: CGFloat = 2.4

    @State private var isExpanded = false

    var body: some View {
        Circle()
            .fill(Color(SharedRes.colors().primary.getUIColor()).opacity(0.45))
            .frame(width: diameter, height: diameter)
            .scaleEffect(isExpanded ? Self.maxScale : 1)
            .opacity(isExpanded ? 0 : 1)
            .onAppear {
                withAnimation(.easeOut(duration: 1.4).repeatForever(autoreverses: false)) {
                    isExpanded = true
                }
            }
    }
}
