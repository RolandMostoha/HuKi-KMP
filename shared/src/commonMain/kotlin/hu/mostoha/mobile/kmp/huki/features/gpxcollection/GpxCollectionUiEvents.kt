package hu.mostoha.mobile.kmp.huki.features.gpxcollection

import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileItem

sealed interface GpxCollectionUiEvents {
    data object BackClicked : GpxCollectionUiEvents
    data object HelpClicked : GpxCollectionUiEvents
    data class FileClicked(val file: GpxFileItem) : GpxCollectionUiEvents
    data class DeleteClicked(val file: GpxFileItem) : GpxCollectionUiEvents
    data object DeleteConfirmed : GpxCollectionUiEvents
    data object DeleteDismissed : GpxCollectionUiEvents
}
