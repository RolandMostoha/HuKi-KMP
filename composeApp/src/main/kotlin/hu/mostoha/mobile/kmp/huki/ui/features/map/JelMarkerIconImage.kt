package hu.mostoha.mobile.kmp.huki.ui.features.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import com.mapbox.maps.extension.compose.annotation.IconImage
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import dev.icerock.moko.resources.ImageResource
import hu.mostoha.mobile.kmp.huki.model.domain.JelSymbol
import hu.mostoha.mobile.kmp.huki.theme.LocalIsDarkTheme
import hu.mostoha.mobile.kmp.huki.theme.SharedDimens
import hu.mostoha.mobile.kmp.huki.util.mokoPainter

@Composable
fun rememberJelMarkerIconImage(background: ImageResource, jelSymbol: JelSymbol): IconImage =
    rememberIconImage(
        key = Triple(background, jelSymbol, LocalIsDarkTheme.current),
        painter = JelMarkerPainter(
            background = mokoPainter(background),
            symbol = mokoPainter(jelSymbol.icon),
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
