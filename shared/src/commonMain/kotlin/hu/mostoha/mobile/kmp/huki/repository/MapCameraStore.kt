package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.CameraPosition

/**
 * Stores the current map camera position, so it can be read by other screens.
 */
interface MapCameraStore {

    val cameraPosition: CameraPosition

    fun update(cameraPosition: CameraPosition)
}
