package hu.mostoha.mobile.kmp.huki.repository

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import hu.mostoha.mobile.kmp.huki.TestContext.appContext
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.kotest.matchers.collections.shouldHaveSize
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
 * Operates on the app's real `filesDir/gpx/external` directory, cleaned around each test.
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class DefaultGpxStorageTest {

    private val storage = DefaultGpxStorage()

    private val sandboxDir = File(appContext.filesDir, "gpx/external")

    @Before
    @After
    fun cleanSandbox() {
        sandboxDir.deleteRecursively()
    }

    @Test
    fun givenNewFile_whenSave_ToSandbox_thenFileWrittenAndPathReturned() {
        runTest {
            val uri = writeSourceFile("track.gpx", "content-a")

            val imported = storage.saveToSandbox(storage.readGpx(uri.toString()))

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
    fun givenImportedFile_whenDelete_thenFileRemoved() {
        runTest {
            importFile(writeSourceFile("track.gpx", "content-a"))

            storage.delete("track.gpx")

            File(sandboxDir, "track.gpx").exists() shouldBe false
        }
    }

    private suspend fun importFile(uri: Uri): PlatformFile = storage.saveToSandbox(storage.readGpx(uri.toString()))

    private fun writeSourceFile(fileName: String, content: String): Uri {
        val file = File(appContext.cacheDir, fileName).apply { writeText(content) }
        return Uri.fromFile(file)
    }
}
