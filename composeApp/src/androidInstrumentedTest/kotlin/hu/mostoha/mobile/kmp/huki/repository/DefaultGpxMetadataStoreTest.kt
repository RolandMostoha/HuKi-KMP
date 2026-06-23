package hu.mostoha.mobile.kmp.huki.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import hu.mostoha.mobile.kmp.huki.TestContext.appContext
import hu.mostoha.mobile.kmp.huki.model.data.GpxMetadataEntry
import hu.mostoha.mobile.kmp.huki.util.toIsoOffsetString
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.time.Instant

@RunWith(AndroidJUnit4::class)
@MediumTest
class DefaultGpxMetadataStoreTest {

    private val gpxDir = File(appContext.filesDir, "gpx")
    private val metadataFile = File(gpxDir, "metadata.json")

    @Before
    @After
    fun cleanSandbox() {
        gpxDir.deleteRecursively()
    }

    @Test
    fun givenRecordedMark_whenReadByNewStore_thenMarkPersisted() {
        runTest {
            val openedAt = Instant.fromEpochSeconds(1_700_000_000)
            DefaultGpxMetadataStore().recordOpened(entry("track-1", openedAt))

            val metadata = DefaultGpxMetadataStore().getMetadata()

            metadata.gpxFiles shouldHaveSize 1
            metadata.gpxFiles.first().trackId shouldBe "track-1"
            metadata.gpxFiles.first().lastOpened shouldBe openedAt.toIsoOffsetString()
        }
    }

    @Test
    fun givenCorruptMetadata_whenGetMetadata_thenRebuildsEmpty() {
        runTest {
            gpxDir.mkdirs()
            metadataFile.writeText("{ not valid json")

            val metadata = DefaultGpxMetadataStore().getMetadata()

            metadata.gpxFiles shouldHaveSize 0
        }
    }

    @Test
    fun givenMissingMetadata_whenGetMetadata_thenReturnsEmpty() {
        runTest {
            DefaultGpxMetadataStore().getMetadata().gpxFiles shouldHaveSize 0
        }
    }

    @Test
    fun givenMultipleAttributes_whenRemove_thenStaleAttributesRemoved() {
        runTest {
            val store = DefaultGpxMetadataStore()
            store.recordOpened(entry("track-1", Instant.fromEpochSeconds(1_700_000_000)))
            store.recordOpened(entry("track-2", Instant.fromEpochSeconds(1_700_000_100)))

            store.remove(setOf("track-1"))

            val metadata = DefaultGpxMetadataStore().getMetadata()
            metadata.gpxFiles shouldHaveSize 1
            metadata.gpxFiles.first().trackId shouldBe "track-1"
        }
    }

    @Test
    fun givenMultipleAttributes_whenRemoveByFileName_thenOnlyMatchingRemoved() {
        runTest {
            val store = DefaultGpxMetadataStore()
            store.recordOpened(entry("track-1", Instant.fromEpochSeconds(1_700_000_000)))
            store.recordOpened(entry("track-2", Instant.fromEpochSeconds(1_700_000_100)))

            store.remove("track-1.gpx")

            val metadata = DefaultGpxMetadataStore().getMetadata()
            metadata.gpxFiles shouldHaveSize 1
            metadata.gpxFiles.first().trackId shouldBe "track-2"
        }
    }

    private fun entry(trackId: String, openedAt: Instant): GpxMetadataEntry =
        GpxMetadataEntry(
            trackId = trackId,
            lastOpened = openedAt.toIsoOffsetString(),
            lastModified = openedAt.toIsoOffsetString(),
            fileName = "$trackId.gpx",
            fileUri = "uri/$trackId.gpx",
            title = "Title of $trackId",
            distanceMeters = 1000.0,
            travelTimeSeconds = 3600,
            incline = 100,
            decline = 100,
        )
}
