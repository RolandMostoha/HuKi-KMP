package hu.mostoha.mobile.kmp.huki.ui.features.support

import hu.mostoha.mobile.kmp.huki.model.domain.ProductPrice
import io.kotest.matchers.shouldBe
import java.util.Locale
import kotlin.test.Test

class ProductPriceFormatTest {

    @Test
    fun `Given product prices - When formatted - Then the expected text is returned`() {
        testCases().forEach { testCase ->
            val actual = testCase.price.format(testCase.locale)

            actual shouldBe testCase.result
        }
    }

    private data class TestCase(
        val price: ProductPrice,
        val locale: Locale,
        val result: String,
    )

    companion object {
        private const val NO_BREAK_SPACE = ' '

        private fun testCases() =
            listOf(
                TestCase(ProductPrice(499_000_000, "HUF"), Locale.US, "HUF${NO_BREAK_SPACE}499"),
                TestCase(ProductPrice(1_499_000_000, "HUF"), Locale.US, "HUF${NO_BREAK_SPACE}1,499"),
                TestCase(ProductPrice(4_500_000, "USD"), Locale.US, "$4.50"),
                TestCase(ProductPrice(5_000_000, "USD"), Locale.US, "$5"),
                TestCase(ProductPrice(499_000_000, "HUF"), Locale.forLanguageTag("hu-HU"), "499${NO_BREAK_SPACE}Ft"),
            )
    }
}
