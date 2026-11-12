package hu.mostoha.mobile.kmp.huki.repository

import co.touchlab.kermit.Logger
import hu.mostoha.mobile.kmp.huki.model.domain.IncomingLink
import hu.mostoha.mobile.kmp.huki.model.domain.LocationLink
import hu.mostoha.mobile.kmp.huki.model.mapper.toPlaceSearchResult
import hu.mostoha.mobile.kmp.huki.model.network.NetworkError
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
import hu.mostoha.mobile.kmp.huki.network.isNoInternetException
import hu.mostoha.mobile.kmp.huki.network.isTimeoutException
import hu.mostoha.mobile.kmp.huki.service.CrashlyticsService
import hu.mostoha.mobile.kmp.huki.util.LocationLinkParser
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.CancellationException
import kotlin.time.Duration.Companion.seconds

class DefaultLocationLinkResolver(
    httpClient: HttpClient,
    private val geocodingRepository: GeocodingRepository,
    private val crashlyticsService: CrashlyticsService,
) : LocationLinkResolver {
    private companion object {
        private const val MAX_REDIRECTS = 3
        private val REDIRECT_STATUS_CODES = 300..399

        /**
         * Shorter than the client-wide timeout, the user waits on the map without feedback meanwhile.
         */
        private val REDIRECT_TIMEOUT = 10.seconds
    }

    /**
     * The redirect target is the point of a short link, so it must not be followed.
     */
    private val redirectClient = httpClient.config { followRedirects = false }

    override suspend fun resolve(link: IncomingLink.Text): LocationLinkResult =
        when (val locationLink = LocationLinkParser.parse(link.text)) {
            null -> LocationLinkResult.Unsupported
            else -> locationLink.toResult(redirectsLeft = MAX_REDIRECTS)
        }

    private suspend fun LocationLink.toResult(redirectsLeft: Int): LocationLinkResult =
        when (this) {
            is LocationLink.Coordinates -> LocationLinkResult.Coordinates(location)
            is LocationLink.PlaceName -> searchPlace(name)
            is LocationLink.ShortUrl -> if (redirectsLeft > 0) {
                followShortUrl(url, redirectsLeft)
            } else {
                LocationLinkResult.Unsupported
            }
        }

    private suspend fun followShortUrl(url: String, redirectsLeft: Int): LocationLinkResult =
        when (val redirect = fetchRedirectTarget(url)) {
            is NetworkResult.Error -> LocationLinkResult.Failed(redirect.error)
            is NetworkResult.Success ->
                redirect.data
                    ?.let(LocationLinkParser::parse)
                    ?.toResult(redirectsLeft - 1)
                    ?: LocationLinkResult.Unsupported
        }

    private suspend fun searchPlace(name: String): LocationLinkResult =
        when (val result = geocodingRepository.autocomplete(name)) {
            is NetworkResult.Success -> result.data.firstOrNull()
                ?.let { LocationLinkResult.PlaceFound(it.toPlaceSearchResult()) }
                ?: LocationLinkResult.Unsupported
            is NetworkResult.Error -> if (result.error == NetworkError.NOT_FOUND) {
                LocationLinkResult.Unsupported
            } else {
                LocationLinkResult.Failed(result.error)
            }
        }

    private suspend fun fetchRedirectTarget(url: String): NetworkResult<String?> =
        try {
            val response = redirectClient.get(url) {
                timeout { requestTimeoutMillis = REDIRECT_TIMEOUT.inWholeMilliseconds }
            }
            val target = response.headers[HttpHeaders.Location]
                ?.takeIf { response.status.value in REDIRECT_STATUS_CODES }
            NetworkResult.Success(target)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            // Exception messages can hold the shared link, which must not reach the logs or Crashlytics
            val failure = "LocationLink: Failed to resolve short link, ${exception::class.simpleName}."
            Logger.e { failure }
            when {
                exception.isNoInternetException() -> NetworkResult.Error(NetworkError.NO_INTERNET)
                exception.isTimeoutException() -> NetworkResult.Error(NetworkError.REQUEST_TIMEOUT)
                else -> {
                    crashlyticsService.recordException(IllegalStateException(failure))
                    NetworkResult.Error(NetworkError.UNKNOWN)
                }
            }
        }
}
