package hu.mostoha.mobile.kmp.huki.util

import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.format
import kotlinx.datetime.offsetAt
import kotlin.time.Instant

/**
 * Formats the instant as a human-readable ISO offset e.g. `2026-06-21T17:24:44+02:00`.
 */
fun Instant.toIsoOffsetString(): String {
    val offset = TimeZone.currentSystemDefault().offsetAt(this)
    return DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET.format {
        setDateTimeOffset(this@toIsoOffsetString, offset)
    }
}

/**
 * Parses an ISO offset date-time string back into an [Instant], or null when malformed.
 */
fun String.toInstantFromIsoOffset(): Instant? =
    runCatching {
        DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET.parse(this).toInstantUsingOffset()
    }.getOrNull()
