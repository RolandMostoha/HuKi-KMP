package hu.mostoha.mobile.kmp.huki.model.domain

sealed interface IncomingLink {
    /**
     * Raw text handed over by another app: a `geo:` URI, a link, or a shared message that contains a link.
     */
    data class Text(val text: String) : IncomingLink

    /**
     * A file opened with HuKi from another app, validated as GPX on import.
     */
    data class GpxFile(val uri: String) : IncomingLink
}

sealed interface LocationLink {
    data class Coordinates(val location: Location) : LocationLink

    data class PlaceName(val name: String) : LocationLink

    /**
     * Shortened link (e.g. `maps.app.goo.gl`), only its redirect target tells the location.
     */
    data class ShortUrl(val url: String) : LocationLink
}
