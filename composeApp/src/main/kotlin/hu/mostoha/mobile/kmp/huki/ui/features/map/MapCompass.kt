package hu.mostoha.mobile.kmp.huki.ui.features.map

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoImage
import hu.mostoha.mobile.kmp.huki.util.mokoString

private val COMPASS_SIZE = 64.dp
private val DISC_SIZE = 56.dp
private val LETTER_SIZE = 9.dp
private val LETTER_INSET = 4.dp

@Composable
fun MapCompass(cameraBearing: () -> Float, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val a11yLabel = mokoString(SharedRes.strings.my_location_a11y_compass)
    Box(
        modifier = modifier.size(COMPASS_SIZE),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier
                .size(DISC_SIZE)
                .semantics {
                    contentDescription = a11yLabel
                    role = Role.Button
                },
            shape = CircleShape,
            color = Color.Transparent,
            shadowElevation = Dimens.FloatingActionElevation,
        ) {
            Box(
                modifier = Modifier.clearAndSetSemantics {},
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    imageVector = mokoImage(SharedRes.images.ic_my_location_compass),
                    contentDescription = null,
                    modifier = Modifier.requiredSize(COMPASS_SIZE),
                )
                CompassLetters(
                    cameraBearing = cameraBearing,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun CompassLetters(cameraBearing: () -> Float, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        CompassLetter(
            text = mokoString(SharedRes.strings.map_compass_north),
            cameraBearing = cameraBearing,
            color = mokoColor(SharedRes.colors.compassNorth),
            modifier = Modifier.align(Alignment.TopCenter),
        )
        CompassLetter(
            text = mokoString(SharedRes.strings.map_compass_east),
            cameraBearing = cameraBearing,
            modifier = Modifier.align(Alignment.CenterEnd),
        )
        CompassLetter(
            text = mokoString(SharedRes.strings.map_compass_south),
            cameraBearing = cameraBearing,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
        CompassLetter(
            text = mokoString(SharedRes.strings.map_compass_west),
            cameraBearing = cameraBearing,
            modifier = Modifier.align(Alignment.CenterStart),
        )
    }
}

@Composable
private fun CompassLetter(
    text: String,
    cameraBearing: () -> Float,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
) {
    // The compass has a fixed size, so the letters must not scale with the system font size.
    val letterSize = with(LocalDensity.current) { LETTER_SIZE.toSp() }
    Text(
        text = text,
        color = color.takeOrElse { MaterialTheme.colorScheme.onSurface },
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = letterSize,
            lineHeight = letterSize,
        ),
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .padding(LETTER_INSET)
            .graphicsLayer { rotationZ = cameraBearing() },
    )
}

@Preview
@Composable
private fun MapCompassPreview() {
    HuKiTheme {
        MapCompass(cameraBearing = { 0f }, onClick = {})
    }
}

@Preview
@Composable
private fun MapCompassRotatedPreview() {
    HuKiTheme {
        val bearing = 30f
        MapCompass(
            cameraBearing = { bearing },
            onClick = {},
            modifier = Modifier.rotate(-bearing),
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
private fun MapCompassDarkPreview() {
    HuKiTheme {
        MapCompass(cameraBearing = { 0f }, onClick = {})
    }
}
