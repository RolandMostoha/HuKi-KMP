package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.util.formatter.LocalizedDateFormatter
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeEmpty
import kotlinx.datetime.LocalDate
import kotlin.test.Test

class LocalizedDateFormatterTest {

    @Test
    fun `Given a date - When formatting month and year - Then the year is followed by a month name`() {
        val input = LocalDate(2026, 8, 1)

        val actual = LocalizedDateFormatter().formatMonthYear(input)

        actual.substringBefore(' ') shouldBe "2026"
        actual.substringAfter(' ').shouldNotBeEmpty()
    }

    @Test
    fun `Given a date - When formatting month and year - Then the month name is capitalized`() {
        val input = LocalDate(2026, 8, 1)

        val actual = LocalizedDateFormatter().formatMonthYear(input).substringAfter(' ')

        actual.first() shouldBe actual.first().uppercaseChar()
    }

    @Test
    fun `Given two different months - When formatting month and year - Then the month names differ`() {
        val formatter = LocalizedDateFormatter()

        val actual = formatter.formatMonthYear(LocalDate(2026, 1, 1))

        actual shouldNotBe formatter.formatMonthYear(LocalDate(2026, 8, 1))
    }
}
