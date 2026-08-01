package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.CameraPosition
import hu.mostoha.mobile.kmp.huki.model.domain.Location

object CameraTargetParser {
    /**
     * Parses a "lat,lon,zoom" string (e.g. "47.78403,18.93396,11.63") into a [CameraPosition].
     * Returns null when the string is blank or malformed, so callers can fall back to a default camera.
     */
    fun parse(value: String): CameraPosition? {
        val parts = value.split(",")
        if (parts.size != 3) {
            return null
        }
        val values = parts.mapNotNull { it.trim().toDoubleOrNull() }
        if (values.size != 3) {
            return null
        }
        return CameraPosition(
            location = Location(latitude = values[0], longitude = values[1]),
            zoom = values[2],
            bearing = 0.0,
            pitch = 0.0,
        )
    }
}
