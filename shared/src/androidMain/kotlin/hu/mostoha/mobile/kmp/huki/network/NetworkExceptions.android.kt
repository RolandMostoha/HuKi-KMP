package hu.mostoha.mobile.kmp.huki.network

import io.ktor.util.network.UnresolvedAddressException
import java.net.NoRouteToHostException
import java.net.UnknownHostException

internal actual fun Throwable.isNoInternetCause(): Boolean =
    this is UnresolvedAddressException ||
        this is UnknownHostException ||
        this is NoRouteToHostException
