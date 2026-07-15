package hu.mostoha.mobile.kmp.huki.model.domain

data class UserPreferences(val mapZoomControlsVisible: Boolean) {
    companion object {
        val DEFAULTS = UserPreferences(
            mapZoomControlsVisible = false,
        )
    }
}
