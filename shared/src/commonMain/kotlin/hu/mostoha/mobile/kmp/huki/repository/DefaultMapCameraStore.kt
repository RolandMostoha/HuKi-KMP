package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.CameraPosition
import hu.mostoha.mobile.kmp.huki.util.MapConstants
import kotlinx.coroutines.flow.MutableStateFlow

class DefaultMapCameraStore : MapCameraStore {

    private val currentCameraPosition = MutableStateFlow(MapConstants.HUNGARY_CAMERA_POSITION)

    override val cameraPosition: CameraPosition
        get() = currentCameraPosition.value

    override fun update(cameraPosition: CameraPosition) {
        currentCameraPosition.value = cameraPosition
    }
}
