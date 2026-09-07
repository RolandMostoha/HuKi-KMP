package hu.mostoha.mobile.kmp.huki.network

import io.kotest.matchers.shouldBe
import io.ktor.client.network.sockets.ConnectTimeoutException
import java.net.NoRouteToHostException
import java.net.UnknownHostException
import kotlin.test.Test

class NetworkExceptionsAndroidTest {

    @Test
    fun `Given OkHttp connectivity exception - When isNoInternetException invoked - Then it is true`() {
        noInternetExceptions().forEach { exception ->
            exception.isNoInternetException() shouldBe true
        }
    }

    @Test
    fun `Given wrapped OkHttp connectivity exception - When isNoInternetException invoked - Then it is true`() {
        noInternetExceptions().forEach { exception ->
            IllegalStateException("Wrapped", exception).isNoInternetException() shouldBe true
        }
    }

    @Test
    fun `Given unrelated exception - When isNoInternetException invoked - Then it is false`() {
        IllegalStateException("Boom").isNoInternetException() shouldBe false
    }

    @Test
    fun `Given connect timeout exception - When isNoInternetException invoked - Then it is false`() {
        ConnectTimeoutException("Connect timeout").isNoInternetException() shouldBe false
    }

    private companion object {
        fun noInternetExceptions() =
            listOf(
                UnknownHostException("Unable to resolve host"),
                NoRouteToHostException("No route to host"),
            )
    }
}
