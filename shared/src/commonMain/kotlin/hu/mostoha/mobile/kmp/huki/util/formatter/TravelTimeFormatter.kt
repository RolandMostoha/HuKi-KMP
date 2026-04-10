package hu.mostoha.mobile.kmp.huki.util.formatter

import androidx.annotation.VisibleForTesting
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import hu.mostoha.mobile.huki.shared.SharedRes
import kotlin.time.Duration

object TravelTimeFormatter {

    /**
     * Formats travel time like "7H 28M".
     */
    fun formatTravelTime(duration: Duration): StringDesc =
        selectTemplate(duration).let { (stringResource, args) ->
            StringDesc.ResourceFormatted(stringResource, *args.toTypedArray())
        }

    @VisibleForTesting
    internal fun selectTemplate(duration: Duration): Pair<StringResource, List<Any>> {
        val totalSeconds = duration.inWholeSeconds
        val totalMinutes = if (totalSeconds == 0L) {
            0L
        } else {
            (totalSeconds + 59) / 60
        }
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return when {
            hours == 0L -> SharedRes.strings.travel_time_minutes_pattern to listOf(minutes)
            minutes == 0L -> SharedRes.strings.travel_time_hours_pattern to listOf(hours)
            else -> SharedRes.strings.travel_time_hours_minutes_pattern to listOf(hours, minutes)
        }
    }
}
