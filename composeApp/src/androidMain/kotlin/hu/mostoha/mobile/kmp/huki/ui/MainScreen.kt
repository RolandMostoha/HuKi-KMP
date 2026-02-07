package hu.mostoha.mobile.kmp.huki.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.permissions.compose.BindEffect
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.main.MainUiEffects
import hu.mostoha.mobile.kmp.huki.features.main.MainUiEvents
import hu.mostoha.mobile.kmp.huki.features.main.MainUiState
import hu.mostoha.mobile.kmp.huki.features.main.MainViewModel
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationStatus
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

    BindEffect(viewModel.permissionsController)

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
    val myLocationState = uiState.myLocationState

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .fillMaxSize(),
    ) {
        MapContent(
            mapUiState = uiState.mapUiState,
            uiEffect = uiEffect,
            onEvent = onEvent,
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
                containerColor = if (myLocationState.myLocationStatus == MyLocationStatus.FollowingLiveCompass) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.background
                },
                contentColor = when (myLocationState.myLocationStatus) {
                    MyLocationStatus.NotAvailable, MyLocationStatus.Default -> MaterialTheme.colorScheme.onBackground
                    MyLocationStatus.Following -> Color.Unspecified
                    MyLocationStatus.FollowingLiveCompass -> MaterialTheme.colorScheme.onPrimary
                },
                shape = CircleShape,
                onClick = { onEvent(MainUiEvents.MyLocationClicked) },
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(
                        when (myLocationState.myLocationStatus) {
                            MyLocationStatus.Default -> R.drawable.ic_fab_my_location_default
                            MyLocationStatus.Following -> R.drawable.ic_fab_my_location_following
                            MyLocationStatus.FollowingLiveCompass -> R.drawable.ic_fab_my_location_live_compass
                            MyLocationStatus.NotAvailable -> R.drawable.ic_fab_my_location_default
                        },
                    ),
                    contentDescription = mokoString(
                        when (myLocationState.myLocationStatus) {
                            MyLocationStatus.NotAvailable -> SharedRes.strings.my_location_a11y_not_available
                            MyLocationStatus.Default -> SharedRes.strings.my_location_a11y_default
                            MyLocationStatus.Following -> SharedRes.strings.my_location_a11y_following
                            MyLocationStatus.FollowingLiveCompass -> SharedRes.strings.my_location_a11y_live_compass
                        },
                    ),
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
