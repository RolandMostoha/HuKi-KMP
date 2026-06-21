package hu.mostoha.mobile.kmp.huki.model.domain

sealed interface GpxFileHeader {
    data object Today : GpxFileHeader
    data object Yesterday : GpxFileHeader
    data class Date(val label: String) : GpxFileHeader
}
