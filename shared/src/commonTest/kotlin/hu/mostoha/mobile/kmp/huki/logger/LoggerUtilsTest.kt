package hu.mostoha.mobile.kmp.huki.logger

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class LoggerUtilsTest {

    @Test
    fun `Given a list within limit, When trimming, Then it remains unchanged`() {
        val input = "Data(items=[1, 2, 3])"

        val actual = input.trimLongLists(limit = 5)

        actual shouldBe "Data(items=[1, 2, 3])"
    }

    @Test
    fun `Given a list exactly at limit, When trimming, Then it remains unchanged`() {
        val input = "Data(items=[1, 2, 3])"

        val actual = input.trimLongLists(limit = 3)

        actual shouldBe "Data(items=[1, 2, 3])"
    }

    @Test
    fun `Given a list above limit, When trimming, Then it is truncated`() {
        val input = "Data(items=[1, 2, 3, 4])"

        val actual = input.trimLongLists(limit = 2)

        actual shouldBe "Data(items=[1, 2, [...trimmed...] (total=4)])"
    }

    @Test
    fun `Given multiple lists, When trimming, Then all are processed`() {
        val input = "Data(a=[1, 2, 3], b=[4, 5, 6, 7])"

        val actual = input.trimLongLists(limit = 2)

        actual shouldBe "Data(a=[1, 2, [...trimmed...] (total=3)], b=[4, 5, [...trimmed...] (total=4)])"
    }

    @Test
    fun `Given no lists, When trimming, Then it remains unchanged`() {
        val input = "Data(a=1, b=2)"

        val actual = input.trimLongLists(limit = 5)

        actual shouldBe "Data(a=1, b=2)"
    }

    @Test
    fun `Given empty list, When trimming, Then it remains unchanged`() {
        val input = "Data(items=[])"

        val actual = input.trimLongLists(limit = 5)

        actual shouldBe "Data(items=[])"
    }
}
