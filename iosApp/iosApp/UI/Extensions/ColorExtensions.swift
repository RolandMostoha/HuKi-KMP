import SwiftUI
import UIKit

extension UIColor {
    /// Brightens on dark (near-black) backgrounds, and darkens on light (near-white) backgrounds.
    func adaptiveTint(
        darkBrightnessBoost: CGFloat = 0.30,
        darkSaturationDrop: CGFloat = 0.12,
        lightBrightnessDrop: CGFloat = 0.18
    ) -> UIColor {
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
