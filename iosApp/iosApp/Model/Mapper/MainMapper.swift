import MapboxMaps
import Shared
import SwiftUI

extension Shared.Sheet: @retroactive Identifiable {
    public var id: String {
        String(onEnum(of: self).hashValue)
    }
}
