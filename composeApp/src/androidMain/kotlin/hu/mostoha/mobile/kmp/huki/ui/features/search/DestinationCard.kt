package hu.mostoha.mobile.kmp.huki.ui.features.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationType
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoImage
import hu.mostoha.mobile.kmp.huki.util.mokoString

private val CardWidth = 180.dp
private val CardHeight = 190.dp
private val CategoryRowHeight = 32.dp

@Composable
fun DestinationCard(destination: Destination, onClick: () -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val typeColor = mokoColor(destination.type.colorRes)

    Box(
        modifier = modifier
            .width(CardWidth)
            .height(CardHeight)
            .clip(RoundedCornerShape(Dimens.Large))
            .background(typeColor)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) { role = Role.Button }
            .testTag(TestTags.DESTINATION_CARD),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = CategoryRowHeight),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AnimatedVisibility(
                    visible = !expanded,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    TypeChip(type = destination.type, typeColor = typeColor)
                }
                Spacer(modifier = Modifier.weight(1f))
                ExpandButton(
                    expanded = expanded,
                    onClick = { expanded = !expanded },
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            DestinationTexts(destination = destination, expanded = expanded)
        }
    }
}

@Composable
private fun TypeChip(type: DestinationType, typeColor: Color) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = Dimens.Medium, vertical = Dimens.Small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.Small),
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = mokoImage(type.iconRes),
            contentDescription = null,
            tint = typeColor,
        )
        Text(
            text = mokoString(type.title),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = typeColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DestinationTexts(destination: Destination, expanded: Boolean) {
    val descriptionMaxLines = if (expanded) 6 else 1
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall)) {
        Text(
            text = destination.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            autoSize = TextAutoSize.StepBased(
                minFontSize = 12.sp,
                maxFontSize = 17.sp,
                stepSize = 0.5.sp,
            ),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = ImageVector.vectorResource(R.drawable.ic_place),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
            )
            Spacer(modifier = Modifier.width(Dimens.ExtraSmall))
            Text(
                text = destination.town,
                style = MaterialTheme.typography.labelMedium.copy(
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.Both,
                    ),
                ),
                color = Color.White.copy(alpha = 0.9f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = mokoString(destination.description),
            modifier = Modifier
                .padding(top = Dimens.ExtraSmall)
                .animateContentSize(animationSpec = tween(durationMillis = 250)),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = Color.White.copy(alpha = 0.85f),
            maxLines = descriptionMaxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ExpandButton(expanded: Boolean, onClick: () -> Unit) {
    val infoLabel = mokoString(SharedRes.strings.destinations_a11y_info)
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.28f))
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = infoLabel
            }
            .testTag(TestTags.DESTINATION_EXPAND_BUTTON),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(14.dp),
            imageVector = ImageVector.vectorResource(
                if (expanded) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down,
            ),
            contentDescription = null,
            tint = Color.White,
        )
    }
}

@Preview
@Composable
private fun DestinationCardPreview() {
    HuKiTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Medium)) {
            DestinationCard(destination = previewDestination, onClick = {})
        }
    }
}

private val previewDestination = Destination(
    osmId = "1",
    name = "Dobogókő",
    town = "Pilisszentkereszt",
    type = DestinationType.PEAK,
    location = Location(47.7181, 18.8948),
    description = SharedRes.strings.destinations_section_title,
    popularity = 10,
)
