package hu.mostoha.mobile.kmp.huki.util.formatter

import org.maplibre.spatialk.gpx.Document

object GpxFormatter {

    private const val MAX_NAME_LENGTH_WITH_DESCRIPTION = 30

    fun formatTitle(gpx: Document): String? {
        gpx.metadata?.name.trimmedOrNull()?.let { return it }

        gpx.tracks.firstOrNull()?.let { track ->
            formatNameAndDescription(track.name, track.description)?.let { return it }
        }

        gpx.routes.firstOrNull()?.let { route ->
            formatNameAndDescription(route.name, route.description)?.let { return it }
        }

        return null
    }

    private fun formatNameAndDescription(name: String?, description: String?): String? {
        val trimmedName = name.trimmedOrNull()
        val trimmedDescription = description.trimmedOrNull()

        return when {
            trimmedName == null -> trimmedDescription
            trimmedDescription == null -> trimmedName
            trimmedName.length > MAX_NAME_LENGTH_WITH_DESCRIPTION -> trimmedName
            else -> "$trimmedName - $trimmedDescription"
        }
    }

    private fun String?.trimmedOrNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
}
