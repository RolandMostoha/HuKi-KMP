package hu.mostoha.mobile.kmp.huki.ui.features.routeplanner

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.theme.LocalIsDarkTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoImage
import hu.mostoha.mobile.kmp.huki.util.mokoString

private const val CARD_BACKGROUND_ALPHA = 0.08f
private const val CARD_BORDER_ALPHA = 0.35f
private const val CARD_FADE_IN_MILLIS = 180
private const val CARD_FADE_OUT_MILLIS = 100
private val CardCornerRadius = 16.dp
private val CardBottomPadding = 12.dp
private val ActionHeight = 50.dp
private val ActionIconSize = 18.dp
private val ActionCornerRadius = 12.dp
private val EmptyStopFill = Color(0xFFFFFFF7)
private val EmptyStopOutline = Color(0xFF1A1A1A)

data class RoutePlannerStopCardAction(
    @param:DrawableRes val iconResId: Int,
    val title: String,
    val testTag: String,
    val onClick: () -> Unit,
)

@Composable
fun RoutePlannerStopCard(
    title: String,
    hasConnectorAbove: Boolean,
    hasConnectorBelow: Boolean,
    isRemovable: Boolean,
    actions: List<RoutePlannerStopCardAction>,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = true,
    isTitleHighlighted: Boolean = false,
    titleTestTag: String? = null,
    onTitleClickLabel: String? = null,
    onTitleClick: (() -> Unit)? = null,
    rowHeight: Dp = Dimens.RoutePlannerRowHeight,
    trailing: (@Composable () -> Unit)? = null,
    icon: @Composable () -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary
    val isTitleEmphasized = isExpanded || isTitleHighlighted
    // The card fades out before the next one fades in, so switching stops never shows two cards at once
    val cardAlpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = if (isExpanded) {
            tween(durationMillis = CARD_FADE_IN_MILLIS, delayMillis = CARD_FADE_OUT_MILLIS)
        } else {
            tween(durationMillis = CARD_FADE_OUT_MILLIS)
        },
        label = "stopCardAlpha",
    )
    val titleColor by animateColorAsState(
        targetValue = if (isTitleEmphasized) primary else MaterialTheme.colorScheme.onSurface,
        label = "stopCardTitleColor",
    )
    val connectorColor = routePlannerConnectorColor
    Row(
        modifier = modifier
            .fillMaxWidth()
            .stopCardBackground(primary, cardAlpha)
            .then(if (hasConnectorBelow) Modifier.routePlannerConnectorBelow(rowHeight, connectorColor) else Modifier),
        horizontalArrangement = Arrangement.spacedBy(Dimens.MediumLarge),
    ) {
        RoutePlannerWaypointIcon(
            hasConnectorAbove = hasConnectorAbove,
            hasConnectorBelow = hasConnectorBelow,
            modifier = Modifier.height(rowHeight),
            icon = icon,
        )
        Row(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.height(rowHeight),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .then(if (titleTestTag != null) Modifier.testTag(titleTestTag) else Modifier)
                            .then(
                                if (onTitleClick != null) {
                                    // The card opening is the feedback, a ripple would fill the title's bare rectangle
                                    Modifier.clickable(
                                        interactionSource = null,
                                        indication = null,
                                        onClickLabel = onTitleClickLabel,
                                        onClick = onTitleClick,
                                    )
                                } else {
                                    Modifier
                                },
                            ),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isTitleEmphasized) FontWeight.SemiBold else null,
                            color = titleColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (isRemovable) {
                        RoutePlannerRemoveStopButton(onClick = onRemoveClick)
                    }
                }
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(tween(durationMillis = CARD_FADE_IN_MILLIS, delayMillis = CARD_FADE_OUT_MILLIS)) +
                        expandVertically(expandFrom = Alignment.Top),
                    exit = fadeOut(tween(durationMillis = CARD_FADE_OUT_MILLIS)) +
                        shrinkVertically(shrinkTowards = Alignment.Top),
                ) {
                    Row(
                        modifier = Modifier.padding(top = Dimens.ExtraSmall, bottom = CardBottomPadding),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SmallMedium),
                    ) {
                        actions.forEach { action ->
                            StopCardActionTile(action = action, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            // Centered on the whole card, so a stop can be dragged with its card open too
            trailing?.let { Box(modifier = Modifier.align(Alignment.CenterVertically)) { it() } }
        }
    }
}

@Composable
private fun StopCardActionTile(action: RoutePlannerStopCardAction, modifier: Modifier = Modifier) {
    Surface(
        onClick = action.onClick,
        modifier = modifier
            .heightIn(min = ActionHeight)
            .testTag(action.testTag),
        shape = RoundedCornerShape(ActionCornerRadius),
        // White on the tinted card, like the grouped buttons on iOS
        color = if (LocalIsDarkTheme.current) MaterialTheme.colorScheme.surfaceVariant else Color.White,
        contentColor = MaterialTheme.colorScheme.primary,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = Dimens.ExtraSmall, vertical = Dimens.SmallMedium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(action.iconResId),
                contentDescription = null,
                modifier = Modifier.size(ActionIconSize),
            )
            Text(
                text = action.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun RoutePlannerRemoveStopButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(Dimens.RoutePlannerStopActionSize)
            .testTag(TestTags.ROUTE_PLANNER_DELETE_STOP_BUTTON),
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_close),
            contentDescription = mokoString(SharedRes.strings.route_planner_a11y_delete_stop),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Dimens.RoutePlannerRemoveStopIconSize),
        )
    }
}

@Composable
fun RoutePlannerEmptyStopIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(Dimens.RoutePlannerWaypointIconColumnWidth)
            .drawBehind {
                val strokeWidth = 1.5.dp.toPx()
                // Fixed colors like the waypoint markers, so the dashes stay visible on the white fill in dark mode too
                drawCircle(color = EmptyStopFill, radius = size.minDimension / 2 - strokeWidth / 2)
                drawCircle(
                    color = EmptyStopOutline,
                    radius = size.minDimension / 2 - strokeWidth / 2,
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(3.dp.toPx(), 2.5.dp.toPx()),
                        ),
                    ),
                )
            },
    )
}

// The card reaches past the row's own padding, so its stop icon stays in line with the plain rows
private fun Modifier.stopCardBackground(color: Color, alpha: Float): Modifier =
    drawBehind {
        if (alpha == 0f) return@drawBehind
        val inset = Dimens.RoutePlannerStopCardInset.toPx()
        val radius = CornerRadius(CardCornerRadius.toPx())
        val topLeft = Offset(-inset, 0f)
        val cardSize = Size(size.width + inset * 2, size.height)
        drawRoundRect(
            color = color.copy(alpha = CARD_BACKGROUND_ALPHA * alpha),
            topLeft = topLeft,
            size = cardSize,
            cornerRadius = radius,
        )
        // A stroke straddles its path, so the border is inset by half its width to stay inside the clipped row
        val strokeWidth = 1.dp.toPx()
        drawRoundRect(
            color = color.copy(alpha = CARD_BORDER_ALPHA * alpha),
            topLeft = topLeft + Offset(strokeWidth / 2, strokeWidth / 2),
            size = Size(cardSize.width - strokeWidth, cardSize.height - strokeWidth),
            cornerRadius = radius,
            style = Stroke(width = strokeWidth),
        )
    }

@Preview
@Composable
private fun RoutePlannerStopCardPreview() {
    HuKiTheme {
        Column(modifier = Modifier.padding(Dimens.Large)) {
            RoutePlannerStopCard(
                title = "Háromkő-hegy",
                hasConnectorAbove = false,
                hasConnectorBelow = true,
                isRemovable = true,
                actions = listOf(
                    RoutePlannerStopCardAction(R.drawable.ic_open_with, "Áthelyezés", "move") {},
                    RoutePlannerStopCardAction(R.drawable.ic_search, "Csere", "replace") {},
                    RoutePlannerStopCardAction(R.drawable.ic_edit_square, "Átnevezés", "rename") {},
                ),
                onRemoveClick = {},
            ) {
                RoutePlannerRowIcon(imageVector = mokoImage(WaypointType.START.icon), tint = Color.Unspecified)
            }
            RoutePlannerStopCard(
                title = "Új megálló",
                hasConnectorAbove = true,
                hasConnectorBelow = false,
                isRemovable = true,
                actions = listOf(
                    RoutePlannerStopCardAction(R.drawable.ic_recenter, "Térképen", "pick") {},
                    RoutePlannerStopCardAction(R.drawable.ic_search, "Keresőből", "search") {},
                ),
                onRemoveClick = {},
            ) {
                RoutePlannerEmptyStopIcon()
            }
        }
    }
}
