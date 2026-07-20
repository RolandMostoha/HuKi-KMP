package hu.mostoha.mobile.kmp.huki.ui.features.map

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.DistanceInfoWindowData
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.testTagAsResourceId

@Composable
fun DistanceInfoWindow(info: DistanceInfoWindowData, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val lightContext = remember(context, configuration) {
        context.createConfigurationContext(
            Configuration(configuration).apply {
                uiMode = Configuration.UI_MODE_NIGHT_NO or (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv())
            },
        )
    }
    // Mapbox is light-only, so keep the window in its light appearance regardless of app theme
    CompositionLocalProvider(LocalContext provides lightContext) {
        HuKiTheme(darkTheme = false) {
            DistanceInfoWindowContent(info, modifier)
        }
    }
}

@Composable
private fun DistanceInfoWindowContent(info: DistanceInfoWindowData, modifier: Modifier = Modifier) {
    val contentDescription = mokoString(SharedRes.strings.gpx_distance_info_window_a11y)
    val strokeColor = mokoColor(SharedRes.colors.mapStroke)
    val surfaceColor = MaterialTheme.colorScheme.surface
    Column(
        modifier = modifier
            .wrapContentSize()
            .testTagAsResourceId(TestTags.MAP_DISTANCE_INFO_WINDOW)
            .semantics { this.contentDescription = contentDescription },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            shape = RoundedCornerShape(Dimens.Large),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            border = BorderStroke(Dimens.InfoWindowBorder, strokeColor),
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 6.dp,
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = info.distance,
                    color = mokoColor(SharedRes.colors.primaryStrong),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    fontSize = 14.sp,
                )
                Text(
                    text = mokoString(info.travelTime),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
        val tailOverlap = Dimens.ExtraSmall
        Canvas(
            modifier = Modifier
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val overlap = tailOverlap.roundToPx()
                    layout(placeable.width, placeable.height - overlap) {
                        placeable.place(0, -overlap)
                    }
                }
                .size(
                    width = Dimens.InfoWindowTailWidth,
                    height = Dimens.InfoWindowTailHeight + tailOverlap,
                ),
        ) {
            val cardBottom = tailOverlap.toPx()
            val width = size.width
            val tip = Offset(width / 2f, size.height - Dimens.InfoWindowBorder.toPx() / 2f)
            val topLeft = Offset(0f, cardBottom)
            val topRight = Offset(width, cardBottom)
            val fillPath = Path().apply {
                moveTo(0f, 0f)
                lineTo(width, 0f)
                lineTo(topRight.x, topRight.y)
                lineTo(tip.x, tip.y)
                lineTo(topLeft.x, topLeft.y)
                close()
            }
            drawPath(fillPath, surfaceColor)
            val outlinePath = Path().apply {
                moveTo(topLeft.x, topLeft.y)
                lineTo(tip.x, tip.y)
                lineTo(topRight.x, topRight.y)
            }
            drawPath(
                path = outlinePath,
                color = strokeColor,
                style = Stroke(
                    width = Dimens.InfoWindowBorder.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
        }
    }
}

@Preview
@Composable
private fun DistanceInfoWindowPreview() {
    HuKiTheme {
        DistanceInfoWindow(
            info = DistanceInfoWindowData(
                location = Location(47.5, 19.0),
                distance = "4.2 km",
                travelTime = StringDesc.ResourceFormatted(
                    SharedRes.strings.travel_time_hours_minutes_pattern,
                    1,
                    20,
                ),
            ),
        )
    }
}
