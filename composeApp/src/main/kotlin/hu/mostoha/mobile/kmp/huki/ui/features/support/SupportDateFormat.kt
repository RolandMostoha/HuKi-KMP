package hu.mostoha.mobile.kmp.huki.ui.features.support

import android.text.format.DateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Instant
import java.time.Instant as JavaInstant

private const val SHORT_DATE_SKELETON = "MMMd"
private const val LONG_DATE_SKELETON = "yMMMMd"

/**
 * Month and day in the locale's order, e.g. "Nov 3" or "nov. 3.".
 */
fun Instant.formatShortDate(locale: Locale): String = format(locale, SHORT_DATE_SKELETON)

fun Instant.formatLongDate(locale: Locale): String = format(locale, LONG_DATE_SKELETON)

private fun Instant.format(locale: Locale, skeleton: String): String =
    DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, skeleton), locale)
        .withZone(ZoneId.systemDefault())
        .format(JavaInstant.ofEpochMilli(toEpochMilliseconds()))
