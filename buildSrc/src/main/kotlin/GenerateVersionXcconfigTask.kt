import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.util.Properties

abstract class GenerateVersionXcconfigTask : DefaultTask() {
    @get:InputFile
    abstract val versionFile: RegularFileProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val versionProperties = Properties().apply {
            versionFile.get().asFile.inputStream().use { load(it) }
        }

        val appVersion = versionProperties.getProperty("appVersion")
        val iosBuildNumber = versionProperties.getProperty("iosBuildNumber")

        val file = outputFile.get().asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            |// GENERATED from version.properties by the generateVersionXcconfig Gradle task — do not edit.
            |MARKETING_VERSION=$appVersion
            |CURRENT_PROJECT_VERSION=$iosBuildNumber
            |""".trimMargin()
        )

        logger.lifecycle("Version: wrote ${file.absolutePath} (MARKETING_VERSION=$appVersion, CURRENT_PROJECT_VERSION=$iosBuildNumber)")
    }
}
