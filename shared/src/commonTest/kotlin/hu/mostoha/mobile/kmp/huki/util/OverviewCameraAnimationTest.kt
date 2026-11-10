package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.CameraPosition
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.util.MyLocationCameraAnimation.MAX_DURATION
import hu.mostoha.mobile.kmp.huki.util.MyLocationCameraAnimation.MIN_DURATION
import hu.mostoha.mobile.kmp.huki.util.OverviewCameraAnimation.FAR_MAX_DURATION
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class OverviewCameraAnimationTest {

    @Test
    fun `Given a nearby route - When transition - Then the camera animates within the near limit`() {
        val camera = camera(DOBOGOKO, zoom = 14.0)

        val actual = OverviewCameraAnimation.transition(camera, PILIS_ROUTE)

        val animate = actual.shouldBeInstanceOf<OverviewCameraTransition.Animate>()
        animate.duration shouldBeGreaterThan MIN_DURATION
        animate.duration shouldBeLessThanOrEqualTo MAX_DURATION
    }

    @Test
    fun `Given a far route - When transition - Then the camera animates with extra duration`() {
        val camera = camera(DOBOGOKO, zoom = 14.5)

        val actual = OverviewCameraAnimation.transition(camera, BUDAPEST_ROUTE)

        val animate = actual.shouldBeInstanceOf<OverviewCameraTransition.Animate>()
        animate.duration shouldBeGreaterThan MAX_DURATION
        animate.duration shouldBeLessThanOrEqualTo FAR_MAX_DURATION
    }

    @Test
    fun `Given a route in another region at street zoom - When transition - Then the camera jumps close and arrives`() {
        val camera = camera(DOBOGOKO, zoom = 16.0)

        val actual = OverviewCameraAnimation.transition(camera, DEBRECEN_ROUTE)

        val jump = actual.shouldBeInstanceOf<OverviewCameraTransition.JumpAndArrive>()
        jump.start.location.longitude shouldBeLessThan DEBRECEN_ROUTE.minOf { it.longitude }
        jump.start.zoom shouldBeLessThan camera.zoom
        jump.duration shouldBeLessThanOrEqualTo MAX_DURATION
    }

    @Test
    fun `Given the Hungary overview - When a route across the country opens - Then the camera animates`() {
        val camera = camera(DOBOGOKO, zoom = 5.4)

        val actual = OverviewCameraAnimation.transition(camera, DEBRECEN_ROUTE)

        actual.shouldBeInstanceOf<OverviewCameraTransition.Animate>()
    }

    @Test
    fun `Given no bounds - When transition - Then it animates for the minimum`() {
        val actual = OverviewCameraAnimation.transition(camera(DOBOGOKO, zoom = 14.0), emptyList())

        actual shouldBe OverviewCameraTransition.Animate(MIN_DURATION)
    }

    companion object {
        private val DOBOGOKO = Location(47.7181, 18.8948)
        private val PILIS_ROUTE = listOf(Location(47.6911, 18.8995), Location(47.7181, 18.8948))
        private val BUDAPEST_ROUTE = listOf(Location(47.5200, 19.0000), Location(47.5300, 19.0100))
        private val DEBRECEN_ROUTE = listOf(Location(47.5316, 21.6273), Location(47.5500, 21.6500))

        private fun camera(location: Location, zoom: Double) = CameraPosition(location, zoom, bearing = 0.0, pitch = 0.0)
    }
}
