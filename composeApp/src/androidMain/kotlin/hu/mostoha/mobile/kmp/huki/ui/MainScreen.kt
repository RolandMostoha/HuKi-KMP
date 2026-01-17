package hu.mostoha.mobile.kmp.huki.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.main.MainUiEffects
import hu.mostoha.mobile.kmp.huki.features.main.MainUiEvents
import hu.mostoha.mobile.kmp.huki.features.main.MainUiState
import hu.mostoha.mobile.kmp.huki.features.main.MainViewModel
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.ui.components.mokoString
import hu.mostoha.mobile.kmp.huki.ui.features.map.MapContent
import hu.mostoha.mobile.kmp.huki.util.TestTags
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreen(viewModel: MainViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MainContent(
        uiState = uiState,
        uiEffect = viewModel.uiEffect,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun MainContent(
    uiState: MainUiState,
    uiEffect: Flow<MainUiEffects>,
    onEvent: (MainUiEvents) -> Unit,
) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .fillMaxSize(),
    ) {
        MapContent(
            mapUiState = uiState.mapUiState,
            uiEffect = uiEffect,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            FloatingActionButton(
                modifier = Modifier
                    .testTag(TestTags.MAIN_MY_LOCATION_BUTTON)
                    .align(Alignment.BottomEnd)
                    .padding(Dimens.Default),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = { onEvent(MainUiEvents.MyLocationClicked) },
            ) {
                Icon(
                    imageVector = Icons.Filled.MyLocation,
                    contentDescription = mokoString(SharedRes.strings.main_my_location_accessibility),
                )
            }
        }
    }
}

@Preview
@Composable
private fun MainContentPreview() {
    MainContent(
        uiState = MainUiState(),
        uiEffect = emptyFlow(),
        onEvent = {},
    )
}
