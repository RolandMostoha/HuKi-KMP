package hu.mostoha.mobile.kmp.huki.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.MailTo
import android.net.Uri
import android.provider.Settings
import androidx.core.net.toUri
import co.touchlab.kermit.Logger

fun Context.navigateToAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    this.startActivity(intent)
}

fun Context.openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Logger.w(throwable = e) { "No activity found to open URL: $url" }
    }
}

fun Context.sendEmail(email: String, subject: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = MailTo.MAILTO_SCHEME.toUri()
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, subject)
    }
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Logger.w(throwable = e) { "No activity found to send email to: $email" }
    }
}
