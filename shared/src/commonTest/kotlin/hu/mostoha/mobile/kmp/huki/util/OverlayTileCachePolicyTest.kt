package hu.mostoha.mobile.kmp.huki.util

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OverlayTileCachePolicyTest {

    @Test
    fun `Given tile response - When cache headers are applied - Then max-age is added only to overlay tiles`() {
        testCases().forEach { testCase ->
            val actual = OverlayTileCachePolicy.headersWithCacheControl(
                url = testCase.url,
                code = testCase.code,
                headers = testCase.headers,
            )

            actual shouldBe testCase.result
        }
    }

    companion object {
        private const val TILE_URL = "https://b.tile.openstreetmap.hu/tt/15/18011/11536.png"
        private const val MAX_AGE_14_DAYS = "max-age=1209600"
        private val SERVER_HEADERS = mapOf("etag" to "\"52b-65e6d6ccbb680\"")

        fun testCases() =
            listOf(
                TestCase(
                    url = TILE_URL,
                    code = 200,
                    headers = SERVER_HEADERS,
                    result = SERVER_HEADERS + ("cache-control" to MAX_AGE_14_DAYS),
                ),
                TestCase(
                    url = TILE_URL,
                    code = 304,
                    headers = SERVER_HEADERS,
                    result = SERVER_HEADERS + ("cache-control" to MAX_AGE_14_DAYS),
                ),
                TestCase(
                    url = TILE_URL,
                    code = 404,
                    headers = SERVER_HEADERS,
                    result = null,
                ),
                TestCase(
                    url = TILE_URL,
                    code = 200,
                    headers = mapOf("Cache-Control" to "max-age=60"),
                    result = null,
                ),
                TestCase(
                    url = TILE_URL,
                    code = 200,
                    headers = mapOf("Expires" to "Wed, 18 Nov 2026 05:00:00 GMT"),
                    result = null,
                ),
                TestCase(
                    url = "https://a.tile.openstreetmap.hu/ttszin/15/18011/11536.png",
                    code = 200,
                    headers = SERVER_HEADERS,
                    result = null,
                ),
                TestCase(
                    url = "https://api.mapbox.com/v4/mapbox.mapbox-streets-v8/15/18011/11536.vector.pbf",
                    code = 200,
                    headers = SERVER_HEADERS,
                    result = null,
                ),
            )
    }

    data class TestCase(
        val url: String,
        val code: Int,
        val headers: Map<String, String>,
        val result: Map<String, String>?,
    )
}
