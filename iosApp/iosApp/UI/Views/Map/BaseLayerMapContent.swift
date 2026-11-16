@preconcurrency @_spi(Experimental) import MapboxMaps
import Shared
import SwiftUI

/// Map content the base layer style doesn't carry itself: the Outdoors dark mode and the TuHu raster tiles.
struct BaseLayerMapContent: MapContent {
    let baseLayer: Shared.BaseLayer
    let isDarkMode: Bool
    let tileScaleFactor: Float

    var body: some MapContent {
        if isDarkMode && baseLayer == .outdoors {
            ColorTheme(base64: OutdoorsColorTheme.shared.DARK_MAP_LUT_BASE64)
        }
        if baseLayer == .tuhu {
            TuHuMapContent(scaleFactor: tileScaleFactor)
        }
    }
}
