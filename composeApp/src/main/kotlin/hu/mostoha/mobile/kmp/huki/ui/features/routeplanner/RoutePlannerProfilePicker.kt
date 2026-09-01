package hu.mostoha.mobile.kmp.huki.ui.features.routeplanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerProfile
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RoutePlannerProfilePicker(
    selectedProfile: RoutePlannerProfile,
    onProfileSelected: (RoutePlannerProfile) -> Unit,
    modifier: Modifier = Modifier,
) {
    val profiles = RoutePlannerProfile.entries
    val profilesLabel = mokoString(SharedRes.strings.route_planner_a11y_profiles)
    val colors = ToggleButtonDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        checkedContainerColor = MaterialTheme.colorScheme.primary,
        checkedContentColor = MaterialTheme.colorScheme.onPrimary,
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag(TestTags.ROUTE_PLANNER_PROFILE_PICKER)
            .semantics { contentDescription = profilesLabel },
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        profiles.forEachIndexed { index, profile ->
            val shapes = when (index) {
                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                profiles.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
            }
            ToggleButton(
                checked = selectedProfile == profile,
                onCheckedChange = { onProfileSelected(profile) },
                shapes = shapes,
                colors = colors,
                contentPadding = PaddingValues(horizontal = Dimens.Small, vertical = Dimens.Medium),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = mokoString(profile.title),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview
@Composable
private fun RoutePlannerProfilePickerPreview() {
    HuKiTheme {
        RoutePlannerProfilePicker(selectedProfile = RoutePlannerProfile.ON_TRAILS, onProfileSelected = {})
    }
}

@Preview
@Composable
private fun RoutePlannerProfilePickerBikePreview() {
    HuKiTheme {
        RoutePlannerProfilePicker(selectedProfile = RoutePlannerProfile.BIKE, onProfileSelected = {})
    }
}
