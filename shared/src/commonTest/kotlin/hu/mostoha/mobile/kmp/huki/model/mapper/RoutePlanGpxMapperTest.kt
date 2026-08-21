package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlan
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerProfile
import hu.mostoha.mobile.kmp.huki.model.domain.RouteStats
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.maplibre.spatialk.gpx.Gpx
import org.maplibre.spatialk.units.extensions.kilometers
import kotlin.test.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class RoutePlanGpxMapperTest {

    @Test
    fun `Given a route plan, When mapped to a GPX document, Then it round-trips through the GPX format`() {
        val routePlan = routePlan()

        val xml = Gpx.encodeToString(
            routePlan.toGpxDocument(TITLE, STOP_NAMES, RoutePlannerProfile.ON_TRAILS, CREATED_AT),
        )
        val decoded = Gpx.decodeFromString(xml)

        decoded.metadata?.name shouldBe TITLE
        decoded.tracks shouldHaveSize 1
        decoded.tracks.first().segments.first().points.map { it.latitude to it.longitude } shouldBe
            routePlan.locations.map { it.latitude to it.longitude }
    }

    @Test
    fun `Given a route plan, When mapped to a GPX document, Then HuKi is watermarked as the creator and author`() {
        val document = routePlan().toGpxDocument(TITLE, STOP_NAMES, RoutePlannerProfile.ON_TRAILS, CREATED_AT)

        document.creator shouldContain "HuKi"
        document.metadata?.author?.name shouldBe "HuKi"
        document.metadata?.author?.link?.href shouldBe "https://huki.hu"
        document.metadata?.timestamp shouldBe CREATED_AT
    }

    @Test
    fun `Given track points with altitude, When mapped to a GPX document, Then the elevation is kept`() {
        val document = routePlan().toGpxDocument(TITLE, STOP_NAMES, RoutePlannerProfile.ON_TRAILS, CREATED_AT)

        document.tracks.first().segments.first().points.map { it.elevation } shouldBe listOf(300.0, 360.0, 420.0)
    }

    @Test
    fun `Given start and end stops, When mapped to a GPX document, Then only intermediate stops become waypoints`() {
        val document = routePlan().toGpxDocument(TITLE, STOP_NAMES, RoutePlannerProfile.ON_TRAILS, CREATED_AT)

        document.waypoints shouldHaveSize 1
        document.waypoints.first().name shouldBe "Rám-szakadék"
        document.waypoints.first().latitude shouldBe 47.55
    }

    @Test
    fun `Given a bike profile, When mapped to a GPX document, Then the track type is cycling`() {
        val document = routePlan().toGpxDocument(TITLE, STOP_NAMES, RoutePlannerProfile.BIKE, CREATED_AT)

        document.tracks.first().type shouldBe "cycling"
    }

    @Test
    fun `Given stop names, When mapped to a title, Then the first and last stop are joined`() {
        placeNames("Dobogókő", "Rám-szakadék", "Pilisszentkereszt").toRoutePlanTitle() shouldBe
            "Dobogókő → Pilisszentkereszt"
    }

    @Test
    fun `Given a round trip, When mapped to a title, Then the same stop is shown on both ends`() {
        placeNames("Dobogókő", "Rám-szakadék", "Dobogókő").toRoutePlanTitle() shouldBe "Dobogókő → Dobogókő"
    }

    @Test
    fun `Given an unresolved start, When mapped to a title, Then only the destination is named`() {
        placeNames(null, "Rám-szakadék", "Dobogókő").toRoutePlanTitle() shouldBe "Dobogókő"
    }

    @Test
    fun `Given an unresolved destination, When mapped to a title, Then only the start is named`() {
        placeNames("Dobogókő", "Rám-szakadék", null).toRoutePlanTitle() shouldBe "Dobogókő"
    }

    @Test
    fun `Given no resolved ends, When mapped to a title, Then a fallback title is used`() {
        placeNames(null, "Rám-szakadék", null).toRoutePlanTitle() shouldBe "Route plan"
    }

    @Test
    fun `Given no stop names, When mapped to a title, Then a fallback title is used`() {
        emptyList<String?>().toRoutePlanTitle() shouldBe "Route plan"
    }

    @Test
    fun `Given a long geocoded stop name, When mapped to a title, Then it is cut back at a word boundary`() {
        val title = placeNames(LONG_STOP_NAME, "Rám-szakadék").toRoutePlanTitle()

        title shouldBe "Dobogókő Pilis kilátó… → Rám-szakadék"
    }

    @Test
    fun `Given a long stop name without spaces, When mapped to a title, Then it is cut hard`() {
        val title = placeNames("Pilisszentkereszthosszúnevűhely", "Rám").toRoutePlanTitle()

        title shouldBe "Pilisszentkereszthosszúne… → Rám"
    }

    @Test
    fun `Given stop names, When mapped to a file name, Then only the destination and the flag are used`() {
        placeNames("Dobogókő", "Rám-szakadék", "Pilisszentkereszt").toRoutePlanFileName(TOKEN) shouldBe
            "Pilisszentkereszt_HuKi_482.gpx"
    }

    @Test
    fun `Given a round trip, When mapped to a file name, Then the stop it started from is used`() {
        placeNames("Dobogókő", "Rám-szakadék", "Dobogókő").toRoutePlanFileName(TOKEN) shouldBe
            "Dobogókő_HuKi_482.gpx"
    }

    @Test
    fun `Given an unresolved destination, When mapped to a file name, Then only the flag is used`() {
        placeNames("Dobogókő", null).toRoutePlanFileName(TOKEN) shouldBe "HuKi_482.gpx"
    }

    @Test
    fun `Given no stop names, When mapped to a file name, Then only the flag is used`() {
        emptyList<String?>().toRoutePlanFileName(TOKEN) shouldBe "HuKi_482.gpx"
    }

    @Test
    fun `Given a destination with illegal characters, When mapped to a file name, Then they are stripped`() {
        val fileName = placeNames("x", "Dobogókő/Pilis: a \"nagy\" túra?").toRoutePlanFileName(TOKEN)

        listOf("/", ":", "\"", "?", "…").forEach { fileName shouldNotContain it }
        fileName shouldContain "HuKi_482.gpx"
    }

    @Test
    fun `Given a long destination, When mapped to a file name, Then it is truncated without an ellipsis`() {
        placeNames("x", "Dobogókő Pilis kilátó torony").toRoutePlanFileName(TOKEN) shouldBe
            "Dobogókő Pilis kilátó_HuKi_482.gpx"
    }

    @Test
    fun `Given a decoded route plan GPX, When read back, Then the intermediate waypoint name survives`() {
        val xml = Gpx.encodeToString(
            routePlan().toGpxDocument(TITLE, STOP_NAMES, RoutePlannerProfile.ON_TRAILS, CREATED_AT),
        )

        val waypoint = Gpx.decodeFromString(xml).waypoints.firstOrNull()

        waypoint.shouldNotBeNull()
        waypoint.name shouldBe "Rám-szakadék"
    }

    private fun routePlan(): RoutePlan =
        RoutePlan(
            waypoints = listOf(
                Location(47.5, 19.0),
                Location(47.55, 18.95),
                Location(47.6, 18.9),
            ),
            locations = listOf(
                Location(47.5, 19.0, 300.0),
                Location(47.55, 18.95, 360.0),
                Location(47.6, 18.9, 420.0),
            ),
            routeStats = RouteStats(
                travelTime = 2.hours,
                distance = 8.5.kilometers,
                incline = 240,
                decline = 120,
            ),
        )

    companion object {
        private const val TITLE = "Dobogókő → Pilisszentkereszt"
        private const val LONG_STOP_NAME = "Dobogókő Pilis kilátó torony"
        private const val TOKEN = 482
        private val STOP_NAMES = placeNames("Dobogókő", "Rám-szakadék", "Pilisszentkereszt")

        private val CREATED_AT = Instant.parse("2026-08-19T10:15:00Z")

        private fun placeNames(vararg names: String?): List<String?> = names.toList()
    }
}
