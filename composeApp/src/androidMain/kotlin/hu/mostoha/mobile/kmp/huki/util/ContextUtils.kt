package hu.mostoha.mobile.kmp.huki.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

fun Context.navigateToAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    this.startActivity(intent)
}
