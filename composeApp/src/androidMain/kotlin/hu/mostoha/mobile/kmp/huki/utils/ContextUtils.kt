package hu.mostoha.mobile.kmp.huki.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS

fun Context.navigateToAppSettings() {
    val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
    }
    startActivity(intent)
}
