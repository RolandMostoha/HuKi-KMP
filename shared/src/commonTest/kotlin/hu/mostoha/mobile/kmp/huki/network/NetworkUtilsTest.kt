package hu.mostoha.mobile.kmp.huki.network

import hu.mostoha.mobile.kmp.huki.model.network.NetworkError
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
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

    @Test
    fun `Given successful response, When handleNetworkCall invoked, Then success result has decoded body`() {
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

            val actual = handleNetworkCall<TestDto> {
                client.get("https://example.com")
            }

            actual shouldBe NetworkResult.Success(TestDto(name = "Budapest", count = 3))
        }
    }

    @Test
    fun `Given bad request response, When handleNetworkCall invoked, Then error result is BAD_REQUEST`() {
        runTest {
            val actual = handleNetworkCall<TestDto> {
                createHttpClient(MockEngine { respond("", HttpStatusCode.BadRequest) })
                    .get("https://example.com")
            }

            actual shouldBe NetworkResult.Error(NetworkError.BAD_REQUEST)
        }
    }

    @Test
    fun `Given not found response, When handleNetworkCall invoked, Then error result is NOT_FOUND`() {
        runTest {
            val actual = handleNetworkCall<TestDto> {
                createHttpClient(MockEngine { respond("", HttpStatusCode.NotFound) })
                    .get("https://example.com")
            }

            actual shouldBe NetworkResult.Error(NetworkError.NOT_FOUND)
        }
    }

    @Test
    fun `Given request timeout response, When handleNetworkCall invoked, Then error result is REQUEST_TIMEOUT`() {
        runTest {
            val actual = handleNetworkCall<TestDto> {
                createHttpClient(MockEngine { respond("", HttpStatusCode.RequestTimeout) })
                    .get("https://example.com")
            }

            actual shouldBe NetworkResult.Error(NetworkError.REQUEST_TIMEOUT)
        }
    }

    @Test
    fun `Given rate limited response, When handleNetworkCall invoked, Then error result is RATE_LIMITED`() {
        runTest {
            val actual = handleNetworkCall<TestDto> {
                createHttpClient(MockEngine { respond("", HttpStatusCode.Locked) })
                    .get("https://example.com")
            }

            actual shouldBe NetworkResult.Error(NetworkError.RATE_LIMITED)
        }
    }

    @Test
    fun `Given server error response, When handleNetworkCall invoked, Then error result is INTERNAL_SERVER_ERROR`() {
        runTest {
            val actual = handleNetworkCall<TestDto> {
                createHttpClient(MockEngine { respond("", HttpStatusCode.InternalServerError) })
                    .get("https://example.com")
            }

            actual shouldBe NetworkResult.Error(NetworkError.INTERNAL_SERVER_ERROR)
        }
    }

    @Test
    fun `Given unexpected response, When handleNetworkCall invoked, Then error result is UNKNOWN`() {
        runTest {
            val actual = handleNetworkCall<TestDto> {
                createHttpClient(MockEngine { respond("", HttpStatusCode.Found) })
                    .get("https://example.com")
            }

            actual shouldBe NetworkResult.Error(NetworkError.UNKNOWN)
        }
    }

    @Test
    fun `Given unresolved address exception, When handleNetworkCall invoked, Then error result is NO_INTERNET`() {
        runTest {
            val client = createHttpClient(
                engine = MockEngine {
                    throw UnresolvedAddressException()
                },
            )

            val actual = handleNetworkCall<TestDto> {
                client.get("https://example.com")
            }

            actual shouldBe NetworkResult.Error(NetworkError.NO_INTERNET)
        }
    }

    @Test
    fun `Given unknown host exception, When handleNetworkCall invoked, Then error result is NO_INTERNET`() {
        runTest {
            val client = createHttpClient(
                engine = MockEngine {
                    throw UnknownHostException(message = "Unable to resolve host")
                },
            )

            val actual = handleNetworkCall<TestDto> {
                client.get("https://example.com")
            }

            actual shouldBe NetworkResult.Error(NetworkError.NO_INTERNET)
        }
    }

    @Test
    fun `Given malformed json response, When handleNetworkCall invoked, Then error result is SERIALIZATION`() {
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

            val actual = handleNetworkCall<TestDto> {
                client.get("https://example.com")
            }

            actual shouldBe NetworkResult.Error(NetworkError.SERIALIZATION)
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
