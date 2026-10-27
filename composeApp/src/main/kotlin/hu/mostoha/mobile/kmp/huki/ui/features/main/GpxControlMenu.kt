package hu.mostoha.mobile.kmp.huki.ui.features.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString
import kotlin.math.roundToInt

@Composable
fun GpxControlMenu(
    isRouteVisible: Boolean,
    isAllDistancesVisible: Boolean,
    isSlopeColorsAvailable: Boolean,
    isSlopeColorsVisible: Boolean,
    isReversible: Boolean,
    onToggleLineClicked: () -> Unit,
    onToggleDistancesClicked: () -> Unit,
    onToggleSlopeColorsClicked: () -> Unit,
    onReverseDirectionClicked: () -> Unit,
    onOverviewClicked: () -> Unit,
    onClearClicked: () -> Unit,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    var menuOrigin by remember { mutableStateOf(Offset.Zero) }
    var menuButtonOrigin by remember { mutableStateOf<Offset?>(null) }
    BackHandler(enabled = expanded) { expanded = false }
    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { menuOrigin = it.positionInRoot() },
    ) {
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown()
                            expanded = false
                        }
                    },
            )
        }
        GpxFabMenu(
            modifier = Modifier.align(Alignment.BottomStart),
            buttonModifier = Modifier.onGloballyPositioned { menuButtonOrigin = it.positionInRoot() },
            expanded = expanded,
            onExpandedChange = { expanded = it },
            isAllDistancesVisible = isAllDistancesVisible,
            isSlopeColorsAvailable = isSlopeColorsAvailable,
            isSlopeColorsVisible = isSlopeColorsVisible,
            isReversible = isReversible,
            onToggleDistancesClicked = onToggleDistancesClicked,
            onToggleSlopeColorsClicked = onToggleSlopeColorsClicked,
            onReverseDirectionClicked = onReverseDirectionClicked,
            onOverviewClicked = onOverviewClicked,
            onClearClicked = onClearClicked,
        )
        // Anchored above the menu button, not stacked on the menu, whose height animates while it collapses
        AnimatedVisibility(
            modifier = Modifier.layout { measurable, constraints ->
                val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
                layout(placeable.width, placeable.height) {
                    val anchor = menuButtonOrigin?.minus(menuOrigin) ?: return@layout
                    val shadowInset = Dimens.Small.roundToPx()
                    placeable.place(
                        x = anchor.x.roundToInt() - shadowInset,
                        y = anchor.y.roundToInt() - Dimens.FloatingActionSpacing.roundToPx() -
                            placeable.height + shadowInset,
                    )
                }
            },
            visible = !expanded,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
        ) {
            // Room for the shadow, which the animation layer would otherwise clip to a rectangle
            GpxRouteVisibilityFab(
                modifier = Modifier.padding(Dimens.Small),
                isRouteVisible = isRouteVisible,
                onClick = onToggleLineClicked,
            )
        }
    }
}

@Composable
private fun GpxRouteVisibilityFab(isRouteVisible: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        modifier = modifier.testTag(TestTags.MAIN_FAB_GPX_TOGGLE_LINE_BUTTON),
        containerColor = MaterialTheme.colorScheme.surface,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = Dimens.FloatingActionElevation),
        shape = CircleShape,
        onClick = onClick,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(
                if (isRouteVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility,
            ),
            contentDescription = mokoString(
                if (isRouteVisible) {
                    SharedRes.strings.gpx_control_route_visibility_hide
                } else {
                    SharedRes.strings.gpx_control_route_visibility_show
                },
            ),
        )
    }
}

@Composable
private fun GpxFabMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    isAllDistancesVisible: Boolean,
    isSlopeColorsAvailable: Boolean,
    isSlopeColorsVisible: Boolean,
    isReversible: Boolean,
    onToggleDistancesClicked: () -> Unit,
    onToggleSlopeColorsClicked: () -> Unit,
    onReverseDirectionClicked: () -> Unit,
    onOverviewClicked: () -> Unit,
    onClearClicked: () -> Unit,
    modifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier,
) {
    val itemContainerColor = MaterialTheme.colorScheme.surface
    val itemContentColor = MaterialTheme.colorScheme.onSurface
    val containerSize = ToggleFloatingActionButtonDefaults.containerSize()
    FloatingActionButtonMenu(
        modifier = modifier,
        expanded = expanded,
        horizontalAlignment = Alignment.Start,
        button = {
            ToggleFloatingActionButton(
                modifier = buttonModifier.testTag(TestTags.MAIN_FAB_GPX_CONTROL_BUTTON),
                checked = expanded,
                onCheckedChange = onExpandedChange,
                containerColor = ToggleFloatingActionButtonDefaults.containerColor(
                    initialColor = MaterialTheme.colorScheme.surface,
                    finalColor = MaterialTheme.colorScheme.primary,
                ),
                containerSize = containerSize,
                containerCornerRadius = { progress -> containerSize(progress) / 2 },
            ) {
                val iconRes by remember {
                    derivedStateOf {
                        if (checkedProgress > 0.5f) R.drawable.ic_close else R.drawable.ic_tune
                    }
                }
                Icon(
                    painter = rememberVectorPainter(ImageVector.vectorResource(iconRes)),
                    contentDescription = mokoString(SharedRes.strings.gpx_control_a11y_fab),
                    tint = Color.Unspecified,
                    modifier = with(ToggleFloatingActionButtonDefaults) {
                        Modifier.animateIcon(
                            checkedProgress = { checkedProgress },
                            color = iconColor(
                                initialColor = MaterialTheme.colorScheme.onSurface,
                                finalColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        )
                    },
                )
            }
        },
    ) {
        FloatingActionButtonMenuItem(
            modifier = Modifier.testTag(TestTags.MAIN_FAB_GPX_TOGGLE_DISTANCES_BUTTON),
            onClick = onToggleDistancesClicked,
            containerColor = itemContainerColor,
            contentColor = itemContentColor,
            icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_ruler),
                    contentDescription = null,
                )
            },
            text = {
                Text(
                    text = mokoString(
                        if (isAllDistancesVisible) {
                            SharedRes.strings.gpx_control_distances_hide
                        } else {
                            SharedRes.strings.gpx_control_distances_show
                        },
                    ),
                )
            },
        )
        if (isSlopeColorsAvailable) {
            FloatingActionButtonMenuItem(
                modifier = Modifier.testTag(TestTags.MAIN_FAB_GPX_TOGGLE_SLOPE_COLORS_BUTTON),
                onClick = onToggleSlopeColorsClicked,
                containerColor = itemContainerColor,
                contentColor = itemContentColor,
                icon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_palette),
                        contentDescription = null,
                    )
                },
                text = {
                    Text(
                        text = mokoString(
                            if (isSlopeColorsVisible) {
                                SharedRes.strings.gpx_control_slope_colors_hide
                            } else {
                                SharedRes.strings.gpx_control_slope_colors_show
                            },
                        ),
                    )
                },
            )
        }
        if (isReversible) {
            FloatingActionButtonMenuItem(
                modifier = Modifier.testTag(TestTags.MAIN_FAB_GPX_REVERSE_DIRECTION_BUTTON),
                onClick = onReverseDirectionClicked,
                containerColor = itemContainerColor,
                contentColor = itemContentColor,
                icon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_swap_horiz),
                        contentDescription = null,
                    )
                },
                text = { Text(text = mokoString(SharedRes.strings.gpx_control_direction_reverse)) },
            )
        }
        FloatingActionButtonMenuItem(
            modifier = Modifier.testTag(TestTags.MAIN_FAB_GPX_OVERVIEW_BUTTON),
            onClick = {
                onExpandedChange(false)
                onOverviewClicked()
            },
            containerColor = itemContainerColor,
            contentColor = itemContentColor,
            icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_maximize),
                    contentDescription = null,
                )
            },
            text = { Text(text = mokoString(SharedRes.strings.gpx_control_overview_title)) },
        )
        FloatingActionButtonMenuItem(
            modifier = Modifier.testTag(TestTags.MAIN_FAB_GPX_CLEAR_BUTTON),
            onClick = onClearClicked,
            containerColor = itemContainerColor,
            contentColor = itemContentColor,
            icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_delete),
                    contentDescription = null,
                )
            },
            text = { Text(text = mokoString(SharedRes.strings.gpx_control_clear_title)) },
        )
    }
}

@Preview
@Composable
private fun GpxControlMenuCollapsedPreview() {
    HuKiTheme {
        Box(modifier = Modifier.size(width = 360.dp, height = 200.dp)) {
            GpxControlMenu(
                isRouteVisible = true,
                isAllDistancesVisible = false,
                isSlopeColorsAvailable = true,
                isSlopeColorsVisible = true,
                isReversible = true,
                onToggleLineClicked = {},
                onToggleDistancesClicked = {},
                onToggleSlopeColorsClicked = {},
                onReverseDirectionClicked = {},
                onOverviewClicked = {},
                onClearClicked = {},
            )
        }
    }
}

@Preview
@Composable
private fun GpxControlMenuExpandedPreview() {
    HuKiTheme {
        Box(modifier = Modifier.size(width = 360.dp, height = 360.dp)) {
            GpxControlMenu(
                isRouteVisible = true,
                isAllDistancesVisible = true,
                isSlopeColorsAvailable = true,
                isSlopeColorsVisible = true,
                isReversible = true,
                onToggleLineClicked = {},
                onToggleDistancesClicked = {},
                onToggleSlopeColorsClicked = {},
                onReverseDirectionClicked = {},
                onOverviewClicked = {},
                onClearClicked = {},
                initiallyExpanded = true,
            )
        }
    }
}
