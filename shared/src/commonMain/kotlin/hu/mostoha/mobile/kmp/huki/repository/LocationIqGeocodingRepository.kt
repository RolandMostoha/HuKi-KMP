package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.Secrets
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.network.LocationIqPlace
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
import hu.mostoha.mobile.kmp.huki.network.handleNetworkCall
import hu.mostoha.mobile.kmp.huki.service.CrashlyticsService
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class LocationIqGeocodingRepository(
    private val httpClient: HttpClient,
    private val crashlyticsService: CrashlyticsService,
) : GeocodingRepository {
    companion object {
        private const val BASE_URL = "https://eu1.locationiq.com/v1/"
        private const val URL_AUTOCOMPLETE = BASE_URL + "autocomplete"
        private const val URL_REVERSE_GEOCODE = BASE_URL + "reverse"
        private const val AUTOCOMPLETE_ITEM_LIMIT = 20
    }

    override suspend fun autocomplete(searchText: String): NetworkResult<List<LocationIqPlace>> =
        handleNetworkCall(crashlyticsService) {
            httpClient.get(urlString = URL_AUTOCOMPLETE) {
                parameter("q", searchText)
                parameter("countrycodes", "hu")
                parameter("accept-language", "hu")
                parameter("normalizecity", 1)
                parameter("dedupe", 1)
                parameter("limit", AUTOCOMPLETE_ITEM_LIMIT)
                parameter("key", Secrets.LOCATION_IQ_API_KEY)
            }
        }

    override suspend fun reverseGeocode(location: Location): NetworkResult<LocationIqPlace?> =
        handleNetworkCall(crashlyticsService) {
            httpClient.get(urlString = URL_REVERSE_GEOCODE) {
                parameter("lat", location.latitude)
                parameter("lon", location.longitude)
                parameter("format", "json")
                parameter("accept-language", "hu")
                parameter("normalizecity", 1)
                // Splits `display_name` into `display_place` + `display_address`, matching the autocomplete shape.
                parameter("normalizeaddress", 1)
                parameter("extratags", 1)
                parameter("key", Secrets.LOCATION_IQ_API_KEY)
            }
        }
}
