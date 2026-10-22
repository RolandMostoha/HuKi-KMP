package hu.mostoha.mobile.kmp.huki.ui.features.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.mapbox.maps.extension.compose.annotation.IconImage
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import dev.icerock.moko.resources.ImageResource
import hu.mostoha.mobile.kmp.huki.model.domain.JelSymbol
import hu.mostoha.mobile.kmp.huki.theme.SharedDimens

@Composable
fun rememberJelMarkerIconImage(background: ImageResource, jelSymbol: JelSymbol): IconImage =
    rememberIconImage(
        key = background to jelSymbol,
        painter = JelMarkerPainter(
            background = painterResource(background.drawableResId),
            symbol = painterResource(jelSymbol.icon.drawableResId),
        ),
    )

private class JelMarkerPainter(
    private val background: Painter,
    private val symbol: Painter,
) : Painter() {

    override val intrinsicSize: Size
        get() = background.intrinsicSize

    override fun DrawScope.onDraw() {
        with(background) { draw(size) }
        val symbolSize = size.minDimension * SharedDimens.GPX_JEL_SYMBOL_MARKER_RATIO.toFloat()
        translate(left = (size.width - symbolSize) / 2, top = (size.height - symbolSize) / 2) {
            with(symbol) { draw(Size(symbolSize, symbolSize)) }
        }
    }
}
