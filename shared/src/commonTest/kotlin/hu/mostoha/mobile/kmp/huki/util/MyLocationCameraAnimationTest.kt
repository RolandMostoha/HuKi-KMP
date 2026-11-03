package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.CameraPosition
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.util.MyLocationCameraAnimation.MAX_DURATION
import hu.mostoha.mobile.kmp.huki.util.MyLocationCameraAnimation.MIN_DURATION
import io.kotest.assertions.withClue
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class MyLocationCameraAnimationTest {

    @Test
    fun `Given zoom change only - When duration - Then it grows with the zoom levels`() {
        testCases().forEach { testCase ->
            val actual = MyLocationCameraAnimation.maxDuration(
                testCase.camera,
                myLocation = null,
                alreadyFollowing = testCase.alreadyFollowing,
            )

            withClue(testCase) { actual shouldBe testCase.result }
        }
    }

    @Test
    fun `Given same zoom - When my location is further away - Then duration is longer`() {
        val camera = camera(DOBOGOKO, zoom = 16.0)

        val near = MyLocationCameraAnimation.maxDuration(camera, PILISSZENTKERESZT, alreadyFollowing = true)
        val far = MyLocationCameraAnimation.maxDuration(camera, DEBRECEN, alreadyFollowing = true)

        near shouldBeGreaterThan MIN_DURATION
        far shouldBeGreaterThan near
    }

    @Test
    fun `Given same distance - When zoomed in further - Then duration is longer`() {
        val zoomedOut = MyLocationCameraAnimation.maxDuration(camera(DOBOGOKO, 11.0), PILISSZENTKERESZT, true)
        val zoomedIn = MyLocationCameraAnimation.maxDuration(camera(DOBOGOKO, 16.0), PILISSZENTKERESZT, true)

        zoomedIn shouldBeGreaterThan zoomedOut
    }

    @Test
    fun `Given across the country at the Hungary overview - When duration - Then it is capped at the max`() {
        val actual = MyLocationCameraAnimation.maxDuration(camera(DOBOGOKO, 5.4), DEBRECEN, alreadyFollowing = false)

        actual shouldBe MAX_DURATION
    }

    companion object {
        private val DOBOGOKO = Location(47.7181, 18.8948)
        private val PILISSZENTKERESZT = Location(47.6911, 18.8995)
        private val DEBRECEN = Location(47.5316, 21.6273)

        private fun camera(location: Location, zoom: Double) = CameraPosition(location, zoom, bearing = 0.0, pitch = 0.0)

        fun testCases() =
            listOf(
                TestCase(camera(DOBOGOKO, 16.0), alreadyFollowing = false, result = MIN_DURATION),
                TestCase(camera(DOBOGOKO, 18.0), alreadyFollowing = false, result = MIN_DURATION),
                TestCase(camera(DOBOGOKO, 5.4), alreadyFollowing = true, result = MIN_DURATION),
                TestCase(camera(DOBOGOKO, 15.0), alreadyFollowing = false, result = 700.milliseconds),
                TestCase(camera(DOBOGOKO, 12.0), alreadyFollowing = false, result = 1300.milliseconds),
                TestCase(camera(DOBOGOKO, 5.4), alreadyFollowing = false, result = MAX_DURATION),
            )
    }

    data class TestCase(
        val camera: CameraPosition,
        val alreadyFollowing: Boolean,
        val result: Duration,
    )
}
