import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.util.Properties

abstract class GenerateSecretsTask : DefaultTask() {
    @get:InputFile
    @get:Optional
    abstract val secretsFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val secretProperties = Properties().apply {
            if (secretsFile.isPresent) {
                val file = secretsFile.get().asFile
                if (file.exists()) {
                    logger.lifecycle("Secrets: loading from ${file.absolutePath}")
                    file.inputStream().use { load(it) }
                } else {
                    logger.warn("Secrets: file declared but missing at ${file.absolutePath} — generating empty Secrets object")
                }
            } else {
                logger.warn("Secrets: no secretsFile configured — generating empty Secrets object")
            }
        }

        // Log key names only — never values.
        val keys = secretProperties.stringPropertyNames().sorted()
        logger.lifecycle("Secrets: loaded ${keys.size} entries [${keys.joinToString()}]")

        val file = outputDirectory.get().file("Secrets.kt").asFile
        file.parentFile.mkdirs()

        val secrets = secretProperties
            .map { "    const val ${it.key} = ${it.value}" }
            .joinToString("\n")

        file.writeText(
            """
            |package hu.mostoha.mobile.kmp.huki
            |
            |object Secrets {
            |$secrets
            |}
            |""".trimMargin()
        )

        logger.lifecycle("Secrets: wrote ${file.absolutePath}")
    }
}
