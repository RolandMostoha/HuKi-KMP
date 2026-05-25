package hu.mostoha.mobile.kmp.huki.model.network

sealed interface NetworkResult<out D> {
    data class Success<out D>(val data: D) : NetworkResult<D>
    data class Error(val error: NetworkError) : NetworkResult<Nothing>
}
