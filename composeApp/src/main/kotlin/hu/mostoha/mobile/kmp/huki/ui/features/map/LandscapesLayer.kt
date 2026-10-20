package hu.mostoha.mobile.kmp.huki.ui.features.map

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mapbox.maps.AnnotatedFeature
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.ViewAnnotationAnchorConfig
import com.mapbox.maps.ViewAnnotationOptions
import com.mapbox.maps.extension.compose.MapboxMapComposable
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.extension.compose.style.ColorValue
import com.mapbox.maps.extension.compose.style.DoubleValue
import com.mapbox.maps.extension.compose.style.StringListValue
import com.mapbox.maps.extension.compose.style.StringValue
import com.mapbox.maps.extension.compose.style.layers.Filter
import com.mapbox.maps.extension.compose.style.layers.FormattedValue
import com.mapbox.maps.extension.compose.style.layers.LayerInteractionsState
import com.mapbox.maps.extension.compose.style.layers.generated.FillLayer
import com.mapbox.maps.extension.compose.style.layers.generated.LineCapValue
import com.mapbox.maps.extension.compose.style.layers.generated.LineJoinValue
import com.mapbox.maps.extension.compose.style.layers.generated.LineLayer
import com.mapbox.maps.extension.compose.style.layers.generated.SymbolLayer
import com.mapbox.maps.extension.compose.style.sources.GeoJSONData
import com.mapbox.maps.extension.compose.style.sources.generated.GeoJsonSourceState
import com.mapbox.maps.extension.compose.style.sources.generated.rememberGeoJsonSourceState
import com.mapbox.maps.extension.style.expressions.generated.Expression
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.Strings
import hu.mostoha.mobile.kmp.huki.features.main.LandscapesUiEvents
import hu.mostoha.mobile.kmp.huki.features.map.LandscapesGrouping
import hu.mostoha.mobile.kmp.huki.features.map.LandscapesUiState
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationType
import hu.mostoha.mobile.kmp.huki.model.domain.Landscape
import hu.mostoha.mobile.kmp.huki.model.mapper.toPoint
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.theme.MapLighting
import hu.mostoha.mobile.kmp.huki.theme.OutdoorsColorTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoImage
import hu.mostoha.mobile.kmp.huki.util.testTagAsResourceId
import hu.mostoha.mobile.kmp.huki.util.toComposeColor

private const val MASK_LAYER_ID = "landscapes_hungary_mask_fill_layer"
private const val FILL_LAYER_ID = "landscapes_fill_layer"
private const val OUTLINE_HALO_LAYER_ID = "landscapes_outline_halo_layer"
private const val OUTLINE_LAYER_ID = "landscapes_outline_layer"
private const val LABEL_LAYER_ID = "landscapes_label_layer"
private const val SELECTED_TINT_LAYER_ID = "landscapes_selected_tint_layer"
private const val SELECTED_GLOW_LAYER_ID = "landscapes_selected_glow_layer"
private const val SELECTED_HALO_LAYER_ID = "landscapes_selected_halo_layer"
private const val SELECTED_OUTLINE_LAYER_ID = "landscapes_selected_outline_layer"
private const val PROPERTY_ID = "id"
private const val MASK_OPACITY = 0.55
private const val FILL_OPACITY = 0.6
private const val OUTLINE_WIDTH = 1.5
private const val OUTLINE_HALO_WIDTH = 4.0
private const val LABEL_SIZE = 14.0
private const val LABEL_HALO_WIDTH = 1.5

/**
 * Darker "ink" shade of the landscape colour, so light colours stay readable as text.
 */
private const val LABEL_INK_DARKENING = 0.4f
private const val SELECTED_OUTLINE_WIDTH = 2.25
private const val SELECTED_HALO_WIDTH = 5.0
private const val SELECTED_TINT_OPACITY = 0.07
private const val SELECTED_GLOW_WIDTH = 10.0
private const val SELECTED_GLOW_BLUR = 7.0
private const val SELECTED_GLOW_OPACITY = 0.45
private const val SELECTED_MARKER_PRIORITY = 11L
private const val INFO_WINDOW_PRIORITY = 12L
private val LabelFonts = listOf("DIN Pro Bold", "Arial Unicode MS Bold")

@Composable
@MapboxMapComposable
fun LandscapesLayer(landscapes: LandscapesUiState, onEvent: (LandscapesUiEvents) -> Unit) {
    HungaryMaskLayer(geoJson = landscapes.hungaryMaskGeoJson)
    if (landscapes.grouping == LandscapesGrouping.LANDSCAPE) {
        LandscapeOutlineLayers(landscapes = landscapes, onEvent = onEvent)
    }
    landscapes.destinations.forEach { destination ->
        key(destination.id) {
            DestinationMarkerAnnotation(
                destination = destination,
                isSelected = destination.id == landscapes.selectedDestinationId,
                onClick = { onEvent(LandscapesUiEvents.LandscapesDestinationClicked(destination)) },
            )
        }
    }
    landscapes.selectedDestination?.let { destination ->
        key(destination.id) {
            val offsetY = with(LocalDensity.current) {
                (Dimens.DestinationSelectedMarkerSize / 2 + Dimens.InfoWindowMarkerPadding).toPx().toDouble()
            }
            ViewAnnotation(
                options = ViewAnnotationOptions.Builder()
                    .annotatedFeature(AnnotatedFeature(destination.location.toPoint()))
                    .variableAnchors(
                        listOf(
                            ViewAnnotationAnchorConfig.Builder()
                                .anchor(ViewAnnotationAnchor.BOTTOM)
                                .offsetY(offsetY)
                                .build(),
                        ),
                    )
                    .allowOverlap(true)
                    .allowOverlapWithPuck(true)
                    .ignoreCameraPadding(true)
                    .priority(INFO_WINDOW_PRIORITY)
                    .build(),
            ) {
                DestinationInfoWindow(
                    destination = destination,
                    onPlaceDetailsClick = { onEvent(LandscapesUiEvents.LandscapesInfoWindowPlaceDetailsClicked) },
                )
            }
        }
    }
}

@OptIn(MapboxExperimental::class)
@Composable
@MapboxMapComposable
private fun HungaryMaskLayer(geoJson: String) {
    val source = rememberLandscapesSource(key = "landscapes_hungary_mask_source", geoJson = geoJson)
    FillLayer(sourceState = source, layerId = MASK_LAYER_ID) {
        fillColor = ColorValue(Color.White)
        fillOpacity = DoubleValue(MASK_OPACITY)
        fillColorUseTheme = StringValue(OutdoorsColorTheme.COLOR_USE_THEME_NONE)
        fillEmissiveStrength = DoubleValue(MapLighting.OVERLAY_EMISSIVE_STRENGTH)
    }
}

@OptIn(MapboxExperimental::class)
@Composable
@MapboxMapComposable
private fun LandscapeOutlineLayers(landscapes: LandscapesUiState, onEvent: (LandscapesUiEvents) -> Unit) {
    val context = LocalContext.current
    val currentOnEvent by rememberUpdatedState(onEvent)
    val outlinesSource = rememberLandscapesSource(key = "landscapes_source", geoJson = landscapes.outlinesGeoJson)
    val labelsSource = rememberLandscapesSource(key = "landscapes_label_source", geoJson = landscapes.labelsGeoJson)
    val fallbackColor = mokoColor(SharedRes.colors.primary)
    val colorExpression = remember(landscapes.landscapes) {
        landscapes.landscapes.colorExpression(fallbackColor) { it.colorRes.toComposeColor(context) }
    }
    val labelColorExpression = remember(landscapes.landscapes) {
        landscapes.landscapes.colorExpression(fallbackColor) {
            it.colorRes.toComposeColor(context).darkened(LABEL_INK_DARKENING)
        }
    }
    val nameExpression = remember(landscapes.landscapes) {
        val strings = Strings(context)
        Expression.match(
            Expression.get(PROPERTY_ID),
            *landscapes.landscapes
                .map { Expression.literal(it.id) to Expression.literal(strings.get(it.nameRes)) }
                .toTypedArray(),
            fallback = Expression.literal(""),
        )
    }
    val interactionsState = remember {
        LayerInteractionsState().onClicked { feature, _ ->
            val landscapeId = feature.properties.optString(PROPERTY_ID)
            if (landscapeId.isNotEmpty()) currentOnEvent(LandscapesUiEvents.LandscapeClicked(landscapeId))
            landscapeId.isNotEmpty()
        }
    }
    val unselectedFilter = Filter(
        Expression.neq(Expression.get(PROPERTY_ID), Expression.literal(landscapes.selectedLandscapeId.orEmpty())),
    )
    FillLayer(sourceState = outlinesSource, layerId = FILL_LAYER_ID) {
        filter = unselectedFilter
        fillColor = ColorValue(colorExpression)
        fillOpacity = DoubleValue(FILL_OPACITY)
        fillColorUseTheme = StringValue(OutdoorsColorTheme.COLOR_USE_THEME_NONE)
        fillEmissiveStrength = DoubleValue(MapLighting.OVERLAY_EMISSIVE_STRENGTH)
        this.interactionsState = interactionsState
    }
    LineLayer(sourceState = outlinesSource, layerId = OUTLINE_HALO_LAYER_ID) {
        filter = unselectedFilter
        lineWidth = DoubleValue(OUTLINE_HALO_WIDTH)
        lineColor = ColorValue(Color.White)
        lineJoin = LineJoinValue.ROUND
        lineColorUseTheme = StringValue(OutdoorsColorTheme.COLOR_USE_THEME_NONE)
        lineEmissiveStrength = DoubleValue(MapLighting.OVERLAY_EMISSIVE_STRENGTH)
    }
    LineLayer(sourceState = outlinesSource, layerId = OUTLINE_LAYER_ID) {
        filter = unselectedFilter
        lineWidth = DoubleValue(OUTLINE_WIDTH)
        lineColor = ColorValue(colorExpression)
        lineJoin = LineJoinValue.ROUND
        lineColorUseTheme = StringValue(OutdoorsColorTheme.COLOR_USE_THEME_NONE)
        lineEmissiveStrength = DoubleValue(MapLighting.OVERLAY_EMISSIVE_STRENGTH)
    }
    SymbolLayer(sourceState = labelsSource, layerId = LABEL_LAYER_ID) {
        filter = unselectedFilter
        textField = FormattedValue(nameExpression)
        textFont = StringListValue(LabelFonts)
        textSize = DoubleValue(LABEL_SIZE)
        textColor = ColorValue(labelColorExpression)
        textHaloColor = ColorValue(Color.White)
        textHaloWidth = DoubleValue(LABEL_HALO_WIDTH)
        textColorUseTheme = StringValue(OutdoorsColorTheme.COLOR_USE_THEME_NONE)
        textHaloColorUseTheme = StringValue(OutdoorsColorTheme.COLOR_USE_THEME_NONE)
        textEmissiveStrength = DoubleValue(MapLighting.OVERLAY_EMISSIVE_STRENGTH)
    }
    landscapes.selectedLandscape?.let { landscape ->
        SelectedLandscapeLayers(
            sourceState = outlinesSource,
            landscape = landscape,
            color = landscape.colorRes.toComposeColor(context),
        )
    }
}

@OptIn(MapboxExperimental::class)
@Composable
@MapboxMapComposable
private fun SelectedLandscapeLayers(sourceState: GeoJsonSourceState, landscape: Landscape, color: Color) {
    val selectedFilter = Filter(Expression.eq(Expression.get(PROPERTY_ID), Expression.literal(landscape.id)))
    FillLayer(sourceState = sourceState, layerId = SELECTED_TINT_LAYER_ID) {
        filter = selectedFilter
        fillColor = ColorValue(color)
        fillOpacity = DoubleValue(SELECTED_TINT_OPACITY)
        fillColorUseTheme = StringValue(OutdoorsColorTheme.COLOR_USE_THEME_NONE)
        fillEmissiveStrength = DoubleValue(MapLighting.OVERLAY_EMISSIVE_STRENGTH)
    }
    // Rings are counter-clockwise, so a negative offset keeps the glow just outside the edge
    LineLayer(sourceState = sourceState, layerId = SELECTED_GLOW_LAYER_ID) {
        filter = selectedFilter
        lineWidth = DoubleValue(SELECTED_GLOW_WIDTH)
        lineOffset = DoubleValue(-SELECTED_GLOW_WIDTH / 2)
        lineBlur = DoubleValue(SELECTED_GLOW_BLUR)
        lineOpacity = DoubleValue(SELECTED_GLOW_OPACITY)
        lineColor = ColorValue(color)
        lineJoin = LineJoinValue.ROUND
        lineColorUseTheme = StringValue(OutdoorsColorTheme.COLOR_USE_THEME_NONE)
        lineEmissiveStrength = DoubleValue(MapLighting.OVERLAY_EMISSIVE_STRENGTH)
    }
    LineLayer(sourceState = sourceState, layerId = SELECTED_HALO_LAYER_ID) {
        filter = selectedFilter
        lineWidth = DoubleValue(SELECTED_HALO_WIDTH)
        lineColor = ColorValue(Color.White)
        lineCap = LineCapValue.ROUND
        lineJoin = LineJoinValue.ROUND
        lineColorUseTheme = StringValue(OutdoorsColorTheme.COLOR_USE_THEME_NONE)
        lineEmissiveStrength = DoubleValue(MapLighting.OVERLAY_EMISSIVE_STRENGTH)
    }
    LineLayer(sourceState = sourceState, layerId = SELECTED_OUTLINE_LAYER_ID) {
        filter = selectedFilter
        lineWidth = DoubleValue(SELECTED_OUTLINE_WIDTH)
        lineColor = ColorValue(color)
        lineCap = LineCapValue.ROUND
        lineJoin = LineJoinValue.ROUND
        lineColorUseTheme = StringValue(OutdoorsColorTheme.COLOR_USE_THEME_NONE)
        lineEmissiveStrength = DoubleValue(MapLighting.OVERLAY_EMISSIVE_STRENGTH)
    }
}

@Composable
private fun rememberLandscapesSource(key: String, geoJson: String): GeoJsonSourceState {
    val source = rememberGeoJsonSourceState(key = key)
    LaunchedEffect(geoJson) {
        source.data = GeoJSONData(geoJson)
    }
    return source
}

private fun List<Landscape>.colorExpression(fallback: Color, color: (Landscape) -> Color): Expression =
    Expression.match(
        Expression.get(PROPERTY_ID),
        *map { Expression.literal(it.id) to Expression.color(color(it).toArgb()) }.toTypedArray(),
        fallback = Expression.color(fallback.toArgb()),
    )

private fun Color.darkened(amount: Float): Color =
    copy(red = red * (1 - amount), green = green * (1 - amount), blue = blue * (1 - amount))

@Composable
private fun DestinationMarkerAnnotation(destination: Destination, isSelected: Boolean, onClick: () -> Unit) {
    ViewAnnotation(
        options = ViewAnnotationOptions.Builder()
            .annotatedFeature(AnnotatedFeature(destination.location.toPoint()))
            .allowOverlap(true)
            .allowOverlapWithPuck(true)
            .ignoreCameraPadding(true)
            .priority(if (isSelected) SELECTED_MARKER_PRIORITY else destination.popularity.toLong())
            .build(),
    ) {
        DestinationMarkerView(
            type = destination.type,
            isSelected = isSelected,
            modifier = Modifier
                .clickable(onClick = onClick)
                .semantics {
                    role = Role.Button
                    contentDescription = destination.name
                }
                .testTagAsResourceId(TestTags.LANDSCAPES_DESTINATION_MARKER),
        )
    }
}

@Composable
fun DestinationMarkerView(type: DestinationType, isSelected: Boolean, modifier: Modifier = Modifier) {
    val categoryColor = mokoColor(type.colorRes)
    val surfaceColor = mokoColor(SharedRes.colors.mapStroke)
    val size by animateDpAsState(
        if (isSelected) Dimens.DestinationSelectedMarkerSize else Dimens.DestinationMarkerSize,
    )
    val iconSize by animateDpAsState(if (isSelected) Dimens.IconSmall else Dimens.IconExtraSmall)
    Box(
        modifier = Modifier
            .size(size)
            .shadow(elevation = 2.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(categoryColor)
            .border(width = 2.dp, color = surfaceColor, shape = CircleShape)
            .then(modifier),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = mokoImage(type.iconRes),
            contentDescription = null,
            tint = surfaceColor,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Preview
@Composable
private fun DestinationMarkerViewPreview() {
    HuKiTheme {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(Dimens.Large),
            horizontalArrangement = Arrangement.spacedBy(Dimens.Large),
        ) {
            DestinationMarkerView(type = DestinationType.RIVER, isSelected = false)
            DestinationMarkerView(type = DestinationType.RIVER, isSelected = true)
            DestinationMarkerView(type = DestinationType.PEAK, isSelected = false)
            DestinationMarkerView(type = DestinationType.CAVE, isSelected = true)
        }
    }
}
