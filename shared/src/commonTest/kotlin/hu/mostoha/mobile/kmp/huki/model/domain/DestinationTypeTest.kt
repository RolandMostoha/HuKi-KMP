package hu.mostoha.mobile.kmp.huki.model.domain

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class DestinationTypeTest {

    @Test
    fun `Given a destination type - When resolving its parent - Then only subtypes have one`() {
        testCases().forEach { (type, expected) ->
            val actual = type.parent

            actual shouldBe expected
        }
    }

    companion object {
        private fun testCases() =
            listOf(
                DestinationType.CAVE to null,
                DestinationType.PEAK to null,
                DestinationType.CASTLE_RUIN to DestinationType.CASTLE,
                DestinationType.CAVE_WITHOUT_GUIDE to DestinationType.CAVE,
                DestinationType.CAVE_GUIDE_REQUIRED to DestinationType.CAVE,
            )
    }
}
