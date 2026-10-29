package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocation

@OptIn(ExperimentalForeignApi::class)
fun CLLocation.toLocation(): Location =
    Location(
        latitude = coordinate.useContents { latitude },
        longitude = coordinate.useContents { longitude },
        altitude = accurateAltitudeOrNull(altitude, verticalAccuracy),
    )
