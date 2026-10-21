package hu.mostoha.mobile.kmp.huki.repository

import dev.icerock.moko.resources.FileResource

interface BundledFileReader {
    fun readText(file: FileResource): String

    /**
     * The bundled file's name without its extension.
     */
    fun baseName(file: FileResource): String
}
