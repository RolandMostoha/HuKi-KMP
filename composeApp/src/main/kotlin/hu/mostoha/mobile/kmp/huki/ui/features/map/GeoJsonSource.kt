package hu.mostoha.mobile.kmp.huki.ui.features.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mapbox.maps.extension.compose.style.sources.generated.GeoJsonSourceState

/**
 * Remembers a [GeoJsonSourceState] without saving it: Mapbox's `rememberGeoJsonSourceState` parcels the whole
 * GeoJSON into the saved instance state, which crashes with `TransactionTooLargeException` for long lines.
 */
@Composable
fun rememberGeoJsonSource(init: GeoJsonSourceState.() -> Unit = {}): GeoJsonSourceState =
    remember { GeoJsonSourceState().apply(init) }
