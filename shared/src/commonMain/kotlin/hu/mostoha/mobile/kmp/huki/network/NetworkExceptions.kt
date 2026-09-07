package hu.mostoha.mobile.kmp.huki.network

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException

/**
 * Network failures are sometimes wrapped by Ktor or platform exceptions, so we
 * inspect the full cause chain instead of only the top-level exception.
 */
@PublishedApi
internal fun Throwable.isNoInternetException(): Boolean =
    causeChain().any { throwable -> throwable.isNoInternetCause() }

@PublishedApi
internal fun Throwable.isTimeoutException(): Boolean =
    causeChain().any { throwable ->
        throwable is SocketTimeoutException ||
            throwable is ConnectTimeoutException ||
            throwable is HttpRequestTimeoutException
    }

internal expect fun Throwable.isNoInternetCause(): Boolean

internal fun Throwable.causeChain(): Sequence<Throwable> = generateSequence(this) { throwable -> throwable.cause }
