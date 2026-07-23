package hu.mostoha.mobile.kmp.huki.ui.features.trailsymbolsguide

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.trailsymbolsguide.TrailSymbolsGuideViewModel
import hu.mostoha.mobile.kmp.huki.model.domain.TrailSymbol
import hu.mostoha.mobile.kmp.huki.model.domain.TrailSymbolSection
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.theme.dividerColor
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TrailSymbolsGuideScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    koinViewModel<TrailSymbolsGuideViewModel>()
    val screenColor = MaterialTheme.colorScheme.surfaceVariant
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .testTag(TestTags.TRAIL_SYMBOLS_GUIDE_SCREEN_ROOT),
        containerColor = screenColor,
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(text = mokoString(SharedRes.strings.trail_symbols_guide_title)) },
                navigationIcon = {
                    IconButton(
                        modifier = Modifier.testTag(TestTags.TRAIL_SYMBOLS_GUIDE_BACK_BUTTON),
                        onClick = onBack,
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_back),
                            contentDescription = mokoString(SharedRes.strings.trail_symbols_guide_a11y_back),
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
            verticalArrangement = Arrangement.spacedBy(Dimens.ExtraLarge),
        ) {
            SymbolSection(
                title = mokoString(SharedRes.strings.trail_symbols_main_section_title),
                symbols = TrailSymbol.entries.filter { it.section == TrailSymbolSection.MAIN },
            )
            SymbolSection(
                title = mokoString(SharedRes.strings.trail_symbols_branch_section_title),
                symbols = TrailSymbol.entries.filter { it.section == TrailSymbolSection.BRANCH },
            )
        }
    }
}

@Composable
private fun SymbolSection(title: String, symbols: List<TrailSymbol>) {
    Column(
        modifier = Modifier.padding(horizontal = Dimens.Large),
        verticalArrangement = Arrangement.spacedBy(Dimens.Small),
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.8.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Dimens.ExtraSmall),
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
                symbols.forEachIndexed { index, symbol ->
                    if (index > 0) {
                        SymbolDivider()
                    }
                    SymbolRow(
                        iconRes = symbol.iconRes.drawableResId,
                        title = mokoString(symbol.title),
                        description = mokoString(symbol.descriptionRes),
                    )
                }
            }
        }
    }
}

@Composable
private fun SymbolDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 84.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.dividerColor,
    )
}

@Composable
private fun SymbolRow(iconRes: Int, title: String, description: String) {
    Row(
        modifier = Modifier.padding(Dimens.MediumLarge),
        horizontalArrangement = Arrangement.spacedBy(Dimens.MediumLarge),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(52.dp)
                .height(44.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall)) {
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
            )
        }
    }
}

@Preview
@Composable
private fun TrailSymbolsGuideScreenPreview() {
    HuKiTheme {
        TrailSymbolsGuideScreen(onBack = {})
    }
}
