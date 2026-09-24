@preconcurrency @_spi(Experimental) import MapboxMaps
import Shared
import SwiftUI

/// Dark mode for the Outdoors base layer.
struct OutdoorsColorThemeContent: MapContent {
    let baseLayer: Shared.BaseLayer
    let isDarkMode: Bool

    var body: some MapContent {
        if isDarkMode && baseLayer == .outdoors {
            ColorTheme(base64: OutdoorsColorTheme.shared.DARK_MAP_LUT_BASE64)
        }
    }
}
