package hu.mostoha.mobile.kmp.huki.util.formatter

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

actual class LocalizedDateFormatter {
    actual fun formatMonthYear(date: LocalDate): String {
        val month = date.toJavaLocalDate()
            .format(DateTimeFormatter.ofPattern("MMMM", Locale.getDefault()))
            .replaceFirstChar { it.uppercase() }
        return "${date.year} $month"
    }
}
