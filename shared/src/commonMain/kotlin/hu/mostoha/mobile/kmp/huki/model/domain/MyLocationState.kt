package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.permissions.PermissionState

data class MyLocationState(
    val permissionState: PermissionState = PermissionState.NotDetermined,
    val myLocationStatus: MyLocationStatus = MyLocationStatus.NotAvailable,
) {
    companion object {
        val Default = MyLocationState()
    }
}
