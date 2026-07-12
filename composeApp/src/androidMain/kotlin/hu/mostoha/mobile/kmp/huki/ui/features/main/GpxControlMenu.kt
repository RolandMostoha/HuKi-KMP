package hu.mostoha.mobile.kmp.huki.ui.features.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString

@Composable
fun GpxControlMenu(
    isRouteVisible: Boolean,
    isAllDistancesVisible: Boolean,
    onToggleLineClicked: () -> Unit,
    onToggleDistancesClicked: () -> Unit,
    onOverviewClicked: () -> Unit,
    onClearClicked: () -> Unit,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
) {
    val itemContainerColor = MaterialTheme.colorScheme.surface
    val itemContentColor = MaterialTheme.colorScheme.onSurface
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    BackHandler(enabled = expanded) { expanded = false }
    FloatingActionButtonMenu(
        modifier = modifier,
        expanded = expanded,
        horizontalAlignment = Alignment.Start,
        button = {
            ToggleFloatingActionButton(
                modifier = Modifier.testTag(TestTags.MAIN_FAB_GPX_CONTROL_BUTTON),
                checked = expanded,
                onCheckedChange = { expanded = it },
                containerColor = ToggleFloatingActionButtonDefaults.containerColor(
                    initialColor = MaterialTheme.colorScheme.surface,
                    finalColor = MaterialTheme.colorScheme.primary,
                ),
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
            modifier = Modifier.testTag(TestTags.MAIN_FAB_GPX_TOGGLE_LINE_BUTTON),
            onClick = onToggleLineClicked,
            containerColor = itemContainerColor,
            contentColor = itemContentColor,
            icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(
                        if (isRouteVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility,
                    ),
                    contentDescription = null,
                )
            },
            text = {
                Text(
                    text = mokoString(
                        if (isRouteVisible) {
                            SharedRes.strings.gpx_control_route_visibility_hide
                        } else {
                            SharedRes.strings.gpx_control_route_visibility_show
                        },
                    ),
                )
            },
        )
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
        FloatingActionButtonMenuItem(
            modifier = Modifier.testTag(TestTags.MAIN_FAB_GPX_OVERVIEW_BUTTON),
            onClick = {
                expanded = false
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
                onToggleLineClicked = {},
                onToggleDistancesClicked = {},
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
                onToggleLineClicked = {},
                onToggleDistancesClicked = {},
                onOverviewClicked = {},
                onClearClicked = {},
                initiallyExpanded = true,
            )
        }
    }
}
