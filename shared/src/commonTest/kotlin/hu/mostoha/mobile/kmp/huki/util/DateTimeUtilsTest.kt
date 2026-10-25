package hu.mostoha.mobile.kmp.huki.util

import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.time.Instant

class DateTimeUtilsTest {

    @Test
    fun `Given instant - When format then parse with iso offset - Then same instant returns`() {
        val instant = Instant.fromEpochSeconds(1_700_000_000)

        val roundTripped = instant.toIsoOffsetString().toInstantFromIsoOffset()

        roundTripped shouldBe instant
    }

    @Test
    fun `Given malformed string - When toInstantFromIsoOffset - Then null returns`() {
        "not-a-date".toInstantFromIsoOffset() shouldBe null
    }

    @Test
    fun `Given instant - When nextUtcMidnight - Then start of the next UTC day returns`() {
        val instant = Instant.parse("2026-10-10T23:30:00+02:00")

        val actual = instant.nextUtcMidnight()

        actual shouldBe Instant.parse("2026-10-11T00:00:00Z")
    }
}
