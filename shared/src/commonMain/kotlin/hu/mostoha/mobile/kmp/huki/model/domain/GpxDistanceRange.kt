package hu.mostoha.mobile.kmp.huki.model.domain

/**
 * Distance buckets of the GPX Collection, longest first, as the list shows them.
 */
enum class GpxDistanceRange(val minKilometers: Double) {
    OVER_20(minKilometers = 20.0),
    FROM_10_TO_20(minKilometers = 10.0),
    FROM_5_TO_10(minKilometers = 5.0),
    UNDER_5(minKilometers = 0.0),
    ;

    companion object {
        fun of(kilometers: Double): GpxDistanceRange = entries.first { kilometers >= it.minKilometers }
    }
}
