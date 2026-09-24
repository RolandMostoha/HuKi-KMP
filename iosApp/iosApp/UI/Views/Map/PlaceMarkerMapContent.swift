@preconcurrency @_spi(Experimental) import MapboxMaps
import Shared
import SwiftUI

struct PlaceMarkerMapContent: MapContent {
    let location: Shared.Location
    let baseLayer: Shared.BaseLayer
    let colorScheme: ColorScheme

    var body: some MapContent {
        PointAnnotation(coordinate: location.coordinate)
            .image(
                MarkerIcons.shared
                    .placeSymbol(isDarkMode: colorScheme == .dark, baseLayer: baseLayer)
                    .annotationImage(for: colorScheme)
            )
            .iconAnchor(.bottom)
            .iconSize(SharedDimens.shared.PLACE_MARKER_SCALE)
            .iconEmissiveStrength(MapLighting.shared.OVERLAY_EMISSIVE_STRENGTH)
    }
}
