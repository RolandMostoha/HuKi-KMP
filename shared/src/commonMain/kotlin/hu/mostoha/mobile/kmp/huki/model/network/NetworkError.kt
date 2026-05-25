package hu.mostoha.mobile.kmp.huki.model.network

enum class NetworkError {
    REQUEST_TIMEOUT,
    BAD_REQUEST,
    NOT_FOUND,
    NO_INTERNET,
    INTERNAL_SERVER_ERROR,
    SERIALIZATION,
    RATE_LIMITED,
    UNKNOWN,
}
