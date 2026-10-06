@preconcurrency @_spi(Experimental) import MapboxMaps
import Shared
import SwiftUI

struct OktMapContent: MapContent {
    let strings: Strings
    let okt: OktUiState
    let onLineClicked: (Shared.Location) -> Void
    let onMarkerClicked: (OktMarker) -> Void
    let onInfoWindowPlaceDetailsClicked: () -> Void

    private static let baseSourceId = "okt_base_source"
    private static let baseLayerId = "okt_base_layer"
    private static let baseHitLayerId = "okt_base_hit_layer"
    private static let selectedSourceId = "okt_selected_source"
    private static let selectedLayerId = "okt_selected_layer"
    private static let lineHitWidth = 24.0
    private static let raisedMarkerPriority = 1
    private static let selectedMarkerPriority = 2
    private static let infoWindowPriority = 3

    var body: some MapContent {
        GeoJSONSource(id: Self.baseSourceId)
            .data(.string(okt.baseLine.geoJson))
        LineLayer(id: Self.baseLayerId, source: Self.baseSourceId)
            .lineWidth(SharedDimens.shared.OKT_BASE_LINE_WIDTH)
            .lineColor(SharedRes.colors().oktBlue.getUIColor())
            .lineBorderWidth(SharedDimens.shared.MAP_LINE_STROKE_WIDTH)
            .lineBorderColor(SharedRes.colors().mapStroke.getUIColor())
            .lineCap(.round)
            .lineJoin(.round)
            .lineColorUseTheme(.none)
            .lineBorderColorUseTheme(.none)
            .lineEmissiveStrength(MapLighting.shared.OVERLAY_EMISSIVE_STRENGTH)
        // Wider hit area than the visible line; Mapbox skips fully transparent (.clear) layers in hit-testing
        LineLayer(id: Self.baseHitLayerId, source: Self.baseSourceId)
            .lineWidth(Self.lineHitWidth)
            .lineColor(.black)
            .lineOpacity(0.01)
        GeoJSONSource(id: Self.selectedSourceId)
            .data(.string(okt.selectedLine.geoJson))
        LineLayer(id: Self.selectedLayerId, source: Self.selectedSourceId)
            .lineWidth(SharedDimens.shared.OKT_SELECTED_LINE_WIDTH)
            .lineColor(SharedRes.colors().oktBlue.getUIColor())
            .lineBorderWidth(SharedDimens.shared.MAP_LINE_STROKE_WIDTH)
            .lineBorderColor(SharedRes.colors().mapStroke.getUIColor())
            .lineCap(.round)
            .lineJoin(.round)
            .lineColorUseTheme(.none)
            .lineBorderColorUseTheme(.none)
            .lineEmissiveStrength(MapLighting.shared.OVERLAY_EMISSIVE_STRENGTH)
        TapInteraction(.layer(Self.baseHitLayerId)) { _, context in
            onLineClicked(context.coordinate.location)
            return true
        }
        ForEvery(okt.markers, id: \.id) { marker in
            MapViewAnnotation(coordinate: marker.location.coordinate) {
                OktMarkerView(type: marker.type, isSelected: marker.id == okt.selectedMarkerId)
                    .onTapGesture {
                        onMarkerClicked(marker)
                    }
                    .accessibilityAddTraits(.isButton)
                    .accessibilityLabel(strings.get(desc: marker.toInfoWindowTitle()))
            }
            .allowOverlap(true)
            .allowOverlapWithPuck(true)
            .priority(markerPriority(marker))
        }
        if let info = okt.infoWindow {
            MapViewAnnotation(coordinate: info.marker.location.coordinate) {
                OktInfoWindowView(
                    strings: strings,
                    info: info,
                    onPlaceDetailsClick: onInfoWindowPlaceDetailsClicked
                )
            }
            .variableAnchors([
                ViewAnnotationAnchorConfig(
                    anchor: .bottom,
                    offsetY: OktMarkerView.size(for: info.marker.type, isSelected: true) / 2
                        + Dimens.infoWindowMarkerPadding
                )
            ])
            .allowOverlap(true)
            .allowOverlapWithPuck(true)
            .priority(Self.infoWindowPriority)
        }
    }

    // Start and end stay readable by default; once a stamp is picked, the stamps come forward
    private func markerPriority(_ marker: OktMarker) -> Int {
        if marker.id == okt.selectedMarkerId {
            return Self.selectedMarkerPriority
        }
        let isEdge = marker.type != .stamp
        return isEdge == (okt.selectedMarkerId == nil) ? Self.raisedMarkerPriority : 0
    }
}

struct OktMarkerView: View {
    let type: OktMarkerType
    let isSelected: Bool

    private let oktBlue = Color(SharedRes.colors().oktBlue.getUIColor())
    private let onOktBlue = Color(SharedRes.colors().onOktBlue.getUIColor())

    static func size(for type: OktMarkerType, isSelected: Bool) -> CGFloat {
        guard type == .stamp else { return Dimens.oktEdgeMarkerSize }
        return isSelected ? Dimens.oktSelectedMarkerSize : Dimens.oktMarkerSize
    }

    private var size: CGFloat {
        Self.size(for: type, isSelected: isSelected)
    }

    var body: some View {
        icon
            .foregroundStyle(isSelected ? onOktBlue : oktBlue)
            .frame(width: size, height: size)
            .background(Circle().fill(isSelected ? oktBlue : onOktBlue))
            .overlay(Circle().strokeBorder(isSelected ? onOktBlue : oktBlue, lineWidth: 2))
            .shadow(color: .black.opacity(0.25), radius: 2, y: 1)
            .contentShape(Circle())
            .animation(.easeInOut(duration: 0.15), value: isSelected)
    }

    @ViewBuilder
    private var icon: some View {
        switch type {
        case .stamp:
            SharedRes.images().ic_okt_stamp.swiftUIImage
                .renderingMode(.template)
                .resizable()
                .scaledToFit()
                .frame(width: isSelected ? 22 : 16, height: isSelected ? 22 : 16)
        case .start, .end:
            SharedRes.images().ic_okt_symbol.swiftUIImage
                .resizable()
                .scaledToFit()
                .frame(width: 18, height: 18)
        }
    }
}

#Preview {
    HStack(spacing: 16) {
        OktMarkerView(type: .stamp, isSelected: false)
        OktMarkerView(type: .stamp, isSelected: true)
        OktMarkerView(type: .start, isSelected: false)
        OktMarkerView(type: .end, isSelected: true)
    }
    .padding()
    .background(Color(.systemGray5))
}
