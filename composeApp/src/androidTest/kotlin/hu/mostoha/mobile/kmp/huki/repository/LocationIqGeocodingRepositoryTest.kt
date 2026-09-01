package hu.mostoha.mobile.kmp.huki.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import hu.mostoha.mobile.kmp.huki.model.network.LocationIqPlace
import hu.mostoha.mobile.kmp.huki.model.network.NetworkError
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
import hu.mostoha.mobile.kmp.huki.network.createHttpClient
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.doubles.shouldBeBetween
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContainIgnoringCase
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Real network integration test: hits the LocationIQ service.
 *
 * Acts as a safeguard for service availability and contract stability.
 * Requires:
 *  - Internet access on the emulator/device.
 *  - A valid `LOCATION_IQ_API_KEY` in `secrets.properties`.
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class LocationIqGeocodingRepositoryTest {

    private val httpClient = createHttpClient(engine = OkHttp.create())
    private val repository = LocationIqGeocodingRepository(
        httpClient = httpClient,
        crashlyticsService = FakeCrashlyticsService,
    )

    @After
    fun tearDown() {
        httpClient.close()
        // Avoid touching the service too frequently
        Thread.sleep(1_000)
    }

    @Test
    fun givenKnownHungarianPlace_whenAutocomplete_thenSuccessWithResults() {
        runTest {
            val result = repository.autocomplete("Budapest")

            result.shouldBeInstanceOf<NetworkResult.Success<List<LocationIqPlace>>>()
            result.data.shouldNotBeEmpty()

            val first = result.data.first()
            first.lat.shouldBeBetween(47.0, 48.0, 0.0)
            first.lon.shouldBeBetween(18.5, 19.5, 0.0)
            first.displayName.shouldContainIgnoringCase("Budapest")
        }
    }

    @Test
    fun givenPartialPlaceName_whenAutocomplete_thenReturnsSuggestions() {
        runTest {
            val result = repository.autocomplete("Debr")

            result.shouldBeInstanceOf<NetworkResult.Success<List<LocationIqPlace>>>()
            result.data.shouldNotBeEmpty()
        }
    }

    @Test
    fun givenObscureQueryWithNoMatches_whenAutocomplete_thenNotFoundError() {
        runTest {
            val result = repository.autocomplete("zzzzzzqqqqqxxxxxxxnonexistent123456")

            result shouldBe NetworkResult.Error(NetworkError.NOT_FOUND)
        }
    }
}
