package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.util.formatter.TravelTimeFormatter
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class TravelTimeFormatterTest {

    @Test
    fun `Given travel times - When formatting compact - Then hours and zero padded minutes are shown`() {
        listOf(
            0.minutes to "0:00",
            45.minutes to "0:45",
            (7.hours + 5.minutes) to "7:05",
            (18.hours + 50.minutes) to "18:50",
            (344.hours + 40.minutes) to "344:40",
            (2.hours + 1.seconds) to "2:01",
        ).forEach { (input, result) ->
            val actual = TravelTimeFormatter.formatTravelTimeCompact(input)

            actual shouldBe result
        }
    }

    @Test
    fun `Given sub hour travel time in english - When formatting - Then time is shown with hour minute suffixes`() {
        val input = 45.minutes

        val actual = TravelTimeFormatter.selectTemplate(input)

        actual.first shouldBe SharedRes.strings.travel_time_minutes_pattern
        actual.second shouldBe listOf(45L)
    }

    @Test
    fun `Given sub minute travel time in english - When formatting - Then time rounds up to one minute`() {
        val input = 1.seconds

        val actual = TravelTimeFormatter.selectTemplate(input)

        actual.first shouldBe SharedRes.strings.travel_time_minutes_pattern
        actual.second shouldBe listOf(1L)
    }

    @Test
    fun `Given multi hour travel time in english - When formatting - Then time is shown with hour minute suffixes`() {
        val input = 135.minutes

        val actual = TravelTimeFormatter.selectTemplate(input)

        actual.first shouldBe SharedRes.strings.travel_time_hours_minutes_pattern
        actual.second shouldBe listOf(2L, 15L)
    }

    @Test
    fun `Given exact multi hour travel time - When formatting - Then hour only template is selected`() {
        val input = 420.minutes

        val actual = TravelTimeFormatter.selectTemplate(input)

        actual.first shouldBe SharedRes.strings.travel_time_hours_pattern
        actual.second shouldBe listOf(7L)
    }
}
