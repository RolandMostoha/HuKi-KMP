package hu.mostoha.mobile.kmp.huki.util

import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.time.Instant

class DateTimeUtilsTest {

    @Test
    fun `Given instant, When format then parse with iso offset, Then same instant returns`() {
        val instant = Instant.fromEpochSeconds(1_700_000_000)

        val roundTripped = instant.toIsoOffsetString().toInstantFromIsoOffset()

        roundTripped shouldBe instant
    }

    @Test
    fun `Given malformed string, When toInstantFromIsoOffset, Then null returns`() {
        "not-a-date".toInstantFromIsoOffset() shouldBe null
    }
}
