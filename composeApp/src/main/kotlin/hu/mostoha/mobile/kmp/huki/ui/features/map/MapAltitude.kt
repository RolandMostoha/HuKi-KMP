package hu.mostoha.mobile.kmp.huki.ui.features.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.formatter.DistanceFormatter
import hu.mostoha.mobile.kmp.huki.util.mokoString

private const val ALTITUDE_ICON = "▲"

@Composable
fun MapAltitude(altitude: Int?, isDarkMap: Boolean, modifier: Modifier = Modifier) {
    val lastAltitude = remember { mutableStateOf(altitude) }
    if (altitude != null) {
        lastAltitude.value = altitude
    }
    AnimatedVisibility(
        visible = altitude != null,
        modifier = modifier,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        lastAltitude.value?.let { AltitudeLabel(altitude = it, isDarkMap = isDarkMap) }
    }
}

@Composable
private fun AltitudeLabel(altitude: Int, isDarkMap: Boolean) {
    val formattedAltitude = DistanceFormatter.formatMeters(altitude)
    val description = mokoString(SharedRes.strings.my_location_a11y_altitude, formattedAltitude)
    val text = "$ALTITUDE_ICON $formattedAltitude"
    val textStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
    // The stroke is centred on the glyph outline, so 3dp reaches 1.5dp past it
    val haloWidth = with(LocalDensity.current) { 3.dp.toPx() }
    val textColor by animateColorAsState(if (isDarkMap) Color.White else Color.Black)
    val haloColor by animateColorAsState(if (isDarkMap) Color.Black else Color.White)
    Box(
        modifier = Modifier.clearAndSetSemantics {
            testTag = TestTags.MAIN_ALTITUDE
            contentDescription = description
        },
    ) {
        Text(
            text = text,
            style = textStyle.copy(
                color = haloColor,
                drawStyle = Stroke(width = haloWidth, join = StrokeJoin.Round),
            ),
        )
        Text(text = text, style = textStyle.copy(color = textColor))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFD8E8C8)
@Composable
private fun MapAltitudePreview() {
    MapAltitude(altitude = 1124, isDarkMap = false)
}

@Preview(showBackground = true, backgroundColor = 0xFF292E29)
@Composable
private fun MapAltitudeDarkPreview() {
    MapAltitude(altitude = 103, isDarkMap = true)
}
