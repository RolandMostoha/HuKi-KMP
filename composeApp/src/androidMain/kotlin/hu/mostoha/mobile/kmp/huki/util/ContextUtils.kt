package hu.mostoha.mobile.kmp.huki.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.MailTo
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import co.touchlab.kermit.Logger
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import java.io.File

fun Context.navigateToDirections(location: Location) {
    val uri = "https://www.google.com/maps/dir/?api=1&destination=${location.latitude},${location.longitude}".toUri()
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Logger.w(throwable = e) { "No activity found to open directions to: $location" }
    }
}

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

fun Context.shareGpxFile(filePath: String, fileName: String) {
    val file = File(filePath)
    if (!file.exists()) {
        Logger.w { "No GPX file to share at: $filePath" }
        return
    }
    // The sandbox lives in internal storage, so the receiver needs a granted content:// URI.
    val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = GPX_MIME_TYPE
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TITLE, fileName)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(intent, fileName).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        startActivity(chooser)
    } catch (e: ActivityNotFoundException) {
        Logger.w(throwable = e) { "No activity found to share GPX file: $fileName" }
    }
}

private const val GPX_MIME_TYPE = "application/gpx+xml"
