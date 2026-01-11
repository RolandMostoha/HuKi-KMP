package hu.mostoha.mobile.kmp.huki

import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc

actual class Strings {
    actual fun get(id: StringResource, args: List<Any>): String =
        if (args.isEmpty()) {
            StringDesc.Resource(id).localized()
        } else {
            StringDesc.ResourceFormatted(id, args).localized()
        }
}
