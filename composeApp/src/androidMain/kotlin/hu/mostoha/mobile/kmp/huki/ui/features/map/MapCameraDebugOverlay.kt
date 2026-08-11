package hu.mostoha.mobile.kmp.huki.ui.features.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapbox.maps.CameraState
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.kmp.huki.model.domain.CameraPosition
import hu.mostoha.mobile.kmp.huki.util.CameraTargetParser
import hu.mostoha.mobile.kmp.huki.util.TestTags
import java.util.Locale

@Composable
fun MapCameraDebugPanel(
    enabled: Boolean,
    visible: Boolean,
    cameraState: CameraState?,
    onMoveCamera: (CameraPosition) -> Unit,
    onOpen: () -> Unit,
    onClose: () -> Unit,
) {
    if (!enabled) {
        return
    }
    if (visible) {
        MapCameraDebugOverlay(
            cameraState = cameraState,
            onMoveCamera = onMoveCamera,
            onClose = onClose,
        )
    } else {
        MapCameraDebugOpenArea(onOpen = onOpen)
    }
}

@Composable
private fun MapCameraDebugOpenArea(onOpen: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        // DEBUG only: opens the debug camera panel
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 16.dp)
                .size(width = 200.dp, height = 32.dp)
                .testTag(TestTags.MAP_DEBUG_CAMERA_PANEL)
                .clickable(onClick = onOpen),
        )
    }
}

@Composable
fun MapCameraDebugOverlay(
    cameraState: CameraState?,
    onMoveCamera: (CameraPosition) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(width = 1.dp, height = 24.dp).background(Color.Red))
            Box(Modifier.size(width = 24.dp, height = 1.dp).background(Color.Red))
        }
        CameraTargetInput(
            cameraState = cameraState,
            onMoveCamera = onMoveCamera,
            onClose = onClose,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 8.dp),
        )
    }
}

@Composable
private fun CameraTargetInput(
    cameraState: CameraState?,
    onMoveCamera: (CameraPosition) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    var input by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    // Mirror the live camera into the field unless the user is editing, so the value stays copyable.
    LaunchedEffect(cameraState, isEditing) {
        if (!isEditing && cameraState != null) {
            input = cameraState.toCameraTargetInput()
        }
    }
    // Clearing focus flips isEditing back off, so the field resumes mirroring the live camera.
    val moveCamera = {
        CameraTargetParser.parse(input)?.let {
            onMoveCamera(it)
            focusManager.clearFocus()
        }
    }
    Row(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            singleLine = true,
            textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
            placeholder = { Text("lat,lon,zoom", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = { moveCamera() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                cursorColor = Color.White,
            ),
            modifier = Modifier
                .width(200.dp)
                .onFocusChanged { isEditing = it.isFocused },
        )
        FilledIconButton(
            onClick = { moveCamera() },
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White),
            modifier = Modifier.padding(start = 4.dp),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_down),
                contentDescription = null,
                tint = Color.Black,
            )
        }
        FilledIconButton(
            onClick = onClose,
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White),
            modifier = Modifier.padding(start = 4.dp),
        ) {
            Text("✕", color = Color.Black, fontSize = 16.sp)
        }
    }
}

private fun CameraState.toCameraTargetInput(): String =
    String.format(Locale.US, "%.5f,%.5f,%.2f", center.latitude(), center.longitude(), zoom)

@Preview
@Composable
private fun MapCameraDebugOverlayPreview() {
    MapCameraDebugOverlay(
        cameraState = CameraState(
            com.mapbox.geojson.Point.fromLngLat(18.9139, 47.7167),
            com.mapbox.maps.EdgeInsets(0.0, 0.0, 0.0, 0.0),
            14.5,
            0.0,
            0.0,
        ),
        onMoveCamera = {},
        onClose = {},
    )
}
