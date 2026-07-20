package hu.mostoha.mobile.kmp.huki.util.formatter

import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarIdentifierGregorian
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale

actual class LocalizedDateFormatter {
    actual fun formatMonthYear(date: LocalDate): String {
        val calendar = NSCalendar(calendarIdentifier = NSCalendarIdentifierGregorian)
        val components = NSDateComponents().apply {
            this.calendar = calendar
            year = date.year.toLong()
            month = date.month.number.toLong()
            day = date.day.toLong()
        }
        val nsDate = calendar.dateFromComponents(components) ?: return ""
        val formatter = NSDateFormatter().apply {
            this.calendar = calendar
            locale = NSLocale.currentLocale
            dateFormat = "MMMM"
        }
        val month = formatter.stringFromDate(nsDate).replaceFirstChar { it.uppercase() }
        return "${date.year} $month"
    }
}
