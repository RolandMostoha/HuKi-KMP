package hu.mostoha.mobile.kmp.huki.util.formatter

import kotlinx.datetime.LocalDate

/**
 * Formats a date as a capitalized "yyyy MMMM" string using the current locale, e.g. "2026 July" / "2026 Július".
 */
expect class LocalizedDateFormatter() {
    fun formatMonthYear(date: LocalDate): String
}
