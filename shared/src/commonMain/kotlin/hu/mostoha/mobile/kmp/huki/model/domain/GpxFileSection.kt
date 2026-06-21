package hu.mostoha.mobile.kmp.huki.model.domain

data class GpxFileSection(
    val header: GpxFileHeader,
    val files: List<GpxFileItem>,
)
