package hu.mostoha.mobile.kmp.huki.ui.features.locationiq

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.openUrl
import hu.mostoha.mobile.kmp.huki.util.resolveMoko

@Composable
fun LocationIqScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    LocationIqContent(
        onBack = onBack,
        onVisitWebsite = { context.openUrl(context.resolveMoko(SharedRes.strings.menu_location_iq_url)) },
    )
}

@Composable
private fun LocationIqContent(onBack: () -> Unit, onVisitWebsite: () -> Unit) {
    val backgroundColor = mokoColor(SharedRes.colors.locationIqBackground)
    val accentColor = mokoColor(SharedRes.colors.locationIqRed)
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag(TestTags.LOCATION_IQ_SCREEN_ROOT),
        containerColor = backgroundColor,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = Dimens.ExtraLarge),
        ) {
            IconButton(
                modifier = Modifier
                    .padding(start = Dimens.Small, top = Dimens.Small)
                    .testTag(TestTags.LOCATION_IQ_BACK_BUTTON),
                onClick = onBack,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_back),
                    contentDescription = mokoString(SharedRes.strings.location_iq_a11y_back),
                )
            }
            LocationIqHero()
            SectionHeader(text = mokoString(SharedRes.strings.location_iq_section_how))
            UsageCard(
                iconResId = R.drawable.ic_search,
                accentColor = accentColor,
                title = mokoString(SharedRes.strings.location_iq_card_autocomplete_title),
                description = mokoString(SharedRes.strings.location_iq_card_autocomplete_description),
            )
            Spacer(modifier = Modifier.height(Dimens.MediumLarge))
            UsageCard(
                iconResId = R.drawable.ic_place,
                accentColor = accentColor,
                title = mokoString(SharedRes.strings.location_iq_card_geocoding_title),
                description = mokoString(SharedRes.strings.location_iq_card_geocoding_description),
            )
            SectionHeader(text = mokoString(SharedRes.strings.location_iq_section_offers))
            OffersRow(accentColor = accentColor)
            Spacer(modifier = Modifier.height(Dimens.ExtraLarge))
            VisitWebsiteButton(onClick = onVisitWebsite)
        }
    }
}

@Composable
private fun LocationIqHero() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.Large, vertical = Dimens.Medium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = mokoString(SharedRes.strings.location_iq_headline),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Image(
            painter = painterResource(SharedRes.images.ic_location_iq_logo.drawableResId),
            contentDescription = mokoString(SharedRes.strings.location_iq_a11y_logo),
            modifier = Modifier
                .padding(top = Dimens.Small)
                .height(Dimens.Huge),
        )
        Text(
            text = mokoString(SharedRes.strings.location_iq_intro),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Dimens.Large),
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = Dimens.Large,
                end = Dimens.Large,
                top = 20.dp,
                bottom = Dimens.Medium,
            ),
    )
}

@Composable
private fun UsageCard(
    iconResId: Int,
    accentColor: Color,
    title: String,
    description: String,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.Large),
        shape = RoundedCornerShape(Dimens.Large),
        color = mokoColor(SharedRes.colors.locationIqCard),
        shadowElevation = 0.5.dp,
    ) {
        Row(
            modifier = Modifier.padding(Dimens.Large),
        ) {
            IconChip(iconResId = iconResId, accentColor = accentColor)
            Column(
                modifier = Modifier
                    .padding(start = Dimens.Large)
                    .weight(1f),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Dimens.Small),
                )
            }
        }
    }
}

@Composable
private fun IconChip(iconResId: Int, accentColor: Color) {
    Box(
        modifier = Modifier
            .size(Dimens.IconContainer)
            .background(
                color = mokoColor(SharedRes.colors.locationIqContainer),
                shape = RoundedCornerShape(Dimens.MediumLarge),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(iconResId),
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(Dimens.IconSmall),
        )
    }
}

@Composable
private fun OffersRow(accentColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.Large),
        horizontalArrangement = Arrangement.spacedBy(Dimens.MediumLarge),
    ) {
        OfferCard(
            iconResId = R.drawable.ic_place,
            accentColor = accentColor,
            label = mokoString(SharedRes.strings.location_iq_offer_geocoding),
            modifier = Modifier.weight(1f),
        )
        OfferCard(
            iconResId = R.drawable.ic_maps,
            accentColor = accentColor,
            label = mokoString(SharedRes.strings.location_iq_offer_maps),
            modifier = Modifier.weight(1f),
        )
        OfferCard(
            iconResId = R.drawable.ic_route,
            accentColor = accentColor,
            label = mokoString(SharedRes.strings.location_iq_offer_routing),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun OfferCard(
    iconResId: Int,
    accentColor: Color,
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Dimens.Large),
        color = mokoColor(SharedRes.colors.locationIqCard),
        shadowElevation = 0.5.dp,
    ) {
        Column(
            modifier = Modifier.padding(vertical = Dimens.Large),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IconChip(iconResId = iconResId, accentColor = accentColor)
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = Dimens.Medium),
            )
        }
    }
}

@Composable
private fun VisitWebsiteButton(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(percent = 50))
                .clickable(onClick = onClick)
                .testTag(TestTags.LOCATION_IQ_VISIT_WEBSITE),
            shape = RoundedCornerShape(percent = 50),
            color = mokoColor(SharedRes.colors.locationIqButton),
            shadowElevation = Dimens.ExtraSmall,
        ) {
            Row(
                modifier = Modifier.padding(vertical = Dimens.MediumLarge),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = mokoString(SharedRes.strings.location_iq_visit_website),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}

@Preview
@Composable
private fun LocationIqContentPreview() {
    HuKiTheme {
        LocationIqContent(onBack = {}, onVisitWebsite = {})
    }
}
