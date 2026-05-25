package hu.mostoha.mobile.kmp.huki.model.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocationIqPlace(
    @SerialName("place_id")
    val placeId: String,
    @SerialName("osm_id")
    val osmId: String,
    @SerialName("osm_type")
    val osmType: String,
    @SerialName("licence")
    val licence: String,
    @SerialName("lat")
    val lat: Double,
    @SerialName("lon")
    val lon: Double,
    @SerialName("display_name")
    val displayName: String,
    @SerialName("display_place")
    val displayPlace: String? = null,
    @SerialName("display_address")
    val displayAddress: String? = null,
    @SerialName("boundingbox")
    val boundingBox: List<Double>? = null,
    @SerialName("class")
    val classType: String? = null,
    @SerialName("type")
    val type: String? = null,
    @SerialName("address")
    val address: LocationIqAddress? = null,
)
