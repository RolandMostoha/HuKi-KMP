package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.kmp.huki.model.domain.OneTimePurchaseRecord
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SupportPurchaseMapperTest {

    @Test
    fun `Given stored entry - When mapped - Then it returns the purchase record or null`() {
        testCases().forEach { testCase ->
            val actual = testCase.input.toOneTimePurchaseRecord()

            actual shouldBe testCase.result
        }
    }

    @Test
    fun `Given purchase records - When stored and read back - Then the records are unchanged`() {
        val records = listOf(
            OneTimePurchaseRecord("huki_support_one_time_level_1", 2, 1_700_000_000_000),
            OneTimePurchaseRecord("huki_support_one_time_level_2", 1, 0),
        )

        val actual = records.toDataStoreEntries().toOneTimePurchaseRecords()

        actual shouldBe records
    }

    companion object {
        fun testCases() =
            listOf(
                TestCase(
                    input = "huki_support_one_time_level_1;2;1700000000000",
                    result = OneTimePurchaseRecord("huki_support_one_time_level_1", 2, 1_700_000_000_000),
                ),
                TestCase(input = "huki_support_one_time_level_1;0;1700000000000", result = null),
                TestCase(input = "huki_support_one_time_level_1;2;-1", result = null),
                TestCase(input = "huki_support_one_time_level_1;two;1700000000000", result = null),
                TestCase(input = "huki_support_one_time_level_1;2", result = null),
                TestCase(input = ";2;1700000000000", result = null),
                TestCase(input = "", result = null),
            )
    }

    data class TestCase(
        val input: String,
        val result: OneTimePurchaseRecord?,
    )
}
