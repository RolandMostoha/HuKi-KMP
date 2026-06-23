package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.kmp.huki.data.TEST_GPX_DETAILS
import hu.mostoha.mobile.kmp.huki.model.data.GpxMetadataEntry
import hu.mostoha.mobile.kmp.huki.util.toIsoOffsetString
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.maplibre.spatialk.units.extensions.inMeters
import org.maplibre.spatialk.units.extensions.meters
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class GpxMapperTest {

    @Test
    fun `Given gpx details, When toGpxFileItem, Then attributes and recency fields are mapped`() {
        val details = TEST_GPX_DETAILS
        val lastModified = Instant.fromEpochSeconds(1_700_000_000)
        val lastOpened = Instant.fromEpochSeconds(1_700_086_400)

        val actual = details.toGpxFileItem(
            trackId = "track-1",
            lastModified = lastModified,
            lastOpened = lastOpened,
        )

        actual.fileName shouldBe details.fileName
        actual.fileUri shouldBe details.fileUri
        actual.trackId shouldBe "track-1"
        actual.title shouldBe details.title
        actual.totalDistance shouldBe details.totalDistance
        actual.travelTime shouldBe details.travelTime
        actual.incline shouldBe details.incline
        actual.decline shouldBe details.decline
        actual.lastModified shouldBe lastModified
        actual.lastOpened shouldBe lastOpened
    }

    @Test
    fun `Given gpx details, When toMetadataEntry, Then attributes are serialized to the cache entry`() {
        val details = TEST_GPX_DETAILS
        val lastModified = Instant.fromEpochSeconds(1_700_000_000)
        val openedAt = Instant.fromEpochSeconds(1_700_086_400)

        val actual = details.toMetadataEntry(
            trackId = "track-1",
            lastModified = lastModified,
            openedAt = openedAt,
        )

        actual.trackId shouldBe "track-1"
        actual.lastOpened shouldBe openedAt.toIsoOffsetString()
        actual.lastModified shouldBe lastModified.toIsoOffsetString()
        actual.fileName shouldBe details.fileName
        actual.fileUri shouldBe details.fileUri
        actual.title shouldBe details.title
        actual.distanceMeters shouldBe details.totalDistance.inMeters
        actual.travelTimeSeconds shouldBe details.travelTime.inWholeSeconds
        actual.incline shouldBe details.incline
        actual.decline shouldBe details.decline
    }

    @Test
    fun `Given valid metadata entry, When toGpxFileItem, Then it maps back to a gpx file item`() {
        val lastModified = Instant.fromEpochSeconds(1_700_000_000)
        val lastOpened = Instant.fromEpochSeconds(1_700_086_400)
        val entry = metadataEntry(
            lastOpened = lastOpened.toIsoOffsetString(),
            lastModified = lastModified.toIsoOffsetString(),
        )

        val actual = entry.toGpxFileItem()

        requireNotNull(actual)
        actual.fileName shouldBe entry.fileName
        actual.fileUri shouldBe entry.fileUri
        actual.trackId shouldBe entry.trackId
        actual.title shouldBe entry.title
        actual.totalDistance shouldBe entry.distanceMeters.meters
        actual.travelTime shouldBe entry.travelTimeSeconds.seconds
        actual.incline shouldBe entry.incline
        actual.decline shouldBe entry.decline
        actual.lastModified shouldBe lastModified
        actual.lastOpened shouldBe lastOpened
    }

    @Test
    fun `Given metadata entry with malformed lastOpened, When toGpxFileItem, Then null returns`() {
        val entry = metadataEntry(
            lastOpened = "not-a-date",
            lastModified = Instant.fromEpochSeconds(1_700_000_000).toIsoOffsetString(),
        )

        entry.toGpxFileItem().shouldBeNull()
    }

    @Test
    fun `Given malformed lastModified, When toGpxFileItem, Then lastModified falls back to lastOpened`() {
        val lastOpened = Instant.fromEpochSeconds(1_700_086_400)
        val entry = metadataEntry(
            lastOpened = lastOpened.toIsoOffsetString(),
            lastModified = "not-a-date",
        )

        val actual = entry.toGpxFileItem()

        requireNotNull(actual)
        actual.lastModified shouldBe lastOpened
        actual.lastOpened shouldBe lastOpened
    }

    private fun metadataEntry(lastOpened: String, lastModified: String) =
        GpxMetadataEntry(
            trackId = "track-1",
            lastOpened = lastOpened,
            lastModified = lastModified,
            fileName = "dera_szurdok.gpx",
            fileUri = "file://dera_szurdok.gpx",
            title = "OKT 5 - Rozália téglagyár - Dobogókő",
            distanceMeters = 15_000.0,
            travelTimeSeconds = 7_200,
            incline = 500,
            decline = 300,
        )
}
