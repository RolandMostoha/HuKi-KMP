package hu.mostoha.mobile.kmp.huki.network

import co.touchlab.kermit.Logger
import hu.mostoha.mobile.kmp.huki.model.network.NetworkError
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
import hu.mostoha.mobile.kmp.huki.service.CrashlyticsService
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException

@Suppress("MagicNumber")
suspend inline fun <reified T> handleNetworkCall(
    crashlyticsService: CrashlyticsService,
    call: suspend () -> HttpResponse,
): NetworkResult<T> =
    try {
        val response = call.invoke()

        when (response.status.value) {
            in 200..299 -> {
                val data = response.body<T>()
                NetworkResult.Success(data)
            }
            400 -> NetworkResult.Error(NetworkError.BAD_REQUEST)
            404 -> NetworkResult.Error(NetworkError.NOT_FOUND)
            408 -> NetworkResult.Error(NetworkError.REQUEST_TIMEOUT)
            429 -> NetworkResult.Error(NetworkError.RATE_LIMITED)
            in 500..599 -> NetworkResult.Error(NetworkError.INTERNAL_SERVER_ERROR)
            else -> {
                val statusCode = response.status.value
                Logger.e { "Network: Unexpected HTTP status $statusCode." }
                crashlyticsService.recordException(IllegalStateException("Network: Unexpected HTTP status $statusCode"))
                NetworkResult.Error(NetworkError.UNKNOWN)
            }
        }
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: SerializationException) {
        Logger.e(exception) { "Network: Failed serialization." }
        crashlyticsService.recordException(exception)
        NetworkResult.Error(NetworkError.SERIALIZATION)
    } catch (exception: JsonConvertException) {
        Logger.e(exception) { "Network: Failed serialization." }
        crashlyticsService.recordException(exception)
        NetworkResult.Error(NetworkError.SERIALIZATION)
    } catch (exception: Exception) {
        when {
            exception.isNoInternetException() -> {
                Logger.e(exception) { "Network: No internet. ${exception.toDeepLog()}" }
                NetworkResult.Error(NetworkError.NO_INTERNET)
            }
            exception.isTimeoutException() -> {
                Logger.e(exception) { "Network: Request timed out. ${exception.toDeepLog()}" }
                NetworkResult.Error(NetworkError.REQUEST_TIMEOUT)
            }
            else -> {
                Logger.e(exception) { "Network: Uncaught exception. ${exception.toDeepLog()}" }
                crashlyticsService.recordException(exception)
                NetworkResult.Error(NetworkError.UNKNOWN)
            }
        }
    }

/**
 * Builds a compact throwable summary for logs because some KMP/platform exceptions
 * do not expose a useful message or stack trace in Logcat by default.
 */
fun Throwable.toDeepLog(maxDepth: Int = 5): String =
    causeChain()
        .take(maxDepth)
        .joinToString(separator = " -> ") { throwable ->
            buildString {
                append(throwable::class.simpleName ?: "UnknownThrowable")

                throwable.message
                    ?.takeIf { message -> message.isNotBlank() }
                    ?.let { message ->
                        append(": ")
                        append(message)
                    }
            }
        }
