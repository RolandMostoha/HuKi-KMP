package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.kmp.huki.data.Landscapes
import hu.mostoha.mobile.kmp.huki.model.domain.HikeRecommendation
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class HikeRecommendationMapperTest {

    @Test
    fun `Given location - When mapped to url - Then it links to the closest landscape`() {
        testCases().forEach { testCase ->
            val actual = testCase.recommendation.toUrl(testCase.location)

            actual shouldBe testCase.result
        }
    }

    @Test
    fun `Given closest landscape without tags - When mapped to url - Then it returns the base url`() {
        val untaggedLandscapes = listOf(Landscapes.first().copy(areaTags = emptyMap(), termeszetjaroTag = null))

        HikeRecommendation.entries.forEach { recommendation ->
            val actual = recommendation.toUrl(BUKK_LOCATION, untaggedLandscapes)

            actual shouldBe recommendation.baseUrl
        }
    }

    data class TestCase(
        val recommendation: HikeRecommendation,
        val location: Location,
        val result: String,
    )

    companion object {
        private val BUKK_LOCATION = Location(48.05, 20.5)
        private val BALATON_FELVIDEK_LOCATION = Location(46.92, 17.73)

        fun testCases() =
            listOf(
                TestCase(
                    recommendation = HikeRecommendation.AKTIVKALANDOR,
                    location = BUKK_LOCATION,
                    result = "https://aktivkalandor.hu/tajegysegek/bukk",
                ),
                TestCase(
                    recommendation = HikeRecommendation.KIRANDULASTIPPEK,
                    location = BUKK_LOCATION,
                    result = "https://kirandulastippek.hu/bukk?tag=gyalogtura",
                ),
                TestCase(
                    recommendation = HikeRecommendation.TERMESZETJARO,
                    location = BUKK_LOCATION,
                    result = "https://www.termeszetjaro.hu/hu/tours/?cat=22729870#area=23359977" +
                        "&wt=B%C3%BCkkvid%C3%A9k+%28%C3%89szak-Magyarorsz%C3%A1g%2C+Magyarorsz%C3%A1g%29",
                ),
                TestCase(
                    recommendation = HikeRecommendation.KIRANDULASTIPPEK,
                    location = BALATON_FELVIDEK_LOCATION,
                    result = "https://kirandulastippek.hu/balaton-felvidek?tag=gyalogtura",
                ),
                TestCase(
                    recommendation = HikeRecommendation.TERMESZETJARO,
                    location = BALATON_FELVIDEK_LOCATION,
                    result = "https://www.termeszetjaro.hu/hu/tours/?cat=22729870#area=23598301" +
                        "&wt=Balaton-felvid%C3%A9k+%28Hegycsoport%29",
                ),
            )
    }
}
