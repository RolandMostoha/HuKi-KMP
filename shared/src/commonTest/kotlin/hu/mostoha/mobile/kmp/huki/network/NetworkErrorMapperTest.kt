package hu.mostoha.mobile.kmp.huki.network

import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.InfoViewData
import hu.mostoha.mobile.kmp.huki.model.domain.InfoViewType
import hu.mostoha.mobile.kmp.huki.model.network.NetworkError
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class NetworkErrorMapperTest {

    @Test
    fun `Given network error, When mapped, Then it returns shared error info view data`() {
        testCases().forEach { testCase ->
            val actual = testCase.input.toInfoViewData()

            actual shouldBe testCase.result
        }
    }

    companion object {
        fun testCases() =
            listOf(
                TestCase(
                    input = NetworkError.REQUEST_TIMEOUT,
                    result = InfoViewData(
                        infoViewType = InfoViewType.ERROR,
                        icon = SharedRes.images.ic_error,
                        title = SharedRes.strings.network_error_request_timeout_title,
                        message = SharedRes.strings.network_error_request_timeout_message,
                    ),
                ),
                TestCase(
                    input = NetworkError.BAD_REQUEST,
                    result = InfoViewData(
                        infoViewType = InfoViewType.ERROR,
                        icon = SharedRes.images.ic_error,
                        title = SharedRes.strings.network_error_bad_request_title,
                        message = SharedRes.strings.network_error_bad_request_message,
                    ),
                ),
                TestCase(
                    input = NetworkError.NOT_FOUND,
                    result = InfoViewData(
                        infoViewType = InfoViewType.INFO,
                        icon = SharedRes.images.ic_not_found,
                        title = SharedRes.strings.network_error_not_found_title,
                        message = SharedRes.strings.network_error_not_found_message,
                    ),
                ),
                TestCase(
                    input = NetworkError.NO_INTERNET,
                    result = InfoViewData(
                        infoViewType = InfoViewType.ERROR,
                        icon = SharedRes.images.ic_no_internet,
                        title = SharedRes.strings.network_error_no_internet_title,
                        message = SharedRes.strings.network_error_no_internet_message,
                    ),
                ),
                TestCase(
                    input = NetworkError.INTERNAL_SERVER_ERROR,
                    result = InfoViewData(
                        infoViewType = InfoViewType.ERROR,
                        icon = SharedRes.images.ic_error,
                        title = SharedRes.strings.network_error_internal_server_title,
                        message = SharedRes.strings.network_error_internal_server_message,
                    ),
                ),
                TestCase(
                    input = NetworkError.SERIALIZATION,
                    result = InfoViewData(
                        infoViewType = InfoViewType.ERROR,
                        icon = SharedRes.images.ic_error,
                        title = SharedRes.strings.network_error_serialization_title,
                        message = SharedRes.strings.network_error_serialization_message,
                    ),
                ),
                TestCase(
                    input = NetworkError.RATE_LIMITED,
                    result = InfoViewData(
                        infoViewType = InfoViewType.ERROR,
                        icon = SharedRes.images.ic_error,
                        title = SharedRes.strings.network_error_rate_limited_title,
                        message = SharedRes.strings.network_error_rate_limited_message,
                    ),
                ),
                TestCase(
                    input = NetworkError.UNKNOWN,
                    result = InfoViewData(
                        infoViewType = InfoViewType.ERROR,
                        icon = SharedRes.images.ic_error,
                        title = SharedRes.strings.network_error_unknown_title,
                        message = SharedRes.strings.network_error_unknown_message,
                    ),
                ),
            )
    }

    data class TestCase(
        val input: NetworkError,
        val result: InfoViewData,
    )
}
