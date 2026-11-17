package hu.mostoha.mobile.kmp.huki.theme

import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class DirectionArrowSdfTest {

    @Test
    fun `Given density - When create - Then the image covers the arrow and its padding`() {
        val density = 2f

        val actual = DirectionArrowSdf.create(density)

        actual.widthPx shouldBe 32
        actual.heightPx shouldBe 32
        actual.alpha.size shouldBe 32 * 32
    }

    @Test
    fun `Given the arrow - When create - Then alpha is above the edge threshold inside and below the halo outside`() {
        val sdf = DirectionArrowSdf.create(density = 1f)

        val inside = sdf.alpha[7 * sdf.widthPx + 9]
        val corner = sdf.alpha[0]

        inside shouldBeGreaterThan 192
        corner shouldBeLessThan 144
    }

    @Test
    fun `Given a pixel just past the arrow tip - When create - Then alpha drops by its distance to the tip`() {
        val sdf = DirectionArrowSdf.create(density = 1f)

        // Pixel center (13.5, 8.5) is sqrt(0.5) dp from the tip at (13, 8)
        val actual = sdf.alpha[8 * sdf.widthPx + 13]

        actual shouldBe 169
    }
}
