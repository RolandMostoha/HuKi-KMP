package hu.mostoha.mobile.kmp.huki.network

import hu.mostoha.mobile.kmp.huki.model.network.NetworkError
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
import hu.mostoha.mobile.kmp.huki.service.FakeCrashlyticsService
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlin.test.Test

class NetworkUtilsTest {

    private val crashlyticsService = FakeCrashlyticsService()

    @Test
    fun `Given successful response - When handleNetworkCall invoked - Then success result has decoded body`() {
        runTest {
            val client = createHttpClient(
                engine = MockEngine {
                    respond(
                        content = """{"name":"Budapest","count":3}""",
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders,
                    )
                },
            )

            val actual = handleNetworkCall<TestDto>(crashlyticsService) {
                client.get("https://example.com")
            }

            actual shouldBe NetworkResult.Success(TestDto(name = "Budapest", count = 3))
        }
    }

    @Test
    fun `Given bad request response - When handleNetworkCall invoked - Then error result is BAD_REQUEST`() {
        runTest {
            val actual = handleNetworkCall<TestDto>(crashlyticsService) {
                createHttpClient(MockEngine { respond("", HttpStatusCode.BadRequest) })
                    .get("https://example.com")
            }

            actual shouldBe NetworkResult.Error(NetworkError.BAD_REQUEST)
        }
    }

    @Test
    fun `Given not found response - When handleNetworkCall invoked - Then error result is NOT_FOUND`() {
        runTest {
            val actual = handleNetworkCall<TestDto>(crashlyticsService) {
                createHttpClient(MockEngine { respond("", HttpStatusCode.NotFound) })
                    .get("https://example.com")
            }

            actual shouldBe NetworkResult.Error(NetworkError.NOT_FOUND)
        }
    }

    @Test
    fun `Given request timeout response - When handleNetworkCall invoked - Then error result is REQUEST_TIMEOUT`() {
        runTest {
            val actual = handleNetworkCall<TestDto>(crashlyticsService) {
                createHttpClient(MockEngine { respond("", HttpStatusCode.RequestTimeout) })
                    .get("https://example.com")
            }

            actual shouldBe NetworkResult.Error(NetworkError.REQUEST_TIMEOUT)
        }
    }

    @Test
    fun `Given rate limited response - When handleNetworkCall invoked - Then error result is RATE_LIMITED`() {
        runTest {
            val actual = handleNetworkCall<TestDto>(crashlyticsService) {
                createHttpClient(MockEngine { respond("", HttpStatusCode.TooManyRequests) })
                    .get("https://example.com")
            }

            actual shouldBe NetworkResult.Error(NetworkError.RATE_LIMITED)
        }
    }

    @Test
    fun `Given server error response - When handleNetworkCall invoked - Then error result is INTERNAL_SERVER_ERROR`() {
        runTest {
            val actual = handleNetworkCall<TestDto>(crashlyticsService) {
                createHttpClient(MockEngine { respond("", HttpStatusCode.InternalServerError) })
                    .get("https://example.com")
            }

            actual shouldBe NetworkResult.Error(NetworkError.INTERNAL_SERVER_ERROR)
        }
    }

    @Test
    fun `Given unexpected response - When handleNetworkCall invoked - Then error is UNKNOWN and exception recorded`() {
        runTest {
            val actual = handleNetworkCall<TestDto>(crashlyticsService) {
                createHttpClient(MockEngine { respond("", HttpStatusCode.Found) })
                    .get("https://example.com")
            }

            actual shouldBe NetworkResult.Error(NetworkError.UNKNOWN)
            crashlyticsService.recordedExceptions shouldHaveSize 1
        }
    }

    @Test
    fun `Given unauthorized response - When handleNetworkCall invoked - Then error is UNKNOWN and exception recorded`() {
        runTest {
            val actual = handleNetworkCall<TestDto>(crashlyticsService) {
                createHttpClient(MockEngine { respond("", HttpStatusCode.Unauthorized) })
                    .get("https://example.com")
            }

            actual shouldBe NetworkResult.Error(NetworkError.UNKNOWN)
            crashlyticsService.recordedExceptions shouldHaveSize 1
        }
    }

    @Test
    fun `Given unresolved address exception - When handleNetworkCall invoked - Then error result is NO_INTERNET`() {
        runTest {
            val client = createHttpClient(
                engine = MockEngine {
                    throw UnresolvedAddressException()
                },
            )

            val actual = handleNetworkCall<TestDto>(crashlyticsService) {
                client.get("https://example.com")
            }

            actual shouldBe NetworkResult.Error(NetworkError.NO_INTERNET)
            crashlyticsService.recordedExceptions.shouldBeEmpty()
        }
    }

    @Test
    fun `Given unknown host exception - When handleNetworkCall invoked - Then error result is NO_INTERNET`() {
        runTest {
            val client = createHttpClient(
                engine = MockEngine {
                    throw UnknownHostException(message = "Unable to resolve host")
                },
            )

            val actual = handleNetworkCall<TestDto>(crashlyticsService) {
                client.get("https://example.com")
            }

            actual shouldBe NetworkResult.Error(NetworkError.NO_INTERNET)
            crashlyticsService.recordedExceptions.shouldBeEmpty()
        }
    }

    @Test
    fun `Given malformed json response - When handleNetworkCall invoked - Then error result is SERIALIZATION`() {
        runTest {
            val client = createHttpClient(
                engine = MockEngine {
                    respond(
                        content = """{"name":"Budapest","count":"invalid"}""",
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders,
                    )
                },
            )

            val actual = handleNetworkCall<TestDto>(crashlyticsService) {
                client.get("https://example.com")
            }

            actual shouldBe NetworkResult.Error(NetworkError.SERIALIZATION)
            crashlyticsService.recordedExceptions shouldHaveSize 1
        }
    }

    @Test
    fun `Given unexpected exception - When handleNetworkCall invoked - Then error is UNKNOWN and exception recorded`() {
        runTest {
            val client = createHttpClient(
                engine = MockEngine {
                    throw IllegalStateException("Boom")
                },
            )

            val actual = handleNetworkCall<TestDto>(crashlyticsService) {
                client.get("https://example.com")
            }

            actual shouldBe NetworkResult.Error(NetworkError.UNKNOWN)
            crashlyticsService.recordedExceptions shouldHaveSize 1
        }
    }

    @Serializable
    private data class TestDto(
        val name: String,
        val count: Int,
    )

    private class UnknownHostException(message: String) : Exception(message)

    private companion object {
        val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")
    }
}
