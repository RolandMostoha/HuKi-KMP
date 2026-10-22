package hu.mostoha.mobile.kmp.huki.data

import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldNotBeNull
import org.maplibre.spatialk.gpx.Gpx
import java.io.File
import kotlin.test.Test

class EducationalTrailGpxTest {

    private val trailFiles = File("src/commonMain/moko-resources/files")
        .listFiles { file -> file.name.startsWith("edut_") && file.extension == "gpx" }
        .orEmpty()

    @Test
    fun `Given bundled educational trail GPX files - When decoded - Then every track point has an elevation`() {
        trailFiles.toList().shouldNotBeEmpty()
        trailFiles.forEach { file ->
            withClue(file.name) {
                val document = Gpx.decodeFromString(file.readText())

                val points = document.tracks.flatMap { it.segments }.flatMap { it.points }

                points.size shouldBeGreaterThan 1
                points.forEach { it.elevation.shouldNotBeNull() }
            }
        }
    }
}
