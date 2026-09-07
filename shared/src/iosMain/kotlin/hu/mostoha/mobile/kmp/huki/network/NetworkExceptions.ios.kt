package hu.mostoha.mobile.kmp.huki.network

import io.ktor.client.engine.darwin.DarwinHttpRequestException
import io.ktor.util.network.UnresolvedAddressException
import platform.Foundation.NSURLErrorCallIsActive
import platform.Foundation.NSURLErrorCannotFindHost
import platform.Foundation.NSURLErrorDataNotAllowed
import platform.Foundation.NSURLErrorDomain
import platform.Foundation.NSURLErrorInternationalRoamingOff
import platform.Foundation.NSURLErrorNotConnectedToInternet

private val noInternetUrlErrorCodes = setOf(
    NSURLErrorNotConnectedToInternet,
    NSURLErrorCannotFindHost,
    NSURLErrorInternationalRoamingOff,
    NSURLErrorCallIsActive,
    NSURLErrorDataNotAllowed,
)

internal actual fun Throwable.isNoInternetCause(): Boolean =
    when (this) {
        is UnresolvedAddressException -> true
        is DarwinHttpRequestException -> origin.domain == NSURLErrorDomain && origin.code in noInternetUrlErrorCodes
        else -> false
    }
