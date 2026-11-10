package hu.mostoha.mobile.kmp.huki.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import hu.mostoha.mobile.kmp.huki.model.domain.SupportAnimal
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.mokoColor

@Composable
fun SupportAnimalBadges(ringColor: Color, modifier: Modifier = Modifier, size: Dp = 42.dp) {
    val ringWidth = 3.dp
    Row(
        modifier = modifier.clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(-size * 0.19f - ringWidth * 2),
    ) {
        SupportAnimal.entries.forEachIndexed { index, animal ->
            Box(
                modifier = Modifier
                    .zIndex((SupportAnimal.entries.size - index).toFloat())
                    .border(ringWidth, ringColor, CircleShape)
                    .padding(ringWidth)
                    .size(size)
                    .background(mokoColor(animal.containerColorRes), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(animal.iconRes.drawableResId),
                    contentDescription = null,
                    tint = mokoColor(animal.colorRes),
                    modifier = Modifier.size(size * 0.62f),
                )
            }
        }
    }
}

@Preview
@Composable
private fun SupportAnimalBadgesPreview() {
    HuKiTheme {
        val surface = MaterialTheme.colorScheme.surface
        SupportAnimalBadges(
            ringColor = surface,
            modifier = Modifier
                .background(surface)
                .padding(16.dp),
        )
    }
}
