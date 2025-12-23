package hu.mostoha.mobile.kmp.huki

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle
import hu.mostoha.mobile.kmp.huki.util.MapConstants
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
            }
            MapboxMap(
                modifier = Modifier.fillMaxSize(),
                style = { MapStyle(Style.OUTDOORS) },
                mapViewportState = rememberMapViewportState {
                    setCameraOptions {
                        zoom(MapConstants.HUNGARY_ZOOM_LEVEL)
                        center(
                            Point.fromLngLat(
                                MapConstants.BUDAPEST_LONGITUDE,
                                MapConstants.BUDAPEST_LATITUDE,
                                BoundingBox.fromLngLats(
                                    MapConstants.HUNGARY_BOX_WEST,
                                    MapConstants.HUNGARY_BOX_SOUTH,
                                    MapConstants.HUNGARY_BOX_EAST,
                                    MapConstants.HUNGARY_BOX_NORTH,
                                )
                            )
                        )
                        pitch(0.0)
                        bearing(0.0)
                    }
                },
            )
        }
    }
}
