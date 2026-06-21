package hu.mostoha.mobile.kmp.huki.features.gpxcollection

import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileItem
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileSection

data class GpxCollectionUiState(
    val isLoading: Boolean = true,
    val fileCount: Int = 0,
    val sections: List<GpxFileSection> = emptyList(),
    val pendingDelete: GpxFileItem? = null,
) {
    companion object {
        val Default = GpxCollectionUiState()
    }
}
