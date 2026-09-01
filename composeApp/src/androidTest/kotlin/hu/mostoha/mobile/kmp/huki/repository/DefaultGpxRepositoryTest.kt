package hu.mostoha.mobile.kmp.huki.repository

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import hu.mostoha.mobile.kmp.huki.TestContext.appContext
import hu.mostoha.mobile.kmp.huki.TestContext.instrumentationContext
import hu.mostoha.mobile.kmp.huki.model.domain.EmptyGpxContentException
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.MalformedGpxException
import hu.mostoha.mobile.kmp.huki.model.domain.NonGpxFileException
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlan
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerProfile
import hu.mostoha.mobile.kmp.huki.model.domain.RouteStats
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.maplibre.spatialk.units.extensions.kilometers
import java.io.File
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.hours

/**
 * Real file-system integration test: parses GPXs from instrumentation assets.
 *
 * Acts as a safeguard for the GPX parser against malformed, edge-case, and round-trip
 * GPX files we have encountered in the wild.
 * Requires:
 *  - Test GPX fixtures under `composeApp/src/androidInstrumentedTest/assets/`.
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class DefaultGpxRepositoryTest {

    val repository = DefaultGpxRepository(
        DefaultGpxStorage(),
        DefaultGpxMetadataStore(FakeCrashlyticsService),
        FakeCrashlyticsService,
    )

    @Test
    fun givenRoutePlan_whenSaveRoutePlan_thenWrittenToTheRoutePlannerDirAndReopensAsGpx() {
        runTest {
            val uri = repository.saveRoutePlan(ROUTE_PLAN, STOP_NAMES, RoutePlannerProfile.ON_TRAILS)

            uri shouldContain "gpx/routeplanner/"
            File(uri).name shouldContain "Pilisszentkereszt_HuKi_"
            File(uri).exists() shouldBe true
            File(uri).readText() shouldContain "HuKi"

            val gpx = repository.readGpxFile(uri)

            gpx.title shouldBe "Dobogókő → Pilisszentkereszt"
            gpx.locations.size shouldBe ROUTE_PLAN.locations.size
            gpx.waypoints.count { it.type == WaypointType.START } shouldBe 1
            gpx.waypoints.count { it.type == WaypointType.END } shouldBe 1
            gpx.waypoints.count { it.type == WaypointType.INTERMEDIATE } shouldBe 1
        }
    }

    @Test
    fun givenUnresolvedDestination_whenSaveRoutePlan_thenOnlyTheFlagNamesTheFile() {
        runTest {
            val stopPlaceNames = listOf("Dobogókő", null)

            val uri = repository.saveRoutePlan(ROUTE_PLAN, stopPlaceNames, RoutePlannerProfile.ON_TRAILS)

            File(uri).name.startsWith("HuKi_") shouldBe true
            repository.readGpxFile(uri).title shouldBe "Dobogókő"
        }
    }

    @Test
    fun givenSavedRoutePlan_whenReopened_thenItIsNotCopiedIntoTheExternalDir() {
        runTest {
            val uri = repository.saveRoutePlan(ROUTE_PLAN, STOP_NAMES, RoutePlannerProfile.ON_TRAILS)
            val fileName = File(uri).name

            repository.readGpxFile(uri)

            File(appContext.filesDir, "gpx/external/$fileName").exists() shouldBe false
            repository.getGpxFiles().count { it.fileName == fileName } shouldBe 1
        }
    }

    @Test
    fun givenSavedRoutePlan_whenGetGpxFiles_thenItIsBrowsableAlongsideExternalFiles() {
        runTest {
            val planUri = repository.saveRoutePlan(ROUTE_PLAN, STOP_NAMES, RoutePlannerProfile.ON_TRAILS)
            repository.readGpxFile(saveTestGpx("gpx_test_with_routes.gpx").toString())

            val fileNames = repository.getGpxFiles().map { it.fileName }

            fileNames shouldContain File(planUri).name
            fileNames shouldContain "gpx_test_with_routes.gpx"
        }
    }

    @Test
    fun givenGpxWithRoutes_whenReadGpxFile_thenCorrectGpxReturns() {
        runTest {
            val uri = saveTestGpx("gpx_test_with_routes.gpx")

            val gpx = repository.readGpxFile(uri.toString())

            gpx.locations shouldNot beEmpty()
            gpx.waypoints shouldNot beEmpty()
        }
    }

    @Test
    fun givenGpxWithWaypointsOnly_whenReadGpxFile_thenCorrectGpxReturns() {
        runTest {
            val uri = saveTestGpx("gpx_test_waypoints_only.gpx")

            val gpx = repository.readGpxFile(uri.toString())

            gpx.locations should beEmpty()
            gpx.waypoints shouldNot beEmpty()
        }
    }

    @Test
    fun givenGpxWithComments_whenReadGpxFile_thenCorrectGpxReturns() {
        runTest {
            val uri = saveTestGpx("gpx_test_with_comments.gpx")

            val gpx = repository.readGpxFile(uri.toString())

            gpx.title shouldBe "OKT-15 - Rozália téglagyár - Dobogókő"
            gpx.locations shouldNot beEmpty()
            gpx.waypoints.count { it.type == WaypointType.START } shouldBe 1
            gpx.waypoints.count { it.type == WaypointType.END } shouldBe 1
            gpx.waypoints.count { it.type == WaypointType.INTERMEDIATE } shouldBe 5
            gpx.waypoints.count { !it.description.isNullOrBlank() } shouldBe 5
        }
    }

    @Test
    fun givenGpxRoundTrip_whenReadGpxFile_thenCorrectGpxReturns() {
        runTest {
            val uri = saveTestGpx("gpx_test_round_trip.gpx")

            val gpx = repository.readGpxFile(uri.toString())

            gpx.locations shouldNot beEmpty()
            gpx.waypoints.count { it.type == WaypointType.ROUND_TRIP } shouldBe 1
            gpx.waypoints.count { it.type == WaypointType.START } shouldBe 0
            gpx.waypoints.count { it.type == WaypointType.END } shouldBe 0
        }
    }

    @Test
    fun givenGpxWithoutRouteTrackOrWaypoints_whenReadGpxFile_thenEmptyContentExceptionReturns() {
        runTest {
            val uri = saveTestGpx("gpx_test_empty_content.gpx")

            assertFailsWith<EmptyGpxContentException> {
                repository.readGpxFile(uri.toString())
            }
        }
    }

    @Test
    fun givenMalformedGpx_whenReadGpxFile_thenMalformedExceptionReturns() {
        runTest {
            val uri = saveTestGpx("gpx_test_malformed.gpx")

            assertFailsWith<MalformedGpxException> {
                repository.readGpxFile(uri.toString())
            }
        }
    }

    @Test
    fun givenMalformedGpx_whenReadGpxFile_thenInvalidCopyNotKeptInSandbox() {
        runTest {
            val fileName = "gpx_test_malformed.gpx"
            val uri = saveTestGpx(fileName)

            assertFailsWith<MalformedGpxException> {
                repository.readGpxFile(uri.toString())
            }

            File(appContext.filesDir, "gpx/external/$fileName").exists() shouldBe false
        }
    }

    @Test
    fun givenNonGpxFile_whenReadGpxFile_thenNonGpxExceptionReturns() {
        runTest {
            val uri = saveTestGpx("gpx_test_non_gpx.txt")

            assertFailsWith<NonGpxFileException> {
                repository.readGpxFile(uri.toString())
            }
        }
    }

    @Test
    fun givenSavedGpx_whenDeleteGpxFile_thenFileIsRemovedFromCollection() {
        runTest {
            val uri = saveTestGpx("gpx_test_with_routes.gpx")
            val gpx = repository.readGpxFile(uri.toString())

            val item = repository.getGpxFiles().first { it.fileName == gpx.fileName }

            repository.deleteGpxFile(item.fileUri, item.trackId)

            repository.getGpxFiles().map { it.fileName } shouldNotContain gpx.fileName
        }
    }

    @Test
    fun givenSameNameInBothOrigins_whenDeleteGpxFile_thenOnlyTheTargetedFileIsRemoved() {
        runTest {
            val planUri = repository.saveRoutePlan(ROUTE_PLAN, STOP_NAMES, RoutePlannerProfile.ON_TRAILS)
            val sharedName = File(planUri).name
            // The same plan re-imported from Downloads lands in the external dir under its original name.
            val externalUri = saveTestGpx("gpx_test_with_routes.gpx", fileName = sharedName)
            repository.readGpxFile(externalUri.toString())

            val items = repository.getGpxFiles().filter { it.fileName == sharedName }
            items shouldHaveSize 2

            val plan = items.first { it.fileUri.contains("routeplanner") }
            repository.deleteGpxFile(plan.fileUri, plan.trackId)

            File(plan.fileUri).exists() shouldBe false
            File(appContext.filesDir, "gpx/external/$sharedName").exists() shouldBe true
            repository.getGpxFiles().count { it.fileName == sharedName } shouldBe 1
        }
    }

    @Test
    fun givenSavedGpx_whenDeleteGpxFile_thenFileIsRemovedFromRecent() {
        runTest {
            val uri = saveTestGpx("gpx_test_with_routes.gpx")
            val gpx = repository.readGpxFile(uri.toString())

            val item = repository.getRecentGpxFiles(limit = 3).first { it.fileName == gpx.fileName }

            repository.deleteGpxFile(item.fileUri, item.trackId)

            repository.getRecentGpxFiles(limit = 3).map { it.fileName } shouldNotContain gpx.fileName
        }
    }

    @Test
    fun givenReadGpxFile_whenGetGpxFiles_thenLastOpenedAndTrackIdRecorded() {
        runTest {
            val uri = saveTestGpx("gpx_test_with_routes.gpx")
            val gpx = repository.readGpxFile(uri.toString())

            val item = repository.getGpxFiles().single { it.fileName == gpx.fileName }

            item.lastOpened shouldNotBe null
            item.trackId.length shouldBe 16
        }
    }

    @Test
    fun givenRecentlyOpenedGpx_whenGetRecentGpxFiles_thenReturnedWithinLimit() {
        runTest {
            val uri = saveTestGpx("gpx_test_with_routes.gpx")
            val gpx = repository.readGpxFile(uri.toString())

            val recent = repository.getRecentGpxFiles(limit = 3)

            recent.map { it.fileName } shouldContain gpx.fileName
        }
    }

    @Test
    fun givenSandboxFileMissing_whenGetRecentGpxFiles_thenEntryOmittedAndPrunedFromMetadata() {
        runTest {
            val uri = saveTestGpx("gpx_test_with_routes.gpx")
            val gpx = repository.readGpxFile(uri.toString())

            // Delete the sandbox file directly, bypassing deleteGpxFile which would also prune metadata.
            File(appContext.filesDir, "gpx/external/${gpx.fileName}").delete() shouldBe true

            repository.getRecentGpxFiles(limit = 3).map { it.fileName } shouldNotContain gpx.fileName
            val metadata = DefaultGpxMetadataStore(FakeCrashlyticsService).getMetadata()
            metadata.gpxFiles.map { it.fileName } shouldNotContain gpx.fileName
        }
    }

    private fun saveTestGpx(assetName: String, fileName: String = assetName): Uri {
        val inputStream = instrumentationContext.assets.open(assetName)
        val file = File(appContext.cacheDir.path + "/$fileName").apply {
            outputStream().use { fileOut ->
                inputStream.copyTo(fileOut)
            }
        }
        return Uri.fromFile(file)
    }

    companion object {
        private val STOP_NAMES = listOf<String?>("Dobogókő", "Rám-szakadék", "Pilisszentkereszt")
        private val ROUTE_PLAN = RoutePlan(
            waypoints = listOf(
                Location(47.7193911, 18.8961602),
                Location(47.7000000, 18.9000000),
                Location(47.6800000, 18.9200000),
            ),
            locations = listOf(
                Location(47.7193911, 18.8961602, 700.0),
                Location(47.7000000, 18.9000000, 500.0),
                Location(47.6800000, 18.9200000, 300.0),
            ),
            routeStats = RouteStats(
                travelTime = 2.hours,
                distance = 8.5.kilometers,
                incline = 240,
                decline = 120,
            ),
        )
    }
}
