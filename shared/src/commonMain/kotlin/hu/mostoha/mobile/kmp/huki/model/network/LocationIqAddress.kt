package hu.mostoha.mobile.kmp.huki.model.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocationIqAddress(
    @SerialName("name")
    val name: String? = null,
    @SerialName("house_number")
    val houseNumber: String? = null,
    @SerialName("road")
    val road: String? = null,
    @SerialName("city")
    val city: String? = null,
    @SerialName("county")
    val county: String? = null,
    @SerialName("state")
    val state: String? = null,
    @SerialName("postcode")
    val postcode: String? = null,
    @SerialName("country")
    val country: String? = null,
    @SerialName("country_code")
    val countryCode: String? = null,
)
