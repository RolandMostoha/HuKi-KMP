package hu.mostoha.mobile.kmp.huki

import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.StringDesc

expect class Strings {
    fun get(id: StringResource, args: List<Any>): String

    fun get(desc: StringDesc): String
}
