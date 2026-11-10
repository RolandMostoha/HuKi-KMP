import CoreGraphics
import Foundation
import Shared

extension KotlinDouble {
    var cgFloat: CGFloat { CGFloat(self.doubleValue) }
}

extension Int64 {
    var asSeconds: Double {
        return Double(self) / 1000.0
    }
}

extension KotlinInstant {
    var date: Date {
        Date(timeIntervalSince1970: TimeInterval(toEpochMilliseconds()) / 1000)
    }
}
