import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.security.SecureRandom
import java.util.Properties

/**
 * Generates the `expect object Secrets` (no [platformSuffix]) or one platform's XOR-masked `actual` from `secrets.properties`.
 * Keys ending in `_ANDROID` / `_IOS` are exposed without the suffix and only compiled into their own platform.
 */
abstract class GenerateSecretsTask : DefaultTask() {
    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val secretsFile: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val platformSuffix: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val properties = loadProperties()
        val sharedSecrets = properties.filterKeys { key -> PLATFORM_SUFFIXES.none { key.endsWith(it) } }
        val platformSecretNames = properties.keys
            .mapNotNull { key -> PLATFORM_SUFFIXES.firstOrNull { key.endsWith(it) }?.let { key.removeSuffix(it) } }
            .toSet()
        val secretNames = (sharedSecrets.keys + platformSecretNames).sorted()
        val conflicts = sharedSecrets.keys.intersect(platformSecretNames)
        if (conflicts.isNotEmpty()) {
            throw GradleException("Secrets: remove the unsuffixed keys, they shadow the platform ones: ${conflicts.joinToString()}")
        }

        val suffix = platformSuffix.orNull
        if (suffix == null) {
            writeCommon(secretNames)
        } else {
            writeActual(suffix, secretNames, sharedSecrets, properties)
        }
    }

    private fun loadProperties(): Map<String, String> {
        val file = secretsFile.orNull?.asFile
        if (file == null || !file.exists()) {
            logger.warn("Secrets: secrets.properties missing — generating an empty Secrets object")
            return emptyMap()
        }
        val properties = Properties().apply { file.inputStream().use { load(it) } }
        // Log key names only — never values.
        logger.lifecycle("Secrets: loaded [${properties.stringPropertyNames().sorted().joinToString()}]")
        return properties.stringPropertyNames().associateWith { properties.getProperty(it).trim().removeSurrounding("\"") }
    }

    private fun writeCommon(secretNames: List<String>) {
        val declarations = secretNames.joinToString("\n") { "    val $it: String" }
        writeSource(
            """
            |package $PACKAGE
            |
            |expect object Secrets {
            |$declarations
            |}
            |
            |internal fun revealSecret(masked: IntArray, mask: IntArray): String =
            |    CharArray(masked.size) { index -> (masked[index] xor mask[index]).toChar() }.concatToString()
            |""".trimMargin(),
        )
    }

    private fun writeActual(
        suffix: String,
        secretNames: List<String>,
        sharedSecrets: Map<String, String>,
        properties: Map<String, String>,
    ) {
        val missing = secretNames.filter { name -> name !in sharedSecrets && properties[name + suffix].isNullOrEmpty() }
        if (missing.isNotEmpty()) {
            throw GradleException("Secrets: missing from secrets.properties: ${missing.joinToString { it + suffix }}")
        }
        val declarations = secretNames.joinToString("\n") { name ->
            val value = sharedSecrets[name] ?: properties.getValue(name + suffix)
            "    actual val $name: String by lazy { ${maskedExpression(value)} }"
        }
        writeSource(
            """
            |package $PACKAGE
            |
            |actual object Secrets {
            |$declarations
            |}
            |""".trimMargin(),
        )
    }

    private fun maskedExpression(value: String): String {
        val mask = IntArray(value.length) { random.nextInt(MASK_BOUND) }
        val masked = IntArray(value.length) { value[it].code xor mask[it] }
        return "revealSecret(intArrayOf(${masked.joinToString()}), intArrayOf(${mask.joinToString()}))"
    }

    private fun writeSource(source: String) {
        val file = File(outputDirectory.get().asFile, FILE_NAME)
        file.parentFile.mkdirs()
        file.writeText(source)
        logger.lifecycle("Secrets: wrote ${file.absolutePath}")
    }

    private companion object {
        const val PACKAGE = "hu.mostoha.mobile.kmp.huki"
        const val FILE_NAME = "Secrets.kt"
        const val MASK_BOUND = 0xFFFF
        val PLATFORM_SUFFIXES = listOf("_ANDROID", "_IOS")
        val random = SecureRandom()
    }
}
