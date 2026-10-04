package hu.mostoha.mobile.kmp.huki.ui.features.map

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

/**
 * Rounded map bubble with a centered tail at the bottom, pointing at the annotated location.
 */
internal class InfoWindowShape(
    private val cornerRadius: Dp,
    private val tailWidth: Dp,
    private val tailHeight: Dp,
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val radius = with(density) { cornerRadius.toPx() }
        val halfTail = with(density) { tailWidth.toPx() } / 2f
        val bodyBottom = size.height - with(density) { tailHeight.toPx() }
        val path = Path().apply {
            addRoundRect(RoundRect(0f, 0f, size.width, bodyBottom, CornerRadius(radius)))
            moveTo(size.width / 2f - halfTail, bodyBottom - 1f)
            lineTo(size.width / 2f, size.height)
            lineTo(size.width / 2f + halfTail, bodyBottom - 1f)
            close()
        }
        return Outline.Generic(path)
    }
}
