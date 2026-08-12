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
