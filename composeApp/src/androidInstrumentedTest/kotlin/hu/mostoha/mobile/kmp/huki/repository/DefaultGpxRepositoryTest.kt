package hu.mostoha.mobile.kmp.huki.repository

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import hu.mostoha.mobile.kmp.huki.TestContext.appContext
import hu.mostoha.mobile.kmp.huki.TestContext.instrumentationContext
import hu.mostoha.mobile.kmp.huki.model.domain.EmptyGpxContentException
import hu.mostoha.mobile.kmp.huki.model.domain.MalformedGpxException
import hu.mostoha.mobile.kmp.huki.model.domain.NonGpxFileException
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.test.assertFailsWith

@RunWith(AndroidJUnit4::class)
class DefaultGpxRepositoryTest {

    val repository = DefaultGpxRepository()

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
    fun givenNonGpxFile_whenReadGpxFile_thenNonGpxExceptionReturns() {
        runTest {
            val uri = saveTestGpx("gpx_test_non_gpx.txt")

            assertFailsWith<NonGpxFileException> {
                repository.readGpxFile(uri.toString())
            }
        }
    }

    private fun saveTestGpx(fileName: String): Uri {
        val inputStream = instrumentationContext.assets.open(fileName)
        val file = File(appContext.cacheDir.path + "/$fileName").apply {
            outputStream().use { fileOut ->
                inputStream.copyTo(fileOut)
            }
        }
        return Uri.fromFile(file)
    }
}
