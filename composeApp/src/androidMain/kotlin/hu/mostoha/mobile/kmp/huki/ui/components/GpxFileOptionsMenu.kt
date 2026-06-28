package hu.mostoha.mobile.kmp.huki.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.FeatureFlags
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.testTagAsResourceId

@Composable
fun GpxFileOptionsMenu(
    onRenameClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    Box {
        IconButton(
            modifier = modifier.testTag(TestTags.GPX_COLLECTION_ITEM_OPTIONS_BUTTON),
            onClick = { expanded = true },
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(SharedRes.images.ic_more_vert.drawableResId),
                contentDescription = mokoString(SharedRes.strings.gpx_collection_a11y_options),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(Dimens.Large),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = Dimens.ExtraSmall,
        ) {
            val itemContentPadding = PaddingValues(
                start = Dimens.Large,
                end = Dimens.Huge,
                top = Dimens.Medium,
                bottom = Dimens.Medium,
            )
            if (FeatureFlags.IS_GPX_RENAME_ENABLED) {
                DropdownMenuItem(
                    contentPadding = itemContentPadding,
                    text = { Text(text = mokoString(SharedRes.strings.gpx_collection_action_rename)) },
                    leadingIcon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(SharedRes.images.ic_edit.drawableResId),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    onClick = {
                        expanded = false
                        onRenameClick()
                    },
                )
            }
            if (FeatureFlags.IS_GPX_SHARE_ENABLED) {
                DropdownMenuItem(
                    contentPadding = itemContentPadding,
                    text = { Text(text = mokoString(SharedRes.strings.gpx_collection_action_share)) },
                    leadingIcon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(SharedRes.images.ic_share.drawableResId),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    onClick = {
                        expanded = false
                        onShareClick()
                    },
                )
            }
            DropdownMenuItem(
                modifier = Modifier.testTagAsResourceId(TestTags.GPX_COLLECTION_ITEM_DELETE_BUTTON),
                contentPadding = itemContentPadding,
                text = {
                    Text(
                        text = mokoString(SharedRes.strings.gpx_collection_action_delete),
                        color = mokoColor(SharedRes.colors.error),
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(SharedRes.images.ic_delete.drawableResId),
                        contentDescription = null,
                        tint = mokoColor(SharedRes.colors.error),
                    )
                },
                onClick = {
                    expanded = false
                    onDeleteClick()
                },
            )
        }
    }
}

@Preview
@Composable
private fun GpxFileOptionsMenuPreview() {
    HuKiTheme {
        GpxFileOptionsMenu(
            onRenameClick = {},
            onShareClick = {},
            onDeleteClick = {},
        )
    }
}

@Preview
@Composable
private fun GpxFileOptionsMenuExpandedPreview() {
    HuKiTheme {
        GpxFileOptionsMenu(
            onRenameClick = {},
            onShareClick = {},
            onDeleteClick = {},
            initiallyExpanded = true,
        )
    }
}
