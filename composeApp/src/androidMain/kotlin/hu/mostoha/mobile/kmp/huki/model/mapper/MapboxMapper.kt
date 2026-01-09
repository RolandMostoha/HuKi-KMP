package hu.mostoha.mobile.kmp.huki.model.mapper

import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import hu.mostoha.mobile.kmp.huki.model.domain.CameraPosition
import hu.mostoha.mobile.kmp.huki.model.domain.Location

fun CameraPosition.toCameraOptions(): CameraOptions =
    CameraOptions.Builder()
        .center(location.toPoint())
        .zoom(zoom)
        .build()

fun Location.toPoint(): Point = Point.fromLngLat(longitude, latitude)
