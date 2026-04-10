package hu.mostoha.mobile.kmp.huki.util

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class UiFormatterTest {

    companion object {
        private val TEST_STYLE = SpanStyle(
            fontSize = 8.sp,
            fontWeight = FontWeight.Medium,
        )
    }

    @Test
    fun `Given compact travel time, When formatting stat value, Then pair spacing is preserved`() {
        val actual = UiFormatter.formatStatValue("7h28m", TEST_STYLE)

        actual.text shouldBe "7H28M"
        actual.spanStyles.size shouldBe 2
        actual.spanStyles[0].start shouldBe 1
        actual.spanStyles[0].end shouldBe 2
        actual.spanStyles[1].start shouldBe 4
        actual.spanStyles[1].end shouldBe 5
    }

    @Test
    fun `Given spaced travel time, When formatting stat value, Then original whitespace is kept`() {
        val actual = UiFormatter.formatStatValue("7h 28m", TEST_STYLE)

        actual.text shouldBe "7H 28M"
        actual.spanStyles.size shouldBe 2
        actual.spanStyles[0].start shouldBe 1
        actual.spanStyles[0].end shouldBe 2
        actual.spanStyles[1].start shouldBe 5
        actual.spanStyles[1].end shouldBe 6
    }

    @Test
    fun `Given spaced distance, When formatting stat value, Then original whitespace is kept`() {
        val actual = UiFormatter.formatStatValue("22.6 km", TEST_STYLE)

        actual.text shouldBe "22.6 KM"
        actual.spanStyles.size shouldBe 1
        actual.spanStyles[0].start shouldBe 5
        actual.spanStyles[0].end shouldBe 7
    }

    @Test
    fun `Given spaced elevation, When formatting stat value, Then original whitespace is kept`() {
        val actual = UiFormatter.formatStatValue("1088 m", TEST_STYLE)

        actual.text shouldBe "1088 M"
        actual.spanStyles.size shouldBe 1
        actual.spanStyles[0].start shouldBe 5
        actual.spanStyles[0].end shouldBe 6
    }
}
