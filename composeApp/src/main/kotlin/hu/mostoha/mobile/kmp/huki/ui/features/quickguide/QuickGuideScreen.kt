package hu.mostoha.mobile.kmp.huki.ui.features.quickguide

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.quickguide.QuickGuideExamples
import hu.mostoha.mobile.kmp.huki.features.quickguide.QuickGuideUiEvents
import hu.mostoha.mobile.kmp.huki.features.quickguide.QuickGuideViewModel
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.theme.SlopeColorScale
import hu.mostoha.mobile.kmp.huki.theme.SlopeLegend
import hu.mostoha.mobile.kmp.huki.theme.SlopeLegendRow
import hu.mostoha.mobile.kmp.huki.ui.components.ButtonSize
import hu.mostoha.mobile.kmp.huki.ui.components.GuideSection
import hu.mostoha.mobile.kmp.huki.ui.components.GuideSectionTitle
import hu.mostoha.mobile.kmp.huki.ui.components.GuideStep
import hu.mostoha.mobile.kmp.huki.ui.components.GuideStepBadge
import hu.mostoha.mobile.kmp.huki.ui.components.GuideStepDivider
import hu.mostoha.mobile.kmp.huki.ui.components.NavigationRowCard
import hu.mostoha.mobile.kmp.huki.ui.components.PrimaryButton
import hu.mostoha.mobile.kmp.huki.ui.components.ScreenViewEffect
import hu.mostoha.mobile.kmp.huki.ui.components.StatChip
import hu.mostoha.mobile.kmp.huki.ui.components.StatChipStyle
import hu.mostoha.mobile.kmp.huki.util.SlopeConstants
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoString
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun QuickGuideScreen(onBack: () -> Unit, onGpxGuideClicked: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<QuickGuideViewModel>()
    ScreenViewEffect { viewModel.onEvent(QuickGuideUiEvents.ScreenViewed) }
    QuickGuideContent(
        onBack = onBack,
        onGpxGuideClicked = onGpxGuideClicked,
        modifier = modifier,
    )
}

@Composable
private fun QuickGuideContent(onBack: () -> Unit, onGpxGuideClicked: () -> Unit, modifier: Modifier = Modifier) {
    val screenColor = MaterialTheme.colorScheme.surfaceVariant
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .testTag(TestTags.QUICK_GUIDE_SCREEN_ROOT),
        containerColor = screenColor,
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(text = mokoString(SharedRes.strings.quick_guide_title)) },
                navigationIcon = {
                    IconButton(
                        modifier = Modifier.testTag(TestTags.QUICK_GUIDE_BACK_BUTTON),
                        onClick = onBack,
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_back),
                            contentDescription = mokoString(SharedRes.strings.quick_guide_a11y_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = screenColor,
                    scrolledContainerColor = screenColor,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(bottom = Dimens.ExtraLarge),
        ) {
            MyLocationSection()
            RoutePlannerSection()
            TravelTimeSection()
            SlopeSection()
            GpxSection(onGpxGuideClicked = onGpxGuideClicked)
        }
    }
}

@Composable
private fun MyLocationSection() {
    GuideSection(title = mokoString(SharedRes.strings.quick_guide_my_location_section)) {
        GuideStep(
            title = mokoString(SharedRes.strings.quick_guide_my_location_following_title),
            message = mokoString(SharedRes.strings.quick_guide_my_location_following_message),
            leading = {
                GestureDemoFab(iconResId = R.drawable.ic_fab_my_location_following, gesture = DemoGesture.CLICK)
            },
        )
        GuideStepDivider(startIndent = MY_LOCATION_DIVIDER_INDENT)
        GuideStep(
            title = mokoString(SharedRes.strings.quick_guide_my_location_navigation_title),
            message = mokoString(SharedRes.strings.quick_guide_my_location_navigation_message),
            leading = {
                GestureDemoFab(
                    iconResId = R.drawable.ic_fab_my_location_live_compass,
                    gesture = DemoGesture.LONG_CLICK,
                )
            },
        )
    }
}

@Composable
private fun RoutePlannerSection() {
    GuideSection(title = mokoString(SharedRes.strings.quick_guide_route_planner_section)) {
        GuideStep(
            title = mokoString(SharedRes.strings.quick_guide_route_planner_step_1_title),
            message = mokoString(SharedRes.strings.quick_guide_route_planner_step_1_message),
            leading = { GuideStepBadge(number = 1) },
        )
        GuideStepDivider()
        GuideStep(
            title = mokoString(SharedRes.strings.quick_guide_route_planner_step_2_title),
            message = mokoString(SharedRes.strings.quick_guide_route_planner_step_2_message),
            leading = { GuideStepBadge(number = 2) },
        )
        GuideStepDivider()
        GuideStep(
            title = mokoString(SharedRes.strings.quick_guide_route_planner_step_3_title),
            message = mokoString(SharedRes.strings.quick_guide_route_planner_step_3_message),
            leading = { GuideStepBadge(number = 3) },
            content = {
                PrimaryButton(
                    iconResId = R.drawable.ic_route,
                    text = mokoString(SharedRes.strings.place_details_route_plan_count, ROUTE_PLAN_EXAMPLE_STOP_COUNT),
                    onClick = {},
                    size = ButtonSize.SMALL,
                    modifier = Modifier
                        .padding(top = Dimens.Small, bottom = Dimens.ExtraSmall)
                        .clearAndSetSemantics {},
                )
            },
        )
    }
}

@Composable
private fun TravelTimeSection() {
    GuideSection(title = mokoString(SharedRes.strings.quick_guide_travel_time_section)) {
        GuideStep(
            title = mokoString(SharedRes.strings.quick_guide_travel_time_time_title),
            message = mokoString(SharedRes.strings.quick_guide_travel_time_time_message),
            leading = {
                StatChip(
                    iconResId = R.drawable.ic_clock_filled,
                    value = mokoString(QuickGuideExamples.travelTime),
                    style = StatChipStyle.Large,
                    modifier = Modifier
                        .width(TRAVEL_TIME_TILE_WIDTH.dp)
                        .clearAndSetSemantics {},
                )
            },
        )
        NoStopsCallout(
            modifier = Modifier.padding(
                start = Dimens.MediumLarge,
                end = Dimens.MediumLarge,
                bottom = Dimens.MediumLarge,
            ),
        )
    }
}

@Composable
private fun StepIcon(iconResId: Int) {
    Box(
        modifier = Modifier
            .size(Dimens.IconContainer)
            .background(mokoColor(SharedRes.colors.primaryContainer), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(iconResId),
            contentDescription = null,
            tint = mokoColor(SharedRes.colors.primary),
            modifier = Modifier.size(Dimens.IconSmall),
        )
    }
}

@Composable
private fun NoStopsCallout(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.Medium))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(Dimens.MediumLarge),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Medium),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_info),
            contentDescription = null,
            tint = mokoColor(SharedRes.colors.primary),
            modifier = Modifier.size(Dimens.IconMedium),
        )
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall)) {
            Text(
                text = mokoString(SharedRes.strings.quick_guide_travel_time_no_stops_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = mokoString(SharedRes.strings.quick_guide_travel_time_no_stops_message),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun SlopeSection() {
    val flatColor = mokoColor(SharedRes.colors.primary)
    val colorScale = remember(flatColor) { SlopeColorScale(flatColor.toArgb()) }
    GuideSection(title = mokoString(SharedRes.strings.quick_guide_slope_section)) {
        GuideStep(
            title = mokoString(SharedRes.strings.quick_guide_slope_coloring_title),
            message = mokoString(
                SharedRes.strings.quick_guide_slope_message,
                SlopeConstants.HIGH_SLOPE_PERCENT.toInt(),
            ),
            leading = { StepIcon(iconResId = R.drawable.ic_trending_up) },
        ) {
            Column(
                modifier = Modifier.padding(top = Dimens.MediumLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.Large),
            ) {
                SlopeBar(colorScale = colorScale)
                SlopeLegendTable(colorScale = colorScale)
            }
        }
    }
}

@Composable
private fun SlopeBar(colorScale: SlopeColorScale) {
    val colorStops = remember(colorScale) {
        colorScale.gradientStops()
            .map { it.fraction to Color(it.rgb).copy(alpha = 1f) }
            .toTypedArray()
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.Medium),
    ) {
        Text(
            text = SlopeLegend.minLabel,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(SLOPE_BAR_HEIGHT.dp)
                .clip(RoundedCornerShape(Dimens.Small))
                .background(Brush.horizontalGradient(colorStops = colorStops)),
        )
        Text(
            text = SlopeLegend.maxLabel,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SlopeLegendTable(colorScale: SlopeColorScale) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SmallMedium)) {
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Medium)) {
            Text(
                text = mokoString(SharedRes.strings.quick_guide_slope_level_column),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(SLOPE_LEVEL_COLUMN_WIDTH.dp),
            )
            Text(
                text = mokoString(SharedRes.strings.quick_guide_slope_difficulty_column),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        SlopeLegend.rows.forEach { row ->
            SlopeLegendTableRow(row = row, color = Color(colorScale.colorRgb(row.slopePercent)).copy(alpha = 1f))
        }
    }
}

@Composable
private fun SlopeLegendTableRow(row: SlopeLegendRow, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.Medium),
    ) {
        Row(
            modifier = Modifier.width(SLOPE_LEVEL_COLUMN_WIDTH.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SmallMedium),
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.Medium)
                    .clip(CircleShape)
                    .background(color),
            )
            Text(
                text = row.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(
            text = mokoString(row.description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun GpxSection(onGpxGuideClicked: () -> Unit) {
    Column(
        modifier = Modifier.padding(top = Dimens.ExtraLarge),
        verticalArrangement = Arrangement.spacedBy(Dimens.Small),
    ) {
        GuideSectionTitle(
            text = mokoString(SharedRes.strings.quick_guide_gpx_section),
            modifier = Modifier.padding(horizontal = Dimens.Large),
        )
        NavigationRowCard(
            iconResId = SharedRes.images.ic_gpx.drawableResId,
            title = mokoString(SharedRes.strings.menu_item_gpx_guide),
            subtitle = mokoString(SharedRes.strings.menu_item_gpx_guide_description),
            onClick = onGpxGuideClicked,
            modifier = Modifier.testTag(TestTags.QUICK_GUIDE_GPX_GUIDE_ROW),
        )
    }
}

private const val MY_LOCATION_DIVIDER_INDENT = 87
private const val ROUTE_PLAN_EXAMPLE_STOP_COUNT = 3
private const val SLOPE_BAR_HEIGHT = 12
private const val TRAVEL_TIME_TILE_WIDTH = 76
private const val SLOPE_LEVEL_COLUMN_WIDTH = 72

@Preview
@Composable
private fun QuickGuideScreenPreview() {
    HuKiTheme {
        QuickGuideContent(onBack = {}, onGpxGuideClicked = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
private fun QuickGuideScreenDarkPreview() {
    HuKiTheme {
        QuickGuideContent(onBack = {}, onGpxGuideClicked = {})
    }
}

@Preview(widthDp = 800, heightDp = 400)
@Composable
private fun QuickGuideScreenLandscapePreview() {
    HuKiTheme {
        QuickGuideContent(onBack = {}, onGpxGuideClicked = {})
    }
}
