package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.InfoViewData
import hu.mostoha.mobile.kmp.huki.model.domain.InfoViewType
import hu.mostoha.mobile.kmp.huki.model.network.NetworkError

private val networkErrorsToMessageMap = mapOf(
    NetworkError.REQUEST_TIMEOUT to InfoViewData(
        infoViewType = InfoViewType.ERROR,
        title = SharedRes.strings.network_error_request_timeout_title,
        message = SharedRes.strings.network_error_request_timeout_message,
        icon = SharedRes.images.ic_error,
    ),
    NetworkError.BAD_REQUEST to InfoViewData(
        infoViewType = InfoViewType.ERROR,
        title = SharedRes.strings.network_error_bad_request_title,
        message = SharedRes.strings.network_error_bad_request_message,
        icon = SharedRes.images.ic_error,
    ),
    NetworkError.NOT_FOUND to InfoViewData(
        infoViewType = InfoViewType.INFO,
        title = SharedRes.strings.network_error_not_found_title,
        message = SharedRes.strings.network_error_not_found_message,
        icon = SharedRes.images.ic_not_found,
    ),
    NetworkError.NO_INTERNET to InfoViewData(
        infoViewType = InfoViewType.ERROR,
        title = SharedRes.strings.network_error_no_internet_title,
        message = SharedRes.strings.network_error_no_internet_message,
        icon = SharedRes.images.ic_no_internet,
    ),
    NetworkError.INTERNAL_SERVER_ERROR to InfoViewData(
        infoViewType = InfoViewType.ERROR,
        title = SharedRes.strings.network_error_internal_server_title,
        message = SharedRes.strings.network_error_internal_server_message,
        icon = SharedRes.images.ic_error,
    ),
    NetworkError.SERIALIZATION to InfoViewData(
        infoViewType = InfoViewType.ERROR,
        title = SharedRes.strings.network_error_serialization_title,
        message = SharedRes.strings.network_error_serialization_message,
        icon = SharedRes.images.ic_error,
    ),
    NetworkError.RATE_LIMITED to InfoViewData(
        infoViewType = InfoViewType.ERROR,
        title = SharedRes.strings.network_error_rate_limited_title,
        message = SharedRes.strings.network_error_rate_limited_message,
        icon = SharedRes.images.ic_error,
    ),
    NetworkError.UNKNOWN to InfoViewData(
        infoViewType = InfoViewType.ERROR,
        title = SharedRes.strings.network_error_unknown_title,
        message = SharedRes.strings.network_error_unknown_message,
        icon = SharedRes.images.ic_error,
    ),
)

fun NetworkError.toInfoViewData(): InfoViewData = networkErrorsToMessageMap.getValue(this)
