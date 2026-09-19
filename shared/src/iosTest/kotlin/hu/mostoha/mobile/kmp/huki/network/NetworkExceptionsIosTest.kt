package hu.mostoha.mobile.kmp.huki.network

import io.kotest.matchers.shouldBe
import io.ktor.client.engine.darwin.DarwinHttpRequestException
import platform.Foundation.NSError
import platform.Foundation.NSURLErrorCannotConnectToHost
import platform.Foundation.NSURLErrorCannotFindHost
import platform.Foundation.NSURLErrorDataNotAllowed
import platform.Foundation.NSURLErrorDomain
import platform.Foundation.NSURLErrorNetworkConnectionLost
import platform.Foundation.NSURLErrorNotConnectedToInternet
import platform.Foundation.NSURLErrorServerCertificateUntrusted
import kotlin.test.Test

class NetworkExceptionsIosTest {

    @Test
    fun `Given Darwin connectivity error - When isNoInternetException invoked - Then it is true`() {
        noInternetUrlErrorCodes().forEach { code ->
            darwinException(code).isNoInternetException() shouldBe true
        }
    }

    @Test
    fun `Given wrapped Darwin connectivity error - When isNoInternetException invoked - Then it is true`() {
        noInternetUrlErrorCodes().forEach { code ->
            IllegalStateException("Wrapped", darwinException(code)).isNoInternetException() shouldBe true
        }
    }

    @Test
    fun `Given Darwin error not caused by the device being offline - When isNoInternetException invoked - Then it is false`() {
        otherUrlErrorCodes().forEach { code ->
            darwinException(code).isNoInternetException() shouldBe false
        }
    }

    @Test
    fun `Given error outside the NSURLError domain - When isNoInternetException invoked - Then it is false`() {
        val error = NSError.errorWithDomain(
            domain = "hu.mostoha.mobile.ios.huki",
            code = NSURLErrorNotConnectedToInternet,
            userInfo = null,
        )

        DarwinHttpRequestException(error).isNoInternetException() shouldBe false
    }

    private fun darwinException(code: Long) =
        DarwinHttpRequestException(
            NSError.errorWithDomain(domain = NSURLErrorDomain, code = code, userInfo = null),
        )

    private companion object {
        fun noInternetUrlErrorCodes() =
            listOf(
                NSURLErrorNotConnectedToInternet,
                NSURLErrorCannotFindHost,
                NSURLErrorDataNotAllowed,
            )

        fun otherUrlErrorCodes() =
            listOf(
                NSURLErrorServerCertificateUntrusted,
                NSURLErrorCannotConnectToHost,
                NSURLErrorNetworkConnectionLost,
            )
    }
}
