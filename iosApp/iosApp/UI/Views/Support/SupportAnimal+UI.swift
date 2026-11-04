import Shared
import SwiftUI

extension SupportAnimal {
    var color: SwiftUI.Color { SwiftUI.Color(colorRes.getUIColor()) }

    var containerColor: SwiftUI.Color { SwiftUI.Color(containerColorRes.getUIColor()) }

    var iconImage: Image {
        Image(uiImage: iconRes.toUIImage()!).renderingMode(.template)
    }
}

extension ProductPrice {
    /// The App Store formats with the device region, this follows the app language instead. Whole amounts drop the
    /// fraction (499 Ft), others keep the currency's digits ($4.50).
    var formatted: String {
        let amount = Decimal(amountMicros) / 1_000_000
        let style = Decimal.FormatStyle.Currency(code: currencyCode, locale: .current)
        return amountMicros.isMultiple(of: 1_000_000)
            ? amount.formatted(style.precision(.fractionLength(0)))
            : amount.formatted(style)
    }
}
