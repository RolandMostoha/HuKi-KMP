package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.StringResource
import hu.mostoha.mobile.huki.shared.SharedRes

sealed class DomainException(
    val stringResource: StringResource,
    cause: Throwable? = null,
) : Exception(cause)

class EmptyGpxContentException(cause: Throwable? = null) :
    DomainException(
        stringResource = SharedRes.strings.gpx_import_error_empty_content_message,
        cause = cause,
    )

class MalformedGpxException(cause: Throwable? = null) :
    DomainException(
        stringResource = SharedRes.strings.gpx_import_error_malformed_message,
        cause = cause,
    )

class NonGpxFileException(cause: Throwable? = null) :
    DomainException(
        stringResource = SharedRes.strings.gpx_import_error_non_gpx_message,
        cause = cause,
    )

class UnreadableGpxFileException(cause: Throwable? = null) :
    DomainException(
        stringResource = SharedRes.strings.gpx_import_error_unreadable_message,
        cause = cause,
    )
