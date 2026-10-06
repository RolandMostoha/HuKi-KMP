package hu.mostoha.mobile.kmp.huki.model.domain

/**
 * A trail line pre-encoded as GeoJSON, identified by [id] so the map only re-uploads it when the id changes.
 */
data class OktLine(
    val id: String,
    val geoJson: String,
    val bounds: List<Location>,
) {
    override fun toString(): String = "OktLine(id=$id, geoJson=${geoJson.length} chars)"
}
