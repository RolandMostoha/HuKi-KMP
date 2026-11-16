package hu.mostoha.mobile.kmp.huki.ui.features.layers

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme

@Composable
fun LayersItem(
    title: String,
    @DrawableRes imageRes: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    showCheckmark: Boolean = false,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.then(
            if (showCheckmark) {
                Modifier.toggleable(
                    value = selected,
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Checkbox,
                    onValueChange = { onClick() },
                )
            } else {
                Modifier.selectable(
                    selected = selected,
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.RadioButton,
                    onClick = onClick,
                )
            },
        ),
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .padding(Dimens.Small)
                    .widthIn(max = 72.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .indication(interactionSource, ripple())
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent,
                    )
                    .border(
                        width = 2.dp,
                        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(20.dp),
                    ),
            ) {
                Image(
                    painter = painterResource(imageRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                        .clip(RoundedCornerShape(16.dp)),
                )
            }
            LayersCheckmarkBadge(visible = showCheckmark && selected)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun LayersCheckmarkBadge(visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut(),
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_check),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .size(24.dp)
                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .padding(4.dp),
        )
    }
}

@Preview
@Composable
private fun LayersItemCheckmarkPreview() {
    HuKiTheme {
        LayersItem(
            title = "Hiking Trails",
            imageRes = SharedRes.images.ic_layers_hiking.drawableResId,
            selected = true,
            showCheckmark = true,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun LayersItemPreview() {
    HuKiTheme {
        LayersItem(
            title = "Satellite",
            imageRes = SharedRes.images.ic_layers_satellite.drawableResId,
            selected = true,
            onClick = {},
        )
    }
}
