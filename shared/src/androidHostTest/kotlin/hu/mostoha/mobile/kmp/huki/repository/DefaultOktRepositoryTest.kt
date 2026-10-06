package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.OktType
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.Test

class DefaultOktRepositoryTest {

    private val gpxFiles = mapOf(
        OktType.OKT to "okt_teljes_bh_20260924.gpx",
        OktType.RPDDK to "rpddk_teljes_bh_20260806.gpx",
        OktType.AKT to "ak_teljes_bh_20260924.gpx",
    )
    private val repository = DefaultOktRepository(
        bundledFileReader = { file ->
            val type = OktType.entries.first { it.gpxFile == file }
            File("src/commonMain/moko-resources/files/${gpxFiles.getValue(type)}").readText()
        },
        dispatcher = Dispatchers.Default,
    )

    @Test
    fun `Given bundled GPX files - When getOktTrail - Then every section is sliced with its stamps`() {
        runTest {
            OktType.entries.forEach { type ->
                val trail = repository.getOktTrail(type)

                trail.sections.size shouldBe type.sections.size
                trail.sections.filter { it.section.id != type.fullTrailId }.forEach { geometry ->
                    geometry.locations.size shouldBeGreaterThan 1
                    geometry.stamps.shouldNotBeEmpty()
                }
            }
        }
    }

    @Test
    fun `Given OKT GPX - When getOktTrail - Then the base line is a fraction of the full track`() {
        runTest {
            val trail = repository.getOktTrail(OktType.OKT)

            trail.locations.size shouldBeGreaterThan 40_000
            trail.baseLine.size shouldBeLessThan trail.locations.size / 4
        }
    }

    @Test
    fun `Given loaded trail - When getOktTrail again - Then the cached trail returns`() {
        runTest {
            val first = repository.getOktTrail(OktType.RPDDK)

            val second = repository.getOktTrail(OktType.RPDDK)

            second shouldBeSameInstanceAs first
        }
    }
}
