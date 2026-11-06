import SwiftUI
import UIKit

private let darkBrightnessBoost: CGFloat = 0.30
private let darkSaturationDrop: CGFloat = 0.12
private let lightBrightnessDrop: CGFloat = 0.18

extension UIColor {
    /// Place category colors: light-only Mapbox vs light AND dark in UI components
    func categoryTint() -> UIColor {
        UIColor { traits in
            let base = self.resolvedColor(with: traits)
            var hue: CGFloat = 0
            var saturation: CGFloat = 0
            var brightness: CGFloat = 0
            var alpha: CGFloat = 0
            guard base.getHue(&hue, saturation: &saturation, brightness: &brightness, alpha: &alpha) else {
                return base
            }
            if traits.userInterfaceStyle == .dark {
                return UIColor(
                    hue: hue,
                    saturation: max(0, saturation - darkSaturationDrop),
                    brightness: min(1, brightness + darkBrightnessBoost),
                    alpha: alpha
                )
            }
            return UIColor(
                hue: hue,
                saturation: saturation,
                brightness: max(0, brightness - lightBrightnessDrop),
                alpha: alpha
            )
        }
    }
}

extension UIColor {
    /// `0xRRGGBB` of the color, as Kotlin `Int`, alpha dropped.
    var rgb: Int32 {
        var red: CGFloat = 0, green: CGFloat = 0, blue: CGFloat = 0, alpha: CGFloat = 0
        getRed(&red, green: &green, blue: &blue, alpha: &alpha)
        return [red, green, blue].reduce(Int32(0)) { rgb, channel in
            rgb << 8 | Int32(min(max((channel * 255).rounded(), 0), 255))
        }
    }
}

extension Color {
    /// Opaque color from a `0xRRGGBB` Kotlin `Int`.
    init(rgb: Int32) {
        self.init(
            red: Double((rgb >> 16) & 0xFF) / 255,
            green: Double((rgb >> 8) & 0xFF) / 255,
            blue: Double(rgb & 0xFF) / 255
        )
    }
}
