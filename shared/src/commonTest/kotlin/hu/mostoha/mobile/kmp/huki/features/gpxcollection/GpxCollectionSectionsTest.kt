package hu.mostoha.mobile.kmp.huki.features.gpxcollection

import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.GpxCollectionItem
import hu.mostoha.mobile.kmp.huki.model.domain.GpxCollectionSortMode
import hu.mostoha.mobile.kmp.huki.model.domain.GpxDistanceRange
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileDay
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileHeader
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileItem
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileSection
import hu.mostoha.mobile.kmp.huki.model.domain.GpxLandscape
import hu.mostoha.mobile.kmp.huki.model.domain.GpxOrigin
import io.kotest.matchers.shouldBe
import kotlinx.datetime.TimeZone
import org.maplibre.spatialk.units.extensions.kilometers
import kotlin.test.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class GpxCollectionSectionsTest {

    @Test
    fun `Given files - When grouped by date - Then days are newest first with landscape tags`() {
        val older = gpxFile("older", lastUsed = NOW - 5.days, landscapeId = "matra")
        val today = gpxFile("today", lastUsed = NOW - 1.hours)
        val yesterday = gpxFile("yesterday", lastUsed = NOW - 1.days)

        val actual = listOf(older, today, yesterday).toGpxFileSections(GpxCollectionSortMode.DATE, NOW, TimeZone.UTC)

        actual shouldBe listOf(
            GpxFileSection(GpxFileHeader.Day(GpxFileDay.Today), listOf(GpxCollectionItem(today, null))),
            GpxFileSection(GpxFileHeader.Day(GpxFileDay.Yesterday), listOf(GpxCollectionItem(yesterday, null))),
            GpxFileSection(
                GpxFileHeader.Day(GpxFileDay.Date("2026.10.28")),
                listOf(GpxCollectionItem(older, MATRA)),
            ),
        )
    }

    @Test
    fun `Given files - When grouped by distance - Then ranges are longest first`() {
        val short = gpxFile("short", kilometers = 4.99)
        val five = gpxFile("five", kilometers = 5.0)
        val ten = gpxFile("ten", kilometers = 10.0)
        val twenty = gpxFile("twenty", kilometers = 20.0)
        val long = gpxFile("long", kilometers = 86.5)

        val actual = listOf(short, five, ten, twenty, long)
            .toGpxFileSections(GpxCollectionSortMode.DISTANCE, NOW, TimeZone.UTC)

        actual shouldBe listOf(
            GpxFileSection(
                GpxFileHeader.Distance(GpxDistanceRange.OVER_20),
                listOf(GpxCollectionItem(long, null), GpxCollectionItem(twenty, null)),
            ),
            GpxFileSection(
                GpxFileHeader.Distance(GpxDistanceRange.FROM_10_TO_20),
                listOf(GpxCollectionItem(ten, null)),
            ),
            GpxFileSection(
                GpxFileHeader.Distance(GpxDistanceRange.FROM_5_TO_10),
                listOf(GpxCollectionItem(five, null)),
            ),
            GpxFileSection(
                GpxFileHeader.Distance(GpxDistanceRange.UNDER_5),
                listOf(GpxCollectionItem(short, null)),
            ),
        )
    }

    @Test
    fun `Given files - When grouped by landscape - Then landscapes keep the bundled order and the outside ones are last`() {
        val outside = gpxFile("outside", lastUsed = NOW - 1.hours)
        val bukk = gpxFile("bukk", lastUsed = NOW - 2.hours, landscapeId = "bukk")
        val matraNewer = gpxFile("matra_newer", lastUsed = NOW - 1.hours, landscapeId = "matra")
        val matraOlder = gpxFile("matra_older", lastUsed = NOW - 1.days, landscapeId = "matra")

        val actual = listOf(outside, matraOlder, bukk, matraNewer)
            .toGpxFileSections(GpxCollectionSortMode.LANDSCAPE, NOW, TimeZone.UTC)

        actual shouldBe listOf(
            GpxFileSection(
                GpxFileHeader.Landscape(BUKK),
                listOf(GpxCollectionItem(bukk, null)),
            ),
            GpxFileSection(
                GpxFileHeader.Landscape(MATRA),
                listOf(
                    GpxCollectionItem(matraNewer, null),
                    GpxCollectionItem(matraOlder, null),
                ),
            ),
            GpxFileSection(
                GpxFileHeader.OutsideLandscapes,
                listOf(GpxCollectionItem(outside, null)),
            ),
        )
    }

    companion object {
        private val NOW = Instant.parse("2026-11-02T12:00:00Z")
        private val MATRA = GpxLandscape(
            id = "matra",
            nameRes = SharedRes.strings.landscape_matra,
            colorRes = SharedRes.colors.landscapeMatra,
            iconRes = SharedRes.images.ic_landscapes_mountain_high,
        )
        private val BUKK = GpxLandscape(
            id = "bukk",
            nameRes = SharedRes.strings.landscape_bukk,
            colorRes = SharedRes.colors.landscapeBukk,
            iconRes = SharedRes.images.ic_landscapes_mountain_high,
        )

        private fun gpxFile(
            name: String,
            lastUsed: Instant = NOW,
            kilometers: Double = 10.0,
            landscapeId: String? = null,
        ) = GpxFileItem(
            fileName = "$name.gpx",
            fileUri = "uri/$name.gpx",
            trackId = "track-$name",
            title = null,
            totalDistance = kilometers.kilometers,
            travelTime = 2.hours,
            incline = 100,
            decline = 100,
            lastModified = lastUsed,
            lastOpened = null,
            origin = GpxOrigin.EXTERNAL,
            landscapeId = landscapeId,
        )
    }
}
