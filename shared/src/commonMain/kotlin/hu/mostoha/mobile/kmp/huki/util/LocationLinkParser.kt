package hu.mostoha.mobile.kmp.huki.util

import co.touchlab.kermit.Logger
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.LocationLink
import io.ktor.http.URLDecodeException
import io.ktor.http.decodeURLQueryComponent

/**
 * Reads a location from `geo:` URIs, including ones embedded in shared text.
 */
object LocationLinkParser {
    private const val MAX_LATITUDE = 90.0
    private const val MAX_LONGITUDE = 180.0
    private const val GEO_SCHEME = "geo:"

    private val LINK = Regex("""(?i)geo:\S+""")
    private val COORDINATES = Regex("""^\s*(-?\d+(?:\.\d+)?)\s*,\s*\+?\s*(-?\d+(?:\.\d+)?)""")

    fun parse(text: String): LocationLink? {
        val trimmedText = text.trim()
        val links = sequence {
            // A geo: URI handed over as the whole intent data may carry unencoded spaces in its query
            if (trimmedText.startsWith(GEO_SCHEME, ignoreCase = true)) yield(trimmedText)
            yieldAll(LINK.findAll(text).map { it.value.trimLinkEnd() })
        }
        return links.firstNotNullOfOrNull(::parseLink)
    }

    private fun parseLink(link: String): LocationLink? =
        try {
            parseSupportedLink(link)
        } catch (exception: URLDecodeException) {
            Logger.w(exception) { "LocationLink: Malformed percent-encoding." }
            null
        }

    private fun parseSupportedLink(link: String): LocationLink? =
        if (link.startsWith(GEO_SCHEME, ignoreCase = true)) parseGeoUri(link.drop(GEO_SCHEME.length)) else null

    /**
     * RFC 5870 `geo:lat,lon[,alt][;params][?q=...]`, where a `q` holding coordinates or a name wins over `0,0`.
     */
    private fun parseGeoUri(value: String): LocationLink? {
        val coordinatePart = value.substringBefore('?').substringBefore(';')
        val query = value.substringAfter('?', missingDelimiterValue = "")
        val q = query.split('&')
            .firstOrNull { it.startsWith("q=") }
            ?.removePrefix("q=")
            ?.decodeURLQueryComponent(plusIsSpace = true)
            ?.trim()
        return q?.let(::parseCoordinates)?.let(LocationLink::Coordinates)
            ?: parseCoordinates(coordinatePart)?.let(LocationLink::Coordinates)
            ?: q?.substringBefore('(')?.trim()?.takeIf { it.isNotEmpty() }?.let(LocationLink::PlaceName)
    }

    private fun parseCoordinates(value: String): Location? = COORDINATES.find(value)?.toLocation()

    private fun MatchResult.toLocation(): Location? {
        val latitude = groupValues[1].toDoubleOrNull() ?: return null
        val longitude = groupValues[2].toDoubleOrNull() ?: return null
        return toLocation(latitude, longitude)
    }

    private fun String.trimLinkEnd(): String {
        val trimmed = trimEnd('.', ',', '>', '"', '\'')
        val hasUnbalancedParenthesis =
            trimmed.endsWith(')') && trimmed.count { it == ')' } > trimmed.count { it == '(' }
        return if (hasUnbalancedParenthesis) trimmed.dropLast(1) else trimmed
    }

    private fun toLocation(latitude: Double, longitude: Double): Location? {
        val isInRange = latitude in -MAX_LATITUDE..MAX_LATITUDE && longitude in -MAX_LONGITUDE..MAX_LONGITUDE
        val isNullIsland = latitude == 0.0 && longitude == 0.0
        return if (isInRange && !isNullIsland) Location(latitude, longitude) else null
    }
}
