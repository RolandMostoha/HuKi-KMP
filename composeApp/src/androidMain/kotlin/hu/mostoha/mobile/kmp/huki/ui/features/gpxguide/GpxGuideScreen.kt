package hu.mostoha.mobile.kmp.huki.ui.features.gpxguide

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.gpxguide.GpxGuideViewModel
import hu.mostoha.mobile.kmp.huki.model.domain.HikeRecommendation
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.theme.dividerColor
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.openUrl
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GpxGuideScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    koinViewModel<GpxGuideViewModel>()
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
    Column(
        modifier = Modifier
            .padding(horizontal = Dimens.Large)
            .padding(top = Dimens.ExtraLarge),
        verticalArrangement = Arrangement.spacedBy(Dimens.Small),
    ) {
        Text(
            text = mokoString(SharedRes.strings.gpx_guide_steps_section).uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.8.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Dimens.ExtraSmall, bottom = Dimens.ExtraSmall),
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(Dimens.Large),
                ),
            shape = RoundedCornerShape(Dimens.Large),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column {
                Step(
                    number = 1,
                    title = mokoString(SharedRes.strings.gpx_guide_step_1_title),
                    message = mokoString(SharedRes.strings.gpx_guide_step_1_message),
                ) {
                    RecommendationsRow(
                        onOpenUrl = onOpenUrl,
                        modifier = Modifier.padding(top = Dimens.Small, end = Dimens.MediumLarge),
                    )
                }
                StepDivider()
                Step(
                    number = 2,
                    title = mokoString(SharedRes.strings.gpx_guide_step_2_title),
                    message = mokoString(SharedRes.strings.gpx_guide_step_2_message),
                )
                StepDivider()
                Step(
                    number = 3,
                    title = mokoString(SharedRes.strings.gpx_guide_step_3_title),
                    message = mokoString(SharedRes.strings.gpx_guide_step_3_message),
                )
                StepDivider()
                Step(
                    number = 4,
                    title = mokoString(SharedRes.strings.gpx_guide_step_4_title),
                    message = mokoString(SharedRes.strings.gpx_guide_step_4_message),
                )
            }
        }
    }
}

@Composable
private fun StepDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 50.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.dividerColor,
    )
}

@Composable
private fun RecommendationsRow(onOpenUrl: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Small),
    ) {
        HikeRecommendation.entries.forEach { recommendation ->
            RecommendationCard(
                recommendation = recommendation,
                onOpenUrl = onOpenUrl,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun RecommendationCard(
    recommendation: HikeRecommendation,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onOpenUrl(recommendation.baseUrl) }
            .padding(vertical = Dimens.MediumLarge, horizontal = Dimens.ExtraSmall),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.Small),
    ) {
        Image(
            painter = painterResource(recommendation.iconRes.drawableResId),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
        )
        Text(
            text = mokoString(recommendation.title),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun Step(
    number: Int,
    title: String,
    message: String,
    content: @Composable () -> Unit = {},
) {
    Row(
        modifier = Modifier.padding(Dimens.MediumLarge),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Medium),
        verticalAlignment = Alignment.Top,
    ) {
        StepBadge(number)
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            content()
        }
    }
}

@Composable
private fun StepBadge(number: Int) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(mokoColor(SharedRes.colors.primary)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = mokoColor(SharedRes.colors.onPrimary),
        )
    }
}

@Preview
@Composable
private fun GpxGuideScreenPreview() {
    HuKiTheme {
        GpxGuideScreen(onBack = {})
    }
}
