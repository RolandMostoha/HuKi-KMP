package hu.mostoha.mobile.kmp.huki

import hu.mostoha.mobile.huki.shared.SharedRes
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlin.test.Test

class StringsTest {

    @Test
    fun `Given a single argument pattern - When resolving - Then the argument is substituted`() {
        val input = listOf(45L)

        val actual = Strings().get(SharedRes.strings.travel_time_minutes_pattern, input)

        actual shouldContain "45"
        actual shouldNotContain "%s"
    }

    @Test
    fun `Given a two argument non positional pattern - When resolving - Then both arguments are substituted`() {
        val input = listOf(2L, 15L)

        val actual = Strings().get(SharedRes.strings.travel_time_hours_minutes_pattern, input)

        actual shouldContain "2"
        actual shouldContain "15"
        actual shouldNotContain "%s"
    }
}
