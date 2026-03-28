package hu.mostoha.mobile.kmp.huki.repository

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import hu.mostoha.mobile.kmp.huki.TestContext.appContext
import hu.mostoha.mobile.kmp.huki.TestContext.instrumentationContext
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

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
