package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.UserPreferences
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<UserPreferences>

    suspend fun setMapZoomControlsVisible(visible: Boolean)
}
