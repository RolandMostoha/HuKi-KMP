package hu.mostoha.mobile.kmp.huki.model.domain

data class GpxFileSection(
    val header: GpxFileHeader,
    val items: List<GpxCollectionItem>,
)
