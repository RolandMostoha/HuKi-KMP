package hu.mostoha.mobile.kmp.huki.ui.features.gpxguide

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.gpxguide.GpxGuideUiEvents
import hu.mostoha.mobile.kmp.huki.features.gpxguide.GpxGuideViewModel
import hu.mostoha.mobile.kmp.huki.model.domain.HikeRecommendation
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.ui.components.GuideSection
import hu.mostoha.mobile.kmp.huki.ui.components.GuideStep
import hu.mostoha.mobile.kmp.huki.ui.components.GuideStepBadge
import hu.mostoha.mobile.kmp.huki.ui.components.GuideStepDivider
import hu.mostoha.mobile.kmp.huki.ui.components.HikeRecommendationCard
import hu.mostoha.mobile.kmp.huki.ui.components.ScreenViewEffect
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.openUrl
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GpxGuideScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<GpxGuideViewModel>()
    ScreenViewEffect { viewModel.onEvent(GpxGuideUiEvents.ScreenViewed) }
    GpxGuideContent(
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
private fun GpxGuideContent(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val screenColor = MaterialTheme.colorScheme.surfaceVariant
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .testTag(TestTags.GPX_GUIDE_SCREEN_ROOT),
        containerColor = screenColor,
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(text = mokoString(SharedRes.strings.gpx_guide_title)) },
                navigationIcon = {
                    IconButton(
                        modifier = Modifier.testTag(TestTags.GPX_GUIDE_BACK_BUTTON),
                        onClick = onBack,
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_back),
                            contentDescription = mokoString(SharedRes.strings.gpx_guide_a11y_back),
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
                .padding(top = Dimens.Small, bottom = Dimens.ExtraLarge),
        ) {
            WhatIsCard()
            StepsSection(onOpenUrl = { context.openUrl(it) })
        }
    }
}

@Composable
private fun WhatIsCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(horizontal = Dimens.Large)
            .clip(RoundedCornerShape(Dimens.Large))
            .background(MaterialTheme.colorScheme.background)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                shape = RoundedCornerShape(Dimens.Large),
            )
            .padding(Dimens.MediumLarge),
        verticalArrangement = Arrangement.spacedBy(Dimens.Small),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconChip()
            Text(
                text = mokoString(SharedRes.strings.gpx_guide_what_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            text = mokoString(SharedRes.strings.gpx_guide_what_message),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun IconChip() {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(mokoColor(SharedRes.colors.primary).copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(SharedRes.images.ic_gpx.drawableResId),
            contentDescription = null,
            tint = mokoColor(SharedRes.colors.primary),
            modifier = Modifier.size(Dimens.IconSmall),
        )
    }
}

@Composable
private fun StepsSection(onOpenUrl: (String) -> Unit) {
    GuideSection(title = mokoString(SharedRes.strings.gpx_guide_steps_section)) {
        GuideStep(
            title = mokoString(SharedRes.strings.gpx_guide_step_1_title),
            message = mokoString(SharedRes.strings.gpx_guide_step_1_message),
            leading = { GuideStepBadge(number = 1) },
        ) {
            RecommendationsRow(
                onOpenUrl = onOpenUrl,
                modifier = Modifier.padding(top = Dimens.Small, end = Dimens.MediumLarge),
            )
        }
        GuideStepDivider()
        GuideStep(
            title = mokoString(SharedRes.strings.gpx_guide_step_2_title),
            message = mokoString(SharedRes.strings.gpx_guide_step_2_message),
            leading = { GuideStepBadge(number = 2) },
        )
        GuideStepDivider()
        GuideStep(
            title = mokoString(SharedRes.strings.gpx_guide_step_3_title),
            message = mokoString(SharedRes.strings.gpx_guide_step_3_message),
            leading = { GuideStepBadge(number = 3) },
        )
        GuideStepDivider()
        GuideStep(
            title = mokoString(SharedRes.strings.gpx_guide_step_4_title),
            message = mokoString(SharedRes.strings.gpx_guide_step_4_message),
            leading = { GuideStepBadge(number = 4) },
        )
    }
}

@Composable
private fun RecommendationsRow(onOpenUrl: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Small),
    ) {
        HikeRecommendation.entries.forEach { recommendation ->
            HikeRecommendationCard(
                recommendation = recommendation,
                onClick = { onOpenUrl(recommendation.baseUrl) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview
@Composable
private fun GpxGuideScreenPreview() {
    HuKiTheme {
        GpxGuideContent(onBack = {})
    }
}
