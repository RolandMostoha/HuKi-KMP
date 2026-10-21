package hu.mostoha.mobile.kmp.huki.repository

import android.content.Context
import dev.icerock.moko.resources.FileResource

class AndroidBundledFileReader(private val context: Context) : BundledFileReader {

    override fun readText(file: FileResource): String = file.readText(context)

    /**
     * moko names raw resources `<name>_<extension>`.
     */
    override fun baseName(file: FileResource): String =
        context.resources.getResourceEntryName(file.rawResId).substringBeforeLast('_')
}
