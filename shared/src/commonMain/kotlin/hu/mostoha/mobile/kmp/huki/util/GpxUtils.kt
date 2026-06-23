package hu.mostoha.mobile.kmp.huki.util

private const val FNV_64_OFFSET_BASIS = 0xCBF29CE484222325UL
private const val FNV_64_PRIME = 0x100000001B3UL
private const val HEX_RADIX = 16
private const val TRACK_ID_LENGTH = 16

/**
 * Stable content hash (FNV-1a, 64-bit) used as a GPX track identifier.
 * Derived purely from file bytes, so it survives file renames.
 */
fun ByteArray.toGpxTrackId(): String {
    var hash = FNV_64_OFFSET_BASIS
    for (byte in this) {
        hash = hash xor byte.toUByte().toULong()
        hash *= FNV_64_PRIME
    }
    return hash.toString(HEX_RADIX).padStart(TRACK_ID_LENGTH, '0')
}
