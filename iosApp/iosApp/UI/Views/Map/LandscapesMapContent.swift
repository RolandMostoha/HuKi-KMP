@preconcurrency @_spi(Experimental) import MapboxMaps
import Shared
import SwiftUI

struct LandscapesMapContent: MapContent {
    let strings: Strings
    let landscapes: LandscapesUiState
    let isDarkMode: Bool
    let onEvent: (LandscapesUiEvents) -> Void

    private static let sourceId = "landscapes_source"
    private static let maskSourceId = "landscapes_hungary_mask_source"
    private static let maskFillLayerId = "landscapes_hungary_mask_fill_layer"
    private static let maskOpacity = 0.55
    private static let maskDarkOpacity = 0.5
    private static let fillLayerId = "landscapes_fill_layer"
    private static let outlineLayerId = "landscapes_outline_layer"
    private static let outlineHaloLayerId = "landscapes_outline_halo_layer"
    private static let labelSourceId = "landscapes_label_source"
    private static let labelLayerId = "landscapes_label_layer"
    private static let selectedOutlineLayerId = "landscapes_selected_outline_layer"
    private static let selectedHaloLayerId = "landscapes_selected_halo_layer"
    private static let selectedTintLayerId = "landscapes_selected_tint_layer"
    private static let selectedGlowLayerId = "landscapes_selected_glow_layer"
    private static let fillOpacity = 0.6
    private static let outlineWidth = 1.5
    private static let outlineHaloWidth = 4.0
    // Darker "ink" shade of the landscape colour, so light colours stay readable as text.
    private static let labelInkDarkening = 0.4
    private static let selectedOutlineWidth = 2.25
    private static let selectedHaloWidth = 5.0
    private static let selectedTintOpacity = 0.07
    private static let selectedGlowWidth = 10.0
    private static let selectedGlowBlur = 7.0
    private static let selectedGlowOpacity = 0.45
    private static let labelSize = 14.0
    private static let labelHaloWidth = 1.5
    private static let selectedMarkerPriority = 11
    private static let infoWindowPriority = 12

    var body: some MapContent {
        GeoJSONSource(id: Self.maskSourceId)
            .data(.string(landscapes.hungaryMaskGeoJson))
        FillLayer(id: Self.maskFillLayerId, source: Self.maskSourceId)
            .fillColor(isDarkMode ? .black : .white)
            .fillOpacity(isDarkMode ? Self.maskDarkOpacity : Self.maskOpacity)
            .fillColorUseTheme(.none)
            .fillEmissiveStrength(MapLighting.shared.OVERLAY_EMISSIVE_STRENGTH)
        if landscapes.grouping == .landscape {
            GeoJSONSource(id: Self.sourceId)
                .data(.string(landscapes.outlinesGeoJson))
            FillLayer(id: Self.fillLayerId, source: Self.sourceId)
                .filter(unselectedFilter)
                .fillColor(landscapeColorExpression())
                .fillOpacity(Self.fillOpacity)
                .fillColorUseTheme(.none)
                .fillEmissiveStrength(MapLighting.shared.OVERLAY_EMISSIVE_STRENGTH)
            LineLayer(id: Self.outlineHaloLayerId, source: Self.sourceId)
                .filter(unselectedFilter)
                .lineWidth(Self.outlineHaloWidth)
                .lineColor(.white)
                .lineJoin(.round)
                .lineColorUseTheme(.none)
                .lineEmissiveStrength(MapLighting.shared.OVERLAY_EMISSIVE_STRENGTH)
            LineLayer(id: Self.outlineLayerId, source: Self.sourceId)
                .filter(unselectedFilter)
                .lineWidth(Self.outlineWidth)
                .lineColor(landscapeColorExpression())
                .lineJoin(.round)
                .lineColorUseTheme(.none)
                .lineEmissiveStrength(MapLighting.shared.OVERLAY_EMISSIVE_STRENGTH)
            TapInteraction(.layer(Self.fillLayerId)) { feature, _ in
                guard case let .string(landscapeId)? = feature.properties["id"] ?? nil else { return false }
                onEvent(LandscapesUiEventsLandscapeClicked(landscapeId: landscapeId))
                return true
            }
            GeoJSONSource(id: Self.labelSourceId)
                .data(.string(landscapes.labelsGeoJson))
            SymbolLayer(id: Self.labelLayerId, source: Self.labelSourceId)
                .filter(unselectedFilter)
                .textField(landscapeNameExpression)
                .textFont(["DIN Pro Bold", "Arial Unicode MS Bold"])
                .textSize(Self.labelSize)
                .textColor(landscapeColorExpression(darkenedBy: Self.labelInkDarkening))
                .textHaloColor(.white)
                .textHaloWidth(Self.labelHaloWidth)
                .textColorUseTheme(.none)
                .textHaloColorUseTheme(.none)
                .textEmissiveStrength(MapLighting.shared.OVERLAY_EMISSIVE_STRENGTH)
            if let selectedLandscape = landscapes.selectedLandscape {
                FillLayer(id: Self.selectedTintLayerId, source: Self.sourceId)
                    .filter(selectedFilter(selectedLandscape))
                    .fillColor(selectedLandscape.colorRes.getUIColor())
                    .fillOpacity(Self.selectedTintOpacity)
                    .fillColorUseTheme(.none)
                    .fillEmissiveStrength(MapLighting.shared.OVERLAY_EMISSIVE_STRENGTH)
                // Rings are counter-clockwise, so a negative offset keeps the glow just outside the edge
                LineLayer(id: Self.selectedGlowLayerId, source: Self.sourceId)
                    .filter(selectedFilter(selectedLandscape))
                    .lineWidth(Self.selectedGlowWidth)
                    .lineOffset(-Self.selectedGlowWidth / 2)
                    .lineBlur(Self.selectedGlowBlur)
                    .lineOpacity(Self.selectedGlowOpacity)
                    .lineColor(selectedLandscape.colorRes.getUIColor())
                    .lineJoin(.round)
                    .lineColorUseTheme(.none)
                    .lineEmissiveStrength(MapLighting.shared.OVERLAY_EMISSIVE_STRENGTH)
                LineLayer(id: Self.selectedHaloLayerId, source: Self.sourceId)
                    .filter(selectedFilter(selectedLandscape))
                    .lineWidth(Self.selectedHaloWidth)
                    .lineColor(.white)
                    .lineCap(.round)
                    .lineJoin(.round)
                    .lineColorUseTheme(.none)
                    .lineEmissiveStrength(MapLighting.shared.OVERLAY_EMISSIVE_STRENGTH)
                LineLayer(id: Self.selectedOutlineLayerId, source: Self.sourceId)
                    .filter(selectedFilter(selectedLandscape))
                    .lineWidth(Self.selectedOutlineWidth)
                    .lineColor(selectedLandscape.colorRes.getUIColor())
                    .lineCap(.round)
                    .lineJoin(.round)
                    .lineColorUseTheme(.none)
                    .lineEmissiveStrength(MapLighting.shared.OVERLAY_EMISSIVE_STRENGTH)
            }
        }
        ForEvery(landscapes.destinations, id: \.id) { destination in
            let isSelected = destination.id == landscapes.selectedDestinationId
            MapViewAnnotation(coordinate: destination.location.coordinate) {
                DestinationMarkerView(type: destination.type, isSelected: isSelected)
                    .onTapGesture {
                        onEvent(LandscapesUiEventsLandscapesDestinationClicked(destination: destination))
                    }
                    .accessibilityAddTraits(.isButton)
                    .accessibilityLabel(destination.name)
                    .accessibilityIdentifier(TestTags.shared.LANDSCAPES_DESTINATION_MARKER)
            }
            .allowOverlap(true)
            .allowOverlapWithPuck(true)
            .ignoreCameraPadding(true)
            .priority(isSelected ? Self.selectedMarkerPriority : Int(destination.popularity))
        }
        if let destination = landscapes.selectedDestination {
            MapViewAnnotation(coordinate: destination.location.coordinate) {
                DestinationInfoWindowView(
                    strings: strings,
                    destination: destination,
                    onPlaceDetailsClick: {
                        onEvent(LandscapesUiEventsLandscapesInfoWindowPlaceDetailsClicked.shared)
                    },
                    onRouteClick: {
                        onEvent(LandscapesUiEventsLandscapesInfoWindowRouteClicked.shared)
                    },
                    onWebsiteClick: {
                        onEvent(LandscapesUiEventsLandscapesInfoWindowWebsiteClicked.shared)
                    }
                )
            }
            .variableAnchors([
                ViewAnnotationAnchorConfig(
                    anchor: .bottom,
                    offsetY: Dimens.destinationSelectedMarkerSize / 2 + Dimens.infoWindowMarkerPadding
                )
            ])
            .allowOverlap(true)
            .allowOverlapWithPuck(true)
            .ignoreCameraPadding(true)
            .priority(Self.infoWindowPriority)
        }
    }
}

private extension LandscapesMapContent {
    func selectedFilter(_ landscape: Landscape) -> Exp {
        Exp(.eq) {
            Exp(.get) { "id" }
            landscape.id
        }
    }

    var unselectedFilter: Exp {
        Exp(.neq) {
            Exp(.get) { "id" }
            landscapes.selectedLandscapeId ?? ""
        }
    }

    var landscapeNameExpression: Exp {
        Exp(.match) {
            Exp(.get) { "id" }
            for landscape in landscapes.landscapes {
                landscape.id
                strings.get(id: landscape.nameRes)
            }
            ""
        }
    }

    func landscapeColorExpression(darkenedBy darkening: CGFloat = 0) -> Exp {
        Exp(.match) {
            Exp(.get) { "id" }
            for landscape in landscapes.landscapes {
                landscape.id
                landscape.colorRes.getUIColor().darkened(by: darkening)
            }
            SharedRes.colors().primary.getUIColor()
        }
    }
}

private extension UIColor {
    func darkened(by amount: CGFloat) -> UIColor {
        var red: CGFloat = 0, green: CGFloat = 0, blue: CGFloat = 0, alpha: CGFloat = 0
        guard amount > 0, getRed(&red, green: &green, blue: &blue, alpha: &alpha) else { return self }
        return UIColor(red: red * (1 - amount), green: green * (1 - amount), blue: blue * (1 - amount), alpha: alpha)
    }
}

struct DestinationMarkerView: View {
    let type: DestinationType
    let isSelected: Bool

    private let surfaceColor = SwiftUI.Color(SharedRes.colors().mapStroke.getUIColor())

    private var categoryColor: SwiftUI.Color {
        SwiftUI.Color(type.colorRes.getUIColor())
    }

    private var size: CGFloat {
        isSelected ? Dimens.destinationSelectedMarkerSize : Dimens.destinationMarkerSize
    }

    var body: some View {
        Image(uiImage: type.iconRes.toUIImage()!)
            .renderingMode(.template)
            .resizable()
            .scaledToFit()
            .frame(width: isSelected ? 22 : 16, height: isSelected ? 22 : 16)
            .foregroundStyle(surfaceColor)
            .frame(width: size, height: size)
            .background(Circle().fill(categoryColor))
            .overlay(Circle().strokeBorder(surfaceColor, lineWidth: 2))
            .shadow(color: .black.opacity(0.25), radius: 2, y: 1)
            .contentShape(Circle())
            .animation(.easeInOut(duration: 0.15), value: isSelected)
    }
}

#Preview {
    HStack(spacing: 16) {
        DestinationMarkerView(type: .river, isSelected: false)
        DestinationMarkerView(type: .river, isSelected: true)
        DestinationMarkerView(type: .peak, isSelected: false)
        DestinationMarkerView(type: .cave, isSelected: true)
    }
    .padding()
    .background(SwiftUI.Color(.systemGray5))
}
