import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.Properties

/**
 * Renders the current release's What's New markdown into the `release_notes.txt` files
 * `fastlane deliver` uploads, so the in-app sheet and the App Store page never drift apart.
 */
abstract class GenerateStoreReleaseNotesTask : DefaultTask() {
    @get:InputFile
    abstract val versionFile: RegularFileProperty

    @get:InputDirectory
    abstract val whatsNewRootDir: DirectoryProperty

    @get:OutputFile
    abstract val enReleaseNotesFile: RegularFileProperty

    @get:OutputFile
    abstract val huReleaseNotesFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val versionProperties = Properties().apply {
            versionFile.get().asFile.inputStream().use { load(it) }
        }
        val appVersion = versionProperties.getProperty("appVersion")
            ?: error("StoreReleaseNotes: 'appVersion' missing from ${versionFile.get().asFile.path}")

        val versionDir = whatsNewRootDir.get().asFile.resolve("v$appVersion")
        require(versionDir.isDirectory) {
            "StoreReleaseNotes: no changelog dir for appVersion=$appVersion (expected ${versionDir.path})"
        }

        write(versionDir, EN_LOCALE, enReleaseNotesFile.get().asFile, required = true)
        write(versionDir, HU_LOCALE, huReleaseNotesFile.get().asFile, required = false)
    }

    private fun write(versionDir: File, locale: String, outputFile: File, required: Boolean) {
        val notes = versionDir.resolve("whatsnew-$locale.md")
        if (!notes.isFile) {
            require(!required) { "StoreReleaseNotes: missing ${notes.path} (base locale is required)" }
            // Delete instead of leaving the previous version's notes for deliver to upload.
            val deleted = outputFile.delete()
            logger.lifecycle("StoreReleaseNotes: no $locale notes${if (deleted) ", removed stale ${outputFile.name}" else ""}")
            return
        }

        val bullets = notes.readLines()
            .map { it.trim().removePrefix("-").removePrefix("*").trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n") { "$BULLET $it" }

        require(bullets.isNotEmpty()) { "StoreReleaseNotes: ${notes.path} has no bullet lines" }
        require(bullets.length <= MAX_LENGTH) {
            "StoreReleaseNotes: $locale release notes are ${bullets.length} chars, App Store allows $MAX_LENGTH"
        }

        outputFile.parentFile.mkdirs()
        outputFile.writeText(bullets)
        logger.lifecycle("StoreReleaseNotes: wrote ${outputFile.path} (${bullets.length} chars)")
    }

    private companion object {
        const val EN_LOCALE = "en-US"
        const val HU_LOCALE = "hu-HU"
        const val BULLET = "•"
        const val MAX_LENGTH = 4000
    }
}
