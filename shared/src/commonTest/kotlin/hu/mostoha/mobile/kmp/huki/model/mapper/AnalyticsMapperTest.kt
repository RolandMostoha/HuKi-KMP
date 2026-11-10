package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.kmp.huki.model.analytics.SupporterLevel
import hu.mostoha.mobile.kmp.huki.model.domain.SupportAnimal
import hu.mostoha.mobile.kmp.huki.model.domain.SupporterState
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class AnalyticsMapperTest {

    @Test
    fun `Given supporter states - When mapped - Then the supporter level is returned`() {
        testCases().forEach { testCase ->
            val actual = testCase.input.toSupporterLevel()

            actual shouldBe testCase.result
        }
    }

    private data class TestCase(
        val input: SupporterState,
        val result: SupporterLevel,
    )

    companion object {
        private fun testCases() =
            listOf(
                TestCase(input = SupporterState(), result = SupporterLevel.NONE),
                TestCase(
                    input = SupporterState(oneTimePurchaseCounts = mapOf(SupportAnimal.OWL to 2)),
                    result = SupporterLevel.ONE_TIME,
                ),
                TestCase(
                    input = SupporterState(activeSubscriptions = setOf(SupportAnimal.DEER)),
                    result = SupporterLevel.RECURRING,
                ),
                TestCase(
                    input = SupporterState(
                        activeSubscriptions = setOf(SupportAnimal.DEER),
                        oneTimePurchaseCounts = mapOf(SupportAnimal.OWL to 1),
                    ),
                    result = SupporterLevel.RECURRING,
                ),
            )
    }
}
