package hu.mostoha.mobile.kmp.huki.util

import co.touchlab.kermit.Logger
import hu.mostoha.mobile.kmp.huki.model.domain.OverlayLayer

object OverlayTileCachePolicy {
    private const val CACHE_CONTROL = "cache-control"
    private const val EXPIRES = "expires"
    private const val HTTP_OK = 200
    private const val HTTP_NOT_MODIFIED = 304

    private val urlPrefixes: List<Pair<String, OverlayLayer>> = OverlayLayer.entries.flatMap { layer ->
        layer.tiles.map { it.substringBefore("{") to layer }
    }

    /**
     * Returns [headers] extended with a `Cache-Control: max-age` for overlay tile responses, or null if unchanged.
     * Headers the server already sends win. Never throws: it runs on every Mapbox response, and a Kotlin exception
     * crashes iOS.
     */
    fun headersWithCacheControl(url: String, code: Int, headers: Map<String, String>): Map<String, String>? =
        runCatching { cacheHeaders(url, code, headers) }
            .onFailure { Logger.e(it) { "Map: Failed to apply overlay tile cache headers." } }
            .getOrNull()

    private fun cacheHeaders(url: String, code: Int, headers: Map<String, String>): Map<String, String>? {
        if (code != HTTP_OK && code != HTTP_NOT_MODIFIED) return null
        val layer = urlPrefixes.firstOrNull { (prefix, _) -> url.startsWith(prefix) }?.second ?: return null
        if (headers.keys.any { it.equals(CACHE_CONTROL, ignoreCase = true) || it.equals(EXPIRES, ignoreCase = true) }) {
            return null
        }
        return headers + (CACHE_CONTROL to "max-age=${layer.cacheMaxAge.inWholeSeconds}")
    }
}
