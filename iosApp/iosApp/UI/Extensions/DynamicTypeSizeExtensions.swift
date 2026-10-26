import SwiftUI

extension DynamicTypeSize {
    /// Mirrors the table of Mapbox's `SymbolScaleBehavior.system`, so the shown percentage matches the map.
    var mapboxSymbolScale: Float {
        switch self {
        case .xSmall: 0.8
        case .small: 0.85
        case .medium: 0.9
        case .large: 1.0
        case .xLarge: 1.1
        case .xxLarge: 1.25
        case .xxxLarge: 1.5
        case .accessibility1: 1.6
        case .accessibility2: 1.75
        case .accessibility3: 1.9
        default: 2.0
        }
    }
}
