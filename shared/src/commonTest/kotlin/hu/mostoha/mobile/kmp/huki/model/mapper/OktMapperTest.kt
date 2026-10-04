package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.kmp.huki.data.TEST_OKT_LOCATIONS
import hu.mostoha.mobile.kmp.huki.data.TEST_OKT_SECTION_01
import hu.mostoha.mobile.kmp.huki.data.TEST_OKT_STAMP
import hu.mostoha.mobile.kmp.huki.data.TEST_OKT_TRAIL
import hu.mostoha.mobile.kmp.huki.model.domain.OktMarkerType
import hu.mostoha.mobile.kmp.huki.model.domain.OktSectionGeometry
import hu.mostoha.mobile.kmp.huki.model.domain.OktStampTag
import hu.mostoha.mobile.kmp.huki.model.domain.OktType
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import org.maplibre.spatialk.geojson.LineString
import kotlin.test.Test

class OktMapperTest {

    @Test
    fun `Given stamping point description - When parsed - Then it returns the stamp tag`() {
        testCases().forEach { testCase ->
            val actual = testCase.description.toOktStampTag(testCase.stampPrefix)

            actual shouldBe testCase.result
        }
    }

    @Test
    fun `Given full trail - When mapped to section item - Then it has no badge number nor stamps and links the trail website`() {
        val fullTrail = TEST_OKT_TRAIL.sections.first()

        val actual = fullTrail.toOktSectionItem(OktType.OKT)

        actual.badgePrefix shouldBe "OKT"
        actual.badgeNumber shouldBe null
        actual.stamps shouldBe emptyList()
        actual.websiteUrl shouldBe OktType.OKT.websiteUrl
    }

    @Test
    fun `Given section - When mapped to section item - Then it has badge number and stamps with leg distance and website`() {
        val section = TEST_OKT_TRAIL.sections[1]

        val actual = section.toOktSectionItem(OktType.OKT)

        actual.badgePrefix shouldBe "OKT"
        actual.badgeNumber shouldBe "01"
        actual.stamps.map { it.tag } shouldBe listOf("OKTPH_02")
        actual.stamps.first().legDistance shouldStartWith "+"
        actual.websiteUrl shouldBe "https://www.kektura.hu/okt-szakasz/okt-01"
    }

    @Test
    fun `Given section with stamps - When mapped to section item - Then reversed stamps measure legs from the end`() {
        val section = OktSectionGeometry(
            section = TEST_OKT_SECTION_01,
            locations = TEST_OKT_LOCATIONS.subList(0, 3),
            stamps = listOf(
                TEST_OKT_STAMP,
                TEST_OKT_STAMP.copy(tag = "OKTPH_03", location = TEST_OKT_LOCATIONS[2], number = 3.0),
            ),
        )

        val actual = section.toOktSectionItem(OktType.OKT)

        actual.reversedStamps.map { it.tag } shouldBe listOf("OKTPH_03", "OKTPH_02")
        actual.reversedStamps.first().legDistance shouldBe "+0 m"
        actual.reversedStamps.last().legDistance shouldBe actual.stamps.last().legDistance
    }

    @Test
    fun `Given section - When mapped to markers - Then start and end enclose the stamps`() {
        val section = TEST_OKT_TRAIL.sections[1]

        val actual = section.toOktMarkers()

        actual.map { it.type } shouldBe listOf(OktMarkerType.START, OktMarkerType.STAMP, OktMarkerType.END)
        actual.first().name shouldBe "Írott-kő"
        actual.last().name shouldBe "Sárvár"
    }

    @Test
    fun `Given locations - When mapped to OktLine - Then bounds are the corners`() {
        val locations = TEST_OKT_TRAIL.locations

        val actual = locations.toOktLine("okt_base")

        actual.id shouldBe "okt_base"
        LineString.fromJson(actual.geoJson).coordinates.map { it.latitude to it.longitude } shouldBe
            locations.map { it.latitude to it.longitude }
        actual.bounds.first().latitude shouldBe locations.minOf { it.latitude }
        actual.bounds.last().longitude shouldBe locations.maxOf { it.longitude }
    }

    companion object {
        fun testCases() =
            listOf(
                TestCase(
                    description = "Hét-forrás - A forrás melletti esőbeálló bejárati oszlopán. (OKTPH_02)",
                    stampPrefix = "OKTPH",
                    result = OktStampTag(tag = "OKTPH_02", number = 2.0),
                ),
                TestCase(
                    description = "Írott-kő - A kilátó bejáratánál. (OKTPH_01_2)",
                    stampPrefix = "OKTPH",
                    result = OktStampTag(tag = "OKTPH_01_2", number = 1.2),
                ),
                TestCase(
                    description = "Szentgál - A kocsma falán. (OKTPH_63_B)",
                    stampPrefix = "OKTPH",
                    result = OktStampTag(tag = "OKTPH_63_B", number = 63.0),
                ),
                TestCase(
                    description = "Írott-kő - A kilátóban. (OKTPH_01_DDKPH_01_2)",
                    stampPrefix = "OKTPH",
                    result = OktStampTag(tag = "OKTPH_01_DDKPH_01_2", number = 1.0),
                ),
                TestCase(
                    description = "Írott-kő - A kilátóban. (OKTPH_01_DDKPH_01_2)",
                    stampPrefix = "DDKPH",
                    result = OktStampTag(tag = "OKTPH_01_DDKPH_01_2", number = 1.2),
                ),
                TestCase(
                    description = "Nagy-Hideg-hegy - A turistaház. (EM134INF) (OKTPH_111_1)",
                    stampPrefix = "OKTPH",
                    result = OktStampTag(tag = "OKTPH_111_1", number = 111.1),
                ),
                TestCase(
                    description = "Szekszárd - A buszállomáson. (AKPH_01)",
                    stampPrefix = "AKPH",
                    result = OktStampTag(tag = "AKPH_01", number = 1.0),
                ),
                TestCase(
                    description = "No stamp code in this description",
                    stampPrefix = "OKTPH",
                    result = null,
                ),
            )
    }

    data class TestCase(
        val description: String,
        val stampPrefix: String,
        val result: OktStampTag?,
    )
}
