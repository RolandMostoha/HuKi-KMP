package hu.mostoha.mobile.kmp.huki.model.domain

sealed interface SupportRestoreResult {
    data object Restored : SupportRestoreResult

    data object NothingToRestore : SupportRestoreResult

    data object Cancelled : SupportRestoreResult

    data class Failed(val message: String) : SupportRestoreResult
}
