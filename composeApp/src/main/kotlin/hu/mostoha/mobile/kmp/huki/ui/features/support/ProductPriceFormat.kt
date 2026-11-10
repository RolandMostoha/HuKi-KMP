package hu.mostoha.mobile.kmp.huki.ui.features.support

import hu.mostoha.mobile.kmp.huki.model.domain.ProductPrice
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

private const val MICROS_SCALE = 6
private const val NO_BREAK_SPACE = '\u00A0'

/**
 * Whole amounts drop the fraction (499 Ft), others keep the currency's digits ($4.50).
 */
fun ProductPrice.format(locale: Locale): String {
    val amount = BigDecimal.valueOf(amountMicros, MICROS_SCALE)
    val format = NumberFormat.getCurrencyInstance(locale).apply {
        currency = Currency.getInstance(currencyCode)
        if (amount.stripTrailingZeros().scale() <= 0) {
            minimumFractionDigits = 0
        }
        (this as? DecimalFormat)?.separateCurrencyCode()
    }
    return format.format(amount)
}

/**
 * A currency without a local symbol is written as its code, which the locale glues to the amount (HUF499).
 */
private fun DecimalFormat.separateCurrencyCode() {
    if (positivePrefix.lastOrNull()?.isLetter() == true) {
        positivePrefix += NO_BREAK_SPACE
    }
    if (positiveSuffix.firstOrNull()?.isLetter() == true) {
        positiveSuffix = NO_BREAK_SPACE + positiveSuffix
    }
}
