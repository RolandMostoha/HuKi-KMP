package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import com.mapbox.common.location.Location as MapboxLocation

fun MapboxLocation.toLocation(): Location =
    Location(
        latitude = latitude,
        longitude = longitude,
        altitude = accurateAltitudeOrNull(altitude, verticalAccuracy),
    )
