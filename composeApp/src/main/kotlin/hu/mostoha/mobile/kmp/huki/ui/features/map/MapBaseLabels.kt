package hu.mostoha.mobile.kmp.huki.ui.features.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mapbox.bindgen.Value
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMapComposable
import hu.mostoha.mobile.kmp.huki.features.map.LandscapesGrouping
import hu.mostoha.mobile.kmp.huki.features.map.MapUiState
import kotlinx.coroutines.awaitCancellation

private const val STANDARD_IMPORT_ID = "basemap"
private const val CLASSIC_STYLE_SOURCE = "composite"
private const val SYMBOL_LAYER_TYPE = "symbol"
private val StandardLabelConfigs = listOf(
    "showPlaceLabels",
    "showRoadLabels",
    "showPointOfInterestLabels",
    "showTransitLabels",
)

/**
 * The landscape overview is a picker, so the base map's own labels would only compete with landscape names.
 */
val MapUiState.isBaseLabelsHidden: Boolean
    get() {
        val landscapes = landscapes ?: return false
        return landscapes.grouping == LandscapesGrouping.LANDSCAPE && landscapes.selectedLandscapeId == null
    }

@Composable
@MapboxMapComposable
fun BaseLabelsVisibilityEffect(isHidden: Boolean) {
    val hiddenBaseLabels = remember { HiddenBaseLabels() }
    MapEffect(isHidden) { mapView ->
        val mapboxMap = mapView.mapboxMap
        if (mapboxMap.isStyleLoaded()) {
            hiddenBaseLabels.sync(mapboxMap, isHidden)
        }
        val styleLoaded = mapboxMap.subscribeStyleLoaded {
            // Switching the base layer or appearance reloads the style with its own label visibility
            hiddenBaseLabels.clear()
            hiddenBaseLabels.sync(mapboxMap, isHidden)
        }
        try {
            awaitCancellation()
        } finally {
            styleLoaded.cancel()
        }
    }
}

/**
 * Restores only the labels it hid, as the style keeps some of its own labels hidden.
 */
private class HiddenBaseLabels {
    private var layerIds: List<String>? = null

    fun clear() {
        layerIds = null
    }

    fun sync(map: MapboxMap, isHidden: Boolean) {
        val hiddenLayerIds = layerIds
        when {
            isHidden && hiddenLayerIds == null -> layerIds = map.hideBaseLabels()
            !isHidden && hiddenLayerIds != null -> {
                map.showBaseLabels(hiddenLayerIds)
                layerIds = null
            }
        }
    }
}

private fun MapboxMap.hideBaseLabels(): List<String> {
    setStandardLabelsVisible(false)
    return styleLayers
        .filter { it.type == SYMBOL_LAYER_TYPE }
        .filter { getStyleLayerProperty(it.id, "source").value.contents == CLASSIC_STYLE_SOURCE }
        .filter { getStyleLayerProperty(it.id, "visibility").value.contents != "none" }
        .map { it.id }
        .onEach { setStyleLayerProperty(it, "visibility", Value("none")) }
}

private fun MapboxMap.showBaseLabels(layerIds: List<String>) {
    setStandardLabelsVisible(true)
    layerIds.forEach { setStyleLayerProperty(it, "visibility", Value("visible")) }
}

private fun MapboxMap.setStandardLabelsVisible(isVisible: Boolean) {
    StandardLabelConfigs.forEach { config ->
        setStyleImportConfigProperty(STANDARD_IMPORT_ID, config, Value(isVisible))
    }
}
