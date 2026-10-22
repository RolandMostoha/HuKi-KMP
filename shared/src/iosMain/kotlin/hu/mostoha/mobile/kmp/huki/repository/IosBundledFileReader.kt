package hu.mostoha.mobile.kmp.huki.repository

import dev.icerock.moko.resources.FileResource

class IosBundledFileReader : BundledFileReader {

    override fun readText(file: FileResource): String = file.readText()

    /**
     * Normalized the way moko names Android raw resources, so both platforms save the same file name.
     */
    override fun baseName(file: FileResource): String = file.fileName.replace('-', '_').replace('.', '_').lowercase()
}
