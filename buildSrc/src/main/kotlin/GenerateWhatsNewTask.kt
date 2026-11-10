import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.Properties

/**
 * Generates `WhatsNewContent` from `tools/release/whatsnew/<platform>/v<version>/`, one version line per platform.
 * Each platform's actual only carries its own releases, keyed by `<platform>AppVersion` in `version.properties`.
 */
abstract class GenerateWhatsNewTask : DefaultTask() {
    @get:InputFile
    abstract val versionFile: RegularFileProperty

    @get:InputDirectory
    abstract val whatsNewRootDir: DirectoryProperty

    @get:OutputDirectory
    abstract val commonKotlinOutputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val androidKotlinOutputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val iosKotlinOutputDir: DirectoryProperty

    @get:OutputFile
    abstract val baseStringsFile: RegularFileProperty

    @get:OutputFile
    abstract val huStringsFile: RegularFileProperty

    /** A single localized moko string: resource name → value per locale. */
    private data class Loc(val name: String, val byLocale: Map<String, String>)

    private data class MessageRes(val title: String, val body: String, val footer: String?)

    private data class Release(val version: String, val releaseDate: String, val notesName: String, val message: MessageRes?)

    private enum class Platform(val dirName: String, val versionKey: String) {
        ANDROID("android", "androidAppVersion"),
        IOS("ios", "iosAppVersion"),
    }

    @TaskAction
    fun generate() {
        val versionProperties = Properties().apply {
            versionFile.get().asFile.inputStream().use { load(it) }
        }

        val strings = mutableListOf<Loc>()
        Platform.entries.forEach { platform ->
            val appVersion = versionProperties.getProperty(platform.versionKey)
                ?: error("WhatsNew: '${platform.versionKey}' missing from ${versionFile.get().asFile.path}")
            val root = whatsNewRootDir.get().asFile.resolve(platform.dirName)
            val versionDirs = root.listFiles { file -> file.isDirectory && file.name.startsWith("v") }.orEmpty()
            require(versionDirs.any { it.name == "v$appVersion" }) {
                "WhatsNew: no changelog dir for ${platform.versionKey}=$appVersion (expected ${root.path}/v$appVersion)"
            }
            val releases = versionDirs
                .map { it.toRelease(platform, strings) }
                .sortedWith(compareByDescending(VERSION_COMPARATOR) { it.version.toVersionKey() })
            val outputDir = when (platform) {
                Platform.ANDROID -> androidKotlinOutputDir
                Platform.IOS -> iosKotlinOutputDir
            }
            writeActual(outputDir.get().asFile, appVersion, releases)
            logger.lifecycle(
                "WhatsNew: generated ${platform.dirName} ${releases.map { it.version }} (current=$appVersion)",
            )
        }

        writeStringsXml(baseStringsFile.get().asFile, strings, EN_LOCALE, required = true)
        writeStringsXml(huStringsFile.get().asFile, strings, HU_LOCALE, required = false)
        writeCommon()
    }

    private fun File.toRelease(platform: Platform, strings: MutableList<Loc>): Release {
        val version = name.removePrefix("v")
        val base = "whatsnew_${platform.dirName}_v" + version.replace(".", "_")

        val notes = listFiles { file -> file.name.startsWith("whatsnew-") && file.name.endsWith(".md") }
            .orEmpty()
            .associate { file ->
                file.name.removePrefix("whatsnew-").removeSuffix(".md") to file.readText().trimEnd()
            }
        require(notes.containsKey(EN_LOCALE)) {
            "WhatsNew: missing whatsnew-$EN_LOCALE.md in $path (base locale is required)"
        }
        strings += Loc(base, notes)

        val metadata = JsonSlurper().parse(resolve("metadata.json")) as Map<*, *>
        val releaseDate = metadata["releaseDate"] as? String
            ?: error("WhatsNew: 'releaseDate' missing from $path/metadata.json")
        try {
            LocalDate.parse(releaseDate)
        } catch (exception: DateTimeParseException) {
            error("WhatsNew: invalid 'releaseDate' \"$releaseDate\" in $path/metadata.json (expected ISO yyyy-MM-dd): ${exception.message}")
        }

        val message = (metadata["message"] as? Map<*, *>)?.let { messageNode ->
            val titleName = "${base}_message_title"
            val bodyName = "${base}_message_body"
            strings += Loc(titleName, messageNode.localeMap("title", path))
            strings += Loc(bodyName, messageNode.localeMap("body", path))
            val footerName = if (messageNode["footer"] != null) {
                "${base}_message_footer".also { name -> strings += Loc(name, messageNode.localeMap("footer", path)) }
            } else {
                null
            }
            MessageRes(titleName, bodyName, footerName)
        }

        return Release(version, releaseDate, base, message)
    }

    private fun Map<*, *>.localeMap(key: String, path: String): Map<String, String> {
        val node = when (val value = this[key]) {
            is Map<*, *> -> value
            null -> error("WhatsNew: message.$key missing from $path/metadata.json")
            else -> error("WhatsNew: message.$key must be a locale object (e.g. {\"$EN_LOCALE\": \"...\"}) in $path/metadata.json")
        }
        val map = node.toLocaleMap()
        require(map.containsKey(EN_LOCALE)) { "WhatsNew: message.$key missing $EN_LOCALE in $path/metadata.json" }
        return map
    }

    private fun Map<*, *>.toLocaleMap(): Map<String, String> =
        entries.associate { (locale, value) -> locale.toString() to value.toString() }

    private fun writeStringsXml(file: File, strings: List<Loc>, locale: String, required: Boolean) {
        val entries = strings.sortedBy { it.name }.mapNotNull { string ->
            val content = string.byLocale[locale]
                ?: if (required) error("WhatsNew: missing $locale value for ${string.name}") else return@mapNotNull null
            """    <string name="${string.name}">${content.escapeXml()}</string>"""
        }
        file.parentFile.mkdirs()
        file.writeText(
            """
            |<?xml version="1.0" encoding="UTF-8"?>
            |<!-- GENERATED from tools/release/whatsnew by the generateWhatsNew Gradle task — do not edit. -->
            |<resources>
            |${entries.joinToString("\n")}
            |</resources>
            |""".trimMargin()
        )
    }

    private fun writeCommon() {
        writeKotlin(
            commonKotlinOutputDir.get().asFile,
            """
            |package hu.mostoha.mobile.kmp.huki
            |
            |import dev.icerock.moko.resources.StringResource
            |
            |// GENERATED from tools/release/whatsnew by the generateWhatsNew Gradle task — do not edit.
            |object WhatsNewContent {
            |    val currentVersion: String get() = platformWhatsNewCurrentVersion
            |
            |    val releases: List<Entry> get() = platformWhatsNewReleases
            |
            |    data class Message(
            |        val title: StringResource,
            |        val body: StringResource,
            |        val footer: StringResource?,
            |    )
            |
            |    data class Entry(
            |        val version: String,
            |        val releaseDate: String,
            |        val notes: StringResource,
            |        val message: Message?,
            |    )
            |}
            |
            |internal expect val platformWhatsNewCurrentVersion: String
            |
            |internal expect val platformWhatsNewReleases: List<WhatsNewContent.Entry>
            |""".trimMargin(),
        )
    }

    private fun writeActual(outputDir: File, appVersion: String, releases: List<Release>) {
        val entries = releases.joinToString(",\n") { it.render() }
        writeKotlin(
            outputDir,
            """
            |package hu.mostoha.mobile.kmp.huki
            |
            |import hu.mostoha.mobile.huki.shared.SharedRes
            |import hu.mostoha.mobile.kmp.huki.WhatsNewContent.Entry
            |import hu.mostoha.mobile.kmp.huki.WhatsNewContent.Message
            |
            |// GENERATED from tools/release/whatsnew by the generateWhatsNew Gradle task — do not edit.
            |internal actual val platformWhatsNewCurrentVersion: String = "$appVersion"
            |
            |internal actual val platformWhatsNewReleases: List<Entry> = listOf(
            |$entries,
            |)
            |""".trimMargin(),
        )
    }

    private fun writeKotlin(outputDir: File, source: String) {
        val file = outputDir.resolve("WhatsNewContent.kt")
        file.parentFile.mkdirs()
        file.writeText(source)
    }

    private fun Release.render(): String {
        val messageArg = message?.let { message ->
            """message = Message(
            |            title = SharedRes.strings.${message.title},
            |            body = SharedRes.strings.${message.body},
            |            footer = ${message.footer?.let { "SharedRes.strings.$it" } ?: "null"},
            |        )""".trimMargin()
        } ?: "message = null"
        return """    Entry(
            |        version = "$version",
            |        releaseDate = "$releaseDate",
            |        notes = SharedRes.strings.$notesName,
            |        $messageArg,
            |    )""".trimMargin()
    }

    private fun String.toVersionKey(): List<Int> = split(".").map { it.toIntOrNull() ?: 0 }

    private fun String.escapeXml(): String =
        replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("'", "\\'")
            .replace("\"", "\\\"")
            .replace("\r", "")
            .replace("\n", "\\n")

    private companion object {
        const val EN_LOCALE = "en-US"
        const val HU_LOCALE = "hu-HU"
        val VERSION_COMPARATOR = Comparator<List<Int>> { a, b ->
            val size = maxOf(a.size, b.size)
            for (i in 0 until size) {
                val diff = a.getOrElse(i) { 0 } - b.getOrElse(i) { 0 }
                if (diff != 0) return@Comparator diff
            }
            0
        }
    }
}
