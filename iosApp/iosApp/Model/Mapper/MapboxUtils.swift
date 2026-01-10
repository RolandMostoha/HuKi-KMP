import MapboxMaps
import Shared

extension RasterSource {
    public func tileSize(_ newValue: Double) -> Self {
        var copy = self
        copy.tileSize = newValue
        return copy
    }
}
