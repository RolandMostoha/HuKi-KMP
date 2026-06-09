package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.StringResource

data class Destination(
    val osmId: String,
    val relId: String? = null,
    val name: String,
    val town: String,
    val type: DestinationType,
    val location: Location,
    val description: StringResource,
    val popularity: Int,
) {
    init {
        require(popularity in 1..10) { "Popularity must be between 1 and 10" }
    }
}
