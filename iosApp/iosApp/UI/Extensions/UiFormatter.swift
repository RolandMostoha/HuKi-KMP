import CoreGraphics
import Foundation
import SwiftUI

enum UiFormatter {
    static func formatStatValue(
        _ value: String,
        smallFont: Font,
        unitBaselineOffset: CGFloat = 1
    ) -> AttributedString {
        var attributed = AttributedString(value)

        guard let pairRegex else {
            return attributed
        }

        let matches = pairRegex.matches(
            in: value,
            range: NSRange(value.startIndex..., in: value)
        )

        for match in matches.reversed() {
            guard match.numberOfRanges == 4,
                  let unitStringRange = Range(match.range(at: 3), in: value),
                  let unitRange = Range(unitStringRange, in: attributed)
            else {
                continue
            }

            attributed[unitRange].font = smallFont
            attributed[unitRange].baselineOffset = unitBaselineOffset
        }

        return attributed
    }

    // Matches [NUMBER][optional spaces][LETTERS] pairs like "22.6 km" or "7h".
    private static let pairRegex = try? NSRegularExpression(
        pattern: #"(\d+(?:[.,]\d+)?)(\s*)([\p{L}]+)"#
    )
}
