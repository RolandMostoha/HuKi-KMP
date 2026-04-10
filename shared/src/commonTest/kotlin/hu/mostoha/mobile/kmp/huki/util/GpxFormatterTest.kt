package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.util.formatter.GpxFormatter
import io.kotest.matchers.shouldBe
import org.maplibre.spatialk.gpx.Gpx
import kotlin.test.Test

class GpxFormatterTest {

    @Test
    fun `Given metadata name, When formatting title, Then metadata name is returned`() {
        val input = decodeGpx(
            """
            <gpx version="1.1" creator="test" xmlns="http://www.topografix.com/GPX/1/1">
                <metadata>
                    <name>Metadata title</name>
                </metadata>
            </gpx>
            """.trimIndent(),
        )

        val actual = GpxFormatter.formatTitle(input)

        actual shouldBe "Metadata title"
    }

    @Test
    fun `Given track name only, When formatting title, Then track name is returned`() {
        val input = decodeGpx(
            """
            <gpx version="1.1" creator="test" xmlns="http://www.topografix.com/GPX/1/1">
                <trk>
                    <name>Track title</name>
                </trk>
            </gpx>
            """.trimIndent(),
        )

        val actual = GpxFormatter.formatTitle(input)

        actual shouldBe "Track title"
    }

    @Test
    fun `Given short track name and description, When formatting title, Then both are returned`() {
        val input = decodeGpx(
            """
            <gpx version="1.1" creator="test" xmlns="http://www.topografix.com/GPX/1/1">
                <trk>
                    <name>OKT-15</name>
                    <desc>Rozalia teglagyar - Dobogoko</desc>
                </trk>
            </gpx>
            """.trimIndent(),
        )

        val actual = GpxFormatter.formatTitle(input)

        actual shouldBe "OKT-15 - Rozalia teglagyar - Dobogoko"
    }

    @Test
    fun `Given long track name and description, When formatting title, Then only name is returned`() {
        val input = decodeGpx(
            """
            <gpx version="1.1" creator="test" xmlns="http://www.topografix.com/GPX/1/1">
                <trk>
                    <name>This track name is definitely long</name>
                    <desc>Should not be appended</desc>
                </trk>
            </gpx>
            """.trimIndent(),
        )

        val actual = GpxFormatter.formatTitle(input)

        actual shouldBe "This track name is definitely long"
    }

    @Test
    fun `Given route name only, When formatting title, Then route name is returned`() {
        val input = decodeGpx(
            """
            <gpx version="1.1" creator="test" xmlns="http://www.topografix.com/GPX/1/1">
                <rte>
                    <name>Route title</name>
                </rte>
            </gpx>
            """.trimIndent(),
        )

        val actual = GpxFormatter.formatTitle(input)

        actual shouldBe "Route title"
    }

    @Test
    fun `Given short route name and description, When formatting title, Then both are returned`() {
        val input = decodeGpx(
            """
            <gpx version="1.1" creator="test" xmlns="http://www.topografix.com/GPX/1/1">
                <rte>
                    <name>Blue route</name>
                    <desc>Forest loop</desc>
                </rte>
            </gpx>
            """.trimIndent(),
        )

        val actual = GpxFormatter.formatTitle(input)

        actual shouldBe "Blue route - Forest loop"
    }

    @Test
    fun `Given blank metadata and track values, When formatting title, Then blank values are ignored`() {
        val input = decodeGpx(
            """
            <gpx version="1.1" creator="test" xmlns="http://www.topografix.com/GPX/1/1">
                <metadata>
                    <name>   </name>
                </metadata>
                <trk>
                    <name>   </name>
                    <desc>Track description</desc>
                </trk>
            </gpx>
            """.trimIndent(),
        )

        val actual = GpxFormatter.formatTitle(input)

        actual shouldBe "Track description"
    }

    @Test
    fun `Given blank metadata track and route values, When formatting title, Then null is returned`() {
        val input = decodeGpx(
            """
            <gpx version="1.1" creator="test" xmlns="http://www.topografix.com/GPX/1/1">
                <metadata>
                    <name>   </name>
                </metadata>
                <trk>
                    <name>   </name>
                    <desc>   </desc>
                </trk>
                <rte>
                    <name>   </name>
                    <desc>   </desc>
                </rte>
            </gpx>
            """.trimIndent(),
        )

        val actual = GpxFormatter.formatTitle(input)

        actual shouldBe null
    }

    @Test
    fun `Given no metadata tracks or routes, When formatting title, Then null is returned`() {
        val input = decodeGpx(
            """
            <gpx version="1.1" creator="test" xmlns="http://www.topografix.com/GPX/1/1" />
            """.trimIndent(),
        )

        val actual = GpxFormatter.formatTitle(input)

        actual shouldBe null
    }

    private fun decodeGpx(xml: String) = Gpx.decodeFromString(xml)
}
