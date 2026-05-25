import MapboxMaps
import Shared
import SwiftUI

extension Shared.Sheet: Identifiable {
    public var id: String {
        String(onEnum(of: self).hashValue)
    }
}
