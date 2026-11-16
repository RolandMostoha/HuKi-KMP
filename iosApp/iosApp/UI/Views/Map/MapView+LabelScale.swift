@preconcurrency @_spi(Experimental) import MapboxMaps
import Shared
import SwiftUI

extension MapView {
    /// TuHu raster tiles follow the label scale, like the vector labels of the other base layers.
    var tileScaleFactor: Float {
        uiState.mapUiState.labelScale.effectiveFactor(systemScale: dynamicTypeSize.mapboxSymbolScale)
    }
}

extension View {
    /// Re-applies once the map is created, otherwise the stored scale would be lost on launch.
    func mapLabelScale(_ labelScale: MapLabelScale, map: MapboxMap?) -> some View {
        task(id: [AnyHashable(labelScale), AnyHashable(map != nil)]) {
            map?.symbolScaleBehavior = labelScale.toSymbolScaleBehavior()
        }
    }
}
