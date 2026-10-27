import MapboxMaps
import Shared
import SwiftUI

extension Shared.Sheet: @retroactive Identifiable {
    public var id: String {
        switch onEnum(of: self) {
        // Reversing replaces the GPX details, which must not re-present the sheet
        case .gpx(let sheet):
            "gpx_\(sheet.gpxDetails.fileUri)"
        default:
            String(onEnum(of: self).hashValue)
        }
    }
}
