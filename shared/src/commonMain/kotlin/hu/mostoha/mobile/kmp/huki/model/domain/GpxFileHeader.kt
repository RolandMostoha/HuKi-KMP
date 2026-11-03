package hu.mostoha.mobile.kmp.huki.model.domain

sealed interface GpxFileHeader {
    /**
     * Stable list key, unique within one grouping.
     */
    val key: String

    data class Day(val day: GpxFileDay) : GpxFileHeader {
        override val key: String = "day_${day.key}"
    }

    data class Distance(val range: GpxDistanceRange) : GpxFileHeader {
        override val key: String = "distance_${range.name}"
    }

    data class Landscape(val landscape: GpxLandscape) : GpxFileHeader {
        override val key: String = "landscape_${landscape.id}"
    }

    data object OutsideLandscapes : GpxFileHeader {
        override val key: String = "landscape_outside"
    }
}
