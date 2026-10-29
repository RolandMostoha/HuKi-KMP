package hu.mostoha.mobile.kmp.huki.model.mapper

/**
 * Location providers report an unknown altitude with a non-positive or missing vertical accuracy.
 */
fun accurateAltitudeOrNull(altitude: Double?, verticalAccuracy: Double?): Double? =
    altitude?.takeIf { verticalAccuracy != null && verticalAccuracy > 0 }
