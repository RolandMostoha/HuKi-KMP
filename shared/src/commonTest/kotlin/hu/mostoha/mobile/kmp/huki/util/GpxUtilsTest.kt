package hu.mostoha.mobile.kmp.huki.util

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.test.Test

class GpxUtilsTest {

    @Test
    fun `Given identical content with different names, When toGpxTrackId, Then ids are equal`() {
        val content = "<gpx>track</gpx>".encodeToByteArray()
        val sameContent = "<gpx>track</gpx>".encodeToByteArray()

        content.toGpxTrackId() shouldBe sameContent.toGpxTrackId()
    }

    @Test
    fun `Given different content, When toGpxTrackId, Then ids differ`() {
        val first = "<gpx>track-a</gpx>".encodeToByteArray()
        val second = "<gpx>track-b</gpx>".encodeToByteArray()

        first.toGpxTrackId() shouldNotBe second.toGpxTrackId()
    }

    @Test
    fun `Given content, When toGpxTrackId, Then id is a 16-char hex string`() {
        val id = "content".encodeToByteArray().toGpxTrackId()

        id.length shouldBe 16
        id.all { it in "0123456789abcdef" } shouldBe true
    }
}
