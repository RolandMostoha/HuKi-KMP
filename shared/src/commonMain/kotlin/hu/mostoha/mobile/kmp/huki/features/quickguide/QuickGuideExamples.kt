package hu.mostoha.mobile.kmp.huki.features.quickguide

import dev.icerock.moko.resources.desc.StringDesc
import hu.mostoha.mobile.kmp.huki.util.formatter.TravelTimeFormatter
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

object QuickGuideExamples {

    /**
     * The travel time shown on the illustrative GPX stat tile.
     */
    val travelTime: StringDesc = TravelTimeFormatter.formatTravelTime(2.hours + 30.minutes)
}
