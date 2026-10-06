package hu.mostoha.mobile.kmp.huki.repository

import dev.icerock.moko.resources.FileResource

fun interface BundledFileReader {
    fun readText(file: FileResource): String
}
