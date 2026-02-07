import CoreGraphics
import Shared

extension KotlinDouble {
    var cgFloat: CGFloat { CGFloat(self.doubleValue) }
}

extension Int64 {
    var asSeconds: Double {
        return Double(self) / 1000.0
    }
}
