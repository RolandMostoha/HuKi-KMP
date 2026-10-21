package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.kmp.huki.model.domain.JelSymbol
import io.kotest.matchers.shouldBe
import org.maplibre.spatialk.gpx.Gpx
import kotlin.test.Test

class JelSymbolMapperTest {

    @Test
    fun `Given GPX - When toJelSymbol - Then the track type is mapped to its jel symbol`() {
        testCases().forEach { testCase ->
            val document = Gpx.decodeFromString(testCase.input)

            val actual = document.toJelSymbol()

            actual shouldBe testCase.result
        }
    }

    data class TestCase(
        val input: String,
        val result: JelSymbol?,
    )

    companion object {
        fun testCases() =
            listOf(
                TestCase(input = gpx(type = "zt"), result = JelSymbol.ZT),
                TestCase(input = gpx(type = "k+"), result = JelSymbol.KP),
                TestCase(input = gpx(type = "hiking"), result = null),
                TestCase(input = gpx(type = null), result = null),
            )

        private fun gpx(type: String?): String =
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1" creator="test">
              <trk>
                <name>Jági tanösvény</name>
                ${type?.let { "<type>$it</type>" }.orEmpty()}
                <trkseg>
                  <trkpt lat="47.607343" lon="18.879042"><ele>243.5</ele></trkpt>
                  <trkpt lat="47.607392" lon="18.879238"><ele>242.8</ele></trkpt>
                </trkseg>
              </trk>
            </gpx>
            """.trimIndent()
    }
}
