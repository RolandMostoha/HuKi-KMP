package hu.mostoha.mobile.kmp.huki.repository

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import hu.mostoha.mobile.kmp.huki.TestContext.appContext
import hu.mostoha.mobile.kmp.huki.model.domain.GpxOrigin
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.path
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Real file-system integration test for the GPX sandbox storage.
 *
 * Operates on the app's real `filesDir/gpx` directory, cleaned around each test.
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class DefaultGpxStorageTest {

    private val storage = DefaultGpxStorage()

    private val gpxDir = File(appContext.filesDir, "gpx")
    private val sandboxDir = File(gpxDir, GpxOrigin.EXTERNAL.dirName)
    private val routePlannerDir = File(gpxDir, GpxOrigin.ROUTE_PLANNER.dirName)

    @Before
    @After
    fun cleanSandbox() {
        sandboxDir.deleteRecursively()
        routePlannerDir.deleteRecursively()
    }

    @Test
    fun givenNewFile_whenSave_ToSandbox_thenFileWrittenAndPathReturned() {
        runTest {
            val uri = writeSourceFile("track.gpx", "content-a")

            val imported = storage.saveToSandbox(storage.readGpx(uri.toString()), GpxOrigin.EXTERNAL)

            imported.name shouldBe "track.gpx"
            File(sandboxDir, "track.gpx").exists() shouldBe true
            File(sandboxDir, "track.gpx").readText() shouldBe "content-a"
        }
    }

    @Test
    fun givenSameContent_whenImportTwice_thenSingleFileReused() {
        runTest {
            val uri = writeSourceFile("track.gpx", "content-a")

            val first = importFile(uri)
            val second = importFile(uri)

            second.name shouldBe first.name
            sandboxDir.listFiles().orEmpty() shouldHaveSize 1
        }
    }

    @Test
    fun givenSameNameDifferentContent_whenImport_thenNameSuffixed() {
        runTest {
            val first = importFile(writeSourceFile("track.gpx", "content-a"))
            val second = importFile(writeSourceFile("track.gpx", "content-b"))

            first.name shouldBe "track.gpx"
            second.name shouldBe "track (2).gpx"
            File(sandboxDir, "track (2).gpx").readText() shouldBe "content-b"
        }
    }

    @Test
    fun givenImportedFiles_whenListGpxFiles_thenAllReturned() {
        runTest {
            importFile(writeSourceFile("a.gpx", "a"))
            importFile(writeSourceFile("b.gpx", "b"))

            val files = storage.listGpxFiles()

            files.map { it.name }.sorted() shouldBe listOf("a.gpx", "b.gpx")
        }
    }

    @Test
    fun givenBothOrigins_whenListGpxFiles_thenFilesOfEveryOriginReturned() {
        runTest {
            importFile(writeSourceFile("external.gpx", "a"))
            importFile(writeSourceFile("plan.gpx", "b"), GpxOrigin.ROUTE_PLANNER)

            val files = storage.listGpxFiles()

            files.map { it.name }.sorted() shouldBe listOf("external.gpx", "plan.gpx")
        }
    }

    @Test
    fun givenRoutePlannerFile_whenResolveGpxFile_thenFoundOutsideTheExternalDir() {
        runTest {
            importFile(writeSourceFile("plan.gpx", "b"), GpxOrigin.ROUTE_PLANNER)

            val resolved = storage.resolveGpxFile("plan.gpx")

            resolved.shouldNotBeNull()
            File(routePlannerDir, "plan.gpx").exists() shouldBe true
        }
    }

    @Test
    fun givenSandboxFile_whenResolveSandboxFile_thenTheSameFileReturned() {
        runTest {
            val imported = importFile(writeSourceFile("plan.gpx", "b"), GpxOrigin.ROUTE_PLANNER)

            val resolved = storage.resolveSandboxFile(imported.path)

            resolved.shouldNotBeNull()
            resolved.name shouldBe "plan.gpx"
        }
    }

    @Test
    fun givenFileOutsideTheSandbox_whenResolveSandboxFile_thenNullReturned() {
        runTest {
            val uri = writeSourceFile("track.gpx", "content-a")

            storage.resolveSandboxFile(uri.path!!).shouldBeNull()
        }
    }

    @Test
    fun givenRoutePlannerFile_whenDelete_thenFileRemoved() {
        runTest {
            val imported = importFile(writeSourceFile("plan.gpx", "b"), GpxOrigin.ROUTE_PLANNER)

            storage.delete(imported.path)

            File(routePlannerDir, "plan.gpx").exists() shouldBe false
        }
    }

    @Test
    fun givenSameNameInBothOrigins_whenDelete_thenOnlyTheTargetedFileRemoved() {
        runTest {
            val external = importFile(writeSourceFile("same.gpx", "a"))
            val plan = importFile(writeSourceFile("same.gpx", "b"), GpxOrigin.ROUTE_PLANNER)

            storage.delete(plan.path)

            File(routePlannerDir, "same.gpx").exists() shouldBe false
            File(sandboxDir, "same.gpx").exists() shouldBe true
            external.name shouldBe "same.gpx"
        }
    }

    @Test
    fun givenFileOutsideTheSandbox_whenDelete_thenNothingIsRemoved() {
        runTest {
            val uri = writeSourceFile("outside.gpx", "content-a")

            storage.delete(uri.path!!)

            File(appContext.cacheDir, "outside.gpx").exists() shouldBe true
        }
    }

    @Test
    fun givenImportedFile_whenDelete_thenFileRemoved() {
        runTest {
            val imported = importFile(writeSourceFile("track.gpx", "content-a"))

            storage.delete(imported.path)

            File(sandboxDir, "track.gpx").exists() shouldBe false
        }
    }

    private suspend fun importFile(uri: Uri, origin: GpxOrigin = GpxOrigin.EXTERNAL): PlatformFile = storage.saveToSandbox(storage.readGpx(uri.toString()), origin)

    private fun writeSourceFile(fileName: String, content: String): Uri {
        val file = File(appContext.cacheDir, fileName).apply { writeText(content) }
        return Uri.fromFile(file)
    }
}
