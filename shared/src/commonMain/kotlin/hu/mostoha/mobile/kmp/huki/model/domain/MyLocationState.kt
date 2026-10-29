package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.permissions.PermissionState

data class MyLocationState(
    val permissionState: PermissionState = PermissionState.NotDetermined,
    val myLocationStatus: MyLocationStatus = MyLocationStatus.NotAvailable,
    val hasLocationFix: Boolean = false,
    val altitude: Int? = null,
) {
    companion object {
        val Default = MyLocationState()
    }
}
