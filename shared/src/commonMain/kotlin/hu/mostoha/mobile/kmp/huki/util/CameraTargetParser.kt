package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.CameraPosition
import hu.mostoha.mobile.kmp.huki.model.domain.Location

object CameraTargetParser {
    /**
     * Parses a "lat,lon,zoom" debug string (e.g. "47.78403,18.93396,11.63") into a [CameraPosition].
     * Returns null when the string is blank or malformed, so callers can fall back to a default camera.
     */
    fun parse(value: String): CameraPosition? {
        val parts = value.split(",").map { it.trim() }
        if (parts.size != 3) return null
        val latitude = parts[0].toDoubleOrNull() ?: return null
        val longitude = parts[1].toDoubleOrNull() ?: return null
        val zoom = parts[2].toDoubleOrNull() ?: return null
        return CameraPosition(
            location = Location(latitude = latitude, longitude = longitude),
            zoom = zoom,
            bearing = 0.0,
            pitch = 0.0,
        )
    }
}
