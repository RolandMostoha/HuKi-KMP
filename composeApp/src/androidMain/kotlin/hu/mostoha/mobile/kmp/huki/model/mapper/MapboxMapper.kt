package hu.mostoha.mobile.kmp.huki.model.mapper

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CameraState
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.Style
import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer
import hu.mostoha.mobile.kmp.huki.model.domain.CameraPosition
import hu.mostoha.mobile.kmp.huki.model.domain.ContentPadding
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.util.dpToPx

fun CameraPosition.toCameraOptions(): CameraOptions =
    CameraOptions.Builder()
        .center(location.toPoint())
        .zoom(zoom)
        .bearing(bearing)
        .pitch(pitch)
        .build()

fun Location.toPoint(): Point = Point.fromLngLat(longitude, latitude)

fun List<Location>.toPoints(): List<Point> = map { it.toPoint() }

fun List<Location>.toLineString(): LineString = LineString.fromLngLats(this.toPoints())

fun CameraState.toCameraPosition() =
    CameraPosition(
        location = Location(center.longitude(), center.latitude()),
        zoom = zoom,
        bearing = bearing,
        pitch = pitch,
    )

fun BaseLayer.toMapStyle(): String =
    when (this) {
        BaseLayer.OUTDOORS -> Style.OUTDOORS
        BaseLayer.STREET -> Style.MAPBOX_STREETS
        BaseLayer.SATELLITE -> Style.SATELLITE
    }

fun ContentPadding.toEdgeInset(density: Density): EdgeInsets =
    EdgeInsets(
        this.top.dp.dpToPx(density).toDouble(),
        this.left.dp.dpToPx(density).toDouble(),
        this.bottom.dp.dpToPx(density).toDouble(),
        this.right.dp.dpToPx(density).toDouble(),
    )
